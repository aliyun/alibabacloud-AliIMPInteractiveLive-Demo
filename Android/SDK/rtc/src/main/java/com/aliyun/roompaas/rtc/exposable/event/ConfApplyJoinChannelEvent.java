package com.aliyun.roompaas.rtc.exposable.event;

import com.alibaba.dingpaas.rtc.ConfUserModel;

import java.io.Serializable;

/**
 * 申请连麦消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfApplyJoinChannelEvent implements Serializable {

    /**
     * 类型
     */
    public int type;

    /**
     * 状态
     */
    public long version;

    /**
     * 会议Id
     */
    public String confId;

    /**
     * true: 申请  false:取消申请
     */
    public boolean isApply;

    /**
     * 申请连麦用户
     */
    public ConfUserModel applyUser;
}
