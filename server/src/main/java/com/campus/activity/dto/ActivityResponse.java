package com.campus.activity.dto;

import com.campus.activity.entity.Activity;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 活动响应：不携带任何敏感信息，活动字段对外视图 */
@Data
@AllArgsConstructor
public class ActivityResponse {

    private Long id;
    private Long teacherId;
    private String title;
    private String description;
    private String location;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupDeadline;

    private Integer maxParticipants;

    /** ACTIVE / CANCELLED */
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    /** 当前已报名人数（实时统计，不入库） */
    private long currentCount;

    /**
     * 派生阶段（实时计算，不入库）：
     * CANCELLED 已取消 / OPEN 报名中 / UPCOMING 报名已截止待开始 / ONGOING 进行中 / FINISHED 已结束
     */
    private String stage;

    /** 由活动实体与实时报名人数构造 */
    public static ActivityResponse from(Activity a, long currentCount) {
        return new ActivityResponse(
                a.getId(), a.getTeacherId(), a.getTitle(), a.getDescription(), a.getLocation(),
                a.getStartTime(), a.getEndTime(), a.getSignupDeadline(),
                a.getMaxParticipants(), a.getStatus(), a.getCreatedAt(),
                currentCount, stageOf(a, LocalDateTime.now()));
    }

    /** 按状态与当前时间派生活动阶段 */
    public static String stageOf(Activity a, LocalDateTime now) {
        if ("CANCELLED".equals(a.getStatus())) {
            return "CANCELLED";
        }
        if (now.isBefore(a.getSignupDeadline())) {
            return "OPEN";
        }
        if (now.isBefore(a.getStartTime())) {
            return "UPCOMING";
        }
        if (now.isBefore(a.getEndTime())) {
            return "ONGOING";
        }
        return "FINISHED";
    }
}
