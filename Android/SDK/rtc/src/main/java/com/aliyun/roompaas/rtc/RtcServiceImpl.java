package com.aliyun.roompaas.rtc;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.alibaba.dingpaas.base.DPSError;
import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.monitorhub.MonitorhubStatusType;
import com.alibaba.dingpaas.room.CreateRtcCb;
import com.alibaba.dingpaas.room.CreateRtcReq;
import com.alibaba.dingpaas.room.CreateRtcRsp;
import com.alibaba.dingpaas.room.DestroyRtcCb;
import com.alibaba.dingpaas.room.DestroyRtcReq;
import com.alibaba.dingpaas.room.DestroyRtcRsp;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomExtInterface;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.room.RoomModule;
import com.alibaba.dingpaas.room.RoomNotificationModel;
import com.alibaba.dingpaas.rtc.AddMembersCb;
import com.alibaba.dingpaas.rtc.AddMembersReq;
import com.alibaba.dingpaas.rtc.AddMembersRsp;
import com.alibaba.dingpaas.rtc.ApplyLinkMicCb;
import com.alibaba.dingpaas.rtc.ApplyLinkMicReq;
import com.alibaba.dingpaas.rtc.ApplyLinkMicRsp;
import com.alibaba.dingpaas.rtc.ApproveLinkMicCb;
import com.alibaba.dingpaas.rtc.ApproveLinkMicReq;
import com.alibaba.dingpaas.rtc.ApproveLinkMicRsp;
import com.alibaba.dingpaas.rtc.ConfInfoModel;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alibaba.dingpaas.rtc.GetConfDetailCb;
import com.alibaba.dingpaas.rtc.GetConfDetailReq;
import com.alibaba.dingpaas.rtc.GetConfDetailRsp;
import com.alibaba.dingpaas.rtc.GetTokenCb;
import com.alibaba.dingpaas.rtc.GetTokenReq;
import com.alibaba.dingpaas.rtc.GetTokenRsp;
import com.alibaba.dingpaas.rtc.KickMembersCb;
import com.alibaba.dingpaas.rtc.KickMembersReq;
import com.alibaba.dingpaas.rtc.KickMembersRsp;
import com.alibaba.dingpaas.rtc.ListApplyLinkMicUserCb;
import com.alibaba.dingpaas.rtc.ListApplyLinkMicUserReq;
import com.alibaba.dingpaas.rtc.ListApplyLinkMicUserRsp;
import com.alibaba.dingpaas.rtc.ListConfUserCb;
import com.alibaba.dingpaas.rtc.ListConfUserReq;
import com.alibaba.dingpaas.rtc.ListConfUserRsp;
import com.alibaba.dingpaas.rtc.OperateCameraCb;
import com.alibaba.dingpaas.rtc.OperateCameraReq;
import com.alibaba.dingpaas.rtc.OperateCameraRsp;
import com.alibaba.dingpaas.rtc.Pane;
import com.alibaba.dingpaas.rtc.PushLiveStreamCb;
import com.alibaba.dingpaas.rtc.PushLiveStreamReq;
import com.alibaba.dingpaas.rtc.PushLiveStreamRsp;
import com.alibaba.dingpaas.rtc.ReportJoinStatusCb;
import com.alibaba.dingpaas.rtc.ReportJoinStatusReq;
import com.alibaba.dingpaas.rtc.ReportJoinStatusRsp;
import com.alibaba.dingpaas.rtc.ReportLeaveStatusCb;
import com.alibaba.dingpaas.rtc.ReportLeaveStatusReq;
import com.alibaba.dingpaas.rtc.ReportLeaveStatusRsp;
import com.alibaba.dingpaas.rtc.ReportRtcMuteCb;
import com.alibaba.dingpaas.rtc.ReportRtcMuteReq;
import com.alibaba.dingpaas.rtc.ReportRtcMuteRsp;
import com.alibaba.dingpaas.rtc.RtcModule;
import com.alibaba.dingpaas.rtc.RtcMuteAllCb;
import com.alibaba.dingpaas.rtc.RtcMuteAllReq;
import com.alibaba.dingpaas.rtc.RtcMuteAllRsp;
import com.alibaba.dingpaas.rtc.RtcMuteUserCb;
import com.alibaba.dingpaas.rtc.RtcMuteUserReq;
import com.alibaba.dingpaas.rtc.RtcMuteUserRsp;
import com.alibaba.dingpaas.rtc.RtcRpcInterface;
import com.alibaba.dingpaas.rtc.SetCustomLayoutCb;
import com.alibaba.dingpaas.rtc.SetCustomLayoutReq;
import com.alibaba.dingpaas.rtc.SetCustomLayoutRsp;
import com.alibaba.dingpaas.rtc.SetLayoutCb;
import com.alibaba.dingpaas.rtc.SetLayoutReq;
import com.alibaba.dingpaas.rtc.SetLayoutRsp;
import com.alibaba.dingpaas.rtc.StartRecordCb;
import com.alibaba.dingpaas.rtc.StartRecordReq;
import com.alibaba.dingpaas.rtc.StartRecordRsp;
import com.alibaba.dingpaas.rtc.StopLiveStreamCb;
import com.alibaba.dingpaas.rtc.StopLiveStreamReq;
import com.alibaba.dingpaas.rtc.StopLiveStreamRsp;
import com.alibaba.dingpaas.rtc.StopRecordCb;
import com.alibaba.dingpaas.rtc.StopRecordReq;
import com.alibaba.dingpaas.rtc.StopRecordRsp;
import com.alibaba.fastjson.JSON;
import com.alivc.rtc.AliRtcAuthInfo;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.aliyun.roompaas.base.AbstractPluginService;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.ModuleRegister;
import com.aliyun.roompaas.base.RoomContext;
import com.aliyun.roompaas.base.annotation.PluginServiceInject;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.callback.UICallback;
import com.aliyun.roompaas.base.error.Errors;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.inner.module.LiveInnerService;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.monitor.MonitorHeartbeatManager;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.beauty_base.BeautyCompat;
import com.aliyun.roompaas.player.LivePlayerManager;
import com.aliyun.roompaas.rtc.cloudconfig.resolution.IResolutionAdapter;
import com.aliyun.roompaas.rtc.cloudconfig.resolution.IResolutionStrategy;
import com.aliyun.roompaas.rtc.cloudconfig.resolution.ResolutionStrategyDelegate;
import com.aliyun.roompaas.rtc.exposable.RTCBypassPeerVideoConfig;
import com.aliyun.roompaas.rtc.exposable.RtcEventHandler;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcStreamConfig;
import com.aliyun.roompaas.rtc.exposable.RtcUserParam;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;
import com.aliyun.roompaas.rtc.exposable.StreamType;
import com.aliyun.roompaas.rtc.exposable.VideoStream;
import com.aliyun.roompaas.rtc.exposable.VideoStreamShowMode;
import com.aliyun.roompaas.rtc.exposable.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfHandleApplyEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfMuteAllMicEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfMuteCameraEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfMuteMicEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfPassiveMuteMicEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfPositiveMuteMicEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfShareScreenEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author puke
 * @version 2021/6/21
 */
@PluginServiceInject
public class RtcServiceImpl extends AbstractPluginService<RtcEventHandler> implements RtcService
        , ISeatChannel, IResolutionAdapter, IDestroyable {

    private static final String TAG = RtcServiceImpl.class.getSimpleName();
    private static final String PLUGIN_ID = "rtc";

    private static final String ERROR_CODE_REJECT_INVITE = "reject_invite";

    // RTC消息
    private static final int RTC_JOIN_SUCCESS = 1;
    private static final int RTC_JOIN_FAIL = 2;
    private static final int RTC_LEAVE_CHANNEL = 3;
    private static final int RTC_KICK_USER = 4;
    private static final int RTC_START = 5;
    private static final int RTC_END = 6;
    private static final int RTC_INVITE = 7;
    private static final int RTC_APPLY = 8;
    private static final int RTC_HANDLE_APPLY = 9;
    private static final int RTC_MUTE_MIC = 10;
    private static final int RTC_MUTE_CAMERA = 11;
    private static final int RTC_SHARE_SCREEN = 12;
    private static final int RTC_POSITIVE_MUTE_MIC = 14;
    private static final int RTC_PASSIVE_MUTE_MIC = 15;
    private static final int RTC_MUTE_ALL_MIC = 16;

    static {
        ModuleRegister.registerLwpModule(RtcModule.getModuleInfo());
    }

    private final LiveInnerService liveInnerService;

    private final RoomExtInterface roomExtInterface;
    private final RtcRpcInterface rtcRpcInterface;

    private AliRTCManager aliRTCManager;
    private final LivePlayerManager livePlayerManager;

    private ConfInfoModel confInfo;
    private String nick;
    private RtcStreamConfig rtcStreamConfig;
    private static final boolean DISABLE_REMOTE_OPEN_SELF_MIC = true;// rtc mic open不主动打开 麦克风，禁用远程开麦功能
    private RTCEventListener eventListener;
    private boolean closeMic;
    private boolean closeCamera;
    private VideoStreamShowMode remoteVideoStreamShowMode = VideoStreamShowMode.Fill;
    private final IResolutionStrategy resolutionStrategy;
    private AtomicBoolean channelJoined = new AtomicBoolean();

    public RtcServiceImpl(RoomContext roomContext) {
        super(roomContext);
        liveInnerService = roomContext.getInnerService(LiveInnerService.class);

        roomExtInterface = RoomModule.getModule(userId).getExtInterface();
        rtcRpcInterface = RtcModule.getModule(userId).getRpcInterface();

        ofRefreshRTCMgr();
        livePlayerManager = new LivePlayerManager(context);
        resolutionStrategy = new ResolutionStrategyDelegate(this);
    }

    public AliRTCManager ofRefreshRTCMgr() {
        if (aliRTCManager == null) {
            aliRTCManager = new AliRTCManager(context);
            eventListener = new RTCEventListener();
            aliRTCManager.setEventListener(eventListener);
            updateBeautySecret();
        }
        return aliRTCManager;
    }

    private void updateBeautySecret() {
        String secret = BeautyCompat.forSecret(roomExtInterface.getQueenProSecret(), roomExtInterface.getQueenLiteSecret());
        if (!TextUtils.isEmpty(secret)) {
            aliRTCManager.setQueenSecret(secret);
        }
    }

    @Override
    public boolean hasRtc() {
        return getInstanceId() != null;
    }

    @Override
    public void listRtcUser(RtcUserParam param, Callback<PageModel<ConfUserModel>> callback) {
        final UICallback<PageModel<ConfUserModel>> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        ListConfUserReq req = new ListConfUserReq();
        req.confId = rtcId;
        req.pageIndex = param.pageNum;
        req.pageSize = param.pageSize;
        rtcRpcInterface.listConfUser(req, new ListConfUserCb() {
            @Override
            public void onSuccess(ListConfUserRsp rsp) {
                PageModel<ConfUserModel> pageModel = new PageModel<>();
                pageModel.list = rsp.userList;
                pageModel.total = rsp.totalCount;
                pageModel.hasMore = rsp.hasMore;
                uiCallback.onSuccess(pageModel);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void listRtcApplyUser(RtcApplyUserParam param, Callback<PageModel<ConfUserModel>> callback) {
        final UICallback<PageModel<ConfUserModel>> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        ListApplyLinkMicUserReq req = new ListApplyLinkMicUserReq();
        req.confId = rtcId;
        req.pageIndex = param.pageNum;
        req.pageSize = param.pageSize;
        rtcRpcInterface.listApplyLinkMicUser(req, new ListApplyLinkMicUserCb() {
            @Override
            public void onSuccess(ListApplyLinkMicUserRsp rsp) {
                PageModel<ConfUserModel> pageModel = new PageModel<>();
                pageModel.list = rsp.userList;
                pageModel.total = rsp.totalCount;
                pageModel.hasMore = rsp.hasMore;
                uiCallback.onSuccess(pageModel);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public View startRtcPreview() {
        ofRefreshRTCMgr().startPreview();
        return ofRefreshRTCMgr().getLocalView();
    }

    @Override
    public void joinRtc(final String nick) {
        joinRtcWithConfig(null, nick);
    }

    @Override
    public void joinRtcWithConfig(RtcStreamConfig config, final String nick) {
        // TODO: 2021/6/28 临时方案, 后续考虑移除
        this.nick = nick;
        storeRtcStreamConfigIfVital(config);
        // 查询房间直播
        List<String> rtcIds = getInstanceIds();
        // 取出第一个
        final String rtcId = CollectionUtil.getFirst(rtcIds);

        if (rtcId != null) {
            // 有rtc, 直接start
            performJoinChannel(rtcId, nick);
        } else {
            // 无rtc, 先创建
            CreateRtcReq req = new CreateRtcReq();
            req.anchorId = userId;
            RoomDetail roomDetail = roomContext.getRoomDetail();
            if (roomDetail != null) {
                RoomInfo roomInfo = roomDetail.roomInfo;
                if (roomInfo != null) {
                    req.title = roomInfo.title;
                    req.roomId = roomId;
                }
            }
            req.anchorNickname = nick;
            roomExtInterface.createRtc(req, new CreateRtcCb() {
                @Override
                public void onSuccess(CreateRtcRsp rsp) {
                    String conferenceId = rsp.conferenceId;
                    addInstanceId(conferenceId);
                    performJoinChannel(conferenceId, nick);

                    MonitorHubChannel.reportCreateRTC(conferenceId, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    final String message = "create rtc error, " + dpsError.reason;
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            eventHandler.onRtcJoinRtcError(message);
                        }
                    });
                    MonitorHubChannel.reportCreateRTC(null, dpsError.getCode(), dpsError.getReason());
                }
            });
        }
    }

    @Override
    public void setRtcStreamConfigBeforePreview(RtcStreamConfig rtcStreamConfig) {
        this.rtcStreamConfig = rtcStreamConfig;
    }

    // 入会前设置RTC配置参数，主要为大流分辨率
    private void setRtcConfig() {
        if (rtcStreamConfig != null) {
            updateVideoEncodeConfig(rtcStreamConfig);
            ofRefreshRTCMgr().publishLocalDualStream(rtcStreamConfig.isVideoStreamTypeLowPublished());
        }
        resolutionStrategy.query();
    }

    @Override
    public void updateVideoEncodeConfig(RtcStreamConfig rtcStreamConfig) {
        updateVideoEncodeConfig(rtcStreamConfig, false);
    }

    @Override
    public boolean isOwner() {
        return super.isOwner();
    }

    @Override
    public boolean isChannelJoined(){
        return channelJoined.get();
    }

    private void updateVideoEncodeConfig(RtcStreamConfig config, boolean mirrorEnabled) {
        storeRtcStreamConfigIfVital(config);
        if (rtcStreamConfig == null) {
            Logger.i(TAG, "updateVideoEncodeConfig: end--invalid param: " + null);
            return;
        }

        AliRtcEngine.AliRtcVideoEncoderConfiguration configuration = new AliRtcEngine.AliRtcVideoEncoderConfiguration();
        configuration.dimensions = new AliRtcEngine.AliRtcVideoDimensions(rtcStreamConfig.getWidth(), rtcStreamConfig.getHeight());
        configuration.mirrorMode = mirrorEnabled ? AliRtcEngine.AliRtcVideoEncoderMirrorMode.AliRtcVideoEncoderMirrorModeEnabled
                : AliRtcEngine.AliRtcVideoEncoderMirrorMode.AliRtcVideoEncoderMirrorModeDisabled;
        ofRefreshRTCMgr().setVideoEncoderConfiguration(configuration);
    }

    private void storeRtcStreamConfigIfVital(@Nullable RtcStreamConfig preferred){
        rtcStreamConfig = Utils.acceptFirstNonNull(preferred, rtcStreamConfig);
    }

    // 入会
    private void performJoinChannel(final String rtcId, final String nick) {
        // 1. 先拿到入会必需的Token信息
        GetTokenReq req = new GetTokenReq();
        req.confId = rtcId;
        rtcRpcInterface.getToken(req, new GetTokenCb() {
            @Override
            public void onSuccess(GetTokenRsp rsp) {
                final AliRtcAuthInfo authInfo = new AliRtcAuthInfo();
                Logger.i(TAG, "getToken result: " + rsp.toString());
                authInfo.setAppId(rsp.appId);
                authInfo.setChannelId(rtcId);
                authInfo.setUserId(userId);
                authInfo.setToken(rsp.token);
                authInfo.setNonce(rsp.nonce);
                authInfo.setTimestamp(rsp.timestamp);
                authInfo.setGslb(new String[]{rsp.gslb});

                // 设置RTC配置参数
                setRtcConfig();
                // 2. 开始入会, 在 RTCEventListener#onJoinChannelResult 回调结果
                if (eventListener != null) {
                    eventListener.userId2HasVideo.clear();
                }
                ofRefreshRTCMgr().enterSeat();
                ofRefreshRTCMgr().joinChannel(authInfo, nick);
                channelJoined.set(true);

                MonitorHubChannel.reportGetRTCToken(rtcId, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                final String message = "get token error, " + dpsError.reason;
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcJoinRtcError(message);
                    }
                });
                MonitorHubChannel.reportGetRTCToken(rtcId, dpsError.getCode(), dpsError.getReason());
            }
        });
    }

    @Override
    public void setCustomBypassLiveLayout(Collection<RTCBypassPeerVideoConfig> configCollection, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            Utils.callError(uiCallback, Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        if (Utils.isEmpty(configCollection)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        SetCustomLayoutReq req = new SetCustomLayoutReq();
        req.confId = rtcId;
        req.paneList = convert2PaneModel(configCollection);
        rtcRpcInterface.setCustomLayout(req, new SetCustomLayoutCb() {
            @Override
            public void onSuccess(SetCustomLayoutRsp setCustomLayoutRsp) {
                Utils.callSuccess(uiCallback, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callError(uiCallback, dpsError);
            }
        });
    }

    private ArrayList<Pane> convert2PaneModel(@NonNull Collection<RTCBypassPeerVideoConfig> configCollection) {
        ArrayList<Pane> result = new ArrayList<>(configCollection.size());
        for (RTCBypassPeerVideoConfig e : configCollection) {
            if (e != null) {
                result.add(new Pane(e.x, e.y, e.width, e.height, e.zOrder, e.userId));
            }
        }
        return result;
    }

    @Override
    public void startRoadPublish(Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        liveInnerService.getPushStreamUrl(new Callback<String>() {
            @Override
            public void onSuccess(String pushStreamUrl) {
                pushLiveStream(rtcId, pushStreamUrl, new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        // 旁路推流成功，上报直播状态
                        liveInnerService.reportLiveStatus();
                        uiCallback.onSuccess(null);
                    }

                    @Override
                    public void onError(String errorMsg) {
                        uiCallback.onError("push stream, " + errorMsg);
                    }
                });
            }

            @Override
            public void onError(String errorMsg) {
                uiCallback.onError("get push stream url error: " + errorMsg);
            }
        });
    }

    @Override
    public void stopRoadPublish(Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        StopLiveStreamReq req = new StopLiveStreamReq();
        req.confId = rtcId;
        rtcRpcInterface.stopLiveStream(req, new StopLiveStreamCb() {
            @Override
            public void onSuccess(StopLiveStreamRsp stopLiveStreamRsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                uiCallback.onError(String.format("stopRoadPublish error. code: %d, reason: %s", dpsError.code, dpsError.reason));
            }
        });
    }

    // 上报旁路推流url, LWP->[Rtc Server]
    private void pushLiveStream(final String conferenceId, final String url, Callback<Void> callback) {
        PushLiveStreamReq req = new PushLiveStreamReq();
        req.confId = conferenceId;
        req.rtmpUrl = url;
        req.resolutionType = rtcStreamConfig != null ? rtcStreamConfig.getBypassLiveResolutionType() : req.resolutionType;
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        rtcRpcInterface.pushLiveStream(req, new PushLiveStreamCb() {
            @Override
            public void onSuccess(PushLiveStreamRsp pushLiveStreamRsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.reportPushLiveStream(conferenceId, url, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                MonitorHubChannel.publishLive(MonitorhubField.MFFIELD_COMMON_RTC, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.reportPushLiveStream(conferenceId, url, dpsError.getCode(), dpsError.getReason());
                MonitorHubChannel.publishLive(MonitorhubField.MFFIELD_COMMON_RTC, dpsError.getCode(), dpsError.getReason());
            }
        });
    }

    @Override
    public void stopPlayRoad() {
        // 停止播放旁路拉流
        livePlayerManager.stopPlay();
    }

    @Override
    public void applyJoinRtc(boolean apply, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        ApplyLinkMicReq req = new ApplyLinkMicReq();
        req.confId = rtcId;
        req.apply = apply;
        rtcRpcInterface.applyLinkMic(req, new ApplyLinkMicCb() {
            @Override
            public void onSuccess(ApplyLinkMicRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void inviteJoinRtc(final List<ConfUserModel> userModels, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (CollectionUtil.isEmpty(userModels)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        AddMembersReq req = new AddMembersReq();
        req.confId = rtcId;
        req.addedCalleeList = new ArrayList<>(userModels);
        rtcRpcInterface.addMembers(req, new AddMembersCb() {
            @Override
            public void onSuccess(AddMembersRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.reportInviteJoinRTC(rtcId, userModels, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.reportInviteJoinRTC(rtcId, userModels, dpsError.getCode(), dpsError.getReason());
            }
        });
    }

    @Override
    public void handleApplyJoinRtc(String userId, boolean agree, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (TextUtils.isEmpty(userId)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        ApproveLinkMicReq req = new ApproveLinkMicReq();
        req.confId = rtcId;
        req.approve = agree;
        req.calleeUid = userId;
        rtcRpcInterface.approveLinkMic(req, new ApproveLinkMicCb() {
            @Override
            public void onSuccess(ApproveLinkMicRsp rsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void reportJoinStatus(RtcUserStatus status, Callback<Void> callback) {
        reportJoinStatusInternal(status, "", callback);
    }

    private void reportJoinStatusInternal(RtcUserStatus status, String errorCode, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        ReportJoinStatusReq req = new ReportJoinStatusReq();
        req.confId = rtcId;
        req.sourceId = userId;
        req.nickname = nick;
        req.joinStatus = status.getStatus();
        req.errorCode = errorCode;
        rtcRpcInterface.reportJoinStatus(req, new ReportJoinStatusCb() {
            @Override
            public void onSuccess(ReportJoinStatusRsp reportJoinStatusRsp) {
                updateConfInfoIfPossible(reportJoinStatusRsp != null ? reportJoinStatusRsp.confInfo : null);
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void rejectInvite(Callback<Void> callback) {
        reportJoinStatusInternal(RtcUserStatus.JOIN_FAILED, ERROR_CODE_REJECT_INVITE, callback);
    }

    private void updateConfInfoIfPossible(@Nullable ConfInfoModel confInfoModel) {
        if (confInfoModel == null) {
            return;
        }
        confInfo = Utils.acceptFirstSuitable(new Utils.Checker<ConfInfoModel>() {
            @Override
            public boolean isSuitable(@Nullable ConfInfoModel confInfoModel) {
                return confInfoModel != null && !TextUtils.isEmpty(confInfoModel.confId);
            }
        }, confInfo, confInfoModel);
    }

    @Override
    public void kickUserFromRtc(final List<String> userIds, Callback<Void> callback) {
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        if (CollectionUtil.isEmpty(userIds)) {
            Utils.invokeInvalidParamError(uiCallback);
            return;
        }

        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        KickMembersReq req = new KickMembersReq();
        req.confId = rtcId;
        req.kickedUserList = new ArrayList<>(userIds);
        rtcRpcInterface.kickMembers(req, new KickMembersCb() {
            @Override
            public void onSuccess(KickMembersRsp rsp) {
                uiCallback.onSuccess(null);
                MonitorHubChannel.reportKickMembers(rtcId, userIds, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
                MonitorHubChannel.reportKickMembers(rtcId, userIds, dpsError.getCode(), dpsError.getReason());
            }
        });
    }

    @Override
    public void getRtcDetail(final Callback<ConfInfoModel> callback) {
        queryRtcDetail(getInstanceId(), new Callback<ConfInfoModel>() {
            @Override
            public void onSuccess(ConfInfoModel data) {
                updateConfInfoIfPossible(data);
                callback.onSuccess(data);
            }

            @Override
            public void onError(String errorMsg) {
                callback.onError(errorMsg);
            }
        });
    }

    @Override
    public void queryRtcDetail(final String conferenceId, Callback<ConfInfoModel> callback) {
        final UICallback<ConfInfoModel> uiCallback = new UICallback<>(callback);
        if (TextUtils.isEmpty(conferenceId)) {
            Logger.i(TAG, "queryRtcDetail: end conferenceId empty");
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }

        GetConfDetailReq req = new GetConfDetailReq();
        req.confId = conferenceId;
        rtcRpcInterface.getConfDetail(req, new GetConfDetailCb() {
            @Override
            public void onSuccess(GetConfDetailRsp rsp) {
                uiCallback.onSuccess(rsp != null ? rsp.confInfo : null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public ConfInfoModel getRtcDetail() {
        return this.confInfo;
    }

    @Override
    public void leaveRtc(boolean destroyRtc) {
        Logger.i(TAG, "leave channel, destroyRtc=" + destroyRtc);
        // 停止预览
        ofRefreshRTCMgr().stopPreview();
        // 老师主动停止旁路推流
        if (isOwner()) {
            stopRoadPublish(null);
        }
        leaveSeatChannelAndReport();
        turnOffLocalDevice();

        if (destroyRtc && isOwner()) {
            destroyRTC(getInstanceId());
        }
    }

    @Override
    public void leaveSeatChannelAndReport() {
        // 下麦
        ofRefreshRTCMgr().leaveSeat();
        // 离会
        ofRefreshRTCMgr().leaveChannel();
        channelJoined.set(false);
        resolutionStrategy.reset();
        reportLeaveStatus(getInstanceId(), new Callbacks.Log<Void>(TAG, "leave channel"));
    }

    @Override
    public void startPreview() {
        ofRefreshRTCMgr().startPreview();
    }

    @Override
    public void stopPreview() {
        ofRefreshRTCMgr().stopPreview();
    }

    @Override
    public void setRemoteViewConfig(AliRtcEngine.AliRtcVideoCanvas videoCanvas, String userId,
                                    AliRtcEngine.AliRtcVideoTrack videoTrack) {
        ofRefreshRTCMgr().setRemoteViewConfig(videoCanvas, userId, videoTrack);
    }

    @Override
    public void setLocalViewConfig(AliRtcEngine.AliRtcVideoCanvas videoCanvas,
                                   AliRtcEngine.AliRtcVideoTrack videoTrack) {
        ofRefreshRTCMgr().setLocalViewConfig(videoCanvas, videoTrack);
    }

    @Override
    public void configRemoteCameraTrack(String userId, boolean isMainStream, boolean enable) {
        ofRefreshRTCMgr().configRemoteCameraTrack(userId, isMainStream, enable);
    }

    @Override
    public void muteLocalMic(final boolean muteLocalMic) {
        if (muteLocalMicViaRtcMgr(muteLocalMic)) {
            // 媒体静音成功，上报远程状态
            reportMuteMic(muteLocalMic);
        } else {
            // 媒体能力静音失败，回调失败，用户自定义逻辑处理
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onRtcUserAudioMutedError(muteLocalMic, userId);
                }
            });
        }
    }

    private boolean turnOffLocalDevice() {
        return muteLocalMicViaRtcMgr(true) && muteLocalCameraViaRtcMgr(true);
    }

    private boolean turnOnLocalDevice() {
        return muteLocalMicViaRtcMgr(false) && muteLocalCameraViaRtcMgr(false);
    }

    private boolean muteLocalMicViaRtcMgr(boolean mute) {
        boolean success = ofRefreshRTCMgr().muteLocalMic(mute) == 0;
        if (success) {
            closeMic = mute;
        }
        return success;
    }

    private boolean muteLocalCameraViaRtcMgr(boolean mute) {
        boolean success = ofRefreshRTCMgr().muteLocalCamera(mute) == 0;
        if (success) {
            closeCamera = mute;
        }
        return success;
    }

    // 上报本地静音状态
    private void reportMuteMic(boolean mute) {
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            return;
        }
        ReportRtcMuteReq req = new ReportRtcMuteReq(rtcId, !mute);
        rtcRpcInterface.reportRtcMute(req, new ReportRtcMuteCb() {
            @Override
            public void onSuccess(ReportRtcMuteRsp reportRtcMuteRsp) {
                Logger.i(TAG, "onSuccess: " + reportRtcMuteRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, "onFailure: " + dpsError.toString());
            }
        });
    }

    @Override
    public void muteRemoteAudioPlaying(final String uid, final boolean mute) {
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            return;
        }
        RtcMuteUserReq req = new RtcMuteUserReq(rtcId, uid, !mute);
        rtcRpcInterface.rtcMuteUser(req, new RtcMuteUserCb() {
            @Override
            public void onSuccess(RtcMuteUserRsp rtcMuteUserRsp) {
                Logger.i(TAG, "onSuccess: " + rtcMuteUserRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, "onFailure: " + dpsError.toString());
            }
        });
    }

    @Override
    public void muteAllRemoteAudioPlaying(boolean mute) {
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            return;
        }
        RtcMuteAllReq req = new RtcMuteAllReq(rtcId, !mute);
        rtcRpcInterface.rtcMuteAll(req, new RtcMuteAllCb() {
            @Override
            public void onSuccess(RtcMuteAllRsp rtcMuteAllRsp) {
                Logger.i(TAG, "onSuccess: " + rtcMuteAllRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, "onFailure: " + dpsError.toString());
            }
        });
    }

    @Override
    public void muteLocalCamera(final boolean muteLocalCamera) {
        if (muteLocalCameraViaRtcMgr(muteLocalCamera)) {
            reportMuteCamera(muteLocalCamera);
        } else {
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onRtcUserVideoMutedError(muteLocalCamera, userId);
                }
            });
        }
    }

    private void reportMuteCamera(boolean muteLocalCamera){
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            return;
        }
        OperateCameraReq req = new OperateCameraReq(rtcId, !muteLocalCamera);
        rtcRpcInterface.operateCamera(req, new OperateCameraCb() {
            @Override
            public void onSuccess(OperateCameraRsp operateCameraRsp) {
                Logger.i(TAG, "onSuccess: " + operateCameraRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Logger.e(TAG, "onFailure: " + dpsError.toString());
            }
        });
    }

    @Override
    public boolean switchCamera() {
        return ofRefreshRTCMgr().switchCamera() == 0;
    }

    @Override
    public void setPreviewMirror(boolean enable) {
        ofRefreshRTCMgr().setPreviewMirror(enable);
    }

    @Override
    public void setVideoStreamMirror(boolean enable) {
        updateVideoEncodeConfig(rtcStreamConfig, enable);
    }

    private void reportLeaveStatus(@Nullable String conferenceId, Callback<Void> callback) {
        if (TextUtils.isEmpty(conferenceId)) {
            Logger.i(TAG, "reportLeaveStatus: end--invalid param: " + conferenceId);
            return;
        }
        ReportLeaveStatusReq req = new ReportLeaveStatusReq();
        req.confId = conferenceId;
        final UICallback<Void> uiCallback = new UICallback<>(callback);
        rtcRpcInterface.reportLeaveStatus(req, new ReportLeaveStatusCb() {
            @Override
            public void onSuccess(ReportLeaveStatusRsp reportLeaveStatusRsp) {
                uiCallback.onSuccess(null);
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    private void destroyRTC(final String rtcId) {
        Utils.destroy(aliRTCManager);
        aliRTCManager = null;
        if (!TextUtils.isEmpty(rtcId)) {
            DestroyRtcReq req = new DestroyRtcReq();
            req.roomId = roomId;
            req.conferenceId = rtcId;
            roomExtInterface.destroyRtc(req, new DestroyRtcCb() {
                @Override
                public void onSuccess(DestroyRtcRsp rsp) {
                    removeInstanceId(rtcId);
                    MonitorHubChannel.reportDestroyRTC(rtcId, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Logger.e(TAG, "leave rtc error: " + dpsError.reason);
                    MonitorHubChannel.reportDestroyRTC(rtcId, dpsError.getCode(), dpsError.getReason());
                }
            });
        }
        destroy();
    }

    // 设置旁路推流的布局模板
    @Override
    public void setLayout(List<String> userIds, RtcLayoutModel layoutModel, Callback<Void> callback) {
        String rtcId = getInstanceId();
        if (rtcId != null) {
            final UICallback uiCallback = new UICallback<>(callback);
            SetLayoutReq req = new SetLayoutReq();
            req.confId = rtcId;
            req.model = layoutModel.getModel();
            req.userIds = (ArrayList<String>) userIds;
            rtcRpcInterface.setLayout(req, new SetLayoutCb() {
                @Override
                public void onSuccess(SetLayoutRsp setLayoutRsp) {
                    uiCallback.onSuccess(null);
                }

                @Override
                public void onFailure(DPSError dpsError) {
                    Utils.callErrorWithDps(uiCallback, dpsError);
                }
            });
        }
    }

    @Override
    public void startRecord(Callback callback) {
        final UICallback uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        StartRecordReq req = new StartRecordReq();
        req.confId = rtcId;
        req.resolutionType = rtcStreamConfig != null ? rtcStreamConfig.getBypassLiveResolutionType() : req.resolutionType;
        rtcRpcInterface.startRecord(req, new StartRecordCb() {
            @Override
            public void onSuccess(StartRecordRsp startRecordRsp) {
                uiCallback.onSuccess(startRecordRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public void stopRecord(Callback callback) {
        final UICallback uiCallback = new UICallback<>(callback);
        final String rtcId = getInstanceId();
        if (rtcId == null) {
            uiCallback.onError(Errors.INNER_STATE_ERROR.getMessage());
            return;
        }
        StopRecordReq req = new StopRecordReq();
        req.confId = rtcId;
        rtcRpcInterface.stopRecord(req, new StopRecordCb() {
            @Override
            public void onSuccess(StopRecordRsp stopRecordRsp) {
                uiCallback.onSuccess(stopRecordRsp.toString());
            }

            @Override
            public void onFailure(DPSError dpsError) {
                Utils.callErrorWithDps(uiCallback, dpsError);
            }
        });
    }

    @Override
    public int publishLocalAudio(boolean enable) {
        return aliRTCManager.publishLocalAudio(enable);
    }

    @Override
    public int publishLocalVideo(boolean enable) {
        return aliRTCManager.publishLocalVideo(enable);
    }

    @Override
    public int enableLocalVideo(boolean enable) {
        return aliRTCManager.enableLocalVideo(enable);
    }

    @Override
    public int subscribeRemoteAudioStream(String uid, boolean sub) {
        return aliRTCManager.subscribeRemoteAudioStream(uid, sub);
    }

    @Override
    public int subscribeAllRemoteAudioStreams(boolean sub) {
        return aliRTCManager.subscribeAllRemoteAudioStreams(sub);
    }

    @Override
    public int subscribeRemoteVideoStream(String uid, AliRtcEngine.AliRtcVideoTrack track, boolean sub) {
        return aliRTCManager.subscribeRemoteVideoStream(uid, track, sub);
    }

    @Override
    public int subscribeAllRemoteVideoStreams(boolean sub) {
        return aliRTCManager.subscribeAllRemoteVideoStreams(sub);
    }

    @Override
    public int setAudioOnlyMode(boolean audioOnly) {
        return aliRTCManager.setAudioOnlyMode(audioOnly);
    }

    @Override
    public boolean isAudioOnly() {
        return aliRTCManager.isAudioOnly();
    }

    @Override
    public int setBasicFaceBeauty(boolean enable, float whiteningLevel, float smoothnessLevel) {
        AliRtcEngine.AliRtcBeautyConfig config = new AliRtcEngine.AliRtcBeautyConfig();
        config.whiteningLevel = whiteningLevel;
        config.smoothnessLevel = smoothnessLevel;
        return aliRTCManager.setBeautyOption(enable, config);
    }

    @Override
    public void setBeautyOn(boolean beautyOn) {
        aliRTCManager.setBeautyOn(beautyOn);
    }

    @Override
    public int startScreenShare() {
        return aliRTCManager.startScreenShare();
    }

    @Override
    public int stopScreenShare() {
        return aliRTCManager.stopScreenShare();
    }

    @Override
    public int addVideoWatermark(AliRtcEngine.AliRtcVideoTrack track, String imageUrl, AliRtcEngine.AliRtcWatermarkConfig config) {
        return aliRTCManager.addVideoWatermark(track, imageUrl, config);
    }

    @Override
    public void setRemoteVideoStreamShowMode(VideoStreamShowMode remoteVideoStreamShowMode) {
        this.remoteVideoStreamShowMode = remoteVideoStreamShowMode;
    }

    @Override
    public void configErrorToast(boolean trueForOn, long shortestIntervalInSeconds) {
        ofRefreshRTCMgr().configErrorToast(trueForOn, shortestIntervalInSeconds);
    }

    @Override
    public void onLeaveRoom(boolean existRoom) {
        super.onLeaveRoom(existRoom);
        // TODO: 2021/6/23 区分两种调用来源: 离开房间、离开会议
//        destroyRTC();
    }

    @Override
    public String getPluginId() {
        return PLUGIN_ID;
    }

    @Override
    public void onSyncEvent(final RoomNotificationModel model) {
        Logger.i(TAG, "onSyncEvent:" + model);
        switch (model.type) {
            case RTC_JOIN_SUCCESS:
                final ConfUserEvent joinSuccessEvent = JSON.parseObject(model.data, ConfUserEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcRemoteJoinSuccess(joinSuccessEvent);
                    }
                });
                break;
            case RTC_JOIN_FAIL:
                final ConfUserEvent joinFailEvent = JSON.parseObject(model.data, ConfUserEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        // 服务端 入会失败 和 拒绝邀请 都是该回调, 需要端上自行区分
                        List<ConfUserModel> userList = joinFailEvent.userList;
                        if (CollectionUtil.isEmpty(userList)) {
                            return;
                        }

                        List<ConfUserModel> rejectInviteUsers = new ArrayList<>();
                        for (ConfUserModel userModel : userList) {
                            if (ERROR_CODE_REJECT_INVITE.equals(userModel.errorCode)) {
                                rejectInviteUsers.add(userModel);
                            }
                        }

                        if (CollectionUtil.isNotEmpty(rejectInviteUsers)) {
                            eventHandler.onRtcInviteRejected(rejectInviteUsers);
                        }

                        eventHandler.onRtcRemoteJoinFail(joinFailEvent);
                    }
                });
                break;
            case RTC_INVITE:
                final ConfInviteEvent confInviteEvent = JSON.parseObject(model.data, ConfInviteEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcUserInvited(confInviteEvent);
                    }
                });
                break;
            case RTC_APPLY:
                // 学生申请连麦
                final ConfApplyJoinChannelEvent joinEV = JSON.parseObject(model.data, ConfApplyJoinChannelEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcApplyJoinChannel(joinEV);
                    }
                });
                break;
            case RTC_HANDLE_APPLY:
                // 学生申请被老师拒绝
                final ConfHandleApplyEvent rejectedEvent = JSON.parseObject(model.data, ConfHandleApplyEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcHandleApplyChannel(rejectedEvent);
                    }
                });
                break;
            case RTC_KICK_USER:
                // 老师挂断
                final ConfUserEvent confUserEvent = JSON.parseObject(model.data, ConfUserEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcKickUser(confUserEvent);
                    }
                });
                break;
            case RTC_LEAVE_CHANNEL:
                // 离会通知
                final ConfUserEvent leaveUserEvent = JSON.parseObject(model.data, ConfUserEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcLeaveUser(leaveUserEvent);
                    }
                });
                break;
            case RTC_START:
                // 会议开始
                final ConfEvent confStartEvent = JSON.parseObject(model.data, ConfEvent.class);
                if (confStartEvent != null && confStartEvent.confInfoModel != null) {
                    updateConfInfoIfPossible(confStartEvent.confInfoModel);
                    addInstanceId(confStartEvent.confInfoModel.confId);
                }
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcStart(confStartEvent);
                    }
                });
                break;
            case RTC_END:
                // 会议结束
                final ConfEvent confEndEvent = JSON.parseObject(model.data, ConfEvent.class);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcEnd(confEndEvent);

                        // the instanceId is Used for release resource, remove it after End Event dispatched
                        if (confEndEvent != null && confEndEvent.confInfoModel != null) {
                            removeInstanceId(confEndEvent.confInfoModel.confId);
                        }
                    }
                });
                break;
            case RTC_MUTE_MIC:
                // 静音
                final ConfMuteMicEvent confMuteMicEvent = JSON.parseObject(model.data, ConfMuteMicEvent.class);
                if (confMuteMicEvent != null && confMuteMicEvent.userList != null) {
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            for (String uid : confMuteMicEvent.userList) {
                                if (confMuteMicEvent.open) {
                                    eventHandler.onRtcUserAudioEnable(uid);
                                } else {
                                    eventHandler.onRtcUserAudioMuted(uid);
                                }
                                if (isSelf(uid)) {
                                    eventHandler.onSelfAudioMuted(!confMuteMicEvent.open);
                                }
                            }
                        }
                    });
                }
                break;
            case RTC_MUTE_CAMERA:
                // 开启/关闭摄像头
                final ConfMuteCameraEvent confMuteCameraEvent = JSON.parseObject(model.data, ConfMuteCameraEvent.class);
                if (confMuteCameraEvent != null) {
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            if (confMuteCameraEvent.open) {
                                eventHandler.onRtcUserVideoEnable(confMuteCameraEvent.userId);
                            } else {
                                eventHandler.onRtcUserVideoMuted(confMuteCameraEvent.userId);
                            }
                            if (isSelf(confMuteCameraEvent.userId)) {
                                eventHandler.onSelfVideoMuted(!confMuteCameraEvent.open);
                            }
                        }
                    });
                }
                break;
            case RTC_SHARE_SCREEN:
                // 分享桌面流
                ConfShareScreenEvent confShareScreenEvent = JSON.parseObject(model.data, ConfShareScreenEvent.class);
            case RTC_POSITIVE_MUTE_MIC: {
                final ConfPositiveMuteMicEvent positiveEvent = JSON.parseObject(model.data, ConfPositiveMuteMicEvent.class);
                final List<String> userList;
                if (positiveEvent != null && Utils.isNotEmpty(userList = positiveEvent.userList)) {
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            for (final String uid : userList) {
                                eventHandler.onRtcPositiveMuteMic(positiveEvent.positiveMute, uid);
                            }
                        }
                    });
                }
                break;
            }
            case RTC_PASSIVE_MUTE_MIC: {
                final ConfPassiveMuteMicEvent passiveEvent = JSON.parseObject(model.data, ConfPassiveMuteMicEvent.class);
                final List<String> userList;
                if (passiveEvent != null && Utils.isNotEmpty(userList = passiveEvent.userList)) {
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            for (final String uid : userList) {
                                eventHandler.onRtcPassiveMuteMic(passiveEvent.passiveMute, uid);
                            }
                        }
                    });
                }
                break;
            }
            case RTC_MUTE_ALL_MIC:
                final ConfMuteAllMicEvent muteAllEvent = JSON.parseObject(model.data, ConfMuteAllMicEvent.class);
                if (muteAllEvent != null) {
                    dispatch(new Consumer<RtcEventHandler>() {
                        @Override
                        public void consume(RtcEventHandler eventHandler) {
                            eventHandler.onRtcMuteAllMic(muteAllEvent.muteAll);
                        }
                    });
                }
                break;
            default:
                Logger.w(TAG, "unknown rtc message: " + JSON.toJSONString(model));
                break;
        }
    }

    private boolean isSelf(String uid) {
        return TextUtils.equals(userId, uid);
    }

    @Override
    public void destroy() {
        Utils.destroy(resolutionStrategy);
    }

    private class RTCEventListener extends IRTCEventListener {

        final Map<String, AliRtcEngine.AliRtcVideoTrack> userId2HasVideo = new HashMap<>();

        @Override
        public void onRemoteTrackAvailableNotify(final String uid,
                                                 AliRtcEngine.AliRtcAudioTrack audioTrack, final AliRtcEngine.AliRtcVideoTrack videoTrack) {
            Logger.i(TAG, String.format(
                    "onRemoteTrackAvailableNotify: uid=%s, audioTrack=%s, videoTrack=%s",
                    uid, audioTrack.name(), videoTrack.name()
            ));
            final AliRtcEngine.AliRtcVideoTrack lastTrack = userId2HasVideo.get(uid);
//            boolean lastHasVideo = Boolean.TRUE.equals(userId2HasVideo.get(uid));
//            boolean currentHasVideo = videoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera
//                    || videoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen
//                    || videoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth;
            userId2HasVideo.put(uid, videoTrack);

            if (lastTrack != videoTrack) {
                Logger.i(TAG, String.format("onRtcStreamIn: uid=%s", uid));
                final RtcStreamEvent rtcStreamEvent = createRtcStreamInfo(uid, videoTrack, false);
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        View validRenderView = null;

                        // 相机流
                        boolean lastHasCamera = hasCameraStream(lastTrack);
                        boolean currentHasCamera = hasCameraStream(videoTrack);
                        if (lastHasCamera ^ currentHasCamera) {
                            VideoStream stream = buildVideoStream(currentHasCamera, StreamType.CAMERA);
                            eventHandler.onRemoteVideoStreamChanged(stream);
                            validRenderView = stream.view;
                        }

                        // 屏幕共享流
                        boolean lastHasScreen = hasScreenStream(lastTrack);
                        boolean currentHasScreen = hasScreenStream(videoTrack);
                        if (lastHasScreen ^ currentHasScreen) {
                            VideoStream stream = buildVideoStream(currentHasScreen, StreamType.SCREEN);
                            eventHandler.onRemoteVideoStreamChanged(stream);
                            validRenderView = stream.view;
                        }

                        if (validRenderView != null && rtcStreamEvent.aliVideoCanvas != null) {
                            rtcStreamEvent.aliVideoCanvas.view = validRenderView;
                        }
                        // 此处回调不准确 (但为了兼容老版本, 不做移除操作)
                        eventHandler.onRtcStreamIn(rtcStreamEvent);
                    }

                    private VideoStream buildVideoStream(boolean available, StreamType streamType) {
                        VideoStream stream = new VideoStream();
                        stream.userId = uid;
                        stream.available = available;
                        stream.view = available ? getRenderView(videoTrack) : null;
                        stream.streamType = streamType;
                        return stream;
                    }

                    private View getRenderView(AliRtcEngine.AliRtcVideoTrack track) {
                        AliRtcEngine.AliRtcVideoCanvas canvas = new AliRtcEngine.AliRtcVideoCanvas();
                        canvas.view = SurfaceViewUtil.generateSophonSurfaceView(context, false);
                        canvas.renderMode = convertMode(remoteVideoStreamShowMode);
                        setRemoteViewConfig(canvas, uid, track);
                        subscribeRemoteVideoStream(uid, track, true);
                        return canvas.view;
                    }

                    private boolean hasCameraStream(AliRtcEngine.AliRtcVideoTrack track) {
                        return track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera
                                || track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth;
                    }

                    private boolean hasScreenStream(AliRtcEngine.AliRtcVideoTrack track) {
                        return track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen
                                || track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth;
                    }
                });
            }
        }

        // 创建订阅流的展示信息
        private RtcStreamEvent createRtcStreamInfo(String userId, AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack,
                                                   boolean isLocalStream) {
            AliRtcRemoteUserInfo userInfo = ofRefreshRTCMgr().getUserInfo(userId);

            String userName = userInfo != null ? (isLocalStream ? "我" : userInfo.getDisplayName()) : "";
            boolean isTeacher = roomContext.isOwner(userId);
            return RtcStreamEventHelper.asRtcStreamEvent(userId, userName, isTeacher, isLocalStream, aliRtcVideoTrack);
        }

        @Override
        public void onRemoteUserOnLineNotify(final String uid, final int elapsed) {
            final AliRtcRemoteUserInfo userInfo = ofRefreshRTCMgr().getUserInfo(uid);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onRemoteUserOnLineNotify(uid, userInfo, elapsed);
                }
            });
            resolutionStrategy.onRemoteUserOnLineNotify(uid);
        }

        @Override
        public void onRemoteUserOffLineNotify(final String uid, final AliRtcEngine.AliRtcUserOfflineReason reason) {
            final AliRtcRemoteUserInfo userInfo = ofRefreshRTCMgr().getUserInfo(uid);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onRtcStreamOut(uid);
                    eventHandler.onRemoteUserOffLineNotify(uid, userInfo, reason);
                }
            });
            resolutionStrategy.onRemoteUserOffLineNotify(uid);
        }

        @Override
        public void onJoinChannelResult(int result, String channel,  String uid, int elapsed) {
            if (!isSelf(uid)) {
                Logger.e(TAG, "onJoinChannelResult: end--invalid param: " + uid);
                return;
            }
            if (result == 0) {
                // 入会成功

                // 1. 上报入会成功事件
                reportJoinStatus(RtcUserStatus.ACTIVE, null);

                reportMuteMic(closeMic);
                reportMuteCamera(closeCamera);

                // 3. 透出入会成功事件
                final SophonSurfaceView localView = ofRefreshRTCMgr().getLocalView();
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcJoinRtcSuccess(localView);
                    }
                });
                MonitorHubChannel.reportJoinChannel(channel, MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE, null);
                MonitorHeartbeatManager.getInstance().setContentId(channel);
                MonitorHeartbeatManager.getInstance().setStatus(isOwner() ? MonitorhubStatusType.RTC_HOST : MonitorhubStatusType.RTC_PARTICIPANT);
                if (rtcStreamConfig != null) {
                    MonitorHeartbeatManager.getInstance().setCameraWidth(isOwner(), String.valueOf(rtcStreamConfig.getWidth()));
                    MonitorHeartbeatManager.getInstance().setCameraHeight(isOwner(), String.valueOf(rtcStreamConfig.getHeight()));
                }
            } else {
                String errorCode = Integer.toHexString(result);
                // 入会失败
                final String message = String.format(
                        "join channel error, result: %s, channel: %s, errorCode: %s",
                        result, channel, errorCode
                );
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcJoinRtcError(message);
                    }
                });
                MonitorHubChannel.reportJoinChannel(channel, result, message);
            }
        }

        @Override
        public void onLeaveChannelResult(int result, AliRtcEngine.AliRtcStats stats) {
            if (result == 0) {
                userId2HasVideo.clear();
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcLeaveRtcSuccess();
                    }
                });
            }
            MonitorHubChannel.reportLeaveChannel(getInstanceId(), result, null);
        }

        @Override
        public void onVideoPublishStateChanged(AliRtcEngine.AliRtcPublishState oldState, AliRtcEngine.AliRtcPublishState newState, int elapseSinceLastState, String channel) {
            if (newState == AliRtcEngine.AliRtcPublishState.AliRtcStatsPublished) {
                // mute this, RtcEventHandler.onRtcJoinRtcSuccess already been invoked by other
                // 本地推流成功的回调
                //final SophonSurfaceView localView = aliRTCManager.getLocalView();
                //dispatch(new Consumer<RtcEventHandler>() {
                //    @Override
                //    public void consume(RtcEventHandler eventHandler) {
                //        eventHandler.onRtcJoinRtcSuccess(localView);
                //    }
                //});
            }
        }

        @Override
        public void onNetworkQualityChanged(final String uid, AliRtcEngine.AliRtcNetworkQuality upQuality, AliRtcEngine.AliRtcNetworkQuality downQuality) {
            super.onNetworkQualityChanged(uid, upQuality, downQuality);
            if (downQuality.getValue() >= AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkBad.getValue() &&
                    downQuality.getValue() <= AliRtcEngine.AliRtcNetworkQuality.AliRtcNetworkVeryBad.getValue()) {
                dispatch(new Consumer<RtcEventHandler>() {
                    @Override
                    public void consume(RtcEventHandler eventHandler) {
                        eventHandler.onRtcNetworkQualityChanged(uid);
                    }
                });
            }
        }

        @Override
        public void onConnectionLost() {
            super.onConnectionLost();
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onConnectionLost();
                }
            });
        }

        @Override
        public void onTryToReconnect() {
            super.onTryToReconnect();
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onTryToReconnect();
                }
            });
        }

        @Override
        public void onConnectionRecovery() {
            super.onConnectionRecovery();
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onConnectionRecovery();
                }
            });
            MonitorHeartbeatManager.getInstance().setStatus(isOwner() ? MonitorhubStatusType.RTC_HOST : MonitorhubStatusType.RTC_PARTICIPANT);
        }

        @Override
        public void onConnectionStatusChange(final AliRtcEngine.AliRtcConnectionStatus status, final AliRtcEngine.AliRtcConnectionStatusChangeReason reason) {
            super.onConnectionStatusChange(status, reason);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onConnectionStatusChange(status, reason);
                }
            });
        }

        @Override
        public void onUserAudioMuted(final String uid, final boolean isMute) {
            Logger.i(TAG, "onUserAudioMuted: uid=" + uid + ", isMute=" + isMute);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onOthersAudioMuted(uid, isMute);
                }
            });
        }

        @Override
        public void onUserVideoMuted(final String uid, final boolean isMute) {
            Logger.i(TAG, "onUserVideoMuted: uid=" + uid + ", isMute=" + isMute);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onOthersVideoMuted(uid, isMute);
                }
            });
        }

        @Override
        public void onAudioVolume(final List<AliRtcEngine.AliRtcAudioVolume> speakers, final int totalVolume) {
            super.onAudioVolume(speakers, totalVolume);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onAudioVolume(speakers, totalVolume);
                }
            });

            for (AliRtcEngine.AliRtcAudioVolume speaker : speakers) {
                if (speaker.mUserId == "0") {
                    MonitorHeartbeatManager.getInstance().setAudioVolume(String.valueOf(speaker.mVolume));
                    break;
                }
            }
        }

        @Override
        public void onActiveSpeaker(final String uid) {
            super.onActiveSpeaker(uid);
            dispatch(new Consumer<RtcEventHandler>() {
                @Override
                public void consume(RtcEventHandler eventHandler) {
                    eventHandler.onActiveSpeaker(uid);
                }
            });
        }

        @Override
        public void onRtcLocalVideoStats(AliRtcEngine.AliRtcLocalVideoStats aliRtcStats) {
            super.onRtcLocalVideoStats(aliRtcStats);
            if (aliRtcStats == null) {
                return;
            }
            String sourceType = null;
            if (aliRtcStats.track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera) {
                MonitorHeartbeatManager.getInstance().setCameraEncodeFPS(isOwner(), String.valueOf(aliRtcStats.encodeFps));
                MonitorHeartbeatManager.getInstance().setCameraSentBitrate(isOwner(), String.valueOf(aliRtcStats.sentBitrate));
                MonitorHeartbeatManager.getInstance().setCameraSentFPS(isOwner(), String.valueOf(aliRtcStats.sentFps));
                sourceType = "camera";
            } else if (aliRtcStats.track == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen) {
                MonitorHeartbeatManager.getInstance().setScreenEncodeFPS(isOwner(), String.valueOf(aliRtcStats.encodeFps));
                MonitorHeartbeatManager.getInstance().setScreenSentBitrate(isOwner(), String.valueOf(aliRtcStats.sentBitrate));
                MonitorHeartbeatManager.getInstance().setScreenSentFPS(isOwner(), String.valueOf(aliRtcStats.sentFps));
                sourceType = "screen";
            }
            MonitorHubChannel.localVideoStats(getInstanceId(), aliRtcStats.encodeFps, aliRtcStats.sentBitrate,
                    aliRtcStats.sentFps, sourceType);
        }

        @Override
        public void onRtcLocalAudioStats(AliRtcEngine.AliRtcLocalAudioStats aliRtcStats) {
            super.onRtcLocalAudioStats(aliRtcStats);
            if (aliRtcStats != null) {
                MonitorHeartbeatManager.getInstance().setAudioSentBitrate(isOwner(), String.valueOf(aliRtcStats.sentBitrate));
                String sourceType = null;
                switch (aliRtcStats.track) {
                    case AliRtcAudioTrackNo:
                        sourceType = "no";
                        break;
                    case AliRtcAudioTrackMic:
                        sourceType = "mic";
                        break;
                }
                MonitorHubChannel.localAudioStats(getInstanceId(), aliRtcStats.numChannel, aliRtcStats.sentBitrate,
                        aliRtcStats.sentSamplerate, sourceType);
            }
        }

        @Override
        public void onAliRtcStats(AliRtcEngine.AliRtcStats aliRtcStats) {
            if (aliRtcStats != null) {
                MonitorHubChannel.rtcStats(getInstanceId(), aliRtcStats.availableSendKbitrate, aliRtcStats.sentKbitrate,
                        aliRtcStats.rcvdKbitrate, aliRtcStats.sentBytes, aliRtcStats.rcvdBytes, aliRtcStats.cpuUsage,
                        aliRtcStats.systemCpuUsage, aliRtcStats.videoRcvdKbitrate, aliRtcStats.videoSentKbitrate,
                        aliRtcStats.callDuration, aliRtcStats.sentLossRate, aliRtcStats.sentLossPkts,
                        aliRtcStats.sentExpectedPkts, aliRtcStats.rcvdLossRate, aliRtcStats.rcvdLossPkts,
                        aliRtcStats.rcvdExpectedPkts, aliRtcStats.lastmileDelay);
            }
        }

        @Override
        public void onRtcRemoteVideoStats(AliRtcEngine.AliRtcRemoteVideoStats aliRtcStats) {
            if (aliRtcStats != null) {
                String sourceType = null;
                switch (aliRtcStats.track) {
                    case AliRtcVideoTrackNo:
                        sourceType = "no";
                        break;
                    case AliRtcVideoTrackBoth:
                        sourceType = "both";
                        break;
                    case AliRtcVideoTrackCamera:
                        sourceType = "camera";
                        break;
                    case AliRtcVideoTrackScreen:
                        sourceType = "screen";
                        break;
                }
                MonitorHubChannel.remoteVideoStats(getInstanceId(), aliRtcStats.decodeFps, aliRtcStats.frozenTimes,
                        aliRtcStats.height, aliRtcStats.renderFps, sourceType, aliRtcStats.userId, aliRtcStats.width);
            }
        }

        @Override
        public void onRtcRemoteAudioStats(AliRtcEngine.AliRtcRemoteAudioStats aliRtcStats) {
            if (aliRtcStats != null) {
                String sourceType = null;
                switch (aliRtcStats.audioTrack) {
                    case AliRtcAudioTrackNo:
                        sourceType = "no";
                        break;
                    case AliRtcAudioTrackMic:
                        sourceType = "mic";
                        break;
                }
                MonitorHubChannel.remoteAudioStats(getInstanceId(), aliRtcStats.audioLossRate, aliRtcStats.jitter_buffer_delay,
                        aliRtcStats.network_transport_delay, aliRtcStats.quality, aliRtcStats.rcvdBitrate, sourceType,
                        aliRtcStats.userId, aliRtcStats.totalFrozenTimes);
            }
        }
    }

    private static AliRtcEngine.AliRtcRenderMode convertMode(VideoStreamShowMode mode) {
        switch (mode) {
            case Stretch:
                return AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeStretch;
            case Fill:
                return AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
            case Crop:
            default:
                return AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeClip;
        }
    }
}
