//
//  ASLSBSceneTeacherBigClass.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBSceneTeacherBigClass.h"
#import "ASLRBLogger.h"
#import "ASLSBLinkMicHandlerTeacher.h"

@interface ASLSBSceneTeacherBigClass ()

@property(nonatomic, strong) ASLSBLinkMicHandlerTeacher* linkMicHandler;

@end

@implementation ASLSBSceneTeacherBigClass

@synthesize linkMicState = _linkMicState;
@synthesize linkMicLocalPreview = _linkMicLocalPreview;
@synthesize linkMicJoinedUsers = _linkMicJoinedUsers;
@synthesize cameraOpened = _cameraOpened;
@synthesize micOpened = _micOpened;
@synthesize selfMicAllowed = _selfMicAllowed;


- (instancetype)initWithScene:(ASLSBSceneModel *)sceneModel user:(ASLSBSceneUserModel *)userModel{
    self = [super initWithScene:sceneModel user:userModel];
    if (self){
        _pusherEngine = [[ASLSBPusherEngine alloc] init];
        _pusherEngine.delegate = self;
        
        if (sceneModel && ![sceneModel.conferenceId isEqualToString:@""]){
            // linkMicHandler在setup成功之后初始化
            _linkMicHandler = [[ASLSBLinkMicHandlerTeacher alloc] init];
            _linkMicHandler.delegate = self;
        }
    }
    return self;
}

- (ASLSBLinkMicHandlerTeacher*)linkMicHandler{
    if ([self.sceneModel.conferenceId isEqualToString:@""]){
        DELEGATE_ACTION_2ARG(onASLSBLinkMicError, ASLSBLinkMicErrorLinkMicNotEnabled, message, @"");
        return nil;
    }
    return _linkMicHandler;
}

#pragma mark LinkMic Methods

- (ASLSBLinkMicState)linkMicState{
    return self.linkMicHandler.linkMicState;
}

- (NSMutableDictionary<NSString*, ASLSBLinkMicUserModel*>*)linkMicJoinedUsers{
    return self.linkMicHandler.linkMicJoinedUsers;
}

- (UIView*) linkMicLocalPreview {
    return self.linkMicHandler.linkMicLocalPreview;
}

- (BOOL)isCameraOpened{
    return self.linkMicHandler.isCameraOpened;
}

- (BOOL)isMicOpened{
    return self.linkMicHandler.isMicOpened;
}

- (BOOL)selfMicAllowed{
    return self.linkMicHandler.selfMicAllowed;
}

- (BOOL)isSelfMicAllowed{
    return [self.linkMicHandler isSelfMicAllowed];
}

- (BOOL)allMicAllowed{
    return self.linkMicHandler.allMicAllowed;
}

- (BOOL)isAllMicAllowed{
    return [self.linkMicHandler isAllMicAllowed];
}

- (void) setVideoStreamTypeHighDimensions:(CGSize)dimensions{
    [self.linkMicHandler setVideoStreamTypeHighDimensions:dimensions];
}

- (void) linkMicJoin{
    if (!self.linkMicHandler){
        return;
    }
    
    [self.linkMicHandler linkMicJoin];
//    [self updateStudentListWhenJoinOrLeaveRTC:YES];
    DELEGATE_ACTION(uiActionJoinLinkMic);
}

- (void) linkMicLeave{
    [self.linkMicHandler linkMicLeave];
}

- (void) linkMicOpenCamera{
    [self.linkMicHandler linkMicOpenCamera];
}

- (void) linkMicCloseCamera{
    [self.linkMicHandler linkMicCloseCamera];
}

- (void) linkMicSwitchCamera{
    [self.linkMicHandler linkMicSwitchCamera];
}

- (void) linkMicSetPreviewMirror:(BOOL)enable{
    [self.linkMicHandler linkMicSetPreviewMirror:enable];
}

- (void) linkMicSetCameraStreamMirror:(BOOL)enable{
    [self.linkMicHandler linkMicSetCameraStreamMirror:enable];
}

- (void) linkMicSetPreviewContentMode:(ASLSBLinkMicViewContentMode)contentMode{
    [self.linkMicHandler linkMicSetPreviewContentMode:contentMode];
}

- (void) linkMicSetRemoteCameraStreamContentMode:(ASLSBLinkMicViewContentMode)contentMode{
    [self.linkMicHandler linkMicSetRemoteCameraStreamContentMode:contentMode];
}

- (void) linkMicOpenMic{
    [self.linkMicHandler linkMicOpenMic];
}

- (void) linkMicCloseMic{
    [self.linkMicHandler linkMicCloseMic];
}

- (void) linkMicInvite:(NSArray<NSString *> *)userIDs{
    [self.linkMicHandler linkMicInvite:userIDs];
}

- (void) linkMicCancelInvite:(NSArray<NSString*>*)userIDs{
    [self.linkMicHandler linkMicCancelInvite:userIDs];
}
- (void) linkMicHandleApply:(NSString*)userID agree:(BOOL)agree{
    [self.linkMicHandler linkMicHandleApply:userID agree:agree];
}

- (void) linkMicKick:(NSArray<NSString*>*)userIDs{
    [self.linkMicHandler linkMicKick:userIDs];
}

- (void) linkMicStartBypassLiveStreaming:(AIRBRTCBypassLiveResolutionType)resolutionType{
    [self.linkMicHandler linkMicStartBypassLiveStreaming:resolutionType];
}

- (void) linkMicStopBypassLiveStreaming{
    [self.linkMicHandler linkMicStopBypassLiveStreaming];
}

- (void) linkMicDestoryBypassLive{
    [self.linkMicHandler linkMicDestoryBypassLive];
}

- (void) linkMicSetEnumBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type
                                userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                              onSuccess:(void(^)(void))onSuccess
                              onFailure:(void(^)(NSString* error))onFailure{
    [self.linkMicHandler linkMicSetEnumBypassLiveLayout:type userIDs:userIDs onSuccess:onSuccess onFailure:onFailure];
}

- (void) linkMicSetCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel*>*) peerModels
                                onSuccess:(void(^)(void))onSuccess
                                onFailure:(void(^)(NSString* error))onFailure{
    [self.linkMicHandler linkMicSetCustomBypassLiveLayout:peerModels onSuccess:onSuccess onFailure:onFailure];
}

#pragma mark - Methods

- (void) startClass {
//    [super startClass];
    [self.pusherEngine startLive];
//    [self.room.rtc joinChannel];
}

- (void) endClass {
    [self.room.rtc stopBypassLiveStreaming:YES];
    [self.room.rtc leaveChannel:YES];
//    [self.lesson end];
//    [self endRecord];
}

- (void) muteLocalMicphone:(BOOL)mute {
    [self.room.rtc muteLocalMicphone:mute onSuccess:^{
        
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) muteLocalCamera:(BOOL)mute {
    [self.room.rtc.rtcLocalView removeFromSuperview];
    [self.room.rtc muteLocalCamera:mute onSuccess:^{
        
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) startRecord {
    [self.room.rtc startRecording:AIRBRTCBypassLiveResolutionType_1280x720
                        onSuccess:^(void) {
        LOG("RTC录制已开始");
        NSString* delegateMsg = @"RTC录制已开始";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//        [self.delegate uiMakeToast:@"RTC录制已开始" type:ASCRBClassroomToastTypeCommon];
        [self.room.whiteboard startRecordingOnSuccess:^(NSString * _Nonnull recordID) {
            LOG("白板录制已开始");
            NSString* delegateMsg = @"白板录制已开始";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//            [self.delegate uiMakeToast:@"白板录制已开始" type:ASCRBClassroomToastTypeCommon];
//            [self.lesson startWhiteBoardRecording:recordID];
            DELEGATE_ACTION(uiActionWhenRecordStarted);
//            [self.delegate uiActionWhenRecordStarted];
        } onFailure:^(NSString * _Nonnull error) {
            LOG("白板开始录制失败（%@）", error);
            NSString* delegateMsg = @"白板开始录制失败";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:@"白板开始录制失败" type:ASCRBClassroomToastTypeFailure];
        }];
    } onFailure:^(NSString * _Nonnull error) {
        LOG("RTC开始录制失败（%@）", error);
        NSString* delegateMsg = @"RTC开始录制失败";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//        [self.delegate uiMakeToast:@"RTC开始录制失败" type:ASCRBClassroomToastTypeFailure];
    }];
}

- (void) endRecord {
    [self.room.rtc stopRecordingOnSuccess:^{
        LOG("RTC录制已结束");
        NSString* delegateMsg = @"RTC录制已结束";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//        [self.delegate uiMakeToast:@"RTC录制已结束" type:ASCRBClassroomToastTypeCommon];
        [self.room.whiteboard stopRecordingOnSuccess:^{
            LOG("白板录制已结束");
            NSString* delegateMsg = @"白板录制已结束";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//            [self.delegate uiMakeToast:@"白板录制已结束" type:ASCRBClassroomToastTypeCommon];
        } onFailure:^(NSString * _Nonnull error) {
            LOG("停止白板录制失败");
            NSString* delegateMsg = @"白板停止录制失败";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:@"停止白板录制失败" type:ASCRBClassroomToastTypeFailure];
        }];
    } onFailure:^(NSString * _Nonnull error) {
        LOG("停止RTC录制失败");
        NSString* delegateMsg = @"RTC停止录制失败";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//        [self.delegate uiMakeToast:@"停止RTC录制失败" type:ASCRBClassroomToastTypeFailure];
    }];
}

- (void) pauseRecord {
    [self.room.rtc pauseRecordingOnSuccess:^{
        LOG("RTC录制已暂停");
        NSString* delegateMsg = @"RTC录制已暂停";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//        [self.delegate uiMakeToast:@"RTC录制已暂停" type:ASCRBClassroomToastTypeCommon];
        [self.room.whiteboard pauseRecordingOnSuccess:^{
            LOG("白板录制已暂停");
            NSString* delegateMsg = @"白板录制已暂停";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//            [self.delegate uiMakeToast:@"白板录制已暂停" type:ASCRBClassroomToastTypeCommon];
        } onFailure:^(NSString * _Nonnull error) {
            LOG("白板录制暂停失败");
            NSString* delegateMsg = @"白板暂停录制失败";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:@"白板录制暂停失败" type:ASCRBClassroomToastTypeFailure];
        }];
    } onFailure:^(NSString * _Nonnull error) {
        LOG("RTC录制暂停失败");
        NSString* delegateMsg = @"RTC暂停录制失败";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//        [self.delegate uiMakeToast:@"RTC录制暂停失败" type:ASCRBClassroomToastTypeFailure];
    }];
}

- (void) resumeRecord {
    [self.room.rtc resumeRecording:AIRBRTCBypassLiveResolutionType_1280x720
                          onSuccess:^{
        LOG("RTC录制已恢复");
        NSString* delegateMsg = @"RTC录制已恢复";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//        [self.delegate uiMakeToast:@"RTC录制已恢复" type:ASCRBClassroomToastTypeCommon];
        [self.room.whiteboard resumeRecordingOnSuccess:^{
            LOG("白板录制已恢复");
            NSString* delegateMsg = @"白板录制已恢复";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeCommon);
//            [self.delegate uiMakeToast:@"白板录制已恢复" type:ASCRBClassroomToastTypeCommon];
        } onFailure:^(NSString * _Nonnull error) {
            LOG("白板录制恢复失败");
            NSString* delegateMsg = @"白板恢复录制失败";
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:@"白板录制恢复失败" type:ASCRBClassroomToastTypeFailure];
        }];
    } onFailure:^(NSString * _Nonnull error) {
        LOG("RTC录制恢复失败");
        NSString* delegateMsg = @"RTC恢复录制失败";
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//        [self.delegate uiMakeToast:@"RTC录制恢复失败" type:ASCRBClassroomToastTypeFailure];
    }];
}

#pragma -mark - Behaviors

- (void) actionWhenSceneRoomEntered {
    [super actionWhenSceneRoomEntered];
    if ([self.sceneModel.bizType isEqualToString:@"standard_class"]){
        self.room.whiteboard.delegate = self;
    }
    if (![self.sceneModel.conferenceId isEqualToString:@""]){
        self.room.rtc.delegate = self;
    } else{
        self.room.livePusher.delegate = self;
    }
    
    if (_linkMicHandler){
        [self.linkMicHandler actionWhenSceneRoomEntered];
    }
    DELEGATE_ACTION(uiActionWhenSceneRoomEntered);
}

-(void) actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType
                                      data:(NSDictionary*)data info:(NSDictionary *)info{
    [super actionWhenRoomMessageReceived:messageType data:data info:info];
    if (_linkMicHandler){
        [self.linkMicHandler actionWhenRoomMessageReceived:messageType data:data info:info];
    }
    switch (messageType) {
        case AIRBRoomChannelMessageTypePeerJoinRTCSucceeded: {  // 某人加入了RTC
            NSArray* userList = [data valueForKey:@"userList"];
            for (NSDictionary* userInfo in userList) {
                NSString* userID = [userInfo valueForKey:@"userId"];
                [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCall];
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
        }
            break;
        case AIRBRoomChannelMessageTypePeerJoinRTCFailed:{   // 某人加入RTC超时或者拒绝加入RTC
            NSArray* userList = [data valueForKey:@"userList"];
            if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                for (NSDictionary* userInfo in userList) {
                    [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:ASCRBStudentStatusReadyForCalled];
                    
                    NSString* delegateMsg = [NSString stringWithFormat:@"%@拒绝连麦邀请!", [userInfo valueForKey:@"userId"]];
                    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeWarning);
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypePeerLeaveRTC: {  // 某人离开RTC
            NSArray* userList = [data valueForKey:@"userList"];
            if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                for (NSDictionary* userInfo in userList) {
                    NSString * userID = [userInfo valueForKey:@"userId"];
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusReadyForCalled];
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypePeerKickedFromRTC: { // 某人被踢出了RTC
            NSArray* userList = [data valueForKey:@"userList"];
            if ([userList isKindOfClass:[NSArray class]] && userList.count > 0) {
                for (NSDictionary* userInfo in userList) {
                    [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:ASCRBStudentStatusReadyForCalled];
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypeRTCStarted:
            break;
        case AIRBRoomChannelMessageTypeRTCStopped:
            break;
        case AIRBRoomChannelMessageTypeOnRTCCalled:{
            NSArray* calleeList = [data valueForKey:@"calleeList"];
            if ([calleeList isKindOfClass:[NSArray class]] && calleeList.count > 0) {
                for (NSDictionary* userInfo in calleeList) {
                    [self updateStudent:[userInfo valueForKey:@"userId"] toNewStatus:ASCRBStudentStatusNowBeenCalling];
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication: {
            NSDictionary* applyingUser = [data valueForKey:@"applyUser"];
            if ([applyingUser isKindOfClass:[NSDictionary class]] && applyingUser.count > 0) {
                NSString* userID = [applyingUser valueForKey:@"userId"];
                BOOL applyingOrCancel = [[data valueForKey:@"isApply"] boolValue];
                if (applyingOrCancel) {
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusNowApplying];
                    
                    NSString* delegateMsg = [NSString stringWithFormat:@"%@申请连麦!", userID];
                    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeWarning);
                } else {
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusReadyForCalled];
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond:{
            NSString *userIDApproved = [data valueForKey:@"uid"];
            BOOL approve = [[data valueForKey:@"approve"] boolValue];
            if (!approve){
                [self updateStudent:userIDApproved toNewStatus:ASCRBStudentStatusReadyForCalled];
                DELEGATE_ACTION(uiRefreshStudentsListView);
            }
        }
            break;
        case AIRBRoomChannelMessageTypeOnMicphoneMuted:{
            NSArray* userList = [data valueForKey:@"userList"];
            for (NSString* userID in userList) {
                ASLSBStudentListItemModel* model = [self.studentsLists valueForKey:userID];
                if (model && (model.status == ASCRBStudentStatusAlreadyOnTheCall || model.status == ASCRBStudentStatusAlreadyOnTheCallButMicMuted)){
                    if ([[data valueForKey:@"open"] boolValue]){
                        [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCall];
                    } else{
                        [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCallButMicMuted];
                    }
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypeOnCameraMuted:{
//            NSString* userID = [data valueForKey:@"userId"];
//            if ([[data valueForKey:@"open"] boolValue]){
//
//            } else{
//
//            }
        }
            break;
        case AIRBRoomChannelMessageTypeOnScreenShareOpened:
            break;
        default:
            return;
    }
}

#pragma mark - linkMicHandler delegate

- (void) onASLSBLinkMicEvent:(ASLSBLinkMicEvent)event info:(NSDictionary*)info{
    switch (event) {
        case ASLSBLinkMicEventLocalJoinSucceeded:
            break;
        case ASLSBLinkMicEventLocalLeaveSucceeded:
            break;
        case ASLSBLinkMicEventLocalPreviewStarted:
            break;
            
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(onASLSBLinkMicEvent, event, info, info);
}

#pragma mark - whiteboard delegate

- (void) onAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBWhiteBoardEventOpened:
            break;
        case AIRBWhiteBoardEventDestroied:
            break;
        case AIRBWhiteBoardEventAddPagesWithBackgroundImagesBegin:
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBWhiteBoardEvent, event, info, info)
//    [self.delegate uiActionOnAIRBWhiteBoardEvent:event info:info];
}

#pragma mark -RTC delegate

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info {
    [self.pusherEngine onAIRBRTCEvent:event info:info];
    [self.linkMicHandler onAIRBRTCEvent:event info:info];
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted:
            break;
        case AIRBRTCEventJoinSucceeded: {
//            self.lesson.conferenceID = [info valueForKey:@"conferenceID"];
//            [self.room.rtc startBypassLiveStreaming:AIRBRTCBypassLiveResolutionType_1280x720];
            break;
        }
        case AIRBRTCEventLeaveSucceeded: {
            [super endClass];
            break;
        }
        case AIRBRTCEventBypassLiveStarted: {
//            [self startRecord];
            [[[AIRBRoomEngine sharedInstance] getRoomSceneClass] startClassWithClassID:self.sceneModel.classId onSuccess:^{
//                DELEGATE_ACTION(uiActionWhenSceneStarted);
            } onFailure:^(NSString * _Nonnull error) {
                NSString* delegateMsg = @"startClassWithClassID:failed";
                DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
            }];
            
            self.hasMoreMembersJoinedRTCAlready = YES;
            self.hasMoreMembersApplyingToJoinRTC = YES;
        }
            break;
        case AIRBRTCEventNotification:{
            NSString* delegateMsg = [info valueForKey:@"data"];
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeWarning);
        }
            break;
        case AIRBRTCEventNetworkConnectionLost:
            break;
        case AIRBRTCEventNetworkReconnectStart:
            break;
        case AIRBRTCEventNetworkReconnectSuccess:
            break;
        case AIRBRTCEventNetworkConnectionFailed:
            break;
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBRTCEvent, event, info, info);
}

- (void) onAIRBRTCRemotePeerViewAvailable:(BOOL)available userID:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type{
    [self.linkMicHandler onAIRBRTCRemotePeerViewAvailable:available userID:userID view:view type:type];
}

- (void) onAIRBRTCRemotePeerViewAvailable:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type{
    DELEGATE_ACTION_3ARG(uiActionOnAIRBRTCRemotePeerViewAvailable, userID, view, view, type, type);
    
    [self.linkMicHandler onAIRBRTCRemotePeerViewAvailable:userID view:view type:type];
}

- (void)onAIRBRTCNetworkQualityChanged:(NSString *_Nonnull)userID
                      upNetworkQuality:(AIRBRTCNetworkQuality)upQuality
                    downNetworkQuality:(AIRBRTCNetworkQuality)downQuality{

}

- (void) onAIRBRTCRemotePeerVideoFirstFrameDrawn:(NSString*)userID type:(AIRBRTCVideoViewType)type{
    
}

- (void) onAIRBRTCActiveSpeaker:(NSString*)userID{
    [self.linkMicHandler onAIRBRTCActiveSpeaker:userID];
}

- (void)onAIRBRTCAudioVolumeCallback:(NSArray <AIRBRTCUserVolumeInfo *> *_Nullable)volumeInfoArray totalVolume:(int)totalVolume{
    [self.linkMicHandler onAIRBRTCAudioVolumeCallback:volumeInfoArray totalVolume:totalVolume];
}

- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    NSString* delegateMsg = [NSString stringWithFormat:@"连麦出错:(0x%lx, %@)", (long)code, message];
    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
}

#pragma mark -PusherEngine delegate

- (void) onASLSBPusherEngineEvent:(ASLSBPusherEngineEvent)event info:(NSDictionary*)info{
    DELEGATE_ACTION_2ARG(onASLSBPusherEngineEvent, event, info, info);
}

#pragma mark -LivePuhser delegate

- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info{
    [self.pusherEngine onAIRBLivePuhserEvent:event info:info];
    DELEGATE_ACTION_2ARG(uiActionOnAIRBLivePuhserEvent, event, info, info);
}

- (void) onAIRBLivePusherError:(AIRBErrorCode)errorCode message:(NSString*)errorMessage{
    DELEGATE_ACTION_2ARG(uiActionOnAIRBLivePusherError, errorCode, message, errorMessage);
}

@end
