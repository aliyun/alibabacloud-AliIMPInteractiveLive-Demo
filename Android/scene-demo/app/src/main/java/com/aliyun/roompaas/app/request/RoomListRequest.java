package com.aliyun.roompaas.app.request;

import java.io.Serializable;
import java.util.Map;

/**
 * @author puke
 * @version 2021/10/29
 */
public class RoomListRequest extends BaseReq implements Serializable {

    public String appId;

    /**
     * 需要查询的第几页房间列表，默认从1开始
     */
    public int pageNumber;

    /**
     * 该页需要显示多少个房间，最大50.
     */
    public int pageSize;

    /**
     * 0:已创建,未开始直播; 1:直播中; 2:直播结束; 3或空:所有状态;
     */
    public int status;

    @Override
    public void appendParams(Map<String, String> params) {
        if (appId != null) {
            params.put("appId", appId);
        }
        params.put("pageNumber", String.valueOf(pageNumber));
        params.put("pageSize", String.valueOf(pageSize));
        params.put("status", String.valueOf(status));
    }
}
