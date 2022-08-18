//
//  ASLSBScene.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2021/10/14.
//

#import <UIKit/UIKit.h>
#import "Model/ASLSBCommonDefines.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "Model/ASLSBStudentListItemModel.h"
#import "LinkMic/ASLSBLinkMicDelegate.h"
#import "Model/ASLSBCommonDefines.h"
#import "ASLSBScene.h"
#import "Model/ASLSBSceneModel.h"
#import "Model/ASLSBSceneUserModel.h"
#import "Model/ASLSBAppInitConfig.h"
#import "Model/ASLSBSceneInitConfig.h"

NS_ASSUME_NONNULL_BEGIN

@protocol ASLSBSceneUIDelegate <NSObject, ASLSBLinkMicDelegate>

#pragma mark ASCRBClassroomUIAction

- (void) uiRefreshStudentsListView; // 刷新成员列表
- (void) uiActionWhenSceneRoomEntered;
- (void) uiActionWhenSceneRoomLeft;
- (void) uiActionWhenSceneNotStarted;
- (void) uiActionWhenSceneStarted;
- (void) uiActionWhenSceneHasStarted;
- (void) uiActionWhenSceneHasEnded;
- (void) uiActionWhenSceneEnded;
- (void) uiActionWhenNoticeUpdated:(NSString*)notice;   // 公告更新
- (void) uiActionWhenChatInfoGotten:(BOOL)succeeded errorMessag:(NSString*)errorMessage;

- (void) uiActionOnAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info;
- (void) uiActionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary*)info;
- (void)uiActionOnAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message;

- (void) uiMakeToast:(NSString*)message type:(ASCRBClassroomToastType)type;
- (void) uiActionOnVideoRecording;

#pragma mark ASCRBClassroomTeacherUIAction
- (void) uiActionOnSetupSucceed;
- (void) uiActionOnSetupFailed:(NSString *)errorMessage;

#pragma mark ASCRBClassroomTeacherBigClassUIAction
- (void) uiActionWhenRecordStarted;
- (void) uiActionWhenRecordEnded;
- (void) uiActionOnAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info;
- (void) uiActionOnAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary *)info;
- (void) uiActionOnAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view type:(AIRBRTCVideoViewType)type;

#pragma mark ASCRBClassroomStudentUIAction
//- (void) uiActionOnSetupSucceed;
//- (void) uiActionOnSetupFailed:(NSString *)errorMessage;
- (void) uiActionOnAIRBVodPlayerEvent:(AIRBVodPlayerEvent)event info:(NSDictionary *)info;
//- (void) uiActionOnAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info;

#pragma mark ASCRBClassroomStudentBigClassUIAction
//- (void) uiActionWhenjoinedRTC;
//- (void) uiActionAfterLeftRTC;
- (void) uiActionJoinLinkMic;
- (void) uiActionOnAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info;
- (void) uiActionOnAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
//- (void) uiActionOnAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary *)info;
//- (void) uiActionOnAIRBRTCRemotePeerViewAvailable:(NSString *)userID view:(UIView *)view type:(AIRBRTCVideoViewType)type;

#pragma mark ASCRBClassroomTeacherBigClassUIAction
- (void) uiActionOnAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info;
- (void) uiActionOnAIRBLivePusherError:(AIRBErrorCode)errorCode message:(NSString*)errorMessage;
- (void) onASLSBPusherEngineEvent:(ASLSBPusherEngineEvent)event info:(NSDictionary*)info;

@end

@interface ASLSBScene : NSObject

//- (ASLSBScene*) initWithAppInitConfig:(ASCRBAppInitConfig*)appInitConifg
//                                        classInitConfig:(ASCRBClassInitConfig*)classInitConfig;

@property(nonatomic, weak) id<ASLSBSceneUIDelegate> delegate;

@property(nonatomic, strong) ASLSBAppInitConfig* appInitConfig;
@property(nonatomic, strong) ASLSBSceneInitConfig* sceneInitConfig;

@property(nonatomic, strong) ASLSBSceneModel* sceneModel;
@property(nonatomic, strong) ASLSBSceneUserModel* userModel;

@property(nonatomic, strong) id<AIRBRoomChannelProtocol> room;
@property(nonatomic, assign) BOOL roomEntered;
@property(nonatomic, copy) void(^enterSucceedBlock)(NSString* classroomID);
@property(nonatomic, copy) void(^enterFailedBlock)(NSString* errorMsg);


// 学员列表相关
@property(nonatomic, strong) NSMutableArray* studentsListDataSource;
@property(nonatomic, strong) NSMutableDictionary* studentsLists;
@property(nonatomic, strong) NSLock* studentListLock;
@property(nonatomic, assign) BOOL hasMoreRoomMembers;
@property(nonatomic, assign) BOOL hasMoreMembersJoinedRTCAlready;
@property(nonatomic, assign) BOOL hasMoreMembersApplyingToJoinRTC;
@property(nonatomic, assign) int32_t currentRoomMemberListPageNum;
@property(nonatomic, assign) int32_t currentMemberJoinedRTCAlreadyListPageNum;
@property(nonatomic, assign) int32_t currentMemberApplyingToJoinRTCListPageNum;

// 登录及初始化相关
/**
 * lwp登陆，只需要调用一次；收到onSuccess后再进行下一步；
 * @param config 初始化需要的配置信息，具体见ASCRBAppInitConfig
 * @param onSuccess  初始化成功后回调；注意避免block内强引用外部对象造成循环引用
 * @param onFailure  初始化失败时回调，会有具体的错误信息；注意避免block内强引用外部对象造成循环引用
 */
- (void) roomEngineLoginWithConfig:(ASLSBAppInitConfig*)config
                         onSuccess:(void(^)(void))onSuccess
                         onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 用来做进入课堂页面前的准备工作
 * @note 成功后会收到ASCRBClassroomEventSetupSucceed事件，注意在收到成功事件之后才可以push当前vc
 */
- (void) setup;
-(instancetype)initWithClassAppConfig:(ASLSBAppInitConfig*)appConfig classConfig:(ASLSBSceneInitConfig*)classConfig;
- (instancetype)initWithSceneClassInfo:(NSDictionary*)response role:(ASCRBClassroomRole)role;
- (instancetype)initWithSceneLiveInfo:(NSDictionary*)response role:(int)role userID:(NSString*)userID nickName:(NSString*)nick userExtension:(NSDictionary<NSString*,NSString*>*) userExtension conferenceId:(NSString*)conferenceId;
- (instancetype)initWithScene:(ASLSBSceneModel*)sceneModel
                            user:(ASLSBSceneUserModel*)userModel;
//- (void) enterClassroomOnSuccess:(void(^)(NSString* roomID))onSuccess onFailure:(void(^)(NSString* error))onFailure;

// 进出房间相关
-(void) enterRoom;
-(void) actionWhenSceneRoomEntered;
-(void) actionWhenSceneRoomLeft;
-(void) leaveRoom;

// 学员列表相关
-(void) queryMoreRoomMemberInfoOnce;
-(void) queryMoreMembersJoinedRTCAlreadyInfoOnce;
-(void) updateStudent:(NSString*)userID toNewStatus:(ASCRBStudentStatus)status;
-(void) cleanStudentListStatus;

// 消息相关
-(void) actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType
                                      data:(NSDictionary*)data
                                      info:(NSDictionary*)info;

// 主动动作相关
- (void)sendMessage:(NSString *)message
          onSuccess:(void (^)(void))onSuccess
          onFailure:(void (^)(NSString * errorMessage))onFailure;

#pragma mark roleAction
- (void) onSetupSucceed;
- (void) onSetupFailed:(NSString *)errorMessage;
- (void) enterSceneClassOnSuccess:(void(^)(NSString* classID))onSuccess
                        onFailure:(void(^)(NSString* errorMessage))onFailure;

-(void) actionWhenSceneStarted;
-(void) actionWhenSceneHasStarted;
-(void) actionWhenSceneEnded;

@end

NS_ASSUME_NONNULL_END
