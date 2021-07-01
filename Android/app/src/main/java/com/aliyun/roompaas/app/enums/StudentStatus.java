package com.aliyun.roompaas.app.enums;

/**
 * @author puke
 * @version 2021/6/4
 */
public enum StudentStatus {

    ONLINE(0, "已连麦"),

    INVITING(1, "待应答"),

    OFFLINE(2, "未连麦"),

    APPLYING(3, "待通过"),
    ;

    private final int value;
    private final String desc;

    StudentStatus(int value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public int getValue() {
        return value;
    }

    public static StudentStatus of(int status) {
        for (StudentStatus current : values()) {
            if (current.value == status) {
                return current;
            }
        }
        return OFFLINE;
    }
}
