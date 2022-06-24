package com.aliyun.interaction.chat;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/14
 */
public class Response<T> implements Serializable {
    public String requestId;

    public boolean responseSuccess;

    public String errorCode;

    public String errorMsg;

    public T result;
}
