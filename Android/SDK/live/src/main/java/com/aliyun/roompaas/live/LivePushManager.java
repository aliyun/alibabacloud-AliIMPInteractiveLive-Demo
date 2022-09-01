package com.aliyun.roompaas.live;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.alibaba.dingpaas.monitorhub.MonitorhubEvent;
import com.alibaba.dingpaas.monitorhub.MonitorhubStatusType;
import com.alivc.component.custom.AlivcLivePushCustomDetect;
import com.alivc.component.custom.AlivcLivePushCustomFilter;
import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcLivePushError;
import com.alivc.live.pusher.AlivcLivePushErrorListener;
import com.alivc.live.pusher.AlivcLivePushInfoListener;
import com.alivc.live.pusher.AlivcLivePushNetworkListener;
import com.alivc.live.pusher.AlivcLivePushStats;
import com.alivc.live.pusher.AlivcLivePushStatsInfo;
import com.alivc.live.pusher.AlivcLivePusher;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcQualityModeEnum;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.monitor.MonitorHeartbeatManager;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.beauty_base.BeautyCompat;
import com.aliyun.roompaas.beauty_base.module.AliLiveBeautyType;
import com.aliyun.roompaas.beauty_base.module.BeautyImageFormat;
import com.aliyun.roompaas.live.cloudconfig.GeneralEncodeParamDelegate;
import com.aliyun.roompaas.live.cloudconfig.ILiveMediaStrategy;
import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;
import com.aliyun.roompaas.player.exposable.CanvasScale;

import java.util.HashMap;
import java.util.Map;

import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
import static com.alivc.live.pusher.AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;

/**
 * 媒体推流服务
 */
public class LivePushManager {

    private static final String TAG = LivePushManager.class.getSimpleName();
    private static final String LIVE_PUSH_CONFIG_EXTRA_INFO = "channel_aliyun_solution";

    private AlivcLivePusher mALivcLivePusher;
    private AliLivePusherOptions mAliLivePusherOptions;
    private SurfaceView mSurfaceView;
    private final ILiveMediaStrategy liveMediaStrategy;

    private boolean isStartPush = false;
    // 美颜开关,默认开启
    private boolean isBeautyOpen = true;
    private boolean isPause = false;

    // 当前直播推流地址
    private String mCurrentPublishUrl;
    private final Context mContext;

    private Callback callback;
    private String queenSecret;
    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;
    private SurfaceHolder.Callback surfaceViewCallback;

    public LivePushManager(Context context, AliLivePusherOptions aliLivePusherOptions) {
        mContext = context;
        mAliLivePusherOptions = aliLivePusherOptions;
        liveMediaStrategy = GeneralEncodeParamDelegate.getInstance();
        init();
    }

    private void init() {
        if (mALivcLivePusher == null) {
            mALivcLivePusher = new AlivcLivePusher();
        }
        final AlivcLivePushConfig alivcLivePushConfig;
        if (mAliLivePusherOptions == null) {
            alivcLivePushConfig = liveMediaStrategy.updateLivePushConfig(null);
        } else {
            alivcLivePushConfig = convertAlivcLivePushConfig(mAliLivePusherOptions);
        }

        // 兼容推流SDK, AlivcLivePushConfig实例化时会把内部的静态变量置空
        Intent permissionResultData = MediaProjectionPermissionResultDataHolder.getMediaProjectionPermissionResultData();
        if (permissionResultData != null) {
            AlivcLivePushConfig.setMediaProjectionPermissionResultData(permissionResultData);
        }

        mALivcLivePusher.init(mContext, alivcLivePushConfig);
        mALivcLivePusher.setLivePushInfoListener(pushInfoListener);
        mALivcLivePusher.setLivePushNetworkListener(pushNetworkListener);
        mALivcLivePusher.setLivePushErrorListener(pushErrorListener);
        mALivcLivePusher.setCustomFilter(new AlivcLivePushCustomFilter() {
            @Override
            public void customFilterCreate() {
                initBeautyManager();
            }

            @Override
            public void customFilterUpdateParam(float v, float v1, float v2, float v3, float v4, float v5, float v6) {

            }

            @Override
            public void customFilterSwitch(boolean b) {

            }

            @Override
            public int customFilterProcess(int inputTexture, int textureWidth, int textureHeight, long extra) {
                int ret = mBeautyManager != null ? mBeautyManager.onTextureInput(inputTexture, textureWidth, textureHeight) : inputTexture;

                return ret;
            }

            @Override
            public void customFilterDestroy() {
                destroyBeautyManager();
            }
        });

        mALivcLivePusher.setCustomDetect(new AlivcLivePushCustomDetect() {
            @Override
            public void customDetectCreate() {
                initBeautyManager();
            }

            @Override
            public long customDetectProcess(long data, int width, int height, int rotation, int format, long extra) {
                if (mBeautyManager != null) {
                    mBeautyManager.onDrawFrame(data, BeautyImageFormat.kNV21, width, height, 0, mCameraId);
                }

                return 0;
            }

            @Override
            public void customDetectDestroy() {
                destroyBeautyManager();
            }
        });

        MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
    }

    @Nullable
    private BeautyInterface mBeautyManager;

    public BeautyInterface getBeautyInterface() {
        return mBeautyManager;
    }

    @SuppressWarnings("unchecked")
    private void initBeautyManager() {
        if (mBeautyManager == null) {
            Logger.d(TAG, "initBeautyManager start");

            try {
                Class<BeautyInterface> beautyImplClz = BeautyCompat.forValidBeautyImpl();
                if (beautyImplClz == null) {
                    Logger.d(TAG, "initBeautyManager parseValidBeautyImpl: empty impl");
                    return;
                }
                mBeautyManager = beautyImplClz.getConstructor(Context.class, String.class).newInstance(mContext, queenSecret);
                // initialize in texture thread.
                mBeautyManager.init();
                mBeautyManager.setBeautyEnable(isBeautyOpen);
                Logger.d(TAG, "initBeautyManager end");
            } catch (Throwable e) {
                Logger.e(TAG, "initBeautyManager: end--invalid param: " + e);
            }
        }
    }

    private void destroyBeautyManager() {
        if (mBeautyManager != null) {
            mBeautyManager.release();
            mBeautyManager = null;
        }
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    protected void onEvent(LiveEvent event) {
        onEvent(event, null);
    }

    protected void onEvent(LiveEvent event, Map<String, Object> extras) {
        if (callback != null) {
            callback.onEvent(event, extras);
        }
    }

    //转换为媒体层AlivcLivePushConfig参数
    private AlivcLivePushConfig convertAlivcLivePushConfig(AliLivePusherOptions options) {
        AlivcLivePushConfig alivcLivePushConfig = new AlivcLivePushConfig();
        alivcLivePushConfig.setExtraInfo(LIVE_PUSH_CONFIG_EXTRA_INFO);

        AliLiveBeautyOptions beautyOptions = options.beautyOptions;
        updateQueenBeautyConfigs(beautyOptions);

        AliLiveMediaStreamOptions mediaStreamOptions = options.mediaStreamOptions;
        if (mediaStreamOptions != null) {
            alivcLivePushConfig.setQualityMode(AlivcQualityModeEnum.QM_CUSTOM);
            alivcLivePushConfig.setEnableBitrateControl(true);
            alivcLivePushConfig.setVideoOnly(mediaStreamOptions.isVideoOnly);
            alivcLivePushConfig.setAudioOnly(mediaStreamOptions.isAudioOnly);
            alivcLivePushConfig.setTargetVideoBitrate(mediaStreamOptions.videoBitrate);
            alivcLivePushConfig.setFps(mediaStreamOptions.fps);
            alivcLivePushConfig.setResolution(mediaStreamOptions.getResolution());
            alivcLivePushConfig.setCameraType(mediaStreamOptions.getCameraType());
            alivcLivePushConfig.setVideoEncodeMode(mediaStreamOptions.getEncodeMode());
            alivcLivePushConfig.setVideoEncodeGop(mediaStreamOptions.getEncodeGop());
            alivcLivePushConfig.setPreviewOrientation(mediaStreamOptions.getPreviewOrientation());
            alivcLivePushConfig.setPreviewDisplayMode(mediaStreamOptions.getPreviewDisplayMode());
            switch (mediaStreamOptions.getCameraType()) {
                case CAMERA_TYPE_BACK:
                    mCameraId = CAMERA_TYPE_BACK.getCameraId();
                    break;
                case CAMERA_TYPE_FRONT:
                    mCameraId = CAMERA_TYPE_FRONT.getCameraId();
                    break;
            }
        }
        alivcLivePushConfig.setAudioEncodeMode(AlivcEncodeModeEnum.Encode_MODE_HARD);
        alivcLivePushConfig.setVideoEncodeMode(AlivcEncodeModeEnum.Encode_MODE_HARD);

        liveMediaStrategy.updateLivePushConfig(alivcLivePushConfig);

        return alivcLivePushConfig;
    }

    /**
     * 切换闪光灯
     *
     * @param open 是否开启
     */
    public void setFlash(boolean open) {
        if (mALivcLivePusher != null && isPreviewedOrPushed()) {
            mALivcLivePusher.setFlash(open);
        }
    }

    /**
     * @return 判断是否支持闪光灯
     */
    public boolean isCameraSupportFlash() {
        return mALivcLivePusher != null && mALivcLivePusher.isCameraSupportFlash();
    }

    /**
     * @param zoom
     */
    public void setZoom(int zoom) {
        if (mALivcLivePusher != null && isPreviewedOrPushed()) {
            mALivcLivePusher.setZoom(zoom);
        }
    }

    public int getCurrentZoom() {
        if (mALivcLivePusher != null && isPreviewedOrPushed()) {
            return mALivcLivePusher.getCurrentZoom();
        }
        return 0;
    }

    public int getMaxZoom() {
        if (mALivcLivePusher != null && isPreviewedOrPushed()) {
            return mALivcLivePusher.getMaxZoom();
        }
        return 0;
    }

    private boolean isPreviewedOrPushed() {
        AlivcLivePushStats pushStats = mALivcLivePusher.getCurrentStatus();
        return pushStats == AlivcLivePushStats.PREVIEWED || pushStats == AlivcLivePushStats.PUSHED;
    }

    private void resumePublish() {
        if (TextUtils.isEmpty(mCurrentPublishUrl)) {
            Logger.w(TAG, "resumePublish publishUrl is empty");
            return;
        }
        if (mALivcLivePusher == null) {
            Logger.w(TAG, "resumePublish mALivcLivePusher is null");
            return;
        }

        AlivcLivePushStats currentStatus = mALivcLivePusher.getCurrentStatus();
        if (currentStatus == AlivcLivePushStats.PUSHED) {
            Logger.w(TAG, "resumePublish currentStatus is already pushed");
            return;
        }

        if (mSurfaceView == null) {
            Logger.w(TAG, "resumePublish mSurfaceView is null");
            return;
        }

        // 未预览时, 先开启预览
        if (currentStatus != AlivcLivePushStats.PREVIEWED) {
            if (currentStatus == AlivcLivePushStats.INIT) {
                Logger.i(TAG, "resumePublish start preview");
                doStartPreviewByMediaSdk(mSurfaceView);
            } else {
                Logger.w(TAG, "resumePublish currentStatus is error");
                return;
            }
        }

        // 开始推流
        Logger.i(TAG, "resumePublish start publish");
        startPublish(mCurrentPublishUrl);
    }

    /**
     * 开启推流
     *
     * @param url
     * @return
     */
    public SurfaceView startPublish(@NonNull String url) {
        MonitorHubChannel.reportNormalEvent(MonitorhubEvent.MHEVT_CLIENT_PUBLISH_START_LIVE,
                null, 0, null);
        if (url == null || url.length() == 0) {
            Logger.e(TAG, "startPublish url must not null");
            return null;
        }
        if (isStartPush
                || (mALivcLivePusher.isPushing() && url.equals(mALivcLivePusher.getPushUrl()))) {
            Logger.i(TAG, "startPublish url is same");
            return mSurfaceView;
        }
        if (mALivcLivePusher.isPushing()) {
            mALivcLivePusher.stopPush();
        }
        mCurrentPublishUrl = url;
        mALivcLivePusher.startPush(url);
        isStartPush = true;
        return mSurfaceView;
    }

    /**
     * 获取当前推流地址
     *
     * @return
     */
    public String getCurrentPublishUrl() {
        return mCurrentPublishUrl;
    }

    /**
     * 结束推流
     */
    public void stopPublish() {
        stopPublish(true);
    }

    /**
     * 结束推流
     */
    private void stopPublish(boolean reportEvent) {
        if (mALivcLivePusher != null && mALivcLivePusher.isPushing()) {
            try {
                mALivcLivePusher.stopPush();
            } catch (Exception e) {
                Logger.e(TAG, "stopPublish error", e);
                MonitorHubChannel.stopLive(MonitorHubChannel.REPORT_EVENT_ERROR_CODE, e.getMessage());
            }
            isStartPush = false;
//            mCurrentPublishUrl = null;
        }
        stopPreview();
        if (reportEvent) {
            MonitorHubChannel.stopLive(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }
    }

    private SurfaceView createSurfaceView() {
        final SurfaceView surfaceView = new SurfaceView(mContext);
        surfaceViewCallback = new SurfaceHolder.Callback() {

            private AlivcLivePushStats lastStatus;

            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (mALivcLivePusher == null) {
                    return;
                }

                AlivcLivePushStats currentStatus = mALivcLivePusher.getCurrentStatus();
                Logger.i(TAG, String.format("surfaceCreated, lastStatus is %s, currentStatus is %s",
                        lastStatus, currentStatus));

                if (isPause) {
                    // 暂停状态不继续预览和推流
                    Logger.i(TAG, "surfaceCreated, pusher is paused");
                    return;
                }

                if (!isScreenCaptureMode() && currentStatus != AlivcLivePushStats.PREVIEWED) {
                    doStartPreviewByMediaSdk(surfaceView);
                }

                resumeLive();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                lastStatus = mALivcLivePusher == null ? null : mALivcLivePusher.getCurrentStatus();
                Logger.i(TAG, String.format("surfaceDestroyed, lastStatus is %s", lastStatus));
                pauseLive(false);
            }
        };
        surfaceView.getHolder().addCallback(surfaceViewCallback);
        return surfaceView;
    }

    private boolean isScreenCaptureMode() {
        return callback != null && callback.isScreenCaptureMode();
    }

    private void doStartPreviewByMediaSdk(SurfaceView surfaceView) {
        if (mALivcLivePusher != null) {
            try {
                mALivcLivePusher.startPreview(surfaceView);
            } catch (IllegalArgumentException | IllegalStateException e) {
                Logger.e(TAG, "surface create error", e);
            }
        }
    }

    public void setViewContentMode(@CanvasScale.Mode int mode) {
        if (mALivcLivePusher != null) {
            mALivcLivePusher.setPreviewMode(convertToAlivcPreviewDisplayMode(mode));
        }
    }

    public static AlivcPreviewDisplayMode convertToAlivcPreviewDisplayMode(@CanvasScale.Mode int mode) {
        switch (mode) {
            case CanvasScale.Mode.SCALE_FILL:
                return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL;
            case CanvasScale.Mode.ASPECT_FIT:
                return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT;
            default:
            case CanvasScale.Mode.ASPECT_FILL:
                return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL;
        }
    }

    /**
     * 开始预览
     */
    public View startPreview() {
        if (mALivcLivePusher == null) {
            init();
        }

        if (isScreenCaptureMode()) {
            Logger.i(TAG, "startPreview by screen capture mode");
            try {
                mALivcLivePusher.startPreview(null);
            } catch (Exception e) {
                e.printStackTrace();
                Logger.e(TAG, "startPreview by screen capture mode error", e);
            }
            return null;
        }

        if (mSurfaceView == null) {
            mSurfaceView = createSurfaceView();
        } else {
            doStartPreviewByMediaSdk(mSurfaceView);
        }

        return mSurfaceView;
    }

    private void stopPreview() {
        if (mALivcLivePusher != null) {
            try {
                mALivcLivePusher.stopPreview();
            } catch (Exception e) {
                Logger.e(TAG, "stopPreview error", e);
            }
        }
    }

    /**
     * 获取预览View
     *
     * @return
     */
    public SurfaceView getAliLiveRenderView() {
        return mSurfaceView;
    }

    /**
     * 销毁直播服务
     */
    public void destroy() {
        stopPublish();

        if (mALivcLivePusher != null) {
            mALivcLivePusher.destroy();
            mALivcLivePusher = null;
        }
    }

    /**
     * 静音播放
     */
    public void setMute(boolean mute) {
        mALivcLivePusher.setMute(mute);
    }

    /**
     * 是否开启美颜
     *
     * @param isBeautyOpen
     */
    public void setBeautyOn(boolean isBeautyOpen) {
        this.isBeautyOpen = isBeautyOpen;
        mALivcLivePusher.setBeautyOn(isBeautyOpen);
        if (mBeautyManager != null) {
            mBeautyManager.setBeautyEnable(isBeautyOpen);
        }
    }

    /**
     * 更新美颜参数
     *
     * @param options
     */
    public void updateBeautyOptions(AliLiveBeautyOptions options) {
        if (options == null) {
            return;
        }
        if (mALivcLivePusher == null) {
            return;
        }
        if (isPushDisable() || !isBeautyOpen) {
            return;
        }
        updateQueenBeautyConfigs(options);
    }

    private void updateQueenBeautyConfigs(AliLiveBeautyOptions beautyOptions) {
        if (beautyOptions != null && isBeautyOpen && mBeautyManager != null) {
            mBeautyManager.setBeautyEnable(true);
            mBeautyManager.setBeautyType(BeautyCompat.kSkinWhiting, true);
            mBeautyManager.setBeautyParams(BeautyCompat.kBPSkinWhitening, BeautyCompat.formatParam(beautyOptions.beautyWhite));
            mBeautyManager.setBeautyParams(BeautyCompat.kBPSkinRed, BeautyCompat.formatParam(beautyOptions.beautyRuddy));

            mBeautyManager.setBeautyType(AliLiveBeautyType.kAliLiveSkinBuffing, true);
            mBeautyManager.setBeautyParams(BeautyCompat.kBPSkinSharpen, BeautyCompat.formatParam(beautyOptions.skinSharpen));
            mBeautyManager.setBeautyParams(BeautyCompat.kBPSkinBuffing, BeautyCompat.formatParam(beautyOptions.beautyBuffing));
        }
    }

    /**
     * 暂停推流
     */
    public void pauseLive(boolean setPauseState) {
        if (mALivcLivePusher == null) {
            return;
        }

        if (isScreenCaptureMode()) {
            if (setPauseState) {
                isPause = true;
            }
            pauseScreenCapture();
            return;
        }

        AlivcLivePushStats pushStats = mALivcLivePusher.getCurrentStatus();
        if (pushStats != AlivcLivePushStats.PUSHED &&
                pushStats != AlivcLivePushStats.PAUSED &&
                pushStats != AlivcLivePushStats.PREVIEWED) {
            return;
        }
        try {
            mALivcLivePusher.pause();
            if (setPauseState) {
                isPause = true;
            }
        } catch (Throwable e) {
            Logger.e(TAG, "pauseLive: error:", e);
            MonitorHubChannel.pauseLive(MonitorHubChannel.REPORT_EVENT_ERROR_CODE, e.getMessage());
        }
        MonitorHubChannel.pauseLive(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
    }

    /**
     * 暂停推流 (使用V2方案, 底层推流SDK提供的pause会有音画不同步、空帧回放、后台使用提醒、无断流消息这些问题)
     */
    public void pauseLiveV2() {
        if (mALivcLivePusher == null) {
            return;
        }
        isPause = true;
        stopPublish(false);
        MonitorHubChannel.pauseLive(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
    }

    /**
     * 恢复推流
     */
    public void resumeLiveV2() {
        isPause = false;
        resumePublish();
    }

    /**
     * 恢复推流
     */
    public void resumeLive() {
        if (mALivcLivePusher == null) {
            return;
        }

        if (isScreenCaptureMode()) {
            resumeScreenCapture();
            isPause = false;
            return;
        }

        AlivcLivePushStats pushStats = mALivcLivePusher.getCurrentStatus();
        if (pushStats != AlivcLivePushStats.PAUSED &&
                pushStats != AlivcLivePushStats.ERROR) {
            return;
        }
        try {
            mALivcLivePusher.resume();
            isPause = false;
        } catch (Throwable e) {
            Logger.e(TAG, "resumeLive: error", e);
        }
    }


    private void resumeScreenCapture() {
        try {
            mALivcLivePusher.resumeScreenCapture();
        } catch (Throwable e) {
            Logger.e(TAG, "resumeScreenCapture: error:", e);
        }
    }

    private void pauseScreenCapture() {
        try {
            mALivcLivePusher.pauseScreenCapture();
        } catch (Throwable e) {
            Logger.e(TAG, "pauseScreenCapture: error:", e);
        }
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        if (mALivcLivePusher == null || isPushDisable()) {
            return;
        }
        if (mCameraId == CAMERA_TYPE_FRONT.getCameraId()) {
            mCameraId = CAMERA_TYPE_BACK.getCameraId();
        } else {
            mCameraId = CAMERA_TYPE_FRONT.getCameraId();
        }
        mALivcLivePusher.switchCamera();

        if (mBeautyManager != null) {
            mBeautyManager.switchCameraId(mCameraId);
        }
    }

    /**
     * 设置预览镜像
     *
     * @param mirror
     */
    public void setPreviewMirror(boolean mirror) {
        if (mALivcLivePusher == null) {
            return;
        }
        if (isPushDisable()) {
            return;
        }
        mALivcLivePusher.setPreviewMirror(mirror);
    }

    /**
     * 设置推流镜像
     *
     * @param mirror
     */
    public void setPushMirror(boolean mirror) {
        if (mALivcLivePusher == null) {
            return;
        }
        if (isPushDisable()) {
            return;
        }
        mALivcLivePusher.setPushMirror(mirror);
    }

    private boolean isPushDisable() {
        AlivcLivePushStats pushStats = mALivcLivePusher.getCurrentStatus();
        return pushStats != AlivcLivePushStats.PREVIEWED && pushStats != AlivcLivePushStats.PUSHED;
    }

    protected void setQueenSecret(String secret) {
        this.queenSecret = secret;
    }

    /**
     * 重新推流
     * 推流状态下或者接收到所有Error相关回调状态下可调用重新推流, 且Error状态下只可以调用此接口(或者reconnectPushAsync重连)或者调用destory销毁推流。
     */
    public void restartPush() {
        if (mALivcLivePusher == null) {
            return;
        }
        try {
            mALivcLivePusher.restartPush();
        } catch (IllegalArgumentException | IllegalStateException e) {
            Logger.e(TAG, "restartPush error", e);
        }
    }

    /**
     * 推流状态下或者接收到AlivcLivePusherNetworkDelegate相关的Error回调状态下可调用此接口,
     * 且Error状态下只可以调用此接口(或者restartPush重新推流)或者调用destory销毁推流。完成后推流重连，重新链接推流RTMP
     */
    public void reconnectPushAsync() {
        if (mALivcLivePusher == null) {
            return;
        }
        try {
            mALivcLivePusher.reconnectPushAsync(mCurrentPublishUrl);
        } catch (IllegalArgumentException | IllegalStateException e) {
            Logger.e(TAG, "restartPush error", e);
        }
    }

    // region listener
    AlivcLivePushInfoListener pushInfoListener = new AlivcLivePushInfoListener() {

        @Override
        public void onPreviewStarted(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.PREVIEW_STARTED);
        }

        @Override
        public void onPreviewStoped(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.PREVIEW_STOPPED);
        }

        @Override
        public void onPushStarted(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.PUSH_STARTED);
            MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.LIVE_PUSH);
            MonitorHubChannel.rtmpConnect(alivcLivePusher.getPushUrl(),
                    MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onFirstAVFramePushed(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.FIRST_FRAME_PUSHED);
        }

        @Override
        public void onPushPauesed(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.PUSH_PAUSED);
        }

        @Override
        public void onPushResumed(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.PUSH_RESUMED);
        }

        @Override
        public void onPushStoped(AlivcLivePusher alivcLivePusher) {
            MonitorHubChannel.rtmpClose(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
            onEvent(LiveEvent.PUSH_STOPPED);
        }

        @Override
        public void onPushRestarted(AlivcLivePusher alivcLivePusher) {

        }

        @Override
        public void onFirstFramePreviewed(AlivcLivePusher alivcLivePusher) {
            onEvent(LiveEvent.FIRST_FRAME_PREVIEWED);
        }

        @Override
        public void onDropFrame(AlivcLivePusher alivcLivePusher, int i, int i1) {

        }

        @Override
        public void onAdjustBitRate(AlivcLivePusher alivcLivePusher, int curBr, int targetBr) {
            MonitorHubChannel.bitrateChange(String.valueOf(curBr), MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onAdjustFps(AlivcLivePusher alivcLivePusher, int curFps, int targetFps) {
            MonitorHeartbeatManager.getInstance().setCameraCaptureFps(String.valueOf(curFps));
        }

        @Override
        public void onPushStatistics(AlivcLivePusher alivcLivePusher, AlivcLivePushStatsInfo info) {
            Map<String, Object> extras = new HashMap<>();
            extras.put("v_bitrate", info.getVideoUploadBitrate());
            extras.put("a_bitrate", info.getAudioUploadBitrate());
            onEvent(LiveEvent.UPLOAD_BITRATE_UPDATED, extras);

            MonitorHeartbeatManager.getInstance().setAppCpu(String.valueOf(info.getCpu()));
            MonitorHeartbeatManager.getInstance().setAppMem(String.valueOf(info.getMemory()));
            MonitorHeartbeatManager.getInstance().setScreenWidth(true, String.valueOf(info.getVideoEncodingWidth()));
            MonitorHeartbeatManager.getInstance().setScreenHeight(true, String.valueOf(info.getVideoEncodingHeight()));
            MonitorHeartbeatManager.getInstance().setAudioEncodeBitrate(true, String.valueOf(info.getAudioEncodeBitrate() / 1000));
            MonitorHeartbeatManager.getInstance().setAudioSentBitrate(true, String.valueOf(info.getAudioUploadBitrate()));
            MonitorHeartbeatManager.getInstance().setAudioSentFps(String.valueOf(info.getAudioUploadFps()));
            MonitorHeartbeatManager.getInstance().setScreenEncodeFPS(true, String.valueOf(info.getVideoEncodeFps()));
            MonitorHeartbeatManager.getInstance().setScreenCaptureFPS(String.valueOf(info.getVideoCaptureFps()));
            MonitorHeartbeatManager.getInstance().setScreenSentFPS(true, String.valueOf(info.getVideoUploadeFps()));
            MonitorHeartbeatManager.getInstance().setScreenSentBitrate(true, String.valueOf(info.getVideoUploadBitrate()));
        }
    };

    AlivcLivePushNetworkListener pushNetworkListener = new AlivcLivePushNetworkListener() {
        @Override
        public void onNetworkPoor(AlivcLivePusher pusher) {
            onEvent(LiveEvent.NETWORK_POOR);
        }

        @Override
        public void onNetworkRecovery(AlivcLivePusher pusher) {
            onEvent(LiveEvent.NETWORK_RECOVERY);
        }

        @Override
        public void onReconnectStart(AlivcLivePusher pusher) {
            onEvent(LiveEvent.RECONNECT_START);
            MonitorHubChannel.netReconnectStart("", pusher.getPushUrl(), MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onReconnectFail(AlivcLivePusher pusher) {
            onEvent(LiveEvent.RECONNECT_FAIL);
        }

        @Override
        public void onReconnectSucceed(AlivcLivePusher pusher) {
            onEvent(LiveEvent.RECONNECT_SUCCESS);
            MonitorHubChannel.netReconnectSuccess(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onSendDataTimeout(AlivcLivePusher pusher) {
        }

        @Override
        public void onConnectFail(AlivcLivePusher pusher) {
            onEvent(LiveEvent.CONNECTION_FAIL);
            MonitorHubChannel.netDisconnect(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onConnectionLost(AlivcLivePusher pusher) {
            //推流已断开
            onEvent(LiveEvent.CONNECTION_LOST);
            MonitorHubChannel.netDisconnect(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public String onPushURLAuthenticationOverdue(AlivcLivePusher pusher) {
            if (pusher != null) {
                return pusher.getPushUrl();
            }
            return null;
        }

        @Override
        public void onSendMessage(AlivcLivePusher pusher) {
        }

        @Override
        public void onPacketsLost(AlivcLivePusher pusher) {
        }
    };

    AlivcLivePushErrorListener pushErrorListener = new AlivcLivePushErrorListener() {

        @Override
        public void onSystemError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            if (error != null) {
                Logger.e(TAG, error.toString());
                reportPushLowPerformance(error);
            }
        }

        @Override
        public void onSDKError(AlivcLivePusher livePusher, AlivcLivePushError error) {
            if (error != null) {
                Logger.e(TAG, error.toString());
                reportPushLowPerformance(error);
            }
        }
    };

    private void reportPushLowPerformance(AlivcLivePushError error) {
        if (error.getCode() == AlivcLivePushError.ALIVC_PUSHER_ERROR_SDK_LIVE_PUSH_LOW_PERFORMANCE.getCode()) {
            MonitorHubChannel.encodeLowFPS(error.getCode(), error.getMsg());
        }
    }

    public void focusCameraAtAdjustedPoint(float x, float y, boolean autoFocus) {
        if (mALivcLivePusher != null) {
            try {
                mALivcLivePusher.focusCameraAtAdjustedPoint(x, y, autoFocus);
            } catch (Exception e) {
                Logger.e(TAG, "focusCameraAtAdjustedPoint error", e);
            }
        }
    }
    // endregion listener

    public interface Callback {

        void onEvent(LiveEvent event, @Nullable Map<String, Object> extras);

        boolean isScreenCaptureMode();
    }
}
