//
//  AIRBPlayerControlView.h
//  TBMediaPlayerBundle
//
//  Created by qiufu on 8/30/16.
//  Copyright © 2016 CX. All rights reserved.
//

#import <UIKit/UIKit.h>
//#import <AIRBCommonDefines.h>

@class AIRBPlayerControlView;

typedef NS_ENUM(NSInteger, AIRBPlayerStatus)
{
    AIRBPlayerStatusPlaying = 0,
    AIRBPlayerStatusPaused,
    AIRBPlayerStatusStopped,
    AIRBPlayerStatusStalled,
    AIRBPlayerStatusCompletion
};

#pragma mark - AIRBPlayerControlViewDelegate Interface
@protocol AIRBPlayerControlViewDelegate <NSObject>

@optional
- (void)playButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)pauseButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)fullScreenButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)miniScreenButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)progressSliderBeginToDragInControlView:(AIRBPlayerControlView *)playerControlView;
//- (void)progressSliderCancelDragInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)progressSliderDidDragToTime:(NSTimeInterval)time inControlView:(AIRBPlayerControlView *)playerControlView;
- (void)controlViewDidTapped:(AIRBPlayerControlView *)playerControlView;
- (void)controlViewDidTwiceTapped:(AIRBPlayerControlView *)playerControlView;
- (void)playRateButtonDidClickedInControlView:(float)rate;
@end


#pragma mark - AIRBPlayerControlViewDataSource Interface
@protocol AIRBPlayerControlViewDataSource <NSObject>
@optional
- (NSTimeInterval)playerDurationForControlView:(AIRBPlayerControlView *)playerControlView;
- (NSTimeInterval)playerCurrentPlaybackTimeForControlView:(AIRBPlayerControlView *)playerControlView;
- (BOOL)isPlayerPlayingForControlView:(AIRBPlayerControlView *)playerControlView;
@end


#pragma mark - AIRBPlayerControlView Interface
@interface AIRBPlayerControlView : UIView

@property (weak, nonatomic) id<AIRBPlayerControlViewDelegate> delegate;
@property (weak, nonatomic) id<AIRBPlayerControlViewDataSource> dataSource;

@property (assign, nonatomic) BOOL gestureRecognizerEnabled; //!< 是否开启播放控制界面的手势识别功能。
@property (assign, nonatomic) BOOL tapGestureRecognizerEnabled; //!< 是否开启单击手势识别。
@property (assign, nonatomic) BOOL twiceTapGestureRecognizerEnabled; //!< 是否开启双击手势识别。
@property (assign, nonatomic) BOOL panGestureRecognizerEnabled; //!< 是否开启上下左右滑动手势识别。

@property (assign, nonatomic) BOOL showControlBar; //!< 是否显示播放器控制条。
@property (strong, nonatomic) UIColor *controlBarBackgroundColor; //!< 播放器控制条背景颜色。
@property (assign, nonatomic) BOOL showFullScreenButton; //!< 是否显示全屏按钮。
@property (strong, nonatomic) UIButton *fullScreenButton;
@property (assign, nonatomic) BOOL showPlayrateButton;

@property (strong, nonatomic) UISlider *progressSlider; //!< 方便其他业务定制颜色等效果

- (instancetype)initWithFrame:(CGRect)frame  options:(NSDictionary*)options;
- (void)refreshPlayButtonStatus:(AIRBPlayerStatus)status; //!< 刷新播放按钮的状态。
- (void)refreshPlayerProgressWithTimeInfo:(NSDictionary *)info; //!< 刷新播放进度。

@end
