//
//  ASLSBScene.m
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/10/14.
//

#import "ASLSBScene.h"
//#import "ASCRBAppInitConfig.h"
//#import "ASCRBClassInitConfig.h"
//#import "ASCRBClassroomTeacherBigClassVC4Pad.h"
#import "ASLRBLogger.h"

@interface ASLSBScene() <AIRBRoomEngineDelegate>
@property (nonatomic, strong) AIRBRoomEngineConfig* roomEngineConfig;
@property (nonatomic, strong) ASLSBAppInitConfig* appInitConfig4RoomEngine;

@property (copy, nonatomic) void(^onGlobalInitSuccess)(void);
@property (copy, nonatomic) void(^onGlobalInitFailure)(NSString* errorMessage);

@end

static const int kStudentListRoomMemberPageSizeForStudentView = 200;

@implementation ASLSBScene

- (void) enterSceneClassOnSuccess:(void(^)(NSString* classID))onSuccess
                        onFailure:(void(^)(NSString* errorMessage))onFailure {
    
}
- (void) onSetupSucceed {
    
}
- (void) onSetupFailed:(NSString *)errorMessage {
    
}

#pragma -mark - Lifecycle

-(void)dealloc {
    [self leaveRoom];
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    // 移除录屏监测
    if (self.sceneInitConfig.screenSecure){
        if (@available(iOS 11.0, *)) {
            [[NSNotificationCenter defaultCenter] removeObserver:self name:UIScreenCapturedDidChangeNotification object:nil];
        } else {
            // Fallback on earlier versions
        }
    }
}

- (void) setup {
    __weak typeof(self) weakSelf = self;
    [self roomEngineLoginWithConfig:weakSelf.appInitConfig onSuccess:^{
        [weakSelf enterSceneClassOnSuccess:^(NSString *classID) {
            [weakSelf onSetupSucceed];
        } onFailure:^(NSString *errorMessage) {
            [weakSelf onSetupFailed:errorMessage];
        }];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        [weakSelf onSetupFailed:errorMessage];
    }];
}

-(instancetype)initWithClassAppConfig:(ASLSBAppInitConfig*)appConfig classConfig:(ASLSBSceneInitConfig*)classConfig {
    self = [self init];
    if (self) {
        _appInitConfig = appConfig;
        _sceneInitConfig = classConfig;
    }
    return self;
}

- (instancetype)initWithSceneClassInfo:(NSDictionary*)response role:(ASCRBClassroomRole)role {
    ASLSBSceneModel* sceneModel = [[ASLSBSceneModel alloc] init];
    sceneModel.bizType = @"standard_class";
    sceneModel.bizID = [response valueForKey:@"class_id"];
    sceneModel.classId = [response valueForKey:@"class_id"];
    sceneModel.liveId = [response valueForKey:@"live_id"];
    sceneModel.conferenceId = [response valueForKey:@"conference_id"];
    sceneModel.whiteBoardId = [response valueForKey:@"whiteboard_id"];
    sceneModel.roomId = [response valueForKey:@"room_id"];
    sceneModel.roomOwnerId = [response valueForKey:@"creator_user_id"];
    sceneModel.roomOwnerNick = [response valueForKey:@"creator_nick_name"];
    sceneModel.title = [response valueForKey:@"title"];
    sceneModel.status = [[response valueForKey:@"status"] intValue];
    sceneModel.isInScene = sceneModel.status == 1 ? YES : NO;
    sceneModel.sceneStartTime = [[response valueForKey:@"start_time"] longLongValue];
    sceneModel.sceneEndTime = [[response valueForKey:@"end_time"] longLongValue];
    ASLSBSceneUserModel* userModel = [[ASLSBSceneUserModel alloc] init];
    userModel.userId = self.appInitConfig.userID;
    userModel.nick = self.appInitConfig.userNick;
    userModel.role = role;
    userModel.extension = [self.appInitConfig.userExtension copy];
    return [self initWithScene:sceneModel user:userModel];
}

- (instancetype)initWithSceneLiveInfo:(NSDictionary*)response role:(int)role userID:(NSString*)userID nickName:(NSString*)nick userExtension:(NSDictionary<NSString*,NSString*>*) userExtension conferenceId:(NSString*)conferenceId{
    ASLSBSceneModel* sceneModel = [[ASLSBSceneModel alloc] init];
    sceneModel.bizType = @"standard_live";
    sceneModel.bizID = [response valueForKey:@"live_id"];
    sceneModel.classId = @"";
    sceneModel.liveId = [response valueForKey:@"live_id"];
    sceneModel.conferenceId = conferenceId;
    sceneModel.whiteBoardId = [response valueForKey:@""];
    sceneModel.roomId = [response valueForKey:@"room_id"];
    sceneModel.roomOwnerId = [response valueForKey:@"anchor_id"];
    sceneModel.roomOwnerNick = [response valueForKey:@"anchor_nick"];
    sceneModel.title = [response valueForKey:@"title"];
    sceneModel.status = [response valueForKey:@"status"] ? [[response valueForKey:@"status"] intValue] : 0;
    sceneModel.isInScene = sceneModel.status == 1 ? YES : NO;
    sceneModel.sceneStartTime = 0;
    sceneModel.sceneEndTime = 0;
    ASLSBSceneUserModel* userModel = [[ASLSBSceneUserModel alloc] init];
    userModel.userId = userID;
    userModel.nick = nick;
    userModel.role = (ASCRBClassroomRole)role;
    userModel.extension = [userExtension copy];
    self = [self init];
    return [self initWithScene:sceneModel user:userModel];
}

- (instancetype)initWithScene:(ASLSBSceneModel *)sceneModel user:(ASLSBSceneUserModel *)userModel{
    if (self){
        _sceneModel = sceneModel;
        _userModel = userModel;
        _studentsLists = [[NSMutableDictionary alloc] init];
        _studentListLock = [[NSLock alloc] init];
        _studentsListDataSource = [[NSMutableArray alloc] init];
        _hasMoreRoomMembers = YES;
        _hasMoreMembersJoinedRTCAlready = NO;
        _hasMoreMembersApplyingToJoinRTC = NO;
//        _hasMoreMembersJoinedRTCAlready = self.userModel.role == ASCRBClassroomRoleTeacher? YES : NO;
//        _hasMoreMembersApplyingToJoinRTC = self.userModel.role == ASCRBClassroomRoleTeacher? YES : NO;
        _currentRoomMemberListPageNum = 1;
        _currentMemberJoinedRTCAlreadyListPageNum = 1;
        _currentMemberApplyingToJoinRTCListPageNum = 1;
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillTerminate) name:UIApplicationWillTerminateNotification object:nil];
    }
    return self;
}

// 杀后台
-(void)applicationWillTerminate{
    [self leaveRoom];
}

//- (void) enterClassroomOnSuccess:(void (^)(NSString * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
//    self.enterSucceedBlock = onSuccess;
//    self.enterFailedBlock = onFailure;
//    [self.room enterRoomWithUserNick:self.userModel.nick];
//}


#pragma mark - Methods

- (void) roomEngineLoginWithConfig:(ASLSBAppInitConfig*)config
                         onSuccess:(void (^)(void))onSuccess
                         onFailure:(void (^)(NSString * _Nonnull))onFailure {
    LOG("ASLSBScene::roomEngineLoginWithConfig(%p, %@)", config, config.userID);
//    if ([[AIRBRoomEngine sharedInstance] isInited]) {
////        if ([self.appInitConfig4RoomEngine.userID isEqualToString:config.userID]) { //不相等时默认把前一个登出
////            LOG("ASCRBClassroomManager::globalInitOnceWithConfig logout pre user(%@).", self.appInitConfig4RoomEngine.userID);
////            [[AIRBRoomEngine sharedInstance] logoutOnSuccess:^{
//                LOG("ASCRBClassroomManager::globalInitOnceWithConfig start login new user(%@).", config.userID);
//                self.appInitConfig4RoomEngine = config;
//                self.onGlobalInitSuccess = onSuccess;
//                self.onGlobalInitFailure = onFailure;
//                [[AIRBRoomEngine sharedInstance] loginWithUserID:self.appInitConfig4RoomEngine.userID];
////            } onFailure:^(NSString * _Nonnull errorMessage) {
////                onFailure(errorMessage);
////            }];
////        } else if ([[AIRBRoomEngine sharedInstance] isLogined:config.userID]){
////            LOG("ASCRBClassroomManager::globalInitOnceWithConfig current user(%@) logined already.", config.userID);
////            onSuccess();
////        }
//        return;
//    }
    
    self.appInitConfig4RoomEngine = config;
    self.onGlobalInitSuccess = onSuccess;
    self.onGlobalInitFailure = onFailure;
    
    self.roomEngineConfig = [[AIRBRoomEngineConfig alloc] init];
    self.roomEngineConfig.appID = config.appID;
    self.roomEngineConfig.appKey = config.appKey;
    self.roomEngineConfig.deviceID = [[UIDevice currentDevice] identifierForVendor].UUIDString;
    [AIRBRoomEngine sharedInstance].delegate = self;
    [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:self.roomEngineConfig];
    [[AIRBRoomEngine sharedInstance] setLogLevel:AIRBLoggerLevelDebug];
}

-(void) enterRoom {
    if (self.sceneInitConfig.screenSecure) {
        // 监测当前设备是否处于录屏状态
        UIScreen * sc = [UIScreen mainScreen];
        if (@available(iOS 11.0,*)) {
            if (sc.isCaptured) {
//                NSLog(@"正在录制-----%d",sc.isCaptured);
                [self onVideoRecording];
                return;
            }
        }else { //ios 11之前处理 未知

        }

        // ios11之后才可以录屏
        if (@available(iOS 11.0,*)) {
            [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(onVideoRecording) name:UIScreenCapturedDidChangeNotification  object:nil];
        }
    }
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.sceneModel.roomId bizType:self.sceneModel.bizType bizID:self.sceneModel.bizID];
    self.room.delegate = self;
    [self.room enterRoomWithUserNick:self.userModel.nick extension:self.userModel.extension];
}

-(void) leaveRoom {
    if (self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
        self.room = nil;
        DELEGATE_ACTION(uiActionWhenSceneRoomLeft);
    }
}

- (void) queryMoreRoomMemberInfoOnce {
    if (self.hasMoreRoomMembers) {
        [self.room getRoomUserListWithPageNum:self.currentRoomMemberListPageNum
                                     pageSize:kStudentListRoomMemberPageSizeForStudentView
                                    onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
            for (AIRBRoomChannelUser* user in response.userList) {
                if ([user.openID isEqualToString:self.sceneModel.roomOwnerId]) {   // 排除老师
                    continue;
                }
                
                ASLSBStudentListItemModel* model = [[ASLSBStudentListItemModel alloc] init];
                model.status = ASCRBStudentStatusReadyForCalled;
                if ([user.openID isEqualToString:self.userModel.userId]) {
                    model.userID = [user.openID stringByAppendingString:@"（我）"];
                    [self addStudentListItemModel:model index:0];
                } else {
                    model.userID = user.openID;
                    [self addStudentListItemModel:model index:self.studentsListDataSource.count];
                }
                [self.studentsLists setValue:model forKey:user.openID];
            }
            
            if (![self.userModel.userId isEqualToString:self.sceneModel.roomOwnerId] && ![self.studentsLists objectForKey:self.userModel.userId]) {   // 学生端保证第一个为自己
                ASLSBStudentListItemModel* model = [[ASLSBStudentListItemModel alloc] init];
                model.status = ASCRBStudentStatusReadyForCalled;
                model.userID = [self.userModel.userId stringByAppendingString:@"（我）"];
                [self addStudentListItemModel:model index:0];
                [self.studentsLists setValue:model forKey:self.userModel.userId];
            }
            
            DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
            if (response.hasMore) {
                self.currentRoomMemberListPageNum++;
            } else {
                self.hasMoreRoomMembers = NO;
            }
            
        } onFailure:^(NSString * _Nonnull errorMessage) {
            NSString* delegateMsg = [NSString stringWithFormat:@"获取成员列表失败(%@)",errorMessage];
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:[NSString stringWithFormat:@"获取成员列表失败(%@)",errorMessage] type:ASCRBClassroomToastTypeFailure];
        }];
    }
    
    [self queryMoreMembersJoinedRTCAlreadyInfoOnce];
    [self queryMoreMembersApplyingToJoinRTCInfoOnce];
}

- (void) queryMoreMembersJoinedRTCAlreadyInfoOnce {
    if (self.hasMoreMembersJoinedRTCAlready){
        //请求在RTC的列表
        [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeJoinedAlready pageNum:self.currentMemberJoinedRTCAlreadyListPageNum pageSize:kStudentListRoomMemberPageSizeForStudentView onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
            for (AIRBRoomChannelUser* user in rsp.userList) {
                if ([user.extension valueForKey:@"micOpen"]){
                    [self updateStudent:user.openID toNewStatus:ASCRBStudentStatusAlreadyOnTheCall];
                } else{
                    [self updateStudent:user.openID toNewStatus:ASCRBStudentStatusAlreadyOnTheCallButMicMuted];
                }
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
            
            if (rsp.hasMore) {
                self.currentMemberJoinedRTCAlreadyListPageNum++;
            } else {
                self.hasMoreMembersJoinedRTCAlready = NO;
            }
        } onFailure:^(NSString * _Nonnull errorMessage) {
            NSString* delegateMsg = [NSString stringWithFormat:@"获取RTC成员列表失败(%@)",errorMessage];
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:[NSString stringWithFormat:@"获取RTC成员列表失败(%@)",errorMessage] type:ASCRBClassroomToastTypeFailure];
        }];
    }
}

- (void) queryMoreMembersApplyingToJoinRTCInfoOnce {
    if (self.hasMoreMembersApplyingToJoinRTC){
        //请求在申请加入RTC的列表
        [self.room.rtc queryCurrentPeerListWithType:AIRBRTCPeerTypeApplyingToJoinNow pageNum:self.currentMemberApplyingToJoinRTCListPageNum pageSize:kStudentListRoomMemberPageSizeForStudentView onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull rsp) {
            for (AIRBRoomChannelUser* user in rsp.userList) {
                [self updateStudent:user.openID toNewStatus:ASCRBStudentStatusNowApplying];
            }
            DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
            
            if (rsp.hasMore) {
                self.currentMemberApplyingToJoinRTCListPageNum++;
            } else {
                self.hasMoreMembersApplyingToJoinRTC = NO;
            }
        } onFailure:^(NSString * _Nonnull errorMessage) {
            NSString* delegateMsg = [NSString stringWithFormat:@"获取申请连麦成员列表失败(%@)",errorMessage];
            DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
//            [self.delegate uiMakeToast:[NSString stringWithFormat:@"获取申请RTC成员列表失败(%@)",errorMessage] type:ASCRBClassroomToastTypeFailure];
        }];
    }
}

- (void) addStudentListItemModel:(ASLSBStudentListItemModel*)model index:(int64_t)index {
    [self.studentListLock lock];
    [self.studentsListDataSource insertObject:model atIndex:index];
    [self.studentListLock unlock];
}

- (void) removeStudentListItemModel:(ASLSBStudentListItemModel*)model {
    [self.studentListLock lock];
    [self.studentsListDataSource removeObject:model];
    [self.studentListLock unlock];
}

-(void) cleanStudentListStatus {
    // 清除学员列表状态
    for (int indexData = 0; indexData < self.studentsListDataSource.count; ++indexData){
        ASLSBStudentListItemModel* model = [self.studentsListDataSource objectAtIndex:indexData];
        if (model.status == ASCRBStudentStatusAlreadyOnTheCall || model.status == ASCRBStudentStatusAlreadyOnTheCallButMicMuted){
            model.status = ASCRBStudentStatusReadyForCalled;
        } else if (indexData > 0){
            break;
        }
    }
    DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
}

-(void)updateStudent:(NSString*)userID toNewStatus:(ASCRBStudentStatus)status{
    //学员列表更新，注意只是更新数据源(单个model),需要列表视图reloadData
    if (userID.length > 0) {
        if ([userID isEqualToString:self.sceneModel.roomOwnerId]) { // 排除老师
            return;
        }
        
        ASLSBStudentListItemModel* model = [self.studentsLists valueForKey:userID];
        if (model) {
            // 判断是否要删除原本的model
            if (model.status == ASCRBStudentStatusReadyForCalled || status == ASCRBStudentStatusReadyForCalled || status == ASCRBStudentStatusAlreadyLeftRoom){
                [self removeStudentListItemModel:model];
            } else {
                model.status = status;  // 只需要修改状态，不需要改变相对位置
                return;
            }
            
        }
        [self.studentsLists removeObjectForKey:userID];
        if (status == ASCRBStudentStatusAlreadyLeftRoom){   // 学员离开房间，无需新增model
            return;
        }
        
        // 新增model
        model = [[ASLSBStudentListItemModel alloc] init];
        model.status = status;
        
        if ([userID isEqualToString:self.userModel.userId]) {
            model.userID = [userID stringByAppendingString:@"（我）"];
            [self addStudentListItemModel:model index:0];
        } else {
            model.userID = userID;
            if (status == ASCRBStudentStatusReadyForCalled){
                [self addStudentListItemModel:model index:self.studentsListDataSource.count];
            } else{
                [self addStudentListItemModel:model index:self.userModel.role == ASCRBClassroomRoleStudent ? 1 : 0];
            }
        }
        
        [self.studentsLists setValue:model forKey:userID];
    }
}

- (void)sendMessage:(NSString *)message
          onSuccess:(void (^)(void))onSuccess
          onFailure:(void (^)(NSString * errorMessage))onFailure{
    [self.room.chat sendComment:message onSuccess:^{
        onSuccess();
    } onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
        onFailure([NSString stringWithFormat:@"发送失败(%@)",message]);
        NSString* delegateMsg = [NSString stringWithFormat:@"发送失败(%@)",message];
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeWarning);
//        [self.delegate uiMakeToast:[NSString stringWithFormat:@"发送失败(%@)",message] type:ASCRBClassroomToastTypeWarning];
    }];
}

//-(void) requestClassroomDetail{
//    NSString* path = [NSString stringWithFormat:@"%@/api/room/%@", [ASCRBEnvironments shareInstance].appServerUrl, @"getLessonStartTime"];
//    NSString* s = [NSString stringWithFormat:@"%@?roomId=%@", path, self.sceneModel.roomId];
//    NSURL* url = [[NSURL alloc]initWithString:s];
//    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
//    request.HTTPMethod = @"POST";
//
//    NSURLSession* session = [NSURLSession sharedSession];
//    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
//        if (data && !error) {
//            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
//                                                                options:NSJSONReadingMutableContainers
//                                                                  error:nil];
//            LOG("getWBToken data:%@", dic);
//
//            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && (![[dic valueForKey:@"result"] isEqual:[NSNull null]])) {
//                self.sceneModel.isInScene = YES;
//                [self actionWhenSceneStarted];
//                [self.classInfoBar startClassTimerWithUtcTime:[dic[@"result"][@"createTime"] longValue]];
//            }else{
//                self.sceneModel.isInScene = NO;
//            }
//        }
//    }];
//    [task resume];
//}


#pragma -mark - Behaviors

// 进入房间成功
-(void) actionWhenSceneRoomEntered {
    self.roomEntered = YES;
//    self.enterSucceedBlock(nil);
//    self.enterSucceedBlock = nil;
//    self.enterFailedBlock = nil;
    
    // 获取学员列表
    [self queryMoreRoomMemberInfoOnce];
    
    if (self.sceneModel.status == 0) {  // 课程未开始
        [self actionWhenSceneNotStarted];
    } else if (self.sceneModel.status == 1) {    // 课程正在上课
        [self actionWhenSceneHasStarted];   //****** 老师端需要实现 ********//
    } else if (self.sceneModel.status == 2){    // 已下课
        [self actionWhenSceneHasEnded];
    }
}

// 离开房间成功
- (void)actionWhenSceneRoomLeft{
    self.roomEntered = NO;
}

// 未上课
- (void)actionWhenSceneNotStarted{
    DELEGATE_ACTION(uiActionWhenSceneNotStarted);
//    [self.delegate uiActionWhenSceneNotStarted];
}

// 开始上课
- (void)actionWhenSceneStarted{
    self.sceneModel.isInScene = YES;
    DELEGATE_ACTION(uiActionWhenSceneStarted);
//    [self.delegate uiActionWhenSceneStarted];
}

// 已经在上课
- (void)actionWhenSceneHasStarted{
    DELEGATE_ACTION(uiActionWhenSceneHasStarted);
//    [self.delegate uiActionWhenSceneHasStarted];
}

// 已经下课
- (void)actionWhenSceneHasEnded{
    DELEGATE_ACTION(uiActionWhenSceneHasEnded);
}

- (void)actionWhenSceneEnded{
    self.sceneModel.isInScene = NO;
    DELEGATE_ACTION(uiActionWhenSceneEnded);
//    [self.delegate uiActionWhenSceneEnded];
}

- (void)actionWhenNoticeUpdated:(NSString*)notice{
    self.sceneModel.notice = notice;
    DELEGATE_ACTION_1ARG(uiActionWhenNoticeUpdated, notice);
}

- (void)actionWhenChatInfoGotten:(BOOL)succeeded errorMessag:(NSString*)errorMessage{
    DELEGATE_ACTION_2ARG(uiActionWhenChatInfoGotten, succeeded, errorMessag, errorMessage);
}

// 当用户录屏
-(void)onVideoRecording {
    [self leaveRoom];
    DELEGATE_ACTION(uiActionOnVideoRecording)
//    [self.delegate uiActionOnVideoRecording];
}

- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info{
    switch (messageType) {
        case AIRBRoomChannelMessageTypeRoomMembersInOut:{
            int32_t onlineCount = [[NSString stringWithFormat:@"%@",[data valueForKey:@"onlineCount"]] intValue];
            int32_t pv = [[NSString stringWithFormat:@"%@",[data valueForKey:@"pv"]] intValue];
            int32_t uv = [[NSString stringWithFormat:@"%@",[data valueForKey:@"uv"]] intValue];
            self.sceneModel.onlineCount = onlineCount;
            self.sceneModel.pv = pv;
            self.sceneModel.uv = uv;
            
            BOOL enter = [[data valueForKey:@"enter"] boolValue];
            NSString* userID = [data valueForKey:@"userId"];
            if (userID.length > 0 && ![userID isEqualToString:self.sceneModel.roomOwnerId] && ![userID isEqualToString:self.userModel.userId]) {
                if (enter) {
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusReadyForCalled];
                } else {
                    [self updateStudent:userID toNewStatus:ASCRBStudentStatusAlreadyLeftRoom];
                }
                
                DELEGATE_ACTION(uiRefreshStudentsListView);
//            [self.delegate uiRefreshStudentsListView];
            }
        }
            break;
        case AIRBRoomChannelMessageTypeSceneClassStarted:{
            [self actionWhenSceneStarted];
        }
            break;
        case AIRBRoomChannelMessageTypeSceneClassStopped:{
            [self actionWhenSceneEnded];
        }
            break;
        case AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot:{
            NSString* userID = [data valueForKey:@"muteUserOpenId"];
            if (userID.length > 0 && [userID isEqualToString:self.userModel.userId]){
                self.sceneModel.isUserCommentBanned = [[data valueForKey:@"mute"] boolValue];
            }
        }
            break;
        case AIRBRoomChannelMessageTypeChatAllUsersCommentBannedOrNot:{
            self.sceneModel.isAllCommentBanned = [[data valueForKey:@"mute"] boolValue];
        }
            break;
        case AIRBRoomChannelMessageTypeRoomNoticeUpdatedNew:{
            [self actionWhenNoticeUpdated:[data valueForKey:@"notice"]];
        }
            break;
        case AIRBRoomChannelMessageTypeChatLikeReceived:{
            self.sceneModel.likeCount = [[data valueForKey:@"likeCount"] intValue];
        }
            break;
        default:
            break;
    }
    
    DELEGATE_ACTION_3ARG(uiActionWhenRoomMessageReceived, messageType, data, data, info, info);
}

#pragma mark - AIRBRoomChannelDelegate

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRoomChannelEventEntered:{
            [self actionWhenSceneRoomEntered];
        }
            break;
        case AIRBRoomChannelEventLeft:
            [self actionWhenSceneRoomLeft];
            break;
        case AIRBRoomChannelEventRoomInfoGotten: {
            NSString* notice = [info valueForKey:@"notice"];
            [self actionWhenNoticeUpdated:notice];
            self.sceneModel.uv = [[info valueForKey:@"uv"] intValue];
            self.sceneModel.pv = [[info valueForKey:@"pv"] intValue];
            self.sceneModel.onlineCount = [[info valueForKey:@"onlineCount"] intValue];
            // 获取评论区状态
            [self.room.chat getCurrentChatInfoOnSuccess:^(NSDictionary * _Nonnull info) {
                self.sceneModel.likeCount = [[info valueForKey:@"total_like"] intValue];
                self.sceneModel.isUserCommentBanned = [[info valueForKey:@"ban"] boolValue];
                self.sceneModel.isAllCommentBanned = [[info valueForKey:@"ban_all"] boolValue];
                [self actionWhenChatInfoGotten:YES errorMessag:@""];
            } onFailure:^(NSString * _Nonnull errMessage) {
                [self actionWhenChatInfoGotten:NO errorMessag:errMessage];
                NSString* delegateMsg = [NSString stringWithFormat:@"onGetCurrentChatInfoFailure:(%@)", errMessage];
                DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeFailure);
            }];
        }
            break;
        case AIRBRoomChannelEventMessageReceived: {
            NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = nil;
            if(turnData){
                dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            }
            AIRBRoomChannelMessageType messageType = [[info valueForKey:@"type"] integerValue];
            [self actionWhenRoomMessageReceived:messageType data:dataDic info:info];
        }
            break;
        default:
            break;
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBRoomChannelEvent, event, info, info);
}

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString *)message {
    if(code == AIRBRoomChannelFailedToEnterRoom){
//        self.enterFailedBlock(message);
//        self.enterFailedBlock = nil;
//        self.enterSucceedBlock = nil;
        
        NSString* delegateMsg = [NSString stringWithFormat:@"进入房间失败:(0x%lx, %@)", (long)code, message];
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
    } else{
        NSString* delegateMsg = [NSString stringWithFormat:@"出错了:(0x%lx, %@)", (long)code, message];
        DELEGATE_ACTION_2ARG(uiMakeToast, delegateMsg, type, ASCRBClassroomToastTypeError);
    }
    
    DELEGATE_ACTION_2ARG(uiActionOnAIRBRoomChannelErrorWithCode, code, message, message);
}

#pragma mark - AIRBRoomEngineDelegate

- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(NSDictionary *)info {
    LOG("ASLSBScene::onAIRBRoomEngineEvent(%ld, %@)", (long)event, info);
    switch (event) {
        case AIRBRoomEngineEventEngineStarted:
            [[AIRBRoomEngine sharedInstance] loginWithUserID:self.appInitConfig4RoomEngine.userID];
            break;
        case AIRBRoomEngineEventEngineLogined: {
            if (self.onGlobalInitSuccess) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.onGlobalInitSuccess();
                });
            }
        }
            break;
        default:
            break;
    }
}

- (void) onAIRBRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString *)msg {
    if (self.onGlobalInitFailure) {
        self.onGlobalInitFailure([NSString stringWithFormat:@"登录出错:(%ld, %@)", (long)code, msg]);
    }
}

- (void) onAIRBRoomEngineRequestToken:(void (^)(AIRBRoomEngineAuthToken * _Nonnull))onTokenGotten {
    NSString* path = [NSString stringWithFormat:@"%@/api/login/getToken", self.appInitConfig4RoomEngine.appServerUrl];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&appKey=%@&userId=%@&deviceId=%@", path, self.roomEngineConfig.appID, self.roomEngineConfig.appKey, self.appInitConfig4RoomEngine.userID, self.roomEngineConfig.deviceID];
    
    NSString* dateString = [AIRBUtility currentDateString];
    NSString* nonce = [AIRBUtility randomNumString];
    
    NSDictionary* headers = @{
        @"a-app-id" : @"imp-room",
        @"a-signature-method" : @"HMAC-SHA1",
        @"a-signature-version" : @"1.0",
        @"a-timestamp" : dateString,
        @"a-signature-nonce" : nonce,
    };
    
    NSDictionary* params = @{
        @"appId" : self.roomEngineConfig.appID,
        @"appKey" : self.roomEngineConfig.appKey,
        @"userId" : self.appInitConfig4RoomEngine.userID,
        @"deviceId" : self.roomEngineConfig.deviceID
    };
        
    NSString* signedString = [AIRBUtility getSignedRequestStringWithSecret:self.appInitConfig4RoomEngine.appServerSignSecret method:@"POST" path:path parameters:params headers:headers];
    
    NSURL* url = [[NSURL alloc] initWithString:s];
    
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"imp-room" forHTTPHeaderField:@"a-app-id"];
    [request setValue:@"HMAC-SHA1" forHTTPHeaderField:@"a-signature-method"];
    [request setValue:@"1.0" forHTTPHeaderField:@"a-signature-version"];
    [request setValue:signedString forHTTPHeaderField:@"a-signature"];
    [request setValue:dateString forHTTPHeaderField:@"a-timestamp"];
    [request setValue:nonce forHTTPHeaderField:@"a-signature-nonce"];
    
    NSURLSession* session = [NSURLSession sharedSession];
    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        AIRBRoomEngineAuthToken* token = nil;
        if (data && !error) {
            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
                                                                options:NSJSONReadingMutableContainers
                                                                  error:nil];
            
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 1 && [resultDic valueForKey:@"accessToken"] && [resultDic valueForKey:@"refreshToken"]) {
                    token = [[AIRBRoomEngineAuthToken alloc] init];
                    token.accessToken = [NSString stringWithString:[resultDic valueForKey:@"accessToken"]];
                    token.refreshToken = [NSString stringWithString:[resultDic valueForKey:@"refreshToken"]];
                }
            }
        }
        onTokenGotten(token);
    }];
    [task resume];
}

- (void) onLog:(NSString *)message {
    LOG("%@", message);
}

@end
