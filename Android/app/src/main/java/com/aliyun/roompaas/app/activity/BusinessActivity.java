package com.aliyun.roompaas.app.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.dingpaas.room.RoomInfo;
import com.alibaba.dingpaas.room.RoomUserModel;
import com.aliyun.roompaas.app.R;
import com.aliyun.roompaas.app.activity.base.BaseRoomActivity;
import com.aliyun.roompaas.app.helper.KeyboardHelper;
import com.aliyun.roompaas.app.helper.RecyclerViewHelper;
import com.aliyun.roompaas.app.model.BusinessUserModel;
import com.aliyun.roompaas.app.model.MessageModel;
import com.aliyun.roompaas.app.util.AppUtil;
import com.aliyun.roompaas.app.util.ClipboardUtil;
import com.aliyun.roompaas.app.util.DialogUtil;
import com.aliyun.roompaas.app.view.LimitSizeRecyclerView;
import com.aliyun.roompaas.app.view.UserListView;
import com.aliyun.roompaas.base.callback.Callback;
import com.aliyun.roompaas.base.callback.Callbacks;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.base.util.ViewUtil;
import com.aliyun.roompaas.biz.RoomEvent;
import com.aliyun.roompaas.biz.event.KickUserEvent;
import com.aliyun.roompaas.biz.event.RoomInOutEvent;
import com.aliyun.roompaas.biz.model.UserParam;
import com.aliyun.roompaas.chat.ChatEvent;
import com.aliyun.roompaas.chat.CommentParam;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.event.CommentEvent;
import com.aliyun.roompaas.chat.event.LikeEvent;
import com.aliyun.roompaas.chat.event.MuteCommentEvent;
import com.aliyun.roompaas.live.LiveEvent;

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

    public static void open(Context context, String roomId, String roomTitle, String nick) {
        Intent intent = new Intent(context, BusinessActivity.class);
        open(context, intent, roomId,roomTitle, nick);
    }

    private FrameLayout renderContainer;
    private TextView title;
    private TextView noticeLabel;
    private UserListView userListView;
    private View startLive;
    private Button pauseLive;
    private Button mute;
    private View stopLive;
    private View switchCamera;
    private View mirrorCamera;
    private View beauty;
    private LimitSizeRecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private EditText commentInput;
    private TextView onlineCount;
    private TextView likeCount;
    private View moreLayout;
    private View bottomLayout;

    private boolean isOwner;
    private boolean isPushing = false;
    private KeyboardHelper keyboardHelper;
    private boolean isMute = false;
    private boolean isMirror = false;
    private RecyclerViewHelper<MessageModel> recyclerViewHelper;

    @Override
    protected void onRoomEvent(RoomEvent event, Object obj) {
        switch (event) {
            case ENTER_ROOM:
            case LEAVE_ROOM:
                // 进出房间
                RoomInOutEvent inOutEvent = (RoomInOutEvent) obj;
                addSystemMessage(String.format(
                        "%s%s了房间", inOutEvent.nick, inOutEvent.enter ? "进入" : "离开"));
                setOnlineCount(inOutEvent.onlineCount);
                if (inOutEvent.enter) {
                    BusinessUserModel model = new BusinessUserModel();
                    model.id = inOutEvent.userId;
                    model.nick = inOutEvent.nick;
                    userListView.addData(model);
                } else {
                    userListView.removeData(inOutEvent.userId);
                }
                break;
            case ROOM_TITLE_CHANGED:
                title.setText((String) obj);
                break;
            case ROOM_NOTICE_CHANGED:
                noticeLabel.setText((String) obj);
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
                addMessage(commentEvent.creatorNick, commentEvent.content);
                break;
            case LIKE_RECEIVED:
                // 点赞
                LikeEvent likeEvent = (LikeEvent) obj;
                setLikeCount(likeEvent.likeCount);
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
        switch (event) {
            case LIVE_STARTED:
                addSystemMessage("直播已开始");
                break;
            case LIVE_STOPPED:
                addSystemMessage("直播已结束");
                break;
        }
    }

    @Override
    protected void init() {
        super.init();
        setContentView(R.layout.activity_business);
        initView();
        initKeyboard();
    }

    private void initView() {
        renderContainer = findViewById(R.id.room_render_container);
        title = findViewById(R.id.room_title);
        noticeLabel = findViewById(R.id.room_notice_label);
        noticeLabel.setSelected(true);
        userListView = findViewById(R.id.room_user_list_view);
        userListView.setMaxHeight(AppUtil.getScreenHeight() / 2);
        commentInput = findViewById(R.id.room_comment_input);
        onlineCount = findViewById(R.id.room_online_count);
        likeCount = findViewById(R.id.room_like_count);
        startLive = findViewById(R.id.room_start_live);
        stopLive = findViewById(R.id.room_stop_live);
        pauseLive = findViewById(R.id.room_pause_live);
        mute = findViewById(R.id.room_mute_live);
        switchCamera = findViewById(R.id.room_switch_camera);
        mirrorCamera = findViewById(R.id.room_mirror_live);
        beauty = findViewById(R.id.room_beauty);
        beauty.setSelected(true);
        recyclerView = findViewById(R.id.room_message_panel);
        int interactiveContainerMaxHeight = AppUtil.sp(250);
        recyclerView.setMaxHeight(interactiveContainerMaxHeight);
        moreLayout = findViewById(R.id.room_more_layout);
        bottomLayout = findViewById(R.id.room_bottom_layout);

        userListView.setCallback(this::handleUserManageLogic);

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

        noticeLabel.setOnClickListener(v -> {
            if (!isOwner) {
                return;
            }
            String defaultValue = noticeLabel.getText().toString();
            DialogUtil.input(context, "更改房间公告", defaultValue,
                    value -> roomChannel.updateNotice(value, new Callbacks.Lambda<>((success, data, errorMsg) -> {
                        if (!success) {
                            showToast("修改公告失败: " + errorMsg);
                        }
                    }))
            );
        });

        findViewById(R.id.room_gesture_layer).setOnClickListener(v -> {
            moreLayout.setVisibility(View.GONE);
            keyboardHelper.shrinkByEditText(commentInput);
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

                    type.setText(model.type);
                    content.setText(model.content);

                    int color = model.color;
                    type.setTextColor(color);
                    content.setTextColor(color);

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

        DialogUtil.doAction(this, "用户管理",
                new DialogUtil.Action("禁言", () -> {
                    int muteSeconds = 5 * 60;
                    chatService.setMuteComment(model.id, muteSeconds,
                            new Callbacks.Lambda<>((success, data, errorMsg) -> {
                                if (success) {
                                    showToast(String.format("已对%s禁言", model.nick));
                                } else {
                                    showToast("禁言失败: " + errorMsg);
                                }
                            })
                    );
                }),
                new DialogUtil.Action("取消禁言", () -> chatService.cancelMuteComment(model.id,
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
        if (roomDetail != null) {
            RoomInfo roomInfo = roomDetail.roomInfo;
            if (roomInfo != null) {
                title.setText(roomInfo.title);
                noticeLabel.setText(roomInfo.notice);
                setOnlineCount(roomInfo.onlineCount);
                // TODO: 2021/5/18 待替换
                setLikeCount(0);
            }
        }

        // 拉取在线列表 (Demo只拉取100条, 业务按需改造)
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
                    userListView.setData(users);
                }
            }

            @Override
            public void onError(String errorMsg) {
                showToast("获取在线列表失败: " + errorMsg);
            }
        });

        // 拉取弹幕列表
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
                showToast("拉取弹幕列表失败: " + errorMsg);
            }
        });

        if (isOwner) {
            // 主播, 开始预览
            liveService.startPreview(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    if (view.getParent() != renderContainer) {
                        ViewUtil.removeSelfSafely(view);
                        renderContainer.addView(view);
                    }
                    startLive.setVisibility(View.VISIBLE);
                    beauty.setVisibility(View.VISIBLE);
                    switchCamera.setVisibility(View.VISIBLE);
                    mirrorCamera.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(String errorMsg) {
                    showToast(errorMsg);
                }
            });
        } else {
            // 观众, 开始观看
            liveService.tryPlayLive(new Callback<View>() {
                @Override
                public void onSuccess(View view) {
                    renderContainer.addView(view);
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
    }

    public void onStartLive(View view) {
        liveService.startLive(new Callback<View>() {
            @Override
            public void onSuccess(View data) {
                startLive.setVisibility(View.GONE);
                stopLive.setVisibility(View.VISIBLE);
                addMessage("主播", "直播已开始");
                pauseLive.setVisibility(View.VISIBLE);
                mute.setVisibility(View.VISIBLE);
                isPushing = true;
            }

            @Override
            public void onError(String errorMsg) {
                showToast("开始直播失败: " + errorMsg);
            }
        });
    }

    public void onStopLive(View view) {
        DialogUtil.confirm(this, "还有观众正在路上，确定要结束直播吗？", new Runnable() {
            @Override
            public void run() {
                liveService.stopLive(new Callback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        addMessage("主播", "直播已结束");
                        startLive.setVisibility(View.VISIBLE);
                        stopLive.setVisibility(View.GONE);
                        pauseLive.setVisibility(View.GONE);
                        mute.setVisibility(View.GONE);
                        isPushing = false;
                    }

                    @Override
                    public void onError(String errorMsg) {
                        showToast("结束直播失败: " + errorMsg);
                    }
                });
            }
        });
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
    }

    @SuppressLint("SetTextI18n")
    public void onBeauty(View view) {
        boolean enabled = view.isSelected();
        boolean isBeautyOpen = !enabled;
        liveService.setBeautyOn(isBeautyOpen);
        view.setSelected(isBeautyOpen);
        view.setAlpha(isBeautyOpen ? 1 : 0.5f);
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
            if (liveService != null) {
                liveService.pauseLive();
            }
            isPushing = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (isOwner && !isPushing) {
            if (liveService != null) {
                liveService.resumeLive();
            }
            isPushing = true;
        }
    }

    @Override
    public void finish() {
        super.finish();
        // 当前有主播正在直播时, 先停止直播
        if (roomChannel != null) {
            if (liveService.hasLive() && roomChannel.isOwner()) {
                liveService.stopLive(new Callbacks.Log<>(TAG, "stop live"));
            }
            if (renderContainer != null) {
                renderContainer.removeAllViews();
            }
        }
    }

    public void onSwitch(View view) {
        moreLayout.setVisibility(View.GONE);
        liveService.switchCamera();
    }

    public void onPauseLive(View view) {
        moreLayout.setVisibility(View.GONE);
        if (isPushing) {
            liveService.pauseLive();
        } else {
            liveService.resumeLive();
        }
        ((Button) view).setText(isPushing ? "恢复直播" : "暂停直播");
        isPushing = !isPushing;
    }

    public void onMore(View view) {
        int target = moreLayout.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE;
        moreLayout.setVisibility(target);
    }

    public void onMuteLive(View view) {
        moreLayout.setVisibility(View.GONE);
        isMute = !isMute;
        liveService.setMutePush(isMute);
        ((Button) view).setText(isMute ? "取消静音" : "静音");
    }

    public void onMirrorLive(View view) {
        moreLayout.setVisibility(View.GONE);
        isMirror = !isMirror;
        liveService.setPreviewMirror(isMirror);
        liveService.setPushMirror(isMirror);
        ((Button) view).setText(isMirror ? "取消镜像" : "镜像");
    }
}
