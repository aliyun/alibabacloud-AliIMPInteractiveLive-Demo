package com.aliyun.room;


import lombok.Data;

import java.io.Serializable;

/**
 * @version 2021/5/11
 */
@Data
public class ResponseResult<T> implements Serializable {
    /**
     * 随机数，序列化用
     */
    private static final long serialVersionUID = 6750743467360735252L;

    /**
     * 业务请求是否成功
     */
    protected boolean responseSuccess;

    /**
     * 失败错误信息
     */
    protected String message;

    /**
     * 成功结果
     */
    protected T result;

    public ResponseResult() {
    }

    public static <T> ResponseResult<T> getSuccessResult(T v) {
        ResponseResult<T> result = new ResponseResult();

        result.setResponseSuccess(true);
        result.setResult(v);
        return result;
    }

    public static <T> ResponseResult<T> getFailureResult(String msg) {
        ResponseResult<T> result = new ResponseResult();

        result.setResponseSuccess(false);
        result.setMessage(msg);
        return result;
    }
}
