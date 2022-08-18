//
//  AIRBMonitorHubLivePusherModel.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBMonitorHubDefines.h"

NS_ASSUME_NONNULL_BEGIN

@interface AIRBMonitorHubLivePusherModel : NSObject
@property (assign, atomic) AIRBMonitorHubComponentStatus status;
@property (copy, nonatomic) NSDictionary* mediaStreamStatistics;
@end

NS_ASSUME_NONNULL_END
