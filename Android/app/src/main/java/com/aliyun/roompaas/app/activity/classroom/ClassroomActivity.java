package com.aliyun.roompaas.app.activity.classroom;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.rtc.ConfUserModel;
import com.alibaba.fastjson.JSON;
import com.alivc.rtc.AliRtcEngine;
import com.aliyun.roompaas.app.BuildConfig;
import com.aliyun.roompaas.app.Const;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.manager.RtcUserManager;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.model.RtcUser;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.viewmodel.WhiteBoardVM;
import com.aliyun.roompaas.app.viewmodel.inter.IWhiteBoardOperate;
import com.aliyun.roompaas.base.callback.Callback;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ThreadUtil;
import com.aliyun.roompaas.biz.RoomEvent;
import com.aliyun.roompaas.biz.event.KickUserEvent;
import com.aliyun.roompaas.biz.event.RoomInOutEvent;
import com.aliyun.roompaas.chat.ChatEvent;
import com.aliyun.roompaas.chat.CommentParam;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.event.CommentEvent;
import com.aliyun.roompaas.chat.event.MuteCommentEvent;
import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.rtc.AliRTCManager;
import com.aliyun.roompaas.rtc.RtcEvent;
import com.aliyun.roompaas.rtc.RtcService;
import com.aliyun.roompaas.rtc.RtcUserStatus;
import com.aliyun.roompaas.rtc.event.ConfApplyJoinChannelEvent;
import com.aliyun.roompaas.rtc.event.ConfEvent;
import com.aliyun.roompaas.rtc.event.ConfInviteEvent;
import com.aliyun.roompaas.rtc.event.ConfRejectedEvent;
import com.aliyun.roompaas.rtc.event.ConfUserEvent;
import com.aliyun.roompaas.rtc.event.RtcStreamEvent;
import com.aliyun.roompaas.whiteboard.WhiteboardService;
import com.aliyun.roompaas.whiteboard.event.WhiteboardEvent;

import org.jetbrains.annotations.NotNull;
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
    private AliRTCManager aliRtcManager;

    private StudentListAdapter adapter;
    private ClassFunctionsAdapter functionAdapter;
    private RtcStreamEvent displayVideoStreamInfo;
    private boolean isJoined;
    private boolean isApplyed;
    private boolean hasShowNetwork;

    public static void open(Context context, String roomId, String roomTitle, String nick) {
        Intent intent = new Intent(context, ClassroomActivity.class);
        open(context, intent, roomId, roomTitle, nick);
    }

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

    @Override
    protected void onRoomEvent(RoomEvent event, Object obj) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, String.format("onEvent: event=%s, obj=%s ", event, obj));
        }
        switch (event) {
            case ENTER_ROOM:
            case LEAVE_ROOM:
                // 进出房间
                RoomInOutEvent inOutEvent = (RoomInOutEvent) obj;
                if (inOutEvent.enter) {
                    addSystemMessage(inOutEvent.nick + "进入了房间");
                    RtcUser model = new RtcUser();
                    model.userId = inOutEvent.userId;
                    model.nick = inOutEvent.nick;
                    model.status = RtcUserStatus.LEAVE;
                    rtcUserManager.addUser(model);
                } else {
                    addSystemMessage(inOutEvent.nick + "离开了房间");
                    rtcUserManager.removeUser(inOutEvent.userId);
                }
                refreshStudentView();
                break;
            case ROOM_USER_KICKED:
                KickUserEvent kickUserEvent = (KickUserEvent) obj;
                if (TextUtils.equals(roomChannel.getUserId(), kickUserEvent.kickUser)) {
                    // 被踢人, 直接离开页面
                    showToast("您已被管理员移除房间");
                    finish();
                } else {
                    // 其他人
                    addSystemMessage(String.format("%s被管理员移除房间", kickUserEvent.kickUserName));

                    rtcUserManager.removeUser(kickUserEvent.userId);
                    refreshStudentView();
                }
                break;
        }
    }

    @Override
    protected void onChatEvent(ChatEvent event, Object obj) {
        switch (event) {
            case COMMENT_RECEIVED:
                // 弹幕
                CommentEvent commentEvent = (CommentEvent) obj;
                view.chatView.addMessage(commentEvent.creatorNick, commentEvent.content);
                break;
            case COMMENT_MUTED:
            case COMMENT_MUTED_CANCEL:
                // 禁言 & 取消禁言
                MuteCommentEvent muteCommentEvent = (MuteCommentEvent) obj;
                String action = muteCommentEvent.mute ? "禁言" : "取消禁言";
                boolean isSelf = TextUtils.equals(roomChannel.getUserId(), muteCommentEvent.muteUserOpenId);
                String subject = isSelf ? "您" : muteCommentEvent.muteUserNick;
                addSystemMessage(String.format("%s被管理员%s了", subject, action));
                break;
        }
    }

    @Override
    protected void onLiveEvent(LiveEvent event, Object obj) {

    }

    private void onRtcEvent(RtcEvent event, Object obj) {
        switch (event) {
            case RTC_STREAM_IN:
                RtcStreamEvent inStreamEvent = (RtcStreamEvent) obj;
                addSystemMessage("Rtc流进入: " + inStreamEvent.userId);
                // 停止旁路拉流
                rtcService.stopPlayRoad();
                // 开始拉取rtc流
                playRtc(inStreamEvent);
                break;
            case RTC_STREAM_UPDATE:
                RtcStreamEvent updateStreamEvent = (RtcStreamEvent) obj;
                addSystemMessage("Rtc流更新: " + updateStreamEvent.userId);
                break;
            case RTC_STREAM_OUT:
                addSystemMessage("Rtc流退出: " + (String) obj);
                break;
            case RTC_USER_INVITED:
                ConfInviteEvent inviteEvent = (ConfInviteEvent) obj;
                // 被邀请人Id
                List<ConfUserModel> calleeList = inviteEvent.calleeList;
                if (CollectionUtil.isEmpty(calleeList)) {
                    return;
                }

                boolean needUpdateUserList = false;
                String teacherNick = inviteEvent.caller.nickname;
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
                break;
            case RTC_KICK_USER:
                List<ConfUserModel> userList = ((ConfUserEvent) obj).userList;
                if (CollectionUtil.isEmpty(userList)) {
                    return;
                }
                // 更新学生列表状态
//                updateConfUserData(confUserEvent, RtcUserStatus.LEAVE);

                // 重新加载学生列表
                loadUser(false);

                for (ConfUserModel userModel : userList) {
                    boolean isSelf = TextUtils.equals(userModel.userId, roomChannel.getUserId());
                    if (isSelf) {
                        // 被踢的是自己, 离会
                        showToast("老师已将你下麦");
                        // 连麦
                        onStudentJoinChannel();
                    } else {
                        // 被踢的是其他人, 面板提示
                        addSystemMessage(userModel.userId + "已下麦");
                    }
                }
                break;
            case RTC_LEAVE_USER:
                // 用户离开课堂
                leaveUser((ConfUserEvent) obj);
                break;
            case RTC_START:
                // 会议开始
                addSystemMessage("老师开始上课");
                break;
            case RTC_END:
                // 会议结束
                addSystemMessage("老师结束上课");
                break;
            case RTC_APPLY_JOIN_CHANNEL:
                // 申请连麦
                ConfApplyJoinChannelEvent applyEvent = (ConfApplyJoinChannelEvent) obj;
                ConfUserModel applyUser = applyEvent.applyUser;
                if (applyUser != null) {
                    addSystemMessage(applyUser.userId +
                            (applyEvent.isApply ? "申请上麦" : "上麦申请已取消"));

                    ConfUserEvent appUserEvent = new ConfUserEvent();
                    appUserEvent.confId = applyEvent.confId;
                    appUserEvent.type = applyEvent.type;
                    appUserEvent.version = applyEvent.version;
                    List<ConfUserModel> confUserModel = new ArrayList<>();
                    confUserModel.add(applyUser);
                    appUserEvent.userList = confUserModel;
                    updateConfUserData(appUserEvent,
                            applyEvent.isApply ? RtcUserStatus.APPLYING : RtcUserStatus.LEAVE);
                }
                break;
            case RTC_APPLY_REJECTED_CHANNEL:
                // 申请连麦被拒绝
                addSystemMessage("老师拒绝连麦申请");
                ConfRejectedEvent rejectedEvent = (ConfRejectedEvent) obj;
                if (TextUtils.equals(rejectedEvent.uid, Const.currentUserId)) {
                    // 自己被拒绝申请上麦时, 更改上麦按钮状态
                    functionAdapter.updateFunction(Join_RTC, "上麦");
                }
                break;
            case RTC_REMOTE_JOIN_SUCCESS:
                isJoined = true;
                ConfUserEvent joinSuccessEvent = (ConfUserEvent) obj;
                addSystemMessage("会议成员变更: " + JSON.toJSONString(joinSuccessEvent.userList));

                // 更新学生列表状态
//                updateConfUserData(joinSuccessEvent, RtcUserStatus.ACTIVE);

                // 重新加载学生列表
                loadUser(true);
                break;
            case RTC_REMOTE_JOIN_FAIL:
                ConfUserEvent joinFailEvent = (ConfUserEvent) obj;
                addSystemMessage("会议成员变更: " + JSON.toJSONString(joinFailEvent.userList));
                updateConfUserData(joinFailEvent, RtcUserStatus.JOIN_FAILED);
                break;
            case RTC_CONF_UPDATED:
                ConfEvent confEvent = (ConfEvent) obj;
                addSystemMessage("会议变更: " + JSON.toJSONString(confEvent));
                break;
            case RTC_JOIN_RTC_SUCCESS:
                // 大班课的场景
                if (roomChannel.isOwner()) {
                    // TODO: 2021/6/3 手动点击上课才触发
                    addSystemMessage("您已上麦成功");
                } else {
                    // 学生
                    addSystemMessage("上麦成功");
                    functionAdapter.updateFunction(Join_RTC, "下麦");
                    // 更改按钮文案
                    // 预览自己
                    initAdapterIfNeed();
                    RtcStreamEvent streamInfo = new RtcStreamEvent.Builder()
                            .setUserId(roomChannel.getUserId())
                            .setAliRtcVideoTrack(AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackCamera)
                            .setUserName("我")
                            .setLocalStream(true)
                            .setTeacher(false)
                            .setAliVideoCanvas(new AliRtcEngine.AliRtcVideoCanvas())
                            .build();
                    adapter.addOrUpdateData(streamInfo);
                }
                break;
            case RTC_JOIN_RTC_ERROR:
                if (roomChannel.isOwner()) {
                    // 老师端上麦失败, 直接离开页面
                    DialogUtil.tips(context, "上麦失败: " + obj, super::finish);
                } else {
                    functionAdapter.initAllBtnStatus();
                    // 学生上麦失败, 面板弹出错误信息
                    addSystemMessage("上课失败, " + obj);
                }
                break;
            case RTC_NETWORK_QUALITY_CHANGED:
                String uid = (String) obj;
                if (!hasShowNetwork) {
                    showToast(TextUtils.isEmpty(uid) ? "当前网络不佳" : "对方网络不佳");
                    hasShowNetwork = true;
                }
                break;
        }
    }

    private void onWhiteboardEvent(WhiteboardEvent event, Object obj) {

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
            view.rtcRenderContainer.addView(aliVideoCanvas.view);
            final AliRtcEngine.AliRtcVideoTrack aliRtcVideoTrack;
            if (rtcStreamEvent.aliRtcVideoTrack == AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackBoth) {
                aliRtcVideoTrack = AliRtcEngine.AliRtcVideoTrack.AliRtcVideoTrackScreen;
            } else {
                aliRtcVideoTrack = rtcStreamEvent.aliRtcVideoTrack;
            }
            aliRtcManager.setRemoteViewConfig(
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
        whiteboardService.setEventHandler(this::onWhiteboardEvent);
        rtcService.setEventHandler(this::onRtcEvent);

        aliRtcManager = rtcService.getAliRtcManager();
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

        if (roomChannel.isOwner()) {
            // 老师身份
            // 展示开始上课
            view.startClass.setVisibility(View.VISIBLE);
            // 开始预览并上麦
            previewAndJoin();
        } else {
            // 加载在线列表
            loadUser(false);
            // 学生身份, 开始拉流
            tryPlay();
            // 初始化工具栏
            initFunctionAdapterIfNeed();
        }
    }

    private void previewAndJoin() {
        // 预览
        View preview = rtcService.startRtcPreview();
        view.setRenderVisible(true);
        view.roadRenderContainer.addView(preview);

        // 上麦
        rtcService.joinRtc(nick);
    }

    private void tryPlay() {
        view.functionList.setVisibility(View.VISIBLE);
        liveService.tryPlayLive(new Callback<View>() {
            @Override
            public void onSuccess(View renderView) {
                ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                view.setRenderVisible(true);

                ViewGroup playerContainer = view.roadRenderContainer;
                ViewParent parent = renderView.getParent();
                if (parent instanceof ViewGroup && parent != playerContainer) {
                    ((ViewGroup) parent).removeView(renderView);
                }

                playerContainer.addView(renderView, layoutParams);
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
        view.chatView.addSystemMessage(message);
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
            // 当前已上麦, 点击后下麦
            rtcService.leaveRtc(false);
            isApplyed = false;
            // 下麦更新按钮状态
            functionAdapter.updateFunction(Join_RTC, "上麦");
            functionAdapter.initBtnStatus();
            displayVideoStreamInfo = null;
            adapter.removeAll();
            liveService.tryPlayLive(new Callback<View>() {
                @Override
                public void onSuccess(View renderView) {
                    // 重新拉旁路推流
                    view.setRenderVisible(true);
                    if (renderView.getParent() != null) {
                        ((ViewGroup) renderView.getParent()).removeView(renderView);
                    }
                    view.roadRenderContainer.addView(renderView);
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
                showToast(isApply ? "申请连麦失败" : "取消申请连麦失败" + errorMsg);
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
                    aliRtcManager.configRemoteCameraTrack(streamInfo.userId, false, true)
            );
        }

        // 2. 大屏处理
        // 2.1 把被点击的小屏数据赋值给大屏
        displayVideoStreamInfo = streamInfo;
        // 2.2 刷新大屏
        refreshMainStream();
    }

    private void refreshMainStream() {
        if (displayVideoStreamInfo == null) {
            return;
        }

        // 主屏切换到大流
        ThreadUtil.runOnSubThread(() ->
                aliRtcManager.configRemoteCameraTrack(displayVideoStreamInfo.userId, true, true)
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
            view.rtcRenderContainer.addView(surfaceView);
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

        if (roomChannel.isOwner()) {
            // 1. 老师
            DialogUtil.doAction(context, "请选择您要执行的操作",
                    new DialogUtil.Action("离开课堂", () -> leaveChannelAndFinish(false)),
                    new DialogUtil.Action("结束课堂", () -> leaveChannelAndFinish(true))
            );
        } else {
            // 2. 学生
            leaveChannelAndFinish(false);
        }
    }
    // 离会并结束页面

    private void leaveChannelAndFinish(boolean needDestroy) {
        rtcService.leaveRtc(needDestroy);
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

    // 工具栏点击事件
    @Override
    public boolean onFunctionChecked(ClassFunctionsAdapter.FunctionName function) {
        boolean result = true;
        switch (function) {
            case Mute_Mic:
                // 静音
                aliRtcManager.muteLocalMic(true);
                break;
            case Mute_Camera:
                // 摄像头
                aliRtcManager.muteLocalCamera(true);
                break;
            case Join_RTC:
                // 发起连麦请求
                functionAdapter.updateFunction(function, isJoined ? "下麦" : isApplyed ? "上麦" : "取消");
                onStudentJoinChannel();
                break;
            case Rotate_Camera:
                // 翻转
                aliRtcManager.switchCamera();
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
            RtcStreamEvent rtcStreamEvent = new RtcStreamEvent.Builder()
                    .setUserId(model.userId)
                    .build();
            if (adapter != null) {
                adapter.removeData(rtcStreamEvent);
            }
            addSystemMessage(model.userId + ":已离开会议");
        }
    }
}
