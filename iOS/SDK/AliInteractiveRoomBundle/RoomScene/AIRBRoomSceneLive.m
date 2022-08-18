//
//  AIRBRoomSceneLive.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/9/15.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRoomSceneLive.h"

#import <vpaassdk/scenelive/VPSCENELIVESceneliveRpcInterface.h>
#import <vpaassdk/scenelive/VPSCENELIVESceneliveModule.h>

#import "../Utilities/AIRBGlobalMacro.h"
#import "AIRBRoomSceneLiveBusinessInfo.h"
     

@interface AIRBRoomSceneLive()
@property (strong, nonatomic) VPSCENELIVESceneliveModule* sceneLiveModule;
@property (strong, nonatomic) VPSCENELIVESceneliveRpcInterface* sceneLiveRpcInterface;
@end

@implementation AIRBRoomSceneLive

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
        _sceneLiveModule = [VPSCENELIVESceneliveModule getModule:userID];
        if (!_sceneLiveModule) {
            return nil;
        }
        
        _sceneLiveRpcInterface = [_sceneLiveModule getRpcInterface];
        if (!_sceneLiveRpcInterface) {
            return nil;
        }
    }
    return self;
}

- (void) createLiveWithTitle:(NSString*)title
                      notice:(NSString*)notice
                    coverUrl:(NSString*)url
                  anchorNick:(NSString*)nick
               enableLinkMic:(BOOL)enable
                   extension:(NSDictionary*)extension
                   onSuccess:(void(^)(NSDictionary* response))onSuccess
                   onFailure:(void(^)(NSString* error))onFailure {
    VPSCENELIVESceneCreateLiveReq* req = [VPSCENELIVESceneCreateLiveReq VPSCENELIVESceneCreateLiveReqWithTitle:title notice:notice coverUrl:url anchorId:self.userID extension:extension anchorNick:nick enableLinkMic:enable];
    [self.sceneLiveRpcInterface createLiveWithBlock:req onSuccess:^(VPSCENELIVESceneCreateLiveRsp * _Nonnull rsp) {
        onSuccess(
                  @{
                      @"room_id" : rsp.roomId ? : @"",
                      @"anchor_id" : rsp.anchorId ? : @"",
                      @"anchor_nick" : rsp.anchorNick ? : @"",
                      @"live_id" : rsp.liveId ?  : @"",
                      @"push_url" : rsp.pushUrl ? : @"",
                      @"live_url" : rsp.liveUrl ? : @"",
                      @"playback_url" : rsp.playbackUrl ? : @"",
                      @"title" : rsp.title ? : @"",
                      @"notice" : rsp.notice ? : @"",
                      @"cover_url" : rsp.coverUrl ? : @"",
                      @"enableLinkMic" : @(rsp.enableLinkMic),
                      @"extension" : rsp.extension ?: @{}
                  }
                  );
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) stopLiveWithLiveID:(NSString*)liveID onSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    VPSCENELIVESceneStopLiveReq* req = [VPSCENELIVESceneStopLiveReq VPSCENELIVESceneStopLiveReqWithLiveId:liveID userId:self.userID];
    [self.sceneLiveRpcInterface stopLiveWithBlock:req onSuccess:^(VPSCENELIVESceneStopLiveRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) getLiveDetailWithLiveID:(NSString*)liveID onSuccess:(void(^)(NSDictionary* response))onSuccess onFailure:(void(^)(NSString* error))onFailure {
    
    VPSCENELIVESceneGetLiveDetailReq* req = [VPSCENELIVESceneGetLiveDetailReq VPSCENELIVESceneGetLiveDetailReqWithLiveId:liveID];
    [self.sceneLiveRpcInterface getLiveDetailWithBlock:req onSuccess:^(VPSCENELIVESceneGetLiveDetailRsp * _Nonnull rsp) {
        onSuccess(
                  @{
                      @"room_id" : rsp.roomId ? : @"",
                      @"anchor_id" : rsp.anchorId ? : @"",
                      @"anchor_nick" : rsp.anchorNick ? : @"",
                      @"live_id" : rsp.liveId ?  : @"",
                      @"conf_id" : rsp.confId ? : @"",
                      @"push_url" : rsp.pushUrl ? : @"",
                      @"live_url" : rsp.liveUrl ? : @"",
                      @"playback_url" : rsp.playbackUrl ? : @"",
                      @"status" : @(rsp.status),
                      @"uv" : @(rsp.uv),
                      @"pv" : @(rsp.pv),
                      @"online_count" : @(rsp.onlineCount),
                      @"title" : rsp.title ? : @"",
                      @"notice" : rsp.notice ? : @"",
                      @"cover_url" : rsp.coverUrl ? : @"",
                      @"enableLinkMic" : @(rsp.enableLinkMic),
                      @"extension" : rsp.extension ?: @{}
                  }
                  );
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) getLiveListWithStatus:(int32_t)status
                       pageNum:(int32_t)pageNum
                      pageSize:(int32_t)pageSize
                     onSuccess:(void(^)(NSDictionary* response))onSuccess
                     onFailure:(void(^)(NSString* error))onFailure {
    VPSCENELIVESceneGetLiveListReq* req = [VPSCENELIVESceneGetLiveListReq VPSCENELIVESceneGetLiveListReqWithStatus:status pageNumber:pageNum pageSize:pageSize];
    [self.sceneLiveRpcInterface getLiveListWithBlock:req onSuccess:^( VPSCENELIVESceneGetLiveListRsp * _Nonnull rsp) {
        onSuccess(@{
            @"total_live" : @(rsp.totalCount),
            @"has_more" : @(rsp.hasMore),
            @"total_page" : @(rsp.pageTotal),
            @"live_list" : rsp.liveList ? : @{}
                  }
                  );
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) updateLiveBusinessInfo:(AIRBRoomSceneLiveBusinessInfo *)info onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    VPSCENELIVESceneUpdateLiveReq* req = [ VPSCENELIVESceneUpdateLiveReq VPSCENELIVESceneUpdateLiveReqWithLiveId:info.liveID
                                                                         title:info.liveTitle
                                                                        notice:info.liveNotice
                                                                      coverUrl:info.liveCoverURL
                                                                      anchorId:nil
                                                                    anchorNick:nil
                                                                     extension:info.liveCustomData];
    [self.sceneLiveRpcInterface updateLiveWithBlock:req onSuccess:^(VPSCENELIVESceneUpdateLiveRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

@end
