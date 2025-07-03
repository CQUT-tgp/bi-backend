package com.t.bi.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    private RedisLimiterManager redisLimiterManager;
    @Test
    void doRateLimit() {
        redisLimiterManager.doRateLimit("test");
        for (int i = 0; i < 1; i++) {
            redisLimiterManager.doRateLimit("test");
            System.out.println("成功");
        }
    }

}