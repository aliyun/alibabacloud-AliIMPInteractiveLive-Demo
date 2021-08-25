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

@protocol AIRBRTCDelegate <NSObject>
- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info;
- (void) onAIRBRTCRemotePeerViewAvailable:(NSString*)userID view:(UIView*)view;
@end

@protocol AIRBRTCProtocol <NSObject>
@property (strong, nonatomic) UIView* rtcLocalView;
@property (weak, nonatomic) id<AIRBRTCDelegate> delegate;

- (void) startLocalPreview;
- (void) joinChannel;
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

- (void) toggleLocalCamera;

- (void) startPublishingBypassLive;

- (void) startRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) pauseRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) resumeRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) stopRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;
- (void) getRecordedVideoUrlWithConferenceID:(NSString*)conferenceID
                                   onSuccess:(void(^)(NSString* url))onSuccess
                                   onFailure:(void(^)(NSString* error))onFailure;
                              

@end

NS_ASSUME_NONNULL_END
