package com.aliyun.roompaas.roombase.request;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by KyleCe on 2021/5/26
 */
public abstract class BaseReq implements Serializable {
    public abstract void appendParams(Map<String, String> params);
}
