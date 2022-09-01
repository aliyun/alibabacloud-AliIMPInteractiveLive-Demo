package com.aliyun.roompaas.rtc.exposable.event;

import com.alibaba.dingpaas.rtc.ConfUserModel;

import java.io.Serializable;
import java.util.List;

/**
 * 邀请消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfInviteEvent implements Serializable {

    /**
     * 类型
     */
    public int type;

    /**
     * 版本
     */
    public long version;

    /**
     * 被邀请者
     */
    public List<ConfUserModel> calleeList;

    /**
     * 邀请者信息
     */
    public ConfUserModel caller;
}
