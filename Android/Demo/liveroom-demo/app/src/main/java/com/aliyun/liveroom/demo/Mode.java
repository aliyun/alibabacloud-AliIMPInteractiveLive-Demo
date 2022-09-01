package com.aliyun.liveroom.demo;

/**
 * @author puke
 * @version 2022/1/14
 */
public enum Mode {

    DEFAULT("默认样式"),
    CUSTOM("自定义样式"),
    LINK_MIC("连麦样式"),
    ECOMMERCE("电商样式"),
    ENTERPRISE("企业直播样式"),
    ;

    public final String desc;

    Mode(String desc) {
        this.desc = desc;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public String toString() {
        return desc;
    }
}
