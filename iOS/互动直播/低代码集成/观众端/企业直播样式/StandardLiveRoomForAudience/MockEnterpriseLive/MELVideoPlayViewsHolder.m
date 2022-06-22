//
//  MELVideoPlayViewsHolder.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/31.
//

#import "MELVideoPlayViewsHolder.h"
#import "MELLiveInfoViewHolder.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"
#import "UIColor+ColorWithHexString.h"

@interface MELVideoPlayViewsHolder()
@property (strong, nonatomic) MELLiveInfoViewHolder* infoView;
@property (strong, nonatomic) UIButton* landscapeButton;
@property (strong, nonatomic) UILabel* liveNotStartedTextLabel;
@property (strong, nonatomic) UILabel* liveEndedTextLabel;
@property (strong, nonatomic) UILabel* loadingTextLabel;
@property (strong, nonatomic) UIImageView* loadingView;
@property (strong, nonatomic) UILabel* loadCompletedTextLabel;
@property (strong, nonatomic) UIButton* reloadButton;
@property (strong, nonatomic) UIVisualEffectView* livePushStoppedBackgroundView;
@property (assign, nonatomic) BOOL livePushStopped;
@property (assign, nonatomic) BOOL liveEnded;
@end

@implementation MELVideoPlayViewsHolder

- (MELLiveInfoViewHolder*)infoView {
    if (!_infoView) {
        _infoView = [[MELLiveInfoViewHolder alloc] init];
        _infoView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        _infoView.layer.masksToBounds = YES;
        _infoView.layer.cornerRadius = 21;
        [self addSubview:_infoView];
        [_infoView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.mas_top).with.offset(13);
            make.right.equalTo(self.mas_right).with.offset(-16);
            make.size.mas_equalTo(CGSizeMake(130, 42));
        }];
    }
    return _infoView;
}

- (UIButton*)landscapeButton {
    if (!_landscapeButton) {
        _landscapeButton = [[UIButton alloc] init];
        [_landscapeButton addTarget:self action:@selector(onLandscapeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_landscapeButton setImage:[UIImage imageNamed:@"视频放大" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:_landscapeButton];
        [_landscapeButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(24, 24));
            make.right.equalTo(self.mas_right).with.offset(-16);
            make.bottom.equalTo(self.mas_bottom).with.offset(-10);
        }];
    }
    return _landscapeButton;
}

-(UILabel*)liveNotStartedTextLabel {
    if (!_liveNotStartedTextLabel) {
        _liveNotStartedTextLabel = [[UILabel alloc] init];
        _liveNotStartedTextLabel.hidden = YES;
//        _liveNotStartedTextLabel.text = @"直播未开始\n将于";
        
        UILabel* statusLabel = [[UILabel alloc] init];
        statusLabel.text = @"直播未开始";
        statusLabel.textColor = [UIColor whiteColor];
        statusLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:14];
        statusLabel.textAlignment = NSTextAlignmentCenter;
        [_liveNotStartedTextLabel addSubview:statusLabel];
        [statusLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(_liveNotStartedTextLabel);
            make.top.equalTo(_liveNotStartedTextLabel);
            make.size.mas_equalTo(CGSizeMake(70, 20));
        }];
        
        UILabel* timeLabel = [[UILabel alloc] init];
//        timeLabel.text = @"将于";
        timeLabel.textColor = [UIColor whiteColor];
        timeLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        timeLabel.textAlignment = NSTextAlignmentCenter;
        [_liveNotStartedTextLabel addSubview:timeLabel];
        [timeLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(statusLabel.mas_bottom).with.offset(6);
            make.centerX.equalTo(_liveNotStartedTextLabel);
            make.size.mas_equalTo(CGSizeMake(160, 17));
        }];
        
        [self addSubview:_liveNotStartedTextLabel];
        [_liveNotStartedTextLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(168, 45));
            make.center.equalTo(self);
        }];
    }
    return _liveNotStartedTextLabel;
}

-(UILabel*)liveEndedTextLabel {
    if (!_liveEndedTextLabel) {
        _liveEndedTextLabel = [[UILabel alloc] init];
        _liveEndedTextLabel.hidden = YES;
        _liveEndedTextLabel.text = @"直播已结束";
        _liveEndedTextLabel.textAlignment = NSTextAlignmentCenter;
        _liveEndedTextLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16];
        _liveEndedTextLabel.textColor = [UIColor whiteColor];
        [self addSubview:_liveEndedTextLabel];
        [_liveEndedTextLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(168, 20));
            make.center.equalTo(self);
        }];
    }
    return _liveEndedTextLabel;
}

-(UILabel*)loadingTextLabel {
    if (!_loadingTextLabel) {
        _loadingTextLabel = [[UILabel alloc] init];
        _loadingTextLabel.hidden = YES;
        _loadingTextLabel.text = @"加载中...";
        _loadingTextLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
        _loadingTextLabel.textColor = [UIColor whiteColor];
        [self addSubview:_loadingTextLabel];
        [_loadingTextLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(56, 20));
            make.centerX.equalTo(self);
            make.top.equalTo(self.loadingView.mas_bottom).with.offset(12);
        }];
    }
    return _loadingTextLabel;
}

- (UIImageView*)loadingView {
    if (!_loadingView) {
        _loadingView = [[UIImageView alloc] init];
        _loadingView.hidden = YES;
        _loadingView.image = [UIImage imageNamed:@"视频-加载中" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
        [self addSubview:_loadingView];
        [_loadingView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(26, 26));
            make.centerX.equalTo(self);
            make.centerY.equalTo(self);
        }];
        
        CABasicAnimation* animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
        animation.fromValue = @(0.0);
        animation.toValue = @(2 * M_PI);
        animation.duration = 2.0;
        animation.autoreverses = NO;
        animation.fillMode = kCAFillModeForwards;
        animation.repeatCount = MAXFLOAT;
        
        [_loadingView.layer addAnimation:animation forKey:nil];
    }
    return _loadingView;
}

-(UILabel*)loadCompletedTextLabel {
    if (!_loadCompletedTextLabel) {
        _loadCompletedTextLabel = [[UILabel alloc] init];
        _loadCompletedTextLabel.hidden = YES;
        _loadCompletedTextLabel.text = @"加载失败，请点击刷新";
        _loadCompletedTextLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
        _loadCompletedTextLabel.textColor = [UIColor whiteColor];
        [self addSubview:_loadCompletedTextLabel];
        [_loadCompletedTextLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(140, 20));
            make.centerX.equalTo(self);
            make.top.equalTo(self.reloadButton.mas_bottom).with.offset(12);
        }];
    }
    return _loadCompletedTextLabel;
}

- (UIButton*)reloadButton {
    if (!_reloadButton) {
        _reloadButton = [[UIButton alloc] init];
        _reloadButton.hidden = YES;
        [_reloadButton addTarget:self action:@selector(onReloadVideoButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_reloadButton setImage:[UIImage imageNamed:@"视频-加载失败" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:_reloadButton];
        [_reloadButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(26, 26));
            make.centerX.equalTo(self);
            make.centerY.equalTo(self);
        }];
    }
    return _reloadButton;
}

- (UIVisualEffectView*)livePushStoppedBackgroundView {
    if (!_livePushStoppedBackgroundView) {
        UIVisualEffect *blurEffect = [UIBlurEffect effectWithStyle:UIBlurEffectStyleLight];
        _livePushStoppedBackgroundView = [[UIVisualEffectView alloc] initWithEffect:blurEffect];
        
        [self addSubview:_livePushStoppedBackgroundView];
        [_livePushStoppedBackgroundView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self);
        }];
        
        UILabel* textLabel = [[UILabel alloc] init];
        textLabel.text = @"主播暂时离开，请稍候~";
        textLabel.textColor = [UIColor blackColor];
        textLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
        [_livePushStoppedBackgroundView.contentView addSubview:textLabel];
        [textLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(168, 17));
            make.centerX.equalTo(_livePushStoppedBackgroundView);
            make.centerY.equalTo(_livePushStoppedBackgroundView);
        }];
    }
    return _livePushStoppedBackgroundView;
}

- (void)setAnchorAvartarImageURL:(NSURL *)anchorAvartarImageURL {
    if (anchorAvartarImageURL) {
        [self.infoView.anchorAvartarView setImage:[UIImage imageWithData:[NSData dataWithContentsOfURL:anchorAvartarImageURL]]];
    }
}

- (void)setAnchorNick:(NSString *)anchorNick {
    self.infoView.anchorNickLabel.text = anchorNick;
}

- (void)setLivePrestartTimestamp:(NSString *)livePrestartTimestamp {
    NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
    [formatter setDateFormat:@"yyyy-MM-dd HH:mm"];
    NSString *currentTime = [formatter stringFromDate:[NSDate dateWithTimeIntervalSince1970:[livePrestartTimestamp doubleValue] / 1000.0]];
    [self.liveNotStartedTextLabel.subviews enumerateObjectsUsingBlock:^(__kindof UIView * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
        UILabel* label = (UILabel*)obj;
        if (!label.text) {
            label.text = [NSString stringWithFormat:@"将于%@开播", currentTime];
        }
    }];
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.infoView];
        [self bringSubviewToFront:self.landscapeButton];
        self.backgroundColor = [UIColor blackColor];
        _livePushStopped = YES;
    }
    return self;
}

- (void)updateLivePV:(int32_t)pv {
    self.infoView.pvLabel.text = [NSString stringWithFormat:@"%d 观看", pv];
}

- (void)rotateToLandscape:(BOOL)rotate {
    if (rotate) {
        _landscapeButton.hidden = YES;
        
        [_infoView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.mas_top).with.offset(64);
            make.left.equalTo(self).with.offset(60);
            make.size.mas_equalTo(CGSizeMake(130, 42));
        }];
    } else {
        _landscapeButton.hidden = NO;
        [_infoView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.mas_top).with.offset(13);
            make.right.equalTo(self.mas_right).with.offset(-16);
            make.size.mas_equalTo(CGSizeMake(130, 42));
        }];
    }
    
    [self layoutIfNeeded];
}

- (void)showLiveNotStartedViews:(BOOL)show; {
    self.liveNotStartedTextLabel.hidden = !show;
}

- (void)showLivePlayLoadingViews:(BOOL)show; {
    self.loadingView.hidden = !show;
    self.loadingTextLabel.hidden = !show;
}

- (void)showLivePlayLoadFailedViews:(BOOL)show {
    self.loadCompletedTextLabel.hidden = !show;
    self.reloadButton.hidden = !show;
}

- (void)showLiveEndedViews:(BOOL)show {
    self.liveEndedTextLabel.hidden = !show;
}

- (void)showLivePushStoppedViews:(BOOL)show {
    self.livePushStoppedBackgroundView.hidden = !show;
}

- (void)setVideoPlayStatus:(MELVideoPlayStatus)videoPlayStatus {
    _videoPlayStatus = videoPlayStatus;
    switch (videoPlayStatus) {
        case MELVideoPlayStatusLiveNotStarted: {
            [self showLivePlayLoadingViews:NO];
            [self showLivePlayLoadFailedViews:NO];
            [self showLiveNotStartedViews:YES];
        }
            break;
            
        case MELVideoPlayStatusLiveStarted: {
            [self showLivePlayLoadFailedViews:NO];
            [self showLiveNotStartedViews:NO];
            [self showLivePlayLoadingViews:YES];
        }
            break;
        case MELVideoPlayStatusLoadingStart: {
            if (_liveEnded) {
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:NO];
                [self showLivePlayLoadFailedViews:NO];
                [self showLivePushStoppedViews:NO];
                [self showLiveEndedViews:YES];
            } else {
                [self showLivePlayLoadFailedViews:NO];
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:YES];
            }
        }
            break;
        case MELVideoPlayStatusLoadingEnd: {
            [self showLivePlayLoadFailedViews:NO];
            [self showLiveNotStartedViews:NO];
            [self showLivePlayLoadingViews:NO];
        }
            break;
        case MELVideoPlayStatusLoadFailed: {
            [self showLiveNotStartedViews:NO];
            [self showLivePlayLoadingViews:NO];
            [self showLivePlayLoadFailedViews:YES];
        }
            break;
        case MELVideoPlayStatusPlayStarted: {
            _livePushStopped = NO;
            [self showLiveNotStartedViews:NO];
            [self showLivePlayLoadingViews:NO];
            [self showLivePlayLoadFailedViews:NO];
        }
            break;
            
        case MELVideoPlayStatusPushStopped: {
            _livePushStopped = YES;
            if (_liveEnded) {
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:NO];
                [self showLivePlayLoadFailedViews:NO];
                [self showLiveEndedViews:YES];
            } else {
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:NO];
                [self showLivePlayLoadFailedViews:NO];
                [self showLiveEndedViews:NO];
                [self showLivePushStoppedViews:YES];
            }
        }
            break;
            
        case MELVideoPlayStatusPushRecovered: {
            _livePushStopped = NO;
            [self showLiveNotStartedViews:NO];
            [self showLivePushStoppedViews:NO];
            [self showLivePlayLoadFailedViews:NO];
            [self showLivePlayLoadingViews:NO];
        }
            break;
            
        case MELVideoPlayStatusLiveEnded: {
            _liveEnded = YES;
            if (_livePushStopped) {
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:NO];
                [self showLivePlayLoadFailedViews:NO];
                [self showLivePushStoppedViews:NO];
                [self showLiveEndedViews:YES];
            }
        }
            break;
        case MELVideoPlayStatusPlayErrored: {
            if (_liveEnded) {
                [self showLiveNotStartedViews:NO];
                [self showLivePlayLoadingViews:NO];
                [self showLivePlayLoadFailedViews:NO];
                [self showLivePushStoppedViews:NO];
                [self showLiveEndedViews:YES];
            }
        }
            break;
            
        default:
            break;
    }
}

- (void)onLandscapeButtonClicked {
    self.onLandscape();
}

- (void)onReloadVideoButtonClicked {
    [self showLivePlayLoadFailedViews:NO];
    [self showLiveNotStartedViews:NO];
    [self showLivePlayLoadingViews:YES];
    
    self.onReload();
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
