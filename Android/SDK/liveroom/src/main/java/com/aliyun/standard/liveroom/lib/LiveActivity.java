package com.aliyun.standard.liveroom.lib;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.room.PluginInstanceInfo;
import com.alibaba.dingpaas.room.PluginInstanceItem;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.roompaas.base.IEventDispatcher;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.exposable.RoomEventHandler;
import com.aliyun.roompaas.biz.exposable.RoomEventHandlerExtends;
import com.aliyun.roompaas.biz.exposable.enums.LiveStatus;
import com.aliyun.roompaas.live.LiveServiceImpl;
import com.aliyun.roompaas.uibase.util.ExStatusBarUtils;
import com.aliyun.standard.liveroom.lib.component.ComponentManager;
import com.aliyun.standard.liveroom.lib.event.EventManager;
import com.aliyun.standard.liveroom.lib.wrapper.RoomChannelExtends;
import com.aliyun.standard.liveroom.lib.wrapper.RoomChannelWrapper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Map;

/**
 * 电商场景房间
 *
 * @author puke
 * @version 2021/5/11
 */
public class LiveActivity extends BaseLiveActivity {

    private static final String TAG = LiveActivity.class.getSimpleName();

    private static WeakReference<LiveActivity> activityRef;

    private final ComponentManager componentManager = new ComponentManager();
    private boolean isPushing = false;
    private boolean isSwitchUser = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        componentManager.dispatchActivityConfigurationChanged(newConfig);
    }

    @Override
    protected void init() {
        super.init();
        activityRef = new WeakReference<>(this);
        if (isSwitchUser) {
            Logger.i(TAG, "switch user, not to setContentView");
        } else {
            LiveHook liveHook = LivePrototype.getInstance().getLiveHook();
            Integer liveLayoutRes = null;
            if (liveHook != null) {
                liveLayoutRes = liveHook.getLiveLayoutRes();
            }
            if (liveLayoutRes == null) {
                liveLayoutRes = R.layout.ilr_activity_live;
            }
            setContentView(liveLayoutRes);
            View decorView = getWindow().getDecorView();
            componentManager.scanComponent(decorView);
            componentManager.addComponentFromHook();
        }
        componentManager.dispatchInit(new LiveContextImpl());
        if (!shouldDisableImmersive()) {
            ExStatusBarUtils.adjustTopMarginForImmersive(findViewById(R.id.room_header_layout));
            ExStatusBarUtils.adjustBottomForNavigationIfNecessary(this, toAdjustBottomView());
        }
    }

    @Override
    protected View toAdjustBottomView() {
        View rootView = findViewById(android.R.id.content);
        return findTargetBottomView(rootView);
    }

    @Nullable
    private View findTargetBottomView(View view) {
        if (view instanceof IContentLayer) {
            return view;
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                View child = group.getChildAt(i);
                View target = findTargetBottomView(child);
                if (target != null) {
                    return target;
                }
            }
        }
        return null;
    }

    @Override
    protected void onEnterRoomSuccess(RoomDetail roomDetail) {
        addLiveIdIfNeed(roomDetail);
        componentManager.dispatchEnterRoomSuccess(roomDetail);

        // 进入房间后, 查询直播信息
        liveService.getLiveDetail(new Callback<LiveDetail>() {
            @Override
            public void onSuccess(LiveDetail data) {
                componentManager.post(Actions.GET_LIVE_DETAIL_SUCCESS);
            }

            @Override
            public void onError(String errorMsg) {

            }
        });

        // 进入房间后, 查询互动信息
        chatService.getChatDetail(new Callback<GetTopicInfoRsp>() {
            @Override
            public void onSuccess(GetTopicInfoRsp data) {
                componentManager.post(Actions.GET_CHAT_DETAIL_SUCCESS);
            }

            @Override
            public void onError(String errorMsg) {

            }
        });

        // 发出直播间的 extension 通知事件 (暂不提供对外的查询接口, 直接通过回调事件进行补偿)
        if (extension != null && roomChannel instanceof IEventDispatcher) {
            @SuppressWarnings({"unchecked"})
            IEventDispatcher<RoomEventHandler> eventDispatcher = (IEventDispatcher<RoomEventHandler>) roomChannel;
            eventDispatcher.dispatch(new EventHandlerManager.Consumer<RoomEventHandler>() {
                @Override
                public void consume(RoomEventHandler eventHandler) {
                    if ((eventHandler instanceof RoomEventHandlerExtends)) {
                        ((RoomEventHandlerExtends) eventHandler).onLiveRoomExtensionChanged(extension);
                    }
                }
            });
        }
    }

    private void addLiveIdIfNeed(RoomDetail roomDetail) {
        if (roomDetail != null) {
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                PluginInstanceInfo pluginInfo = roomInfo.pluginInstanceInfo;
                if (pluginInfo != null) {
                    if (pluginInfo.instanceList == null) {
                        pluginInfo.instanceList = new ArrayList<>();
                    }

                    boolean hasTheLiveId = false;
                    for (PluginInstanceItem plugin : pluginInfo.instanceList) {
                        if (TextUtils.equals(LiveServiceImpl.PLUGIN_ID, plugin.pluginId)
                                && TextUtils.equals(liveId, plugin.instanceId)) {
                            hasTheLiveId = true;
                        }
                    }

                    if (!hasTheLiveId) {
                        PluginInstanceItem plugin = new PluginInstanceItem();
                        plugin.pluginId = LiveServiceImpl.PLUGIN_ID;
                        plugin.instanceId = liveId;
                        pluginInfo.instanceList.add(plugin);
                    }
                }
            }
        }
    }

    @Override
    protected void onEnterRoomError(String errorMsg) {
        componentManager.dispatchEnterRoomError(errorMsg);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        componentManager.dispatchActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        componentManager.dispatchActivityPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        componentManager.dispatchActivityResume();
        LogoutTaskManager.cancelLogout();
    }

    @Override
    public void onBackPressed() {
        if (!componentManager.interceptBackKey()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        componentManager.dispatchActivityDestroy();
        LogoutTaskManager.prepareLogout();
    }

    @Override
    public void finish() {
        super.finish();
        componentManager.dispatchActivityFinish();
        activityRef = null;
    }

    static void leaveRoom(Callback<Void> callback) {
        UICallback<Void> uiCallback = new UICallback<>(callback);
        LiveActivity activity = activityRef == null ? null : activityRef.get();
        if (activity == null || !activity.isActivityValid()) {
            Logger.e(TAG, "leaveRoom: activity is null or invalid");
            uiCallback.onError("leaveRoom: activity is null or invalid");
        } else {
            activity.roomChannel.leaveRoom(false, uiCallback);
        }
    }

    static void reInit(String userNick, Map<String, String> userExtension) {
        LiveActivity activity = activityRef == null ? null : activityRef.get();
        if (activity == null || !activity.isActivityValid()) {
            Logger.e(TAG, "reInit: activity is null or invalid");
        } else {
            activity.nick = userNick;
            activity.userExtension = userExtension;
            activity.isSwitchUser = true;
            activity.init();
        }
    }

    private class LiveContextImpl implements LiveContext {

        RoomChannelWrapper roomChannelWrapper;

        @Override
        public RoomChannelExtends getRoomChannel() {
            // 这里返回对外的api包装类, 预留一下后续可能的逻辑封装
            if (roomChannelWrapper == null) {
                roomChannelWrapper = new RoomChannelWrapper(this, roomChannel);
            }
            return roomChannelWrapper;
        }

        @Override
        public Activity getActivity() {
            return LiveActivity.this;
        }

        @Override
        public LivePrototype.Role getRole() {
            return role;
        }

        @Override
        public String getNick() {
            return nick;
        }

        @Override
        public LiveStatus getLiveStatus() {
            return liveStatus;
        }

        @Override
        public EventManager getEventManager() {
            return componentManager;
        }

        @Override
        public boolean isPushing() {
            return isPushing;
        }

        @Override
        public boolean isSwitchUser() {
            return isSwitchUser;
        }

        @Override
        public void setPushing(boolean isPushing) {
            LiveActivity.this.isPushing = isPushing;
        }

        @Override
        public boolean isLandscape() {
            return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        }

        @Override
        public void setLandscape(boolean landscape) {
            if (landscape) {
                // 竖屏 => 横屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else {
                // 横屏 => 竖屏
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        @Override
        public boolean supportLinkMic() {
            return supportLinkMic;
        }

        @Override
        public View getAdjustBottomView() {
            return toAdjustBottomView();
        }
    }
}
