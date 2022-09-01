package com.aliyun.standard.liveroom.lib.linkmic.model;

import android.view.View;

/**
 * @author puke
 * @version 2022/1/12
 */
public class LinkMicUserModel {

    /**
     * 用户Id
     */
    public String userId;

    /**
     * 用户昵称
     */
    public String nickname;

    /**
     * 麦克风是否打开
     */
    public boolean isMicOpen;

    /**
     * 摄像头是否打开
     */
    public boolean isCameraOpen;

    /**
     * 是不是主播
     */
    public boolean isAnchor;

    /**
     * 相机流视图
     */
    public View cameraView;
}
