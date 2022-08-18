//
//  ASLSBLinkMicProtocol.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/1/6.
//

#import <Foundation/Foundation.h>
#import "../Model/ASLSBSceneUserModel.h"
#import "../LinkMic/ASLSBLinkMicUserModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, AIRBRTCBypassLiveLayoutType);
typedef NS_ENUM(NSInteger, AIRBRTCBypassLiveResolutionType);
@class AIRBRTCBypassLiveLayoutPeerVideoModel;

typedef NS_ENUM(NSInteger, ASLSBLinkMicState)
{
    ASLSBLinkMicStateReady = 0,
    ASLSBLinkMicStateApplying,
    ASLSBLinkMicStateInvited,
    ASLSBLinkMicStateJoined,
    ASLSBLinkMicStateKicked,
};

typedef NS_ENUM(NSUInteger, ASLSBLinkMicViewContentMode) {
    ASLSBLinkMicViewContentModeAuto          = 0,  // 自动模式
    ASLSBLinkMicViewContentModeFill          = 1,  // 不保持比例平铺
    ASLSBLinkMicViewContentModeAspectFit     = 2,  // 保持比例，黑边
    ASLSBLinkMicViewContentModeAspectFill    = 3,  // 保持比例填充，需裁剪
};

//typedef NS_ENUM(NSInteger, ASLSBEnumBypassLiveLayoutType){
//    ASLSBEnumBypassLiveLayoutTypeOnePeer = 1,     // 相机流一宫格
//    ASLSBEnumBypassLiveLayoutTypeFivePeer = 2,    // 相机流五宫格（一大四小）
//    ASLSBEnumBypassLiveLayoutTypeNinePeer = 3,    // 相机流九宫格
//    ASLSBEnumBypassLiveLayoutTypeScreenShare = 4, // 屏幕共享流
//};

@protocol ASLSBLinkMicProtocol <NSObject>

@property (assign, nonatomic) ASLSBLinkMicState linkMicState;

/**
 * 连麦时本地摄像头的打开状态
 */
@property (assign, nonatomic, readonly, getter=isCameraOpened) BOOL cameraOpened;

/**
 * 连麦时本地麦克风的打开状态
 */
@property (assign, nonatomic, readonly, getter=isMicOpened) BOOL micOpened;

/**
 * 连麦时是否允许打开麦克风（包括被单独静音和全体静音）
 */
@property (assign, nonatomic, getter=isSelfMicAllowed) BOOL selfMicAllowed DEPRECATED_MSG_ATTRIBUTE ("已废弃,请使用新属性 allMicAllowed");

/**
 * 是否开启了全体静音
 */
@property (assign, nonatomic, getter=isAllMicAllowed) BOOL allMicAllowed;

@property (strong, nonatomic) NSMutableDictionary<NSString*, ASLSBLinkMicUserModel*>* linkMicJoinedUsers;


/**
 * 摄像头本地预览画面
 */
@property (strong, nonatomic) UIView* linkMicLocalPreview;

/**
 * 设置高分辨率视频流（大流）的编码分辨率，默认为640x480
 */
- (void) setVideoStreamTypeHighDimensions:(CGSize)dimensions;

/**
 * 加入连麦
 */
- (void) linkMicJoin;

/**
 * 退出连麦
 */
- (void) linkMicLeave;

/**
 * 打开摄像头
 */
- (void) linkMicOpenCamera;

/**
 * 关闭摄像头
 */
- (void) linkMicCloseCamera;

/**
 * 切换摄像头 (前后置切换)
 */
- (void) linkMicSwitchCamera;

///**
// * 设置预览画面是否开启镜像（仅前置摄像头），默认开启
// * @param enable YES: 开启; NO: 关闭;
// */
//- (void) linkMicSetPreviewMirror:(BOOL)enable;
//
///**
// * 设置视频流是否开启镜像，默认不开启
// * @param enable YES: 开启; NO: 关闭;
// */
//- (void) linkMicSetCameraStreamMirror:(BOOL)enable;

/**
 * 开启本地麦克风，默认开启
 */
- (void) linkMicOpenMic;

/**
 * 关闭本地麦克风
 */
- (void) linkMicCloseMic;

/**
 * 邀请观众加入连麦
 * 发出邀请后, 接收方会收到{@link onASLSBLinkMicInvited:userInvitedList:}事件
 * @param userIDs 被邀请的用户ID
 */
- (void) linkMicInvite:(NSArray<NSString*>*)userIDs;

/**
 * 取消邀请
 * 发出邀请后, 接收方会收到{@link onASLSBLinkMicInviteCanceledForMe}事件
 * @param userIDs 被取消邀请的用户ID
 */
- (void) linkMicCancelInvite:(NSArray<NSString*>*)userIDs;

/**
 * 处理主播的加入连麦邀请
 * 在{@link onASLSBLinkMicInvited:inviter:}事件中使用
 * @param agree YES: 同意邀请; NO: 拒绝邀请;
*/
- (void) linkMicHandleInvite:(BOOL)agree;

/**
 * 申请连麦
 * 发出申请后, 接收方会收到{@link onASLSBLinkMicApplied:}事件
 */
- (void) linkMicApply;

/**
 * 取消申请连麦
 * 取消申请后, 接收方会收到{@link onASLSBLinkMicApplyCanceled:}事件
*/
- (void) linkMicCancelApply;

/**
 * 处理申请连麦的响应结果
 * 在{@link onASLRBLinkMicApplyResponse:user:}事件中使用
 * @param join 是否加入连麦
 */
- (void) linkMicHandleApplyResponse:(BOOL)join;

/**
 * 处理观众的加入连麦申请
 * 在{@link onASLSBLinkMicApplied:userList:}事件中使用
 * @param userID 待处理的用户ID
 * @param agree YES: 同意申请; NO: 拒绝申请;
 */
- (void) linkMicHandleApply:(NSString*)userID agree:(BOOL)agree;

/**
 * 踢出连麦
 * @param userIDs 被踢出的用户ID
 */
- (void) linkMicKick:(NSArray<NSString*>*)userIDs;

/**
 * 退出连麦
 */
- (void) linkMicSetRemoteMicEnable;

/**
 * 退出连麦
 */
- (void) linkMicSetAllRemoteMicEnable;

///**
// * 查询当前已加入连麦的成员列表
// * @param pageNum  第几页 (从1开始)
// * @param pageSize 每页展示多少数据 (最大200)
// */
//- (void) linkMicListJoinedUsers:(int32_t)pageNum
//                pageSize:(int32_t)pageSize
//               onSuccess:(void(^)(AIRBRoomChannelUserListResponse* rsp))onSuccess
//               onFailure:(void(^)(NSString* errorMessage))onFailure;
//
///**
// * 查询正在申请连麦的成员列表
// * @param pageNum  第几页 (从1开始)
// * @param pageSize 每页展示多少数据 (最大200)
// */
//- (void) linkMicListApplyingUsers:(int32_t)pageNum
//                  pageSize:(int32_t)pageSize
//                 onSuccess:(void(^)(AIRBRoomChannelUserListResponse* rsp))onSuccess
//                 onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 开启旁路直播推流
 */
- (void) linkMicStartBypassLiveStreaming:(AIRBRTCBypassLiveResolutionType)resolutionType;

/**
 * 暂停旁路直播推流
 */
- (void) linkMicStopBypassLiveStreaming;

/**
 * 停止旁路直播(同时会停止推流)
 */
- (void) linkMicDestoryBypassLive;

/**
 * 设置预设的旁路直播布局方式
 * @param type 预设的布局样式
 * @param userIDs 要展示的用户ID列表:从左上到右下依次排序, 为@""则该位置为空
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) linkMicSetEnumBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type
                                userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                              onSuccess:(void(^)(void))onSuccess
                              onFailure:(void(^)(NSString* error))onFailure;

/**
 * 设置自定义的旁路直播布局方式
 */
- (void) linkMicSetCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel*>*) peerModels
                                onSuccess:(void(^)(void))onSuccess
                                onFailure:(void(^)(NSString* error))onFailure;

/**
 * 设置预览画面是否开启镜像（仅前置摄像头），默认开启
 * @param enable YES: 开启; NO: 关闭;
 */
- (void) linkMicSetPreviewMirror:(BOOL)enable;

/**
 * 设置视频流是否开启镜像（仅前置摄像头），默认不开启
 * @param enable YES: 开启; NO: 关闭;
 */
- (void) linkMicSetCameraStreamMirror:(BOOL)enable;

/**
 * 设置预览画面的填充方式，默认为ASLSBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetPreviewContentMode:(ASLSBLinkMicViewContentMode)contentMode;

/**
 * 设置远端视频流的填充方式，默认为ASLSBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetRemoteCameraStreamContentMode:(ASLSBLinkMicViewContentMode)contentMode;

@end

NS_ASSUME_NONNULL_END
