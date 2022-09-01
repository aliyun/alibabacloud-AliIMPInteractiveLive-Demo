// Copyright (c) 2019 The Alibaba DingTalk Authors. All rights reserved.

package com.alibaba.dingpaas.room;

import java.util.HashMap;

public final class PluginInstanceItem {


    /**
     * 插件ID
     */
    public String pluginId;
    /**
     * 实例ID
     */
    public String instanceId;
    /**
     * 创建时间ms
     */
    public long createTime = 0L;
    /**
     * 插件透传信息
     */
    public HashMap<String, String> extension;

    public PluginInstanceItem(
            String pluginId,
            String instanceId,
            long createTime,
            HashMap<String, String> extension) {
        this.pluginId = pluginId;
        this.instanceId = instanceId;
        this.createTime = createTime;
        this.extension = extension;
    }

    public PluginInstanceItem() {};

    /**
     * 插件ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * 实例ID
     */
    public String getInstanceId() {
        return instanceId;
    }

    /**
     * 创建时间ms
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * 插件透传信息
     */
    public HashMap<String, String> getExtension() {
        return extension;
    }

    @Override
    public String toString() {
        return "PluginInstanceItem{" +
                "pluginId=" + pluginId +
                "," + "instanceId=" + instanceId +
                "," + "createTime=" + createTime +
                "," + "extension=" + extension +
                "}";
    }

}
