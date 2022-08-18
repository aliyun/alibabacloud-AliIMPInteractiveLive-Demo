//
//  ASLRBLivePlayLoadingIndicatorView.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import "ASLRBLivePlayLoadingIndicatorView.h"

@implementation ASLRBLivePlayLoadingIndicatorView

- (instancetype) init {
    self = [super init];
    if (self) {
        
    }
    return self;
}

- (void) show:(BOOL)show {
    dispatch_async(dispatch_get_main_queue(), ^{
        self.hidden = !show;
        if (show) {
            if (!self.isAnimating) {
                [self startAnimating];
            }
        } else {
            if (self.isAnimating) {
                [self stopAnimating];
            }
        }
    });
}

@end
