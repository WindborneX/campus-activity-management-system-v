package com.campus.activity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 报名名单项（F9 教师查看） */
@Data
@AllArgsConstructor
public class RegistrationResponse {

    private Long id;
    private Long userId;
    private String username;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime registeredAt;
}
