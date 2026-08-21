package com.lifemate.utils;

import lombok.Data;

import java.time.LocalDateTime;

/** 逻辑过期缓存的数据载体：data（业务数据）+ expireTime（逻辑过期时间）。配合 CacheClient.setWithLogicalExpire 使用。 */
@Data
public class RedisData {
    //逻辑过期时间
    private LocalDateTime expireTime;

    //组合
    private Object data;
}
