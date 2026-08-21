package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.Voucher;
import com.baomidou.mybatisplus.extension.service.IService;

/** 优惠券服务接口：新增（含秒杀券）、按店铺查询。实现见 VoucherServiceImpl。 */
public interface IVoucherService extends IService<Voucher> {

    Result queryVoucherOfShop(Long shopId);

    void addSeckillVoucher(Voucher voucher);
}
