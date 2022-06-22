//
//  MELHeaderViewHolder.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/31.
//

#import "MELHeaderViewHolder.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"

@interface MELHeaderViewHolder()

@end

@implementation MELHeaderViewHolder

-(UILabel*)liveTitleLabel {
    if (!_liveTitleLabel) {
        _liveTitleLabel = [[UILabel alloc] init];
        _liveTitleLabel.textAlignment = NSTextAlignmentCenter;
        [self addSubview:_liveTitleLabel];
        _liveTitleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:15];
        [_liveTitleLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(89.5);
            make.right.equalTo(self.mas_right).with.offset(-89.5);
            make.bottom.equalTo(self.mas_bottom).with.offset(-10);
            make.top.equalTo(self.mas_top).with.offset(54);
        }];
    }
    return _liveTitleLabel;
}

- (UIButton*)exitButton {
    if (!_exitButton) {
        _exitButton = [[UIButton alloc] init];
        [_exitButton addTarget:self action:@selector(onExitButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_exitButton setImage:[UIImage imageNamed:@"返回" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:_exitButton];
        [_exitButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(24, 24));
            make.left.equalTo(self.mas_left).with.offset(16);
            make.bottom.equalTo(self.mas_bottom).with.offset(-10);
        }];
    }
    return _exitButton;
}

- (instancetype) init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.liveTitleLabel];
        [self bringSubviewToFront:self.exitButton];
    }
    return self;
}


- (void) onExitButtonClicked {
    self.onExit();
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

@end
