package com.lifemate.service;

import com.lifemate.dto.Result;
import com.lifemate.entity.VoucherOrder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 秒杀订单服务接口。
 */
public interface IVoucherOrderService extends IService<VoucherOrder> {

    /** 秒杀下单：Redis+Lua 判资格，通过后投递 RocketMQ 异步落单 */
    Result seckillVoucher(Long voucherId);

    /** 支付回调 */
    boolean payCallback(Long orderId);

    /** 超时关单 */
    boolean closeTimeoutOrder(Long orderId);
}
