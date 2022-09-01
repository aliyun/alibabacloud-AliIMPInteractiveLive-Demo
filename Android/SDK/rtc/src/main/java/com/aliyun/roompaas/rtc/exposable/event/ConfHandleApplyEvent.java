package com.aliyun.roompaas.rtc.exposable.event;


import java.io.Serializable;

/**
 * 拒绝连麦消息
 *
 * @author puke
 * @version 2021/6/2
 */
public class ConfHandleApplyEvent implements Serializable {

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
     * 申请连麦用户
     */
    public String uid;

    // true: 同意申请连麦，false: 拒绝申请连麦
    public boolean approve;
}
