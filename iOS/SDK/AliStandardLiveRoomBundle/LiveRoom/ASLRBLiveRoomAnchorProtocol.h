//
//  ASLRBLiveRoomAnchorProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/3.
//

#import <Foundation/Foundation.h>
#import <AliStandardLiveRoomBundle/ASLRBCommonDefines.h>

NS_ASSUME_NONNULL_BEGIN

@class ASLRBLiveInitConfig, ASLRBLiveBusinessInfo;
@class ASLRBLinkMicUserModel, ASLRBCustomBypassLiveLayoutUserModel;

@protocol ASLRBLiveRoomAnchorProtocol <NSObject>

/**
 * 直播推流时预览画面的伸缩模式，目前仅支持三种，即UIViewContentModeScaleToFill, UIViewContentModeScaleAspectFit和UIViewContentModeScaleAspectFill
 * 默认为UIViewContentModeScaleAspectFill；
 */
@property (assign, nonatomic) UIViewContentMode previewContentMode;

/**
 * 主播侧渲染预览画面的view，非必须；
 *  说明： 默认情况下，外部不需要设置预览渲染的view，内部会自动生成一个铺满全屏的UIView；
 *       如果不想使用默认的全屏铺满UIView，可以通过此接口设置自定义的UIView进来而且建议其宽度跟屏幕宽度保持一直，高度 = 宽度 / 9 * 16
 *       需要在setup之前设置；
 */
@property (nonatomic, strong) UIView* externalLivePushPreview;

/**
 * 主播端：手动开播，移除livePrestartCustomizedViewHolder，展示开播后的UI界面;
 * @param config 本场直播相关的配置
 */
- (void) startLiveAndUpdateConfig:(ASLRBLiveInitConfig*)config;

/**
 * 更新直播相关信息；仅开播后在主播侧调用有效；
 * @param config 具体见ASLRBLiveInitConfig
 */
- (void) updateLiveConfig:(ASLRBLiveInitConfig*)config
                onSuccess:(void (^)(void))onSuccess
                onFailure:(void (^)(NSString* errorMessage))onFailure DEPRECATED_MSG_ATTRIBUTE("建议使用updateLiveBusinessInfo");

/**
 * @brief 更新直播相关信息；仅开播后在主播侧调用有效；
 * @param info 具体见ASLRBLiveBusinessInfo
 */
- (void) updateLiveBusinessInfo:(ASLRBLiveBusinessInfo*)info
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;
                
/**
 * 切换前后摄像头，主播端调用;
 */
- (void) switchCamera;

/**
 * 是否打开闪光灯;
 */
- (void) setFlash:(BOOL)open;

/**
 * 展示/收起美颜面板（已经内部定义好的一个ViewController），主播端调用，建议高度200;
 * 注意：从2.0.0版本开始，美颜默认关闭（即调用当前方法不能展示出美颜面板），如需打开需要在阿里云低代码音视频控制台配置打开
 */
- (void) showBeautyPanel;

/**
 * 切换麦克风状态（关闭/开启），默认为开启，主播端调用;
 */
- (void) toggleMutedMicrophone;

/**
 * 开启/关闭直播画面镜像（包括本地画面预览和观众看到的直播画面），默认为关闭，主播端调用;
 * @param mirror 是否开启摄像头画面镜像，为YES则开启，为NO则关闭
 */
- (void) mirrorLiveVideo:(BOOL)mirror;

/**
 * 暂停/恢复直播推流，默认为推流状态，主播端调用
 * @param pause 是否暂停直播推流，为YES则暂停，为NO则恢复推流
 */
- (void) pauseLiveStreaming:(BOOL)pause;

/**
 * 重新启动推流，适合在重试失败（收到ASLRBAnchorEventLivePusherStreamingReconnectFailed事件）之后调用；仅主播端调用；
 */
- (void) restartLiveStreaming;

/**
 * @brief 开启/关闭某user的禁言
 * @param userID 要开启或关闭禁言的用户id
 * @param banSeconds 要禁言的秒数（注意，取消禁言时可不传入；注意，需要永久禁言时可传入0）
 * @param ban YES即开启禁言，NO即关闭禁言
 */
- (void) banCommentsOfUser:(NSString*)userID bannedSeconds:(int32_t)banSeconds ban:(BOOL)ban;

/**
 * @brief 开启/关闭全员禁言，主播端调用
 * @param ban 是否开启禁言全员禁言，为YES则开启，为NO则关闭
 */
- (void) banAllComments:(BOOL)ban;

/**
 * @brief 主播踢人
 * @param kickedSeconds 踢出时长（踢出后多久才能进）
 */
- (void) kickUser:(NSString*)userID
    kickedSeconds:(int32_t)kickedSeconds
        onSuccess:(void(^)(void))onSuccess
        onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * @brief 获取本直播间内人员列表，具体信息在response中的ASLRBLiveRoomUserModel
 * @param pageNum  分页index，从1开始
 * @param pageSize  每页的数量
 */
- (void) getLiveUserListWithPageNum:(int32_t)pageNum
                           pageSize:(int32_t)pageSize
                          onSuccess:(void (^)(NSDictionary* response))onSuccess
                          onFailure:(void (^)(NSString* errorMessage))onFailure;

/** ********************************** 直播间主播连麦 ************************************ */
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
 * 打开摄像头
 */
- (void) linkMicOpenCamera;

/**
 * 关闭摄像头
 */
- (void) linkMicCloseCamera;

/**
 * 邀请观众加入连麦
 * 发出邀请后, 接收方会收到{@link onASLRBLinkMicInvited:userInvitedList:}事件
 * @param userIDs 被邀请的用户ID
 */
- (void) linkMicInvite:(NSArray<NSString*>*)userIDs;

/**
 * 取消邀请
 * 发出邀请后, 接收方会收到{@link onASLRBLinkMicInviteCanceledForMe}事件
 * @param userIDs 被取消邀请的用户ID
 */
- (void) linkMicCancelInvite:(NSArray<NSString*>*)userIDs;

/**
 * 处理观众的加入连麦申请
 * 在{@link onASLRBLinkMicApplied:userList:}事件中使用
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
 * 设置预设的旁路直播布局方式（麦下观众观看的画面布局）
 * @param type 预设的布局样式
 * @param userIDs 要展示的用户ID列表:从左上到右下依次排序, 为@""则该位置为空
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) linkMicSetEnumBypassLiveLayout:(ASLRBEnumBypassLiveLayoutType)type
                                userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                              onSuccess:(void(^)(void))onSuccess
                              onFailure:(void(^)(NSString* error))onFailure;

/**
 * 设置自定义的旁路直播布局方式（麦下观众观看的画面布局）
 * @param userModels 要展示的视频流，具体见{@link ASLRBCustomBypassLiveLayoutUserModel}
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) linkMicSetCustomBypassLiveLayout:(NSArray<ASLRBCustomBypassLiveLayoutUserModel*>*) userModels
                                onSuccess:(void(^)(void))onSuccess
                                onFailure:(void(^)(NSString* error))onFailure;

/**
 * 设置本地预览画面的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetPreviewContentMode:(ASLRBLinkMicViewContentMode)contentMode;

/**
 * 设置本地观看远端视频流的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetRemoteCameraStreamContentMode:(ASLRBLinkMicViewContentMode)contentMode;

@end

NS_ASSUME_NONNULL_END
