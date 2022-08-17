//
//  MELLiveIntroductionContentView.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import "MELLiveIntroductionContentView.h"
#import <Masonry/Masonry.h>
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"

@implementation MELLiveIntroductionContentView

- (UILabel*)anchorIntroductionTitle {
    if (!_anchorIntroductionTitle) {
        _anchorIntroductionTitle = [[UILabel alloc] init];
        [self addSubview:_anchorIntroductionTitle];
        _anchorIntroductionTitle.font = [UIFont fontWithName:@"PingFangSC-Medium" size:14];
        _anchorIntroductionTitle.text = @"主播介绍";
        _anchorIntroductionTitle.textColor = [UIColor colorWithHexString:@"#000000" alpha:0.7];
        [self addSubview:_anchorIntroductionTitle];
        [_anchorIntroductionTitle mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(56, 22));
            make.left.equalTo(self).with.offset(24);
            make.top.equalTo(self).with.offset(12);
        }];
    }
    return _anchorIntroductionTitle;
}

- (UIImageView*)anchorAvartarView {
    if (!_anchorAvartarView) {
        _anchorAvartarView = [[UIImageView alloc] init];
        _anchorAvartarView.contentMode = UIViewContentModeScaleAspectFill;
        _anchorAvartarView.layer.cornerRadius = 22;
        _anchorAvartarView.layer.masksToBounds = YES;
        [_anchorAvartarView setImage:[UIImage imageNamed:@"默认头像" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
        [self addSubview:_anchorAvartarView];
        _anchorAvartarView.layer.masksToBounds = YES;
        [_anchorAvartarView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(44, 44));
            make.left.equalTo(self).with.offset(20);
            make.top.equalTo(self).with.offset(44);
        }];
    }
    return _anchorAvartarView;
}

- (UILabel*)anchorNickLabel {
    if (!_anchorNickLabel) {
        _anchorNickLabel = [[UILabel alloc] init];
        _anchorNickLabel.textAlignment = NSTextAlignmentLeft;
        _anchorNickLabel.textColor = [UIColor colorWithRed:50/255.0 green:50/255.0 blue:51/255.0 alpha:1/1.0];
        _anchorNickLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
        [self addSubview:_anchorNickLabel];
        [_anchorNickLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.anchorAvartarView.mas_right).with.offset(8);
            make.top.equalTo(self).with.offset(44);
            make.size.mas_equalTo(CGSizeMake(100, 20));
        }];
    }
    return _anchorNickLabel;
}

- (UILabel*)anchorIntroductionLabel {
    if (!_anchorIntroductionLabel) {
        _anchorIntroductionLabel = [[UILabel alloc] init];
        _anchorIntroductionLabel.text = @"这是默认写死的主播简介。";
        _anchorIntroductionLabel.textAlignment = NSTextAlignmentLeft;
        _anchorIntroductionLabel.textColor = [UIColor colorWithRed:150/255.0 green:151/255.0 blue:153/255.0 alpha:1/1.0];
        _anchorIntroductionLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        [self addSubview:_anchorIntroductionLabel];
        [_anchorIntroductionLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.anchorAvartarView.mas_right).with.offset(8);
            make.top.equalTo(self.anchorNickLabel.mas_bottom).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(250, 16));
        }];
    }
    return _anchorIntroductionLabel;
}

- (UILabel*)liveContentIntroductionTitle {
    if (!_liveContentIntroductionTitle) {
        _liveContentIntroductionTitle = [[UILabel alloc] init];
        [self addSubview:_liveContentIntroductionTitle];
        _liveContentIntroductionTitle.font = [UIFont fontWithName:@"PingFangSC-Medium" size:14];
        _liveContentIntroductionTitle.text = @"直播内容";
        _liveContentIntroductionTitle.textColor = [UIColor colorWithHexString:@"#000000" alpha:0.7];
        [self addSubview:_liveContentIntroductionTitle];
        [_liveContentIntroductionTitle mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(56, 22));
            make.left.equalTo(self).with.offset(24);
            make.top.equalTo(self).with.offset(108);
        }];
    }
    return _liveContentIntroductionTitle;
}

- (UILabel*)liveContentIntroduction {
    if (!_liveContentIntroduction) {
        _liveContentIntroduction = [[UILabel alloc] init];
        [self addSubview:_liveContentIntroduction];
        _liveContentIntroduction.text = @"这是默认写死的直播简介。";
        _liveContentIntroduction.textAlignment = NSTextAlignmentLeft;
        _liveContentIntroduction.textColor = [UIColor colorWithRed:150/255.0 green:151/255.0 blue:153/255.0 alpha:1/1.0];
        _liveContentIntroduction.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
        [_liveContentIntroduction mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self).with.offset(16);
//            make.right.equalTo(self).with.offset(-16);
            make.top.equalTo(self.liveContentIntroductionTitle.mas_bottom).with.offset(10);
//            make.bottom.equalTo(self);
            make.size.mas_equalTo(CGSizeMake(250, 16));
        }];
    }
    return _liveContentIntroduction;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.anchorIntroductionTitle];
        [self bringSubviewToFront:self.anchorAvartarView];
        [self bringSubviewToFront:self.anchorNickLabel];
        [self bringSubviewToFront:self.anchorIntroductionLabel];
        [self bringSubviewToFront:self.liveContentIntroductionTitle];
        [self bringSubviewToFront:self.liveContentIntroduction];
    }
    return self;
}

@end
