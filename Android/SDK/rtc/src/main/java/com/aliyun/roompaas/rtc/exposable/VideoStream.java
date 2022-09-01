package com.aliyun.roompaas.rtc.exposable;

import android.view.View;

/**
 * 远端视频流
 *
 * @author puke
 * @version 2022/4/26
 */
public class VideoStream {

    /**
     * 用户Id
     */
    public String userId;

    /**
     * 是否可见
     */
    public boolean available;

    /**
     * 流视图
     */
    public View view;

    /**
     * 流类型
     */
    public StreamType streamType;
}
