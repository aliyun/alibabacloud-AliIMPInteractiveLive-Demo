//
//  ASLSBPusherEngine.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/20.
//

#import "ASLSBPusherEngine.h"

@interface ASLSBPusherEngine ()

@property(nonatomic, assign) BOOL microphoneMuted;

@end

@implementation ASLSBPusherEngine

- (UIViewController*) faceBeautyConfigViewController{
    if ([self isLinkMicEnabled]){
        return self.delegate.room.rtc.faceBeautyConfigViewController;
    } else{
        return self.delegate.room.livePusher.faceBeautyConfigViewController;
    }
    
    return nil;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        _byPassLiveResolutionType = AIRBRTCBypassLiveResolutionType_1280x720;
        [_delegate.linkMicHandler setVideoStreamTypeHighDimensions:CGSizeMake(1280, 720)];
    }
    return self;
}

- (void) setByPassLiveResolutionType:(AIRBRTCBypassLiveResolutionType)byPassLiveResolutionType{
    _byPassLiveResolutionType = byPassLiveResolutionType;
    if (![self isLinkMicEnabled]){
        return;
    }
    
    switch (byPassLiveResolutionType) {
        case AIRBRTCBypassLiveResolutionType_720x1280:
            [self.delegate.linkMicHandler setVideoStreamTypeHighDimensions:CGSizeMake(720, 1280)];
            break;
        case AIRBRTCBypassLiveResolutionType_1280x720:
            [self.delegate.linkMicHandler setVideoStreamTypeHighDimensions:CGSizeMake(1280, 720)];
            break;
        case AIRBRTCBypassLiveResolutionType_1080x1920:
            [self.delegate.linkMicHandler setVideoStreamTypeHighDimensions:CGSizeMake(1080, 1920)];
            break;
        case AIRBRTCBypassLiveResolutionType_1920x1080:
            [self.delegate.linkMicHandler setVideoStreamTypeHighDimensions:CGSizeMake(1920, 1080)];
            break;
            
        default:
            break;
    }
}

- (UIView*)localPreview{
    if ([self isLinkMicEnabled]) {
        return self.delegate.linkMicHandler.linkMicLocalPreview;
    } else {
        return self.delegate.room.livePusher.pusherView;
    }
}

- (BOOL) isLinkMicEnabled{
    if (![self.delegate.sceneModel.conferenceId isEqualToString:@""]){
        return YES;
    } else{
        return NO;
    }
}

- (void) startLive{
    if ([self isLinkMicEnabled]){
        [self.delegate.linkMicHandler linkMicJoin];
    } else{
        [self.delegate.room.livePusher startLiveStreaming];
    }
}

- (void) pauseLive{
    if ([self isLinkMicEnabled]){
        
    }
}

- (void) stopLiveOnSuccess:(void(^)(void))onSuccess
                 onFailure:(void(^)(NSString* error))onFailure{
    if ([self isLinkMicEnabled]){
        [self.delegate.linkMicHandler linkMicStopBypassLiveStreaming];
        [self.delegate.room.rtc leaveChannel:YES];
    }
    
    if (self.delegate.sceneModel.liveId.length > 0) {
        [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] stopLiveWithLiveID:self.delegate.sceneModel.liveId onSuccess:^{
            onSuccess();
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    }
}

- (void) startPreview:(UIView*)preview pushOrientation:(int)orientation{
    if ([self isLinkMicEnabled]){
        [self mirrorLiveVideo:YES]; //镜像
        if (orientation == 0) {
            self.byPassLiveResolutionType = AIRBRTCBypassLiveResolutionType_720x1280;
        } else if (orientation == 1){
            self.byPassLiveResolutionType = AIRBRTCBypassLiveResolutionType_1280x720;
        }
        [self.delegate.linkMicHandler linkMicOpenCamera];
    } else{
        AIRBLivePusherOptions* options = [AIRBLivePusherOptions defaultOptions];
        if (orientation == 1) {
            options.mediaStreamingOptions.orientation = 2;
        }
        [self.delegate.room.livePusher startLocalPreviewWithOptions:options preview:preview];
        [self.delegate.room.livePusher setContentMode:AIRBVideoViewContentModeAspectFill];

    }
}

- (void) startScreenCaptureWithOrientation:(int)orientation appGroupID:(NSString*)appGroupID{
    if ([self isLinkMicEnabled]){

    } else{
        AIRBLivePusherOptions* options = [AIRBLivePusherOptions defaultOptions];
        if (orientation == 1) {
            options.mediaStreamingOptions.orientation = 2;
        }
        [self.delegate.room.livePusher startScreenCaptureWithOptions:options appGroupID:appGroupID];
        [self.delegate.room.livePusher setContentMode:AIRBVideoViewContentModeAspectFill];

    }
}

- (void) switchCamera{
    if ([self isLinkMicEnabled]){
        [self.delegate.linkMicHandler linkMicSwitchCamera];
    } else{
        [self.delegate.room.livePusher toggleCamera];
    }
}

- (void) toggleMutedMicrophone{
    self.microphoneMuted = !self.microphoneMuted;
    if ([self isLinkMicEnabled]){
        if (self.microphoneMuted){
            [self.delegate.linkMicHandler linkMicCloseMic];
        } else{
            [self.delegate.linkMicHandler linkMicOpenMic];
        }
    } else{
        [self.delegate.room.livePusher toggleMuted];
    }
}

- (void) mirrorLiveVideo:(BOOL)mirror{
    if ([self isLinkMicEnabled]){
        if(mirror){
            [self.delegate.linkMicHandler linkMicSetPreviewMirror:YES];
            [self.delegate.linkMicHandler linkMicSetCameraStreamMirror:YES];
        }else{
            [self.delegate.linkMicHandler linkMicSetPreviewMirror:NO];
            [self.delegate.linkMicHandler linkMicSetCameraStreamMirror:NO];
        }
    } else{
        if(mirror){
            [self.delegate.room.livePusher setPreviewMirror:NO];
            [self.delegate.room.livePusher setStreamingVideoMirror:NO];
        }else{
            [self.delegate.room.livePusher setPreviewMirror:YES];
            [self.delegate.room.livePusher setStreamingVideoMirror:YES];
        }
    }
}

- (void) pauseLiveStreaming:(BOOL)pause{
    if ([self isLinkMicEnabled]){
        if (pause) {
            [self.delegate.linkMicHandler linkMicStopBypassLiveStreaming];
        } else {
            [self.delegate.linkMicHandler linkMicStartBypassLiveStreaming:self.byPassLiveResolutionType];
        }
    } else{
        if (pause) {
            [self.delegate.room.livePusher pauseLiveStreaming];
        } else {
            [self.delegate.room.livePusher resumeLiveStreaming];
        }
    }
}

- (void) restartLiveStreaming{
    if ([self isLinkMicEnabled]){
        
    } else{
        [self.delegate.room.livePusher restartLiveStreaming];
    }
}

- (void) updateLiveInfoWithTitle:(NSString*)title liveCoverURL:(NSString*)coverURL customDataStr:(NSString*)customDataStr customDataDic: (NSDictionary<NSString*,NSString*>*)customDataDic onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure{
    // 原子能力层
    if (![self isLinkMicEnabled]){
        if (title) {
            self.delegate.room.livePusher.options.businessOptions.liveTitle = title;
        }
        if (coverURL) {
            self.delegate.room.livePusher.options.businessOptions.liveCoverUrl = coverURL;
        }
        if (customDataStr.length > 0) {
            self.delegate.room.livePusher.options.businessOptions.extension = customDataStr;
        } else if ([customDataDic count] > 0) {
            NSData* jsonData = [NSJSONSerialization dataWithJSONObject:customDataDic options:NSJSONWritingPrettyPrinted error:nil];
            self.delegate.room.livePusher.options.businessOptions.extension = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        }
        [self.delegate.room.livePusher updateLiveBusinessOptions:self.delegate.room.livePusher.options.businessOptions onSuccess:^{
            onSuccess();
        } onFailure:^(NSString * _Nonnull errorMessage) {
            onFailure(errorMessage);
        }];
    }
}

- (void) updateLiveBusinessInfoWithLiveID:(NSString*)liveID title:(NSString*)title notice:(NSString*)notice liveCoverURL:(NSString*)coverURL customDataDic: (NSDictionary<NSString*,NSString*>*)customDataDic onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * errorMessage))onFailure{
    [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] getLiveDetailWithLiveID:liveID onSuccess:^(NSDictionary * _Nonnull response) {
        NSString* liveTitle = [response valueForKey:@"title"];
        NSString* liveNotice = [response valueForKey:@"notice"];
        NSString* liveCoverURL = [response valueForKey:@"cover_url"];
        NSDictionary* liveCustomData = [response valueForKey:@"extension"];
        
        [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] updateLiveBusinessInfo:({
            AIRBRoomSceneLiveBusinessInfo* businessInfo = [[AIRBRoomSceneLiveBusinessInfo alloc] init];
            businessInfo.liveID = liveID;
            businessInfo.liveTitle = title ?: liveTitle;
            businessInfo.liveNotice = notice ?: liveNotice;
            businessInfo.liveCoverURL = coverURL ?: liveCoverURL;
            businessInfo.liveCustomData = customDataDic ?: liveCustomData;
            businessInfo;
        }) onSuccess:^{
            onSuccess();
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    } onFailure:^(NSString * _Nonnull error) {
        onFailure(error);
    }];
}

#pragma mark -RTC delegate

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted:{
            DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, ASLSBPusherEngineEventPreviewStarted, info, info);
        }
            break;
        case AIRBRTCEventJoinSucceeded: {
            [self.delegate.linkMicHandler linkMicStartBypassLiveStreaming:self.byPassLiveResolutionType];
        }
            break;
        case AIRBRTCEventBypassLiveStarted:{
            DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, ASLSBPusherEngineEventStreamStarted, info, info);
        }
            break;
            
        default:
            break;
    }
}

#pragma mark -LivePuhser delegate

- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info{
    switch (event) {
        case AIRBLivePusherEventPreviewStarted:{
            DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, ASLSBPusherEngineEventPreviewStarted, info, info);
        }
            break;
        case AIRBLivePusherEventStreamResumed:{
            DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, ASLSBPusherEngineEventStreamResumed, info, info);
        }
            break;
        case AIRBLivePusherEventStreamStarted:{
            DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, ASLSBPusherEngineEventStreamStarted, info, info);
        }
            break;
            
        default:
            break;
    }
}

@end
