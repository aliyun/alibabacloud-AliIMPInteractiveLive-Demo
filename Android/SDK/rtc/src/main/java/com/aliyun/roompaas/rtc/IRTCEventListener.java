package com.aliyun.roompaas.rtc;

import com.alivc.rtc.AliRtcEngine;

import java.util.List;

public abstract class IRTCEventListener {

    public void onRemoteTrackAvailableNotify(String uid, AliRtcEngine.AliRtcAudioTrack audioTrack,
                                             AliRtcEngine.AliRtcVideoTrack videoTrack) {
    }

    public void onRemoteUserOnLineNotify(String uid, int elapsed) {

    }

    public void onRemoteUserOffLineNotify(String uid, AliRtcEngine.AliRtcUserOfflineReason reason) {

    }

    @Deprecated
    public void onJoinChannelResult(int result, String channel, int elapsed) {
    }

    public void onJoinChannelResult(int result, String channel, String uid, int elapsed) {
    }

    public void onLeaveChannelResult(int result, AliRtcEngine.AliRtcStats stats) {

    }

    public void onAudioPublishStateChanged(AliRtcEngine.AliRtcPublishState oldState, AliRtcEngine.AliRtcPublishState newState, int elapseSinceLastState, String channel) {
    }

    public void onVideoPublishStateChanged(AliRtcEngine.AliRtcPublishState oldState, AliRtcEngine.AliRtcPublishState newState, int elapseSinceLastState, String channel) {
    }

    public void onUserAudioMuted(String uid, boolean isMute) {
    }

    public void onUserVideoMuted(String uid, boolean isMute) {
    }

    public void onNetworkQualityChanged(String uid, AliRtcEngine.AliRtcNetworkQuality upQuality, AliRtcEngine.AliRtcNetworkQuality downQuality) {
    }

    public void onConnectionLost() {
    }

    public void onTryToReconnect() {
    }

    public void onConnectionRecovery() {
    }

    public void onConnectionStatusChange(AliRtcEngine.AliRtcConnectionStatus status, AliRtcEngine.AliRtcConnectionStatusChangeReason reason) {
    }

    public void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> speakers, int totalVolume) {
    }

    public void onActiveSpeaker(String uid) {
    }

    public void onRtcLocalVideoStats(AliRtcEngine.AliRtcLocalVideoStats aliRtcStats) {
    }

    public void onRtcLocalAudioStats(AliRtcEngine.AliRtcLocalAudioStats aliRtcStats) {
    }

    public void onAliRtcStats(AliRtcEngine.AliRtcStats aliRtcStats) {
    }

    public void onRtcRemoteVideoStats(AliRtcEngine.AliRtcRemoteVideoStats aliRtcStats) {
    }

    public void onRtcRemoteAudioStats(AliRtcEngine.AliRtcRemoteAudioStats aliRtcStats) {
    }
}
