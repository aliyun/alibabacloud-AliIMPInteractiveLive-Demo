package com.aliyun.roompaas.base;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.alibaba.dingpaas.room.PluginInstanceInfo;
import com.alibaba.dingpaas.room.PluginInstanceItem;
import com.aliyun.roompaas.base.util.CollectionUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author puke
 * @version 2021/6/21
 */
public class PluginManager {

    private PluginInstanceInfo pluginInstanceInfo;

    public void setPluginInstanceInfo(PluginInstanceInfo pluginInstanceInfo) {
        this.pluginInstanceInfo = pluginInstanceInfo;
    }

    @Nullable
    public String getInstanceId(String pluginId) {
        return CollectionUtil.getFirst(getInstanceIds(pluginId));
    }

    public List<String> getInstanceIds(String pluginId) {
        List<String> instanceIds = new ArrayList<>();
        List<PluginInstanceItem> instanceList = getInstanceList();
        if (CollectionUtil.isEmpty(instanceList)) {
            return instanceIds;
        }

        for (PluginInstanceItem instanceItem : instanceList) {
            if (pluginId.equals(instanceItem.pluginId)) {
                instanceIds.add(instanceItem.instanceId);
            }
        }
        return instanceIds;
    }

    public void addInstanceId(String pluginId, String pluginInstanceId) {
        List<PluginInstanceItem> instanceList = getInstanceList();
        if (CollectionUtil.isEmpty(instanceList)) {
            return;
        }

        for (PluginInstanceItem instanceItem : instanceList) {
            if (TextUtils.equals(instanceItem.pluginId, pluginId)
                    && TextUtils.equals(instanceItem.instanceId, pluginInstanceId)) {
                // 添加去重逻辑
                return;
            }
        }

        PluginInstanceItem pluginInstance = new PluginInstanceItem();
        pluginInstance.pluginId = pluginId;
        pluginInstance.instanceId = pluginInstanceId;
        instanceList.add(pluginInstance);
    }

    public void removeInstanceId(String pluginId, String pluginInstanceId) {
        List<PluginInstanceItem> instanceList = getInstanceList();
        if (TextUtils.isEmpty(pluginInstanceId) || CollectionUtil.isEmpty(instanceList)) {
            return;
        }

        Iterator<PluginInstanceItem> iterator = instanceList.iterator();
        while (iterator.hasNext()) {
            PluginInstanceItem current = iterator.next();
            if (pluginId.equals(current.getPluginId())
                    && pluginInstanceId.equals(current.instanceId)) {
                iterator.remove();
            }
        }
    }

    public List<PluginInstanceItem> getInstanceList() {
        return pluginInstanceInfo == null ? null : pluginInstanceInfo.instanceList;
    }
}
