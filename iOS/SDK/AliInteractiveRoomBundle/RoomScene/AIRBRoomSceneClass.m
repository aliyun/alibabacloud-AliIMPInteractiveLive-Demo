//
//  AIRBRoomSceneClass.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/9/15.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRoomSceneClass.h"

#import <vpaassdk/sceneclass/VPSCENECLASSSceneclassModule.h>
#import <vpaassdk/sceneclass/VPSCENECLASSSceneclassRpcInterface.h>

#import "../Utilities/AIRBGlobalMacro.h"

@interface AIRBRoomSceneClass()
@property (strong, nonatomic) VPSCENECLASSSceneclassModule* sceneClassModule;
@property (strong, nonatomic) VPSCENECLASSSceneclassRpcInterface* sceneClassRpcInterface;
@end

@implementation AIRBRoomSceneClass

- (instancetype) initWithUserID:(NSString*)userID {
    self = [super init];
    if (self) {
        _userID = userID;
        _sceneClassModule = [VPSCENECLASSSceneclassModule getModule:userID];
        if (!_sceneClassModule) {
            return nil;
        }
        
        _sceneClassRpcInterface = [_sceneClassModule getRpcInterface];
        if (!_sceneClassRpcInterface) {
            return nil;
        }
    }
    return self;
}

- (void)createClassWithTitle:(nonnull NSString *)title
                    nickName:(nonnull NSString *)nickName
                   onSuccess:(void(^)(NSDictionary* response))onSuccess
                   onFailure:(void(^)(NSString* error))onFailure;{
    VPSCENECLASSCreateClassReq* req = [VPSCENECLASSCreateClassReq VPSCENECLASSCreateClassReqWithTitle:title createNickname:nickName];
    [self.sceneClassRpcInterface createClassWithBlock:req onSuccess:^(VPSCENECLASSCreateClassRsp * _Nonnull rsp) {
        onSuccess(
                  @{
                      @"class_id":rsp.classId ? : @"",
                      @"title":     rsp.title ? : @"",
                      @"creator_user_id":rsp.createUserId ? :@"",
                      @"creator_nick_name":rsp.createNickname ? :@"",
                      @"status": [NSNumber numberWithInt:rsp.status],
                      @"start_time": [NSNumber numberWithLongLong:rsp.startTime],
                      @"end_time": [NSNumber numberWithLongLong:rsp.endTime],
                      @"room_id" : rsp.roomId ? : @"",
                      @"live_id" : rsp.liveId ? : @"",
                      @"conference_id" : rsp.confId ? : @"",
                      @"whiteboard_id" : rsp.whiteboardId ? : @"",
                      @"whiteboard_record_id" : rsp.whiteboardRecordId ? : @""
                  }
                  );
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

-(void)getClassDetailWithClassID:(NSString*)classID
                       onSuccess:(void(^)(NSDictionary* response))onSuccess
                       onFailure:(void(^)(NSString* error))onFailure {
    VPSCENECLASSGetClassDetailReq* req = [VPSCENECLASSGetClassDetailReq VPSCENECLASSGetClassDetailReqWithClassId:classID];
    [self.sceneClassRpcInterface getClassDetailWithBlock:req onSuccess:^(VPSCENECLASSGetClassDetailRsp * _Nonnull rsp) {
        onSuccess(
                  @{
                      @"class_id":rsp.classId ? : @"",
                      @"title":     rsp.title ? : @"",
                      @"creator_user_id":rsp.createUserId ? :@"",
                      @"creator_nick_name":rsp.createNickname ? :@"",
                      @"status": [NSNumber numberWithInt:rsp.status],
                      @"start_time": [NSNumber numberWithLongLong:rsp.startTime],
                      @"end_time": [NSNumber numberWithLongLong:rsp.endTime],
                      @"room_id" : rsp.roomId ? : @"",
                      @"live_id" : rsp.liveId ? : @"",
                      @"conference_id" : rsp.confId ? : @"",
                      @"whiteboard_id" : rsp.whiteboardId ? : @"",
                      @"whiteboard_record_id" : rsp.whiteboardRecordId ? : @""
                  }
                  );
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void)startClassWithClassID:(NSString*)classID
                    onSuccess:(void (^)(void))onSuccess
                    onFailure:(void (^)(NSString * _Nonnull))onFailure {
    VPSCENECLASSStartClassReq* req = [VPSCENECLASSStartClassReq VPSCENECLASSStartClassReqWithClassId:classID];
    [self.sceneClassRpcInterface startClassWithBlock:req onSuccess:^(VPSCENECLASSStartClassRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

-(void)stopClassWithClassID:(NSString*)classID
                  OnSuccess:(void(^)(void))onSuccess
                  onFailure:(void(^)(NSString* error))onFailure {
    VPSCENECLASSStopClassReq* req = [VPSCENECLASSStopClassReq VPSCENECLASSStopClassReqWithClassId:classID];
    [self.sceneClassRpcInterface stopClassWithBlock:req onSuccess:^(VPSCENECLASSStopClassRsp * _Nonnull rsp) {
        onSuccess();
    } onFailure:^(DPSError * _Nonnull error) {
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

@end
