//
//  AIRBLivePusherEngineWrapper.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLivePusherEngineWrapper.h"

#import <objc/message.h>
#import <AlivcLivePusher/AlivcLivePusher.h>
#import <AliinteractiveRoomBundle/AIRBLivePusherOptions.h>

#define LOG(...) NSLog(__VA_ARGS__)

@interface AIRBLivePusherEngineWrapper() <AlivcLivePusherInfoDelegate, AlivcLivePusherErrorDelegate, AlivcLivePusherCustomFilterDelegate, AlivcLivePusherNetworkDelegate, AlivcLivePusherCustomDetectorDelegate>

@property (strong, nonatomic) AlivcLivePushConfig* pusherEngineConfig;
@property (strong, nonatomic) AlivcLivePusher* pusherEngine;
@property (assign, atomic) BOOL muted;
@property (assign, atomic) BOOL beautyOn; //实时打开或者关闭美颜
@property (assign, atomic) BOOL enableFaceBeauty; //云控配置下是否开启美颜
@property (strong, nonatomic) id faceBeautyManager;
@property (assign, nonatomic) BOOL previewEverStarted; // 表明预览已经打开过了
@property (assign, nonatomic) BOOL pushingEverStarted; // 表明推流已经开始过了
@property (assign, nonatomic) BOOL pushingPaused;
@property (strong, nonatomic) NSLock* faceBeautyLock; //美颜的回调不在同一个线程时需要加锁保证
@property (assign, nonatomic) BOOL streamingPaused;
@property (assign, nonatomic) BOOL isScreenCapture;
@property (copy, nonatomic) NSString* appGroupID;
@end

@implementation AIRBLivePusherEngineWrapper

#pragma -mark Properties

- (UIView*) preview {
    if (!_preview) {
        _preview = [[UIView alloc] initWithFrame:[UIScreen mainScreen].bounds];
        _preview.backgroundColor = [UIColor clearColor];
    }
    return _preview;
}

- (void) setContentMode:(int8_t)contentMode {
    _contentMode = contentMode;
    if (_pusherEngine) {
        [self.pusherEngine setpreviewDisplayMode:contentMode];
    }
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

- (UIViewController*)faceBeautyConfigViewController {
    if (_faceBeautyManager && _queenEngine) {
        return ((UIViewController* (*)(id, SEL))objc_msgSend)(_faceBeautyManager, NSSelectorFromString(@"faceBeautyVC"));
    }
    return nil;
}

+ (instancetype) createPusherEngine {
    AIRBLivePusherEngineWrapper* instance = [[AIRBLivePusherEngineWrapper alloc] init];
    LOG(@"AIRBLivePusherEngineWrapper::createPusherEngine(%p)", instance);
    return instance;
}

#pragma -mark Lifecycle

- (instancetype) init {
    self = [super init];
    if (self) {
        _muted = NO;
        _beautyOn = YES;
        _contentMode = ALIVC_LIVE_PUSHER_PREVIEW_ASPECT_FILL;
        _faceBeautyLock = [[NSLock alloc] init];
        _pusherEngineConfig = [[AlivcLivePushConfig alloc] init];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationWillResignActiveNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationDidBecomeActiveNotification object:nil];
    }

    return self;
}

- (void) dealloc {
    LOG(@"AIRBLivePusherEngineWrapper::dealloc(%p)", self);
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) setupLivePusherEngineWithOptions:(AIRBLivePusherOptions*)options
                                  preview:(UIView*)preview
                              cloudConfig:(NSDictionary*)cloudConfig {
    LOG(@"AIRBLivePusherEngineWrapper::setupLivePusherEngineWithOptions:preview:cloudConfig:");
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (preview) {
            _preview = preview;
        }
        
        [self createLivePushConfigWithOptions:options cloudConfig:cloudConfig];
        [self createPushEngine];
        [self.pusherEngine startPreview:self.preview];
    });
}

- (void) setupLivePusherEngineWithOptions:(AIRBLivePusherOptions*)options
                               appGroupID:(NSString*)appGroupID
                              cloudConfig:(NSDictionary*)cloudConfig {
    LOG(@"AIRBLivePusherEngineWrapper::setupLivePusherEngineWithOptions:appGroupID:cloudConfig");
    
    _isScreenCapture = YES;
    _appGroupID = appGroupID;
    dispatch_async(dispatch_get_main_queue(), ^{
        [self createLivePushConfigWithOptions:options cloudConfig:cloudConfig];
        [self createPushEngine];
        [self.pusherEngine startScreenCapture:_appGroupID];
    });
}

- (void)createLivePushConfigWithOptions:(AIRBLivePusherOptions*)options
                            cloudConfig:(NSDictionary*)cloudConfig {
    if (options.mediaStreamingOptions.alivcLivePushConfig) {
        self.pusherEngineConfig = options.mediaStreamingOptions.alivcLivePushConfig;
    }
    
    self.pusherEngineConfig.previewDisplayMode = options.mediaStreamingOptions.previewDisplayMode;
    
    if (_isScreenCapture) {
        self.pusherEngineConfig.externMainStream = true;
        self.pusherEngineConfig.externVideoFormat = AlivcLivePushVideoFormatYUV420P;
        self.pusherEngineConfig.audioFromExternal = false;
        self.pusherEngineConfig.videoEncoderMode = AlivcLivePushVideoEncoderModeSoft;
    }

//    self.pusherEngineConfig.audioOnly = options.mediaStreamingOptions.audioOnly;
//    self.pusherEngineConfig.videoOnly = options.mediaStreamingOptions.videoOnly;
    self.pusherEngineConfig.orientation = options.mediaStreamingOptions.orientation;
//    self.pusherEngineConfig.cameraType = options.mediaStreamingOptions.cameraType;
//    self.pusherEngineConfig.previewMirror = options.mediaStreamingOptions.previewMirror;
//    self.pusherEngineConfig.pushMirror = options.mediaStreamingOptions.pushMirror;
//    self.pusherEngineConfig.autoFocus = options.mediaStreamingOptions.autoFocus;
//    self.pusherEngineConfig.flash = options.mediaStreamingOptions.flash;
////    self.pusherEngineConfig.previewDisplayMode = options.mediaStreamingOptions.previewDisplayMode;
//
////    @{@"encodeParamAndroid" : @"{\"videoEncodeGop\":2,\"targetVideoBitrate\":1500,\"audioChannels\":2,\"audioProfile\":2,\"fps\":20,\"videoEncodeMode\":0,\"videoEncodeType\":0,\"cameraType\":1,\"audioSamepleRate\":48000}"};
//
//
//    NSString* encodeConfigJsonString = [cloudConfig valueForKey:@"encodeParamiOS"];
//    NSData* encodeConfigData = [encodeConfigJsonString dataUsingEncoding:NSUTF8StringEncoding];
//    NSDictionary* encodeConfigDic = nil;
//    if (encodeConfigData) {
//        encodeConfigDic = [NSJSONSerialization JSONObjectWithData:encodeConfigData options:NSJSONReadingMutableLeaves error:nil];
//    }
//
//    NSString* pushMode = [encodeConfigDic valueForKey:@"pushMode"];
//    if ([pushMode isEqualToString:@"custom"]) {
//        self.pusherEngineConfig.qualityMode = AlivcLivePushQualityModeCustom;
//        int videoBitrate = [[encodeConfigDic valueForKey:@"targetVideoBitrate"] intValue];
//        if (videoBitrate >= 100 && videoBitrate <= 10 * 1000) {
//            self.pusherEngineConfig.enableAutoBitrate = NO;
//            self.pusherEngineConfig.enableAutoResolution = NO;
//            self.pusherEngineConfig.initialVideoBitrate = videoBitrate;
//            self.pusherEngineConfig.minVideoBitrate = videoBitrate;
//            self.pusherEngineConfig.targetVideoBitrate = videoBitrate;
//        }
//
//        int videoFps = [[encodeConfigDic valueForKey:@"fps"] intValue];
//        if (videoFps >= 1 && videoFps <= 100) {
//            self.pusherEngineConfig.fps = videoFps;
//        } else {
//            self.pusherEngineConfig.fps = 20;
//        }
//    } else if ([pushMode isEqualToString:@"resolutionFirst"]) {
//        self.pusherEngineConfig.qualityMode = AlivcLivePushQualityModeResolutionFirst;
//    } else {
//        self.pusherEngineConfig.qualityMode = AlivcLivePushQualityModeFluencyFirst;
//    }
//
//    int audioChannel = [[encodeConfigDic valueForKey:@"audioChannels"] intValue];
//    if (audioChannel == 1) {
//        self.pusherEngineConfig.audioChannel = AlivcLivePushAudioChannel_1;
//    } else {
//        self.pusherEngineConfig.audioChannel = AlivcLivePushAudioChannel_2;
//    }
//
//    int audioSampleRate = [[encodeConfigDic valueForKey:@"audioSamepleRate"] intValue];
//    if (audioSampleRate == 32000) {
//        self.pusherEngineConfig.audioSampleRate = AlivcLivePushAudioSampleRate32000;
//    } else if (audioSampleRate == 44100){
//        self.pusherEngineConfig.audioSampleRate = AlivcLivePushAudioSampleRate44100;
//    } else {
//        self.pusherEngineConfig.audioSampleRate = AlivcLivePushAudioSampleRate48000;
//    }
//
//    int audioProfile = [[encodeConfigDic valueForKey:@"audioProfile"] intValue];
//    if (audioProfile == 2 || audioProfile == 5 || audioProfile == 29 || audioProfile == 23) {
//        self.pusherEngineConfig.audioEncoderProfile = audioProfile;
//    } else {
//        self.pusherEngineConfig.audioEncoderProfile = AlivcLivePushAudioEncoderProfile_AAC_LC;
//    }
//
//    int videoGOP = [[encodeConfigDic valueForKey:@"videoEncodeGop"] intValue];
//    if (videoGOP >= 1 && videoGOP <= 5) {
//        self.pusherEngineConfig.videoEncodeGop = videoGOP;
//    } else {
//        self.pusherEngineConfig.videoEncodeGop = 2;
//    }
//
//    //    @"visibleConfig" : @"{\"maxPushStreamResolve\": \"720P\",\"enableBeautify\": true,\"enableRTS\": true,\"maxMessageCharacter\": 1000,\"sendMessageFrequency\": 1,\"maxLinkMicCount\": 20}"
//    NSString* visibleConfigJsonString = [cloudConfig valueForKey:@"visibleConfig"];
//    NSData* visibleConfigData = [visibleConfigJsonString dataUsingEncoding:NSUTF8StringEncoding];
//    NSDictionary* visibleConfigDic = nil;
//    if (visibleConfigData) {
//        visibleConfigDic = [NSJSONSerialization JSONObjectWithData:visibleConfigData options:NSJSONReadingMutableLeaves error:nil];
//    }
//
//    NSString* resolution = @"720P";
//    if ([visibleConfigDic valueForKey:@"maxPushStreamResolve"]) {
//        resolution = [visibleConfigDic valueForKey:@"maxPushStreamResolve"];
//    }
//    if ([resolution isEqualToString:@"1080P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution1080P;
//    } else if ([resolution isEqualToString:@"720P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution720P;
//    } else if ([resolution isEqualToString:@"540P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution540P;
//    } else if ([resolution isEqualToString:@"480P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution480P;
//    } else if ([resolution isEqualToString:@"360P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution360P;
//    } else if ([resolution isEqualToString:@"240P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution240P;
//    } else if ([resolution isEqualToString:@"180P"]) {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution180P;
//    } else {
//        self.pusherEngineConfig.resolution = AlivcLivePushResolution720P;
//    }
//
//    if ([visibleConfigDic valueForKey:@"enableBeautify"]) {
//        self.enableFaceBeauty = [[visibleConfigDic valueForKey:@"enableBeautify"] boolValue];
//    }

    self.pusherEngineConfig.businessInfo = @{@"traceId" : @"channel_aliyun_solution"};
}

- (void) createPushEngine {

    //这句强制需要主线程
    self.pusherEngine = [[AlivcLivePusher alloc] initWithConfig:self.pusherEngineConfig];
    [self.pusherEngine setLogLevel:AlivcLivePushLogLevelAll];
    
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *documentsDirectory = [paths firstObject];
    [self.pusherEngine setLogPath:documentsDirectory maxPartFileSizeInKB:1024];
    
    LOG(@"AIRBLivePusherEngineWrapper::create pusher(%p,version:%@)", self.pusherEngine, [AlivcLivePusher getSDKVersion]);
    
    [self.pusherEngine setErrorDelegate:self];
    [self.pusherEngine setInfoDelegate:self];
    [self.pusherEngine setNetworkDelegate:self];
    [self.pusherEngine setCustomFilterDelegate:self];
    [self.pusherEngine setCustomDetectorDelegate:self];
}

- (void) startLiveStreaming {
    LOG(@"AIRBLivePusherEngineWrapper::startLiveStreaming(%@)", self.mediaURL);
    
    // 4.4.1版本的推流SDK需要在主线程startPush，要么内部的埋点上报timer不启动
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [weakSelf.pusherEngine startPushWithURL:weakSelf.mediaURL];
    });
}

- (void) restartLiveStreaming {
    LOG(@"AIRBLivePusherEngineWrapper::restartLiveStreaming(%@)", self.mediaURL);
    [self.pusherEngine reconnectPushAsync];
}

- (void) pauseLiveStreaming {
    LOG(@"AIRBLivePusherEngineWrapper::pauseLiveStreaming");
    
    if (_isScreenCapture) {
        ;
    } else {
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            if (weakSelf.pushingEverStarted) {
                [weakSelf.pusherEngine stopPush];
                weakSelf.pushingPaused = YES;
            }
        });
    }
}

- (void) resumeLiveStreaming {
    LOG(@"AIRBLivePusherEngineWrapper::resumeLiveStreaming");
    if (_isScreenCapture) {
        ;
    } else {
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            if (weakSelf.pushingPaused && [UIApplication sharedApplication].applicationState == UIApplicationStateActive) {
                if (weakSelf.pushingEverStarted) {
                    [weakSelf.pusherEngine startPushWithURL:self.mediaURL];
                }
                weakSelf.pushingPaused = NO;
            }
        });
    }
}

- (void) stopLiveStreaming {
    LOG(@"AIRBLivePusherEngineWrapper::stopLiveStreaming");
        
    [self.pusherEngine stopPush];
    [self.pusherEngine destory];
    self.pusherEngine = nil;
}

- (void) toggleMuted {
    LOG(@"AIRBLivePusherEngineWrapper::toggleMuted");
    self.muted = (!self.muted);
    [self.pusherEngine setMute:self.muted];
}

- (void) toggleCamera {
    LOG(@"AIRBLivePusherEngineWrapper::toggleCamera");
    int result = [self.pusherEngine switchCamera];
    if (!result) {
        self.pusherEngineConfig.cameraType = (self.pusherEngineConfig.cameraType == AlivcLivePushCameraTypeBack ? AlivcLivePushCameraTypeFront : AlivcLivePushCameraTypeBack);
    }
}

- (void) setPreviewMirror:(BOOL)mirror {
    LOG(@"AIRBLivePusherEngineWrapper::setPreviewMirror(%d)", mirror);
    [self.pusherEngine setPreviewMirror:mirror];
    self.pusherEngineConfig.previewMirror = mirror;
}

- (void) setStreamingVideoMirror:(BOOL)mirror {
    LOG(@"AIRBLivePusherEngineWrapper::setStreamingVideoMirror(%d)", mirror);
    [self.pusherEngine setPushMirror:mirror];
    self.pusherEngineConfig.pushMirror = mirror;
}

- (void) toggleLiveFaceBeauty {
    LOG(@"AIRBLivePusherEngineWrapper::toggleLiveFaceBeauty");
    self.beautyOn = !self.beautyOn;
}

- (void)setFlash:(BOOL)open {
    LOG(@"AIRBLivePusherEngineWrapper::setFlash:%d", open);
    [self.pusherEngine setFlash:open];
    self.pusherEngineConfig.flash = open;
}

- (int)setZoom:(float)zoom {
    LOG(@"AIRBLivePusherEngineWrapper::setZoom:%f.", zoom);
    return [self.pusherEngine setZoom:zoom];
}

- (float)getMaxZoom {
    LOG(@"AIRBLivePusherEngineWrapper::getMaxZoom.");
    return [self.pusherEngine getMaxZoom];
}

- (float)getCurrentZoom {
    LOG(@"AIRBLivePusherEngineWrapper::getCurrentZoom.");
    return [self.pusherEngine getCurrentZoom];
}

- (int)focusCameraAtAdjustedPoint:(CGPoint)point autoFocus:(bool)autoFocus {
    LOG(@"AIRBLivePusherEngineWrapper::focusCameraAtAdjustedPoint.");
    return [self.pusherEngine focusCameraAtAdjustedPoint:point autoFocus:autoFocus];
}

- (int)setExposure:(float)exposure {
    LOG(@"AIRBLivePusherEngineWrapper::setZoom:%f.", exposure);
    return [self.pusherEngine setExposure:exposure];
}

- (float)getCurrentExposure {
    LOG(@"AIRBLivePusherEngineWrapper::getCurrentExposure.");
    return [self.pusherEngine getCurrentExposure];
}

- (float)getSupportedMinExposure {
    LOG(@"AIRBLivePusherEngineWrapper::getSupportedMinExposure.");
    return [self.pusherEngine getSupportedMinExposure];
}

- (float)getSupportedMaxExposure {
    LOG(@"AIRBLivePusherEngineWrapper::getSupportedMaxExposure.");
    return [self.pusherEngine getSupportedMaxExposure];
}

#pragma mark - AlivcLivePusherInfoDelegate
- (void)onPreviewStarted:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPreviewStarted");
    
    if (!_previewEverStarted) {
        [self.delegate onLivePusherPreviewStarted]; //事件只上报一次
    }
    _previewEverStarted = YES;
}
- (void)onPreviewStoped:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPreviewStoped");
}
- (void)onFirstFramePreviewed:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onFirstFramePreviewed");
}
- (void)onPushStarted:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushStarted");
        
    if (!_pushingEverStarted) {
        
        NSString* aac_profile = @"AAC-LC";
        if (self.pusherEngineConfig.audioEncoderProfile == 5) {
            aac_profile = @"AAC-HE";
        } else if (self.pusherEngineConfig.audioEncoderProfile == 29) {
            aac_profile = @"AAC-HEv2";
        } else if (self.pusherEngineConfig.audioEncoderProfile == 23) {
            aac_profile = @"AAC-LD";
        }
        
        [self.delegate onLivePuhserStreamingStartedWithMediaInfo:@{
            @"video" : @{
                @"codec" : self.pusherEngineConfig.videoHardEncoderCodec == AlivcLivePushVideoEncoderModeHardCodecH264 ? @"H264" : @"H265",
                @"encoder" : self.pusherEngineConfig.videoEncoderMode == AlivcLivePushVideoEncoderModeHard ? @"VTB" : @"OpenH264",
                @"width" : [NSString stringWithFormat:@"%.0f", [self.pusherEngineConfig getPushResolution].width],
                @"height" : [NSString stringWithFormat:@"%.0f", [self.pusherEngineConfig getPushResolution].height],
                @"gop_size" : [NSString stringWithFormat:@"%d",[self.pusherEngine getLivePushStatusInfo].videoGop],
            },
            @"audio" : @{
                @"codec" : @"AAC",
                @"encoder" : self.pusherEngineConfig.audioEncoderMode == AlivcLivePushAudioEncoderModeHard ? @"AudioUnit" : @"FAAC",
                @"sample" : [NSString stringWithFormat:@"%d",[self.pusherEngine getLivePushStatusInfo].audioSampleRate],
                @"channel" : [self.pusherEngine getLivePushStatusInfo].channel == 1 ? @"Mono" : @"Stereo",
                @"profile" : aac_profile
            }
        }]; //事件只上报一次
    }
    _pushingPaused = NO;
    _pushingEverStarted = YES;
}

- (void)onPushPaused:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushPaused");
}
 
- (void)onPushResumed:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushResumed");
}

- (void)onPushRestart:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushRestart");
    self.streamingPaused = NO;
    if ([self.delegate respondsToSelector:@selector(onLivePuhserStreamingRestarted)]) {
        [self.delegate onLivePuhserStreamingRestarted];
    }
}

- (void)onPushStoped:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushStoped");
    [self.delegate onLivePuhserStreamingStopped];
}

- (void)onPushStatistics:(AlivcLivePusher *)pusher statsInfo:(AlivcLivePushStatsInfo*)statistics {
    [self.delegate onLivePusherStreamingStatistics:@{
        @"screen_width": [NSString stringWithFormat:@"%d", statistics.videoWidth],
        @"screen_height":[NSString stringWithFormat:@"%lu", statistics.videoHeight],
        @"screen_sent_bitrate":[NSString stringWithFormat:@"%d", statistics.videoUploadBitrate],
        @"screen_sent_fps":[NSString stringWithFormat:@"%d", statistics.videoUploadFps],
        @"screen_encode_fps":[NSString stringWithFormat:@"%d", statistics.videoEncodedFps],
        @"screen_capture_fps":[NSString stringWithFormat:@"%d", statistics.videoCaptureFps],
        @"audio_sent_bitrate":[NSString stringWithFormat:@"%d", statistics.audioUploadBitrate],
        @"audio_encode_bitrate":[NSString stringWithFormat:@"%d", statistics.audioEncodedBitrate / 1000],
    }];
}

#pragma mark - AlivcLivePusherErrorDelegate
- (void)onSystemError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    LOG(@"AIRBLivePusherEngineWrapper::onSystemError(%ld,%@)", (long)error.errorCode, error.errorDescription);
    if ([self.delegate respondsToSelector:@selector(onLivePusherErrorWithCode:message:)]) {
        [self.delegate onLivePusherErrorWithCode:0x202 message:error.errorDescription];
    }
}

- (void)onSDKError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    LOG(@"AIRBLivePusherEngineWrapper::onSDKError(%ld,%@)", (long)error.errorCode, error.errorDescription);
    if ([self.delegate respondsToSelector:@selector(onLivePusherErrorWithCode:message:)]) {
        if ([error.errorDescription isEqualToString:@"capture camera open failed."]) {
            [self.delegate onLivePusherErrorWithCode:0x203 message:error.errorDescription];
        } else if ([error.errorDescription isEqualToString:@"capture mic open failed."]) {
            [self.delegate onLivePusherErrorWithCode:0x207 message:error.errorDescription];
        }
    }
}

#pragma mark - AlivcLivePusherNetworkDelegate
- (void) onNetworkPoor:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onNetworkPoor");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingNetworkPoor)]) {
        [self.delegate onLivePusherStreamingNetworkPoor];
    }
}

- (void) onConnectRecovery:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onConnectRecovery");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingNetworkRecoveried)]) {
        [self.delegate onLivePusherStreamingNetworkRecoveried];
    }
}

- (void) onConnectionLost:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onConnectionLost");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingNetworkConnectionLost)]) {
        [self.delegate onLivePusherStreamingNetworkConnectionLost];
    }
}

- (void) onConnectFail:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    LOG(@"AIRBLivePusherEngineWrapper::onConnectFailWithError %ld", (long)error.errorCode);
    if ([self.delegate respondsToSelector:@selector(onLivePusherErrorWithCode:message:)]) {
        [self.delegate onLivePusherErrorWithCode:(int32_t)error.errorCode message:[NSString stringWithFormat:@"onConnectFail(%@)", error.errorDescription]];
    }
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingConnectFailed)]) {
        [self.delegate onLivePusherStreamingConnectFailed];
    }
}

- (void) onReconnectError:(AlivcLivePusher *)pusher error:(AlivcLivePushError *)error {
    LOG(@"AIRBLivePusherEngineWrapper::onReconnectError");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingReconnectSuccess:)]) {
        [self.delegate onLivePusherStreamingReconnectSuccess:NO];
    }
}

- (void) onSendDataTimeout:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onSendDataTimeout");
//    if ([self.delegate respondsToSelector:@selector(onLivePusherErrorWithCode:message:object:)]) {
//        [self.delegate onLivePusherErrorWithCode:AIRBRoomChannelFailedToSendOutData message:nil object:self];
}


- (NSString *)onPushURLAuthenticationOverdue:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onPushURLAuthenticationOverdue");
    return @"";
}


- (void)onReconnectStart:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onReconnectStart");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingReconnectStart)]) {
        [self.delegate onLivePusherStreamingReconnectStart];
    }
}


- (void)onReconnectSuccess:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onReconnectSuccess");
    if ([self.delegate respondsToSelector:@selector(onLivePusherStreamingReconnectSuccess:)]) {
        [self.delegate onLivePusherStreamingReconnectSuccess:YES];
    }
}

- (void)onSendSeiMessage:(AlivcLivePusher *)pusher {
    LOG(@"AIRBLivePusherEngineWrapper::onSendSeiMessage");
}

#pragma mark - AlivcLivePusherCustomFilterDelegate
/**
外置美颜滤镜创建回调
*/
- (void)onCreate:(AlivcLivePusher *)pusher context:(void*)context
{
    LOG(@"AIRBLivePusherEngineWrapper::AlivcLibBeautyManager onCreate");
//    if (self.enableFaceBeauty) {
        ((void (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"setupQueenEngine"));
        _queenEngine = ((id (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"queenEngine"));
//    }
}

/**
 通知外置滤镜销毁回调
 */
- (void)onDestory:(AlivcLivePusher*)pusher
{
    LOG(@"AIRBLivePusherEngineWrapper::AlivcLibBeautyManager onDestory");
    [_faceBeautyLock lock];
    if (_queenEngine) {
        ((void (*)(id, SEL))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"destroyQueenEngine"));
    }
    [_faceBeautyLock unlock];
}

/**
 通知外置滤镜处理回调
 */
- (int)onProcess:(AlivcLivePusher *)pusher texture:(int)texture textureWidth:(int)width textureHeight:(int)height extra:(long)extra
{
    if (self.beautyOn && _queenEngine) {
        return ((int (*)(id, SEL, int, int, int, long))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"processTexture:textureWidth:textureHeight:extra:"), texture, width, height, extra);
    }
    return texture;
}

#pragma mark - AlivcLivePusherCustomDetectorDelegate
/**
 通知外置视频检测创建回调
 */
- (void)onCreateDetector:(AlivcLivePusher *)pusher
{
}
/**
 通知外置视频检测处理回调
 */
- (long)onDetectorProcess:(AlivcLivePusher*)pusher data:(long)data w:(int)w h:(int)h rotation:(int)rotation format:(int)format extra:(long)extra
{
    
    if (self.beautyOn && _queenEngine) {
        [_faceBeautyLock lock];
        ((void (*)(id, SEL, long, int, int, int, int, extra))objc_msgSend)(self.faceBeautyManager, NSSelectorFromString(@"updateInputDataAndRunAlg:w:h:rotation:format:extra:"), data, w, h, rotation, format, extra);
        [_faceBeautyLock unlock];
    }
    
    return data;
}

/**
 通知外置视频检测销毁回调
 */
- (void)onDestoryDetector:(AlivcLivePusher *)pusher
{
    
}

- (void)systemActiveStateChange:(NSNotification *) note {
    if (!_isScreenCapture) {
        if (note.name == UIApplicationDidBecomeActiveNotification) {
            LOG(@"AIRBLivePusherEngineWrapper::UIApplicationDidBecomeActiveNotification restart push");
            [self createPushEngine];
            if (_previewEverStarted) {
                [self.pusherEngine startPreview:self.preview];
                [self.pusherEngine setMute:self.muted];
                if (_pushingEverStarted && !_pushingPaused) {
                    [self.pusherEngine startPushWithURL:self.mediaURL];
                }
            }
        } else if (note.name == UIApplicationWillResignActiveNotification) {
            LOG(@"AIRBLivePusherEngineWrapper::UIApplicationWillResignActiveNotification stopPush");
            if (_pushingEverStarted) {
                [self.pusherEngine stopPush];
            }
            [self.pusherEngine destory];
            self.pusherEngine = nil;
        }
    }
}
@end
