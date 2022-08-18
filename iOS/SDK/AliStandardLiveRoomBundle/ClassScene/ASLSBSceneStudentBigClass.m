//
//  ASLSBSceneStudentBigClass.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBSceneStudentBigClass.h"
#import "ASLSBLinkMicHandlerStudent.h"

@interface ASLSBSceneStudentBigClass()

@property(nonatomic, strong) ASLSBLinkMicHandlerStudent* linkMicHandler;

@end

@implementation ASLSBSceneStudentBigClass

@synthesize linkMicState = _linkMicState;
@synthesize linkMicLocalPreview = _linkMicLocalPreview;
@synthesize linkMicJoinedUsers = _linkMicJoinedUsers;
@synthesize cameraOpened = _cameraOpened;
@synthesize micOpened = _micOpened;
@synthesize selfMicAllowed = _selfMicAllowed;

- (instancetype)init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (instancetype)initWithScene:(ASLSBSceneModel *)sceneModel user:(ASLSBSceneUserModel *)userModel{
    self = [super initWithScene:sceneModel user:userModel];
    if (self){
//        if (sceneModel && ![sceneModel.conferenceId isEqualToString:@""]){
//            // linkMicHandler在获取sceneModel之后初始化
//            _linkMicHandler = [[ASLSBLinkMicHandlerStudent alloc] init];
//            _linkMicHandler.delegate = self;
//        }
    }
    return self;
}

- (void)dealloc{
    
}

- (ASLSBLinkMicHandlerStudent*)linkMicHandler{
//    if ([self.sceneModel.conferenceId isEqualToString:@""]){
//        DELEGATE_ACTION_2ARG(onASLSBLinkMicError, ASLSBLinkMicErrorLinkMicNotEnabled, message, @"");
//        return nil;
//    }
    
    if (!_linkMicHandler){
        _linkMicHandler = [[ASLSBLinkMicHandlerStudent alloc] init];
        _linkMicHandler.delegate = self;
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
    [self updateStudentListWhenJoinOrLeaveRTC:YES];
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

- (void) linkMicHandleInvite:(BOOL)agree{
    [self.linkMicHandler linkMicHandleInvite:agree];
}

- (void) linkMicApply{
    [self.linkMicHandler linkMicApply];
}

- (void) linkMicCancelApply{
    [self.linkMicHandler linkMicCancelApply];
}

- (void) linkMicHandleApplyResponse:(BOOL)join{
    [self.linkMicHandler linkMicHandleApplyResponse:join];
}

- (void) updateStudentListWhenJoinOrLeaveRTC:(BOOL)join {
    if (join){
        self.currentMemberJoinedRTCAlreadyListPageNum = 1;
        self.hasMoreMembersJoinedRTCAlready = YES;
        [self queryMoreMembersJoinedRTCAlreadyInfoOnce];
    } else {
        self.currentMemberJoinedRTCAlreadyListPageNum = 1;
        self.hasMoreMembersJoinedRTCAlready = NO;
        [self cleanStudentListStatus];
    }
}

#pragma -mark - Behaviors

- (void)actionWhenSceneRoomEntered{
    [super actionWhenSceneRoomEntered];
    if (_linkMicHandler){
        [self.linkMicHandler actionWhenSceneRoomEntered];
    }
    DELEGATE_ACTION(uiActionWhenSceneRoomEntered);
}

- (void)actionWhenSceneStarted{
    if ([self.sceneModel.bizType isEqualToString:@"standard_class"]){
        self.room.whiteboard.delegate = self;
    }
    self.room.livePlayer.delegate = self;
    self.room.rtc.delegate = self;
    [super actionWhenSceneStarted];
    
    self.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFill;
    [self.room.livePlayer start];
}

- (void)actionWhenSceneEnded{
    [super actionWhenSceneEnded];
    if ([self.sceneModel.bizType isEqualToString:@"standard_class"]){
        [self.room.whiteboard destroy];
    }
    
    [self.room.livePlayer stop];
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.room.livePlayer.playerView removeFromSuperview];
    });
}

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    [super actionWhenRoomMessageReceived:messageType data:data info:info];
    if (_linkMicHandler){
        [self.linkMicHandler actionWhenRoomMessageReceived:messageType data:data info:info];
    }
    switch (messageType) {
        case AIRBRoomChannelMessageTypeRTCStarted:
            break;
        case AIRBRoomChannelMessageTypeRTCStopped:
            break;
        case AIRBRoomChannelMessageTypeLiveStartedByOther:
//            self.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFill;
//            [self.room.livePlayer start];
            break;
        case AIRBRoomChannelMessageTypeLiveStoppedByOther:
            break;
        case AIRBRoomChannelMessageTypeOnRTCCalled:
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication:
            break;
        case AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond:
            break;
        case AIRBRoomChannelMessageTypePeerKickedFromRTC:{
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRoomChannelMessageTypePeerJoinRTCSucceeded:  // 某人加入了RTC
            break;
        case AIRBRoomChannelMessageTypePeerJoinRTCFailed:
            break;
        case AIRBRoomChannelMessageTypePeerLeaveRTC:  // 某人离开RTC
            break;
        case AIRBRoomChannelMessageTypeOnMicphoneMuted:
            break;
        case AIRBRoomChannelMessageTypeOnCameraMuted:{
//            // 更新连麦中的列表
//            NSString* userID = [data valueForKey:@"userId"];
//            if ([self.linkMicJoinedUsers objectForKey:userID]){
//                [self.linkMicJoinedUsers objectForKey:userID].isCameraOpened = [[data valueForKey:@"open"] boolValue];
//            }
//            DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteCameraStateChanged, userID, open, [[data valueForKey:@"open"] boolValue]);
        }
            break;
        case AIRBRoomChannelMessageTypeOnScreenShareOpened:
            break;
            
        default:
            break;
    }
}
    
#pragma mark - LivePlayer delegate

- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info {
    switch (event) {
        case AIRBLivePlayerEventStarted:
            break;
        case AIRBLivePlayerEventStartLoading:
            break;
        case AIRBLivePlayerEventEndLoading:
            break;
        case AIRBLivePlayerEventNotification:
            break;
        default:
            break;
    }
    DELEGATE_ACTION_2ARG(uiActionOnAIRBLivePlayerEvent, event, info, info);
//    [self.delegate uiActionOnAIRBLivePlayerEvent:event info:info];
}

- (void) onAIRBLivePlayerImageSnapshot:(UIImage *)image{
    
}

- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    DELEGATE_ACTION_2ARG(uiActionOnAIRBLivePlayerErrorWithCode, code, message, msg);
    
    NSString* delegateMsg = [NSString stringWithFormat:@"播放出错:(0x%lx, %@)", (long)code, msg];
    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
//    [self.delegate uiMakeToast:[NSString stringWithFormat:@"onAIRBLivePlayerErrorWithCode:(0x%lx, %@)", (long)code, msg] type:ASCRBClassroomToastTypeError];
}

#pragma mark -RTC delegate

- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary *)info{
    [self.linkMicHandler onAIRBRTCEvent:event info:info];
    switch (event) {
        case AIRBRTCEventLocalPreviewStarted:
            break;
        case AIRBRTCEventJoinSucceeded:
            break;
        case AIRBRTCEventLeaveSucceeded:
            break;
        case AIRBRTCEventBypassLiveStarted:
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
        case AIRBRTCEventRemoteUserOnline:{
            NSString* userID = [info objectForKey:@"userID"];
            [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCall];
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRTCEventRemoteUserOffline:{
            NSString* userID = [info objectForKey:@"userID"];
            [self updateStudent:userID toNewStatus:ASCRBStudentStatusReadyForCalled];
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRTCEventRemoteUserMicphoneMuted:{
            NSString* userID = [info objectForKey:@"userID"];
            
            // 更新学员列表
            ASLSBStudentListItemModel* model = [self.studentsLists valueForKey:userID];
            if (model && (model.status == ASCRBStudentStatusAlreadyOnTheCall || model.status == ASCRBStudentStatusAlreadyOnTheCallButMicMuted)){
                if (![[info objectForKey:@"muted"] boolValue]){
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCall];
                } else{
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyOnTheCallButMicMuted];
                }
            }
                
            DELEGATE_ACTION(uiRefreshStudentsListView);
        }
            break;
        case AIRBRTCEventRemoteUserCameraMuted:{
//            // 更新连麦中的列表
//            NSString* userID = [info objectForKey:@"userID"];
//            if ([self.linkMicJoinedUsers objectForKey:userID]){
//                [self.linkMicJoinedUsers objectForKey:userID].isCameraOpened = ![[info objectForKey:@"muted"] boolValue];
//            }
//            DELEGATE_ACTION_2ARG(onASLSBLinkMicRemoteCameraStateChanged, userID, open, ![[info objectForKey:@"muted"] boolValue]);
        }
            break;
            
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBRTCEvent, event, info, info);
//    [self.delegate uiActionOnAIRBRTCEvent:event info:info];
}

- (void) onAIRBRTCRemotePeerViewAvailable:(BOOL)available userID:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type{
    [self.linkMicHandler onAIRBRTCRemotePeerViewAvailable:available userID:userID view:view type:type];
}

- (void) onAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view type:(AIRBRTCVideoViewType)type{
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

- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    [self.linkMicHandler onAIRBRTCErrorWithCode:code message:msg];
    NSString* delegateMsg = [NSString stringWithFormat:@"连麦出错:(0x%lx, %@)", (long)code, msg];
    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
}

#pragma mark - linkMicHandler delegate

- (void) onASLSBLinkMicEvent:(ASLSBLinkMicEvent)event info:(NSDictionary*)info{
    switch (event) {
        case ASLSBLinkMicEventLocalJoinSucceeded:{
            [self updateStudentListWhenJoinOrLeaveRTC:YES];
        }
            break;
        case ASLSBLinkMicEventLocalLeaveSucceeded:{
            [self.room.livePlayer start];
            [self updateStudentListWhenJoinOrLeaveRTC:NO];
        }
            break;
        case ASLSBLinkMicEventLocalPreviewStarted:{
            
        }
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
        case AIRBWhiteBoardEventReplayerReady:
            break;
        case AIRBWhiteBoardEventSeekCompletion:
            [self.room.whiteboard resumeReplay];
        case AIRBWhiteBoardEventResumeCompletion:
            [self.room.vodPlayer play];
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBWhiteBoardEvent, event, info, info)
//    [self.delegate uiActionOnAIRBWhiteBoardEvent:event info:info];
}

- (void) onAIRBWhiteBoardErrorWithCode:(AIRBErrorCode)code message:(nonnull NSString *)msg {
    NSString* delegateMsg = [NSString stringWithFormat:@"白板出错:%@, code:%ld", msg, (long)code];
    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
//    [self.delegate uiMakeToast:[NSString stringWithFormat:@"WhiteBoardError:%@, code:%ld", msg, (long)code] type:ASCRBClassroomToastTypeError];
}

@end
