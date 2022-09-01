package com.aliyun.roompaas.live.exposable;

import android.app.Activity;
import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.live.BeautyInterface;
import com.aliyun.roompaas.player.exposable.CanvasScale;

/**
 * 直播推流服务
 *
 * @author puke
 * @version 2021/7/2
 */
public interface LivePusherService {

    int REQUEST_CODE_CAPTURE_PERMISSION = 0x1123;

    /**
     * 开始预览
     *
     * @param callback 回调函数
     */
    void startPreview(Callback<View> callback);

    /**
     * 开始直播
     *
     * @param callback 回调函数
     */
    void startLive(Callback<View> callback);

    /**
     * 开始直播
     *
     * @param businessOptions 直播业务参数
     * @param callback        回调函数
     */
    void startLive(BusinessOptions businessOptions, Callback<View> callback);

    /**
     * 开始直播
     *
     * @param businessOptions 直播业务参数
     * @param pushUrl         手动指定推流Url
     * @param callback        回调函数
     */
    void startLive(BusinessOptions businessOptions, String pushUrl, Callback<View> callback);

    /**
     * 设置播放器/预览视图画面填充模式 {@link CanvasScale.Mode}
     *
     * @param mode 想要设置的模式
     */
    void setPreviewMode(@CanvasScale.Mode int mode);

    /**
     * 暂停直播
     */
    void pauseLive();

    /**
     * 恢复直播
     */
    void resumeLive();

    /**
     * 切换摄像头
     */
    void switchCamera();

    /**
     * 设置镜像预览
     *
     * @param mirror 是否镜像处理
     */
    void setPreviewMirror(boolean mirror);

    /**
     * 设置推流镜像
     *
     * @param mirror 是否镜像处理
     */
    void setPushMirror(boolean mirror);

    /**
     * 开启/关闭美颜
     *
     * @param isBeautyOpen 是否开启美颜
     */
    void setBeautyOn(boolean isBeautyOpen);

    /**
     * 设置美颜参数
     */
    @Deprecated
    void updateBeautyOptions(AliLiveBeautyOptions beautyOptions);

    /**
     * 设置静音推流
     *
     * @param mute 推流端设置静音
     */
    void setMutePush(boolean mute);

    /**
     * 结束直播
     *
     * @param callback 回调函数
     */
    void stopLive(Callback<Void> callback);

    /**
     * 结束直播
     *
     * @param destroyLive 是否销毁当前直播
     * @param callback    回调函数
     */
    void stopLive(boolean destroyLive, Callback<Void> callback);

    /**
     * 重新开始直播推流，适用于断网后恢复推流
     */
    void restartLive();

    /**
     * 切换闪光灯
     *
     * @param open 是否开启
     */
    void setFlash(boolean open);

    /**
     * @return 判断是否支持闪光灯
     */
    boolean isCameraSupportFlash();

    /**
     * 调整相机焦距
     */
    void setZoom(int zoom);

    /**
     * 获取当前相机焦距值
     *
     * @return
     */
    int getCurrentZoom();

    /**
     * 获取当前相机支持的最大焦躁
     *
     * @return
     */
    int getMaxZoom();

    /**
     * 手动对焦
     */
    void focusCameraAtAdjustedPoint(float x, float y, boolean autoFocus);

    /**
     * @return 美颜操作接口
     */
    BeautyInterface getBeautyInterface();

    /**
     * 开始录屏
     *
     * @param activity 当前所在的Activity
     * @param callback 回调函数
     */
    void startScreenCapture(final Activity activity, Callback<Void> callback);
}
