package com.lifemate.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/**
 * 雪花式 ID 生成器：64 位 = 时间戳差值(左移 32 位) | 当日自增序列。
 * 序列号用 Redis INCR 按天生成（key: icr:{业务}:{yyyy:MM:dd}），
 * 被 VoucherOrderServiceImpl 用来生成秒杀订单号。
 */
@Component
public class RedisIdWorker {
    //开始时间戳
    private static final long BEGIN_TIMESTAMP=1640995200L;

    //序列号位数
    private static final int COUNT_BITS=32;

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    public Long nextId(String keyPrefix){
        //1.生成时间戳
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timestamp = nowSecond - BEGIN_TIMESTAMP;
        //2.生成序列号
        //2.1.获取当前日期，精确到天
        String date = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        Long count = stringRedisTemplate.opsForValue().increment("icr:" + keyPrefix + ":" + date);


        //3.拼接并返回
        return timestamp<<COUNT_BITS | count;
    }

}
