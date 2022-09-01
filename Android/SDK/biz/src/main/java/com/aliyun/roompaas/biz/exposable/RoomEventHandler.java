package com.aliyun.roompaas.biz.exposable;

import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;

/**
 * @author puke
 * @version 2021/7/2
 */
public interface RoomEventHandler {

    /**
     * 进入房间
     */
    void onEnterOrLeaveRoom(RoomInOutEvent event);

    /**
     * 房间公告变化
     */
    void onRoomNoticeChanged(String notice);

    /**
     * 房间标题变化
     */
    void onRoomTitleChanged(String title);

    /**
     * 用户被踢出房间
     */
    void onRoomUserKicked(KickUserEvent event);
}
