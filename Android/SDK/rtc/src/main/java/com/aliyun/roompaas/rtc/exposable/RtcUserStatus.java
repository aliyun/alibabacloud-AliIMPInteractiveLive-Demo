package com.aliyun.roompaas.rtc.exposable;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/6/15
 */
public enum RtcUserStatus implements Serializable {

    /**
     * 呼叫状态
     */
    ON_JOINING(2, "待应答"),

    /**
     * 会议中
     */
    ACTIVE(3, "会议中"),

    /**
     * 入会失败
     */
    JOIN_FAILED(4, "入会失败"),

    /**
     * 离会
     */
    LEAVE(6, "未连麦"),

    /**
     * 申请中 (该status仅为客户端标识, 故取-1)
     */
    APPLYING(-1, "待通过"),
    ;

    private final int status;
    private final String desc;

    RtcUserStatus(int status, String desc) {
        this.status = status;
        this.desc = desc;
    }

    public int getType() {
        return status;
    }

    public String getDesc() {
        return desc;
    }

    public int getStatus() {
        return status;
    }

    public static RtcUserStatus of(int status) {
        for (RtcUserStatus current : values()) {
            if (current.status == status) {
                return current;
            }
        }
        return LEAVE;
    }
}
