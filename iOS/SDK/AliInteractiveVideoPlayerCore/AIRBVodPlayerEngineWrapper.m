//
//  AIRBVodPlayerEngineWrapper.m
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/7/6.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBVodPlayerEngineWrapper.h"
#import <AliyunPlayer/AliPlayer.h>

#define LOG(...) NSLog(__VA_ARGS__)

NSString *const AIRBVodPlayerEngineLog = @"AIRBVodPlayerEngineLog";

@interface AIRBVodPlayerEngineWrapper() <AIRBPlayerControlViewDelegate, AVPDelegate>
@property (strong, nonatomic) AliPlayer* player;
@property (copy, nonatomic) NSString* mediaURL;
@property (strong, nonatomic) AVPUrlSource* source;
@property (assign, nonatomic) AIRBPlayerStatus vodPlayerStatus;
@property (assign, nonatomic) int64_t currentPosition;
@property (assign, nonatomic) int64_t bufferedPosition;
@property (assign, nonatomic) BOOL soughtToTimeAfterCompletion;     // 在播放完成之后是否进行了进度跳转
@end

@implementation AIRBVodPlayerEngineWrapper
#pragma mark - Properties
- (void) setVolume:(float)volume {
    self.player.volume = volume;
}

- (float) volume {
    return self.player.volume;
}

- (void) setRate:(float)rate {
    self.player.rate = rate;
}

- (float) rate {
    return self.player.rate;
}

- (void) setProgressSlider:(UISlider *)progressSlider {
    self.playerControlView.progressSlider = progressSlider;
}

- (UISlider *) progressSlider {
    return self.playerControlView.progressSlider;
}

- (UIView*) view {
    if (!_view) {
        _view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _view.backgroundColor = [UIColor clearColor];
    }
    return _view;
}

- (UIView*) playerControlView {
    if (!_playerControlView) {
        _playerControlView = [[AIRBPlayerControlView alloc] initWithFrame:self.view.bounds options:nil];
    }
    return _playerControlView;
}

- (BOOL)tapGestureRecognizerEnabled {
    return self.playerControlView.tapGestureRecognizerEnabled;
}

- (void)setTapGestureRecognizerEnabled:(BOOL)tapGestureRecognizerEnabled {
    self.playerControlView.tapGestureRecognizerEnabled = tapGestureRecognizerEnabled;
}

- (BOOL)twiceTapGestureRecognizerEnabled {
    return self.playerControlView.twiceTapGestureRecognizerEnabled;
}

- (void)setTwiceTapGestureRecognizerEnabled:(BOOL)twiceTapGestureRecognizerEnabled {
    self.playerControlView.twiceTapGestureRecognizerEnabled = twiceTapGestureRecognizerEnabled;
}

- (BOOL)showControlBar {
    return self.playerControlView.showControlBar;
}

- (void)setShowControlBar:(BOOL)showControlBar {
    self.playerControlView.showControlBar = showControlBar;
}

- (void) setContentMode:(int8_t)contentMode {
    _contentMode = contentMode;
    if (_player) {
        self.player.scalingMode = contentMode;
    }
}

- (BOOL) autoPlay {
    return self.player.autoPlay;
}

- (void) setAutoPlay:(BOOL)autoPlay {
    self.player.autoPlay = autoPlay;
}

#pragma mark -

+ (instancetype) createPlayer {
    AIRBVodPlayerEngineWrapper* instance = [[AIRBVodPlayerEngineWrapper alloc] init];
    LOG(@"AIRBVodPlayerEngineWrapper::create(%p)", instance);
    return instance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _contentMode = AVP_SCALINGMODE_SCALEASPECTFIT;
        
        __weak typeof(self) weakSelf = self;
        [AliPlayer setLogCallbackInfo:LOG_LEVEL_ERROR callbackBlock:^(AVPLogLevel logLevel, NSString *strLog) {
            LOG(@"AIRBVodPlayerEngineWrapper(%p)::AliPlayer::%@", weakSelf, strLog);
            [[NSNotificationCenter defaultCenter] postNotificationName:AIRBVodPlayerEngineLog object:nil userInfo:@{
                @"log" : [NSString stringWithFormat:@"AIRBVodPlayerEngineWrapper(%p)::AliPlayer::%@", weakSelf, strLog]
            }];
        }];
    }
    return self;
}

- (void) dealloc {
    LOG(@"AIRBVodPlayerEngineWrapper::AIRBVodPlayerEngineWrapper(%p) dealloc", self);
    if ([[NSThread currentThread] isMainThread]) {
        _player = nil;
    } else {
        dispatch_sync(dispatch_get_main_queue(), ^{ //注意这里是同步调用
            _player = nil;
        });
    }
}

- (void) prepareWithMediaURL:(NSString*)url {
    LOG(@"AIRBVodPlayerEngineWrapper::prepareWithMediaURL(%@)", url);
    self.mediaURL = url;
    self.source = [[AVPUrlSource alloc] urlWithString:self.mediaURL];
    
    if ([[NSThread currentThread] isMainThread]) {
        [self createPlayerCore];
        [self.player setUrlSource:self.source];
        [self.player prepare];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            [self createPlayerCore];
            [self.player setUrlSource:self.source];
            [self.player prepare];
        });
    }
}

- (void) pause {
    LOG(@"AIRBVodPlayerEngineWrapper::pause");
    [self.player pause];
}

- (void) play {
    LOG(@"AIRBVodPlayerEngineWrapper::play");
    [self.player start];
}

- (void) stop {
    LOG(@"AIRBVodPlayerEngineWrapper::stop");
    [self.player stop];
}

- (void) seekToTime:(int64_t)time {
    LOG(@"AIRBVodPlayerEngineWrapper::seekToTime(%lld)", time);
    [self.player seekToTime:(int64_t)time seekMode:AVP_SEEKMODE_ACCURATE];
    if (self.vodPlayerStatus == AIRBPlayerStatusCompletion){
        self.soughtToTimeAfterCompletion = YES;
    }
}

//- (int64_t) getDuration{
//    return self.player.duration;
//}

//- (int64_t) getCurrentPosition{
//    return self.player.currentPosition;
//}

- (void) snapshot {
    LOG(@"AIRBVodPlayerEngineWrapper::snapshot");
    [self.player snapShot];
}

- (void)toggleMuted {
    LOG(@"AIRBVodPlayerEngineWrapper::toggleMuted");
    [self.player setMuted:![self.player isMuted]];
}

- (void) createPlayerCore {
    if (!_player) {
        _player = [[AliPlayer alloc] init];
        _player.playerView = self.view;
        _player.autoPlay = YES;
        _player.delegate = self;
        _player.scalingMode = self.contentMode;
        AVPConfig * config = [_player getConfig];
//        config.networkTimeout = 5000;
//        config.networkRetryCount = 3;
        config.maxBufferDuration = 30000;
        [_player setConfig:config];
//        [_player setUrlSource:self.source];
        
//        [_player addObserver:self forKeyPath:@"duration" options:(NSKeyValueObservingOptionNew) context:nil];
//        [_player addObserver:self forKeyPath:@"currentPosition" options:(NSKeyValueObservingOptionNew) context:nil];
//        [_player addObserver:self forKeyPath:@"bufferedPosition" options:(NSKeyValueObservingOptionNew) context:nil];
        
//        AVPCacheConfig * cacheConfig = [[AVPCacheConfig alloc] init];
//        cacheConfig.maxSizeMB = 100;
//        cacheConfig.enable = YES;
//        [_player setCacheConfig:cacheConfig];
        
        self.playerControlView.delegate = self;
    }
}

#pragma mark - AVPDelegate

- (void) onPlayerEvent:(AliPlayer *)player eventType:(AVPEventType)eventType {
    LOG(@"AIRBVodPlayerEngineWrapper::onPlayerEvent(%lu)", (unsigned long)eventType);
    if (self.player == player) {
        switch (eventType) {
            case AVPEventPrepareDone:{
//                LOGD("AVPEventPrepareDone");
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEnginePrepareDone:)]) {
                    [self.delegate onVodPlayerEnginePrepareDone:@{
                        @"width":[NSNumber numberWithInt:self.player.width],
                        @"height":[NSNumber numberWithInt:self.player.height],
                        @"duration":[NSNumber numberWithLongLong:self.player.duration]
                    }];
                }
                
                [self.playerControlView refreshPlayerProgressWithTimeInfo:@{
                    @"duration":[NSNumber numberWithLongLong:self.player.duration],
                }];
                
//                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineDurationUpdated:)]) {
//                    [self.delegate onVodPlayerEngineDurationUpdated:self.player.duration];
//                }
            }
                break;
            case AVPEventAutoPlayStart:
//                LOGD("AVPEventAutoPlayStart");
                // 自动播放开始事件
                break;
            case AVPEventFirstRenderedStart:
//                LOGD("AVPEventFirstRenderedStart");
//                self.vodPlayerStatus = AIRBPlayerStatusPlaying;
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineFirstRenderedStart:)]) {
                    NSString* connectionInfo = [self.player getPropertyString:AVP_KEY_CONNECT_INFO];
                    NSData* dicData = [connectionInfo dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary* connectionInfoDic = [NSJSONSerialization JSONObjectWithData:dicData options:NSJSONReadingMutableContainers error:nil];

                    [self.delegate onVodPlayerEngineFirstRenderedStart:@{
                        @"width" : @(self.player.width),
                        @"height" : @(self.player.height),
                        @"cdnIP" : [connectionInfoDic objectForKey:@"ip"] ?: @""
                    }];
                }
                break;
            case AVPEventCompletion:
                // 播放完成
//                self.vodPlayerStatus = AIRBPlayerStatusStopped;
                [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusStopped];
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineCompletion)]) {
                    [self.delegate onVodPlayerEngineCompletion];
                }
                break;
            case AVPEventLoadingStart:
//                LOGD("AVPEventLoadingStart");
//                self.vodPlayerStatus = AIRBPlayerStatusStalled;
                [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusStalled];
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineLoadingStart)]) {
                    [self.delegate onVodPlayerEngineLoadingStart];
                }
                break;
            case AVPEventLoadingEnd:
//                LOGD("AVPEventLoadingEnd");
//                self.vodPlayerStatus = AIRBPlayerStatusPlaying;
                if (self.vodPlayerStatus == AIRBPlayerStatusPlaying){
                    [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusPlaying];
                } else{
                    [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusPaused];
                }
                
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineLoadingEnd)]) {
                    [self.delegate onVodPlayerEngineLoadingEnd];
                }
                break;
            case AVPEventSeekEnd:{
                if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineSeekEnd)]) {
                    [self.delegate onVodPlayerEngineSeekEnd];
                }
            }
                break;
            default:
                break;
        }
    }
}

- (void)onCurrentPositionUpdate:(AliPlayer*)player position:(int64_t)position{
//    LOG(@"AIRBVodPlayerEngineWrapper::onCurrentPositionUpdate(%lld)", position);
    self.currentPosition = position;
    [self.playerControlView refreshPlayerProgressWithTimeInfo:@{
        @"currentTime":[NSNumber numberWithLongLong:position],
    }];
    if ([self.delegate respondsToSelector:@selector(onVodPlayerEnginePositionUpdated:)]) {
        [self.delegate onVodPlayerEnginePositionUpdated:@{
            @"currentPosition":[NSNumber numberWithLongLong:self.currentPosition],
            @"bufferedPosition":[NSNumber numberWithLongLong:self.bufferedPosition]
        }];
    }
}

- (void)onCurrentUtcTimeUpdate:(AliPlayer *)player time:(int64_t)time{
//    LOG(@"AIRBVodPlayerEngineWrapper::onCurrentUtcTimeUpdate(%lld)", time);
    if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineCurrentUtcTimeUpdated:)]) {
        [self.delegate onVodPlayerEngineCurrentUtcTimeUpdated:time];
    }
}

- (void)onBufferedPositionUpdate:(AliPlayer*)player position:(int64_t)position{
//    LOG(@"AIRBVodPlayerEngineWrapper::onBufferedPositionUpdate(%lld)", position);
    self.bufferedPosition = position;
//    if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineBufferedPositionUpdated:)]) {
//        [self.delegate onVodPlayerEngineBufferedPositionUpdated:position];
//    }
}

- (void)onPlayerStatusChanged:(AliPlayer*)player oldStatus:(AVPStatus)oldStatus newStatus:(AVPStatus)newStatus{
    LOG(@"AIRBVodPlayerEngineWrapper::onPlayerStatusChanged(%lu,%lu)", (unsigned long)oldStatus, (unsigned long)newStatus);
    switch (newStatus) {
        case AVPStatusPrepared:
            if (self.player.isAutoPlay) {
                self.vodPlayerStatus = AIRBPlayerStatusPlaying;
            }
            break;
        case AVPStatusStarted: {
            self.vodPlayerStatus = AIRBPlayerStatusPlaying;
            self.soughtToTimeAfterCompletion = NO;
            [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusPlaying];
            if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineStatusChangedToPlaying)]) {
                [self.delegate onVodPlayerEngineStatusChangedToPlaying];
            }
        }
            break;
        case AVPStatusPaused: {
            self.vodPlayerStatus = AIRBPlayerStatusPaused;
            [self.playerControlView refreshPlayButtonStatus:AIRBPlayerStatusPaused];
            if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineStatusChangedToPaused)]) {
                [self.delegate onVodPlayerEngineStatusChangedToPaused];
            }
        }
            break;
        case AVPStatusStopped:
            self.vodPlayerStatus = AIRBPlayerStatusStopped;
            break;
        case AVPStatusCompletion:
            self.vodPlayerStatus = AIRBPlayerStatusCompletion;
            break;
        default:
            break;
    }
}

- (void) onError:(AliPlayer *)player errorModel:(AVPErrorModel *)errorModel {
    LOG(@"AIRBVodPlayerEngineWrapper::onError(0x%lx,%@)", (unsigned long)errorModel.code, errorModel.message);
    if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineErrorCode:errorMessage:)]) {
        [self.delegate onVodPlayerEngineErrorCode:errorModel.code errorMessage:errorModel.message];
    }
}

- (void) onCaptureScreen:(AliPlayer *)player image:(UIImage *)image {
    LOG(@"AIRBVodPlayerEngineWrapper::onCaptureScreen");
    if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineImageSnapshot:)]) {
        [self.delegate onVodPlayerEngineImageSnapshot:image];
    }
}

- (void) onVideoRendered:(AliPlayer *)player timeMs:(int64_t)timeMs pts:(int64_t)pts {
//    LOG(@"AIRBLivePlayerEngineWrapper::onVideoRendered(time:%lld, pts:%lld)", timeMs, pts);
    if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineVideoRendered)]) {
        [self.delegate onVodPlayerEngineVideoRendered];
    }
}

//#pragma mark - ObserveEvent
//
//-(void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context{
//    //观察者观察变化,chang就会捕获新值.
//    if ([keyPath isEqualToString:@"currentPosition"]){
//        [self.playerControlView refreshPlayerProgressWithTimeInfo:@{
//            @"currentTime":[change valueForKey:@"new"],
//        }];
//        if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineCurrentPositionUpdated:)]) {
//            [self.delegate onVodPlayerEngineCurrentPositionUpdated:[change valueForKey:@"new"]];
//        }
//    } else if ([keyPath isEqualToString:@"duration"]){
//        [self.playerControlView refreshPlayerProgressWithTimeInfo:@{
//            @"duration":[change valueForKey:@"new"],
//        }];
//        if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineDurationUpdated:)]) {
//            [self.delegate onVodPlayerEngineDurationUpdated:[change valueForKey:@"new"]];
//        }
//    } else if ([keyPath isEqualToString:@"bufferedPosition"]){
//        if ([self.delegate respondsToSelector:@selector(onVodPlayerEngineBufferedPositionUpdated:)]) {
//            [self.delegate onVodPlayerEngineBufferedPositionUpdated:[change valueForKey:@"new"]];
//        }
//    }
//}

#pragma -mark AIRBPlayerControlViewDelegate
- (void)playButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView{
    LOG(@"AIRBVodPlayerEngineWrapper::playButtonDidClickedInControlView");
    if (self.vodPlayerStatus == AIRBPlayerStatusCompletion && !self.soughtToTimeAfterCompletion){
        [self seekToTime:0];
    }
    [self play];
}

- (void)pauseButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView{
    LOG(@"AIRBVodPlayerEngineWrapper::pauseButtonDidClickedInControlView");
    [self pause];
}
//- (void)fullScreenButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
//- (void)miniScreenButtonDidClickedInControlView:(AIRBPlayerControlView *)playerControlView;
//- (void)progressSliderBeginToDragInControlView:(AIRBPlayerControlView *)playerControlView;
////- (void)progressSliderCancelDragInControlView:(AIRBPlayerControlView *)playerControlView;
- (void)progressSliderDidDragToTime:(NSTimeInterval)time inControlView:(AIRBPlayerControlView *)playerControlView{
    LOG(@"AIRBVodPlayerEngineWrapper::progressSliderDidDragToTime");
    [self seekToTime:(time * 1000)];
}
- (void)controlViewDidTapped:(AIRBPlayerControlView *)playerControlView{
    LOG(@"AIRBVodPlayerEngineWrapper::controlViewDidTapped");
    self.playerControlView.showControlBar = !self.playerControlView.showControlBar;
}
- (void)controlViewDidTwiceTapped:(AIRBPlayerControlView *)playerControlView{
    LOG(@"AIRBVodPlayerEngineWrapper::controlViewDidTwiceTapped");
    if (self.vodPlayerStatus == AIRBPlayerStatusPlaying){
        [self pause];
    } else if (self.vodPlayerStatus == AIRBPlayerStatusPaused) {
        [self play];
    } else if (self.vodPlayerStatus == AIRBPlayerStatusCompletion) {
        if (!self.soughtToTimeAfterCompletion) {
            [self seekToTime:0];
        }
        [self play];
    }
}
//- (void)playRateButtonDidClickedInControlView:(float)rate;

@end
