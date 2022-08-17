//
//  SECLRAGiftsViewController.m
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/11.
//

#import "MELGiftsViewController.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"
#import "MELInteractionButton.h"

@interface MELGiftsViewController ()
@property (strong, nonatomic) UILabel* headerTitleLabel;
@property (strong, nonatomic) MELInteractionButton* rocket;

@end

@implementation MELGiftsViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    self.view.backgroundColor = [UIColor whiteColor];
    
    [self.view bringSubviewToFront:self.headerTitleLabel];
    [self.view bringSubviewToFront:self.rocket];
    
    UIBezierPath *maskPath = [UIBezierPath bezierPathWithRoundedRect:CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height) byRoundingCorners:UIRectCornerTopLeft|UIRectCornerTopRight cornerRadii:CGSizeMake(12, 12)];
    CAShapeLayer *maskLayer = [[CAShapeLayer alloc] init];
    maskLayer.frame = self.view.bounds;
    maskLayer.path = maskPath.CGPath;
    self.view.layer.mask = maskLayer;
}

- (UILabel*)headerTitleLabel {
    if (!_headerTitleLabel) {
        _headerTitleLabel = [[UILabel alloc] init];
        _headerTitleLabel.text = @"选择礼物";
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

- (MELInteractionButton*)rocket {
    if (!_rocket) {
        _rocket = [[MELInteractionButton alloc] init];
        [self.view addSubview:_rocket];
        [_rocket.button setImage:[UIImage imageNamed:@"icon_rocket" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [_rocket.button addTarget:self action:@selector(onRocketButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        _rocket.titleLabel.text = @"火箭";
        [_rocket mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.headerTitleLabel.mas_bottom);
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(24);
            } else {
                make.left.equalTo(self.view.mas_left).with.offset(24);
            }
            make.size.mas_equalTo(CGSizeMake(48, 72));
        }];
    }
    return _rocket;
}

#pragma mark UIButton Actions

- (void)onRocketButtonClicked {
    [self.delegate onRocketButtonClicked];
}

@end
