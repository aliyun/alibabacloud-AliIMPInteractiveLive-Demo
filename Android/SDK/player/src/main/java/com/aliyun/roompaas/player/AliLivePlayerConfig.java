package com.aliyun.roompaas.player;

/**
 * 播放器相关配置参数
 */
public class AliLivePlayerConfig {

    /**
     * 开启RTS播放
     */
    public final boolean lowDelay;

    /**
     * 网络重试次数，默认为3
     */
    public final int networkRetryCount;

    /**
     * 网络超时时间
     */
    public final int networkTimeout;

    /**
     * 禁用视频
     */
    public final boolean disableVideo;

    /**
     * 禁用音频
     */
    public final boolean disableAudio;

    public AliLivePlayerConfig(Builder builder) {
        lowDelay = builder.lowDelay;
        networkRetryCount = builder.networkRetryCount;
        networkTimeout = builder.networkTimeout;
        disableAudio = builder.disableAudio;
        disableVideo = builder.disableVideo;
    }

    public static final class Builder {
        public boolean lowDelay = true;
        public int networkRetryCount = 3;
        public int networkTimeout = 5000;
        public boolean disableVideo = false;
        public boolean disableAudio = false;

        public Builder() {

        }

        public Builder setLowDelay(boolean lowDelay) {
            this.lowDelay = lowDelay;
            return this;
        }

        public Builder setNetworkRetryCount(int networkRetryCount) {
            this.networkRetryCount = networkRetryCount;
            return this;
        }

        public Builder setNetworkTimeout(int networkTimeout) {
            this.networkTimeout = networkTimeout;
            return this;
        }

        public Builder setDisableVideo(boolean disableVideo) {
            this.disableVideo = disableVideo;
            return this;
        }

        public Builder setDisableAudio(boolean disableAudio) {
            this.disableAudio = disableAudio;
            return this;
        }

        public AliLivePlayerConfig build() {
            return new AliLivePlayerConfig(this);
        }
    }
}
