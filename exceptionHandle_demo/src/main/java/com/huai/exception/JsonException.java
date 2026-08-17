package com.huai.exception;

import com.huai.constant.Status;
import lombok.Getter;

/**
 * json异常处理
 */
@Getter
public class JsonException extends BaseException {

    public JsonException(Status status) {
        super(status);
    }

    public JsonException(Integer code, String message) {
        super(code, message);
    }
}
