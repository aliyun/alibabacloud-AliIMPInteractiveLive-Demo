package com.aliyun.roompaas.rtc.exposable;

/**
 * 连麦摄像头显示模式
 *
 * @author puke
 * @version 2022/1/18
 */
public enum VideoStreamShowMode {

    /**
     * 不保持比例平铺
     */
    Stretch,

    /**
     * 保持比例, 黑边
     */
    Fill,

    /**
     * 保持比例填充, 需裁剪
     */
    Crop,
}
