package com.aliyun.interaction.chat;

import java.util.Map;

/**
 * Created by KyleCe on 2021/5/26
 */
public abstract class BaseReq {
    public abstract void appendParams(Map<String, String> params);
}
