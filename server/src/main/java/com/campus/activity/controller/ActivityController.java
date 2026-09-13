package com.campus.activity.controller;

import com.campus.activity.common.Result;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.ActivitySaveRequest;
import com.campus.activity.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 活动发布与管理接口（F3/F4）。
 * userId / role 由 JwtInterceptor 校验后注入请求属性（见 JwtInterceptor）。
 * 公开浏览接口（GET 列表/详情）不在本模块范围。
 */
@RestController
@RequestMapping("/api/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;

    /** F5 公开活动列表（无需登录），stage: all/open/ongoing/finished，默认 open */
    @GetMapping
    public Result<List<ActivityResponse>> list(@RequestParam(required = false) String stage) {
        return Result.ok(activityService.listPublic(stage));
    }

    /** F6 公开活动详情（无需登录，已取消活动可查看） */
    @GetMapping("/{id}")
    public Result<ActivityResponse> detail(@PathVariable Long id) {
        return Result.ok(activityService.getPublic(id));
    }

    /** F3 教师发布活动 */
    @PostMapping
    public Result<ActivityResponse> create(@Valid @RequestBody ActivitySaveRequest req,
                                           @RequestAttribute Long userId,
                                           @RequestAttribute String role) {
        return Result.ok(activityService.create(req, userId, role));
    }

    /** F4 教师查看自己发布的活动 */
    @GetMapping("/mine")
    public Result<List<ActivityResponse>> listMine(@RequestAttribute Long userId,
                                                   @RequestAttribute String role) {
        return Result.ok(activityService.listMine(userId, role));
    }

    /** F4 教师修改自己的活动 */
    @PutMapping("/{id}")
    public Result<ActivityResponse> update(@PathVariable Long id,
                                           @Valid @RequestBody ActivitySaveRequest req,
                                           @RequestAttribute Long userId,
                                           @RequestAttribute String role) {
        return Result.ok(activityService.update(id, req, userId, role));
    }

    /** F4 教师取消自己的活动（软取消，不物理删除） */
    @DeleteMapping("/{id}")
    public Result<Void> cancel(@PathVariable Long id,
                               @RequestAttribute Long userId,
                               @RequestAttribute String role) {
        activityService.cancel(id, userId, role);
        return Result.ok();
    }
}
