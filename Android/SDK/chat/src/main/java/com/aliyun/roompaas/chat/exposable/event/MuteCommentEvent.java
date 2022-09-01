package com.aliyun.roompaas.chat.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/21
 */
public class MuteCommentEvent implements Serializable {

    /**
     * 禁言/取消禁言
     */
    public boolean mute;

    /**
     * 禁言时长
     */
    public long muteTime;

    /**
     * 被禁言的用户昵称
     */
    public String muteUserNick;

    /**
     * 被禁言的用户id
     */
    public String muteUserOpenId;

    /**
     * 话题id
     */
    public String topicId;
}
