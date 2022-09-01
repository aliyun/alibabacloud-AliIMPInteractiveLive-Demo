package com.aliyun.roompaas.rtc.exposable;

import android.view.View;

import com.alibaba.dingpaas.rtc.ConfInfoModel;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.PluginService;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.rtc.RtcApplyUserParam;
import com.aliyun.roompaas.rtc.RtcLayoutModel;

import java.util.Collection;
import java.util.List;

/**
 * @author puke
 * @version 2021/6/21
 */
@SuppressWarnings("UnusedReturnValue")
public interface RtcService extends PluginService<RtcEventHandler> {

    /**
     * @return 判断是否有Rtc
     */
    boolean hasRtc();

    /**
     * 查询Rtc在线用户列表
     *
     * @param param    查询参数
     * @param callback 回调函数
     */
    void listRtcUser(RtcUserParam param, Callback<PageModel<ConfUserModel>> callback);

    /**
     * 查询Rtc申请连麦的用户列表
     *
     * @param param    查询参数
     * @param callback 回调函数
     */
    void listRtcApplyUser(RtcApplyUserParam param, Callback<PageModel<ConfUserModel>> callback);

    /**
     * 开始Rtc预览
     */
    View startRtcPreview();

    /**
     * 进入会议
     *
     * <pre>
     * 1. 大班课
     *    1.1 老师端:
     *        1.1.1 开始上课: 入会, 上麦, 订阅(enterSeat, startPublish)
     *              1.1.1.1 开始推流: 旁路推流(pushLiveStream)
     *              1.1.1.2 停止推流: 停止旁路推流
     *        1.1.2 停止上课: 下麦, 离会
     *    1.2 学生端: 旁路拉流(rtc待添加)
     *        1.2.1 被邀请上麦: 入会, 上麦, 订阅, 关闭旁路拉流
     *        1.2.2 下麦: 开始旁路拉流, 取消订阅, 下麦, 离会
     *
     * 2. 小班课
     *    2.1 老师端: 入会, 上麦, 订阅(enterSeat, startPublish)
     *    2.2 学生端: 入会, 上麦, 订阅(enterSeat, startPublish)
     * </pre>
     *
     * @param nick 入会昵称
     */
    void joinRtc(String nick);

    /**
     * 进入会议
     *
     * @param config RTC配置参数
     * @param nick   昵称
     */
    @Deprecated
    void joinRtcWithConfig(RtcStreamConfig config, String nick);

    /**
     * 设置推流相关配置 (startPreview之前调用)
     *
     * @param rtcStreamConfig 推流相关配置
     */
    void setRtcStreamConfigBeforePreview(RtcStreamConfig rtcStreamConfig);

    /**
     * 设置自定义的旁路推流的布局
     */
    void setCustomBypassLiveLayout(Collection<RTCBypassPeerVideoConfig> configCollection, Callback<Void> callback);

    /**
     * 开始旁路推流
     *
     * @param callback 回调函数
     */
    void startRoadPublish(Callback<Void> callback);


    /**
     * 停止旁路推流
     *
     * @param callback
     */
    void stopRoadPublish(Callback<Void> callback);

    /**
     * 停止旁路拉流
     */
    void stopPlayRoad();

    /**
     * (取消)申请入会
     *
     * @param apply    true: 申请; false: 取消申请;
     * @param callback 回调函数
     */
    void applyJoinRtc(boolean apply, Callback<Void> callback);

    /**
     * 邀请入会
     *
     * @param userModels 被邀请人Id
     * @param callback   回调函数
     */
    void inviteJoinRtc(List<ConfUserModel> userModels, Callback<Void> callback);

    /**
     * 处理申请入会事件
     *
     * @param userId   处理的用户Id
     * @param agree    true: 同意入会; false: 拒绝入会;
     * @param callback 回调函数
     */
    void handleApplyJoinRtc(String userId, boolean agree, Callback<Void> callback);

    /**
     * 上报会议状态
     *
     * @param status   用户状态
     * @param callback 回调函数
     */
    void reportJoinStatus(RtcUserStatus status, Callback<Void> callback);

    /**
     * 拒绝会议邀请
     *
     * @param callback 回调函数
     */
    void rejectInvite(Callback<Void> callback);

    /**
     * 会议踢人
     *
     * @param userIds  目标用户Id
     * @param callback 回调函数
     */
    void kickUserFromRtc(List<String> userIds, Callback<Void> callback);

    /**
     * 查询会议详情
     *
     * @param callback 回调函数
     */
    void getRtcDetail(Callback<ConfInfoModel> callback);

    /**
     * 查询会议详情
     *
     * @param conferenceId 会议ID
     * @param callback     回调函数
     */
    void queryRtcDetail(String conferenceId, Callback<ConfInfoModel> callback);

    /**
     * @return 获取会议详情
     */
    ConfInfoModel getRtcDetail();

    /**
     * 下麦 + 离会
     *
     * @param destroyRtc 是否需要销毁会议
     */
    void leaveRtc(boolean destroyRtc);

    /**
     * 开始预览画面
     */
    void startPreview();

    /**
     * 结束预览画面
     */
    void stopPreview();

    /**
     * 设置远端视频渲染参数
     *
     * @param videoCanvas 视频画布
     * @param userId      用户Id
     * @param videoTrack  视频轨道
     */
    void setRemoteViewConfig(AliRtcEngine.AliRtcVideoCanvas videoCanvas, String userId,
                             AliRtcEngine.AliRtcVideoTrack videoTrack);

    /**
     * 设置本地视频流渲染参数
     *
     * @param videoCanvas 视频画布
     * @param videoTrack  视频轨道
     */
    void setLocalViewConfig(AliRtcEngine.AliRtcVideoCanvas videoCanvas,
                            AliRtcEngine.AliRtcVideoTrack videoTrack);

    /**
     * 订阅远端相机流，默认为订阅大流，手动订阅
     *
     * @param userId       用户Id
     * @param isMainStream true 优先订阅大流，false订阅小流
     * @param enable       true 订阅远端相机流，false停止订阅远端相机流
     */
    void configRemoteCameraTrack(String userId, boolean isMainStream, boolean enable);

    /**
     * 静音，成功返回true
     *
     * @param muteLocalMic 返回0为成功
     * @return 是否发布本地音频流
     */
    void muteLocalMic(boolean muteLocalMic);

    /**
     * 静音远端用户
     *
     * @param uid
     * @param mute
     */
    void muteRemoteAudioPlaying(String uid, boolean mute);

    /**
     * 静音所有远端用户
     *
     * @param mute
     */
    void muteAllRemoteAudioPlaying(boolean mute);

    /**
     * 关闭摄像头，成功返回true
     *
     * @param muteLocalCamera 返回0为切换成功
     * @return 是否发布本地相机流
     */
    void muteLocalCamera(boolean muteLocalCamera);

    /**
     * 切换摄像头
     *
     * @return 是够切换成功
     */
    boolean switchCamera();

    /**
     * 是否开启本地预览镜像，默认为是
     * 仅对前置摄像头画面生效
     *
     * @param enable true时打开
     */
    void setPreviewMirror(boolean enable);

    /**
     * 是否开启视频流镜像，默认为否
     * 仅对前置摄像头画面生效
     *
     * @param enable true时打开
     */
    void setVideoStreamMirror(boolean enable);

    /**
     * 设置RTC混流布局格式
     */
    void setLayout(List<String> userIds, RtcLayoutModel layoutModel, Callback<Void> callback);

    /**
     * RTC录制
     */
    void startRecord(Callback callback);

    /**
     * 结束RTC录制
     */
    void stopRecord(Callback callback);

    /**
     * 是否发布音频流
     *
     * @param enable
     */
    int publishLocalAudio(boolean enable);

    /**
     * 是否发布视频流
     *
     * @param enable
     */
    int publishLocalVideo(boolean enable);

    /**
     * 禁用或重新启用本地视频采集
     *
     * @return
     */
    int enableLocalVideo(boolean enable);

    /**
     * 停止或恢复指定远端用户的音频流拉取
     *
     * @param uid userId
     * @param sub 是否订阅
     */
    int subscribeRemoteAudioStream(String uid, boolean sub);

    /**
     * 停止或恢复接收所有远端音频流
     *
     * @param sub ture
     * @return
     */
    int subscribeAllRemoteAudioStreams(boolean sub);

    /**
     * 停止或恢复特定远端用户的视频流拉取
     *
     * @param uid
     * @param track
     * @param sub
     * @return
     */
    int subscribeRemoteVideoStream(String uid, AliRtcEngine.AliRtcVideoTrack track, boolean sub);

    /**
     * 停止或恢复接收所有远端视频流
     *
     * @param sub
     * @return
     */
    int subscribeAllRemoteVideoStreams(boolean sub);

    /**
     * 设置纯音频模式 还是 音视频模式
     *
     * @param audioOnly true: 只有音频推拉流 false
     * @return
     */
    int setAudioOnlyMode(boolean audioOnly);

    /**
     * 获取音频或音视频模式
     *
     * @return
     */
    boolean isAudioOnly();

    /**
     * @param enable          基础美颜开关
     * @param whiteningLevel  美白等级[0-1.0]
     * @param smoothnessLevel 磨皮等级[0-1.0]
     * @brief 设置基础美颜
     */
    int setBasicFaceBeauty(boolean enable, float whiteningLevel, float smoothnessLevel);

    /**
     * @param beautyOn 美颜开关
     */
    void setBeautyOn(boolean beautyOn);

    /**
     * 开始屏幕分享
     *
     * @return
     */
    int startScreenShare();

    /**
     * 结束屏幕分享
     *
     * @return
     */
    int stopScreenShare();

    /**
     * 添加水印
     *
     * @param track
     * @param imageUrl
     * @param config
     * @return
     */
    int addVideoWatermark(AliRtcEngine.AliRtcVideoTrack track, String imageUrl, AliRtcEngine.AliRtcWatermarkConfig config);

    /**
     * 设置远端视频流显示模式
     *
     * @param remoteVideoStreamShowMode 参:{@link VideoStreamShowMode}
     */
    void setRemoteVideoStreamShowMode(VideoStreamShowMode remoteVideoStreamShowMode);

    /**
     * 设置错误Toast提示
     * 默认配置：开启，最短间隔取值为5s
     * @param trueForOn 是否打开：true打开
     * @param shortestIntervalInSeconds 提示之间最短时间间隔
     */
    void configErrorToast(boolean trueForOn, long shortestIntervalInSeconds);
}
