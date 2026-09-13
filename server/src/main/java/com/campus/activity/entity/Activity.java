package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 活动 */
@Data
@TableName("activity")
public class Activity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发布教师 id */
    private Long teacherId;

    private String title;

    private String description;

    private String location;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 报名截止时间 */
    private LocalDateTime signupDeadline;

    /** 人数上限 */
    private Integer maxParticipants;

    /** 状态：ACTIVE / CANCELLED */
    private String status;

    private LocalDateTime createdAt;
}
