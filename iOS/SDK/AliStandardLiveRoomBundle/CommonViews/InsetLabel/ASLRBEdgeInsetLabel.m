//
//  ASLRBEdgeInsetLabel.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/9/27.
//

#import "ASLRBEdgeInsetLabel.h"

@implementation ASLRBEdgeInsetLabel

- (instancetype) init {
    self = [super init];
    if (self) {
        _textInsets = UIEdgeInsetsZero;
    }
    return self;
}

- (instancetype) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        _textInsets = UIEdgeInsetsZero;
    }
    return self;
}

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
 */
- (void)drawRect:(CGRect)rect {
    [super drawTextInRect:UIEdgeInsetsInsetRect(rect, _textInsets)];
}

@end
