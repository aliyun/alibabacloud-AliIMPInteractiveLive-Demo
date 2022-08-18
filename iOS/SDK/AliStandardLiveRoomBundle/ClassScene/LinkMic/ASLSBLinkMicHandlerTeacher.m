//
//  ASLSBLinkMicHandlerTeacher.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/11.
//

#import "ASLSBLinkMicHandlerTeacher.h"

@implementation ASLSBLinkMicHandlerTeacher

@synthesize selfMicAllowed = _selfMicAllowed;

- (void) linkMicJoin:(BOOL)updateRTCInfo{
    void(^join)(BOOL micAllowed) = ^(BOOL micAllowed){
        self.selfMicAllowed = micAllowed;
        self.allMicAllowed = micAllowed;
        [self.delegate.room.rtc joinChannel];
    };
    
    if (updateRTCInfo) {
        [self.delegate.room.rtc getCurrentRTCDetailOnSuccess:^(AIRBRTCDetailModel * _Nonnull info) {
            join(!info.muteAll);
        } onFailure:^(NSString * _Nonnull errMessage) {
            join(YES);
        }];
    } else {
        join(self.allMicAllowed);
    }
}

- (void) linkMicInvite:(NSArray<NSString*>*)userIDs{
    [self.delegate.room.rtc addPeers:userIDs];
}

- (void) linkMicCancelInvite:(NSArray<NSString*>*)userIDs{
    [self.delegate.room.rtc removePeers:userIDs];
}
- (void) linkMicHandleApply:(NSString*)userID agree:(BOOL)agree{
    [self.delegate.room.rtc approveJoiningApplication:agree fromPeer:userID];
}

- (void) linkMicKick:(NSArray<NSString*>*)userIDs{
    [self.delegate.room.rtc removePeers:userIDs];
}

- (void) linkMicStartBypassLiveStreaming:(AIRBRTCBypassLiveResolutionType)resolutionType{
    [self.delegate.room.rtc startBypassLiveStreaming:resolutionType];
}

- (void) linkMicStopBypassLiveStreaming{
    [self.delegate.room.rtc stopBypassLiveStreaming:NO];
}

- (void) linkMicDestoryBypassLive{
    [self.delegate.room.rtc stopBypassLiveStreaming:YES];
}

- (void) linkMicSetEnumBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type
                                userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                              onSuccess:(void(^)(void))onSuccess
                              onFailure:(void(^)(NSString* error))onFailure{
    [self.delegate.room.rtc setBypassLiveLayout:type userIDs:userIDs onSuccess:onSuccess onFailure:onFailure];
}

- (void) linkMicSetCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel*>*) peerModels
                                onSuccess:(void(^)(void))onSuccess
                                onFailure:(void(^)(NSString* error))onFailure{
    [self.delegate.room.rtc setCustomBypassLiveLayout:peerModels onSuccess:onSuccess onFailure:onFailure];
}

- (void) linkMicOpenMic{
    [super linkMicOpenMic];
}

#pragma -mark - Behaviors

- (void)actionWhenSceneRoomEntered{
    [super actionWhenSceneRoomEntered];
//    if (self.cameraOpened){
////        [self linkMicOpenCamera];
//    }
}

@end
