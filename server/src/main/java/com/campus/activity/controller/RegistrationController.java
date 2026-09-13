package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.dto.MyRegistrationResponse;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.service.RegistrationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 报名相关接口（F7/F8/F9 + 我的报名）。
 * userId / role 由 JwtInterceptor 校验后注入；公开浏览见 ActivityController。
 */
@RestController
@RequiredArgsConstructor
public class RegistrationController {

    private final RegistrationService registrationService;

    /** F7 学生报名 */
    @PostMapping("/api/activities/{activityId}/signup")
    public Result<Void> signup(@PathVariable Long activityId,
                               @RequestAttribute Long userId,
                               @RequestAttribute String role) {
        registrationService.signup(activityId, userId, role);
        return Result.ok();
    }

    /** F8 学生取消报名（活动开始前） */
    @DeleteMapping("/api/activities/{activityId}/signup")
    public Result<Void> cancel(@PathVariable Long activityId,
                               @RequestAttribute Long userId,
                               @RequestAttribute String role) {
        registrationService.cancel(activityId, userId, role);
        return Result.ok();
    }

    /** F9 发布教师查看报名名单（R5） */
    @GetMapping("/api/activities/{activityId}/registrations")
    public Result<List<RegistrationResponse>> list(@PathVariable Long activityId,
                                                   @RequestAttribute Long userId) {
        return Result.ok(registrationService.listRegistrations(activityId, userId));
    }

    /** 学生查看我的报名 */
    @GetMapping("/api/registrations/mine")
    public Result<List<MyRegistrationResponse>> mine(@RequestAttribute Long userId,
                                                     @RequestAttribute String role) {
        return Result.ok(registrationService.mine(userId, role));
    }
}
