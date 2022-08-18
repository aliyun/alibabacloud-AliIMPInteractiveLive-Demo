//
//  AIRBRoomChannel.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRoomChannel.h"


#import <vpaassdk/room/VPROOMRoomRpcInterface.h>
#import <vpaassdk/room/VPROOMRoomExtInterface.h>
#import <vpaassdk/room/VPROOMRoomModule.h>

#import <vpaassdk/room/VPROOMRoomNotificationListener.h>
#import <vpaassdk/room/VPROOMRoomNotificationModel.h>
#import <vpaassdk/room/VPROOMEnterRoomReq.h>

#import "AliInteractiveRoomLogger.h"
#import "AIRBRoomChannelUserListResponse.h"
#import "AIRBRoomChannelUser.h"

#import "AIRBRoomBasicInfo.h"

#import "AIRBChat.h"
#import "AIRBLivePlayer.h"
#import "AIRBLivePusher.h"
#import "AIRBRTC.h"
#import "AIRBWhiteBoard.h"
#import "AIRBVodPlayer.h"
#import "AIRBDocument.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"

extern NSString *const kAIRBRoomEngineLoginedNotification;

@interface AIRBRoomChannel() <VPROOMRoomNotificationListener>
@property (assign, nonatomic) BOOL roomEntered;
@property (strong, nonatomic) VPROOMRoomModule* roomModule;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (copy, nonatomic) NSDictionary* userExtension;
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* rtcID;
@property (copy, nonatomic) NSString* whiteboardID;
@property (strong, nonatomic) NSLock* createComponentLock;
@end

@implementation AIRBRoomChannel

@synthesize delegate = _delegate;
@synthesize chat = _chat;
@synthesize livePlayer = _livePlayer;
@synthesize livePusher = _livePusher;
@synthesize vodPlayer = _vodPlayer;
@synthesize rtc = _rtc;
@synthesize whiteboard = _whiteboard;
@synthesize document = _document;

#pragma mark -Properties

- (id<AIRBChatProtocol>) chat {
    if (!_chat) {
        if (self.roomEntered) {
            _chat = [[AIRBChat alloc] initWithUserID:self.userID];
            ((AIRBChat*)_chat).room = self;
            LOGI("AIRBRoomChannel::create AIRBChat(%p)", _chat);
        }
    }
    return _chat;
}

- (id<AIRBLivePlayerProtocol>) livePlayer {
    if (!_livePlayer) {
        [self.createComponentLock lock];
        if (self.roomEntered) {
            _livePlayer = [[AIRBLivePlayer alloc] initWithUserID:self.userID];
            [((AIRBLivePlayer*)_livePlayer) updateLiveID:self.liveID];
            ((AIRBLivePlayer*)_livePlayer).room = self;
            LOGI("AIRBRoomChannel::create AIRBLivePlayer(%p)", _livePlayer);
        }
        [self.createComponentLock unlock];
    }
    return _livePlayer;
}

- (id<AIRBLivePusherProtocol>) livePusher {
    if (!_livePusher) {
        [self.createComponentLock lock];
        if (self.roomEntered && [self.roomOwnerID isEqualToString:self.userID]) {
            _livePusher = [[AIRBLivePusher alloc] initWithUserID:self.userID];
            [((AIRBLivePlayer*)_livePusher) updateLiveID:self.liveID];
            ((AIRBLivePusher*)_livePusher).room = self;
            LOGI("AIRBRoomChannel::create AIRBLivePusher(%p)", _livePusher);
        }
        [self.createComponentLock unlock];
    }
    return _livePusher;
}

- (id<AIRBVodPlayerProtocol>) vodPlayer {
    if (!_vodPlayer) {
        if (self.roomEntered) {
            _vodPlayer = [[AIRBVodPlayer alloc] initWithUserID:self.userID];
            ((AIRBVodPlayer*)_vodPlayer).room = self;
            LOGI("AIRBRoomChannel::create AIRBVodPlayer(%p)", _vodPlayer);
        }
    }
    return _vodPlayer;
}

- (id<AIRBWhiteBoardProtocol>) whiteboard {
    if (!_whiteboard) {
        [self.createComponentLock lock];
        if (self.roomEntered) {
            _whiteboard = [[AIRBWhiteBoard alloc] initWithUserID:self.userID];
            ((AIRBWhiteBoard*)_whiteboard).docKey = self.whiteboardID;
            ((AIRBWhiteBoard*)_whiteboard).room = self;
            LOGI("AIRBRoomChannel::create AIRBWhiteBoard(%p)", _whiteboard);
        }
        [self.createComponentLock unlock];
    }
    return _whiteboard;
}

- (id<AIRBRTCProtocol>) rtc {
    if (!_rtc) {
        [self.createComponentLock lock];
        if (self.roomEntered) {
            _rtc = [[AIRBRTC alloc] initWithUserID:self.userID userNick:self.userNick];
            [((AIRBRTC*)_rtc) updateConferenceID:self.rtcID];
            [((AIRBRTC*)_rtc) updateLiveID:self.liveID];
            ((AIRBRTC*)_rtc).room = self;
            LOGI("AIRBRoomChannel::create AIRBRTC(%p)", _rtc);
        }
        [self.createComponentLock unlock];
    }
    return _rtc;
}

- (id<AIRBDocumentProtocol>) document {
    if (!_document) {
        if (self.roomEntered) {
            _document = [[AIRBDocument alloc] initWithUserID:self.userID roomID:self.roomID];
            ((AIRBDocument*)_document).room = self;
            LOGI("AIRBRoomChannel::create AIRBDocument(%p)", _document);
        }
    }
    return _document;
}

#pragma mark -Livecycle
- (instancetype) initWithRoomID:(nonnull NSString*)roomID userID:(NSString*)userID{
    self = [super init];
    if (self) {
        _roomID = roomID;
        _userID = userID;
        _createComponentLock = [[NSLock alloc] init];
        _roomModule = [VPROOMRoomModule getModule:_userID];
        if (!_roomModule) {
            self = nil;
        } else {
            _roomRpcInterface = [_roomModule getRpcInterface];
            _roomExtInterface = [_roomModule getExtInterface];
        }
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNSNotification:) name:kAIRBRoomEngineLoginedNotification object:nil];
    }
    
    return self;
}

- (void) dealloc {
    LOGD("AIRBRoomChannel::dealloc(%p)", self);
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark -Actions
- (void) enterRoomWithUserNick:(NSString *)userNick extension:(NSDictionary<NSString*,NSString*>*)extension {
    self.userNick = userNick;
    self.userExtension = extension;
    
    LOGD("AIRBRoomChannel(%p)::enterRoomWithUserNick(%@, %d),extension(%@)", self, userNick, self.roomEntered, extension);
//    if (!self.roomEntered) {
        
    [self.roomExtInterface setListener: self.roomID listener:self];
    
    [self.roomRpcInterface enterRoomWithBlock:[VPROOMEnterRoomReq VPROOMEnterRoomReqWithRoomId: self.roomID nick:userNick role:nil extension:extension context:nil] onSuccess:^(VPROOMEnterRoomRsp * _Nonnull rsp) {
            LOGE("AIRBRoomChannel::enterRoomWithBlock succeeded with userid(%@)", self.userID);
            self.roomEntered = YES;
        
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomEnterRoom info:@{
            @"error_code" : @"0"
        }];
            
            [self.roomRpcInterface getRoomDetailWithBlock:[VPROOMGetRoomDetailReq VPROOMGetRoomDetailReqWithRoomId: self.roomID] onSuccess:^(VPROOMGetRoomDetailRsp * _Nonnull rsp) {
                self.roomOwnerID = rsp.ownerId;
                LOGI("AIRBRoomChannel::getRoomDetailWithBlock succeeded with owner(%@)", self.roomOwnerID);
                for (VPROOMPluginInstance* item in rsp.pluginInstanceModelList) {
                    if ([item.pluginId isEqualToString:@"chat"]) {
                        ((AIRBChat*)self.chat).chatID = item.instanceId;
                    } else if ([item.pluginId isEqualToString:@"live"]) {
                        [self.createComponentLock lock];
                        self.liveID = item.instanceId;
                        [((AIRBLivePlayer*)_livePlayer) updateLiveID:self.liveID];
                        [((AIRBLivePusher*)_livePusher) updateLiveID:self.liveID];
                        [((AIRBRTC*)_rtc) updateLiveID:self.liveID];
                        [self.createComponentLock unlock];
                    } else if ([item.pluginId isEqualToString:@"wb"]) {
                        [self.createComponentLock lock];
                        self.whiteboardID = item.instanceId;
                        ((AIRBWhiteBoard*)_whiteboard).docKey = item.instanceId;
                        [self.createComponentLock unlock];
                    } else if ([item.pluginId isEqualToString:@"rtc"]) {
                        [self.createComponentLock lock];
                        self.rtcID = item.instanceId;
                        [((AIRBRTC*)_rtc) updateConferenceID:item.instanceId];
                        [self.createComponentLock unlock];
                    }
                }
                
                if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                    [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventEntered info:@{}];
                }
                
                if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                    [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventRoomInfoGotten
                                                     info:@{
                        @"type":@"updateRoomInfo",
                        @"title":rsp.title ?: @"",
                        @"notice":rsp.notice ?: @"",
                        @"onlineCount":[NSNumber numberWithInt:rsp.onlineCount],
                        @"uv":[NSNumber numberWithInt:rsp.uv],
                        @"pv" : @(rsp.pv),
                        @"extension" : rsp.extension
                                                     }];
                }
            } onFailure:^(DPSError * _Nonnull error) {
                LOGE("AIRBRoomChannel::error FailedToGetRoomDetail");
                if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelErrorWithCode:message:)]) {
                    [self.delegate onAIRBRoomChannelErrorWithCode:AIRBRoomChannelFailedToGetRoomDetail message:ERR_MSG_FROM_DPSERROR(error)];
                }
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRoomChannel::error FailedToEnterRoom");
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelErrorWithCode:message:)]) {
                [self.delegate onAIRBRoomChannelErrorWithCode:AIRBRoomChannelFailedToEnterRoom message:ERR_MSG_FROM_DPSERROR(error)];
            }
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomEnterRoom info:@{
                @"error_code" : [NSString stringWithFormat:@"%d", error.code] ?: @""
            }];
        }];
}

- (void) enterRoomWithUserNick:(NSString*)userNick {
    LOGD("AIRBRoomChannel(%p)::enterRoomWithUserNick(%@, %d)", self, userNick, self.roomEntered);
    [self enterRoomWithUserNick:userNick extension:nil];
}

- (void) leaveRoom {
    LOGI("AIRBRoomChannel::Leaving room");
    if (_livePusher) {
        [_livePusher stopLiveStreaming:NO];
        _livePusher = nil;
    }
    
    if (_livePlayer) {
        [_livePlayer stop];
        _livePlayer = nil;
    }
    
    if (_vodPlayer) {
        [_vodPlayer stop];
        _vodPlayer = nil;
    }
    
    if (_rtc) {
        [_rtc stopBypassLiveStreaming:NO];
        [_rtc leaveChannel:NO];
        _rtc = nil;
    }
    
    if (_chat) {
        [((AIRBChat*)_chat) destroy];
        _chat = nil;
    }
    
    _whiteboard = nil;
        
//    if (self.roomEntered) {
        self.roomEntered = NO;
    [self.roomExtInterface setListener:self.roomID listener:nil];
    
        [self.roomRpcInterface leaveRoomWithBlock:[VPROOMLeaveRoomReq VPROOMLeaveRoomReqWithRoomId:self.roomID] onSuccess:^(VPROOMLeaveRoomRsp * _Nonnull rsp) {
            LOGE("AIRBRoomChannel::leaveRoomWithBlock succeeded with userid(%@)", self.userID);
            
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventLeft info:@{}];
            }
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomLeaveRoom info:@{
                @"error_code" : @"0"
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRoomChannel::error FailedToLeaveRoom");
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelErrorWithCode:message:)]) {
                [self.delegate onAIRBRoomChannelErrorWithCode:AIRBRoomChannelFailedToLeaveRoom message:ERR_MSG_FROM_DPSERROR(error)];
            }
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomLeaveRoom info:@{
                @"error_code" : [NSString stringWithFormat:@"%d", error.code] ?: @""
            }];
        }];
//    }
}

- (void) getRoomDetail:(void (^)(AIRBRoomBasicInfo * _Nullable))onGotten {
    [self.roomRpcInterface getRoomDetailWithBlock:[VPROOMGetRoomDetailReq VPROOMGetRoomDetailReqWithRoomId:self.roomID] onSuccess:^(VPROOMGetRoomDetailRsp * _Nonnull rsp) {
        AIRBRoomBasicInfo* roomInfo = [[AIRBRoomBasicInfo alloc] init];
        roomInfo.roomID = rsp.roomId;
        roomInfo.title = rsp.title;
        roomInfo.notice = rsp.notice;
        roomInfo.ownerID = rsp.ownerId;
        roomInfo.uv = rsp.uv;
        roomInfo.onlineCount = rsp.onlineCount;
        roomInfo.pluginInstanceInfo = [[AIRBPluginInstanceInfo alloc] init];
        
        // TODO check
        NSMutableArray* instanceList = [[NSMutableArray alloc] init];
        for (VPROOMPluginInstance* info in rsp.pluginInstanceModelList) {
            AIRBPluginInstanceItem* item = [[AIRBPluginInstanceItem alloc] init];
            item.pluginId = [info.pluginId copy];
            item.instanceId = [info.instanceId copy];
            item.createTime = info.createTime;
            item.extension = [info.extension copy];
            [instanceList addObject:item];
        }
        roomInfo.pluginInstanceInfo.instanceList = instanceList;
        
        roomInfo.pv = rsp.pv;
        roomInfo.extension = rsp.extension;
        roomInfo.administers = [rsp.adminIdList copy];
        onGotten(roomInfo);
    } onFailure:^(DPSError * _Nonnull error) {
        onGotten(nil);
    }];
}

- (void)handleNSNotification:(NSNotification*)notification {
    if ([notification.name isEqualToString:kAIRBRoomEngineLoginedNotification]) {
        LOGD("AIRBRoomChannel::received logined when roomEntered(%d)", self.roomEntered);
        if (self.roomEntered) {
            [self.roomRpcInterface enterRoomWithBlock:({
                VPROOMEnterRoomReq* req = [VPROOMEnterRoomReq VPROOMEnterRoomReqWithRoomId:self.roomID
                                                                                      nick:self.userNick
                                                                                      role:nil
                                                                                 extension:self.userExtension
                                                                                   context:@{
                    @"enter_room_after_online_flag" : @"true"
                }];
                req;
            }) onSuccess:^(VPROOMEnterRoomRsp * _Nonnull rsp) {
                LOGD("AIRBRoomChannel::reenter room succeed.");
            } onFailure:^(DPSError * _Nonnull error) {
                LOGD("AIRBRoomChannel::reenter room failed.");
                if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelErrorWithCode:message:)]) {
                    [self.delegate onAIRBRoomChannelErrorWithCode:AIRBRoomChannelFailedToEnterRoom message:ERR_MSG_FROM_DPSERROR(error)];
                }
            }];
        }
    }
}

#pragma mark -VPROOMRoomNotificationListener

- (void)onChatMessage:(nonnull VPROOMRoomNotificationModel *)msg {
    LOGI("AIRBRoomChannel::onChatMessage:%@", msg);
    AIRBRoomChannelMessageType messageType;
    switch (msg.type) {
        case 10000: // CHAT_COMMENT_RECEIVED
            messageType = AIRBRoomChannelMessageTypeChatCommentReceived;
            break;
        case 10001: //CHAT_LIKE_RECEIVED
            messageType = AIRBRoomChannelMessageTypeChatLikeReceived;
            break;
        case 10002: //MUTE/UNMUTE_USER
            messageType = AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot;
            break;
        case 10003:
            messageType = AIRBRoomChannelMessageTypeChatAllUsersCommentBannedOrNot;
            break;
        case 30000:
            messageType = AIRBRoomChannelMessageTypeChatCustomMessageReceived;
            break;
        default:
            return;
    }
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
        [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventMessageReceived info:@{
            @"type" : @(messageType),
            @"message_id" : msg.messageId ? : @"",
            @"data" : msg.data ? : @""
        }];
    }
}

- (void)onLiveMessage:(nonnull VPROOMRoomNotificationModel *)msg {
    LOGI("AIRBRoomChannel::onLiveMessage:%@", msg);
    
    AIRBRoomChannelMessageType messageType;
    switch (msg.type) {
        case 1001: // LIVE_CREATED
            messageType = AIRBRoomChannelMessageTypeLiveCreatedByOther;
            break;
        case 1002:{ //LIVE_STARTED
            messageType = AIRBRoomChannelMessageTypeLiveStartedByOther;
            NSData *turnData = [msg.data dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            [self.createComponentLock lock];
            self.liveID = [dataDic valueForKey:@"liveId"];
            [((AIRBLivePlayer*)_livePlayer) updateLiveID:[dataDic valueForKey:@"liveId"]];
            [self.createComponentLock unlock];
        }
            break;
        case 1003: //LIVE_STOPPED
            messageType = AIRBRoomChannelMessageTypeLiveStoppedByOther;
            break;
            
        case 1005:
            messageType = AIRBRoomChannelMessageTypeLivePushStart;
            [((AIRBLivePlayer*)_livePlayer) notifyLivePushStart:YES];
            break;
            
        case 1006:
            messageType = AIRBRoomChannelMessageTypeLivePushStop;
            [((AIRBLivePlayer*)_livePlayer) notifyLivePushStart:NO];
            break;
        default:
            return;
    }
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
        [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventMessageReceived info:@{
            @"type" : @(messageType),
            @"message_id" : msg.messageId ? : @"",
            @"data" : @""
        }];
    }
}

- (void)onRoomMessage:(nonnull VPROOMRoomNotificationModel *)msg {
    LOGI("AIRBRoomChannel::onRoomMessage:%@", msg);
    AIRBRoomChannelMessageType messageType;
    switch (msg.type) {
        case 20000: // ROOM_IN_OUT
            messageType = AIRBRoomChannelMessageTypeRoomMembersInOut;
            break;
        case 20001:
            messageType = AIRBRoomChannelMessageTypeRoomTitleUpdated;
            break;
        case 20002: // ROOM_UPDATE_NOTICE
            messageType = AIRBRoomChannelMessageTypeRoomNoticeUpdated;
            break;
        case 20003:
            messageType = AIRBRoomChannelMessageTypeRoomOneUserKickedOut;
            break;
        case 20004:
            messageType = AIRBRoomChannelMessageTypeRoomExtensionUpdated;
            break;
        case 20005:
            messageType = AIRBRoomChannelMessageTypeRoomNoticeUpdatedNew;
            break;
        default:
            return;
    }
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
        [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventMessageReceived info:@{
            @"type" : @(messageType),
            @"message_id" : msg.messageId ? : @"",
            @"data" : msg.data ? : @""
        }];
    }
}

- (void)onRtcMessage:(VPROOMRoomNotificationModel *)msg {
    LOGD("AIRBRoomChannel::onRtcMessage:%@", msg);
    AIRBRoomChannelMessageType message;
    switch (msg.type) {
        case 1: {
            message = AIRBRoomChannelMessageTypePeerJoinRTCSucceeded;
        }
            break;
        case 2:
            message = AIRBRoomChannelMessageTypePeerJoinRTCFailed;
            break;
        case 3:
            message = AIRBRoomChannelMessageTypePeerLeaveRTC;
            break;
        case 4:
            message = AIRBRoomChannelMessageTypePeerKickedFromRTC;
            break;
        case 5:{
            /*
             data:{"confInfoModel":{"anchorId":"890","anchorNickname":"890","appId":"xxxx","confId":"xxxx","duration":0,"preEndTime":0,"preStartTime":0,"roomId":"a967c72e-f350-4561-8342-8674ff21c593","source":0,"sourceId":"xxxxxx","status":0,"tenantId":"1569899459811379","title":"hhh","userList":[]},"type":5,"version":4029983054785365383}
             */
            message = AIRBRoomChannelMessageTypeRTCStarted;
            NSData *turnData = [msg.data dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            NSDictionary* confInfo = [dataDic valueForKey:@"confInfoModel"];
            if (confInfo && [confInfo isKindOfClass:[NSDictionary class]]) {
                [self.createComponentLock lock];
                self.rtcID = [confInfo valueForKey:@"confId"];
                [((AIRBRTC*)_rtc) updateConferenceID:[confInfo valueForKey:@"confId"]];
                [self.createComponentLock unlock];
            }
            break;
        }
        case 6:
            message = AIRBRoomChannelMessageTypeRTCStopped;
            break;
        case 7:
            message = AIRBRoomChannelMessageTypeOnRTCCalled;
            break;
        case 8:
            message = AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplication;
            break;
        case 9:
            message = AIRBRoomChannelMessageTypeOnReceiveRTCJoiningApplicationRespond;
            break;
        case 10:
            message = AIRBRoomChannelMessageTypeOnMicphoneMuted;
            break;
        case 11:
            message = AIRBRoomChannelMessageTypeOnCameraMuted;
            break;
        case 12:
            message = AIRBRoomChannelMessageTypeOnScreenShareOpened;
            break;
        case 13:
            message = AIRBRoomChannelMessageTypeOnRTCLayoutChanged;
            break;
        case 14:
            message = AIRBRoomChannelMessageTypeOnRTCMicphonePositiveMuted;
            break;
        case 15:
            message = AIRBRoomChannelMessageTypeOnRTCMicphonePassiveMuted;
            break;
        case 16:
            message = AIRBRoomChannelMessageTypeOnRTCMicphonePassiveAllMuted;
            break;
        default:
            return;
    }
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
        [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventMessageReceived info:@{
            @"type" : @(message),
            @"message_id" : msg.messageId ? : @"",
            @"data" : msg.data ? : @""
        }];
    }
}

- (void)onClassSceneMessage:(nonnull VPROOMRoomNotificationModel *)msg {
    LOGD("AIRBRoomChannel::onRtcMessage:%@", msg);
    AIRBRoomChannelMessageType message;
    switch (msg.type) {
        case 1:
            message = AIRBRoomChannelMessageTypeSceneClassStarted;
            break;
        case 2:
            message = AIRBRoomChannelMessageTypeSceneClassStopped;
            break;
        default:
            return;
    }
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
        [self.delegate onAIRBRoomChannelEvent:AIRBRoomChannelEventMessageReceived info:@{
            @"type" : @(message),
            @"message_id" : msg.messageId ? : @"",
            @"data" : msg.data ? : @""
        }];
    }
}

- (void) onDocMessage:(VPROOMRoomNotificationModel *)msg {
    LOGD("AIRBRoomChannel::onDocMessage:%@", msg);
    switch (msg.type) {
        case 2001:
            break;
        case 2002:
            [((AIRBDocument*)self.document) notifyCompletionOfDocumentFormatConversion:msg.data];
            break;
            
        default:
            break;
    }
}

- (void) updateRoomTitle:(nonnull NSString *)title onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure{
//    if (![self.roomOwnerID isEqualToString:self.userID]) {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can update the room title."]);
//        return;
//    }
    
    LOGI("AIRBRoomChannel::Update room title(title:%@) with userID:(%@) %@", title, self.userID, self.roomRpcInterface);
    [self.roomRpcInterface updateRoomTitleWithBlock:[VPROOMUpdateRoomTitleReq VPROOMUpdateRoomTitleReqWithRoomId:self.roomID title:title] onSuccess:^(VPROOMUpdateRoomTitleRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) updateRoomNotice:(nonnull NSString *)notice onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure{
//    if (![self.roomOwnerID isEqualToString:self.userID]) {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can update the room notice."]);
//        return;
//    }
    
    LOGI("AIRBRoomChannel::Update room notice(notice:%@) with userID:(%@) %@", notice, self.userID, self.roomRpcInterface);
    [self.roomRpcInterface updateRoomNoticeWithBlock:[VPROOMUpdateRoomNoticeReq VPROOMUpdateRoomNoticeReqWithRoomId:self.roomID notice: notice] onSuccess:^(VPROOMUpdateRoomNoticeRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) getRoomUserListWithPageNum:(int32_t)pageNum
                           pageSize:(int32_t)pageSize
                          onSuccess:(void (^)(AIRBRoomChannelUserListResponse * _Nonnull response))onSuccess
                          onFailure:(void (^)(NSString* errorMessage))onFailure{
    LOGI("AIRBRoomChannel::Get room user list(pageNum:%d pageSize:%d) with userID:(%@) %@", pageNum, pageSize, self.userID, self.roomRpcInterface);
    [self.roomRpcInterface getRoomUserListWithBlock:[VPROOMGetRoomUserListReq VPROOMGetRoomUserListReqWithRoomId: self.roomID pageNum:pageNum pageSize:pageSize] onSuccess:^(VPROOMGetRoomUserListRsp *_Nonnull rsp){
        @autoreleasepool {
            onSuccess([self createUserListResponseWithInnerResponse:rsp]);
        }
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) kickRoomUserWithUserID:(nonnull NSString*)kickUserID
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure{
    [self kickRoomUserWithUserID:kickUserID kickedSeconds:300 onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) kickRoomUserWithUserID:(NSString *)kickUserID
                  kickedSeconds:(int32_t)kickedSeconds
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString * _Nonnull))onFailure {
//    if ([self.roomOwnerID isEqualToString:self.userID]) {
        LOGI("AIRBRoomChannel::Kick room user(kickUserId:%@) with userID:(%@) %@", kickUserID, self.userID, self.roomRpcInterface);
        [self.roomRpcInterface kickRoomUserWithBlock:[VPROOMKickRoomUserReq VPROOMKickRoomUserReqWithRoomId:self.roomID kickUser:kickUserID blockTime:kickedSeconds] onSuccess:^(VPROOMKickRoomUserRsp * _Nonnull rsp) {
            onSuccess();
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
//    } else {
//        onFailure([NSString stringWithFormat:@"Permission denied: Only the room owner can kick users."]);
//    }
}

- (AIRBRoomChannelUserListResponse*)createUserListResponseWithInnerResponse:(VPROOMGetRoomUserListRsp*)rsp {
    AIRBRoomChannelUserListResponse* response = [[AIRBRoomChannelUserListResponse alloc] init];
    response.totalCount = rsp.total;
    response.hasMore = rsp.hasMore;
    NSMutableArray* userList = [[NSMutableArray alloc] init];
    for (VPROOMRoomUserModel* item in rsp.userList) {
        AIRBRoomChannelUser* user = [[AIRBRoomChannelUser alloc] init];
        user.openID = item.openId;
        user.nick = item.nick;
        user.role = item.role;
        user.extension = [NSDictionary dictionaryWithDictionary:item.extension];
        
        [userList addObject:user];
    }
    if (userList.count > 0) {
        response.userList = [NSArray arrayWithArray:userList];
    }
    return response;
}

@end
