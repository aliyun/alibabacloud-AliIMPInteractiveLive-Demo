//
//  AIRBCommonDefines.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/10.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, AIRBLoggerLevel)
{
    AIRBLoggerLevelDebug = 0,
    AIRBLoggerLevelInfo,
    AIRBLoggerLevelWarning,
    AIRBLoggerLevelError,
};

typedef NS_ENUM(NSInteger, AIRBErrorCode)
{
    AIRBEngineFailedToStart = 0x000,
    AIRBEngineFailedToCreateDPSManager,
    AIRBEngineFailedToLogin,
    AIRBEngineFailedToLogout,
    
    AIRBRoomChannelFailedToEnterRoom = 0x100, // Room
    AIRBRoomChannelFailedToGetRoomDetail,
    AIRBRoomChannelFailedToLeaveRoom,
    
    AIRBLivePusherFailedToCreate = 0x200, // LivePusher
    AIRBLivePusherFailedToGetDetail,
    AIRBLivePusherFailedToStartPusherEngine,
    AIRBLivePusherFailedToOpenCamera,
    AIRBLivePusherFailedToConnect,
    AIRBLivePusherFailedToReconnect,
    AIRBLivePusherFailedToSendOutData,
    AIRBLivePusherFailedToOpenMicphone,
    AIRBLivePusherFailedToPublishLive,
    AIRBLivePusherFailedToStopLive,
    AIRBLivePusherConnectionLost,
    
    AIRBLivePlayerFailedToStartPlayWhenNoLiveHere = 0x300,
    AIRBLivePlayerFailedToGetDetail,
    AIRBLivePlayerFailedToStartPlayWithInactiveLive,
    AIRBLivePlayerFailedToPlayWithFatalError,
    
    AIRBWhiteBoardFailedToCreateWhenServiceUnavailable = 0x400,
    AIRBWhiteBoardFailedToCreateWhenRoleInvalid,
    AIRBWhiteBoardFailedToCreateWhenTokenInvalid,
    
    AIRBRTCFailedToJoinWhenNoRTCHere = 0x500,
    AIRBRTCFailedToGetToken,
    AIRBRTCFailedToCreate,
    AIRBRTCFailedWithInternalError,
    AIRBRTCFailedToAddPeers,
    AIRBRTCFailedToRemovePeers,
    AIRBRTCFailedToAcceptWhenCalled,
    AIRBRTCFailedToApplyOrWithdrawJoinning,
    AIRBRTCFailedToLeave,
    AIRBRTCFailedToReportJoinChannelStatus,
    AIRBRTCFailedToReportLeaveChannelStatus,
    AIRBRTCFailedToCreateBypassLive,
    AIRBRTCFailedToPublishBypassLive,
    AIRBRTCFailedToGetBypassLiveDetail,
    AIRBRTCFailedToDestroyBypassLive,
};

typedef NS_ENUM(NSInteger, AIRBRoomEngineEvent)
{
    AIRBRoomEngineEventEngineStarted = 0,
    AIRBRoomEngineEventEngineLogined,
    AIRBRoomEngineEventEngineLogouted,
};

typedef NS_ENUM(NSInteger, AIRBRoomChannelEvent)
{
    AIRBRoomChannelEventEntered = 0,
    AIRBRoomChannelEventMessageReceived,
    AIRBRoomChannelEventLeft,
    AIRBRoomChannelEventRoomInfoGotten,
};

typedef NS_ENUM(NSInteger, AIRBRoomChannelMessageType)
{
    AIRBRoomChannelMessageTypeRoomMembersInOut = 0,
    AIRBRoomChannelMessageTypeRoomTitleUpdated,
    AIRBRoomChannelMessageTypeRoomNoticeUpdated,
    AIRBRoomChannelMessageTypeRoomOneUserKickedOut,
    
    AIRBRoomChannelMessageTypeChatCommentReceived,
    AIRBRoomChannelMessageTypeChatLikeReceived,
    AIRBRoomChannelMessageTypeChatOneUserMutedOrUnmuted,
    
    AIRBRoomChannelMessageTypeLiveCreatedByOther,
    AIRBRoomChannelMessageTypeLiveStartedByOther,
    AIRBRoomChannelMessageTypeLiveStoppedByOther,
    AIRBRoomChannelMessageTypeLiveNotStarted,
    
    AIRBRoomChannelMessageTypePeerJoinRTCSucceeded, // 某人加入了RTC
    AIRBRoomChannelMessageTypePeerJoinRTCFailed, // 某人加入RTC超时或者拒绝加入RTC
    AIRBRoomChannelMessageTypePeerLeaveRTC, // 某人离开RTC
    AIRBRoomChannelMessageTypePeerKickedFromRTC, // 某人被踢出了RTC
    AIRBRoomChannelMessageTypeRTCStarted,
    AIRBRoomChannelMessageTypeRTCStopped,
    AIRBRoomChannelMessageTypeOnRTCCalled,
    AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication,
    AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond,
};

typedef NS_ENUM(NSInteger, AIRBLivePusherEvent)
{
    AIRBLivePusherEventCreated = 0,
    AIRBLivePusherEventStarted,
    AIRBLivePusherEventNetworkPoored,
    AIRBLivePusherEventNetworkRecoveried,
    AIRBLivePusherEventStopped,
};

typedef NS_ENUM(NSInteger, AIRBLivePlayerEvent)
{
    AIRBLivePlayerEventStarted = 0,
    AIRBLivePlayerEventStartLoading,
    AIRBLivePlayerEventEndLoading,
    AIRBLivePlayerEventImageCaptured,
};

typedef NS_ENUM(NSInteger, AIRBWhiteBoardEvent)
{
    AIRBWhiteBoardEventOpened = 0,
    AIRBWhiteBoardEventDestroied
};

typedef NS_ENUM(NSInteger, AIRBRTCEvent)
{
    AIRBRTCEventLocalPreviewStarted = 0,
    AIRBRTCEventJoinSucceeded,
    AIRBRTCEventLeaveSucceeded,
    AIRBRTCEventBypassLiveStarted,
    AIRBRTCEventStatusNotificationReceived,
};

typedef NS_ENUM(NSInteger, AIRBRoomChatCommentsSortedType)
{
    AIRBRoomChatCommentsSortedTypeTimestampAscending = 0,
    AIRBRoomChatCommentsSortedTypeTimestampDescending
};

typedef NS_ENUM(NSInteger, AIRBLivePusherVideoProfile)
{
    AIRBLivePusherVideoProfile360P = 0,
    AIRBLivePusherVideoProfile720P,
};

typedef NS_ENUM(NSInteger, AIRBLivePusherVideoOrientation)
{
    AIRBLivePusherVideoOrientationPortrait = 0,
    AIRBLivePusherVideoOrientationLandscape,
};

typedef NS_ENUM(NSInteger, AIRBLivePusherCameraType)
{
    AIRBLivePusherCameraTypeRear = 0,
    AIRBLivePusherCameraTypeFront,
};

/**
 美颜模式

 - AlivcLivePushBeautyModeNormal: 普通版，不具备人脸识别功能
 - AlivcLivePushBeautyModeProfessional: 专业版，具备人脸识别功能。可以调节大眼瘦脸。
 */
typedef NS_ENUM(NSInteger, AIRBLivePushBeautyMode){
    AIRBLivePushBeautyModeNormal = 0,
    AIRBLivePushBeautyModeProfessional,
};

typedef NS_ENUM(NSInteger, AIRBLiveViewContentMode){
    /**@brief 不保持比例平铺*/
    AIRBLiveViewContentModeFill,
    /**@brief 保持比例，黑边*/
    AIRBLiveViewContentModeAspectFit,
    /**@brief 保持比例填充，需裁剪*/
    AIRBLiveViewContentModeAspectFill,
};

typedef NS_ENUM(NSInteger, AIRBRTCPeerType){
    AIRBRTCPeerTypeJoinedAlready = 0,
    AIRBRTCPeerTypeApplyingToJoinNow
};

