package com.aliyun.roompaas.biz.exposable;

import java.util.Map;

/**
 * @author puke
 * @version 2021/12/14
 */
public interface RoomEventHandlerExtends {

    /**
     * 直播间拓展字段变化
     *
     * @param extension 直播间的extension字段
     */
    void onLiveRoomExtensionChanged(Map<String, String> extension);
}
