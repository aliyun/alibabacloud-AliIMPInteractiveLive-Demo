//
//  SECLRAMoreInteractionViewController.m
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import "SECLRAMoreInteractionViewController.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"
#import "SECLRAInteractionButton.h"

@interface SECLRAMoreInteractionViewController ()
@property (strong, nonatomic) UILabel* headerTitleLabel;
@property (strong, nonatomic) SECLRAInteractionButton* giftButton;
@end

@implementation SECLRAMoreInteractionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    [self.view bringSubviewToFront:self.headerTitleLabel];
    [self.view bringSubviewToFront:self.giftButton];
    
    UIBezierPath *maskPath = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height) byRoundingCorners:UIRectCornerTopLeft|UIRectCornerTopRight cornerRadii:CGSizeMake(12, 12)];
    CAShapeLayer *maskLayer = [[CAShapeLayer alloc] init];
    maskLayer.frame = self.view.bounds;
    maskLayer.path = maskPath.CGPath;
    self.view.layer.mask = maskLayer;
}

- (UILabel*)headerTitleLabel {
    if (!_headerTitleLabel) {
        _headerTitleLabel = [[UILabel alloc] init];
        _headerTitleLabel.text = @"主播互动";
        _headerTitleLabel.textAlignment = NSTextAlignmentCenter;
        _headerTitleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:16];
        _headerTitleLabel.textColor = [UIColor colorWithRed:50/255.0 green:50/255.0 blue:51/255.0 alpha:1/1.0];
        [self.view addSubview:_headerTitleLabel];
        [_headerTitleLabel mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.view);
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.height.mas_equalTo(48);
        }];
    }
    return _headerTitleLabel;
}

- (SECLRAInteractionButton*)giftButton {
    if (!_giftButton) {
        _giftButton = [[SECLRAInteractionButton alloc] init];
        [self.view addSubview:_giftButton];
        [_giftButton.button setImage:[UIImage imageNamed:@"主播互动-送礼物" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [_giftButton.button addTarget:self action:@selector(onGiftButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        _giftButton.titleLabel.text = @"送礼物";
        [_giftButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.headerTitleLabel.mas_bottom);
            make.left.equalTo(self.view.mas_left).with.offset(24);
            make.size.mas_equalTo(CGSizeMake(48, 72));
        }];
    }
    return _giftButton;
}

#pragma mark UIButton Actions

- (void)onGiftButtonClicked {
    [self.delegate onGiftButtonClicked];
}

@end
