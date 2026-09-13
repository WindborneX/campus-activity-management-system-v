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

    public static ActivityResponse from(Activity a) {
        return new ActivityResponse(
                a.getId(), a.getTeacherId(), a.getTitle(), a.getDescription(), a.getLocation(),
                a.getStartTime(), a.getEndTime(), a.getSignupDeadline(),
                a.getMaxParticipants(), a.getStatus(), a.getCreatedAt());
    }
}
