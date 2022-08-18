//
//  AIRBRTCEngineWrapper.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRTCEngineWrapper.h"
#import <AliRTCSdk/AliRTCSdk.h>
#import <objc/message.h>
#import "AIRBRTCEngineConfig.h"
#import "../AliInteractiveRoomBundle/Utilities/NSTimer+WeakTarget.h"

//#import "AliInteractiveRoomLogger.h"

#define LOG(...) NSLog(__VA_ARGS__)

@interface AIRBRTCEngineWrapper() <AliRtcEngineDelegate>
@property (strong, nonatomic) AliRtcEngine* rtcEngine;
@property (strong, nonatomic) AliRtcAuthInfo* rtcAuthInfo;
@property (strong, nonatomic) AIRBRTCEngineConfig* config;
@property (assign, nonatomic) BOOL muteLocalCameraInvoked;
@property (assign, nonatomic) BOOL isJoiningChannel; //调用join到收到join的结果前为YES
@property (assign, nonatomic) BOOL hasJoinedChannel; //join成功到调用leave前为YES
@property (strong, nonatomic) NSLock* joinChannelLock;
@property (nonatomic, strong) AliVideoCanvas* previewCanvas;
@property (nonatomic, strong) AliRtcVideoEncoderConfiguration* videoEncoderConfig;
@property (nonatomic, strong) NSMutableDictionary<NSString*, NSNumber*>* clientVideoTrackStatus;

@property (nonatomic, assign) NSUInteger previewContentMode;    //本地相机预览的填充模式，默认为Auto

@property (nonatomic, assign) NSUInteger remoteVideoStreamContentMode;  // 远端视频流在本地的填充模式，默认为Auto
@property (strong, nonatomic) id faceBeautyManager;

@property(nonatomic, strong) NSTimer* retryJoinCountTimer; // 计时器
@property(nonatomic, assign) int retryJoinTimerInterval; // 计时时长
@property(nonatomic, assign) int retryJoinCount; // 重试次数
@property(nonatomic, strong) NSTimer* retryPublishAudioCountTimer;
@property(nonatomic, assign) int retryPublishAudioTimerInterval;
@property(nonatomic, assign) int retryPublishAudioCount;
@property(nonatomic, strong) NSTimer* retryPublishVideoCountTimer;
@property(nonatomic, assign) int retryPublishVideoTimerInterval;
@property(nonatomic, assign) int retryPublishVideoCount;
@property(nonatomic, assign) AliRtcPublishState audioPublishState;
@property(nonatomic, assign) AliRtcPublishState videoPublishState;

@end

@implementation AIRBRTCEngineWrapper

@synthesize previewMirrorEnabled = _previewMirrorEnabled;
@synthesize previewContentMode = _previewContentMode;
@synthesize videoStreamMirrorEnabled = _videoStreamMirrorEnabled;

#pragma -mark Properties
- (AliRtcEngine*) rtcEngine {
    if (!_rtcEngine) {
        [AliRtcEngine setH5CompatibleMode:YES];
        _rtcEngine = [AliRtcEngine sharedInstance:self extras:@""];
        [_rtcEngine setLogLevel:AliRtcLogLevelInfo];
    }
    return _rtcEngine;
}

- (AliVideoCanvas*) previewCanvas{
    if (!_previewCanvas){
        _previewCanvas = [[AliVideoCanvas alloc] init];
        _previewCanvas.view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _previewCanvas.renderMode = _previewContentMode;
        _previewCanvas.mirrorMode = _previewMirrorEnabled ? AliRtcRenderMirrorModeOnlyFrontCameraPreviewEnabled : AliRtcRenderMirrorModeAllDisabled;
    }
    return _previewCanvas;
}

- (id) faceBeautyManager {
    if (!_faceBeautyManager) {
        Class faceBeautyManagerClass = NSClassFromString(@"AIRBFaceBeautyManager");
        if (faceBeautyManagerClass) {
            _faceBeautyManager = ((id (*)(id, SEL))objc_msgSend)(faceBeautyManagerClass, NSSelectorFromString(@"createManager"));
        }
    }
    return _faceBeautyManager;
}

- (id) queenEngine {
    if (!_queenEngine) {
        _faceBeautyOn = YES;
        [self registerLocalVideoTexture:YES];
        ((void (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"setupQueenEngine"));
        _queenEngine = ((id (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"queenEngine"));
    }
    return _queenEngine;
}

- (UIViewController*)faceBeautyConfigViewController {
    if (!_queenEngine) {
        [self queenEngine];
    }
    return ((UIViewController* (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"faceBeautyVC"));
}

#pragma -mark Lifecycle

+ (instancetype) createRTCEngine {
    AIRBRTCEngineWrapper* instance = [[AIRBRTCEngineWrapper alloc] init];
    LOG(@"AIRBRTCEngineWrapper::create(%p)", instance);
    return instance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _clientVideoTrackStatus = [[NSMutableDictionary alloc] init];
        _previewMirrorEnabled = YES;
        _videoStreamMirrorEnabled = NO;
        _previewContentMode = 0;
        _remoteVideoStreamContentMode = 0;
        _videoEncoderConfig = [[AliRtcVideoEncoderConfiguration alloc] init];
        _videoEncoderConfig.mirrorMode = AliRtcVideoEncoderMirrorModeDisabled;
        _faceBeautyOn = NO;
        
        _joinChannelLock = [[NSLock alloc] init];
        
        _retryJoinCount = 0;
        _retryJoinCountTimer = [NSTimer weakTargetScheduledTimerWithTimeInterval:1 target:self selector:@selector(retryJoinCountTimerAction) userInfo:nil repeats:YES];
        _retryPublishAudioCount = 0;
        _retryPublishAudioCountTimer = [NSTimer weakTargetScheduledTimerWithTimeInterval:1 target:self selector:@selector(retryPublishAudioCountTimerAction) userInfo:nil repeats:YES];
        _retryPublishVideoCount = 0;
        _retryPublishVideoCountTimer = [NSTimer weakTargetScheduledTimerWithTimeInterval:1 target:self selector:@selector(retryPublishVideoCountTimerAction) userInfo:nil repeats:YES];

    }
    return self;
}

- (void)dealloc {
    LOG(@"AIRBRTCEngineWrapper::dealloc(%p)", self);
    [_retryJoinCountTimer invalidate];
    _retryJoinCountTimer = nil;
    [_retryPublishAudioCountTimer invalidate];
    _retryPublishAudioCountTimer = nil;
    [_retryPublishVideoCountTimer invalidate];
    _retryPublishVideoCountTimer = nil;
}

- (void) setAudioOnlyMode:(BOOL)audioOnly{
    [self.rtcEngine setAudioOnlyMode:audioOnly];
}

- (void) setVideoDimensions:(CGSize)videoDimensions frameRate:(NSInteger)frameRate bitrate:(NSInteger)bitrate {
    // 视频流设置
    self.videoEncoderConfig.dimensions = videoDimensions;
    self.videoEncoderConfig.frameRate = frameRate;
    self.videoEncoderConfig.bitrate = bitrate;
    [self.rtcEngine setVideoEncoderConfiguration:self.videoEncoderConfig];
}

- (UIView*) localView{
    return self.previewCanvas.view;
}

- (BOOL) previewMirrorEnabled{
    return _previewMirrorEnabled;
}

- (void) setPreviewMirrorEnabled:(BOOL)previewMirrorEnabled{
    _previewMirrorEnabled = previewMirrorEnabled;
    dispatch_async(dispatch_get_main_queue(), ^{
//        if (!self.previewCanvas){
//            self.previewCanvas = [[AliVideoCanvas alloc] init];
//            self.previewCanvas.view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
//            self.previewCanvas.renderMode = AliRtcRenderModeAuto;
//        }
        self.previewCanvas.mirrorMode = previewMirrorEnabled ? AliRtcRenderMirrorModeOnlyFrontCameraPreviewEnabled : AliRtcRenderMirrorModeAllDisabled;
        [self.rtcEngine setLocalViewConfig:self.previewCanvas forTrack:AliRtcVideoTrackCamera];
    });
}

- (NSUInteger) previewContentMode{
    return _previewContentMode;
}

- (void) setPreviewContentMode:(NSUInteger)previewContentMode{
    _previewContentMode = previewContentMode;
    dispatch_async(dispatch_get_main_queue(), ^{
        self.previewCanvas.renderMode = previewContentMode;
        [self.rtcEngine setLocalViewConfig:self.previewCanvas forTrack:AliRtcVideoTrackCamera];
    });
}

- (BOOL) videoStreamMirrorEnabled{
    return _videoStreamMirrorEnabled;
}

- (void) setVideoStreamMirrorEnabled:(BOOL)videoStreamMirrorEnabled{
    _videoStreamMirrorEnabled = videoStreamMirrorEnabled;
    
    AliRtcCameraDirection currentCameraDirection = [self.rtcEngine getCurrentCameraDirection];
    if (currentCameraDirection == AliRtcCameraDirectionFront || currentCameraDirection == AliRtcCameraDirectionInvalid){
        self.videoEncoderConfig.mirrorMode = videoStreamMirrorEnabled ? AliRtcVideoEncoderMirrorModeEnabled : AliRtcVideoEncoderMirrorModeDisabled;
    } else if (currentCameraDirection == AliRtcCameraDirectionBack){
        self.videoEncoderConfig.mirrorMode =  AliRtcVideoEncoderMirrorModeDisabled;
    }
    [self.rtcEngine setVideoEncoderConfiguration:self.videoEncoderConfig];
}

- (void) startPreview {
    LOG(@"AIRBRTCEngineWrapper::startPreview");
    dispatch_async(dispatch_get_main_queue(), ^{
//        UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
//        if (UIInterfaceOrientationIsPortrait(orientation)) {
//            [self.rtcEngine setDeviceOrientationMode:AliRtcOrientationModePortrait];
//        } else {
//            [self.rtcEngine setDeviceOrientationMode:AliRtcOrientationModeLandscapeLeft];
//        }
        
//        if (!self->_previewCanvas){
//            self.previewCanvas = [[AliVideoCanvas alloc] init];
//            self.previewCanvas.view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
//            self.previewCanvas.renderMode = AliRtcRenderModeAuto;
//            self.previewCanvas.mirrorMode = self.previewMirrorEnabled ? AliRtcRenderMirrorModeOnlyFrontCameraPreviewEnabled : AliRtcRenderMirrorModeAllDisabled;
        [self.rtcEngine setLocalViewConfig:self.previewCanvas forTrack:AliRtcVideoTrackCamera];
//        }
        
        [self.rtcEngine startPreview];
        
    });
}

- (void)joinChannelWithConfig:(AIRBRTCEngineConfig*)config {
    LOG(@"AIRBRTCEngineWrapper::joinChannelWithConfig");
    
    [self.joinChannelLock lock];
    if (self.isJoiningChannel || self.hasJoinedChannel) {
        [self.delegate onAIRBRTCEngineErrorWithCode:8 message:@"已在连麦中"];
        [self.joinChannelLock unlock];
        return;
    }
    self.isJoiningChannel = YES;
    [self.joinChannelLock unlock];
    
    [self.rtcEngine setChannelProfile:AliRtcInteractivelive];
    [self.rtcEngine setClientRole:AliRtcClientRoleInteractive];
    
    self.config = config;
    self.rtcAuthInfo = [[AliRtcAuthInfo alloc] init];
    self.rtcAuthInfo.appId = self.config.appId;
    self.rtcAuthInfo.agent = @[@""];
    self.rtcAuthInfo.channelId = self.config.conferenceID;
    self.rtcAuthInfo.gslb = @[self.config.gslb];
    self.rtcAuthInfo.nonce = self.config.nonce;
    self.rtcAuthInfo.timestamp = self.config.timestamp;
    self.rtcAuthInfo.token = self.config.token;
    self.rtcAuthInfo.userId = self.config.userID;
    
//    LOGE("joinChannelWithConfig::\nappID:%@\nagent:%@\nchannelID:%@\ngslb:%@\nnonce:%@\nts:%lld\ntoken:%@\nuid:%@\n", self.rtcAuthInfo.appId, self.rtcAuthInfo.agent, self.rtcAuthInfo.channelId, self.rtcAuthInfo.gslb, self.rtcAuthInfo.nonce, self.rtcAuthInfo.timestamp, self.rtcAuthInfo.token, self.rtcAuthInfo.userId);
    
    if (self.config.audioOnlyModeEnabled) {
//        [self.rtcEngine setAudioOnlyMode:self.config.audioOnlyModeEnabled];
    } else {
        [self.rtcEngine publishLocalDualStream:config.dualStreamPublished];
        
        if (!self.muteLocalCameraInvoked){
            [self muteLocalCamera:NO];
        }
    }
    
    // 开启音频音量和说话人uid回调
    [self.rtcEngine enableAudioVolumeIndication:350 smooth:3 reportVad:1];
                
    [self.rtcEngine setDefaultSubscribeAllRemoteAudioStreams:YES];
    [self.rtcEngine setDefaultSubscribeAllRemoteVideoStreams:NO];
    
    [self.clientVideoTrackStatus removeAllObjects];
    self.hasJoinedChannel = NO;
    
//    [self.rtcEngine joinChannel:self.rtcAuthInfo name:self.config.userNick onResult:^(NSInteger errCode, NSString * _Nonnull channel, NSInteger elapsed) {
//    }];
    [self.rtcEngine joinChannel:self.rtcAuthInfo name:self.config.userNick onResultWithUserId:^(NSInteger errCode, NSString * _Nonnull channel, NSString * _Nonnull userId, NSInteger elapsed) {
        
    }];
}

- (void) leaveChannel {
    LOG(@"AIRBRTCEngineWrapper::leaveChannel");
    [self.rtcEngine leaveChannel];
    self.hasJoinedChannel = NO;
    
    // 停止重试计时
    [self stopCountTimer:self.retryJoinCountTimer];
    self.retryJoinCount = 0;
    [self stopCountTimer:self.retryPublishAudioCountTimer];
    self.retryPublishAudioCount = 0;
    [self stopCountTimer:self.retryPublishVideoCountTimer];
    self.retryPublishVideoCount = 0;
}

- (void) destroyEngine {
    LOG(@"AIRBRTCEngineWrapper::destroyEngine");
    [AliRtcEngine destroy];
}

- (int) muteLocalMicphone:(BOOL)mute {
    LOG(@"AIRBRTCEngineWrapper::muteLocalMicphone(%d)", mute);
    return [self.rtcEngine muteLocalMic:mute mode:AliRtcMuteAudioModeDefault];
//    int muteResult = [self.rtcEngine muteLocalMic:mute mode:AliRtcMuteAudioModeDefault];
//    int publishResult =  [self.rtcEngine publishLocalAudioStream:!mute];
//    if (muteResult == 0 && publishResult == 0){
//        return 0;
//    }
//
//    return -1;
}

- (int) muteAllRemoteAudioPlaying:(BOOL)mute {
    LOG(@"AIRBRTCEngineWrapper::muteAllRemoteAudioPlaying(%d)", mute);
    return [self.rtcEngine muteAllRemoteAudioPlaying:mute];
}

- (int) muteRemoteAudioPlaying:(BOOL)mute remotePeer:(NSString*)userID {
    LOG(@"AIRBRTCEngineWrapper::muteRemoteAudioPlaying(%d, %@)", mute, userID);
    return [self.rtcEngine muteRemoteAudioPlaying:userID mute:mute];
}

- (int) muteLocalCamera:(BOOL)mute {
    LOG(@"AIRBRTCEngineWrapper::muteLocalCamera(%d)", mute);
    self.muteLocalCameraInvoked = YES;
//    if ([self.rtcEngine muteLocalCamera:mute forTrack:AliRtcVideoTrackCamera] != 0 || [self.rtcEngine publishLocalVideoStream:!mute] != 0){
    int localMuteSuccess = [self.rtcEngine muteLocalCamera:mute forTrack:AliRtcVideoTrackCamera];
    int muteResult = localMuteSuccess;
    if (localMuteSuccess == 0){
        localMuteSuccess = [self.rtcEngine publishLocalVideoStream:!mute];
    }
    if (localMuteSuccess == 0){
        if (mute){
            if (!self.hasJoinedChannel){
                localMuteSuccess = [self.rtcEngine stopPreview];
            }
        } else{
            [self startPreview];
        }
    }
    if (localMuteSuccess == 0 && self.hasJoinedChannel){
        localMuteSuccess = [self.rtcEngine enableLocalVideo:!mute];
    }
    
    return muteResult;
}

- (void) toggleLocalCamera {
    LOG(@"AIRBRTCEngineWrapper::toggleLocalCamera");
    [self.rtcEngine switchCamera];
    if (_videoStreamMirrorEnabled){
        [self setVideoStreamMirrorEnabled:_videoStreamMirrorEnabled];
    }
}

- (void) subscribeRemoteAudioStream:(BOOL)sub fromUser:(NSString*)userID {
    LOG(@"AIRBRTCEngineWrapper::subscribeRemoteAudioStream(%d, %@)", sub, userID);
    [self.rtcEngine subscribeRemoteAudioStream:userID sub:sub];
}

- (void) subscribeRemoteVideoStream:(BOOL)sub type:(int8_t)type fromUser:(NSString*)userID {
    LOG(@"AIRBRTCEngineWrapper::subscribeRemoteVideoStream(%d, %d, %@)", sub, type, userID);
    [self.rtcEngine setRemoteVideoStreamType:userID type:type];
    [self.rtcEngine subscribeRemoteVideoStream:userID track:AliRtcVideoTrackCamera sub:sub];
}

- (void) subscribeRemoteScreenShareStream:(BOOL)sub fromUser:(NSString*)userID {
    [self.rtcEngine subscribeRemoteVideoStream:userID track:AliRtcVideoTrackScreen sub:sub];
}

- (void) setBasicFaceBeauty:(BOOL)open whiteningLevel:(float)whiteningLevel smoothnessLevel:(float)smoothnessLevel {
    AliRtcBeautyConfig* beautyConfig = [[AliRtcBeautyConfig alloc] init];
    beautyConfig.whiteningLevel = whiteningLevel;
    beautyConfig.smoothnessLevel = smoothnessLevel;
    [self.rtcEngine setBeautyEffect:open config:beautyConfig];
}

- (int) startScreenShare:(NSString*)appGroup {
    return [self.rtcEngine startScreenShare:appGroup mode:AliRtcScreenShareVideo];
}

- (int) stopScreenShare {
    return [self.rtcEngine stopScreenShare];
}

- (void) registerLocalVideoTexture:(BOOL)reg {
    if (reg) {
        [self.rtcEngine registerLocalVideoTexture];
    } else {
        [self.rtcEngine unregisterLocalVideoTexture];
    }
}

#pragma -mark RetryLogic

- (void)retryJoinCountTimerAction{
    if(self.retryJoinTimerInterval-- <= 0){
        LOG(@"AIRBRTCEngineWrapper::stopRetryJoinCountTimer(%d)", self.retryJoinCount);
        [self stopCountTimer:self.retryJoinCountTimer];
        self.retryJoinCount++;
        
        [self leaveChannel];
        [self.delegate queryRTCTokenInfoAndJoinChannel];
    }
}

- (void)startRetryJoinCountTimer{
    if (self.retryJoinCount >= 3) {
        self.retryJoinCount = 0;
        // 进行报错处理
        LOG(@"AIRBRTCEngineWrapper::AIRBRTCRetryJoinChannelFatalError(JoinCount:%d)", self.retryJoinCount);
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCEngineErrorWithCode:2 message:@"重试加入超时"];
        }
    } else{
        LOG(@"AIRBRTCEngineWrapper::startRetryJoinCountTimer(%d)", self.retryJoinCount);
        self.retryJoinTimerInterval = (self.retryJoinCount + 1) * 5;
        self.retryJoinCountTimer.fireDate = [NSDate distantPast]; // 打开定时器
    }
}

- (void)retryPublishAudioCountTimerAction{
    if(self.retryPublishAudioTimerInterval-- <= 0){
        LOG(@"AIRBRTCEngineWrapper::stopRetryPublishAudioCountTimer(%d)", self.retryPublishAudioCount);
        [self stopCountTimer:self.retryPublishAudioCountTimer];
        self.retryPublishAudioCount++;
        
        [self.rtcEngine publishLocalAudioStream:YES];
    }
}

- (void)startRetryPublishAudioCountTimer{
    LOG(@"AIRBRTCEngineWrapper::startRetryPublishAudioCountTimer(%d)", self.retryPublishAudioCount);
    self.retryPublishAudioTimerInterval = (self.retryPublishAudioCount + 1) * 5;
    self.retryPublishAudioCountTimer.fireDate = [NSDate distantPast]; // 打开定时器
}

- (void)retryPublishVideoCountTimerAction{
    if(self.retryPublishVideoTimerInterval-- <= 0){
        LOG(@"AIRBRTCEngineWrapper::stopRetryPublishVideoCountTimer(%d)", self.retryPublishVideoCount);
        [self stopCountTimer:self.retryPublishVideoCountTimer];
        self.retryPublishVideoCount++;
        
        [self.rtcEngine publishLocalVideoStream:YES];
    }
}

- (void)startRetryPublishVideoCountTimer{
    LOG(@"AIRBRTCEngineWrapper::startRetryPublishVideoCountTimer(%d)", self.retryPublishVideoCount);
    self.retryPublishVideoTimerInterval = (self.retryPublishVideoCount + 1) * 5;
    self.retryPublishVideoCountTimer.fireDate = [NSDate distantPast]; // 打开定时器
}

- (void)stopCountTimer:(NSTimer*)timer{
    timer.fireDate = [NSDate distantFuture]; //关闭定时器
}


#pragma -mark AliRtcEngineDelegate
/**
 * @brief 加入频道结果
 * @param result 加入频道结果，成功返回0，失败返回错误码
 * @param channel 加入频道的id
 * @param elapsed 加入频道耗时
 * @note 此回调等同于joinChannel接口的block，二者择一处理即可
 */
- (void)onJoinChannelResult:(int)result channel:(NSString *_Nonnull)channel elapsed:(int) elapsed {
    LOG(@"AIRBRTCEngineWrapper::onJoinChannelResult 0x%x, %@", result, [AliRtcEngine getErrorDescription:result]);
    self.isJoiningChannel = NO;
    if (result == 0){
        self.hasJoinedChannel = YES;
        self.retryJoinCount = 0;
    }
    if ([self.delegate respondsToSelector:@selector(reportJoinChannelSucceeded:errorCode:)]) {
        [self.delegate reportJoinChannelSucceeded:(result == 0) errorCode:[AliRtcEngine getErrorDescription:result]];
    }
}

/**
 * @brief 离开频道结果
 * @param result 离开频道结果，成功返回0，失败返回错误码
 * @param stats 本次频道内会话的数据统计汇总。
 * @note 调用leaveChannel接口后返回，如果leaveChannel后直接调用destroy，将不会收到此回调
 */
- (void)onLeaveChannelResult:(int)result stats:(AliRtcStats)stats {
    LOG(@"AIRBRTCEngineWrapper::onLeaveChannelResult 0x%x, %@", result, [AliRtcEngine getErrorDescription:result]);
    if ([self.delegate respondsToSelector:@selector(reportLeaveChannelSucceeded:errorCode:)]) {
        [self.delegate reportLeaveChannelSucceeded:(result == 0) errorCode:[AliRtcEngine getErrorDescription:result]];
    }
}

/**
 * @brief 音频推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onAudioPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    self.audioPublishState = newState;
    if (newState == AliRtcStatsPublished){
        self.retryPublishAudioCount = 0;
    }
}

/**
 * @brief 视频推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onVideoPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    self.videoPublishState = newState;
    if (newState == AliRtcStatsPublished){
        self.retryPublishVideoCount = 0;
    }
}

/**
 * @brief 次要流推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onDualStreamPublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 屏幕分享推流变更回调
 * @param oldState 之前的推流状态
 * @param newState 当前的推流状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
*/
- (void)onScreenSharePublishStateChanged:(AliRtcPublishState)oldState newState:(AliRtcPublishState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 当远端用户上线时会返回这个消息
 * @param uid userID 从App server分配的唯一标示符
 * @param elapsed 用户加入频道时的耗时
 * @note This message does not mean the user remains online state
 */
- (void)onRemoteUserOnLineNotify:(NSString *_Nonnull)uid elapsed:(int)elapsed {
    NSDictionary* userInfo = [self.rtcEngine getUserInfo:uid];
    if ([self.delegate respondsToSelector:@selector(onRTCEngineRemoteUserOnlineNotify:userInfo:)]) {
        [self.delegate onRTCEngineRemoteUserOnlineNotify:uid userInfo:userInfo];
    }
}

/**
 * @brief 当远端用户下线时会返回这个消息
 * @param uid userID 从App server分配的唯一标示符
 * @param reason 用户离线的原因
 * @note This message does not mean the user remains offline state
 */
- (void)onRemoteUserOffLineNotify:(NSString *_Nonnull)uid offlineReason:(AliRtcUserOfflineReason)reason {
    NSString* reasonStr;
    switch (reason) {
        case AliRtcUserOfflineQuit:
            reasonStr = @"quit";
            break;
        case AliRtcUserOfflineDropped:
            reasonStr = @"dropped";
            break;
        case AliRtcUserOfflineBecomeAudience:
            reasonStr = @"other";
            break;
            
        default:
            break;
    }
    
    if ([self.delegate respondsToSelector:@selector(onRTCEngineRemoteUserOfflineNotify:reason:)]) {
        [self.delegate onRTCEngineRemoteUserOfflineNotify:uid reason:reasonStr];
    }
}

/**
 * @brief 当远端用户的流发生变化时，返回这个消息
 * @note 远方用户停止推流，也会发送这个消息
 */
- (void)onRemoteTrackAvailableNotify:(NSString *_Nonnull)uid audioTrack:(AliRtcAudioTrack)audioTrack videoTrack:(AliRtcVideoTrack)videoTrack {
    if ([[self.clientVideoTrackStatus allKeys] containsObject:uid] && [[self.clientVideoTrackStatus objectForKey:uid] intValue] == videoTrack){
        return;
    }

    int oldVideoTrack = [[self.clientVideoTrackStatus allKeys] containsObject:uid] ? [[self.clientVideoTrackStatus objectForKey:uid] intValue] : 0;
    [self.clientVideoTrackStatus setValue:[NSNumber numberWithInt:(int)videoTrack] forKey:uid];
    dispatch_async(dispatch_get_main_queue(), ^{
        if (videoTrack == AliRtcVideoTrackCamera || videoTrack == AliRtcVideoTrackBoth) {
            if (oldVideoTrack != AliRtcVideoTrackCamera && oldVideoTrack != AliRtcVideoTrackBoth) {   //  相机流从不可用变为可用
                AliVideoCanvas *canvas = [[AliVideoCanvas alloc] init];
                canvas.renderMode = self.remoteVideoStreamContentMode;
                canvas.view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
                [self.rtcEngine setRemoteViewConfig:canvas uid:uid forTrack:AliRtcVideoTrackCamera];
                if ([self.delegate respondsToSelector:@selector(onRemotePeerCameraViewAvailable:view:)]) {
                    [self.delegate onRemotePeerCameraViewAvailable:uid view:canvas.view];
                }
            }
        } else if (oldVideoTrack == AliRtcVideoTrackCamera || oldVideoTrack == AliRtcVideoTrackBoth){
            if (videoTrack != AliRtcVideoTrackCamera && videoTrack != AliRtcVideoTrackBoth){   // 相机流从可用变为不可用
                if ([self.delegate respondsToSelector:@selector(onRemotePeerCameraViewUnavailable:)]) {
                    [self.delegate onRemotePeerCameraViewUnavailable:uid];
                }
            }
        }
        
        if (videoTrack == AliRtcVideoTrackScreen || videoTrack == AliRtcVideoTrackBoth) {
            if (oldVideoTrack != AliRtcVideoTrackScreen && oldVideoTrack != AliRtcVideoTrackBoth) {   // 屏幕共享流从不可用变为可用
                AliVideoCanvas *canvas = [[AliVideoCanvas alloc] init];
                canvas.renderMode = AliRtcRenderModeAuto;
                canvas.view = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
                [self.rtcEngine setRemoteViewConfig:canvas uid:uid forTrack:AliRtcVideoTrackScreen];
                if ([self.delegate respondsToSelector:@selector(onRemotePeerScreenShareViewAvailable:view:)]) {
                    [self.delegate onRemotePeerScreenShareViewAvailable:uid view:canvas.view];
                }
            }
        } else if (oldVideoTrack == AliRtcVideoTrackScreen || oldVideoTrack == AliRtcVideoTrackBoth){
            if (videoTrack != AliRtcVideoTrackScreen && videoTrack != AliRtcVideoTrackBoth){
                if ([self.delegate respondsToSelector:@selector(onRemotePeerScreenShareViewUnavailable:)]) {
                    [self.delegate onRemotePeerScreenShareViewUnavailable:uid];
                }
            }
        }
    });
    
//        if (!self.remoteUserView) {
//            UICollectionViewFlowLayout *flowLayout = [[UICollectionViewFlowLayout alloc] init];
//            flowLayout.itemSize = CGSizeMake(140, 280);
//            flowLayout.minimumLineSpacing = 10;
//            flowLayout.minimumInteritemSpacing = 10;
//            flowLayout.scrollDirection = UICollectionViewScrollDirectionHorizontal;
//
//            self.remoteUserView = [[UICollectionView alloc] initWithFrame:CGRectZero collectionViewLayout:flowLayout];
////            self.remoteUserView.frame = rc;
//            self.remoteUserView.backgroundColor = [UIColor clearColor];
//            self.remoteUserView.delegate   = self;
//            self.remoteUserView.dataSource = self;
//            self.remoteUserView.showsHorizontalScrollIndicator = NO;
//            [self.remoteUserView registerClass:[AIRBRealTimeCommunicationRemoteUserView class] forCellWithReuseIdentifier:@"cell"];
//        }
//
//        [self.remoteUserManager updateRemoteUser:uid forTrack:videoTrack];
//        else if (videoTrack == AliRtcVideoTrackBoth) {
//
//            AliVideoCanvas *canvas = [[AliVideoCanvas alloc] init];
//            canvas.renderMode = AliRtcRenderModeAuto;
//            canvas.view = [self.remoteUserManager cameraView:uid];
//            [self.rtcEngine setRemoteViewConfig:canvas uid:uid forTrack:AliRtcVideoTrackCamera];
//
//            AliVideoCanvas *canvas2 = [[AliVideoCanvas alloc] init];
//            canvas2.renderMode = AliRtcRenderModeAuto;
//            canvas2.view = [self.remoteUserManager screenView:uid];
//            [self.rtcEngine setRemoteViewConfig:canvas2 uid:uid forTrack:AliRtcVideoTrackScreen];
//        }
//        [self.remoteUserView reloadData];
//    });
}

/**
 * @brief 音频订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onAudioSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 相机流订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onVideoSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 屏幕分享流订阅情况变更回调
 * @param oldState 之前的订阅状态
 * @param newState 当前的订阅状态
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onScreenShareSubscribeStateChanged:(NSString *_Nonnull)uid oldState:(AliRtcSubscribeState)oldState newState:(AliRtcSubscribeState)newState elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 大小流订阅情况变更回调
 * @param oldStreamType 之前的订阅的大小流类型
 * @param newStreamType 当前的订阅的大小流类型
 * @param elapseSinceLastState 状态变更时间间隔(毫秒)
 * @param channel 当前频道id
 */
- (void)onSubscribeStreamTypeChanged:(NSString *_Nonnull)uid oldStreamType:(AliRtcVideoStreamType)oldStreamType newStreamType:(AliRtcVideoStreamType)newStreamType elapseSinceLastState:(NSInteger)elapseSinceLastState channel:(NSString *_Nonnull)channel {
    ;
}

/**
 * @brief 当用户角色发生变化化时通知
 * @param oldRole 变化前角色类型
 * @param newRole 变化后角色类型
 * @note 调用setClientRole方法切换角色成功时触发此回调
 */
- (void)onUpdateRoleNotifyWithOldRole:(AliRtcClientRole)oldRole newRole:(AliRtcClientRole)newRole {
    ;
}

/**
 * @brief 网络质量变化时发出的消息
 * @param uid 网络质量发生变化的uid
 * @param upQuality  上行网络质量
 * @param downQuality  下行网络质量
 * @note 当网络质量发生变化时触发，uid为@""时代表self的网络质量变化
 */
- (void)onNetworkQualityChanged:(NSString *_Nonnull)uid
               upNetworkQuality:(AliRtcNetworkQuality)upQuality
             downNetworkQuality:(AliRtcNetworkQuality)downQuality {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineNetworkQualityChanged:upNetworkQuality:downNetworkQuality:)]) {
        [self.delegate onRTCEngineNetworkQualityChanged:uid upNetworkQuality:(int)upQuality downNetworkQuality:(int)downQuality];
    }
}

/**
 * @brief 网络连接断开
 */
- (void)onConnectionLost {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineNetworkConnectionLost)]) {
        [self.delegate onRTCEngineNetworkConnectionLost];
    }
}

/**
 * @brief 网络连接正在尝试重连中
 */
- (void)onTryToReconnect {
    ;
}

/**
 * @brief 网络连接重连成功
 */
- (void)onConnectionRecovery {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineNetworkReconnectSuccess)]) {
        [self.delegate onRTCEngineNetworkReconnectSuccess];
    }
}

/**
 * @brief 网络连接状态改变的回调
 * @param status 当前状态值, 对应值参考枚举AliRtcConnectionStatus
 * @param reason 引起状态变化的具体原因, 对应值参考枚举AliRtcConnectionStatusChangeReason
*/
- (void)onConnectionStatusChange:(AliRtcConnectionStatus)status reason:(AliRtcConnectionStatusChangeReason)reason {
    if (status == AliRtcConnectionStatusReconnecting) {
        if ([self.delegate respondsToSelector:@selector(onRTCEngineNetworkReconnectStart)]) {
            [self.delegate onRTCEngineNetworkReconnectStart];
        }
    } else if (status == AliRtcConnectionStatusFailed) {
        if ([self.delegate respondsToSelector:@selector(onRTCEngineNetworkConnectFailed)]) {
            [self.delegate onRTCEngineNetworkConnectFailed];
        }
    }
}

/**
 * @brief 被服务器踢出频道的消息
 */
- (void)onBye:(int)code {
    ;
}

/**
 * @brief 如果engine出现warning，通过这个回调通知app
 * @param warn  Warning type
 * @param message 警告描述
 */
- (void)onOccurWarning:(int)warn message:(NSString *_Nonnull)message {
    ;
}

/**
 * @brief 如果engine出现error，通过这个回调通知app
 * @param error  Error type
 * @param message 错误描述
 */
- (void)onOccurError:(int)error message:(NSString *_Nonnull)message {
//    LOGE("onOccurError(0x%x, %@, %@)", error, message, [AliRtcEngine getErrorDescription:error]);
    
    switch (error) {
            // 无法恢复需要重新创建Rtc实例并加入
        case 0x01030204:
        case 0x01010103:
        case 0x0101010C:
        case 0x0101010D:
        case 0x01040202:
        case 0x02010105:
        case 0x0102020C:
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCUnrecoverableFatalError(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:1 message:[NSString stringWithFormat:@"请尝试重新加入连麦(%x)", error]];
            }
            break;

            // join失败，进行重试
        case 0x01020201:
        case 0x01020204:
            // 提示检查网络
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCNerworkError(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:4 message:[NSString stringWithFormat:@"请检查网络连接是否正常(%x)", error]];
            }
        case 0x02010203:
        case 0x02010205:
        case 0x02010702:
        case 0x02010104:
        case 0x05010012:
        case 0x01030201:
        case 0x01010406: // 未进入频道推流失败
            LOG(@"AIRBRTCEngineWrapper::startRetryJoinCountTimer(%x)", error);
            [self startRetryJoinCountTimer];
            break;
            
            // 重新推流音频和视频
        case 0x01030304:
        case 0x01030305:
        case 0x01030308:
        case 0x02010401:
            if (self.retryPublishAudioCount >= 3 || self.retryPublishVideoCount >= 3) {
                // 进行报错处理
                LOG(@"AIRBRTCEngineWrapper::AIRBRTCRetryPublishFatalError(AudioCount:%d, VideoCount:%d, Error:%x)", self.retryPublishAudioCount, self.retryPublishVideoCount, error);
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                    [self.delegate onAIRBRTCEngineErrorWithCode:3 message:[NSString stringWithFormat:@"重试推流超时(%x)", error]];
                }
            } else{
                if (self.audioPublishState != AliRtcStatsPublished) { // 音频
                    [self startRetryPublishAudioCountTimer];
                }
                if (self.delegate.localCameraOpened && self.videoPublishState != AliRtcStatsPublished) { // 视频
                    [self startRetryPublishVideoCountTimer];
                }
            }
            break;
            
            // 重试推流音频
        case 0x01010450:
            if (self.retryPublishAudioCount >= 3) {
                // 进行报错处理
                LOG(@"AIRBRTCEngineWrapper::AIRBRTCRetryPublishFatalError(AudioCount:%d, Error:%x)", self.retryPublishAudioCount, error);
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                    [self.delegate onAIRBRTCEngineErrorWithCode:3 message:[NSString stringWithFormat:@"重试推流超时(%x)", error]];
                }
            } else{
                [self startRetryPublishAudioCountTimer];
            }
            break;
            
            // 重试推流视频
        case 0x01010451:
            if (self.retryPublishVideoCount >= 3) {
                // 进行报错处理
                LOG(@"AIRBRTCEngineWrapper::AIRBRTCRetryPublishFatalError(VideoCount:%d, Error:%x)", self.retryPublishVideoCount, error);
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                    [self.delegate onAIRBRTCEngineErrorWithCode:3 message:[NSString stringWithFormat:@"重试推流超时(%x)", error]];
                }
            } else{
                [self startRetryPublishVideoCountTimer];
            }
            break;
            
            // 推送屏幕共享失败
        case 0x01010453:
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCRetryPublishFatalError(ScreenShare, Error:%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:3 message:[NSString stringWithFormat:@"推送屏幕共享失败(%x)", error]];
            }
            break;
            
            // 麦克风无权限
        case 0x01040408:
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCMicrophoneNoPermissionError(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:5 message:[NSString stringWithFormat:@"无麦克风权限(%x)", error]];
            }
            break;
            
            // 摄像头打开失败(无权限或被占用）
        case 0x01040104:
        case 0x01040106:
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCCameraNoPermissionOrOccupiedError(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:6 message:[NSString stringWithFormat:@"摄像头打开失败(无权限或被占用）(%x)", error]];
            }
            break;
            
            // 提示网络问题
        case 0x01020401:
            
        case 0x01020210:    // 信令请求超时
        case 0x01020211:
        case 0x01020212:
        case 0x01020213:
        case 0x01020214:
        case 0x01020215:
        case 0x01020216:
        case 0x01020217:
        case 0x01020218:
            
        case 0x01050201:    // 媒体通道建立失败
        case 0x01050202:    // 媒体通道重连失败
            
        case 0x01020202:    // 信令请求超时
        case 0x01020203:
        case 0x01020205:
        case 0x01020206:
        case 0x01020207:
        case 0x01020208:
        case 0x01020209:
        case 0x0102020A:
        case 0x0102020B:
        case 0x0102020D:
        case 0x0102020E:
        case 0x0102020F:
        case 0x01020219:
        case 0x01020220:
        case 0x01020221:
        case 0x01020222:
            
        case 0x00000103:    // 本地网络连接断开
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCNerworkError(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:4 message:[NSString stringWithFormat:@"请检查网络连接是否正常(%x)", error]];
            }
            break;
            
            // 订阅出错
        case 0x01030404:
        case 0x01010550:
        case 0x01010551:
        case 0x01010552:
        case 0x01010553:
        case 0x01010554:
            LOG(@"AIRBRTCEngineWrapper::AIRBRTCFailedToSubscribe(%x)", error);
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEngineErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCEngineErrorWithCode:7 message:[NSString stringWithFormat:@"订阅错误(%x)", error]];
            }
            break;
            
        default:
            if ([self.delegate respondsToSelector:@selector(onRTCEngineError:message:)]) {
                [self.delegate onRTCEngineError:error message:[AliRtcEngine getErrorDescription:error]];
            }
            break;
    }
}

/**
 * @brief 当前设备性能不足
 */
- (void)onPerformanceLow {
    ;
}

/**
 * @brief 当前设备性能恢复
*/
- (void)onPerformanceRecovery {
    ;
}

/**
 * @brief 音频首包数据发送成功
 * @param timeCost  发送耗时
 */
- (void)onFirstAudioPacketSentWithTimecost:(int)timeCost {
    ;
}

/**
 * @brief 视频首包数据发送成功
 * @param videoTrack  发送成功的视频流类型
 * @param timeCost  发送耗时
 */
- (void)onFirstVideoPacketSentWithVideoTrack:(AliRtcVideoTrack)videoTrack
                                    timeCost:(int)timeCost {
    ;
}

/**
 * @brief 音频首包数据接收成功
 * @param uid   User ID。从App server分配的唯一标示符
 * @param timeCost  接收耗时
 */
- (void)onFirstAudioPacketReceivedWithUid:(NSString *_Nonnull)uid
                                 timeCost:(int)timeCost {
    ;
}

/**
 * @brief 视频首包数据接收成功
 * @param uid   User ID。从App server分配的唯一标示符
 * @param videoTrack  接收成功的视频流类型
 * @param timeCost  接收耗时
 */
- (void)onFirstVideoPacketReceivedWithUid:(NSString *_Nonnull)uid
                          videoTrack:(AliRtcVideoTrack)videoTrack
                            timeCost:(int)timeCost {
    ;
}

/**
 * @brief 已解码远端音频首帧回调
 * @param uid  用户userId
 * @param elapsed  从本地用户加入频道直至该回调触发的延迟, 单位为毫秒
 */
- (void)onFirstRemoteAudioDecodedWithUid:(NSString *_Nonnull)uid elapsed:(int)elapsed {
    ;
}

/**
 * @brief remote user的第一帧视频帧显示时触发这个消息
 * @param uid   User ID。从App server分配的唯一标示符
 * @param videoTrack 屏幕流或者相机流
 * @param width 视频宽度
 * @param height 视频高度
 * @param elapsed 总耗时
  */
- (void)onFirstRemoteVideoFrameDrawn:(NSString *_Nonnull)uid videoTrack:(AliRtcVideoTrack)videoTrack width:(int)width height:(int)height elapsed:(int)elapsed {
    if (videoTrack == AliRtcVideoTrackCamera){
        if ([self.delegate respondsToSelector:@selector(onFirstRemoteCameraVideoFrameDrawn:)]) {
            [self.delegate onFirstRemoteCameraVideoFrameDrawn:uid];
        }
    }
    
    if (videoTrack == AliRtcVideoTrackScreen){
        if ([self.delegate respondsToSelector:@selector(onFirstRemoteScreenShareVideoFrameDrawn:)]) {
            [self.delegate onFirstRemoteScreenShareVideoFrameDrawn:uid];
        }
    }
}

/**
 * @brief 预览开始显示第一帧视频帧时触发这个消息
 * @param width 视频宽度
 * @param height 视频高度
 * @param elapsed 总耗时
 */
- (void)onFirstLocalVideoFrameDrawn:(int)width height:(int)height elapsed:(int)elapsed {
    if ([self.delegate respondsToSelector:@selector(onLocalPreviewStarted)]) {
        [self.delegate onLocalPreviewStarted];
    }
}

/**
 * @brief 订阅的音频数据回调
 * @param audioSource audio source
 * @param audioSample audio sample
 */
- (void)onAudioSampleCallback:(AliRtcAudioSource)audioSource audioSample:(AliRtcAudioDataSample *_Nonnull)audioSample {
    ;
}

/**
 * @brief 混音前的指定用户的音频数据
 * @param uid user id
 * @param audioSample the audio data sampl
 */
- (void)onPlaybackAudioFrameBeforeMixing:(NSString *_Nonnull)uid audioSample:(AliRtcAudioDataSample *_Nonnull)audioSample {
    ;
}

/**
 * @brief 订阅的音频音量，语音状态和uid
 * @param array 表示回调用户音量信息数组，包含用户uid,语音状态以及音量，uid为"0"表示本地说话人。
 * @param totalVolume 混音后的总音量，范围[0,255]。在本地用户的回调中，totalVolume;为本地用户混音后的音量；在远端用户的回调中，totalVolume; 为所有说话者混音后的总音量
 */
- (void)onAudioVolumeCallback:(NSArray <AliRtcUserVolumeInfo *> *_Nullable)array totalVolume:(int)totalVolume {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineAudioVolumeCallback:totalVolume:)]) {
        [self.delegate onRTCEngineAudioVolumeCallback:array totalVolume:totalVolume];
    }
}

/**
 * @brief 订阅当前正在说话的人
 * @param uid 说话人uid, 为"0"表示本地说话人。其返回的是当前时间段内声音最大的用户ID，而不是瞬时声音最大的用户ID
*/
- (void)onActiveSpeaker:(NSString *_Nonnull)uid {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineActiveSpeaker:)]) {
        [self.delegate onRTCEngineActiveSpeaker:uid];
    }
}

/**
 * @brief 视频数据输出格式
 * @return 期望视频输出格式
 * @note 在注册registerVideoSampleObserver后触发回调，应用可返回期望输出的视频数据格式，默认返回AliRtcYUV420
 */
//- (AliRtcVideoFormat)onGetVideoFormatPreference {
//    ;
//}

/**
 * @brief 视频数据输出位置
 * @return 期望视频输出，参考AliRtcVideoObserPosition
 * @note 在注册registerVideoSampleObserver后触发回调，应用可返回期望输出的视频内容，对应数据将分别从onCaptureVideoSample/onPreEncodeVideoSample/onRemoteVideoSample
 * @note 默认返回全部类型数据，即AliRtcPositionPostCapture | AliRtcPositionPreRender | AliRtcPositionPreEncoder
 */
//- (NSInteger)onGetVideoObservedFramePosition {
//    ;
//}

/**
 * @brief 订阅的本地采集视频数据回调
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
//- (BOOL)onCaptureVideoSample:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample {
//    ;
//}

/**
 * @brief 订阅的本地编码前视频数据回调
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
//- (BOOL)onPreEncodeVideoSample:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample {
//    ;
//}

/**
 * @brief 订阅的远端视频数据回调
 * @param uid user id
 * @param videoSource video source
 * @param videoSample video sample
 * @return true: 需要写回SDK（只对I420和CVPixelBuffer(ios/mac)有效），false: 不需要写回SDK
*/
//- (BOOL)onRemoteVideoSample:(NSString *_Nonnull)uid videoSource:(AliRtcVideoSource)videoSource videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample {
//    ;
//}


/**
 * @brief 用户muteAudio通知
 * @param uid 执行muteAudio的用户
 * @param isMute YES:静音 NO:未静音
 */
- (void)onUserAudioMuted:(NSString *_Nonnull)uid audioMuted:(BOOL)isMute {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineUserAudioMuted:audioMuted:)]) {
        [self.delegate onRTCEngineUserAudioMuted:uid audioMuted:isMute];
    }
}

/**
 * @brief 用户muteVideo通知
 * @param uid 执行muteVideo的用户
 * @param isMute YES:推流黑帧 NO:正常推流
 */
- (void)onUserVideoMuted:(NSString *_Nonnull)uid videoMuted:(BOOL)isMute {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineUserVideoMuted:videoMuted:)]) {
        [self.delegate onRTCEngineUserVideoMuted:uid videoMuted:isMute];
    }
}

/**
 * @brief 用户EnableLocalVideo通知
 * @param uid 执行EnableLocalVideo的用户
 * @param isEnable YES:打开相机流采集 NO:关闭相机流采集
 */
- (void)onUserVideoEnabled:(NSString *_Nullable)uid videoEnabled:(BOOL)isEnable {
    ;
}

/**
 * @brief 用户audio被中断通知（一般用户打电话等音频被抢占场景）
 * @param uid audio被中断的用户
 */
- (void)onUserAudioInterruptedBegin:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 用户audio中断结束通知（对应onUserAudioInterruptedBegin）
 * @param uid audio中断结束的用户
 */
- (void)onUserAudioInterruptedEnded:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 远端用户应用退到后台
 * @param uid 用户
 */
- (void)onUserWillResignActive:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 远端用户应用返回前台
 * @param uid 用户
 */
- (void)onUserWillBecomeActive:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 订阅本地视频Texture 创建回调
 * @param context context
 */
- (void)onTextureCreate:(void *_Nullable)context {
    [self queenEngine];
}

/**
 * @brief 订阅本地视频Texture绘制回调
 * @param textureId texture id
 * @param width width
 * @param height height
 * @param videoSample video sample
 */
- (int)onTextureUpdate:(int)textureId width:(int)width height:(int)height videoSample:(AliRtcVideoDataSample *_Nonnull)videoSample {
    
    if (self.faceBeautyOn && _queenEngine) {
        NSInteger imgFormat = 0;
        switch (videoSample.format)
        {
            case AliRtcVideoFormat_I420:
            case AliRtcVideoFormat_NV12:
                imgFormat = 2;
                break;
            case AliRtcVideoFormat_NV21:
                imgFormat = 1;
                break;
            case AliRtcVideoFormat_RGB24:
                imgFormat = 0;
                break;
            case AliRtcVideoFormat_RGBA:
                imgFormat = 3;
                break;
            default:
                break;
        }
        
        

        return ((int (*)(id, SEL, int, int, int, NSInteger, uint8_t*))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"processTexture:width:height:format:data:"), textureId, width, height, imgFormat, (uint8_t *)videoSample.dataPtr);
    }
    return textureId;
}

/**
 * @brief 订阅本地视频Texture销毁回调
 */
- (void)onTextureDestory {
    if (_queenEngine) {
        ((void (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"destroyQueenEngine"));
        _queenEngine = nil;
    }
}

/**
 * @brief 本地伴奏播放状态回调
 * @param playState 当前播放状态
 * @param errorCode 播放错误码
 */
- (void)onAudioPlayingStateChanged:(AliRtcAudioPlayingStateCode)playState errorCode:(AliRtcAudioPlayingErrorCode)errorCode {
    ;
}

/**
 * @brief 远端用户伴奏播放开始回调
 * @param uid 用户uid
*/
- (void)onRemoteAudioAccompanyStarted:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 远端用户伴奏播放结束回调
 * @param uid 用户uid
*/
- (void)onRemoteAudioAccompanyFinished:(NSString *_Nonnull)uid {
    ;
}

/**
 * @brief 本地音效播放结束回调
 * @param soundId 用户给该音效文件分配的ID
*/
- (void)onAudioEffectFinished:(int)soundId {
    ;
}

/**
 * @brief 网络质量探测回调
 * @param networkQuality 网络质量
 */
- (void)onLastmileDetectResultWithQuality:(AliRtcNetworkQuality)networkQuality {
    ;
}

/**
 * @brief 文件录制回调事件
 * @param event 录制事件
 * @param filePath 录制文件路径
 */
- (void)onMediaRecordEvent:(int)event filePath:(NSString *_Nullable)filePath {
    ;
}

/**
 * @brief 实时数据回调(2s触发一次)
 * @param stats stats
 */
- (void)onRtcStats:(AliRtcStats)stats {
    [self.delegate onRTCEngineTotalStatistics:@{
        @"channel_id": self.config.conferenceID ?: @"none",
        @"app_cpu": [NSString stringWithFormat:@"%f", stats.cpu_usage],
        @"call_duration":[NSString stringWithFormat:@"%lld", stats.call_duration],
        @"lastmile_delay":[NSString stringWithFormat:@"%lld", stats.lastmile_delay],
        @"rcvd_bytes":[NSString stringWithFormat:@"%lld", stats.rcvd_bytes],
        @"rcvd_expected_pkts":[NSString stringWithFormat:@"%lld", stats.rcvd_expected_pkts],
        @"rcvd_k_bitrate":[NSString stringWithFormat:@"%lld", stats.rcvd_kbitrate],
        @"rcvd_loss_pkts":[NSString stringWithFormat:@"%lld", stats.rcvd_loss_pkts],
        @"rcvd_loss_rate":[NSString stringWithFormat:@"%lld", stats.rcvd_loss_rate],
        @"sent_bytes":[NSString stringWithFormat:@"%lld", stats.sent_bytes],
        @"sent_expected_pkts":[NSString stringWithFormat:@"%lld", stats.sent_expected_pkts],
        @"sent_k_bitrate":[NSString stringWithFormat:@"%lld", stats.sent_kbitrate],
        @"sent_loss_pkts":[NSString stringWithFormat:@"%lld", stats.sent_loss_pkts],
        @"sent_loss_rate":[NSString stringWithFormat:@"%lld", stats.sent_loss_rate],
        @"system_cpu":[NSString stringWithFormat:@"%f", stats.systemCpuUsage],
        @"video_rcvd_k_bitrate":[NSString stringWithFormat:@"%lld", stats.video_rcvd_kbitrate],
        @"video_sent_k_bitrate":[NSString stringWithFormat:@"%lld", stats.video_sent_kbitrate],
    }];
}

/**
 * @brief 本地视频统计信息(2s触发一次)
 * @param localVideoStats 本地视频统计信息
 * @note SDK每两秒触发一次此统计信息回调
 */
- (void)onRtcLocalVideoStats:(AliRtcLocalVideoStats *_Nonnull)localVideoStats {
    NSString* track = @"no";
    if (localVideoStats.track == AliRtcVideoTrackBoth) {
        track = @"both";
    } else if (localVideoStats.track == AliRtcVideoTrackCamera) {
        track = @"camera";
    } else if (localVideoStats.track == AliRtcVideoTrackScreen) {
        track = @"screen";
    }
    if ([self.delegate respondsToSelector:@selector(onRTCEngineLocalVideoStatistics:)]) {
        [self.delegate onRTCEngineLocalVideoStatistics:@{
            @"channel_id":self.config.conferenceID ?: @"none",
            @"encode_fps":[NSString stringWithFormat:@"%u", localVideoStats.encode_fps],
            @"sent_bitrate":[NSString stringWithFormat:@"%u", localVideoStats.sent_bitrate],
            @"sent_fps":[NSString stringWithFormat:@"%u", localVideoStats.sent_fps],
            @"source_type":track
        }];
    }
}

/**
 * @brief 远端视频统计信息(2s触发一次)
 * @param remoteVideoStats 远端视频统计信息
 */
- (void)onRtcRemoteVideoStats:(AliRtcRemoteVideoStats *_Nonnull)remoteVideoStats {
    
    NSString* track = @"no";
    if (remoteVideoStats.track == AliRtcVideoTrackBoth) {
        track = @"both";
    } else if (remoteVideoStats.track == AliRtcVideoTrackCamera) {
        track = @"camera";
    } else if (remoteVideoStats.track == AliRtcVideoTrackScreen) {
        track = @"screen";
    }
    
    [self.delegate onRTCEngineRemoteVideoStatistics:@{
        @"channel_id": self.config.conferenceID ?: @"none",
        @"decode_fps": [NSString stringWithFormat:@"%u", remoteVideoStats.decode_fps],
        @"frozen_times":[NSString stringWithFormat:@"%u", remoteVideoStats.frozen_times],
        @"height":[NSString stringWithFormat:@"%u", remoteVideoStats.height],
        @"render_fps":[NSString stringWithFormat:@"%u", remoteVideoStats.render_fps],
        @"source_type": track,
        @"target_uid":[NSString stringWithFormat:@"%@", remoteVideoStats.userId],
        @"width":[NSString stringWithFormat:@"%u", remoteVideoStats.width],
    }];
}

/**
 * @brief 本地音频统计信息(2s触发一次)
 * @param localAudioStats 本地视频统计信息
 * @note SDK每两秒触发一次此统计信息回调
 */
- (void)onRtcLocalAudioStats:(AliRtcLocalAudioStats *_Nonnull)localAudioStats {
    if ([self.delegate respondsToSelector:@selector(onRTCEngineLocalAudioStatistics:)]) {
        [self.delegate onRTCEngineLocalAudioStatistics:@{
            @"channel_id":self.config.conferenceID ?: @"none",
//            @"input_level":[NSString stringWithFormat:@"%u", localAudioStats.input_level],
            @"num_channel":[NSString stringWithFormat:@"%u", localAudioStats.num_channel],
            @"sent_bitrate":[NSString stringWithFormat:@"%u", localAudioStats.sent_bitrate],
            @"sent_samplerate":[NSString stringWithFormat:@"%u", localAudioStats.sent_samplerate],
            @"source_type":localAudioStats.track == AliRtcAudioTrackMic ? @"mic" : @"no",
        }];
    }
}

/**
 * @brief 远端音频统计信息(2s触发一次)
 * @param remoteAudioStats 远端音频统计信息
 */
- (void)onRtcRemoteAudioStats:(AliRtcRemoteAudioStats *_Nonnull)remoteAudioStats {
    [self.delegate onRTCEngineRemoteAudioStatistics:@{
        @"channel_id": self.config.conferenceID ?: @"none",
        @"audio_loss_rate":[NSString stringWithFormat:@"%u", remoteAudioStats.audio_loss_rate],
        @"jitter_buffer_delay":[NSString stringWithFormat:@"%u", remoteAudioStats.jitter_buffer_delay],
        @"network_transport_delay":[NSString stringWithFormat:@"%u", remoteAudioStats.network_transport_delay],
        @"quality":[NSString stringWithFormat:@"%u", remoteAudioStats.quality],
        @"rcvd_bitrate":[NSString stringWithFormat:@"%u", remoteAudioStats.rcvd_bitrate],
        @"source_type": remoteAudioStats.track == AliRtcAudioTrackMic ? @"mic" : @"no",
        @"target_uid":[NSString stringWithFormat:@"%@", remoteAudioStats.userId],
        @"total_frozen_times":[NSString stringWithFormat:@"%u", remoteAudioStats.total_frozen_times]
    }];
}

/**
 * @brief 收到媒体扩展信息回调
 * @param uid 远端用户uid
 * @param data 媒体扩展信息
 */
- (void)onMediaExtensionMsgReceived:(NSString *_Nonnull)uid message:(NSData *_Nonnull)data {
    ;
}

/**
 * @brief 下行消息通道（接收消息）
 * @param messageInfo message
 */
- (void)onDownlinkMessageNotify:(AliRtcMessage *_Nonnull)messageInfo {
    ;
}

/**
 * @brief 发送上行消息后返回结果
 * @param resultInfo send message result
 */
- (void)onUplinkMessageResponse:(AliRtcMessageResponse *_Nonnull)resultInfo {
    ;
}

/**
 * @brief 语音路由发生变化回调
 * @param routing 当前使用的语音路由
 */
- (void)onAudioRouteChanged:(AliRtcAudioRouteType)routing {
    ;
}

/**
 * @brief 截图回调
 * @param uid 远端用户uid
 * @param videoTrack 截图视频track
 * @param image 截图数据
 * @param success 截图结果
 * @note 成功时image返回截图数据，失败返回nil
 */
- (void)onSnapshotComplete:(NSString*_Nullable)uid videoTrack:(AliRtcVideoTrack)videoTrack image:(UIImage* _Nullable)image success:(BOOL)success {
    ;
}

/**
 * @brief 旁路推流状态改变回调
 * @param streamURL 流地址
 * @param state 推流状态, 参考 {@link AliRtcLiveTranscodingState}
 * @param errCode 错误码, 参考 {@link AliRtcTrascodingLiveStreamErrorCode}
 * @note 该接口用于旁路推流状态改变的回调
 */
- (void)onPublishLiveStreamStateChanged:(NSString *_Nonnull)streamURL state:(AliRtcLiveTranscodingState)state errCode:(AliRtcTrascodingLiveStreamErrorCode)errCode {
    
}

/**
 * @brief 旁路任务状态改变回调
 * @param streamURL  流地址
 * @param state 任务状态, 参考 {@link AliRtcTrascodingLiveTaskStatus}
 * @note 该接口用于旁路任务状态改变的回调
 */
- (void)onPublishTaskStateChanged:(NSString *_Nonnull)streamURL state:(AliRtcTrascodingLiveTaskStatus)state {
    
}

/**
 * @brief 跨频道转推状态变化
 * @param state 当前状态，参考AliRtcChannelRelayState
 * @param code 错误码
 * @param message 状态信息
 */
- (void)onChannelRelayStateChanged:(int)state code:(int)code message:(NSString *_Nullable)message {
    ;
}

/**
 * @brief 跨频道转推事件回调
 * @param event 事件，参考AliRtcChannelRelayState
 */
- (void)onChannelRelayEvent:(int)event {
    ;
}

@end
