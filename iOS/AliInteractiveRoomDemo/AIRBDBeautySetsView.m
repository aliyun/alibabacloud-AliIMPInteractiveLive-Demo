//
//  AIRBDBeautySetsView.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/21.
//
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDBeautySetsView.h"
#import <Masonry/Masonry.h>
@interface AIRBDBeautySetsView()
@property (strong, nonatomic) UISlider* beautyWhiteSlider;
@property (strong, nonatomic) UISlider* beautyBuffingSlider;
@property (strong, nonatomic) UISlider* beautyRuddySlider;
@property (strong, nonatomic) UISlider* beautyCheekPinkSlider;
@property (strong, nonatomic) UISlider* beautyThinFaceSlider;
@property (strong, nonatomic) UISlider* beautyShortenFaceSlider;
@property (strong, nonatomic) UISlider* beautyBigEyeSlider;
@property (strong, nonatomic) UILabel* beautyWhiteSliderName;
@property (strong, nonatomic) UILabel* beautyBuffingSliderName;
@property (strong, nonatomic) UILabel* beautyRuddySliderName;
@property (strong, nonatomic) UILabel* beautyCheekPinkSliderName;
@property (strong, nonatomic) UILabel* beautyThinFaceSliderName;
@property (strong, nonatomic) UILabel* beautyShortenFaceSliderName;
@property (strong, nonatomic) UILabel* beautyBigEyeSliderName;
@end
@implementation AIRBDBeautySetsView

- (instancetype)init
{
    
    self = [super init];
    if (self) {
        self.delaysContentTouches = YES;
        self.scrollEnabled = YES;
    }
    return self;
}

- (instancetype)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        self.delaysContentTouches = YES;
        self.scrollEnabled = YES;
    }
    return self;
}


- (UISlider*) beautyWhiteSlider {
    if (!_beautyWhiteSlider) {
//        _beautyWhiteSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 60, self.bounds.size.width / 2, 20)];
        _beautyWhiteSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyWhiteSlider];
        [_beautyWhiteSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyWhiteSlider.maximumValue = 100;
        _beautyWhiteSlider.minimumValue = 0;
        _beautyWhiteSlider.value = self.beautyOptions.beautyWhite;
        [_beautyWhiteSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];
        
//        self.beautyWhiteSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 60, 40, 20)];
        self.beautyWhiteSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyWhiteSliderName];
        [self.beautyWhiteSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyWhiteSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyWhiteSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyWhiteSliderName setText:@"美白"];
    }
    return _beautyWhiteSlider;
}

- (UISlider*) beautyRuddySlider {
    if (!_beautyRuddySlider) {
//        _beautyRuddySlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 90, self.bounds.size.width / 2, 20)];
        _beautyRuddySlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyRuddySlider];
        [_beautyRuddySlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyWhiteSlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyRuddySlider.maximumValue = 100;
        _beautyRuddySlider.minimumValue = 0;
        _beautyRuddySlider.value = self.beautyOptions.beautyRuddy;
        [_beautyRuddySlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];
        
//        self.beautyRuddySliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 90, 40, 20)];
        self.beautyRuddySliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyRuddySliderName];
        [self.beautyRuddySliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyRuddySlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyRuddySlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyRuddySliderName setText:@"红润"];
    }
    return _beautyRuddySlider;
}

- (UISlider*) beautyBuffingSlider {
    if (!_beautyBuffingSlider) {
//        _beautyBuffingSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 120, self.bounds.size.width / 2, 20)];
        _beautyBuffingSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyBuffingSlider];
        [_beautyBuffingSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyRuddySlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyBuffingSlider.maximumValue = 100;
        _beautyBuffingSlider.minimumValue = 0;
        _beautyBuffingSlider.value = self.beautyOptions.beautyBuffing;
        [_beautyBuffingSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];

        
//        self.beautyBuffingSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 120, 40, 20)];
        self.beautyBuffingSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyBuffingSliderName];
        [self.beautyBuffingSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyBuffingSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyBuffingSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyBuffingSliderName setText:@"磨皮"];
    }
    return _beautyBuffingSlider;
}

- (UISlider*) beautyCheekPinkSlider {
    if (!_beautyCheekPinkSlider) {
//        _beautyCheekPinkSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 150, self.bounds.size.width / 2, 20)];
        _beautyCheekPinkSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyCheekPinkSlider];
        [_beautyCheekPinkSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyBuffingSlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyCheekPinkSlider.maximumValue = 100;
        _beautyCheekPinkSlider.minimumValue = 0;
        _beautyCheekPinkSlider.value = self.beautyOptions.beautyCheekPink;
        [_beautyCheekPinkSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];

        
//        self.beautyCheekPinkSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 150, 40, 20)];
        self.beautyCheekPinkSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyCheekPinkSliderName];
        [self.beautyCheekPinkSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyCheekPinkSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyCheekPinkSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyCheekPinkSliderName setText:@"腮红"];
    }
    return _beautyCheekPinkSlider;
}

- (UISlider*) beautyShortenFaceSlider {
    if (!_beautyShortenFaceSlider) {
//        _beautyShortenFaceSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 180, self.bounds.size.width / 2, 20)];
        _beautyShortenFaceSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyShortenFaceSlider];
        [_beautyShortenFaceSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyCheekPinkSlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyShortenFaceSlider.maximumValue = 100;
        _beautyShortenFaceSlider.minimumValue = 0;
        _beautyShortenFaceSlider.value = self.beautyOptions.beautyShortenFace;
        [_beautyShortenFaceSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];

        
//        self.beautyShortenFaceSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 180, 60, 20)];
        self.beautyShortenFaceSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyShortenFaceSliderName];
        [self.beautyShortenFaceSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyShortenFaceSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyShortenFaceSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(60);
        }];
        [self.beautyShortenFaceSliderName setText:@"收下巴"];
    }
    return _beautyShortenFaceSlider;
}

- (UISlider*) beautyThinFaceSlider {
    if (!_beautyThinFaceSlider) {
//        _beautyThinFaceSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 210, self.bounds.size.width / 2, 20)];
        _beautyThinFaceSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyThinFaceSlider];
        [_beautyThinFaceSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyShortenFaceSlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyThinFaceSlider.maximumValue = 100;
        _beautyThinFaceSlider.minimumValue = 0;
        _beautyThinFaceSlider.value = self.beautyOptions.beautyThinFace;
        [_beautyThinFaceSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];

        
//        self.beautyThinFaceSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 210, 40, 20)];
        self.beautyThinFaceSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyThinFaceSliderName];
        [self.beautyThinFaceSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyThinFaceSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyThinFaceSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyThinFaceSliderName setText:@"瘦脸"];
    }
    return _beautyThinFaceSlider;
}

- (UISlider*) beautyBigEyeSlider {
    if (!_beautyBigEyeSlider) {
//        _beautyBigEyeSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 240, self.bounds.size.width / 2, 20)];
        _beautyBigEyeSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyBigEyeSlider];
        [_beautyBigEyeSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf);
            make.top.equalTo(weakSelf.beautyThinFaceSlider.mas_bottom).with.offset(2);
            make.height.mas_equalTo(weakSelf.bounds.size.height*0.14);
            make.width.mas_equalTo(weakSelf.bounds.size.width*0.6);
        }];
        
        _beautyBigEyeSlider.maximumValue = 100;
        _beautyBigEyeSlider.minimumValue = 0;
        _beautyBigEyeSlider.value = self.beautyOptions.beautyBigEye;
        [_beautyBigEyeSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];

        
//        self.beautyBigEyeSliderName = [[UILabel alloc] initWithFrame:CGRectMake(self.bounds.size.width / 2, 240, 40, 20)];
        self.beautyBigEyeSliderName = [[UILabel alloc] init];
        [self addSubview:self.beautyBigEyeSliderName];
        [self.beautyBigEyeSliderName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyBigEyeSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.beautyBigEyeSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.width.mas_equalTo(40);
        }];
        [self.beautyBigEyeSliderName setText:@"大眼"];
    }
    return _beautyBigEyeSlider;
}

- (void)loadSubviews{
    [self beautyWhiteSlider];
    [self beautyRuddySlider];
    [self beautyBuffingSlider];
    [self beautyCheekPinkSlider];
    [self beautyShortenFaceSlider];
    [self beautyThinFaceSlider];
    [self beautyBigEyeSlider];
}

- (void)setBeautyOptions:(AIRBLivePusherFaceBeautyOptions *)beautyOptions{
    _beautyOptions = beautyOptions;
    _beautyRuddySlider.value = beautyOptions.beautyRuddy;
    _beautyWhiteSlider.value = beautyOptions.beautyWhite;
    _beautyBuffingSlider.value = beautyOptions.beautyBuffing;
    _beautyCheekPinkSlider.value = beautyOptions.beautyCheekPink;
    _beautyShortenFaceSlider.value = beautyOptions.beautyShortenFace;
    _beautyThinFaceSlider.value = beautyOptions.beautyThinFace;
    _beautyBigEyeSlider.value = beautyOptions.beautyBigEye;
}

-(void)sliderValueChanged:(UISlider*)slider{
    if(!self.beautyOptions){
        return;
    }
    if (slider == self.beautyBigEyeSlider) {
        self.beautyOptions.beautyBigEye = slider.value;
    } else if (slider == self.beautyBuffingSlider) {
        self.beautyOptions.beautyBuffing = slider.value;
    } else if (slider == self.beautyCheekPinkSlider) {
        self.beautyOptions.beautyCheekPink = slider.value;
    } else if (slider == self.beautyRuddySlider) {
        self.beautyOptions.beautyRuddy = slider.value;
    } else if (slider == self.beautyShortenFaceSlider) {
        self.beautyOptions.beautyShortenFace = slider.value;
    } else if (slider == self.beautyThinFaceSlider) {
        self.beautyOptions.beautyThinFace = slider.value;
    } else if (slider == self.beautyWhiteSlider) {
        self.beautyOptions.beautyWhite = slider.value;
    }
    [self.beautySetsDelegate beautySetsValueChanged:self.beautyOptions];
}
@end
