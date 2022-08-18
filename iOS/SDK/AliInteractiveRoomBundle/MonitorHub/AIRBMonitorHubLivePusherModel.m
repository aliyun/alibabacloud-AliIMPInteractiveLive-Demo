//
//  AIRBMonitorHubLivePusherModel.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBMonitorHubLivePusherModel.h"

@interface AIRBMonitorHubLivePusherModel()
@property (strong, nonatomic) NSLock* lock;
@end

@implementation AIRBMonitorHubLivePusherModel

@synthesize mediaStreamStatistics = _mediaStreamStatistics;

- (NSDictionary*)mediaStreamStatistics {
    [self.lock lock];
    NSDictionary* statistics = [_mediaStreamStatistics mutableCopy];
    [self.lock unlock];
    return statistics;
}

- (void) setMediaStreamStatistics:(NSDictionary *)mediaStreamStatistics {
    [self.lock lock];
    _mediaStreamStatistics = mediaStreamStatistics;
    [self.lock unlock];
}
@end
