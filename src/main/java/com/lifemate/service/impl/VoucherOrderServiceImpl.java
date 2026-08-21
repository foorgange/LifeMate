package com.lifemate.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lifemate.dto.Result;
import com.lifemate.entity.VoucherOrder;
import com.lifemate.mapper.VoucherOrderMapper;
import com.lifemate.service.ISeckillVoucherService;
import com.lifemate.service.IVoucherOrderService;
import com.lifemate.utils.RedisIdWorker;
import com.lifemate.utils.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;

/**
 * 秒杀订单服务。
 *
 * <p>下单链路：Redis + Lua 原子判资格（库存充足 + 一人一单）→ RocketMQ 异步落单 →
 * 延迟消息超时关单。方案演进（DB 悲观锁 → DB 乐观锁 → Redis+Lua+MQ）详见 README。
 */
@Slf4j
@Service
public class VoucherOrderServiceImpl extends ServiceImpl<VoucherOrderMapper, VoucherOrder>
        implements IVoucherOrderService {

    /** seckill.lua 返回码：0=成功，1=库存不足，2=重复下单 */
    private static final long SECKILL_SUCCESS = 0L;
    private static final long SECKILL_STOCK_NOT_ENOUGH = 1L;
    private static final long SECKILL_DUPLICATE_ORDER = 2L;

    /** 秒杀资格判断脚本：原子完成库存判断、一人一单校验与 Redis 库存扣减 */
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private ISeckillVoucherService seckillVoucherService;
    @Resource
    private RocketMQTemplate rocketMQTemplate;
    @Resource
    private RedisIdWorker redisIdWorker;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result seckillVoucher(Long voucherId) {
        // 1. 获取用户 id 与预生成的雪花订单 id
        Long userId = UserHolder.getUser().getId();
        long orderId = redisIdWorker.nextId("order");

        // 2. 执行 Lua 脚本：在 Redis 内存中完成库存判断、一人一单校验与库存扣减
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT,
                Collections.emptyList(),
                voucherId.toString(), userId.toString(), String.valueOf(orderId)
        );

        // 3. 根据返回码判断是否具备购买资格
        if (result == null) {
            // Redis 执行异常：不能放行（放行等于跳过库存校验）
            log.error("秒杀 Lua 脚本执行失败 voucherId={} userId={}", voucherId, userId);
            return Result.fail("系统繁忙，请稍后再试");
        }
        if (result == SECKILL_STOCK_NOT_ENOUGH) {
            return Result.fail("库存不足");
        }
        if (result == SECKILL_DUPLICATE_ORDER) {
            return Result.fail("不能重复下单");
        }
        if (result != SECKILL_SUCCESS) {
            log.error("秒杀脚本返回未知状态码 result={} voucherId={}", result, voucherId);
            return Result.fail("秒杀失败，请稍后再试");
        }

        // 4. 组装订单并投递 RocketMQ 异步落单，接口立即返回订单号
        VoucherOrder order = new VoucherOrder();
        order.setId(orderId);
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        try {
            rocketMQTemplate.convertAndSend("seckill-order-topic", JSONUtil.toJsonStr(order));
        } catch (Exception e) {
            log.error("发送 RocketMQ 消息失败，订单ID: {}", orderId, e);
            throw new RuntimeException("发送消息失败");
        }
        return Result.ok(orderId);
    }

    /**
     * 支付回调：使用乐观锁确保只有"未支付"订单能流转为"已支付"。
     */
    @Transactional
    public boolean payCallback(Long orderId) {
        return update()
                .eq("id", orderId)
                .eq("status", 1)
                .set("status", 2)
                .set("pay_time", LocalDateTime.now())
                .update();
    }

    /**
     * 超时关单：使用乐观锁确保只有"未支付"订单能流转为"已取消"，
     * 关单成功后再释放被占用的库存。
     */
    @Transactional
    public boolean closeTimeoutOrder(Long orderId) {
        VoucherOrder order = getById(orderId);
        if (order == null || order.getStatus() == null || order.getStatus() != 1) {
            return false;
        }
        boolean updated = update()
                .eq("id", orderId)
                .eq("status", 1)
                .set("status", 4)
                .update();
        if (updated) {
            seckillVoucherService.update()
                    .setSql("stock = stock + 1")
                    .eq("voucher_id", order.getVoucherId())
                    .update();
        }
        return updated;
    }
}
