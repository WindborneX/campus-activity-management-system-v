package com.campus.activity.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 活动报名记录 */
@Data
@TableName("registration")
public class Registration {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long activityId;

    private Long studentId;

    private LocalDateTime registeredAt;
}
