//
//  AIRBRoomChannelProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AIRBLivePlayerProtocol.h"
#import "AIRBLivePusherProtocol.h"
#import "AIRBRTCProtocol.h"
#import "AIRBChatProtocol.h"
#import "AIRBWhiteBoardProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;
@class AIRBRoomChannelUserListResponse;

@protocol AIRBRoomChannelDelegate <NSObject>

@optional
- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info;
- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message;
@end

@protocol AIRBRoomChannelProtocol <NSObject>

@property (weak, nonatomic) id<AIRBRoomChannelDelegate> delegate;

@property (strong, nonatomic) id<AIRBChatProtocol> chat;
@property (strong, nonatomic) id<AIRBLivePusherProtocol> livePusher;
@property (strong, nonatomic) id<AIRBLivePlayerProtocol> livePlayer;
@property (strong, nonatomic) id<AIRBRTCProtocol> rtc;
@property (strong, nonatomic) id<AIRBWhiteBoardProtocol> whiteboard;

- (void) enterRoom;
- (void) leaveRoom;

- (void) updateRoomTitle:(nonnull NSString*)title
               onSuccess:(void (^)(void))onSuccess
               onFailure:(void (^)(NSString* errorMessage))onFailure;

- (void) updateRoomNotice:(nonnull NSString*)notice
               onSuccess:(void (^)(void))onSuccess
               onFailure:(void (^)(NSString* errorMessage))onFailure;

- (void) getRoomUserListWithPageNum:(int32_t)pageNum
                           pageSize:(int32_t)pageSize
                          onSuccess:(void (^)(AIRBRoomChannelUserListResponse * _Nonnull response))onSuccess
                          onFailure:(void (^)(NSString* errorMessage))onFailure;

- (void) kickRoomUserWithUserID:(nonnull NSString*)kickUserID
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;
@end

NS_ASSUME_NONNULL_END
