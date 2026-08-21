package com.lifemate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lifemate.dto.LoginFormDTO;
import com.lifemate.dto.Result;
import com.lifemate.entity.User;

/**
 * 用户服务接口：验证码登录（Redis 存储令牌，不依赖 HttpSession）与签到。
 */
public interface IUserService extends IService<User> {

    /** 发送手机验证码 */
    Result sendCode(String phone);

    /** 验证码登录：成功返回 token */
    Result login(LoginFormDTO loginForm);

    /** 签到 */
    Result sign();

    /** 统计连续签到天数 */
    Result signCount();
}
