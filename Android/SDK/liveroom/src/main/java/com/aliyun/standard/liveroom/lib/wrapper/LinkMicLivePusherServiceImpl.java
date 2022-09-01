package com.aliyun.standard.liveroom.lib.wrapper;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.live.BeautyInterface;
import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.live.exposable.BusinessOptions;
import com.aliyun.roompaas.live.exposable.LivePusherService;
import com.aliyun.roompaas.live.exposable.LiveService;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.exposable.RTCBypassPeerVideoConfig;
import com.aliyun.roompaas.rtc.exposable.RtcStreamConfig;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.linkmic.enums.ContentMode;
import com.aliyun.standard.liveroom.lib.linkmic.impl.CommonServiceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * 连麦直播的实现
 *
 * @author puke
 * @version 2021/9/26
 */
class LinkMicLivePusherServiceImpl extends CommonServiceImpl implements LinkMicPusherService {

    private static final String TAG = LinkMicLivePusherServiceImpl.class.getSimpleName();

    private final LiveService liveService;
    private final LivePusherService pusherService;

    LinkMicLivePusherServiceImpl(RoomChannel roomChannel, LiveService liveService, LivePusherService pusherService) {
        super(roomChannel);
        this.liveService = liveService;
        this.pusherService = pusherService;
    }

    @Override
    protected RtcStreamConfig getRtcStreamConfig() {
        RtcStreamConfig config = new RtcStreamConfig();
        boolean isVertical = !LivePrototype.getInstance().getOpenLiveParam().screenLandscape;
        if (isVertical) {
            // 竖屏
            config.setWidth(720);
            config.setHeight(1280);
            config.setVideoStreamTypeLowPublished(false);
            config.setBypassLiveResolutionType(RtcStreamConfig.BypassLiveResolutionType.Type_720x1280);
        } else {
            // 横屏
            config.setWidth(1280);
            config.setHeight(720);
            config.setVideoStreamTypeLowPublished(false);
            config.setBypassLiveResolutionType(RtcStreamConfig.BypassLiveResolutionType.Type_1280x720);
        }
        return config;
    }

    @Override
    public void startPreview(Callback<View> callback) {
        View view = openCamera();
        if (callback != null) {
            callback.onSuccess(view);
        }
    }

    @Override
    public void startLive(Callback<View> callback) {
        startLive(null, null, callback);
    }

    @Override
    public void startLive(BusinessOptions businessOptions, Callback<View> callback) {
        startLive(businessOptions, null, callback);
    }

    @Override
    public void startLive(BusinessOptions businessOptions, String pushUrl, Callback<View> callback) {
        UICallback<View> uiCallback = new UICallback<>(callback);
        if (!roomChannel.isOwner()) {
            // 观众不允许调用开播
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        if (!TextUtils.isEmpty(pushUrl)) {
            if (callback != null) {
                callback.onError("连麦场景不支持手动设置pushUrl");
            }
            return;
        }

        join();
        if (callback != null) {
            callback.onSuccess(openCamera());
        }
    }

    @Override
    protected void onSelfJoinRtcSuccess() {
        rtcService.startRoadPublish(new Callbacks.Log<Void>(TAG, "startRoadPublish"));
    }

    @Override
    public void setPreviewMode(int mode) {
        final ContentMode contentMode;
        switch (mode) {
            case CanvasScale.Mode.SCALE_FILL:
                contentMode = ContentMode.Stretch;
                break;
            case CanvasScale.Mode.ASPECT_FIT:
                contentMode = ContentMode.Fill;
                break;
            case CanvasScale.Mode.ASPECT_FILL:
            default:
                contentMode = ContentMode.Crop;
                break;
        }
        setPreviewContentMode(contentMode);
    }

    @Override
    public void pauseLive() {
        rtcService.stopRoadPublish(new Callbacks.Log<Void>(TAG, "pauseLive"));
    }

    @Override
    public void resumeLive() {
        // TODO: 2022/4/28 开始旁路推流
        rtcService.startRoadPublish(new Callbacks.Log<Void>(TAG, "startRoadPublish"));
    }

    @Override
    public void setPushMirror(boolean mirror) {
        setCameraStreamMirror(mirror);
    }

    @Override
    public void updateBeautyOptions(AliLiveBeautyOptions beautyOptions) {
        Logger.w(TAG, "updateBeautyOptions not supported for link mic case");
    }

    @Override
    public void setMutePush(boolean mute) {
        Logger.w(TAG, "setMutePush not supported for link mic case");
    }

    @Override
    public void stopLive(Callback<Void> callback) {
        UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!roomChannel.isOwner()) {
            // 观众不允许调用停播
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        StopLiveHelper.stop(liveService, pusherService, null);

        rtcService.stopRoadPublish(callback);
        // 离会时, 销毁rtc
        rtcService.leaveRtc(true);
    }

    @Override
    public void stopLive(boolean destroyLive, Callback<Void> callback) {
        throw new UnsupportedOperationException("The method is not supported.");
    }

    @Override
    public void restartLive() {
        Logger.w(TAG, "restartLive not supported for link mic case");
    }

    @Override
    public void setFlash(boolean open) {
        Logger.w(TAG, "setFlash not supported for link mic case");
    }

    @Override
    public boolean isCameraSupportFlash() {
        Logger.w(TAG, "isCameraSupportFlash not supported for link mic case");
        return false;
    }

    @Override
    public void setZoom(int zoom) {
        Logger.w(TAG, "isCameraSupportFlash not supported for link mic case");
    }

    @Override
    public int getCurrentZoom() {
        Logger.w(TAG, "isCameraSupportFlash not supported for link mic case");
        return 0;
    }

    @Override
    public int getMaxZoom() {
        Logger.w(TAG, "isCameraSupportFlash not supported for link mic case");
        return 0;
    }

    @Override
    public void focusCameraAtAdjustedPoint(float x, float y, boolean autoFocus) {

    }

    @Override
    public BeautyInterface getBeautyInterface() {
        return null;
    }

    @Override
    public void startScreenCapture(Activity activity, Callback<Void> callback) {
        Logger.w(TAG, "startScreenCapture not supported for link mic case");
    }

    @Override
    public void invite(List<String> userIds, Callback<Void> callback) {
        List<ConfUserModel> userModels = new ArrayList<>();
        for (String userId : userIds) {
            ConfUserModel userModel = new ConfUserModel();
            userModel.userId = userId;
            // TODO: 2022/1/4 待确认（@泊涯）
//            userModel.nickname = userId;
            userModels.add(userModel);
        }
        rtcService.inviteJoinRtc(userModels, callback);
    }

    @Override
    public void cancelInvite(List<String> userIds, Callback<Void> callback) {
        rtcService.kickUserFromRtc(userIds, callback);
    }

    @Override
    public void handleApply(String userId, boolean agree, Callback<Void> callback) {
        rtcService.handleApplyJoinRtc(userId, agree, callback);
    }

    @Override
    public void kick(List<String> userIds, Callback<Void> callback) {
        UICallback<Void> uiCallback = new UICallback<>(callback);
        if (!roomChannel.isOwner()) {
            // 观众不允许调用踢人
            Logger.e(TAG, "audience hasn't permission");
            uiCallback.onError(Errors.BIZ_PERMISSION_DENIED.getMessage());
            return;
        }

        rtcService.kickUserFromRtc(userIds, callback);
    }

    @Override
    public void setEnumBypassLiveLayout(RtcLayoutModel layout, List<String> userIds, Callback<Void> callback) {
        rtcService.setLayout(userIds, layout, callback);
    }

    @Override
    public void setCustomBypassLiveLayout(List<RTCBypassPeerVideoConfig> configs, Callback<Void> callback) {
        rtcService.setCustomBypassLiveLayout(configs, callback);
    }
}
