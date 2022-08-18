//
//  AIRBChat.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBChat.h"

#import <vpaassdk/room/VPROOMRoomNotificationModel.h>
#import <vpaassdk/chat/VPCHATChatModule.h>
#import <vpaassdk/chat/VPCHATChatRpcInterface.h>

#import "AliInteractiveRoomLogger.h"
#import "AIRBRoomChannel.h"
#import "AIRBRoomChannelCommentsResponse.h"
#import "AIRBRoomChannelComment.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"


@interface AIRBChat()
@property (copy, nonatomic) NSString* userID;
@property (strong, nonatomic) VPCHATChatModule* chatModule;
@property (strong, nonatomic) VPCHATChatRpcInterface* chatRpcInterface;
@property (strong, nonatomic) NSTimer* sendingLikeTimer;
@property (assign, nonatomic) int unsentLikeCount;
@property (strong, nonatomic) NSLock* timerLock;
@property (strong, nonatomic) NSLock* unsentLikeCountLock;
@property (strong, nonatomic) NSDate* lastCommentSentDate;
@property (assign, atomic) NSTimeInterval minTimeIntervalOfSendComment;
@property (assign, atomic) BOOL commentSettingGotten;
@end

@implementation AIRBChat

@synthesize room = _room;

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
        _chatModule = [VPCHATChatModule getModule:_userID];
        _chatRpcInterface = [_chatModule getRpcInterface];
        _timerLock = [[NSLock alloc] init];
        _unsentLikeCountLock = [[NSLock alloc] init];
        _minTimeIntervalOfSendComment = 1.0; // 默认1秒
        _commentSettingGotten = NO;
    }
    return self;
}

- (void) dealloc {
    LOGD("AIRBChat::AIRBChat(%p) dealloc", self);
}

- (void) destroy {
    if (_sendingLikeTimer) {
        [_sendingLikeTimer invalidate];
        _sendingLikeTimer = nil;
    }
}

- (void) getCurrentChatInfoOnSuccess:(void (^)(NSDictionary * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    if (self.chatID.length > 0) {
        [self.chatRpcInterface getTopicInfoWithBlock:[VPCHATGetTopicInfoReq VPCHATGetTopicInfoReqWithTopicId:self.chatID] onSuccess:^(VPCHATGetTopicInfoRsp * _Nonnull rsp) {
            self.minTimeIntervalOfSendComment = (rsp.sendCommentInterval > 0 ? rsp.sendCommentInterval : 1000) * 1.000 / 1000.000;
            self.commentSettingGotten = YES;
            onSuccess(@{
                @"total_like" : @(rsp.likeCount),
                @"total_comment" : @(rsp.commentCount),
                @"ban" : @(rsp.mute),
                @"ban_all" : @(rsp.muteAll)
                      });
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBChat::failed to getCurrentChatInfo when error(%d, %@)", error.code, error.reason);
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    }
}

- (void) sendComment:(NSString *)comment onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(AIRBErrorCode, NSString * _Nonnull))onFailure {
    [self sendComment:comment extension:@{} onSuccess:onSuccess onFailure:onFailure];
}

- (void) sendComment:(NSString*)comment
           extension:(NSDictionary<NSString*, NSString*>*)extension
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(AIRBErrorCode code, NSString* message))onFailure {
    
    if (self.lastCommentSentDate && [self.lastCommentSentDate timeIntervalSinceNow] * -1 < self.minTimeIntervalOfSendComment) {
        onFailure(AIRBChatCommentSentFrequencyExceedsLimit, [NSString stringWithFormat:@"comment sent frequency exceeds %f", 1.0 / self.minTimeIntervalOfSendComment]);
        return;
    }

//    if (comment.length > self.maxCommentLength) {
//        onFailure(AIRBChatCommentLengthExceedsLimit, [NSString stringWithFormat:@"comment length exceeds %d", self.maxCommentLength]);
//        return;
//    }
    
    self.lastCommentSentDate = [NSDate date];
    
    if (!self.commentSettingGotten) {
        [self.chatRpcInterface getTopicInfoWithBlock:[VPCHATGetTopicInfoReq VPCHATGetTopicInfoReqWithTopicId:self.chatID] onSuccess:^(VPCHATGetTopicInfoRsp * _Nonnull rsp) {
            self.minTimeIntervalOfSendComment = (rsp.sendCommentInterval > 0 ? rsp.sendCommentInterval : 1000) * 1.000 / 1000.000;
            self.commentSettingGotten = YES;
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBChat::failed to getCurrentChatInfo when error(%d, %@)", error.code, error.reason);
        }];
    }
    
    if (self.chatID) {
        LOGD("AIRBChat::send message(%@) with chatID:(%@) %@", comment, self.chatID, self.chatRpcInterface);
        [self.chatRpcInterface sendCommentWithBlock:[VPCHATSendCommentReq VPCHATSendCommentReqWithTopicId:self.chatID content:comment extension:extension] onSuccess:^(VPCHATSendCommentRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(AIRBChatCommentSentInnerError, ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(AIRBChatCommentSentInvalidChatID, [NSString stringWithFormat:@"You can't send message until entered room."]);
    }
}

- (void) sendLike {
    
    [self.unsentLikeCountLock lock];
    self.unsentLikeCount++;
    [self.unsentLikeCountLock unlock];
    
    if (!self.sendingLikeTimer) {
        __weak typeof(self) weakSelf = self;
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf.timerLock lock];
            if (!weakSelf.sendingLikeTimer) {
                weakSelf.sendingLikeTimer = [NSTimer scheduledTimerWithTimeInterval:2.5 target:weakSelf selector:@selector(sendLikeRepeatly) userInfo:nil repeats:YES];
            }
            [weakSelf.timerLock unlock];
        });
    }
}

- (void) sendLikeRepeatly {
    if (self.chatID.length > 0) {
        [self.unsentLikeCountLock lock];
        int unsentLikeCount = self.unsentLikeCount;
        self.unsentLikeCount = 0;
        [self.unsentLikeCountLock unlock];
        if (unsentLikeCount > 0) {
            [self.chatRpcInterface sendLikeWithBlock:[VPCHATSendLikeReq VPCHATSendLikeReqWithTopicId:self.chatID count:unsentLikeCount] onSuccess:^(VPCHATSendLikeRsp * _Nonnull rsp) {
            } onFailure:^(DPSError * _Nonnull error) {
                LOGE("AIRBChat::sendlike failed(%@)", ERR_MSG_FROM_DPSERROR(error));
            }];
        }
    } else {
        LOGE("AIRBChat::sendlike failed(invalid chat id)");
    }
}

- (void) queryCommentsWithSortedType:(AIRBRoomChatCommentsSortedType)sortedType pageNum:(int)pageNum pageSize:(int)pageSize onSuccess:(void (^)(AIRBRoomChannelCommentsResponse * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    if (self.chatID) {
        [self.chatRpcInterface listCommentWithBlock:[VPCHATListCommentReq VPCHATListCommentReqWithTopicId:self.chatID sortType:(int32_t)sortedType pageNum:pageNum pageSize:pageSize]
                                          onSuccess:^(VPCHATListCommentRsp * _Nonnull rsp) {
            
            
            @autoreleasepool {
                onSuccess([self createCommentsResponseWithInnerResponse:rsp]);
            }
        }
                                          onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    }
}

- (void) banCommentWithUserID:(nonnull NSString*)userID
          banTimeInSeconds:(int32_t)seconds
                  onSuccess:(void (^)(void))onSuccess
                  onFailure:(void (^)(NSString* errorMessage))onFailure{
    if (!self.chatID) {
        onFailure([NSString stringWithFormat:@"You can't mute user until entered room."]);
        return;
    }
    
//    if (![self.room.roomOwnerID isEqualToString:self.userID]) {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can mute users."]);
//        return;
//    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkChatMuteUser info:nil];
    
    LOGD("AIRBChat::Mute user(muteUserID:%@) with userID:(%@) %@", userID, self.userID, self.chatRpcInterface);
    [self.chatRpcInterface muteUserWithBlock:[VPCHATMuteUserReq VPCHATMuteUserReqWithTopicId:self.chatID muteUser:userID muteTime:seconds] onSuccess:^(VPCHATMuteUserRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) cancelBanCommentWithUserID:(nonnull NSString*)userID
                    onSuccess:(void (^)(void))onSuccess
                    onFailure:(void (^)(NSString* errorMessage))onFailure{
    if (!self.chatID) {
        onFailure([NSString stringWithFormat:@"You can't unmuted user until entered room."]);
        return;
    }
    
//    if (![self.room.roomOwnerID isEqualToString:self.userID]) {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can unmuted users."]);
//        return;
//    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkChatCancelMuteUser info:nil];
    
    LOGD("AIRBChat::Cancel unmute user(unmuteUserID:%@) with userID:(%@) %@", userID, self.userID, self.chatRpcInterface);
    [self.chatRpcInterface cancelMuteUserWithBlock:[VPCHATCancelMuteUserReq VPCHATCancelMuteUserReqWithTopicId:self.chatID cancelMuteUser:userID] onSuccess:^(VPCHATCancelMuteUserRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) banAllComment:(BOOL)ban
       onSuccess:(void (^)(void))onSuccess
       onFailure:(void (^)(NSString* errorMessage))onFailure{
    if (!self.chatID) {
        onFailure([NSString stringWithFormat:@"You can't mute all users until entered room."]);
        return;
    }
    
//    if (![self.room.roomOwnerID isEqualToString:self.userID]) {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can mute all users."]);
//        return;
//    }
    
    if (ban) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkChatCancelMuteAll info:nil];
    } else {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkChatMuteAll info:nil];
    }
    
    if (ban){  // 全员禁言
        LOGD("AIRBChat::Mute all users %@", self.userID, self.chatRpcInterface);
        [self.chatRpcInterface muteAllWithBlock:[VPCHATMuteAllReq VPCHATMuteAllReqWithTopicId:self.chatID] onSuccess:^(VPCHATMuteAllRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else{ //  取消全员禁言
        LOGD("AIRBChat::Unmute all users with userID:(%@) %@", self.userID, self.chatRpcInterface);
        [self.chatRpcInterface cancelMuteAllWithBlock:[VPCHATCancelMuteAllReq VPCHATCancelMuteAllReqWithTopicId:self.chatID] onSuccess:^(VPCHATCancelMuteAllRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    }
}

- (void) sendCustomMessage:(NSString *)message toUsers:(NSArray *)userList onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    if (!message || !userList) {
        onFailure(@"Invalid input parameters.");
        return;
    }
    
    if ([userList isKindOfClass:[NSArray class]]) {
        VPCHATSendCustomMessageToUsersReq* req = [VPCHATSendCustomMessageToUsersReq VPCHATSendCustomMessageToUsersReqWithTopicId:self.chatID body:message receiverList:userList];
        [self.chatRpcInterface sendCustomMessageToUsersWithBlock:req onSuccess:^(VPCHATSendCustomMessageToUsersRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(@"Invalid input parameters.");
    }
}

- (void) sendCustomMessageToALL:(NSString *)message onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    if (!message) {
        onFailure(@"Invalid input parameters.");
        return;
    }
    
    VPCHATSendCustomMessageReq* req = [VPCHATSendCustomMessageReq VPCHATSendCustomMessageReqWithTopicId:self.chatID body:message];
    [self.chatRpcInterface sendCustomMessageWithBlock:req onSuccess:^(VPCHATSendCustomMessageRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (AIRBRoomChannelCommentsResponse*)createCommentsResponseWithInnerResponse:(VPCHATListCommentRsp*)rsp {
    AIRBRoomChannelCommentsResponse* response = [[AIRBRoomChannelCommentsResponse alloc] init];
    response.total = rsp.total;
    response.hasMore = rsp.hasMore;
    NSMutableArray* commentsList = [[NSMutableArray alloc] init];
    for (VPCHATCommentModel* item in rsp.commentModelList) {
        AIRBRoomChannelComment* comment = [[AIRBRoomChannelComment alloc] init];
        comment.commentId = item.commentId;
        comment.topicId = item.topicId;
        comment.createAt = item.createAt;
        comment.creatorId = item.creatorId;
        comment.creatorNick = item.creatorNick;
        comment.type = item.type;
        comment.content = item.content;
        comment.extension = [NSMutableDictionary dictionaryWithDictionary:item.extension];
        
        [commentsList addObject:comment];
    }
    if (commentsList.count > 0) {
        response.commentList = [NSArray arrayWithArray:commentsList];
    }
    return response;
}

@end
