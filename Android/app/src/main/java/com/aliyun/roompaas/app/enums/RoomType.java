package com.aliyun.roompaas.app.enums;

/**
 * @author puke
 * @version 2021/5/13
 */
public enum RoomType {

    BUSINESS("business", "电商直播"),

    CLASSROOM("classroom", "课堂");

    private final String bizType;
    private final String desc;

    RoomType(String bizType, String desc) {
        this.bizType = bizType;
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }

    public String getBizType() {
        return bizType;
    }
}
