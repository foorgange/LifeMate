package com.lifemate.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifemate.dto.Result;
import com.lifemate.entity.ShopType;
import com.lifemate.mapper.ShopTypeMapper;
import com.lifemate.service.IShopTypeService;
import com.lifemate.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺分类服务：优先从 Redis List 缓存读取，未命中时查库并回填缓存。
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result querySort() {
        // 1. 优先从 Redis List 缓存读取
        List<String> shopTypeJson = stringRedisTemplate.opsForList().range(RedisConstants.SHOP_TYPE_KEY, 0, -1);
        if (shopTypeJson != null && !shopTypeJson.isEmpty()) {
            List<ShopType> shopTypes = shopTypeJson.stream()
                    .map(json -> JSONUtil.toBean(json, ShopType.class))
                    .collect(Collectors.toList());
            return Result.ok(shopTypes);
        }
        // 2. 未命中：查询数据库并按 sort 排序
        List<ShopType> shopTypes = query().orderByAsc("sort").list();
        if (shopTypes == null || shopTypes.isEmpty()) {
            return Result.fail("没有分类数据");
        }
        // 3. 回填缓存
        for (ShopType shopType : shopTypes) {
            stringRedisTemplate.opsForList().rightPush(RedisConstants.SHOP_TYPE_KEY, JSONUtil.toJsonStr(shopType));
        }
        return Result.ok(shopTypes);
    }
}
