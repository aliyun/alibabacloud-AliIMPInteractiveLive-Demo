package com.aliyun.roompaas.chat.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/8/5
 */
public class MuteAllCommentEvent implements Serializable {

    /**
     * 话题id
     */
    public String topicId;

    /**
     * 禁言/取消禁言
     */
    public boolean mute;
}
