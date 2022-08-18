//
//  ASLRBPlaybackLiveRoomBottomViewHolder.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import "ASLRBPlaybackLiveRoomBottomViewHolder.h"

#import <Masonry/Masonry.h>

#import "ASLRBLikeButton.h"
#import "ASLRBLiveRoomBottomViewActionsDelegate.h"
#import "../CommonTools/ASLRBResourceManager.h"
#import "ASLRBLiveRoomBottomViewsHolderProtocol.h"

@implementation ASLRBPlaybackLiveRoomBottomViewHolder

@synthesize shareButton = _shareButton;
@synthesize likeButton = _likeButton;

- (UIButton*)shareButton {
    if (!_shareButton) {
        UIButton* button = [[UIButton alloc] init];
        [self addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(self.likeButton.mas_left).with.offset(-10);
            make.centerY.equalTo(self.likeButton);
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        
        [button setImage:[UIImage imageNamed:@"直播-互动区-分享" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(shareButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _shareButton = button;
    }
    return _shareButton;
}

- (UIButton*)likeButton {
    if (!_likeButton) {
        ASLRBLikeButton* button = [[ASLRBLikeButton alloc] init];
        [self addSubview:button];
        
        __weak typeof(self) weakSelf = self;
        button.onLikeSent = ^{
            [weakSelf.actionsDelegate onLikeSent];
        };
    
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(self).with.offset(-9);
            make.right.equalTo(self.mas_right).with.offset(-10);
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        
        [button setImage:[UIImage imageNamed:@"直播-互动区-点赞" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        _likeButton = button;
    }
    return _likeButton;
}

#pragma mark --Lifecycle

- (instancetype) init {
    self = [super init];
    if (self) {
        [self addSubview:self.likeButton];
        [self addSubview:self.shareButton];
    }
    
    return self;
}

#pragma mark --UIButton Selectors

- (void)shareButtonAction:(UIButton*)sender {
    [self.actionsDelegate onShareButtonClicked];
}

@end
