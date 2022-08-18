//
//  ASLRBLiveRoomAudienceProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/3.
//

#import <Foundation/Foundation.h>

@class ASLRBLinkMicUserModel;
typedef NS_ENUM(NSUInteger, ASLRBLinkMicViewContentMode);

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBFloatingPlayWindowProtocol;

@protocol ASLRBLiveRoomAudienceProtocol <NSObject>
/**
 * 直播观看时画面的伸缩模式，目前仅支持三种，即UIViewContentModeScaleToFill, UIViewContentModeScaleAspectFit和UIViewContentModeScaleAspectFill
 * 默认为UIViewContentModeScaleAspectFit；
 */
@property (assign, nonatomic) UIViewContentMode playerViewContentMode;

/**
 * 播放画面view，如要更改frame请使用Masonry
 */
@property (weak, nonatomic) UIView* playerView;

/**
 * 小窗模式下的背景view，用来承载播放器view；
 */
@property (strong, nonatomic) UIView<ASLRBFloatingPlayWindowProtocol>* floatingPlayWindow;

/**
 * 在不退出当前直播间的情况下暂停直播播放；要继续播放的时候，请调用refreshPlayer
 */
- (void) pausePlayer;

/**
 * 在不退出当前直播间的情况下重新加载视频播放
 * 仅观众端调用有效；
 */
- (void) refreshPlayer;

/**
 * 静音或者不静音
 */
- (void)mute:(BOOL)mute;

/**
 * 切换观众，主播侧调用无效；
 * @param userID 新的用户ID
 * @param nick     新的用户昵称
 */
- (void) switchAudience:(NSString*)userID nick:(NSString*)nick;

/**
 * 切换到小窗播放模式
 * @param enter 是否切换到小窗播放模式，YES表示从全屏播放切换到小窗，NO表示从小窗模式切换到全屏播放
 */
- (void) enterFloatingMode:(BOOL)enter;

/** ******************************* 直播间观众横竖屏切换 ******************************** */
/**
 * 是否开启横竖屏切换，默认关闭，主播侧调用无效；
 */
@property (nonatomic, assign) BOOL enableViewRotation;

/**
 * 横竖屏切换，主播侧调用无效；
 * @note 横竖屏切换的时候会触发{@link ASLRBCommonEventViewOrientationChanged}事件
 */
- (void) switchViewOrientation;


/** ********************************** 直播间观众连麦 ************************************ */
/** *********** 需要在能够连麦的直播中使用(在创建直播时开启连麦功能) *********** */

/**
 * 连麦时本地摄像头的打开状态
 */
@property (assign, nonatomic, readonly, getter=isCameraOpened) BOOL cameraOpened;

/**
 * 连麦时本地麦克风的打开状态
 */
@property (assign, nonatomic, readonly, getter=isMicOpened) BOOL micOpened;

/**
 * 连麦时是否允许打开麦克风（被禁音状态）
 */
@property (assign, nonatomic, readonly, getter=isSelfMicAllowed) BOOL selfMicAllowed DEPRECATED_MSG_ATTRIBUTE ("已废弃,请使用新属性 allMicAllowed");

/**
 * 主播是否允许观众打开麦克风
 */
@property (assign, nonatomic, readonly, getter=isAllMicAllowed) BOOL allMicAllowed;

/**
 * 摄像头本地预览画面
 */
@property (strong, nonatomic, readonly) UIView* linkMicLocalPreview;

/**
 * 已连麦的成员
 */
@property (strong, nonatomic, readonly) NSDictionary<NSString*, ASLRBLinkMicUserModel*>* linkMicJoinedUsers;

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

/**
 * 开启本地麦克风，默认开启
 */
- (void) linkMicOpenMic;

/**
 * 关闭本地麦克风
 */
- (void) linkMicCloseMic;

/**
 * 处理主播的加入连麦邀请
 * 在{@link onASLRBLinkMicInvited:userInvitedList:}事件中使用
 * @param agree YES: 同意邀请; NO: 拒绝邀请;
*/
- (void) linkMicHandleInvite:(BOOL)agree;

/**
 * 申请连麦
 * 发出申请后, 接收方会收到{@link onASLRBLinkMicApplied:userList:}事件
 */
- (void) linkMicApply;

/**
 * 取消申请连麦
 * 取消申请后, 接收方会收到{@link onASLRBLinkMicApplyCanceled:}事件
*/
- (void) linkMicCancelApply;

/**
 * 处理申请连麦的响应结果
 * 在{@link onASLRBLinkMicApplyResponse:user:}事件中使用
 * @param join 是否加入连麦
 */
- (void) linkMicHandleApplyResponse:(BOOL)join;

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
 * 设置预览画面的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetPreviewContentMode:(ASLRBLinkMicViewContentMode)contentMode;

/**
 * 设置远端视频流的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetRemoteCameraStreamContentMode:(ASLRBLinkMicViewContentMode)contentMode;

@end

NS_ASSUME_NONNULL_END
