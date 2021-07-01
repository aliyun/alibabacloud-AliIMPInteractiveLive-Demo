package com.aliyun.roompaas.app.request;

import java.util.Map;

/**
 * Created by KyleCe on 2021/5/26
 */
public class GetTokenRequest extends BaseReq{
    public String uid;

    public GetTokenRequest(String uid) {
        this.uid = uid;
    }

    @Override
    public void appendParams(Map<String, String> params) {
        params.put("appUid", uid);
    }
}
