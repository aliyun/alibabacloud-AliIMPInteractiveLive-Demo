//
//  AIRBVodPlayerProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/7/6.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBVodPlayerDelegate <NSObject>
- (void) onAIRBVodPlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBVodPlayerEvent:(AIRBVodPlayerEvent)event info:(NSDictionary*)info;
- (void) onAIRBVodPlayerImageSnapshot:(UIImage*)image;
@end

@protocol AIRBVodPlayerProtocol <NSObject>

/**
 * 用来接收事件和错误通知
 */
@property (weak, nonatomic) id<AIRBVodPlayerDelegate> delegate;

/**
 * 播放器的画面view
 */
@property (strong, nonatomic) UIView* playerView;

/**
 * 播放器画面view的展开模式，默认AIRBVideoViewContentModeAspectFit
 */
@property (assign, nonatomic) AIRBVideoViewContentMode contentMode;

/**
 * 设置是否自动播放，默认为ture
 */
@property (assign, nonatomic) BOOL autoPlay;

/**
 * 播放器的控制view
 * 包括控制条和手势识别
 */
@property (strong, nonatomic) UIView* playerControlView;

/**
 * 是否开启单击手势识别
 * 默认为是，控制隐藏/显示控制条
 */
@property (assign, nonatomic) BOOL tapGestureRecognizerEnabled;

/**
 * 是否开启双击手势识别
 * 默认为是，控制暂停/播放
 */
@property (assign, nonatomic) BOOL twiceTapGestureRecognizerEnabled;

/**
 * 是否显示播放器控制条
 * 默认为是
 */
@property (assign, nonatomic) BOOL showControlBar;

/**
 * 进度条
 * 方便定制颜色等效果
 */
@property (strong, nonatomic) UISlider* progressSliderInControlBar;

/**
 * 播放的音量，范围0.0~2.0，当音量大于1.0时，可能出现噪音，不推荐使用
 */
@property (assign, nonatomic) float playerVolume;

/**
 * 播放速率，0.5-2.0之间，1为正常播放
 */
@property (assign, nonatomic) float playerRate;

/**
 * 准备播放器
 * @param mediaURL 视频URL
 */
- (void) prepareWithMediaURL:(NSString*) mediaURL;

/**
 * 准备播放直播回放
 * @param liveID 回放所对应的直播ID
 * @param statistic 是否开启直播回放播放的时长统计
 */
- (void) prepareWithLiveID:(NSString*) liveID statistic:(BOOL)statistic;

/**
 * 暂停播放
 */
- (void) pause;

/**
 * 播放
 */
- (void) play;

/**
 * 停止播放（不可再恢复）
 */
- (void) stop;

/**
 * 跳转到指定的播放位置
 * @param time 新的播放位置，单位毫秒
 */
- (void) seekToTime:(int64_t)time;

///**
// * 获取视频的长度
// */
//- (int64_t) getDuration;

///**
// * 获取当前播放位置
// */
//- (int64_t) getCurrentPosition;

/**
 * 异步截取当前视频画面，通过delegate返回一个UIImage，具体见onAIRBVodPlayerImageSnapshot
 */
- (void) snapshotAsync;

/**
 * 切换是否静音
 */
- (void) toggleMuted;

@end

NS_ASSUME_NONNULL_END
