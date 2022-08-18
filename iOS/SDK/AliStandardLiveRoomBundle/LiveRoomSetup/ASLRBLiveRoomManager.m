//
//  ASLRBLiveRoomManager.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/7/20.
//

#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "ASLRBLiveRoomManager.h"
#import "../LiveRoomLogin/ASLRBLoginManager.h"
#import "../LiveRoomSetup/ASLRBAppInitConfig.h"
#import "../LiveRoomSetup/ASLRBLiveInitConfig.h"
#import "../LiveRoom/ASLRBAudienceViewController.h"
#import "../LiveRoom/ASLRBAnchorViewController.h"
#import "../LiveRoom/ASLRBPlaybackViewController.h"
#import "ASLRBLogger.h"


@interface ASLRBLiveRoomManager()
@property (strong, nonatomic) ASLRBLoginManager* loginManager;
@property (strong, nonatomic) ASLRBAppInitConfig* appInitConfig;
@end

@implementation ASLRBLiveRoomManager

+ (ASLRBLiveRoomManager*) sharedInstance {
    static ASLRBLiveRoomManager *sharedInstance = nil;
    static dispatch_once_t ASLRBLiveRoomManagerToken;
    dispatch_once(&ASLRBLiveRoomManagerToken, ^{
        sharedInstance = [[ASLRBLiveRoomManager alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _loginManager = [[ASLRBLoginManager alloc] init];
    }
    return self;
}

- (void) globalInitOnceWithConfig:(ASLRBAppInitConfig *)config
                        onSuccess:(void (^)(void))onSuccess
                        onFailure:(void (^)(NSString * _Nonnull))onFailure
{
    NSParameterAssert(config.appID != nil);
    NSParameterAssert(config.appKey != nil);
    NSParameterAssert(config.appServerUrl != nil);
    NSParameterAssert(config.appServerSignSecret != nil);
    NSParameterAssert(config.userID != nil);
    NSParameterAssert(config.userNick != nil);
    
    self.appInitConfig = config;
    __weak typeof(self) weakSelf = self;
    [[ASLRBLogger sharedInstance] setOnLog:^(NSString * _Nonnull log) {
        if (weakSelf.onLogMessage) {
            weakSelf.onLogMessage(log);
        } else {
            NSLog(@"%@", log);
        }
    }];
    
    onSuccess();
}

- (void) createLiveRoomVCWithConfig:(ASLRBLiveInitConfig *)config
                        onCompletion:(void(^)(ASLRBLiveRoomViewController*))onCompletion{
    [self createLiveRoomVCWithConfig:config onSuccess:^(ASLRBLiveRoomViewController * _Nonnull liveRoomVC) {
        onCompletion(liveRoomVC);
    } onFailure:^(NSString * _Nonnull errorMessage) {
        LOG("ASLRBLiveRoomManager::createLiveRoomVC failed(%@)", errorMessage);
        onCompletion(nil);
    }];
}

- (void) createLiveRoomVCWithConfig:(ASLRBLiveInitConfig *)config
                          onSuccess:(void (^)(ASLRBLiveRoomViewController * _Nonnull))onSuccess
                          onFailure:(void (^)(NSString * _Nonnull))onFailure {
    LOG("ASLRBLiveRoomManager::createLiveRoomVC config(role:%d, liveID:%@)", config.role, config.liveID);
    __weak typeof(self) weakSelf = self;
    [self.loginManager loginWithConfig:self.appInitConfig onSuccess:^(ASLRBAppInitConfig * _Nonnull loginedAppInitConfig) {
        [weakSelf createLiveRoomVCAfterLoginWithAppInitConfig:loginedAppInitConfig
                                               liveInitConfig:config
                                                    onSuccess:onSuccess
                                                    onFailure:onFailure];
                                     
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void)createLiveRoomVCAfterLoginWithAppInitConfig:(ASLRBAppInitConfig*)appInitconfig
                                     liveInitConfig:(ASLRBLiveInitConfig *)liveInitConfig
                                          onSuccess:(void (^)(ASLRBLiveRoomViewController * _Nonnull))onSuccess
                                          onFailure:(void (^)(NSString * _Nonnull))onFailure {
    
    __block ASLRBLiveRoomViewController * liveRoomViewController = nil;
    if (liveInitConfig.liveID.length > 0) {
        [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] getLiveDetailWithLiveID:liveInitConfig.liveID onSuccess:^(NSDictionary * _Nonnull response)
        {
            dispatch_async(dispatch_get_main_queue(), ^{
                int32_t status = [[response valueForKey:@"status"] intValue];
                
                if (status == 2 && liveInitConfig.enableLivePlayback) {
                    liveRoomViewController = [[ASLRBPlaybackViewController alloc] initWithAppInitConfig:appInitconfig liveInitConfig:liveInitConfig liveDetail:response delegate:self.loginManager];
                } else {
                    // 使用服务端的连麦配置参数
                    liveInitConfig.enableLinkMic = [[response objectForKey:@"enableLinkMic"] boolValue];

                    if (liveInitConfig.role == ASLRBUserRoleAnchor) {
                        liveRoomViewController = [[ASLRBAnchorViewController alloc] initWithAppInitConfig:appInitconfig liveInitConfig:liveInitConfig liveDetail:response delegate:self.loginManager];
                    } else {
                        liveRoomViewController = [[ASLRBAudienceViewController alloc] initWithAppInitConfig:appInitconfig liveInitConfig:liveInitConfig liveDetail:response delegate:self.loginManager];
                    }
                }
                onSuccess(liveRoomViewController);
            });
        }
         onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    } else {
        if (liveInitConfig.role == ASLRBUserRoleAnchor) {
            dispatch_async(dispatch_get_main_queue(), ^{
                liveRoomViewController = [[ASLRBAnchorViewController alloc] initWithAppInitConfig:appInitconfig liveInitConfig:liveInitConfig liveDetail:nil delegate:self.loginManager];
                onSuccess(liveRoomViewController);
            });
        } else {
            onFailure(@"can't create vc with audience when no live-id here.");
        }
    }
}

@end
