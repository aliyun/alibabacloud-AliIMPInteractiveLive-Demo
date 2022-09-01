package com.aliyun.roompaas.biz.exposable.enums;

/**
 * @author puke
 * @version 2021/9/22
 */
public enum LiveStatus {

    NOT_START(0, "未开始"),
    DOING(1, "正在进行"),
    END(2, "已结束"),
    ALL(3, "全部"),
    ;

    public final int value;
    public final String desc;

    LiveStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }
    
    public static LiveStatus of(int value) {
        for (LiveStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        return LiveStatus.NOT_START;
    }
}
