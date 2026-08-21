package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.ShopType;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/** 商铺分类服务接口。实现见 ShopTypeServiceImpl。 */
public interface IShopTypeService extends IService<ShopType> {

    Result querySort();
}
