package com.campus.activity.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.activity.common.BizException;
import com.campus.activity.dto.ActivityResponse;
import com.campus.activity.dto.ActivitySaveRequest;
import com.campus.activity.entity.Activity;
import com.campus.activity.entity.Registration;
import com.campus.activity.mapper.ActivityMapper;
import com.campus.activity.mapper.RegistrationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 活动发布与管理业务（F3/F4），并提供公开浏览查询（F5/F6）。
 * 授权规则（R5）：角色（仅 TEACHER）+ 资源所有权（仅发布者本人可改/取消）。
 */
@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final RegistrationMapper registrationMapper;

    /** F3 教师创建活动 */
    public ActivityResponse create(ActivitySaveRequest req, Long userId, String role) {
        requireTeacher(role);
        validateTime(req, true);

        Activity activity = new Activity();
        activity.setTeacherId(userId);
        applyFields(activity, req);
        activity.setStatus("ACTIVE");
        activityMapper.insert(activity);
        return ActivityResponse.from(activity, 0);
    }

    /** F4 教师查看自己发布的活动 */
    public List<ActivityResponse> listMine(Long userId, String role) {
        requireTeacher(role);
        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getTeacherId, userId)
                .orderByDesc(Activity::getCreatedAt));
        Map<Long, Long> counts = countByActivityIds(idList(list));
        return list.stream().map(a -> ActivityResponse.from(a, counts.getOrDefault(a.getId(), 0L))).toList();
    }

    /** F4 教师修改自己的活动 */
    public ActivityResponse update(Long id, ActivitySaveRequest req, Long userId, String role) {
        requireTeacher(role);
        validateTime(req, false);
        Activity activity = requireOwnedActivity(id, userId);

        // 已取消的活动不可再编辑
        if ("CANCELLED".equals(activity.getStatus())) {
            throw new BizException("活动已取消，不可修改");
        }
        // 容量不得小于当前已报名人数（人工确认规则②）
        long registered = countByActivityId(id);
        if (req.getMaxParticipants() < registered) {
            throw new BizException("人数上限不能小于当前已报名人数（" + registered + " 人）");
        }

        applyFields(activity, req);
        activityMapper.updateById(activity);
        return ActivityResponse.from(activity, registered);
    }

    /** F4 教师取消自己的活动（软删除：状态置 CANCELLED，保留数据与报名记录） */
    public void cancel(Long id, Long userId, String role) {
        requireTeacher(role);
        Activity activity = requireOwnedActivity(id, userId);
        if ("CANCELLED".equals(activity.getStatus())) {
            throw new BizException("活动已取消");
        }
        activity.setStatus("CANCELLED");
        activityMapper.updateById(activity);
    }

    /**
     * F5 公开浏览活动列表（无需登录）。
     * @param stage all/open/ongoing/finished，默认 open；已取消活动不出现在列表
     */
    public List<ActivityResponse> listPublic(String stage) {
        String filter = (stage == null || stage.isBlank()) ? "open" : stage;
        if (!Set.of("all", "open", "ongoing", "finished").contains(filter)) {
            throw new BizException("不支持的状态筛选：" + stage);
        }
        List<Activity> list = activityMapper.selectList(new LambdaQueryWrapper<Activity>()
                .eq(Activity::getStatus, "ACTIVE")
                .orderByDesc(Activity::getCreatedAt));
        Map<Long, Long> counts = countByActivityIds(idList(list));
        LocalDateTime now = LocalDateTime.now();
        return list.stream()
                .filter(a -> "all".equals(filter) || ActivityResponse.stageOf(a, now).equals(filter.toUpperCase()))
                .map(a -> ActivityResponse.from(a, counts.getOrDefault(a.getId(), 0L)))
                .toList();
    }

    /** F6 公开活动详情（无需登录）；已取消活动也可查看并显示已取消 */
    public ActivityResponse getPublic(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        return ActivityResponse.from(activity, countByActivityId(id));
    }

    /** 角色校验：仅教师可管理活动 */
    private void requireTeacher(String role) {
        if (!"TEACHER".equals(role)) {
            throw new BizException(403, "仅教师可执行该操作");
        }
    }

    /** 取出活动并校验存在性与所有权（R5：非发布者返回 403） */
    private Activity requireOwnedActivity(Long id, Long userId) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(404, "活动不存在");
        }
        if (!activity.getTeacherId().equals(userId)) {
            throw new BizException(403, "只能操作自己发布的活动");
        }
        return activity;
    }

    /**
     * 时间关系校验（人工确认规则①：报名截止时间严格早于开始时间）。
     * @param isCreate 创建时额外要求开始时间晚于当前
     */
    private void validateTime(ActivitySaveRequest req, boolean isCreate) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new BizException("活动结束时间必须晚于开始时间");
        }
        if (!req.getSignupDeadline().isBefore(req.getStartTime())) {
            throw new BizException("报名截止时间必须早于活动开始时间");
        }
        if (isCreate && !req.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BizException("活动开始时间必须晚于当前时间");
        }
    }

    private void applyFields(Activity activity, ActivitySaveRequest req) {
        activity.setTitle(req.getTitle());
        activity.setDescription(req.getDescription());
        activity.setLocation(req.getLocation());
        activity.setStartTime(req.getStartTime());
        activity.setEndTime(req.getEndTime());
        activity.setSignupDeadline(req.getSignupDeadline());
        activity.setMaxParticipants(req.getMaxParticipants());
    }

    private long countByActivityId(Long activityId) {
        return registrationMapper.selectCount(new LambdaQueryWrapper<Registration>()
                .eq(Registration::getActivityId, activityId));
    }

    /** 批量统计各活动报名人数（避免 N+1） */
    private Map<Long, Long> countByActivityIds(List<Long> activityIds) {
        Map<Long, Long> result = new HashMap<>();
        if (activityIds.isEmpty()) {
            return result;
        }
        List<Registration> regs = registrationMapper.selectList(new LambdaQueryWrapper<Registration>()
                .in(Registration::getActivityId, activityIds));
        for (Registration r : regs) {
            result.merge(r.getActivityId(), 1L, Long::sum);
        }
        return result;
    }

    private List<Long> idList(List<Activity> list) {
        return list.stream().map(Activity::getId).toList();
    }
}
