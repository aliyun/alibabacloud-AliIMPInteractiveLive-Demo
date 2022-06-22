//
//  MELLiveInfoViewHolder.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/31.
//

#import "MELLiveInfoViewHolder.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"

@implementation MELLiveInfoViewHolder

- (UIImageView *)anchorAvartarView{
    if (!_anchorAvartarView) {
        UIImageView* imageView = [[UIImageView alloc] init];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 18.25;
        [imageView setImage: [UIImage imageNamed:@"默认头像" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
        [self addSubview:imageView];
        [imageView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(4);
            make.top.equalTo(self.mas_top).with.offset(4);
            make.size.mas_equalTo(CGSizeMake(34, 34));
        }];
        _anchorAvartarView = imageView;
    }
    return _anchorAvartarView;
}

- (UILabel *)anchorNickLabel{
    if(!_anchorNickLabel){
        UILabel *label = [[UILabel alloc] init];
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [self addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.mas_right).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(6);
            make.size.mas_equalTo(CGSizeMake(84, 14));
        }];
        _anchorNickLabel = label;
    }
    return _anchorNickLabel;
}

- (UILabel *)pvLabel{
    if(!_pvLabel){
        UILabel *label = [[UILabel alloc] init];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [self addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.anchorAvartarView.mas_right).with.offset(4);
            make.bottom.equalTo(self.mas_bottom).with.offset(-6);
            make.size.mas_equalTo(CGSizeMake(65.5, 12));
        }];
        _pvLabel = label;
    }
    return _pvLabel;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.pvLabel];
        [self bringSubviewToFront:self.anchorAvartarView];
        [self bringSubviewToFront:self.anchorNickLabel];
    }
    return self;
}

@end
