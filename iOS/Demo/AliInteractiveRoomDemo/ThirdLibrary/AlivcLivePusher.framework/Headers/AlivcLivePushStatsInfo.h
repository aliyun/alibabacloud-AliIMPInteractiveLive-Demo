//
//  AlivcLivePushStatusInfo.h
//  AlivcLiveCaptureLib
//
//  Created by TripleL on 2017/9/25.
//  Copyright © 2017年 Alibaba. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 推流性能参数类
 */
@interface AlivcLivePushStatsInfo : NSObject

#pragma mark - system

/**
 当前推流CPU占比
 * 单位 : %
 */
@property (nonatomic, assign) float CPUHold;


/**
 当前推流内存占比
 * 单位 : %
 */
@property (nonatomic, assign) float memoryHold;



#pragma mark - video capture

/**
 视频采集FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int videoCaptureFps;


/**
 当前视频采集PTS
 */
@property (nonatomic, assign) uint64_t currentlyVideoCapturePts;



#pragma mark - audio capture

/**
 当前音频采集PTS
 */
@property (nonatomic, assign) uint64_t currentlyAudioCapturePts;



#pragma mark - audio encode

/**
 音频编码码率
 * 单位 : Kbps
 */
@property (nonatomic, assign) int audioEncodedBitrate;


/**
 音频编码FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int audioEncodedFps;


/**
 音频编码队列帧数
 */
@property (nonatomic, assign) int audioFramesInEncodeBuffer;



#pragma mark - video render

/**
 视频渲染FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int videoRenderFps;


/**
 视频渲染队列帧数
 */
@property (nonatomic, assign) int videoFramesInRenderBuffer;


/**
 平均每帧渲染耗时
 */
@property (nonatomic, assign) int videoRenderConsumingTimePerFrame;



#pragma mark - video encode

/**
 视频编码码率
 * 单位 : Kbps
 */
@property (nonatomic, assign) int videoEncodedBitrate;


/**
 视频编码队列帧数
 */
@property (nonatomic, assign) int videoFramesInEncodeBuffer;


/**
 视频编码FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int videoEncodedFps;


/**
 视频编码总帧数
 */
@property (nonatomic, assign) uint64_t totalFramesOfEncodedVideo;


/**
 视频编码总耗时
 * 单位 : ms
 */
@property (nonatomic, assign) uint64_t totalTimeOfEncodedVideo;


/**
 设置的视频编码码率参数
 * 单位 : Kbps
 */
@property (nonatomic, assign) int videoEncodeParam;


/**
 设置的视频编码模式
 * 0:硬编  1:软编
 */
@property (nonatomic, assign) int videoEncoderMode;



#pragma mark - pusher

/**
 音频上传码率
 * 单位 : Kbps
 */
@property (nonatomic, assign) int audioUploadBitrate;


/**
 视频上传码率
 * 单位 : Kbps
 */
@property (nonatomic, assign) int videoUploadBitrate;


/**
 推流队列中的音频buffer个数
 */
@property (nonatomic, assign) int audioPacketsInUploadBuffer;


/**
 推流队列中的视频buffer个数
 */
@property (nonatomic, assign) int videoPacketsInUploadBuffer;


/**
 当前视频上传FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int videoUploadFps;


/**
 当前音频上传FPS
 * 单位 : Frames per Second
 */
@property (nonatomic, assign) int audioUploadFps;


/**
 当前上传视频帧PTS
 */
@property (nonatomic, assign) uint64_t currentlyUploadedVideoFramePts;


/**
 当前上传音频帧PTS
 */
@property (nonatomic, assign) uint64_t currentlyUploadedAudioFramePts;


/**
 当前最近一次上传的关键帧PTS
 */
@property (nonatomic, assign) uint64_t previousVideoKeyframePts;


/**
 推流队列中最后一帧视频PTS
 */
@property (nonatomic, assign) uint64_t lastVideoPtsInBuffer;


/**
 推流队列中最后一帧音频PTS
 */
@property (nonatomic, assign) uint64_t lastAudioPtsInBuffer;


/**
 推流数据总大小
 */
@property (nonatomic, assign) uint64_t totalSizeOfUploadedPackets;


/**
 推流总时长
 */
@property (nonatomic, assign) uint64_t totalTimeOfUploading;


/**
 视频推流总帧数
 */
@property (nonatomic, assign) uint64_t totalFramesOfUploadedVideo;


/**
 视频丢帧总数
 */
@property (nonatomic, assign) int totalDurationOfDropingVideoFrames;


/**
 音频丢帧总数
 */
@property (nonatomic, assign) int totalDurationOfDropingAudioFrames;


/**
 视频丢帧总次数
 */
@property (nonatomic, assign) int totalTimesOfDropingVideoFrames;


/**
 断网总次数
 */
@property (nonatomic, assign) int totalTimesOfDisconnect;


/**
 重连总次数
 */
@property (nonatomic, assign) int totalTimesOfReconnect;


/**
 视频从采集到上传耗时
 * 单位 : ms
 */
@property (nonatomic, assign) uint64_t videoDurationFromCaptureToUpload;


/**
 音频从采集到上传耗时
 * 单位 : ms
 */
@property (nonatomic, assign) uint64_t audioDurationFromCaptureToUpload;


/**
 推流队列当前上传帧大小
 */
@property (nonatomic, assign) uint64_t currentUploadPacketSize;


/**
 当前音视频PTS差异
 */
@property (nonatomic, assign) uint64_t audioVideoPtsDiff;


/**
 修正前视频帧PTS
 */
@property (nonatomic, assign) uint64_t beforeAdjustVideoPts;


/**
 修正前音频帧PTS
 */
@property (nonatomic, assign) uint64_t beforeAdjustAudioPts;


/**
 修正后视频帧PTS
 */
@property (nonatomic, assign) uint64_t afterAdjustVideoPts;


/**
 修正后音频帧PTS
 */
@property (nonatomic, assign) uint64_t afterAdjustAudioPts;


/**
 缓冲队列中曾经最大的视频帧size
 */
@property (nonatomic, assign) uint64_t maxSizeOfVideoPacketsInBuffer;


/**
 缓冲队列中曾经最大的音频帧size
 */
@property (nonatomic, assign) uint64_t maxSizeOfAudioPacketsInBuffer;


/**
 推流队列最近一次视频发送码率
 */
@property (nonatomic, assign) int latestUploadedVideoBitrate;


/**
 推流队列最近一次音频发送码率
 */
@property (nonatomic, assign) int latestUploadedAudioBitrate;


/**
 推流队列最近一次发送包大小
 */
@property (nonatomic, assign) int lastUploadedSendSize;


/**
 推流队列最近一次发送时间戳
 */
@property (nonatomic, assign) int lastUploadedSendTime;

@end
