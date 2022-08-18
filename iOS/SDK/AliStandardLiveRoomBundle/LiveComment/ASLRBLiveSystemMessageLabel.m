//
//  ASLRBLiveSystemLabel.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/15.
//

#import "ASLRBLiveSystemMessageLabel.h"

#import <Masonry/Masonry.h>

#import "ASLRBLiveSystemMessageModel.h"

@interface ASLRBLiveSystemMessageLabel()
@property (nonatomic,strong) NSMutableArray* unpresentedMessages;;
//@property (nonatomic, strong) NSTimer* timerForShowingMessage;
@property (nonatomic, strong) NSTimer* timer;
@property (atomic, assign) BOOL isPresenting;
@end

@implementation ASLRBLiveSystemMessageLabel

- (instancetype) init {
    self = [super init];
    if (self) {
        _unpresentedMessages = [[NSMutableArray alloc] init];
        _timer = [NSTimer scheduledTimerWithTimeInterval:2.0 target:self selector:@selector(presentMessageRepeatly) userInfo:nil repeats:YES];
    }
    return self;
}

- (void) dealloc {
    
}

- (void) stopPresenting {
    if (_timer) {
        [_timer invalidate];
        _timer = nil;
    }
}

- (void) presentMessageRepeatly {
    if (self.isPresenting) {
        self.isPresenting = NO;
        self.alpha = 0.0;
        if (self.superview) {
            [(UILabel*)self mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.left.equalTo(self.superview.mas_left).with.offset(-150);
            }];
        }
    }
    
    if (self.unpresentedMessages.count > 0 && self.canPresenting) {
        self.isPresenting = YES;
        ASLRBLiveSystemMessageModel* model = [self.unpresentedMessages objectAtIndex:0];
        [self.unpresentedMessages removeObjectAtIndex:0];
        self.alpha = 1.0;
        self.attributedText = [[NSAttributedString alloc] initWithString:model.rawMessage attributes:@{
            NSFontAttributeName : self.font
        }];
        CGSize sizeNew = [self.attributedText size];
        
        __weak typeof(self) weakSelf = self;
        [UIView animateWithDuration:0.7 animations:^{
            if (weakSelf.superview) {
                [(UILabel*)weakSelf mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.width.mas_equalTo(sizeNew.width + 18);
                    make.left.equalTo(weakSelf.superview.mas_left);
                }];
                
                [weakSelf.superview layoutIfNeeded];
            }
        }];
    }
}

- (void) insertLiveSystemMessage:(ASLRBLiveSystemMessageModel *)model {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.unpresentedMessages.count < 10) {
            [self.unpresentedMessages addObject:model];
        }
    });
}

@end
