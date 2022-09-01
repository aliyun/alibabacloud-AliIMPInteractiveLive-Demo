package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.dingpaas.chat.CommentModel;
import com.alibaba.dingpaas.room.RoomDetail;
import com.alibaba.fastjson.JSON;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.model.PageModel;
import com.aliyun.roompaas.base.util.CollectionUtil;
import com.aliyun.roompaas.biz.SampleRoomEventHandler;
import com.aliyun.roompaas.biz.exposable.event.KickUserEvent;
import com.aliyun.roompaas.biz.exposable.event.RoomInOutEvent;
import com.aliyun.roompaas.chat.CommentSortType;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.CommentParam;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.helper.RecyclerViewHelper;
import com.aliyun.roompaas.uibase.util.AppUtil;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LimitSizeRecyclerView;
import com.aliyun.standard.liveroom.lib.LiveConst;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.MessageModel;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;
import com.aliyun.standard.liveroom.lib.helper.MessageHelper;
import com.aliyun.standard.liveroom.lib.widget.FlyView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveMessageView extends RelativeLayout implements ComponentHolder {

    private static final String TAG = LiveMessageView.class.getSimpleName();
    private static final int NICK_SHOW_MAX_LENGTH = 15;

    protected final FlyView flyView;
    protected final LimitSizeRecyclerView recyclerView;
    protected final TextView unreadTips;

    private final MessageHelper<MessageModel> messageHelper;
    private final Component component = new Component();
    private final LinearLayoutManager layoutManager;
    private final RecyclerViewHelper<MessageModel> recyclerViewHelper;
    private final int commentMaxHeight = AppUtil.getScreenHeight() / 3;
    private final Runnable refreshUITask = new Runnable() {
        @Override
        public void run() {
            recyclerView.invalidate();
        }
    };

    private boolean isSystemAlertMessageAlreadyAdded;
    private int lastPosition;
    private boolean forceHover;

    public LiveMessageView(Context context) {
        this(context, null, 0);
    }

    public LiveMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        View.inflate(context, R.layout.ilr_view_live_message, this);
        flyView = findViewById(R.id.message_fly_view);
        recyclerView = findViewById(R.id.message_recycler_view);
        unreadTips = findViewById(R.id.message_unread_tips);

        // 弹幕面板
        recyclerView.setMaxHeight(commentMaxHeight);
        layoutManager = new LinearLayoutManager(context);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerViewHelper = RecyclerViewHelper.of(recyclerView, R.layout.ilr_item_message,
                new RecyclerViewHelper.HolderRenderer<MessageModel>() {
                    @Override
                    public void render(RecyclerViewHelper.ViewHolder holder, final MessageModel model, int position, int itemCount) {
                        TextView content = holder.getView(R.id.item_content);
                        content.setTextColor(model.contentColor);

                        if (TextUtils.isEmpty(model.type)) {
                            content.setText(model.content);
                        } else {
                            String prefix = model.type + "：";
                            String postfix = model.content;

                            SpannableString spannableString = new SpannableString(prefix + postfix);
                            spannableString.setSpan(new ForegroundColorSpan(model.color), 0,
                                    prefix.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                            content.setText(spannableString);
                        }

                        content.setOnLongClickListener(new OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                if (!TextUtils.isEmpty(model.userId)) {
                                    component.handleMsgLongClick(model);
                                }
                                return true;
                            }
                        });

                        content.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (!TextUtils.isEmpty(model.userId)) {
                                    component.handleMsgClick(model);
                                }
                            }
                        });
                    }
                });

        // 维度消息控制逻辑
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                refreshUnreadTips();
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                forceHover = false;
                refreshUnreadTips();
            }
        });
        unreadTips.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lastPosition = 0;
                forceHover = false;
                recyclerView.scrollToPosition(recyclerViewHelper.getItemCount() - 1);
            }
        });

        // 消息控制辅助类
        messageHelper = new MessageHelper<MessageModel>()
                .setCallback(new MessageHelper.Callback<MessageModel>() {
                    @Override
                    public int getTotalSize() {
                        return recyclerViewHelper.getItemCount();
                    }

                    @Override
                    public void onMessageAdded(MessageModel message) {
                        addMessageToPanel(Collections.singletonList(message));
                    }

                    @Override
                    public void onMessageRemoved(int suggestRemoveCount) {
                        lastPosition -= suggestRemoveCount;
                        if (forceHover) {
                            postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    forceHover = true;
                                }
                            }, 10);
                        }
                        recyclerViewHelper.removeDataWithoutAnimation(0, suggestRemoveCount);
                    }
                });
    }

    /**
     * @return 是否开启未读提示条逻辑
     */
    protected boolean enableUnreadTipsLogic() {
        return true;
    }

    /**
     * @return 是否开启系统消息显示逻辑
     */
    protected boolean enableSystemLogic() {
        return true;
    }

    private void refreshUnreadTips() {
        if (!enableUnreadTipsLogic()) {
            // 未开启未读提示条逻辑时, 不做额外处理
            return;
        }

        int itemCount = recyclerViewHelper.getItemCount();
        int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
        if (lastPosition >= itemCount) {
            lastPosition = lastVisibleItemPosition;
        } else {
            lastPosition = Math.max(lastVisibleItemPosition, lastPosition);
        }

        if (forceHover || (lastPosition >= 0 && lastPosition < itemCount - 1)) {
            // 一旦悬停, 就要等到列表滚动后, 才能解除悬停状态
            forceHover = true;
            unreadTips.setVisibility(VISIBLE);
            int showCount = lastPosition + 1;
            int unreadCount = itemCount - showCount;
            unreadTips.setText(String.format("%s条新消息", unreadCount));
        } else {
            unreadTips.setVisibility(GONE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = MeasureSpec.getSize(heightMeasureSpec);
        final int maxMessageHeight;
        if (component.isLandscape()) {
            // 横屏
            maxMessageHeight = AppUtil.getScreenHeight() / 3;
            // 宽度为屏幕一半
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(
                    AppUtil.getScreenWidth() / 2, MeasureSpec.getMode(widthMeasureSpec)
            );
        } else {
            // 竖屏
            int systemMaxHeight = enableSystemLogic() ? (getResources().getDimensionPixelOffset(R.dimen.live_message_fly_height) + getResources().getDimensionPixelOffset(R.dimen.message_fly_bottom_margin)) : 0;
            maxMessageHeight = commentMaxHeight + systemMaxHeight;
        }
        if (height > maxMessageHeight) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(
                    maxMessageHeight, MeasureSpec.getMode(heightMeasureSpec)
            );
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void addSystemMessage(CharSequence content) {
        FlyView.FlyItem item = new FlyView.FlyItem();
        item.content = content;
        addSystemMessage(item);
    }

    protected void addSystemMessage(FlyView.FlyItem item) {
        if (enableSystemLogic()) {
            // 开启系统消息显示逻辑, 才做处理
            flyView.addItem(item);
        }
    }

    protected void addMessage(String type, String content) {
        addMessage(new MessageModel(type, content));
    }

    protected void addMessage(MessageModel messageModel) {
        messageHelper.addMessage(messageModel);
    }

    protected void addMessageByUserId(String userId, String type, String content) {
        messageHelper.addMessage(new MessageModel(userId, type, content));
    }

    /**
     * 弹幕信息添加到面板
     *
     * @param addedList 弹幕信息
     */
    protected void addMessageToPanel(final List<MessageModel> addedList) {
        boolean isLastCompletelyVisible = layoutManager.findLastVisibleItemPosition()
                == recyclerViewHelper.getItemCount() - 1;
        recyclerViewHelper.addData(addedList);
        if (!forceHover && isLastCompletelyVisible) {
            // 已触底时, 随消息联动
            layoutManager.scrollToPositionWithOffset(
                    recyclerViewHelper.getItemCount() - 1, Integer.MIN_VALUE);
            postDelayed(refreshUITask, 100);
            lastPosition = 0;
        } else {
            refreshUnreadTips();
        }
    }

    /**
     * @return 首条系统消息 (返回null时, 不展示)
     */
    @Nullable
    protected MessageModel getSystemAlertMessageModel() {
        MessageModel systemMessage = new MessageModel(
                LiveConst.SYSTEM_NOTICE_NICKNAME, LiveConst.SYSTEM_NOTICE_ALERT);
        systemMessage.contentColor = Color.parseColor("#12DBE6");
        return systemMessage;
    }

    protected void onEnterOrLeaveRoom(RoomInOutEvent event) {
        addSystemMessage(String.format(
                "%s%s了房间", truncateNick(event.nick), event.enter ? "进入" : "离开"));
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    protected static String truncateNick(String nick) {
        if (!TextUtils.isEmpty(nick) && nick.length() > NICK_SHOW_MAX_LENGTH) {
            nick = nick.substring(0, NICK_SHOW_MAX_LENGTH);
        }
        return nick;
    }

    private class Component extends BaseComponent {
        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);

            // 回放不展示信息面板
            setVisibility(needPlayback() ? GONE : VISIBLE);

            // 监听房间事件
            roomChannel.addEventHandler(new SampleRoomEventHandler() {
                @Override
                public void onEnterOrLeaveRoom(RoomInOutEvent event) {
                    LiveMessageView.this.onEnterOrLeaveRoom(event);
                }

                @Override
                public void onRoomUserKicked(KickUserEvent event) {
                    if (!TextUtils.equals(roomChannel.getUserId(), event.kickUser)) {
                        // 其他人, 移除列表, 面板提示
                        addSystemMessage(String.format("%s被管理员移除房间", truncateNick(event.kickUserName)));
                    }
                }
            });

            // 监听互动事件
            chatService.addEventHandler(new SampleChatEventHandler() {
                @Override
                public void onCommentReceived(CommentEvent event) {
                    String senderId = event.creatorOpenId;
                    if (TextUtils.equals(senderId, Const.getCurrentUserId())) {
                        // 自己发送的消息不做上屏显示
                        return;
                    }

                    String nick = truncateNick(event.creatorNick);
                    addMessageByUserId(senderId, nick, event.content);
                }

                @Override
                public void onCommentMutedOrCancel(MuteCommentEvent event) {
                    // 禁言 & 取消禁言
                    String action = event.mute ? "禁言" : "取消禁言";
                    boolean isSelf = TextUtils.equals(roomChannel.getUserId(), event.muteUserOpenId);
                    String subject = isSelf ? "您" : truncateNick(event.muteUserNick);
                    addSystemMessage(String.format("%s被管理员%s了", subject, action));
                }

                @Override
                public void onCommentAllMutedOrCancel(MuteAllCommentEvent event) {
                    // 全体禁言 & 取消全体禁言
                    String action = event.mute ? "开启了全体禁言" : "取消了全体禁言";
                    addSystemMessage(String.format("管理员%s", action));
                }
            });

            // 监听直播事件
            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStarted(LiveCommonEvent event) {
                    if (!isOwner()) {
                        addSystemMessage("直播已开始");
                    }
                }

                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    if (!isOwner()) {
                        addSystemMessage("直播已结束");
                    }
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            super.onEnterRoomSuccess(roomDetail);

            // 禁用、切换用户或回放时, 不需要加载弹幕信息
            if (getOpenLiveParam().loadHistoryComment
                    && !liveContext.isSwitchUser()
                    && !needPlayback()) {
                loadComment();
            }
        }

        private void loadComment() {
            CommentParam commentParam = new CommentParam();
            commentParam.pageNum = 1;
            commentParam.pageSize = 100;
            commentParam.sortType = CommentSortType.DESC_BY_TIME;
            chatService.listComment(commentParam, new Callback<PageModel<CommentModel>>() {
                @Override
                public void onSuccess(PageModel<CommentModel> pageModel) {
                    if (pageModel != null) {
                        List<CommentModel> list = pageModel.list;
                        if (!isSystemAlertMessageAlreadyAdded && recyclerViewHelper != null) {
                            MessageModel systemAlert = getSystemAlertMessageModel();
                            if (systemAlert != null) {
                                recyclerViewHelper.insertCell(0, systemAlert);
                                isSystemAlertMessageAlreadyAdded = true;
                            }
                        }
                        if (CollectionUtil.isNotEmpty(list)) {
                            // 记录插入前的索引值
                            List<MessageModel> addedList = new ArrayList<>();
                            // 倒序取的
                            for (int i = list.size() - 1; i >= 0; i--) {
                                CommentModel model = list.get(i);
                                String nick = truncateNick(model.creatorNick);
                                addedList.add(new MessageModel(model.creatorId, nick, model.content));
                            }
                            addMessageToPanel(addedList);
                        }
                    }
                }

                @Override
                public void onError(String errorMsg) {
                    showToast("获取弹幕列表失败: " + errorMsg);
                }
            });
        }

        @Override
        public void onEvent(String action, Object... args) {
            if (Actions.SHOW_MESSAGE.equals(action)) {
                if (args.length >= 1) {
                    MessageModel messageModel = (MessageModel) args[0];
                    // 判断是否忽略弹幕频率限制
                    boolean ignoreFreqLimit = args.length > 1 && Boolean.TRUE.equals(args[1]);
                    if (ignoreFreqLimit) {
                        // 忽略限流控制时, 直接上屏
                        addMessageToPanel(Collections.singletonList(messageModel));
                    } else {
                        // 默认是交给消息流控
                        addMessage(messageModel);
                    }
                } else {
                    Logger.w(TAG, "Received invalid message param: " + JSON.toJSONString(args));
                }
            } else if (Actions.SHOW_SYSTEM_MESSAGE.equals(action)) {
                if (args.length >= 1) {
                    if ((args[0] instanceof String)) {
                        addSystemMessage((String) args[0]);
                    } else if (args[0] instanceof FlyView.FlyItem) {
                        addSystemMessage((FlyView.FlyItem) args[0]);
                    }
                } else {
                    Logger.w(TAG, "Received invalid message param: " + JSON.toJSONString(args));
                }
            }
        }

        @Override
        public void onActivityDestroy() {
            if (messageHelper != null) {
                messageHelper.destroy();
            }
        }

        private void handleMsgClick(MessageModel model) {
            postEvent(Actions.SHOW_MESSAGE_CLICKED, model);
        }

        private void handleMsgLongClick(MessageModel model) {
            postEvent(Actions.SHOW_MESSAGE_LONG_CLICKED, model);
        }
    }
}
