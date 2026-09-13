package com.campus.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.activity.common.BizException;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.MyRegistrationResponse;
import com.campus.activity.dto.RegistrationResponse;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Registration;
import com.campus.activity.entity.User;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.RegistrationMapper;
import com.campus.activity.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报名业务（F7/F8/F9 + 我的报名）。
 * 规则在 Service 层统一判定（顺序不可随意调换）：
 * 角色 → 活动存在 → 已取消(R6) → 截止(R4) → 满员(R3) → 重复(R2)；UNIQUE 约束为最终兜底。
 */
@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final RegistrationMapper registrationMapper;
    private final ActivityMapper activityMapper;
    private final UserMapper userMapper;

    /** F7 学生报名活动 */
    public void signup(Long activityId, Long userId, String role) {
        requireStudent(role);
        Activity activity = requireActivity(activityId);
        LocalDateTime now = LocalDateTime.now();

        if ("CANCELLED".equals(activity.getStatus())) {
            throw new BizException("活动已取消，无法报名");
        }
        if (!now.isBefore(activity.getSignupDeadline())) {
            throw new BizException("报名已截止");
        }
        long count = countByActivity(activityId);
        if (count >= activity.getMaxParticipants()) {
            throw new BizException("报名人数已满");
        }
        Long exists = registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getStudentId, userId));
        if (exists > 0) {
            throw new BizException("你已报名该活动");
        }

        Registration reg = new Registration();
        reg.setActivityId(activityId);
        reg.setStudentId(userId);
        try {
            registrationMapper.insert(reg);
        } catch (DuplicateKeyException e) {
            // 并发重复报名由数据库唯一约束兜底 (activity_id, student_id)
            throw new BizException("你已报名该活动");
        }
    }

    /** F8 学生取消报名（人工决策：仅活动开始前可取消） */
    public void cancel(Long activityId, Long userId, String role) {
        requireStudent(role);
        Activity activity = requireActivity(activityId);
        if (!LocalDateTime.now().isBefore(activity.getStartTime())) {
            throw new BizException("活动已开始，无法取消报名");
        }
        Registration reg = registrationMapper.selectOne(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .eq(Registration::getStudentId, userId));
        if (reg == null) {
            throw new BizException("你未报名该活动");
        }
        registrationMapper.deleteById(reg.getId());
    }

    /** F9 教师查看某活动的报名名单（R5：仅发布教师） */
    public List<RegistrationResponse> listRegistrations(Long activityId, Long userId) {
        Activity activity = requireActivity(activityId);
        if (!activity.getTeacherId().equals(userId)) {
            throw new BizException(403, "仅活动发布教师可查看报名名单");
        }
        List<Registration> regs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId)
                .orderByAsc(Registration::getRegisteredAt));
        if (regs.isEmpty()) {
            return List.of();
        }
        List<Long> studentIds = regs.stream().map(Registration::getStudentId).distinct().toList();
        Map<Long, String> nameMap = new HashMap<>();
        for (User u : userMapper.selectBatchIds(studentIds)) {
            nameMap.put(u.getId(), u.getUsername());
        }
        return regs.stream()
                .map(r -> new RegistrationResponse(r.getId(), r.getStudentId(),
                        nameMap.get(r.getStudentId()), r.getRegisteredAt()))
                .toList();
    }

    /** 我的报名（学生，第 11 个 API，人工批准） */
    public List<MyRegistrationResponse> mine(Long userId, String role) {
        requireStudent(role);
        List<Registration> regs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getStudentId, userId)
                .orderByDesc(Registration::getRegisteredAt));
        if (regs.isEmpty()) {
            return List.of();
        }
        List<Long> activityIds = regs.stream().map(Registration::getActivityId).distinct().toList();
        Map<Long, Activity> activityMap = new HashMap<>();
        for (Activity a : activityMapper.selectBatchIds(activityIds)) {
            activityMap.put(a.getId(), a);
        }
        Map<Long, Long> counts = countByActivityIds(activityIds);
        return regs.stream()
                .filter(r -> activityMap.containsKey(r.getActivityId()))
                .map(r -> new MyRegistrationResponse(r.getId(), r.getRegisteredAt(),
                        ActivityResponse.from(activityMap.get(r.getActivityId()),
                                counts.getOrDefault(r.getActivityId(), 0L))))
                .toList();
    }

    private void requireStudent(String role) {
        if (!"STUDENT".equals(role)) {
            throw new BizException(403, "仅学生可执行该操作");
        }
    }

    private Activity requireActivity(Long activityId) {
        Activity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        return activity;
    }

    private long countByActivity(Long activityId) {
        return registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId));
    }

    /** 批量统计报名人数（避免 N+1） */
    private Map<Long, Long> countByActivityIds(List<Long> activityIds) {
        Map<Long, Long> result = new HashMap<>();
        List<Registration> regs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getActivityId, activityIds));
        for (Registration r : regs) {
            result.merge(r.getActivityId(), 1L, Long::sum);
        }
        return result;
    }
}
