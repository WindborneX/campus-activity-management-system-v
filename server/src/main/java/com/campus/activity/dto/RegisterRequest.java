package com.campus.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 注册请求 */
@Data
public class RegisterRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度需在 3~50 之间")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 64, message = "密码长度需在 6~64 之间")
    private String password;

    /** 角色：STUDENT / TEACHER */
    @NotBlank(message = "角色不能为空")
    @Pattern(regexp = "STUDENT|TEACHER", message = "角色不合法")
    private String role;
}
