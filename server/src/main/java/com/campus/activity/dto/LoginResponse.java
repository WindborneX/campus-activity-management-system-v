package com.campus.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录响应：JWT 与用户信息 */
@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String role;
}
