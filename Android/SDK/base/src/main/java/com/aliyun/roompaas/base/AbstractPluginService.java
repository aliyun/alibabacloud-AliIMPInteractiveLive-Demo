package com.aliyun.roompaas.base;

import android.content.Context;
import android.support.annotation.Nullable;

import com.alibaba.dingpaas.room.PluginInstance;
import com.alibaba.dingpaas.room.PluginInstanceItem;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.aliyun.roompaas.base.exposable.PluginService;

import java.util.List;

/**
 * @author puke
 * @version 2021/6/21
 */
public abstract class AbstractPluginService<EH> extends EventHandlerManager<EH>
        implements PluginService<EH> {

    protected final RoomContext roomContext;
    protected final String userId;
    protected final String roomId;
    protected final Context context;

    private final PluginManager pluginManager;

    public AbstractPluginService(RoomContext roomContext) {
        this.roomContext = roomContext;
        this.userId = roomContext.getUserId();
        this.roomId = roomContext.getRoomId();
        this.context = roomContext.getContext();
        this.pluginManager = roomContext.getPluginManager();
    }

    protected boolean isOwner() {
        return roomContext.isOwner(userId);
    }

    @Nullable
    @Override
    public String getInstanceId() {
        String pluginId = getPluginId();
        return pluginManager.getInstanceId(pluginId);
    }

    protected List<String> getInstanceIds() {
        String pluginId = getPluginId();
        return pluginManager.getInstanceIds(pluginId);
    }

    protected void addInstanceId(String pluginInstanceId) {
        String pluginId = getPluginId();
        pluginManager.addInstanceId(pluginId, pluginInstanceId);
    }

    protected void removeInstanceId(String pluginInstanceId) {
        String pluginId = getPluginId();
        pluginManager.removeInstanceId(pluginId, pluginInstanceId);
    }

    protected List<PluginInstanceItem> getInstanceList() {
        return pluginManager.getInstanceList();
    }

    @Override
    public void onLeaveRoom(boolean existPage) {

    }

    @Override
    public void onSyncEvent(RoomNotificationModel model) {

    }
}
