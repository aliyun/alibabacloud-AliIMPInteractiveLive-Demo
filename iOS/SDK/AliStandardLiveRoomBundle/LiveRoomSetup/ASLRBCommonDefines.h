//
//  ASLRBCommonDefines.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/7/20.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, ASLRBUserRole)
{
    ASLRBUserRoleAnchor = 0,  // 直播中的主播身份，进行推流（发起直播）
    ASLRBUserRoleAudience     // 直播中的观众身份，进行拉流（观看直播）
};

typedef NS_ENUM(NSInteger, ASLRBLiveRoomType)
{
    ASLRBLiveRoomTypeNone = 0,
    ASLRBLiveRoomTypeLivePushing,      // 主播正在进行推流的直播间
    ASLRBLiveRoomTypeLivePlaying,      // 直播正在进行拉流的直播间
    ASLRBLiveRoomTypeLivePlayback      // 直播回放观看的直播间
};

typedef NS_ENUM(NSInteger, ASLRBEvent)
{
    ASLRBAnchorEventLivePusherStarted = 0,    // 主播端：直播开始（推流开始）事件，info字段会携带liveID
    ASLRBAnchorEventLivePusherStreamingConnectFailed, // 主播端：推流建连失败
    ASLRBAnchorEventLivePusherStreamingPoorNetworkStart, //主播端推流遭遇弱网
    ASLRBAnchorEventLivePusherStreamingPoorNetworkEnd, //主播端推流从弱网状态恢复
    ASLRBAnchorEventLivePusherStreamingReconnectStart, //主播端推流网络开始重连
    ASLRBAnchorEventLivePusherStreamingReconnectSuccess, //主播端推流网络重连成功
    ASLRBAnchorEventLivePusherStreamingReconnectFailed, //主播端推流网络重连失败，不再重连
    ASLRBAnchorEventLivePusherStreamingNetworkConnectionLost, //主播端推流网络断开
    ASLRBAnchorEventLivePusherUploadBitrateUpdated, //主播端实时码率通知，单位kb
    ASLRBAnchorEventLiveEnded,        // 主播端，收到直播停止消息
    
    ASLRBAudienceEventLivePlayerStartPlaying, // 观众端的直播开始播放
    ASLRBAudienceEventLivePlayerStartLoading, // 观众端观看直播过程中出现卡顿
    ASLRBAudienceEventLivePlayerEndLoading, // 观众端观看直播过程中卡顿结束
    ASLRBAudienceEventLivePlayerPrepared, //观众端拉流播放器准备好了
    ASLRBAudienceEventLiveNotExit,          // 观众端收到了直播不存在的消息
    ASLRBAudienceEventLiveNotStarted,        // 观众端收到了直播已创建但未开始的消息
    ASLRBAudienceEventSwitchAudienceSucceeded,  // 切换观众成功
    ASLRBAudienceEventLiveEnded,            //观众端，收到直播结束的消息
    ASLRBAudienceEventFloatingPlayWindowTapped,  //观众端小窗播放时，点击小窗触发的事件
    ASLRBAudienceEventLivePushStarted,     //观众端收到主播端推流开始的消息
    ASLRBAudienceEventLivePushStopped,     //观众端收到主播端推流结束的消息
    ASLRBAudienceEventLivePlayerDownloadBitrateUpdated, // 观众端播放拉流的实时下载带宽，单位kb
    ASLRBAudienceEventLivePlayerVideoSizeChanged, // info中会包含具体的宽高值
    
    ASLRBCommonEventExitButtonDidClicked,     // 退出按钮点击事件
    ASLRBCommonEventShareButtonDidClicked,    // 分享按钮点击事件
    ASLRBCommonEventCustomMessageReceived,    // 收到自定义消息，info字段会携带自定义消息内容
    ASLRBCommonEventLiveDataUpdated,          // 直播数据（在线人数、点赞数）更新，info字段会携带更新后数据
    ASLRBCommonEventYourCommentsBannedOrNot,    // 个人禁言/取消禁言，info字段携带是否禁言的标志布尔值
    ASLRBCommonEventAllCommentsBannedOrNot,    // 全员禁言/取消禁言，info字段携带是否禁言的标志布尔值
    ASLRBCommonEventYouWereKickedOut,             // 你被(管理员)踢出当前直播间
    ASLRBCommonEventLiveExtensionUpdated,     // 收到直播扩展字段更新的消息，info字段会携带具体内容
    ASLRBCommonEventLiveStarted,              // 直播开始了（仅业务状态，非流状态）
    ASLRBCommonEventViewWillAppear,           // 视图控制器的viewWillAppear
    ASLRBCommonEventViewWillDisappear,        // 视图控制器的viewWillDisappear
    ASLRBCommonEventViewDidAppear,           // 视图控制器的viewDidAppear
    ASLRBCommonEventViewDidDisappear,        // 视图控制器的viewDidDisappear
    ASLRBCommonEventViewOrientationChanged,  // 界面方向改变，info字段会携带具体方向信息orientation，分别为portrait、landscapeLeft和landscapeRight，对应的是UIInterfaceOrientation中的三个方向
    ASLRBCommonEventFaceBeautyViewControllerWillAppear, //  将要唤起美颜面板
    ASLRBCommonEventFaceBeautyViewControllerWillDisappear, // 将要收起美颜面板
    
    ASLRBCommonEventForcedToDisconnect, // 被强制下线(在其他设备登录)
};

typedef NS_ENUM(NSInteger, ASLRBLiveRoomError)
{
    ASLRBLiveRoomErrorUpdateTitleFailure = 0x000,   // 更新标题失败
    ASLRBLiveRoomErrorLivePusherError,          // 主播端：推流错误
    ASLRBLiveRoomErrorLivePlayerError,          // 观众端：播放错误
    ASLRBLiveRoomErrorEnterRoomFailedWhenKicked, // 观众端：被踢出直播间后再次尝试进入会触发当前错误
    ASLRBLiveRoomErrorOthers,                   // 其他错误
    
    ASLRBLiveRoomCommentSentInnerError = 0x700, // 发送弹幕内部错误
    ASLRBLiveRoomCommentSentInvalidChatID, // 无效chatID
    ASLRBLiveRoomCommentSentFrequencyExceedsLimit, // 发送弹幕过于频繁
    ASLRBLiveRoomCommentLengthExceedsLimit,  // 发送弹幕长度超限
};

typedef NS_ENUM(NSUInteger, ASLRBLinkMicViewContentMode) {
    ASLRBLinkMicViewContentModeAuto          = 0,  // 自动模式
    ASLRBLinkMicViewContentModeFill          = 1,  // 不保持比例平铺
    ASLRBLinkMicViewContentModeAspectFit     = 2,  // 保持比例，黑边
    ASLRBLinkMicViewContentModeAspectFill    = 3,  // 保持比例填充，需裁剪
};

typedef NS_ENUM(NSInteger, ASLRBEnumBypassLiveLayoutType){
    ASLRBEnumBypassLiveLayoutTypeOnePeer = 1,     // 相机流一宫格
    ASLRBEnumBypassLiveLayoutTypeFivePeer = 2,    // 相机流五宫格（一大四小）
    ASLRBEnumBypassLiveLayoutTypeNinePeer = 3,    // 相机流九宫格
    ASLRBEnumBypassLiveLayoutTypeScreenShare = 4, // 屏幕共享流
};
