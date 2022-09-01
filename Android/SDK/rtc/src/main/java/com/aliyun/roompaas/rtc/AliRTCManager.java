package com.aliyun.roompaas.rtc;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.support.annotation.Nullable;

import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.monitorhub.MonitorhubStatusType;
import com.alibaba.fastjson.JSONObject;
import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcEngineEventListener;
import com.alivc.rtc.AliRtcEngineNotify;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.aliyun.roompaas.base.AppLifecycle;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.base.Producer;
import com.aliyun.roompaas.base.cloudconfig.base.IBaseCloudConfig;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.monitor.MonitorHeartbeatManager;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.beauty_base.BeautyCompat;
import com.aliyun.roompaas.live.BeautyInterface;
import com.aliyun.roompaas.rtc.cloudconfig.GeneralEncodeParamDelegate;
import com.aliyun.roompaas.rtc.error.RtcErrorDelegate;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.HashMap;
import java.util.List;

import static com.alivc.rtc.AliRtcEngine.AliRtcMuteLocalAudioMode.AliRtcMuteAudioModeDefault;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoStreamType.AliRtcVideoStreamTypeHigh;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoStreamType.AliRtcVideoStreamTypeLow;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera;
import static com.alivc.rtc.AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen;

/**
 * aliRtc 媒体层
 */
public class AliRTCManager implements IDestroyable {
    private static final String TAG = "AliRTCManager";
    private static final int RTC_MANAGER_NOT_INIT = -1;

    private Context mContext;
    private AliRtcEngine mAliRtcEngine;
    private SophonSurfaceView mLocalView;
    private AliRtcEngine.AliRtcRenderMirrorMode mirrorMode;
    public static final AliRtcEngine.AliRtcRenderMode DEFAULT_RENDER_MODE = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
    private boolean previewStarted;
    private boolean beautyOn = true;
    private final IBaseCloudConfig generalEncodeParamDelegate;
    private final RtcErrorDelegate errorDelegate;
    private LifecycleObserver processLifecycleObserver;

    public AliRTCManager(Context context) {
        mContext = context;
        errorDelegate = new RtcErrorDelegate(context);
        init();
        initLocalView();
        generalEncodeParamDelegate = new GeneralEncodeParamDelegate();
        generalEncodeParamDelegate.query();
    }

    private void init() {
        if (mAliRtcEngine == null) {
            AliRtcEngine.setH5CompatibleMode(1);
            JSONObject extra = new JSONObject();
            extra.put("user_specified_codec_type", "CODEC_TYPE_HARDWARE_ENCODER_SOFTWARE_DECODER");
            mAliRtcEngine = AliRtcEngine.getInstance(mContext.getApplicationContext(), extra.toString());
            //设置频道模式为互动模式
            mAliRtcEngine.setChannelProfile(AliRtcEngine.AliRTCSdkChannelProfile.AliRTCSdkInteractiveLive);
            //默认进来为观看角色
            mAliRtcEngine.setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkLive);
            // 默认发布和订阅音频流，视频流手动发布和订阅
            mAliRtcEngine.publishLocalAudioStream(true);
            mAliRtcEngine.publishLocalVideoStream(false);
            mAliRtcEngine.setDefaultSubscribeAllRemoteAudioStreams(true);
            mAliRtcEngine.setDefaultSubscribeAllRemoteVideoStreams(false);
            // 设置监听
            mAliRtcEngine.setRtcEngineEventListener(mEventListener);
            mAliRtcEngine.setRtcEngineNotify(mEngineNotify);
            // 注册音量数据监听
            enableAudioVolumeIndication(350, 3, 1);
            mAliRtcEngine.registerAudioVolumeObserver(mAudioVolumeObserver);

            mAliRtcEngine.registerLocalVideoTextureObserver(observer);
        }
    }

    // 设置本地的预览视图的view
    private void initLocalView() {
        mLocalView = SurfaceViewUtil.generateSophonSurfaceView(mContext, false);

        updateLocalViewConfig();
    }

    private void updateLocalViewConfig() {
        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = new AliRtcEngine.AliRtcVideoCanvas();
        aliVideoCanvas.view = mLocalView;
        aliVideoCanvas.renderMode = DEFAULT_RENDER_MODE;
        aliVideoCanvas.mirrorMode = mirrorMode;
        setLocalViewConfig(aliVideoCanvas, AliRtcVideoTrackCamera);
    }

    private void updatePreviewMirror(boolean enable) {
        mirrorMode = enable ? AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeOnlyFront
                : AliRtcEngine.AliRtcRenderMirrorMode.AliRtcRenderMirrorModeAllDisable;
        if (mLocalView != null) {
            updateLocalViewConfig();
        }
    }

    @Override
    public void destroy() {
        // AliRtcEngine SDK使用单例模式，不能直接destroy;已知问题：引发教师rtc画面第二次使用时方向错误。
        mAliRtcEngine = null;
        if (mLocalView != null) {
            mLocalView.destroyDrawingCache();
            mLocalView = null;
        }
        Utils.destroy(generalEncodeParamDelegate, errorDelegate);
    }

    /**
     * 加入频道
     *
     * @param userInfo
     * @param userName
     */
    public void joinChannel(final AliRtcAuthInfo userInfo, final String userName) {
        if (mAliRtcEngine == null) {
            return;
        }
        if (mAliRtcEngine.isInCall()) {
            mAliRtcEngine.leaveChannel();
        }
        Logger.i(TAG, "joinChannel userInfo : " + userInfo.toString());
        errorDelegate.takeAction(RtcErrorDelegate.Type.JOIN_CHANNEL, new Producer<Integer>() {
            @Override
            public Integer produce() {
                return mAliRtcEngine.joinChannel(userInfo, userName);
            }
        });
    }

    /**
     * 退出频道
     */
    public void leaveChannel() {
        if (mAliRtcEngine == null) {
            return;
        }
        mAliRtcEngine.stopPreview();
        AppLifecycle.INSTANCE.removeObserver(processLifecycleObserver);
        processLifecycleObserver = null;
        errorDelegate.runOnDirectRetry(new Producer<Integer>() {
            @Override
            public Integer produce() {
                return mAliRtcEngine.leaveChannel();
            }
        });
        errorDelegate.reset();
    }

    /**
     * 连麦-上麦
     */
    public void enterSeat() {
        setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkInteractive);
    }

    /**
     * 下麦
     */
    public void leaveSeat() {
        setClientRole(AliRtcEngine.AliRTCSdkClientRole.AliRTCSdkLive);
    }

    /**
     * 切换用户角色
     *
     * @param clientRole
     */
    public void setClientRole(AliRtcEngine.AliRTCSdkClientRole clientRole) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.setClientRole(clientRole);
        }
    }

    /**
     * 是否发布本地音频流
     *
     * @param muteLocalMic 返回0为成功
     * @return
     */
    public int muteLocalMic(boolean muteLocalMic) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.muteLocalMic(muteLocalMic, AliRtcMuteAudioModeDefault);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 是否发布本地相机流
     *
     * @param muteLocalCamera 返回0为切换成功
     * @return
     */
    public int muteLocalCamera(boolean muteLocalCamera) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.muteLocalCamera(muteLocalCamera, AliRtcVideoTrackCamera);
        }
        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 切换摄像头
     *
     * @return 返回0为切换成功
     */
    public int switchCamera() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.switchCamera();
        }

        return RTC_MANAGER_NOT_INIT;
    }

    public void setPreviewMirror(boolean enable) {
        updatePreviewMirror(enable);
    }

    /**
     * 开始预览画面
     */
    public void startPreview() {
        if (mAliRtcEngine == null) {
            Logger.e(TAG, "AliRtcEngine must be init");
            return;
        }
        try {
            if (previewStarted) {
                Logger.i(TAG, "startPreview end: started already");
                return;
            }
            int ret = mAliRtcEngine.startPreview();
            previewStarted = ret != -1;
            MonitorHubChannel.reportStartPreview(ret, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 结束预览画面
     */
    public void stopPreview() {
        if (mAliRtcEngine == null) {
            Logger.e(TAG, "AliRtcEngine must be init");
            return;
        }
        try {
            if (!previewStarted) {
                Logger.i(TAG, "startPreview end: not started yet");
                return;
            }
            int ret = mAliRtcEngine.stopPreview();
            if (ret != -1) {
                previewStarted = false;
            }
            MonitorHubChannel.reportStopPreview(ret, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    public SophonSurfaceView getLocalView() {
        return mLocalView;
    }

    /**
     * 停止或恢复本地扬声器
     *
     * @param mute
     */
    public void muteSpeaker(boolean mute) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.setPlayoutVolume(mute ? 0 : 100);
        }
    }

    /**
     * 停止或恢复远端用户音频播放
     *
     * @param uid
     * @param mute
     */
    public void muteRemoteAudioPlaying(String uid, boolean mute) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.muteRemoteAudioPlaying(uid, mute);
        }
    }

    /**
     * 停止或恢复远端所有的音频播放
     *
     * @param mute
     */
    public void muteAllRemoteAudioPlaying(boolean mute) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.muteAllRemoteAudioPlaying(mute);
        }
    }

    /**
     * 停止或恢复远端所有的视频渲染
     *
     * @param mute
     */
    public void muteAllRemoteVideoRendering(boolean mute) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.muteAllRemoteVideoRendering(mute);
        }
    }

    /**
     * 设置音量回调频率和平滑系数
     *
     * @param interval  时间音隔。单位毫秒，最小值不不于10ms.建议300-500.小于0表示不启用音量提示和说话人提示功能，默认为300
     * @param smooth    平滑系统。数值越大平滑程度越高。建议设置为3，取值范围【0，9】
     * @param reportVad 本地语音检测开关。1，开启；0，关闭
     * @return
     */
    public void enableAudioVolumeIndication(int interval, int smooth, int reportVad) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.enableAudioVolumeIndication(interval, smooth, reportVad);
        }
    }


    /**
     * 订阅远端相机流，默认为订阅大流，手动订阅
     *
     * @param userId       用户Id
     * @param isMainStream true 优先订阅大流，false订阅小流
     * @param enable       true 订阅远端相机流，false停止订阅无端相机流
     */
    public void configRemoteCameraTrack(String userId, boolean isMainStream, boolean enable) {
        if (mAliRtcEngine != null) {
            // 订阅远端大小流
            mAliRtcEngine.setRemoteVideoStreamType(userId, isMainStream ? AliRtcVideoStreamTypeHigh : AliRtcVideoStreamTypeLow);
            // 订阅远端相机流
            mAliRtcEngine.subscribeRemoteVideoStream(userId, AliRtcVideoTrackCamera, enable);
            // 订阅远端屏幕流。
            mAliRtcEngine.subscribeRemoteVideoStream(userId, AliRtcVideoTrackScreen, enable);
        }
    }

    /**
     * 获取用户相关的信息
     *
     * @param userId 用户Id
     * @return
     */
    public AliRtcRemoteUserInfo getUserInfo(String userId) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.getUserInfo(userId);
        }

        return null;
    }

    /**
     * 设置远端视频渲染参数
     *
     * @param aliVideoCanvas
     * @param uid
     * @param aliRtcVideoTrack
     */
    public void setRemoteViewConfig(AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas,
                                    String uid, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.setRemoteViewConfig(aliVideoCanvas, uid, aliRtcVideoTrack);
        }
    }

    /**
     * 设置本地视频流渲染参数
     *
     * @param localAliVideoCanvas
     * @param aliRtcVideoTrackCamera
     */
    public void setLocalViewConfig(AliRtcEngine.AliRtcVideoCanvas localAliVideoCanvas,
                                   AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrackCamera) {
        if (mAliRtcEngine != null) {
            samplingAutoPauseResumeProcess(mAliRtcEngine);
            mAliRtcEngine.setLocalViewConfig(localAliVideoCanvas, aliRtcVideoTrackCamera);
        }
    }

    private void samplingAutoPauseResumeProcess(final AliRtcEngine engine) {
        if (processLifecycleObserver == null) {
            processLifecycleObserver = new LifecycleObserver() {
                private boolean samplingAutoPaused;
                @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
                public void onProcessResume() {
                    Logger.i("KyleCe", "ON_RESUME");
                    if (samplingAutoPaused) {
                        samplingAutoPaused = false;
                        // 开始预览
                        startPreview();
                        // 采集 (如果关闭, 采集黑帧)
                        enableLocalVideo(true);
                    }
                }

                @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
                public void onProcessPause() {
                    Logger.i("KyleCe", "ON_PAUSE");
                    if (engine.isCameraOn()) {
                        samplingAutoPaused = true;
                        // 停止预览
                        stopPreview();
                        // 采集
                        enableLocalVideo(false);
                    }
                }
            };
            AppLifecycle.INSTANCE.addObserver(processLifecycleObserver);
        }
    }

    /**
     * 是否发布次要流
     *
     * @param enable
     */
    public void publishLocalDualStream(boolean enable) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.publishLocalDualStream(enable);
        }
    }

    /**
     * 设置VideoEncoderConfiguration
     *
     * @param configuration
     */
    public void setVideoEncoderConfiguration(AliRtcEngine.AliRtcVideoEncoderConfiguration configuration) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.setVideoEncoderConfiguration(configuration);
        }
    }

    /**
     * 禁用或启用本地视频采集
     *
     * @param enable
     * @return
     */
    public int enableLocalVideo(boolean enable) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.enableLocalVideo(enable);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 是否发布音频流
     *
     * @param enable true:发布音频流，false:不发布音频流
     */
    public int publishLocalAudio(final boolean enable) {
        if (mAliRtcEngine != null) {
            return errorDelegate.runOnDirectRetry(new Producer<Integer>() {
                @Override
                public Integer produce() {
                    return mAliRtcEngine.publishLocalAudioStream(enable);
                }
            });
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 是否发布视频流
     *
     * @param enable true:发布相机流，false:不发布相机流
     */
    public int publishLocalVideo(final boolean enable) {
        if (mAliRtcEngine != null) {
           return errorDelegate.takeAction(RtcErrorDelegate.Type.PUBLISH_VIDEO, new Producer<Integer>() {
                @Override
                public Integer produce() {
                    return mAliRtcEngine.publishLocalVideoStream(enable);
                }
            });
        }
        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 查询是否发布音频流状态
     *
     * @return
     */
    public boolean isPublishLocalAudio() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.isLocalAudioStreamPublished();
        }
        return false;
    }

    /**
     * 查询是否发布视频流状态
     *
     * @return
     */
    public boolean isPublishLocalVideo() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.isLocalVideoStreamPublished();
        }
        return false;
    }

    /**
     * 停止或恢复指定远端用户的音频流拉取
     *
     * @param uid 用户id
     * @param sub 停止或恢复 true: 恢复 false: 停止
     */
    public int subscribeRemoteAudioStream(String uid, boolean sub) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.subscribeRemoteAudioStream(uid, sub);
        }
        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 停止或恢复接收所有远端音频流
     *
     * @param sub
     * @return
     */
    public int subscribeAllRemoteAudioStreams(boolean sub) {
        if (mAliRtcEngine != null) {
            mAliRtcEngine.subscribeAllRemoteAudioStreams(sub);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 停止或恢复特定远端用户的视频流拉取
     *
     * @param uid   用户id
     * @param track 视频流类型
     * @param sub   停止或恢复 true: 恢复 false 停止
     */
    public int subscribeRemoteVideoStream(String uid, AliRtcEngine.AliRtcVideoTrack track, boolean sub) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.subscribeRemoteVideoStream(uid, track, sub);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 停止或恢复接收所有远端视频流
     *
     * @param sub
     * @return
     */
    public int subscribeAllRemoteVideoStreams(boolean sub) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.subscribeAllRemoteVideoStreams(sub);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    public int setAudioOnlyMode(boolean audioOnly) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.setAudioOnlyMode(audioOnly);
        }
        return RTC_MANAGER_NOT_INIT;
    }

    public boolean isAudioOnly() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.isAudioOnly();
        }
        return false;
    }

    public int setBeautyOption(boolean enable, AliRtcEngine.AliRtcBeautyConfig config) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.setBeautyEffect(enable, config);
        }
        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 开启屏幕共享
     *
     * @return
     */
    public int startScreenShare() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.startScreenShare();
        }

        return RTC_MANAGER_NOT_INIT;
    }

    /**
     * 关闭屏幕共享
     *
     * @return
     */
    public int stopScreenShare() {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.stopScreenShare();
        }

        return RTC_MANAGER_NOT_INIT;
    }

    public void configErrorToast(boolean trueForOn, long shortestIntervalInSeconds){
        errorDelegate.configErrorToast(trueForOn, shortestIntervalInSeconds);
    }

    /**
     * @param track
     * @param imageUrl
     * @param config
     * @return
     */
    public int addVideoWatermark(AliRtcEngine.AliRtcVideoTrack track, String imageUrl, AliRtcEngine.AliRtcWatermarkConfig config) {
        if (mAliRtcEngine != null) {
            return mAliRtcEngine.addVideoWatermark(track, imageUrl, config);
        }

        return RTC_MANAGER_NOT_INIT;
    }

    private IRTCEventListener eventListener;

    public void setEventListener(IRTCEventListener eventListener) {
        this.eventListener = eventListener;
    }

    // 本地用户行为操作回调事件
    private AliRtcEngineEventListener mEventListener = new AliRtcEngineEventListener() {
        @Override
        public void onJoinChannelResult(int result, String channel, String uid, int elapsed) {
            if (result >= 0) {
                errorDelegate.actionSuccess(RtcErrorDelegate.Type.JOIN_CHANNEL);
            } else{
                errorDelegate.actionFail(RtcErrorDelegate.Type.JOIN_CHANNEL);
            }
            if (eventListener != null) {
                Logger.d(TAG, "onJoinChannelResult: result:" + result + " uid：" + uid);
                eventListener.onJoinChannelResult(result, channel, uid, elapsed);
            }
        }

        @Override
        public void onAudioPublishStateChanged(AliRtcEngine.AliRtcPublishState oldState, AliRtcEngine.AliRtcPublishState newState, int elapseSinceLastState, String channel) {
            if (eventListener != null) {
                Logger.d(TAG, "onAudioPublishStateChanged: oldState:" + oldState + " newState：" + newState);
                eventListener.onAudioPublishStateChanged(oldState, newState, elapseSinceLastState, channel);
            }
        }

        @Override
        public void onVideoPublishStateChanged(AliRtcEngine.AliRtcPublishState oldState, AliRtcEngine.AliRtcPublishState newState, int elapseSinceLastState, String channel) {
            if (eventListener != null) {
                Logger.d(TAG, "onVideoPublishStateChanged: oldState:" + oldState + " newState：" + newState);
                eventListener.onVideoPublishStateChanged(oldState, newState, elapseSinceLastState, channel);
            }
        }

        @Override
        public void onLeaveChannelResult(int result, AliRtcEngine.AliRtcStats stats) {
            super.onLeaveChannelResult(result, stats);
            if (eventListener != null) {
                eventListener.onLeaveChannelResult(result, stats);
            }
        }

        @Override
        public void onAudioSubscribeStateChanged(String uid, AliRtcEngine.AliRtcSubscribeState oldState,
                                                 AliRtcEngine.AliRtcSubscribeState newState, int elapseSinceLastState, String channel) {
            Logger.d(TAG, "onAudioSubscribeStateChanged: uid: " + uid + " oldState" + oldState + " newState: " + newState);
            super.onAudioSubscribeStateChanged(uid, oldState, newState, elapseSinceLastState, channel);

        }

        @Override
        public void onVideoSubscribeStateChanged(String uid, AliRtcEngine.AliRtcSubscribeState oldState,
                                                 AliRtcEngine.AliRtcSubscribeState newState, int elapseSinceLastState, String channel) {
            Logger.i(TAG, "onVideoSubscribeStateChanged: uid :" + uid);
            super.onVideoSubscribeStateChanged(uid, oldState, newState, elapseSinceLastState, channel);
        }

        @Override
        public void onSubscribeStreamTypeChanged(String uid, AliRtcEngine.AliRtcVideoStreamType oldStreamType,
                                                 AliRtcEngine.AliRtcVideoStreamType newStreamType, int elapseSinceLastState, String channel) {
            Logger.i(TAG, "onSubscribeStreamTypeChanged: uid :" + uid);
            super.onSubscribeStreamTypeChanged(uid, oldStreamType, newStreamType, elapseSinceLastState, channel);
        }

        @Override
        public void onScreenShareSubscribeStateChanged(String uid, AliRtcEngine.AliRtcSubscribeState oldState,
                                                       AliRtcEngine.AliRtcSubscribeState newState, int elapseSinceLastState, String channel) {
            Logger.i(TAG, "onScreenShareSubscribeStateChanged: uid :" + uid);
            super.onScreenShareSubscribeStateChanged(uid, oldState, newState, elapseSinceLastState, channel);
        }

        @Override
        public void onOccurError(int error, String message) {
            Logger.e(TAG, "onOccurError: error :" + error + ", message : " + message);
            errorDelegate.error(error, message);
            MonitorHubChannel.reportRTCError(error, message);
        }

        @Override
        public void onPerformanceLow() {
            super.onPerformanceLow();
            MonitorHubChannel.reportPerfLow(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onPermormanceRecovery() {
            super.onPermormanceRecovery();
            MonitorHubChannel.reportPerfNormal(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onNetworkQualityChanged(String uid, AliRtcEngine.AliRtcNetworkQuality upQuality, AliRtcEngine.AliRtcNetworkQuality downQuality) {
            super.onNetworkQualityChanged(uid, upQuality, downQuality);
            if (eventListener != null) {
                eventListener.onNetworkQualityChanged(uid, upQuality, downQuality);
            }
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            if (eventListener != null) {
                eventListener.onConnectionLost();
            }
            MonitorHubChannel.reportNetDisconnect(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            MonitorHeartbeatManager.getInstance().setStatus(MonitorhubStatusType.NOT_START);
        }

        @Override
        public void onTryToReconnect() {
            super.onTryToReconnect();
            if (eventListener != null) {
                eventListener.onTryToReconnect();
            }
            MonitorHubChannel.reportNetReconnect(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onConnectionRecovery() {
            super.onConnectionRecovery();
            if (eventListener != null) {
                eventListener.onConnectionRecovery();
            }
            MonitorHubChannel.reportNetReconnectSuccess(MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onConnectionStatusChange(AliRtcEngine.AliRtcConnectionStatus status, AliRtcEngine.AliRtcConnectionStatusChangeReason reason) {
            super.onConnectionStatusChange(status, reason);
            if (eventListener != null) {
                eventListener.onConnectionStatusChange(status, reason);
            }
            MonitorHubChannel.reportNetChange(status.getValue(), reason.name());
        }
    };

    // 远端用户行为事件回调
    private AliRtcEngineNotify mEngineNotify = new AliRtcEngineNotify() {

        /**
         * 远端用户上线通知
         * @param uid
         * @param elapsed
         */
        @Override
        public void onRemoteUserOnLineNotify(String uid, int elapsed) {
            super.onRemoteUserOnLineNotify(uid, elapsed);
            Logger.i(TAG, "onRemoteUserOnLineNotify: uid : " + uid + "elapsed : " + elapsed);
            if (eventListener != null) {
                eventListener.onRemoteUserOnLineNotify(uid, elapsed);
            }
        }

        /**
         * 远端用户下线通知
         * @param uid
         * @param reason
         */
        @Override
        public void onRemoteUserOffLineNotify(String uid, AliRtcEngine.AliRtcUserOfflineReason reason) {
            super.onRemoteUserOffLineNotify(uid, reason);
            Logger.i(TAG, "onRemoteUserOffLineNotify: uid : " + uid + ", reason : " + reason);
            if (eventListener != null) {
                eventListener.onRemoteUserOffLineNotify(uid, reason);
            }
            MonitorHubChannel.reportRemoteOffline(reason.getValue(), MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        /**
         * 远端用户发布音视频流变化通知
         * @param uid
         * @param audioTrack
         * @param videoTrack
         */
        @Override
        public void onRemoteTrackAvailableNotify(String uid, AliRtcEngine.AliRtcAudioTrack audioTrack,
                                                 AliRtcEngine.AliRtcVideoTrack videoTrack) {
            super.onRemoteTrackAvailableNotify(uid, audioTrack, videoTrack);
            Logger.i(TAG, "onRemoteTrackAvailableNotify: uid : " + uid);
            if (eventListener != null) {
                eventListener.onRemoteTrackAvailableNotify(uid, audioTrack, videoTrack);
            }
        }

        /**
         * 对端用户停止音频数据发送通知
         * @param uid
         * @param isMute
         */
        @Override
        public void onUserAudioMuted(String uid, boolean isMute) {
            super.onUserAudioMuted(uid, isMute);
            if (eventListener != null) {
                eventListener.onUserAudioMuted(uid, isMute);
            }
        }

        /**
         * 对端用户停止视频频数据发送通知
         * @param uid
         * @param isMute
         */
        @Override
        public void onUserVideoMuted(String uid, boolean isMute) {
            super.onUserVideoMuted(uid, isMute);
            if (eventListener != null) {
                eventListener.onUserVideoMuted(uid, isMute);
            }
        }

        /**
         * 远端视频流首帧渲染完回调
         * @param uid
         * @param videoTrack
         * @param width
         * @param height
         * @param elapsed
         */
        @Override
        public void onFirstRemoteVideoFrameDrawn(String uid, AliRtcEngine.AliRtcVideoTrack videoTrack, int width, int height, int elapsed) {
            super.onFirstRemoteVideoFrameDrawn(uid, videoTrack, width, height, elapsed);
            HashMap<String, String> params = new HashMap<>();
            params.put(MonitorhubField.MFFIELD_COMMON_TARGET_UID, uid);
            params.put(MonitorhubField.MFFIELD_PAASSDK_RTC_SOURCE_TYPE, videoTrack.name());
            params.put(MonitorhubField.MFFIELD_COMMON_WIDTH, String.valueOf(width));
            params.put(MonitorhubField.MFFIELD_COMMON_HEIGHT, String.valueOf(height));
            params.put(MonitorhubField.MFFIELD_COMMON_COST_TIME, String.valueOf(elapsed));
            MonitorHubChannel.reportFirstFrameRender(params, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
        }

        @Override
        public void onAliRtcStats(AliRtcEngine.AliRtcStats aliRtcStats) {
            super.onAliRtcStats(aliRtcStats);
            if (eventListener != null) {
                eventListener.onAliRtcStats(aliRtcStats);
            }
        }

        @Override
        public void onRtcLocalVideoStats(AliRtcEngine.AliRtcLocalVideoStats aliRtcStats) {
            super.onRtcLocalVideoStats(aliRtcStats);
            if (eventListener != null) {
                eventListener.onRtcLocalVideoStats(aliRtcStats);
            }
        }

        @Override
        public void onRtcLocalAudioStats(AliRtcEngine.AliRtcLocalAudioStats aliRtcStats) {
            super.onRtcLocalAudioStats(aliRtcStats);
            if (eventListener != null) {
                eventListener.onRtcLocalAudioStats(aliRtcStats);
            }
        }

        @Override
        public void onRtcRemoteVideoStats(AliRtcEngine.AliRtcRemoteVideoStats aliRtcStats) {
            if (eventListener != null) {
                eventListener.onRtcRemoteVideoStats(aliRtcStats);
            }
        }

        @Override
        public void onRtcRemoteAudioStats(AliRtcEngine.AliRtcRemoteAudioStats aliRtcStats) {
            if (eventListener != null) {
                eventListener.onRtcRemoteAudioStats(aliRtcStats);
            }
        }
    };

    // 音量数据输出对象
    private AliRtcEngine.AliRtcAudioVolumeObserver mAudioVolumeObserver = new AliRtcEngine.AliRtcAudioVolumeObserver() {
        @Override
        public void onAudioVolume(List<AliRtcEngine.AliRtcAudioVolume> speakers, int totalVolume) {
//            Logger.i(TAG, "onAudioVolume: totalVolume : " + totalVolume);
            super.onAudioVolume(speakers, totalVolume);
            if (eventListener != null) {
                eventListener.onAudioVolume(speakers, totalVolume);
            }
        }

        @Override
        public void onActiveSpeaker(String uid) {
            super.onActiveSpeaker(uid);
            Logger.i(TAG, "onActiveSpeaker: uid : " + uid);
            if (eventListener != null) {
                eventListener.onActiveSpeaker(uid);
            }
        }
    };

    public AliRtcEngine getAliRTcEngine() {
        return mAliRtcEngine;
    }

    AliRtcEngine.AliRtcTextureObserver observer = new AliRtcEngine.AliRtcTextureObserver() {
        @Override
        public void onTextureCreate(long l) {
            initBeautyManager();
        }

        @Override
        public int onTextureUpdate(int inputTexture, int textureWidth, int textureHeight, AliRtcEngine.AliRtcVideoSample aliRtcVideoSample) {
            int ret = mBeautyManager != null ? mBeautyManager.onTextureUpdate(inputTexture, textureWidth, textureHeight) : inputTexture;

            return ret;
        }

        @Override
        public void onTextureDestroy() {
            destroyBeautyManager();
        }
    };

    private BeautyInterface mBeautyManager;
    protected String queenSecret;

    protected void setQueenSecret(String secret) {
        this.queenSecret = secret;
    }

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
                mBeautyManager.setBeautyEnable(beautyOn);
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

    public void setBeautyOn(boolean beautyOn) {
        this.beautyOn = beautyOn;
        if (mBeautyManager != null) {
            mBeautyManager.setBeautyEnable(beautyOn);
        }
    }
}
