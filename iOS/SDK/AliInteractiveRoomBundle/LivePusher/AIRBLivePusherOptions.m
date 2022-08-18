//
//  AIRBLivePusherOptions.m
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/10.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBLivePusherOptions.h"
@implementation AIRBLivePusherLiveBusinessOptions

@end

@implementation AIRBLivePusherMediaStreamingOptions

@end


@implementation AIRBLivePusherOptions

- (instancetype) init {
    self = [super init];
    if (self) {
        _mediaStreamingOptions = [[AIRBLivePusherMediaStreamingOptions alloc] init];
        _businessOptions = [[AIRBLivePusherLiveBusinessOptions alloc] init];
    }
    return self;
}

+ (instancetype) defaultOptions {
    AIRBLivePusherOptions* options = [[AIRBLivePusherOptions alloc] init];
    
    options.businessOptions.liveTitle = @"DefaultLiveTitle";
    options.businessOptions.liveIntroduction = @"DefaultLiveIntroduction";
    options.businessOptions.liveStartTime = [[NSDate date] timeIntervalSince1970] * 1000;
    options.businessOptions.liveEndTime = options.businessOptions.liveStartTime + 3600 * 1000;
    
    options.mediaStreamingOptions.orientation = 0;
//    options.mediaStreamingOptions.cameraType = AIRBLivePusherCameraTypeFront;
//    options.mediaStreamingOptions.pushMirror = NO;
//    options.mediaStreamingOptions.previewMirror = NO;
//    options.mediaStreamingOptions.audioOnly = NO;
//    options.mediaStreamingOptions.videoOnly = NO;
//    options.mediaStreamingOptions.autoFocus = YES;
//    options.mediaStreamingOptions.flash = NO;
    options.mediaStreamingOptions.previewDisplayMode = 2;
    
    return options;
}

@end
