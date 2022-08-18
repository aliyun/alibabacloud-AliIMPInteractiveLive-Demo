//
//  AIRBMonitorHubRTCModel.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBMonitorHubDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBMonitorHubRTCModel : NSObject
@property (assign, atomic) AIRBMonitorHubComponentStatus status;
@property (copy, nonatomic) NSString* contentID;
@property (assign, atomic) BOOL isHost;
@property (assign, nonatomic) int32_t cameraImageWidth;
@property (assign, nonatomic) int32_t cameraImageHeight;

@property (copy, nonatomic) NSDictionary* cameraVideoStreamStatistics;
@property (copy, nonatomic) NSDictionary* screenVideoStreamStatistics;
@property (copy, nonatomic) NSDictionary* audioStreamStatistics;


@end

NS_ASSUME_NONNULL_END
