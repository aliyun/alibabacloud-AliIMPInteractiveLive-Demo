package com.aliyun.roompaas.live;

/**
 * @author puke
 * @version 2021/6/24
 */
public enum LiveEvent {

    /**
     * 首帧预览 (仅主播端收到)
     */
    FIRST_FRAME_PREVIEWED,

    /**
     * 首帧推流 (仅主播端收到)
     */
    FIRST_FRAME_PUSHED,

    /**
     * 预览开始 (仅主播端收到)
     */
    PREVIEW_STARTED,

    /**
     * 预览结束 (仅主播端收到)
     */
    PREVIEW_STOPPED,

    /**
     * 推流开始 (仅主播端收到)
     */
    PUSH_STARTED,

    /**
     * 推流暂停 (仅主播端收到)
     */
    PUSH_PAUSED,

    /**
     * 推流暂停开始 (仅主播端收到)
     */
    PUSH_RESUMED,

    /**
     * 推流结束 (仅主播端收到)
     */
    PUSH_STOPPED,

    /**
     * 网络连接失败 (仅主播端收到)
     */
    CONNECTION_FAIL,

    /**
     * 网络断开 (仅主播端收到)
     */
    CONNECTION_LOST,

    /**
     * 网络状态不佳 (仅主播端收到)
     */
    NETWORK_POOR,

    /**
     * 网络状态恢复 (仅主播端收到)
     */
    NETWORK_RECOVERY,

    /**
     * 渲染开始 (仅主播端收到)
     */
    RENDER_START,

    /**
     * 播放器出错 (仅主播端收到)
     */
    PLAYER_ERROR,

    /**
     * 重连开始 (仅主播端收到)
     */
    RECONNECT_START,

    /**
     * 重连成功 (仅主播端收到)
     */
    RECONNECT_SUCCESS,

    /**
     * 重连失败 (仅主播端收到)
     */
    RECONNECT_FAIL,

    /**
     * 推流码率变化 (仅主播端收到)
     */
    UPLOAD_BITRATE_UPDATED,
}
