package com.test.wang.redislock.aspect;

import com.test.wang.redislock.api.JedisClient;
import com.test.wang.redislock.api.RedisLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import java.util.Collections;
import java.util.UUID;

/**
 * @author wangxi
 * @date 2019-09-02 17:24
 */
@Aspect
@Configuration
public class RedisLockAspect {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    JedisClient jedisClient;
    @Pointcut("@annotation(com.test.wang.redislock.api.RedisLock)")
    public void pointCut() {

    }

    @Around("pointCut()")
    public Object interceptRedisAspect(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取注解
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RedisLock redisLock = signature.getMethod().getAnnotation(RedisLock.class);

        //获取注解参数
        String key = redisLock.key();
        String value = UUID.randomUUID().toString();

        if(StringUtils.isEmpty(key)){
            throw new RuntimeException("key不能为空");
        }

        //阻塞式获取锁
        try {
            Boolean isGetLock = lockInBlock(key, value, redisLock.ttl(), redisLock.timeout());

            if(!isGetLock){
                logger.error("获取锁失败");
                throw new RuntimeException("获取锁失败");
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        try {
            //获取获取成功，执行业务逻辑
            return joinPoint.proceed();
        }finally {
            //释放锁，释放的锁必须是自己加的，保证原子性，使用lua脚本
            //try中包含return，finally依然会执行。
            unlock(key, value);
        }

    }

    /**
     * 阻塞式获取锁
     * @param key
     * @param value
     * @param ttl
     * @param timeout
     * @return
     * @throws InterruptedException
     */
    public boolean lockInBlock(String key, String value, long ttl, long timeout) throws InterruptedException {

        Long sleepTime = 10L;
        while (timeout >= 0){

            //参数三PX，则时间为单位毫秒
            Boolean result = jedisClient.set(key, value, "NX", "PX", ttl);

            if (result){
                return true;
            }

            timeout -= sleepTime ;
            Thread.sleep(sleepTime) ;
        }

        return false ;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value){
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 'fail' end";
        jedisClient.eval(script, Collections.singletonList(key), Collections.singletonList(value));
    }
}
