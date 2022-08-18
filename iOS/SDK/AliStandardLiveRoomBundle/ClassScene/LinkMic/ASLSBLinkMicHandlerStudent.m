//
//  ASLSBLinkMicHandlerStudent.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/11.
//

#import "ASLSBLinkMicHandlerStudent.h"

@implementation ASLSBLinkMicHandlerStudent

- (void) linkMicJoin:(BOOL)updateRTCInfo{   // updateRTCInfo是否更新info
    [self.delegate.room.livePlayer stop];
    
    void(^join)(BOOL micAllowed) = ^(BOOL micAllowed){
        self.selfMicAllowed = micAllowed;
        self.allMicAllowed = micAllowed;
        [self.delegate.room.rtc joinChannel];
    };
    
    if (updateRTCInfo) {
        [self.delegate.room.rtc getCurrentRTCDetailOnSuccess:^(AIRBRTCDetailModel * _Nonnull info) {
            join(!info.muteAll);
            if (info.muteAll) { // 通知到外部更新UI
                [self linkMicCloseMic];
                DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicAllMicAllowed, NO);
            }
        } onFailure:^(NSString * _Nonnull errMessage) {
            join(YES);
        }];
    } else {
        join(self.allMicAllowed);
    }
}

- (void) linkMicHandleInvite:(BOOL)agree{
    if (!agree){
        self.linkMicState = ASLSBLinkMicStateReady;
        [self.delegate.room.rtc acceptCall:NO];
    } else if (self.linkMicState == ASLSBLinkMicStateInvited){
        [self linkMicJoin];
    }
}

- (void) linkMicApply{
    if (self.linkMicState == ASLSBLinkMicStateJoined){
        return;
    }
    
    self.linkMicState = ASLSBLinkMicStateApplying;
    [self.delegate.room.rtc applyForJoining:YES];
}

- (void) linkMicCancelApply{
    if (self.linkMicState != ASLSBLinkMicStateApplying){
        return;
    }
    
    self.linkMicState = ASLSBLinkMicStateReady;
    [self.delegate.room.rtc applyForJoining:NO];
}

- (void) linkMicHandleApplyResponse:(BOOL)join{
    if (self.linkMicState != ASLSBLinkMicStateApplying){
        return;
    }
    
    if (!join){
        self.linkMicState = ASLSBLinkMicStateReady;
        [self.delegate.room.rtc acceptCall:NO];
    } else{
        [self linkMicJoin];
    }
}

- (void) linkMicOpenMic{
    // 被禁音
    if (!self.allMicAllowed){
        DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicError, ASLSBLinkMicErrorNotAllowedToOpenMic, message, @"");
        return;
    }
    
    [super linkMicOpenMic];
}

#pragma -mark - Behaviors

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    [super actionWhenRoomMessageReceived:messageType data:data info:info];
    switch (messageType) {
        
            
        default:
            break;
    }
}

@end
