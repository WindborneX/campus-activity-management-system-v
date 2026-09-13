package com.campus.activity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 注册响应：不含任何密码信息 */
@Data
@AllArgsConstructor
public class RegisterResponse {

    private Long userId;
    private String username;
    private String role;
}
