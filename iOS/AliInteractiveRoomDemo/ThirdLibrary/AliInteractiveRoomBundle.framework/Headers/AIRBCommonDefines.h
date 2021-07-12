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
    AIRBWhiteBoardFailedToDoRecording,
    
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
    /*
     其info格式为@{}
     */
    AIRBRoomChannelEventEntered = 0,
    
    /*
     其info格式为:
     @{
        @"type" : AIRBRoomChannelMessageType,
        @"info" : 根据不同消息类型的json字符串，具体见每种消息的说明
     }
     */
    AIRBRoomChannelEventMessageReceived,
    
    /*
     其info格式为@{}
     */
    AIRBRoomChannelEventLeft,
    
    /*
     其info格式为
     @{
        @"type": 无意义，可忽略
        @"title": 房间标题
        @"notice": 房间公告
        @"onlineCount": 房间实时在线人数
        @"uv": 房间累积uv
     }
     
     */
    AIRBRoomChannelEventRoomInfoGotten,
};

typedef NS_ENUM(NSInteger, AIRBRoomChannelMessageType)
{
    /* data内容反序列化后内容如下
     {
         "enter":true,
         "nick":"5556",
         "onlineCount":1,
         "userId":"5556",
         "uv":1
     }
     */
    AIRBRoomChannelMessageTypeRoomMembersInOut = 0,
    
    /* data内容反序列化后内容如下
      "xxxxx" //即新的房间title
     */
    AIRBRoomChannelMessageTypeRoomTitleUpdated,
    
    /* data内容反序列化后内容如下
       "xxxxx" //即新的房间公告
     */
    AIRBRoomChannelMessageTypeRoomNoticeUpdated,
    
    /* data内容反序列化后内容如下
     {
         "userId":"xxxxx", //踢人者用户id
         "kickUser":"xxxxx", // 被踢者用户id
         "kickUserName":xxxxx // 被踢者用户昵称
     }
     */
    AIRBRoomChannelMessageTypeRoomOneUserKickedOut,
    
    /* data内容反序列化后内容如下
    {
        "commentId":"xxxxx",
        "content":"xxxxx",
        "createAt":xxxxx,
        "creatorNick":"5586",
        "creatorOpenId":"5586",
        "topicId":"xxxxxxxx",
        "type":0
    }
    */
    AIRBRoomChannelMessageTypeChatCommentReceived,
    
    /* data内容反序列化后内容如下
    {
        "topicId":"b305f7fb-04d0-437e-99f7-4d856ba57aa1",
        "likeCount":2
    }
     */
    AIRBRoomChannelMessageTypeChatLikeReceived,
    
    /* data内容反序列化后内容如下
    { //对某用户禁言
        "mute":true,
        "muteTime":300,
        "muteUserNick":"3789",
        "muteUserOpenId":"3789",
        "topicId":"dc8d3f0c-da78-4bb4-8d1c-89fb8a555513"
    }

    { //取消对某用户禁言
        "mute":false,
        "muteUserNick":"3789",
        "muteUserOpenId":"3789",
        "topicId":"dc8d3f0c-da78-4bb4-8d1c-89fb8a555513"
    }
    */
    AIRBRoomChannelMessageTypeChatOneUserMutedOrUnmuted,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveCreatedByOther,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveStartedByOther,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveStoppedByOther,
    
    /* data内容反序列化后内容如下
     {
         "confId":"AliRtcxxxxxxxxxxx",
         "type":1,
         "userList":[
             {
                 "cameraStatus":0,
                 "micphoneStatus":0,
                 "nickname":"",
                 "sourceId":"156164",
                 "status":3,
                 "userId":"156164"
             }
         ],
         "version":xxxxx
     }
     */
    AIRBRoomChannelMessageTypePeerJoinRTCSucceeded, // 某人加入了RTC
    
    /* data内容反序列化后内容如下
    {
        "confId":"AliRtcd7bd2a3ad04b4e8699",
        "type":2,
        "userList":[
            {
                "errorCode":"",  //这里目前为空，原因都是对端拒绝
                "status":4,
                "userId":"5586"
            }
        ],
        "version":"3939406403651062016"
    }
    */
    AIRBRoomChannelMessageTypePeerJoinRTCFailed, // 某人加入RTC超时或者拒绝加入RTC
    
    /* data内容反序列化后内容如下
    {
        "confId":"xxxxxxx",
        "type":3,
        "userList":[
            {
                "status":6,   // 这里status表示状态 1：初始状态 2：呼叫状态 3：会议中 4：入会失败 5：被踢出 6：离会
                "userId":"5586"
            }
        ],
        "version":"5071784198576555155"
    }
    */
    AIRBRoomChannelMessageTypePeerLeaveRTC, // 某人离开RTC
    
    /* data内容反序列化后内容如下
     {
         "confId":"xxxxx",
         "type":x,
         "userList":[
             {
                 "cameraStatus":0,  // 0：关闭，1：打开
                 "deviceId":"",
                 "duration":0,
                 "micphoneStatus":0,  // 0：关闭，1：打开
                 "nickname":"",
                 "source":0,
                 "sourceId":"345",
                 "status":5,
                 "tenantId":"",
                 "userId":"345"
             }
         ],
         "version":xxxxxx
     }
     */
    AIRBRoomChannelMessageTypePeerKickedFromRTC, // 某人被踢出了RTC
    
    /* 目前无需处理data字段内容，但data内容反序列化后如下
     {
         "confInfoModel":{
             "anchorId":"xxxx",
             "anchorNickname":"",
             "appId":"xxxxx",
             "confId":"xxxxx",
             "duration":0,
             "preEndTime":0,
             "preStartTime":0,
             "roomId":"xxxxxx",
             "source":0,
             "sourceId":"xxxxxxx",
             "status":0,
             "tenantId":"xxxxxx",
             "title":"",
             "userList":[

             ]
         },
         "type":5,
         "version":"7414494959543993692"
     }
     */
    AIRBRoomChannelMessageTypeRTCStarted,
    
    /* 目前无需处理data字段内容，但data内容反序列化后如下
     {
         "confInfoModel":{
             "anchorId":"5586",
             "anchorNickname":"",
             "appId":"xxxxx",
             "confId":"xxxxxxx",
             "duration":0,
             "preEndTime":0,
             "preStartTime":0,
             "roomId":"xxxxxx",
             "source":0,
             "sourceId":"xxxxxxxx",
             "status":0,
             "tenantId":"xxxxxxx",
             "title":"",
             "userList":[
                 {
                     "cameraStatus":0,
                     "deviceId":"xxxxxxx",
                     "duration":0,
                     "errorCode":"",
                     "micphoneStatus":0,
                     "nickname":"",
                     "source":0,
                     "sourceId":"xxxxxxx",
                     "status":4,
                     "tenantId":"1569899459811379",
                     "userId":"3789"
                 }
             ]
         },
         "type":6,
         "version":"398653650867341799"
     }
     */
    AIRBRoomChannelMessageTypeRTCStopped,
    
    /* data内容反序列化后内容如下
    {
        "calleeList":[
            {
                "cameraStatus":0,
                "deviceId":"",
                "duration":0, // 累计入会时长（秒）
                "enterTime":0,
                "errorCode":"",
                "extension":"",
                "leaveTime":0,
                "micphoneStatus":0,
                "nickname":"",
                "source":0,
                "sourceId":"",
                "status":2,
                "tenantId":"",
                "userId":"3789"
            }
        ],
        "caller":{
            "userId":"5586"
        },
        "type":7,
        "version":"7799158292960264722"
    }
    */
    AIRBRoomChannelMessageTypeOnRTCCalled,
    
    /* data反序列化后内容如下
     {
         "applyUser":{
             "userId":"678"
         },
         "confId":"xxxxx",
         "isApply":false,
         "type":8,
         "version":9045665597740303614
     }
     */
    AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication,
    
    /* data反序列化后内容如下
    {
        "approve":true,
        "confId":"xxxxx",
        "type":9,
        "uid":"6567",
        "version":"xxxxxxx"
    }
     */
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
    AIRBLivePlayerEventNotification,
};

typedef NS_ENUM(NSInteger, AIRBWhiteBoardEvent)
{
    AIRBWhiteBoardEventOpened = 0,
    
    /*
    跟随当前事件的info信息如下，
     {
         @"recordID" : @"xxxxx", //白板的录制ID
         @"recordStartTime" : @"xxx" //白板开始录制的unix时间，单位毫秒
     }
     */
    AIRBWhiteBoardEventRecordingStarted,
    AIRBWhiteBoardEventRecordingPaused,
    AIRBWhiteBoardEventRecordingResumed,
    
    /*
    跟随当前事件的info信息如下，
     {
         @"recordStopTime" : @"xxxxxx" //白板结束录制的unix时间，单位毫秒
     }
     */
    AIRBWhiteBoardEventRecordingStopped,
    AIRBWhiteBoardEventDestroied
};

typedef NS_ENUM(NSInteger, AIRBRTCEvent)
{
    AIRBRTCEventLocalPreviewStarted = 0,
    AIRBRTCEventJoinSucceeded,
    AIRBRTCEventLeaveSucceeded,
    AIRBRTCEventBypassLiveStarted,
    AIRBRTCEventNotification,
};

typedef NS_ENUM(NSInteger, AIRBRoomChatCommentsSortedType)
{
    AIRBRoomChatCommentsSortedTypeTimestampAscending = 0,
    AIRBRoomChatCommentsSortedTypeTimestampDescending
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

typedef NS_ENUM(NSInteger, AIRBWhiteBoardPlayMode){
    AIRBWhiteBoardPlayModeLiving = 0,
    AIRBWhiteBoardPlayModeReplay
};

