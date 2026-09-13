package com.campus.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.activity.common.BizException;
import com.campus.activity.common.JwtUtil;
import com.campus.activity.dto.LoginRequest;
import com.campus.activity.dto.LoginResponse;
import com.campus.activity.dto.RegisterRequest;
import com.campus.activity.dto.RegisterResponse;
import com.campus.activity.entity.User;
import com.campus.activity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** 注册登录业务（F1/F2，规则 R7） */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;

    /** 注册：用户名唯一校验 + BCrypt 加密存储（R7） */
    public RegisterResponse register(RegisterRequest req) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (count > 0) {
            throw new BizException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        user.setRole(req.getRole());
        try {
            userMapper.insert(user);
        } catch (DuplicateKeyException e) {
            // 并发注册同名时由数据库唯一约束兜底
            throw new BizException("用户名已存在");
        }
        return new RegisterResponse(user.getId(), user.getUsername(), user.getRole());
    }

    /** 登录：校验密码并签发 JWT；失败统一提示，不区分账号是否存在（防账号探测） */
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null || !passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BizException("用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRole());
    }
}
