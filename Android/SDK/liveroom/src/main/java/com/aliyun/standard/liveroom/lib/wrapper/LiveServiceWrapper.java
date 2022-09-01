package com.aliyun.standard.liveroom.lib.wrapper;

import android.support.annotation.MainThread;

import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.biz.RoomEngine;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.biz.exposable.RoomSceneLive;
import com.aliyun.roompaas.biz.exposable.model.LiveRoomInfo;
import com.aliyun.roompaas.biz.exposable.model.Result;
import com.aliyun.roompaas.live.AliLivePusherOptions;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;
import com.aliyun.roompaas.live.exposable.LiveEventHandler;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.live.exposable.model.LiveInfoModel;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.linkmic.LeaveRoomListener;

import java.util.HashMap;

/**
 * @author puke
 * @version 2021/9/26
 */
class LiveServiceWrapper implements LiveServiceExtends {

    private final LiveService liveService;
    private final LiveContext liveContext;
    private final RoomChannel roomChannel;

    private LivePlayerServiceExtends playerService;
    private LinkMicPusherService pusherService;

    LiveServiceWrapper(LiveContext liveContext, LiveService liveService) {
        this.liveService = liveService;
        this.liveContext = liveContext;
        this.roomChannel = liveContext.getRoomChannel();
    }

    @Override
    public boolean hasLive() {
        return liveService.hasLive();
    }

    @Override
    public LiveDetail getLiveDetail() {
        return liveService.getLiveDetail();
    }

    @Override
    public void getLiveDetail(Callback<LiveDetail> callback) {
        liveService.getLiveDetail(callback);
    }

    @Override
    public void updateLiveInfo(LiveInfoModel model, Callback<Void> callback) {
        liveService.updateLiveInfo(model, callback);

        // 兼容场景化服务端的补偿逻辑
        Result<RoomSceneLive> result = RoomEngine.getInstance().getRoomSceneLive();
        String liveId = getInstanceId();
        if (result.success && liveId != null) {
            LiveRoomInfo info = new LiveRoomInfo();
            info.liveId = liveId;
            info.title = model.title;
            info.coverUrl = model.coverUrl;
            info.extension = new HashMap<>();
            info.extension.put("extension", model.userDefineField);
            result.value.updateLive(info, null);
        }
    }

    @Override
    public LivePlayerServiceExtends getPlayerService() {
        if (playerService == null) {
            LivePlayerService originPlayerService = liveService.getPlayerService();
            playerService = new LivePlayerServiceWrapper(liveService, originPlayerService);
        }
        return playerService;
    }

    @Override
    public LinkMicPusherService getLinkMicPusherService() {
        return getLivePusherService(null);
    }

    @Override
    public LivePusherServiceExtends getPusherService() {
        return getLivePusherService(null);
    }

    @Override
    public LivePusherServiceExtends getPusherService(AliLivePusherOptions options) {
        return getLivePusherService(options);
    }

    private LinkMicPusherService getLivePusherService(AliLivePusherOptions options) {
        if (pusherService != null) {
            return pusherService;
        }

        // 获取底层的LivePusherService对象
        final LivePusherService originPusherService;
        if (options == null) {
            // 外部设置AliLiveMediaStreamOptions, 优先取它
            LivePrototype.OpenLiveParam openLiveParam = LivePrototype.getInstance().getOpenLiveParam();
            AliLiveMediaStreamOptions mediaStreamOptions = openLiveParam.mediaPusherOptions;
            if (mediaStreamOptions == null) {
                // 未设置AliLiveMediaStreamOptions, 再取isAudioOnly配置
                mediaStreamOptions = new AliLiveMediaStreamOptions();
                mediaStreamOptions.previewDisplayMode = AliLiveMediaStreamOptions.getPreviewDisplayMode(openLiveParam.liveShowMode);
                mediaStreamOptions.isAudioOnly = openLiveParam.isAudioOnly;
                boolean isVertical = !LivePrototype.getInstance().getOpenLiveParam().screenLandscape;
                mediaStreamOptions.previewOrientation = isVertical
                        ? AliLiveMediaStreamOptions.AliLivePreviewOrientation.ORIENTATION_PORTRAIT
                        : AliLiveMediaStreamOptions.AliLivePreviewOrientation.ORIENTATION_LANDSCAPE_HOME_RIGHT;
            }

            AliLivePusherOptions aliLivePusherOptions = new AliLivePusherOptions();
            aliLivePusherOptions.mediaStreamOptions = mediaStreamOptions;

            originPusherService = liveService.getPusherService(aliLivePusherOptions);
        } else {
            originPusherService = liveService.getPusherService(options);
        }

        // 包装底层的pusher逻辑, 封装为样板间的pusher
        if (liveContext.supportLinkMic()) {
            // 连麦直播Pusher
            pusherService = new LinkMicLivePusherServiceImpl(roomChannel, liveService, originPusherService);
        } else {
            // 普通直播Pusher
            pusherService = new NormalLivePusherServiceImpl(roomChannel, liveService, originPusherService);
        }
        return pusherService;
    }

    @Override
    public String getPluginId() {
        return liveService.getPluginId();
    }

    @Override
    public String getInstanceId() {
        return liveService.getInstanceId();
    }

    @Override
    public void onLeaveRoom(boolean existPage) {
        liveService.onLeaveRoom(existPage);
        if (pusherService instanceof LeaveRoomListener) {
            ((LeaveRoomListener) pusherService).onLeaveRoom();
        }
    }

    @Override
    @MainThread
    public void onSyncEvent(RoomNotificationModel model) {
        liveService.onSyncEvent(model);
    }

    @Override
    public void addEventHandler(LiveEventHandler eventHandler) {
        liveService.addEventHandler(eventHandler);
    }

    @Override
    public void removeEventHandler(LiveEventHandler eventHandler) {
        liveService.removeEventHandler(eventHandler);
    }

    @Override
    public void removeAllEventHandler() {
        liveService.removeAllEventHandler();
    }
}
