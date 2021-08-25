//
//  AlivcLivePusher.h
//  AlivcLiveCaptureLib
//
//  Created by TripleL on 17/7/13.
//  Copyright © 2017年 Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <VideoToolbox/VideoToolbox.h>
#import <UIKit/UIKit.h>
#import <ReplayKit/ReplayKit.h>
#import "AlivcLivePushConfig.h"
#import "AlivcLivePushStatsInfo.h"
#import "AlivcLivePushError.h"


/**
 错误回调, 网络回调, 状态回调, BGM回调, 滤镜回调
 */
@protocol AlivcLivePusherErrorDelegate,
AlivcLivePusherNetworkDelegate,
AlivcLivePusherInfoDelegate,
AlivcLivePusherBGMDelegate,
AlivcLivePusherCustomFilterDelegate,
AlivcLivePusherCustomDetectorDelegate,
AlivcLivePusherSnapshotDelegate;



/**
 推流类
 */
@interface AlivcLivePusher : NSObject



/**
 显示调试悬浮窗
 * 注意 ：Debug悬浮窗会占用一定的系统资源，只可用于APP研发的Debug阶段，Release版本请勿调用此接口
 */
+ (void)showDebugView;



/**
 隐藏调试悬浮窗
 */
+ (void)hideDebugView;


/**
 init

 @param config 推流配置
 @return self:success  nil:failure
 */
- (instancetype)initWithConfig:(AlivcLivePushConfig *)config;


/**
 设置推流错误监听回调

 @param delegate AlivcLivePusherErrorDelegate
 */
- (void)setErrorDelegate:(id<AlivcLivePusherErrorDelegate>)delegate;


/**
 设置推流状态监听回调

 @param delegate AlivcLivePusherInfoDelegate
 */
- (void)setInfoDelegate:(id<AlivcLivePusherInfoDelegate>)delegate;


/**
 设置推流网络监听回调

 @param delegate AlivcLivePusherNetworkDelegate
 */
- (void)setNetworkDelegate:(id<AlivcLivePusherNetworkDelegate>)delegate;

/**
 设置用户自定义滤镜回调
 
 @param delegate AlivcLivePusherCustomFilterDelegate
 */
- (void)setCustomFilterDelegate:(id<AlivcLivePusherCustomFilterDelegate>)delegate;


/**
 设置用户自定义人脸识别回调
 
 @param delegate AlivcLivePusherCustomDetectorDelegate
 */
- (void)setCustomDetectorDelegate:(id<AlivcLivePusherCustomDetectorDelegate>)delegate;

/**
 设置背景音乐监听回调

 @param delegate AlivcLivePusherBGMDelegate
 */
- (void)setBGMDelegate:(id<AlivcLivePusherBGMDelegate>)delegate;


/**
 开始预览 同步接口

 @param previewView 预览view
 @return 0:success  非0:failure
 */
- (int)startPreview:(UIView *)previewView;


/**
 停止预览

 @return 0:success  非0:failure
 */
- (int)stopPreview;


/**
 开始推流 同步接口

 @param pushURL 推流URL
 @return 0:success  非0:failure
 */
- (int)startPushWithURL:(NSString *)pushURL;


/**
 停止推流
 
 @return 0:success  非0:failure
 */
- (int)stopPush;


/**
 重新推流 同步接口

 @return 0:success  非0:failure
 */
- (int)restartPush;


/**
 暂停推流
 
 @return 0:success  非0:failure
 */
- (int)pause;



/**
 恢复推流 同步接口

 @return 0:success  非0:failure
 */
- (int)resume;


/**
 重连 异步接口

 @return 0:success  非0:failure
 */
- (int)reconnectPushAsync;

/**
 重连 异步接口
 
 @return 0:success  非0:failure
 */
- (int)reconnectPushAsync:(NSString *)pushURL;

/**
 销毁推流
 */
- (void)destory;



/* ***********************异步接口*********************** */
/**
 开始预览 异步接口
 
 @param preview 预览view
 @return 0:success  非0:failure
 */
- (int)startPreviewAsync:(UIView *)preview;


/**
 开始推流 异步接口
 
 @param pushURL 推流URL
 @return 0:success  非0:failure
 */
- (int)startPushWithURLAsync:(NSString *)pushURL;


/**
 重新推流 异步接口
 
 @return 0:success  非0:failure
 */
- (int)restartPushAsync;


/**
 恢复推流 异步接口
 
 @return 0:success  非0:failure
 */
- (int)resumeAsync;

/* ****************************************************** */



/**
 旋转摄像头
 
 @return 0:success  非0:failure
 */
- (int)switchCamera;


/**
 设置自动对焦
 
 @param autoFocus true:自动对焦 false:手动对焦
 @return 0:success  非0:failure
 */
- (int)setAutoFocus:(bool)autoFocus;


/**
 对焦
 
 @param point 对焦的点
 @param autoFocus 是否自动对焦
 @return 0:success  非0:failure
 */
- (int)focusCameraAtAdjustedPoint:(CGPoint)point autoFocus:(bool)autoFocus;


/**
 缩放
 
 @param zoom 缩放值[0:MaxZoom]
 @return 0:success  非0:failure
 */
- (int)setZoom:(float)zoom;


/**
 获取支持的最大变焦值
 
 @return 最大变焦值
 */
- (float)getMaxZoom;


/**
 获取当前变焦值
 
 @return 当前变焦值
 */
- (float)getCurrentZoom;


/**
 闪光灯开关
 
 @param flash true:打开闪光灯 false:关闭闪光灯
 @return 0:success  非0:failure
 */
- (int)setFlash:(bool)flash;


/**
 设置曝光度
 
 @param exposure 曝光度
 @return 0:success  非0:failure
 */
- (int)setExposure:(float)exposure;


/**
 获取当前曝光度
 
 @return  曝光度
 */
- (float)getCurrentExposure;

/**
 获取支持最小曝光度
 
 @return  最小曝光度
 */
- (float)getSupportedMinExposure;

/**
 获取支持最大曝光度
 
 @return  最大曝光度
 */
- (float)getSupportedMaxExposure;


/**
 推流镜像开关
 
 @param mirror true:打开推流镜像 false:关闭推流镜像
 */
- (void)setPushMirror:(bool)mirror;


/**
 预览镜像开关
 
 @param mirror true:打开预览镜像 false:关闭预览镜像
 */
- (void)setPreviewMirror:(bool)mirror;


/**
 静音推流
 
 @param mute true:静音推流 false:正常推流
 */
- (void)setMute:(bool)mute;


/**
 设置美颜开关

 @param beautyOn true:打开美颜 false:关闭美颜
 */
- (void)setBeautyOn:(bool)beautyOn;


/**
 设置美颜 美白度

 @param value 美白度 范围0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyWhite:(int)value;


/**
 设置美颜 磨皮度

 @param value 磨皮度 范围0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyBuffing:(int)value;

/**
 设置美颜 红润度
 
 @param value 红润度 范围0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyRuddy:(int)value;

/**
 设置美颜 腮红度
 
 @param value 腮红度 范围 0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyCheekPink:(int)value;


/**
 设置美颜 瘦脸程度
 
 @param value 瘦脸程度 范围 0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyThinFace:(int)value;

/**
 设置美颜 收下巴程度
 
 @param value 收下巴程度 范围 0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyShortenFace:(int)value;

/**
 设置美颜 大眼程度
 
 @param value 大眼程度 范围 0~100
 @return 0:success  非0:failure
 */
- (int)setBeautyBigEye:(int)value;


/**
 设置推流模式

 @param qualityMode 推流模式 : 选择 ResolutionFirst 模式时，SDK内部会优先保障推流视频的清晰度; 选择 FluencyFirst 模式时，SDK内部会优先保障推流视频的流畅度，此接口只支持这两种模式。设置后码率设置失效。
 @return 0:success  非0:failure
 */
- (int)setQualityMode:(AlivcLivePushQualityMode)qualityMode;


/**
 设置目标码率

 @param targetBitrate 目标码率 [100  5000](Kbps)
 @return 0:success  非0:failure
 */
- (int)setTargetVideoBitrate:(int)targetBitrate;


/**
 设置最小码率

 @param minBitrate 最小码率 [100  5000](Kbps)
 @return 0:success  非0:failure
 */
- (int)setMinVideoBitrate:(int)minBitrate;

/**
 设置预览显示模式

 @param displayMode 预览显示模式
 */
- (void)setpreviewDisplayMode:(AlivcPusherPreviewDisplayMode)displayMode;


/**
 设置推流分辨率，只在预览模式下生效，推流中不能设置
 
 @param resolution 推流分辨率
 */
- (void)setResolution:(AlivcLivePushResolution)resolution;



/* ***********************背景音乐*********************** */

/**
 播放背景音乐

 @param path 背景音乐路径
 @return 0:success  非0:failure
 */
- (int)startBGMWithMusicPathAsync:(NSString *)path;


/**
 停止播放背景音乐

 @return 0:success  非0:failure
 */
- (int)stopBGMAsync;


/**
 暂停播放背景音乐

 @return 0:success  非0:failure
 */
- (int)pauseBGM;


/**
 恢复播放背景音乐

 @return 0:success  非0:failure
 */
- (int)resumeBGM;


/**
 设置背景音乐是否循环播放

 @param isLoop 是否循环  true:循环 false:不循环
 @return 0:success  非0:failure
 */
- (int)setBGMLoop:(bool)isLoop;


/**
 设置背景音乐耳返开关

 @param isOpen 是否打开耳返  true:开启耳返 false:关闭耳返
 @return 0:success  非0:failure
 */
- (int)setBGMEarsBack:(bool)isOpen;


/**
 设置降噪开关
 
 @param isOpen 是否打开降噪 true:开启 false:关闭 默认:false
 @return 0:success  非0:failure
 */
- (int)setAudioDenoise:(bool)isOpen;


/**
 设置背景音乐混音 音乐音量

 @param volume 音乐音量大小 范围:[0 ~ 100] 默认:50
 @return 0:success  非0:failure
 */
- (int)setBGMVolume:(int)volume;


/**
 设置背景音乐混音 人声音量

 @param volume 人声音量大小 范围:[0 ~ 100] 默认:50
 @return 0:success  非0:failure
 */
- (int)setCaptureVolume:(int)volume;

/* ****************************************************** */


/* ***********************外部数据*********************** */

/**
 发送自定义video SampleBuffer

 @param sampleBuffer video sample buffer
 */
- (void)sendVideoSampleBuffer:(CMSampleBufferRef)sampleBuffer;

/**
 发送自定义的audio SampleBuffer
 只限于replaykit录屏直播使用
 
 @param sampleBuffer audio sample buffer
 @param sampleBufferType audio sample buffer type
 */
- (void)sendAudioSampleBuffer:(CMSampleBufferRef)sampleBuffer withType:(RPSampleBufferType)sampleBufferType;

/**
 发送自定义视频数据
 
 @param data 视频数据
 @param width 视频宽度
 @param height 视频高度
 @param size 数据大小
 @param pts 时间戳（单位微秒）
 @param rotation 旋转
 */
- (void)sendVideoData:(char *)data width:(int)width height:(int)height size:(int)size pts:(uint64_t)pts rotation:(int)rotation;

/**
 发送自定义音频数据
 
 @param data 音频数据
 @param size 数据大小
 @param sampleRate 采样率
 @param channel 声道数
 @param pts 时间戳（单位微秒）
 */
- (void)sendPCMData:(char *)data size:(int)size sampleRate:(int)sampleRate channel:(int)channel pts:(uint64_t)pts;

/**
 添加视频混流设置
 */
- (int)addMixVideo:(int)format width:(int)width height:(int)height rotation:(int)rotation displayX:(float)displayX displayY:(float)displayY displayW:(float)displayW displayH:(float)displayH adjustHeight:(bool)adjustHeight;

/**
 改变视频混流位置
 */
- (void)changeMixVideoPosition:(int)handler displayX:(float)displayX displayY:(float)displayY displayW:(float)displayW displayH:(float)displayH;

/**
 改变视频混流镜像
 */
- (void)setMixVideoMirror:(int)handler isMirror:(BOOL)isMirror;

/**
 输入视频混流数据
 */
- (void)inputMixVideoData:(int)handler data:(long)dataptr width:(int)width height:(int)height stride:(int)stride size:(int)size pts:(long)pts rotation:(int)rotation;

/**
 移除视频混流
 */
- (void)removeMixVideo:(int) handler;

/**
 添加音频混流设置
 */
- (int)addMixAudio:(int)channels format:(int)format audioSample:(int)audioSample;

/**
 输入音频混流数据
 */
- (bool)inputMixAudioData:(int)handler data:(long)dataptr size:(int)size pts:(long)pts;

/**
 移除音频混流
 */
- (void)removeMixAudio:(int)handler;

/* ****************************************************** */

/**
 设置Message
 
 @param msg 用户推流消息
 @param count 重复次数
 @param time 延时时间，单位毫秒
 @param isKeyFrame 是否只发关键帧
 @return 0:success  非0:failure
 */
- (int)sendMessage:(NSString *)msg repeatCount:(int)count delayTime:(int)time KeyFrameOnly:(bool)isKeyFrame;


/**
 获取是否正在推流

 @return YES:正在推流 NO:未推流
 */
- (BOOL)isPushing;


/**
 获取当前推流URL

 @return 推流URL
 */
- (NSString *)getPushURL;

/**
 获取当前推流状态
 
 @return 推流状态
 */
- (AlivcLivePushStatus)getLiveStatus;

/**
 获取推流数据统计

 @return 推流数据
 */
- (AlivcLivePushStatsInfo *)getLivePushStatusInfo;


/**
 设置Log级别

 @param level Log级别 default:AlivcLivePushLogLevelError
 @return 0:success  非0:failure
 */
- (int)setLogLevel:(AlivcLivePushLogLevel)level;


/**
 获取SDK版本号

 @return 版本号
 */
- (NSString *)getSDKVersion;

/**
  添加动态贴纸,最多支持添加5个贴纸
  waterMarkDirPath：贴纸图片sequence目录
  x,y：显示屏幕位置（0~1.0f)
  w,h：显示屏幕长宽（0~1.0f)
 
  @return 返回动态贴纸的id号，删除贴纸传此id
 **/
- (int)addDynamicWaterMarkImageDataWithPath:(NSString *)waterMarkDirPath x:(float)x y:(float)y w:(float)w h: (float)h;

/**
 删除动态贴纸
 vid:贴纸id，add时返回的
 
 **/
- (void)removeDynamicWaterMark:(int)vid;

/**
 截图
 count:张数
 interval:每张间隔(ms)
 **/
- (void)snapshot:(int)count interval:(int)interval;


/**
 设置截图回调
 **/
- (void)setSnapshotDelegate:(id<AlivcLivePusherSnapshotDelegate>)delegate;
@end



@protocol AlivcLivePusherErrorDelegate <NSObject>

@required

/**
 系统错误回调

 @param pusher 推流AlivcLivePusher
 @param error error
 */
- (void)onSystemError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error;


/**
 SDK错误回调

 @param pusher 推流AlivcLivePusher
 @param error error
 */
- (void)onSDKError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error;

@end



@protocol AlivcLivePusherNetworkDelegate <NSObject>

@required

/**
 网络差回调

 @param pusher 推流AlivcLivePusher
 */
- (void)onNetworkPoor:(AlivcLivePusher *)pusher;


/**
 推流链接失败

 @param pusher 推流AlivcLivePusher
 @param error error
 */
- (void)onConnectFail:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error;


/**
 网络恢复

 @param pusher 推流AlivcLivePusher
 */
- (void)onConnectRecovery:(AlivcLivePusher *)pusher;


/**
 重连开始回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onReconnectStart:(AlivcLivePusher *)pusher;


/**
 重连成功回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onReconnectSuccess:(AlivcLivePusher *)pusher;

/**
 连接被断开
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onConnectionLost:(AlivcLivePusher *)pusher;


/**
 重连失败回调

 @param pusher 推流AlivcLivePusher
 @param error error
 */
- (void)onReconnectError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error;


/**
 发送数据超时
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onSendDataTimeout:(AlivcLivePusher *)pusher;


/**
 推流URL的鉴权时长即将过期(将在过期前1min内发送此回调)

 @param pusher 推流AlivcLivePusher
 @return 新的推流URL
 */
- (NSString *)onPushURLAuthenticationOverdue:(AlivcLivePusher *)pusher;


/**
 发送SEI Message 通知
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onSendSeiMessage:(AlivcLivePusher *)pusher;

@optional

/**
 网络原因导致音视频丢包
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onPacketsLost:(AlivcLivePusher *)pusher;


@end




@protocol AlivcLivePusherInfoDelegate <NSObject>

@optional


- (void)onPreviewStarted:(AlivcLivePusher *)pusher;



/**
 停止预览回调

 @param pusher 推流AlivcLivePusher
 */
- (void)onPreviewStoped:(AlivcLivePusher *)pusher;


/**
 渲染第一帧回调

 @param pusher 推流AlivcLivePusher
 */
- (void)onFirstFramePreviewed:(AlivcLivePusher *)pusher;


/**
 推流开始回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onPushStarted:(AlivcLivePusher *)pusher;


/**
 推流暂停回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onPushPaused:(AlivcLivePusher *)pusher;


/**
 推流恢复回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onPushResumed:(AlivcLivePusher *)pusher;


/**
 重新推流回调

 @param pusher 推流AlivcLivePusher
 */
- (void)onPushRestart:(AlivcLivePusher *)pusher;


/**
 推流停止回调
 
 @param pusher 推流AlivcLivePusher
 */
- (void)onPushStoped:(AlivcLivePusher *)pusher;

@end


@protocol AlivcLivePusherBGMDelegate <NSObject>

@required

/**
 背景音乐开始播放

 @param pusher 推流AlivcLivePusher
 */
- (void)onStarted:(AlivcLivePusher *)pusher;


/**
 背景音乐停止播放

 @param pusher 推流AlivcLivePusher
 */
- (void)onStoped:(AlivcLivePusher *)pusher;


/**
 背景音乐暂停播放

 @param pusher 推流AlivcLivePusher
 */
- (void)onPaused:(AlivcLivePusher *)pusher;


/**
 背景音乐恢复播放

 @param pusher 推流AlivcLivePusher
 */
- (void)onResumed:(AlivcLivePusher *)pusher;


/**
 背景音乐当前播放进度

 @param pusher 推流AlivcLivePusher
 @param progress 播放时长
 @param duration 总时长
 */
- (void)onProgress:(AlivcLivePusher *)pusher progress:(long)progress duration:(long)duration;


/**
 背景音乐播放完毕

 @param pusher 推流AlivcLivePusher
 */
- (void)onCompleted:(AlivcLivePusher *)pusher;


/**
 背景音乐开启失败

 @param pusher 推流AlivcLivePusher
 */
- (void)onOpenFailed:(AlivcLivePusher *)pusher;


/**
 背景音乐下载播放超时

 @param pusher 推流AlivcLivePusher
 */
- (void)onDownloadTimeout:(AlivcLivePusher *)pusher;


@end
@protocol AlivcLivePusherCustomFilterDelegate <NSObject>
@required
/**
 通知外置滤镜创建回调
 */
- (void)onCreate:(AlivcLivePusher *)pusher context:(void*)context;
/**
 通知外置滤镜设置参数
 */
- (void)updateParam:(AlivcLivePusher *)pusher buffing:(float)buffing whiten:(float)whiten pink:(float)pink cheekpink:(float)cheekpink thinface:(float)thinface shortenface:(float)shortenface bigeye:(float)bigeye;
/**
 通知外置滤镜开馆
 */
- (void)switchOn:(AlivcLivePusher *)pusher on:(bool)on;
/**
 通知外置滤镜处理回调
 */
- (int)onProcess:(AlivcLivePusher *)pusher texture:(int)texture textureWidth:(int)width textureHeight:(int)height extra:(long)extra;

/**
 通知外置滤镜销毁回调
 */
- (void)onDestory:(AlivcLivePusher *)pusher;

@end

@protocol AlivcLivePusherCustomDetectorDelegate <NSObject>

@required
/**
 通知外置识别器创建回调
 */
- (void)onCreateDetector:(AlivcLivePusher *)pusher;

/**
 通知外置识别器处理回调
 */
- (long)onDetectorProcess:(AlivcLivePusher *)pusher data:(long)data w:(int)w h:(int)h rotation:(int)rotation format:(int)format extra:(long)extra;

/**
 通知外置识别器销毁回调
 */
- (void)onDestoryDetector:(AlivcLivePusher *)pusher;

@end

@protocol AlivcLivePusherSnapshotDelegate <NSObject>

@required

/**
 截图回调
 
 @param pusher 推流AlivcLivePusher
 @param image 截图
 @param error error
 */
- (void)onSnapshot:(AlivcLivePusher *)pusher image:(UIImage *)image;
@end
