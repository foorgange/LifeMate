package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.Shop;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiWei
 * @since 2021-12-22
 */
public interface IShopService extends IService<Shop> {
    Result queryById(Long id) throws InterruptedException;

    Result update(Shop shop);

    Result queryShopByType(Integer typeId, Integer current, Double x, Double y);
}
