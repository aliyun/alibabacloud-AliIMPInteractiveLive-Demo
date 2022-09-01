package com.aliyun.roompaas.live.exposable;

import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushCameraTypeEnum;
import com.alivc.live.pusher.AlivcPreviewDisplayMode;
import com.alivc.live.pusher.AlivcPreviewOrientationEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.alivc.live.pusher.AlivcVideoEncodeGopEnum;
import com.aliyun.roompaas.player.exposable.CanvasScale;

public class AliLiveMediaStreamOptions {

    public AliLiveMediaStreamOptions() {

    }

    // 纯视频推流
    public boolean isVideoOnly = false;
    // 纯音频推流
    public boolean isAudioOnly = false;
    // 视频编码码率
    @Deprecated
    public int videoBitrate = 1500;
    // 视频帧率
    @Deprecated
    public AlivcFpsEnum fps = AlivcFpsEnum.FPS_20;
    // preview 展示模式
    public AliLivePreviewDisplayMode previewDisplayMode = AliLivePreviewDisplayMode.PUSHER_PREVIEW_ASPECT_FILL;
    // 预览横竖屏
    public AliLivePreviewOrientation previewOrientation = AliLivePreviewOrientation.ORIENTATION_PORTRAIT;
    // 视频编码Gop
    @Deprecated
    public AliLiveVideoEncodeGop videoEncodeGop = AliLiveVideoEncodeGop.GOP_FOUR;
    // 视频编码
    @Deprecated
    public AliLiveVideoEncode videoEncode = AliLiveVideoEncode.ENCODE_MODE_HARD;
    // 摄像头
    @Deprecated
    public AliLivePushCameraType cameraType = AliLivePushCameraType.CAMERA_TYPE_FRONT;
    // 分辨率
    @Deprecated
    public AliLiveResolution resolution = AliLiveResolution.RESOLUTION_1080P;

    public AlivcResolutionEnum getResolution() {
        if (resolution != null) {
            switch (resolution) {
                case RESOLUTION_180P:
                    return AlivcResolutionEnum.RESOLUTION_180P;
                case RESOLUTION_240P:
                    return AlivcResolutionEnum.RESOLUTION_240P;
                case RESOLUTION_360P:
                    return AlivcResolutionEnum.RESOLUTION_360P;
                case RESOLUTION_480P:
                    return AlivcResolutionEnum.RESOLUTION_480P;
                case RESOLUTION_540P:
                    return AlivcResolutionEnum.RESOLUTION_540P;
                case RESOLUTION_720P:
                    return AlivcResolutionEnum.RESOLUTION_720P;
                case RESOLUTION_1080P:
                    return AlivcResolutionEnum.RESOLUTION_1080P;
            }
        }
        return AlivcResolutionEnum.RESOLUTION_720P;
    }

    public static AlivcResolutionEnum getResolutionByCloudConfig(String resolution) {
        if ("720P".equalsIgnoreCase(resolution)) {
            return AlivcResolutionEnum.RESOLUTION_720P;
        } else if ("1080P".equalsIgnoreCase(resolution)) {
            return AlivcResolutionEnum.RESOLUTION_1080P;
        }
        return AlivcResolutionEnum.RESOLUTION_720P;
    }

    public AlivcLivePushCameraTypeEnum getCameraType() {
        if (cameraType != null) {
            switch (cameraType) {
                case CAMERA_TYPE_BACK:
                    return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
                case CAMERA_TYPE_FRONT:
                    return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;
            }
        }
        return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;
    }

    public AlivcEncodeModeEnum getEncodeMode() {
        if (videoEncode != null) {
            switch (videoEncode) {
                case ENCODE_MODE_HARD:
                    return AlivcEncodeModeEnum.Encode_MODE_HARD;
                case ENCODE_MODE_SOFT:
                    return AlivcEncodeModeEnum.Encode_MODE_SOFT;
            }
        }
        return AlivcEncodeModeEnum.Encode_MODE_HARD;
    }

    public AlivcVideoEncodeGopEnum getEncodeGop() {
        if (videoEncodeGop != null) {
            switch (videoEncodeGop) {
                case GOP_ONE:
                    return AlivcVideoEncodeGopEnum.GOP_ONE;
                case GOP_TWO:
                    return AlivcVideoEncodeGopEnum.GOP_TWO;
                case GOP_THREE:
                    return AlivcVideoEncodeGopEnum.GOP_THREE;
                case GOP_FOUR:
                    return AlivcVideoEncodeGopEnum.GOP_FOUR;
                case GOP_FIVE:
                    return AlivcVideoEncodeGopEnum.GOP_FIVE;
            }
        }
        return AlivcVideoEncodeGopEnum.GOP_TWO;
    }

    public AlivcPreviewOrientationEnum getPreviewOrientation() {
        if (previewOrientation != null) {
            switch (previewOrientation) {
                case ORIENTATION_PORTRAIT:
                    return AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;
                case ORIENTATION_LANDSCAPE_HOME_LEFT:
                    return AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_LEFT;
                case ORIENTATION_LANDSCAPE_HOME_RIGHT:
                    return AlivcPreviewOrientationEnum.ORIENTATION_LANDSCAPE_HOME_RIGHT;
            }
        }
        return AlivcPreviewOrientationEnum.ORIENTATION_PORTRAIT;
    }

    public AlivcPreviewDisplayMode getPreviewDisplayMode() {
        if (previewDisplayMode != null) {
            switch (previewDisplayMode) {
                case PUSHER_PREVIEW_ASPECT_FILL:
                    return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL;
                case LIVE_PUSHER_PREVIEW_ASPECT_FIT:
                    return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT;
                case LIVE_PUSHER_PREVIEW_SCALE_FILL:
                    return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL;
            }
        }
        return AlivcPreviewDisplayMode.ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL;
    }

    public static AliLivePreviewDisplayMode getPreviewDisplayMode(int showMode) {
        switch (showMode) {
            case CanvasScale.Mode.ASPECT_FILL:
                return AliLivePreviewDisplayMode.PUSHER_PREVIEW_ASPECT_FILL;
            case CanvasScale.Mode.ASPECT_FIT:
                return AliLivePreviewDisplayMode.LIVE_PUSHER_PREVIEW_ASPECT_FIT;
            case CanvasScale.Mode.SCALE_FILL:
                return AliLivePreviewDisplayMode.LIVE_PUSHER_PREVIEW_SCALE_FILL;
        }
        return AliLivePreviewDisplayMode.PUSHER_PREVIEW_ASPECT_FILL;
    }

        public enum AliLiveResolution {
        RESOLUTION_180P,
        RESOLUTION_240P,
        RESOLUTION_360P,
        RESOLUTION_480P,
        RESOLUTION_540P,
        RESOLUTION_720P,
        RESOLUTION_1080P,
    }
    public enum  AliLivePushCameraType {
        CAMERA_TYPE_BACK,
        CAMERA_TYPE_FRONT
    }

    public enum  AliLiveVideoEncode {
        ENCODE_MODE_HARD,
        ENCODE_MODE_SOFT
    }

    public enum AliLiveVideoEncodeGop {
        GOP_ONE,
        GOP_TWO,
        GOP_THREE,
        GOP_FOUR,
        GOP_FIVE
    }

    public enum AliLivePreviewOrientation {
        // 竖屏
        ORIENTATION_PORTRAIT,
        // 横，Home在右
        ORIENTATION_LANDSCAPE_HOME_RIGHT,
        // 横，Home在左
        ORIENTATION_LANDSCAPE_HOME_LEFT,
    }

    public enum AliLivePreviewDisplayMode {
        // 铺满窗口，视频比例和窗口比例不一致时预览会有变形
        LIVE_PUSHER_PREVIEW_SCALE_FILL,
        // 保持视频比例，比例与窗口比例不一致时有黑边
        LIVE_PUSHER_PREVIEW_ASPECT_FIT,
        // 剪切视频以适配窗口比例，视频比例和窗口比例不一致时会裁剪视频
        PUSHER_PREVIEW_ASPECT_FILL
    }
}
