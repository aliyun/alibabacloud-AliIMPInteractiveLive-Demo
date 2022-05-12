package com.aliyun.roompaas.app.activity.business;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.activity.business.view.LiveAudienceView;
import com.aliyun.roompaas.app.activity.business.view.LiveNoticeView;
import com.aliyun.roompaas.app.helper.ActivityFloatHelper;
import com.aliyun.roompaas.app.helper.KeyboardHelper;
import com.aliyun.roompaas.app.helper.RecyclerViewHelper;
import com.aliyun.roompaas.app.model.BusinessUserModel;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.simple.SimpleLottieListener;
import com.aliyun.roompaas.app.util.AnimUtil;
import com.aliyun.roompaas.app.util.AppUtil;
import com.aliyun.roompaas.app.util.ClipboardUtil;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.view.LimitSizeRecyclerView;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.beauty_base.BeautyStrategy;
import com.aliyun.roompaas.beauty_base.IBeautyOptUpdate;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.exposable.model.UserParam;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.CommentParam;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.LikeEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;
import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.AliLiveBeautyOptions;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.aliyun.roompaas.uibase.util.ViewUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * 电商场景房间
 *
 * @author puke
 * @version 2021/5/11
 */
public class BusinessActivity extends BaseRoomActivity {

    private static final String TAG = BusinessActivity.class.getSimpleName();

    private FrameLayout renderContainer;
    private View curtain;
    private TextView title;
    // TODO: 2021/7/2 观众列表页待实现 (写在 LiveAudienceView 里)
    private LiveAudienceView audienceView;
    private LiveNoticeView noticeView;
    private View startLive;
    private View stopLive;
    private View beauty;
    private LimitSizeRecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EditText commentInput;
    private TextView onlineCount;
    private TextView likeCount;
    private View bottomLayout;
    private ActivityFloatHelper moreViewFloat;

    private View likeIcon;
    private LottieAnimationView likeLottieView;

    private boolean isOwner;
    private boolean isPushing = false;
    private boolean isPlaying = false;
    private KeyboardHelper keyboardHelper;
    private boolean isMute = false;
    private boolean isMutePlay = false;
    private boolean isMirror = false;
    private RecyclerViewHelper<MessageModel> recyclerViewHelper;

    private Dialog dialog;

    //private static final boolean DEBUG_SCALE_MODE = BuildConfig.DEBUG;
    private static final boolean DEBUG_SCALE_MODE = false;

    @Override
    protected void init() {
        super.init();
        setContentView(R.layout.activity_business);
        initView();
        initKeyboard();

        // 注册事件监听器
        roomChannel.addEventHandler(new RoomEventHandlerImpl());
        chatService.addEventHandler(new ChatEventHandlerImpl());
        liveService.addEventHandler(new LiveEventHandlerImpl());
    }

    private void initView() {
        renderContainer = findViewById(R.id.room_render_container);
        curtain = findViewById(R.id.curtain);
        title = findViewById(R.id.room_title);
        audienceView = findViewById(R.id.business_view_audience);
        noticeView = findViewById(R.id.business_view_notice);
        commentInput = findViewById(R.id.room_comment_input);
        onlineCount = findViewById(R.id.room_online_count);
        likeCount = findViewById(R.id.room_like_count);
        startLive = findViewById(R.id.room_start_live);
        stopLive = findViewById(R.id.room_stop_live);
        beauty = findViewById(R.id.room_beauty);
        beauty.setSelected(true);
        recyclerView = findViewById(R.id.room_message_panel);
        int interactiveContainerMaxHeight = AppUtil.sp(250);
        recyclerView.setMaxHeight(interactiveContainerMaxHeight);
        bottomLayout = findViewById(R.id.room_bottom_layout);
        likeIcon = findViewById(R.id.likeIcon);
        likeLottieView = findViewById(R.id.likeLottieView);
        moreViewFloat = new ActivityFloatHelper(this, R.layout.view_float_live_more);
        audienceView.setCallback(this::handleUserManageLogic);

        commentInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                onCommentSubmit();
                return true;
            }
            return false;
        });

        title.setOnClickListener(v -> {
            if (!isOwner) {
                return;
            }
            String defaultValue = title.getText().toString();
            DialogUtil.input(context, "更改房间标题", defaultValue,
                    value -> roomChannel.updateTitle(value, new Callbacks.Lambda<>((success, data, errorMsg) -> {
                        if (!success) {
                            showToast("修改标题失败: " + errorMsg);
                        }
                    }))
            );
        });

        noticeView.setOnLongClickListener(v -> {
            if (!isOwner) {
                return true;
            }
            String currentNotice = noticeView.getNotice();
            DialogUtil.input(context, "更改房间公告", currentNotice,
                    value -> roomChannel.updateNotice(value, new Callbacks.Lambda<>((success, data, errorMsg) -> {
                        if (!success) {
                            showToast("修改公告失败: " + errorMsg);
                        }
                    }))
            );
            return true;
        });

        findViewById(R.id.room_gesture_layer).setOnClickListener(v -> {
            keyboardHelper.shrinkByEditText(commentInput);
            noticeView.setExpand(false);
        });

        // 添加快速测试弹幕功能
        findViewById(R.id.room_share).setOnLongClickListener(v -> {
            String message = UUID.randomUUID().toString().replace("-", "");
            commentInput.setText(message);
            onCommentSubmit();
            return true;
        });

        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        recyclerViewHelper = RecyclerViewHelper.of(recyclerView, R.layout.item_message,
                (holder, model, position, itemCount) -> {
                    TextView type = holder.getView(R.id.item_type);
                    TextView content = holder.getView(R.id.item_content);

                    type.setText(model.type + "：");
                    content.setText(model.content);

                    int color = model.color;
                    type.setTextColor(color);

                    content.setOnLongClickListener(v -> {
                        String text = model.content;
                        ClipboardUtil.copyText(text);
                        showToast("已复制: " + text);
                        return true;
                    });
                }
        );
    }

    /**
     * 处理用户管理逻辑
     *
     * @param model 目标用户
     */
    private void handleUserManageLogic(BusinessUserModel model) {
        if (roomChannel == null) {
            return;
        }

        if (!roomChannel.isOwner()) {
            showToast("您当前无操作权限哦");
            return;
        }

        if (roomChannel.isOwner(model.id)) {
            showToast("不能对主播进行操作哦");
            return;
        }

        DialogUtil.doAction(this, "用户管理",
                new DialogUtil.Action("禁言", () -> {
                    int muteSeconds = 5 * 60;
                    chatService.banComment(model.id, muteSeconds,
                            new Callbacks.Lambda<>((success, data, errorMsg) -> {
                                if (success) {
                                    showToast(String.format("已对%s禁言", model.nick));
                                } else {
                                    showToast("禁言失败: " + errorMsg);
                                }
                            })
                    );
                }),
                new DialogUtil.Action("取消禁言", () -> chatService.cancelBanComment(model.id,
                        new Callbacks.Lambda<>((success, data, errorMsg) -> {
                            if (success) {
                                showToast(String.format("已对%s取消禁言", model.nick));
                            } else {
                                showToast("取消禁言失败: " + errorMsg);
                            }
                        })
                )),
                new DialogUtil.Action("移出房间", () -> roomChannel.kickUser(model.id,
                        new Callbacks.Lambda<>((success, data, errorMsg) -> {
                            if (success) {
                                showToast(String.format("已将%s移除房间", model.nick));
                            } else {
                                showToast("移除失败: " + errorMsg);
                            }
                        })
                ))
        );
    }

    private void initKeyboard() {
        keyboardHelper = new KeyboardHelper(this);
        keyboardHelper.setOnSoftKeyBoardChangeListener(new KeyboardHelper.OnSoftKeyBoardChangeListener() {
            @Override
            public void keyBoardShow(int height) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                layoutParams.bottomMargin = 12;
                bottomLayout.setLayoutParams(layoutParams);
            }

            @Override
            public void keyBoardHide(int height) {
                ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) bottomLayout.getLayoutParams();
                layoutParams.bottomMargin = getResources().getDimensionPixelOffset(R.dimen.room_bottom_layout_margin_bottom);
                bottomLayout.setLayoutParams(layoutParams);
            }
        });
    }

    @Override
    protected void onEnterRoomSuccess(RoomDetail roomDetail) {
        isOwner = roomChannel.isOwner();

        // 填充房间基本信息
        fillRoomInfo(roomDetail);

        // 加载在线列表
        loadUser();

        // 加载弹幕列表
        loadComment();

        // 加载互动信息
        loadChatDetail();

        if (isOwner) {
            // 主播, 开始预览
            startPreview();
        } else {
            // 观众, 开始观看
            tryPlayLive();
        }
    }

    private void fillRoomInfo(RoomDetail roomDetail) {
        if (roomDetail != null) {
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                title.setText(roomInfo.title);
                noticeView.setNotice(roomInfo.notice);
                setOnlineCount(roomInfo.onlineCount);
                // TODO: 2021/5/18 待替换
                setLikeCount(0);
            }
        }
    }

    private void loadUser() {
        // Demo只拉取100条, 业务按需改造
        UserParam userParam = new UserParam();
        userParam.pageNum = 1;
        userParam.pageSize = 100;
        roomChannel.listUser(userParam, new Callback<PageModel<RoomUserModel>>() {
            @Override
            public void onSuccess(PageModel<RoomUserModel> pageModel) {
                final List<BusinessUserModel> users = new ArrayList<>();
                if (CollectionUtil.isNotEmpty(pageModel.list)) {
                    for (RoomUserModel roomUserModel : pageModel.list) {
                        BusinessUserModel userModel = new BusinessUserModel();
                        userModel.id = roomUserModel.openId;
                        userModel.nick = roomUserModel.nick;
                        users.add(userModel);
                    }
                    audienceView.setData(users);
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取在线列表失败: " + errorMsg);
            }
        });
    }

    private void loadComment() {
        CommentParam commentParam = new CommentParam();
        commentParam.pageNum = 1;
        commentParam.pageSize = 100;
        commentParam.sortType = CommentSortType.TIME_DESC;
        chatService.listComment(commentParam, new Callback<PageModel<CommentModel>>() {
            @Override
            public void onSuccess(PageModel<CommentModel> pageModel) {
                if (pageModel != null) {
                    List<CommentModel> list = pageModel.list;
                    if (CollectionUtil.isNotEmpty(list)) {
                        // 记录插入前的索引值
                        List<MessageModel> addedList = new ArrayList<>();
                        // 倒序取的
                        for (int i = list.size() - 1; i >= 0; i--) {
                            CommentModel model = list.get(i);
                            addedList.add(new MessageModel(model.creatorNick, model.content));
                        }
                        addMessage(addedList);
                    }
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取弹幕列表失败: " + errorMsg);
            }
        });
    }

    private void loadChatDetail() {
        chatService.getChatDetail(new Callback<GetTopicInfoRsp>() {
            @Override
            public void onSuccess(GetTopicInfoRsp rsp) {
                setLikeCount(rsp.likeCount);
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取互动信息失败: " + errorMsg);
            }
        });
    }

    private void startPreview() {
        AnimUtil.animOut(curtain);
        livePusherService.setPreviewMode(parseScaleMode());
        livePusherService.startPreview(new Callback<View>() {
            @Override
            public void onSuccess(View view) {
                if (view.getParent() != renderContainer) {
                    ViewUtil.removeSelfSafely(view);
                    renderContainer.addView(view);
                }
                startLive.setVisibility(View.VISIBLE);
                beauty.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    private void tryPlayLive() {
        livePlayerService.setViewContentMode(parseScaleMode());
        livePlayerService.tryPlay(new Callback<View>() {
            @Override
            public void onSuccess(View view) {
                if (view.getParent() != renderContainer) {
                    renderContainer.addView(view);
                }
                if (!liveService.hasLive()) {
                    showToast("当前暂无直播");
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast(errorMsg);
            }
        });
    }

    @CanvasScale.Mode
    private int parseScaleMode() {
        if (!DEBUG_SCALE_MODE) {
            return CanvasScale.Mode.ASPECT_FILL;
        }
        long rand = System.currentTimeMillis() % 3;
        return rand == 0 ? CanvasScale.Mode.ASPECT_FIT : rand == 1 ? CanvasScale.Mode.ASPECT_FILL : CanvasScale.Mode.SCALE_FILL;
    }

    private void liveStopped(){
        AnimUtil.animIn(curtain);
    }

    public void onStartLive(View view) {
        livePusherService.startLive(new Callback<View>() {
            @Override
            public void onSuccess(View data) {
                startLive.setVisibility(View.GONE);
                stopLive.setVisibility(View.VISIBLE);
                addMessage("主播", "直播已开始");
                isPushing = true;
            }

            @Override
            public void onError(String errorMsg) {
                showToast("开始直播失败:  " + errorMsg);
            }
        });
    }

    public void onStopLive(View view) {
        readyStopLive();
    }

    private void readyStopLive(){
        DialogUtil.showCustomDialog(this, "还有观众正在路上，确定要结束直播吗？", this::finish, null);
    }

    private void onCommentSubmit() {
        String input = commentInput.getText().toString().trim();
        if (TextUtils.isEmpty(input)) {
            showToast("请输入评论内容");
            return;
        }

        chatService.sendComment(input, new Callback<String>() {
            @Override
            public void onSuccess(String data) {
                commentInput.setText(null);
                keyboardHelper.shrinkByEditText(commentInput);
            }

            @Override
            public void onError(String errorMsg) {
                showToast("发送失败: " + errorMsg);
            }
        });
    }

    public void onShare(View view) {
        showToast("房间Id为: " + roomId);
    }

    public void onLike(View view) {
        chatService.sendLike();
        initLottieIfNecessary();
        ViewUtil.setVisible(likeLottieView);
        ViewUtil.applyAlpha(1, likeLottieView);
        likeLottieView.addAnimatorListener(new SimpleLottieListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                AnimUtil.animOut(likeIcon);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                ViewUtil.setVisible(likeIcon);
                AnimUtil.animOut(likeLottieView);
                AnimUtil.animIn(likeIcon);
                ViewUtil.setInvisible(likeLottieView);
            }
        });
        likeLottieView.playAnimation();
    }

    private void initLottieIfNecessary() {
        if (likeLottieView.getAnimation() == null) {
            likeLottieView.setAnimation("like.json");
        }
    }

    @SuppressLint("SetTextI18n")
    public void onBeauty(View view) {
        ofDialog().show();
    }

    private Dialog ofDialog(){
        if (dialog == null) {
            dialog = com.aliyun.roompaas.uibase.util.DialogUtil.createDialogOfBottom(context, FrameLayout.LayoutParams.WRAP_CONTENT,
                    R.layout.ilr_view_float_live_queen_beauty, true);
            BeautyStrategy.INSTANCE.setUp(dialog.findViewById(R.id.beautyContainer), new IBeautyOptUpdate() {
                @Override
                public void onUpdateBeautyOpt(AliLiveBeautyOptions aliLiveBeautyOptions) {
                    livePusherService.updateBeautyOptions(aliLiveBeautyOptions);
                }
            });
        }

        return dialog;
    }

    private void setOnlineCount(int count) {
        String value = formatNumber(count);
        onlineCount.setText(String.format("%s观看", value));
    }

    private void setLikeCount(int count) {
        String value = formatNumber(count);
        likeCount.setText(String.format("%s点赞", value));
    }

    private String formatNumber(int number) {
        if (number > 10000) {
            return String.format(Locale.getDefault(), "%.1f", number / 10000f);
        } else {
            return String.valueOf(number);
        }
    }

    private void addSystemMessage(String content) {
        addMessage("系统", content);
    }

    private void addMessage(String type, String content) {
        addMessage(Collections.singletonList(new MessageModel(type, content)));
    }

    private void addMessage(final List<MessageModel> addedList) {
        recyclerViewHelper.addData(addedList);

        // 已触底时, 随消息联动
        layoutManager.scrollToPositionWithOffset(
                recyclerViewHelper.getItemCount() - 1, Integer.MIN_VALUE);
        recyclerView.postDelayed(() -> recyclerView.invalidate(), 100);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isOwner && isPushing) {
            if (livePusherService != null) {
                livePusherService.pauseLive();
            }
            isPushing = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOwner && !isPushing) {
            if (livePusherService != null) {
                livePusherService.resumeLive();
            }
            isPushing = true;
        }
    }

    @Override
    public void onBackPressed() {
        boolean isOwner = roomChannel != null && roomChannel.isOwner();
        boolean shouldShowConfirm = isOwner & ((liveService != null && liveService.hasLive()) || isPushing);
        if (shouldShowConfirm) {
            readyStopLive();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void finish() {
        super.finish();
        // 当前有主播正在直播时, 先停止直播
        if (roomChannel != null) {
            if (liveService.hasLive() && roomChannel.isOwner()) {
                livePusherService.stopLive(new Callbacks.Log<>(TAG, "stop live"));
            }
            if (renderContainer != null) {
                renderContainer.removeAllViews();
            }
        }
    }

    public void onMore(View view) {
        moreViewFloat.setTopOffset(AppUtil.getScreenHeight() * 4 / 5);
        moreViewFloat.show();
        setMoreToolbarListener();
    }

    public void onMuteLive(View view) {
        isMute = !isMute;
        livePusherService.setMutePush(isMute);
        ((TextView)view.findViewById(R.id.live_tool_mute_txt)).setText(isMute ? "取消静音" : "静音");
        view.findViewById(R.id.live_tool_mute_select).setVisibility(isMute ? View.VISIBLE : View.GONE);
    }

    public void onMutePlay(View view) {
        isMutePlay = !isMutePlay;
        livePlayerService.setMutePlay(isMutePlay);
        ((TextView)view.findViewById(R.id.live_tool_mute_txt)).setText(isMute ? "取消静音" : "静音");
        view.findViewById(R.id.live_tool_mute_select).setVisibility(isMutePlay ? View.VISIBLE : View.GONE);
    }

    public void onPauseLive(View view) {
        if (isPushing) {
            livePusherService.pauseLive();
        } else {
            livePusherService.resumeLive();
        }
        ((TextView)view.findViewById(R.id.live_tool_pause_txt)).setText(isPushing ? "结束暂停" : "暂停");
        view.findViewById(R.id.live_tool_pause_select).setVisibility(isPushing ? View.VISIBLE : View.GONE);
        isPushing = !isPushing;
    }

    public void onPausePlay(View view) {
        if (isPlaying) {
            livePlayerService.pausePlay();
        } else {
            livePlayerService.resumePlay();
        }
        ((TextView)view.findViewById(R.id.live_tool_pause_txt)).setText(isPlaying ? "结束暂停" : "暂停");
        view.findViewById(R.id.live_tool_pause_select).setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        isPlaying = !isPlaying;
    }

    public void onMirrorLive(View view) {
        isMirror = !isMirror;
        livePusherService.setPreviewMirror(isMirror);
        livePusherService.setPushMirror(isMirror);
        ((TextView)view.findViewById(R.id.live_tool_mirror_txt)).setText(isMirror ? "镜像开" : "开启镜像");
        view.findViewById(R.id.live_tool_mirror_select).setVisibility(isMirror ? View.VISIBLE : View.GONE);
    }

    public void onSwitch(View view) {
        livePusherService.switchCamera();
    }

    private void setMoreToolbarListener() {
        moreViewFloat.findViewById(R.id.live_tool_mute).setOnClickListener(view -> {
            if (isOwner) {
                onMuteLive(view);
            } else {
                onMutePlay(view);
            }
        });
        moreViewFloat.findViewById(R.id.live_tool_pause).setOnClickListener(view -> {
            if (isOwner) {
                onPauseLive(view);
            } else {
                onPausePlay(view);
            }
        });
        moreViewFloat.findViewById(R.id.live_tool_switch).setOnClickListener(this::onSwitch);
        moreViewFloat.findViewById(R.id.live_tool_mirror).setOnClickListener(this::onMirrorLive);

        if (!isOwner) {
            moreViewFloat.findViewById(R.id.live_tool_switch).setVisibility(View.GONE);
            moreViewFloat.findViewById(R.id.live_tool_mirror).setVisibility(View.GONE);
        }
    }

    private AliLiveBeautyOptions beautyOptions = new AliLiveBeautyOptions.Builder().build();;
    private BeautyOptions currentSelected = BeautyOptions.beautyCheekPink;

    private enum BeautyOptions {
        beautyCheekPink,
        beautyBrightness,
        beautyBuffing,
        beautyWhite,
        beautyRuddy,
        beautySlimFace,
        beautyShortenFace,
        beautyBigEye
    }

    private class RoomEventHandlerImpl extends SampleRoomEventHandler {
        @Override
        public void onEnterOrLeaveRoom(RoomInOutEvent event) {
            addSystemMessage(String.format(
                    "%s%s了房间", event.nick, event.enter ? "进入" : "离开"));
            setOnlineCount(event.onlineCount);
            if (event.enter) {
                BusinessUserModel model = new BusinessUserModel();
                model.id = event.userId;
                model.nick = event.nick;
                audienceView.addData(model);
            } else {
                audienceView.removeData(event.userId);
            }
        }

        @Override
        public void onRoomNoticeChanged(String notice) {
            noticeView.setNotice(notice);
        }

        @Override
        public void onRoomTitleChanged(String title) {
            BusinessActivity.this.title.setText(title);
        }

        @Override
        public void onRoomUserKicked(KickUserEvent event) {
            if (TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                // 被踢人, 直接离开页面
                showToast("您已被管理员移除房间");
                finish();
            } else {
                // 其他人, 移除列表, 面板提示
                audienceView.removeData(event.kickUser);
                addSystemMessage(String.format("%s被管理员移除房间", event.kickUserName));
            }
        }
    }

    private class ChatEventHandlerImpl extends SampleChatEventHandler {
        @Override
        public void onLikeReceived(LikeEvent event) {
            setLikeCount(event.likeCount);
        }

        @Override
        public void onCommentReceived(CommentEvent event) {
            addMessage(event.creatorNick, event.content);
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
        public void onLiveCreated(LiveCommonEvent event) {
        }

        @Override
        public void onLiveStarted(LiveCommonEvent event) {
            if (!isOwner) {
                tryPlayLive();
                addSystemMessage("直播已开始");
            }
        }

        @Override
        public void onLiveStopped(LiveCommonEvent event) {
            if (!isOwner) {
                liveStopped();
                addSystemMessage("直播已结束");
            }
        }

        @Override
        public void onPusherEvent(LiveEvent event) {
            super.onPusherEvent(event);
        }

        @Override
        public void onRenderStart() {
            AnimUtil.animOut(curtain);
        }

        @Override
        public void onPlayerError() {
            AnimUtil.animIn(curtain);
        }
    }
}
