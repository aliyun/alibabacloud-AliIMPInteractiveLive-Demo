// Copyright (c) 2019 The Alibaba DingTalk Authors. All rights reserved.

package com.alibaba.dingpaas.room;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @brief 房间信息
 */
public final class RoomInfo {


    /**
     * @param 房间id 必填
     */
    public String roomId = "";
    /**
     * @param 标题
     */
    public String title = "";
    /**
     * @param 公告
     */
    public String notice = "";
    /**
     * @param 房主Id
     */
    public String ownerId = "";
    /**
     * @param uv
     */
    public int uv = 0;
    /**
     * @param 在线人数
     */
    public int onlineCount = 0;
    /**
     * @param 插件信息
     */
    public PluginInstanceInfo pluginInstanceInfo;
    /**
     * @param pv
     */
    public int pv = 0;
    /**
     * @param 房间扩展信息
     */
    public HashMap<String, String> extension;
    /**
     * @param 管理员id列表
     */
    public ArrayList<String> adminIdList;

    public RoomInfo(
            String roomId,
            String title,
            String notice,
            String ownerId,
            int uv,
            int onlineCount,
            PluginInstanceInfo pluginInstanceInfo,
            int pv,
            HashMap<String, String> extension,
            ArrayList<String> adminIdList) {
        this.roomId = roomId;
        this.title = title;
        this.notice = notice;
        this.ownerId = ownerId;
        this.uv = uv;
        this.onlineCount = onlineCount;
        this.pluginInstanceInfo = pluginInstanceInfo;
        this.pv = pv;
        this.extension = extension;
        this.adminIdList = adminIdList;
    }

    public RoomInfo() {};

    /**
     * @param 房间id 必填
     */
    public String getRoomId() {
        return roomId;
    }

    /**
     * @param 标题
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param 公告
     */
    public String getNotice() {
        return notice;
    }

    /**
     * @param 房主Id
     */
    public String getOwnerId() {
        return ownerId;
    }

    /**
     * @param uv
     */
    public int getUv() {
        return uv;
    }

    /**
     * @param 在线人数
     */
    public int getOnlineCount() {
        return onlineCount;
    }

    /**
     * @param 插件信息
     */
    public PluginInstanceInfo getPluginInstanceInfo() {
        return pluginInstanceInfo;
    }

    /**
     * @param pv
     */
    public int getPv() {
        return pv;
    }

    /**
     * @param 房间扩展信息
     */
    public HashMap<String, String> getExtension() {
        return extension;
    }

    /**
     * @param 管理员id列表
     */
    public ArrayList<String> getAdminIdList() {
        return adminIdList;
    }

    @Override
    public String toString() {
        return "RoomInfo{" +
                "roomId=" + roomId +
                "," + "title=" + title +
                "," + "notice=" + notice +
                "," + "ownerId=" + ownerId +
                "," + "uv=" + uv +
                "," + "onlineCount=" + onlineCount +
                "," + "pluginInstanceInfo=" + pluginInstanceInfo +
                "," + "pv=" + pv +
                "," + "extension=" + extension +
                "," + "adminIdList=" + adminIdList +
                "}";
    }

}
