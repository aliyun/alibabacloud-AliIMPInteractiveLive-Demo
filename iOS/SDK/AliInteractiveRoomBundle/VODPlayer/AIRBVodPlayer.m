//
//  AIRBVodPlayer.m
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/7/6.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBVodPlayer.h"

#import <objc/message.h>

#import "AliInteractiveRoomLogger.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"

@interface AIRBVodPlayer()

@property (strong, nonatomic) id playerEngine;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* mediaURL;
@property (copy, nonatomic) NSString* contentID;

//直播回放相关
//@property (copy, nonatomic) NSString* liveID;
//@property (strong, nonatomic) LiveModule* liveModule;
//@property (strong, nonatomic) LiveManager* liveManager;
//@property (assign, nonatomic) BOOL livePlaybackStatistic;
//@property (assign, nonatomic) BOOL livePlaybackStartTimingReported;
//@property (strong, nonatomic) NSTimer* reportLivePlaybackContinueTimingTimer;
//@property (strong, nonatomic) NSLock* timerLock;

@end

@implementation AIRBVodPlayer

@synthesize delegate = _delegate;
@synthesize playerView = _playerView;
@synthesize contentMode = _contentMode;
@synthesize playerControlView = _playerControlView;

- (id)playerEngine {
    if (!_playerEngine) {
        Class playerEngineClass = NSClassFromString(@"AIRBVodPlayerEngineWrapper");
        if (playerEngineClass) {
//            if (self.mediaURL) {
                _playerEngine = ((id (*)(id, SEL))objc_msgSend)(playerEngineClass, NSSelectorFromString(@"createPlayer"));
                if (_playerEngine) {
                    ((void (*)(id, SEL, id))objc_msgSend)(_playerEngine, NSSelectorFromString(@"setDelegate:"), self);
                }
//            }
        }
    }
    return _playerEngine;
}

- (void) setPlayerVolume:(float)volume {
    ((void (*)(id, SEL, float))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setVolume:"), volume);
}

- (float) playerVolume {
    return ((float (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"volume"));
}

- (void) setPlayerRate:(float)playerRate {
    LOGD("AIRBVodPlayer::setPlayerRate(%f)", playerRate);
    ((void (*)(id, SEL, float))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setRate:"), playerRate);
}

- (float) playerRate {
    return ((float (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"rate"));
}

- (void) setProgressSliderInControlBar:(UISlider *)progressSliderInControlBar {
    ((void (*)(id, SEL, UISlider *))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setProgressSlider:"), progressSliderInControlBar);
}

- (UISlider *) progressSliderInControlBar {
    return ((UISlider * (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"progressSlider"));
}

- (UIView*) playerView {
    return ((UIView* (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"view"));
}

- (UIView*) playerControlView {
    return ((UIView* (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"playerControlView"));
}

- (BOOL)tapGestureRecognizerEnabled {
    return ((BOOL (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"tapGestureRecognizerEnabled"));
}

- (void)setTapGestureRecognizerEnabled:(BOOL)tapGestureRecognizerEnabled {
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setTapGestureRecognizerEnabled:"), tapGestureRecognizerEnabled);
}

- (BOOL)twiceTapGestureRecognizerEnabled {
    return ((BOOL (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"twiceTapGestureRecognizerEnabled"));
}

- (void)setTwiceTapGestureRecognizerEnabled:(BOOL)twiceTapGestureRecognizerEnabled {
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setTwiceTapGestureRecognizerEnabled:"), twiceTapGestureRecognizerEnabled);
}

- (BOOL)showControlBar {
    return ((BOOL (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"showControlBar"));
}

- (void)setShowControlBar:(BOOL)showControlBar {
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setShowControlBar:"), showControlBar);
}

- (void)setContentMode:(AIRBVideoViewContentMode)contentMode {
    LOGD("AIRBVodPlayer::setContentMode(%d)", contentMode);
    _contentMode = contentMode;
    if (_playerEngine) {
        ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setContentMode:"), contentMode);
    }
}

- (BOOL) autoPlay {
    return ((BOOL (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"autoPlay"));
}

- (void)setAutoPlay:(BOOL)autoPlay {
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setAutoPlay:"), autoPlay);
}

#pragma mark -Actions

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
//        _liveModule = [LiveModule getLiveModule:userID];
//        _liveManager = [_liveModule getLiveManager];
//        _livePlaybackStatistic = NO;
//        _timerLock = [[NSLock alloc] init];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onLogFromLiveCore:) name:@"AIRBVodPlayerEngineLog" object:nil];
    }
    return self;
}

- (void)dealloc {
    LOGD("AIRBVodPlayer::AIRBVodPlayer(%p) dealloc", self);
    if (_playerEngine) {
        _playerEngine = nil;
    }
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) prepareWithMediaURL:(NSString*)mediaURL {
    LOGD("AIRBVodPlayer::prepareWithMediaURL(%@)", mediaURL);
    [self prepareWithMediaURL:mediaURL contentID:nil];
}

- (void) prepareWithMediaURL:(NSString*)mediaURL
                   contentID:(NSString*)contentID {
    LOGD("AIRBVodPlayer::prepareWithMediaURL(%@,%@)", mediaURL, contentID);
    self.mediaURL = mediaURL;
    self.contentID = contentID;
    ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setContentMode:"), self.contentMode);
    ((void (*)(id, SEL, id))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"prepareWithMediaURL:"), self.mediaURL);
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLivePlay info:@{
        @"type" : @"hls",
        @"url" : self.mediaURL ?: @"",
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.url = self.mediaURL;
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.protocol = @"hls";
}

- (void) pause {
    LOGD("AIRBVodPlayer::pause");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"pause"));
        
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayPause info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
}

- (void) play {
    LOGD("AIRBVodPlayer::play");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"play"));
}

- (void) stop {
    LOGD("AIRBVodPlayer::stop");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"stop"));
        
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLiveStop info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) seekToTime:(int64_t)time {
    LOGD("AIRBVodPlayer::seekToTime(%lld)", time);
    ((void (*)(id, SEL, int64_t))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"seekToTime:"), time);
}

- (void) snapshotAsync {
    LOGD("AIRBVodPlayer::snapshotAsync");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"snapshot"));
}

- (void) toggleMuted {
    LOGD("AIRBVodPlayer::toggleMuted");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"toggleMuted"));
}

#pragma mark -NSNotification

- (void) onLogFromLiveCore:(NSNotification *)notification {
    if ([notification.userInfo objectForKey:@"log"]) {
        LOGE("%@", [notification.userInfo objectForKey:@"log"]);
    }
}

#pragma mark -Delegates

- (void) onVodPlayerEnginePrepareDone:(NSDictionary*)info{
    LOGD("AIRBVodPlayer::onVodPlayerEnginePrepareDone");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventPrepareDone info:info];
    }
}

- (void) onVodPlayerEngineFirstRenderedStart:(NSDictionary*)info {
    LOGD("AIRBVodPlayer::onVodPlayerEngineFirstRenderedStart");
        
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventFirstRenderedStart info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayFirstFrame info:@{
        @"cdn_ip" : [info objectForKey:@"cdnIP"] ?: @"",
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.videoWidth = [[info objectForKey:@"width"] intValue];
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.videoHeight = [[info objectForKey:@"height"] intValue];
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.playType = @"vod";
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.contentID = self.contentID;
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusRunning;
}

- (void) onVodPlayerEngineCompletion {
    LOGD("AIRBVodPlayer::onVodPlayerEngineCompletion");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventCompletion info:@{}];
    }
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) onVodPlayerEngineLoadingStart {
    LOGD("AIRBVodPlayer::onVodPlayerEngineLoadingStart");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventStartLoading info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayNetLoadingBegin info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) onVodPlayerEngineLoadingEnd {
    LOGD("AIRBVodPlayer::onVodPlayerEngineLoadingEnd");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventEndLoading info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayNetLoadingEnd info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusRunning;
}

- (void) onVodPlayerEngineStatusChangedToPlaying {
    LOGD("AIRBVodPlayer::onVodPlayerEngineStatusChangedToPlaying");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventStatusChangedToPlaying info:@{}];
    }
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusRunning;
}

- (void) onVodPlayerEngineStatusChangedToPaused {
    LOGD("AIRBVodPlayer::onVodPlayerEngineStatusChangedToPaused");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventStatusChangedToPaused info:@{}];
    }
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) onVodPlayerEngineSeekEnd{
    LOGD("AIRBVodPlayer::onVodPlayerEngineSeekEnd");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventSeekEnd info:@{}];
    }
}

- (void) onVodPlayerEngineErrorCode:(unsigned int)errorCode errorMessage:(NSString*)errorMessage {
    LOGE("AIRBVodPlayer::onVodPlayerEngineError(%@)", errorMessage);
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerErrorWithCode:message:)]) {
        [self.delegate onAIRBVodPlayerErrorWithCode:AIRBVodPlayerFailedToPlayWithFatalError message:errorMessage];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayErrorEvent info:@{
        @"error_code" : [NSString stringWithFormat:@"%u",errorCode],
        @"error_msg" : errorMessage
    }];
}

- (void) onVodPlayerEngineImageSnapshot:(UIImage*)image {
    LOGD("AIRBVodPlayer::onVodPlayerEngineImageSnapshot(%p)", image);
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerImageSnapshot:)]) {
        [self.delegate onAIRBVodPlayerImageSnapshot:image];
    }
}

- (void) onVodPlayerEnginePositionUpdated:(NSDictionary*)info{
//    LOGI("AIRBVodPlayer::onVodPlayerEnginePositionUpdated");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventPositionUpdated info:info];
    }
}

- (void) onVodPlayerEngineCurrentUtcTimeUpdated:(int64_t)currentUtcTime{
//    LOGD("AIRBVodPlayer::onVodPlayerEngineCurrentUtcTimeUpdated");
    if ([self.delegate respondsToSelector:@selector(onAIRBVodPlayerEvent:info:)]) {
        [self.delegate onAIRBVodPlayerEvent:AIRBVodPlayerEventExtensionReceived info:@{
            @"currentUtcTime":[NSNumber numberWithLongLong:currentUtcTime]
        }];
    }
}

- (void) onVodPlayerEngineVideoRendered {
//    LOGD("AIRBVodPlayer::onVodPlayerEngineVideoRendered");
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        [AIRBMonitorHubManager sharedInstance].videoPlayerModel.renderedVideoFrameCount++;
    });
}

@end
