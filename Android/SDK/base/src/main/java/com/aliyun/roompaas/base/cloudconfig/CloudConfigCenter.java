package com.aliyun.roompaas.base.cloudconfig;

import com.alibaba.fastjson.JSONObject;
import static com.aliyun.roompaas.base.cloudconfig.IKeys.LiveRoom.ENABLE_BEAUTIFY;
import static com.aliyun.roompaas.base.cloudconfig.IKeys.LiveRoom.MAX_LINK_MIC_COUNT;
import static com.aliyun.roompaas.base.cloudconfig.IKeys.LiveRoom.MAX_PUSH_STREAM_RESOLVE;

/**
 * 云控中心
 */
public class CloudConfigCenter {
    private static final CloudConfigCenter INSTANCE = new CloudConfigCenter();

    public static CloudConfigCenter getInstance() {
        return INSTANCE;
    }

    private JSONObject visibleConfigByLive;

    private CloudConfigCenter() {
    }

    public void setVisibleConfigByLive(JSONObject visibleConfig) {
        visibleConfigByLive = visibleConfig;
    }

    public String maxPushStreamResolveByLive() {
        return (visibleConfigByLive != null && visibleConfigByLive.containsKey(MAX_PUSH_STREAM_RESOLVE)) ?
                visibleConfigByLive.getString(MAX_PUSH_STREAM_RESOLVE) : "720P";
    }

    public boolean enableBeautify() {
        return (visibleConfigByLive != null && visibleConfigByLive.containsKey(ENABLE_BEAUTIFY)) ?
                visibleConfigByLive.getBoolean(ENABLE_BEAUTIFY) : false;
    }

//    public int maxMessageCharacter() {
//        return visibleConfigByLive != null ? visibleConfigByLive.getIntValue(MAX_MESSAGE_CHARACTER) : 1000;
//    }
//
//    public int sendMessageFrequency() {
//        return visibleConfigByLive != null ? visibleConfigByLive.getIntValue(SEND_MESSAGE_FREQUENCY) : 1000;
//    }

    public int maxLinkMicCount() {
        return visibleConfigByLive != null ? visibleConfigByLive.getIntValue(MAX_LINK_MIC_COUNT) : 20;
    }

}
