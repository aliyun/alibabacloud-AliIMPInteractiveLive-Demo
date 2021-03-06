package com.aliyun.roompaas.app.activity.classroom;

import static com.aliyun.roompaas.app.activity.classroom.ClassFunctionsAdapter.FunctionName.Join_RTC;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.delegate.chat.ISystemMessage;
import com.aliyun.roompaas.app.delegate.rtc.IRtcDelegateReceiver;
import com.aliyun.roompaas.app.delegate.rtc.RtcDelegate;
import com.aliyun.roompaas.app.manager.RtcUserManager;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.viewmodel.WhiteBoardVM;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.Check;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.CommentParam;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;
import com.aliyun.roompaas.uibase.util.ViewUtil;
import com.aliyun.roompaas.whiteboard.exposable.ToolbarOrientation;
import com.aliyun.roompaas.whiteboard.exposable.WhiteboardService;

import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java8.util.stream.StreamSupport;

/**
 * ??????????????????
 *
 * @author puke
 * @version 2021/5/24
 */
public class ClassroomActivity extends BaseRoomActivity implements IWhiteBoardOperate,
        StudentListAdapter.ItemClickListener, ClassFunctionsAdapter.FunctionCheckedListener
, ISystemMessage, IRtcDelegateReceiver {

    private static final String TAG = ClassroomActivity.class.getSimpleName();

    private ClassroomView view;
    private RtcUserManager rtcUserManager;
    private IWhiteBoardOperate whiteBoardVM = IWhiteBoardOperate.NULL;

    private WhiteboardService whiteboardService;
    private RtcService rtcService;

    private ClassFunctionsAdapter functionAdapter;
    private RtcStreamEvent displayVideoStreamInfo;
    private boolean isJoined;
    private boolean isApplyed;

    private RtcDelegate rtcDelegate;

    @Override
    public String getRoomId() {
        return roomId;
    }

    @Override
    public void whiteBoardProcess() {
        whiteBoardVM.whiteBoardProcess();
    }

    @Override
    public void openWhiteBoard(Callback<View> callback) {
        whiteBoardVM.openWhiteBoard(callback);
    }

    @Override
    public void setToolbarOrientation(ToolbarOrientation orientation) {
       whiteBoardVM.setToolbarOrientation(orientation);
    }

    @Override
    public void setToolbarVisibility(int visibility) {
        whiteBoardVM.setToolbarVisibility(visibility);
    }

    @Override
    public void getScale(Callback<Float> callback) {
        whiteBoardVM.getScale(callback);
    }

    @Override
    public void setScale(float scale, @Nullable Runnable resultAction) {
        whiteBoardVM.setScale(scale, resultAction);
    }

    @Override
    public void startWhiteboardRecording() {
        whiteBoardVM.startWhiteboardRecording();
    }

    @Override
    protected void init() {
        super.init();
        setContentView(R.layout.activity_classroom);
        view = new ClassroomView(this);

        // ????????????
        whiteboardService = roomChannel.getPluginService(WhiteboardService.class);
        rtcService = roomChannel.getPluginService(RtcService.class);

        // ?????????????????????
        roomChannel.addEventHandler(new RoomEventHandlerImpl());
        chatService.addEventHandler(new ChatEventHandlerImpl());
        liveService.addEventHandler(new LiveEventHandlerImpl());
        ofRtcDelegate().addEventHandler();

        rtcUserManager = new RtcUserManager(roomChannel);
        whiteBoardVM = new WhiteBoardVM(roomChannel);
    }

    @Override
    protected void onEnterRoomSuccess(RoomDetail roomDetail) {
        // ????????????
        loadComment();

        updateTitle();

        // ????????????
        whiteBoardVM.whiteBoardProcess();

        if (isOwner()) {
            teacherSceneInit();
        } else {
            // ??????????????????
            loadUser(false);
            // ????????????, ????????????
            tryPlayLive();
            // ??????????????????
            initFunctionAdapterIfNeed();
        }
    }

    private void teacherSceneInit() {
        ViewUtil.setVisible(view.startClass);
        ViewUtil.setGone(view.endClass);
        previewAndJoin();
    }

    private void previewAndJoin() {
        updateRoadRender(ofRtcDelegate().startRtcPreview());
    }

    private void tryPlayLive() {
        view.functionList.setVisibility(View.VISIBLE);
        livePlayerService.tryPlay(new Callback<View>() {
            @Override
            public void onSuccess(View renderView) {
                updateRoadRender(renderView);
                if (!rtcService.hasRtc()) {
                    showToast("??????????????????");
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    private void loadUser(boolean loadRtcUserList) {
        loadUser(loadRtcUserList, null);
    }

    private void loadUser(boolean loadRtcUserList, @Nullable Callback<List<RtcUser>> callback) {
        rtcUserManager.loadUserList(loadRtcUserList, new Callback<List<RtcUser>>() {
            @Override
            public void onSuccess(List<RtcUser> data) {
                view.studentView.setData(data);
                Utils.callSuccess(callback, data);
            }

            @Override
            public void onError(String errorMsg) {
                showToast("????????????????????????: " + errorMsg);
                Utils.callError(callback, errorMsg);
            }
        });
    }

    public void updateTitle() {
        if (view != null && !TextUtils.isEmpty(roomTitle)) {
            view.updateTitle(roomTitle);
        }
    }

    // ??????????????????
    private void loadComment() {
        CommentParam commentParam = new CommentParam();
        commentParam.pageNum = 1;
        commentParam.pageSize = 100;
        commentParam.sortType = CommentSortType.TIME_DESC;
        chatService.listComment(commentParam, new Callback<PageModel<CommentModel>>() {
            @Override
            public void onSuccess(PageModel<CommentModel> pageModel) {
                List<CommentModel> list = pageModel.list;
                if (CollectionUtil.isNotEmpty(list)) {
                    // ???????????????????????????
                    List<MessageModel> addedList = new ArrayList<>();
                    // ????????????
                    for (int i = list.size() - 1; i >= 0; i--) {
                        CommentModel model = list.get(i);
                        addedList.add(new MessageModel(model.creatorNick, model.content));
                    }
                    view.chatView.addMessage(addedList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("????????????????????????: " + errorMsg);
            }
        });
    }

    // ????????????, ????????????????????????
    public void onUserClick(RtcUser model) {
        // do nothing
    }

    @Override
    public void addSystemMessage(String message) {
        Runnable task = () -> view.chatView.addSystemMessage(message);
        if (Check.checkMainThread()) {
            task.run();
        } else {
            ThreadUtil.runOnUiThread(task);
        }
    }

    void onSend(String inputText) {
        if (TextUtils.isEmpty(inputText)) {
            showToast("??????????????????");
            return;
        }

        chatService.sendComment(inputText, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                view.clearInput();
                view.shrinkKeyboard();
            }

            @Override
            public void onError(String errorMsg) {
                showToast("????????????: " + errorMsg);
            }
        });
    }

    // ????????????
    void onStartClass() {
        if (!isJoined) {
            ofRtcDelegate().joinRtcWithConfig(1280, 720, nick);
            showToast("????????????, ?????????...");
        }
    }

    void onEndClass() {
        if (isJoined) {
            leaveRtcProcess();
            if (isOwner()) {
                teacherSceneInit();
            }
        }
    }

    // ????????????
    void onStudentJoinChannel() {
        if (isJoined) {
            leaveRtcProcess();
            livePlayerService.tryPlay(new Callback<View>() {
                @Override
                public void onSuccess(View renderView) {
                    updateRoadRender(renderView);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else {
            // ???????????????, ?????????????????????
            onApplyJoinRtc(!isApplyed);
        }
    }

    private void updateRoadRender(View toAdd) {
        ViewGroup vg = view != null ? view.roadRenderContainer : null;
        ViewUtil.removeSelfSafely(ViewUtil.findFirstSurfaceViewAtLevel0(vg));
        if (toAdd != null) {
            toAdd.setTag(R.integer.viewTagMarkForRoadPlayerWrapper, toAdd.hashCode());
        }
        ViewUtil.addChildMatchParentSafely(true, vg, toAdd);
    }

    private void leaveRtcProcess() {
        ofRtcDelegate().leaveRtcProcess();

        isJoined = false;
        isApplyed = false;
        if (functionAdapter != null) {
            functionAdapter.initBtnStatus();
            functionAdapter.updateFunction(Join_RTC, "??????");
        }
        displayVideoStreamInfo = null;
    }

    // ??????????????????????????????
    public void onHandleUserApply(RtcUser model, boolean agree) {
        ofRtcDelegate().onHandleUserApply(model, agree);
    }

    // ????????????
    public void onKickFromChannel(String userId) {
        ofRtcDelegate().onKickFromChannel(userId);
    }

    // ????????????
    public void onInviteUser(RtcUser invitedUser) {
        ofRtcDelegate().onInviteUser(invitedUser);
    }

    // ??????????????????
    private void onApplyJoinRtc(boolean isApply) {
        ofRtcDelegate().onApplyJoinRtc(isApply, data -> {
            showToast(isApply ? "?????????????????????" : "???????????????????????????");
            isApplyed = isApply;
        }, (errorMsg) -> {
            showToast((isApply ? "??????????????????: " : "????????????????????????: ") + errorMsg);
            isApplyed = !isApply;
        });
    }

    // ???????????????Adapter init
    private void initFunctionAdapterIfNeed() {
        if (functionAdapter == null) {
            functionAdapter = new ClassFunctionsAdapter(this);
            functionAdapter.setListener(this);
            view.functionList.setAdapter(functionAdapter);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (whiteboardService != null) {
            whiteboardService.onResumeWhiteBoard();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (whiteboardService != null) {
            whiteboardService.onPauseWhiteBoard();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (whiteboardService != null) {
            whiteboardService.onDestroyWhiteBoard();
        }
        Utils.destroy(rtcDelegate);
    }

    @Override
    public void onItemClicked(int position, RtcStreamEvent streamInfo) {
        // 0. ????????????
        if (streamInfo == null) {
            return;
        }
    }

    @Nullable
    private View parsePossibleSurfaceView() {
        ViewGroup roadVG;
        if ((roadVG = view.roadRenderContainer) == null) {
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

    private RtcDelegate ofRtcDelegate() {
        if (rtcDelegate == null) {
            rtcDelegate = new RtcDelegate(this, this, getUserId(), nick
                    , roomChannel ,rtcService, livePlayerService, view.rtcRenderContainer, this);
        }

        return rtcDelegate;
    }

    @Override
    public void onRtcStart() {

    }

    @Override
    public void onRtcEnd() {
        leaveRtcProcess();
    }

    @Override
    public void onRtcGetKickedOffline() {
        isJoined = false;
        isApplyed = false;

        loadUser(false);
    }

    @Override
    public void onRtcLinkRequestRejected() {
        isApplyed = false;
    }

    @Override
    public void onRtcRemoteJoinSuccess(ConfUserEvent userEvent) {
        isJoined = true;
        loadUser(true, new Callback<List<RtcUser>>() {
            @Override
            public void onSuccess(List<RtcUser> rtcUsers) {
                if (isOwner()){
                    ofRtcDelegate().setLayoutModel(RtcLayoutModel.ONE_SUPPORT_FOUR);
                }
            }

            @Override
            public void onError(String s) {
            }
        });
    }

    @Override
    public void onRtcJoinRtcSuccess() {

    }

    @Override
    public void onRtcJoinRtcError(String msg) {
        if (isOwner()) {
            // ?????????????????????, ??????????????????
            DialogUtil.tips(context, "????????????: " + msg, this::finish);
        } else {
            // ??????????????????, ????????????????????????
        }
    }

    @Override
    public void onUpdateSelfMicStatus(boolean mute) {

    }

    @Override
    public void onUpdateSelfCameraStatus(boolean mute) {

    }

    @Override
    public void startRoadPublishSuccess() {
        ViewUtil.setGone(view.startClass);
        ViewUtil.setVisible(view.endClass);
    }

    @Override
    public void updateUser(Collection<RtcUser> users) {
        if (rtcUserManager != null && Utils.isNotEmpty(users)) {
            rtcUserManager.updateUser(users);
            refreshStudentView();
        }
    }

    @Override
    public void usersLeave(ConfUserEvent confUserEvent) {
        List<ConfUserModel> leaveUserList = confUserEvent.userList;
        if (CollectionUtil.isEmpty(leaveUserList)) {
            return;
        }

        boolean selfLeave = StreamSupport.stream(leaveUserList)
                .anyMatch(userModel -> TextUtils.equals(userModel.userId, Const.currentUserId));
        if (selfLeave) {
            isJoined = false;
            loadUser(false);
        } else {
            updateUser(RtcDelegate.convertIntoRtcUserListWithStatus(confUserEvent, RtcUserStatus.JOIN_FAILED));
        }
    }

    @Override
    public List<RtcUser> getUserList() {
        return rtcUserManager != null ? rtcUserManager.getUserList() : null;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        view.setOrientation(newConfig.orientation);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void finish() {
        if (roomChannel == null) {
            super.finish();
            return;
        }

        if (isOwner()) {
            // 1. ??????
            DialogUtil.showCustomDialog(context, "??????????????????????????????",
//                    new DialogUtil.Action("????????????", () -> leaveChannelAndFinish(false)),
                    new Pair<>("??????", () -> leaveChannelAndFinish(true)), null);
        } else {
            // 2. ??????, ?????????, ????????????
            if (isJoined) {
                leaveChannelAndFinish(false);
            } else {
                super.finish();
            }
        }
    }
    // ?????????????????????

    private void leaveChannelAndFinish(boolean destroyRtc) {
        if (isOwner()) {
            // ???????????????, ??????????????????????????????????????????
            livePusherService.stopLive(new Callbacks.Log<>(TAG, "destroy live"));
        }
        // ??????
        ofRtcDelegate().leaveRtc(destroyRtc);
        super.finish();
    }

    // ?????????????????????
    public boolean isOwner() {
        return roomChannel != null && roomChannel.isOwner();
    }

    public String getUserId() {
        return roomChannel != null ? roomChannel.getUserId() : "";
    }

    // ??????????????????
    public boolean isJoined() {
        return isJoined;
    }

    // ?????????????????????
    @Override
    public boolean onFunctionChecked(ClassFunctionsAdapter.FunctionName function) {
        boolean result = true;
        switch (function) {
            case Mute_Mic:
                // ??????
                if (isJoined) {
                    ofRtcDelegate().toggleMic();
                } else {
                    result = false;
                    showToast("??????????????????");
                }
                break;
            case Mute_Camera:
                // ?????????
                if (isJoined) {
                    ofRtcDelegate().toggleCamera();
                } else {
                    result = false;
                    showToast("??????????????????");
                }
                break;
            case Join_RTC:
                // ??????????????????
                functionAdapter.updateFunction(function, isJoined ? "??????" : isApplyed ? "??????" : "??????");
                onStudentJoinChannel();
                break;
            case Rotate_Camera:
                // ??????
                ofRtcDelegate().switchCamera();
                break;
            case Leave_Channel:
                // ????????????
                DialogUtil.confirm(context, "????????????", this::finish);
                break;
            default:
        }
        return result;
    }

    public void updateFunctionSelectionOfMic(boolean closeMic){
        if (functionAdapter != null) {
            functionAdapter.updateFunctionSelected(ClassFunctionsAdapter.FunctionName.Mute_Mic, closeMic);
        }
    }

    public void updateFunctionSelectionOfCamera(boolean closeCamera){
        if (functionAdapter != null) {
            functionAdapter.updateFunctionSelected(ClassFunctionsAdapter.FunctionName.Mute_Camera, closeCamera);
        }
    }

    private void updateConfUserData(ConfUserEvent confUserEvent, RtcUserStatus status) {
        if (confUserEvent == null) {
            return;
        }

        List<ConfUserModel> userList = confUserEvent.userList;
        if (CollectionUtil.isEmpty(userList)) {
            return;
        }

        for (ConfUserModel confUserModel : userList) {
            RtcUser rtcUser = new RtcUser();
            rtcUser.userId = confUserModel.userId;
            rtcUser.nick = confUserModel.nickname;
            rtcUser.status = status;
            rtcUserManager.updateUser(rtcUser);
        }

        refreshStudentView();
    }

    private void refreshStudentView() {
        List<RtcUser> rtcUsers = rtcUserManager.getUserList();
        view.studentView.setData(rtcUsers);
    }

    private class RoomEventHandlerImpl extends SampleRoomEventHandler {
        @Override
        public void onEnterOrLeaveRoom(RoomInOutEvent event) {
            // ????????????
            if (event.enter) {
                addSystemMessage(event.nick + "???????????????");
                RtcUser model = new RtcUser();
                model.userId = event.userId;
                model.nick = event.nick;
                model.status = RtcUserStatus.LEAVE;
                rtcUserManager.addUser(model);
            } else {
                addSystemMessage(event.nick + "???????????????");
                rtcUserManager.removeUser(event.userId);
            }
            refreshStudentView();
        }

        @Override
        public void onRoomUserKicked(KickUserEvent event) {
            if (TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                // ?????????, ??????????????????
                showToast("??????????????????????????????");
                finish();
            } else {
                // ?????????
                addSystemMessage(String.format("%s????????????????????????", event.kickUserName));

                rtcUserManager.removeUser(event.userId);
                refreshStudentView();
            }
        }
    }

    private class ChatEventHandlerImpl extends SampleChatEventHandler {
        @Override
        public void onCommentReceived(CommentEvent event) {
            view.chatView.addMessage(event.creatorNick, event.content);
        }

        @Override
        public void onCommentMutedOrCancel(MuteCommentEvent event) {
            // ?????? & ????????????
            String action = event.mute ? "??????" : "????????????";
            boolean isSelf = TextUtils.equals(roomChannel.getUserId(), event.muteUserOpenId);
            String subject = isSelf ? "???" : event.muteUserNick;
            addSystemMessage(String.format("%s????????????%s???", subject, action));
        }
    }

    private class LiveEventHandlerImpl extends SampleLiveEventHandler {
        @Override
        public void onLiveStarted(LiveCommonEvent event) {
            if (!isOwner()) {
                addSystemMessage("??????????????????");
                tryPlayLive();
            }
        }

        @Override
        public void onLiveStopped(LiveCommonEvent event) {
            if (!isOwner()) {
                addSystemMessage("??????????????????");
            }
        }
    }
}
