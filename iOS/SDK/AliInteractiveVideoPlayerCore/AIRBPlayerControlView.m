//
//  AIRBPlayerControlView.m
//  TBMediaPlayerBundle
//
//  Created by qiufu on 8/30/16.
//  Copyright © 2016 CX. All rights reserved.
//

#import "AIRBPlayerControlView.h"
#import <MediaPlayer/MediaPlayer.h>

#pragma mark - AIRBPlayerControlView Implementation
@interface AIRBPlayerControlView ()
@property (strong, nonatomic) UIView *gestureRecognizerView;
@property (strong, nonatomic) UITapGestureRecognizer *twiceTapGesture;
@property (strong, nonatomic) UITapGestureRecognizer *tapGesture;
@property (strong, nonatomic) UIPanGestureRecognizer *panGesture;

@property (strong, nonatomic) UIView *playerControlBar;
@property (strong, nonatomic) UIButton *playButton;
@property (strong, nonatomic) UIActivityIndicatorView *activityIndicatorView;
@property (strong, nonatomic) UILabel *currentTimeLabel;
@property (strong, nonatomic) UILabel *totalTimeLabel;
@property (strong, nonatomic) NSLayoutConstraint *totalTimeLabelLeftLayoutConstraint;
@property (strong, nonatomic) NSLayoutConstraint *playRateButtonRightLayoutConstraint;
@property (assign, nonatomic) BOOL isProgressSliderDragging;
@property (assign, nonatomic) AIRBPlayerStatus status;

@property (strong, nonatomic) MPVolumeView *volumeView;
@property (strong, nonatomic) UISlider *volumeSlider;

@property (strong, nonatomic) UIButton *playRateButton;
@end


@implementation AIRBPlayerControlView
@synthesize gestureRecognizerEnabled = _gestureRecognizerEnabled;
@synthesize controlBarBackgroundColor = _controlBarBackgroundColor;

#pragma mark - Property
- (UIView *)gestureRecognizerView {
    if (!_gestureRecognizerView) {
        _gestureRecognizerView = [[UIView alloc] init];
        
        // Add twice tap gesture.
        self.twiceTapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onGestureRecognizerViewTwiceTapped:)];
        self.twiceTapGesture.numberOfTapsRequired = 2;
        [_gestureRecognizerView addGestureRecognizer:self.twiceTapGesture];
        
        // Add tap gesture.
        self.tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onGestureRecognizerViewTapped:)];
        self.tapGesture.numberOfTapsRequired = 1;
        [self.tapGesture requireGestureRecognizerToFail:self.twiceTapGesture];
        [_gestureRecognizerView addGestureRecognizer:self.tapGesture];
        
        // Add pan gesture.
        self.panGesture = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(onGestureRecognizerViewPanned:)];
        [_gestureRecognizerView addGestureRecognizer:self.panGesture];
        
    }
    
    return _gestureRecognizerView;
}

- (UIButton *)playButton {
    if (!_playButton) {
        _playButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_playButton setTitle:@"▶︎" forState:UIControlStateNormal];
        [_playButton setTitle:@"▶︎" forState:UIControlStateNormal | UIControlStateHighlighted];
        [_playButton setTitle:@"| |" forState:UIControlStateSelected];
        [_playButton setTitle:@"| |" forState:UIControlStateSelected | UIControlStateHighlighted];
        
        [_playButton addTarget:self action:@selector(onPlayButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    return _playButton;
}

- (UIButton *)playRateButton {
    if (!_playRateButton) {
        _playRateButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_playRateButton setTitle:@"倍速" forState:UIControlStateNormal];
        [_playRateButton.titleLabel setFont:[UIFont systemFontOfSize:12]];
        [_playRateButton setBackgroundColor:[UIColor clearColor]];
        _playRateButton.layer.borderWidth = 1.0;
        _playRateButton.layer.borderColor = [UIColor whiteColor].CGColor;
        _playRateButton.layer.masksToBounds = YES;
        _playRateButton.layer.cornerRadius = 8.5;
        _playRateButton.hidden = NO;
        
        [_playRateButton addTarget:self action:@selector(playRateButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _playRateButton;
}

- (UIActivityIndicatorView *)activityIndicatorView {
    if (!_activityIndicatorView) {
        _activityIndicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
    }
    
    return _activityIndicatorView;
}

- (UILabel *)currentTimeLabel {
    if (!_currentTimeLabel) {
        _currentTimeLabel = [[UILabel alloc] init];
        _currentTimeLabel.text = @"00:00:00";
        _currentTimeLabel.font = [UIFont systemFontOfSize:10.0f];
        _currentTimeLabel.textAlignment = NSTextAlignmentLeft;
        _currentTimeLabel.textColor = [UIColor whiteColor];
    }
    
    return _currentTimeLabel;
}

- (UILabel *)totalTimeLabel {
    if (!_totalTimeLabel) {
        _totalTimeLabel = [[UILabel alloc] init];
        _totalTimeLabel.text = @"00:00:00";
        _totalTimeLabel.font = [UIFont systemFontOfSize:10.0f];
        _totalTimeLabel.textAlignment = NSTextAlignmentRight;
        _totalTimeLabel.textColor = [UIColor whiteColor];
    }
    
    return _totalTimeLabel;
}

- (UIButton *)fullScreenButton {
    if (!_fullScreenButton) {
        _fullScreenButton = [UIButton buttonWithType:UIButtonTypeCustom];
        [_fullScreenButton setImage:[UIImage imageNamed:@"TBMediaPlayerBundle.bundle/button_fullscreen"]  forState:UIControlStateNormal];
        [_fullScreenButton setImage:[UIImage imageNamed:@"TBMediaPlayerBundle.bundle/button_fullcreen"] forState:UIControlStateNormal | UIControlStateHighlighted];
        [_fullScreenButton setImage:[UIImage imageNamed:@"TBMediaPlayerBundle.bundle/button_miniscreen"] forState:UIControlStateSelected];
        [_fullScreenButton setImage:[UIImage imageNamed:@"TBMediaPlayerBundle.bundle/button_miniscreen"] forState:UIControlStateSelected | UIControlStateHighlighted];
        
        _fullScreenButton.selected = NO;
        
        [_fullScreenButton addTarget:self action:@selector(onFullScreenButtonClicked:) forControlEvents:UIControlEventTouchUpInside];
    }
    
    return _fullScreenButton;
}

- (void)setShowFullScreenButton:(BOOL)showFullScreenButton {
    if (_showFullScreenButton != showFullScreenButton) {
        _showFullScreenButton = showFullScreenButton;
        self.fullScreenButton.hidden = !showFullScreenButton;
        if (showFullScreenButton) {
            self.totalTimeLabelLeftLayoutConstraint.constant = -5-35-8;
            if (self.showPlayrateButton) {
                self.totalTimeLabelLeftLayoutConstraint.constant = -5-35-8-35-5;
                self.playRateButtonRightLayoutConstraint.constant = -8-35-5;
            }
        } else {
            self.totalTimeLabelLeftLayoutConstraint.constant = -8;
            if (self.showPlayrateButton) {
                self.totalTimeLabelLeftLayoutConstraint.constant = -5-35-8;
                self.playRateButtonRightLayoutConstraint.constant = -8;
            }
        }
        [self.totalTimeLabel layoutIfNeeded];
    }
}

- (UISlider *)progressSlider {
    if (!_progressSlider) {
        _progressSlider = [[UISlider alloc] init];
        [_progressSlider setMinimumTrackTintColor:[UIColor colorWithRed:1 green:1 blue:1 alpha:1]];
//        [_progressSlider setMinimumTrackTintColor:[UIColor colorWithRed:255/255.0 green:68/255.0 blue:0 alpha:1]];
        [_progressSlider setMaximumTrackTintColor:[UIColor colorWithRed:1 green:1 blue:1 alpha:0.3f]];
        UIImage *thumbImage = [AIRBPlayerControlView circleImageWithColor:[UIColor whiteColor] size:CGSizeMake(18, 18)];
        [_progressSlider setThumbImage:thumbImage forState:UIControlStateNormal];
        [_progressSlider setThumbImage:thumbImage forState:UIControlStateHighlighted];
        
        [_progressSlider addTarget:self action:@selector(onProgressSliderTouchDown:) forControlEvents:UIControlEventTouchDown];
        //[_progressSlider addTarget:self action:@selector(onProgressSliderTouchCancel:) forControlEvents:UIControlEventTouchCancel];
        [_progressSlider addTarget:self action:@selector(onProgressSliderTouchUp:) forControlEvents:UIControlEventTouchUpInside | UIControlEventTouchUpOutside | UIControlEventTouchCancel];
        [_progressSlider addTarget:self action:@selector(onProgressSliderValueChanged:) forControlEvents:UIControlEventValueChanged];
    }
    
    return _progressSlider;
}

- (UIView *)playerControlBar {
    if (!_playerControlBar) {
        _playerControlBar = [[UIView alloc] init];
        
        // Set background color.
        _playerControlBar.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
                
        // Add play button.
        [_playerControlBar addSubview:self.playButton];
        self.playButton.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
            [NSLayoutConstraint constraintWithItem:self.playButton attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeLeft multiplier:1.0 constant:5],
            [NSLayoutConstraint constraintWithItem:self.playButton attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.playButton attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:35],
            [NSLayoutConstraint constraintWithItem:self.playButton attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:35]
        ]];
        
        // Add activityIndicatorView.
        [_playerControlBar addSubview:self.activityIndicatorView];
        self.activityIndicatorView.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
            [NSLayoutConstraint constraintWithItem:self.activityIndicatorView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeCenterX multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.activityIndicatorView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.activityIndicatorView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:16],
            [NSLayoutConstraint constraintWithItem:self.activityIndicatorView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:16]
        ]];
        
        // Add current time label.
        [_playerControlBar addSubview:self.currentTimeLabel];
        self.currentTimeLabel.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
            [NSLayoutConstraint constraintWithItem:self.currentTimeLabel attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeRight multiplier:1.0 constant:7],
            [NSLayoutConstraint constraintWithItem:self.currentTimeLabel attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.currentTimeLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:46],
            [NSLayoutConstraint constraintWithItem:self.currentTimeLabel attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:52]
        ]];
        
        // Add fullScreen button.
        [_playerControlBar addSubview:self.fullScreenButton];
        self.fullScreenButton.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
            [NSLayoutConstraint constraintWithItem:self.fullScreenButton attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-5],
            [NSLayoutConstraint constraintWithItem:self.fullScreenButton attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.fullScreenButton attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:35],
            [NSLayoutConstraint constraintWithItem:self.fullScreenButton attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:35]
        ]];
        self.fullScreenButton.hidden = !self.showFullScreenButton;
        
        
        // Add total time label.
        [_playerControlBar addSubview:self.totalTimeLabel];
        self.totalTimeLabel.translatesAutoresizingMaskIntoConstraints = NO;
        if (self.showFullScreenButton) {
            self.totalTimeLabelLeftLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-5-35-8];
            if (self.showPlayrateButton) {
                self.totalTimeLabelLeftLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-5-35-8-35-5];
            }
        } else {
            self.totalTimeLabelLeftLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-8];
            if (self.showPlayrateButton) {
                self.totalTimeLabelLeftLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-8-35-5];
            }
        }
        [_playerControlBar addConstraints:@[
            self.totalTimeLabelLeftLayoutConstraint,
            [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.currentTimeLabel attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:46],
            [NSLayoutConstraint constraintWithItem:self.totalTimeLabel attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:52]
        ]];
        
        if (self.showFullScreenButton) {
            self.playRateButtonRightLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.playRateButton attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-8-35-5];
        } else {
            self.playRateButtonRightLayoutConstraint = [NSLayoutConstraint constraintWithItem:self.playRateButton attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:_playerControlBar attribute:NSLayoutAttributeRight multiplier:1.0 constant:-8];
        }
        
        [_playerControlBar addSubview:self.playRateButton];
        self.playRateButton.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
                                            self.playRateButtonRightLayoutConstraint,
                                            [NSLayoutConstraint constraintWithItem:self.playRateButton attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.playButton attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
                                            [NSLayoutConstraint constraintWithItem:self.playRateButton attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:21],
                                            [NSLayoutConstraint constraintWithItem:self.playRateButton attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:35]
                                            ]];
        self.playRateButton.hidden = !self.showPlayrateButton;
        
        // Add progress slider.
        [_playerControlBar addSubview:self.progressSlider];
        self.progressSlider.translatesAutoresizingMaskIntoConstraints = NO;
        [_playerControlBar addConstraints:@[
            [NSLayoutConstraint constraintWithItem:self.progressSlider attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.currentTimeLabel attribute:NSLayoutAttributeRight multiplier:1.0 constant:6],
            [NSLayoutConstraint constraintWithItem:self.progressSlider attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.totalTimeLabel attribute:NSLayoutAttributeLeft multiplier:1.0 constant:-6],
            [NSLayoutConstraint constraintWithItem:self.progressSlider attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self.currentTimeLabel attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
            [NSLayoutConstraint constraintWithItem:self.progressSlider attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:18]
        ]];
    }
    
    return _playerControlBar;
}

- (BOOL)gestureRecognizerEnabled {
    if (_gestureRecognizerView) {
        return _gestureRecognizerView.userInteractionEnabled;
    } else {
        return NO;
    }
}

- (void)setGestureRecognizerEnabled:(BOOL)gestureRecognizerEnabled {
    if (_gestureRecognizerView) {
        _gestureRecognizerView.userInteractionEnabled = gestureRecognizerEnabled;
    }
}

- (BOOL)tapGestureRecognizerEnabled {
    if (_tapGesture) {
        return self.tapGesture.isEnabled;
    } else {
        return NO;
    }
}

- (void)setTapGestureRecognizerEnabled:(BOOL)tapGestureRecognizerEnabled {
    if (_tapGesture) {
        self.tapGesture.enabled = tapGestureRecognizerEnabled;
    }
}

- (BOOL)twiceTapGestureRecognizerEnabled {
    if (_twiceTapGesture) {
        return self.twiceTapGesture.isEnabled;;
    } else {
        return NO;
    }
}

- (void)setTwiceTapGestureRecognizerEnabled:(BOOL)tapGestureRecognizerEnabled {
    if (_twiceTapGesture) {
        self.twiceTapGesture.enabled = tapGestureRecognizerEnabled;
    }
}

- (BOOL)panGestureRecognizerEnabled {
    if (_panGesture) {
        return self.panGesture.isEnabled;
    } else {
        return NO;
    }
}

- (void)setPanGestureRecognizerEnabled:(BOOL)panGestureRecognizerEnabled {
    if (_panGesture) {
        self.panGesture.enabled = panGestureRecognizerEnabled;
    }
}

- (BOOL)showControlBar {
    if (_playerControlBar) {
        return !self.playerControlBar.hidden;
    } else {
        return NO;
    }
}

- (void)setShowControlBar:(BOOL)showControlBar {
    if (_playerControlBar) {
        self.playerControlBar.hidden = !showControlBar;
    }
}

- (UIColor *)controlBarBackgroundColor {
    if (_playerControlBar) {
        return _playerControlBar.backgroundColor;
    } else {
        return nil;
    }
}

- (void)setControlBarBackgroundColor:(UIColor *)controlBarBackgroundColor {
    if (_playerControlBar) {
        _playerControlBar.backgroundColor = controlBarBackgroundColor;
    }
}

- (MPVolumeView *)volumeView {
    if (!_volumeView) {
        _volumeView = [[MPVolumeView alloc] init];
        
        _volumeView.showsRouteButton = NO;
        _volumeView.showsVolumeSlider = YES;
        
        // Make volume view vertical.
        _volumeView.transform = CGAffineTransformMakeRotation(-M_PI_2);
        
        _volumeView.hidden = YES;
    }
    
    return _volumeView;
}

- (UISlider *)volumeSlider {
    if (!_volumeSlider) {
        for (UIView *view in [self.volumeView subviews]){
            if ([[view.class description] isEqualToString:@"MPVolumeSlider"]) {
                _volumeSlider = (UISlider*) view;
                break;
            }
        }
    }
    
    return _volumeSlider;
}


#pragma mark - Lifecycle

- (instancetype)initWithFrame:(CGRect)frame options:(NSDictionary*)options{
    self = [super initWithFrame:frame];
    if (self) {
        _gestureRecognizerEnabled = YES;
        _isProgressSliderDragging = NO;
        _showFullScreenButton = NO;
        _showPlayrateButton = NO;
        if (options && [options isKindOfClass:[NSDictionary class]] && options[@"playrateButtonHidden"]) {
            _showPlayrateButton = ![options[@"playrateButtonHidden"] boolValue];
        }
        
        // Add gesture recognizer view.
        [self addSubview:self.gestureRecognizerView];
        self.gestureRecognizerView.translatesAutoresizingMaskIntoConstraints = NO;
        [self addConstraints:@[
           [NSLayoutConstraint constraintWithItem:self.gestureRecognizerView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.gestureRecognizerView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.gestureRecognizerView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.gestureRecognizerView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1.0 constant:0]
        ]];
        
        // Add player control bar.
        [self addSubview:self.playerControlBar];
        self.playerControlBar.translatesAutoresizingMaskIntoConstraints = NO;
        [self addConstraints:@[
           [NSLayoutConstraint constraintWithItem:self.playerControlBar attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.playerControlBar attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.playerControlBar attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.playerControlBar attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:45]
        ]];
        
        /*
        // Add volume control view. Note: volume control view is tranformed -90.
        [self addSubview:self.volumeView];
        self.volumeView.translatesAutoresizingMaskIntoConstraints = NO;
        [self addConstraints:@[
           [NSLayoutConstraint constraintWithItem:self.volumeView attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1.0 constant:30],
           [NSLayoutConstraint constraintWithItem:self.volumeView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1.0 constant:0],
           [NSLayoutConstraint constraintWithItem:self.volumeView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:200],
           [NSLayoutConstraint constraintWithItem:self.volumeView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1.0 constant:30]
        ]];
         */
    }
    
    return self;
}

#pragma mark - Action
- (void)onGestureRecognizerViewTapped:(id)sender {
    if ([self.delegate respondsToSelector:@selector(controlViewDidTapped:)]) {
        [self.delegate controlViewDidTapped:self];
    }
}

- (void)onGestureRecognizerViewTwiceTapped:(id)sender {
    if ([self.delegate respondsToSelector:@selector(controlViewDidTwiceTapped:)]) {
        [self.delegate controlViewDidTwiceTapped:self];
    }
}

- (void)onGestureRecognizerViewPanned:(id)sender {
//    debugLog(@"AIRBPlayerControlView-Panned");
    
    // 1: up/down, volume control mode; 2: left/right, progress control mode.
    static int32_t panMode = 0;
    
    UIPanGestureRecognizer *panGesture = (UIPanGestureRecognizer *) sender;
    CGPoint offset = [panGesture translationInView:self.gestureRecognizerView];
    
    if (panGesture.state == UIGestureRecognizerStateBegan) {
        // Judge pan control mode.
        if (fabs(offset.x) < fabs(offset.y)) {
            panMode = 1; // Volume control mode.
        } else {
            panMode = 2; // Progress control mode.
        }
        
        // Different control logic in different control mode.
        if (panMode == 1) {
            
        } else if (panMode == 2) {
            if (![self isMediaItemLiveVideo]) {
                [self onProgressSliderTouchDown:nil];
            }
        }
        
    } else if (panGesture.state == UIGestureRecognizerStateChanged) {
        // Different control logic in different control mode.
        if (panMode == 1) {
            float offsetVolume = 0;
            if (self.frame.size.width != 0) {
                offsetVolume = self.volumeSlider.maximumValue * -offset.y / self.frame.size.width;
            }
            self.volumeSlider.value += offsetVolume;
        } else if (panMode == 2) {
            if (![self isMediaItemLiveVideo]) {
                NSTimeInterval totalTimeInterval = 0.0f;
                if ([self.dataSource respondsToSelector:@selector(playerDurationForControlView:)]) {
                    totalTimeInterval = [self.dataSource playerDurationForControlView:self];
                }
                
                // total-time / control-view-width = offset-time / offset-x.
                float offsetTime = 0;
                if (self.frame.size.width != 0) {
                    offsetTime = totalTimeInterval * offset.x / self.frame.size.width;
                }
                self.progressSlider.value += offsetTime;
            }
        }
        
    } else if (panGesture.state == UIGestureRecognizerStateCancelled) {
        // Different control logic in different control mode.
        if (panMode == 1) {
            
        } else if (panMode == 2) {
            if (![self isMediaItemLiveVideo]) {
                [self onProgressSliderTouchUp:nil];
            }
        }
        
    } else if (panGesture.state == UIGestureRecognizerStateEnded) {
        // Different control logic in different control mode.
        if (panMode == 1) {
            
        } else if (panMode == 2) {
            if (![self isMediaItemLiveVideo]) {
                [self onProgressSliderTouchUp:nil];
            }
        }
        
    }
    
    [panGesture setTranslation:CGPointZero inView:self.gestureRecognizerView];

}

- (void)onPlayButtonClicked:(id)sender {
    UIButton *button = (UIButton *) sender;
    if (button.selected) {
        if ([self.delegate respondsToSelector:@selector(pauseButtonDidClickedInControlView:)]) {
            [self.delegate pauseButtonDidClickedInControlView:self];
        }
        button.selected = NO;
    } else {
        if ([self.delegate respondsToSelector:@selector(playButtonDidClickedInControlView:)]) {
            [self.delegate playButtonDidClickedInControlView:self];
        }
        button.selected = YES;
    }
}

- (void)playRateButtonClicked:(UIButton *)button {
    NSString *speed = @"1.0";
    if ([button.titleLabel.text isEqualToString:@"倍速"] || [button.titleLabel.text isEqualToString:@"1.0X"]) {
        [button setTitle:@"1.5X" forState:UIControlStateNormal];
        if ([self.delegate respondsToSelector:@selector(playRateButtonDidClickedInControlView:)]) {
            [self.delegate playRateButtonDidClickedInControlView:1.5f];
        }
        speed = @"1.5";
    } else if ([button.titleLabel.text isEqualToString:@"1.5X"]) {
        [button setTitle:@"2.0X" forState:UIControlStateNormal];
        if ([self.delegate respondsToSelector:@selector(playRateButtonDidClickedInControlView:)]) {
            [self.delegate playRateButtonDidClickedInControlView:2.0f];
        }
        speed = @"2.0";
    } else if ([button.titleLabel.text isEqualToString:@"2.0X"]) {
        [button setTitle:@"1.0X" forState:UIControlStateNormal];
        if ([self.delegate respondsToSelector:@selector(playRateButtonDidClickedInControlView:)]) {
            [self.delegate playRateButtonDidClickedInControlView:1.0f];
        }
        speed = @"1.0";
    }
}

- (void)onFullScreenButtonClicked:(id)sender {
    UIButton *button = (UIButton *) sender;
    if (button.selected == NO) { // Will enter full screen.
        if ([self.delegate respondsToSelector:@selector(fullScreenButtonDidClickedInControlView:)]) {
            [self.delegate fullScreenButtonDidClickedInControlView:self];
        }
        button.selected = YES;

    } else { // Will leave full screen.
        if ([self.delegate respondsToSelector:@selector(miniScreenButtonDidClickedInControlView:)]) {
            [self.delegate miniScreenButtonDidClickedInControlView:self];
        }
        button.selected = NO;
    }
}

- (void)onProgressSliderTouchDown:(id)sender {
//    debugLog(@"AIRBPlayerControlView-Slider: Down");
    self.isProgressSliderDragging = YES;
    if ([self.delegate respondsToSelector:@selector(progressSliderBeginToDragInControlView:)]) {
        [self.delegate progressSliderBeginToDragInControlView:self];
    }
}

//- (void)onProgressSliderTouchCancel:(id)sender {
//    debugLog(@"AIRBPlayerControlView-Slider: Cancel");
//    if ([self.delegate respondsToSelector:@selector(progressSliderCancelDragInControlView:)]) {
//        [self.delegate progressSliderCancelDragInControlView:self];
//    }
//    self.isProgressSliderDragging = NO;
//}

- (void)onProgressSliderTouchUp:(id)sender {
//    debugLog(@"AIRBPlayerControlView-Slider: Up");
    if ([self.delegate respondsToSelector:@selector(progressSliderDidDragToTime:inControlView:)]) {
        [self.delegate progressSliderDidDragToTime:self.progressSlider.value inControlView:self];
    }
    self.currentTimeLabel.text = [self timeStringForTimeInterval:self.progressSlider.value];
    self.isProgressSliderDragging = NO;
}

- (void)onProgressSliderValueChanged:(id)sender {
//    debugLog(@"AIRBPlayerControlView-Slider: ValueChanged");
    self.currentTimeLabel.text = [self timeStringForTimeInterval:self.progressSlider.value];
}

#pragma mark - Utility
- (void)refreshPlayButtonStatus:(AIRBPlayerStatus)status {
    self.status = status;
    switch (status) {
        case AIRBPlayerStatusPlaying: {
            [self.activityIndicatorView stopAnimating];
            self.playButton.hidden = NO;
            self.playButton.selected = YES;
            break;
        }

        case AIRBPlayerStatusPaused: {
            [self.activityIndicatorView stopAnimating];
            self.playButton.hidden = NO;
            self.playButton.selected = NO;
            break;
        }

        case AIRBPlayerStatusStopped: {
            [self.activityIndicatorView stopAnimating];
            self.playButton.hidden = NO;
            self.playButton.selected = NO;
            break;
        }

//        case AIRBPlayerStatusLoading:
        case AIRBPlayerStatusStalled:
//        case AIRBPlayerStatusSeekingForward:
//        case AIRBPlayerStatusSeekingBackward:
        {
            self.playButton.hidden = YES;
            [self.activityIndicatorView startAnimating];
            break;
        }

        default:
            break;
    }
}

- (void)refreshPlayerProgressWithTimeInfo:(NSDictionary *)info {
    if (self.isProgressSliderDragging || self.status == AIRBPlayerStatusStalled) {
        return;
    }
//    if (self.isProgressSliderDragging || self.status == AIRBPlayerStatusLoading || self.status == AIRBPlayerStatusSeekingForward || self.status == AIRBPlayerStatusSeekingBackward || self.status == AIRBPlayerStatusStalled) {
//        return;
//    }

    // Get video total time and current time from player.
    if ([info objectForKey:@"duration"]){
        // Get video total time and current time from player.
        NSTimeInterval totalTimeInterval = [[info objectForKey:@"duration"] doubleValue] / 1000;
        // Set slider progress.
        self.progressSlider.maximumValue = totalTimeInterval;
        // Set time label text.
        self.totalTimeLabel.text = [self timeStringForTimeInterval:totalTimeInterval];
    }
    
    if ([info objectForKey:@"currentTime"]){
        NSTimeInterval currentTimeInterval = [[info objectForKey:@"currentTime"] doubleValue] / 1000;
        self.progressSlider.value = currentTimeInterval;
        self.progressSlider.userInteractionEnabled = YES;
        self.currentTimeLabel.text = [self timeStringForTimeInterval:currentTimeInterval];
    }
    // Set progress.
//    if ([self isMediaItemLiveVideo]) { // Live video.
//        // Set slider progress.
//        self.progressSlider.maximumValue = 1.0f;
//        self.progressSlider.value = 0.0f;
//        self.progressSlider.userInteractionEnabled = NO;
//
//        // Set time label text.
//        self.currentTimeLabel.text = @"00:00:00";
//        self.totalTimeLabel.text = @"00:00:00";
//    } else {

//    }

}

- (BOOL)isMediaItemLiveVideo {
    NSTimeInterval totalTimeInterval = 0.0f;
    if ([self.dataSource respondsToSelector:@selector(playerDurationForControlView:)]) {
        totalTimeInterval = [self.dataSource playerDurationForControlView:self];
    }
    
    if (totalTimeInterval == 0 || isnan(totalTimeInterval)) { // Live video.
        return YES;
    } else {
        return NO;
    }
}

- (NSString *)timeStringForTimeInterval:(NSTimeInterval)timeInterval {
    int64_t totalSeconds = (int64_t) timeInterval;
    int32_t hours = (int32_t) totalSeconds / 3600;
    int32_t minutes = (int32_t) totalSeconds % 3600 / 60;
    int32_t seconds = (int32_t) totalSeconds % 3600 % 60;
    
    NSString *timeString = @"00:00:00";
    timeString = [NSString stringWithFormat:@"%02d:%02d:%02d", hours, minutes, seconds];
    
    return timeString;
}

+ (UIImage *)circleImageWithColor:(UIColor *)color size:(CGSize)size {
    CGRect rect = CGRectMake(0.0f, 0.0f, size.width, size.height);
    UIGraphicsBeginImageContextWithOptions(rect.size, NO, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSaveGState(context);
    
    CGContextSetFillColorWithColor(context, [color CGColor]);
    //CGContextFillRect(context, rect);
    CGContextFillEllipseInRect(context, rect);
    
    CGContextRestoreGState(context);
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    return image;
}

@end
