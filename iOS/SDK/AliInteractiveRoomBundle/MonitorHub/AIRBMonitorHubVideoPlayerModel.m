//
//  AIRBMonitorHubLivePlayerModel.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/12/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBMonitorHubVideoPlayerModel.h"

@interface AIRBMonitorHubVideoPlayerModel()
@property (nonatomic, strong) NSLock* videoFrameCountLock;
@end

@implementation AIRBMonitorHubVideoPlayerModel

@synthesize renderedVideoFrameCount = _renderedVideoFrameCount;

- (instancetype) init {
    self = [super init];
    if (self) {
        _videoFrameCountLock = [[NSLock alloc] init];
        _playType = @"live";
    }
    return self;
}

- (void) setRenderedVideoFrameCount:(int64_t)renderedVideoFrameCount {
    [self.videoFrameCountLock lock];
    _renderedVideoFrameCount = renderedVideoFrameCount;
    [self.videoFrameCountLock unlock];
}

- (int64_t)renderedVideoFrameCount {
    [self.videoFrameCountLock lock];
    int64_t count = _renderedVideoFrameCount;
    _renderedVideoFrameCount = 0;
    [self.videoFrameCountLock unlock];
    return count;
}

@end
