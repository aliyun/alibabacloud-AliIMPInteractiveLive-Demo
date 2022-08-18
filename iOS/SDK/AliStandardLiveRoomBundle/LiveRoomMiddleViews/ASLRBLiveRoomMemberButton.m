//
//  ASLRBLiveRoomMemberButton.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import "ASLRBLiveRoomMemberButton.h"
#import <Masonry/Masonry.h>
#import "../CommonTools/ASLRBResourceManager.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"

@implementation ASLRBLiveRoomMemberButton
#pragma mark -Properties
- (UIButton*) memberTextButton {
    if (!_memberTextButton) {
        _memberTextButton = [[UIButton alloc] init];
        _memberTextButton.contentHorizontalAlignment = UIControlContentHorizontalAlignmentLeft;
        [_memberTextButton addTarget:self action:@selector(membersButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_memberTextButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"观众"
                                                                          attributes:
                                           @{
                                               NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#FFFFFF" alpha:1.0],
                                               NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Medium" size:10]
                                           }] forState:UIControlStateNormal];
        [self addSubview:_memberTextButton];
        [_memberTextButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(26);
            make.top.equalTo(self.mas_top).with.offset(3);
            make.size.mas_equalTo(CGSizeMake(20, 14));
        }];
    }
    return _memberTextButton;
}

- (UIButton*) memberHeaderImageButton {
    if (!_memberHeaderImageButton) {
        _memberHeaderImageButton = [[UIButton alloc] init];
        [_memberHeaderImageButton setImage:[UIImage imageNamed:@"直播-观众" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:_memberHeaderImageButton];
        [_memberHeaderImageButton addTarget:self action:@selector(membersButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_memberHeaderImageButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(7);
            make.top.equalTo(self.mas_top).with.offset(3);
            make.size.mas_equalTo(CGSizeMake(14.2, 13.8));
        }];
    }
    return _memberHeaderImageButton;
}

- (UIButton*)memberDowndropFlagImageButton {
    if (!_memberDowndropFlagImageButton) {
        _memberDowndropFlagImageButton = [[UIButton alloc] init];
        [self addSubview:_memberDowndropFlagImageButton];
        [_memberDowndropFlagImageButton setImage:[UIImage imageNamed:@"按钮-返回" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        _memberDowndropFlagImageButton.transform = CGAffineTransformMakeRotation(M_PI * 1.5);
        [_memberDowndropFlagImageButton addTarget:self action:@selector(membersButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [_memberDowndropFlagImageButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(53);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(3.2, 6.3));
        }];
    }
    return _memberDowndropFlagImageButton;
}

#pragma mark Lifecycle

- (instancetype) init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.memberTextButton];
        [self bringSubviewToFront:self.memberDowndropFlagImageButton];
        [self bringSubviewToFront:self.memberHeaderImageButton];
    }
    return self;
}

#pragma mark UIButton Selector

- (void)membersButtonAction:(UIButton*)sender {
    if (self.onMemberButtonClicked) {
        self.onMemberButtonClicked();
    }
}

@end
