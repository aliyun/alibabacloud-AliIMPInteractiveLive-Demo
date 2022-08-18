//
//  AIRBLivePlayerEngineWrapper.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLivePlayerEngineWrapper.h"
//#import "AliInteractiveRoomLogger.h"
#import <AliyunPlayer/AliPlayer.h>

#define LOG(...) NSLog(__VA_ARGS__)

NSString *const AIRBLivePlayerEngineLog = @"AIRBLivePlayerEngineLog";

@interface AIRBLivePlayerEngineWrapper() <AVPDelegate>
@property (strong, nonatomic) AliPlayer* player;
@property (copy, nonatomic) NSString* mediaURL;
@property (assign, atomic) AVPStatus playerStatus;
@property (copy, nonatomic) NSString* rtsTraceID;
@property (copy, nonatomic) NSString* rtsServerIP;
@property (assign, nonatomic) BOOL isRefreshingPlay;
@end

@implementation AIRBLivePlayerEngineWrapper
#pragma mark - Properties
- (void) setVolume:(float)volume {
    self.player.volume = volume;
}

- (float) volume {
    return self.player.volume;
}

- (void) setContentMode:(int8_t)contentMode {
    _contentMode = contentMode;
    if (_player) {
        _player.scalingMode = contentMode;
    }
}

#pragma mark -

+ (instancetype) createPlayer {
    AIRBLivePlayerEngineWrapper* instance = [[AIRBLivePlayerEngineWrapper alloc] init];
    LOG(@"AIRBLivePlayerEngineWrapper::createPlayer(%p)", instance);
    return instance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _contentMode = AVP_SCALINGMODE_SCALEASPECTFILL;
        _playerStatus = AVPStatusIdle;
        
        __weak typeof(self) weakSelf = self;
        [AliPlayer setLogCallbackInfo:LOG_LEVEL_ERROR callbackBlock:^(AVPLogLevel logLevel, NSString *strLog) {
            LOG(@"AIRBLivePlayerEngineWrapper(%p)::AliPlayer::%@", weakSelf, strLog);
            [[NSNotificationCenter defaultCenter] postNotificationName:AIRBLivePlayerEngineLog object:nil userInfo:@{
                @"log" : [NSString stringWithFormat:@"AIRBLivePlayerEngineWrapper(%p)::AliPlayer::%@", weakSelf, strLog]
            }];
        }];
    }
    return self;
}

- (void) dealloc {
    LOG(@"AIRBLivePlayerEngineWrapper::dealloc(%p)", self);
    if ([[NSThread currentThread] isMainThread]) {
        _player = nil;
    } else {
        dispatch_sync(dispatch_get_main_queue(), ^{ //注意这里是同步调用
            _player = nil;
        });
    }
}

- (void) startWithMediaURL:(NSString*)url {
    LOG(@"AIRBLivePlayerEngineWrapper::startWithMediaURL(%@)", url);

    __weak typeof(self) weakSelf = self;
    void(^preparePlayer)(void) = ^{
        weakSelf.mediaURL = url;
        [weakSelf createPlayerCore];
        [weakSelf.player setUrlSource:[[AVPUrlSource alloc] urlWithString:weakSelf.mediaURL]];
        [weakSelf.player prepare];
    };
    if ([[NSThread currentThread] isMainThread]) {
        preparePlayer();
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            preparePlayer();
        });
    }
}

- (void) pause {
    LOG(@"AIRBLivePlayerEngineWrapper::pause");
    [self.player pause];
}

- (void) resume {
    LOG(@"AIRBLivePlayerEngineWrapper::resume");
    [self.player start];
}

- (void) stop {
    LOG(@"AIRBLivePlayerEngineWrapper::stop");
    [self.player stop];
}

- (void) refresh {
    LOG(@"AIRBLivePlayerEngineWrapper::refresh");
    _isRefreshingPlay = YES;
    [self.player stop];
    [self.player prepare];
}

- (void) snapshot {
    LOG(@"AIRBLivePlayerEngineWrapper::snapshot");
    [self.player snapShot];
}

- (void)toggleMuted {
    LOG(@"AIRBLivePlayerEngineWrapper::toggleMuted");
    [self.player setMuted:![self.player isMuted]];
}

- (void)mute:(BOOL)mute {
    [self.player setMuted:mute];
}

- (void) createPlayerCore {
    if (!_player) {
        _player = [[AliPlayer alloc] init];
        _player.playerView = self.playerView;
        _player.autoPlay = YES;
        _player.delegate = self;
        _player.scalingMode = self.contentMode;
        AVPConfig* config = [_player getConfig];
        if (self.lowDelay) {
            config.maxDelayTime = 1000;
            config.highBufferDuration = 10;
            config.startBufferDuration = 10;
            config.networkRetryCount = 1;
            config.networkTimeout = 1000;
        } else {
//            config.networkTimeout = 5000;
//            config.networkRetryCount = 3;
        }
        [_player setConfig:config];
    }
}

#pragma mark - AVPDelegate

- (void) onPlayerEvent:(AliPlayer *)player eventType:(AVPEventType)eventType {
    LOG(@"AIRBLivePlayerEngineWrapper::onPlayerEvent(%lu)", (unsigned long)eventType);
    if (self.player == player) {
        switch (eventType) {
            case AVPEventPrepareDone: {
                if ([self.delegate respondsToSelector:@selector(onLivePlayerEnginePrepareDone)]) {
                    [self.delegate onLivePlayerEnginePrepareDone];
                }
            }
//                LOGD("AVPEventPrepareDone");
                break;
            case AVPEventAutoPlayStart:
//                LOGD("AVPEventAutoPlayStart");
                // 自动播放开始事件
                break;
            case AVPEventFirstRenderedStart:
//                LOGD("AVPEventFirstRenderedStart");
                
                if (_isRefreshingPlay) {
                    _isRefreshingPlay = NO;
                    return;
                }
                
                if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineFirstRenderedStartWithInfo:)]) {
                    
                    NSString* connectionInfo = [self.player getPropertyString:AVP_KEY_CONNECT_INFO];
                    NSData* dicData = [connectionInfo dataUsingEncoding:NSUTF8StringEncoding];
                    NSDictionary* connectionInfoDic = [NSJSONSerialization JSONObjectWithData:dicData options:NSJSONReadingMutableContainers error:nil];
                    
                    [self.delegate onLivePlayerEngineFirstRenderedStartWithInfo:@{
                        @"width" : @(self.player.width),
                        @"height" : @(self.player.height),
                        @"cdnIP" : [connectionInfoDic objectForKey:@"ip"] ?: (self.rtsServerIP ?: @""),
                        @"traceID" : self.rtsTraceID ?: @""
                    }];
                }
                break;
            case AVPEventCompletion:
                // 播放完成
                break;
            case AVPEventLoadingStart:
                if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineLoadingStart)]) {
                    [self.delegate onLivePlayerEngineLoadingStart];
                }
                break;
            case AVPEventLoadingEnd: {
//                LOGD("AVPEventLoadingEnd");
                if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineLoadingEnd)]) {
                    [self.delegate onLivePlayerEngineLoadingEnd];
                }
                break;
            }
            default:
                break;
        }
    }
}

-(void)onPlayerEvent:(AliPlayer*)player eventWithString:(AVPEventWithString)eventWithString description:(NSString *)description {
    switch (eventWithString) {
        case EVENT_PLAYER_DIRECT_COMPONENT_MSG: {
            NSData* dicData = [description dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary* connectionInfoDic = [NSJSONSerialization JSONObjectWithData:dicData options:NSJSONReadingMutableContainers error:nil];
            if ([connectionInfoDic objectForKey:@"content"]) {
                NSString* contentString = [connectionInfoDic objectForKey:@"content"];
                if ([contentString containsString:@"code=104"] && [contentString containsString:@"-sub-"]) {
                    NSString* traceID = [contentString substringFromIndex:[contentString rangeOfString:@"-sub-"].location + 5];
                    self.rtsTraceID = [traceID substringToIndex:traceID.length - 1];
                } else if ([contentString containsString:@"code=122"] && [contentString containsString:@"sfu:"]) {
                    NSString* sfuIPInfo = [contentString substringFromIndex:[contentString rangeOfString:@"sfu:"].location + 4];
                    self.rtsServerIP = [sfuIPInfo substringToIndex:[sfuIPInfo rangeOfString:@","].location];
                }
            }
        }
    
            break;
            
        default:
            break;
    }
        
}

- (void)onPlayerStatusChanged:(AliPlayer*)player oldStatus:(AVPStatus)oldStatus newStatus:(AVPStatus)newStatus {
    _playerStatus = newStatus;
}

- (void) onError:(AliPlayer *)player errorModel:(AVPErrorModel *)errorModel {
    LOG(@"AIRBLivePlayerEngineWrapper::onError(0x%lx, %@)", (unsigned long)errorModel.code, errorModel.message);
    
    if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineErrorCode:errorMessage:)]) {
        [self.delegate onLivePlayerEngineErrorCode:errorModel.code errorMessage:errorModel.message];
    }
}

- (void) onCaptureScreen:(AliPlayer *)player image:(UIImage *)image {
//    LOGE("AIRBLivePlayerEngineWrapper::onCaptureScreen");
    if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineImageSnapshot:)]) {
        [self.delegate onLivePlayerEngineImageSnapshot:image];
    }
}

- (void) onVideoRendered:(AliPlayer *)player timeMs:(int64_t)timeMs pts:(int64_t)pts {
//    LOG(@"AIRBLivePlayerEngineWrapper::onVideoRendered(time:%lld, pts:%lld)", timeMs, pts);
    if ([self.delegate respondsToSelector:@selector(onLivePlayerEngineVideoRendered)]) {
        [self.delegate onLivePlayerEngineVideoRendered];
    }
}

- (void)onVideoSizeChanged:(AliPlayer*)player width:(int)width height:(int)height rotation:(int)rotation {
    LOG(@"AIRBLivePlayerEngineWrapper::onVideoSizeChanged(%d, %d)", width, height);
    [self.delegate onLivePlayerEngineVideoSizeChanged:width height:height];
}

- (void)onCurrentDownloadSpeed:(AliPlayer *)player speed:(int64_t)speed {
    [self.delegate onLivePlayerEngineDownloadBitrate:speed / 1024.0];
}
@end
