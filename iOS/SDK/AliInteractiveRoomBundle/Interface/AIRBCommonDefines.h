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
    AIRBLoggerLevelVerbose = 0,
    AIRBLoggerLevelDebug,
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
    AIRBWhiteBoardFailedToOpenWhenServiceUnavailable,
    AIRBWhiteBoardFailedToCreateWhenRoleInvalid,
    AIRBWhiteBoardFailedToOpenWhenTokenInvalid,
    AIRBWhiteBoardFailedToDoRecording,
    AIRBWhiteBoardFailedToReportWhiteboardPageOperate,
    AIRBWhiteBoardFailedToGetWhiteboardPageInfo,
    
    AIRBRTCFailedToJoinWhenNoRTCHere = 0x500,
    AIRBRTCFailedToGetToken,
    AIRBRTCFailedToCreate,
    AIRBRTCFailedWithInternalError,
    AIRBRTCFailedToAddPeers,
    AIRBRTCFailedToRemovePeers,
    AIRBRTCFailedToAcceptWhenCalled,
    AIRBRTCFailedToApplyOrWithdrawJoinning,
    AIRBRTCFailedToLeave,
    AIRBRTCFailedToDestroy,
    AIRBRTCFailedToReportJoinChannelStatus,
    AIRBRTCFailedToReportLeaveChannelStatus,
    AIRBRTCFailedToCreateBypassLive,
    AIRBRTCFailedToStartBypassLive,
    AIRBRTCFailedToPublishBypassLive,
    AIRBRTCFailedToPushBypassLiveStreaming,
    AIRBRTCFailedToStopBypassLiveStreaming,
    AIRBRTCFailedToGetBypassLiveDetail,
    AIRBRTCFailedToDestroyBypassLive,
    AIRBRTCFailedToSubscribe,
    AIRBRTCUnrecoverableFatalError,             // 请尝试重新加入
    AIRBRTCRetryJoinChannelFatalError,          // 重试加入超时
    AIRBRTCRetryPublishFatalError,              // 推流超时
    AIRBRTCNerworkError,                        // 网络错误，请检查网络连接是否正常
    AIRBRTCMicrophoneNoPermissionError,         // 无麦克风权限
    AIRBRTCCameraNoPermissionOrOccupiedError,   // 摄像头打开失败(无权限或被占用）
    AIRBRTCJoinChannelDuplicately,              // 重复加入(已在连麦中),请先调用leaveChannel

    AIRBVodPlayerFailedToPlayWithFatalError = 0x600,
    
    AIRBChatCommentSentInnerError = 0x700,
    AIRBChatCommentSentInvalidChatID,
    AIRBChatCommentSentFrequencyExceedsLimit,
    AIRBChatCommentLengthExceedsLimit,
};

typedef NS_ENUM(NSInteger, AIRBRoomEngineEvent)
{
    AIRBRoomEngineEventEngineStarted = 0,
    AIRBRoomEngineEventEngineLogined,
    AIRBRoomEngineEventEngineKickedOut,
    AIRBRoomEngineEventConnectionUnconnected,
    AIRBRoomEngineEventConnectionConnected,
    AIRBRoomEngineEventConnectionAuthed
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
        @"pv": 房间累积pv
        @"extension" : 创建房间时传入的扩展字段
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
    AIRBRoomChannelMessageTypeRoomMembersInOut = 0x0000,
    
    /* data内容反序列化后内容如下
      "xxxxx" //即新的房间title
     */
    AIRBRoomChannelMessageTypeRoomTitleUpdated,
    
    /* data内容反序列化后内容如下
       "xxxxx" //即新的房间公告
     */
    // 已废弃，请使用AIRBRoomChannelMessageTypeRoomNoticeUpdatedNew
    AIRBRoomChannelMessageTypeRoomNoticeUpdated,
    
    /* data内容反序列化后内容如下
     {
         "userId":"xxxxx", //更新公告的用户id
         "notice":xxxxx //新的公告
     }
     */
    AIRBRoomChannelMessageTypeRoomNoticeUpdatedNew,
    
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
     
     }
     */
    AIRBRoomChannelMessageTypeRoomExtensionUpdated,
    
    /* data内容反序列化后内容如下
    {
        "commentId":"xxxxx",
        "content":"xxxxx",
        "createAt":xxxxx,
        "creatorNick":"5586",
        "creatorOpenId":"5586",
        "extension" : {}
        "topicId":"xxxxxxxx",
        "type":0
    }
    */
    AIRBRoomChannelMessageTypeChatCommentReceived = 0x1000,
    
    /* data内容反序列化后内容如下
    {
        "topicId":"b305f7fb-04d0-437e-99f7-4d856ba57aa1",
        "likeCount":2
    }
     */
    AIRBRoomChannelMessageTypeChatLikeReceived,
    
    /* data内容反序列化后内容如下
    { //对某用户禁言
        "mute":true, //这里的mute即ban
        "muteTime":300,
        "muteUserNick":"3789",
        "muteUserOpenId":"3789",
        "topicId":"dc8d3f0c-da78-4bb4-8d1c-89fb8a555513"
    }
    { //取消对某用户禁言
        "mute":false,  //这里的mute即ban
        "muteUserNick":"3789",
        "muteUserOpenId":"3789",
        "topicId":"dc8d3f0c-da78-4bb4-8d1c-89fb8a555513"
    }
    */
    AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot,
    
    /* data内容反序列化后内容如下
     "topicId":"xxxxx", //接入方无需关注
     "mute":true(或者false), //这里的mute即ban
     */
    AIRBRoomChannelMessageTypeChatAllUsersCommentBannedOrNot,
    
    /* data内容反序列化后内容如下
      "xxxxx" //即消息体
     */
    AIRBRoomChannelMessageTypeChatCustomMessageReceived,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveCreatedByOther = 0x2000,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveStartedByOther,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLiveStoppedByOther,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLivePushStart,
    
    /* data为空*/
    AIRBRoomChannelMessageTypeLivePushStop,
    
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
    AIRBRoomChannelMessageTypePeerJoinRTCSucceeded = 0x3000, // 某人加入了RTC // 已废弃，建议使用{@link -(void)onAIRBRTCEvent:info:}中的AIRBRTCEventRemoteUserOnline事件
    
    /* data内容反序列化后内容如下
    {
        "confId":"AliRtcd7bd2a3ad04b4e8699",
        "type":2,
        "userList":[
            {
                "errorCode":"",  //这里目前为空，原因都是对端拒绝
                "status":4,
                "nickname":"5586昵称"
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
    AIRBRoomChannelMessageTypePeerLeaveRTC, // 某人离开RTC // 已废弃，建议使用{@link -(void)onAIRBRTCEvent:info:}中的AIRBRTCEventRemoteUserOffline事件
    
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
    
    /* data反序列化后内容如下
    {
        "confId":"xxxxxxxx";
        "open":true;
        "type":10;
        "userList":[
                    "xxxx"
                    ];
        "version" = "xxxxxx";
    }
     */
    AIRBRoomChannelMessageTypeOnMicphoneMuted, // 已废弃，建议使用{@link -(void)onAIRBRTCEvent:info:}中的AIRBRTCEventRemoteUserMicphoneMuted事件
    
    /* data反序列化后内容如下
    {
        "confId" = "xxxxx";
        "open" = false;
        "type" = 11;
        "userId" = "xxxxxx";
        "version" = "xxxxx";
    }
     */
    AIRBRoomChannelMessageTypeOnCameraMuted, // 已废弃，建议使用{@link -(void)onAIRBRTCEvent:info:}中的AIRBRTCEventRemoteUserCameraMuted事件
    
    AIRBRoomChannelMessageTypeOnScreenShareOpened,
    
    AIRBRoomChannelMessageTypeOnRTCLayoutChanged,
    
    /* RTC用户主动开关麦克风
     data反序列化后内容如下
     {
         "confId":"xxxxxxxx";
         "positiveMute":true;
         "type":14;
         "userList":[
                     "xxxx"
                     ];
         "version" = "xxxxxx";
     }
     */
    AIRBRoomChannelMessageTypeOnRTCMicphonePositiveMuted,
    
    /* RTC用户被单独静音
     data反序列化后内容如下
     {
         "confId":"xxxxxxxx";
         "passiveMute":true;
         "type":15;
         "userList":[
                     "xxxx"
                     ];
         "version" = "xxxxxx";
     }
     */
    AIRBRoomChannelMessageTypeOnRTCMicphonePassiveMuted,
    
    /* RTC开启全员静音
     data反序列化后内容如下
     {
         "confId":"xxxxxxxx";
         "muteAll":true;
         "type":16;
         "version" = "xxxxxx";
     }
     */
    AIRBRoomChannelMessageTypeOnRTCMicphonePassiveAllMuted,
    
    AIRBRoomChannelMessageTypeSceneClassStarted = 0x4000,
    
    AIRBRoomChannelMessageTypeSceneClassStopped,
};

typedef NS_ENUM(NSInteger, AIRBLivePusherEvent)
{
    AIRBLivePusherEventPreviewStarted = 0,
    AIRBLivePusherEventStreamStarted,
    AIRBLivePusherEventStreamRestarted,
    AIRBLivePusherEventStreamResumed,
    AIRBLivePusherEventNetworkPoored,
    AIRBLivePusherEventNetworkRecoveried,
    AIRBLivePusherEventNetworkConnectFailed,
    AIRBLivePusherEventNetworkConnectionLost,
    AIRBLivePusherEventNetworkReconnectStart,
    AIRBLivePusherEventNetworkReconnectSuccess,
    AIRBLivePusherEventNetworkReconnectFailed,
    AIRBLivePusherEventStopped,
    AIRBLivePusherEventStreamingUploadBitrateUpdated,
};

typedef NS_ENUM(NSInteger, AIRBLivePlayerEvent)
{
    AIRBLivePlayerEventLiveNotExist = 0,
    AIRBLivePlayerEventLiveNotStarted,
    AIRBLivePlayerEventLiveEnded,
    AIRBLivePlayerEventPrepared,
    AIRBLivePlayerEventStarted,
    AIRBLivePlayerEventStartLoading,
    AIRBLivePlayerEventEndLoading,
    AIRBLivePlayerEventImageCaptured,
    AIRBLivePlayerEventNotification,
    AIRBLivePlayerEventVideoSizeChanged,
    AIRBLivePlayerEventDownloadBitrateUpdated,
};

typedef NS_ENUM(NSInteger, AIRBVodPlayerEvent)
{
    /*
     播放器准备完成，跟随当前事件的info信息如下，
     {
        @"width":xxxxx,      //视频的宽
        @"height":xxxxx,     //视频的高
        @"duration":xxxxx,   //视频的长度，单位为毫秒
     }
     */
    AIRBVodPlayerEventPrepareDone = 0,
    AIRBVodPlayerEventFirstRenderedStart,
    AIRBVodPlayerEventCompletion,
    AIRBVodPlayerEventStartLoading,
    AIRBVodPlayerEventEndLoading,
    AIRBVodPlayerEventStatusChangedToPlaying,
    AIRBVodPlayerEventStatusChangedToPaused,
    AIRBVodPlayerEventSeekEnd,
    AIRBVodPlayerEventImageCaptured,
    
    /*
     播放器位置更新，跟随当前事件的info信息如下，
     {
        @"currentPosition":xxxxx        // 当前播放位置，单位为毫秒
        @"bufferedPosition":xxxxx       // 缓存位置，单位为毫秒
     }
     */
    AIRBVodPlayerEventPositionUpdated,
    
    /*
     自定义额外信息，跟随当前事件的info信息如下，
     {
        @"currentUtcTime":@"xxxxx"    // 单位为毫秒
     }
     */
    AIRBVodPlayerEventExtensionReceived,
};

typedef NS_ENUM(NSInteger, AIRBWhiteBoardEvent)
{
    AIRBWhiteBoardEventOpened = 0,
    AIRBWhiteBoardEventReplayerReady,
    AIRBWhiteBoardEventDestroied,
    AIRBWhiteBoardEventSeekCompletion,
    AIRBWhiteBoardEventResumeCompletion,
    AIRBWhiteBoardEventAddPagesWithBackgroundImagesBegin,
    AIRBWhiteBoardEventAddPagesWithBackgroundImagesCompletion
};

typedef NS_ENUM(NSInteger, AIRBRTCEvent)
{
    AIRBRTCEventLocalPreviewStarted = 0,
    AIRBRTCEventJoinSucceeded,
    AIRBRTCEventLeaveSucceeded,
    AIRBRTCEventDestroySucceeded,
    AIRBRTCEventBypassLiveStarted,
    AIRBRTCEventBypassLiveStopped,
    AIRBRTCEventBypassLiveDestroyed,
    AIRBRTCEventNotification,
    AIRBRTCEventNetworkConnectionLost,      // 网络连接断开
    AIRBRTCEventNetworkReconnectStart,      // 网络开始重连
    AIRBRTCEventNetworkReconnectSuccess,    // 网络重连成功
    AIRBRTCEventNetworkConnectionFailed,    // 网络连接失败（不再进行重连）
    
    /*
     远端用户上线(包括自己加入前已经在线的)，跟随当前事件的info信息如下，
     {
        @"userID":xxxxx,            //用户ID
        @"userNick":xxxxx,          //用户昵称
        @"cameraOpened":xxxxx,      //摄像头状态 YES:打开 NO:关闭
        @"micphoneOpened":xxxxx,    //麦克风状态 YES:打开 NO:关闭
        @"screenSharing":xxxxx,     //屏幕共享状态 YES:打开 NO:关闭
     }
     */
    AIRBRTCEventRemoteUserOnline,
    
    /*
     远端用户下线，跟随当前事件的info信息如下，
     {
        @"userID":xxxxx,            //用户ID
        @"reason":xxxxx,            //下线原因 quit:主动离开 dropped:超时掉线 other:其他
     }
     */
    AIRBRTCEventRemoteUserOffline,
    
    /*
     远端用户操作关闭摄像头，跟随当前事件的info信息如下，
     {
        @"userID":xxxxx,            //用户ID
        @"muted":xxxxx,             //状态 YES:关闭 NO:取消关闭
     }
     */
    AIRBRTCEventRemoteUserCameraMuted,
    
    /*
     远端用户操作静音麦克风，跟随当前事件的info信息如下，
     {
        @"userID":xxxxx,            //用户ID
        @"muted":xxxxx,             //状态 YES:静音 NO:取消静音
     }
     */
    AIRBRTCEventRemoteUserMicphoneMuted,
};

typedef NS_ENUM(NSInteger, AIRBRoomChatCommentsSortedType)
{
    AIRBRoomChatCommentsSortedTypeTimestampDescending = 0,
    AIRBRoomChatCommentsSortedTypeTimestampAscending
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

typedef NS_ENUM(NSInteger, AIRBVideoViewContentMode){
    /**@brief 不保持比例平铺*/
    AIRBVideoViewContentModeFill,
    /**@brief 保持比例，黑边*/
    AIRBVideoViewContentModeAspectFit,
    /**@brief 保持比例填充，需裁剪*/
    AIRBVideoViewContentModeAspectFill,
};

typedef NS_ENUM(NSInteger, AIRBRTCPeerType){
    AIRBRTCPeerTypeJoinedAlready = 0,
    AIRBRTCPeerTypeApplyingToJoinNow
};

/**
 * @brief 相机流类型
 - AIRBRTCVideoStreamTypeHigh: 高码率，高分辨率流（大流）
 - AIRBRTCVideoStreamTypeLow: 低码率，低分辨率流（小流）
 */
typedef NS_ENUM(NSInteger, AIRBRTCVideoStreamType) {
    AIRBRTCVideoStreamTypeHigh = 1,
    AIRBRTCVideoStreamTypeLow = 2,
};

/**
 * @brief 视频流类型
 - AIRBRTCVideoViewTypeCamera: 相机流
 - AIRBRTCVideoViewTypeScreen: 屏幕共享流
 */
typedef NS_ENUM(NSInteger, AIRBRTCVideoViewType) {
    AIRBRTCVideoViewTypeCamera = 1,
    AIRBRTCVideoViewTypeScreen = 2,
};

typedef NS_ENUM(NSInteger, AIRBWhiteBoardPlayMode){
    AIRBWhiteBoardPlayModeLiving = 0,
    AIRBWhiteBoardPlayModeReplay
};

typedef NS_ENUM(NSInteger, AIRBRTCBypassLiveLayoutType){
    AIRBRTCBypassLiveLayoutTypeOnePeer = 1,     // 相机流一宫格
    AIRBRTCBypassLiveLayoutTypeFivePeer = 2,    // 相机流五宫格（一大四小）
    AIRBRTCBypassLiveLayoutTypeNinePeer = 3,    // 相机流九宫格
    AIRBRTCBypassLiveLayoutTypeScreenShare = 4, // 屏幕共享流
};

typedef NS_ENUM(NSInteger, AIRBRTCBypassLiveResolutionType){
    AIRBRTCBypassLiveResolutionType_1280x720 = 1,   // 1280x720(横屏)
    AIRBRTCBypassLiveResolutionType_720x1280 = 2,   // 720x1280(竖屏)
    AIRBRTCBypassLiveResolutionType_1920x1080 = 3,  // 1920x1080(横屏)
    AIRBRTCBypassLiveResolutionType_1080x1920 = 4   // 1080x1920(竖屏)
};

/**
 * @brief AIRBRTC网络质量
 */
typedef NS_ENUM(NSUInteger, AIRBRTCNetworkQuality) {
    AIRBRTCNetworkQualityExcellent  = 0,    // 网络极好，流畅度清晰度质量好
    AIRBRTCNetworkQualityGood       = 1,    // 网络好，流畅度清晰度和极好差不多
    AIRBRTCNetworkQualityPoor       = 2,    // 网络较差，音视频流畅度清晰度有瑕疵，不影响沟通
    AIRBRTCNetworkQualityBad        = 3,    // 网络差，视频卡顿严重，音频能正常沟通
    AIRBRTCNetworkQualityVeryBad    = 4,    // 网络极差，基本无法沟通
    AIRBRTCNetworkQualityDisconnect = 5,    // 网络中断
    AIRBRTCNetworkQualityUnknow     = 6,    // 未知
};

/**
 * @brief AIRBRTC视图显示模式
 */
typedef NS_ENUM(NSUInteger, AIRBRTCViewContentMode) {
    AIRBRTCViewContentModeAuto          = 0,  // 自动模式
    AIRBRTCViewContentModeFill          = 1,  // 不保持比例平铺
    AIRBRTCViewContentModeAspectFit     = 2,  // 保持比例，黑边
    AIRBRTCViewContentModeAspectFill    = 3,  // 保持比例填充，需裁剪
};
