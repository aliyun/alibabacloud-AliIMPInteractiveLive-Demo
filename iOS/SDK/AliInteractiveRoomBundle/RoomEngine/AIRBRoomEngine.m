//
//  AIRBRoomEngine.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRoomEngine.h"

#import <libdps/DPSPubEngine.h>
#import <libdps/DPSPubSettingService.h>
#import <libdps/DPSMediaHost.h>
#import <libdps/DPSError.h>
#import <libdps/DPSConnectionStatus.h>
#import <libdps/DPSAuthToken.h>
#import <libdps/DPSAuthTokenGotCallback.h>
#import <libdps/DPSPubAuthTokenCallback.h>
#import <libdps/DPSPubEngineListener.h>
#import <libdps/DPSAuthListener.h>
#import <libdps/DPSLogHandler.h>
#import <libdps/DPSUserId.h>
#import <libdps/DPSAuthService.h>
#import <libdps/DPSPubManager.h>
#import <libdps/DPSModuleInfo.h>

#import <vpaassdk/mps/VPMPSEngine.h>
#import <vpaassdk/mps/VPMpsEngineType.h>
#import <vpaassdk/mps/VPMPSSettingService.h>
#import <vpaassdk/mps/VPMPSAuthTokenCallback.h>
#import <vpaassdk/mps/VPMPSLogHandler.h>
#import <vpaassdk/mps/VPMPSManager.h>

#import <vpaassdk/room/VPROOMRoomModule.h>
#import <vpaassdk/room/VPROOMRoomRpcInterface.h>
#import <vpaassdk/live/VPLIVELiveModule.h>
#import <vpaassdk/chat/VPCHATChatModule.h>
#import <vpaassdk/rtc/VPRTCRtcModule.h>
#import <vpaassdk/wb/VPWBWbModule.h>
#import <vpaassdk/doc/VPDOCDocModule.h>
#import <vpaassdk/scenelive/VPSCENELIVESceneliveModule.h>
#import <vpaassdk/sceneclass/VPSCENECLASSSceneclassModule.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGCloudconfigModule.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGCloudconfigExtInterface.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGCloudconfigRpcInterface.h>

#import <vpaassdk/meta_ai/VPMETA_AIMetaAiModule.h>

#import <vpaassdk/monitorhub/VPMonitorhubNetType.h>

#import "AIRBProduct360.h"
#import "AIRBRoomChannel.h"
#import "AIRBRoomEngineConfig.h"
#import "AIRBRoomEngineAuthToken.h"
#import "AIRBEnvironments.h"
#import "AIRBLoggerManager.h"
#import "AIRBUtility.h"
#import "AIRBRoomEngineRoomListResponse.h"
#import "AIRBRoomBasicInfo.h"
#import "AliInteractiveRoomLogger.h"
#import "AIRBRoomSceneLive.h"
#import "AIRBRoomSceneClass.h"
#import "../Utilities/AIRBGlobalMacro.h"
#import "../MonitorHub/AIRBMonitorHubManager.h"
#import "../Utilities/AIRBReachability.h"
#import "AIRBRoomEngineReloginUtility.h"

NSString *const kAIRBReachabilityBecameWiFiNotification = @"kAIRBReachabilityBecameWiFiNotification";
NSString *const kAIRBReachabilityBecameWWANNotification = @"kAIRBReachabilityBecameWWANNotification";
NSString *const kAIRBReachabilityBecameUnrechableNotification = @"kAIRBReachabilityBecameUnrechableNotification";
NSString *const kAIRBRoomEngineLoginedNotification = @"kAIRBRoomEngineLoginedNotification";
     

@interface AIRBRoomEngine() <VPMPSAuthTokenCallback, DPSAuthListener, VPMPSLogHandler, AIRBLoggerManagerDelegate>
@property (nonatomic, strong) AIRBRoomEngineConfig* engineConfig;
@property (nonatomic, strong) VPMPSEngine* mpsEngine;
@property (nonatomic, copy) NSString* userID;
@property (nonatomic, strong) id<AIRBRoomSceneLiveProtocol> sceneLive;
@property (nonatomic, strong) id<AIRBRoomSceneClassProtocol> sceneClass;
@property (nonatomic, strong) AIRBReachability* reachability;
@property (nonatomic, strong) id<AIRBProduct360Protocol> product360Client;
@end

@implementation AIRBRoomEngine

- (void) setLogLevel:(AIRBLoggerLevel)logLevel {
    [[AIRBLoggerManager sharedInstance] setLoggerLevel:logLevel];
}

+ (AIRBRoomEngine*) sharedInstance {
    static AIRBRoomEngine *sharedInstance = nil;
    static dispatch_once_t onceAIRBRoomEngineSharedInstanceToken;
    dispatch_once(&onceAIRBRoomEngineSharedInstanceToken, ^{
        sharedInstance = [[AIRBRoomEngine alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _mpsEngine = [VPMPSEngine createMPSEngine:(VPMpsEngineTypeMpsEngineTypeMeta)];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationWillResignActiveNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationDidBecomeActiveNotification object:nil];
    }
    return self;
}

- (void) globalInitOnceWithConfig:(nonnull AIRBRoomEngineConfig*)config {
    if (!self.mpsEngine.isStarted) {
        LOGD("AIRBRoomEngine::globalInitOnceWithConfig(%p), isStarted(%d)", config, NO);
        self.engineConfig = config;
        [[AIRBLoggerManager sharedInstance] setDelegate:self];
        [self setupNetworkReachability];
        [self setupDPSEngine];
    } else {
        LOGD("AIRBRoomEngine::globalInitOnceWithConfig(%p), isStarted(%d)", config, YES);
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
            [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineStarted info:nil];
        }
    }
}

- (void) dealloc {
    [VPMPSEngine releaseMPSEngine];
}

- (void) setupDPSEngine {
#ifdef DEBUG
    [VPMPSEngine setLogHandler:VPMPSLogLevelMPSLOGLEVELDEBUG handler:self];
#else
    [VPMPSEngine setLogHandler:VPMPSLogLevelMPSLOGLEVELERROR handler:self];
#endif
    
    // 获取配置服务
    VPMPSSettingService *setting = self.mpsEngine.getSettingService;
    [setting setDataPath:[AIRBEnvironments shareInstance].dataPath];
    [setting setAppKey:self.engineConfig.appKey];
    
    if ([[AIRBEnvironments shareInstance] usePrereleaseEnvironment]) {
        [setting setEnvType:DPSEnvTypeEnvTypePreRelease];
    } else {
        [setting setEnvType:DPSEnvTypeEnvTypeOnline];
    }
    
    [setting setLonglinkServerAddress:[AIRBEnvironments shareInstance].longLinkAddr];
    [setting setOSName:[AIRBEnvironments shareInstance].osName];
    [setting setAppName:@"VPaasSDK"];
    [setting setAppVersion:[AIRBEnvironments shareInstance].sdkVersion];
    [setting setOSVersion:[AIRBEnvironments shareInstance].osVersion];
    [setting setDisableSslVerify:YES];
    [setting setDeviceId:self.engineConfig.deviceID];
    [setting setDeviceName:[AIRBEnvironments shareInstance].deviceName];
    [setting setDeviceType:[AIRBEnvironments shareInstance].deviceType];
    [setting setAppID:self.engineConfig.appID];
    [setting setAuthTokenCallback:self];
    
    [self.mpsEngine registerModule:[VPROOMRoomModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPLIVELiveModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPCHATChatModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPRTCRtcModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPWBWbModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPDOCDocModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPSCENELIVESceneliveModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPSCENECLASSSceneclassModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPCLOUDCONFIGCloudconfigModule getModuleInfo]];
    [self.mpsEngine registerModule:[VPMETA_AIMetaAiModule getModuleInfo]];    
    
    [self.mpsEngine startWithBlock:^(void) {
        LOGI("AIRBRoomEngine::mpsEngine started.");
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
            [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineStarted info:nil];
        }
    } onFailure:^(DPSError * _Nonnull error){
        LOGE("AIRBRoomEngine::mpsEngine failed to start with error(%@).", ERR_MSG_FROM_DPSERROR(error));
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineErrorWithCode:errorMessage:)]) {
            [self.delegate onAIRBRoomEngineErrorWithCode:AIRBEngineFailedToStart errorMessage:ERR_MSG_FROM_DPSERROR(error)];
        }
    }];
}

#pragma mark -功能
#pragma markd -Action
- (void)loginWithUserID:(nonnull NSString*)userID {
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomLogin info:nil];
    
    if ([self isLogined:userID]) {
        LOGD("AIRBRoomEngine::loginWithUserID(%@), isLogined(%d)", userID, YES);
        self.userID = userID;
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
            [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineLogined info:nil];
        }
                
        return;
    }
    
    LOGD("AIRBRoomEngine::loginWithUserID(%@), isLogined(%d)", userID, NO);
    self.userID = userID;
    DPSUserId *dpsUserId = [[DPSUserId alloc] init];
    dpsUserId.uid = userID;
    dpsUserId.domain = self.engineConfig.appID;
        
    [self.mpsEngine createMPSManagerWithBlock:dpsUserId.uid onSuccess:^(VPMPSManager * _Nullable manager) {
        DPSAuthService *authService = [manager getAuthService];
        LOGD("AIRBRoomEngine::createMPSManagerWithBlock:manager(%p) authService(%p)", manager, authService);
        NSString* uid = [manager getUserId];
        if (authService){
            // 注册监听
            
            [[[VPCLOUDCONFIGCloudconfigModule getModule:uid] getExtInterface] setCloudconfigNotifyCb:[AIRBMonitorHubManager sharedInstance]];
            
            [authService addListener:self];
            // 发起登录
            [authService login];
        }
        return;
    } onFailure:^(DPSError * _Nonnull error) {
        LOGE("AIRBRoomEngine::failed to createDPSManager with error(%@).", ERR_MSG_FROM_DPSERROR(error));
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineErrorWithCode:errorMessage:)]) {
            [self.delegate onAIRBRoomEngineErrorWithCode:AIRBEngineFailedToCreateDPSManager errorMessage:error.reason];
        }
     }];
}

- (void)logoutOnSuccess:(void (^)(void))onSuccess
              onFailure:(void (^)(NSString* errorMessage))onFailure {
    LOGD("AIRBRoomEngine::logoutOnSuccess:onFailure");
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtPaassdkRoomLogout info:nil];
    
    [[AIRBMonitorHubManager sharedInstance] stopMonitoring];
    
    if (self.mpsEngine.isStarted) {
        VPMPSManager* manager = [self.mpsEngine getMPSManager:self.userID];
        if (manager) {
            DPSAuthService *authService = [manager getAuthService];
            [authService logoutWithBlock:^{
                LOGD("AIRBRoomEngine::logoutSucceeded");
                onSuccess();
            } onFailure:^(DPSError * _Nonnull error) {
                LOGD("AIRBRoomEngine::logoutFailed");
                onFailure(ERR_MSG_FROM_DPSERROR(error));
            }];
            [authService removeListener:self];
        } else {
            LOGE("AIRBRoomEngine::please login before logout.");
            onSuccess();
        }
    } else {
        LOGE("AIRBRoomEngine::please start mpsEngine before logout.");
        onSuccess();
    }
}

- (BOOL) isInited {
    return self.mpsEngine.isStarted;
}

- (BOOL) isLogined:(NSString *)userID {
    VPMPSManager* manager = [self.mpsEngine getMPSManager:userID];
    if (manager) {
        DPSAuthService *authService = [manager getAuthService];
        if (authService && [authService getConnectionStatus] == DPSConnectionStatusCsAuthed) {
            return YES;
        }
    }
    return NO;
}

- (nullable id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(nonnull NSString*)roomID {
    return [self getRoomChannelWithRoomID:roomID bizType:nil bizID:nil];
}

- (id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(NSString *)roomID
                                                 bizType:(NSString *)bizType
                                                   bizID:(NSString*)bizID {
    AIRBRoomChannel* roomChannel = nil;
    if (roomID.length > 0) {
        roomChannel = [[AIRBRoomChannel alloc] initWithRoomID:roomID userID:self.userID];
        
        [AIRBMonitorHubManager sharedInstance].configModel.appID = self.engineConfig.appID;
        [AIRBMonitorHubManager sharedInstance].configModel.userID = self.userID;
        [AIRBMonitorHubManager sharedInstance].configModel.bizType = bizType;
        [AIRBMonitorHubManager sharedInstance].configModel.bizID = bizID;
        [AIRBMonitorHubManager sharedInstance].configModel.deviceID = self.engineConfig.deviceID;
        [AIRBMonitorHubManager sharedInstance].configModel.roomID = roomID;
        
        [[AIRBMonitorHubManager sharedInstance] startMonitoring];
        
        [self getCloudConfigWithBizType:bizType];
    }
    LOGI("AIRBRoomEngine::create AIRBRoomChannel (%@)", roomChannel);
    return roomChannel;
}

- (void) getRoomListWithPageNum:(int32_t)pageNum
                       pageSize:(int32_t)pageSize
                      onSuccess:(void (^)(AIRBRoomEngineRoomListResponse * _Nonnull response))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure{
    [[[VPROOMRoomModule getModule:self.userID] getRpcInterface] getRoomListWithBlock:[VPROOMGetRoomListReq VPROOMGetRoomListReqWithDomain:self.engineConfig.appID pageNum:pageNum pageSize:pageSize] onSuccess:^(VPROOMGetRoomListRsp * _Nonnull rsp) {
        onSuccess([self createRoomListResponseWithInnerResponse:rsp]);
    } onFailure:^(DPSError * _Nonnull error) {
        LOGE("AIRBRoomEngine::failed to getRoomListWithBlock with error(%@).", ERR_MSG_FROM_DPSERROR(error));
        onFailure(ERR_MSG_FROM_DPSERROR(error));
    }];
}

- (void) getRoomDetailWithID:(NSString*) roomID
                   onSuccess:(void (^)(AIRBRoomBasicInfo * _Nonnull info))onSuccess
                   onFailure:(void (^)(NSString* errorMessage))onFailure {
    VPROOMRoomRpcInterface* manager = [[VPROOMRoomModule getModule:self.userID] getRpcInterface];
    if (manager) {
        VPROOMGetRoomDetailReq* req = [VPROOMGetRoomDetailReq VPROOMGetRoomDetailReqWithRoomId:roomID];
        [manager getRoomDetailWithBlock:req onSuccess:^(VPROOMGetRoomDetailRsp * _Nonnull rsp) {
            AIRBRoomBasicInfo* roomInfo = [[AIRBRoomBasicInfo alloc] init];
            roomInfo.roomID = rsp.roomId;
            roomInfo.title = rsp.title;
            roomInfo.notice = rsp.notice;
            roomInfo.ownerID = rsp.ownerId;
            roomInfo.uv = rsp.uv;
            roomInfo.onlineCount = rsp.onlineCount;
            roomInfo.pluginInstanceInfo = [[AIRBPluginInstanceInfo alloc] init];
            //TODO check
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
            onSuccess(roomInfo);
        } onFailure:^(DPSError * _Nonnull error) {
            LOGE("AIRBRoomEngine::failed to getRoomDetailWithBlock with error(%@).", ERR_MSG_FROM_DPSERROR(error));
            onFailure(ERR_MSG_FROM_DPSERROR(error));
        }];
    } else {
        onFailure(@"Failed to get detail when failed to get manager.");
    }
}

- (AIRBRoomEngineRoomListResponse*)createRoomListResponseWithInnerResponse:(VPROOMGetRoomListRsp*)rsp {
    AIRBRoomEngineRoomListResponse* response = [[AIRBRoomEngineRoomListResponse alloc] init];
    response.total = rsp.total;
    response.hasMore = rsp.hasMore;
    NSMutableArray* roomList = [[NSMutableArray alloc] init];
    for (VPROOMRoomBasicInfo* item in rsp.roomInfoList) {
        AIRBRoomBasicInfo* room = [[AIRBRoomBasicInfo alloc] init];
        room.ownerID = item.ownerId;
        room.title = item.title;
        room.roomID = item.roomId;
        
        [roomList addObject:room];
    }
    if (roomList.count > 0) {
        response.roomBasicInfoList = [NSArray arrayWithArray:roomList];
    }
    return response;
}

- (id<AIRBRoomSceneLiveProtocol>) getRoomSceneLive {
    if (!_sceneLive || (_sceneLive && [self isLogined:self.userID] && ![self.userID isEqualToString:((AIRBRoomSceneLive*)_sceneLive).userID])) {
        _sceneLive = [[AIRBRoomSceneLive alloc] initWithUserID:self.userID];
    }
    return _sceneLive;
}

- (id<AIRBRoomSceneClassProtocol>) getRoomSceneClass {
    if (!_sceneClass || (_sceneClass && [self isLogined:self.userID] && ![self.userID isEqualToString:((AIRBRoomSceneClass*)_sceneClass).userID])) {
        _sceneClass = [[AIRBRoomSceneClass alloc] initWithUserID:self.userID];
    }
    return _sceneClass;
}

- (id<AIRBProduct360Protocol>) getProduct360Channel {
    if (![self isLogined:self.userID]) {
        return nil;
    }
    if (_product360Client) {
        if ([self.userID isEqualToString:((AIRBProduct360 *)_product360Client).userID]) {
            return _product360Client;
        }
    }
    _product360Client = [[AIRBProduct360 alloc] initWithUserID:self.userID];
    return _product360Client;
}

-(void) setupNetworkReachability {
    _reachability = [AIRBReachability reachabilityForInternetConnection];
    [AIRBEnvironments shareInstance].netowrkType = _reachability.currentReachabilityString;
    
    _reachability.reachableBlock = ^(AIRBReachability *reachability) {
        LOGD("AIRBRoomEngine::network(%d) available now, notify gaea.", reachability.currentReachabilityStatus);
        [AIRBRoomEngineReloginUtility notifyNetworkAvailable:YES];
        
        [AIRBEnvironments shareInstance].netowrkType = reachability.currentReachabilityString;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            if (reachability.currentReachabilityStatus == ReachableViaWiFi) {
                [[NSNotificationCenter defaultCenter] postNotificationName:kAIRBReachabilityBecameWiFiNotification object:nil];
            } else if (reachability.currentReachabilityStatus == ReachableViaWWAN) {
                [[NSNotificationCenter defaultCenter] postNotificationName:kAIRBReachabilityBecameWWANNotification object:nil];
            }
        });
    };
    _reachability.unreachableBlock = ^(AIRBReachability *reachability) {
        LOGD("AIRBRoomEngine::network not available now, notify gaea.");
        [AIRBRoomEngineReloginUtility notifyNetworkAvailable:NO];
        
        [AIRBEnvironments shareInstance].netowrkType = reachability.currentReachabilityString;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [[NSNotificationCenter defaultCenter] postNotificationName:kAIRBReachabilityBecameUnrechableNotification object:nil];
        });
    };
    [_reachability startNotifier];
}

- (void)getCloudConfigWithBizType:(NSString*)bizType {
    
    VPCLOUDCONFIGGetVisibleConfigReq* req = [VPCLOUDCONFIGGetVisibleConfigReq VPCLOUDCONFIGGetVisibleConfigReqWithBaseInfo:({
        VPCLOUDCONFIGCloudConfigBaseInfoV2* info = [VPCLOUDCONFIGCloudConfigBaseInfoV2 VPCLOUDCONFIGCloudConfigBaseInfoV2WithPcSystemInfo:nil mobileSystemInfo:({
            
            VPCLOUDCONFIGMobileSystemInfoV2* mobileSystemInfo =
            [VPCLOUDCONFIGMobileSystemInfoV2 VPCLOUDCONFIGMobileSystemInfoV2WithDeviceType:@"ios"
                                                                             systemVersion:[AIRBEnvironments shareInstance].osVersion
                                                                                deviceName:[AIRBEnvironments shareInstance].deviceType];
            mobileSystemInfo;
        }) webSystemInfo:nil];
        info;
    }) appVersion:[AIRBEnvironments shareInstance].sdkVersion keyList:@[@"encodeParamiOS",@"slaveCameraResolutionStrategyMobile",@"classDefaultOpenCamera",@"visibleConfig"]];
    
    
    [[[VPCLOUDCONFIGCloudconfigModule getModule:self.userID] getRpcInterface] getVisibleConfigWithBlock:req onSuccess:^(VPCLOUDCONFIGGetVisibleConfigRsp * _Nonnull rsp) {
        [AIRBEnvironments shareInstance].cloudConfig = rsp.keyConfigMap;
    } onFailure:^(DPSError * _Nonnull error) {
        LOGD("AIRBRoomEngine::getVisibleConfig failed(%@)", ERR_MSG_FROM_DPSERROR(error));
    }];
}

#pragma mark -NSNotification
- (void)systemActiveStateChange:(NSNotification *) note {
    if (note.name == UIApplicationDidBecomeActiveNotification) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtDefaultNoevent info:nil];
        [self.mpsEngine onAppWillEnterForeground];
    } else if (note.name == UIApplicationWillResignActiveNotification) {
        [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtDefaultNoevent info:nil];
        [self.mpsEngine onAppDidEnterBackground];
    }
}

#pragma mark -SDK回调方法
#pragma mark -DPSLogHandler

/**
 * 初始化Engine Log回调
 * @param logLevel logContent
 */
- (void)onLog:(VPMPSLogLevel)logLevel logContent:(nonnull NSString *)logContent{
    switch (logLevel) {
        case VPMPSLogLevelMPSLOGLEVELINFO:
            LOGI([logContent UTF8String]);
            break;
        case VPMPSLogLevelMPSLOGLEVELDEBUG:
            LOGD([logContent UTF8String]);
            break;
        case VPMPSLogLevelMPSLOGLEVELWARNING:
            LOGW([logContent UTF8String]);
            break;
        case VPMPSLogLevelMPSLOGLEVELERROR:
        case VPMPSLogLevelMPSLOGLEVELFATAL:
            LOGE([logContent UTF8String]);
            break;
        default:
            break;
    }
}

#pragma mark -DPSAuthListener
/**
 * 连接状态事件
 * @param status     登录 网络状态
 */
- (void)onConnectionStatusChanged:(DPSConnectionStatus)status{
 switch (status) {
        case DPSConnectionStatusCsUnconnected: {
            LOGD("AIRBRoomEngine::DPSConnectionStatusCsUnconnected");
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventConnectionUnconnected info:nil];
            }
        }
            break;
        case DPSConnectionStatusCsConnecting:
            LOGD("AIRBRoomEngine::DPSConnectionStatusCsConnecting");
            break;
        case DPSConnectionStatusCsConnected: {
            LOGD("AIRBRoomEngine::DPSConnectionStatusCsConnected");
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventConnectionConnected info:nil];
            }
        }
            break;
        case DPSConnectionStatusCsAuthing:
            LOGD("AIRBRoomEngine::DPSConnectionStatusCsAuthing");
            break;
        case DPSConnectionStatusCsAuthed: {
            LOGD("AIRBRoomEngine::DPSConnectionStatusCsAuthed");
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomChannelEvent:info:)]) {
                [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventConnectionAuthed info:nil];
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [[NSNotificationCenter defaultCenter] postNotificationName:kAIRBRoomEngineLoginedNotification object:nil];
            });
        }
            break;
        default:
            break;
    }
    
    NSString* networkType = VPMonitorhubNetTypeNetWiFi;
    if ([[AIRBEnvironments shareInstance].netowrkType isEqualToString:@"5G"]) {
        networkType = VPMonitorhubNetTypeNet5G;
    } else if ([[AIRBEnvironments shareInstance].netowrkType isEqualToString:@"4G"]) {
        networkType = VPMonitorhubNetTypeNet4G;
    } else if ([[AIRBEnvironments shareInstance].netowrkType isEqualToString:@"3G"] || [[AIRBEnvironments shareInstance].netowrkType isEqualToString:@"2G"]) {
        networkType = VPMonitorhubNetTypeNet3G;
    } else if ([[AIRBEnvironments shareInstance].netowrkType isEqualToString:@"WiFi"]) {
        networkType = VPMonitorhubNetTypeNetWiFi;
    } else {
        networkType = VPMonitorhubNetTypeNetNoWire;
    }
    
    [[AIRBMonitorHubManager sharedInstance] reportEvent:VPMonitorhubEventMhevtMetapathClientLinkConnStateChange info:@{
                VPMonitorhubFieldMffieldMetapathClientLinkActConnState : [NSString stringWithFormat:@"%ld", status],
                VPMonitorhubFieldMffieldMetapathClientLinkActEngineType : [[NSString alloc] initWithFormat:@"%ld", (long)[VPMPSEngine getEngineType]],
                VPMonitorhubFieldMffieldMetapathClientLinkActNetType : networkType}];
}
/**
 * 登录token获取失败事件
 * @param errorCode  获取登录token失败错误值
 * @param errorMsg   获取登录token失败错误信息
 */
- (void)onGetAuthCodeFailed:(int32_t)errorCode errorMsg:(nonnull NSString *)errorMsg{
    LOGE("AIRBRoomEngine::onGetAuthCodeFailed(%ld, %@)", errorCode, errorMsg);
}

/**
 * 本地登录事件
 * 如果本地已有登录信息，调用Login接口后会立即回调；反之会等待网络登录成功之后回调
 */
- (void)onLocalLogin{
    LOGI("AIRBRoomEngine::onLocalLogin");
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
        [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineLogined info:nil];
    }
}

/**
 * 被踢事件
 * @param message     被踢下线时附带的消息
 */
- (void)onKickout:(nonnull NSString *)message{
    LOGI("AIRBRoomEngine::onKickout(%@)", message);
    
    [[AIRBMonitorHubManager sharedInstance] stopMonitoring];
    
    [self logoutOnSuccess:^{
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
            [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineKickedOut info:nil];
        }
    } onFailure:^(NSString * _Nonnull errorMessage) {
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineEvent:info:)]) {
            [self.delegate onAIRBRoomEngineEvent:AIRBRoomEngineEventEngineKickedOut info:nil];
        }
    }];
}

/**
 * 其他端设备在（离）线情况
 * @param type        事件类型（1：事件通知，包括上下线，2：状态通知，在线状态）
 * @param deviceType 设备类型
 * （0:default,1:web,2:Android,3:iOS,4:Mac,5:Windows,6:iPad）
 * @param status      设备状态（1：上线或在线，2：下线或离线）
 * @param time        时间（上线或下线时间）
 */
- (void)onDeviceStatus:(int32_t)type deviceType:(int32_t)deviceType status:(int32_t)status time:(int64_t)time{
}

/**
 * 下载资源cookie变更事件
 * @param cookie      新cookie
 */
- (void)onMainServerCookieRefresh:(nonnull NSString *)cookie{
}

#pragma mark -DPSPubAuthTokenCallback
/**
 * 登录后回调
 *  @param userId onGot  reason
*/
- (void)onCallback:(nonnull NSString *)userId
             onGot:(nullable DPSAuthTokenGotCallback *)onGot
            reason:(DPSAuthTokenExpiredReason)reason {
    LOGI("AIRBRoomEngine::login onCallback(%d).", reason);
    if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineRequestToken:)]) {
        [self.delegate onAIRBRoomEngineRequestToken:^(AIRBRoomEngineAuthToken * _Nonnull token) {
            if (token && token.accessToken.length > 0 && token.refreshToken.length > 0) {
                DPSAuthToken* dpsToken = [DPSAuthToken DPSAuthTokenWithAccessToken:token.accessToken refreshToken:token.refreshToken];
                dispatch_async(dispatch_get_main_queue(), ^{
                    [onGot onSuccess:dpsToken];
                });
                LOGI("AIRBRoomEngine::get token succeeded.");
                return;
            }
            [onGot onFailure:-1 errorMsg:@"invalid token."];
            if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineErrorWithCode:errorMessage:)]) {
                [self.delegate onAIRBRoomEngineErrorWithCode:AIRBEngineFailedToLogin errorMessage:[NSString stringWithFormat:@"invalid token(%@, %@).", token.accessToken, token.refreshToken]];
            }
        }];
    } else {
        [onGot onFailure:-1 errorMsg:@"nil responds selector."];
        if ([self.delegate respondsToSelector:@selector(onAIRBRoomEngineErrorWithCode:errorMessage:)]) {
            [self.delegate onAIRBRoomEngineErrorWithCode:AIRBEngineFailedToLogin errorMessage:@"failed to get token when nil selector."];
        }
    }
}

#pragma mark - LoggerManagerDelegate
- (void) onLogMessage:(NSString *)message {
    if ([self.delegate respondsToSelector:@selector(onLog:)]) {
        [self.delegate onLog:message];
    } else {
        NSLog(@"%@", message);
    }
}

@end
