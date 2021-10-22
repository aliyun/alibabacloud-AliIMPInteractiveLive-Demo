package com.aliyun.roompaas.app.response;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/14
 */
public class Response<T> implements Serializable {

    public boolean responseSuccess;

    public String message;

    public T result;
}
