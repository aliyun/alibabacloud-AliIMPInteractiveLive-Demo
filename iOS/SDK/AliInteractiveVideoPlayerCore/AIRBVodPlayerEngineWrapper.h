//
//  AIRBVodPlayerEngineWrapper.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/7/6.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AIRBPlayerControlView.h"

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBVodPlayerEngineWrapperDelegate <NSObject>

- (void) onVodPlayerEnginePrepareDone:(NSDictionary*)info;
- (void) onVodPlayerEngineFirstRenderedStart:(NSDictionary*)info;
- (void) onVodPlayerEngineCompletion;
- (void) onVodPlayerEngineLoadingStart;
- (void) onVodPlayerEngineLoadingEnd;
- (void) onVodPlayerEngineSeekEnd;
- (void) onVodPlayerEngineStatusChangedToPlaying;
- (void) onVodPlayerEngineStatusChangedToPaused;
- (void) onVodPlayerEnginePositionUpdated:(NSDictionary*)info;
- (void) onVodPlayerEngineCurrentUtcTimeUpdated:(int64_t)currentUtcTime;
//- (void) onVodPlayerEngineDurationUpdated:(int64_t)duration;
- (void) onVodPlayerEngineBufferedPositionUpdated:(int64_t)bufferedPosition;
- (void) onVodPlayerEngineErrorCode:(unsigned int)errorCode errorMessage:(NSString*)errorMessage;
- (void) onVodPlayerEngineImageSnapshot:(UIImage*)image;
- (void) onVodPlayerEngineVideoRendered;

@end

@interface AIRBVodPlayerEngineWrapper : NSObject
@property (weak, nonatomic) id<AIRBVodPlayerEngineWrapperDelegate> delegate;
@property (strong, nonatomic) UIView* view;
@property (strong, nonatomic) AIRBPlayerControlView* playerControlView;
@property (assign, nonatomic) BOOL tapGestureRecognizerEnabled;
@property (assign, nonatomic) BOOL twiceTapGestureRecognizerEnabled;
@property (assign, nonatomic) BOOL showControlBar;
@property (assign, nonatomic) float volume;
@property (assign, nonatomic) float rate;
@property (strong, nonatomic) UISlider *progressSlider;
@property (assign, nonatomic) int8_t contentMode;
@property (assign, nonatomic) BOOL autoPlayer;

+ (instancetype) createPlayer;
- (void) prepareWithMediaURL:(NSString*)url;
- (void) pause;
- (void) play;
- (void) stop;
- (void) seekToTime:(int64_t)time;
//- (int64_t) getDuration;
//- (int64_t) getCurrentPosition;
- (void) setAutoPlay:(BOOL)autoPlay;
- (void) snapshot;
- (void) toggleMuted;
@end

NS_ASSUME_NONNULL_END
