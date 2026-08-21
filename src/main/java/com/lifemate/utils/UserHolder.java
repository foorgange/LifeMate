package com.lifemate.utils;

import com.lifemate.dto.UserDTO;
import com.lifemate.entity.User;

/** 当前登录用户容器（ThreadLocal）：RefreshTokenInterceptor 写入，LoginInterceptor/业务代码读取。 */
public class UserHolder {
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();

    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}
