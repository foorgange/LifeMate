package com.lifemate.dto;

import lombok.Builder;
import lombok.Data;

/** 登录表单：手机号 + 验证码（或密码）。 */
@Data
@Builder
public class LoginFormDTO {
    private String phone;
    private String code;
    private String password;
}
