//
//  AIRBMonitorHubRTCModel.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBMonitorHubRTCModel.h"

@interface AIRBMonitorHubRTCModel()
@property (strong, nonatomic) NSLock* lock;
@end

@implementation AIRBMonitorHubRTCModel

@synthesize cameraVideoStreamStatistics = _cameraVideoStreamStatistics;
@synthesize screenVideoStreamStatistics = _screenVideoStreamStatistics;
@synthesize audioStreamStatistics = _audioStreamStatistics;

- (instancetype) init {
    self = [super init];
    if (self) {
        _lock = [[NSLock alloc] init];
    }
    return self;
}

- (NSDictionary*)cameraVideoStreamStatistics {
    [self.lock lock];
    NSDictionary* statistics = [_cameraVideoStreamStatistics mutableCopy];
    [self.lock unlock];
    return statistics;
}

- (void) setCameraVideoStreamStatistics:(NSDictionary *)cameraVideoStreamStatistics {
    [self.lock lock];
    _cameraVideoStreamStatistics = cameraVideoStreamStatistics;
    [self.lock unlock];
}

-(NSDictionary*)screenVideoStreamStatistics {
    [self.lock lock];
    NSDictionary* statistics = [_screenVideoStreamStatistics mutableCopy];
    [self.lock unlock];
    return statistics;
}

-(void) setScreenVideoStreamStatistics:(NSDictionary *)screenVideoStreamStatistics {
    [self.lock lock];
    _screenVideoStreamStatistics = screenVideoStreamStatistics;
    [self.lock unlock];
}

- (NSDictionary*)audioStreamStatistics {
    [self.lock lock];
    NSDictionary* statistics = [_audioStreamStatistics mutableCopy];
    [self.lock unlock];
    return statistics;
}

- (void) setAudioStreamStatistics:(NSDictionary *)audioStreamStatistics {
    [self.lock lock];
    _audioStreamStatistics = audioStreamStatistics;
    [self.lock unlock];
}

@end
