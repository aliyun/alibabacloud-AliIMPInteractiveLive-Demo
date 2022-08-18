//
//  AIRBMonitorHubManager.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <vpaassdk/monitorhub/VPMonitorhubEvent.h>
#import <vpaassdk/monitorhub/VPMonitorhubField.h>
#import <vpaassdk/cloudconfig/VPCLOUDCONFIGCloudconfigNotifyCb.h>

#import "AIRBMonitorHubConfigModel.h"
#import "AIRBMonitorHubVideoPlayerModel.h"
#import "AIRBMonitorHubRTCModel.h"
#import "AIRBMonitorHubLivePusherModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBMonitorHubManager : NSObject<VPCLOUDCONFIGCloudconfigNotifyCb>
@property (strong, nonatomic) AIRBMonitorHubConfigModel* configModel;
@property (strong, nonatomic) AIRBMonitorHubVideoPlayerModel* videoPlayerModel;
@property (strong, nonatomic) AIRBMonitorHubLivePusherModel* livePusherModel;
@property (strong, nonatomic) AIRBMonitorHubRTCModel* rtcModel;

/**
 * 获取RoomEngine全局单例对象
 */
+ (AIRBMonitorHubManager*)sharedInstance;

- (void) startMonitoring; //start调用之前请保证configModel中的字段已经完全填充完毕；
- (void) stopMonitoring;

- (void) reportEvent:(VPMonitorhubEvent)event info:(NSDictionary*)info;


@end

NS_ASSUME_NONNULL_END
