package com.campus.activity.common;

import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/** 全局异常处理：统一转为 Result 结构 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常：如报名人数已满、重复报名等，HTTP 状态码与业务码一致 */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e, HttpServletResponse response) {
        response.setStatus(e.getCode());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** 参数校验失败：取第一条校验提示 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e, HttpServletResponse response) {
        String msg = e.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        response.setStatus(400);
        return Result.error(400, msg);
    }

    /** 未匹配到接口：返回 404 而不是 500 */
    @ExceptionHandler(NoResourceFoundException.class)
    public Result<Void> handleNotFound(NoResourceFoundException e, HttpServletResponse response) {
        response.setStatus(404);
        return Result.error(404, "接口不存在");
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e, HttpServletResponse response) {
        log.error("未处理异常", e);
        response.setStatus(500);
        return Result.error(500, "系统繁忙，请稍后重试");
    }
}
