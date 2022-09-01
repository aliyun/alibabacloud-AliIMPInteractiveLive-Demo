// Copyright (c) 2019 The Alibaba DingTalk Authors. All rights reserved.

package com.alibaba.dingpaas.room;

import java.util.ArrayList;

public final class PluginInstanceInfo {


    /**
     * 插件事例列表
     */
    public ArrayList<PluginInstanceItem> instanceList;

    public PluginInstanceInfo(
            ArrayList<PluginInstanceItem> instanceList) {
        this.instanceList = instanceList;
    }

    public PluginInstanceInfo() {};

    /**
     * 插件事例列表
     */
    public ArrayList<PluginInstanceItem> getInstanceList() {
        return instanceList;
    }

    @Override
    public String toString() {
        return "PluginInstanceInfo{" +
                "instanceList=" + instanceList +
                "}";
    }

}
