package com.huai.constant;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Status {
    OK(200, "操作成功"),
    UNKNOWN_ERROR(500, "服务器出错了");

    private final Integer code;
    private final String message;
}
