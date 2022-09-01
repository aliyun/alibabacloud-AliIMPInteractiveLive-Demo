package com.aliyun.roompaas.live.cloudconfig;

import com.alivc.live.pusher.AlivcAudioAACProfileEnum;
import com.alivc.live.pusher.AlivcAudioChannelEnum;
import com.alivc.live.pusher.AlivcAudioSampleRateEnum;
import com.alivc.live.pusher.AlivcEncodeModeEnum;
import com.alivc.live.pusher.AlivcFpsEnum;
import com.alivc.live.pusher.AlivcLivePushCameraTypeEnum;
import com.alivc.live.pusher.AlivcQualityModeEnum;
import com.alivc.live.pusher.AlivcVideoEncodeGopEnum;

public class LiveMediaParamBean {
   public int audioChannels;
   public int audioSampleRate;
   public int audioProfile;
   public int cameraType;
   public int videoEncodeGop;
   public int fps;
   public int targetVideoBitrate;
   public int videoEncodeMode;
   // Puhser4.2.1版本无编码API
   public int videoEncodeType;
   public String pushMode;


   public AlivcAudioChannelEnum getAudioChannels() {
      if (audioChannels == AlivcAudioChannelEnum.AUDIO_CHANNEL_ONE.getChannelCount()) {
         return AlivcAudioChannelEnum.AUDIO_CHANNEL_ONE;
      } else if (audioChannels == AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO.getChannelCount()) {
         return AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO;
      }
      return AlivcAudioChannelEnum.AUDIO_CHANNEL_TWO;
   }

   public AlivcAudioSampleRateEnum getAudioSampleRate() {
      if (audioSampleRate == AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_16000.getAudioSampleRate()) {
         return AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_16000;
      } else if (audioSampleRate == AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_32000.getAudioSampleRate()) {
         return AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_32000;
      } else if (audioSampleRate == AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_48000.getAudioSampleRate()) {
         return AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_48000;
      }
      return AlivcAudioSampleRateEnum.AUDIO_SAMPLE_RATE_48000;
   }

   public AlivcAudioAACProfileEnum getAudioProfile() {
      if (audioProfile == AlivcAudioAACProfileEnum.AAC_LC.getAudioProfile()) {
         return AlivcAudioAACProfileEnum.AAC_LC;
      } else if (audioProfile == AlivcAudioAACProfileEnum.HE_AAC.getAudioProfile()) {
         return AlivcAudioAACProfileEnum.HE_AAC;
      } else if (audioProfile == AlivcAudioAACProfileEnum.HE_AAC_v2.getAudioProfile()) {
         return AlivcAudioAACProfileEnum.HE_AAC_v2;
      } else if (audioProfile == AlivcAudioAACProfileEnum.AAC_LD.getAudioProfile()) {
         return AlivcAudioAACProfileEnum.AAC_LD;
      }
      return AlivcAudioAACProfileEnum.AAC_LC;
   }

   public AlivcLivePushCameraTypeEnum getCameraType() {
      if (cameraType == AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK.getCameraId()) {
         return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_BACK;
      } else if (cameraType == AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT.getCameraId()){
         return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;
      }
      return AlivcLivePushCameraTypeEnum.CAMERA_TYPE_FRONT;
   }

   public AlivcVideoEncodeGopEnum getVideoEncodeGop() {
      if (videoEncodeGop == AlivcVideoEncodeGopEnum.GOP_ONE.getGop()) {
         return AlivcVideoEncodeGopEnum.GOP_ONE;
      } else if (videoEncodeGop == AlivcVideoEncodeGopEnum.GOP_TWO.getGop()) {
         return AlivcVideoEncodeGopEnum.GOP_TWO;
      } else if (videoEncodeGop == AlivcVideoEncodeGopEnum.GOP_THREE.getGop()) {
         return AlivcVideoEncodeGopEnum.GOP_THREE;
      } else if (videoEncodeGop == AlivcVideoEncodeGopEnum.GOP_FOUR.getGop()) {
         return AlivcVideoEncodeGopEnum.GOP_FOUR;
      } else if (videoEncodeGop == AlivcVideoEncodeGopEnum.GOP_FIVE.getGop()) {
         return AlivcVideoEncodeGopEnum.GOP_FIVE;
      }
      return AlivcVideoEncodeGopEnum.GOP_TWO;
   }

   public AlivcFpsEnum getFps() {
      if (fps == AlivcFpsEnum.FPS_8.getFps()) {
         return AlivcFpsEnum.FPS_8;
      } else if (fps == AlivcFpsEnum.FPS_10.getFps()) {
         return AlivcFpsEnum.FPS_10;
      } else if (fps == AlivcFpsEnum.FPS_12.getFps()) {
         return AlivcFpsEnum.FPS_12;
      } else if (fps == AlivcFpsEnum.FPS_15.getFps()) {
         return AlivcFpsEnum.FPS_15;
      } else if (fps == AlivcFpsEnum.FPS_20.getFps()) {
         return AlivcFpsEnum.FPS_20;
      } else if (fps == AlivcFpsEnum.FPS_25.getFps()) {
         return AlivcFpsEnum.FPS_25;
      } else if (fps == AlivcFpsEnum.FPS_30.getFps()) {
         return AlivcFpsEnum.FPS_30;
      }

      return AlivcFpsEnum.FPS_20;
   }

   public int getTargetVideoBitrate() {
      return targetVideoBitrate == 0 ? 1500 : targetVideoBitrate;
   }

   public AlivcEncodeModeEnum getVideoEncodeMode() {
      if (videoEncodeMode == AlivcEncodeModeEnum.Encode_MODE_HARD.ordinal()) {
         return AlivcEncodeModeEnum.Encode_MODE_HARD;
      } else if (videoEncodeMode == AlivcEncodeModeEnum.Encode_MODE_SOFT.ordinal()) {
         return AlivcEncodeModeEnum.Encode_MODE_SOFT;
      }
      return AlivcEncodeModeEnum.Encode_MODE_HARD;
   }

   public AlivcQualityModeEnum getQualityMode() {
      if (pushMode == null || pushMode.length() == 0) {
         return AlivcQualityModeEnum.QM_FLUENCY_FIRST;
      }

      if ("fluencyFirst".equalsIgnoreCase(pushMode)) {
         return AlivcQualityModeEnum.QM_FLUENCY_FIRST;
      } else if ("resolutionFirst".equalsIgnoreCase(pushMode)) {
         return AlivcQualityModeEnum.QM_RESOLUTION_FIRST;
      } else if ("custom".equalsIgnoreCase(pushMode)) {
         return AlivcQualityModeEnum.QM_CUSTOM;
      }

      return AlivcQualityModeEnum.QM_FLUENCY_FIRST;
   }
}
