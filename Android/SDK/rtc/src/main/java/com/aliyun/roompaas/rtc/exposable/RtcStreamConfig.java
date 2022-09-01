package com.aliyun.roompaas.rtc.exposable;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * RTC配置类
 * videoStreamTypeHeightWidth 大流视频宽度，默认640
 * videoStreamTypeHeightHeight 大流视频高度，默认480
 * videoStreamTypeLowPublished 是否开启同时推低分辨率（小流），默认开启。小流无法修改分辨率
 */
public class RtcStreamConfig {
    private int videoStreamTypeHeightWidth = 640;
    private int videoStreamTypeHeightHeight = 480;
    private boolean videoStreamTypeLowPublished = true;

    //旁路推流分辨率
    @BypassLiveResolutionType
    private int bypassLiveResolutionType = BypassLiveResolutionType.Type_1280x720;

    @Retention(RetentionPolicy.RUNTIME)
    @IntDef({BypassLiveResolutionType.Type_1280x720
            , BypassLiveResolutionType.Type_720x1280
            , BypassLiveResolutionType.Type_1920x1080
            , BypassLiveResolutionType.Type_1080x1920})
    public @interface BypassLiveResolutionType {
        int Type_1280x720 = 1;   // 1280x720(横屏)
        int Type_720x1280 = 2;   // 720x1280(竖屏)
        int Type_1920x1080 = 3;  // 1920x1080(横屏)
        int Type_1080x1920 = 4;   // 1080x1920(竖屏)
    }

    public RtcStreamConfig() {

    }

    public RtcStreamConfig(int width, int height) {
        videoStreamTypeHeightWidth = width;
        videoStreamTypeHeightHeight = height;
    }

    public RtcStreamConfig(int videoStreamTypeHeightWidth, int videoStreamTypeHeightHeight, boolean videoStreamTypeLowPublished) {
        this.videoStreamTypeHeightWidth = videoStreamTypeHeightWidth;
        this.videoStreamTypeHeightHeight = videoStreamTypeHeightHeight;
        this.videoStreamTypeLowPublished = videoStreamTypeLowPublished;
    }

    public RtcStreamConfig(int videoStreamTypeHeightWidth, int videoStreamTypeHeightHeight, boolean videoStreamTypeLowPublished, int bypassLiveResolutionType) {
        this.videoStreamTypeHeightWidth = videoStreamTypeHeightWidth;
        this.videoStreamTypeHeightHeight = videoStreamTypeHeightHeight;
        this.videoStreamTypeLowPublished = videoStreamTypeLowPublished;
        this.bypassLiveResolutionType = bypassLiveResolutionType;
    }

    public int getWidth() {
        return videoStreamTypeHeightWidth;
    }

    public void setWidth(int width) {
        this.videoStreamTypeHeightWidth = width;
    }

    public int getHeight() {
        return videoStreamTypeHeightHeight;
    }

    public void setHeight(int height) {
        this.videoStreamTypeHeightHeight = height;
    }

    public boolean isVideoStreamTypeLowPublished() {
        return videoStreamTypeLowPublished;
    }

    public void setVideoStreamTypeLowPublished(boolean videoStreamTypeLowPublished) {
        this.videoStreamTypeLowPublished = videoStreamTypeLowPublished;
    }

    @BypassLiveResolutionType
    public int getBypassLiveResolutionType() {
        return bypassLiveResolutionType;
    }

    public void setBypassLiveResolutionType(@BypassLiveResolutionType int bypassLiveResolutionType) {
        this.bypassLiveResolutionType = bypassLiveResolutionType;
    }
}
