package com.campus.activity.common;

import lombok.Getter;

/** 业务异常（如：报名人数已满、重复报名），message 面向用户展示 */
@Getter
public class BizException extends RuntimeException {

    private final int code;

    public BizException(String message) {
        this(400, message);
    }

    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }
}
