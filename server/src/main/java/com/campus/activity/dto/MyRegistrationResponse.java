package com.campus.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 我的报名项（F：学生查看自己的报名，含活动快照信息） */
@Data
@AllArgsConstructor
public class MyRegistrationResponse {

    private Long registrationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registeredAt;

    private ActivityResponse activity;
}
