package com.aliyun.roompaas.chat.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/17
 */
public class LikeEvent implements Serializable {

    /**
     * 话题Id
     */
    public String topicId;

    /**
     * 点赞数
     */
    public int likeCount;
}
