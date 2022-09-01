package com.aliyun.roompaas.chat.exposable.event;

import java.io.Serializable;
import java.util.Map;

/**
 * @author puke
 * @version 2021/5/17
 */
public class CommentEvent implements Serializable {

    /**
     * 话题id
     */
    public String topicId;

    /**
     * 评论id
     */
    public String commentId;

    /**
     * 发送者id
     */
    public String creatorOpenId;

    /**
     * 发送者nick
     */
    public String creatorNick;

    /**
     * 创建时间
     */
    public Long createAt;

    /**
     * 类型
     */
    public int type;

    /**
     * 内容
     */
    public String content;

    /**
     * 扩展字段
     */
    public Map<String, String> extension;
}
