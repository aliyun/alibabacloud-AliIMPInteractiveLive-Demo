package com.aliyun.roompaas.app.request;

import java.util.Map;

/**
 * Created by KyleCe on 2021/5/26
 */
public abstract class BaseReq {
    public abstract void appendParams(Map<String, String> params);
}
