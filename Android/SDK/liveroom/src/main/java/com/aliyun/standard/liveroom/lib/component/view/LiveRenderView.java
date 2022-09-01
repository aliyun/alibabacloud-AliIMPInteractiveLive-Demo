package com.aliyun.standard.liveroom.lib.component.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.MediaProjectionPermissionResultDataHolder;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.LivePlayerService;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.player.AliLivePlayerConfig;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.player.exposable.CanvasScale.Mode;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.roompaas.uibase.util.DialogUtil;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowManager;
import com.aliyun.standard.liveroom.lib.floatwindow.FloatWindowPermissionUtil;
import com.aliyun.standard.liveroom.lib.wrapper.LivePlayerServiceExtends;

/**
 * 媒体流渲染视图
 *
 * @author puke
 * @version 2021/7/30
 */
public class LiveRenderView extends FrameLayout implements ComponentHolder {

    public static final String ACTION_SHOW_FLOAT_WINDOW = "show_float_window";
    private static final String TAG = LiveRenderView.class.getSimpleName();
    private static final int ORDER_RENDER = -100;

    protected View renderView;

    private final Component component = new Component();
    private boolean fromFloatWindow;

    public LiveRenderView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveRenderView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveRenderView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @CanvasScale.Mode
    protected int parseScaleMode(boolean isAnchor) {
        return component.getOpenLiveParam().liveShowMode;
    }

    @Nullable
    protected AliLivePlayerConfig getAliLivePlayerConfig() {
        boolean lowDelay = component.getOpenLiveParam().lowDelay;
        return new AliLivePlayerConfig(new AliLivePlayerConfig.Builder().setLowDelay(lowDelay));
    }

    /**
     * 展示悬浮窗 (当前小窗逻辑为样板间默认实现, 用户可重写该方法做上层业务定制处理)<hr/>
     * 悬浮窗权限检测: {@link FloatWindowPermissionUtil} <br/>
     * 悬浮窗显示/隐藏: {@link FloatWindowManager} <br/>
     *
     * @param activity 直播间Activity
     */
    protected void showFloatWindow(final Activity activity) {
        // 检测悬浮窗权限
        FloatWindowPermissionUtil.checkPermission(activity, new Runnable() {
            @Override
            public void run() {
                // 有权限时, 开始弹窗
                FloatWindowManager.instance().show(renderView, activity);
            }
        });
    }

    @Mode
    protected int getFillModeForAudience(LivePrototype.OpenLiveParam openLiveParam, int videoWidth, int videoHeight) {
        boolean autoFit = openLiveParam.showModeAutoFitForAudience;
        Logger.i(TAG, String.format(
                "getFillModeForAudience, autoFit=%s, videoWidth=%s, videoHeight=%s",
                autoFit, videoWidth, videoHeight
        ));
        if (autoFit && videoWidth > 0 && videoHeight > 0) {
            if (videoWidth == videoHeight) {
                // 正方形视频, 统一用Fit
                return Mode.ASPECT_FIT;
            }
            if (AppUtil.getScreenWidth() < AppUtil.getScreenHeight()) {
                // 竖屏 (高屏幕)
                boolean isHeightVideo = videoWidth < videoHeight;
                return isHeightVideo ? Mode.ASPECT_FILL : Mode.ASPECT_FIT;
            } else {
                // 横屏 (宽屏幕)
                return Mode.ASPECT_FIT;
            }
        }

        // 外部传递进来的值
        return openLiveParam.liveShowMode;
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {

        private int videoWidth;
        private int videoHeight;

        @Override
        public void onInit(final LiveContext liveContext) {
            super.onInit(liveContext);

            if (getOpenLiveParam().screenLandscape) {
                setLandscape(true);
            }

            AliLivePlayerConfig playerConfig = getAliLivePlayerConfig();
            if (playerConfig != null) {
                getPlayerService().setPlayerConfig(playerConfig);
            }

            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStarted(LiveCommonEvent event) {
                    // 观众监听到直播开始时, 开始尝试拉流
                    if (!isOwner()) {
                        tryPlayLive();
                    }
                }

                @Override
                public void onPusherEvent(LiveEvent event) {
                    switch (event) {
                        case PUSH_STARTED:
                            liveContext.setPushing(true);
                            break;
                        case PUSH_STOPPED:
                            liveContext.setPushing(false);
                            break;
                    }
                }

                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    // TODO: 2021/11/2 先不添加自动转化逻辑
//                    if (!supportPlayback()) {
//                        return;
//                    }
//
//                    if (!isOwner()) {
//                        showToast("主播离开了，即将为您加载回放～");
//                        tryPlayLive();
//                    }
                }

                @Override
                public void onPlayerVideoSizeChanged(int width, int height) {
                    Logger.i(TAG, String.format(
                            "onPlayerVideoSizeChanged width=%s, height=%s", width, height));
                    videoWidth = width;
                    videoHeight = height;
                    updateFillModeForAudience();
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            if (liveContext.isSwitchUser()) {
                Logger.i(TAG, "switch user, do not play live");
                return;
            }

            if (needPlayback()) {
                // 已结束, 观看回放
                tryPlayLive();
            } else {
                // 未开始 或 进行中
                if (isOwner()) {
                    // 主播
                    if (getOpenLiveParam().screenCaptureMode) {
                        requestScreenCapturePermission();
                    } else {
                        startPreview();
                    }
                } else {
                    // 观众, 开始观看
                    tryPlayLive();
                }
            }
        }

        private void requestScreenCapturePermission() {
            LivePusherService pusherService = getPusherService();
            Logger.i(TAG, "current is screen capture mode, don't need to start preview");
            pusherService.startScreenCapture(activity, new Callback<Void>() {
                @Override
                public void onSuccess(Void data) {
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }

        private void startPreview() {
            LivePusherService pusherService = getPusherService();
            pusherService.setPreviewMode(parseScaleMode(true));
            pusherService.startPreview(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    if (view.getParent() != LiveRenderView.this) {
                        ViewUtil.removeSelfSafely(view);
                        addView(view);
                    }

                    eventManager.post(Actions.PREVIEW_SUCCESS);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }

        private void tryPlayLive() {
            LivePlayerService playerService = getPlayerService();
            if (fromFloatWindow) {
                // 直播小窗进来时, 手动触发一次底层的createPlayerManager逻辑
                playerService.resumePlay();
                return;
            }

            // 设置显示模式
            int contentMode = parseScaleMode(false);
            Logger.i(TAG, "tryPlayLive, contentMode=" + contentMode);
            playerService.setViewContentMode(contentMode);

            playerService.tryPlay(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    if (view.getParent() != LiveRenderView.this) {
                        ViewUtil.removeSelfSafely(view);
                        removeAllViews();
                        renderView = view;
                        addView(renderView);
                    }
                    if (!liveService.hasLive()) {
//                        showToast("直播未开始");
                    }

                    eventManager.post(Actions.TRY_PLAY_LIVE_SUCCESS);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(errorMsg);
                }
            });
        }

        @Override
        public void onActivityConfigurationChanged(Configuration newConfig) {
            Logger.i(TAG, String.format("onActivityConfigurationChanged, isLandscape=%s", isLandscape()));
            updateFillModeForAudience();
        }

        private void updateFillModeForAudience() {
            if (liveService == null) {
                Logger.w(TAG, "updateFillModeForAudience, liveService == null");
            } else {
                int contentMode = getFillModeForAudience(getOpenLiveParam(), videoWidth, videoHeight);
                Logger.i(TAG, "updateFillModeForAudience, contentMode=" + contentMode);
                liveService.getPlayerService().setViewContentMode(contentMode);
            }
        }

        @Override
        public void onActivityPause() {
            if (isOwner()) {
                // 主播
            } else {
                // 观众
                if (FloatWindowManager.instance().isShowing()) {
                    // 变小窗时不做暂停
                } else {
                    LivePlayerService playerService = getPlayerService();
                    if (playerService != null) {
                        LivePrototype.OpenLiveParam openLiveParam = LivePrototype.getInstance().getOpenLiveParam();
                        if (!openLiveParam.supportBackgroundPlay) {
                            playerService.pausePlay();
                        }
                    }
                }
            }
        }

        @Override
        public void onActivityResume() {
            if (isOwner()) {
                // 主播
            } else {
                // 观众
                if (FloatWindowManager.instance().isShowing()) {
                    View floatWindowView = FloatWindowManager.instance().dismiss(false);
                    // 小窗恢复
                    fromFloatWindow = true;
                    ViewUtil.removeSelfSafely(floatWindowView);
                    removeAllViews();
                    renderView = floatWindowView;
                    addView(renderView);
                } else {
                    // 主动播放
                    LivePlayerService playerService = getPlayerService();
                    if (playerService != null) {
                        playerService.resumePlay();
                    }
                }
            }
        }

        @Override
        public void onActivityFinish() {
            // Activity离开时, 手动关闭播放器状态
            if (FloatWindowManager.instance().isShowing()) {
                // 小窗时不处理
                return;
            }

            if (!isOwner() || needPlayback()) {
                LivePlayerServiceExtends playerService = getPlayerService();
                playerService.stopPlay();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (requestCode == LivePusherService.REQUEST_CODE_CAPTURE_PERMISSION) {
                if (resultCode == Activity.RESULT_OK) {
                    Logger.i(TAG, "request capture permission success");
                    MediaProjectionPermissionResultDataHolder.setMediaProjectionPermissionResultData(data);
                    getPusherService().startPreview(null);
                } else {
                    Logger.i(TAG, "request capture permission fail");
                    DialogUtil.confirm(activity, "未授权屏幕录制, 无法进行直播", new Runnable() {
                        @Override
                        public void run() {
                            activity.finish();
                        }
                    });
                }
            }
        }

        @Override
        public void onActivityDestroy() {
            removeAllViews();
        }

        @Override
        public void onEvent(final String action, Object... args) {
            if (ACTION_SHOW_FLOAT_WINDOW.equals(action)) {
                showFloatWindow(activity);
            }
        }

        @Override
        public int getOrder() {
            return ORDER_RENDER;
        }
    }
}
