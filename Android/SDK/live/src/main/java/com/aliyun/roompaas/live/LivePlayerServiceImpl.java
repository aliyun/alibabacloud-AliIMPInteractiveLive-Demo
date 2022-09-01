package com.aliyun.roompaas.live;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.dingpaas.live.ArtcInfoModel;
import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.live.LiveInfo;
import com.alibaba.dingpaas.monitorhub.MonitorhubEvent;
import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.monitorhub.MonitorhubStatusType;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.roompaas.base.EventHandler;
import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.monitor.MonitorHeartbeatManager;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.network.NetworkAvailableManager;
import com.aliyun.roompaas.base.network.OnNetworkAvailableChangeListener;
import com.aliyun.roompaas.live.exposable.LiveEventHandler;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.player.AliLivePlayerConfig;
import com.aliyun.roompaas.player.LivePlayerManager;
import com.aliyun.roompaas.player.LivePlayerManagerHolder;
import com.aliyun.roompaas.player.PlayerEvent;
import com.aliyun.roompaas.player.exposable.CanvasScale;

import static com.aliyun.roompaas.live.LiveServiceImpl.LIVE_STATUS_END;
import static com.aliyun.roompaas.live.LiveServiceImpl.LIVE_STATUS_NO;
import static com.aliyun.roompaas.live.LiveServiceImpl.LIVE_STATUS_START;

/**
 * @author puke
 * @version 2022/4/19
 */
class LivePlayerServiceImpl implements LivePlayerService {

    private static final String TAG = LivePlayerServiceImpl.class.getSimpleName();

    private final LiveServiceImpl.LiveServiceContext serviceContext;
    private final Context context;

    private LivePlayerManager livePlayerManager;
    private ViewGroup playerContainer;
    private int maxRetryPlayCount = 3;
    private int retryPlayCount;
    // 该变量用来判断, 在监听到断流重连时是否需要刷新播放
    private boolean needPlay = false;

    private final OnNetworkAvailableChangeListener availableChangeListener = new OnNetworkAvailableChangeListener() {
        @Override
        public void onNetworkAvailableChanged(boolean available) {
            // 网络恢复时, 刷新播放器
            if (available && livePlayerManager != null && isNeedPlay()) {
                refreshPlay();
            }
        }
    };

    LivePlayerServiceImpl(final LiveServiceImpl.LiveServiceContext serviceContext) {
        this.serviceContext = serviceContext;
        this.context = serviceContext.getContext();
        NetworkAvailableManager.instance().register(availableChangeListener);
    }

    private void reportEventByLivePlayError(Object obj) {
        if (obj instanceof ErrorInfo) {
            ErrorInfo errorInfo = (ErrorInfo) obj;
            MonitorHubChannel.reportLivePlay(errorInfo.getCode().getValue(), errorInfo.getMsg());
            MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
        }
    }

    private void retryPlay() {
        if (retryPlayCount++ < maxRetryPlayCount) {
            refreshPlay();
        }
    }

    @Override
    public void refreshPlay() {
        setNeedPlay(true);
        getPlayerManager().stopPlay();
        getPlayerManager().preparePlay();
    }

    @Override
    public void resumePlay() {
        Logger.i(TAG, "resumePlay");
        setNeedPlay(true);
        getPlayerManager().startPlay();
    }

    @Override
    public void pausePlay() {
        Logger.i(TAG, "pausePlay");
        setNeedPlay(false);
        getPlayerManager().pausePlay();
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_PAUSE,
                null, 0, null);
    }

    @Override
    public void stopPlay() {
        Logger.i(TAG, "stopPlay");
        setNeedPlay(false);
        LivePlayerManagerHolder.stopPlay(livePlayerManager);
        if (playerContainer != null) {
            playerContainer.removeAllViews();
        }
        MonitorHubChannel.reportLiveStop(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
    }

    @Override
    public void setMutePlay(boolean mute) {
        getPlayerManager().setMute(mute);
    }

    @Override
    public void tryPlay(Callback<View> callback) {
        setNeedPlay(true);
        String liveId = serviceContext.getLiveId();
        if (TextUtils.isEmpty(liveId)) {
            callback.onSuccess(getPlayerViewWrapperByLazy());
        } else {
            doPlayLive(callback);
        }
    }

    @Override
    public SurfaceView playUrl(String url) {
        setNeedPlay(true);
        return getPlayerManager().startPlay(url);
    }

    @Override
    public String getLastTriggerPlayUrl() {
        return getPlayerManager().getLastTriggerPlayUrl();
    }

    @Override
    public void setViewContentMode(@CanvasScale.Mode int mode) {
        getPlayerManager().setViewContentMode(mode);
    }

    @Override
    public void updatePositionTimerInternalMs(long internal) {
        getPlayerManager().updatePositionTimerInternalMs(internal);
    }

    @Override
    public void setUtcTimeListener(LivePlayerManager.UtcTimeListener utcTimeListener) {
        getPlayerManager().setUtcTimeListener(utcTimeListener);
    }

    @Override
    public void seekTo(long position) {
        getPlayerManager().seekTo(position);
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_SEEK,
                null, 0, null);
    }

    @Override
    public long getDuration() {
        return getPlayerManager().getDuration();
    }

    @Override
    public void setPlayerConfig(AliLivePlayerConfig playerConfig) {
        if (playerConfig != null) {
            maxRetryPlayCount = playerConfig.networkRetryCount;
        }
        getPlayerManager().setPlayerConfig(playerConfig);
    }

    private void doPlayLive(final Callback<View> callback) {
        serviceContext.getLiveDetail(new Callback<LiveDetail>() {
            @Override
            public void onSuccess(LiveDetail liveDetail) {
                SurfaceView surfaceView = startPlay(liveDetail.liveInfo);
                if (surfaceView == null) {
                    callback.onError("startPlay fail");
                    return;
                }
                addChildMatchParentSafely(getPlayerViewWrapperByLazy(), surfaceView);
                callback.onSuccess(getPlayerViewWrapperByLazy());

                // 上报播放成功数据
                MonitorHubChannel.reportLivePlay(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                MonitorHeartbeatManager.getInstance().setRemoteVideoWidth(String.valueOf(getPlayerManager().getVideoWidth()));
                MonitorHeartbeatManager.getInstance().setRemoteVideoHeight(String.valueOf(getPlayerManager().getVideoHeight()));
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
        MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.LIVE_PLAY);
    }

    public void addChildMatchParentSafely(@Nullable ViewGroup vg, @Nullable View child) {
        if (vg == null || child == null) {
            return;
        }

        if (child.getParent() instanceof ViewGroup) {
            ViewGroup originParent = (ViewGroup) child.getParent();
            if (!originParent.equals(vg)) {
                removeChildFromParent(originParent, child);
            } else {
                // already added to parent
                return;
            }
        }

        vg.addView(child, new ViewGroup.LayoutParams(-1, -1));
    }

    public void removeChildFromParent(@NonNull ViewGroup vg, @NonNull View self) {
        self.clearAnimation();
        vg.removeView(self);
    }

    private ViewGroup getPlayerViewWrapperByLazy() {
        if (playerContainer == null) {
            playerContainer = new FrameLayout(context);
            playerContainer.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        }
        return playerContainer;
    }

    void destroy() {
        // 离开房间，上报离开直播
        LiveInfo liveInfo = serviceContext.getLiveInfo();
        LiveHelper liveHelper = serviceContext.getLiveHelper();
        if (!serviceContext.isOwner() && liveInfo != null && liveHelper != null) {
            if (liveInfo.status == LIVE_STATUS_NO || liveInfo.status == LIVE_STATUS_START) {
                liveHelper.endLiveTiming();
            } else if (liveInfo.status == LIVE_STATUS_END) {
                liveHelper.endPlaybackTiming();
            }
        }

        setNeedPlay(false);
        retryPlayCount = 0;
        LivePlayerManagerHolder.destroy(livePlayerManager);
        NetworkAvailableManager.instance().unregister(availableChangeListener);
    }

    @Nullable
    private SurfaceView startPlay(LiveInfo liveInfo) {
        Logger.i(TAG, "startPlay: " + liveInfo);
        serviceContext.setLiveInfo(liveInfo);
        if (liveInfo == null) {
            return null;
        }
        // 0： 未开始  1： 进行中 2：结束
        int status = liveInfo.status;
        ArtcInfoModel artcInfo = liveInfo.artcInfo;
        SurfaceView surfaceView = null;
        if (status == LIVE_STATUS_NO || status == LIVE_STATUS_START) {
            if (getPlayerManager().needLowDelay() &&
                    (artcInfo != null && artcInfo.artcUrl != null && !artcInfo.artcUrl.isEmpty())) {
                surfaceView = playLive(artcInfo.artcUrl);
                // 上报bizId
                MonitorHeartbeatManager.getInstance().setProtoType(MonitorhubField.MFFIELD_COMMON_RTS);
            } else {
                surfaceView = playLive(liveInfo.liveUrl);
                // 上报bizId
                MonitorHeartbeatManager.getInstance().setProtoType(MonitorhubField.MFFIELD_COMMON_FLV);
            }
        } else if (status == LIVE_STATUS_END) {
            surfaceView = playVod(liveInfo.playUrl);
        }

        return surfaceView;
    }

    private SurfaceView playLive(String url) {
        LiveInfo liveInfo = serviceContext.getLiveInfo();
        serviceContext.getLiveHelper().startLiveTiming();
        MonitorHubChannel.setBizId(liveInfo.liveId);
        MonitorHeartbeatManager.getInstance().setLiveUrl(url);
        MonitorHeartbeatManager.getInstance().setPlayType(MonitorhubField.MFFIELD_COMMON_LIVE);
        MonitorHeartbeatManager.getInstance().setContentId(liveInfo.liveId);
        return getPlayerManager().startPlay(url);
    }

    private SurfaceView playVod(String url) {
        serviceContext.getLiveHelper().startPlaybackTiming("");
        MonitorHeartbeatManager.getInstance().setPlayType(MonitorhubField.MFFIELD_COMMON_VOD);
        MonitorHeartbeatManager.getInstance().setContentId(serviceContext.getLiveInfo().liveId);
        return getPlayerManager().startPlay(url);
    }

    private void setNeedPlay(boolean needPlay) {
        this.needPlay = needPlay;
    }

    public boolean isNeedPlay() {
        return needPlay;
    }

    // 懒加载处理, 使用时再获取
    private LivePlayerManager getPlayerManager() {
        if (livePlayerManager == null) {
            LivePlayerManager holdPlayerManager = LivePlayerManagerHolder.getHoldPlayerManager();
            if (holdPlayerManager == null) {
                // 一般场景
                livePlayerManager = new LivePlayerManager(context);
                LivePlayerManagerHolder.setLivePlayerManager(livePlayerManager);
            } else {
                // 小窗场景
                livePlayerManager = holdPlayerManager;
            }
            bindPlayerManagerListener(livePlayerManager);
        }
        return livePlayerManager;
    }

    private void bindPlayerManagerListener(final LivePlayerManager playerManager) {
        playerManager.setCallback(new LivePlayerManager.Callback() {
            @Override
            public void onRtsPlayerError() {
                // RTS播放失败，downgrade 降级策略
                Logger.d(TAG, "RTS play error");
                // 移除rtc播放失败后的降低逻辑, 对齐iOS端
                retryPlay();
            }

            @Override
            public void onPlayerHttpRangeError() {
                // Http range error
                Logger.d(TAG, "aliPlayer error network http range.");
                retryPlay();
            }
        });

        playerManager.clearEventHandler();
        playerManager.addEventHandler(new EventHandler<PlayerEvent>() {

            @Override
            public void onEvent(final PlayerEvent event, final Object obj) {
                switch (event) {
                    case RENDER_START:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onRenderStart();
                            }
                        });
                        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_FIRST_FRAME,
                                null, 0, null);
                        break;
                    case PLAYER_LOADING_BEGIN:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onLoadingBegin();
                            }
                        });
                        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_NET_LOADING_BEGIN,
                                null, 0, null);
                        break;
                    case PLAYER_LOADING_PROGRESS:
                        if (!(obj instanceof Integer)) {
                            break;
                        }
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onLoadingProgress((Integer) obj);
                            }
                        });
                        break;
                    case PLAYER_LOADING_END:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onLoadingEnd();
                            }
                        });
                        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PLAY_NET_LOADING_END,
                                null, 0, null);
                        break;
                    case PLAYER_ERROR:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerError();
                            }
                        });
                        reportEventByLivePlayError(obj);
                        break;
                    case PLAYER_ERROR_RAW:
                        if (!(obj instanceof ErrorInfo)) {
                            break;
                        }
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerError((ErrorInfo) obj);
                            }
                        });
                        reportEventByLivePlayError(obj);
                        break;
                    case PLAYER_PREPARED:
                        // 重置重试次数
                        retryPlayCount = 0;
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPrepared();
                            }
                        });
                        break;
                    case PLAYER_END:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerEnd();
                            }
                        });
                        break;
                    case CURRENT_POSITION:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerCurrentPosition((Long) obj);
                            }
                        });
                        break;
                    case BUFFERED_POSITION:
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerBufferedPosition((Long) obj);
                            }
                        });
                        break;
                    case PLAYER_VIDEO_SIZE:
                        LivePlayerManager.VideoSize videoSize = (LivePlayerManager.VideoSize) obj;
                        final int width = videoSize.width;
                        final int height = videoSize.height;
                        // 数据上报
                        MonitorHeartbeatManager.getInstance().setRemoteVideoWidth(String.valueOf(width));
                        MonitorHeartbeatManager.getInstance().setRemoteVideoHeight(String.valueOf(height));
                        // 外部回调
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerVideoSizeChanged(width, height);
                            }
                        });
                        break;
                    case PLAYER_VIDEO_RENDERED:
                        MonitorHeartbeatManager.getInstance().setRemoteVideoRenderFrames(playerManager.getRenderFPS());
                        break;
                    case PLAYER_STATUS_CHANGE:
                        final int status = (Integer) obj;
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerStatusChange(status);
                            }
                        });
                        switch (status) {
                            case IPlayer.paused:
                            case IPlayer.stopped:
                            case IPlayer.completion:
                            case IPlayer.error:
                                MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
                                break;
                            case IPlayer.started:
                                MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.LIVE_PLAY);
                                break;
                        }
                        Logger.i(TAG, "PLAYER_STATUS_CHANGE, status = " + status);
                        break;
                    case PLAYER_DOWNLOAD_SPEED_CHANGE:
                        final long downloadSpeed = (long) obj;
                        serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                            @Override
                            public void consume(LiveEventHandler eventHandler) {
                                eventHandler.onPlayerDownloadSpeedChanged(downloadSpeed);
                            }
                        });
                        break;
                }
            }
        });
    }
}
