package com.aliyun.roompaas.live.exposable;

import android.support.annotation.Nullable;

import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.roompaas.live.LiveEvent;
import com.aliyun.roompaas.live.exposable.event.LiveCommonEvent;

import java.util.Map;

/**
 * @author puke
 * @version 2021/7/2
 */
public interface LiveEventHandler {

    /**
     * 创建直播
     */
    void onLiveCreated(LiveCommonEvent event);

    /**
     * 开始直播
     */
    void onLiveStarted(LiveCommonEvent event);

    /**
     * 结束直播
     */
    void onLiveStopped(LiveCommonEvent event);

    /**
     * 直播流开始
     */
    void onLiveStreamStarted(LiveCommonEvent event);

    /**
     * 直播流结束
     */
    void onLiveStreamStopped(LiveCommonEvent event);

    /**
     * 推流事件回调
     *
     * @param event 事件
     */
    void onPusherEvent(LiveEvent event, @Nullable Map<String, Object> extras);

    /**
     * 推流事件回调 (该回调不带参数, 推荐使用{@link #onPusherEvent(LiveEvent, Map)})
     *
     * @param event 事件
     */
    @Deprecated
    void onPusherEvent(LiveEvent event);

    /**
     * 渲染开始
     */
    void onRenderStart();

    /**
     * load开始
     */
    void onLoadingBegin();

    /**
     * load进度，0~100
     */
    void onLoadingProgress(int progress);

    /**
     * load结束
     */
    void onLoadingEnd();

    /**
     * 播放器出错
     *
     * @param errorInfo 原始错误信息
     */
    void onPlayerError(ErrorInfo errorInfo);

    /**
     * 播放器出错
     */
    void onPlayerError();

    /**
     * 播放器准备完毕
     */
    void onPrepared();

    /**
     * 播放结束
     */
    void onPlayerEnd();

    /**
     * 播放器当前进度
     */
    void onPlayerCurrentPosition(long position);

    /**
     * 播放器缓冲进度
     */
    void onPlayerBufferedPosition(long position);

    /**
     * 播放器生命周期
     *
     * @param status
     */
    void onPlayerStatusChange(int status);

    /**
     * 播放器画面尺寸变化回调
     *
     * @param width  画面宽度
     * @param height 画面高度
     */
    void onPlayerVideoSizeChanged(int width, int height);

    /**
     * 下载速度变化回调
     *
     * @param kb 下载速度, 单位 kb
     */
    void onPlayerDownloadSpeedChanged(long kb);
}
