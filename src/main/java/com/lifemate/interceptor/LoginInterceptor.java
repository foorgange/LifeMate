package com.lifemate.interceptor;

import com.lifemate.utils.UserHolder;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录校验拦截器：ThreadLocal 中没有用户（未登录）时返回 401。
 * 用户信息的恢复由前置的 RefreshTokenInterceptor 完成。
 */
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 判断 ThreadLocal 中是否有用户
        if (UserHolder.getUser() == null) {
            // 2. 未登录：设置 401 并拦截
            response.setStatus(401);
            return false;
        }
        return true;
    }
}
