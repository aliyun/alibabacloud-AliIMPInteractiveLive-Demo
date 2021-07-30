//
//  AIRBLivePlayerProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBLivePlayerDelegate <NSObject>
- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info;
- (void) onAIRBLivePlayerImageSnapshot:(UIImage*)image;
@end

@protocol AIRBLivePlayerProtocol <NSObject>

/**
 * 用来接收事件和错误通知
 */
@property (weak, nonatomic) id<AIRBLivePlayerDelegate> delegate;

/**
 * 播放器的画面view
 */
@property (strong, nonatomic) UIView* playerView;

/**
 * 播放器画面view的展开模式，默认AIRBVideoViewContentModeAspectFit
 */
@property (assign, nonatomic) AIRBVideoViewContentMode contentMode;

/**
 * 播放的音量，范围0.0~2.0，当音量大于1.0时，可能出现噪音，不推荐使用
 */
@property (assign, nonatomic) float playerVolume;

/**
 * 启动播放
 */
- (void) start;

/**
 * 暂停播放
 */
- (void) pause;

/**
 * 从暂停状态下恢复播放
 */
- (void) resume;

/**
 * 停止播放（不可再恢复）
 */
- (void) stop;

/**
 * 异步截取当前视频画面，通过delegate返回一个UIImage，具体见onAIRBLivePlayerImageSnapshot
 */
- (void) snapshotAsync;

/**
 * 切换是否静音
 */
- (void) toggleMuted;
@end

NS_ASSUME_NONNULL_END
