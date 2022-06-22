//
//  MELLiveIntroductionBottomViews.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import "MELLiveIntroductionBottomViews.h"
#import <Masonry/Masonry.h>

#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"

@implementation MELLiveIntroductionBottomViews

-(UIButton*)subscribeButton {
    if (!_subscribeButton) {
        _subscribeButton = [[UIButton alloc] init];
        [self addSubview:_subscribeButton];
        _subscribeButton.backgroundColor = [UIColor colorWithHexString:@"#00BCD4" alpha:1.0];
        [_subscribeButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"立即预约"
                                                                            attributes:
                                             @{
                                                 NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#FFFFFF" alpha:1.0],
                                                 NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Medium" size:16]
                                             }]
                                   forState:UIControlStateNormal];
        _subscribeButton.layer.cornerRadius = 20;
        [_subscribeButton addTarget:self action:@selector(onSubscribeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_subscribeButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.shareButton.mas_right).with.offset(12);
            make.right.equalTo(self.mas_right).with.offset(-16);
            make.height.mas_equalTo(38);
            make.top.equalTo(self.mas_top).with.offset(9);
        }];
        
    }
    return _subscribeButton;
}

- (UIButton*)shareButton {
    if (!_shareButton) {
        _shareButton = [[UIButton alloc] init];
        [_shareButton addTarget:self action:@selector(onShareButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_shareButton];
        [_shareButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(16);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
        [_shareButton setImage:[UIImage imageNamed:@"企业直播-分享-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _shareButton;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [self bringSubviewToFront:self.shareButton];
        [self bringSubviewToFront:self.subscribeButton];
    }
    return self;
}

- (void)onShareButtonClicked {
    self.onShare();
}

- (void)onSubscribeButtonClicked {
    
    if ([self.subscribeButton.titleLabel.text isEqualToString:@"立即预约"]) {
        __weak typeof(self) weakSelf = self;
        self.onSubscribe(^(BOOL didSubscribed) {
            if (didSubscribed) {
                weakSelf.subscribeButton.backgroundColor = [UIColor colorWithHexString:@"#CBCBCB" alpha:1.0];
                [weakSelf.subscribeButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"已经预约"
                                                                                            attributes:
                                                             @{
                                                                 NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#FFFFFF" alpha:1.0],
                                                                 NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Medium" size:16]
                                                             }]
                                                   forState:UIControlStateNormal];
            }
        });
    } else if ([self.subscribeButton.currentTitle isEqualToString:@"已经预约"]) {
        
    }
}

@end
