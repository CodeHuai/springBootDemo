package com.huai.bean;

import com.huai.constant.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> of(Integer code, String msg, T data) {
        return new ApiResponse<>(code, msg, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return of(Status.OK.getCode(), Status.OK.getMessage(), data);
    }

    public static <T> ApiResponse<T> success() {
        return of(Status.OK.getCode(), Status.OK.getMessage(), null);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return of(Status.UNKNOWN_ERROR.getCode(), msg, null);
    }
}
