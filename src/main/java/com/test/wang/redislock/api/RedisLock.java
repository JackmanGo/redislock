package com.test.wang.redislock.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * redis锁的注解
 * @author wangxi
 * @date 2019-09-02 17:14
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RedisLock {


    /**
     * redis锁的key值
     * @return
     */
    String key();

    /**
     * 锁的超时时间，单位ms，默认10分钟
     */
    long ttl() default 600000L;

    /**
     * 阻塞式获取redis锁的最大等待时间，单位ms，默认30秒
     * @return
     */
    long timeout() default 30000;
}
