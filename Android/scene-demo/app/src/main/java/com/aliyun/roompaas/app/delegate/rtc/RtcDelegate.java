package com.aliyun.roompaas.app.delegate.rtc;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.dingpaas.rtc.ConfInfoModel;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alibaba.fastjson.JSON;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.delegate.chat.ISystemMessage;
import com.aliyun.roompaas.app.helper.AliRtcHelper;
import com.aliyun.roompaas.app.helper.RoomHelper;
import com.aliyun.roompaas.app.helper.UserHelper;
import com.aliyun.roompaas.app.manager.RtcUserManager;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.util.EventHandlerUtil;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.exposable.NegCallback;
import com.aliyun.roompaas.base.exposable.PosCallback;
import com.aliyun.roompaas.base.exposable.SimpleCallback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.CommonUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.exposable.RoomChannel;
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.SampleRtcEventHandler;
import com.aliyun.roompaas.rtc.exposable.RTCBypassPeerVideoConfig;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcStreamConfig;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;
import com.aliyun.roompaas.rtc.exposable.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfHandleApplyEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;
import com.aliyun.roompaas.uibase.util.ViewUtil;

import org.webrtc.sdk.SophonSurfaceView;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import java8.util.stream.Collectors;
import java8.util.stream.StreamSupport;

/**
 * Created by KyleCe on 2021/9/15
 */
public class RtcDelegate extends SampleRtcEventHandler {
    public static final String TAG = "RtcDelegate";
    private Activity activity;
    private Reference<ISystemMessage> systemMessageRef;
    private final Reference<RtcService> rtcServiceRef;
    private final Reference<RoomChannel> roomChannelRef;
    private final String userId;
    private final String nick;
    private StudentRtcDelegate studentRtcDelegate;
    private RtcSubscribeDelegate rtcSubscribeDelegate;
    private boolean hasShowNetwork;
    private boolean muteLocalMic;
    private boolean muteLocalCamera;
    private Context context;
    private ViewGroup rtcContainer;
    private IRtcDelegateReceiver rtcDelegateReceiver;
    private boolean muteToastHint;

    public RtcDelegate(Activity activity, ISystemMessage systemMessage, String userId, String nick
            , RoomChannel roomChannel, RtcService rtcService
            , ViewGroup rtcContainer, IRtcDelegateReceiver receiver) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        this.systemMessageRef = new WeakReference<>(systemMessage);
        this.userId = userId;
        this.nick = nick;
        this.rtcServiceRef = new WeakReference<>(rtcService);
        this.roomChannelRef = new WeakReference<>(roomChannel);
        this.rtcContainer = rtcContainer;
        this.rtcDelegateReceiver = receiver;
    }

    public void addEventHandler() {
        EventHandlerUtil.addEventHandler(rtcServiceRef, this);
    }

    public void joinRtcWithConfig(int width, int height, String nick) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        rtcService.joinRtcWithConfig(new RtcStreamConfig(width, height, false, RtcStreamConfig.BypassLiveResolutionType.Type_720x1280), nick);
    }

    public View startRtcPreview() {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return null;
        }

        return rtcService.startRtcPreview();
    }

    public boolean hasRtc() {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return false;
        }

        return rtcService.hasRtc();
    }

    public void onHandleUserApply(RtcUser model, boolean agree) {
        if (agree) {
            // 同意, 则触发会议邀请
            onInviteUser(model);
        }

        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        rtcService.handleApplyJoinRtc(model.userId, agree, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                toast(agree ? "已同意连麦" : "已拒绝连麦");
            }

            @Override
            public void onError(String errorMsg) {
                toast("处理失败: " + errorMsg);
            }
        });
    }

    // 老师挂断
    public void onKickFromChannel(String kickedUserId) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        rtcService.kickUserFromRtc(Collections.singletonList(kickedUserId), new SimpleCallback<Void>() {
            @Override
            public void onError(String errorMsg) {
                toast("挂断失败: " + errorMsg);
            }
        });
    }

    public void onInviteUser(RtcUser invitedUser) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        ConfUserModel userModel = new ConfUserModel();
        userModel.userId = invitedUser.userId;
        userModel.nickname = invitedUser.nick;
        List<ConfUserModel> userModels = Collections.singletonList(userModel);
        rtcService.inviteJoinRtc(userModels, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                toast("邀请已发送");
            }

            @Override
            public void onError(String errorMsg) {
                toast("邀请失败: " + errorMsg);
            }
        });
    }

    public void onApplyJoinRtc(boolean isApply, PosCallback<Void> posCallback, NegCallback negCallback) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        rtcService.applyJoinRtc(isApply, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                Utils.callSuccess(posCallback, data);
            }

            @Override
            public void onError(String errorMsg) {
                Utils.callError(negCallback, errorMsg);
            }
        });
    }

    public void switchSceneByRtcStatus(Runnable ongoingAction, Runnable notPresentingAction) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            Utils.run(notPresentingAction);
            return;
        }

        rtcService.getRtcDetail(new Callback<ConfInfoModel>() {
            @Override
            public void onSuccess(ConfInfoModel data) {
                Logger.i(TAG, "queryRtcDetailInfo onSuccess: " + data);
                if (data != null && data.startTime != 0 && data.status == RoomHelper.ConferenceStatus.ON_GOING) {
                    Utils.run(ongoingAction);
                } else {
                    Utils.run(notPresentingAction);
                }
            }

            @Override
            public void onError(String errorMsg) {
                Logger.i(TAG, "queryRtcDetailInfo onError: " + errorMsg);
                Utils.run(notPresentingAction);
            }
        });
    }

    public void muteLocalMic() {
        muteLocalMic = !muteLocalMic;
        ofStudentRtcDelegate().updateLocalMic(userId, muteLocalMic);

        rtcMuteLocalMic(muteLocalMic);
    }

    private void rtcMuteLocalMic(boolean muteLocalMic) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService != null) {
            rtcService.muteLocalMic(muteLocalMic);
        }
    }

    public void muteLocalCamera() {
        muteLocalCamera = !muteLocalCamera;
        ofStudentRtcDelegate().updateLocalCamera(userId, muteLocalCamera);

        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService != null) {
            rtcService.muteLocalCamera(muteLocalCamera);
        }
    }

    public void switchCamera() {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService != null) {
            rtcService.switchCamera();
        }
    }

    @Override
    public void onRtcStreamIn(RtcStreamEvent event) {
        addSystemMessage("Rtc流进入: " + event.userId);
        playRtc(event);
    }

    @Override
    public void onRtcStreamUpdate(RtcStreamEvent event) {
        addSystemMessage("Rtc流更新: " + event.userId);
    }

    @Override
    public void onRtcStreamOut(String userId) {
        addSystemMessage("Rtc流退出: " + userId);
        ofStudentRtcDelegate().removeData(userId);
    }

    @Override
    public void onRtcUserInvited(ConfInviteEvent event) {
        // 被邀请人Id
        List<ConfUserModel> calleeList = event.calleeList;
        if (CollectionUtil.isEmpty(calleeList)) {
            return;
        }

        List<RtcUser> toUpdateList = new ArrayList<>();
        String teacherNick = event.caller.nickname;

        RtcService rtcService = Utils.getRef(rtcServiceRef);

        for (ConfUserModel userModel : calleeList) {
            //boolean isSelf = TextUtils.equals(userModel.userId, userId);
            if (isSelf(userModel.userId)) {
                // 被邀请人是自己, 弹窗询问
                String message = teacherNick + "邀请你上麦，是否同意？";
                if (rtcService != null) {
                    DialogUtil.confirm(activity, message,
                            () -> rtcService.joinRtcWithConfig(new RtcStreamConfig(480, 360), nick),
                            () -> rtcService.reportJoinStatus(RtcUserStatus.JOIN_FAILED, null)
                    );
                }
            } else {
                // 被邀请人是其他人, 面板提示
                addSystemMessage(teacherNick + "正在邀请" + userModel.nickname + "上麦");

                // 用户列表更改为"呼叫中"
                toUpdateList.add(RtcUserManager.asRtcUser(userModel.userId, userModel.nickname, RtcUserStatus.ON_JOINING));
            }
        }
        updateUser(toUpdateList);
    }

    private void updateUser(String uid, RtcUserStatus status) {
        updateUser(Collections.singletonList(RtcUserManager.asRtcUser(uid, status)));
    }

    private void updateUser(String uid, String nick, RtcUserStatus status) {
        updateUser(Collections.singletonList(RtcUserManager.asRtcUser(uid, nick, status)));
    }

    private void updateUser(Collection<RtcUser> list) {
        if (rtcDelegateReceiver != null && Utils.isNotEmpty(list)) {
            rtcDelegateReceiver.updateUser(list);
        }
    }

    @Override
    public void onRtcKickUser(ConfUserEvent event) {
        List<ConfUserModel> userList = event.userList;
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }

        List<String> userIdList = new ArrayList<>(userList.size());

        filterUserListWithValidId(userList, new Callbacks.PosLambda<>((pair) -> {
            userIdList.add(pair.first);
            if (!isSelf(pair.first)) {
                addSystemMessage(pair.first + "已下麦");
            } else {
                kickedOffline();
            }
        }));

        ofStudentRtcDelegate().removeData(userIdList);
    }

    void kickedOffline() {
        toast("老师已将你下麦");
        leaveRtcProcess();
        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.onRtcGetKickedOffline();
        }
    }

    public void leaveRtcProcess() {
        ofStudentRtcDelegate().removeAll();

        leaveRtc(false);
        muteLocalMic = false;
        muteLocalCamera = false;
        ofRtcSubscribeDelegate().unsubscribe(parseOwnerId());
    }

    public void leaveRtc(boolean destroy) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService != null) {
            rtcService.leaveRtc(destroy);
        }
    }

    private String parseOwnerId() {
        return RoomHelper.getOwnerId(Utils.getRef(roomChannelRef));
    }

    @Override
    public void onRtcLeaveUser(ConfUserEvent leaveUserEvent) {
        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.usersLeave(leaveUserEvent);
        }

        List<ConfUserModel> leaveUserList = leaveUserEvent.userList;
        filterUserListWithValidId(leaveUserList, new Callbacks.PosLambda<>((pair) -> {
            ofStudentRtcDelegate().removeData(pair.first);
            addSystemMessage(pair.first + ":已离开会议");
        }));
    }

    public static void filterUserListWithValidId(Collection<ConfUserModel> collection, Callbacks.PosLambda<Pair<String, ConfUserModel>> callback) {
        for (ConfUserModel model : collection) {
            String id;
            if (model == null || TextUtils.isEmpty(id = model.userId)) {
                continue;
            }

            Utils.callSuccess(callback, new Pair<>(id, model));
        }
    }

    @Override
    public void onRtcStart(ConfEvent confStartEvent) {
        Logger.i(TAG, "onRtcStart" + confStartEvent);
        // 老师开始上课
        toast("老师开始上课");
        addSystemMessage("老师开始上课");
        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.onRtcStart();
        }
    }

    @Override
    public void onRtcEnd(ConfEvent confEndEvent) {
        Logger.i(TAG, "onRtcEnd" + confEndEvent);
        toast("老师结束上课");
        addSystemMessage("老师结束上课");

        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.onRtcEnd();
        }

        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService != null) {
            rtcService.stopPreview();
            rtcService.stopPlayRoad();
        }

        leaveRtcProcess();
        removeSohponSurfaceView(rtcContainer);
        removeRoadSurfaceView();
    }

    @Override
    public void onRtcApplyJoinChannel(ConfApplyJoinChannelEvent event) {
        ConfUserModel applyUser = event != null ? event.applyUser : null;
        if (applyUser == null || rtcDelegateReceiver == null) {
            return;
        }

        addSystemMessage(applyUser.userId + (event.isApply ? "申请上麦" : "上麦申请已取消"));

        updateUser(applyUser.userId, applyUser.nickname, event.isApply ? RtcUserStatus.APPLYING : RtcUserStatus.LEAVE);
    }

    @Override
    public void onRtcHandleApplyChannel(ConfHandleApplyEvent event) {
        String message = "老师拒绝连麦申请";
        addSystemMessage(message);
        if (event.approve) {
            // 老师统一申请, "申请中" 改为 "呼叫中"
            updateUser(event.uid, RtcUserStatus.ON_JOINING);
            return;
        }

        if (isSelf(event.uid)) {
            // 自己被拒绝, 申请上麦时弹窗提示, 并更改上麦按钮状态
            DialogUtil.confirm(activity, message, null);
            if (rtcDelegateReceiver != null) {
                rtcDelegateReceiver.onRtcLinkRequestRejected();
            }
        } else {
            // 别人被拒绝, 更改用户列表状态
            if (isTeacherAndOwner()) {
                // 老师收到被拒绝的事件
                toast(event.uid + "拒绝了您的邀请");
            }
            updateUser(event.uid, RtcUserStatus.LEAVE);
        }
    }

    @Override
    public void onRtcRemoteJoinSuccess(ConfUserEvent event) {
        addSystemMessage("会议成员变更: " + JSON.toJSONString(event.userList));

        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.onRtcRemoteJoinSuccess(event);
        }
    }

    @Override
    public void onRtcRemoteJoinFail(ConfUserEvent event) {
        addSystemMessage("会议成员变更: " + JSON.toJSONString(event.userList));

        updateUser(convertIntoRtcUserListWithStatus(event, RtcUserStatus.JOIN_FAILED));
    }

    @NonNull
    public static List<RtcUser> convertIntoRtcUserListWithStatus(ConfUserEvent confUserEvent, RtcUserStatus status) {
        List<ConfUserModel> userList;
        if (confUserEvent == null || CollectionUtil.isEmpty((userList = confUserEvent.userList))) {
            return new ArrayList<>();
        }

        List<RtcUser> list = new ArrayList<>();
        filterUserListWithValidId(userList, new Callbacks.PosLambda<>((pair) -> {
            list.add(RtcUserManager.asRtcUser(pair.second.userId, pair.second.nickname, status));
        }));
        return list;
    }

    @Override
    public void onRtcConfUpdated(ConfEvent event) {
        addSystemMessage("会议变更: " + JSON.toJSONString(event));
    }

    @Override
    public void onRtcJoinRtcSuccess(View view) {
        addSystemMessage("上麦成功");
        // 大班课的场景
        if (isTeacherAndOwner()) {
            startRoadPublish();
        } else {
            ofStudentRtcDelegate().updateData(assembleRtcStreamEventForSelf(true));
            if (rtcDelegateReceiver != null) {
                rtcDelegateReceiver.onRtcJoinRtcSuccess();
            }
        }
    }

    private void startRoadPublish() {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        // 老师上麦后默认开启旁路推流
        rtcService.publishLocalVideo(true);
        rtcService.startRoadPublish(new Callbacks.Lambda<>((success, data, errorMsg) -> {
            if (success) {
                if (rtcDelegateReceiver != null) {
                    rtcDelegateReceiver.startRoadPublishSuccess();
                }

                RTCBypassPeerVideoConfig conf = new RTCBypassPeerVideoConfig(0f,0f,1,1,1,parseOwnerId());
                rtcService.setCustomBypassLiveLayout(Collections.singletonList(conf), new Callback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                    }

                    @Override
                    public void onError(String s) {

                    }
                });
            } else {
                toast("推流失败: " + errorMsg);
            }
        }));
    }

    private RtcStreamEvent assembleRtcStreamEventForSelf(boolean certainlyNotTeacher) {
        return new RtcStreamEvent.Builder()
                .setUserId(userId)
                .setAliRtcVideoTrack(AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera)
                .setUserName("我")
                .setLocalStream(true)
                .setTeacher(!certainlyNotTeacher && isTeacherAndOwner())
                .setAliVideoCanvas(new AliRtcEngine.AliRtcVideoCanvas())
                .build();
    }

    @Override
    public void onRtcJoinRtcError(String event) {
        addSystemMessage("上课失败, " + event);

        if (rtcDelegateReceiver != null) {
            rtcDelegateReceiver.onRtcJoinRtcError(event);
        }
    }

    @Override
    public void onRtcNetworkQualityChanged(String uid) {
        if (!hasShowNetwork) {
            toast(isSelf(uid) ? "当前网络不佳" : "对方网络不佳");
            hasShowNetwork = true;
        }
    }

    @Override
    public void onRtcUserAudioMuted(String uid) {
        if (isSelf(uid)) {
            muteLocalMic = true;
            rtcMuteLocalMic(true);

            if (rtcDelegateReceiver != null) {
                rtcDelegateReceiver.onUpdateSelfMicStatus(true);
            }
        }
        ofStudentRtcDelegate().updateLocalMic(uid, true);
    }

    @Override
    public void onRtcUserAudioEnable(String uid) {
        if (isSelf(uid)) {
            muteLocalMic = false;
            rtcMuteLocalMic(false);

            if (rtcDelegateReceiver != null) {
                rtcDelegateReceiver.onUpdateSelfMicStatus(false);
            }
        }
        ofStudentRtcDelegate().updateLocalMic(uid, false);
    }

    @Override
    public void onRtcUserVideoMuted(String uid) {
        if (isSelf(uid)) {
            muteLocalCamera = true;
        }
        ofStudentRtcDelegate().updateLocalCamera(uid, true);
    }

    @Override
    public void onRtcUserVideoEnable(String uid) {
        if (isSelf(uid)) {
            muteLocalCamera = false;
        }
        ofStudentRtcDelegate().updateLocalCamera(uid, false);
    }

    private boolean isSelf(String uid) {
        return TextUtils.equals(uid, userId);
    }

    private void playRtc(RtcStreamEvent rtcStreamEvent) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcStreamEvent == null || rtcService == null) {
            return;
        }
        if (rtcStreamEvent.isTeacher) {
            rtcService.stopPlayRoad();
            removeRoadSurfaceView();

            AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = rtcStreamEvent.aliVideoCanvas;
            AliRtcHelper.fillCanvasViewIfNecessary(aliVideoCanvas, context, false);
            ViewUtil.addChildMatchParentSafely(true, rtcContainer, aliVideoCanvas.view);

            rtcService.setRemoteViewConfig(aliVideoCanvas, rtcStreamEvent.userId
                    , AliRtcHelper.interceptTrack(rtcStreamEvent.aliRtcVideoTrack));

            ofRtcSubscribeDelegate().subscribe(rtcStreamEvent);
        } else {
            ofStudentRtcDelegate().updateData(rtcStreamEvent);
        }
    }

    private boolean removeRoadSurfaceView() {
        ViewGroup roadContainer = rtcContainer;
        if (roadContainer == null) {
            return false;
        }

        for (int i = 0, size = roadContainer.getChildCount(); i < size; i++) {
            View child = roadContainer.getChildAt(i);
            if (child == null || child.getTag(R.integer.viewTagMarkForRoadPlayerWrapper) == null) {
                continue;
            }

            ViewUtil.removeSelfSafely(child);
            return true;
        }

        removeSohponSurfaceView(roadContainer);
        return false;
    }

    private void removeSohponSurfaceView(@Nullable ViewGroup vg) {
        ViewUtil.removeSelfSafely(parsePossibleSophonSurfaceView(vg));
    }

    @Nullable
    private View parsePossibleSophonSurfaceView(@Nullable ViewGroup roadVG) {
        if (roadVG == null) {
            return null;
        }

        for (int i = 0, size = roadVG.getChildCount(); i < size; i++) {
            View child = roadVG.getChildAt(i);
            if (child instanceof SophonSurfaceView) {
                return child;
            }
        }
        return null;
    }

    // 设置布局信息
    public void setLayoutModel(RtcLayoutModel model) {
        RtcService rtcService = Utils.getRef(rtcServiceRef);
        if (rtcService == null) {
            return;
        }

        List<String> userIds = new ArrayList();
        switch (model) {
            case ONE_GRID:
                userIds.add(parseOwnerId());
                break;
            case ONE_SUPPORT_FOUR:
                userIds = getRtcUsers();
                userIds.add(parseOwnerId());
                break;
            case NINE_GRID:
                userIds = getRtcUsers();
                break;

        }
        rtcService.setLayout(userIds, model, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                addSystemMessage("设置布局成功");
            }

            @Override
            public void onError(String errorMsg) {
                toast(errorMsg);
            }
        });
    }

    private List<String> getRtcUsers() {
        List<RtcUser> list = rtcDelegateReceiver != null ? rtcDelegateReceiver.getUserList() : null;
        if (list == null) {
            return new ArrayList<>(0);
        }

        return StreamSupport.stream(list)
                .filter(rtcUser -> rtcUser.status == RtcUserStatus.ACTIVE)
                .map(rtcUser -> rtcUser.userId)
                .collect(Collectors.toList());
    }

    public void setMuteToastHint(boolean muteToastHint) {
        this.muteToastHint = muteToastHint;
    }

    private boolean isTeacherAndOwner() {
        return UserHelper.isOwner(context, Utils.getRef(roomChannelRef));
    }

    private void toast(CharSequence cs) {
        if (muteToastHint || TextUtils.isEmpty(cs) || isDestroyed()) {
            Logger.i(TAG, "toast: end--invalid param: muteToastHint, cs= " + cs);
            return;
        }

        CommonUtil.showToast(context, cs.toString());
    }

    private void addSystemMessage(String msg) {
        ISystemMessage systemMessage = Utils.getRef(systemMessageRef);

        if (systemMessage != null) {
            systemMessage.addSystemMessage(msg);
        }
    }

    private StudentRtcDelegate ofStudentRtcDelegate() {
        if (studentRtcDelegate == null) {
            studentRtcDelegate = new StudentRtcDelegate(activity, Utils.getRef(roomChannelRef)
                    , Utils.getRef(rtcServiceRef));
        }
        return studentRtcDelegate;
    }

    private RtcSubscribeDelegate ofRtcSubscribeDelegate() {
        if (rtcSubscribeDelegate == null) {
            rtcSubscribeDelegate = new RtcSubscribeDelegate(Utils.getRef(rtcServiceRef));
        }

        return rtcSubscribeDelegate;
    }

    @Override
    public void destroy() {
        super.destroy();
        EventHandlerUtil.removeEventHandler(rtcServiceRef, this);

        Utils.clear(rtcServiceRef, systemMessageRef);
        Utils.destroy(studentRtcDelegate, rtcSubscribeDelegate);
        rtcDelegateReceiver = null;
        activity = null;
    }
}
