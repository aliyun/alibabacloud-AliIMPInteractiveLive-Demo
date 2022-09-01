package com.aliyun.roompaas.rtc.exposable;

import android.view.View;

import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcRemoteUserInfo;
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
public interface RtcEventHandler {

    /**
     * 入会成功
     */
    void onRtcJoinRtcSuccess(View view);

    /**
     * 离会成功
     */
    void onRtcLeaveRtcSuccess();

    /**
     * 进入会议失败
     */
    void onRtcJoinRtcError(String event);

    /**
     * 新流进入
     */
    @Deprecated
    void onRtcStreamIn(RtcStreamEvent event);

    /**
     * 远端视频流发生改变
     *
     * @param videoStream 视频流数据
     */
    void onRemoteVideoStreamChanged(VideoStream videoStream);

    /**
     * 老流退出
     */
    @Deprecated
    void onRtcStreamOut(String event);

    /**
     * 用户上线通知
     *
     * @param uid      userId
     * @param userInfo 用户信息
     * @param elapsed  时长
     */
    void onRemoteUserOnLineNotify(String uid, AliRtcRemoteUserInfo userInfo, int elapsed);

    /**
     * 用户下线通知
     *
     * @param uid      userId
     * @param userInfo 用户信息
     * @param reason   原因
     */
    void onRemoteUserOffLineNotify(final String uid, AliRtcRemoteUserInfo userInfo, AliRtcEngine.AliRtcUserOfflineReason reason);

    /**
     * 流更新
     */
    @Deprecated
    void onRtcStreamUpdate(RtcStreamEvent event);


    /**
     * 入会成功
     */
    @Deprecated
    void onRtcRemoteJoinSuccess(ConfUserEvent event);

    /**
     * 入会失败
     */
    void onRtcRemoteJoinFail(ConfUserEvent event);

    /**
     * 拒绝邀请
     *
     * @param rejectInviteUsers 拒绝邀请的用户
     */
    void onRtcInviteRejected(List<ConfUserModel> rejectInviteUsers);

    /**
     * 会议状态变更消息
     */
    void onRtcConfUpdated(ConfEvent event);

    /**
     * 停止响铃消息
     */
    void onRtcRingStopped(ConfStopRingEvent event);

    /**
     * 邀请消息
     */
    void onRtcUserInvited(ConfInviteEvent event);

    /**
     * 挂断消息
     */
    void onRtcKickUser(ConfUserEvent event);

    /**
     * RTC离会
     *
     * @param leaveUserEvent
     */
    @Deprecated
    void onRtcLeaveUser(ConfUserEvent leaveUserEvent);

    /**
     * RTC会议开始
     *
     * @param confStartEvent
     */
    void onRtcStart(ConfEvent confStartEvent);

    /**
     * RTC会议结束
     *
     * @param confEndEvent
     */
    void onRtcEnd(ConfEvent confEndEvent);

    /**
     * 命令消息
     */
    void onRtcCommand(ConfCommandEvent event);

    /**
     * 申请连麦消息
     */
    void onRtcApplyJoinChannel(ConfApplyJoinChannelEvent event);

    /**
     * 申请连麦被拒绝
     */
    void onRtcHandleApplyChannel(ConfHandleApplyEvent event);

    /**
     * RTC 网络状态变化
     */
    void onRtcNetworkQualityChanged(String uid);

    /**
     * 网络断开回调
     */
    void onConnectionLost();

    /**
     * 尝试网络重连回调
     */
    void onTryToReconnect();

    /**
     * 网络重连成功回调
     */
    void onConnectionRecovery();

    /**
     * 网络连接状态改变的回调
     */
    void onConnectionStatusChange(AliRtcEngine.AliRtcConnectionStatus status, AliRtcEngine.AliRtcConnectionStatusChangeReason reason);

    /**
     * 音频静音
     */
    @Deprecated
    void onRtcUserAudioMuted(String uid);

    /**
     * 音频可用
     */
    @Deprecated
    void onRtcUserAudioEnable(String uid);

    /**
     * 视频静音
     */
    @Deprecated
    void onRtcUserVideoMuted(String uid);

    /**
     * 视频可用
     */
    @Deprecated
    void onRtcUserVideoEnable(String uid);

    /**
     * 自己音频被禁用
     *
     * @param isMute true禁用
     */
    @Deprecated
    void onSelfAudioMuted(boolean isMute);

    /**
     * 自己画面被禁用
     *
     * @param isMute true禁用
     */
    @Deprecated
    void onSelfVideoMuted(boolean isMute);

    /**
     * 别人音频开启/关闭
     *
     * @param uid    id
     * @param isMute true禁用
     */
    void onOthersAudioMuted(String uid, boolean isMute);

    /**
     * 别人视频开启/关闭
     *
     * @param uid    id
     * @param isMute true禁用
     */
    @Deprecated
    void onOthersVideoMuted(String uid, boolean isMute);

    /**
     * 静音失败
     */
    void onRtcUserAudioMutedError(boolean mute, String uid);

    /**
     * 开关摄像头失败
     */
    void onRtcUserVideoMutedError(boolean mute, String uid);

    /**
     * 主动静音
     */
    void onRtcPositiveMuteMic(boolean mute, String uid);

    /**
     * 被动静音
     */
    void onRtcPassiveMuteMic(boolean mute, String uid);

    /**
     * 全员静音
     */
    void onRtcMuteAllMic(boolean mute);

    /**
     * 订阅音频音量
     *
     * @param speakers    户音量信息数组，包含用户uid、语音状态以及音量。
     * @param totalVolume 混音后的总音量，取值范围：[0,255]
     */
    void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> speakers, int totalVolume);

    /**
     * 连麦中当前正在说话的人。uid为0，表示当前正在说话的是本地的人
     */
    void onActiveSpeaker(String uid);
}
