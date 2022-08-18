//
//  AIRBRTC.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRTC.h"

#import <objc/message.h>

#import <vpaassdk/room/VPROOMRoomRpcInterface.h>
#import <vpaassdk/room/VPROOMRoomExtInterface.h>
#import <vpaassdk/live/VPLIVELiveRpcInterface.h>
#import <vpaassdk/live/VPLIVELiveModule.h>
#import <vpaassdk/rtc/VPRTCRtcRpcInterface.h>
#import <vpaassdk/rtc/VPRTCRtcModule.h>

#import "AIRBRoomChannel.h"
#import "AIRBRoomChannelUserListResponse.h"
#import "AIRBRoomChannelUser.h"
#import "AliInteractiveRoomLogger.h"
#import "AIRBRTCConfig.h"
#import "AIRBRTCBypassLiveLayoutPeerVideoModel.h"
#import "AIRBRTCUserVolumeInfo.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"

#import "AIRBRTCDetailModel.h"
#import "AIRBRTCUserModel.h"

@interface AIRBRTC()
@property (strong, nonatomic) id rtcEngine;
@property (strong, nonatomic) VPRTCRtcModule* rtcModule;
@property (strong, nonatomic) VPRTCRtcRpcInterface* rtcRpcInterface;
@property (copy, nonatomic) NSString* conferenceID;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (strong, nonatomic) VPLIVELiveModule* liveModule;
@property (strong, nonatomic) VPLIVELiveRpcInterface* liveRpcInterface;
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* mediaURL;
@property (assign, nonatomic) BOOL recording;
@property (assign, nonatomic) BOOL localCameraOpened;
@property (assign, nonatomic) BOOL localMicphoneOpened;
@property (assign, nonatomic) BOOL hasJoinedChannel;
@property (assign, nonatomic) BOOL shouldDestroyRTCInstance;
@property (assign, nonatomic) BOOL bypassLiveStreamingStarted;
@property (nonatomic, assign) AIRBRTCBypassLiveResolutionType bypassLiveResolutionType;
@property (nonatomic, assign) AIRBRTCBypassLiveResolutionType recordResolutionType;
@property (strong, nonatomic) AIRBRTCConfig* config;

@property (nonatomic, strong) NSMutableDictionary* userCameraOpenedStatus;
@property (nonatomic, strong) NSMutableDictionary* userMicOpenedStatus;
@property (nonatomic, strong) NSMutableDictionary* userScreenShareOpenedStatus;

@end

@implementation AIRBRTC

@synthesize delegate = _delegate;
@synthesize rtcLocalView = _rtcLocalView;
@synthesize config = _config;

- (void) setConfig:(AIRBRTCConfig*)config {
    _config = config;
    ((void(*) (id, SEL, CGSize, NSInteger, NSInteger))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setVideoDimensions:frameRate:bitrate:"), self.config.videoStreamTypeHighDimensions, self.config.videoStreamTypeHighFrameRate, self.config.videoStreamTypeHighBitrate);
    
    if (config.audioOnlyModeEnabled) {
        ((void(*) (id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setAudioOnlyMode:"), self.config.audioOnlyModeEnabled);
    }
}

- (UIView*)rtcLocalView {
    return ((UIView* (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"localView"));
}

- (BOOL) previewMirrorEnabled{
    return ((NSInteger(*) (id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"previewMirrorEnabled"));
}

- (void) setPreviewMirrorEnabled:(BOOL)previewMirrorEnabled{
    ((void(*) (id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setPreviewMirrorEnabled:"), previewMirrorEnabled);
}

- (AIRBRTCViewContentMode) previewContentMode{
    return (AIRBRTCViewContentMode)((NSUInteger(*) (id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"previewContentMode"));
}

- (void) setPreviewContentMode:(AIRBRTCViewContentMode)previewContentMode{
    ((void(*) (id, SEL, NSUInteger))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setPreviewContentMode:"), (NSUInteger)previewContentMode);
}

- (AIRBRTCViewContentMode) remoteVideoStreamContentMode{
    return (AIRBRTCViewContentMode)((NSUInteger(*) (id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"remoteVideoStreamContentMode"));
}

- (void) setRemoteVideoStreamContentMode:(AIRBRTCViewContentMode)remoteVideoStreamContentMode{
    ((void(*) (id, SEL, NSUInteger))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setRemoteVideoStreamContentMode:"), (NSUInteger)remoteVideoStreamContentMode);
}

- (BOOL) videoStreamMirrorEnabled{
    return ((BOOL(*) (id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"videoStreamMirrorEnabled"));
}

- (void) setVideoStreamMirrorEnabled:(BOOL)videoStreamMirrorEnabled{
    ((void(*) (id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setVideoStreamMirrorEnabled:"), videoStreamMirrorEnabled);
}

- (id) rtcEngine {
    if (!_rtcEngine) {
        Class rtcEngineClass = NSClassFromString(@"AIRBRTCEngineWrapper");
        if (rtcEngineClass) {
            _rtcEngine = ((id (*)(id, SEL))objc_msgSend)(rtcEngineClass, NSSelectorFromString(@"createRTCEngine"));
            if (_rtcEngine) {
                ((void (*)(id, SEL, id))objc_msgSend)(_rtcEngine, NSSelectorFromString(@"setDelegate:"), self);
            }
        }
    }
    return _rtcEngine;
}

- (id) queenEngine {
    return ((id (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"queenEngine"));
}

- (UIViewController*)faceBeautyConfigViewController {
    return ((UIViewController* (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"faceBeautyConfigViewController"));
}

- (instancetype) initWithUserID:(NSString*)userID userNick:(NSString *)userNick{
    self = [super init];
    if (self) {
        _userID = userID;
        _userNick = userNick;
        _rtcModule = [VPRTCRtcModule getModule:_userID];
        _rtcRpcInterface = [self.rtcModule getRpcInterface];
        _liveModule = [VPLIVELiveModule getModule:userID];
        _liveRpcInterface = [_liveModule getRpcInterface];
        _localCameraOpened = YES;
        _localMicphoneOpened = YES;
        _hasJoinedChannel = NO;
        _shouldDestroyRTCInstance = YES;
        _bypassLiveStreamingStarted = NO;
        _config = [[AIRBRTCConfig alloc] init];
        
        _userCameraOpenedStatus = [[NSMutableDictionary alloc] init];
        _userMicOpenedStatus = [[NSMutableDictionary alloc] init];
        _userScreenShareOpenedStatus = [[NSMutableDictionary alloc] init];
    }
    return self;
}

- (void) dealloc {
    LOGD("AIRBRTC::AIRBRTC(%p) dealloc", self);
    if (_rtcEngine) {
        ((void (*)(id, SEL))objc_msgSend)(_rtcEngine, NSSelectorFromString(@"destroyEngine"));
        _rtcEngine = nil;
    }
    
    [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void) updateConferenceID:(NSString *)conferenceID {
    self.conferenceID = conferenceID;
}

- (void) updateLiveID:(NSString*)liveID {
    self.liveID = liveID;
}

- (void) getCurrentRTCDetailOnSuccess:(void(^)(AIRBRTCDetailModel* info))onSuccess onFailure:(void(^)(NSString* errMessage))onFailure{
    LOGD("AIRBRTC::getCurrentRTCDetailOnSuccess(%@).", self.conferenceID);
    if (self.conferenceID.length > 0) {
        VPRTCGetConfDetailReq* req = [VPRTCGetConfDetailReq VPRTCGetConfDetailReqWithConfId:self.conferenceID];
        [self.rtcRpcInterface getConfDetailWithBlock:req onSuccess:^(VPRTCGetConfDetailRsp * _Nonnull rsp) {
            onSuccess([self createRTCInfoModelWithInnerResponse:rsp]);
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(@"Failed to get current RTC info because current conference id is empty.");
    }
}

- (AIRBRTCDetailModel*)createRTCInfoModelWithInnerResponse:(VPRTCGetConfDetailRsp*)rsp{
    AIRBRTCDetailModel* infoModel = [[AIRBRTCDetailModel alloc] init];
    infoModel.roomId = rsp.confInfo.roomId;
    infoModel.confId = rsp.confInfo.confId;
    infoModel.status = rsp.confInfo.status;
    infoModel.extension = rsp.confInfo.extension;
    infoModel.muteAll = rsp.confInfo.muteAll;
    infoModel.userList = [self createRTCUserListWithConfUserList:rsp.confInfo.userList];
    return infoModel;
}

- (NSArray<AIRBRTCUserModel*>*)createRTCUserListWithConfUserList:(NSArray<VPRTCConfUserModel*>*)confUserList{
    NSMutableArray<AIRBRTCUserModel*>* userList = [[NSMutableArray<AIRBRTCUserModel*> alloc] init];
    for (VPRTCConfUserModel* confUser in confUserList){
        AIRBRTCUserModel* user = [[AIRBRTCUserModel alloc] init];
        user.userId = confUser.userId;
        user.nickname = confUser.nickname;
        user.status = confUser.status;
        user.cameraOpened = confUser.cameraStatus == 0 ? NO : YES;
        user.micphoneOpened = !confUser.positiveMute;
        user.passiveMute = confUser.passiveMute;
        [userList addObject:user];
    }
    
    return [[NSArray<AIRBRTCUserModel*> alloc] initWithArray:userList];
}

- (void) queryRTCTokenInfoAndJoinChannel {
    LOGD("AIRBRTC::queryRTCTokenInfoAndJoinChannel");
    [self.rtcRpcInterface getTokenWithBlock:[VPRTCGetTokenReq VPRTCGetTokenReqWithConfId:self.conferenceID]
                             onSuccess:^(VPRTCGetTokenRsp * _Nonnull rsp) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcGetRtcToken info:@{
            @"rtc_id" : self.conferenceID ?: @"null"
        }];
        
        LOGD("AIRBRTC::getTokenWithBlock succeeded.");
        Class rtcEngineConfigClass = NSClassFromString(@"AIRBRTCEngineConfig");
        if (rtcEngineConfigClass) {
            id rtcEngineConfig = ((id (*)(id, SEL))objc_msgSend)(rtcEngineConfigClass, NSSelectorFromString(@"createConfig"));
            if (rtcEngineConfig) {
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setToken:"), rsp.token);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setAppId:"), rsp.appId);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setGslb:"), rsp.gslb);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setNonce:"), rsp.nonce);
                ((void (*)(id, SEL, int64_t))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setTimestamp:"), rsp.timestamp);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setUserID:"), self.userID);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setUserNick:"), self.userNick);
                ((void (*)(id, SEL, id))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setConferenceID:"), self.conferenceID);
                ((void (*)(id, SEL, BOOL))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setAudioOnlyModeEnabled:"), self.config.audioOnlyModeEnabled);
                ((void (*)(id, SEL, CGSize))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setVideoDimensions:"), self.config.videoStreamTypeHighDimensions);
                ((void (*)(id, SEL, BOOL))objc_msgSend)(rtcEngineConfig, NSSelectorFromString(@"setDualStreamPublished:"), self.config.videoStreamTypeLowPublished);
                
                // 防止二次入会麦克风、摄像头不生效
                [self muteLocalMicphone:!self.localMicphoneOpened onSuccess:^{} onFailure:^(NSString * _Nonnull errorMessage){}];
                [self muteLocalCamera:!self.localCameraOpened onSuccess:^{} onFailure:^(NSString * _Nonnull errorMessage) {}];
                
                ((void (*)(id, SEL, id))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"joinChannelWithConfig:"), rtcEngineConfig);
            }
        }
    } onFailure:^(DPSError * _Nonnull error) {
        ((void (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"startRetryJoinCountTimer")); // 重试加入
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToGetToken message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) createRTCAndJoinChannelInternal {
    LOGD("AIRBRTC::createRTCAndJoinChannelInternal");
    VPROOMCreateRtcReq* req = [VPROOMCreateRtcReq VPROOMCreateRtcReqWithRoomId:self.room.roomID
                                                                       anchorId:self.userID
                                                                 anchorNickname:@""
                                                                          title:@""
                                                                   preStartTime:0
                                                                     preEndTime:0
                                                                      extension:@""];

    [self.room.roomExtInterface createRtcWithBlock:req onSuccess:^(VPROOMCreateRtcRsp * _Nonnull rsp) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcCreateRtc info:@{
            @"rtc_id" : rsp.conferenceId ?: @"null"
        }];
        self.conferenceID = rsp.conferenceId;
        [self queryRTCTokenInfoAndJoinChannel];
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToCreate message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

//- (void)startLocalPreview {
//    LOGD("AIRBRTC::startLocalPreview");
//    ((void (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"startPreview"));
//}

- (void) joinChannel {
    LOGD("AIRBRTC::joinChannel");
    if (self.conferenceID.length > 0) {
        [self queryRTCTokenInfoAndJoinChannel];
    } else {
        if ([self.userID isEqualToString:self.room.roomOwnerID]) {
            [self createRTCAndJoinChannelInternal];
        } else {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                [self.delegate onAIRBRTCEvent:AIRBRTCEventNotification info:@{
                    @"type" : @"AIRBRTCStatus",
                    @"data" : @"RTC未创建"
                }];
            }
            
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToJoinWhenNoRTCHere message:@"Failed to join channel when no RTC here"];
            }
        }
    }
}

- (void)addPeers:(NSArray<NSString*>*)userIDs {
    LOGD("AIRBRTC::addPeers");
    if ([userIDs isKindOfClass:[NSArray class]] && userIDs.count > 0) {
        NSMutableArray* userModels = [[NSMutableArray alloc] init];
        __block NSString* userListString = @"[";
        for (NSString* userID in userIDs) {
            if (userID) {
                if (userListString.length > 1) {
                    userListString = [[userListString stringByAppendingString:@","] stringByAppendingString:userID];
                } else {
                    userListString = [userListString stringByAppendingString:userID];
                }

                VPRTCConfUserModel* model = [VPRTCConfUserModel VPRTCConfUserModelWithUserId:userID
                                                                     nickname:userID
                                                                    extension:@""
                                                                       status:2
                                                                    errorCode:@""
                                                                 cameraStatus:0
                                                               micphoneStatus:0
                                                                       source:0
                                                                     sourceId:@""
                                                                     deviceId:@""
                                                                     enterTime:0
                                                                     leaveTime:0
                                                                     tenantId:@""
                                                                     duration:0
                                                                  passiveMute:NO
                                                                 positiveMute:YES];
                                        
                [userModels addObject:model];
            }
        }
        
        __weak typeof(self) weakSelf = self;
        
        VPRTCAddMembersReq* req = [VPRTCAddMembersReq VPRTCAddMembersReqWithConfId:self.conferenceID addedCalleeList:userModels];
        [self.rtcRpcInterface addMembersWithBlock:req onSuccess:^(VPRTCAddMembersRsp * _Nonnull rsp) {
            userListString = [userListString stringByAppendingString:@"]"];
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcInviteJoinRtc info:@{
                @"rtc_id" : weakSelf.conferenceID ?: @"null",
                @"user_list" : userListString
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            if ([weakSelf.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [weakSelf.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToAddPeers message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    } else  {
        LOGE("addRTCPeers error with invalid user id.");
    }
}

- (void)removePeers:(NSArray<NSString *> *)userIDs {
    LOGD("AIRBRTC::removePeers");
    if ([userIDs isKindOfClass:[NSArray class]] && userIDs.count > 0) {
        __weak typeof(self) weakSelf = self;
        VPRTCKickMembersReq* req = [VPRTCKickMembersReq VPRTCKickMembersReqWithConfId:self.conferenceID kickedUserList:userIDs];
        [self.rtcRpcInterface kickMembersWithBlock:req onSuccess:^(VPRTCKickMembersRsp * _Nonnull rsp) {
            
            NSString* userListString = @"[";
            for (NSString* userID in userIDs) {
                if (userListString.length > 1) {
                    userListString = [[userListString stringByAppendingString:@","] stringByAppendingString:userID];
                } else {
                    userListString = [userListString stringByAppendingString:userID];
                }
            }
            userListString = [userListString stringByAppendingString:@"]"];
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcKickMembers info:@{
                @"rtc_id" : weakSelf.conferenceID ?: @"null",
                @"user_list" : userListString
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            if ([weakSelf.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [weakSelf.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToRemovePeers message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    } else {
        LOGE("removeRTCPeers error with invalid user id.");
    }
}

- (void)approveJoiningApplication:(BOOL)approve fromPeer:(NSString*)userID {
    LOGD("AIRBRTC::approveJoiningApplication(%d, %@)", approve, userID);
    VPRTCApproveLinkMicReq* req = [VPRTCApproveLinkMicReq VPRTCApproveLinkMicReqWithConfId:self.conferenceID calleeUid:userID approve:approve];
    [self.rtcRpcInterface approveLinkMicWithBlock:req onSuccess:^(VPRTCApproveLinkMicRsp * _Nonnull rsp) {
        if (approve){
            [self addPeers:@[userID]];
        }
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToAcceptWhenCalled message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) applyForJoining:(BOOL)applyOrWithdraw {
    LOGD("AIRBRTC::applyForJoining(%d)", applyOrWithdraw);
        if (self.conferenceID.length > 0) {
            VPRTCApplyLinkMicReq* req = [VPRTCApplyLinkMicReq VPRTCApplyLinkMicReqWithConfId:self.conferenceID apply:applyOrWithdraw];
            [self.rtcRpcInterface applyLinkMicWithBlock:req onSuccess:^(VPRTCApplyLinkMicRsp * _Nonnull rsp) {
                ;
            } onFailure:^(DPSError * _Nonnull error) {
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                    [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToApplyOrWithdrawJoinning message:ERR_MSG_FROM_DPSERROR(error)];
                }
            }];
        } else {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToApplyOrWithdrawJoinning message:@"invalid rtc id."];
            }
        }
}

- (void) acceptCall:(BOOL)accept {
    LOGD("AIRBRTC::acceptCall(%d)", accept);
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcApplyLinkMic info:@{
        @"rtc_id" : self.conferenceID ?: @"null",
        @"apply" : accept ? @"true" : @"false"
    }];
    VPRTCReportJoinStatusReq* req = [VPRTCReportJoinStatusReq VPRTCReportJoinStatusReqWithConfId:self.conferenceID
                                                                               sourceId:self.userID
                                                                               nickname:@""
                                                                             joinStatus:accept ? 3 : 4
                                                                        errorCode: accept ? @"" : @"reject_invite"
                                                                           cameraStatus:0
                                                                         micphoneStatus:0
                                ];
    [self.rtcRpcInterface reportJoinStatusWithBlock:req onSuccess:^(VPRTCReportJoinStatusRsp * _Nonnull rsp) {
        ;
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToReportJoinChannelStatus message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) leaveChannel:(BOOL)destroy {
    LOGD("AIRBRTC::leaveChannel(%d)", destroy);
    self.shouldDestroyRTCInstance = destroy;
    ((void (*)(id, SEL))objc_msgSend)(_rtcEngine, NSSelectorFromString(@"leaveChannel"));
    
    // 上报离会
    if (self.hasJoinedChannel) {
        LOGD("AIRBRTC.rtcRpcInterface::reportLeaveStatusWithBlock");
        [self.rtcRpcInterface reportLeaveStatusWithBlock:[VPRTCReportLeaveStatusReq VPRTCReportLeaveStatusReqWithConfId:self.conferenceID errorCode:@""] onSuccess:^(VPRTCReportLeaveStatusRsp * _Nonnull rsp) {
            self.hasJoinedChannel = NO;
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                [self.delegate onAIRBRTCEvent:AIRBRTCEventLeaveSucceeded info:@{}];
            }
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRTC::leaveChannel:AIRBRTCFailedToReportLeaveChannelStatus");
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToReportLeaveChannelStatus message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
    
    // 销毁rtc
    if (destroy && [self.room.roomOwnerID isEqualToString:self.userID]) {
        LOGD("AIRBRTC.rtcRpcInterface::destroyRtcWithBlock");
        
        VPROOMDestroyRtcReq* req = [VPROOMDestroyRtcReq VPROOMDestroyRtcReqWithRoomId:self.room.roomID conferenceId:self.conferenceID];
        [self.room.roomExtInterface destroyRtcWithBlock:req onSuccess:^(VPROOMDestroyRtcRsp * _Nonnull rsp) {
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcDestoryRtc info:@{
                @"rtc_id" : self.conferenceID ?: @"null"
            }];
            self.conferenceID = @"";
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                [self.delegate onAIRBRTCEvent:AIRBRTCEventDestroySucceeded info:@{}];
            }
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToDestroy message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
}

//- (void)leaveChannel {
//    [self leaveChannel:YES];
//}

- (void) queryBypassLiveDetailAndStartPublishing {
    LOGD("AIRBRTC::queryBypassLiveDetailAndStartPublishing");
    [self.liveRpcInterface getLiveDetailWithBlock:[VPLIVEGetLiveDetailReq VPLIVEGetLiveDetailReqWithUuid:self.liveID] onSuccess:^(VPLIVEGetLiveDetailRsp * _Nonnull rsp) {
        LOGD("AIRBRTC::getLiveDetailWithBlock succeeded.");
        self.mediaURL = rsp.live.pushUrl;
        int32_t liveStatus = rsp.live.status;
        
        VPRTCPushLiveStreamReq* req = [VPRTCPushLiveStreamReq VPRTCPushLiveStreamReqWithConfId:self.conferenceID rtmpUrl:self.mediaURL resolutionType:(int)self.bypassLiveResolutionType];
        [self.rtcRpcInterface pushLiveStreamWithBlock:req onSuccess:^(VPRTCPushLiveStreamRsp * _Nonnull rsp) {
            self.bypassLiveStreamingStarted = YES;
            
            [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcPushLivestream info:@{
                @"rtc_id" : self.conferenceID ?: @"null",
                @"push_url" : self.mediaURL ?: @"null"
            }];
            
            if (liveStatus == 1) {  // 已在直播中，不需要publish
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                    [self.delegate onAIRBRTCEvent:AIRBRTCEventBypassLiveStarted info:@{}];
                }
                return;
            }
            
            [self.liveRpcInterface publishLiveWithBlock:[VPLIVEPublishLiveReq VPLIVEPublishLiveReqWithUuid:self.liveID] onSuccess:^(VPLIVEPublishLiveRsp * _Nonnull rsp) {
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                    [self.delegate onAIRBRTCEvent:AIRBRTCEventBypassLiveStarted info:@{}];
                }
                
                [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishPublishLive info:@{
                    @"rtc_id" : self.conferenceID ?: @"null",
                    @"push_url" : self.mediaURL ?: @"null",
                    @"engine" : @"rtc"
                }];
                
            } onFailure:^(DPSError * _Nonnull error) {
                if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                    [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToPublishBypassLive message:ERR_MSG_FROM_DPSERROR(error)];
                }
                
                [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtClientPublishPublishLive info:@{
                    @"engine" : @"rtc",
                    @"error_code" : [NSString stringWithFormat:@"%d", error.code]
                }];
            }];
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToPushBypassLiveStreaming message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToGetBypassLiveDetail message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) createBypassLiveAndStartPublishing {
    LOGD("AIRBRTC::createBypassLiveAndStartPublishing.");
    VPROOMCreateLiveReq* req = [VPROOMCreateLiveReq VPROOMCreateLiveReqWithRoomId:self.room.roomID anchorId:self.userID title:@"" preStartDate:0 preEndDate:0 introduction:@"" coverUrl:@"" userDefineField:@""];
    [self.room.roomExtInterface createLiveWithBlock:req onSuccess:^(VPROOMCreateLiveRsp * _Nonnull rsp) {
        self.liveID = rsp.liveId;
        [self queryBypassLiveDetailAndStartPublishing];
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToCreateBypassLive message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

- (void) queryCurrentPeerListWithType:(AIRBRTCPeerType)type
                                 pageNum:(int32_t)pageNum
                                pageSize:(int32_t)pageSize
                               onSuccess:(void(^)(AIRBRoomChannelUserListResponse* rsp))onSuccess
                               onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRTC::queryCurrentPeerList.");
    if (type == AIRBRTCPeerTypeJoinedAlready) {
        
        VPRTCListConfUserReq* req = [VPRTCListConfUserReq VPRTCListConfUserReqWithConfId:self.conferenceID pageIndex:pageNum pageSize:pageSize];
        [self.rtcRpcInterface listConfUserWithBlock:req onSuccess:^(VPRTCListConfUserRsp * _Nonnull rsp) {
            @autoreleasepool {
                onSuccess([self createRTCUserListResponseWithInnerResponse:rsp.userList type:type hasMore:rsp.hasMore totalCount:rsp.totalCount]);
            }
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure([NSString stringWithFormat:@"listConfUser failed when %@", ERR_MSG_FROM_DPSERROR(error)]);
        }];
    } else if (type == AIRBRTCPeerTypeApplyingToJoinNow) {
        VPRTCListApplyLinkMicUserReq* req = [VPRTCListApplyLinkMicUserReq VPRTCListApplyLinkMicUserReqWithConfId:self.conferenceID pageIndex:pageNum pageSize:pageSize];
        [self.rtcRpcInterface listApplyLinkMicUserWithBlock:req onSuccess:^(VPRTCListApplyLinkMicUserRsp * _Nonnull rsp) {
            onSuccess([self createRTCUserListResponseWithInnerResponse:rsp.userList type:type hasMore:rsp.hasMore totalCount:rsp.totalCount]);
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure([NSString stringWithFormat:@"listApplyLinkMicUser failed when %@", ERR_MSG_FROM_DPSERROR(error)]);
        }];
    }
}

- (void) muteLocalMicphone:(BOOL)mute
                 onSuccess:(void(^)(void))onSuccess
                 onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRTC::muteLocalMicphone(%d).", mute);
    int result = ((int (*)(id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"muteLocalMicphone:"), mute);
    if (result == 0) {
        self.localMicphoneOpened = !mute;
        if (self.hasJoinedChannel){
            VPRTCReportRtcMuteReq* req = [VPRTCReportRtcMuteReq VPRTCReportRtcMuteReqWithConfId:self.conferenceID open:!mute];
            [self.rtcRpcInterface reportRtcMuteWithBlock:req onSuccess:^(VPRTCReportRtcMuteRsp * _Nonnull rsp) {
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
        } else{
            onSuccess();
        }
    } else  {
        onFailure([NSString stringWithFormat:@"Failed to mute local micphone with internal error(%d).", result]);
    }
}

- (void) muteRemoteMicphone:(BOOL)mute
                 remotePeer:(NSString *)userID
                  onSuccess:(void(^)(void))onSuccess
                  onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRTC::muteRemoteMicphone(%d, %@).", mute, userID);
    if (userID.length > 0 && [self.userID isEqual:self.room.roomOwnerID]) {
        int result = ((int (*)(id, SEL, BOOL, NSString*))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"muteRemoteAudioPlaying:remotePeer:"), mute, userID);
        if (result == 0) {
            VPRTCRtcMuteUserReq* req = [VPRTCRtcMuteUserReq VPRTCRtcMuteUserReqWithConfId:self.conferenceID userId:userID open:!mute];
            [self.rtcRpcInterface rtcMuteUserWithBlock:req onSuccess:^(VPRTCRtcMuteUserRsp * _Nonnull rsp) {
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
        } else  {
            onFailure([NSString stringWithFormat:@"Failed to mute remote micphone with internal error(%d).", result]);
        }
    }
}

- (void) muteAllRemoteMicphone:(BOOL)mute
                     onSuccess:(void(^)(void))onSuccess
                     onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRTC::muteAllRemoteMicphone(%d).", mute);
    if ([self.userID isEqual:self.room.roomOwnerID]) {
//        int result = ((int (*)(id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"muteAllRemoteAudioPlaying:"), mute);
//        if (result == 0) {
            VPRTCRtcMuteAllReq* req = [VPRTCRtcMuteAllReq VPRTCRtcMuteAllReqWithConfId:self.conferenceID open:!mute];
            [self.rtcRpcInterface rtcMuteAllWithBlock:req onSuccess:^(VPRTCRtcMuteAllRsp * _Nonnull rsp) {
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                LOGE("AIRBRTC::rtcRpcInterface rtcMuteAllWithBlock:%d failure(%@)", mute, ERR_MSG_FROM_DPSERROR(error));
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
//        } else  {
//            onFailure([NSString stringWithFormat:@"Failed to mute all remote micphone with internal error(%d).", result]);
//        }
    }
}

- (void) muteLocalCamera:(BOOL)mute
               onSuccess:(void(^)(void))onSuccess
               onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRTC::muteLocalCamera(%d).", mute);
    int result = ((int (*)(id, SEL, BOOL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"muteLocalCamera:"), mute);
    if (result == 0) {
        self.localCameraOpened = !mute;
        if (self.hasJoinedChannel){
            VPRTCOperateCameraReq* req = [VPRTCOperateCameraReq VPRTCOperateCameraReqWithConfId:self.conferenceID open:!mute];
            [self.rtcRpcInterface operateCameraWithBlock:req onSuccess:^(VPRTCOperateCameraRsp * _Nonnull rsp) {
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
        } else{
            onSuccess();
        }
    } else  {
        onFailure([NSString stringWithFormat:@"Failed to mute local camera with internal error(%d).", result]);
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:mute ? VPMonitorhubEventMhevtPaassdkRtcStopPreview : VPMonitorhubEventMhevtPaassdkRtcStartPreview info:nil];
}

- (void) setBasicFaceBeauty:(BOOL)enable whiteningLevel:(float)whiteningLevel smoothnessLevel:(float)smoothnessLevel {
    LOGD("AIRBRTC::setBasicFaceBeauty(%f, %f).", whiteningLevel, smoothnessLevel);
    ((void (*)(id, SEL, BOOL, float, float))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"setBasicFaceBeauty:whiteningLevel:smoothnessLevel:"), enable, whiteningLevel, smoothnessLevel);
}

- (void) subscribeRemoteAudioStream:(BOOL)sub fromUser:(NSString*)userID {
    LOGD("AIRBRTC::subscribeRemoteAudioStream(%d, %@).", sub, userID);
    ((void (*)(id, SEL, BOOL, NSString*))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"subscribeRemoteAudioStream:fromUser:"), sub, userID);
}

- (void) subscribeRemoteVideoStream:(BOOL)sub type:(AIRBRTCVideoStreamType)type fromUser:(NSString*)userID {
    LOGD("AIRBRTC::subscribeRemoteVideoStream(%d, %d, %@).", sub, type, userID);
    ((void (*)(id, SEL, BOOL, AIRBRTCVideoStreamType, NSString*))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"subscribeRemoteVideoStream:type:fromUser:"), sub, type, userID);
    
    if (sub) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcShowStream info:@{
            @"tartget_uid" : userID ?: @""
        }];
    } else {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcStopStream info:@{
            @"tartget_uid" : userID ?: @""
        }];
    }
    
}

- (void) subscribeRemoteScreenShareStream:(BOOL)sub fromUser:(NSString*)userID {
    LOGD("AIRBRTC::subscribeRemoteScreenShareStream(%d, %@).", sub, userID);
    ((void (*)(id, SEL, BOOL, NSString*))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"subscribeRemoteScreenShareStream:fromUser:"), sub, userID);
}

- (void) toggleLocalCamera {
    ((void (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"toggleLocalCamera"));
}

- (int)startScreenShare:(NSString*)appGroup {
    int res = ((int (*)(id, SEL, NSString*))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"startScreenShare:"), appGroup);
    if (res == 0){
        VPRTCShareScreenReq* req = [VPRTCShareScreenReq VPRTCShareScreenReqWithConfId:self.conferenceID open:YES];
        [self.rtcRpcInterface shareScreenWithBlock:req onSuccess:^(VPRTCShareScreenRsp * _Nonnull rsp) {
            
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRTC::rtcRpcInterface shareScreenWithBlock:YES failure(%@)", ERR_MSG_FROM_DPSERROR(error));
        }];
    }
    
    return res;
}

- (int)stopScreenShare {
    int res = ((int (*)(id, SEL))objc_msgSend)(self.rtcEngine, NSSelectorFromString(@"stopScreenShare"));
    //    if (res == 0){
    VPRTCShareScreenReq* req = [VPRTCShareScreenReq VPRTCShareScreenReqWithConfId:self.conferenceID open:NO];
    [self.rtcRpcInterface shareScreenWithBlock:req onSuccess:^(VPRTCShareScreenRsp * _Nonnull rsp) {
        
    } onFailure:^(DPSError * _Nonnull error) {
        LOGE("AIRBRTC::rtcRpcInterface shareScreenWithBlock:NO failure(%@)", ERR_MSG_FROM_DPSERROR(error));
    }];
    //    }
    
    return res;
}

- (void)startBypassLiveStreaming:(AIRBRTCBypassLiveResolutionType)resolutionType {
    if (![self.room.roomOwnerID isEqualToString:self.userID] || self.conferenceID.length == 0) {
        return;
    }
    
    LOGD("AIRBRTC::startBypassLiveStreaming:(%d).", resolutionType);
    self.bypassLiveResolutionType = resolutionType;
    if (self.liveID.length > 0) {
        [self queryBypassLiveDetailAndStartPublishing];
    } else {
        if ([self.room.roomOwnerID isEqualToString:self.userID]) {
            [self createBypassLiveAndStartPublishing];
        } else {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToStartBypassLive message:@"Only room owner can publish RTCBypassLive"];
            }
        }
    }
}

- (void) stopBypassLiveStreaming:(BOOL)destroy {
    LOGD("AIRBRTC::stopBypassLiveStreaming:(%d).", destroy);
    if (![self.room.roomOwnerID isEqualToString:self.userID]) {
        return;
    }
    
    // 停止推流
    if (self.conferenceID.length > 0 && self.bypassLiveStreamingStarted) {
        VPRTCStopLiveStreamReq* req = [VPRTCStopLiveStreamReq VPRTCStopLiveStreamReqWithConfId:self.conferenceID];
        [self.rtcRpcInterface stopLiveStreamWithBlock:req onSuccess:^(VPRTCStopLiveStreamRsp * _Nonnull rsp) {
            self.bypassLiveStreamingStarted = NO;
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                [self.delegate onAIRBRTCEvent:AIRBRTCEventBypassLiveStopped info:@{}];
            }
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToStopBypassLiveStreaming message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
    
    // 销毁直播
    if (destroy && self.liveID.length > 0) {
        VPROOMDestroyLiveReq* reqDestroyLive = [VPROOMDestroyLiveReq VPROOMDestroyLiveReqWithRoomId:self.room.roomID liveId:self.liveID];
        [self.room.roomExtInterface destroyLiveWithBlock:reqDestroyLive onSuccess:^(VPROOMDestroyLiveRsp * _Nonnull rsp) {
            self.liveID = @"";
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
                [self.delegate onAIRBRTCEvent:AIRBRTCEventBypassLiveDestroyed info:@{}];
            }
        } onFailure:^(DPSError * _Nonnull error) {
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToDestroyBypassLive message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
}

- (void) setBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type  userIDs:(NSArray<NSString*>*) userIDs onSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::setBypassLiveLayout:(%d).", type);
    VPRTCSetLayoutReq* req = [VPRTCSetLayoutReq VPRTCSetLayoutReqWithConfId:self.conferenceID userIds:userIDs model:(int)type];
    [self.rtcRpcInterface setLayoutWithBlock:req onSuccess:^(VPRTCSetLayoutRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) setCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel *>*) peerModels onSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::setCustomBypassLiveLayout.");
    VPRTCSetCustomLayoutReq* req = [VPRTCSetCustomLayoutReq VPRTCSetCustomLayoutReqWithConfId:self.conferenceID paneList:(NSArray<VPRTCPane*>*)peerModels crop:YES];
    [self.rtcRpcInterface setCustomLayoutWithBlock:req onSuccess:^(VPRTCSetCustomLayoutRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) startRecording:(AIRBRTCBypassLiveResolutionType)resolutionType
              onSuccess:(void(^)(void))onSuccess
              onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::startRecording:(%d).", resolutionType);
    self.recordResolutionType = resolutionType;
    if (!self.recording) {
        if ([self.room.roomOwnerID isEqualToString:self.userID]) {
            VPRTCStartRecordReq* req = [VPRTCStartRecordReq VPRTCStartRecordReqWithConfId:self.conferenceID resolutionType:(int)self.recordResolutionType];
            [self.rtcRpcInterface startRecordWithBlock:req onSuccess:^(VPRTCStartRecordRsp * _Nonnull rsp) {
                self.recording = YES;
                
                
                [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcStartRecord info:@{
                    @"rtc_id" : self.conferenceID ?: @"null"
                }];
                
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
        } else {
            onFailure(@"Only the room owner can start recording.");
        }
    } else {
        onFailure(@"Recording has been started.");
    }
}

- (void) pauseRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::pauseRecording.");
//    if (self.recording) {
    VPRTCStopRecordReq* req = [VPRTCStopRecordReq VPRTCStopRecordReqWithConfId:self.conferenceID];
    [self.rtcRpcInterface stopRecordWithBlock:req onSuccess:^(VPRTCStopRecordRsp * _Nonnull rsp) {
        self.recording = NO;
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
//    } else {
//        onFailure(@"Recording has not been started.");
//    }
}

- (void) resumeRecording:(AIRBRTCBypassLiveResolutionType)resolutionType
               onSuccess:(void(^)(void))onSuccess
               onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::resumeRecording:(%d).", resolutionType);
    self.recordResolutionType = resolutionType;
    if (!self.recording) {
        if ([self.room.roomOwnerID isEqualToString:self.userID]) {
            VPRTCStartRecordReq* req = [VPRTCStartRecordReq VPRTCStartRecordReqWithConfId:self.conferenceID resolutionType:(int)self.recordResolutionType];
            [self.rtcRpcInterface startRecordWithBlock:req onSuccess:^(VPRTCStartRecordRsp * _Nonnull rsp) {
                self.recording = YES;
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
        } else {
            onFailure(@"Only the room owner can start recording.");
        }
    } else {
        onFailure(@"Recording has been started.");
    }
}

- (void) stopRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    LOGD("AIRBRTC::stopRecording.");
//    if (self.recording) {
    VPRTCStopRecordReq* req = [VPRTCStopRecordReq VPRTCStopRecordReqWithConfId:self.conferenceID];
    [self.rtcRpcInterface stopRecordWithBlock:req onSuccess:^(VPRTCStopRecordRsp * _Nonnull rsp) {
        
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcStopRecord info:@{
            @"rtc_id" : self.conferenceID ?: @"null"
        }];
        
        self.recording = NO;
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
//    } else {
//        onFailure(@"Recording has not been started.");
//    }
}

- (void) getRecordedVideoUrlWithConferenceID:(NSString *)conferenceID onSuccess:(void (^)(NSString * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    LOGD("AIRBRTC::getRecordedVideoUrlWithConferenceID(%@).", conferenceID);
    if (conferenceID.length > 0) {
        VPRTCGetConfDetailReq* req = [VPRTCGetConfDetailReq VPRTCGetConfDetailReqWithConfId:conferenceID];
        [self.rtcRpcInterface getConfDetailWithBlock:req onSuccess:^(VPRTCGetConfDetailRsp * _Nonnull rsp) {
            onSuccess(rsp.confInfo.playbackUrl);
        } onFailure:^(DPSError * _Nonnull error) {
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(@"Failed to get url with empty conference id.");
    }
}

- (AIRBRoomChannelUserListResponse*) createRTCUserListResponseWithInnerResponse:(NSArray<VPRTCConfUserModel *>*)fromUserList type:(AIRBRTCPeerType)type hasMore:(BOOL)hasMore totalCount:(int32_t)totalCount {
    AIRBRoomChannelUserListResponse* response = [[AIRBRoomChannelUserListResponse alloc] init];
    NSMutableArray* tmpUserList = [[NSMutableArray alloc] init];
    
    if (type == AIRBRTCPeerTypeApplyingToJoinNow){
        response.totalCount = totalCount;
        response.hasMore = hasMore;
        for (VPRTCConfUserModel* model in fromUserList) {
            AIRBRoomChannelUser* user = [[AIRBRoomChannelUser alloc] init];
            user.openID = model.userId;
            user.nick = model.nickname ? : @"";
            user.extension = @{
                @"userNick" : model.nickname ? : @"",
                @"cameraOpen" : @(model.cameraStatus == 0 ? NO : YES),
                @"micOpen" : @(model.micphoneStatus == 0 ? NO : YES)
            };
            [tmpUserList addObject:user];
        }
    } else{
        for (VPRTCConfUserModel* model in fromUserList) {
            if (model.status == 3){
                AIRBRoomChannelUser* user = [[AIRBRoomChannelUser alloc] init];
                user.openID = model.userId;
                user.nick = model.nickname ? : @"";
                user.extension = @{
                    @"userNick" : model.nickname ? : @"",
                    @"cameraOpen" : @(model.cameraStatus == 0 ? NO : YES),
                    @"micOpen" : @(model.micphoneStatus == 0 ? NO : YES)
                };
                [tmpUserList addObject:user];
            }
        }
        response.totalCount = (int)tmpUserList.count;
        response.hasMore = hasMore;
    }
    
    response.userList = [NSArray arrayWithArray:tmpUserList];
    return response;
}

#pragma -mark AIRBRealTimeCommunicationEngineDelegate

- (void) reportJoinChannelSucceeded:(BOOL)succeeded errorCode:(NSString *)errorCode {
    LOGD("AIRBRTC::reportJoinChannelSucceeded(%d, %@).", succeeded, errorCode);
    if (succeeded) {
        self.hasJoinedChannel = YES;
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
            [self.delegate onAIRBRTCEvent:AIRBRTCEventJoinSucceeded info:@{
                @"conferenceID" : self.conferenceID
            }];
        }
        
        [AIRBMonitorHubManager sharedInstance].rtcModel.cameraImageWidth = self.config.videoStreamTypeHighDimensions.width;
        [AIRBMonitorHubManager sharedInstance].rtcModel.cameraImageHeight = self.config.videoStreamTypeHighDimensions.height;
        if ([self.userID isEqualToString:self.room.roomOwnerID]) {
            [AIRBMonitorHubManager sharedInstance].rtcModel.isHost = YES;
        }
        [AIRBMonitorHubManager sharedInstance].rtcModel.contentID = self.conferenceID;
        [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusRunning;
        
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcSdkJoinChannel info:@{
        @"error_code" : errorCode ?: @"0",
        @"error_msg" : @"",
        @"rtc_id" : self.conferenceID
    }];
    
    VPRTCReportJoinStatusReq* req = [VPRTCReportJoinStatusReq VPRTCReportJoinStatusReqWithConfId:self.conferenceID
                                                                         sourceId:self.userID
                                                                         nickname:@""
                                                                       joinStatus:succeeded ? 3 : 4
                                                                        errorCode:errorCode
                                                                     cameraStatus:self.localCameraOpened ? 1 : 0
                                                                   micphoneStatus:self.localMicphoneOpened ? 1 : 0
                                ];
    [self.rtcRpcInterface reportJoinStatusWithBlock:req onSuccess:^(VPRTCReportJoinStatusRsp * _Nonnull rsp) {
        
    } onFailure:^(DPSError * _Nonnull error) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
            [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToReportJoinChannelStatus message:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
    
    // 补充上报麦克风摄像头状态
    if (succeeded) {
        VPRTCReportRtcMuteReq* req = [VPRTCReportRtcMuteReq VPRTCReportRtcMuteReqWithConfId:self.conferenceID open:self.localMicphoneOpened];
        [self.rtcRpcInterface reportRtcMuteWithBlock:req onSuccess:^(VPRTCReportRtcMuteRsp * _Nonnull rsp) {
            
        } onFailure:^(DPSError * _Nonnull error) {
            
        }];
        
        VPRTCOperateCameraReq* req2 = [VPRTCOperateCameraReq VPRTCOperateCameraReqWithConfId:self.conferenceID open:self.localCameraOpened];
        [self.rtcRpcInterface operateCameraWithBlock:req2 onSuccess:^(VPRTCOperateCameraRsp * _Nonnull rsp) {
            
        } onFailure:^(DPSError * _Nonnull error) {
            
        }];
    }
}

- (void) reportLeaveChannelSucceeded:(BOOL)succeeded errorCode:(NSString *)errorCode {
    LOGD("AIRBRTC::reportLeaveChannelSucceeded(%d, %@).", succeeded, errorCode);
    if (succeeded) {
        [self.userCameraOpenedStatus removeAllObjects];
        [self.userMicOpenedStatus removeAllObjects];
        [self.userScreenShareOpenedStatus removeAllObjects];
        [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusNotRunning;
    }
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcSdkLeaveChannel info:@{
        @"error_code" : errorCode ?: @"0",
        @"error_msg" : @"",
        @"rtc_id" : self.conferenceID
    }];
    if (!succeeded && self.conferenceID.length > 0) {    // 离会失败则再次上报离会及错误
        [self.rtcRpcInterface reportLeaveStatusWithBlock:[VPRTCReportLeaveStatusReq VPRTCReportLeaveStatusReqWithConfId:self.conferenceID errorCode:errorCode] onSuccess:^(VPRTCReportLeaveStatusRsp * _Nonnull rsp) {
            ;
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRTC::reportLeaveChannelSucceeded:AIRBRTCFailedToReportLeaveChannelStatus");
            if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
                [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedToReportLeaveChannelStatus message:ERR_MSG_FROM_DPSERROR(error)];
            }
        }];
    }
}

- (void)onLocalPreviewStarted {
    LOGD("AIRBRTC::onLocalPreviewStarted.");
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventLocalPreviewStarted info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcStartPreview info:nil];
}

- (void) onAIRBRTCEngineErrorWithCode:(NSInteger)code message:(NSString*)msg{   // 分类处理过的错误
    LOGE("AIRBRTC::onAIRBRTCEngineErrorWithCode (%lx, %@).", code, msg);
    
    AIRBErrorCode errorCode;
    
    switch (code) {
        case 1:
            errorCode = AIRBRTCUnrecoverableFatalError;
            if (_rtcEngine) {
                ((void (*)(id, SEL))objc_msgSend)(_rtcEngine, NSSelectorFromString(@"destroyEngine"));
                _rtcEngine = nil;
            }
            break;
            
        case 2:
            errorCode = AIRBRTCRetryJoinChannelFatalError;
            break;
            
        case 3:
            errorCode = AIRBRTCRetryPublishFatalError;
            break;
            
        case 4:
            errorCode = AIRBRTCNerworkError;
            break;
            
        case 5:
            errorCode = AIRBRTCMicrophoneNoPermissionError;
            break;
            
        case 6: 
            errorCode = AIRBRTCCameraNoPermissionOrOccupiedError;
            break;
            
        case 7:
            errorCode = AIRBRTCFailedToSubscribe;
            break;
        case 8:
            errorCode = AIRBRTCJoinChannelDuplicately;
            break;
            
        default:
            break;
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcRtcError info:@{
        @"error_type" : @"AIRBErrorCode",
        @"error_code" : [NSString stringWithFormat:@"%lx", errorCode],
        @"error_msg" : msg ?: @"null"
    }];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
        [self.delegate onAIRBRTCErrorWithCode:errorCode message:msg];
    }
}

- (void)onRTCEngineError:(int)errorCode message:(NSString*)errorMessage{    // 未分类处理过的错误
    LOGE("AIRBRTC::onRTCEngineError (%lx, %@).", errorCode, errorMessage);
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcRtcError info:@{
        @"error_type" : @"InternalError",
        @"error_code" : [NSString stringWithFormat:@"%d", errorCode],
        @"error_msg" : errorMessage ?: @"null"
    }];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCErrorWithCode:message:)]) {
        [self.delegate onAIRBRTCErrorWithCode:AIRBRTCFailedWithInternalError message:[NSString stringWithFormat:@"%@(%x)", errorMessage, errorCode]];
    }
}

- (void)onRemotePeerCameraViewAvailable:(NSString *)userID view:(UIView *)view {
    LOGD("AIRBRTC::onRemotePeerViewAvailable(%@).", userID);
    
    [self.userCameraOpenedStatus setObject:@(YES) forKey:userID];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:userID:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:YES userID:userID view:view type:AIRBRTCVideoViewTypeCamera];
    }
 
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:userID view:view type:AIRBRTCVideoViewTypeCamera];
    }
}

- (void)onRemotePeerCameraViewUnavailable:(NSString*)userID{
    LOGD("AIRBRTC::onRemotePeerCameraViewUnavailable(%@).", userID);
    
    [self.userCameraOpenedStatus setObject:@(NO) forKey:userID];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:userID:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:NO userID:userID view:nil type:AIRBRTCVideoViewTypeCamera];
    }
}

- (void)onRemotePeerScreenShareViewAvailable:(NSString *)userID view:(UIView *)view {
    LOGD("AIRBRTC::onRemotePeerViewAvailable(%@).", userID);
    
    [self.userScreenShareOpenedStatus setObject:@(YES) forKey:userID];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:userID:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:YES userID:userID view:view type:AIRBRTCVideoViewTypeScreen];
    }

    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:userID view:view type:AIRBRTCVideoViewTypeScreen];
    }
}

- (void)onRemotePeerScreenShareViewUnavailable:(NSString*)userID{
    LOGD("AIRBRTC::onRemotePeerScreenShareViewUnavailable(%@).", userID);
    
    [self.userScreenShareOpenedStatus setObject:@(NO) forKey:userID];
    
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerViewAvailable:userID:view:type:)]) {
        [self.delegate onAIRBRTCRemotePeerViewAvailable:NO userID:userID view:nil type:AIRBRTCVideoViewTypeScreen];
    }
}

- (void)onFirstRemoteCameraVideoFrameDrawn:(NSString *)userID{
    LOGD("AIRBRTC::onFirstRemoteCameraVideoFrameDrawn(%@).", userID);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerVideoFirstFrameDrawn:type:)]) {
        [self.delegate onAIRBRTCRemotePeerVideoFirstFrameDrawn:userID type:AIRBRTCVideoViewTypeCamera];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcFirstFrameRender info:@{
        @"target_uid" : userID ?: @"null",
        @"source_type" : @"camera"
    }];
}

- (void)onFirstRemoteScreenShareVideoFrameDrawn:(NSString *)userID{
    LOGD("AIRBRTC::onFirstRemoteScreenShareVideoFrameDrawn(%@).", userID);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCRemotePeerVideoFirstFrameDrawn:type:)]) {
        [self.delegate onAIRBRTCRemotePeerVideoFirstFrameDrawn:userID type:AIRBRTCVideoViewTypeScreen];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcFirstFrameRender info:@{
        @"target_uid" : userID ?: @"null",
        @"source_type" : @"screen"
    }];
}

- (void)onRTCEngineActiveSpeaker:(NSString *_Nonnull)userID{
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCActiveSpeaker:)]) {
        [self.delegate onAIRBRTCActiveSpeaker:userID];
    }
}

- (void)onRTCEngineAudioVolumeCallback:(id)volumeInfoArray totalVolume:(int)totalVolume{
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCAudioVolumeCallback:totalVolume:)]) {
        [self.delegate onAIRBRTCAudioVolumeCallback:(NSArray< AIRBRTCUserVolumeInfo*>*)volumeInfoArray totalVolume:totalVolume];
    }
}

- (void)onRTCEngineNetworkConnectionLost{
    LOGD("AIRBRTC::onRTCEngineNetworkConnectionLost.");
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventNetworkConnectionLost info:@{}];
    }
    
    [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void)onRTCEngineNetworkReconnectStart{
    LOGD("AIRBRTC::onRTCEngineNetworkReconnectStart.");
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventNetworkReconnectStart info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcNetReconnect info:nil];
}

- (void)onRTCEngineNetworkReconnectSuccess{
    LOGD("AIRBRTC::onRTCEngineNetworkReconnectSuccess.");
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventNetworkReconnectSuccess info:@{}];
    }
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcNetReconnectSuc info:nil];
    
    [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusRunning;
}

- (void)onRTCEngineNetworkConnectFailed{
    LOGD("AIRBRTC::onRTCEngineNetworkConnectFailed.");
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventNetworkConnectionFailed info:@{}];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcNetDisconnect info:nil];
    
    [AIRBMonitorHubManager sharedInstance].rtcModel.status = AIRBMonitorHubComponentStatusNotRunning;
}

- (void)onRTCEngineNetworkQualityChanged:(NSString *_Nonnull)userID
                        upNetworkQuality:(int)upQuality
                      downNetworkQuality:(int)downQuality{
    LOGD("AIRBRTC::onRTCEngineNetworkQualityChanged:%@ upNetworkQuality:@d downNetworkQuality: %d.", userID, upQuality, downQuality);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCNetworkQualityChanged:upNetworkQuality:downNetworkQuality:)]) {
        [self.delegate onAIRBRTCNetworkQualityChanged:userID upNetworkQuality:upQuality downNetworkQuality:downQuality];
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcNetChanged info:@{
        @"target_uid" : userID ?: @"null",
        @"up_quality" : [NSString stringWithFormat:@"%d", upQuality],
        @"down_quality" : [NSString stringWithFormat:@"%d", downQuality]
    }];
}

- (void)onRTCEngineLocalVideoStatistics:(NSDictionary*)statistic {
    if (statistic) {
        if ([[statistic objectForKey:@"source_type"] isEqualToString:@"video"]) {
            [AIRBMonitorHubManager sharedInstance].rtcModel.cameraVideoStreamStatistics = [statistic copy];
        } else if ([[statistic objectForKey:@"source_type"] isEqualToString:@"screen"] || [[statistic objectForKey:@"source_type"] isEqualToString:@"both"]) {
            [AIRBMonitorHubManager sharedInstance].rtcModel.screenVideoStreamStatistics = [statistic copy];
        }
    }
}

- (void)onRTCEngineLocalAudioStatistics:(NSDictionary*)statistic {
    if (statistic) {
        if ([[statistic objectForKey:@"source_type"] isEqualToString:@"mic"]) {
            [AIRBMonitorHubManager sharedInstance].rtcModel.audioStreamStatistics = [statistic copy];
        }
    }
}

- (void)onRTCEngineRemoteVideoStatistics:(NSDictionary*)statistic {
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcRemoteVideoStats info:statistic];
}

- (void)onRTCEngineRemoteAudioStatistics:(NSDictionary*)statistic {
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcRemoteAudioStats info:statistic];
}

- (void)onRTCEngineTotalStatistics:(NSDictionary*)statistic {
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRtcStats info:statistic];
}

- (void)onRTCEngineRemoteUserOnlineNotify:(NSString *)uid userInfo:(NSDictionary*)userInfo{
    LOGD("AIRBRTC::onRTCEngineRemoteUserOnlineNotify(%@).", uid);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventRemoteUserOnline info:@{
            @"userID" : uid,
            @"userNick" : [userInfo objectForKey:@"displayName"],
            @"cameraOpened" : [self.userCameraOpenedStatus objectForKey:uid] ? : [userInfo objectForKey:@"hasCameraMaster"],
            @"micphoneOpened" : [self.userMicOpenedStatus objectForKey:uid] ? : [userInfo objectForKey:@"hasAudio"],
            @"screenSharing" : [self.userScreenShareOpenedStatus objectForKey:uid] ? : [userInfo objectForKey:@"hasScreenSharing"]
        }];
    }
}

- (void)onRTCEngineRemoteUserOfflineNotify:(NSString *)uid reason:(NSString*)reason{
    LOGD("AIRBRTC::onRTCEngineRemoteUserOfflineNotify(%@) reason(%@).", uid, reason);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventRemoteUserOffline info:@{
            @"userID" : uid,
            @"reason" : reason
        }];
    }
}

- (void)onRTCEngineUserVideoMuted:(NSString *)uid videoMuted:(BOOL)isMute{
    LOGD("AIRBRTC::onRTCEngineUserVideoMuted(%@) muted(%d).", uid, isMute);
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventRemoteUserCameraMuted info:@{
            @"userID" : uid,
            @"muted" : @(isMute)
        }];
    }
}

- (void)onRTCEngineUserAudioMuted:(NSString *)uid audioMuted:(BOOL)isMute{
    LOGD("AIRBRTC::onRTCEngineUserAudioMuted(%@) muted(%d).", uid, isMute);
    [self.userMicOpenedStatus setObject:@(!isMute) forKey:uid];
    if ([self.delegate respondsToSelector:@selector(onAIRBRTCEvent:info:)]) {
        [self.delegate onAIRBRTCEvent:AIRBRTCEventRemoteUserMicphoneMuted info:@{
            @"userID" : uid,
            @"muted" : @(isMute)
        }];
    }
}

@end
