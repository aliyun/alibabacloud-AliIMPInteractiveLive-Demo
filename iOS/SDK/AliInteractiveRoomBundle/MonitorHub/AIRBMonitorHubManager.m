//
//  AIRBMonitorHubManager.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBMonitorHubManager.h"

#import <UIKit/UIKit.h>
#import <sys/utsname.h>

#import <vpaassdk/monitorhub/VPMonitorHubModule.h>
#import <vpaassdk/monitorhub/VPMonitorhubHeartbeatCallback.h>
#import <vpaassdk/monitorhub/VPMonitorhubAppinfo.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGSlsReportMode.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGSlsConfig.h>
#import <vpaassdk/monitorhub/VPMonitorhubNetType.h>

#import "AIRBMonitorHubConfigModel.h"
#import "AIRBMonitorHubVideoPlayerModel.h"
#import "AIRBMonitorHubRTCModel.h"
#import "AIRBMonitorHubLivePusherModel.h"
#import "../Utilities/AliInteractiveRoomLogger.h"
#import "../Utilities/AIRBEnvironments.h"


@interface AIRBMonitorHubManager() <VPMonitorhubHeartbeatCallback>
@property (nonatomic, strong) dispatch_queue_t taskQueue;


@end



@implementation AIRBMonitorHubManager

+ (AIRBMonitorHubManager*) sharedInstance {
    static AIRBMonitorHubManager *sharedInstance = nil;
    static dispatch_once_t onceAIRBMonitorHubManagerSharedInstanceToken;
    dispatch_once(&onceAIRBMonitorHubManagerSharedInstanceToken, ^{
        sharedInstance = [[AIRBMonitorHubManager alloc] init];
    });
    return sharedInstance;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _configModel = [[AIRBMonitorHubConfigModel alloc] init];
        _taskQueue = dispatch_queue_create("com.aliyun.vc.interactiveroom.monitorhub", DISPATCH_QUEUE_SERIAL);
        _videoPlayerModel = [[AIRBMonitorHubVideoPlayerModel alloc] init];
        _rtcModel = [[AIRBMonitorHubRTCModel alloc] init];
        _livePusherModel = [[AIRBMonitorHubLivePusherModel alloc] init];
    }
    return self;
}

- (void) onGetSlsConfig:(nonnull VPCLOUDCONFIGGetSlsConfigRsp *)slsConfig {
    
    dispatch_async(self.taskQueue, ^{
        

        VPMonitorhubReportModel reportMode = [slsConfig reportMode] != VPCLOUDCONFIGSlsReportModeWebtracking ? VPMonitorhubReportModelSlsSdk : VPMonitorhubReportModelSlsWebtracking;
        VPMonitorhubStsTokenModel* stsToken = [VPMonitorhubStsTokenModel VPMonitorhubStsTokenModelWithAccessKeyId:[[slsConfig stsToken] accessKeyId]  accessKeySecret:[[slsConfig stsToken] accessKeySecret] securityToken:[[slsConfig stsToken] securityToken]  expireTimeS:[[slsConfig stsToken] expireTime]];
        VPMonitorhubSlsConfigModel* subSlsConfig = [VPMonitorhubSlsConfigModel VPMonitorhubSlsConfigModelWithEndpoint:[[slsConfig slsConfig] endpoint] project:[[slsConfig slsConfig] project] logStore:[[slsConfig slsConfig] logStore] stsToken:stsToken];


        VPMonitorhubReportConfig* monitorhubInitConfig = [VPMonitorhubReportConfig VPMonitorhubReportConfigWithReportMode:reportMode slsConfig:subSlsConfig heartbeatIntervalS:[slsConfig heartbeatInterval]];
        
        [[VPMonitorhubModule getMonitorhubModule] setConfig:monitorhubInitConfig];
    });
}

- (void) onUpdateSlsStsToken:(nonnull VPCLOUDCONFIGStsToken *)stsToken {
    
    dispatch_async(self.taskQueue, ^{
        VPMonitorhubStsTokenModel* token = [VPMonitorhubStsTokenModel VPMonitorhubStsTokenModelWithAccessKeyId:[stsToken accessKeyId] accessKeySecret:[stsToken accessKeySecret] securityToken:[stsToken securityToken] expireTimeS:[stsToken expireTime]];
        [[VPMonitorhubModule getMonitorhubModule] updateStsToken:token];
    });
    
}

- (void) startMonitoring {
    dispatch_async(self.taskQueue, ^{
        
        LOGI("AIRBMonitorHubManager::startMonitoring");
        
        [[VPMonitorhubModule getMonitorhubModule] initMonitorhubModule];
        [[VPMonitorhubModule getMonitorhubModule] setHeartbeatCallback:self];
        
        VPMonitorhubAppInfo* info = [[VPMonitorhubModule getMonitorhubModule] getAppInfo];
        
        [info setOsName:VPMonitorhubFieldMffieldCommonPLATFORMIOS];
        [info setOsVersion:[UIDevice currentDevice].systemVersion];
        
        [info setDeviceId:self.configModel.deviceID];
        [info setDeviceType:@"ios"];
        
        NSString* networkType = [AIRBEnvironments shareInstance].netowrkType;
        if ([networkType isEqualToString:@"5G"]) {
            [info setNetType:VPMonitorhubNetTypeNet5G];
        } else if ([networkType isEqualToString:@"4G"]) {
            [info setNetType:VPMonitorhubNetTypeNet4G];
        } else if ([networkType isEqualToString:@"3G"] || [networkType isEqualToString:@"2G"]) {
            [info setNetType:VPMonitorhubNetTypeNet3G];
        } else if ([networkType isEqualToString:@"WiFi"]) {
            [info setNetType:VPMonitorhubNetTypeNetWiFi];
        } else {
            [info setNetType:VPMonitorhubNetTypeNetNoWire];
        }

        [info setDeviceName:[AIRBEnvironments shareInstance].deviceName];
        
        NSDictionary *infoDictionary = [[NSBundle mainBundle] infoDictionary];
        [info setAppName:[infoDictionary objectForKey:@"CFBundleDisplayName"]];
        [info setAppVersion:[infoDictionary objectForKey:@"CFBundleShortVersionString"]];
        [info setAppId:self.configModel.appID];
        
        [info setPaasSdkVersion:[AIRBEnvironments shareInstance].sdkVersion];
        
        [info setRoomId:self.configModel.roomID];
        [info setUid:self.configModel.userID];
        [info setBizId:self.configModel.bizID];
        
        if (!self.configModel.bizType) {
            [info setBizType:@"basic_sdk"];
        } else {
            [info setBizType:self.configModel.bizType];
        }
    });
}

- (void) stopMonitoring {
    dispatch_async(self.taskQueue, ^{
        LOGI("AIRBMonitorHubManager::stopMonitoring");
        [[VPMonitorhubModule getMonitorhubModule] uninitMonitorhubModule];
    });
}

- (void) reportEvent:(VPMonitorhubEvent)event info:(NSDictionary *)info {
    dispatch_async(self.taskQueue, ^{
        LOGI("AIRBMonitorHubManager::reportEvent(%d)", event);
        [[VPMonitorhubModule getMonitorhubModule] reportNormalEvent:event extraFields:info errorCode:[[info objectForKey:@"error_code"] longLongValue] errorMsg:[info objectForKey:@"error_msg"]];
    });
}

#pragma mark -MonitorhubHeartbeatCallback
- (nonnull NSDictionary<NSString *,NSString *> *)onHeartbeatProcess {
    NSMutableDictionary* result = [[NSMutableDictionary alloc] init];
    [result setValue:@"not_start" forKey:@"status"];
    if (self.videoPlayerModel.status == AIRBMonitorHubComponentStatusRunning) {
        [result setValue:@"live_play" forKey:@"status"];
        [result setValue:self.videoPlayerModel.contentID ?: @"null" forKey:@"content_id"];
        [result setValue:self.videoPlayerModel.playType forKey:@"play_type"];
        [result setValue:self.videoPlayerModel.url ?: @"" forKey:@"live_url"];
        [result setValue:self.videoPlayerModel.protocol ?: @"" forKey:@"proto_type"];
        [result setValue:[NSString stringWithFormat:@"%d",self.videoPlayerModel.videoWidth] ?: @"" forKey:@"remote_video_width"];
        [result setValue:[NSString stringWithFormat:@"%d",self.videoPlayerModel.videoHeight] ?: @"" forKey:@"remote_video_height"];
        [result setValue:self.videoPlayerModel.rtsTraceID forKey:@"trace_id"];
        
        [result setValue:[NSString stringWithFormat:@"%lld",self.videoPlayerModel.renderedVideoFrameCount] ?: @"" forKey:@"remote_video_render_frames"];
    } else if (self.livePusherModel.status == AIRBMonitorHubComponentStatusRunning) {
        [result setValue:@"live_push" forKey:@"status"];
        [result addEntriesFromDictionary:self.livePusherModel.mediaStreamStatistics];
    } else if (self.rtcModel.status == AIRBMonitorHubComponentStatusRunning) {
        if (self.rtcModel.isHost) {
            [result setValue:@"rtc_host" forKey:@"status"];
        } else {
            [result setValue:@"rtc_participant" forKey:@"status"];
        }
        
        [result setValue:self.rtcModel.contentID ?: @"null" forKey:@"content_id"];
        [result setValue:[NSString stringWithFormat:@"%u", self.rtcModel.cameraImageWidth] forKey:@"camera_width"];
        [result setValue:[NSString stringWithFormat:@"%u", self.rtcModel.cameraImageHeight] forKey:@"camera_height"];
        
        [result setValue:[self.rtcModel.cameraVideoStreamStatistics objectForKey:@"sent_bitrate"] ?: @"" forKey:@"camera_sent_bitrate"];
        [result setValue:[self.rtcModel.cameraVideoStreamStatistics objectForKey:@"sent_fps"] ?: @"" forKey:@"camera_sent_fps"];
        [result setValue:[self.rtcModel.cameraVideoStreamStatistics objectForKey:@"encode_fps"] ?: @"" forKey:@"camera_encode_fps"];
        
        [result setValue:[self.rtcModel.screenVideoStreamStatistics objectForKey:@"sent_bitrate"] ?: @"" forKey:@"screen_sent_bitrate"];
        [result setValue:[self.rtcModel.screenVideoStreamStatistics objectForKey:@"sent_fps"] ?: @"" forKey:@"screen_sent_fps"];
        [result setValue:[self.rtcModel.screenVideoStreamStatistics objectForKey:@"encode_fps"] ?: @"" forKey:@"screen_encode_fps"];
        
        [result setValue:[self.rtcModel.audioStreamStatistics objectForKey:@"sent_bitrate"] ?: @"" forKey:@"audio_sent_bitrate"];
    }
//    LOGI("AIRBMonitorHubManager::onHeartbeatProcess status(%@)", result);
    return result;
}

@end
