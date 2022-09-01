package com.aliyun.roompaas.rtc;

import android.view.View;

import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.aliyun.roompaas.base.BaseDestroy;
import com.aliyun.roompaas.rtc.exposable.RtcEventHandler;
import com.aliyun.roompaas.rtc.exposable.VideoStream;
import com.aliyun.roompaas.rtc.exposable.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfCommandEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfHandleApplyEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfStopRingEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;

import java.util.List;

/**
 * @author puke
 * @version 2021/7/2
 */
public class SampleRtcEventHandler extends BaseDestroy implements RtcEventHandler {

    @Override
    public void onRtcJoinRtcSuccess(View view) {

    }

    @Override
    public void onRtcLeaveRtcSuccess() {
        
    }

    @Override
    public void onRtcJoinRtcError(String event) {

    }

    @Deprecated
    @Override
    public void onRtcStreamIn(RtcStreamEvent event) {

    }

    @Override
    public void onRemoteVideoStreamChanged(VideoStream videoStream) {

    }

    @Deprecated
    @Override
    public void onRtcStreamOut(String event) {

    }

    @Override
    public void onRemoteUserOnLineNotify(String uid, AliRtcRemoteUserInfo userInfo, int elapsed) {

    }

    @Override
    public void onRemoteUserOffLineNotify(String uid, AliRtcRemoteUserInfo userInfo, AliRtcEngine.AliRtcUserOfflineReason reason) {

    }

    @Override
    public void onRtcStreamUpdate(RtcStreamEvent event) {

    }

    @Deprecated
    @Override
    public void onRtcRemoteJoinSuccess(ConfUserEvent event) {

    }

    @Override
    public void onRtcRemoteJoinFail(ConfUserEvent event) {

    }

    @Override
    public void onRtcInviteRejected(List<ConfUserModel> rejectInviteUsers) {

    }

    @Override
    public void onRtcConfUpdated(ConfEvent event) {

    }

    @Override
    public void onRtcRingStopped(ConfStopRingEvent event) {

    }

    @Override
    public void onRtcUserInvited(ConfInviteEvent event) {

    }

    @Override
    public void onRtcKickUser(ConfUserEvent event) {

    }

    @Deprecated
    @Override
    public void onRtcLeaveUser(ConfUserEvent leaveUserEvent) {

    }

    @Override
    public void onRtcStart(ConfEvent confStartEvent) {

    }

    @Override
    public void onRtcEnd(ConfEvent confEndEvent) {

    }

    @Override
    public void onRtcCommand(ConfCommandEvent event) {

    }

    @Override
    public void onRtcApplyJoinChannel(ConfApplyJoinChannelEvent event) {

    }

    @Override
    public void onRtcHandleApplyChannel(ConfHandleApplyEvent event) {

    }

    @Override
    public void onRtcNetworkQualityChanged(String uid) {

    }

    /**
     * 网络断开回调
     */
    @Override
    public void onConnectionLost() {

    }

    /**
     * 尝试网络重连回调
     */
    @Override
    public void onTryToReconnect() {

    }

    /**
     * 网络重连成功回调
     */
    @Override
    public void onConnectionRecovery() {

    }

    /**
     * 网络连接状态改变的回调
     *
     * @param status 状态
     * @param reason 原因
     */
    @Override
    public void onConnectionStatusChange(AliRtcEngine.AliRtcConnectionStatus status, AliRtcEngine.AliRtcConnectionStatusChangeReason reason) {

    }

    @Override
    @Deprecated
    public void onRtcUserAudioMuted(String uid) {

    }

    @Override
    @Deprecated
    public void onRtcUserAudioEnable(String uid) {

    }

    @Deprecated
    @Override
    public void onRtcUserVideoMuted(String uid) {

    }

    @Deprecated
    @Override
    public void onRtcUserVideoEnable(String uid) {

    }

    @Deprecated
    @Override
    public void onSelfAudioMuted(boolean isMute) {

    }

    @Deprecated
    @Override
    public void onSelfVideoMuted(boolean isMute) {

    }

    @Override
    public void onOthersAudioMuted(String uid, boolean isMute) {

    }

    @Deprecated
    @Override
    public void onOthersVideoMuted(String uid, boolean isMute) {

    }

    @Override
    public void onRtcUserAudioMutedError(boolean mute, String uid) {

    }

    @Override
    public void onRtcUserVideoMutedError(boolean mute, String uid) {

    }

    @Override
    public void onRtcPositiveMuteMic(boolean mute, String uid) {

    }

    @Override
    public void onRtcPassiveMuteMic(boolean mute, String uid) {

    }

    @Override
    public void onRtcMuteAllMic(boolean mute) {

    }

    @Override
    public void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> speakers, int totalVolume) {

    }

    @Override
    public void onActiveSpeaker(String uid) {

    }
}
