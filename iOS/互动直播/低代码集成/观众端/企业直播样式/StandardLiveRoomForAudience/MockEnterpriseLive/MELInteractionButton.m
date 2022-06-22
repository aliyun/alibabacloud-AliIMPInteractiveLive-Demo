//
//  MELInteractionButton.m
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import "MELInteractionButton.h"
#import <Masonry/Masonry.h>
#import "UIColor+ColorWithHexString.h"

@implementation MELInteractionButton

- (UIButton*)button {
    if (!_button) {
        _button = [[UIButton alloc] init];
        [self addSubview:_button];
        [_button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.mas_left).with.offset(6);
            make.top.equalTo(self.mas_top).with.offset(6);
            make.size.mas_equalTo(CGSizeMake(36, 36));
        }];
    }
    return _button;
}

- (UILabel*)titleLabel {
    if (!_titleLabel) {
        _titleLabel = [[UILabel alloc] init];
        _titleLabel.textAlignment = NSTextAlignmentCenter;
        _titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        _titleLabel.textColor = [UIColor colorWithHexString:@"#646566" alpha:1.0];
        [self addSubview:_titleLabel];
        [_titleLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.bottom.equalTo(self.mas_bottom).with.offset(-6);
            make.centerX.equalTo(self.mas_centerX);
            make.left.equalTo(self);
            make.right.equalTo(self);
            make.height.mas_equalTo(16);
        }];
    }
    return _titleLabel;
}

@end
