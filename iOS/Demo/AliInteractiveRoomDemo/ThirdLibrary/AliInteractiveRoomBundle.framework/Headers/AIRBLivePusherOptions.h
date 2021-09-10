//
//  AIRBLivePusherOptions.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/10.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AIRBCommonDefines.h"


NS_ASSUME_NONNULL_BEGIN

@interface AIRBLivePusherLiveBusinessOptions : NSObject
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* liveCoverUrl;
@property (copy, nonatomic) NSString* liveIntroduction;
@property (assign, nonatomic) int64_t liveStartTime;
@property (assign, nonatomic) int64_t liveEndTime;
@property (copy, nonatomic) NSString* extension;
@end

@interface AIRBLivePusherFaceBeautyOptions : NSObject
/**
 美颜模式
 * 默认 : AIRBLivePushBeautyModeNormal 普通模式
 */
@property (nonatomic, assign) AIRBLivePushBeautyMode beautyMode;
/**
 美颜 美白参数
 * 默认 : 70
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyWhite;


/**
 美颜 磨皮参数
 * 默认 : 40
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyBuffing;


/**
 美颜 红润参数
 * 默认 : 40
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyRuddy;


/**
 美颜 腮红参数
 * 默认 : 15
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyCheekPink;


/**
 美颜 瘦脸参数
 * 默认 : 40
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyThinFace;


/**
 美颜 收下巴参数
 * 默认 : 50
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyShortenFace;


/**
 美颜 大眼参数
 * 默认 : 30
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int beautyBigEye;
@end

@interface AIRBLivePusherMediaStreamingOptions : NSObject

/**
 * 是否进行仅视频推流（不录制音频），默认NO
 */
@property (assign, nonatomic) BOOL onlyVideo;

/**
 * 视频的帧率，默认20，不建议外部调节
 */
@property (assign, nonatomic) int16_t videoFrameRate;

/**
 * 视频的码率，单位 kbps，范围[400, 5000)，默认 1500，不建议外部调节
 */
@property (assign, nonatomic) int32_t videoBitrate;

/**
 * 视频帧最大关键帧间隔，单位秒，默认4秒，不建议外部调节
 */
@property (assign, nonatomic) int16_t maxVideoKeyFrameInterval;

/**
 * 摄像头的默认类型，默认为前置摄像头
 */
@property (assign, nonatomic) AIRBLivePusherCameraType cameraDefaultType;

/**
 * 是否进行仅音频推流（不录制视频）
 */
@property (assign, nonatomic) BOOL onlyAudio;
@end

@interface AIRBLivePusherOptions : NSObject
@property (strong, nonatomic) AIRBLivePusherLiveBusinessOptions* businessOptions;
@property (strong, nonatomic) AIRBLivePusherMediaStreamingOptions* mediaStreamingOptions;
@property (strong, nonatomic) AIRBLivePusherFaceBeautyOptions* faceBeautyOptions;

+ (instancetype) defaultOptions;
@end

NS_ASSUME_NONNULL_END
