//
//  AIRBLivePusher.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLivePusher.h"

#import <objc/message.h>

#import <vpaassdk/room/VPROOMRoomExtInterface.h>
#import <vpaassdk/live/VPLIVELiveRpcInterface.h>
#import <vpaassdk/live/VPLIVELiveModule.h>
#import <vpaassdk/easyutils/VPEasyutils.h>

#import "AIRBRoomChannel.h"
#import "AIRBLivePusherOptions.h"
#import "AliInteractiveRoomLogger.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"
#import "../Utilities/AIRBEnvironments.h"
     

@interface AIRBLivePusher()
@property (strong, nonatomic) id livePuserEngine;
@property (copy, nonatomic) NSString* mediaURL;
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* userID;
@property (assign, nonatomic) BOOL livePublished;

@property (strong, nonatomic) VPLIVELiveModule* liveModule;
@property (strong, nonatomic) VPLIVELiveRpcInterface* liveRpcInterface;
@end

@implementation AIRBLivePusher

@synthesize options = _options;
@synthesize delegate = _delegate;
@synthesize pusherView = _pusherView;
@synthesize contentMode = _contentMode;

- (id) queenEngine {
    return ((id (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"queenEngine"));
}

- (UIView*) pusherView {
    return ((UIView* (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"preview"));
}

- (id)livePuserEngine {
    if (!_livePuserEngine) {
        Class livePusherEngineClass = NSClassFromString(@"AIRBLivePusherEngineWrapper");
        if (livePusherEngineClass) {
            _livePuserEngine = ((id (*)(id, SEL))objc_msgSend)(livePusherEngineClass, NSSelectorFromString(@"createPusherEngine"));
            ((void (*)(id, SEL, id))objc_msgSend)(_livePuserEngine, NSSelectorFromString(@"setDelegate:"), self);
        }
    }
    return _livePuserEngine;
}

- (void)setContentMode:(AIRBVideoViewContentMode)contentMode {
    _contentMode = contentMode;
    if (_livePuserEngine) {
        ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setContentMode:"), contentMode);
    }
}

- (UIViewController*)faceBeautyConfigViewController {
    return ((UIViewController* (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"faceBeautyConfigViewController"));
}

#pragma mark -Actions

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
        _liveModule = [VPLIVELiveModule getModule:_userID];
        _liveRpcInterface = [_liveModule getRpcInterface];
        
        
    }
    return self;
}

- (void)dealloc {
    LOGD("AIRBLivePusher(%p) dealloc", self);
    if (_livePuserEngine) {
        _livePuserEngine = nil;
    }
    
    [AIRBMonitorHubManager sharedInstance].livePusherModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) updateLiveID:(NSString*)liveID {
    if (![liveID isEqualToString:self.liveID]) {
        self.livePublished = NO;
    }
    self.liveID = liveID;
}

- (void) getLivePusherDetailAndStartStreaming {
    LOGD("AIRBLivePusher::getLivePusherDetailAndStartStreaming");
    [self.liveRpcInterface getLiveDetailWithBlock:[VPLIVEGetLiveDetailReq VPLIVEGetLiveDetailReqWithUuid:self.liveID] onSuccess:^(VPLIVEGetLiveDetailRsp * _Nonnull rsp) {
        self.livePublished = rsp.live.status == 1 ? YES : NO;
        self.mediaURL = [self decodeURL:rsp.live.pushUrl];
        ((void (*)(id, SEL, id))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setMediaURL:"), self.mediaURL);
        
        ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"startLiveStreaming"));
        ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setContentMode:"), self.contentMode);
        
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
            [self.delegate onAIRBLivePusherError:AIRBLivePusherFailedToGetDetail message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) createLiveAndStartStreaming {
    LOGD("AIRBLivePusher::createLiveAndStartStreaming");
    VPROOMCreateLiveReq* req = [VPROOMCreateLiveReq VPROOMCreateLiveReqWithRoomId:self.room.roomID
                                                         anchorId:self.userID
                                                            title:self.options.businessOptions.liveTitle
                                                     preStartDate:self.options.businessOptions.liveStartTime
                                                       preEndDate:self.options.businessOptions.liveEndTime
                                                     introduction:self.options.businessOptions.liveIntroduction coverUrl:self.options.businessOptions.liveCoverUrl
                                                  userDefineField:self.options.businessOptions.extension];
    
    [self.room.roomExtInterface createLiveWithBlock:req onSuccess:^(VPROOMCreateLiveRsp * _Nonnull rsp) {
        LOGD("AIRBLivePusher::createLiveWithBlock succeeded.");
        self.liveID = rsp.liveId;
        [self getLivePusherDetailAndStartStreaming];
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
            [self.delegate onAIRBLivePusherError:AIRBLivePusherFailedToCreate message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options {
    LOGD("AIRBLivePusher::startLocalPreviewWithOptions(%@).", options);
    [self startLocalPreviewWithOptions:options preview:nil];
}

- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options preview:(nonnull UIView *)preview {
    LOGD("AIRBLivePusher::startLocalPreviewWithOptions(%@, %@).", options, preview);
    self.options = options;
    [AIRBMonitorHubManager sharedInstance].livePusherModel.status = AIRBMonitorHubComponentStatusRunning;
    ((void (*)(id, SEL, id, id, id))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setupLivePusherEngineWithOptions:preview:cloudConfig:"), options, preview, [AIRBEnvironments shareInstance].cloudConfig);
}

- (void) startScreenCaptureWithOptions:(AIRBLivePusherOptions*)options appGroupID:(nonnull NSString *)appGroupID {
    LOGD("AIRBLivePusher::startScreenCaptureWithOptions(%@).", options);
    self.options = options;
    [AIRBMonitorHubManager sharedInstance].livePusherModel.status = AIRBMonitorHubComponentStatusRunning;
    ((void (*)(id, SEL, id, id, id))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setupLivePusherEngineWithOptions:appGroupID:cloudConfig:"), options, appGroupID, [AIRBEnvironments shareInstance].cloudConfig);
}

- (void) startLiveStreaming {
    LOGD("AIRBLivePusher::startLiveStreaming.");
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishStartLive info:nil];
    
    if (self.liveID) {
        [self getLivePusherDetailAndStartStreaming];
    } else {
        [self createLiveAndStartStreaming];
    }
}

- (void) startLiveStreamingWithPushUrl:(NSString *)url {
    LOGD("AIRBLivePusher::startLiveStreamingWithPushUrl:%@, liveID:%@", url, self.liveID);
    if (self.liveID) {
        self.mediaURL = [self decodeURL:url];
        ((void (*)(id, SEL, id))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setMediaURL:"), self.mediaURL);
        
        ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"startLiveStreaming"));
        ((void (*)(id, SEL, AIRBVideoViewContentMode))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setContentMode:"), self.contentMode);
    } else {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
            [self.delegate onAIRBLivePusherError:AIRBLivePusherFailedToStartPusherEngine message:@"invalid live id"];
        }
    }
}

- (void) restartLiveStreaming {
    LOGD("AIRBLivePusher::restartLiveStreaming.");
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"restartLiveStreaming"));
}

- (void) pauseLiveStreaming {
    LOGD("AIRBLivePusher::pauseLiveStreaming.");
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishPauseLive info:@{
        @"error_code" : @"0"
    }];
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"pauseLiveStreaming"));
}

- (void) resumeLiveStreaming {
    LOGD("AIRBLivePusher::resumeLiveStreaming.");
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"resumeLiveStreaming"));
}

- (void) stopLiveStreaming:(BOOL)stopLive {
    LOGD("AIRBLivePusher::stopLiveStreaming(%d).", stopLive);
    
    [AIRBMonitorHubManager sharedInstance].livePusherModel.status = AIRBMonitorHubComponentStatusNotRunning;
    
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"stopLiveStreaming"));
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishStopLive info:@{
        @"error_code" : @"0"
    }];
    
    if (stopLive && self.liveID.length > 0) {
        [self.room.roomExtInterface destroyLiveWithBlock:[VPROOMDestroyLiveReq VPROOMDestroyLiveReqWithRoomId:self.room.roomID liveId:self.liveID] onSuccess:^(VPROOMDestroyLiveRsp * _Nonnull rsp) {
            LOGD("AIRBLivePusher::destroyLiveWithBlock succeeded.");
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
                [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventStopped info:@{}];
            }
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
                [self.delegate onAIRBLivePusherError:AIRBLivePusherFailedToStopLive message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
}

- (void) stopLiveStreaming {
    LOGD("AIRBLivePusher::stopLiveStreaming.");
    [self stopLiveStreaming:YES];
}

- (void) toggleMuted {
    LOGD("AIRBLivePusher::toggleMuted.");
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"toggleMuted"));
}

- (void) toggleCamera {
    LOGD("AIRBLivePusher::toggleCamera.");
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"toggleCamera"));
}

- (void) setPreviewMirror:(BOOL)mirror {
    LOGD("AIRBLivePusher::setPreviewMirror(%d).", mirror);
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setPreviewMirror:"), mirror);
}

- (void) setStreamingVideoMirror:(BOOL)mirror {
    LOGD("AIRBLivePusher::setStreamingVideoMirror(%d).", mirror);
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setStreamingVideoMirror:"), mirror);
}

- (void) toggleFaceBeauty {
    LOGD("AIRBLivePusher::toggleFaceBeauty.");
    ((void (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"toggleLiveFaceBeauty"));
}

- (void) setFlash:(BOOL)open {
    LOGD("AIRBLivePusher::setFlash:%d.", open);
    ((void (*)(id, SEL, BOOL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setFlash:"), open);
}

- (int)setZoom:(float)zoom {
    LOGD("AIRBLivePusher::setZoom:%f.", zoom);
    return ((int (*)(id, SEL, float))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setZoom:"), zoom);
}

- (float)getMaxZoom {
    LOGD("AIRBLivePusher::getMaxZoom.");
    return ((float (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"getMaxZoom"));
}

- (float)getCurrentZoom {
    LOGD("AIRBLivePusher::getCurrentZoom.");
    return ((float (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"getCurrentZoom"));
}

- (int)focusCameraAtAdjustedPoint:(CGPoint)point autoFocus:(bool)autoFocus {
    LOGD("AIRBLivePusher::focusCameraAtAdjustedPoint.");
    return ((int (*)(id, SEL, CGPoint, BOOL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"focusCameraAtAdjustedPoint:autoFocus:"), point, autoFocus);
}

- (int)setExposure:(float)exposure {
    LOGD("AIRBLivePusher::setZoom:%f.", exposure);
    return ((int (*)(id, SEL, float))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"setExposure:"), exposure);
}

- (float)getCurrentExposure {
    LOGD("AIRBLivePusher::getCurrentExposure.");
    return ((float (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"getCurrentExposure"));
}

- (float)getSupportedMinExposure {
    LOGD("AIRBLivePusher::getSupportedMinExposure.");
    return ((float (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"getSupportedMinExposure"));
}

- (float)getSupportedMaxExposure {
    LOGD("AIRBLivePusher::getSupportedMaxExposure.");
    return ((float (*)(id, SEL))objc_msgSend)(self.livePuserEngine, NSSelectorFromString(@"getSupportedMaxExposure"));
}

- (void) updateLiveBusinessOptions:(AIRBLivePusherLiveBusinessOptions*) businessOptions
                         onSuccess:(void (^)(void))onSuccess
                         onFailure:(void (^)(NSString* errorMessage))onFailure {
    if (businessOptions) {
        if (businessOptions.liveTitle && ![businessOptions.liveTitle isEqualToString:self.options.businessOptions.liveTitle]) {
            self.options.businessOptions.liveTitle = businessOptions.liveTitle;
        }
        if (businessOptions.liveCoverUrl && ![businessOptions.liveCoverUrl isEqualToString:self.options.businessOptions.liveCoverUrl]) {
            self.options.businessOptions.liveCoverUrl = businessOptions.liveCoverUrl;
        }
        if (businessOptions.extension && ![businessOptions.extension isEqualToString:self.options.businessOptions.extension]) {
            self.options.businessOptions.extension = businessOptions.extension;
        }
        if (businessOptions.liveIntroduction && ![businessOptions.liveIntroduction isEqualToString:self.options.businessOptions.liveIntroduction]) {
            self.options.businessOptions.liveIntroduction = businessOptions.liveIntroduction;
        }
        VPLIVEUpdateLiveReq* req = [VPLIVEUpdateLiveReq VPLIVEUpdateLiveReqWithUuid:self.liveID
                                                              title:self.options.businessOptions.liveTitle
                                                       introduction:self.options.businessOptions.liveIntroduction coverUrl:self.options.businessOptions.liveCoverUrl
                                                    userDefineField:self.options.businessOptions.extension];
        
        [self.liveRpcInterface updateLiveWithBlock:req onSuccess:^(VPLIVEUpdateLiveRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(@"Invalid input option.");
    }
}

- (NSString*)decodeURL:(NSString*)URL {
    NSURL* url = [NSURL URLWithString:URL];
    if ([URL containsString:@"vpaasPrint="] && url && url.path.length >= 7 && [url.query containsString:@"vpaasPrint="]) {
        NSString* encodedString = [[url.path substringFromIndex:6] lowercaseString];
        NSString* password = [url.query substringFromIndex:11];
        char byte_chars[3] = {'\0','\0','\0'};
        NSMutableData* encodedData = [[NSMutableData alloc] init];
        for (int i = 0; i < [encodedString length]; i += 2) {
            byte_chars[0] = [encodedString characterAtIndex:i];
            byte_chars[1] = [encodedString characterAtIndex:i+1];
            unsigned char whole_byte = strtol(byte_chars, NULL, 16);
            [encodedData appendBytes:&whole_byte length:1];
        }
        
        NSData* passwordData = [password dataUsingEncoding:NSUTF8StringEncoding];
        NSData* saltData = [[AIRBMonitorHubManager sharedInstance].configModel.appID  dataUsingEncoding:NSUTF8StringEncoding];
        
        VPEasyutilsEncryptDecryptDataResult* decRes = [VPEasyutils decryptData:passwordData salt:saltData inData:encodedData];
        if (decRes.ok) {
            NSString* decString = [[NSString alloc] initWithData:decRes.data encoding:NSUTF8StringEncoding];
            if (decString.length > 0) {
                return [NSString stringWithFormat:@"%@://%@/live/%@", url.scheme, url.host, decString];
            }
        }
    }
    return URL;
}

#pragma mark -Delegates

- (void) onLivePusherErrorWithCode:(int32_t)code message:(NSString *)msg{
    LOGE("AIRBLivePusher::onLivePusherErrorWithCode(%ld, %@).", code, msg);
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
        [self.delegate onAIRBLivePusherError:code message:msg];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishErrorEvent info:@{
        @"error_code" : [NSString stringWithFormat:@"%d", code],
        @"error_msg" : msg ?: @""
    }];
    
    if (code == 0x30020303) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishEncoderLowFps info:nil];
    }
}

- (void) onLivePusherPreviewStarted {
    LOGD("AIRBLivePusher::onLivePusherPreviewStarted.");
    [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventPreviewStarted info:@{}];
}

- (void) onLivePuhserStreamingStartedWithMediaInfo:(NSDictionary*)info {
    LOGD("AIRBLivePusher::onLivePuhserStreamingStarted.");
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishVideoEncoderInit info:({
        NSMutableDictionary* mediaInfo = [[NSMutableDictionary alloc] initWithDictionary:info[@"video"]];
        [mediaInfo setValue:@"0" forKey:@"code"];
        mediaInfo;
    })];
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishAudioEncoderInit info:({
        NSMutableDictionary* mediaInfo = [[NSMutableDictionary alloc] initWithDictionary:info[@"audio"]];
        [mediaInfo setValue:@"0" forKey:@"code"];
        mediaInfo;
    })];
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishRtmpConnect info:@{
        @"code" : @"0",
        @"push_url" : self.mediaURL
    }];
    
    if (!self.livePublished) {
        [self.liveRpcInterface publishLiveWithBlock:[VPLIVEPublishLiveReq VPLIVEPublishLiveReqWithUuid:self.liveID] onSuccess:^(VPLIVEPublishLiveRsp * _Nonnull rsp) {
            LOGD("AIRBLivePusher::publishLiveWithBlock succeeded.");
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
                [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventStreamStarted info:@{
                    @"liveID" : self.liveID ? : @""
                }];
            }
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishPublishLive info:@{
                @"error_code" : @"0",
                @"engine" : @"rtmp"
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBLivePusherError:message:)]) {
                [self.delegate onAIRBLivePusherError:AIRBLivePusherFailedToPublishLive message:ERR_MSG_FROM_DPSERROR(error)];
            }
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishPublishLive info:@{
                @"error_code" : [NSString stringWithFormat:@"%d",error.code],
                @"engine" : @"rtmp"
            }];
        }];
    } else {
        if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
            [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventStreamResumed info:@{
                @"liveID" : self.liveID ? : @""
            }];
        }
    }
}

- (void) onLivePuhserStreamingStopped {
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishRtmpClose info:nil];
}

- (void) onLivePuhserStreamingRestarted {
    LOGD("AIRBLivePusher::onLivePuhserStreamingRestarted.");
    [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventStreamRestarted info:@{}];
}

- (void) onLivePusherStreamingConnectFailed {
    LOGD("AIRBLivePusher::onLivePusherStreamingConnectFailed.");
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventNetworkConnectFailed info:@{}];
    }
}

- (void) onLivePusherStreamingNetworkPoor {
    LOGD("AIRBLivePusher::onLivePusherStreamingNetworkPoor.");
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventNetworkPoored info:@{}];
    }
}

- (void) onLivePusherStreamingNetworkRecoveried {
    LOGD("AIRBLivePusher::onLivePusherStreamingNetworkRecoveried.");
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventNetworkRecoveried info:@{}];
    }
}

- (void) onLivePusherStreamingNetworkConnectionLost {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventNetworkConnectionLost info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishNetDisconnect info:nil];
}

- (void) onLivePusherStreamingReconnectStart {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventNetworkReconnectStart info:@{}];
    }
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishNetReconnectStart info:@{
        @"cdn_ip" : @"",
        @"url" : self.mediaURL ?: @""
    }];
}

- (void) onLivePusherStreamingReconnectSuccess:(BOOL)success {
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:success ? AIRBLivePusherEventNetworkReconnectSuccess : AIRBLivePusherEventNetworkReconnectFailed info:@{}];
    }
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishNetReconnectSucess info:nil];
}

- (void) onLivePusherStreamingStatistics:(NSDictionary*)statistics {
    [AIRBMonitorHubManager sharedInstance].livePusherModel.mediaStreamStatistics = statistics;
    if ([self.delegate respondsToSelector:@selector(onAIRBLivePuhserEvent:info:)]) {
        [self.delegate onAIRBLivePuhserEvent:AIRBLivePusherEventStreamingUploadBitrateUpdated info:@{
            @"v_bitrate" : statistics[@"screen_sent_bitrate"],
            @"a_bitrate" : statistics[@"audio_sent_bitrate"]
        }];
    }
}
@end
