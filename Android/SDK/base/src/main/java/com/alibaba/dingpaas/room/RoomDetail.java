// Copyright (c) 2019 The Alibaba DingTalk Authors. All rights reserved.

package com.alibaba.dingpaas.room;

/**
 * @brief 房间详细信息
 */
public final class RoomDetail {


    /**
     * @param 房间信息
     */
    public RoomInfo roomInfo;

    public RoomDetail(
            RoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    public RoomDetail() {};

    /**
     * @param 房间信息
     */
    public RoomInfo getRoomInfo() {
        return roomInfo;
    }

    @Override
    public String toString() {
        return "RoomDetail{" +
                "roomInfo=" + roomInfo +
                "}";
    }

}
