//
//  ASLRBLinkMicDelegate.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/1/6.
//

#import <Foundation/Foundation.h>
#import "ASLRBLinkMicUserModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, ASLRBLinkMicEvent)
{
    ASLRBLinkMicEventLocalPreviewStarted = 0,   // 本地摄像头预览开始
    ASLRBLinkMicEventLocalJoinSucceeded = 1,    // 本地加入连麦成功
    ASLRBLinkMicEventLocalLeaveSucceeded = 2,   // 本地退出连麦成功
};

typedef NS_ENUM(NSInteger, ASLRBLinkMicError)
{
    ASLRBLinkMicErrorInternal = 0x101,          // 内部错误
    ASLRBLinkMicErrorLinkMicNotEnabled = 0x102, // 本场直播未开启连麦功能
    ASLRBLinkMicErrorNotAllowedToOpenMic = 0x103,   // 全体静音中，无法打开麦克风
};

@protocol ASLRBLinkMicDelegate <NSObject>

/**
 * 连麦触发的事件
 * @param event 事件类型
 * @param info 事件信息
 */
- (void) onASLRBLinkMicEvent:(ASLRBLinkMicEvent)event info:(NSDictionary*)info;

/**
 * 连麦出现报错
 * @param error 错误类型
 * @param msg 错误信息
 */
- (void) onASLRBLinkMicError:(ASLRBLinkMicError)error message:(NSString*)msg;

/**
 * 有成员加入连麦
 * @param userList 加入连麦的成员
 */
- (void) onASLRBLinkMicUserJoined:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 有成员加入连麦
 * @param isNewJoined YES: (我加入连麦之后) 新加入连麦的; NO: (我加入连麦之前) 已经在连麦中的; （已废弃，该参数无效）
 * @param userList 加入连麦的成员
 */
- (void) onASLRBLinkMicUserJoined:(BOOL)isNewJoined userList:(NSArray<ASLRBLinkMicUserModel*>*)userList DEPRECATED_MSG_ATTRIBUTE ("建议使用新接口 - (void) onASLRBLinkMicUserJoined:");

/**
 * 有成员退出连麦
 * @param userList 退出连麦的成员
 */
- (void) onASLRBLinkMicUserLeft:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 远端摄像头画面可用
 * @param userID 摄像头视频流可用的成员
 * @param isAnchor 该成员是否是主播
 * @param view 摄像头视频流画面
 */
- (void) onASLRBLinkMicCameraStreamAvailable:(NSString*)userID isAnchor:(BOOL)isAnchor view:(UIView*)view;

/**
 * 远端摄像头开关状态改变
 * @param userID 状态改变的成员
 * @param open YES: 开启; NO: 关闭;
 */
- (void) onASLRBLinkMicRemoteCameraStateChanged:(NSString*)userID open:(BOOL)open;

/**
 * 远端麦克风开关状态改变
 * @param userIDList 状态改变的成员
 * @param open YES: 开启; NO: 关闭;
 */
- (void) onASLRBLinkMicRemoteMicStateChanged:(NSArray<NSString*>*)userIDList open:(BOOL)open;

/**
 * 收到连麦邀请
 * @param inviter 邀请者
 * @param userInvitedList 被邀请者
 */
- (void) onASLRBLinkMicInvited:(ASLRBLinkMicUserModel*)inviter userInvitedList:(NSArray<ASLRBLinkMicUserModel*>*)userInvitedList;

/**
 * 取消了对我的邀请 (主播邀请我之后, 我还没接受就被主播取消)
 */
- (void) onASLRBLinkMicInviteCanceledForMe;

/**
 * 邀请被拒绝
 * @param userList 被拒绝的成员
 */
- (void) onASLRBLinkMicInviteRejected:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 收到连麦申请
 * @param isNewApplied YES: (我加入连麦之后) 新申请连麦的; NO: (我加入连麦之前) 已经申请连麦的;
 * @param userList 申请连麦的成员
 */
- (void) onASLRBLinkMicApplied:(BOOL)isNewApplied userList:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 连麦申请被取消
 * @param userList 取消申请的成员
 */
- (void) onASLRBLinkMicApplyCanceled:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 连麦申请发出后, 收到主播的处理结果
 * @param approve YES: 同意; NO: 拒绝;
 * @param userID 申请被处理的成员
 */
- (void) onASLRBLinkMicApplyResponse:(BOOL)approve user:(NSString*)userID;

/**
 * 有成员被踢出连麦
 * @param userList 被踢出的成员
 */
- (void) onASLRBLinkMicKicked:(NSArray<ASLRBLinkMicUserModel*>*)userList;

/**
 * 自己的麦克风被(取消)禁音
 * @param allowed true: 取消禁音; false: 禁音
 */
- (void) onASLRBLinkMicSelfMicAllowed:(BOOL)allowed DEPRECATED_MSG_ATTRIBUTE ("已废弃,请使用新接口 - (void) onASLRBLinkMicSelfMicClosedByAnchor; - (void) onASLRBLinkMicAnchorInviteToOpenMic; - (void) onASLRBLinkMicAllMicAllowed:;");

/**
 * 自己的麦克风被主播关闭
 */
- (void) onASLRBLinkMicSelfMicClosedByAnchor;

/**
 * 主播请求打开你的麦克风
 */
- (void) onASLRBLinkMicAnchorInviteToOpenMic;

/**
 * 主播允许/不允许所有观众打开麦克风, 默认为允许
 * @param allowed true: 允许打开麦克风; false: 不允许打开麦克风
 * @note 当主播不允许所有观众打开麦克风时, 如果观众当前麦克风为开启状态, 内部会关闭麦克风; 如果观众主动打开麦克风, 会通过{@link -(void) onASLRBLinkMicError:message:}中的ASLRBLinkMicErrorNotAllowedToOpenMic事件进行报错
 */
- (void) onASLRBLinkMicAllMicAllowed:(BOOL)allowed;

@end

NS_ASSUME_NONNULL_END
