package com.aliyun.roompaas.biz;

import android.support.annotation.CallSuper;
import android.text.TextUtils;

import com.alibaba.dingpaas.room.RoomNotificationListener;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;

import java.util.List;

/**
 * 同步协议的消息分发器, 用来将房间内监听的消息, 分发给各个插件模块
 * <p>
 * todo 该类需要优化, 不在C++层改动进行区分, 而应该泛化透出
 *
 * @author puke
 * @version 2021/6/24
 */
abstract class SyncEventDispatcher implements RoomNotificationListener {

    private static final String PLUGIN_ID_4_CHAT = "chat";
    private static final String PLUGIN_ID_4_LIVE = "live";
    private static final String PLUGIN_ID_4_RTC = "rtc";
    private static final String PLUGIN_ID_4_CLASS_SCENE = "class";
    private static final String PLUGIN_ID_4_DOC = "doc";
    private static final String PLUGIN_ID_4_WB = "wb";

    abstract List<PluginService<?>> getPluginServices();

    @CallSuper
    @Override
    public void onChatMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_CHAT, model);
    }

    @CallSuper
    @Override
    public void onLiveMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_LIVE, model);
    }

    @CallSuper
    @Override
    public void onRtcMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_RTC, model);
    }

    @Override
    public void onClassSceneMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_CLASS_SCENE, model);
    }

    @CallSuper
    @Override
    public void onDocMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_DOC, model);
    }

    @CallSuper
    @Override
    public void onWbMessage(RoomNotificationModel model) {
        dispatch(PLUGIN_ID_4_WB, model);
    }

    // 向对应插件服务分发sync事件 (这里不是广播, 而是点对点消息)
    private void dispatch(final String pluginId, final RoomNotificationModel model) {
        ThreadUtil.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                List<PluginService<?>> pluginServices = getPluginServices();
                if (CollectionUtil.isEmpty(pluginServices)) {
                    return;
                }

                for (PluginService<?> pluginService : pluginServices) {
                    if (TextUtils.equals(pluginId, pluginService.getPluginId())) {
                        pluginService.onSyncEvent(model);
                        break;
                    }
                }
            }
        });

    }
}
