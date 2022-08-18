//
//  ASLSBLinkMic.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/11.
//

#import "ASLSBLinkMicHandler.h"

@interface ASLSBLinkMicHandler ()

@property (strong, nonatomic) NSMutableDictionary<NSString*, UIView*>* userCameraViewStashedDic;
@property (strong, nonatomic) NSMutableDictionary<NSString*, UIView*>* userScreenViewStashedDic;
@property (assign, nonatomic) ASLSBLinkMicViewContentMode previewContentMode;
@property (assign, nonatomic) ASLSBLinkMicViewContentMode remoteCameraStreamContentMode;

@end

@implementation ASLSBLinkMicHandler

@synthesize linkMicState = _linkMicState;
@synthesize linkMicLocalPreview = _linkMicLocalPreview;
@synthesize linkMicJoinedUsers = _linkMicJoinedUsers;
@synthesize cameraOpened = _cameraOpened;
@synthesize micOpened = _micOpened;
@synthesize selfMicAllowed = _selfMicAllowed;
@synthesize allMicAllowed = _allMicAllowed;

- (instancetype)init {
    self = [super init];
    if (self) {
        _linkMicState = ASLSBLinkMicStateReady;
        _cameraOpened = YES;
        _micOpened = YES;
        _selfMicAllowed = YES;
        _previewContentMode = ASLSBLinkMicViewContentModeAuto;
        _remoteCameraStreamContentMode = ASLSBLinkMicViewContentModeAuto;
        _videoStreamTypeHighDimensions = CGSizeMake(640, 480);
        _RTCConfig = [[AIRBRTCConfig alloc] init];
    }
    return self;
}

- (NSMutableDictionary<NSString*, ASLSBLinkMicUserModel*>*)linkMicJoinedUsers{
    if (!_linkMicJoinedUsers){
        _linkMicJoinedUsers = [[NSMutableDictionary<NSString*, ASLSBLinkMicUserModel*> alloc] init];
    }
    return _linkMicJoinedUsers;
}

- (NSMutableDictionary<NSString*, UIView*>*)userCameraViewStashedDic{
    if (!_userCameraViewStashedDic){
        _userCameraViewStashedDic = [[NSMutableDictionary<NSString*, UIView*> alloc] init];
    }
    return _userCameraViewStashedDic;
}

- (NSMutableDictionary<NSString*, UIView*>*)userScreenViewStashedDic{
    if (!_userScreenViewStashedDic){
        _userScreenViewStashedDic = [[NSMutableDictionary<NSString*, UIView*> alloc] init];
    }
    return _userScreenViewStashedDic;
}

#pragma mark LinkMic Methods

- (void) userViewShouldUnstashed:(NSMutableArray<ASLSBLinkMicUserModel*>*)userList{
    // 将view可用的user回调出去
    if (self.userCameraViewStashedDic.count > 0){
        for (ASLSBLinkMicUserModel* user in userList){
            if ([self.userCameraViewStashedDic objectForKey:user.userID]){
                UIView* view = [self.userCameraViewStashedDic objectForKey:user.userID];
                [self.userCameraViewStashedDic removeObjectForKey:user.userID];
                [self.linkMicJoinedUsers objectForKey:user.userID].cameraView = view;
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicCameraStreamAvailable, user.userID, view, view);
            }
        }
    }
    
    if (self.userScreenViewStashedDic.count > 0){
        for (ASLSBLinkMicUserModel* user in userList){
            if ([self.userScreenViewStashedDic objectForKey:user.userID]){
                UIView* view = [self.userScreenViewStashedDic objectForKey:user.userID];
                [self.userScreenViewStashedDic removeObjectForKey:user.userID];
                [self.linkMicJoinedUsers objectForKey:user.userID].screenView = view;
                // /////// 屏幕共享开关消息逻辑 临时加在这里 //////
                [self.linkMicJoinedUsers objectForKey:user.userID].isScreenSharing = YES;
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteScreenShareStateChanged, user.userID, open, YES);
                // ///////////////      /   ////////////
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicScreenShareStreamAvailable, user.userID, view, view);
            }
        }
    }
}

- (ASLSBLinkMicUserModel*) turnUserDicToLinkMicUserModel:(NSDictionary*)userDic{
    ASLSBLinkMicUserModel* userModel = [[ASLSBLinkMicUserModel alloc] init];
    userModel.userID = [userDic valueForKey:@"userId"];
    userModel.nickname = [userDic objectForKey:@"nickname"] ? : @"";
    userModel.isCameraOpened = [[userDic valueForKey:@"cameraStatus"] boolValue];
    userModel.isMicOpened = [[userDic valueForKey:@"micphoneStatus"] boolValue];
    if ([[userDic valueForKey:@"userId"] isEqualToString:self.delegate.sceneModel.roomOwnerId]){
        userModel.isAnchor = YES;
    } else{
        userModel.isAnchor = NO;
    }
    
    return userModel;
}

- (NSArray<ASLSBLinkMicUserModel*>*) turnUserDicListToLinkMicUserModelList:(NSArray*)userListArray{
    NSMutableArray<ASLSBLinkMicUserModel*>* userList = [[NSMutableArray<ASLSBLinkMicUserModel*> alloc] init];
    
    for (NSDictionary* user in userListArray) {
        ASLSBLinkMicUserModel* userModel = [self turnUserDicToLinkMicUserModel:user];
        [userList addObject:userModel];
    }
    
    return [NSArray arrayWithArray:userList];
}

// 清理用户数据
- (void)removeSavedData:(NSString*)userID{
    if ([self.linkMicJoinedUsers objectForKey:userID]){
        [self.linkMicJoinedUsers removeObjectForKey:userID];
    }
    
    if ([self.userCameraViewStashedDic objectForKey:userID]){
        [self.userCameraViewStashedDic removeObjectForKey:userID];
    }
    
    if ([self.userScreenViewStashedDic objectForKey:userID]){
        [self.userScreenViewStashedDic removeObjectForKey:userID];
    }
}

- (UIView*) linkMicLocalPreview {
    return self.delegate.room.rtc.rtcLocalView;
}

- (void) setVideoStreamTypeHighDimensions:(CGSize)dimensions{
    _videoStreamTypeHighDimensions = dimensions;
    self.RTCConfig.videoStreamTypeHighDimensions = dimensions;
    [self.delegate.room.rtc setConfig:self.RTCConfig];
}

- (void) linkMicJoin{
    [self linkMicJoin:YES];
}

- (void) linkMicJoin:(BOOL)updateRTCInfo{
    
}

- (void) linkMicLeave{
    [self.delegate.room.rtc leaveChannel:NO];
}

- (void) linkMicOpenCamera{
    _cameraOpened = YES;
    [self.delegate.room.rtc muteLocalCamera:NO onSuccess:^{
        // 更新连麦中的列表
        if ([self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId]){
            [self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId].isCameraOpened = YES;
        }
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) linkMicCloseCamera{
    _cameraOpened = NO;
    [self.delegate.room.rtc muteLocalCamera:YES onSuccess:^{
        // 更新连麦中的列表
        if ([self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId]){
            [self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId].isCameraOpened = NO;
        }
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) linkMicSwitchCamera{
    [self.delegate.room.rtc toggleLocalCamera];
}

- (void) linkMicSetPreviewMirror:(BOOL)enable{
    [self.delegate.room.rtc setPreviewMirrorEnabled:enable];
}

- (void) linkMicSetCameraStreamMirror:(BOOL)enable{
    [self.delegate.room.rtc setVideoStreamMirrorEnabled:enable];
}

- (void) linkMicSetPreviewContentMode:(ASLSBLinkMicViewContentMode)contentMode{
    self.previewContentMode = contentMode;
    self.delegate.room.rtc.previewContentMode = (AIRBRTCViewContentMode)contentMode;
}

- (void) linkMicSetRemoteCameraStreamContentMode:(ASLSBLinkMicViewContentMode)contentMode{
    self.remoteCameraStreamContentMode = contentMode;
    self.delegate.room.rtc.remoteVideoStreamContentMode = (AIRBRTCViewContentMode)contentMode;
}

- (void) linkMicOpenMic{
    _micOpened = YES;
    [self.delegate.room.rtc muteLocalMicphone:NO onSuccess:^{
        // 更新连麦中的列表
        if ([self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId]){
            [self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId].isMicOpened = YES;
        }
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) linkMicCloseMic{
    _micOpened = NO;
    [self.delegate.room.rtc muteLocalMicphone:YES onSuccess:^{
        // 更新连麦中的列表
        if ([self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId]){
            [self.linkMicJoinedUsers objectForKey:self.delegate.userModel.userId].isMicOpened = NO;
        }
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

#pragma -mark - Behaviors

- (void)actionWhenSceneRoomEntered{
    // 让在enterRoom之前设置的填充方式、开关摄像头麦克风生效
    if (self.previewContentMode != ASLSBLinkMicViewContentModeAuto){
        [self linkMicSetPreviewContentMode:self.previewContentMode];
    }
    if (self.remoteCameraStreamContentMode != ASLSBLinkMicViewContentModeAuto){
        [self linkMicSetRemoteCameraStreamContentMode:self.remoteCameraStreamContentMode];
    }
    if (!_cameraOpened){
        [self linkMicCloseCamera];
    }
    if (!_micOpened){
        [self linkMicCloseMic];
    }
}

- (void) actionWhenjoinedRTC{
//    self.hasMoreMembersJoinedRTCAlready = YES;
//    [self queryMoreRoomMemberInfoOnce];
    self.linkMicState = ASLSBLinkMicStateJoined;
    
//    // 连麦中的成员列表
//    [self.delegate.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeJoinedAlready pageNum:1 pageSize:200 onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
//        NSMutableArray<ASLSBLinkMicUserModel*>* userList = [[NSMutableArray<ASLSBLinkMicUserModel*> alloc] init];
//        for (AIRBRoomChannelUser* user in rsp.userList){
//            NSString* userID = user.openID;
//            BOOL isCameraOpened = [[user.extension objectForKey:@"cameraOpen"] boolValue];
//            BOOL isMicOpened = [[user.extension objectForKey:@"micOpen"] boolValue];
//            ASLSBLinkMicUserModel* userModel = [[ASLSBLinkMicUserModel alloc] init];
//            userModel.userID = userID;
//            userModel.isCameraOpened = isCameraOpened;
//            userModel.isMicOpened = isMicOpened;
//            userModel.nickname = user.nick;
//            if ([userID isEqualToString:self.delegate.sceneModel.roomOwnerId]){
//                userModel.isAnchor = YES;
//            } else{
//                userModel.isAnchor = NO;
//            }
//
//            [self.linkMicJoinedUsers setObject:userModel forKey:userID];
//
//            [userList addObject:[userModel copy]];
//        }
//
//        DELEGATE_ACTION_2ARG(onASLSBLinkMicUserJoined, NO, userList, [NSArray arrayWithArray:userList]);
//
//        // 将view可用的user回调出去
//        [self userViewShouldUnstashed:userList];
//
//    } onFailure:^(NSString * _Nonnull errorMessage) {
//
//    }];

    // 申请连麦中的成员列表
    [self.delegate.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeApplyingToJoinNow pageNum:1 pageSize:200 onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
        NSMutableArray<ASLSBLinkMicUserModel*>* userList = [[NSMutableArray<ASLSBLinkMicUserModel*> alloc] init];
        for (AIRBRoomChannelUser* user in rsp.userList){
            NSString* userID = user.openID;
            BOOL isCameraOpened = [[user.extension objectForKey:@"cameraOpen"] boolValue];
            BOOL isMicOpened = [[user.extension objectForKey:@"micOpen"] boolValue];
            ASLSBLinkMicUserModel* userModel = [[ASLSBLinkMicUserModel alloc] init];
            userModel.userID = userID;
            userModel.isCameraOpened = isCameraOpened;
            userModel.isMicOpened = isMicOpened;
            userModel.nickname = user.nick;
            if ([userID isEqualToString:self.delegate.sceneModel.roomOwnerId]){
                userModel.isAnchor = YES;
            } else{
                userModel.isAnchor = NO;
            }
            [userList addObject:userModel];
        }

        DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicApplied, NO, userList, [NSArray arrayWithArray:userList]);
    } onFailure:^(NSString * _Nonnull errorMessage) {

    }];
    
    DELEGATE_ACTION_2ARG(onASLSBLinkMicEvent, ASLSBLinkMicEventLocalJoinSucceeded, info, @{});
}

- (void) actionAfterLeftRTC{
    DELEGATE_ACTION_2ARG(onASLSBLinkMicEvent, ASLSBLinkMicEventLocalLeaveSucceeded, info, @{});
    self.linkMicState = ASLSBLinkMicStateReady;
    [self.linkMicJoinedUsers removeAllObjects];
    [self.userCameraViewStashedDic removeAllObjects];
    [self.userScreenViewStashedDic removeAllObjects];
}

- (void)actionWhenPeerKickedFromRTC:(NSString *)userId{
    if([userId isEqualToString:self.delegate.userModel.userId]){
        if (self.linkMicState == ASLSBLinkMicStateJoined){
            [self.delegate.room.rtc leaveChannel:NO];
            self.linkMicState = ASLSBLinkMicStateKicked;
//            [self actionAfterLeftRTC];
        } else{
            self.linkMicState = ASLSBLinkMicStateReady;
            DELEGATE_DELEGATE_ACTION(onASLSBLinkMicInviteCanceledForMe);
        }
    }
//    else{
//        [self updateStudent:userId toNewStatus:ASCRBStudentStatusReadyForCalled];
//    }
}

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    switch (messageType) {
        case AIRBRoomChannelMessageTypeRTCStopped:{
            [self.delegate.room.rtc leaveChannel:NO];
        }
            break;
        case AIRBRoomChannelMessageTypeOnRTCCalled:{
            NSArray* userList = [data valueForKey:@"calleeList"];
            for (NSDictionary* userInfo in userList) {
                NSString* userID = [userInfo valueForKey:@"userId"];
                if ([self.delegate.userModel.userId isEqualToString:userID]){
                    // 处于非申请状态，收到老师邀请
                    if (self.linkMicState != ASLSBLinkMicStateApplying){
                        self.linkMicState = ASLSBLinkMicStateInvited;
                    }
                    break;
                }
            }
            DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicInvited, [self turnUserDicToLinkMicUserModel:[data valueForKey:@"caller"]], userInvitedList, [self turnUserDicListToLinkMicUserModelList:userList]);
        }
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication:{
            NSDictionary* user = [data valueForKey:@"applyUser"];
            ASLSBLinkMicUserModel* userModel = [self turnUserDicToLinkMicUserModel:user];
            
            if ([[data valueForKey:@"isApply"] boolValue]){
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicApplied, YES, userList, [NSArray<ASLSBLinkMicUserModel*> arrayWithObject:userModel]);
            } else{
                DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicApplyCanceled, [NSArray<ASLSBLinkMicUserModel*> arrayWithObject:userModel]);
            }
        }
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond:{
            if ([[data valueForKey:@"uid"] isEqualToString:self.delegate.userModel.userId] && ![[data valueForKey:@"approve"] boolValue]){   // 自己被拒绝
                self.linkMicState = ASLSBLinkMicStateReady;
            }
            
            DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicApplyResponse, [[data valueForKey:@"approve"] boolValue], user, [data valueForKey:@"uid"]);
        }
            break;
        case AIRBRoomChannelMessageTypePeerKickedFromRTC:{
            NSArray* userList = [data valueForKey:@"userList"];
            if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                for (NSDictionary* userInfo in userList) {
                    [self actionWhenPeerKickedFromRTC:[userInfo valueForKey:@"userId"]];
                }
            }
            DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicKicked, [self turnUserDicListToLinkMicUserModelList:userList]);
        }
            break;
        case AIRBRoomChannelMessageTypePeerJoinRTCSucceeded:  // 某人加入了RTC
            break;
        case AIRBRoomChannelMessageTypePeerJoinRTCFailed:{
            NSArray* userList = [data valueForKey:@"userList"];
            DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicInviteRejected, [self turnUserDicListToLinkMicUserModelList:userList]);
        }
            break;
        case AIRBRoomChannelMessageTypePeerLeaveRTC:  // 某人离开RTC
            break;
        case AIRBRoomChannelMessageTypeOnMicphoneMuted:{
            NSArray* userList = [data valueForKey:@"userList"];
            for (NSString* userID in userList) {
                // 自己被静音
                if ([userID isEqualToString:self.delegate.userModel.userId]){
                    _selfMicAllowed = [[data valueForKey:@"open"] boolValue];
//                    if (!_selfMicAllowed){
//                        [self linkMicCloseMic];
//                    }
                    
                    DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicSelfMicAllowed, [[data valueForKey:@"open"] boolValue]);
                }
            }
        }
            break;
        case AIRBRoomChannelMessageTypeOnScreenShareOpened:
            break;
        case AIRBRoomChannelMessageTypeOnRTCMicphonePositiveMuted:
            break;
        case AIRBRoomChannelMessageTypeOnRTCMicphonePassiveMuted:{
            NSArray* userList = [data valueForKey:@"userList"];
            for (NSString* userID in userList) {
                // 自己被静音
                if ([userID isEqualToString:self.delegate.userModel.userId]){
                    BOOL passiveMute = [[data valueForKey:@"passiveMute"] boolValue];
                    if (passiveMute){
                        [self linkMicCloseMic];
                    }
                    
                    DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicSelfMicChangedByOthers, !passiveMute);
                }
            }
        }
            break;
        case AIRBRoomChannelMessageTypeOnRTCMicphonePassiveAllMuted:{
            BOOL muteAll = [[data valueForKey:@"muteAll"] boolValue];
            _allMicAllowed = !muteAll;
            if (muteAll) {
                [self linkMicCloseMic];
            }
            DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicAllMicAllowed, !muteAll);
        }
            break;
            
        default:
            break;
    }
}

#pragma mark -RTC delegate

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary *)info{
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted:{
            DELEGATE_ACTION_2ARG(onASLSBLinkMicEvent, ASLSBLinkMicEventLocalPreviewStarted, info, info)
        }
            break;
        case AIRBRTCEventJoinSucceeded:
            [self actionWhenjoinedRTC];
            break;
        case AIRBRTCEventLeaveSucceeded:
            [self actionAfterLeftRTC];
            break;
        case AIRBRTCEventRemoteUserOnline:{
            NSMutableArray<ASLSBLinkMicUserModel*>* userList = [[NSMutableArray<ASLSBLinkMicUserModel*> alloc] init];
            
            NSString* userID = [info objectForKey:@"userID"];
            
            ASLSBLinkMicUserModel* userModel = [[ASLSBLinkMicUserModel alloc] init];
            userModel.userID = [info objectForKey:@"userID"];
            userModel.nickname = [info objectForKey:@"userNick"];
            userModel.isCameraOpened = [[info objectForKey:@"cameraOpened"] boolValue];
            userModel.isMicOpened = [[info objectForKey:@"micphoneOpened"] boolValue];
            userModel.isScreenSharing = [[info objectForKey:@"screenSharing"] boolValue];
            if ([userID isEqualToString:self.delegate.sceneModel.roomOwnerId]){
                userModel.isAnchor = YES;
            } else{
                userModel.isAnchor = NO;
            }
            [self.linkMicJoinedUsers setObject:userModel forKey:userID];
            [userList addObject:[userModel copy]];
            
            DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicUserJoined, YES, userList, [NSArray arrayWithArray:userList])
            
            // 将view可用的user回调出去
            [self userViewShouldUnstashed:userList];
        }
            break;
        case AIRBRTCEventRemoteUserOffline:{
            NSString* userID = [info objectForKey:@"userID"];
            
            ASLSBLinkMicUserModel* userModel;
            if ([self.linkMicJoinedUsers objectForKey:userID]){
                userModel = [self.linkMicJoinedUsers objectForKey:userID];
            } else{
                userModel = [[ASLSBLinkMicUserModel alloc] init];
                userModel.userID = [info objectForKey:@"userID"];
            }
            NSMutableArray<ASLSBLinkMicUserModel*>* userList = [[NSMutableArray<ASLSBLinkMicUserModel*> alloc] init];
            [userList addObject:userModel];
            
            [self removeSavedData:userID];
            DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicUserLeft, [NSArray arrayWithArray:userList]);
        }
            break;
        case AIRBRTCEventRemoteUserMicphoneMuted:{
            NSString* userID = [info objectForKey:@"userID"];
            // 更新连麦中的列表
            if ([self.linkMicJoinedUsers objectForKey:userID]){
                [self.linkMicJoinedUsers objectForKey:userID].isMicOpened = ![[info objectForKey:@"muted"] boolValue];
            }
            
            NSArray<NSString*>* userList = [[NSArray alloc] initWithObjects:userID, nil];
            DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteMicStateChanged, userList, open, ![[info objectForKey:@"muted"] boolValue]);
        }
            break;
        case AIRBRTCEventRemoteUserCameraMuted:{
            
        }
            break;
            
        default:
            break;
    }
}

- (void) onAIRBRTCRemotePeerViewAvailable:(BOOL)available userID:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type{
    if (type == AIRBRTCVideoViewTypeCamera){
        // 更新连麦中的列表
        if ([self.linkMicJoinedUsers objectForKey:userID]){
            [self.linkMicJoinedUsers objectForKey:userID].isCameraOpened = available;
        }
        DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteCameraStateChanged, userID, open, available);
        
        if (available){
            if ([self.linkMicJoinedUsers objectForKey:userID]){
                [self.linkMicJoinedUsers objectForKey:userID].cameraView = view;
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicCameraStreamAvailable, userID, view, view);
            } else{ // 暂存view
                [self.userCameraViewStashedDic setObject:view forKey:userID];
            }
        }
    } else if (type == AIRBRTCVideoViewTypeScreen){
        // /////// 屏幕共享开关消息逻辑 临时加在这里 //////
        // 更新连麦中的列表
        [self.linkMicJoinedUsers objectForKey:userID].isScreenSharing = available;
        DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteScreenShareStateChanged, userID, open, available);
        // ///////////////      /   ////////////
        
        if (available){
            if ([self.linkMicJoinedUsers objectForKey:userID]){
                [self.linkMicJoinedUsers objectForKey:userID].screenView = view;
                DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicScreenShareStreamAvailable, userID, view, view);
            } else{ // 暂存view
                [self.userScreenViewStashedDic setObject:view forKey:userID];
            }
        }
    }
}

- (void) onAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view type:(AIRBRTCVideoViewType)type{
    
}

- (void) onAIRBRTCActiveSpeaker:(NSString*)userID{
    DELEGATE_DELEGATE_ACTION_1ARG(onASLSBLinkMicActiveSpeaker, userID);
}

- (void)onAIRBRTCAudioVolumeCallback:(NSArray <AIRBRTCUserVolumeInfo *> *_Nullable)volumeInfoArray totalVolume:(int)totalVolume{
    DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicAudioVolumeCallback, volumeInfoArray, totalVolume, totalVolume);
}

- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    NSString* delegateMsg = [NSString stringWithFormat:@"连麦出错:(0x%lx, %@)", (long)code, msg];
    DELEGATE_DELEGATE_ACTION_2ARG(onASLSBLinkMicError, ASLSBLinkMicErrorInternal, message, delegateMsg);
}

@end
