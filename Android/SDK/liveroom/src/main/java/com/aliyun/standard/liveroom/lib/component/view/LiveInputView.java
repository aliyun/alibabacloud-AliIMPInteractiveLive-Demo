package com.aliyun.standard.liveroom.lib.component.view;

import android.content.Context;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.alibaba.dingpaas.chat.GetTopicInfoRsp;
import com.alibaba.dingpaas.room.RoomDetail;
import com.aliyun.roompaas.base.IDestroyable;
import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.base.util.Utils;
import com.aliyun.roompaas.chat.SampleChatEventHandler;
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;
import com.aliyun.roompaas.live.SampleLiveEventHandler;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;
import com.aliyun.roompaas.roombase.Const;
import com.aliyun.roompaas.uibase.view.DialogInputView;
import com.aliyun.standard.liveroom.lib.Actions;
import com.aliyun.standard.liveroom.lib.LiveContext;
import com.aliyun.standard.liveroom.lib.MessageModel;
import com.aliyun.standard.liveroom.lib.R;
import com.aliyun.standard.liveroom.lib.component.BaseComponent;
import com.aliyun.standard.liveroom.lib.component.ComponentHolder;
import com.aliyun.standard.liveroom.lib.component.IComponent;

/**
 * @author puke
 * @version 2021/7/29
 */
public class LiveInputView extends DialogInputView implements ComponentHolder {
    public static final String TAG = "LiveInputView";

    private final Component component = new Component(this);

    public LiveInputView(@NonNull Context context) {
        this(context, null, 0);
    }

    public LiveInputView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LiveInputView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @LayoutRes
    protected int getDefaultLayoutResId() {
        return R.layout.ilr_view_live_input;
    }

    @IdRes
    protected int respondingEditTextId() {
        return R.id.room_comment_input;
    }

    @Override
    protected int inputHintInDialog() {
        return R.string.live_input_default_tips;
    }

    @Override
    protected void onRespondingViewClicked() {
        component.postEvent(Actions.SEND_COMMENT_INPUT_CLICKED);
    }

    protected int getSendCommentMaxLength() {
        return component.getOpenLiveParam().sendCommentMaxLength;
    }

    @Override
    protected void onCommentLenReachLimit(int maxLen) {
        component.showToast(String.format("最多输入%s个字符", maxLen));
    }

    @Override
    protected void onSendClickContentEmpty() {
        component.showToast("请输入评论内容");
    }

    @Override
    protected void onCommentSubmit(String inputText) {
        component.onCommentSubmit(inputText);
    }

    protected void updateMuteState(GetTopicInfoRsp chatDetail) {
        if (chatDetail != null && !component.isOwner()) {
            if (chatDetail.muteAll) {
                // 全员被禁言
                setInputStyle(false, R.string.live_ban_all_tips);
            } else if (chatDetail.mute) {
                // 自己被禁言
                setInputStyle(false, R.string.live_ban_tips);
            } else {
                // 未被禁言
                setInputStyle(true, R.string.live_input_default_tips);
            }
        }
    }

    protected void setInputStyle(boolean enable, @StringRes int hintRes) {
        commentInput.setEnabled(enable);
        commentInput.setText(hintRes);
    }

    @Override
    public IComponent getComponent() {
        return component;
    }

    private class Component extends BaseComponent {
        private IDestroyable iDestroyable;

        public Component(IDestroyable iDestroyable) {
            this.iDestroyable = iDestroyable;
        }

        @Override
        public void onActivityDestroy() {
            Utils.destroy(iDestroyable);
        }

        @Override
        public void onInit(LiveContext liveContext) {
            super.onInit(liveContext);
            chatService.addEventHandler(new SampleChatEventHandler() {
                @Override
                public void onCommentMutedOrCancel(MuteCommentEvent event) {
                    boolean isSelf = TextUtils.equals(event.muteUserOpenId, roomChannel.getUserId());
                    if (isSelf) {
                        // 是自己, 才处理
                        updateMuteState(chatService.getChatDetail());
                    }
                }

                @Override
                public void onCommentAllMutedOrCancel(MuteAllCommentEvent event) {
                    updateMuteState(chatService.getChatDetail());
                }
            });

            liveService.addEventHandler(new SampleLiveEventHandler() {
                @Override
                public void onLiveStopped(LiveCommonEvent event) {
                    // TODO: 2021/11/2 先不添加自动转化逻辑
//                    if (supportPlayback()) {
//                        handlePlaybackLogic();
//                    }
                }
            });
        }

        @Override
        public void onEnterRoomSuccess(RoomDetail roomDetail) {
            if (needPlayback()) {
                handlePlaybackLogic();
            }
        }

        private void handlePlaybackLogic() {
            commentInput.setText("当前不可发言");
            commentInput.setClickable(false);
        }

        private void onCommentSubmit(final String input) {
            postEvent(Actions.SEND_COMMENT_TRIGGERED);
            chatService.sendComment(input, new Callback<String>() {
                @Override
                public void onSuccess(String data) {
                    // 通知面板, 自己发送的弹幕信息
                    String userId = Const.getCurrentUserId();
                    String currentNick = liveContext.getNick();
                    String type = currentNick == null ? "" : currentNick;
                    MessageModel messageModel = new MessageModel(userId, type, input);
                    postEvent(Actions.SHOW_MESSAGE, messageModel, true);
                    postEvent(Actions.SEND_COMMENT_SUCCESS, input);
                }

                @Override
                public void onError(String errorMsg) {
                    if (!getOpenLiveParam().commentConfigDisableSendFailToast) {
                        showToast(errorMsg);
                    }
                    postEvent(Actions.SEND_COMMENT_FAIL, errorMsg);
                }
            });
        }

        @Override
        public void onEvent(String action, Object... args) {
            if (Actions.GET_CHAT_DETAIL_SUCCESS.equals(action)) {
                updateMuteState(chatService.getChatDetail());
            }
        }
    }
}
