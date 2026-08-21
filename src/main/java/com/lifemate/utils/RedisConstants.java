package com.lifemate.utils;

/**
 * Redis 键前缀与 TTL 常量。
 */
public class RedisConstants {

    /** 登录验证码 */
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    /** 登录令牌（有效期：分钟，登录与拦截器刷新共用） */
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 30L;

    /** 缓存空值 TTL（缓存穿透兜底） */
    public static final Long CACHE_NULL_TTL = 2L;

    /** 店铺缓存 */
    public static final String CACHE_SHOP_KEY = "cache:shop:";
    public static final Long CACHE_SHOP_TTL = 30L;

    /** 店铺缓存重建互斥锁 */
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;

    /** 秒杀库存 */
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";

    /** 笔记点赞 */
    public static final String BLOG_LIKED_KEY = "blog:liked:";

    /** 关注推送收件箱 */
    public static final String FEED_KEY = "feed:";

    /** 用户关注列表 */
    public static final String FOLLOW_KEY = "follows:";

    /** 店铺 GEO 坐标 */
    public static final String SHOP_GEO_KEY = "shop:geo:";

    /** 用户签到位图 */
    public static final String USER_SIGN_KEY = "sign:";

    /** 店铺分类 */
    public static final String SHOP_TYPE_KEY = "shop_type:";
}
