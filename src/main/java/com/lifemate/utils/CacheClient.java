package com.lifemate.utils;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * 缓存工具：封装缓存写入、缓存穿透（空值缓存）与缓存击穿（逻辑过期）两种方案。
 */
@Slf4j
@Component
public class CacheClient {

    private final StringRedisTemplate stringRedisTemplate;

    /** 逻辑过期后用于后台重建缓存的线程池 */
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(10);

    public CacheClient(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /** 写入缓存（物理 TTL） */
    public void set(String key, Object value, Long time, TimeUnit unit) {
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(value), time, unit);
    }

    /** 写入缓存（逻辑过期：过期时间存在 value 里，不设物理 TTL） */
    public void setWithLogicalExpire(String key, Object value, Long time, TimeUnit unit) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plusSeconds(unit.toSeconds(time)));
        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(redisData));
    }

    /**
     * 缓存穿透方案：查不到数据时缓存空值（短 TTL），拦截对不存在数据的重复查询。
     */
    public <R, ID> R queryWithPassThrough(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 命中：反序列化返回
        if (StrUtil.isNotBlank(json)) {
            return JSONUtil.toBean(json, type);
        }
        // 3. 命中空值缓存（""）：说明数据库中没有该数据，直接返回 null
        if (json != null) {
            return null;
        }
        // 4. 未命中：查数据库
        R r = dbFallback.apply(id);
        if (r == null) {
            // 5. 数据库也没有：写入空值缓存，防止穿透
            stringRedisTemplate.opsForValue().set(key, "", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 6. 写入缓存并返回
        this.set(key, r, time, unit);
        return r;
    }

    /**
     * 缓存击穿方案（逻辑过期）：过期后先返回旧数据，抢到锁的线程在后台重建缓存。
     * 注意：缓存未命中（key 不存在）时直接返回 null，不查数据库——由调用方决定兜底策略。
     */
    public <R, ID> R queryWithLogicalExpire(
            String keyPrefix, ID id, Class<R> type, Function<ID, R> dbFallback, Long time, TimeUnit unit) {
        String key = keyPrefix + id;
        // 1. 查缓存
        String json = stringRedisTemplate.opsForValue().get(key);
        // 2. 缓存不存在：直接返回 null
        if (StrUtil.isBlank(json)) {
            return null;
        }
        // 3. 反序列化并读取逻辑过期时间
        RedisData redisData = JSONUtil.toBean(json, RedisData.class);
        R value = JSONUtil.toBean((JSONObject) redisData.getData(), type);
        LocalDateTime expireTime = redisData.getExpireTime();
        // 4. 未过期：直接返回
        if (expireTime.isAfter(LocalDateTime.now())) {
            return value;
        }
        // 5. 已过期：尝试获取互斥锁，抢到锁的线程在后台重建缓存
        String lockKey = RedisConstants.LOCK_SHOP_KEY + id;
        if (tryLock(lockKey)) {
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    R fresh = dbFallback.apply(id);
                    this.setWithLogicalExpire(key, fresh, time, unit);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    unlock(lockKey);
                }
            });
        }
        // 6. 无论是否抢到锁，都先返回旧数据（AP 取向：重可用、轻一致）
        return value;
    }

    /** 互斥锁：SETNX + 10 秒过期兜底（防止持锁线程异常退出导致死锁） */
    private boolean tryLock(String key) {
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(flag);
    }

    /** 释放互斥锁 */
    private void unlock(String key) {
        stringRedisTemplate.delete(key);
    }
}
