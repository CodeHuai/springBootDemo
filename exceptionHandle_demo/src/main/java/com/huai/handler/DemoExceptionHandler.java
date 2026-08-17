package com.huai.handler;

import com.huai.bean.ApiResponse;
import com.huai.exception.JsonException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 统一异常处理（分别包括json异常处理 和 page异常处理）
 */
@ControllerAdvice
@Slf4j
public class DemoExceptionHandler {

    @ExceptionHandler(JsonException.class)
    @ResponseBody
    public ApiResponse jsonErrorHandler(JsonException jsonException) {
        log.error("【jsonException】: {}", jsonException.getMessage());
        return ApiResponse.ofException(jsonException);
    }

}
