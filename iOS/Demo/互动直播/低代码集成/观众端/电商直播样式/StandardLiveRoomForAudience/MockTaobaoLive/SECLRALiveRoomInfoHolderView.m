//
//  ASLRBLiveRoomInfoHolderView.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/17.
//

#import "SECLRALiveRoomInfoHolderView.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"

@implementation SECLRALiveRoomInfoHolderView

- (instancetype) init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.anchorNickLabel];
        [self bringSubviewToFront:self.anchorAvatarView];
        [self bringSubviewToFront:self.pvLabel];
        [self bringSubviewToFront:self.followButton];
    }
    return self;
}

- (UIImageView *)anchorAvatarView{
    if (!_anchorAvatarView) {
        UIImageView* imageView = [[UIImageView alloc] init];
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 18.25;
        [self addSubview:imageView];
        [imageView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(2);
            make.top.equalTo(self.mas_top).with.offset(3);
            make.size.mas_equalTo(CGSizeMake(36.5, 36.5));
        }];
        _anchorAvatarView = imageView;
    }
    return _anchorAvatarView;
}

- (UILabel *)anchorNickLabel{
    if(!_anchorNickLabel){
        UILabel *label = [[UILabel alloc] init];
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [self addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(48);
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
            make.left.equalTo(self.mas_left).with.offset(47);
            make.top.equalTo(self.mas_top).with.offset(24);
            make.size.mas_equalTo(CGSizeMake(50, 14));
        }];
        _pvLabel = label;
    }
    return _pvLabel;
}

- (UIButton *)followButton{
    if(!_followButton){
        _followButton = [[UIButton alloc] init];
        [_followButton addTarget:self action:@selector(onFollowButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_followButton setImage:[UIImage imageNamed:@"关注按钮" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:_followButton];
        [_followButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.size.mas_equalTo(CGSizeMake(64, 26));
            make.centerY.equalTo(self.mas_centerY);
            make.right.equalTo(self).with.offset(-8);
        }];
    }
    return _followButton;
}

- (void) updatePV:(int32_t)pv {
    if (pv < 0) {
        self.pvLabel.text = @"0 观看";
    } else if (pv < 10000) {
        self.pvLabel.text = [NSString stringWithFormat:@"%d 观看", pv];
    } else {
        self.pvLabel.text = [NSString stringWithFormat:@"%.1f万 观看", pv * 1.0 / 10000.0];
    }
    
    CGSize sizeNew = [self.pvLabel.text sizeWithAttributes:@{NSFontAttributeName:self.pvLabel.font}];
    [self.pvLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(sizeNew.width + 18);
    }];
}

- (void) follow {
    if (_followButton) {
        [_followButton removeFromSuperview];
        _followButton = nil;
    }
    
    [self mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.right.equalTo(self.superview.mas_right).with.offset(-35);
    }];
}

#pragma mark UIButton

- (void)onFollowButtonClicked {
    [self follow];
    
    // nofify
    [[NSNotificationCenter defaultCenter] postNotificationName:@"SELRAFollowButtonClicked" object:nil];
}

@end
