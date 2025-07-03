package com.t.bi.manager;

import com.t.bi.common.ErrorCode;
import com.t.bi.config.RedissonConfig;
import com.t.bi.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;


import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * redis limiter 限流器管理
 */
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonConfig redissonConfig;

    /**
     * @param key 区分不同的限流器，例如不同的id限流多少，或者某个地区限制多少dps
     * @return
     */
    public boolean doRateLimit(String key){
        RedissonClient redissonClient = redissonConfig.getRedissonClient();
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        boolean b = rateLimiter.tryAcquire(1);
        if (!b){
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
        return b;
    }
}
