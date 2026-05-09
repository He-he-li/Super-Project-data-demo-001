package com.example.data_demo_002.common.result;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Result<T> {
    private int code;
    private String message;
    private T data;

    // 成功
    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }
    // 成功
    public static <T> Result<T> success() {
        return success(null);
    }

    // 成功（带自定义消息）
    public static <T> Result<T> success(T data, String message) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage(message);
        r.setData(data);
        return r;
    }


    // 失败
    public static <T> Result<T> error(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> error(String message) {
        return error(500, message);
    }
}