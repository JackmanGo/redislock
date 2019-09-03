package com.test.wang.redislock.testservice;

import com.test.wang.redislock.api.RedisLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 测试分布式锁的可用行
 * @author wangxi
 * @date 2019-09-02 21:03
 */
@Service
public class ServiceTest {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    @RedisLock(key = "testRedisLock")
    public void testRedisLock()  {

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.info("业务代码执行===>{}", System.currentTimeMillis());
    }
}
