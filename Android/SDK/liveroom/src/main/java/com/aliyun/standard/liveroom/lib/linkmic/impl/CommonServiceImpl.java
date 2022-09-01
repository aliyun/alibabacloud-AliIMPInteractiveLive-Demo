package com.aliyun.standard.liveroom.lib.linkmic.impl;

import android.text.TextUtils;
import android.view.View;

import com.alibaba.dingpaas.rtc.ConfInfoModel;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alivc.rtc.AliRtcEngine;
import com.alivc.rtc.AliRtcRemoteUserInfo;
import com.aliyun.roompaas.base.EventHandlerManager;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.rtc.RtcApplyUserParam;
import com.aliyun.roompaas.rtc.SampleRtcEventHandler;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcStreamConfig;
import com.aliyun.roompaas.rtc.exposable.StreamType;
import com.aliyun.roompaas.rtc.exposable.VideoStream;
import com.aliyun.roompaas.rtc.exposable.VideoStreamShowMode;
import com.aliyun.roompaas.rtc.exposable.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfHandleApplyEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.standard.liveroom.lib.LivePrototype;
import com.aliyun.standard.liveroom.lib.linkmic.CommonService;
import com.aliyun.standard.liveroom.lib.linkmic.LeaveRoomListener;
import com.aliyun.standard.liveroom.lib.linkmic.LinkMicEventHandler;
import com.aliyun.standard.liveroom.lib.linkmic.enums.ContentMode;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;
import com.aliyun.standard.liveroom.lib.util.EntityConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author puke
 * @version 2022/1/5
 */
public class CommonServiceImpl extends EventHandlerManager<LinkMicEventHandler>
        implements CommonService, LeaveRoomListener {

    private static final String TAG = CommonServiceImpl.class.getSimpleName();

    private static final int DEFAULT_PAGE_NUM = 1;
    private static final int DEFAULT_PAGE_SIZE = 200;
    private static final boolean DEFAULT_CAMERA_ENABLE = true;
    private static final boolean DEFAULT_MIC_ENABLE = true;

    protected final RoomChannel roomChannel;
    protected final RtcService rtcService;

    // 自己的状态
    protected State state = State.OFFLINE;

    // userId=>用户实体 映射关系
    private final Map<String, LinkMicUserModel> joinedUsers = new HashMap<>();
    // userId=>视频流 映射关系
    private final Map<String, View> userId2ViewTemp = new HashMap<>();
    // userId=>语音状态 映射关系
    private final Map<String, Boolean> userId2AudioEnable = new HashMap<>();

    // 摄像头是否打开 (使用包装类型, 用以区分外部是否手动设置)
    private Boolean isCameraOpen;
    // 麦克风是否打开 (同上)
    private Boolean isMicOpen;
    // 麦克风是否允许打开
    @Deprecated
    private boolean isMicAllowed = true;
    // 是否全员静音
    private boolean isMicAllMuted = false;

    private AliRtcEngine.AliRtcVideoCanvas videoCanvas;
    private ContentMode localShowMode = ContentMode.Crop;

    public CommonServiceImpl(RoomChannel roomChannel) {
        this.roomChannel = roomChannel;
        this.rtcService = roomChannel.getPluginService(RtcService.class);
        this.rtcService.setRemoteVideoStreamShowMode(convertShowMode(localShowMode));

        LinkMicEventHandlerDispatcher handlerDispatcher = new LinkMicEventHandlerDispatcher(this);
        RtcEventHandlerImpl rtcEventHandler = new RtcEventHandlerImpl(handlerDispatcher);
        this.rtcService.addEventHandler(rtcEventHandler);
        this.rtcService.setRtcStreamConfigBeforePreview(getRtcStreamConfig());
    }

    /**
     * 加入连麦
     */
    protected void join() {
        isMicAllowed = true;
        // 外部设置了用外部的, 没设置用默认的
        if (isCameraOpened()) {
            openCamera();
        } else {
            closeCamera();
        }
        if (isMicOpened()) {
            openMic();
        } else {
            closeMic();
        }

        String currentNick = LivePrototype.getInstance().getOpenLiveParam().nick;
        rtcService.joinRtc(currentNick);
    }

    protected RtcStreamConfig getRtcStreamConfig() {
        // 默认是 640x480
        return new RtcStreamConfig();
    }

    @Override
    public boolean isJoined() {
        return state == State.ONLINE;
    }

    @Override
    public void leave() {
        rtcService.leaveRtc(false);
        joinedUsers.clear();
        userId2ViewTemp.clear();
    }

    @Override
    public boolean isCameraOpened() {
        return isCameraOpen == null ? DEFAULT_CAMERA_ENABLE : isCameraOpen;
    }

    @Override
    public View openCamera() {
        isCameraOpen = true;
        LinkMicUserModel user = joinedUsers.get(Const.getCurrentUserId());
        if (user != null) {
            user.isCameraOpen = true;
        }
        // 本地硬件摄像头开关, 可以调多次, 同一个View
        View preview = rtcService.startRtcPreview();
        videoCanvas = new AliRtcEngine.AliRtcVideoCanvas();
        videoCanvas.view = preview;
        videoCanvas.renderMode = convertRenderMode(localShowMode);
        rtcService.setLocalViewConfig(videoCanvas,
                AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
        // 是否发布本地相机流到远端
        rtcService.publishLocalVideo(true);
        // 采集 (如果关闭, 采集黑帧)
        rtcService.enableLocalVideo(true);
        // 发布
        rtcService.muteLocalCamera(false);
        return preview;
    }

    @Override
    public void closeCamera() {
        isCameraOpen = false;
        LinkMicUserModel user = joinedUsers.get(Const.getCurrentUserId());
        if (user != null) {
            user.isCameraOpen = false;
        }
        // 停止预览
        rtcService.stopPreview();
        // 是否发布本地相机流到远端
        rtcService.publishLocalVideo(false);
        // 采集
        rtcService.enableLocalVideo(false);
        // 发布
        rtcService.muteLocalCamera(true);
    }

    @Override
    public void switchCamera() {
        rtcService.switchCamera();
    }

    @Override
    public void setPreviewMirror(boolean enable) {
        rtcService.setPreviewMirror(enable);
    }

    @Override
    public void setCameraStreamMirror(boolean enable) {
        rtcService.setVideoStreamMirror(enable);
    }

    @Override
    public void setPreviewContentMode(ContentMode mode) {
        localShowMode = mode;

        // 正在预览时, 动态设置显示模式
        if (videoCanvas != null) {
            AliRtcEngine.AliRtcRenderMode currentMode = videoCanvas.renderMode;
            AliRtcEngine.AliRtcRenderMode targetMode = convertRenderMode(localShowMode);
            if (targetMode != currentMode) {
                videoCanvas.renderMode = targetMode;
                rtcService.setLocalViewConfig(videoCanvas,
                        AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera);
            }
        }
    }

    @Override
    public void setRemoteCameraContentMode(ContentMode mode) {
        rtcService.setRemoteVideoStreamShowMode(convertShowMode(mode));
    }

    @Override
    public boolean isMicOpened() {
        return isMicOpen == null ? DEFAULT_MIC_ENABLE : isMicOpen;
    }

    @Override
    public boolean isSelfMicAllowed() {
        return isMicAllowed;
    }

    public boolean isMicAllMuted() {
        return isMicAllMuted;
    }

    @Override
    public void openMic() {
        if (!isMicAllowed) {
            Logger.w(TAG, "Can't open mic, current mic's state is disallowed.");
            return;
        }

        if (isMicAllMuted) {
            Logger.w(TAG, "Can't open mic, mic all muted.");
            return;
        }

        isMicOpen = true;
        LinkMicUserModel user = joinedUsers.get(Const.getCurrentUserId());
        if (user != null) {
            user.isMicOpen = true;
        }
        rtcService.muteLocalMic(false);
    }

    @Override
    public void closeMic() {
        isMicOpen = false;
        LinkMicUserModel user = joinedUsers.get(Const.getCurrentUserId());
        if (user != null) {
            user.isMicOpen = false;
        }
        rtcService.muteLocalMic(true);
    }

    @Override
    public Map<String, LinkMicUserModel> getJoinedUsers() {
        return Collections.unmodifiableMap(joinedUsers);
    }

    @Override
    public void setBeautyOn(boolean beautyOn) {
        rtcService.setBeautyOn(beautyOn);
    }

    @Override
    public void onLeaveRoom() {
        removeAllEventHandler();
    }

    protected void onSelfJoinRtcSuccess() {

    }

    private class RtcEventHandlerImpl extends SampleRtcEventHandler {

        final LinkMicEventHandler handler;

        RtcEventHandlerImpl(LinkMicEventHandler handler) {
            this.handler = handler;
        }

        @Override
        public void onRtcJoinRtcSuccess(View view) {
            state = State.ONLINE;
            boolean isCameraOpened = isCameraOpened();
            String selfId = Const.getCurrentUserId();

            handler.onJoinedSuccess(view);

            LinkMicUserModel self = new LinkMicUserModel();
            self.userId = selfId;
            self.nickname = LivePrototype.getInstance().getCurrentNick();
            self.isCameraOpen = isCameraOpened;
            self.isMicOpen = isMicOpened();
            self.isAnchor = roomChannel.isOwner();
            self.cameraView = view;
            joinedUsers.put(selfId, self);
            handler.onUserJoined(ofSafeList(self));

            // 相机打开的话, 再主动推一次, 解决观众端无法展示的问题
            if (isCameraOpened) {
                // 是否发布本地相机流到远端
                rtcService.publishLocalVideo(true);
                // 采集 (如果关闭, 采集黑帧)
                rtcService.enableLocalVideo(true);
                // 发布
                rtcService.muteLocalCamera(false);
            }

            // 自己入会成功后的回调
            onSelfJoinRtcSuccess();

            // 加载申请用户
            RtcApplyUserParam applyingParam = new RtcApplyUserParam();
            applyingParam.pageNum = DEFAULT_PAGE_NUM;
            applyingParam.pageSize = DEFAULT_PAGE_SIZE;
            rtcService.listRtcApplyUser(applyingParam, new Callback<PageModel<ConfUserModel>>() {
                @Override
                public void onSuccess(PageModel<ConfUserModel> data) {
                    List<ConfUserModel> list = data.list;
                    if (CollectionUtil.isEmpty(list)) {
                        return;
                    }

                    List<LinkMicUserModel> mickUsers = new ArrayList<>();
                    for (ConfUserModel confUser : list) {
                        LinkMicUserModel micUser = convertModel(confUser);
                        mickUsers.add(micUser);
                    }
                    handler.onApplied(false, mickUsers);
                }

                @Override
                public void onError(String errorMsg) {

                }
            });

            rtcService.getRtcDetail(new Callback<ConfInfoModel>() {
                @Override
                public void onSuccess(ConfInfoModel data) {
                    Logger.i(TAG, "queryRtcDetailInfo onSuccess: " + data);
                    if (data != null && data.muteAll) {
                        onRtcMuteAllMic(true);
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    Logger.i(TAG, "queryRtcDetailInfo onError: " + errorMsg);
                }
            });
        }

        @Override
        public void onRtcLeaveRtcSuccess() {
            state = State.OFFLINE;
            isMicAllowed = true;
            userId2AudioEnable.clear();
            handler.onLeftSuccess();
        }

        @Override
        public void onRemoteVideoStreamChanged(VideoStream videoStream) {
            String userId = videoStream.userId;
            boolean available = videoStream.available;
            View view = videoStream.view;
            StreamType streamType = videoStream.streamType;
            LinkMicUserModel user = joinedUsers.get(userId);

            if (streamType == StreamType.CAMERA) {
                if (user == null) {
                    // 未记录的连麦用户, 先内部暂存, 等连麦时再回调出去 (为了保证外部拿到View时, 该用户连麦状态已回调)
                    userId2ViewTemp.put(userId, view);
                } else {
                    // 已经记录的连麦用户, 直接回调出去
                    if (available) {
                        boolean isAnchor = roomChannel.isOwner(userId);
                        user.isCameraOpen = true;
                        user.cameraView = view;
                        handler.onCameraStreamAvailable(userId, isAnchor, view);
                    }

                    user.isCameraOpen = available;
                    user.cameraView = view;
                    handler.onRemoteCameraStateChanged(userId, available);
                }
            }
        }

        @Override
        public void onRemoteUserOnLineNotify(String uid, AliRtcRemoteUserInfo userInfo, int elapsed) {
            // 麦克风状态, 优先从麦克风回调里去
            Boolean isAudioOpen = userId2AudioEnable.get(uid);
            if (isAudioOpen == null) {
                // 麦克风回调里取不到时, 直接认为是已开启的状态
                // 注: 此处有rtc的bug, 其中hasAudio不准确, 且true时无audioChange回调
                isAudioOpen = true;
            }

            LinkMicUserModel user = new LinkMicUserModel();
            user.userId = uid;
            user.nickname = userInfo.getDisplayName();
            user.isMicOpen = isAudioOpen;
            user.isCameraOpen = false;
            user.isAnchor = roomChannel.isOwner(uid);
            user.cameraView = null;

            joinedUsers.put(uid, user);

            handler.onUserJoined(ofSafeList(user));

            // 对暂存的视频流做回调 (回调务必要在onJoined之后, 以保证时序)
            // 这里直接对temp进行remove操作, 用完一次即清理
            View tempView = userId2ViewTemp.remove(uid);
            if (tempView != null) {
                boolean isAnchor = roomChannel.isOwner(uid);
                handler.onCameraStreamAvailable(uid, isAnchor, tempView);

                user.isCameraOpen = true;
                handler.onRemoteCameraStateChanged(uid, true);
            }
        }

        private List<LinkMicUserModel> convertModel(List<ConfUserModel> confUsers) {
            List<LinkMicUserModel> result = new ArrayList<>();
            if (confUsers != null) {
                for (ConfUserModel confUser : confUsers) {
                    LinkMicUserModel micUser = convertModel(confUser);
                    result.add(micUser);
                }
            }
            return result;
        }

        private LinkMicUserModel convertModel(ConfUserModel confUser) {
            LinkMicUserModel micUser = EntityConverter.confUser2MicUser(confUser);

            if (micUser != null) {
                micUser.isAnchor = roomChannel.isOwner(micUser.userId);
                micUser.nickname = confUser.nickname;
                boolean isSelf = TextUtils.equals(micUser.userId, Const.getCurrentUserId());
                if (isSelf) {
                    // 是自己时, 注入用户摄像头、麦克风等信息
                    micUser.isCameraOpen = isCameraOpen == null
                            ? DEFAULT_CAMERA_ENABLE : isCameraOpen;
                    micUser.isMicOpen = isMicOpen == null
                            ? DEFAULT_MIC_ENABLE : isMicOpen;
                }
            }

            return micUser;
        }

        @Override
        public void onRtcInviteRejected(List<ConfUserModel> rejectInviteUsers) {
            List<LinkMicUserModel> users = convertModel(rejectInviteUsers);
            handler.onInviteRejected(users);
        }

        @Override
        public void onRtcUserInvited(ConfInviteEvent event) {
            LinkMicUserModel inviter = convertModel(event.caller);
            List<LinkMicUserModel> invitedUsers = convertModel(event.calleeList);
            List<LinkMicUserModel> calledInvitedUsers = new ArrayList<>();
            for (LinkMicUserModel invitedUser : invitedUsers) {
                String myUserId = Const.getCurrentUserId();
                boolean isSelf = TextUtils.equals(invitedUser.userId, myUserId);
                if (isSelf) {
                    if (state == State.APPLYING) {
                        // 我正在申请中, 主播同时在邀请我, 此时自动作为主播同意我的申请来处理
                        handler.onApplyResponse(true, myUserId);
                        continue;
                    }
                    state = State.INVITED;
                }
                calledInvitedUsers.add(invitedUser);
            }

            if (CollectionUtil.isNotEmpty(calledInvitedUsers)) {
                handler.onInvited(inviter, calledInvitedUsers);
            }
        }

        @Override
        public void onRtcKickUser(ConfUserEvent event) {
            List<ConfUserModel> userList = event == null ? null : event.userList;
            if (CollectionUtil.isEmpty(userList)) {
                return;
            }

            List<LinkMicUserModel> kickedUsers = new ArrayList<>();
            for (ConfUserModel userModel : userList) {
                boolean isSelf = TextUtils.equals(Const.getCurrentUserId(), userModel.userId);
                if (isSelf) {
                    if (isJoined()) {
                        leave();
                    } else {
                        // 主播取消邀请我, 如果没在麦上, 就改为麦下状态; 已上麦就不改了
                        if (state != State.ONLINE) {
                            state = State.OFFLINE;
                        }
                        // 自己的邀请被主播取消 (服务端将取消邀请和踢出放在了同一个接口里)
                        handler.onInviteCanceledForMe();
                        continue;
                    }
                }

                LinkMicUserModel micUser = convertModel(userModel);
                kickedUsers.add(micUser);
            }

            if (CollectionUtil.isNotEmpty(kickedUsers)) {
                handler.onKicked(kickedUsers);
            }
        }

        @Override
        public void onRemoteUserOffLineNotify(String uid, AliRtcRemoteUserInfo userInfo, AliRtcEngine.AliRtcUserOfflineReason reason) {
            LinkMicUserModel user = joinedUsers.remove(uid);
            userId2ViewTemp.remove(uid);
            userId2AudioEnable.remove(uid);
            if (user != null) {
                handler.onUserLeft(ofSafeList(user));
            }
        }

        @Override
        public void onRtcApplyJoinChannel(ConfApplyJoinChannelEvent event) {
            LinkMicUserModel micUser = convertModel(event.applyUser);
            if (micUser == null) {
                return;
            }

            List<LinkMicUserModel> users = ofSafeList(micUser);
            if (event.isApply) {
                handler.onApplied(true, users);
            } else {
                handler.onApplyCanceled(users);
            }
        }

        @Override
        public void onRtcHandleApplyChannel(ConfHandleApplyEvent event) {
            if (TextUtils.equals(event.uid, Const.getCurrentUserId())) {
                if (event.approve) {
                    if (state == State.ONLINE) {
                        // 主播同意我的申请时, 我已经在麦上, 则不处理
                        return;
                    }
                } else {
                    state = State.OFFLINE;
                }
            }

            handler.onApplyResponse(event.approve, event.uid);
        }

        @Override
        public void onRtcPassiveMuteMic(boolean mute, String uid) {
            if (TextUtils.equals(Const.getCurrentUserId(), uid)) {
                // 1. 主播单独静音self，self任然有mic 操作权限，所以不对 isMicAllowed 进行变更
                // 2. 主播邀请开麦时，不直接对mic进行操作，透传给业务层决定如何处理开麦请求
                if (mute) {
                    closeMic();
                    handler.onSelfMicClosedByAnchor();
                } else {
                    handler.onAnchorInviteToOpenMic();
                }
            }
        }

        @Override
        public void onRtcMuteAllMic(boolean mute) {
            isMicAllMuted = mute;
            if (mute) {
                isMicAllowed = false;
                closeMic();
                handler.onSelfMicAllowed(false); // 版本兼容；保留调用，旧版兼容
                handler.onAllMicAllowed(false);
            } else {
                isMicAllowed = true;
                handler.onSelfMicAllowed(true); // 版本兼容；保留调用，旧版兼容
                handler.onAllMicAllowed(true);
            }
        }

        @Override
        public void onOthersAudioMuted(String uid, boolean isMute) {
            boolean isMicOpen = !isMute;
            LinkMicUserModel user = joinedUsers.get(uid);
            if (user != null) {
                user.isMicOpen = isMicOpen;
            }
            userId2AudioEnable.put(uid, isMicOpen);
            handler.onRemoteMicStateChanged(uid, isMicOpen);
        }
    }

    protected enum State {
        OFFLINE,
        INVITED,
        APPLYING,
        ONLINE,
    }

    private static AliRtcEngine.AliRtcRenderMode convertRenderMode(ContentMode mode) {
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

    private static VideoStreamShowMode convertShowMode(ContentMode mode) {
        switch (mode) {
            case Stretch:
                return VideoStreamShowMode.Stretch;
            case Fill:
                return VideoStreamShowMode.Fill;
            case Crop:
            default:
                return VideoStreamShowMode.Crop;
        }
    }

    private static <T> List<T> ofSafeList(T element) {
        List<T> list = new ArrayList<>();
        list.add(element);
        return list;
    }
}
