package com.aliyun.roompaas.app.activity.classroom;

import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alibaba.fastjson.JSON;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.manager.RtcUserManager;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.viewmodel.WhiteBoardVM;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.Check;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.base.util.ViewUtil;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.chat.exposable.CommentParam;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.rtc.exposable.RtcService;
import com.aliyun.roompaas.rtc.exposable.RtcUserStatus;
import com.aliyun.roompaas.rtc.RtcLayoutModel;
import com.aliyun.roompaas.rtc.SampleRtcEventHandler;
import com.aliyun.roompaas.rtc.exposable.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfHandleApplyEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.exposable.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.exposable.event.RtcStreamEvent;
import com.aliyun.roompaas.whiteboard.exposable.WhiteboardService;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.webrtc.sdk.SophonSurfaceView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import java8.util.stream.StreamSupport;

import static com.aliyun.roompaas.app.activity.classroom.ClassFunctionsAdapter.FunctionName.Join_RTC;

/**
 * 课堂场景房间
 *
 * @author puke
 * @version 2021/5/24
 */
public class ClassroomActivity extends BaseRoomActivity implements IWhiteBoardOperate,
        StudentListAdapter.ItemClickListener, ClassFunctionsAdapter.FunctionCheckedListener {

    private static final String TAG = ClassroomActivity.class.getSimpleName();

    private ClassroomView view;
    private RtcUserManager rtcUserManager;
    private WhiteBoardVM whiteBoardVM;

    private WhiteboardService whiteboardService;
    private RtcService rtcService;

    private StudentListAdapter adapter;
    private ClassFunctionsAdapter functionAdapter;
    private RtcStreamEvent displayVideoStreamInfo;
    private boolean isJoined;
    private boolean isApplyed;
    private boolean hasShowNetwork;

    @Override
    public String getRoomId() {
        return roomId;
    }

    @Override
    public void openWhiteBoard(Callback<View> callback) {
        if (whiteBoardVM != null) {
            whiteBoardVM.openWhiteBoard(callback);
        }
    }

    private void playRtc(RtcStreamEvent rtcStreamEvent) {
        initAdapterIfNeed();

        if (rtcStreamEvent == null) {
            return;
        }
        if (rtcStreamEvent.isTeacher) {
            displayVideoStreamInfo = rtcStreamEvent;
            AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = rtcStreamEvent.aliVideoCanvas;
            if (aliVideoCanvas.view == null) {
                SophonSurfaceView sophonSurfaceView = new SophonSurfaceView(this);
                sophonSurfaceView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
                // true 在最顶层，会遮挡一切view
                sophonSurfaceView.setZOrderOnTop(false);
                // true 如已绘制SurfaceView则在surfaceView上一层绘制。
                sophonSurfaceView.setZOrderMediaOverlay(false);
                aliVideoCanvas.view = sophonSurfaceView;
                // 设置渲染模式,一共有四种
                aliVideoCanvas.renderMode = AliRtcEngine.AliRtcRenderMode.AliRtcRenderModeFill;
            }
            // 添加LocalView

            view.setRenderVisible(false);
            ViewUtil.setVisible(view.rtcRenderContainer);
            view.rtcRenderContainer.addView(aliVideoCanvas.view);
            final AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack;
            if (rtcStreamEvent.aliRtcVideoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth) {
                aliRtcVideoTrack = AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen;
            } else {
                aliRtcVideoTrack = rtcStreamEvent.aliRtcVideoTrack;
            }
            rtcService.setRemoteViewConfig(
                    aliVideoCanvas, rtcStreamEvent.userId, aliRtcVideoTrack);
        } else {
            adapter.addOrUpdateData(rtcStreamEvent);
        }
    }

    @Override
    protected void init() {
        super.init();
        view = new ClassroomView(this);

        // 获取服务
        whiteboardService = roomChannel.getPluginService(WhiteboardService.class);
        rtcService = roomChannel.getPluginService(RtcService.class);

        // 注册事件监听器
        roomChannel.addEventHandler(new RoomEventHandlerImpl());
        chatService.addEventHandler(new ChatEventHandlerImpl());
        liveService.addEventHandler(new LiveEventHandlerImpl());
        rtcService.addEventHandler(new RtcEventHandlerImpl());

        rtcUserManager = new RtcUserManager(roomChannel);
        whiteBoardVM = new WhiteBoardVM(roomChannel);
    }

    @Override
    protected void onEnterRoomSuccess(RoomDetail roomDetail) {
        // 加载弹幕
        loadComment();

        updateTitle();

        // 加载白板
        whiteBoardVM.whiteBoardProcess();

        if (isOwner()) {
            // 老师身份
            // 展示开始上课
            view.startClass.setVisibility(View.VISIBLE);
            // 开始预览并上麦
            previewAndJoin();
        } else {
            // 加载在线列表
            loadUser(false);
            // 学生身份, 开始拉流
            tryPlayLive();
            // 初始化工具栏
            initFunctionAdapterIfNeed();
        }
    }

    private void previewAndJoin() {
        // 预览
        updateRoadRender(rtcService.startRtcPreview());

        // 上麦
        rtcService.joinRtc(nick);
    }

    private void tryPlayLive() {
        view.functionList.setVisibility(View.VISIBLE);
        livePlayerService.tryPlay(new Callback<View>() {
            @Override
            public void onSuccess(View renderView) {
                updateRoadRender(renderView);
                if (!rtcService.hasRtc()) {
                    showToast("老师暂未开课");
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    private void loadUser(boolean loadRtcUserList) {
        rtcUserManager.loadUserList(loadRtcUserList, new Callback<List<RtcUser>>() {
            @Override
            public void onSuccess(List<RtcUser> data) {
                view.studentView.setData(data);
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取用户列表失败: " + errorMsg);
            }
        });
    }

    public void updateTitle() {
        if (view != null && !TextUtils.isEmpty(roomTitle)) {
            view.updateTitle(roomTitle);
        }
    }

    // 加载历史弹幕
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
                    // 记录插入前的索引值
                    List<MessageModel> addedList = new ArrayList<>();
                    // 倒序取的
                    for (int i = list.size() - 1; i >= 0; i--) {
                        CommentModel model = list.get(i);
                        addedList.add(new MessageModel(model.creatorNick, model.content));
                    }
                    view.chatView.addMessage(addedList);
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("拉取弹幕列表失败: " + errorMsg);
            }
        });
    }

    // 点击用户, 进入用户管理逻辑
    public void onUserClick(RtcUser model) {
        // do nothing
    }

    private void addSystemMessage(String message) {
        Runnable task = () -> view.chatView.addSystemMessage(message);
        if (Check.checkMainThread()) {
            task.run();
        } else {
            ThreadUtil.runOnUiThread(task);
        }
    }

    void onSend(String inputText) {
        if (TextUtils.isEmpty(inputText)) {
            showToast("请先输入内容");
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
                showToast("发送失败: " + errorMsg);
            }
        });
    }

    // 开始上课
    void onStartClass() {
        if (!isJoined) {
            showToast("正在上麦, 请稍等...");
            return;
        }

        // 老师上麦后默认开启旁路推流
        rtcService.startRoadPublish(new Callbacks.Lambda<>((success, data, errorMsg) -> {
            if (success) {
                view.startClass.setVisibility(View.GONE);
            } else {
                showToast("推流失败: " + errorMsg);
            }
        }));
    }

    // 学生上麦
    void onStudentJoinChannel() {
        if (isJoined) {
            leaveRtcProcess();
            if (adapter != null) {
                adapter.removeAll();
            }
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
            // 当前未上麦, 点击后申请上麦
            onApplyJoinRtc(!isApplyed);
        }
    }

    private void updateRoadRender(View toAdd) {
        if (view == null || toAdd == null || view.roadRenderContainer == null) {
            return;
        }

        view.setRenderVisible(true);
        ViewUtil.removeSelfSafely(toAdd);
        view.roadRenderContainer.addView(toAdd);
    }

    private void leaveRtcProcess() {
        // 当前已上麦, 点击后下麦
        if (rtcService != null) {
            rtcService.leaveRtc(false);
        }
        isJoined = false;
        isApplyed = false;
        // 下麦更新按钮状态
        muteLocalMic = false;
        muteLocalCamera = false;
        if (functionAdapter != null) {
            functionAdapter.initBtnStatus();
            functionAdapter.updateFunction(Join_RTC, "上麦");
        }
        displayVideoStreamInfo = null;
    }

    // 处理学生申请连麦事件
    public void onHandleUserApply(RtcUser model, boolean agree) {
        if (agree) {
            // 同意, 则触发会议邀请
            onInviteUser(model);
        }
        rtcService.handleApplyJoinRtc(model.userId, agree, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                showToast(agree ? "已同意连麦" : "已拒绝连麦");
            }

            @Override
            public void onError(String errorMsg) {
                showToast("处理失败: " + errorMsg);
            }
        });
    }

    // 老师挂断
    public void onKickFromChannel(String kickedUserId) {
        rtcService.kickUserFromRtc(Collections.singletonList(kickedUserId), new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
            }

            @Override
            public void onError(String errorMsg) {
                showToast("挂断失败: " + errorMsg);
            }
        });
    }

    // 邀请连麦
    public void onInviteUser(RtcUser invitedUser) {
        ConfUserModel userModel = new ConfUserModel();
        userModel.userId = invitedUser.userId;
        userModel.nickname = invitedUser.nick;
        List<ConfUserModel> userModels = Collections.singletonList(userModel);
        rtcService.inviteJoinRtc(userModels, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                showToast("邀请已发送");
            }

            @Override
            public void onError(String errorMsg) {
                showToast("邀请失败: " + errorMsg);
            }
        });
    }

    // 学生申请连麦
    private void onApplyJoinRtc(boolean isApply) {
        rtcService.applyJoinRtc(isApply, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                showToast(isApply ? "申请连麦已发送" : "取消申请连麦已发送");
                isApplyed = isApply;
            }

            @Override
            public void onError(String errorMsg) {
                showToast((isApply ? "申请连麦失败: " : "取消申请连麦失败: ") + errorMsg);
                isApplyed = !isApply;
            }
        });
    }

    // 学生列表Adapter init
    private void initAdapterIfNeed() {
        if (adapter == null) {
            adapter = new StudentListAdapter(roomChannel, this);
            adapter.setItemClickListener(this);
            view.studentList.setAdapter(adapter);
        }
    }

    // 入会工具栏Adapter init
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
    }

    @Override
    public void onItemClicked(int position, RtcStreamEvent streamInfo) {
        // 0. 保护逻辑
        if (streamInfo == null) {
            return;
        }

        parseRtcStreamInfoIfMyselfPreviewIsInRoadRender();

        // 1. 小屏处理
        if (displayVideoStreamInfo == null) {
            // 1.1 大屏为空, 直接删除小屏
            adapter.removeData(position);
        } else {
            // 1.2 大屏不为空, 大小屏交换
            // 1.2.1 把大屏数据赋值给被点击的小屏
            adapter.updateData(position, displayVideoStreamInfo);
            // 1.2.2 小屏切换到小流
            ThreadUtil.runOnSubThread(() ->
                    rtcService.configRemoteCameraTrack(streamInfo.userId, false, true)
            );
        }

        // 2. 大屏处理
        // 2.1 把被点击的小屏数据赋值给大屏
        displayVideoStreamInfo = streamInfo;
        // 2.2 刷新大屏
        refreshMainStream();
    }

    private void parseRtcStreamInfoIfMyselfPreviewIsInRoadRender() {
        View surfaceView;
        if (displayVideoStreamInfo == null && (surfaceView = parsePossibleSurfaceView()) instanceof SophonSurfaceView) {
            RtcStreamEvent me = assembleRtcStreamEventForSelf(false);
            me.aliVideoCanvas.view = surfaceView;
            displayVideoStreamInfo = me;
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

    private RtcStreamEvent assembleRtcStreamEventForSelf(boolean certainlyNotTeacher) {
        return new RtcStreamEvent.Builder()
                .setUserId(roomChannel.getUserId())
                .setAliRtcVideoTrack(AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera)
                .setUserName("我")
                .setLocalStream(true)
                .setTeacher(!certainlyNotTeacher && isOwner())
                .setAliVideoCanvas(new AliRtcEngine.AliRtcVideoCanvas())
                .build();
    }

    private void refreshMainStream() {
        if (displayVideoStreamInfo == null) {
            return;
        }

        // 主屏切换到大流
        ThreadUtil.runOnSubThread(() ->
                rtcService.configRemoteCameraTrack(displayVideoStreamInfo.userId, true, true)
        );

        AliRtcEngine.AliRtcVideoCanvas aliVideoCanvas = displayVideoStreamInfo.aliVideoCanvas;
        if (aliVideoCanvas != null && aliVideoCanvas.view instanceof SophonSurfaceView) {
            // true 在最顶层，会遮挡一切view
            SophonSurfaceView surfaceView = (SophonSurfaceView) aliVideoCanvas.view;
            surfaceView.setZOrderOnTop(false);
            // true 如已绘制SurfaceView则在surfaceView上一层绘制。
            surfaceView.setZOrderMediaOverlay(false);
            view.rtcRenderContainer.removeAllViews();

            ViewParent parent = surfaceView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeAllViews();
            }
            ViewUtil.setVisible(view.rtcRenderContainer);
            view.rtcRenderContainer.addView(surfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
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
            // 1. 老师
            DialogUtil.showCustomDialog(context, "请选择您要执行的操作",
//                    new DialogUtil.Action("离开课堂", () -> leaveChannelAndFinish(false)),
                    new Pair<>("下课", () -> leaveChannelAndFinish(true)), null);
        } else {
            // 2. 学生, 若入会, 需要离会
            if (isJoined) {
                leaveChannelAndFinish(false);
            } else {
                super.finish();
            }
        }
    }
    // 离会并结束页面

    private void leaveChannelAndFinish(boolean destroyRtc) {
        if (isOwner()) {
            // 老师下麦时, 同时要销毁旁路推流的直播实例
            livePusherService.stopLive(new Callbacks.Log<>(TAG, "destroy live"));
        }
        // 下麦
        rtcService.leaveRtc(destroyRtc);
        super.finish();
    }

    // 判断是否是老师
    public boolean isOwner() {
        return roomChannel != null && roomChannel.isOwner();
    }

    public String getUserId() {
        return roomChannel != null ? roomChannel.getUserId() : "";
    }

    // 判断是否入会
    public boolean isJoined() {
        return isJoined;
    }

    private boolean muteLocalMic;
    private boolean muteLocalCamera;

    // 工具栏点击事件
    @Override
    public boolean onFunctionChecked(ClassFunctionsAdapter.FunctionName function) {
        boolean result = true;
        switch (function) {
            case Mute_Mic:
                // 静音
                if (isJoined) {
                    muteLocalMic = !muteLocalMic;
                    rtcService.muteLocalMic(muteLocalMic);
                    //刷新UI
                    adapter.updateLocalMic(roomChannel.getUserId(), muteLocalMic);
                } else {
                    result = false;
                    showToast("上麦后可操作");
                }
                break;
            case Mute_Camera:
                // 摄像头
                if (isJoined) {
                    muteLocalCamera = !muteLocalCamera;
                    rtcService.muteLocalCamera(muteLocalCamera);
                    //刷新UI
                    adapter.updateLocalCamera(roomChannel.getUserId(), muteLocalCamera);
                } else {
                    result = false;
                    showToast("上麦后可操作");
                }
                break;
            case Join_RTC:
                // 发起连麦请求
                functionAdapter.updateFunction(function, isJoined ? "下麦" : isApplyed ? "上麦" : "取消");
                onStudentJoinChannel();
                break;
            case Rotate_Camera:
                // 翻转
                rtcService.switchCamera();
                break;
            case Leave_Channel:
                // 退出课程
                DialogUtil.confirm(context, "确认退出", this::finish);
                break;
            default:
        }
        return result;
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

    private void leaveUser(ConfUserEvent confUserEvent) {
        List<ConfUserModel> leaveUserList = confUserEvent.userList;
        if (CollectionUtil.isEmpty(leaveUserList)) {
            return;
        }

        boolean selfLeave = StreamSupport.stream(leaveUserList)
                .anyMatch(userModel -> TextUtils.equals(userModel.userId, Const.currentUserId));
        if (selfLeave) {
            isJoined = false;
            // 自己离会, 重新加载学生列表
            loadUser(false);
        } else {
            // 别人离会, 刷新学生状态
            updateConfUserData(confUserEvent, RtcUserStatus.LEAVE);
        }

        for (ConfUserModel model : leaveUserList) {
            if (adapter != null) {
                RtcStreamEvent rtcStreamEvent = new RtcStreamEvent.Builder()
                        .setUserId(model.userId)
                        .build();
                adapter.removeData(rtcStreamEvent);
            }
            addSystemMessage(model.userId + ":已离开会议");
        }
    }

    private class RoomEventHandlerImpl extends SampleRoomEventHandler {
        @Override
        public void onEnterOrLeaveRoom(RoomInOutEvent event) {
            // 进出房间
            if (event.enter) {
                addSystemMessage(event.nick + "进入了房间");
                RtcUser model = new RtcUser();
                model.userId = event.userId;
                model.nick = event.nick;
                model.status = RtcUserStatus.LEAVE;
                rtcUserManager.addUser(model);
            } else {
                addSystemMessage(event.nick + "离开了房间");
                rtcUserManager.removeUser(event.userId);
            }
            refreshStudentView();
        }

        @Override
        public void onRoomUserKicked(KickUserEvent event) {
            if (TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                // 被踢人, 直接离开页面
                showToast("您已被管理员移除房间");
                finish();
            } else {
                // 其他人
                addSystemMessage(String.format("%s被管理员移除房间", event.kickUserName));

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
            // 禁言 & 取消禁言
            String action = event.mute ? "禁言" : "取消禁言";
            boolean isSelf = TextUtils.equals(roomChannel.getUserId(), event.muteUserOpenId);
            String subject = isSelf ? "您" : event.muteUserNick;
            addSystemMessage(String.format("%s被管理员%s了", subject, action));
        }
    }

    private class LiveEventHandlerImpl extends SampleLiveEventHandler {
        @Override
        public void onLiveStarted(LiveCommonEvent event) {
            if (!isOwner()) {
                addSystemMessage("老师开启推流");
                tryPlayLive();
            }
        }

        @Override
        public void onLiveStopped(LiveCommonEvent event) {
            if (!isOwner()) {
                addSystemMessage("老师结束推流");
            }
        }
    }

    private class RtcEventHandlerImpl extends SampleRtcEventHandler {
        @Override
        public void onRtcStreamIn(RtcStreamEvent event) {
            addSystemMessage("Rtc流进入: " + event.userId);
            // 停止旁路拉流
            rtcService.stopPlayRoad();
            // 开始拉取rtc流
            playRtc(event);
        }

        @Override
        public void onRtcStreamUpdate(RtcStreamEvent event) {
            addSystemMessage("Rtc流更新: " + event.userId);
        }

        @Override
        public void onRtcStreamOut(String event) {
            addSystemMessage("Rtc流退出: " + event);
        }

        @Override
        public void onRtcUserInvited(ConfInviteEvent event) {
            // 被邀请人Id
            List<ConfUserModel> calleeList = event.calleeList;
            if (CollectionUtil.isEmpty(calleeList)) {
                return;
            }

            boolean needUpdateUserList = false;
            String teacherNick = event.caller.nickname;
            for (ConfUserModel userModel : calleeList) {
                boolean isSelf = TextUtils.equals(userModel.userId, roomChannel.getUserId());
                if (isSelf) {
                    // 被邀请人是自己, 弹窗询问
                    String message = teacherNick + "邀请你上麦，是否同意？";
                    DialogUtil.confirm(context, message,
                            () -> rtcService.joinRtc(nick),
                            () -> rtcService.reportJoinStatus(RtcUserStatus.JOIN_FAILED, null)
                    );
                } else {
                    // 被邀请人是其他人, 面板提示
                    addSystemMessage(teacherNick + "正在邀请" + userModel.nickname + "上麦");

                    // 用户列表更改为"呼叫中"
                    RtcUser rtcUser = new RtcUser();
                    rtcUser.userId = userModel.userId;
                    rtcUser.status = RtcUserStatus.ON_JOINING;
                    rtcUserManager.updateUser(rtcUser);
                    needUpdateUserList = true;
                }
            }
            if (needUpdateUserList) {
                refreshStudentView();
            }
        }

        @Override
        public void onRtcKickUser(ConfUserEvent event) {
            List<ConfUserModel> userList = event.userList;
            if (CollectionUtil.isEmpty(userList)) {
                return;
            }

            boolean needReloadUserList = false;
            for (ConfUserModel userModel : userList) {
                boolean isSelf = TextUtils.equals(userModel.userId, roomChannel.getUserId());
                if (isSelf) {
                    needReloadUserList = true;
                    // 被踢的是自己, 离会
                    showToast("老师已将你下麦");
                    // 上下麦处理
                    onStudentJoinChannel();
                    functionAdapter.initAllBtnStatus();

                } else {
                    // 被踢的是其他人, 面板提示
                    addSystemMessage(userModel.userId + "已下麦");
                }
            }
            // 重新加载学生列表
            if (needReloadUserList) {
                loadUser(false);
            }
        }

        @Override
        public void onRtcLeaveUser(ConfUserEvent leaveUserEvent) {
            // 用户离开课堂
            leaveUser(leaveUserEvent);
        }

        @Override
        public void onRtcStart(ConfEvent confStartEvent) {
            // 老师开始上课
            showToast("老师开始上课");
            addSystemMessage("老师开始上课");
        }

        @Override
        public void onRtcEnd(ConfEvent confEndEvent) {
            // 会议结束
            showToast("老师结束上课");
            addSystemMessage("老师结束上课");
            if (rtcService != null) {
                rtcService.stopPreview();
                rtcService.stopPlayRoad();
                if (adapter != null) {
                    adapter.removeAll();
                }
                leaveRtcProcess();
                view.rtcRenderContainer.removeAllViews();
                view.roadRenderContainer.removeAllViews();
            }
        }

        @Override
        public void onRtcApplyJoinChannel(ConfApplyJoinChannelEvent event) {
            // 申请连麦
            ConfUserModel applyUser = event.applyUser;
            if (applyUser != null) {
                addSystemMessage(applyUser.userId +
                        (event.isApply ? "申请上麦" : "上麦申请已取消"));

                ConfUserEvent appUserEvent = new ConfUserEvent();
                appUserEvent.confId = event.confId;
                appUserEvent.type = event.type;
                appUserEvent.version = event.version;
                List<ConfUserModel> confUserModel = new ArrayList<>();
                confUserModel.add(applyUser);
                appUserEvent.userList = confUserModel;
                updateConfUserData(appUserEvent,
                        event.isApply ? RtcUserStatus.APPLYING : RtcUserStatus.LEAVE);
            }
        }

        @Override
        public void onRtcHandleApplyChannel(ConfHandleApplyEvent event) {
            // 申请连麦被拒绝
            String message = "老师拒绝连麦申请";
            addSystemMessage(message);
            if (event.approve) {
                // 老师统一申请, "申请中" 改为 "呼叫中"
                rtcUserManager.updateUser(new RtcUser.Builder()
                        .userId(event.uid)
                        .status(RtcUserStatus.ON_JOINING)
                        .build()
                );
                refreshStudentView();
                return;
            }

            if (TextUtils.equals(event.uid, Const.currentUserId)) {
                // 自己被拒绝, 申请上麦时弹窗提示, 并更改上麦按钮状态
                DialogUtil.confirm(ClassroomActivity.this, message, null);
                isApplyed = false;
                functionAdapter.initAllBtnStatus();
                functionAdapter.updateFunction(Join_RTC, "上麦");
            } else {
                // 别人被拒绝, 更改用户列表状态
                if (isOwner()) {
                    // 老师收到被拒绝的事件
                    showToast(event.uid + "拒绝了您的邀请");
                }
                rtcUserManager.updateUser(new RtcUser.Builder()
                        .userId(event.uid)
                        .status(RtcUserStatus.LEAVE)
                        .build()
                );
                refreshStudentView();
            }
        }

        @Override
        public void onRtcRemoteJoinSuccess(ConfUserEvent event) {
            isJoined = true;
            addSystemMessage("会议成员变更: " + JSON.toJSONString(event.userList));

            // 重新加载学生列表
            loadUser(true);
        }

        @Override
        public void onRtcRemoteJoinFail(ConfUserEvent event) {
            addSystemMessage("会议成员变更: " + JSON.toJSONString(event.userList));
            updateConfUserData(event, RtcUserStatus.JOIN_FAILED);
        }

        @Override
        public void onRtcConfUpdated(ConfEvent event) {
            addSystemMessage("会议变更: " + JSON.toJSONString(event));
        }

        @Override
        public void onRtcJoinRtcSuccess(View view) {
            // 大班课的场景
            if (isOwner()) {
                // TODO: 2021/6/3 手动点击上课才触发
                addSystemMessage("您已上麦成功");
            } else {
                // 学生
                addSystemMessage("上麦成功");
                functionAdapter.updateFunction(Join_RTC, "下麦");
                // 更改按钮文案
                // 预览自己
                initAdapterIfNeed();
                adapter.addOrUpdateData(assembleRtcStreamEventForSelf(true));
            }
        }

        @Override
        public void onRtcJoinRtcError(String event) {
            if (isOwner()) {
                // 老师端上麦失败, 直接离开页面
                DialogUtil.tips(context, "上麦失败: " + event, ClassroomActivity.super::finish);
            } else {
                functionAdapter.initAllBtnStatus();
                // 学生上麦失败, 面板弹出错误信息
                addSystemMessage("上课失败, " + event);
            }
        }

        @Override
        public void onRtcNetworkQualityChanged(String uid) {
            if (!hasShowNetwork) {
                showToast(TextUtils.isEmpty(uid) ? "当前网络不佳" : "对方网络不佳");
                hasShowNetwork = true;
            }
        }

        @Override
        public void onRtcUserAudioMuted(String uid) {
            // 静音
            if (adapter != null) {
                adapter.updateLocalMic(uid, true);
            }
        }

        @Override
        public void onRtcUserAudioEnable(String uid) {
            if (adapter != null) {
                adapter.updateLocalMic(uid, false);
            }
        }

        @Override
        public void onRtcUserVideoMuted(String uid) {
            if (adapter != null) {
                adapter.updateLocalCamera(uid, true);
            }
        }

        @Override
        public void onRtcUserVideoEnable(String uid) {
            if (adapter != null) {
                adapter.updateLocalCamera(uid, false);
            }
        }
    }

    // 设置布局信息
    public void setLayoutModel(RtcLayoutModel model) {
        List userIds = new ArrayList();
        switch (model) {
            case ONE_GRID:
                userIds.add(roomChannel.getRoomDetail().roomInfo.getOwnerId());
                break;
            case ONE_SUPPORT_FOUR:
                getRtcUsers(userIds);
                userIds.add(roomChannel.getRoomDetail().roomInfo.getOwnerId());
                break;
            case NINE_GRID:
                getRtcUsers(userIds);
                break;

        }
        rtcService.setLayout(userIds, model, new Callback<Void>() {
            @Override
            public void onSuccess(Void data) {
                addSystemMessage("设置布局成功");
            }

            @Override
            public void onError(String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    private void getRtcUsers(List userIds) {
        List<RtcUser> rtcUsers = view.studentView.getData();
        if (rtcUsers != null) {
            for (RtcUser rtcUser : rtcUsers) {
                if (rtcUser.status == RtcUserStatus.ACTIVE) {
                    userIds.add(rtcUser.userId);
                }
            }
        }
    }
}
