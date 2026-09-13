package com.campus.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动创建/修改请求（F3/F4 共用）。
 * 跨字段时间关系（截止时间 < 开始时间 < 结束时间）在 Service 层校验。
 */
@Data
public class ActivitySaveRequest {

    @NotBlank(message = "活动标题不能为空")
    @Size(max = 100, message = "活动标题最长 100 字")
    private String title;

    @Size(max = 2000, message = "活动简介最长 2000 字")
    private String description;

    @NotBlank(message = "活动地点不能为空")
    @Size(max = 100, message = "活动地点最长 100 字")
    private String location;

    @NotNull(message = "活动开始时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @NotNull(message = "活动结束时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @NotNull(message = "报名截止时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime signupDeadline;

    @NotNull(message = "人数上限不能为空")
    @Min(value = 1, message = "人数上限至少为 1")
    @Max(value = 10000, message = "人数上限不能超过 10000")
    private Integer maxParticipants;
}
