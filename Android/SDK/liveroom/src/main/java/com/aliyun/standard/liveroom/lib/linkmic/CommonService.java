package com.aliyun.standard.liveroom.lib.linkmic;

import android.view.View;

import com.aliyun.roompaas.base.exposable.IEventHandlerManager;
import com.aliyun.standard.liveroom.lib.linkmic.enums.ContentMode;
import com.aliyun.standard.liveroom.lib.linkmic.model.LinkMicUserModel;

import java.util.Map;

/**
 * @author puke
 * @version 2022/1/5
 */
public interface CommonService extends IEventHandlerManager<LinkMicEventHandler> {

    /**
     * @return 判断自己是否在麦上
     */
    boolean isJoined();

    /**
     * 退出连麦
     */
    void leave();

    /**
     * @return 判断连麦时是否打开摄像头
     */
    boolean isCameraOpened();

    /**
     * 打开摄像头 (默认开启)
     *
     * @return 本地渲染View
     */
    View openCamera();

    /**
     * 关闭摄像头
     */
    void closeCamera();

    /**
     * 切换摄像头 (前后置切换)
     */
    void switchCamera();

    /**
     * 设置预览画面是否开启镜像
     *
     * @param enable true: 开启; false: 关闭;
     */
    void setPreviewMirror(boolean enable);

    /**
     * 设置视频流是否开启镜像
     *
     * @param enable true: 开启; false: 关闭;
     */
    void setCameraStreamMirror(boolean enable);

    /**
     * 设置预览画面的显示模式
     *
     * @param mode 显示模式 (默认Crop)
     */
    void setPreviewContentMode(ContentMode mode);

    /**
     * 设置观看别人画面的显示模式
     *
     * @param mode 显示模式 (默认Crop)
     */
    void setRemoteCameraContentMode(ContentMode mode);

    /**
     * @return 判断连麦时是否打开麦克风
     */
    boolean isMicOpened();

    /**
     * @return 判断是否允许打开麦克风
     * @see #isMicAllMuted()
     */
    @Deprecated
    boolean isSelfMicAllowed();

    /**
     * 是否全员静音
     * @return true if anchor开启全员静音
     */
    boolean isMicAllMuted();

    /**
     * 开启本地麦克风 (默认开启)
     */
    void openMic();

    /**
     * 关闭本地麦克风
     */
    void closeMic();

    /**
     * 返回已加入连麦的用户信息
     *
     * @return userId=>用户实体 映射关系
     */
    Map<String, LinkMicUserModel> getJoinedUsers();

    /**
     * 设置美颜开关
     *
     * @param beautyOn 是否开启美颜
     */
    void setBeautyOn(boolean beautyOn);
}
