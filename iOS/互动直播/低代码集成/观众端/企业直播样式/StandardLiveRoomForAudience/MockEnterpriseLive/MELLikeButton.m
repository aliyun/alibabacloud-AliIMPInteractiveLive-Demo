//
//  MELLikeButton.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/10.
//

#import "MELLikeButton.h"
#import <Masonry/Masonry.h>

@implementation MELLikeButton

- (UILabel*)likeCountLabel {
    if (!_likeCountLabel) {
        _likeCountLabel = [[UILabel alloc] init];
        _likeCountLabel.hidden = YES;
        _likeCountLabel.text = @"0";
        _likeCountLabel.backgroundColor = [UIColor colorWithRed:217/255.0 green:48/255.0 blue:38/255.0 alpha:1.0];
        _likeCountLabel.layer.cornerRadius = 8;
        _likeCountLabel.layer.masksToBounds = YES;
        _likeCountLabel.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1/1.0];
        _likeCountLabel.textAlignment = NSTextAlignmentCenter;
        _likeCountLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        [self addSubview:_likeCountLabel];
        [_likeCountLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.bottom.equalTo(self.mas_bottom).with.offset(-32);
            make.centerX.equalTo(self.mas_centerX);
            make.size.mas_equalTo(CGSizeMake(38, 16));
        }];
    }
    return _likeCountLabel;
}

@end
