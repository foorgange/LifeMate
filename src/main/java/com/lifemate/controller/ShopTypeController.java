package com.lifemate.controller;

import com.lifemate.dto.Result;
import com.lifemate.service.IShopTypeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 商铺分类接口：/shop-type/list 返回按 sort 排序的分类（Redis 缓存，未命中查库回填）。
 */
@RestController
@RequestMapping("/shop-type")
public class ShopTypeController {

    @Resource
    private IShopTypeService typeService;

    @GetMapping("/list")
    public Result queryTypeList() {
        return typeService.querySort();
    }
}
