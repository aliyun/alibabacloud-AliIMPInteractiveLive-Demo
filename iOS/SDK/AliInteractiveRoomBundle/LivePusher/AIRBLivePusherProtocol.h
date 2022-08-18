//
//  AIRBLivePusherProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <AliInteractiveRoomBundle/AIRBCommonDefines.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBLivePusherOptions;
@class AIRBLivePusherFaceBeautyOptions;
@class AIRBLivePusherLiveBusinessOptions;

@protocol AIRBLivePusherDelegate <NSObject>
- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info;
- (void) onAIRBLivePusherError:(AIRBErrorCode)errorCode message:(NSString*)errorMessage;
@end

@protocol AIRBLivePusherProtocol <NSObject>

/**
 * 用来接收推流相关的事件和错误
 */
@property (weak, nonatomic) id<AIRBLivePusherDelegate> delegate;

/**
 * 推流相关的配置，视频默认分辨率720P，具体见AIRBLivePusherOptions的字段说明
 */
@property (strong, nonatomic) AIRBLivePusherOptions* options;

/**
 * 推流时的相机画面预览view
 */
@property (strong, nonatomic) UIView* pusherView;

/**
 * 推流时的相机画面预览view的拉伸模式，具体见AIRBVideoViewContentMode，默认为AIRBVideoViewContentModeAspectFill
 */
@property (assign, nonatomic) AIRBVideoViewContentMode contentMode;

/**
 * 美颜操作句柄，是 一个来自Queen.framework的实例对象，内部已经构造好，外部直接使用即可，具体见Queen.framework
 */
@property (weak, nonatomic, readonly) id queenEngine;

/**
 * 内部已经实现好了的一个美颜操控面板，可以直接push来展示；面板建议高度200，建议宽度与屏幕宽度相同；
 */
@property (weak, nonatomic, readonly) UIViewController* faceBeautyConfigViewController;

/**
 * 根据配置，打开推流预览画面
 * @param options 推流相关的配置
 */
- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options;

/**
 * 根据配置，打开推流预览画面
 * @param options 推流相关的配置
 * @param preview 预览画面渲染的view
 */
- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options preview:(UIView*)preview;

/**
 * 根据配置，打开屏幕捕捉推流
 * @param options 推流相关的配置
 * @param appGroupID 使用的appGroupID，具体配置方法见 https://help.aliyun.com/document_detail/94828.html?spm=a2c4g.11186623.6.989.10842d47PvxYrf#section-wi9-stp-9lz
 */
- (void) startScreenCaptureWithOptions:(AIRBLivePusherOptions*)options
                            appGroupID:(NSString*)appGroupID;

/**
 * 开始直播推流
 */
- (void) startLiveStreaming;

/**
 * 使用传入的推流地址开始直播推流
 */
- (void) startLiveStreamingWithPushUrl:(NSString*)url;

/**
 * 重新开始直播推流，适用于断网后恢复重推
 */
- (void) restartLiveStreaming;

/**
 * 暂停直播推流
 */
- (void) pauseLiveStreaming;

/**
 * 从暂停状态下恢复直播推流
 */
- (void) resumeLiveStreaming;

/**
 * 停止直播推流
 * @param stopLive YES表示结束直播，NO表示只停止推流不结束直播
 */
- (void) stopLiveStreaming:(BOOL)stopLive;

/**
 * 停止直播推流并结束直播
 * @note 等同于stopLiveStreaming:YES
 */
- (void) stopLiveStreaming;

/**
 * 切换前后摄像头，默认使用前置摄像头
 */
- (void) toggleCamera;

/**
 * 切换推流是否静音（静音后观众侧侧听不到主播侧的声音）
 */
- (void) toggleMuted;

/**
 * 是否开启摄像头预览画面的镜像
 */
- (void) setPreviewMirror:(BOOL)mirror;

/**
 * 是否开启视频流的画面镜像
 */
- (void) setStreamingVideoMirror:(BOOL)mirror;

/**
 * 动态打开或者关闭美颜
 */
- (void) toggleFaceBeauty;

/**
 * 打开或关闭闪光灯
 */
- (void) setFlash:(BOOL)open;

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
 对焦
 
 @param point 对焦的点
 @param autoFocus 是否自动对焦
 @return 0:success  非0:failure
 */
- (int)focusCameraAtAdjustedPoint:(CGPoint)point autoFocus:(bool)autoFocus;

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
 * 动态更新直播业务信息，具体见AIRBLivePusherLiveBusinessOptions
 */
- (void) updateLiveBusinessOptions:(AIRBLivePusherLiveBusinessOptions*) businessOptions
                         onSuccess:(void (^)(void))onSuccess
                         onFailure:(void (^)(NSString* errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
