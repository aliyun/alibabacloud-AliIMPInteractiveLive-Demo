package com.aliyun.roompaas.base.exposable;

import android.support.annotation.MainThread;

import com.alibaba.dingpaas.room.RoomNotificationModel;

/**
 * @author puke
 * @version 2021/6/21
 */
public interface PluginService<EH> extends IEventHandlerManager<EH> {

    /**
     * @return 插件唯一标识符
     */
    String getPluginId();

    /**
     * @return 插件实例Id
     */
    String getInstanceId();

    /**
     * 离开房间的事件回调
     *
     * @param existPage 是否离开页面
     */
    void onLeaveRoom(boolean existPage);

    /**
     * 房间事件回调
     *
     * @param model 事件类型
     */
    @MainThread
    void onSyncEvent(RoomNotificationModel model);
}
