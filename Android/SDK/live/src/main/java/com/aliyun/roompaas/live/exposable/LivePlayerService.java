package com.aliyun.roompaas.live.exposable;

import android.view.SurfaceView;
import android.view.View;

import com.aliyun.roompaas.base.exposable.Callback;
import com.aliyun.roompaas.player.AliLivePlayerConfig;
import com.aliyun.roompaas.player.LivePlayerManager;
import com.aliyun.roompaas.player.exposable.CanvasScale;

/**
 * 直播播放服务
 *
 * @author puke
 * @version 2021/7/2
 */
public interface LivePlayerService {

    /**
     * 重新播放
     */
    void refreshPlay();

    /**
     * 继续播放
     */
    void resumePlay();

    /**
     * 暂停播放
     */
    void pausePlay();

    /**
     * 停止播放
     */
    void stopPlay();

    /**
     * 拉流端静音播放
     */
    void setMutePlay(boolean mute);

    /**
     * 尝试播放直播
     *
     * @param callback 回调函数
     */
    void tryPlay(Callback<View> callback);

    /**
     * 播放特定url的视频
     * @return 播放视图
     */
    SurfaceView playUrl(String url);

    /**
     * 获取最后一次尝试播放的url地址，忽略播放状态（是否成功播放未知）
     * @return 最后尝试播放的url
     */
    String getLastTriggerPlayUrl();

    /**
     * 设置播放器/预览视图画面填充模式 {@link CanvasScale.Mode}
     *
     * @param mode 想要设置的模式
     */
    void setViewContentMode(@CanvasScale.Mode int mode);

    /**
     * recommend to invoke this API when player is paused
     * @param internal ms
     */
    void updatePositionTimerInternalMs(long internal);

    /**
     * 设置utcTime回调，获取到utcTime
     * @param utcTimeListener
     */
    void setUtcTimeListener(LivePlayerManager.UtcTimeListener utcTimeListener);

    /**
     * 跳转到某个时间
     * @param position
     */
    void seekTo(long position);

    /**
     * 获取总时长
     */
    long getDuration();

    /**
     * 设置AliLivePlayerConfig 参数
     * @param playerConfig
     */
    void setPlayerConfig(AliLivePlayerConfig playerConfig);
}
