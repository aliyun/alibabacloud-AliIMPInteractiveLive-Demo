package com.aliyun.roompaas.live.exposable;

/**
 * 互动直播业务配置参数
 */
public class BusinessOptions {
    /**
     * 直播title
     */
    public String liveTitle = "";
    /**
     * 直播开始时间
     */
    public long liveStartTime = 0L;

    /**
     * 直播结束时间
     */
    public long liveEndTime = 0L;

    /**
     * 直播简介
     */
    public String liveIntroduction = "";
    /**
     * 直播封面
     */
    public String liveCoverUrl = "";
    /**
     * 扩展字段
     */
    public String extension = "";
}
