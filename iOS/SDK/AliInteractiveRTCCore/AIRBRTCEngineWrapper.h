//
//  AIRBRTCEngineWrapper.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRTCEngineConfig, AliRtcUserVolumeInfo;

@protocol AIRBRTCEngineWrapperDelegate <NSObject>

@property (assign, nonatomic) BOOL localCameraOpened;
- (void)queryRTCTokenInfoAndJoinChannel;

- (void)reportJoinChannelSucceeded:(BOOL)Succeeded
                       errorCode:(nonnull NSString *)errorCode;
- (void)reportLeaveChannelSucceeded:(BOOL)Succeeded
                        errorCode:(nonnull NSString *)errorCode;
- (void)onLocalPreviewStarted;
- (void)onRemotePeerCameraViewAvailable:(NSString*)userID view:(UIView*)view;
- (void)onRemotePeerCameraViewUnavailable:(NSString*)userID;
- (void)onRemotePeerScreenShareViewAvailable:(NSString*)userID view:(UIView*)view;
- (void)onRemotePeerScreenShareViewUnavailable:(NSString*)userID;
- (void)onFirstRemoteCameraVideoFrameDrawn:(NSString*)userID;
- (void)onFirstRemoteScreenShareVideoFrameDrawn:(NSString*)userID;
- (void)onRTCEngineNetworkConnectionLost;
- (void)onRTCEngineNetworkReconnectStart;
- (void)onRTCEngineNetworkReconnectSuccess;
- (void)onRTCEngineNetworkConnectFailed;
- (void)onRTCEngineActiveSpeaker:(NSString *_Nonnull)userID;
- (void)onRTCEngineAudioVolumeCallback:(id)volumeInfoArray totalVolume:(int)totalVolume;
- (void)onRTCEngineNetworkQualityChanged:(NSString *_Nonnull)userID
                        upNetworkQuality:(int)upQuality
                      downNetworkQuality:(int)downQuality;
- (void)onRTCEngineError:(int)errorCode message:(NSString*)errorMessage;
- (void) onAIRBRTCEngineErrorWithCode:(NSInteger)code message:(NSString*)msg;

- (void)onRTCEngineLocalVideoStatistics:(NSDictionary*)statistic;
- (void)onRTCEngineLocalAudioStatistics:(NSDictionary*)statistic;
- (void)onRTCEngineRemoteVideoStatistics:(NSDictionary*)statistic;
- (void)onRTCEngineRemoteAudioStatistics:(NSDictionary*)statistic;
- (void)onRTCEngineTotalStatistics:(NSDictionary*)statistic;

- (void)onRTCEngineRemoteUserOnlineNotify:(NSString *)uid userInfo:(NSDictionary*)userInfo;
- (void)onRTCEngineRemoteUserOfflineNotify:(NSString *)uid reason:(NSString*)reason;
- (void)onRTCEngineUserVideoMuted:(NSString *)uid videoMuted:(BOOL)isMute;
- (void)onRTCEngineUserAudioMuted:(NSString *)uid audioMuted:(BOOL)isMute;

@end

@interface AIRBRTCEngineWrapper : NSObject
@property (weak, nonatomic) id<AIRBRTCEngineWrapperDelegate> delegate;
@property (strong, nonatomic) UIView* localView;
//@property (nonatomic, assign) NSInteger previewMirrorMode;         // 本地预览镜像模式：0为只有前置摄像头预览镜像，后置摄像头不镜像；1为全部摄像头镜像；2为全部摄像头不镜像；默认为0
@property (nonatomic, assign) BOOL previewMirrorEnabled;             // 是否开启前置摄像头本地预览镜像，默认为是
@property (nonatomic, assign) BOOL videoStreamMirrorEnabled;         // 是否开启前置摄像头视频流镜像，默认为否
@property (nonatomic, assign) BOOL faceBeautyOn;
@property (weak, nonatomic) id queenEngine;
@property (weak, nonatomic) UIViewController* faceBeautyConfigViewController;


+ (instancetype) createRTCEngine;
//- (void) startPreview;
- (void) joinChannelWithConfig:(AIRBRTCEngineConfig*)config;
- (void) leaveChannel;
- (void) destroyEngine;

- (void) registerLocalVideoTexture:(BOOL)reg;
//- (void) muteLocalCamera:(BOOL)mute;
@end

NS_ASSUME_NONNULL_END
