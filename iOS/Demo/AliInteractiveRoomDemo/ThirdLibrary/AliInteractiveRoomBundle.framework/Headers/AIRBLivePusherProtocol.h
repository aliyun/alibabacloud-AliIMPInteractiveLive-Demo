//
//  AIRBLivePusherProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "AIRBCommonDefines.h"

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
 * 根据配置，打开推流预览画面
 * @param options 推流相关的配置
 */
- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options;

/**
 * 开始直播推流
 */
- (void) startLiveStreaming;

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
 * 动态更新美颜参数，具体见AIRBLivePusherFaceBeautyOptions
 */
- (void) updateFaceBeautyParameters:(AIRBLivePusherFaceBeautyOptions*) beautyOptions;

/**
 * 动态更新直播业务信息，具体见AIRBLivePusherLiveBusinessOptions
 */
- (void) updateLiveBusinessOptions:(AIRBLivePusherLiveBusinessOptions*) businessOptions
                         onSuccess:(void (^)(void))onSuccess
                         onFailure:(void (^)(NSString* errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
