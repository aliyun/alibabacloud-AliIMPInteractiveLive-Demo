package com.aliyun.roompaas.biz;

import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;

/**
 * @author puke
 * @version 2021/5/17
 */
public enum RoomEvent {

    /**
     * 进入房间, 数据类型为 {@link RoomInOutEvent}
     */
    ENTER_ROOM,

    /**
     * 房间公告变化, 数据类型为 {@link String}
     */
    ROOM_NOTICE_CHANGED,

    /**
     * 房间标题变化, 数据类型为 {@link String}
     */
    ROOM_TITLE_CHANGED,

    /**
     * 用户被踢出房间, 数据类型为 {@link KickUserEvent}
     */
    ROOM_USER_KICKED,

    /**
     * 离开房间, 数据类型为 {@link RoomInOutEvent}
     */
    LEAVE_ROOM,
}
