package com.test.wang.redislock.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.List;

/**
 * @author wangxi
 * @date 2019-09-02 22:41
 */
@Component
public class JedisClient {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private JedisPool jedisPool;

    ThreadLocal<Jedis> threadConnection = new ThreadLocal();

    public Boolean set(String key, String value, String nx, String px, long ttl){

        logger.info("尝试获取锁===>{}", value);

        Jedis jedis = threadConnection.get();
        if(jedis == null) {
            jedis = jedisPool.getResource();
            threadConnection.set(jedis);
        }

        String result = jedis.set(key, value, nx, px, ttl);
        logger.info("获取锁===>{},结果===>{}", value, result);

        return "OK".equals(result);
    }

    public void eval(String script, List<String> keyList, List<String> valueList) {

        logger.info("释放锁===>{}", valueList.get(0));
        Object result = jedisPool.getResource().eval(script, keyList, valueList);
        logger.error("释放锁===>{},结果===>{}", valueList.get(0),  result);
    }
}
