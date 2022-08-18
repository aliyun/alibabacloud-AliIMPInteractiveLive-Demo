//
//  ASLRBLivePushLoadingIndicatorView.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/28.
//

#import "ASLRBLivePushLoadingIndicatorView.h"

@implementation ASLRBLivePushLoadingIndicatorView

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

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
