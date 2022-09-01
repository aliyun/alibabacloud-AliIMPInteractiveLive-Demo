package com.aliyun.standard.liveroom.lib.wrapper;

import android.app.Activity;
import android.view.View;

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
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.exposable.RTCBypassPeerVideoConfig;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.enums.ContentMode;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.List;
import java.util.Map;

/**
 * 普通直播的实现 (不带连麦)
 *
 * @author puke
 * @version 2021/9/26
 */
class NormalLivePusherServiceImpl implements LinkMicPusherService {

    private static final String NOT_SUPPORTED_TIPS = "Not supported api for current live.";
    private static final String TAG = NormalLivePusherServiceImpl.class.getSimpleName();

    private final RoomChannel roomChannel;
    private final LiveService liveService;
    private final LivePusherService pusherService;

    NormalLivePusherServiceImpl(RoomChannel roomChannel, LiveService liveService, LivePusherService pusherService) {
        this.roomChannel = roomChannel;
        this.liveService = liveService;
        this.pusherService = pusherService;
    }

    @Override
    public void startPreview(Callback<View> callback) {
        pusherService.startPreview(callback);
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
        pusherService.startLive(businessOptions, pushUrl, callback);
    }

    @Override
    public void setPreviewMode(int mode) {
        pusherService.setPreviewMode(mode);
    }

    @Override
    public void pauseLive() {
        pusherService.pauseLive();
    }

    @Override
    public void resumeLive() {
        pusherService.resumeLive();
    }

    @Override
    public void switchCamera() {
        pusherService.switchCamera();
    }

    @Override
    public void setPreviewMirror(boolean mirror) {
        pusherService.setPreviewMirror(mirror);
    }

    @Override
    public void setPushMirror(boolean mirror) {
        pusherService.setPushMirror(mirror);
    }

    @Override
    public void setBeautyOn(boolean isBeautyOpen) {
        pusherService.setBeautyOn(isBeautyOpen);
    }

    @Override
    public void updateBeautyOptions(AliLiveBeautyOptions beautyOptions) {
        pusherService.updateBeautyOptions(beautyOptions);
    }

    @Override
    public void setMutePush(boolean mute) {
        pusherService.setMutePush(mute);
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

        StopLiveHelper.stop(liveService, pusherService, callback);
    }

    @Override
    public void stopLive(boolean destroyLive, Callback<Void> callback) {
        throw new UnsupportedOperationException("The method is not supported.");
    }

    @Override
    public void restartLive() {
        pusherService.restartLive();
    }

    @Override
    public void setFlash(boolean open) {
        if (pusherService != null) {
            pusherService.setFlash(open);
        }
    }

    @Override
    public boolean isCameraSupportFlash() {
        return pusherService.isCameraSupportFlash();
    }

    @Override
    public void setZoom(int zoom) {
        pusherService.setZoom(zoom);
    }

    @Override
    public int getCurrentZoom() {
        return pusherService.getCurrentZoom();
    }

    @Override
    public int getMaxZoom() {
        return pusherService.getMaxZoom();
    }

    @Override
    public void focusCameraAtAdjustedPoint(float x, float y, boolean autoFocus) {
        if (pusherService != null) {
            pusherService.focusCameraAtAdjustedPoint(x, y, autoFocus);
        }
    }

    @Override
    public BeautyInterface getBeautyInterface() {
        return pusherService.getBeautyInterface();
    }

    @Override
    public void startScreenCapture(Activity activity, Callback<Void> callback) {
        if (pusherService != null) {
            pusherService.startScreenCapture(activity, callback);
        }
    }

    @Override
    public boolean isJoined() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public boolean isCameraOpened() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public View openCamera() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void closeCamera() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void setRemoteCameraContentMode(ContentMode mode) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public boolean isMicOpened() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public boolean isSelfMicAllowed() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public boolean isMicAllMuted() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void openMic() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void closeMic() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public Map<String, LinkMicUserModel> getJoinedUsers() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void addEventHandler(LinkMicEventHandler eventHandler) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void removeEventHandler(LinkMicEventHandler eventHandler) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void removeAllEventHandler() {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void invite(List<String> userIds, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void cancelInvite(List<String> userIds, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void handleApply(String userId, boolean agree, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void kick(List<String> userIds, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void setEnumBypassLiveLayout(RtcLayoutModel layoutModel, List<String> userIds, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }

    @Override
    public void setCustomBypassLiveLayout(List<RTCBypassPeerVideoConfig> configs, Callback<Void> callback) {
        throw new RuntimeException(NOT_SUPPORTED_TIPS);
    }
}
