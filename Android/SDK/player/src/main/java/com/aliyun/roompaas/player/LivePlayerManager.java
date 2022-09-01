package com.aliyun.roompaas.player;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.aliyun.player.AliPlayer;
import com.aliyun.player.AliPlayerFactory;
import com.aliyun.player.IPlayer;
import com.aliyun.player.bean.ErrorCode;
import com.aliyun.player.bean.ErrorInfo;
import com.aliyun.player.bean.InfoBean;
import com.aliyun.player.bean.InfoCode;
import com.aliyun.player.nativeclass.MediaInfo;
import com.aliyun.player.nativeclass.PlayerConfig;
import com.aliyun.player.source.UrlSource;
import com.aliyun.roompaas.base.BaseManager;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.player.exposable.CanvasScale;
import com.cicada.player.utils.FrameInfo;

/**
 * 媒体拉流服务
 */
public class LivePlayerManager extends BaseManager<PlayerEvent> implements SurfaceHolder.Callback {
    private static final String TAG = "LivePlayerManager";
    private static final int DEF_MAX_BUFFER_DURATION = 30000;

    private String mPullUrl;
    private AliPlayer mAliPlayer;
    private final SurfaceView mSurfaceView;
    private Context mContext;

    private AliLivePlayerConfig playerConfig;
    private Callback callback;
    private IPlayer.OnRenderFrameCallback onRenderFrameCallback;

    public LivePlayerManager(Context context) {
        mContext = context;
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setTag(R.id.vpaas_view_with_player_manager, this);
        mSurfaceView.getHolder().addCallback(this);

        init();
    }

    private void init() {
        mAliPlayer = AliPlayerFactory.createAliPlayer(mContext);
        mAliPlayer.setAutoPlay(true);
        mAliPlayer.setScaleMode(IPlayer.ScaleMode.SCALE_ASPECT_FIT);

        mAliPlayer.setOnErrorListener(errorListener);
        mAliPlayer.setOnPreparedListener(preparedListener);
        mAliPlayer.setOnRenderingStartListener(renderingStartListener);
        mAliPlayer.setOnLoadingStatusListener(loadingStatusListener);
        mAliPlayer.setOnInfoListener(infoListener);
        mAliPlayer.setOnCompletionListener(completionListener);
        mAliPlayer.setOnVideoSizeChangedListener(onVideoSizeChangedListener);
        mAliPlayer.setOnVideoRenderedListener(onVideoRenderedListener);
        mAliPlayer.setOnStateChangedListener(onStateChangedListener);
        mAliPlayer.setOnRenderFrameCallback(onRenderFrameCallbackInner);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    /**
     * recommend to invoke this API when player is paused
     *
     * @param internal ms
     */
    public void updatePositionTimerInternalMs(long internal) {
        if (mAliPlayer == null || internal <= 0) {
            Logger.e(TAG, "updatePositionInternal end: not init or internal invalid:" + internal);
            return;
        }
        PlayerConfig config = mAliPlayer.getConfig();
        config.mPositionTimerIntervalMs = (int) internal;
        mAliPlayer.setConfig(config);
    }

    public void setViewContentMode(@CanvasScale.Mode int mode) {
        if (mAliPlayer != null) {
            mAliPlayer.setScaleMode(convertToPlayerScaleMode(mode));
        }
    }

    private static IPlayer.ScaleMode convertToPlayerScaleMode(@CanvasScale.Mode int mode) {
        switch (mode) {
            case CanvasScale.Mode.SCALE_FILL:
                return IPlayer.ScaleMode.SCALE_TO_FILL;
            case CanvasScale.Mode.ASPECT_FIT:
                return IPlayer.ScaleMode.SCALE_ASPECT_FIT;
            default:
            case CanvasScale.Mode.ASPECT_FILL:
                return IPlayer.ScaleMode.SCALE_ASPECT_FILL;
        }
    }

    /**
     * 开启拉流
     *
     * @param url
     */
    public SurfaceView startPlay(@NonNull String url) {
        Logger.i(TAG, "startPlay, url :" + url);
        mSurfaceView.setVisibility(View.VISIBLE);
        this.mPullUrl = url;
        if (TextUtils.isEmpty(url)) {
            Logger.e(TAG, "startPlay url must not null");
            return mSurfaceView;
        }
        UrlSource source = new UrlSource();
        source.setUri(mPullUrl);
        if (mAliPlayer != null) {
            mAliPlayer.setDataSource(source);
            mAliPlayer.prepare();
        }
        return mSurfaceView;
    }

    public String getLastTriggerPlayUrl() {
        return mPullUrl;
    }

    public void preparePlay() {
        if (mAliPlayer != null) {
            mAliPlayer.prepare();
        }
    }

    /**
     * 停止拉流/播放
     */
    public void stopPlay() {
        if (mAliPlayer != null) {
            mAliPlayer.stop();
        }
    }

    /**
     * 销毁
     */
    public void destroy() {
        super.destroy();
        if (mAliPlayer != null) {
            stopPlay();
            mAliPlayer.setSurface(null);
            mAliPlayer.release();
            mAliPlayer = null;
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        if (mAliPlayer != null) {
            mAliPlayer.setSurface(surfaceHolder.getSurface());
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        if (mAliPlayer != null) {
            mAliPlayer.surfaceChanged();
        }
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {
        if (mAliPlayer != null) {
            mAliPlayer.setSurface(null);
        }
    }

    /**
     * 开始播放
     */
    public void startPlay() {
        if (mAliPlayer != null) {
            mAliPlayer.start();
        }
    }

    /**
     * 暂停播放
     */
    public void pausePlay() {
        if (mAliPlayer != null) {
            mAliPlayer.pause();
        }
    }

    /**
     * 静音播放
     *
     * @param mute
     */
    public void setMute(boolean mute) {
        if (mAliPlayer != null) {
            mAliPlayer.setMute(mute);
        }
    }

    /**
     * 设置utcTime回调，获取utcTime
     */
    public void setUtcTimeListener(UtcTimeListener listener) {
        utcTimeListener = listener;
    }

    public UtcTimeListener utcTimeListener;

    public interface UtcTimeListener {
        /**
         * 回调拿到utcTime
         *
         * @param utcTime
         */
        void onUtcTime(long utcTime);
    }

    /**
     * 快进到相应的位置
     *
     * @param position
     */
    public void seekTo(long position) {
        if (mAliPlayer != null) {
            mAliPlayer.seekTo(position);
        }
    }

    /**
     * 获取总时长
     *
     * @return
     */
    public long getDuration() {
        if (mAliPlayer == null) {
            return -1;
        }

        return mAliPlayer.getDuration();
    }

    /**
     * 获取MediaInfo
     *
     * @return
     */
    public MediaInfo getMediaInfo() {
        if (mAliPlayer != null) {
            return mAliPlayer.getMediaInfo();
        }

        return null;
    }

    /**
     * 设置播放器相关配置参数
     *
     * @param playerConfig
     */
    public void setPlayerConfig(AliLivePlayerConfig playerConfig) {
        if (playerConfig == null) {
            Logger.e(TAG, "playerConfig params is null.");
            return;
        }
        if (mAliPlayer == null) {
            Logger.e(TAG, "aliPlayer not init");
            return;
        }
        this.playerConfig = playerConfig;

        PlayerConfig config = mAliPlayer.getConfig();
        if (config != null) {
            config.mNetworkRetryCount = playerConfig.networkRetryCount;
            config.mNetworkTimeout = playerConfig.networkTimeout;
            config.mDisableAudio = playerConfig.disableAudio;
            config.mDisableVideo = playerConfig.disableVideo;
            config.mMaxBufferDuration = DEF_MAX_BUFFER_DURATION;

            mAliPlayer.setConfig(config);
        }
    }

    /**
     * 获取当前播放参数
     *
     * @return
     */
    public AliLivePlayerConfig getPlayerConfig() {
        return playerConfig;
    }

    /**
     * 获取视频宽度
     * @return
     */
    public int getVideoWidth() {
        if (mAliPlayer == null) {
            Logger.e(TAG, "aliPlayer not init");
            return 0;
        }
        return mAliPlayer.getVideoWidth();
    }

    public String getRenderFPS() {
        return mAliPlayer.getOption(IPlayer.Option.RenderFPS).toString();
    }

    /**
     * 获取视频高度
     * @return
     */
    public int getVideoHeight() {
        if (mAliPlayer == null) {
            Logger.e(TAG, "aliPlayer not init");
            return 0;
        }
        return mAliPlayer.getVideoHeight();
    }

    IPlayer.OnPreparedListener preparedListener = new IPlayer.OnPreparedListener() {
        @Override
        public void onPrepared() {
            mAliPlayer.start();
            Logger.i(TAG, "onPrepared");
            postEvent(PlayerEvent.PLAYER_PREPARED);
        }
    };

    IPlayer.OnRenderingStartListener renderingStartListener = new IPlayer.OnRenderingStartListener() {
        @Override
        public void onRenderingStart() {
            Logger.i(TAG, "onRenderingStart");
            postEvent(PlayerEvent.RENDER_START);
        }
    };

    IPlayer.OnLoadingStatusListener loadingStatusListener = new IPlayer.OnLoadingStatusListener() {
        @Override
        public void onLoadingBegin() {
            Logger.i(TAG, "onLoadingBegin");
            postEvent(PlayerEvent.PLAYER_LOADING_BEGIN);
        }

        @Override
        public void onLoadingProgress(int i, float v) {
            Logger.i(TAG, "onLoadingProgress " + i);
            postEvent(PlayerEvent.PLAYER_LOADING_PROGRESS, i);
        }

        @Override
        public void onLoadingEnd() {
            Logger.i(TAG, "onLoadingEnd");
            postEvent(PlayerEvent.PLAYER_LOADING_END);
        }
    };

    IPlayer.OnInfoListener infoListener = new IPlayer.OnInfoListener() {
        @Override
        public void onInfo(InfoBean infoBean) {
            InfoCode infoCode = infoBean.getCode();
            if ((infoCode != InfoCode.BufferedPosition
                    && infoCode != InfoCode.CurrentPosition)
                    && infoCode != InfoCode.CurrentDownloadSpeed
                    && infoCode != InfoCode.DirectComponentMSG) {
                // 过滤高频的日志输出
                Logger.i(TAG, "onInfo: " + infoCode + ", value: " + infoBean.getExtraValue());
            }
            if (infoCode == InfoCode.UtcTime) {
                if (utcTimeListener != null) {
                    utcTimeListener.onUtcTime(infoBean.getExtraValue());
                }
            } else if (infoCode == InfoCode.BufferedPosition) {
                postEvent(PlayerEvent.BUFFERED_POSITION, infoBean.getExtraValue());
            } else if (infoCode == InfoCode.CurrentPosition) {
                postEvent(PlayerEvent.CURRENT_POSITION, infoBean.getExtraValue());
            } else if (infoCode == InfoCode.CurrentDownloadSpeed) {
                long kb = infoBean.getExtraValue() / 1024;
                postEvent(PlayerEvent.PLAYER_DOWNLOAD_SPEED_CHANGE, kb);
            }
        }
    };

    IPlayer.OnCompletionListener completionListener = new IPlayer.OnCompletionListener() {
        @Override
        public void onCompletion() {
            postEvent(PlayerEvent.PLAYER_END);
        }
    };

    IPlayer.OnErrorListener errorListener = new IPlayer.OnErrorListener() {
        @Override
        public void onError(ErrorInfo errorInfo) {
            stopPlay();
            Logger.e(TAG, "onError " + errorInfo.getCode() + ", msg " + errorInfo.getMsg());
            postEvent(PlayerEvent.PLAYER_ERROR_RAW, errorInfo);
            if (needLowDelay()) {
                if (callback != null) {
                    callback.onRtsPlayerError();
                }
                return;
            } else if (errorInfo.getCode() == ErrorCode.ERROR_NETWORK_HTTP_RANGE) {
                if (callback != null) {
                    callback.onPlayerHttpRangeError();
                }
                return;
            }
            postEvent(PlayerEvent.PLAYER_ERROR, errorInfo);
        }
    };

    IPlayer.OnVideoSizeChangedListener onVideoSizeChangedListener = new IPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(int width, int height) {
            postEvent(PlayerEvent.PLAYER_VIDEO_SIZE, new VideoSize(width, height));
        }
    };

    IPlayer.OnVideoRenderedListener onVideoRenderedListener = new IPlayer.OnVideoRenderedListener() {
        @Override
        public void onVideoRendered(long timeMs, long pts) {
            postEvent(PlayerEvent.PLAYER_VIDEO_RENDERED);
        }
    };

    IPlayer.OnStateChangedListener onStateChangedListener = new IPlayer.OnStateChangedListener() {
        @Override
        public void onStateChanged(int newStatus) {
            Logger.i(TAG, "onStateChanged newStatus " + newStatus);
            postEvent(PlayerEvent.PLAYER_STATUS_CHANGE, newStatus);
        }
    };

    IPlayer.OnRenderFrameCallback onRenderFrameCallbackInner = new IPlayer.OnRenderFrameCallback() {
        @Override
        public boolean onRenderFrame(FrameInfo frameInfo) {
            return onRenderFrameCallback != null && onRenderFrameCallback.
                    onRenderFrame(frameInfo);
        }
    };

    public void setOnRenderFrameCallback(IPlayer.OnRenderFrameCallback onRenderFrameCallback) {
        this.onRenderFrameCallback = onRenderFrameCallback;
    }

    /**
     * @return 是否需要打开RTS低延时播放
     */
    public boolean needLowDelay() {
        return playerConfig == null || playerConfig.lowDelay;
    }

    public interface Callback {
        void onRtsPlayerError();

        void onPlayerHttpRangeError();
    }
    
    public static class VideoSize {
        public final int width;
        public final int height;

        public VideoSize(int width, int height) {
            this.width = width;
            this.height = height;
        }
    }
}
