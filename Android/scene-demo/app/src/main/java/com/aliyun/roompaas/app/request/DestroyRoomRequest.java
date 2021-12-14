package com.aliyun.roompaas.app.request;

import java.util.Map;

/**
 * @author puke
 * @version 2021/5/14
 */
public class DestroyRoomRequest extends BaseReq {

    public String appId;
    public String roomId;
    public String userId;

    @Override
    public void appendParams(Map<String, String> params) {
        params.put("appId", this.appId);
        params.put("roomId", this.roomId);
        params.put("userId", this.userId);
    }
}
