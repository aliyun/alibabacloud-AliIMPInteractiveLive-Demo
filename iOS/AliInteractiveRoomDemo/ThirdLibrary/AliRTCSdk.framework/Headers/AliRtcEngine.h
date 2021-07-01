/* Copyright (c) 2018, Alibaba Inc.
 * All rights reserved
 */

/* This header file is part of Alibaba RTC engine sdk for
 * Mac system.
 */

#import <UIKit/UIKit.h>
#import <UIKit/UIImage.h>

/**
 * @brief 视频流类型

 - AliRtcVideoTrackNo: 无摄像头和屏幕共享
 - AliRtcVideoTrackCamera: 摄像头
 - AliRtcVideoTrackScreen: 屏幕共享
 - AliRtcVideoTrackBoth: 摄像头+屏幕共享
 */
typedef NS_ENUM(NSUInteger, AliRtcVideoTrack) {
    AliRtcVideoTrackNo     = 0,
    AliRtcVideoTrackCamera = 1,
    AliRtcVideoTrackScreen = 2,
    AliRtcVideoTrackBoth   = 3,
};

/**
 * @brief 音频流类型

 - AliRtcAudioTrackNo: 无麦克风
 - AliRtcAudioTrackMic: 麦克风
 */
typedef NS_ENUM(NSUInteger, AliRtcAudioTrack) {
    AliRtcAudioTrackNo   = 0,
    AliRtcAudioTrackMic  = 1,
};


/**
 * @brief 网络质量
 
 - AlivcRtcNetworkQualityExcellent: 网络极好，流程度清晰度质量好
 - AlivcRtcNetworkQualityGood: 网络好，流畅度清晰度和极好差不多
 - AlivcRtcNetworkQualityPoor: 网络较差，音视频流畅度清晰度有瑕疵，不影响沟通
 - AlivcRtcNetworkQualityBad: 网络差，视频卡顿严重，音频能正常沟通
 - AlivcRtcNetworkQualityVeryBad: 网络极差，基本无法沟通
 - AlivcRtcNetworkQualityDisconnect: 网络中断
 - AlivcRtcNetworkQualityUnknow: 未知
 */
typedef NS_ENUM(NSUInteger, AliRtcNetworkQuality) {
    AlivcRtcNetworkQualityExcellent  = 0,
    AlivcRtcNetworkQualityGood       = 1,
    AlivcRtcNetworkQualityPoor       = 2,
    AlivcRtcNetworkQualityBad        = 3,
    AlivcRtcNetworkQualityVeryBad    = 4,
    AlivcRtcNetworkQualityDisconnect = 5,
    AlivcRtcNetworkQualityUnknow     = 6,
};


/**
 * @brief 音频质量

- AliRtcAudioQulityUnknow 质量未知
- AliRtcAudioQulityExcellent 质量极好
- AliRtcAudioQulityGood 用户主观感觉和极好差不多 ，但码率可能略低于极好
- AliRtcAudioQulityPoor 用户主观感受有瑕疵，但不影响沟通
- AliRtcAudioQulityBad 勉强能沟通但不顺畅
- AliRtcAudioQulityVeryBad 网络质量非常差，基本不能沟通
- AliRtcAudioQulityNetworkDisconnected 网络连接已断开，完全无法沟通
*/
typedef NS_ENUM(NSInteger, AliRtcTranportAudioQuality) {
    AliRtcAudioQulityUnknow = 0,
    AliRtcAudioQulityExcellent = 1,
    AliRtcAudioQulityGood = 2,
    AliRtcAudioQulityPoor = 3,
    AliRtcAudioQulityBad = 4,
    AliRtcAudioQulityVeryBad = 5,
    AliRtcAudioQulityNetworkDisconnected = 6,
};

/**
 * @brief 音频编码配置
 
 - AliRtcEngineLowQualityMode 音频低音质模式，默认8000Hz采样率，单声道，最大编码码率12kbps
 - AliRtcEngineBasicQualityMode 默认）标准音质模式，默认16000Hz采样率，单声道，最大编码码率24kbps
 - AliRtcEngineHighQualityMode 高音质模式，默认48000Hz采样率，单声道，最大编码码率48kbps
 - AliRtcEngineStereoHighQualityMode 立体声高音质模式，默认48000Hz采样率，双声道，最大编码码率64kbps
 - AliRtcEngineSuperHighQualityMode 超高音质模式，默认48000Hz采样率，单声道，最大编码码率96kbps
 - AliRtcEngineStereoSuperHighQualityMode 立体声超高音质模式，默认48000Hz采样率，双声道，最大编码码率128kbps
 */
typedef NS_ENUM(NSInteger, AliRtcAudioProfile) {
    AliRtcEngineLowQualityMode        = 0x0000,  //1ch, 8khz,  12kbps;
    AliRtcEngineBasicQualityMode      = 0x0001,  //1ch, 16khz, 24kbps;
    AliRtcEngineHighQualityMode       = 0x0010,  //1ch, 48khz, 48kbps;
    AliRtcEngineStereoHighQualityMode = 0x0011,  //2ch, 48khz, 64kbps;
    AliRtcEngineSuperHighQualityMode = 0x0012, //1ch, 48khz, 96kbps;
    AliRtcEngineStereoSuperHighQualityMode = 0x0013, //2ch, 48khz, 128kbps;
};

/**
 * @brief 音频应用场景
 
 - AliRtcSceneDefaultMode 默认场景，一般的音视频通信场景推荐使用
 - AliRtcSceneEducationMode 教育场景，优先保证音频连续性与稳定性
 - AliRtcSeneMediaMode 媒体场景，保真人声与音乐音质, 连麦直播间推荐使用
 - AliRtcSceneMusicMode 音乐场景，高保真音乐音质，乐器教学等对音乐音质有要求的场景推荐使用
 */
typedef NS_ENUM(NSInteger, AliRtcAudioScenario) {
    AliRtcSceneDefaultMode   = 0x0000,
    AliRtcSceneEducationMode = 0x0100,
    AliRtcSeneMediaMode      = 0x0200,
    AliRtcSceneMusicMode     = 0x0300,
};

/**
 * @brief 渲染模式

 - AliRtcRenderModeAuto: 自动模式
 - AliRtcRenderModeStretch: 延伸
 - AliRtcRenderModeFill: 填充
 - AliRtcRenderModeCrop: 裁剪
 */
typedef NS_ENUM(NSUInteger, AliRtcRenderMode) {
    AliRtcRenderModeAuto    = 0,
    AliRtcRenderModeStretch = 1,
    AliRtcRenderModeFill    = 2,
    AliRtcRenderModeCrop    = 3,
};

/**
 * @brief Log级别
 */
typedef NS_ENUM(NSUInteger, AliRtcLogLevel) {
    AliRtcLogLevelInfo = 3,
    AliRtcLogLevelWarn,
    AliRtcLogLevelError,
    AliRtcLogLevelFatal,
    AliRtcLogLevelNone,
};

/**
 * @brief 视频编码帧率
 */
typedef NS_ENUM(NSInteger, AliRtcVideoEncoderFrameRate) {
    AliRtcVideoEncoderFrameRate_FPS_5 = 5,
    AliRtcVideoEncoderFrameRate_FPS_10 = 10,
    AliRtcVideoEncoderFrameRate_FPS_15 = 15,
    AliRtcVideoEncoderFrameRate_FPS_20 = 20,
    AliRtcVideoEncoderFrameRate_FPS_30 = 30,
};

/**
 * @brief 视频编码码率
 */
typedef NS_ENUM(NSInteger, AliRtcVideoEncoderBitrate) {
    AliRtcVideoEncoderStandardBitrate = 0,
};

/**
 * @brief 编码视频镜像模式

 - AliRtcVideoEncoderMirrorModeDisabled: 编码视频镜像
 - AliRtcVideoEncoderMirrorModeEnabled: 编码视频不镜像
 */
typedef NS_ENUM(NSInteger, AliRtcVideoEncoderMirrorMode) {
    AliRtcVideoEncoderMirrorModeDisabled = 0,
    AliRtcVideoEncoderMirrorModeEnabled = 1,
};

/**
 * @brief 视频旋转角度
 */
typedef NS_ENUM(NSInteger, AliRtcRotationMode) {
  AliRtcRotationMode_0    = 0,
  AliRtcRotationMode_90   = 90,
  AliRtcRotationMode_180  = 180,
  AliRtcRotationMode_270  = 270,
};

/**
 * @brief 编码视频旋转模式

 - AliRtcVideoEncoderOrientationModeAdaptive: 自适应，和采集视频保持一致
 - AliRtcVideoEncoderOrientationModeFixedLandscape: 固定横屏
 - AliRtcVideoEncoderOrientationModeFixedPortrait: 固定竖屏
 */
typedef NS_ENUM(NSInteger, AliRtcVideoEncoderOrientationMode) {
    AliRtcVideoEncoderOrientationModeAdaptive = 0,
    AliRtcVideoEncoderOrientationModeFixedLandscape = 1,
    AliRtcVideoEncoderOrientationModeFixedPortrait = 2,
};

/**
 * @brief 设备方向
 - AliRtcOrientationModePortrait: 固定竖屏模式
 - AliRtcOrientationModeLandscapeLeft: 固定左横屏模式
 - AliRtcOrientationModePortraitUpsideDown: 固定倒立模式
 - AliRtcOrientationModeLandscapeRight: 固定右横屏模式
 - AliRtcOrientationModeAuto: 自适应
 */
typedef NS_ENUM(NSInteger, AliRtcOrientationMode) {
    AliRtcOrientationModePortrait = 0,
    AliRtcOrientationModeLandscapeLeft = 1,
    AliRtcOrientationModePortraitUpsideDown = 2,
    AliRtcOrientationModeLandscapeRight = 3,
    AliRtcOrientationModeAuto = 4,
};

/**
 * @brief SDK对Audio Session的控制权限
 
 - AliRtcAudioSessionOperationRestrictionNone: 没有限制，SDK完全控制AVAudioSession
 - AliRtcAudioSessionOperationRestrictionSetCategory: SDK不能修改AVAudioSession的Category
 - AliRtcAudioSessionOperationRestrictionConfigureSession: SDK不能修改AVAudioSession的配置，包括Category，Mode，CategoryOptions
 - AliRtcAudioSessionOperationRestrictionDeactivateSession: SDK不能关闭AVAudioSession的活动状态，离开频道时，AVAudioSession依然处于活动状态
 */
typedef NS_ENUM(NSInteger, AliRtcAudioSessionOperationRestriction) {
    AliRtcAudioSessionOperationRestrictionNone = 0,
    AliRtcAudioSessionOperationRestrictionSetCategory = 1,
    AliRtcAudioSessionOperationRestrictionConfigureSession = 2,
    AliRtcAudioSessionOperationRestrictionDeactivateSession = 3
};


/**
 * @brief 相机流类型
 
 - AliRtcVideoStreamTypeNone: 无，在OnSubscribeStreamTypeChanged回调表示当前未订阅
 - AliRtcVideoStreamTypeHigh: 高码率，高分辨率流（大流）
 - AliRtcVideoStreamTypeLow: 低码率，低分辨率流（小流）
 */
typedef NS_ENUM(NSInteger, AliRtcVideoStreamType) {
  AliRtcVideoStreamTypeNone = 0,
  AliRtcVideoStreamTypeHigh = 1,
  AliRtcVideoStreamTypeLow = 2,
};

/**
 * @brief 设备音量类型
 * @note 默认为Auto类型，SDK 内部会自动选择合适的音量类型，如无特殊需求，不推荐自行设置
 
 - AliRtcDeviceVolumeTypeAuto: SDK自动设置音量类型
 - AliRtcDeviceVolumeTypeMedia: 设置为媒体音量，适合媒体播放场景，音量可以调节到零
 - AliRtcDeviceVolumeTypeVOIP: 设置为通话音量，适合通话场景，音量无法调节到零
 
 */
typedef NS_ENUM(NSInteger, AliRtcDeviceVolumeType) {
    AliRtcDeviceVolumeTypeAuto = 0,
    AliRtcDeviceVolumeTypeMedia = 1,
    AliRtcDeviceVolumeTypeVOIP = 2,
};

/**
 * @brief 视频裸数据源类型

 - AliRtcVideosourceCameraType: 视频流
 - AliRtcVideosourceScreenShareType: 屏幕共享流
 - AliRtcVideosourceTypeMax: 占位符，无意义
 */
typedef NS_ENUM(NSInteger, AliRtcVideoSource) {
    AliRtcVideosourceCameraType = 0,
    AliRtcVideosourceScreenShareType = 1,
    AliRtcVideosourceTypeMax,
};

/**
 * @brief 音频裸数据源类型
 
 - AliRtcAudiosourcePub: 推流音频数据
 - AliRtcAudiosourceSub: 拉流音频数据
 - AliRtcAudiosourceRawData: 采集音频裸数据
 */
typedef NS_ENUM(NSInteger, AliRtcAudioSource) {
    AliRtcAudiosourcePub = 0,
    AliRtcAudiosourceSub = 1,
    AliRtcAudiosourceRawData = 2,
};

/**
 * @brief 视频数据格式
 */
typedef NS_ENUM(NSInteger, AliRtcVideoFormat) {
    AliRtcVideoFormat_UNKNOW = -1,
    AliRtcVideoFormat_BGRA = 0,
    AliRtcVideoFormat_I420,
    AliRtcVideoFormat_NV21,
    AliRtcVideoFormat_NV12,
    AliRtcVideoFormat_RGBA,
    AliRtcVideoFormat_I422,
    AliRtcVideoFormat_ARGB,
    AliRtcVideoFormat_ABGR,
    AliRtcVideoFormat_RGB24,
    AliRtcVideoFormat_BGR24,
    AliRtcVideoFormat_RGB565,
};


/**
 * @brief 视频数据类型

 - AliRtcBufferType_Raw_Data: YUV裸数据
 - AliRtcBufferType_CVPixelBuffer: CVPixelBuffer数据
 */
typedef NS_ENUM(NSInteger, AliRtcBufferType) {
    AliRtcBufferType_Raw_Data = 0,
    AliRtcBufferType_CVPixelBuffer,
};

/**
 * @brief 音频声道数

 - AliRtcMonoAudio: 单声道
 - AliRtcStereoAudio: 双声道
 */
typedef NS_ENUM(NSInteger, AliRtcAudioNumChannel) {
    AliRtcMonoAudio = 1,
    AliRtcStereoAudio = 2,
};

/**
 * @brief 文件录制质量
 
 - AliRtcRecording_Low_Quality: 低质量文件录制，文件体积较小，音质一般
 - AliRtcRecording_Midium_Quality: 中等质量文件录制，文件体积中等，音质中等
 - AliRtcRecording_High_Quality: 高质量文件录制，文件体积较大，音质较好
 */
typedef NS_ENUM(NSInteger, AliRtcRecordingQuality) {
    AliRtcRecording_Low_Quality = 0,
    AliRtcRecording_Midium_Quality = 1,
    AliRtcRecording_High_Quality = 2,
};

/**
 * @brief 变声音效模式
 */
typedef NS_ENUM(NSInteger, AliRtcAudioEffectVoiceChangerMode) {
    AliRtcAudioEffectvVoiceChanger_OFF = 0,     //关闭
    AliRtcAudioEffectvVoiceChanger_OLD_MAN,     //老人
    AliRtcAudioEffectvVoiceChanger_BABYBOY,     //男孩
    AliRtcAudioEffectvVoiceChanger_BABYGILR,    //女孩
    AliRtcAudioEffectvVoiceChanger_ROBOT,       //机器人
    AliRtcAudioEffectvVoiceChanger_DAIMO,       //大魔王
    AliRtcAudioEffectvVoiceChanger_KTV,         //KTV
    AliRtcAudioEffectvVoiceChangerR_ECHO,        //回声
    AliRtcAudioEffectvVoiceChanger_MAX,
};

/**
 * @brief 音效混响模式
 */
typedef NS_ENUM(NSInteger, AliRtcAudioEffectReverbMode) {
    AliRtcAudioEffectReverb_Off = 0,            // 关闭
    AliRtcAudioEffectReverb_Vocal_I ,           //人声 I
    AliRtcAudioEffectReverb_Vocal_II,           // 人声 II
    AliRtcAudioEffectReverb_Bathroom,           // 澡堂
    AliRtcAudioEffectReverb_Small_Room_Bright,  // 明亮小房间
    AliRtcAudioEffectReverb_Small_Room_Dark,    // 黑暗小房间
    AliRtcAudioEffectReverb_Medium_Room,        // 中等房间
    AliRtcAudioEffectReverb_Large_Room,         // 大房间
    AliRtcAudioEffectReverb_Church_Hall,        // 教堂走廊
    AliRtcAudioEffectReverb_Mode_Max,           // 占位符，无含义
};

/**
 * @brief 音效混响参数
 */
typedef NS_ENUM(NSInteger, AliRtcAudioEffectReverbParamType) {
    AliRtcAudioEffectReverb_Room_Size = 0, // 房间大小 [0, 100]
    AliRtcAudioEffectReverb_Pre_Delay,     // 预延时ms [0, 200]
    AliRtcAudioEffectReverb_Revetberance,  // 混响感[0,100]
    AliRtcAudioEffectReverb_Hf_Damping,    // 消声 [0,100]
    AliRtcAudioEffectReverb_Tone_Low,      // 低音调[0,100]
    AliRtcAudioEffectReverb_Tone_High,     // 高音调[0,100]
    AliRtcAudioEffectReverb_Dry_Gain,      // 干增益[-20,10]
    AliRtcAudioEffectReverb_Wet_Gain,      // 湿增益[-20,10]
    AliRtcAudioEffectReverb_Type_Max,      // 占位符，无含义
};

/**
 * @brief 网络连接状态
*/
typedef NS_ENUM(NSInteger, AliRtcConnectionStatus) {
    AliRtcConnectionStatusInit = 0,       //初始化完成
    AliRtcConnectionStatusDisconnected,   //网络连接断开
    AliRtcConnectionStatusConnecting,     //建立网络连接中
    AliRtcConnectionStatusConnected,      //网络已连接
    AliRtcConnectionStatusReconnecting,   //重新建立网络连接中
    AliRtcConnectionStatusFailed,         //网络连接失败
};

/**
 * @brief 引起网络状态变更原因
*/
typedef NS_ENUM(NSInteger, AliRtcConnectionStatusChangeReason) {
    AliRtcConnectionChangedDummyReason = 0,               //未知原因
    AliRtcConnectionChangedMediaPathChanged,              //媒体通道变更
    AliRtcConnectionChangedSignalingHeartbeatTimeout,     //心跳超时
    AliRtcConnectionChangedSignalingHeartbeatAlive,       //心跳恢复
    AliRtcConnectionChangedSignalingHttpdnsResolved,      //DNS解析成功
    AliRtcConnectionChangedSignalingHttpdnsFailure,       //DNS解析失败
    AliRtcConnectionChangedSignalingGslbFailure,          //GSLB请求失败
    AliRtcConnectionChangedSignalingGslbSucccess,         //GSLB请求成功
    AliRtcConnectionChangedSignalingJoinChannelFailure,   //加入频道失败
    AliRtcConnectionChangedSignalingJoinChannelSuccess,   //加入频道成功
    AliRtcConnectionChangedSignalingLeaveRoom,            //离开频道
    AliRtcConnectionChangedSignalingConnecting,           //信令建立连接
};


/**
 * @brief 频道类型
 
 - AliRtcCommunication: 通信模式
 - AliRtcInteractivelive: 直播模式
 - AliRtcInteractiveWithLowLatencyLive: 低延时互动直播模式
 */
typedef NS_ENUM(NSInteger, AliRtcChannelProfile) {
    AliRtcCommunication = 0,
    AliRtcInteractivelive,
    AliRtcInteractiveWithLowLatencyLive,
};

/**
 * @brief 角色类型
 
 - AliRtcClientRoleInteractive: 参与互动角色
 - AliRtcClientRolelive: 仅观看角色
 */
typedef NS_ENUM(NSInteger, AliRtcClientRole) {
    AliRtcClientRoleInteractive = 0,
    AliRtcClientRolelive,
};

/**
 * @brief 本地静音模式

- AliRtcMuteAudioModeDefault: 默认模式(静音全部，包括麦克风及外部输入音频)
- AliRtcMuteAllAudioMode: 静音全部(包括麦克风及外部输入音频)
- AliRtcMuteOnlyMicAudioMode: 只静音麦克风
*/
typedef NS_ENUM(NSInteger, AliRtcMuteLocalAudioMode) {
    AliRtcMuteAudioModeDefault = 0,
    AliRtcMuteAllAudioMode,
    AliRtcMuteOnlyMicAudioMode,
    AliRtcMuteLocalAudioMax,
};


/**
 * @brief 渲染镜像模式

 - AliRtcRenderMirrorModeOnlyFrontCameraPreviewEnabled: 只有前置摄像头预览镜像，其余不镜像
 - AliRtcRenderMirrorModeAllEnabled: 全部镜像
 - AliRtcRenderMirrorModeAllDisabled: 全部不镜像
 */
typedef NS_ENUM(NSInteger, AliRtcRenderMirrorMode) {
    AliRtcRenderMirrorModeOnlyFrontCameraPreviewEnabled = 0,
    AliRtcRenderMirrorModeAllEnabled = 1,
    AliRtcRenderMirrorModeAllDisabled = 2,
};

/**
 * @brief 音乐伴奏播放状态
 
 - AliRtcAudioPlayingStarted: 开始播放
 - AliRtcAudioPlayingStopped: 停止播放
 - AliRtcAudioPlayingPaused: 播放暂停
 - AliRtcAudioPlayingResumed: 播放恢复
 - AliRtcAudioPlayingEnded: 播放完毕
 - AliRtcAudioPlayingBuffering: 正在缓冲
 - AliRtcAudioPlayingBufferingEnd: 缓冲结束
 - AliRtcAudioPlayingFailed: 播放失败
 */
typedef NS_ENUM(NSInteger, AliRtcAudioPlayingStateCode) {
    AliRtcAudioPlayingStarted   = 100,
    AliRtcAudioPlayingStopped   = 101,
    AliRtcAudioPlayingPaused    = 102,
    AliRtcAudioPlayingResumed   = 103,
    AliRtcAudioPlayingEnded     = 104,
    AliRtcAudioPlayingBuffering = 105,
    AliRtcAudioPlayingBufferingEnd = 106,
    AliRtcAudioPlayingFailed = 107,
};

/**
 * @brief 音乐伴奏播放错误码
 
 - AliRtcAudioPlayingNoError: 没有错误
 - AliRtcAudioPlayingOpenFailed: 打开文件错误
 - AliRtcAudioPlayingDecodeFailed: 解码文件错误
 */
typedef NS_ENUM(NSInteger, AliRtcAudioPlayingErrorCode) {
    AliRtcAudioPlayingNoError        = 0,
    AliRtcAudioPlayingOpenFailed     = -100,
    AliRtcAudioPlayingDecodeFailed   = -101,
};

/**
 * @brief 录制类型

 - AliRtcRecordTypeAudio: 纯音频录制
*/
typedef NS_ENUM(NSUInteger, AliRtcRecordType) {
    AliRtcRecordTypeAudio = 0,
};

/**
 * @brief 录制格式

 - AliRtcRecordFormatAAC: AAC文件
 - AliRtcRecordFormatWAV: WAV文件
*/
typedef NS_ENUM(NSUInteger, AliRtcRecordFormat) {
    AliRtcRecordFormatAAC = 0,
    AliRtcRecordFormatWAV,
};

/**
 * @brief 录制音频质量

 - AliRtcAudioQualityLow: 低
 - AliRtcAudioQualityMidium: 中
 - AliRtcAudioQualityHigh: 高
*/
typedef NS_ENUM(NSUInteger, AliRtcAudioQuality) {
    AliRtcAudioQualityLow = 0,
    AliRtcAudioQualityMidium,
    AliRtcAudioQualityHigh,
};

/**
 * @brief 录制视频质量

 - AliRtcVideoQualityDefault: 默认，和采集保持一致
*/
typedef NS_ENUM(NSUInteger, AliRtcVideoQuality) {
    AliRtcVideoQualityDefault = 0,
};

/**
 * @brief 录制音频采样率
*/
typedef NS_ENUM(NSInteger, AliRtcAudioSampleRate) {
    AliRtcAudioSampleRate_8000 = 0,
    AliRtcAudioSampleRate_11025 = 1,
    AliRtcAudioSampleRate_16000 = 2,
    AliRtcAudioSampleRate_22050 = 3,
    AliRtcAudioSampleRate_32000 = 4,
    AliRtcAudioSampleRate_44100 = 5,
    AliRtcAudioSampleRate_48000 = 6,
};

/**
 * @brief 语音路由
 */
typedef NS_ENUM(NSInteger, AliRtcAudioRouteType) {
    AliRtcAudioRouteTypeDefault        = 0, //默认的语音路由
    AliRtcAudioRouteTypeHeadset        = 1, //耳机
    AliRtcAudioRouteTypeEarpiece       = 2, //听筒
    AliRtcAudioRouteTypeHeadsetNoMic   = 3, //不带麦的耳机
    AliRtcAudioRouteTypeSpeakerphone   = 4, //手机扬声器
    AliRtcAudioRouteTypeLoudSpeaker    = 5, //外接扬声器
    AliRtcAudioRouteTypeBlueTooth      = 6, //蓝牙耳机
};

/**
 * @brief 视频数据输出位置内容
 */
typedef NS_ENUM(NSInteger, AliRtcVideoObserPosition) {
    AliRtcPositionPostCapture = 1 << 0,       // 采集视频数据，对应输出回调onCaptureVideoSample
    AliRtcPositionPreRender = 1 << 1,         // 渲染视频数据，对应输出回调onRemoteVideoSample
    AliRtcPositionPreEncoder = 1 << 2,        // 编码前视频数据，对应输出回调onPreEncodeVideoSample
};

/**
 * @brief 录制音频设置

 - sampleRate: 音频采样率
 - quality: 音频质量
 - enableRecordExternalRenderPCM: 是否允许录制外部pcm输入渲染数据
*/
typedef struct  {
    AliRtcAudioSampleRate sampleRate;
    AliRtcAudioQuality quality;
    bool enableRecordExternalRenderPCM;
}AliRtcRecordAudioConfig;

/**
 * @brief 录制视频设置

 - quality: 视频质量
*/
typedef struct  {
    AliRtcVideoQuality quality; //视频质量
}AliRtcRecordVideoConfig;

/**
 * @brief 美颜设置

 - whiteningLevel: 美白等级
 - smoothnessLevel: 磨皮等级
*/
@interface AliRtcBeautyConfig : NSObject

@property (nonatomic, assign) float whiteningLevel;      //美白等级[0-1.0]
@property (nonatomic, assign) float smoothnessLevel;     //磨皮等级[0-1.0]

@end


/**
 * @brief 错误码
 
 - AliRtcErrNone: 无
 **************************入会错误码****************************
 - AliRtcErrJoinBadAppId: AppId不存在
 - AliRtcErrJoinInvalidAppId: AppId已失效
 - AliRtcErrJoinBadChannel: 频道不存在
 - AliRtcErrJoinInvalidChannel: 频道已失效
 - AliRtcErrJoinBadToken: token不存在
 - AliRtcErrJoinTimeout: 加入频道超时
 - AliRtcErrJoinBadParam: 参数错误
 ************************音频设备错误码************************
 - AliRtcErrMicOpenFail: 采集设备初始化失败
 - AliRtcErrSpeakerOpenFail: 播放设备初始化失败
 - AliRtcErrMicInterrupt: 采集过程中出现异常
 - AliRtcErrSpeakerInterrupt: 播放过程中出现异常
 - AliRtcErrMicAuthFail: 麦克风设备未授权
 - AliRtcErrMicNotAvailable: 无可用的音频采集设备
 - AliRtcErrSpeakerNotAvailable: 无可用的音频播放设备
 ************************视频设备错误码*************************
 - AliRtcErrCameraOpenFail: 采集设备初始化失败
 - AliRtcErrCameraInterrupt: 采集过程中出现异常
 - AliRtcErrDisplayOpenFail: 渲染设备初始化失败
 - AliRtcErrDisplayInterrupt: 渲染过程中出现异常
 ***************************网络错误码***************************
 - AliRtcErrIceConnectionConnectFail: 媒体通道建立失败
 - AliRtcErrIceConnectionReconnectFail: 媒体通道重连失败
 - AliRtcErrIceConnectionHeartbeatTimeout: 信令心跳超时
  ********************推流相关错误码********************
 - AliRtcErrPublishInvaild:推流无效
 - AliRtcErrPublishNotJoinChannel:未进入频道推流失败
 - AliRtcErrPublishAudioStreamFailed: 推送音频流失败
 - AliRtcErrPublishVideoStreamFailed: 推送视频流失败
 - AliRtcErrPublishDualStreamFailed: 推送双流失败
 - AliRtcErrPublishScreenShareFailed: 推送屏幕共享失败
 - AliRtcErrPublishScreenShareConfigError:屏幕共享配置错误
  ********************订阅相关错误码********************
 - AliRtcErrSubscribeInvaild:订阅无效
 - AliRtcErrSubscribeNotJoinChannel:未进入频道订阅错误
 - AliRtcErrSubscribeAudioStreamFailed:订阅音频流失败
 - AliRtcErrSubscribeVideoStreamFailed:订阅视频流失败
 - AliRtcErrSubscribeDualStreamFailed:订阅双流失败
 - AliRtcErrSubscribeScreenShareFailed:订阅屏幕共享失败
 ********************低延时互动直播模式相关错误码********************
 - AliRtcErrCdnLiveCommunicationFailed: 低延时互动直播信令失败
 - AliRtcErrCdnLiveMeidaSettingFailed: 低延时互动直播媒体设置错误
 - AliRtcErrCdnLiveNoneBroadcaster: 低延时互动直播当前房间没有主播推流
 - AliRtcErrCdnLiveNeedRestart: 低延时互动直播错误，应用层需要重新创建新的SDK实例
 - AliRtcErrCdnLiveNeedRestartAgainLater: 低延时互动直播错误，应用层需要稍等一定时间（自定义间隔）重新创建新的SDK实例
 - AliRtcErrCdnLiveServiceUnavailable: 低延时互动直播服务不可用或者服务降级
 - AliRtcErrCdnLiveMediaConnectionInterruption: 低延时互动直播网络中断错误
 ***************************其他错误码***************************
 - AliRtcErrSdkInvalidState: sdk状态错误
 - AliRtcErrSessionRemoved: Session已经被移除
 - AliRtcErrInvalidArguments: 参数不匹配
 - AliRtcErrAudioBufferFull: buffer队列饱和，用于外部输送裸数据功能
 -AliRtcErrVideoBufferFull: buffer队列饱和，用于外部输送裸数据功能
 - AliRtcErrUpdateRoleChannel: 互动模式下设置角色错误
 - AliRtcErrInner: 其他错误
 */
typedef NS_ENUM(NSInteger, AliRtcErrorCode) {
    AliRtcErrNone                           = 0,
    AliRtcErrJoinBadAppId                   = 0x02010201,
    AliRtcErrJoinInvalidAppId               = 0x02010202,
    AliRtcErrJoinBadChannel                 = 0x02010204,
    AliRtcErrJoinInvalidChannel             = 0x02010203,
    AliRtcErrJoinBadToken                   = 0x02010205,
    AliRtcErrJoinTimeout                    = 0x01020204,
    AliRtcErrJoinBadParam                   = 0x01030101,
    AliRtcErrJoinChannelFailed              = 0x01030202,
    AliRtcErrMicOpenFail                    = 0x01040404,
    AliRtcErrSpeakerOpenFail                = 0x01040405,
    AliRtcErrMicInterrupt                   = 0x01040406,
    AliRtcErrSpeakerInterrupt               = 0x01040407,
    AliRtcErrMicAuthFail                    = 0x01040408,
    AliRtcErrMicNotAvailable                = 0x01040409,
    AliRtcErrSpeakerNotAvailable            = 0x01040410,
    AliRtcErrCameraOpenFail                 = 0x01040104,
    AliRtcErrCameraInterrupt                = 0x01040106,
    AliRtcErrDisplayOpenFail                = 0x01040201,
    AliRtcErrDisplayInterrupt               = 0x01040202,
    AliRtcErrIceConnectionConnectFail       = 0x01050201,
    AliRtcErrIceConnectionReconnectFail     = 0x01050202,
    AliRtcErrIceConnectionHeartbeatTimeout  = 0x0102020C,
    AliRtcErrCdnLiveCommunicationFailed     = 0x01070003,
    AliRtcErrCdnLiveMeidaSettingFailed      = 0x01070004,
    AliRtcErrCdnLiveNoneBroadcaster         = 0x01070005,
    AliRtcErrCdnLiveNeedRestart             = 0x01070006,
    AliRtcErrCdnLiveNeedRestartAgainLater   = 0x01070007,
    AliRtcErrCdnLiveServiceUnavailable      = 0x01070008,
    AliRtcErrCdnLiveMediaConnectionInterruption = 0x01070009,
    AliRtcErrSdkInvalidState                = 0x01030204,
    AliRtcErrSessionRemoved                 = 0x02010105,
    AliRtcErrInvalidArguments               = 0x01030103,
    AliRtcErrAudioBufferFull                = 0x01070101,
    AliRtcErrVideoBufferFull                = 0x01070102,
    AliRtcErrUpdateRoleChannel              = 0x01020201,
    
    AliRtcErrPublishInvaild                 = 0x01030305,
    AliRtcErrPublishNotJoinChannel          = 0x01010406,
    AliRtcErrPublishAudioStreamFailed       = 0x01010450,
    AliRtcErrPublishVideoStreamFailed       = 0x01010451,
    AliRtcErrPublishDualStreamFailed        = 0x01010452,
    AliRtcErrPublishScreenShareFailed       = 0x01010453,
    AliRtcErrPublishScreenShareConfigError  = 0x01010454,

    AliRtcErrSubscribeInvaild               = 0x01030404,
    AliRtcErrSubscribeNotJoinChannel        = 0x01010550,
    AliRtcErrSubscribeAudioStreamFailed     = 0x01010551,
    AliRtcErrSubscribeVideoStreamFailed     = 0x01010552,
    AliRtcErrSubscribeDualStreamFailed      = 0x01010553,
    AliRtcErrSubscribeScreenShareFailed     = 0x01010554,
    
    AliRtcErrInner                          = -1,
};

/**
 * @brief OnBye类型

 - AliRtcOnByeBeKickedOut: 当前user被踢出channel
 - AliRtcOnByeChannelTerminated: channel已结束，需要离开会议
 - AliRtcOnByeUserReplaced: 相同userID在其他设备joinChannel，当前设备被下线
 */
typedef NS_ENUM(NSInteger, AliRtcOnByeType) {
    AliRtcOnByeBeKickedOut          = 1,
    AliRtcOnByeChannelTerminated    = 2,
    AliRtcOnByeUserReplaced         = 3,
};

/**
 * @brief SDK反馈问题类型
 */
typedef NS_ENUM(NSInteger, AliRtcFeedbackType) {
    AliRtcFeedbackVideoDeviceFail   = 101, // 视频设备问题
    AliRtcFeedbackVideoNotRender    = 102, // 视频无画面
    AliRtcFeedbackAudioDeviceFail   = 201, // 音频设备问题
    AliRtcFeedbackAudioNotRender    = 202, // 音频无声音
    AliRtcFeedbackAudioEchoError    = 203, // 音频回声异常
    AliRtcFeedbackChannelTypeError  = 301, // 会议状态异常：入会异常、离会异常等
    AliRtcFeedbackSDKTypeError      = 302, // SDK其他状态异常
    AliRtcFeedbackNetworkUnfluent   = 401, // 网络卡顿问题
    AliRtcFeedbackVideoBlurring     = 402, // 视频清晰问题
    AliRtcFeedbackUnkonw            = -1,  // 未知类型
};

/**
 * @brief 旁路推流LiveStream状态
 */
typedef NS_ENUM(NSInteger, AliRtcTrascodingLiveStreamStatus) {
    AliRtcTrascodingStatusStart         = 0, // 任务开始
    AliRtcTrascodingStatusUpdate        = 1, // 任务更新
    AliRtcTrascodingStatusStop          = 2, // 任务停止
    AliRtcTrascodingStatusEnd           = 3, // 推流未开始或已结束
    AliRtcTrascodingStatusConnect       = 4, // 正在连接推流服务器和 RTMP 服务器
    AliRtcTrascodingStatusRunning       = 5, // 推流正在进行
    AliRtcTrascodingStatusRecovering    = 6, // 正在恢复推流
    AliRtcTrascodingStatusFailed        = 7, // 推流失败
    AliRtcTrascodingStatusUnknow        = 8, // 未知错误
};

/**
 * @brief 旁路推流错误码
 */
typedef NS_ENUM(NSInteger, AliRtcTrascodingLiveStreamErrorCode) {
    AliRtcTrascodingLiveStreamErrorPublishOk            = 0,
    AliRtcTrascodingLiveStreamErrorStreamNotFound       = 0x01100001,
    AliRtcTrascodingLiveStreamErrorStreamAlreadyExist   = 0x01100002,
    AliRtcTrascodingLiveStreamErrorInvalidParam         = 0x01100003,
    AliRtcTrascodingLiveStreamErrorInternalError        = 0x01100004,
    AliRtcTrascodingLiveStreamErrorRtmpServerError      = 0x01100005,
    AliRtcTrascodingLiveStreamErrorRtmpStreamUrlError   = 0x01100006,
    AliRtcTrascodingLiveStreamErrorPublishTimeout       = 0x01100007,
    AliRtcTrascodingLiveStreamErrorNotAuthorized        = 0x01100008,
};

/**
 * @brief 跨频道转推事件
 */
typedef NS_ENUM(NSInteger, AliRtcChannelRelayEvent) {
    AliRtcChannelRelayStart     = 0,
    AliRtcChannelRelayUpdate    = 1,
    AliRtcChannelRelayStop      = 2,
};

/**
 * @brief 跨频道转推事件
 */
typedef NS_ENUM(NSInteger, AliRtcChannelRelayState) {
    AliRtcChannelRelayStateInit         = 3,
    AliRtcChannelRelayStateConnecting   = 4,
    AliRtcChannelRelayStateRunning      = 5,
    AliRtcChannelRelayStateFailure      = 6,
};

/**
 * @brief 采集偏好
*/
typedef NS_ENUM(NSInteger, AliRtcCaptureOutputPreference)  {
  AliRtcCaptureOutputPreferenceAuto = 0,               /* SDK自动调整采集参数*/
  AliRtcCaptureOutputPreferencePerformance = 1,        /* SDK优先保证设备性能，根据设置编码参数选择最接近的摄像头输出参数 */
  AliRtcCaptureOutputPreferencePreview = 2,            /* SDK优先保证视频预览质量，选择较高的摄像头输出参数 */
};

/**
 * @brief 摄像头方向
*/
typedef NS_ENUM(NSInteger, AliRtcCameraDirection)  {
  AliRtcCameraDirectionInvalid = -1,
  AliRtcCameraDirectionBack = 0,
  AliRtcCameraDirectionFront = 1,
};

/**
 * @brief 推流状态
 
 - AliRtcStatsPublishIdle:初始状态
 - AliRtcStatsNoPublish: 未推流
 - AliRtcStatsPublishing:推流中
 - AliRtcStatsPublished: 已推流
*/
typedef NS_ENUM(NSInteger, AliRtcPublishState) {
    AliRtcStatsPublishIdle = 0,
    AliRtcStatsNoPublish   = 1,
    AliRtcStatsPublishing  = 2,
    AliRtcStatsPublished   = 3,
};

/**
 * @brief 订阅状态
 
 - AliRtcStatsSubscribeIdle:初始状态
 - AliRtcStatsNoSubscribe: 未订阅
 - AliRtcStatsSubscribing: 订阅中
 - AliRtcStatsSubscribed:  已订阅
*/
typedef NS_ENUM(NSInteger, AliRtcSubscribeState) {
    AliRtcStatsSubscribeIdle = 0,
    AliRtcStatsNoSubscribe   = 1,
    AliRtcStatsSubscribing   = 2,
    AliRtcStatsSubscribed    = 3,
};

/**
 * @brief 用户离线原因
*/
typedef NS_ENUM(NSInteger, AliRtcUserOfflineReason) {
    AliRtcUserOfflineQuit = 0,              /* 用户主动离开 */
    AliRtcUserOfflineDropped = 1,           /* 因过长时间收不到对方数据包，超时掉线 */
    AliRtcUserOfflineBecomeAudience = 2,    /* 用户身份从主播切换为观众时触发 */
};

/**
 * @brief 实时数据
 */
typedef struct {
    long long sent_kbitrate;      // 总发送码率(kb)
    long long rcvd_kbitrate;      // 总接收码率(kb)
    long long sent_bytes;         // 总发送数据量(bytes)
    long long rcvd_bytes;         // 总接收数据量(bytes)
    long long video_rcvd_kbitrate;// 视频发送码率(kb)
    long long video_sent_kbitrate;// 视频接收码率(kb)
    long long call_duration;     // 通话时长(s)
    float cpu_usage;            // 进程CPU使用量(%)
    float systemCpuUsage;       // 系统CPU使用量(%)
    long long sent_loss_rate;     // 客户端到服务器的丢包率(%)
    long long sent_loss_pkts;     // 客户端到服务器的丢包数
    long long sent_expected_pkts; // 客户端到服务器的总包数
    long long rcvd_loss_rate;     // 客户端到服务器的下行丢包率（%）
    long long rcvd_loss_pkts;     // 客户端到服务器的下行丢包数
    long long rcvd_expected_pkts; // 客户端到服务器的下行总包数
    long long lastmile_delay;     // 客户端到服务器的延迟(ms)
} AliRtcStats;


/**
 * @brief 本地视频统计信息
 */
@interface AliRtcLocalVideoStats : NSObject

@property (nonatomic, assign) AliRtcVideoTrack track; // 视频流track类型
@property (nonatomic, assign) unsigned int sent_bitrate;  // 发送码率
@property (nonatomic, assign) unsigned int sent_fps;      // 发送帧率
@property (nonatomic, assign) unsigned int encode_fps;    // 编码帧率

@end

/**
 * @brief 远端视频统计信息
 */
@interface AliRtcRemoteVideoStats : NSObject

@property (nonatomic, copy) NSString * _Nonnull userId; // 远端用户userID
@property (nonatomic, assign) AliRtcVideoTrack track;   // 视频流track类型
@property (nonatomic, assign) unsigned int width;           // width
@property (nonatomic, assign) unsigned int height;          // height
@property (nonatomic, assign) unsigned int decode_fps;      // 解码帧率
@property (nonatomic, assign) unsigned int render_fps;      // 渲染帧率
@property (nonatomic, assign) unsigned int frozen_times;    // 卡顿次数

@end

/**
 * @brief 本地音频统计信息
 */
@interface AliRtcLocalAudioStats : NSObject

@property (nonatomic, assign) AliRtcAudioTrack track;   // 音频流track类型
@property (nonatomic, assign) unsigned int num_channel;     // 声道数
@property (nonatomic, assign) unsigned int sent_samplerate; // 采样率
@property (nonatomic, assign) unsigned int sent_bitrate;    // 发送端音频的瞬时码率（Kbps）

@end

/**
 * @brief 远端音频统计信息
 */
@interface AliRtcRemoteAudioStats : NSObject

@property (nonatomic, copy) NSString * _Nonnull userId;    // 远端用户userID
@property (nonatomic, assign) AliRtcAudioTrack track;      // 音频流track类型
@property (nonatomic, assign) unsigned int quality;            // 远端用户发送的音频流质量, 具体类型参看AliRtcTranportAudioQuality
@property (nonatomic, assign) unsigned int audio_loss_rate;    // 回调周期内的音频丢帧率
@property (nonatomic, assign) unsigned int rcvd_bitrate;       // 接收流的瞬时码率（Kbps）
@property (nonatomic, assign) unsigned int total_frozen_times; // 远端用户加入频道后音频卡顿的累计时长 (ms)。
@property (nonatomic, assign) unsigned int network_transport_delay; // 发送端到接收端的网络延时（ms)
@property (nonatomic, assign) unsigned int jitter_buffer_delay; // 接受端到网络抖动缓存的网络延时（ms)

@end

/**
 * @brief 音量信息格式
 */
@interface AliRtcUserVolumeInfo : NSObject

@property (nonatomic, copy) NSString * _Nonnull uid;
@property (nonatomic, assign) BOOL speech_state;// true:表示正在说话;false:没说话
@property (nonatomic, assign) int volume;

@end

/**
 * @brief 入会鉴权信息
 */
@interface AliRtcAuthInfo : NSObject

@property (nonatomic, retain) NSString * _Nonnull channelId;
@property (nonatomic, retain) NSString * _Nonnull userId;
@property (nonatomic, retain) NSString * _Nonnull appId;
@property (nonatomic, retain) NSString * _Nonnull nonce;
@property (nonatomic, retain) NSString * _Nonnull token;
@property (nonatomic, retain) NSString * _Nullable session;
@property (nonatomic, retain) NSString * _Nullable role;
@property (nonatomic, retain) NSArray <NSString *> * _Nullable gslb;
@property (nonatomic, retain) NSArray <NSString *> * _Nullable agent;
@property (nonatomic, assign) long long timestamp;

@end

/**
 * @brief 渲染配置
 */
@interface AliVideoCanvas : NSObject

@property (nonatomic, strong) UIView * _Nonnull view;         // 渲染view，不可为nil
@property (nonatomic) AliRtcRenderMode renderMode;            // 渲染模式，默认AliRtcRenderModeAuto
@property (nonatomic) AliRtcRenderMirrorMode mirrorMode;      // 镜像模式
@property (nonatomic) int backgroundColor;                    // 背景颜色，格式为RGB的Hex
@property (nonatomic, assign) AliRtcRotationMode rotationMode;

@property (nonatomic) int textureId;                          // 纹理Id
@property (nonatomic) int textureWidth;                       // 纹理宽
@property (nonatomic) int textureHeight;                      // 纹理高

@end

/**
 * @brief 水印配置
 */
@interface AliRtcWatermarkConfig : NSObject

@property (nonatomic, assign) BOOL visibleInPreview;
@property (nonatomic, assign) CGRect positionInPortraitMode;
@property (nonatomic, assign) CGRect positionInLandscapeMode;
@property (nonatomic, assign) float alpha;
@property (nonatomic, assign) BOOL normalized;

@end

/**
 * @brief 音频裸数据
 */
@interface AliRtcAudioDataSample : NSObject

@property (nonatomic, assign) long dataPtr;
@property (nonatomic, assign) int numOfSamples;
@property (nonatomic, assign) int bytesPerSample;
@property (nonatomic, assign) int numOfChannels;
@property (nonatomic, assign) int samplesPerSec;

@end

/**
 * @brief 视频裸数据
 */
@interface AliRtcVideoDataSample : NSObject

@property (nonatomic, assign) AliRtcVideoFormat format;
@property (nonatomic, assign) AliRtcBufferType type;
@property (nonatomic, assign) CVPixelBufferRef _Nullable pixelBuffer;
@property (nonatomic, assign) long dataPtr;
@property (nonatomic, assign) long dataYPtr;
@property (nonatomic, assign) long dataUPtr;
@property (nonatomic, assign) long dataVPtr;
@property (nonatomic, assign) long dataLength;
@property (nonatomic, assign) int strideY;
@property (nonatomic, assign) int strideU;
@property (nonatomic, assign) int strideV;
@property (nonatomic, assign) int stride;
@property (nonatomic, assign) int height;
@property (nonatomic, assign) int width;
@property (nonatomic, assign) int rotation;
@property (nonatomic, assign) long long timeStamp;

@end

/**
 * @brief 跨频道转推配置
 */
@interface AliRtcChannelRelayConfiguration : NSObject

@property (nonatomic, copy, readonly) NSDictionary<NSString*,AliRtcAuthInfo*> *_Nullable destChannelInfos;

- (void)addDestChannelInfoWithChannelId:(NSString *_Nullable)channeId
                               authInfo:(AliRtcAuthInfo *_Nullable)authInfo;

- (void)removeDestChannelInfo:(NSString *_Nullable)channeId;

- (void)removeAllDestChannelInfo;

@end


/**
 * @brief 消息通道消息
 * @note 已废弃使用
 */
@interface AliRtcMessage : NSObject

@property (nonatomic, strong) NSString * _Nullable tID;
@property (nonatomic, strong) NSString * _Nullable contentType;
@property (nonatomic, strong) NSString * _Nullable content;
@end

/**
 * @brief 消息通道Response
 * @note 已废弃使用
 */
@interface AliRtcMessageResponse : NSObject

@property (nonatomic, assign) int result;
@property (nonatomic, strong) NSString * _Nullable contentType;
@property (nonatomic, strong) NSString * _Nullable content;
@end

/**
 * @brief 摄像头采集偏好
*/
@interface AliRtcCameraCapturerConfiguration : NSObject

@property (nonatomic) AliRtcCaptureOutputPreference preference;
@property (nonatomic) AliRtcCameraDirection cameraDirection;

@end

/**
 * @brief 旁路直播用户信息
*/
@interface AliRtcTranscodingUser : NSObject

@property (nonatomic, assign) NSString *_Nonnull userId;

@end

/**
 * @brief 旁路直播水印配置
*/
@interface AliRtcTranscodingImage : NSObject

@property (nonatomic, copy) NSString *_Nonnull url;
@property (nonatomic, assign) float alpha;
@property (nonatomic, assign) int display;
@property (nonatomic, assign) float x;
@property (nonatomic, assign) float y;
@property (nonatomic, assign) float width;
@property (nonatomic, assign) float height;
@property (nonatomic, assign) int zorder;

@end

/**
 * @brief 旁路直播文字配置
*/
@interface AliRtcTranscodingText : NSObject

@property (nonatomic, copy) NSString *_Nonnull text;
@property (nonatomic, assign) float x;
@property (nonatomic, assign) float y;
@property (nonatomic, assign) float width;
@property (nonatomic, assign) float height;
@property (nonatomic, assign) int zorder;
@property (nonatomic, assign) int fontSize;

@end

/**
 * @brief 旁路直播用户窗格信息
*/
@interface AliRtcTranscodingUserPane : NSObject

@property (nonatomic, assign) int paneId;
@property (nonatomic, copy) NSString *_Nonnull userId;
@property (nonatomic, copy) NSString *_Nonnull sourceType;

@end

/**
 * @brief 旁路直播用户自定义窗格信息
*/

@interface AliRtcTranscodingPane : NSObject

@property (nonatomic, assign) int paneId;
@property (nonatomic, assign) int majorPane;
@property (nonatomic, assign) float x;
@property (nonatomic, assign) float y;
@property (nonatomic, assign) float width;
@property (nonatomic, assign) float height;
@property (nonatomic, assign) int zorder;
@property (nonatomic, copy) NSString *_Nonnull userId;
@property (nonatomic, copy) NSString *_Nonnull sourceType;
@property (nonatomic, assign) int segmentType;

@end

/**
 * @brief 旁路直播用户自定义layout信息
*/

@interface AliRtcTranscodingLayout : NSObject

@property (nonatomic, assign) int audioMixCount;
@property (nonatomic, copy) NSArray<AliRtcTranscodingPane *> *_Nullable panes;

@end

/**
 * @brief 旁路直播配置
*/
@interface AliRtcLiveTranscoding : NSObject

@property (nonatomic, assign) int mixMode;  // 0 single stream, 1 multy stream
@property (nonatomic, copy) NSArray<NSString *> *_Nullable layoutIds;
@property (nonatomic, strong) NSMutableArray<AliRtcTranscodingUser *> *_Nullable subSpecUsers;
@property (nonatomic, copy) NSArray<AliRtcTranscodingUserPane *> *_Nullable userPanes;
@property (nonatomic, copy) NSArray<AliRtcTranscodingLayout *> *_Nullable layouts;
@property (nonatomic, copy) NSString *_Nullable taskProfile;
@property (nonatomic, strong) UIColor *_Nullable backgroundColor;
@property (nonatomic, assign) int mediaEncode;
@property (nonatomic, assign) int cropMode;
@property (nonatomic, assign) int streamType;  // 0 original stream, 1 audio only, 2 video only 
@property (nonatomic, copy) NSString *_Nullable sourceType;
@property (nonatomic, copy) NSString *_Nullable externalParam;

- (void)addUser:(AliRtcTranscodingUser *_Nonnull)user;
- (void)removeUser:(AliRtcTranscodingUser *_Nonnull)user;

@end

/**
 * @brief 视频编码偏好
*/
@interface AliRtcVideoEncoderConfiguration : NSObject

@property (nonatomic, assign) CGSize dimensions;
@property (nonatomic, assign) NSInteger frameRate;
@property (nonatomic, assign) NSInteger bitrate;
@property (nonatomic, assign) AliRtcVideoEncoderMirrorMode mirrorMode;
@property (nonatomic, assign) AliRtcVideoEncoderOrientationMode orientationMode;
@property (nonatomic, assign) AliRtcRotationMode rotationMode;

@end

/**
 * @brief 屏幕编码偏好
*/
@interface AliRtcScreenShareEncoderConfiguration : NSObject

@property (nonatomic, assign) CGSize dimensions;
@property (nonatomic, assign) NSInteger frameRate;
@property (nonatomic, assign) NSInteger bitrate;
@property (nonatomic, assign) AliRtcRotationMode rotationMode;
@end


#pragma mark - 回调

@protocol AliRtcEngineDelegate <NSObject>
@optional

/**
 * @brief 加入频道结果
 * @param result 加入频道结果，成功返回0，失败返回错误码
 * @param channel 加入频道的id
 * @param elapsed 加入频道耗时
 * @note 此回调等同于joinChannel接口的block，二者择一处理即可
 */
- (void)onJoinChannelResult:(int)result channel:(NSString *_Nonnull)channel elapsed:(int) elapsed;

/**
 * @brief 离开频道结果
 * @param result 离开频道结果，成功返回0，失败返回错误码
 * @param stats 本次频道内会话的数据统计汇总。
 * @note 调用leaveChannel接口后返回，如果leaveChannel后直接调用destroy，将不会收到此回调
 */
- (void)onLeaveChannelResult:(int)result stats:(AliRtcStats)stats;

/**
 * @brief 音频推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onAudioPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 视频推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onVideoPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 次要流推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onDualStreamPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 屏幕分享推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onScreenSharePublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 当远端用户上线时会返回这个消息
 * @param uid userID 从App server分配的唯一标示符
 * @param elapsed 用户加入频道时的耗时
 * @note This message does not mean the user remains online state
 */
- (void)onRemoteUserOnLineNotify:(NSString *_Nonnull)uid elapsed:(int)elapsed;

/**
 * @brief 当远端用户下线时会返回这个消息
 * @param uid userID 从App server分配的唯一标示符
 * @param reason 用户离线的原因
 * @note This message does not mean the user remains offline state
 */
- (void)onRemoteUserOffLineNotify:(NSString *_Nonnull)uid offlineReason:(AliRtcUserOfflineReason)reason;

/**
 * @brief 当远端用户的流发生变化时，返回这个消息
 * @note 远方用户停止推流，也会发送这个消息
 */
- (void)onRemoteTrackAvailableNotify:(NSString *_Nonnull)uid audioTrack:(AliRtcAudioTrack)audioTrack videoTrack:(AliRtcVideoTrack)videoTrack;

/**
 * @brief 音频订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onAudioSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 相机流订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onVideoSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 屏幕分享流订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onScreenShareSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 大小流订阅情况变更回调
 * @param oldStreamType 之前的订阅的大小流类型
 * @param newStreamType 当前的订阅的大小流类型
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onSubscribeStreamTypeChanged:(NSString *_Nonnull)uid oldStreamType:(AliRtcVideoStreamType)oldStreamType newStreamType:(AliRtcVideoStreamType)newStreamType elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel;

/**
 * @brief 当用户角色发生变化化时通知
 * @param oldRole 变化前角色类型
 * @param newRole 变化后角色类型
 * @note 调用setClientRole方法切换角色成功时触发此回调
 */
- (void)onUpdateRoleNotifyWithOldRole:(AliRtcClientRole)oldRole newRole:(AliRtcClientRole)newRole;

/**
 * @brief 网络质量变化时发出的消息
 * @param uid 网络质量发生变化的uid
 * @param upQuality  上行网络质量
 * @param downQuality  下行网络质量
 * @note 当网络质量发生变化时触发，uid为@""时代表self的网络质量变化
 */
- (void)onNetworkQualityChanged:(NSString *_Nonnull)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality;

/**
 * @brief 网络连接断开
 */
- (void)onConnectionLost;

/**
 * @brief 网络连接正在尝试重连中
 */
- (void)onTryToReconnect;

/**
 * @brief 网络连接重连成功
 */
- (void)onConnectionRecovery;

/**
 * @brief 网络连接状态改变的回调
 * @param status 当前状态值, 对应值参考枚举AliRtcConnectionStatus
 * @param reason 引起状态变化的具体原因, 对应值参考枚举AliRtcConnectionStatusChangeReason
*/
- (void)onConnectionStatusChange:(AliRtcConnectionStatus)status reason:(AliRtcConnectionStatusChangeReason)reason;

/**
 * @brief 被服务器踢出频道的消息
 */
- (void)onBye:(int)code;

/**
 * @brief 如果engine出现warning，通过这个回调通知app
 * @param warn  Warning type
 * @param message 警告描述
 */
- (void)onOccurWarning:(int)warn message:(NSString *_Nonnull)message;

/**
 * @brief 如果engine出现error，通过这个回调通知app
 * @param error  Error type
 * @param message 错误描述
 */
- (void)onOccurError:(int)error  message:(NSString *_Nonnull)message;

/**
 * @brief 当前设备性能不足
 */
- (void)onPerformanceLow;

/**
 * @brief 当前设备性能恢复
*/
- (void)onPerformanceRecovery;

/**
 * @brief 音频首包数据发送成功
 * @param timeCost  发送耗时
 */
- (void)onFirstAudioPacketSentWithTimecost:(int)timeCost;

/**
 * @brief 视频首包数据发送成功
 * @param videoTrack  发送成功的视频流类型
 * @param timeCost  发送耗时
 */
- (void)onFirstVideoPacketSentWithVideoTrack:(AliRtcVideoTrack)videoTrack
                                    timeCost:(int)timeCost;

/**
 * @brief 音频首包数据接收成功
 * @param uid   User ID。从App server分配的唯一标示符
 * @param timeCost  接收耗时
 */
- (void)onFirstAudioPacketReceivedWithUid:(NSString *_Nonnull)uid
                                 timeCost:(int)timeCost;

/**
 * @brief 视频首包数据接收成功
 * @param uid   User ID。从App server分配的唯一标示符
 * @param videoTrack  接收成功的视频流类型
 * @param timeCost  接收耗时
 */
- (void)onFirstVideoPacketReceivedWithUid:(NSString *_Nonnull)uid
                          videoTrack:(AliRtcVideoTrack)videoTrack
                            timeCost:(int)timeCost;

/**
 * @brief 已解码远端音频首帧回调
 * @param uid  用户userId
 * @param elapsed  从本地用户加入频道直至该回调触发的延迟, 单位为毫秒
 */
- (void)onFirstRemoteAudioDecodedWithUid:(NSString *_Nonnull)uid elapsed:(int)elapsed;

/**
 * @brief remote user的第一帧视频帧显示时触发这个消息
 * @param uid   User ID。从App server分配的唯一标示符
 * @param videoTrack 屏幕流或者相机流
 * @param width 视频宽度
 * @param height 视频高度
 * @param elapsed 总耗时
  */
- (void)onFirstRemoteVideoFrameDrawn:(NSString *_Nonnull)uid videoTrack:(AliRtcVideoTrack)videoTrack width:(int)width height:(int)height elapsed:(int)elapsed;

/**
 * @brief 预览开始显示第一帧视频帧时触发这个消息
 * @param width 视频宽度
 * @param height 视频高度
 * @param elapsed 总耗时
 */
- (void)onFirstLocalVideoFrameDrawn:(int)width height:(int)height elapsed:(int)elapsed;

/**
 * @brief 订阅的音频数据回调
 * @param audioSource audio source
 * @param audioSample audio sample
 */
- (void)onAudioSampleCallback:(AliRtcAudioSource)audioSource audioSample:(AliRtcAudioDataSample *_Nonnull)audioSample;

/**
 * @brief 混音前的指定用户的音频数据
 * @param uid user id
 * @param audioSample the audio data sampl
 */
- (void)onPlaybackAudioFrameBeforeMixing:(NSString *_Nonnull)uid audioSample:(AliRtcAudioDataSample *_Nonnull)audioSample;

/**
 * @brief 订阅的音频音量，语音状态和uid
 * @param array 表示回调用户音量信息数组，包含用户uid,语音状态以及音量，uid为"0"表示本地说话人。
 * @param totalVolume 混音后的总音量，范围[0,255]。在本地用户的回调中，totalVolume;为本地用户混音后的音量；在远端用户的回调中，totalVolume; 为所有说话者混音后的总音量
 */
- (void)onAudioVolumeCallback:(NSArray <AliRtcUserVolumeInfo *> *_Nullable)array totalVolume:(int)totalVolume;

/**
 * @brief 订阅当前正在说话的人
 * @param uid 说话人uid, 为"0"表示本地说话人。其返回的是当前时间段内声音最大的用户ID，而不是瞬时声音最大的用户ID
*/
- (void)onActiveSpeaker:(NSString *_Nonnull)uid;

/**
 * @brief 视频数据输出格式
 * @return 期望视频输出格式
 * @note 在注册registerVideoSampleObserver后触发回调，应用可返回期望输出的视频数据格式，默认返回AliRtcYUV420
 */
- (AliRtcVideoFormat)onGetVideoFormatPreference;

/**
 * @brief 视频数据输出位置
 * @return 期望视频输出，参考AliRtcVideoObserPosition
 * @note 在注册registerVideoSampleObserver后触发回调，应用可返回期望输出的视频内容，对应数据将分别从onCaptureVideoSample/onPreEncodeVideoSample/onRemoteVideoSample
 * @note 默认返回全部类型数据，即AliRtcPositionPostCapture | AliRtcPositionPreRender | AliRtcPositionPreEncoder
 */
- (NSInteger)onGetVideoObservedFramePosition;

/**
 * @brief 订阅的本地采集视频数据回调
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
- (BOOL)onCaptureVideoSample:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample;

/**
 * @brief 订阅的本地编码前视频数据回调
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
- (BOOL)onPreEncodeVideoSample:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample;

/**
 * @brief 订阅的远端视频数据回调
 * @param uid user id
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
- (BOOL)onRemoteVideoSample:(NSString *_Nonnull)uid videoSource:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample;


/**
 * @brief 用户muteAudio通知
 * @param uid 执行muteAudio的用户
 * @param isMute YES:静音 NO:未静音
 */
- (void)onUserAudioMuted:(NSString *_Nonnull)uid audioMuted:(BOOL)isMute;

/**
 * @brief 用户muteVideo通知
 * @param uid 执行muteVideo的用户
 * @param isMute YES:推流黑帧 NO:正常推流
 */
- (void)onUserVideoMuted:(NSString *_Nonnull)uid videoMuted:(BOOL)isMute;

/**
 * @brief 用户EnableLocalVideo通知
 * @param uid 执行EnableLocalVideo的用户
 * @param isEnable YES:打开相机流采集 NO:关闭相机流采集
 */
- (void)onUserVideoEnabled:(NSString *_Nullable)uid videoEnabled:(BOOL)isEnable;

/**
 * @brief 用户audio被中断通知（一般用户打电话等音频被抢占场景）
 * @param uid audio被中断的用户
 */
- (void)onUserAudioInterruptedBegin:(NSString *_Nonnull)uid;

/**
 * @brief 用户audio中断结束通知（对应onUserAudioInterruptedBegin）
 * @param uid audio中断结束的用户
 */
- (void)onUserAudioInterruptedEnded:(NSString *_Nonnull)uid;

/**
 * @brief 远端用户应用退到后台
 * @param uid 用户
 */
- (void)onUserWillResignActive:(NSString *_Nonnull)uid;

/**
 * @brief 远端用户应用返回前台
 * @param uid 用户
 */
- (void)onUserWillBecomeActive:(NSString *_Nonnull)uid;

/**
 * @brief 订阅本地视频Texture 创建回调
 * @param context context
 */
- (void)onTextureCreate:(void *_Nullable)context;

/**
 * @brief 订阅本地视频Texture绘制回调
 * @param textureId texture id
 * @param width width
 * @param height height
 * @param videoSample video sample
 */
- (int)onTextureUpdate:(int)textureId width:(int)width height:(int)height videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample;

/**
 * @brief 订阅本地视频Texture销毁回调
 */
- (void)onTextureDestory;

/**
 * @brief 本地伴奏播放状态回调
 * @param playState 当前播放状态
 * @param errorCode 播放错误码
 */
- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState errorCode:(AliRtcAudioPlayingErrorCode)errorCode;

/**
 * @brief 远端用户伴奏播放开始回调
 * @param uid 用户uid
*/
- (void)onRemoteAudioAccompanyStarted:(NSString *_Nonnull)uid;

/**
 * @brief 远端用户伴奏播放结束回调
 * @param uid 用户uid
*/
- (void)onRemoteAudioAccompanyFinished:(NSString *_Nonnull)uid;

/**
 * @brief 本地音效播放结束回调
 * @param soundId 用户给该音效文件分配的ID
*/
- (void)onAudioEffectFinished:(int)soundId;

/**
 * @brief 网络质量探测回调
 * @param networkQuality 网络质量
 */
- (void)onLastmileDetectResultWithQuality:(AliRtcNetworkQuality)networkQuality;

/**
 * @brief 文件录制回调事件
 * @param event 录制事件
 * @param filePath 录制文件路径
 */
- (void)onMediaRecordEvent:(int)event filePath:(NSString *_Nullable)filePath;

/**
 * @brief 实时数据回调(2s触发一次)
 * @param stats stats
 */
- (void)onRtcStats:(AliRtcStats)stats;

/**
 * @brief 本地视频统计信息(2s触发一次)
 * @param localVideoStats 本地视频统计信息
 * @note SDK每两秒触发一次此统计信息回调
 */
- (void)onRtcLocalVideoStats:(AliRtcLocalVideoStats *_Nonnull)localVideoStats;

/**
 * @brief 远端视频统计信息(2s触发一次)
 * @param remoteVideoStats 远端视频统计信息
 */
- (void)onRtcRemoteVideoStats:(AliRtcRemoteVideoStats *_Nonnull)remoteVideoStats;

/**
 * @brief 本地音频统计信息(2s触发一次)
 * @param localAudioStats 本地视频统计信息
 * @note SDK每两秒触发一次此统计信息回调
 */
- (void)onRtcLocalAudioStats:(AliRtcLocalAudioStats *_Nonnull)localAudioStats;

/**
 * @brief 远端音频统计信息(2s触发一次)
 * @param remoteAudioStats 远端音频统计信息
 */
- (void)onRtcRemoteAudioStats:(AliRtcRemoteAudioStats *_Nonnull)remoteAudioStats;

/**
 * @brief 收到媒体扩展信息回调
 * @param uid 远端用户uid
 * @param data 媒体扩展信息
 */
- (void)onMediaExtensionMsgReceived:(NSString *_Nonnull)uid message:(NSData *_Nonnull)data;

/**
 * @brief 下行消息通道（接收消息）
 * @param messageInfo message
 */
- (void)onDownlinkMessageNotify:(AliRtcMessage *_Nonnull)messageInfo;

/**
 * @brief 发送上行消息后返回结果
 * @param resultInfo send message result
 */
- (void)onUplinkMessageResponse:(AliRtcMessageResponse *_Nonnull)resultInfo;

/**
 * @brief 语音路由发生变化回调
 * @param routing 当前使用的语音路由
 */
- (void)onAudioRouteChanged:(AliRtcAudioRouteType)routing;

/**
 * @brief 截图回调
 * @param uid 远端用户uid
 * @param videoTrack 截图视频track
 * @param image 截图数据
 * @param success 截图结果
 * @note 成功时image返回截图数据，失败返回nil
 */
- (void)onSnapshotComplete:(NSString*_Nullable)uid videoTrack:(AliRtcVideoTrack)videoTrack image:(UIImage* _Nullable)image success:(BOOL)success;

/**
 * @brief 旁路推流状态改变回调
 * @param streamURL  流地址
 * @param state  状态
 * @param errCode 错误码
 */
- (void)onPublishLiveStreamStateChanged:(NSString *_Nonnull)streamURL state:(AliRtcTrascodingLiveStreamStatus)state errCode:(AliRtcTrascodingLiveStreamErrorCode)errCode;

/**
 * @brief 旁路任务状态改变回调
 * @param streamURL  流地址
 * @param state  状态
 */
- (void)onPublishTaskStateChanged:(NSString *_Nonnull)streamURL state:(AliRtcTrascodingLiveStreamStatus)state;

/**
 * @brief 跨频道转推状态变化
 * @param state 当前状态，参考AliRtcChannelRelayState
 * @param code 错误码
 * @param message 状态信息
 */
- (void)onChannelRelayStateChanged:(int)state code:(int)code message:(NSString *_Nullable)message;

/**
 * @brief 跨频道转推事件回调
 * @param event 事件，参考AliRtcChannelRelayState
 */
- (void)onChannelRelayEvent:(int)event;

@end


#pragma mark - 接口主类

@interface AliRtcEngine : NSObject <AliRtcEngineDelegate>

/** remove for support multil channels */
//- (instancetype)init __attribute__((unavailable("use sharedInstance to create the singletone")));

#pragma mark - "Engine的生命周期，工作参数以及频道操作"

/**
 * @brief 初始化
 * @param delegate delegate
 * @param extras extras [可以为空 @""]
 * @return self
 */
+ (instancetype _Nonnull )sharedInstance:(id<AliRtcEngineDelegate>_Nullable)delegate extras:(NSString *_Nullable)extras;

/**
 * @brief 销毁
 */
+ (void)destroy;

/**
 * @brief 获取sdk版本号
 * @return SDK版本号
 * @note 在任何时刻都可以调用
 */
+ (NSString *_Nonnull)getSdkVersion;

/**
 * @brief 设置SDK日志文件保存路径
 * @param logDirPath 日志文件保存绝对路径
 * @return 0为成功，非0失败
 * @note 如需调用此接口，请在调用所有SDK接口前先进行设置，避免日志出现丢失，同时App必须保证指定的目录已存在且可写入
*/
+ (int)setLogDirPath:(NSString *_Nullable)logDirPath;

/**
 * @brief 上报日志
 */
+ (void)uploadLog;

/**
 * @brief 获取错误描述
 * @param errCode 错误码
 * @return 错误描述字符串
*/
+ (NSString *_Nullable)getErrorDescription:(NSInteger)errCode;

/**
 * @brief 是否分发回调到主线程
 * @param enabled YES:回调分发至主线程队列  NO:回调不分发至主线程队列
 * @note 回调默认分发至主线程队列(Default:YES)
 * @return 0表示Success 非0表示Failure
 */
- (int)enableDelegateMainQueue:(BOOL)enabled;

/**
 * @brief 指定回调线程队列
 * @param queue 回调queue
 * @note enableDelegateMainQueue为YES时，此接口设置无效，回调线程为主线程;
 * @note enableDelegateMainQueue为NO时，可通过此接口指定回调线程，若不设置，则使用SDK的默认子线程
 * @return 0表示Success 非0表示Failure
 */
- (int)setDelegateQueue:(NSOperationQueue *_Nullable)queue;

/**
 * @brief 设置音频模式还是音视频模式
 * @param audioOnly  YES表示只有音频推流和拉流；NO表示音视频都支持
 * @return 0表示Success 非0表示Failure
 * @note 只可以在joinChannel之前设置
 */
- (int)setAudioOnlyMode:(BOOL)audioOnly;

/**
 * @brief 加入频道
 * @param authInfo    认证信息，从App Server获取。
 * @param userName    任意用于显示的用户名称。不是User ID
 * @param onResult    当joinChannel执行结束后调用这个回调
 * @return 0为调用成功，非0失败
 * @note 异步接口。是否成功入会，通过onResult判断
 */
- (int)joinChannel:(AliRtcAuthInfo *_Nonnull)authInfo name:(NSString *_Nullable)userName onResult:(void(^_Nullable)(NSInteger errCode,NSString * _Nonnull channel,NSInteger elapsed))onResult;

/**
 * @brief 离开频道
 * @return 0为调用成功，非0失败
 * @note 如果当前不在频道内，leaveChannel不会有任何影响。leaveChannel会产生消息通知频道内其他用户
 */
- (int)leaveChannel;

/**
 * @brief 切换频道
 * @param authInfo    认证信息，从App Server获取。
 * @return 0表示Success 非0表示Failure
 * @note 本方法只允许在直播模式AliRtcInteractiveLive下，观看角色AliRtcClientRoleLive使用
 * @note 异步接口 调用此方法成功切换频道后，SDK会先触发离开原频道的回调onLeaveChannelResult，在返回加入新频道的回调onJoinChannelResult
 * @note 返回AliRtcErrSdkInvalidState请确认是否频道模式/角色不匹配，或当前未加入任何频道中
 * @note 返回AliRtcErrInvalidArguments请确认鉴权信息是否合法，或者是否加入相同频道
 * @note 返回AliRtcErrInner为SDK内部状态错误
 */
- (int)switchChannel:(AliRtcAuthInfo *_Nonnull)authInfo;

/**
 * @brief 设置频道模式
 * @param profile 频道模式类型
 * @return 0为成功，非0失败
 * @note 只可以在入会之前调用，会议中不可以重新设置，LeaveChannel后可以重新设置
 */
- (int)setChannelProfile:(AliRtcChannelProfile)profile;

/**
 * @brief 设置用户角色
 * @param role 用户角色类型
 * @return 0为成功，非0失败
 * @note 只可以在频道模式为InteractiveLive下调用，入会前/会议中均可设置，设置成功会收到onUpdateRoleNotify
 * @note 从Interactive转换为Live角色需要先停止推流，否则返回失败
 */
- (int)setClientRole:(AliRtcClientRole)role;

/**
 * @brief 获取当前用户角色
 * @return 当前用户角色
 */
- (AliRtcClientRole)getCurrentClientRole;

/**
 * @brief 更新鉴权信息
 * @param authInfo 鉴权信息
 * @return 0为成功，非0失败
*/
- (int)refreshAuthInfo:(AliRtcAuthInfo *_Nonnull)authInfo;


#pragma mark - "设置和操作本地的媒体"

/**
 * @brief 设置设备横竖屏方向
 * @param mode 设备方向
 * @return 0表示Success 非0表示Failure
 * @note 当前只支持固定横竖屏模式，仅允许在推流和预览之前进行设置
 */
- (int)setDeviceOrientationMode:(AliRtcOrientationMode)mode;

/**
 * @brief 为本地预览设置窗口以及绘制参数
 * @param viewConfig 包含了窗口以及渲染方式
 * @param track      must be AliVideoTrackCamera
 * @return 0表示Success 非0表示Failure
 * @note 支持joinChannel之前和之后切换窗口。如果viewConfig或者viewConfig中的view为nil，则停止渲染
 *       如果在播放过程中需要重新设置render mode，请保持canvas中其他成员变量不变，仅修改
 *       renderMode
 *       如果在播放过程中需要重新设置mirror mode，请保持canvas中其他成员变量不变，仅修改
 *       mirrorMode
 */
- (int)setLocalViewConfig:(AliVideoCanvas *_Nullable)viewConfig forTrack:(AliRtcVideoTrack)track;

/**
 * @brief 开始本地预览
 * @return 0表示Success 非0表示Failure
 * @note 如果没有设置view，则无法预览。可以在joinChannel之前就开启预览
 *       会自动打开摄像头
 */
- (int)startPreview;

/**
 * @brief 停止本地预览
 * @return 0表示Success 非0表示Failure
 * @note leaveChannel会自动停止本地预览
 *       会自动关闭摄像头 (如果正在publish camera流，则不会关闭摄像头)
 */
- (int)stopPreview;

/**
 * @brief 设置camera采集偏好
 * @param config 偏好设置，详见AliRtcCameraCapturerConfiguration参数
 * @return 0为成功，非0失败
 * @note 必须在首次打开摄像头之前设置，如startPreview，joinchannel，publish之前设置
*/
- (int)setCameraCapturerConfiguration:(AliRtcCameraCapturerConfiguration* _Nonnull )config;

/**
 * @brief 禁用或重新启用本地视频采集
 * @param enabled NO表示停止视频采集；YES表示恢复正常
 * @return 0表示Success 非0表示Failure
*/
- (int)enableLocalVideo:(BOOL)enabled;

/**
 * @brief 是否将停止本地视频数据发送
 * @param mute  YES表示视频数据发送黑帧；NO表示恢复正常
 * @param track  需要停止发送的track
 * @return 0表示Success 非0表示Failure
 * @note 发送黑色的视频帧。本地预览也呈现黑色。采集，编码，发送模块仍然工作，
 *       只是视频内容是黑色帧
*/
- (int)muteLocalCamera:(BOOL)mute forTrack:(AliRtcVideoTrack)track;

/**
 * @brief 是否将停止本地视频发送
 * @param enabled    YES表示发送视频；NO表示停止发送, 默认YES
 * @return 0表示Success 非0表示Failure
 * @note 加入频道前可以调用此接口配置是否推送视频流，并在加入频道后生效
 */
- (int)publishLocalVideoStream:(BOOL)enabled;

/**
 * @brief 设置视频编码属性
 * @param config     预定义的视频编码属性
 * @note 可以在join channel之前或者之后设置
*/
- (void)setVideoEncoderConfiguration:(AliRtcVideoEncoderConfiguration* _Nonnull)config;

/**
 * @brief 设置屏幕共享编码属性
 * @param config     预定义的屏幕共享编码属性
 * @note 可以在join channel之前或者之后设置
*/
- (void)setScreenShareEncoderConfiguration:(AliRtcScreenShareEncoderConfiguration* _Nonnull)config;

/**
 * @brief 停止/恢复本地音频数据发送
 * @param mute  YES表示本地音频发送空帧；NO表示恢复正常
 * @note mute是指音频流发送静音帧。采集和编码模块仍然在工作
 * @return 0为成功，非0失败
 */
- (int)muteLocalMic:(BOOL)mute __attribute__((deprecated("已经废弃，建议使用muteLocalMic:mode:接口")));

/**
 * @brief 停止/恢复本地音频数据发送
 * @param mute  YES表示本地音频发送空帧；NO表示恢复正常
 * @param mode  静音模式
 * @note mute是指音频流发送静音帧。采集和编码模块仍然在工作
 * @return 0为成功，非0失败
 */
- (int)muteLocalMic:(BOOL)mute mode:(AliRtcMuteLocalAudioMode)mode;

/**
 * @brief 发送/停止本地音频流
 * @param enabled  YES表示发送本地音频流；NO表示停止推流, 默认YES
 * @return 0表示Success 非0表示Failure
 * @note 加入频道前可以调用此接口配置是否推送音频流，并在加入频道后生效
*/
- (int)publishLocalAudioStream:(BOOL)enabled;

#pragma mark - "操作来自远端的媒体"

/**
 * @brief 为远端的视频设置窗口以及绘制参数
 * @param canvas canvas包含了窗口以及渲染方式
 * @param uid    User ID。从App server分配的唯一标示符
 * @param track  需要设置的track
 * @return 0表示Success 非0表示Failure
 * @note 支持joinChannel之前和之后切换窗口。如果canvas为nil或者view为nil，则停止渲染相应的流
 *       如果在播放过程中需要重新设置render mode，请保持canvas中其他成员变量不变，仅修改
 *       renderMode
 *       如果在播放过程中需要重新设置mirror mode，请保持canvas中其他成员变量不变，仅修改
 *       mirrorMode
 */
- (int)setRemoteViewConfig:(AliVideoCanvas *_Nullable)canvas uid:(NSString *_Nonnull)uid forTrack:(AliRtcVideoTrack)track;

/**
 * @brief 调节本地播放的指定远端用户音量
 * @param uid User ID。从App server分配的唯一标示符
 * @param volume 播放音量，取值范围:[0, 100]，0:静音，100:原始音量
 * @return 0表示Success，非0表示Failure
 */
- (int)setRemoteAudioVolume:(NSString *_Nonnull)uid volume:(NSInteger)volume;

/**
 * @brief 停止/恢复远端的音频流的播放
 * @param uid     User ID。从App server分配的唯一标示符
 * @param mute    YES表示停止播放；NO表示恢复播放
 * @return 0表示Success 非0表示Failure
 * @note 拉流和解码不受影响。支持joinChannel之前和之后设置
 */
- (int)muteRemoteAudioPlaying:(NSString *_Nonnull)uid mute:(BOOL)mute;

/**
 * @brief 停止/恢复远端的所有音频流的播放
 * @param mute    YES表示停止播放；NO表示恢复播放
 * @return 0表示Success 非0表示Failure
 * @note 拉流和解码不受影响。支持joinChannel之前和之后设置
 */
- (int)muteAllRemoteAudioPlaying:(BOOL)mute;

/**
 * @brief mute/unmute远端的所有视频track的渲染
 * @param mute    YES表示停止渲染；NO表示恢复渲染
 * @return 0表示Success 非0表示Failure
 * @note 拉流和解码不受影响。支持joinChannel之前和之后设置
 */
- (int)muteAllRemoteVideoRendering:(BOOL)mute;

#pragma mark - "用户管理"

/**
 * @brief 查询是否设置推送视频流
 * @return YES: 设置推送；NO: 未设置推送
 */
- (BOOL)isLocalVideoStreamPublished;

/**
 * @brief 查询是否设置推送音频流
 * @return YES: 设置推送；NO: 未设置推送
 */
- (BOOL)isLocalAudioStreamPublished;

/**
 * @brief 是否设置推送次要视频流
 * @param enabled    YES表示需要推送次流；NO表示不推送次流, 默认NO
 * @return 0表示Success 非0表示Failure
 * @note 加入频道前可以调用此接口配置是否推送次要流，并在加入频道后生效
 */
- (int)publishLocalDualStream:(BOOL)enabled;

/**
 * @brief 查询设置推送次要视频流
 * @return YES: 设置推送；NO: 未设置推送
 */
- (BOOL)isDualStreamPublished;

/**
 * @brief 启动屏幕分享
 * @return 0: 成功；其他: 失败
 */
- (int)startScreenShare;

/**
 * @brief 停止屏幕分享
 * @return 0: 成功；其他: 失败
 */
- (int)stopScreenShare;

/**
 * @brief 查询是否设置推送屏幕流
 * @return YES: 设置推送；NO: 未设置推送
 */
- (BOOL)isScreenSharePublished;

/**
 * @brief 设置订阅的相机流格式，大流或小流
 * @param uid  userId 从App server分配的唯一标示符
 * @param streamType 流类型
 * @note 如果发送端没有调用 enableDualStreamMode(false) 关闭双流模式，接收端可以选择订阅大流还是小流
 */
- (int)setRemoteVideoStreamType:(NSString *_Nonnull)uid type:(AliRtcVideoStreamType)streamType;

/**
 * @brief 设置默认订阅的相机流格式，大流或小流
 * @param streamType 流类型
 * @return 0为成功，非0失败
 */
- (int)setRemoteDefaultVideoStreamType:(AliRtcVideoStreamType)streamType;

/**
 * @brief 停止/恢复接收所有远端音频流
 * @param sub YES:接收所有用户的音频流 NO:停止接收所有用户的音频流
 * @note 该接口作为订阅远端音频流的总开关，如果设置为NO，则不仅当前会议中所有远端音频流都会停止订阅，后续入会的新用户也将不再订阅（即使设置了setDefaultSubscribeAllRemoteAudioStreams:YES）
 * @return 0为成功，非0失败
 */
- (int)subscribeAllRemoteAudioStreams:(BOOL)sub;

/**
 * @brief 停止/恢复接收所有远端视频流
 * @param sub YES:接收所有用户的视频流 NO:停止允许接收所有用户的视频流
 * @note 该接口作为订阅远端视频流的总开关，如果设置为NO，则不仅当前会议中所有远端视频流都会停止订阅，后续入会的新用户也将不再订阅（即使设置了setDefaultSubscribeAllRemoteVideoStreams:YES）
 * @return 0为成功，非0失败
 */
- (int)subscribeAllRemoteVideoStreams:(BOOL)sub;

/**
 * @brief 停止/恢复特定远端用户的音频流拉取
 * @param uid uid
 * @param sub YES:接收指定用户的音频流 NO:停止接收指定用户的音频流
 * @return 0为成功，非0失败
 * @note 如果之前有调用过 subscribeAllRemoteAudioStreams:NO 对所有远端音频进行静音，在调用本 API 之前请确保你已调用 subscribeAllRemoteAudioStreams:YES。
 * @note subscribeAllRemoteAudioStreams 是全局控制，subscribeRemoteAudioStream 是精细控制。
 */
- (int)subscribeRemoteAudioStream:(NSString *_Nonnull)uid sub:(BOOL)sub;

/**
 * @brief 停止/恢复特定远端用户的视频流拉取
 * @param uid uid
 * @param track 视频流类型
 * @param sub YES:接收指定用户的视频流 NO:停止接收指定用户的视频流
 * @return 0为成功，非0失败
 * @note 如果之前有调用过 subscribeAllRemoteVideoStreams:NO 暂停接收所有远端视频，在调用本 API 之前请确保你已调用 subscribeAllRemoteVideoStreams:YES。
 * @note subscribeAllRemoteVideoStreams 是全局控制，subscribeRemoteVideoStream 是精细控制。
 */
- (int)subscribeRemoteVideoStream:(NSString *_Nonnull)uid track:(AliRtcVideoTrack)track sub:(BOOL)sub;

/**
 * @brief 设置是否默认接收音频流
 * @param sub YES:接收用户的音频流 NO:停止接收用户的音频流
 * @return 0为成功，非0失败
 * @note 入会前入会后均可调用。如果在加入频道后调用 setDefaultSubscribeAllRemoteAudioStreams:NO，会接收不到设置后加入频道的用户的音频流。
 * @note 停止接收音频流后，如果想要恢复接收，请调用 subscribeRemoteAudioStream:uid sub:YES，并指定你想要接收的远端用户 uid；
 * @note 如果想恢复接收多个用户的音频流，则需要多次调用 subscribeRemoteAudioStream。setDefaultSubscribeAllRemoteAudioStreams:YES 只能恢复接收后面加入频道的用户的音频流。
 */
- (int)setDefaultSubscribeAllRemoteAudioStreams:(BOOL)sub;

/**
 * @brief 设置是否默认接收视频流
 * @param sub YES:接收用户的视频流 NO:不接收用户的视频流
 * @return 0为成功，非0失败
 * @note 入会前入会后均可调用。如果在加入频道后调用 setDefaultSubscribeAllRemoteVideoStreams:NO，会接收不到设置后加入频道的用户的视频流。
 * @note 停止接收视频流后，如果想要恢复接收，请调用 subscribeRemoteVideoStream:uid track:track sub:YES，并指定你想要接收的远端用户 uid；
 * @note 如果想恢复接收多个用户的视频流，则需要多次调用 subscribeRemoteVideoStream；setDefaultSubscribeAllRemoteVideoStreams:YES 只能恢复接收后面加入频道的用户的视频流。
 */
- (int)setDefaultSubscribeAllRemoteVideoStreams:(BOOL)sub;

/**
 * @brief 获取远端在线用户列表
 * @return 用户列表，保存的是用户ID
 */
- (NSArray<NSString *> *_Nullable)getOnlineRemoteUsers;

/**
 * @brief 判断用户是否在线
 * @param uid userId 从App server分配的唯一标示符。不可为nil
 * @return YES表示在线，NO表示不在线
 */
- (BOOL)isUserOnline:(NSString *_Nonnull)uid;

/**
 * @brief 获取当前网络链接状态
 * @return 当前状态
 * @note 入会前调用此接口，均返回状态AliRtcConnectionStatusDisconnected
 */
- (AliRtcConnectionStatus)getCurrentConnectionStatus;


#pragma mark - "设备管理"

/**
 * @brief 设置音频输出为听筒还是扬声器
 * @param enable YES:扬声器模式(默认扬声器)；NO:听筒模式
 * @return 0表示Success 非0表示Failure
 */
- (int)enableSpeakerphone:(BOOL)enable;

/**
 * @brief 获取当前音频输出为听筒还是扬声器
 * @return YES:扬声器模式；NO:听筒模式
 */
- (BOOL)isEnableSpeakerphone;

/**
 * @brief 设置录音音量
 * @param volume   [0 400],0:静音，>100:放大音量，<100:减小音量
 * @return 0表示Success 非0表示Failure
 */
- (int)setRecordingVolume:(NSInteger)volume;

/**
 * @brief 设置播放音量
 * @param volume   [0 400],0:静音，>100:放大音量，<100:减小音量
 * @return 0表示Success 非0表示Failure
 */
- (int)setPlayoutVolume:(NSInteger)volume;

/**
 * @brief 设置音频profile
 * @param audio_profile 音频采集/编码模式参数
 * @param audio_scene 音频场景模式参数
 * @return 0: 成功; 其他: 失败
 */
- (int)setAudioProfile:(AliRtcAudioProfile)audio_profile audio_scene:(AliRtcAudioScenario)audio_scene;

/**
 * @brief 设置SDK对AVAudioSession的控制权限
 * @param restriction 控制权限
 * @return 0为成功，非0失败
 */
- (int)setAudioSessionOperationRestriction:(AliRtcAudioSessionOperationRestriction)restriction;

/**
 * @brief 设置SDK设备音量类型
 * @param type 音量类型
 * @return 0为成功，非0失败
 */
- (int)setDeviceVolumeType:(AliRtcDeviceVolumeType)type;

/**
 * @brief 切换前后摄像头
 * @return 0表示Success 非0表示Failure
 * @note 只有iOS和android提供这个接口
 */
- (int)switchCamera;

/**
 * @brief 获取当前摄像头方向，默认前置摄像头
 * @return 摄像头方向
 */
- (AliRtcCameraDirection)getCurrentCameraDirection;

/**
 * @brief 设置摄像头参数
 * @param zoom   zoom的级别
 * @return 0表示Success 非0表示Failure
 */
 - (int)setCameraZoom:(float)zoom;

/**
 * @brief 设置摄像头参数
 * @param flash  是否允许闪光灯
 * @return 0表示Success 非0表示Failure
 */
- (int)setCameraFlash:(BOOL)flash;

/**
 * @brief 摄像头是否支持手动聚焦
 * @return YES表示支持 NO表示不支持
 */
- (BOOL)isCameraFocusPointSupported;

/**
 * @brief 摄像头是否支持设置曝光区域
 * @return YES表示支持 NO表示不支持
 */
- (BOOL)isCameraExposurePointSupported;

/**
 * @brief 设置摄像头手动聚焦
 * @param point   聚焦点
 * @return 0表示Success 非0表示Failure
 */
- (int)setCameraFocusPoint:(CGPoint)point;

/**
 * @brief 设置摄像头曝光点
 * @param point  曝光点
 * @return 0表示Success 非0表示Failure
 */
- (int)setCameraExposurePoint:(CGPoint)point;

/**
 * @brief 摄像头是否支持人脸聚焦
 * @return YES 表示支持 NO表示不支持
*/
- (BOOL)isCameraAutoFocusFaceModeSupported;

/**
 * @brief 设置摄像头人脸对焦
 * @param enable  YES: 开启; NO:关闭
 * @return YES表示成功 NO表示失败
*/
- (BOOL)setCameraAutoFocusFaceModeEnabled:(BOOL)enable;

/**
 * @brief 开启音频采集
 * @note 此接口可以控制提前打开音频采集，如果不设置，则SDK会在合适的时机在打开音频采集
 * @note 调用此接口后，离会后采集设备保持开启状态
 */
- (void)startAudioCapture;

/**
 * @brief 开启音频采集
 * @note 此接口可以控制提前打开音频采集，如果不设置，则SDK会在合适的时机在打开音频采集
 * @param keepAlive YES: 离会后采集设备保持开启状态；NO: 离会后采集设备关闭
 */
- (void)startAudioCapture:(BOOL)keepAlive;

/**
 * @brief 关闭音频采集
 * @note 此接口可以控制关闭音频采集，与startAudioCapture对应
 * @note 调用此接口后，入会后、离会再入会，采集设备保持关闭状态
 */
- (void)stopAudioCapture;

/**
 * @brief 开启音频播放
 * @note 此接口可以控制提前打开音频播放，如果不设置，则SDK会在合适的时机在打开音频播放
 */
- (void)startAudioPlayer;

/**
 * @brief 关闭音频播放
 * @note 此接口可以控制关闭音频播放，与stopAudioPlayer对应
 */
- (void)stopAudioPlayer;


#pragma mark - "其他"

/**
 * @brief 设置log
 * @param logLevel  Log级别
 */
- (void)setLogLevel:(AliRtcLogLevel)logLevel;

/**
 * @brief 检查camera是否打开
 * @return YES表示摄像头已打开；NO表示摄像头没有打开
 */
- (BOOL)isCameraOn;

/**
 * @brief 检查当前是否在频道中
 * @return YES表示加入了频道；NO表示不在频道中
 */
- (BOOL)isInCall;

/**
 * @brief 查询是否audio only
 * @return YES表示audio Only  NO表示非audio Only
 */
- (BOOL)isAudioOnly;

/**
 * @brief 查询远端用户的各种状态
 * @param uid userId 从App server分配的唯一标示符
 * @note Key: userID, isOnline, sessionID, callID, displayName
 *            hasAudio, hasCameraMaster, hasCameraSlave, hasScreenSharing
 *            requestAudio, requestCameraMaster, requestCameraSlave, requestScreenSharing, preferCameraMaster
 *            subScribedAudio, subScribedCameraMaster, subScribedCamearSlave, subScribedScreenSharing
 *            hasCameraView, hasScreenView
 *            muteAudioPlaying
 */
- (NSDictionary *_Nullable)getUserInfo:(NSString *_Nonnull)uid;

/**
 * @brief 设置H5兼容模式
 * @param comp    YES: 兼容H5; NO: 传统模式，不兼容H5
 * @note 只支持在sdk实例创建之前设置。如果在sdk创建之后改这个设置会引起互通问题。缺省不兼容H5
 */
+ (void)setH5CompatibleMode:(BOOL)comp;

/**
 * @brief 检查当前是否兼容H5
 * @return YES: 兼容H5; NO: 不兼容H5
 */
+ (BOOL)getH5CompatibleMode;


#pragma mark - "音视频裸数据输入输出"

/**
 * @brief 设置回调音频声道数，默认单声道 （混音前数据不支持该参数设置）
 * @param audioNumChannel audio num channel
 */
- (void)setSubscribeAudioNumChannel:(AliRtcAudioNumChannel)audioNumChannel;

/**
 * @brief 设置回调音频采样率，默认44100（混音前数据不支持该参数设置）
 * @param audioSampleRate audio samplerate
 */
- (void)setSubscribeAudioSampleRate:(AliRtcAudioSampleRate)audioSampleRate;

/**
 * @brief 订阅音频数据
 * @param audioSource audio source
 * @note 订阅音频数据输出前，需先通过SetSubscribeAudioNumChannel与SetSubscribeAudioSampleRate设置输出音频数据参数
 */
- (void)subscribeAudioData:(AliRtcAudioSource)audioSource;

/**
 * @brief 取消订阅音频数据
 * @param audioSource audio source
 */
- (void)unSubscribeAudioData:(AliRtcAudioSource)audioSource;

/**
 * @brief 设置音量回调频率和平滑系数
 * @param interval 时间间隔，单位毫秒，最小值不得小于10ms, 建议设置300-500ms, <= 0表示不启用音量提示和说话人提示功能
 * @param smooth 平滑系数，数值越大平滑程度越高，反之越低，实时性越好，建议设置3，范围[0, 9];
 * @param reportVad 说话人检测开关，1：开启，通过onAudioVolumeCallback会回调每一个说话人的状态, 0:关闭
 * @return 0:成功，<0:方法调用失败
 * @note 设置之后，音频音量和说话人uid会分别通过onAudioVolumeCallback和onActiveSpeaker回调
*/
- (int)enableAudioVolumeIndication:(NSInteger)interval smooth:(NSInteger)smooth reportVad:(NSInteger)reportVad;

/**
 * @brief 订阅视频数据
*/
- (void)registerVideoSampleObserver;

/**
 * @brief 取消订阅视频数据
*/
- (void)unregisterVideoSampleObserver;

/**
 * @brief 订阅本地视频纹理数据
 */
- (void)registerLocalVideoTexture;

/**
 * @brief 取消订阅本地视频纹理数据
 */
- (void)unregisterLocalVideoTexture;

/**
 * @brief 是否启用外部视频输入源
 * @param enable YES 开启，NO 关闭
 * @param useTexture 是否使用texture 模式
 * @param type 流类型
 * @param renderMode 输入视频比例和推流profile不一致时，按照设置的renderMode进行对应处理
 * @return 0为成功，非0失败
 */
- (int)setExternalVideoSource:(BOOL)enable useTexture:(BOOL)useTexture sourceType:(AliRtcVideoSource)type renderMode:(AliRtcRenderMode)renderMode;

/**
 * @brief 输入视频数据
 * @param frame 帧数据
 * @param type 流类型
 * @return 0为成功，非0失败
 * @note 如果返回值为errorCode中的AliRtcErrVideoBufferFull，代表当前buffer队列塞满，需要等待后再继续输送数据
 */
- (int)pushExternalVideoFrame:(AliRtcVideoDataSample *_Nonnull)frame sourceType:(AliRtcVideoSource)type;

/**
 * @brief 设置是否启用外部音频输入源
 * @param enable YES 开启，NO 关闭
 * @param sampleRate 采样率 16k, 48k...
 * @param channelsPerFrame 声道数 1, 2
 * @return return>=0 Success, return<0 Failure
 */
- (int)setExternalAudioSource:(BOOL)enable withSampleRate:(NSUInteger)sampleRate channelsPerFrame:(NSUInteger)channelsPerFrame;

/**
 * @brief 输入音频数据
 * @param data 音频数据 不建议超过40ms数据
 * @param samples 采样
 * @param timestamp 时间戳
 * @return return>=0 Success, return<0 Failure
 * @note 如果返回值为errorCode中的AliRtcErrAudioBufferFull，代表当前buffer队列塞满，需要等待后再继续输送数据，，建议等待20ms
 */
- (int)pushExternalAudioFrameRawData:(void *_Nonnull)data samples:(NSUInteger)samples timestamp:(NSTimeInterval)timestamp;

/**
 * @brief 设置混音音量
 * @param vol 音量 0-100
 * @return 0为成功，非0失败
 */
- (int)setExternalAudioVolume:(int)vol;

/**
 * @brief 获取混音音量
 * @return 混音音量
 */
- (int)getExternalAudioVolume;

/**
 * @brief 设置是否与麦克风采集音频混合
 * @param mixed YES 混音，NO 完全替换麦克风采集数据
 * @return 0为成功，非0失败
 */
- (int)setMixedWithMic:(BOOL)mixed;

/**
 * @brief 设置是否启用外部输入音频播放
 * @param enable YES 开启，NO 关闭
 * @param sampleRate 采样率 16000 48000...
 * @param channelsPerFrame 声道数 1 or 2
 * @return return>=0 Success, return<0 Failure
 */
- (int)setExteranlAudioRender:(BOOL)enable sampleRate:(NSUInteger)sampleRate channelsPerFrame:(NSUInteger)channelsPerFrame;

/**
 * @brief 输入音频播放数据
 * @param audioSamples 音频数据
 * @param sampleLength 音频数据长度
 * @param sampleRate 音频采样率
 * @param channelsPerFrame 音频声道数
 * @param timestamp 时间戳
 * @return return>=0 Success, return<0 Failure
 * @note 如果返回值为errorCode中的AliRtcErrAudioBufferFull，代表当前buffer队列塞满，需要等待后再继续输送数据，建议等待20ms
 */
- (int)pushExternalAudioRenderRawData:(const void* _Nullable)audioSamples sampleLength:(NSUInteger)sampleLength sampleRate:(NSUInteger)sampleRate channelsPerFrame:(NSUInteger)channelsPerFrame timestamp:(long long)timestamp;


#pragma mark - "音乐伴奏音效"

/**
 * @brief 设置变调参数
 * @param value 值，范围[0.5, 2.0], 默认1.0表示音调不变。
 * @return 0表示Success 非0表示Failure
 */
- (int)setAudioEffectPitchValue:(double)value;

/**
 * @brief 设置变声音效模式
 * @param mode 模式值，参看对应枚举类型
 * @return 0表示Success 非0表示Failure
 */
- (int)setAudioEffectVoiceChangerMode:(AliRtcAudioEffectVoiceChangerMode)mode;

/**
 * @brief 设置混响音效模式
 * @param mode 模式值，参看对应枚举类型
 * @return 0表示Success 非0表示Failure
 */
- (int)setAudioEffectReverbMode:(AliRtcAudioEffectReverbMode)mode;

/**
 * @brief 设置混响音效类型
 * @param type 模式值，参看对应枚举类型
 * @return 0表示Success 非0表示Failure
 */
- (int)setAudioEffectReverbParamType:(AliRtcAudioEffectReverbParamType)type value:(float)value;

/**
 * @brief 开始伴奏混音
 * @param onlyLocalPlay 是否只本地播放
 * @param replaceMic 是否替换掉MIC
 * @param loopCycles 循环次数(可以设置-1或者正整数)
 * @return 返回0为成功，其他返回错误码
 * @note 异步接口，可通过onAudioPlayingStateChanged监听播放器状态
 */
- (int)startAudioAccompanyWithFile:(NSString *_Nonnull)filePath onlyLocalPlay:(BOOL)onlyLocalPlay replaceMic:(BOOL)replaceMic loopCycles:(NSInteger)loopCycles;

/**
 * @brief 停止伴奏混音
 * @return 返回0为成功，其他返回错误码
 */
- (int)stopAudioAccompany;

/**
 * @brief 设置伴奏混音音量
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioAccompanyVolume:(NSInteger)volume;

/**
 * @brief 设置伴奏混音之后推流出去的音量
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioAccompanyPublishVolume:(NSInteger)volume;

/**
 * @brief 获取伴奏推流出去的混音音量
 * @return 返回0~100为成功，其他返回错误码
 */
- (int)getAudioAccompanyPublishVolume;

/**
 * @brief 设置伴奏混音之后本地播放的音量
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioAccompanyPlayoutVolume:(NSInteger)volume;

/**
 * @brief 获取伴奏混音本地播放的音量
 * @return 返回0~100为成功，其他返回错误码
 */
- (int)getAudioAccompanyPlayoutVolume;


/**
 * @brief 暂停伴奏混音
 * @return 返回0为成功，其他返回错误码
 */
- (int)pauseAudioAccompany;

/**
 * @brief 重新开始伴奏混音
 * @return 返回0为成功，其他返回错误码
 */
- (int)resumeAudioAccompany;

/**
 * @brief 获取伴奏文件时长, 单位为ms
 * @return 返回伴奏文件时长，小于0返回错误码
 */
- (int)getAudioAccompanyDuration;

/**
 * @brief 获取伴奏文件播放进度，单位为毫秒。
 * @return 返回伴奏文件播放进度，小于0返回错误码
 */
- (int)getAudioAccompanyCurrentPosition;

/**
 * @brief 设置伴奏文件的播放位置
 * @param pos 进度条位置，单位为毫秒
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioAccompanyPosition:(int)pos;

/**
 * @brief 预加载音效文件
 * @param soundId 用户给该音效文件分配的ID
 * @param filePath 音效文件路径
 * @return 返回0为成功，其他返回错误码
 */
- (int)preloadAudioEffectWithSoundId:(NSInteger)soundId filePath:(NSString *_Nonnull)filePath;

/**
 * @brief 删除预加载的音效文件
 * @param soundId 用户给该音效文件分配的ID
 * @return 返回0为成功，其他返回错误码
 */
- (int)unloadAudioEffectWithSoundId:(NSInteger)soundId;

/**
 * @brief 开始播放音效
 * @param soundId 用户给该音效文件分配的ID
 * @param filePath 文件路径
 * @param cycles 循环次数(可以设置-1或者正整数)
 * @param publish 是否发布
 * @return 返回0为成功，其他返回错误码
 */
- (int)playAudioEffectWithSoundId:(NSInteger)soundId filePath:(NSString *_Nonnull)filePath cycles:(NSInteger)cycles publish:(BOOL)publish;

/**
 * @brief 停止播放音效
 * @param soundId 用户给该音效文件分配的ID
 * @return 返回0为成功，其他返回错误码
 */
- (int)stopAudioEffectWithSoundId:(NSInteger)soundId;

/**
 * @brief 停止播放所有音效
 * @return 返回0为成功，其他返回错误码
*/
- (int)stopAllAudioEffects;

/**
 * @brief 设置音效推流音量
 * @param soundId 用户给该音效文件分配的ID
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioEffectPublishVolumeWithSoundId:(NSInteger)soundId volume:(NSInteger)volume;

/**
 * @brief 获取推流音效音量
 * @param soundId 用户给该音效文件分配的ID
 * @return 返回0~100为成功，其他返回错误码
 */
- (int)getAudioEffectPublishVolumeWithSoundId:(NSInteger)soundId;

/**
 * @brief 设置音效本地播放音量
 * @param soundId 用户给该音效文件分配的ID
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAudioEffectPlayoutVolumeWithSoundId:(NSInteger)soundId volume:(NSInteger)volume;

/**
 * @brief 获取音效本地播放音量
 * @param soundId 用户给该音效文件分配的ID
 * @return 返回0~100为成功，其他返回错误码
 */
- (int)getAudioEffectPlayoutVolumeWithSoundId:(NSInteger)soundId;

/**
 * @brief 设置所有音效本地播放音量
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAllAudioEffectsPlayoutVolume:(NSInteger)volume;

/**
 * @brief 设置所有音效推流音量
 * @param volume 混音音量 0~100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setAllAudioEffectsPublishVolume:(NSInteger)volume;

/**
 * @brief 暂停音效
 * @param soundId 用户给该音效文件分配的ID
 * @return 返回0为成功，其他返回错误码
 */
- (int)pauseAudioEffectWithSoundId:(NSInteger)soundId;

/**
 * @brief 暂停所有音效
 * @return 返回0为成功，其他返回错误码
*/
- (int)pauseAllAudioEffects;

/**
 * @brief 重新开始播放音效
 * @@param soundId 用户给该音效文件分配的ID
 * @return 返回0为成功，其他返回错误码
 */
- (int)resumeAudioEffectWithSoundId:(NSInteger)soundId;

/**
 * @brief 重新开始播放所有音效
 * @return 返回0为成功，其他返回错误码
*/
- (int)resumeAllAudioEffects;

/**
 * @brief 是否开启语音活动检测
 * @param enable true表示开启,false表示关闭， 默认false
 * @note joinchannel之后，推流之前调用有效。
 * @note 开启语音活动检测可以在检测到没有语音的情况下，直接发送静音包，节省用户流量。
 * @return 0表示Success 非0表示Failure
 */
- (int)enableAudioDTX:(BOOL)enable;

/**
 * @brief 开启音频静音检测（静音不发送音频包）
 * @param enable true表示开启，false表示关闭，默认false;
 * @note joinchannel之后，推流之前调用有效。开启此选项可以在关闭麦克风或者静音的状态下，停止音频包的发送，节省用户流量。
 * @return 0表示Success 非0表示Failure
*/
- (int)enableAudioAMD:(BOOL)enable;

/**
 * @brief 启用耳返
 * @param enable 是否启用耳返
 * @return 返回0为成功，其他返回错误码
 */
- (int)enableEarBack:(BOOL)enable;

/**
 * @brief 设置耳返音量
 * @param volume 音量 0~100 默认100
 * @return 返回0为成功，其他返回错误码
 */
- (int)setEarBackVolume:(NSInteger)volume;

 /**
  * @brief 开启智能降噪
  * @note 此接口可以通话过程中控制打开智能降噪功能，通话过程中可以支持开启和关闭智能降噪；
  * @note 默认关闭，开启后可能导致功耗增加，智能降噪适合于会议，教育等语音通讯为主的场景，不适合有背景音乐的场景；
  * @return 0为成功，非0失败
  */
- (int)startIntelligentDenoise;

/**
 * @brief 关闭智能降噪
 * @note 此接口可以通话过程中控制关闭智能降噪功能
 */
- (void)stopIntelligentDenoise;

/**
 * 启用音乐模式
 * @param enable 是否启用音乐模式, true表示开启, false表示关闭, 默认关闭
 * @return 返回0为成功，其他返回错误码
 */
- (int)enableMusicMode:(BOOL)enable;

#pragma mark - "文件录制"

/**
 * @brief 开始录制
 * @param recordType 录制类型
 * @param recordFormat 录制文件格式
 * @param filePath 文件路径
 * @param audioConfig 音频设置
 * @param videoConfig 视频设置
 * @return 成功/失败
 */
- (BOOL)startRecord:(AliRtcRecordType)recordType recordFormat:(AliRtcRecordFormat)recordFormat filePath:(NSString*_Nonnull)filePath audioConfig:(AliRtcRecordAudioConfig*_Nullable)audioConfig videoConfig:(AliRtcRecordVideoConfig*_Nullable)videoConfig;


/**
 @brief 停止录制
 */
- (void)stopRecord;


#pragma mark - "网络测试"

/**
 * @brief 开始网络质量探测
 * @return 0表示Success 非0表示Failure
 * @note 请在joinChannel之前调用，探测结果在onLastmileDetect回调
 */
- (int)startLastmileDetect;

/**
 * @brief 停止网络质量探测
 * @return 0表示Success 非0表示Failure
 */
- (int)stopLastmileDetect;


#pragma mark - "低延时互动直播模式"

/**
 * @brief 开始低延时互动直播拉流
 * @param authInfo 入会鉴权信息
 * @param onResult 拉流结果 errCode=0为Success，其他为Failure
 */
- (void)startLiveStreamingWithAuthInfo:(AliRtcAuthInfo *_Nonnull)authInfo onResult:(void(^_Nullable)(int errCode))onResult;

/**
 * @brief 停止低延时互动直播拉流
 */
- (int)stopLiveStreaming;

/**
 * @brief 设置低延时互动直播拉流渲染Canvas
 * @param canvas 渲染参数
 */
- (int)setLiveStreamingViewConfig:(AliVideoCanvas *_Nullable)canvas;

#pragma mark - "美颜控制"
/**
 * @brief 设置美颜
 * @param enable 美颜开关
 * @param config 美颜参数控制
*/
- (int)setBeautyEffect:(BOOL)enable config:(AliRtcBeautyConfig *_Nullable)config;

#pragma mark - "视频画面截图"
/**
 * @brief 视频画面截图
 */
- (int)snapshotVideo:(NSString*_Nullable)userId type:(AliRtcVideoTrack)type;

#pragma mark - "媒体扩展信息"

/**
 * @brief 发送媒体扩展信息
 */
- (int)sendMediaExtensionMsg:(NSData *_Nonnull)data repeatCount:(int)repeatCount;

/**
 * @brief 添加水印
 * @param imageUrl png 文件路径
 * @param config 水印配置 详见:AliRtcWatermarkConfig
 * @param track 流类型
*/
- (int)addVideoWatermark:(AliRtcVideoTrack)track image:(NSString*_Nonnull)imageUrl config:(AliRtcWatermarkConfig *_Nullable)config;

/**
 * @brief 去除水印
 * @param track 流类型
*/
- (int)clearVideoWatermark:(AliRtcVideoTrack)track;

#pragma mark - "问题上报"

/**
 * @brief SDK问题反馈
 * @param uid 当前 uid(允许为nil)
 * @param channelId 当前channel id(允许为nil)
 * @param description 问题描述(支持中英文, Nonnull)
 * @param type 问题类型(见AliRtcFeedbackType注释)
 * @param timeStamp 问题发生的时间戳(Unix时间戳，大致时间，无需特别精确，可以为0)
 */
- (void)postFeedbackWithUid:(NSString *_Nullable)uid channleId:(NSString *_Nullable)channelId description:(NSString *_Nonnull)description type:(AliRtcFeedbackType)type timeStamp:(NSTimeInterval)timeStamp;

#pragma mark - "消息通道"

/**
 * @brief 下行消息通道 反馈消息处理结果
 * @param messageInfo message
 * @note 已废弃使用
 */
- (int)sendDownlinkMessageResponse:(AliRtcMessage *_Nonnull)messageInfo;

/**
 * @brief 发送上行通道消息
 * @param messageInfo message
 * @note 已废弃使用
 */
- (int)sendUplinkMessage:(AliRtcMessage *_Nonnull)messageInfo;

#pragma mark - "多频道"
/**
 * @brief 创建子频道
 * @param delegate delegate
 * @param extras extras [可以为空 @""]
 * @return 成功返回子频道实例，失败返回nil
 */
- (AliRtcEngine *_Nullable)createChannelWithDelegate:(id<AliRtcEngineDelegate>_Nonnull)delegate extras:(NSString *_Nullable)extras;

/**
 * @brief 销毁子频道
 */
- (void)destroyChannel;

#pragma mark - "旁路转推"

/**
 * @brief 开启旁路直播
 * @param streamURL 推流地址
 * @param trancoding 推流所需参数，详见AliRtcLiveTranscoding
 * @return 0:Success 非0:Failure
 */
- (int)startPublishLiveStreamWithURL:(NSString *_Nonnull)streamURL liveTranscoding:(AliRtcLiveTranscoding *_Nonnull)trancoding;

/**
 * @brief 更新旁路直播相关参数
 * @param streamURL 推流地址
 * @param trancoding 推流所需参数，详见AliRtcLiveTranscoding
 * @return 0:Success 非0:Failure
 */
- (int)updatePublishLiveStreamWithURL:(NSString *_Nonnull)streamURL liveTranscoding:(AliRtcLiveTranscoding *_Nonnull)trancoding;

/**
 * @brief 停止旁路直播
 * @param streamURL 推流地址
 * @return 0:Success 非0:Failure
 */
- (int)stopPublishLiveStreamWithURL:(NSString *_Nonnull)streamURL;

#pragma mark - "跨频道转推"
/**
 * @brief 开始跨频道转推
 * @param configuration 配置
 * @return 0 Success 非0 Failure
 */
- (int)startChannelRelay:(AliRtcChannelRelayConfiguration *_Nonnull)configuration;

/**
 * @brief 更新跨频道转推
 * @param configuration 配置
 * @return 0 Success 非0 Failure
 */
- (int)updateChannelRelay:(AliRtcChannelRelayConfiguration *_Nonnull)configuration;

/**
 * @brief 停止跨频道转推
 * @return 0 Success 非0 Failure
 */
- (int)stopChannelRelay;

/**
 * @brief 设置视频流附加属性
 * @param track 视频流类型
 * @param attr 流属性
 * @note 已废弃使用
*/
- (void)setPublishVideoStreamAttribute:(AliRtcVideoTrack)track attr:(NSString *_Nullable)attr;
@end
