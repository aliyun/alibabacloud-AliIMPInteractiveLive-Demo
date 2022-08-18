//
//  AIRBLivePlayer.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/12.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLivePlayer.h"

#import <objc/message.h>

#import <vpaassdk/live/VPLIVELiveRpcInterface.h>
#import <vpaassdk/live/VPLIVELiveModule.h>

#import "AliInteractiveRoomLogger.h"
#import "AIRBRoomChannel.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"
     

@interface AIRBLivePlayer()

@property (strong, nonatomic) id playerEngine;
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* mediaURL;
@property (copy, nonatomic) NSString* backupMediaURL;

@property (strong, nonatomic) VPLIVELiveModule* liveModule;
@property (strong, nonatomic) VPLIVELiveRpcInterface* liveRpcInterface;

@property (assign, nonatomic) BOOL shouldSwitchToNonNormalDelayWhenError;
@property (assign, nonatomic) BOOL startLiveTimingReported;
@end

@implementation AIRBLivePlayer

@synthesize delegate = _delegate;
@synthesize playerView = _playerView;
@synthesize lowDelay = _lowDelay;
@synthesize contentMode = _contentMode;

- (id)playerEngine {
    if (!_playerEngine) {
        Class playerEngineClass = NSClassFromString(@"AIRBLivePlayerEngineWrapper");
        if (playerEngineClass) {
            if (self.mediaURL) {
                _playerEngine = ((id (*)(id, SEL))objc_msgSend)(playerEngineClass, NSSelectorFromString(@"createPlayer"));
                if (_playerEngine) {
                    ((void (*)(id, SEL, id))objc_msgSend)(_playerEngine, NSSelectorFromString(@"setPlayerView:"), self.playerView);
                    ((void (*)(id, SEL, id))objc_msgSend)(_playerEngine, NSSelectorFromString(@"setDelegate:"), self);
                }
            }
        }
    }
    return _playerEngine;
}

- (void) setPlayerVolume:(float)volume {
    LOGD("AIRBLivePlayer::setPlayerVolume(%f)", volume);
    ((void (*)(id, SEL, float))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setVolume:"), volume);
}

- (float) playerVolume {
    return ((float (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"volume"));
}

- (UIView*) playerView {
    if (!_playerView) {
        if ([[NSThread currentThread] isMainThread]) {
            _playerView = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
            _playerView.backgroundColor = [UIColor clearColor];
            _playerView.autoresizingMask = YES;
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                _playerView = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
                _playerView.backgroundColor = [UIColor clearColor];
            });
        }
    }
    return _playerView;
}

- (void)setContentMode:(AIRBVideoViewContentMode)contentMode {
    LOGD("AIRBLivePlayer::setContentMode(%d)", contentMode);
    _contentMode = contentMode;
    if (_playerEngine) {
        ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setContentMode:"), contentMode);
    }
}

#pragma mark -Actions

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
        _liveModule = [VPLIVELiveModule getModule:userID];
        _liveRpcInterface = [_liveModule getRpcInterface];
        _shouldSwitchToNonNormalDelayWhenError = NO;
        _startLiveTimingReported = NO;
        _lowDelay = YES;
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onLogFromLiveCore:) name:@"AIRBLivePlayerEngineLog" object:nil];
    }
    return self;
}

- (void)dealloc {
    LOGD("AIRBLivePlayer::AIRBLivePlayer(%p) dealloc", self);
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) updateLiveID:(NSString *)liveID {
    self.liveID = liveID;
}

- (void)notifyLivePushStart:(BOOL)started {
    if (started) {
        [self restartPlayWhenLivePushStarted];
    }
}

- (void) restartPlayWhenLivePushStarted {
    [self stop];
    [self getLivePlayerDetailAndStartPlayerIfPossible];
}

- (void) getLivePlayerDetailAndStartPlayerIfPossible {
    LOGD("AIRBLivePlayer::getLivePlayerDetailAndStartPlayerIfPossible");
    [self.liveRpcInterface getLiveDetailWithBlock:[VPLIVEGetLiveDetailReq VPLIVEGetLiveDetailReqWithUuid:self.liveID] onSuccess:^(VPLIVEGetLiveDetailRsp * _Nonnull rsp) {
        if (rsp.live.status == 1) { //正在直播中
            NSString* streamType = @"rts";
            if (self.lowDelay && rsp.live.artcInfo.artcUrl && rsp.live.artcInfo.artcUrl.length > 0) {
                self.mediaURL = rsp.live.artcInfo.artcUrl;
                ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setLowDelay:"), YES);
                self.backupMediaURL = rsp.live.liveUrl;
                self.shouldSwitchToNonNormalDelayWhenError = YES;
            } else {
                streamType = @"flv";
                self.mediaURL = rsp.live.liveUrl;
            }
            ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setContentMode:"), self.contentMode);
            ((void (*)(id, SEL, id))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"startWithMediaURL:"), self.mediaURL);
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLivePlay info:@{
                @"type" : streamType,
                @"url" : self.mediaURL ?: @"",
                @"error_code" : @"",
                @"error_msg" : @""
            }];
            
            [AIRBMonitorHubManager sharedInstance].videoPlayerModel.url = self.mediaURL;
            [AIRBMonitorHubManager sharedInstance].videoPlayerModel.protocol = streamType;
            
        } else if (rsp.live.status == 0) {
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
                [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventLiveNotStarted info:@{}];
            }
        } else if (rsp.live.status == 2) {
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
                [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventLiveEnded info:@{}];
            }
        }
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerErrorWithCode:message:)]) {
            [self.delegate onAIRBLivePlayerErrorWithCode:AIRBLivePlayerFailedToGetDetail message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) start {
    LOGD("AIRBLivePlayer::start");
    if (self.liveID.length > 0) {
        [self getLivePlayerDetailAndStartPlayerIfPossible];
    } else {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
            [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventLiveNotExist info:@{}];
        }
    }
}

- (void) pause {
    LOGD("AIRBLivePlayer::pause");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"pause"));
    [self reportEndTiming];
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayPause info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
}

- (void) resume {
    LOGD("AIRBLivePlayer::resume");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"resume"));
    [self reportStartTiming];
}

- (void) stop {
    LOGD("AIRBLivePlayer::stop");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"stop"));
    
    [self reportEndTiming];
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLiveStop info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) refresh {
    LOGD("AIRBLivePlayer::refresh");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"refresh"));
}

- (void)refreshWithMediaURL:(NSString *)url {
    LOGD("AIRBLivePlayer::refreshWithMediaURL(%@)", url);
    if (url && ![url isEqualToString:self.mediaURL]) {
        [self stop];
        self.mediaURL = url;
        ((void (*)(id, SEL, id))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"startWithMediaURL:"), self.mediaURL);
        
        NSString* streamType = @"none";
        if ([self.mediaURL hasPrefix:@"artc"]) {
            streamType = @"rts";
        } else if ([self.mediaURL containsString:@".mp4"]) {
            streamType = @"mp4";
        } else if ([self.mediaURL containsString:@".flv"]) {
            streamType = @"flv";
        } else if ([self.mediaURL containsString:@".m3u8"]) {
            streamType = @"hls";
        }
        
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLivePlay info:@{
            @"type" : streamType,
            @"url" : self.mediaURL ?: @"",
            @"error_code" : @"",
            @"error_msg" : @""
        }];
        
        [AIRBMonitorHubManager sharedInstance].videoPlayerModel.url = self.mediaURL;
    }
}

- (void) snapshotAsync {
    LOGD("AIRBLivePlayer::snapshotAsync");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"snapshot"));
}

- (void) toggleMuted {
    LOGD("AIRBLivePlayer::toggleMuted");
    ((void (*)(id, SEL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"toggleMuted"));
}

- (void) mute:(BOOL)mute {
    LOGD("AIRBLivePlayer::mute:%d", mute);
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"mute:"), mute);
}

- (void) handleLowDelayLivePlayingError {
    LOGD("AIRBLivePlayer::handleLowDelayLivePlayingError");
    self.shouldSwitchToNonNormalDelayWhenError = NO;
    self.lowDelay = NO;
    [self stop];
    self.playerEngine = nil;
    ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setContentMode:"), self.contentMode);
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"setLowDelay:"), NO);
    ((void (*)(id, SEL, id))objc_msgSend)(self.playerEngine, NSSelectorFromString(@"startWithMediaURL:"), self.backupMediaURL);
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayLivePlay info:@{
        @"type" : @"flv",
        @"url" : self.backupMediaURL ?: @"",
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.url = self.backupMediaURL;
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.protocol = @"flv";
}

- (void) reportStartTiming {
        VPLIVEStartLiveTimingReq* req = [VPLIVEStartLiveTimingReq VPLIVEStartLiveTimingReqWithUuid:self.liveID];
        [self.liveRpcInterface startLiveTimingWithBlock:req onSuccess:^(VPLIVEStartLiveTimingRsp * _Nonnull rsp) {
            self.startLiveTimingReported = rsp.success;
            LOGD("AIRBLivePlayer::startLiveTimingWithBlock(%d)", rsp.success);
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBLivePlayer::startLiveTimingWithBlock error(%@)", ERR_MSG_FROM_DPSERROR(error));
        }];
}

- (void) reportEndTiming {
    if (self.startLiveTimingReported) {
        VPLIVEEndLiveTimingReq* req = [VPLIVEEndLiveTimingReq VPLIVEEndLiveTimingReqWithUuid:self.liveID];
        [self.liveRpcInterface endLiveTimingWithBlock:req onSuccess:^(VPLIVEEndLiveTimingRsp * _Nonnull rsp) {
            self.startLiveTimingReported = NO;
            LOGD("AIRBLivePlayer::endLiveTimingWithBlock(%d)", rsp.success);
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBLivePlayer::endLiveTimingWithBlock error(%@)", ERR_MSG_FROM_DPSERROR(error));
        }];
    }
}

#pragma mark -NSNotification

- (void) onLogFromLiveCore:(NSNotification *)notification {
    if ([notification.userInfo objectForKey:@"log"]) {
        LOGE("%@", [notification.userInfo objectForKey:@"log"]);
    }
}

#pragma mark -Delegates

- (void) onLivePlayerEnginePrepareDone {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventPrepared info:@{}];
    }
}

- (void) onLivePlayerEngineFirstRenderedStartWithInfo:(NSDictionary*)info {
    LOGD("AIRBLivePlayer::onLivePlayerEngineFirstRenderedStart");
    
    [self reportStartTiming];
        
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventStarted info:info];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayFirstFrame info:@{
        @"cdn_ip" : [info objectForKey:@"cdnIP"] ?: @"",
        @"hw_decode" : @"1",
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.contentID = self.liveID;
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusRunning;
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.videoWidth = [[info objectForKey:@"width"] intValue];
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.videoHeight = [[info objectForKey:@"height"] intValue];
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.rtsTraceID = [info objectForKey:@"traceID"];
}

- (void) onLivePlayerEngineLoadingStart {
    LOGD("AIRBLivePlayer::onLivePlayerEngineLoadingStart");
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventStartLoading info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayNetLoadingBegin info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) onLivePlayerEngineLoadingEnd {
    LOGD("AIRBLivePlayer::onLivePlayerEngineLoadingEnd");
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventEndLoading info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayNetLoadingEnd info:@{
        @"error_code" : @"",
        @"error_msg" : @""
    }];
    
    [AIRBMonitorHubManager sharedInstance].videoPlayerModel.status = AIRBMonitorHubComponentStatusRunning;
}

- (void) onLivePlayerEngineErrorCode:(unsigned long)code errorMessage:(NSString*)errorMessage {
    LOGE("AIRBLivePlayer::onLivePlayerEngineError(%@)", errorMessage);
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPlayErrorEvent info:@{
        @"error_code" : [NSString stringWithFormat:@"%lu",code],
        @"error_msg" : errorMessage
    }];
    
//    if (self.shouldSwitchToNonNormalDelayWhenError && code == 0x20050001) {
//        LOGE("AIRBLivePlayer::onLivePlayerEngineError(%@), switch to FLV.", errorMessage);
//        [self handleLowDelayLivePlayingError];
//        return;
//    }
    
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerErrorWithCode:message:)]) {
        [self.delegate onAIRBLivePlayerErrorWithCode:AIRBLivePlayerFailedToPlayWithFatalError message:errorMessage];
    }
}

- (void) onLivePlayerEngineImageSnapshot:(UIImage*)image {
    LOGD("AIRBLivePlayer::onLivePlayerEngineImageSnapshot(%p)", image);
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventImageCaptured info:@{
            @"image" : image
        }];
    }
}

- (void) onLivePlayerEngineVideoRendered {
//    LOGD("AIRBLivePlayer::onLivePlayerEngineVideoRendered");
    dispatch_async(dispatch_get_global_queue(0, 0), ^{
        [AIRBMonitorHubManager sharedInstance].videoPlayerModel.renderedVideoFrameCount++;
    });
}

- (void) onLivePlayerEngineVideoSizeChanged:(int)width height:(int)height {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventVideoSizeChanged info:@{
            @"width" : @(width),
            @"height" : @(height)
        }];
    }
}

- (void) onLivePlayerEngineDownloadBitrate:(int32_t)bitrate {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePlayerEvent:info:)]) {
        [self.delegate onAIRBLivePlayerEvent:AIRBLivePlayerEventDownloadBitrateUpdated info:@{
            @"bitrate" : @(bitrate)
        }];
    }
}

@end
