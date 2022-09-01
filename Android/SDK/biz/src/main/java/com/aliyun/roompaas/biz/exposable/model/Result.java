package com.aliyun.roompaas.biz.exposable.model;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/9/28
 */
public class Result<T> implements Serializable {
    public boolean success;
    public T value;
    public String errorMsg;

    private Result(boolean success, T value, String errorMsg) {
        this.success = success;
        this.value = value;
        this.errorMsg = errorMsg;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(true, value, null);
    }

    public static <T> Result<T> error(String errorMsg) {
        return new Result<>(false, null, errorMsg);
    }
}
