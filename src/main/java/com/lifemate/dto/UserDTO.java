package com.lifemate.dto;

import lombok.Data;

/** 登录用户的轻量信息（存入 Redis Hash 与 ThreadLocal）。 */
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}
