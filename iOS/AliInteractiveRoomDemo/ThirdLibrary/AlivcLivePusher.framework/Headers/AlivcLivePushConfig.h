//
//  AlivcLivePushConfig.h
//  AlivcLiveCaptureLib
//
//  Created by TripleL on 2017/9/26.
//  Copyright © 2017年 Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AlivcLivePushConstants.h"


/**
 推流配置类
 */
@interface AlivcLivePushConfig : NSObject


/**
 分辨率
 * 默认 : AlivcLivePushDefinition540P
 */
@property (nonatomic, assign) AlivcLivePushResolution resolution;


/**
 推流模式
 * 默认 : AlivcLivePushQualityModeResolutionFirst 清晰度优先
 * 描述 : 选择 ResolutionFirst 模式时，SDK内部会优先保障推流视频的清晰度; 选择 FluencyFirst 模式时，SDK内部会优先保障推流视频的流畅度; 以上两种模式下，所有码率与帧率的设置均不生效，SDK会根据您设置的分辨率做出默认设置。 选择 Custom 模式时，SDK会根据您自定义设置的帧率与码率进行推流。
 */
@property (nonatomic, assign) AlivcLivePushQualityMode qualityMode;


/**
 是否打开码率自适应
 * 默认 : true
 */
@property (nonatomic, assign) bool enableAutoBitrate;


/**
 是否打开分辨率自适应 (动态分辨率)
 * 默认 : false
 * 注 : qualityMode在custom模式下，分辨率自适应无效
 */
@property (nonatomic, assign) bool enableAutoResolution;


/**
 视频采集帧率
 * 默认 : AlivcLivePushFPS20
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) AlivcLivePushFPS fps;


/**
 最小视频采集帧率
 * 默认 : AlivcLivePushFPS8
 * 单位 : Frames per Second
 * 不可大于 视频采集帧率fps
 */
@property (nonatomic, assign) AlivcLivePushFPS minFps;


/**
 目标视频编码码率
 * 默认 : 800
 * 范围 : [100,5000]
 * 单位 : Kbps
 */
@property (nonatomic, assign) int targetVideoBitrate;


/**
 最小视频编码码率
 * 默认 : 200
 * 范围 : [100,5000]
 * 单位 : Kbps
 */
@property (nonatomic, assign) int minVideoBitrate;


/**
 初始视频编码码率
 * 默认 : 800
 * 范围 : [100,5000]
 * 单位 : Kbps
 */
@property (nonatomic, assign) int initialVideoBitrate;


/**
 音频编码码率
 * 默认 : 64
 * 范围 : [10,1000]
 * 单位 : Kbps
 */
@property (nonatomic, assign) int audioBitrate;


/**
 音频采样率
 * 默认 : AlivcLivePushAudioSampleRate32000
 */
@property (nonatomic, assign) AlivcLivePushAudioSampleRate audioSampleRate;


/**
 声道数
 * 默认 : AlivcLivePushAudioChannel_2 双声道
 */
@property (nonatomic, assign) AlivcLivePushAudioChannel audioChannel;


/**
 关键帧间隔
 * 默认 : AlivcLivePushVideoEncodeGOP_2
 * 单位 : s
 */
@property (nonatomic, assign) AlivcLivePushVideoEncodeGOP videoEncodeGop;


/**
 重连次数
 * 默认 : 5
 * 范围 : [0,100]
 */
@property (nonatomic, assign) int connectRetryCount;


/**
 重连时长
 * 默认 : 1000
 * 范围 : (0,10000]
 * 单位 : ms
 */
@property (nonatomic, assign) float connectRetryInterval;


/**
 网络发送数据超时
 * 默认 : 3000
 * 单位 : ms
 */
@property (nonatomic, assign) int sendDataTimeout;


/**
 推流方向 : 竖屏、90度横屏、270度横屏
 * 默认 : AlivcLivePushOrientationPortrait
 */
@property (nonatomic, assign) AlivcLivePushOrientation orientation;


/**
 摄像头类型
 * 默认 : AlivcLivePushCameraTypeFront
 */
@property (nonatomic, assign)AlivcLivePushCameraType cameraType;


/**
 推流镜像
 * 默认 : false 关闭镜像
 */
@property (nonatomic, assign) bool pushMirror;


/**
 预览镜像
 * 默认 : false 关闭镜像
 */
@property (nonatomic, assign) bool previewMirror;


/**
 纯音频推流
 * 默认 : false
 * 注 : 与 videoOnly互斥
 */
@property (nonatomic, assign) bool audioOnly;


/**
 纯视频推流
 * 默认 : false
 * 注 : 与 audioOnly互斥
 */
@property (nonatomic, assign) bool videoOnly;

/**
 自动聚焦
 * 默认 : true
 */
@property (nonatomic, assign) bool autoFocus;


/**
 是否打开美颜
 * 默认 : true
 */
@property (nonatomic, assign) bool beautyOn;


/**
 暂停推流图片
 */
@property(nonatomic, retain) UIImage *pauseImg;


/**
 码率低图片
 */
@property(nonatomic, retain) UIImage *networkPoorImg;


/**
 美颜模式
 * 默认 : AlivcLivePushBeautyModeProfessional 普通模式
 */
@property (nonatomic, assign) AlivcLivePushBeautyMode beautyMode;


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


/**
 是否开启闪光灯
 * 默认 : false
 */
@property (nonatomic, assign) bool flash;


/**
 视频编码模式
 * 默认 : AlivcLivePushVideoEncoderModeHard
 */
@property (nonatomic, assign) AlivcLivePushVideoEncoderMode videoEncoderMode;



/**
 音频编码格式
 * 默认 : AlivcLivePushAudioEncoderProfile_AAC_LC
 */
@property (nonatomic, assign) AlivcLivePushAudioEncoderProfile audioEncoderProfile;


/**
 音频编码模式
 * 默认 : AlivcLivePushAudioEncoderModeSoft
 */
@property (nonatomic, assign) AlivcLivePushAudioEncoderMode audioEncoderMode;


/**
 是否外部自定义数据推流
 * 默认 : false
 */
@property (nonatomic, assign) bool externMainStream;

/**
 外部自定义视频数据
 * 默认 : unknown
 */
@property (nonatomic, assign) AlivcLivePushVideoFormat externVideoFormat;

/**
 外部自定义音频数据
 * 默认 : unknown
 */
@property (nonatomic, assign) AlivcLivePushAudioFormat externAudioFormat;

/**
 预览显示模式
 * 默认 : fit
 */
@property (nonatomic, assign) AlivcPusherPreviewDisplayMode previewDisplayMode;

/**
 开启 openGL Shared Context 模式
 * 默认 : false
 */
@property (nonatomic, assign) BOOL requireGLSharedContext;


/**
 业务信息
 */

@property (nonatomic, copy) NSDictionary *businessInfo;


/**
 init 分辨率 其余值为默认值
 
 @param resolution 推流分辨率
 @return self
 */
- (instancetype)initWithResolution:(AlivcLivePushResolution)resolution;


/**
 添加水印 最多支持3个水印

 @param path 水印路径
 @param coordX 水印左上顶点x的相对坐标 [0,1]
 @param coordY 水印左上顶点y的相对坐标 [0,1]
 @param width 水印的相对宽度 (水印会根据水印图片实际大小和水印宽度等比缩放) (0,1]
 */
- (void)addWatermarkWithPath:(NSString *)path
             watermarkCoordX:(CGFloat)coordX
             watermarkCoordY:(CGFloat)coordY
              watermarkWidth:(CGFloat)width;

/**
 移除水印
 
 @param path 水印路径
 */
- (void)removeWatermarkWithPath:(NSString *)path;


/**
 获取全部水印

 * key:watermarkPath value:水印图片路径
 * key:watermarkCoordX value:x值
 * key:watermarkCoordY value:y值
 * key:watermarkWidth value:width值
 @return 全部水印配置数组
 */
- (NSArray<NSDictionary *> *)getAllWatermarks;


/**
 获取推流宽高具体数值

 @return 推流宽高Rect
 */
- (CGSize)getPushResolution;

@end
