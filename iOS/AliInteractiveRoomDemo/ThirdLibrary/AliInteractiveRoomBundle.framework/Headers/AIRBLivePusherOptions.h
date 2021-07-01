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

@interface AIRBLivePusherUserDefinedLiveInfo : NSObject
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* liveIntroduction;
@property (assign, nonatomic) int64_t liveStartDate;
@property (assign, nonatomic) int64_t liveEndDate;
@end

@interface AIRBLivePusherFaceBeautyOptions : NSObject
/**
 美颜模式
 * 默认 : AlivcLivePushBeautyModeProfessional 普通模式
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

@property (assign, nonatomic) BOOL onlyVideo;
@property (assign, nonatomic) int16_t videoFrameRate;
@property (assign, nonatomic) int64_t videoBitrate; // 单位 kbps，范围[400, 5000)
@property (assign, nonatomic) AIRBLivePusherVideoProfile videoProfile;
@property (assign, nonatomic) int16_t maxVideoKeyFrameInterval;
@property (assign, nonatomic) AIRBLivePusherCameraType cameraDefaultType;

@property (assign, nonatomic) BOOL onlyAudio;
@property (assign, nonatomic) int64_t audioBitrate;
@property (assign, nonatomic) int16_t channels;
@property (assign, nonatomic) int64_t sampleRate;
@end

@interface AIRBLivePusherOptions : NSObject
@property (strong, nonatomic) AIRBLivePusherMediaStreamingOptions* mediaStreamingOptions;
@property (strong, nonatomic) AIRBLivePusherFaceBeautyOptions* faceBeautyOptions;
@property (strong, nonatomic) AIRBLivePusherUserDefinedLiveInfo* userLiveInfo;

+ (instancetype) defaultOptions;
@end

NS_ASSUME_NONNULL_END
