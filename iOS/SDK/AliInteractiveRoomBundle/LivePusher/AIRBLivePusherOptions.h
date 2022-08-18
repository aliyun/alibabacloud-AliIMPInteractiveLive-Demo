//
//  AIRBLivePusherOptions.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/10.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <AliInteractiveRoomBundle/AIRBCommonDefines.h>


NS_ASSUME_NONNULL_BEGIN

@interface AIRBLivePusherLiveBusinessOptions : NSObject
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* liveCoverUrl;
@property (copy, nonatomic) NSString* liveIntroduction;
@property (assign, nonatomic) int64_t liveStartTime;
@property (assign, nonatomic) int64_t liveEndTime;
@property (copy, nonatomic) NSString* extension;
@end

@interface AIRBLivePusherMediaStreamingOptions : NSObject

/**
 推流方向 : 竖屏、90度横屏、270度横屏
 * 默认 : 0， 即竖屏推流
 */
@property (nonatomic, assign) int orientation;
//
//
///**
// 摄像头类型，0表示后置摄像头，1表示前置摄像头
// * 默认 AIRBLivePusherCameraTypeFront，前置摄像头
// */
//@property (nonatomic, assign) AIRBLivePusherCameraType cameraType;
//
//
///**
// 推流镜像
// * 默认 : false 关闭镜像
// */
//@property (nonatomic, assign) bool pushMirror;
//
//
///**
// 预览镜像
// * 默认 : false 关闭镜像
// */
//@property (nonatomic, assign) bool previewMirror;
//
///**
// 纯音频推流，只有RTMP推流才支持纯音频推流，当前RTC不支持纯音频推流
// * 默认 : false
// * 注 : 与 videoOnly互斥
// */
//@property (nonatomic, assign) bool audioOnly;
//
//
///**
// 纯视频推流，只有RTMP推流才支持纯视频推流，当前RTC不支持纯视频推流
// * 默认 : false
// * 注 : 与 audioOnly互斥
// */
//@property (nonatomic, assign) bool videoOnly;
//
///**
// 自动聚焦
// * 默认 : true
// */
//@property (nonatomic, assign) bool autoFocus;
//
///**
// 是否开启闪光灯
// * 默认 : false
// */
//@property (nonatomic, assign) bool flash;

/**
 预览显示模式
 PREVIEW_SCALE_FILL= 0, // 铺满窗口，视频比例和窗口比例不一致时预览会有变形
 PREVIEW_ASPECT_FIT= 1, // 保持视频比例，视频比例和窗口比例不一致时有黑边
 PREVIEW_ASPECT_FILL = 2, //剪切视频以适配窗口比例，视频比例和窗口比例不一致时会裁剪视频
 * 默认 : 2，即ASPECT_FILL
 */
@property (nonatomic, assign) int previewDisplayMode;

/**
 推流媒体相关的配置请使用当前config，具体见AlivcLivePusher.framework中的alivcLivePushConfig类
 */
@property (nonatomic, strong) id alivcLivePushConfig;

@end

@interface AIRBLivePusherOptions : NSObject
@property (strong, nonatomic) AIRBLivePusherLiveBusinessOptions* businessOptions;
@property (strong, nonatomic) AIRBLivePusherMediaStreamingOptions* mediaStreamingOptions;

+ (instancetype) defaultOptions;
@end

NS_ASSUME_NONNULL_END
