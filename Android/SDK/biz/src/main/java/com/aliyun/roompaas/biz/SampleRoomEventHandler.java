package com.aliyun.roompaas.biz;

import com.aliyun.roompaas.biz.exposable.RoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;

/**
 * @author puke
 * @version 2021/7/2
 */
public class SampleRoomEventHandler implements RoomEventHandler {

    @Override
    public void onEnterOrLeaveRoom(RoomInOutEvent event) {

    }

    @Override
    public void onRoomNoticeChanged(String notice) {

    }

    @Override
    public void onRoomTitleChanged(String title) {

    }

    @Override
    public void onRoomUserKicked(KickUserEvent event) {

    }
}
