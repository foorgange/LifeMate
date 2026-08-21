package com.lifemate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lifemate.dto.LoginFormDTO;
import com.lifemate.dto.Result;
import com.lifemate.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author LiWei
 * @since 2021-12-22
 */
public interface IUserService extends IService<User> {

    Result sendCode(String phone, HttpSession session);

    Result login(LoginFormDTO loginForm, HttpSession session);

    Result sign();

    Result signCount();
}
