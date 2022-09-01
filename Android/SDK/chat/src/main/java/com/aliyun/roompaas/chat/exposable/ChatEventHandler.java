package com.aliyun.roompaas.chat.exposable;

import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent;
import com.aliyun.roompaas.chat.exposable.event.LikeEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;

/**
 * @author puke
 * @version 2021/7/2
 */
public interface ChatEventHandler {

    /**
     * 收到点赞消息
     */
    void onLikeReceived(LikeEvent event);

    /**
     * 收到弹幕消息
     */
    void onCommentReceived(CommentEvent event);

    /**
     * 收到(取消)禁言消息
     */
    void onCommentMutedOrCancel(MuteCommentEvent event);

    /**
     * 收到(取消)全体禁言消息
     */
    void onCommentAllMutedOrCancel(MuteAllCommentEvent event);

    /**
     * 收到自定义消息
     */
    void onCustomMessageReceived(CustomMessageEvent event);
}
