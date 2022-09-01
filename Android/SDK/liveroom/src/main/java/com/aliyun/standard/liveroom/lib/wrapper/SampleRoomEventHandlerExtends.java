package com.aliyun.standard.liveroom.lib.wrapper;

import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomEventHandlerExtends;

import java.util.Map;

/**
 * @author puke
 * @version 2021/12/14
 */
public class SampleRoomEventHandlerExtends extends SampleRoomEventHandler implements RoomEventHandlerExtends {

    @Override
    public void onLiveRoomExtensionChanged(Map<String, String> extension) {

    }
}
