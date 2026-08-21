-- 秒杀资格判断脚本（Redis 原子执行）：库存判断 + 一人一单 + 扣库存。
-- 返回码：0=成功 1=库存不足 2=重复下单 -1=库存未初始化。
-- 联动：VoucherOrderServiceImpl.seckillVoucher 调用；通过后由 RocketMQ 异步落单（SeckillVoucherListener）。
-- 1.参数列表
--1.1.优惠券id
local voucherId=ARGV[1]
--1.2.用户id
local userId=ARGV[2]
--1.3.订单id（由 Java 生成雪花 id，随 RocketMQ 消息传给消费者，本脚本不再使用）
local orderId=ARGV[3]

-- 2.数据key
--2.1.库存key
local stockKey='seckill:stock:' .. voucherId
--2.2.订单key
local orderKey='seckill:order:' .. voucherId

-- 3.脚本业务
--3.1.判断库存是否充足
local stock = tonumber(redis.call('get', stockKey))
if stock == nil then
    -- 库存 key 不存在（秒杀活动库存未初始化）
    return -1
end

if (stock<= 0) then
    --3.2.库存不足，返回1
    return 1
end
--3.3.判断用户是否下单
if(redis.call('sismember',orderKey,userId)==1) then
    --3.4.存在，说明重复下单，返回2
    return 2
end
-- 3.5.扣库存 incrby stockKey -1
redis.call('incrby',stockKey,-1)
-- 3.6.下单(保存)用户 sadd orderKey userId
redis.call('sadd',orderKey,userId)
-- 3.7.资格判断通过（库存已扣、一人一单已登记），返回 0 表示成功；
--     订单落库由 RocketMQ 异步完成（见 SeckillVoucherListener）
return 0
