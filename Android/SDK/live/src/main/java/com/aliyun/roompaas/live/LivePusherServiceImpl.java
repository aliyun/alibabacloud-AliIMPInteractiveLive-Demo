package com.aliyun.roompaas.live;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.live.LiveDetail;
import com.alibaba.dingpaas.room.DestroyLiveCb;
import com.alibaba.dingpaas.room.DestroyLiveReq;
import com.alibaba.dingpaas.room.DestroyLiveRsp;
import com.alibaba.dingpaas.room.RoomExtInterface;
import com.alibaba.dingpaas.room.RoomModule;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.beauty_base.BeautyCompat;
import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.live.exposable.BusinessOptions;
import com.aliyun.roompaas.live.exposable.LiveEventHandler;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.player.exposable.CanvasScale;

import java.util.Map;

/**
 * @author puke
 * @version 2022/4/19
 */
class LivePusherServiceImpl implements LivePusherService {

    private static final String TAG = LivePusherServiceImpl.class.getSimpleName();

    private final LiveServiceImpl.LiveServiceContext serviceContext;
    private final Context context;
    private final RoomExtInterface roomExtInterface;
    private final AliLivePusherOptions aliLivePusherOptions;

    private LivePushManager livePushManager;

    public LivePusherServiceImpl(LiveServiceImpl.LiveServiceContext serviceContext, AliLivePusherOptions aliLivePusherOptions) {
        this.serviceContext = serviceContext;
        this.context = serviceContext.getContext();
        this.roomExtInterface = RoomModule.getModule(serviceContext.getUserId()).getExtInterface();
        this.aliLivePusherOptions = aliLivePusherOptions;
    }

    @Override
    public void startPreview(Callback<View> callback) {
        if (!serviceContext.isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            callback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        if (noPermission(Manifest.permission.CAMERA)
                || noPermission(Manifest.permission.RECORD_AUDIO)) {
            callback.onError(Errors.OS_PERMISSION_DENIED.getMessage());
            return;
        }

        // 主播
        View previewLive = previewLive();
        if (callback != null) {
            callback.onSuccess(previewLive);
        }
    }

    private boolean noPermission(String permission) {
        int checkResult = context.checkPermission(
                permission, android.os.Process.myPid(), Process.myUid());
        return checkResult != PackageManager.PERMISSION_GRANTED;
    }

    private View previewLive() {
        return getPushManager().startPreview();
    }

    @Override
    public void startLive(Callback<View> callback) {
        startLive(null, callback);
    }

    @Override
    public void startLive(BusinessOptions businessOptions, Callback<View> callback) {
        startLive(businessOptions, null, callback);
    }

    @Override
    public void startLive(BusinessOptions businessOptions, final String pushUrl, Callback<View> callback) {
        final UICallback<View> uiCallback = new UICallback<>(callback);
        if (!serviceContext.isOwner()) {
            // 观众不允许调用开播
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        serviceContext.getLiveHelper().getPushStreamUrl(businessOptions, new Callback<String>() {
            @Override
            public void onSuccess(String pushStreamUrl) {
                Logger.i(TAG, "startLive, liveId: " + serviceContext.getLiveId());
                String finalPushUrl = TextUtils.isEmpty(pushUrl) ? pushStreamUrl : pushUrl;
                doStartLive(finalPushUrl, uiCallback);
            }

            @Override
            public void onError(String errorMsg) {
                uiCallback.onError(errorMsg);
            }
        });
    }

    @Override
    public void setPreviewMode(@CanvasScale.Mode int mode) {
        getPushManager().setViewContentMode(mode);
    }

    private void doStartLive(final String pushStreamUrl, final Callback<View> callback) {
        final SurfaceView renderView;
        try {
            renderView = getPushManager().startPublish(pushStreamUrl);
        } catch (Exception e) {
            e.printStackTrace();
            Logger.e(TAG, "start publish error", e);
            callback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        callback.onSuccess(renderView);
    }

    @Override
    public void pauseLive() {
        if (!serviceContext.isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            return;
        }
        if (livePushManager != null) {
            livePushManager.pauseLive(true);
        }
    }

    @Override
    public void resumeLive() {
        if (!serviceContext.isOwner()) {
            Logger.e(TAG, "audience hasn't permission");
            return;
        }
        if (livePushManager != null) {
            livePushManager.resumeLive();
        }
    }

    @Override
    public void switchCamera() {
        getPushManager().switchCamera();
    }

    @Override
    public void setPreviewMirror(boolean mirror) {
        getPushManager().setPreviewMirror(mirror);
    }

    @Override
    public void setPushMirror(boolean mirror) {
        getPushManager().setPushMirror(mirror);
    }

    @Override
    public void setBeautyOn(boolean isBeautyOpen) {
        getPushManager().setBeautyOn(isBeautyOpen);
    }

    @Override
    public void updateBeautyOptions(AliLiveBeautyOptions beautyOptions) {
        getPushManager().updateBeautyOptions(beautyOptions);
    }

    @Override
    public void setMutePush(boolean mute) {
        getPushManager().setMute(mute);
    }

    @Override
    public void stopLive(Callback<Void> callback) {
        stopLive(true, callback);
    }

    @Override
    public void stopLive(boolean destroyLive, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!serviceContext.isOwner()) {
            // 观众不允许调用停播
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        final String liveId = serviceContext.getLiveId();
        if (liveId == null) {
            // 取不到liveId时仍认为调用成功, 不做额外处理
            Logger.w(TAG, "stopLive: the liveId is null");
            stopPublishIfNeed();
            uiCallback.onSuccess(null);
            return;
        }

        if (!destroyLive) {
            // 不销毁直播实例 (为了兼容样板间中同时调用服务端场景化和原子能力的stopLive导致的锁获取问题)
            serviceContext.removeLiveId(liveId);
            stopPublishIfNeed();
            uiCallback.onSuccess(null);
            return;
        }

        DestroyLiveReq req = new DestroyLiveReq();
        req.liveId = liveId;
        req.roomId = serviceContext.getRoomId();
        roomExtInterface.destroyLive(req, new DestroyLiveCb() {
            @Override
            public void onSuccess(DestroyLiveRsp rsp) {
                // 结束直播时, 停止推流
                serviceContext.removeLiveId(liveId);
                stopPublishIfNeed();
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    private void stopPublishIfNeed() {
        if (livePushManager != null) {
            livePushManager.stopPublish();
        }
    }

    @Override
    public void restartLive() {
        if (!serviceContext.isOwner()) {
            // 观众不允许调用开播
            Logger.e(TAG, "audience hasn't permission");
            return;
        }

        long startTime = System.currentTimeMillis();
        getPushManager().reconnectPushAsync();
        long duration = System.currentTimeMillis() - startTime;
        Logger.i(TAG, String.format("restartLive take %sms", duration));
    }

    @Override
    public void setFlash(boolean open) {
        if (livePushManager != null) {
            livePushManager.setFlash(open);
        }
    }

    @Override
    public boolean isCameraSupportFlash() {
        return livePushManager != null && livePushManager.isCameraSupportFlash();
    }

    @Override
    public void setZoom(int zoom) {
        if (livePushManager != null) {
            livePushManager.setZoom(zoom);
        }
    }

    @Override
    public int getCurrentZoom() {
        if (livePushManager != null) {
            return livePushManager.getCurrentZoom();
        }
        return 0;
    }

    @Override
    public int getMaxZoom() {
        if (livePushManager != null) {
            return livePushManager.getMaxZoom();
        }
        return 0;
    }

    @Override
    public void focusCameraAtAdjustedPoint(float x, float y, boolean autoFocus) {
        if (livePushManager != null) {
            livePushManager.focusCameraAtAdjustedPoint(x, y, autoFocus);
        }
    }


    @Override
    public BeautyInterface getBeautyInterface() {
        return livePushManager == null ? null : livePushManager.getBeautyInterface();
    }

    @Override
    public void startScreenCapture(Activity activity, Callback<Void> callback) {
        Logger.i(TAG, "startScreenCapture");
        UICallback<Void> uiCallback = new UICallback<>(callback);
        if (Build.VERSION.SDK_INT >= 21) {
            MediaProjectionManager mediaProjectionManager = (MediaProjectionManager)
                    activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            try {
                activity.startActivityForResult(
                        mediaProjectionManager.createScreenCaptureIntent(), REQUEST_CODE_CAPTURE_PERMISSION);
            } catch (ActivityNotFoundException ex) {
                ex.printStackTrace();
                Logger.e(TAG, "Start ScreenRecording failed, current device is NOT suuported!");
                uiCallback.onError("当前设备不支持录屏");
                return;
            }
            uiCallback.onSuccess(null);
        } else {
            Logger.i(TAG, "version is low: " + Build.VERSION.SDK_INT);
            uiCallback.onError("录屏需要5.0版本以上");
        }
    }

    void destroy() {
        getPushManager().destroy();
        MediaProjectionPermissionResultDataHolder.setMediaProjectionPermissionResultData(null);
        AlivcLivePushConfig.setMediaProjectionPermissionResultData(null);
    }

    private class LiveSdkEventListener implements LivePushManager.Callback {

        @Override
        public void onEvent(final LiveEvent event, @Nullable final Map<String, Object> extras) {
            LiveDetail liveDetail = serviceContext.getLiveDetail();
            // 推流成功, 上报直播状态
            if (event == LiveEvent.PUSH_STARTED) {
                if (liveDetail != null
                        && liveDetail.liveInfo != null
                        && liveDetail.liveInfo.status == 1) {
                    // 直播已经开始时, 不再重复上报
                } else {
                    serviceContext.getLiveHelper().reportLiveStatus();
                }
            }

            serviceContext.dispatch(new EventHandlerManager.Consumer<LiveEventHandler>() {
                @Override
                public void consume(LiveEventHandler eventHandler) {
                    // 透出到外部
                    // 1. 兼容老版本
                    eventHandler.onPusherEvent(event);
                    // 2. 新版本
                    eventHandler.onPusherEvent(event, extras);
                }
            });
        }

        @Override
        public boolean isScreenCaptureMode() {
            return MediaProjectionPermissionResultDataHolder.getMediaProjectionPermissionResultData() != null;
        }
    }

    // 懒加载处理, 使用时再获取
    @NonNull
    private LivePushManager getPushManager() {
        if (livePushManager == null) {
            livePushManager = createLivePushManager();
        }
        return livePushManager;
    }

    @NonNull
    private LivePushManager createLivePushManager() {
        LivePushManager pushManager = new LivePushManager(context, aliLivePusherOptions);
        String secret = BeautyCompat.forSecret(roomExtInterface.getQueenProSecret(), roomExtInterface.getQueenLiteSecret());
        if (!TextUtils.isEmpty(secret)) {
            pushManager.setQueenSecret(secret);
        }
        pushManager.setCallback(new LiveSdkEventListener());
        return pushManager;
    }
}
