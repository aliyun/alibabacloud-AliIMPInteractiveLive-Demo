package com.aliyun.roompaas.biz.exposable.event;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/5/21
 */
public class KickUserEvent implements Serializable {

    /**
     * 踢人方的id
     */
    public String userId;

    /**
     * 被踢方的id
     */
    public String kickUser;

    /**
     * 被踢方的昵称
     */
    public String kickUserName;
}
