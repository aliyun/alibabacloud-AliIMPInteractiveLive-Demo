//
//  AlivcLivePushConstants.h
//  AlivcLiveCaptureLib
//
//  Created by TripleL on 2017/9/25.
//  Copyright © 2017年 Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>



/**
 SDK log级别

 - AlivcLivePushLogLevelAll: 全部
 - AlivcLivePushLogLevelVerbose: 冗长
 - AlivcLivePushLogLevelDebug: 调试
 - AlivcLivePushLogLevelInfo: 提示
 - AlivcLivePushLogLevelWarn: 警告
 - AlivcLivePushLogLevelError: 错误
 - AlivcLivePushLogLevelFatal: 阻塞错误
 */
typedef NS_ENUM(NSInteger, AlivcLivePushLogLevel){
    AlivcLivePushLogLevelAll = 1,
    AlivcLivePushLogLevelVerbose,
    AlivcLivePushLogLevelDebug,
    AlivcLivePushLogLevelInfo,
    AlivcLivePushLogLevelWarn,
    AlivcLivePushLogLevelError,
    AlivcLivePushLogLevelFatal,
};

/**
 推流状态
 
 - AlivcLivePushStatusIdle: 空闲
 - AlivcLivePushStatusInitialized: 初始化成功
 - AlivcLivePushStatusPreviewing: 打开预览中
 - AlivcLivePushStatusPreviewed: 正在预览
 - AlivcLivePushStatusPushing: 推流连接中
 - AlivcLivePushStatusPushed: 正在推流
 - AlivcLivePushStatusStoping: 停止推流中
 - AlivcLivePushStatusPausing: 暂停推流中
 - AlivcLivePushStatusPaused: 暂停推流
 - AlivcLivePushStatusResuming: 恢复推流中
 - AlivcLivePushStatusRestarting: 重启推流中
 - AlivcLivePushStatusError: 错误状态
 */
typedef NS_ENUM(NSInteger, AlivcLivePushStatus){
    AlivcLivePushStatusIdle = 0,
    AlivcLivePushStatusInitialized,
    AlivcLivePushStatusPreviewing,
    AlivcLivePushStatusPreviewed,
    AlivcLivePushStatusPushing,
    AlivcLivePushStatusPushed,
    AlivcLivePushStatusStoping,
    AlivcLivePushStatusPausing,
    AlivcLivePushStatusPaused,
    AlivcLivePushStatusResuming,
    AlivcLivePushStatusRestarting,
    AlivcLivePushStatusError,
};

/**
 分辨率

 - AlivcLivePushResolution180P: 180P
 - AlivcLivePushResolution240P: 240P
 - AlivcLivePushResolution360P: 360P
 - AlivcLivePushResolution480P: 480P
 - AlivcLivePushResolution540P: 540P
 - AlivcLivePushResolution720P: 720P
 - AlivcLivePushResolutionPassThrough: pass through only used for external main video stream
 */
typedef NS_ENUM(NSInteger, AlivcLivePushResolution){
    AlivcLivePushResolution180P = 0,
    AlivcLivePushResolution240P,
    AlivcLivePushResolution360P,
    AlivcLivePushResolution480P,
    AlivcLivePushResolution540P,
    AlivcLivePushResolution720P,
    AlivcLivePushResolutionPassThrough,
};

/**
Image format
 */
typedef NS_ENUM(NSInteger, AlivcLivePushVideoFormat){
    AlivcLivePushVideoFormatUnknown   = -1,
    AlivcLivePushVideoFormatBGR       = 0,
    AlivcLivePushVideoFormatRGB,
    AlivcLivePushVideoFormatARGB,
    AlivcLivePushVideoFormatBGRA,
    AlivcLivePushVideoFormatRGBA,
    AlivcLivePushVideoFormatYUV420P,
    AlivcLivePushVideoFormatYUVYV12,
    AlivcLivePushVideoFormatYUVNV21,
    AlivcLivePushVideoFormatYUVNV12,
    AlivcLivePushVideoFormatYUVJ420P,
    AlivcLivePushVideoFormatYUVJ420SP,
    AlivcLivePushVideoFormatYUVJ444P,
    AlivcLivePushVideoFormatYUV444P,
};


/**
 sound format
 */
typedef NS_ENUM(NSInteger, AlivcLivePushAudioFormat){
    AlivcLivePushAudioFormatUnknown   = -1,
    AlivcLivePushAudioFormatU8       = 0,
    AlivcLivePushAudioFormatS16,
    AlivcLivePushAudioFormatS32,
    AlivcLivePushAudioFormatF32,
    AlivcLivePushAudioFormatU8P,
    AlivcLivePushAudioFormatS16P,
    AlivcLivePushAudioFormatS32P,
    AlivcLivePushAudioFormatF32P,
};


/**
 推流模式

 - AlivcLivePushQualityModeResolutionFirst: 清晰度优先模式
 - AlivcLivePushQualityModeFluencyFirst: 流畅度优先模式
 - AlivcLivePushQualityModeCustom: 自定义模式
 */
typedef NS_ENUM(NSInteger, AlivcLivePushQualityMode){
    AlivcLivePushQualityModeResolutionFirst = 0,
    AlivcLivePushQualityModeFluencyFirst,
    AlivcLivePushQualityModeCustom,
};



typedef NS_ENUM(NSInteger, AlivcLivePushFPS) {
    AlivcLivePushFPS8  = 8,
    AlivcLivePushFPS10 = 10,
    AlivcLivePushFPS12 = 12,
    AlivcLivePushFPS15 = 15,
    AlivcLivePushFPS20 = 20,
    AlivcLivePushFPS25 = 25,
    AlivcLivePushFPS30 = 30,
};


/**
 推流屏幕方向

 - AlivcLivePushOrientationPortrait: 竖屏推流
 - AlivcLivePushOrientationLandscapeLeft: 横屏Left方向
 - AlivcLivePushOrientationLandscapeRight: 横屏Right方向
 */
typedef NS_ENUM(NSInteger, AlivcLivePushOrientation){
    AlivcLivePushOrientationPortrait = 0,
    AlivcLivePushOrientationLandscapeLeft,
    AlivcLivePushOrientationLandscapeRight,
};


/**
 摄像头方向

 - AlivcLivePushCameraTypeBack: 后置摄像头
 - AlivcLivePushCameraTypeFront: 前置摄像头
 */
typedef NS_ENUM(NSInteger, AlivcLivePushCameraType){
    AlivcLivePushCameraTypeBack = 0,
    AlivcLivePushCameraTypeFront,
};


/**
 缩放模式
 
 - AlivcLivePushScallingModeFit: 填充
 - AlivcLivePushScallingModeCrop: 裁剪
 */
typedef NS_ENUM(NSInteger, AlivcLivePushScallingMode){
    AlivcLivePushScallingModeFit = 0,
    AlivcLivePushScallingModeCrop,
};


/**
 视频编码模式
 
 - AlivcLivePushVideoEncoderModeHard: 硬编码
 - AlivcLivePushVideoEncoderModeSoft: 软编码
 */
typedef NS_ENUM(NSInteger, AlivcLivePushVideoEncoderMode){
    AlivcLivePushVideoEncoderModeHard = 0,
    AlivcLivePushVideoEncoderModeSoft,
};

/**
 音频编码模式
 
 - AlivcLivePushAudioEncoderModeHard: 硬编码
 - AlivcLivePushAudioEncoderModeSoft: 软编码
 */
typedef NS_ENUM(NSInteger, AlivcLivePushAudioEncoderMode){
    AlivcLivePushAudioEncoderModeHard = 0,
    AlivcLivePushAudioEncoderModeSoft,
};


/**
 音频编码格式

 - AlivcLivePushAudioEncoderProfile_AAC_LC: AAC_LC
 - AlivcLivePushAudioEncoderProfile_HE_AAC: HE_AAC
 - AlivcLivePushAudioEncoderProfile_HE_AAC_V2: HE_AAC_V2
 - AlivcLivePushAudioEncoderProfile_AAC_LD: AAC_LD
 */
typedef NS_ENUM(NSInteger, AlivcLivePushAudioEncoderProfile){
    AlivcLivePushAudioEncoderProfile_AAC_LC = 2,
    AlivcLivePushAudioEncoderProfile_HE_AAC = 5,
    AlivcLivePushAudioEncoderProfile_HE_AAC_V2 = 29,
    AlivcLivePushAudioEncoderProfile_AAC_LD = 29,
};


/**
 音频采样率
 
 - AlivcLivePushAudioSample32000: 32000Hz
 */
typedef NS_ENUM(NSInteger, AlivcLivePushAudioSampleRate){
    AlivcLivePushAudioSampleRate16000 = 16000,
    AlivcLivePushAudioSampleRate32000 = 32000,
    AlivcLivePushAudioSampleRate44100 = 44100,
    AlivcLivePushAudioSampleRate48000 = 48000,
};


/**
 声道数

 - AlivcLivePushAudioChannelOne: 单声道
 - AlivcLivePushAudioChannelTwo: 双声道
 */
typedef NS_ENUM(NSInteger, AlivcLivePushAudioChannel){
    AlivcLivePushAudioChannel_1 = 1,
    AlivcLivePushAudioChannel_2 = 2,
};


/**
 关键帧间隔

 - AlivcLivePushVideoEncodeGOP_1: 1s
 - AlivcLivePushVideoEncodeGOP_2: 2s
 - AlivcLivePushVideoEncodeGOP_3: 3s
 - AlivcLivePushVideoEncodeGOP_4: 4s
 - AlivcLivePushVideoEncodeGOP_5: 5s
 */
typedef NS_ENUM(NSInteger, AlivcLivePushVideoEncodeGOP){
    AlivcLivePushVideoEncodeGOP_1 = 1,
    AlivcLivePushVideoEncodeGOP_2 = 2,
    AlivcLivePushVideoEncodeGOP_3 = 3,
    AlivcLivePushVideoEncodeGOP_4 = 4,
    AlivcLivePushVideoEncodeGOP_5 = 5,
};


/**
 美颜模式

 - AlivcLivePushBeautyModeNormal: 普通版，不具备人脸识别功能
 - AlivcLivePushBeautyModeProfessional: 专业版，具备人脸识别功能。可以调节大眼瘦脸。
 */
typedef NS_ENUM(NSInteger, AlivcLivePushBeautyMode){
    AlivcLivePushBeautyModeNormal = 0,
    AlivcLivePushBeautyModeProfessional,
};



/**
 接口返回值错误码
 */
typedef NS_ENUM(NSInteger, AlivcPusherErrorCode){
    ALIVC_LIVE_PUSHER_INTERFACE_ERROR   = -1, // 接口调用内部错误
    ALIVC_LIVE_PUSHER_PARAM_ERROR       = -2, // 接口调用传入参数错误
    ALIVC_LIVE_PUSHER_UNKNOW_ERROR      = -3, // 接口调用未知错误
    ALIVC_LIVE_PUSHER_SEQUENCE_ERROR    = -4, // 接口调用顺序错误
};

/**
 预览窗口显示模式
 */
typedef NS_ENUM(NSInteger, AlivcPusherPreviewDisplayMode){
    ALIVC_LIVE_PUSHER_PREVIEW_SCALE_FILL= 0, // 铺满窗口，视频比例和窗口比例不一致时预览会有变形
    ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FIT= 1, // 保持视频比例，视频比例和窗口比例不一致时有黑边
    ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL = 2, //剪切视频以适配窗口比例，视频比例和窗口比例不一致时会裁剪视频
};

