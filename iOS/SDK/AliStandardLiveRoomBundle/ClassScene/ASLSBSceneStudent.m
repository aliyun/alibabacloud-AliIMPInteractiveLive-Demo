//
//  ASLSBSceneStudent.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//
#import "ASLSBSceneStudent.h"
#import "ASLRBLogger.h"

@interface ASLSBSceneStudent()

@end

@implementation ASLSBSceneStudent

#pragma mark - Methods

- (void) enterSceneClassOnSuccess:(void(^)(NSString* classID))onSuccess
                        onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOG("ASLSBSceneStudent::enterSceneClassOnSuccess(classID:%@).", self.sceneInitConfig.classID);
    if (self.sceneInitConfig.classID.length > 0) {
        [[[AIRBRoomEngine sharedInstance] getRoomSceneClass] getClassDetailWithClassID:self.sceneInitConfig.classID onSuccess:^(NSDictionary * _Nonnull response) {
            if (![self.appInitConfig.userID isEqualToString:[response valueForKey:@"creator_user_id"]]) {
                [self initWithSceneClassInfo:response role:ASCRBClassroomRoleStudent];
                onSuccess(self.sceneModel.classId);
//                // 进入房间
//                self.enterSucceedBlock = onSuccess;
//                self.enterFailedBlock = onFailure;
//                [self.room enterRoomWithUserNick:self.userModel.nick];
            } else {
                onFailure([NSString stringWithFormat:@"老师不能以学生身份进入课堂(%@).", self.sceneInitConfig.classID]);
            }
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    } else {
        onFailure([NSString stringWithFormat:@"课程码无效(%@)", self.sceneInitConfig.classID]);
    }
}

- (void)leaveRoom{
    [super leaveRoom];
}

#pragma -mark - Behaviors
- (void)actionWhenSceneStarted{
    [super actionWhenSceneStarted];
    
//    self.room.vodPlayer.delegate = self;
}

// 已经在上课
- (void)actionWhenSceneHasStarted{
    [super actionWhenSceneHasStarted];
    [self actionWhenSceneStarted];
}

- (void)actionWhenSceneRoomEntered{
    [super actionWhenSceneRoomEntered];
    
//    DELEGATE_ACTION(uiActionWhenSceneRoomEntered);
}

- (void)actionWhenSceneRoomLeft{
    [super actionWhenSceneRoomLeft];
}

- (void)actionWhenSceneEnded{
    [super actionWhenSceneEnded];
}

- (void)onSetupSucceed{
    DELEGATE_ACTION(uiActionOnSetupSucceed);
//    [self.delegate uiActionOnSetupSucceed];
}

- (void)onSetupFailed:(NSString *)errorMessage{
    DELEGATE_ACTION_1ARG(uiActionOnSetupFailed, errorMessage)
//    [self.delegate uiActionOnSetupFailed:errorMessage];
}

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    [super actionWhenRoomMessageReceived:messageType data:data info:info];
    switch (messageType) {
        //学生先进入房间，后上下课逻辑
        case AIRBRoomChannelMessageTypeSceneClassStarted:{
//            self.classroomModel.isInScene = YES;
//            [self actionWhenSceneStarted];
        }
            break;
        case AIRBRoomChannelMessageTypeSceneClassStopped:{
//            self.classroomModel.isInScene = NO;
//            [self actionWhenSceneEnded];
        }
            break;
        case AIRBRoomChannelMessageTypeLiveStartedByOther:{
            if ([self.sceneModel.bizType isEqualToString:@"standard_live"]){
                [self actionWhenSceneStarted];
            }
        }
            break;
        case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
            if ([self.sceneModel.bizType isEqualToString:@"standard_live"]){
                [self actionWhenSceneEnded];
            }
        }
            break;
        default:
            break;
    }
}

#pragma mark - AIRBRoomChannelDelegate

#pragma mark - AIRBVodPlayerDelegate

- (void)onAIRBVodPlayerEvent:(AIRBVodPlayerEvent)event info:(NSDictionary *)info{
    static BOOL shouldWhiteBoardStepTo = YES;
    switch (event) {
        case AIRBVodPlayerEventPrepareDone:
            break;
        case AIRBVodPlayerEventFirstRenderedStart:
            break;
        case AIRBVodPlayerEventStartLoading:
            break;
        case AIRBVodPlayerEventEndLoading:
            break;
        case AIRBVodPlayerEventCompletion:
            break;
        case AIRBVodPlayerEventPositionUpdated:
            break;
        case AIRBVodPlayerEventExtensionReceived:
            if(shouldWhiteBoardStepTo){
                [self.room.whiteboard pauseReplay];
                [self.room.vodPlayer pause];
                [self.room.whiteboard seek:[info[@"currentUtcTime"] longLongValue]];
//                LOG("cj77771vodplayer拖拽时间:%lld",[info[@"currentUtcTime"] longLongValue]);
                shouldWhiteBoardStepTo = NO;
            }
            break;
        case AIRBVodPlayerEventSeekEnd:
            shouldWhiteBoardStepTo = YES;
            break;
        case AIRBVodPlayerEventStatusChangedToPlaying:
            [self.room.whiteboard resumeReplay];
            break;
        case AIRBVodPlayerEventStatusChangedToPaused:
            [self.room.whiteboard pauseReplay];
            break;
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBVodPlayerEvent, event, info, info);
//    [self.delegate uiActionOnAIRBVodPlayerEvent:event info:info];
}

- (void)onAIRBVodPlayerImageSnapshot:(UIImage *)image{
    
}

- (void)onAIRBVodPlayerErrorWithCode:(AIRBErrorCode)code message:(NSString *)msg{
    NSString* delegateMsg = [NSString stringWithFormat:@"播放出错了:(0x%lx, %@)", (long)code, msg];
    DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
//    [self.delegate uiMakeToast:[NSString stringWithFormat:@"onAIRBVodPlayerErrorWithCode:(0x%lx, %@)", (long)code, msg] type:ASCRBClassroomToastTypeError];
}

@end
