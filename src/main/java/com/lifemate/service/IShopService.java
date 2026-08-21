package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 店铺服务接口。
 */
public interface IShopService extends IService<Shop> {

    Result queryById(Long id);

    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
