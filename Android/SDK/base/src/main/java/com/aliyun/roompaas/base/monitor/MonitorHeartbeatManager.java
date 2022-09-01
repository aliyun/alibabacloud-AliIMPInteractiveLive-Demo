package com.aliyun.roompaas.base.monitor;

import com.alibaba.dingpaas.monitorhub.MonitorhubField;
import com.alibaba.dingpaas.monitorhub.MonitorhubMetric;

import java.util.HashMap;

/**
 * 心跳数据
 */
public class MonitorHeartbeatManager {

    private static MonitorHeartbeatManager mInstance;

    public static MonitorHeartbeatManager getInstance() {
        if (mInstance == null) {
            mInstance = new MonitorHeartbeatManager();
        }

        return mInstance;
    }

    private final HashMap<String, String> heartbeatData = new HashMap<>();
    public HashMap<String, String> getHeartbeatData() {
        return heartbeatData;
    }

    // region 通用数据
    public void setStatus(String status) {
        heartbeatData.put(MonitorhubField.MFFIELD_COMMON_STATUS, status);
    }

    public void setSystemCpu(String systemCpu) {
        heartbeatData.put(MonitorhubField.MFFIELD_SYSINFO_CPU_USAGE_SYS,systemCpu);
    }

    public void setAppCpu(String cpu) {
        heartbeatData.put(MonitorhubField.MFFIELD_SYSINFO_CPU_USAGE_PROC, cpu);
    }

    public void setAppMem(String mem) {
        heartbeatData.put(MonitorhubField.MFFIELD_SYSINFO_APP_MEM, mem);
    }
    // endregion

    // region 下行数据
    public void setLiveUrl(String liveUrl) {
        heartbeatData.put(MonitorhubField.MFFIELD_COMMON_LIVE_URL, liveUrl);
    }

    public void setProtoType(String protoType) {
        heartbeatData.put(MonitorhubField.MFFIELD_COMMON_PROTO_TYPE, protoType);
    }

    public void setPlayType(String type) {
        heartbeatData.put(MonitorhubField.MFFIELD_COMMON_PLAY_TYPE, type);
    }

    public void setContentId(String contentId) {
        heartbeatData.put(MonitorhubField.MFFIELD_COMMON_CONTENT_ID, contentId);
    }

    public void setRemoteVideoWidth(String videoWidth) {
        heartbeatData.put(MonitorhubField.MFFIELD_LIVE_AUDIENCE_HEART_REMOTE_VIDEO_WIDTH, videoWidth);
    }

    public void setRemoteVideoHeight(String videoHeight) {
        heartbeatData.put(MonitorhubField.MFFIELD_LIVE_AUDIENCE_HEART_REMOTE_VIDEO_HEIGHT, videoHeight);
    }

    public void setRemoteVideoRenderFrames(String videoRenderFrames) {
        heartbeatData.put(MonitorhubField.MFFIELD_LIVE_AUDIENCE_HEART_REMOTE_VIDEO_RENDER_FRAMES, videoRenderFrames);
    }
    // endregion

    // region 麦上数据
    public void setCameraWidth(boolean isOwner, String width) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_CAMERA_WIDTH :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_WIDTH, width);

    }
    public void setCameraHeight(boolean isOwner, String width) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_CAMERA_HEIGHT :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_HEIGHT, width);

    }

    public void setCameraSentBitrate(boolean isOwner, String bitrate) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_CAMERA_SENT_BITRATE :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_SENT_BITRATE, bitrate);
    }

    public void setCameraSentFPS(boolean isOwner, String fps) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_CAMERA_SENT_FPS :
            MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_SENT_FPS , fps);
    }

    public void setCameraEncodeFPS(boolean isOwner, String fps) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_CAMERA_ENCODE_FPS :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_ENCODE_FPS, fps);
    }

    public void setCameraCaptureFps(String fps) {
        heartbeatData.put(MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_CAMERA_CAPTURE_FPS, fps);
    }

    public void setScreenWidth(boolean isOwner, String width) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_SCREEN_WIDTH :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_WIDTH, width);
    }

    public void setScreenHeight(boolean isOwner, String height) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_SCREEN_HEIGHT :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_HEIGHT, height);
    }

    public void setScreenSentBitrate(boolean isOwner, String bitrate) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_SCREEN_SENT_BITRATE :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_SENT_BITRATE, bitrate);

    }

    public void setScreenSentFPS(boolean isOwner, String fps) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_SCREEN_SENT_FPS :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_SENT_FPS, fps);
    }

    public void setScreenEncodeFPS(boolean isOwner, String fps) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_SCREEN_ENCODE_FPS :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_ENCODE_FPS, fps);
    }

    public void setScreenCaptureFPS(String fps) {
        heartbeatData.put(MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_SCREEN_CAPTURE_FPS, fps);
    }

    public void setAudioSentBitrate(boolean isOwner, String bitrate) {
        heartbeatData.put(isOwner ? MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_AUDIO_SEND_BITRATE :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_AUDIO_SEND_BITRATE, bitrate);
    }

    public void setAudioEncodeBitrate(boolean isOwner, String bitrate) {
        heartbeatData.put(isOwner ?
                MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_AUDIO_ENCODE_BITRATE :
                MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_AUDIO_ENCODE_BITRATE, bitrate);
    }

    public void setAudioSentFps(String fps) {
        heartbeatData.put(MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_AUDIO_SEND_FPS,
                fps);
    }

    public void setAudioVolume(String volume) {
        heartbeatData.put(MonitorhubField.MFFIELD_LIVE_ANCHOR_HEART_AUDIO_VOLUME, volume);
    }

    public void setAudioDesktopAudioByTeacher(String audio) {
        heartbeatData.put(MonitorhubField.MFFIELD_CLASS_TEACHER_HEART_AUDIO_DESKTOP_AUDIO, audio);
    }

    public void setAudioDesktopAudioByStudent(String audio) {
        heartbeatData.put(MonitorhubField.MFFIELD_CLASS_STUDENT_HEART_AUDIO_DESKTOP_AUDIO, audio);
    }
    // endregion
}
