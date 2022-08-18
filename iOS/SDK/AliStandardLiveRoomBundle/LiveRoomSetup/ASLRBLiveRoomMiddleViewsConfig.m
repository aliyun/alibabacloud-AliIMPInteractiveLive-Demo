//
//  ASLRBLiveRoomMiddleViewsConfig.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import "ASLRBLiveRoomMiddleViewsConfig.h"

@implementation ASLRBLiveRoomMiddleViewsConfig

- (instancetype) init {
    self = [super init];
    if (self) {
        _liveNoticeButtonHidden = YES;
        _liveMembersButtonHidden = YES;
        _livePushStatusLabelHidden = NO;
        _livePlayLoadingIndicatorHidden = NO;
        _livePushRestartAlertHidden = NO;
        _livePushLoadingIndicatorHidden = NO;
    }
    return self;
}

@end
