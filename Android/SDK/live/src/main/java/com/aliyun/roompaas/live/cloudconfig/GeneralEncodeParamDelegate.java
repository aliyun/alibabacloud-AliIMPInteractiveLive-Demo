package com.aliyun.roompaas.live.cloudconfig;

import android.support.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.TypeReference;
import com.alivc.live.pusher.AlivcAudioAACProfileEnum;
import com.alivc.live.pusher.AlivcAudioChannelEnum;
import com.alivc.live.pusher.AlivcAudioSampleRateEnum;
import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushConfig;
import com.alivc.live.pusher.AlivcQualityModeEnum;
import com.alivc.live.pusher.AlivcResolutionEnum;
import com.aliyun.roompaas.base.cloudconfig.CloudConfigCenter;
import com.aliyun.roompaas.base.cloudconfig.IKeys;
import com.aliyun.roompaas.base.cloudconfig.base.BaseCloudConfigDelegate;
import com.aliyun.roompaas.base.log.Logger;
import com.aliyun.roompaas.base.monitor.MonitorHubChannel;
import com.aliyun.roompaas.live.exposable.AliLiveMediaStreamOptions;

import java.lang.reflect.Type;

public class GeneralEncodeParamDelegate extends BaseCloudConfigDelegate implements ILiveMediaStrategy{
    public static final String TAG = "GeneralEncodeParamDelegate";
    private static final GeneralEncodeParamDelegate INSTANCE = new GeneralEncodeParamDelegate();

    public static GeneralEncodeParamDelegate getInstance() {
        return INSTANCE;
    }

    private GeneralEncodeParamDelegate() {
    }
    private LiveMediaParamBean liveMediaParamBean;

    @Override
    protected String singlePrimaryKey() {
        return IKeys.General.encodeParamAndroid;
    }

    @Override
    protected void singlePrimaryKeyResultFetched(String result) {
        try {
            Type type = new TypeReference<LiveMediaParamBean>() {
            }.getType();

            liveMediaParamBean = JSON.parseObject(result, type);
            Logger.i(TAG, "primaryKey: " + result);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void reset() {

    }

    @Override
    public AlivcLivePushConfig updateLivePushConfig(AlivcLivePushConfig config) {
        if (config == null) {
            config = new AlivcLivePushConfig();
        }
        AlivcResolutionEnum resolution = AliLiveMediaStreamOptions.getResolutionByCloudConfig(
                CloudConfigCenter.getInstance().maxPushStreamResolveByLive());
        config.setResolution(resolution);
        if (liveMediaParamBean != null) {
            AlivcQualityModeEnum qualityMode = liveMediaParamBean.getQualityMode();
            config.setQualityMode(qualityMode);
            switch (qualityMode) {
                case QM_CUSTOM:
                    config.setTargetVideoBitrate(liveMediaParamBean.getTargetVideoBitrate());
                    break;
                case QM_FLUENCY_FIRST:
                case QM_RESOLUTION_FIRST:
                    config.setEnableAutoResolution(true);
                    break;
            }
            config.setAudioChannels(liveMediaParamBean.getAudioChannels());
            config.setAudioSamepleRate(liveMediaParamBean.getAudioSampleRate());
            config.setAudioProfile(liveMediaParamBean.getAudioProfile());
            config.setCameraType(liveMediaParamBean.getCameraType());
            config.setVideoEncodeGop(liveMediaParamBean.getVideoEncodeGop());
            config.setFps(liveMediaParamBean.getFps());
            config.setVideoEncodeMode(liveMediaParamBean.getVideoEncodeMode());
        }

        MonitorHubChannel.videoEncoderInit("H264", "mediacodec",
                String.valueOf(AlivcResolutionEnum.GetResolutionWidth(resolution)),
                String.valueOf(AlivcResolutionEnum.GetResolutionHeight(resolution)),
                String.valueOf(liveMediaParamBean == null ? AlivcFpsEnum.FPS_20.getFps() : liveMediaParamBean.fps),
                String.valueOf(liveMediaParamBean == null ? 1500 : liveMediaParamBean.targetVideoBitrate),
                String.valueOf(liveMediaParamBean == null ? AliLiveMediaStreamOptions.AliLiveVideoEncodeGop.GOP_FOUR : liveMediaParamBean.videoEncodeGop),
                MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE,
                null);
        int audioSampleRate = (liveMediaParamBean == null ?
                AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_48000 : liveMediaParamBean.getAudioSampleRate()).getAudioSampleRate() / 1000;
        MonitorHubChannel.audioEncoderInit("AAC", "AAC",
                String.valueOf(liveMediaParamBean == null ? AlivcAudioAACProfileEnum.AAC_LC : liveMediaParamBean.getAudioProfile()),
                String.valueOf(audioSampleRate),
                String.valueOf(liveMediaParamBean == null ? AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO : liveMediaParamBean.audioChannels),
                String.valueOf(config.getAudioBitRate()),
                MonitorHubChannel.REPORT_EVENT_SUCCESS_CODE,
                null);
        return config;
    }

    @NonNull
    @Override
    public String toString() {
        return liveMediaParamBean == null ? "LiveMediaParamBean{ liveMediaParamBean = null }" :
                "LiveMediaParamBean{audioChannels=" + liveMediaParamBean.getAudioChannels() +
                ",audioSampleRate=" + liveMediaParamBean.getAudioSampleRate() +
                ",audioProfile=" + liveMediaParamBean.getAudioProfile() +
                ",cameraType=" + liveMediaParamBean.getCameraType() +
                ",videoEncodeGop=" + liveMediaParamBean.getVideoEncodeGop()+
                ",fps=" + liveMediaParamBean.getFps() +"" +
                ",targetVideoBitrate=" + liveMediaParamBean.getTargetVideoBitrate()+
                ",videoEncodeMode=" + liveMediaParamBean.getVideoEncodeMode() +
                ",pushMode=" + liveMediaParamBean.getQualityMode() + "}";
    }
}
