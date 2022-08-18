//
//  ASLSBSceneTeacher.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/11/19.
//

#import "ASLSBSceneTeacher.h"
#import "ASLRBLogger.h"

@interface ASLSBSceneTeacher()

@end

@implementation ASLSBSceneTeacher

#pragma mark - Methods

- (void) enterSceneClassOnSuccess:(void(^)(NSString* classID))onSuccess
                   onFailure:(void(^)(NSString* errorMessage))onFailure {
    if (self.sceneInitConfig.classID.length > 0) {
        LOG("ASCRBClassroomTeacherVC4Pad::getClassDetailWithClassID(%@)", self.sceneInitConfig.classID);
        [[[AIRBRoomEngine sharedInstance] getRoomSceneClass] getClassDetailWithClassID:self.sceneInitConfig.classID onSuccess:^(NSDictionary * _Nonnull response) {
            if ([self.appInitConfig.userID isEqualToString:[response valueForKey:@"creator_user_id"]]) {
                int32_t status = [[response valueForKey:@"status"] intValue];
                if (status < 2) {
                    [self initWithSceneClassInfo:response role:ASCRBClassroomRoleTeacher];
                    onSuccess(self.sceneModel.classId);
//                    // 进入房间
//                    self.enterSucceedBlock = onSuccess;
//                    self.enterFailedBlock = onFailure;
//                    [self.room enterRoomWithUserNick:self.userModel.nick];
                    return;
                } else {
                    onFailure([NSString stringWithFormat:@"课程已结束，无法再次进入(课程ID:%@)", self.sceneInitConfig.classID]);
                }
            } else {
                onFailure([NSString stringWithFormat:@"You aren't the teacher of this class(%@).", self.sceneInitConfig.classID]);
            }
//            self.sceneInitConfig.classID = nil;
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    } else {
        LOG("ASCRBClassroomTeacherVC4Pad::createClass");
        self.sceneInitConfig.classTitle = self.sceneInitConfig.classTitle ? : @"阿里云课堂";
//        if ([self.appInitConfig.userNick isEqualToString:@""]){
//            self.appInitConfig.userNick = self.appInitConfig.userID;
//        }
        
        [[[AIRBRoomEngine sharedInstance] getRoomSceneClass] createClassWithTitle:self.sceneInitConfig.classTitle nickName:self.appInitConfig.userNick onSuccess:^(NSDictionary * _Nonnull response) {
            [self initWithSceneClassInfo:response role:ASCRBClassroomRoleTeacher];
            onSuccess(self.sceneModel.classId);
//            // 进入房间
//            self.enterSucceedBlock = onSuccess;
//            self.enterFailedBlock = onFailure;
//            [self.room enterRoomWithUserNick:self.userModel.nick];
            return;
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    }
}

- (void)leaveRoom{
    [super leaveRoom];
}

- (void)startClass{
//    DELEGATE_ACTION(uiActionWhenSceneStarted);
}

- (void)endClass{
    // 课堂场景化下课
    [[[AIRBRoomEngine sharedInstance] getRoomSceneClass] stopClassWithClassID:self.sceneModel.classId OnSuccess:^{
//        DELEGATE_ACTION(uiActionWhenSceneEnded);
        [self cleanStudentListStatus];
    } onFailure:^(NSString * _Nonnull error) {
        NSString* delegateMsg = [NSString stringWithFormat:@"stopClassWithClassID:%@ failed", self.sceneModel.classId];
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
    }];
}

#pragma -mark - Behaviors

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    [super actionWhenRoomMessageReceived:messageType data:data info:info];
}

- (void)actionWhenSceneRoomEntered {
    [super actionWhenSceneRoomEntered];
}

- (void)onSetupSucceed{
    DELEGATE_ACTION(uiActionOnSetupSucceed);
//    [self.delegate uiActionOnSetupSucceed];
}

- (void)onSetupFailed:(NSString *)errorMessage{
    DELEGATE_ACTION_1ARG(uiActionOnSetupFailed, errorMessage)
//    [self.delegate uiActionOnSetupFailed:errorMessage];
}

#pragma -mark - uiBehaviors

@end
