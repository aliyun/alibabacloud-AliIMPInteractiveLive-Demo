package com.aliyun.roompaas.chat;

import com.aliyun.roompaas.base.BaseDestroy;
import com.aliyun.roompaas.chat.exposable.ChatEventHandler;
import com.aliyun.roompaas.chat.exposable.event.CommentEvent;
import com.aliyun.roompaas.chat.exposable.event.CustomMessageEvent;
import com.aliyun.roompaas.chat.exposable.event.LikeEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteAllCommentEvent;
import com.aliyun.roompaas.chat.exposable.event.MuteCommentEvent;

/**
 * @author puke
 * @version 2021/7/2
 */
public class SampleChatEventHandler extends BaseDestroy implements ChatEventHandler {

    @Override
    public void onLikeReceived(LikeEvent event) {

    }

    @Override
    public void onCommentReceived(CommentEvent event) {

    }

    @Override
    public void onCommentMutedOrCancel(MuteCommentEvent event) {

    }

    @Override
    public void onCommentAllMutedOrCancel(MuteAllCommentEvent event) {

    }

    @Override
    public void onCustomMessageReceived(CustomMessageEvent event) {
        
    }
}
