//
//  ASLRBLiveInitConfig.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/8/17.
//

#import "ASLRBLiveInitConfig.h"
#import "ASLRBLiveBusinessInfo.h"
#import "ASLRBLiveRoomMiddleViewsConfig.h"
#import "ASLRBLiveCommentViewConfig.h"
#import "ASLRBLiveRoomBackgroundImageConfig.h"

@implementation ASLRBLiveInitConfig

- (ASLRBLiveBusinessInfo*) liveBusinessInfo {
    if (!_liveBusinessInfo) {
        _liveBusinessInfo = [[ASLRBLiveBusinessInfo alloc] init];
    }
    return _liveBusinessInfo;
}

- (ASLRBLiveRoomMiddleViewsConfig*) middleViewsConfig {
    if (!_middleViewsConfig) {
        _middleViewsConfig = [[ASLRBLiveRoomMiddleViewsConfig alloc] init];
    }
    return _middleViewsConfig;
}

- (ASLRBLiveCommentViewConfig*)liveCommentViewsConfig {
    if (!_liveCommentViewsConfig) {
        _liveCommentViewsConfig = [[ASLRBLiveCommentViewConfig alloc] init];
    }
    return _liveCommentViewsConfig;
}

- (ASLRBLiveRoomBackgroundImageConfig*)liveRoomBackgroundImageConfig {
    if (!_liveRoomBackgroundImageConfig) {
        _liveRoomBackgroundImageConfig = [[ASLRBLiveRoomBackgroundImageConfig alloc] init];
    }
    return _liveRoomBackgroundImageConfig;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        _maxCommentLength = 50;
        _lowDelayLivePlaying = YES;
    }
    return self;
}

@end
