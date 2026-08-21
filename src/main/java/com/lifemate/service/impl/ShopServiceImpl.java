package com.lifemate.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifemate.dto.Result;
import com.lifemate.entity.Shop;
import com.lifemate.mapper.ShopMapper;
import com.lifemate.service.IShopService;
import com.lifemate.utils.CacheClient;
import com.lifemate.utils.SystemConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lifemate.utils.RedisConstants.CACHE_SHOP_KEY;
import static com.lifemate.utils.RedisConstants.CACHE_SHOP_TTL;
import static com.lifemate.utils.RedisConstants.SHOP_GEO_KEY;

/**
 * 店铺服务：逻辑过期缓存（解决缓存击穿）、附近商铺 GEO 查询、更新后删除缓存并用
 * RocketMQ 补偿（解决缓存一致性）。
 */
@Slf4j
@Service
public class ShopServiceImpl extends ServiceImpl<ShopMapper, Shop> implements IShopService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private CacheClient cacheClient;

    @Resource
    private RocketMQTemplate rocketMQTemplate;

    @Override
    public Result queryById(Long id) {
        // 逻辑过期方案解决缓存击穿：过期后先返回旧数据，由后台线程重建缓存（见 CacheClient）
        Shop shop = cacheClient
                .queryWithLogicalExpire(CACHE_SHOP_KEY, id, Shop.class, this::getById, CACHE_SHOP_TTL, TimeUnit.MINUTES);
        if (shop == null) {
            return Result.fail("店铺不存在！");
        }
        return Result.ok(shop);
    }

    @Override
    @Transactional
    public Result update(Shop shop) {
        Long id = shop.getId();
        if (id == null) {
            return Result.fail("店铺id不能为空");
        }
        // 1. 先修改数据库
        updateById(shop);
        // 2. 再删除缓存；删除失败时发送 RocketMQ 补偿消息（由 CacheDeleteListener 重试删除）
        try {
            stringRedisTemplate.delete(CACHE_SHOP_KEY + shop.getId());
        } catch (Exception e) {
            log.error("删除缓存失败，发送 RocketMQ 补偿消息，shopId={}", shop.getId(), e);
            rocketMQTemplate.convertAndSend("cache-delete-topic", String.valueOf(shop.getId()));
        }
        return Result.ok();
    }

    @Override
    public Result queryShopByType(Integer typeId, Integer current, Double x, Double y) {
        // 1. 未携带坐标：直接分页查数据库
        if (x == null || y == null) {
            Page<Shop> page = query()
                    .eq("type_id", typeId)
                    .page(new Page<>(current, SystemConstants.DEFAULT_PAGE_SIZE));
            return Result.ok(page.getRecords());
        }
        // 2. 携带坐标：在 Redis GEO 中按距离查询（结果只含 shopId 与 distance）
        int from = (current - 1) * SystemConstants.DEFAULT_PAGE_SIZE;
        int end = current * SystemConstants.DEFAULT_PAGE_SIZE;
        GeoResults<RedisGeoCommands.GeoLocation<String>> results = stringRedisTemplate.opsForGeo()
                .search(
                        SHOP_GEO_KEY + typeId,
                        GeoReference.fromCoordinate(x, y),
                        new Distance(5000),
                        RedisGeoCommands.GeoSearchCommandArgs.newGeoSearchArgs().includeDistance().limit(end)
                );
        if (results == null || results.getContent().size() <= from) {
            return Result.ok(Collections.emptyList());
        }
        // 3. 截取当前页（跳过前 from 个），收集 shopId 与距离
        List<GeoResult<RedisGeoCommands.GeoLocation<String>>> list = results.getContent();
        List<Long> ids = new ArrayList<>(list.size());
        Map<String, Distance> distanceMap = new HashMap<>(list.size());
        list.stream().skip(from).forEach(result -> {
            String shopIdStr = result.getContent().getName();
            ids.add(Long.valueOf(shopIdStr));
            distanceMap.put(shopIdStr, result.getDistance());
        });
        // 4. 按 id 批量查询店铺，并保持 GEO 返回的顺序
        String idStr = StrUtil.join(",", ids);
        List<Shop> shops = query()
                .in("id", ids).last("order by field(id," + idStr + ")").list();
        for (Shop shop : shops) {
            shop.setDistance(distanceMap.get(shop.getId().toString()).getValue());
        }
        return Result.ok(shops);
    }
}
