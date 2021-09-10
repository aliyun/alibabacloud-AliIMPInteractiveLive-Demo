//
//  AIRBRTCProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannelUserListResponse;
@class AIRBRTCConfig;

@protocol AIRBRTCDelegate <NSObject>
- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info;
- (void) onAIRBRTCRemotePeerViewAvailable:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type;
- (void) onAIRBRTCRemotePeerVideoFirstFrameDrawn:(NSString*)userID type:(AIRBRTCVideoViewType)type;
@end

@protocol AIRBRTCProtocol <NSObject>
@property (strong, nonatomic) UIView* rtcLocalView;
@property (nonatomic, assign) NSInteger previewMirrorMode;         // 本地预览镜像模式：0为只有前置摄像头预览镜像，后置摄像头不镜像；1为全部摄像头镜像；2为全部摄像头不镜像；默认为0
@property (nonatomic, assign) BOOL videoStreamMirrorEnabled;         // 是否开启视频流镜像，默认为否
@property (weak, nonatomic) id<AIRBRTCDelegate> delegate;

//- (void) startLocalPreview;
- (void) joinChannelWithConfig:(AIRBRTCConfig*)config;
- (void) leaveChannel;
- (void) addPeers:(NSArray<NSString*>*)userIDs;
- (void) removePeers:(NSArray<NSString*>*)userIDs;
- (void) approveJoiningApplication:(BOOL)approve fromPeer:(NSString*)userID; //approveRTCJoiningApplication
- (void) acceptCall:(BOOL)accept;
- (void) applyForJoining:(BOOL)applyOrWithdraw;
- (void) queryCurrentPeerListWithType:(AIRBRTCPeerType)type
                                 pageNum:(int32_t)pageNum
                                pageSize:(int32_t)pageSize
                               onSuccess:(void(^)(AIRBRoomChannelUserListResponse* rsp))onSuccess
                               onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) muteLocalMicphone:(BOOL)mute
                 onSuccess:(void(^)(void))onSuccess
                 onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) muteRemoteMicphone:(BOOL)mute remotePeer:(NSString*)userID
                      onSuccess:(void(^)(void))onSuccess
                      onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) muteAllRemoteMicphone:(BOOL)mute
                     onSuccess:(void(^)(void))onSuccess
                     onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) muteLocalCamera:(BOOL)mute
               onSuccess:(void(^)(void))onSuccess
               onFailure:(void(^)(NSString* errorMessage))onFailure;


- (void) subscribeRemoteAudioStream:(BOOL)sub fromUser:(NSString*)userID;

- (void) subscribeRemoteVideoStream:(BOOL)sub type:(AIRBRTCVideoStreamType)type fromUser:(NSString*)userID;

- (void) toggleLocalCamera;

- (void) startPublishingBypassLive;

/**
 * 设置旁路推流的布局
 * @param type 布局样式
 * @param userIDs 要展示的用户ID列表:从左上到右下依次排序
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) setBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type userIDs:(NSArray<NSString*>*) userIDs onSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;

- (void) startRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) pauseRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) resumeRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) stopRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) getRecordedVideoUrlWithConferenceID:(NSString*)conferenceID
                                   onSuccess:(void(^)(NSString* url))onSuccess
                                   onFailure:(void(^)(NSString* error))onFailure;
                              

@end

NS_ASSUME_NONNULL_END
