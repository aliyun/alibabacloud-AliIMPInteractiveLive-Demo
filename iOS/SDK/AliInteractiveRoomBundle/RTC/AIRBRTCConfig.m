//
//  AIRBRTCConfig.m
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/8/19.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBRTCConfig.h"


@implementation AIRBRTCConfig

- (instancetype) init {
    self = [super init];
    if (self) {
        _videoStreamTypeHighDimensions = CGSizeMake(640, 480);
        _videoStreamTypeHighFrameRate = 15;
        _videoStreamTypeHighBitrate = 0;
        _videoStreamTypeLowPublished = YES;
//        _bypassLiveResolutionType = AIRBRTCBypassLiveResolutionType_1280x720;
    }
    return self;
}

@end
