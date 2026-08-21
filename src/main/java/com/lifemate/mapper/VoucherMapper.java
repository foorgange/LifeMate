package com.lifemate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifemate.entity.Voucher;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/** tb_voucher 表数据访问接口；queryVoucherOfShop 的 SQL 见 resources/mapper/VoucherMapper.xml。 */
public interface VoucherMapper extends BaseMapper<Voucher> {

    List<Voucher> queryVoucherOfShop(@Param("shopId") Long shopId);
}
