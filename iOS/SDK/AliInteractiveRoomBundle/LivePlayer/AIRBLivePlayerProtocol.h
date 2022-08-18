//
//  AIRBLivePlayerProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import <AliInteractiveRoomBundle/AIRBCommonDefines.h>

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBLivePlayerDelegate <NSObject>
- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info;
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
 *
 * 具体包括：
 * AIRBVideoViewContentModeFill = 0,         不保持比例平铺
 * AIRBVideoViewContentModeAspectFit,  保持比例，黑边
 * AIRBVideoViewContentModeAspectFill, 保持比例填充，需裁剪
*/
@property (assign, nonatomic) AIRBVideoViewContentMode contentMode;

/**
 * 是否开启低延迟直播拉流，默认打开；
 * 注意: 只有在start之前设置才会生效；低延迟拉流观看直播会比普通的拉流观看直播方式产生更多的费用，具体情况请到阿里云互动直播控制台了解；
 */
@property (assign, nonatomic) BOOL lowDelay;

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
 * 在当前房间重新加载本场直播，前提是已经调用过start；
 */
- (void) refresh;

/**
 * 重新加载新的播放地址进行播放；
 */
- (void) refreshWithMediaURL:(NSString*)url;

/**
 * 异步截取当前视频画面，通过delegate返回一个UIImage，具体见AIRBLivePlayerEventImageCaptured事件
 */
- (void) snapshotAsync;

/**
 * 切换是否静音
 */
- (void) toggleMuted;

/**
 * 切换是否静音
 */
- (void)mute:(BOOL)mute;
@end

NS_ASSUME_NONNULL_END
