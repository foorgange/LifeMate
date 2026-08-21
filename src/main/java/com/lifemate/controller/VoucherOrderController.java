package com.lifemate.controller;


import com.lifemate.annotation.RateLimiter;
import com.lifemate.dto.Result;
import com.lifemate.service.IVoucherOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/** 秒杀订单接口：秒杀下单（@RateLimiter 用户维度 1 秒 5 次）+ 支付回调。下单链路见 VoucherOrderServiceImpl。 */
@RestController
@RequestMapping("/voucher-order")
public class VoucherOrderController {
    @Resource
    private IVoucherOrderService voucherOrderService;
    @RateLimiter(key = "seckill", windowSeconds = 1, count = 5, dimension = "user")
    @PostMapping("/seckill/{id}")
    public Result seckillVoucher(@PathVariable("id") Long voucherId) {
        return voucherOrderService.seckillVoucher(voucherId);
    }

    @PostMapping("/pay/callback/{orderId}")
    public Result payCallback(@PathVariable("orderId") Long orderId) {
        boolean success = voucherOrderService.payCallback(orderId);
        return success ? Result.ok() : Result.fail("支付回调处理失败或订单状态已变更");
    }
}
