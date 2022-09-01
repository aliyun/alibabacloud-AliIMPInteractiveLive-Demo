package com.aliyun.roompaas.live;

import android.content.Context;
import android.text.TextUtils;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.live.GetLiveDetailCb;
import com.alibaba.dingpaas.live.GetLiveDetailReq;
import com.alibaba.dingpaas.live.GetLiveDetailRsp;
import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.live.LiveInfo;
import com.alibaba.dingpaas.live.LiveModule;
import com.alibaba.dingpaas.live.LiveRpcInterface;
import com.alibaba.dingpaas.live.UpdateLiveCb;
import com.alibaba.dingpaas.live.UpdateLiveReq;
import com.alibaba.dingpaas.live.UpdateLiveRsp;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.alibaba.dingpaas.scenelive.SceneliveModule;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.AbstractPluginService;
import com.aliyun.roompaas.base.ModuleRegister;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.cloudconfig.base.IBaseCloudConfig;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.inner.InnerServiceManager;
import com.aliyun.roompaas.base.inner.module.LiveInnerService;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.live.cloudconfig.GeneralEncodeParamDelegate;
import com.aliyun.roompaas.live.exposable.LiveEventHandler;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.live.exposable.model.LiveInfoModel;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/6/21
 */
@PluginServiceInject
public class LiveServiceImpl extends AbstractPluginService<LiveEventHandler> implements LiveService, Serializable {

    public static final String PLUGIN_ID = "live";

    private static final String TAG = LiveServiceImpl.class.getSimpleName();

    // 直播消息
    private static final int LIVE_CREATED = 1001;
    private static final int LIVE_STARTED = 1002;
    private static final int LIVE_STOPPED = 1003;
    private static final int LIVE_GOT = 1004;
    private static final int LIVE_STREAM_STARTED = 1005;
    private static final int LIVE_STREAM_STOPPED = 1006;

    // 直播状态  0： 未开始  1： 进行中 2：结束
    static final int LIVE_STATUS_NO = 0;
    static final int LIVE_STATUS_START = 1;
    static final int LIVE_STATUS_END = 2;

    static {
        // 加载rts依赖
        System.loadLibrary("RtsSDK");
        // 注册lwp网络模块
        ModuleRegister.registerLwpModule(LiveModule.getModuleInfo());
        ModuleRegister.registerLwpModule(SceneliveModule.getModuleInfo());
        // 注册插件间内部通信服务 (Rtc模块依赖该服务)
        InnerServiceManager.register(LiveInnerService.class, LiveInnerServiceImpl.class);
    }

    private final LiveRpcInterface liveRpcInterface;
    private final LiveServiceContext serviceContext;

    private LivePlayerServiceImpl playerService;
    private LivePusherServiceImpl pusherService;

    private final LiveHelper liveHelper;
    private final IBaseCloudConfig generalEncodeParamDelegate;
    private LiveInfo liveInfo;
    private LiveDetail liveDetail;

    public LiveServiceImpl(RoomContext roomContext) {
        super(roomContext);
        liveRpcInterface = LiveModule.getModule(userId).getRpcInterface();
        serviceContext = new LiveServiceContext();
        liveHelper = new LiveHelper(roomContext);
        generalEncodeParamDelegate = GeneralEncodeParamDelegate.getInstance();
        generalEncodeParamDelegate.query();
    }

    @Override
    public boolean hasLive() {
        return getInstanceId() != null;
    }

    @Override
    public LiveDetail getLiveDetail() {
        return liveDetail;
    }

    @Override
    public void getLiveDetail(Callback<LiveDetail> callback) {
        final UICallback<LiveDetail> uiCallback = new UICallback<>(callback);
        final String liveId = CollectionUtil.getFirst(getInstanceIds());
        if (liveId == null) {
            uiCallback.onError(Errors.LIVE_NOT_EXISTS.getMessage());
            return;
        }

        GetLiveDetailReq req = new GetLiveDetailReq();
        req.uuid = liveId;
        liveRpcInterface.getLiveDetail(req, new GetLiveDetailCb() {
            @Override
            public void onSuccess(GetLiveDetailRsp detailRsp) {
                LiveServiceImpl.this.liveDetail = LiveModelConvertor.convertLiveDetail(detailRsp);
                uiCallback.onSuccess(LiveServiceImpl.this.liveDetail);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void updateLiveInfo(LiveInfoModel model, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (model == null) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        if (!isOwner()) {
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        final String liveId = CollectionUtil.getFirst(getInstanceIds());
        if (liveId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        UpdateLiveReq req = new UpdateLiveReq();
        req.uuid = liveId;
        req.title = model.title;
        req.coverUrl = model.coverUrl;
        req.introduction = model.introduction;
        req.userDefineField = model.userDefineField;
        liveRpcInterface.updateLive(req, new UpdateLiveCb() {
            @Override
            public void onSuccess(UpdateLiveRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void onLeaveRoom(boolean existPage) {
        super.onLeaveRoom(existPage);
        if (existPage) {
            // 离开直播间页面时, 销毁并释放对应资源
            if (pusherService != null) {
                pusherService.destroy();
            }
            if (playerService != null) {
                playerService.destroy();
            }
        }
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void onSyncEvent(final RoomNotificationModel model) {
        switch (model.type) {
            case LIVE_CREATED:
                dispatch(new Consumer<LiveEventHandler>() {
                    @Override
                    public void consume(LiveEventHandler eventHandler) {
                        eventHandler.onLiveCreated(parseLiveEvent(model));
                    }
                });
                break;
            case LIVE_STARTED:
                // 从回放回到直播状态，移除之前回放实例
                if (!isOwner()) {
                    String oldLiveId = getInstanceId();
                    if (!TextUtils.isEmpty(oldLiveId)) {
                        removeInstanceId(oldLiveId);
                    }
                }

                final LiveCommonEvent startedEvent = parseLiveEvent(model);
                // 开始直播, 开始拉流
                String startedLiveId = startedEvent.liveId;
                addInstanceId(startedLiveId);
                updateLiveStatusIfNeed(startedLiveId, LIVE_STATUS_START);
                dispatch(new Consumer<LiveEventHandler>() {
                    @Override
                    public void consume(LiveEventHandler eventHandler) {
                        eventHandler.onLiveStarted(startedEvent);
                    }
                });
                break;
            case LIVE_STOPPED:
                final LiveCommonEvent stoppedEvent = parseLiveEvent(model);

                // TODO: 2021/6/24 考虑将该部分逻辑迁移业务层
                // 停止直播, 移除直播实例、停止拉流
                if (isOwner()) {
                    removeInstanceId(stoppedEvent.liveId);
                } else {
                    if (playerService != null) {
                        playerService.stopPlay();
                    }
                }
                updateLiveStatusIfNeed(stoppedEvent.liveId, LIVE_STATUS_END);
                dispatch(new Consumer<LiveEventHandler>() {
                    @Override
                    public void consume(LiveEventHandler eventHandler) {
                        eventHandler.onLiveStopped(stoppedEvent);
                    }
                });
                break;
            case LIVE_STREAM_STARTED:
                // 流开始
                final LiveCommonEvent streamStartEvent = parseLiveEvent(model);
                if (playerService != null
                        && playerService.isNeedPlay()
                        && getLiveStatus() == LIVE_STATUS_START) {
                    playerService.refreshPlay();
                }
                dispatch(new Consumer<LiveEventHandler>() {
                    @Override
                    public void consume(LiveEventHandler eventHandler) {
                        eventHandler.onLiveStreamStarted(streamStartEvent);
                    }
                });
                break;
            case LIVE_STREAM_STOPPED:
                // 流断开
                final LiveCommonEvent streamStopEvent = parseLiveEvent(model);
                dispatch(new Consumer<LiveEventHandler>() {
                    @Override
                    public void consume(LiveEventHandler eventHandler) {
                        eventHandler.onLiveStreamStopped(streamStopEvent);
                    }
                });
                break;
            default:
                Logger.w(TAG, "unknown live message: " + JSON.toJSONString(model));
                break;
        }
    }

    private int getLiveStatus() {
        LiveDetail liveDetail = getLiveDetail();
        if (liveDetail != null) {
            LiveInfo liveInfo = liveDetail.liveInfo;
            if (liveInfo != null) {
                return liveInfo.status;
            }
        }
        return LIVE_STATUS_NO;
    }

    private void updateLiveStatusIfNeed(String liveId, int status) {
        LiveDetail liveDetail = getLiveDetail();
        if (liveDetail != null) {
            LiveInfo liveInfo = liveDetail.liveInfo;
            if (liveInfo != null && TextUtils.equals(liveInfo.liveId, liveId)) {
                liveInfo.status = status;
            }
        }
    }

    private LiveCommonEvent parseLiveEvent(RoomNotificationModel model) {
        return JSON.parseObject(model.data, LiveCommonEvent.class);
    }

    @Override
    public LivePlayerService getPlayerService() {
        if (playerService == null) {
            playerService = new LivePlayerServiceImpl(serviceContext);
        }
        return playerService;
    }

    @Override
    public LivePusherService getPusherService() {
        return getPusherService(null);
    }

    @Override
    public LivePusherService getPusherService(AliLivePusherOptions options) {
        if (pusherService == null) {
            pusherService = new LivePusherServiceImpl(serviceContext, options);
        }
        return pusherService;
    }

    /**
     * 代码重构, 将{@link LivePlayerServiceImpl}和{@link LivePusherServiceImpl}从本类中独立出去<br/>
     * 其中与本类耦合的逻辑, 通过{@link LiveServiceContext}进行桥接
     */
    class LiveServiceContext {
        Context getContext() {
            return context;
        }

        String getUserId() {
            return userId;
        }

        String getRoomId() {
            return roomId;
        }

        LiveDetail getLiveDetail() {
            return liveDetail;
        }

        void getLiveDetail(Callback<LiveDetail> callback) {
            LiveServiceImpl.this.getLiveDetail(callback);
        }

        LiveInfo getLiveInfo() {
            return liveInfo;
        }

        void setLiveInfo(LiveInfo liveInfo) {
            LiveServiceImpl.this.liveInfo = liveInfo;
        }

        LiveHelper getLiveHelper() {
            return liveHelper;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        boolean isOwner() {
            return LiveServiceImpl.this.isOwner();
        }

        String getLiveId() {
            return getInstanceId();
        }

        void removeLiveId(String liveId) {
            removeInstanceId(liveId);
        }

        void dispatch(Consumer<LiveEventHandler> consumer) {
            LiveServiceImpl.this.dispatch(consumer);
        }
    }
}
