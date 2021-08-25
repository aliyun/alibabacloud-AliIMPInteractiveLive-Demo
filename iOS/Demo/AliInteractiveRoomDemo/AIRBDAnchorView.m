//
//  AIRBDAnchorView.m
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/14.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBDAnchorView.h"
#import "AIRBDCommentView.h"
#import "AIRBDItemsView.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDToast.h"
#import "Utilities/Utility.h"
#import <Masonry/Masonry.h>

@interface AIRBDAnchorView() <AIRBRoomChannelDelegate,UITextFieldDelegate,AIRBDItemsViewDelegate, AIRBLivePusherDelegate>
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property (assign, nonatomic) BOOL roomEntered;
@property (strong, nonatomic) AIRBLivePusherFaceBeautyOptions* beautyOptions;
@property (copy,   nonatomic) NSString* userID;
@property (copy,   nonatomic) NSString* roomID;
@property (strong, nonatomic) NSMutableDictionary* infoDictionary;
@property (assign, nonatomic) int audienceNum;
@property (strong, nonatomic) UIButton* liveButton;

@property (strong, nonatomic) UIButton* shareButton;
@property (strong, nonatomic) UIButton* editButton;
@property (strong, nonatomic) UIButton* moreButton;
@property (strong, nonatomic) UIButton* exitButton;
@property (strong, nonatomic) UIButton* addButton;
@property (strong, nonatomic) UIButton* goodsButton;
@property (strong, nonatomic) UIButton * beautyButton;
@property (strong, nonatomic) UIButton * membersButton;
@property (strong, nonatomic) UIButton* pauseButton;
@property (strong, nonatomic) UIButton* mutedButton;
@property (strong, nonatomic) UIButton * cameraButton;
@property (strong, nonatomic) UISwitch * beautySwitch;
@property (strong, nonatomic) UITextField* sendField;
@property (strong, nonatomic) UITextField* titleField;
@property (strong, nonatomic) UITextField* noticeField;
@property (assign, nonatomic) BOOL livePusherStarted;
@property (assign, nonatomic) BOOL isEditingRoom;
@property (assign, nonatomic) BOOL moreButtonShowed;
@property (assign, nonatomic) BOOL beautySetsShowed;
@property (assign, nonatomic) BOOL membersViewShowed;
@property (assign, nonatomic) BOOL isMuted;
@property (assign, nonatomic) BOOL isStopped;
@property (assign, nonatomic) BOOL beautyOn;
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
@property (strong, nonatomic) UILabel* roomTitle;
@property (strong, nonatomic) UILabel* roomInfo;
@property (strong, nonatomic) UITextView* roomNotice;
@property (strong, nonatomic) UIImageView* userProfileImageView;

@property (strong, nonatomic) AIRBDCommentView* commentView;
@property (strong, nonatomic) AIRBDItemsView* membersView;
@property (strong, nonatomic) UIStepper* membersStepper;

@end

@implementation AIRBDAnchorView
#pragma mark -  Properties
- (UIButton*)liveButton {
    if (!_liveButton) {
        _liveButton = [[UIButton alloc]init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_liveButton];
        [_liveButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.mas_equalTo(weakSelf.mas_centerX);
            make.centerY.mas_equalTo(weakSelf.mas_centerY);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(70);
        }];
        [_liveButton addTarget:self action:@selector(liveButtonAction:) forControlEvents:UIControlEventTouchUpInside];
//        [_liveButton setBackgroundImage:[UIImage imageNamed:@"start"] forState:UIControlStateNormal];
        [_liveButton setTitle:@"开始直播" forState:UIControlStateNormal];
        _liveButton.layer.masksToBounds = YES;
        _liveButton.layer.cornerRadius = 15;
        [_liveButton.titleLabel setFont:[UIFont boldSystemFontOfSize:25]];
        [_liveButton setBackgroundColor:[UIColor colorWithRed:1.0 green:0.3 blue:0.3 alpha:0.5]];
    }
    return _liveButton;
}

- (UITextField*) sendField {
    if (!_sendField) {
        _sendField = [[UITextField alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_sendField];
        [_sendField mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.goodsButton).with.offset(60);
            make.bottom.equalTo(weakSelf.mas_bottom).with.offset(-33);
            make.right.equalTo(weakSelf.editButton.mas_left).with.offset(-10);
            make.height.mas_equalTo(30);
        }];
        _sendField.textColor = [UIColor blackColor];
        _sendField.alpha = 0.8;
        _sendField.placeholder = @"说点什么…";
        _sendField.backgroundColor = [UIColor grayColor];
        _sendField.textAlignment = NSTextAlignmentLeft;
        _sendField.keyboardType = UIKeyboardTypeDefault;
        _sendField.returnKeyType = UIReturnKeySend;
        _sendField.keyboardAppearance = UIKeyboardAppearanceDefault;
        _sendField.delegate = self;
        _sendField.borderStyle = UITextBorderStyleRoundedRect;
        [_sendField setContentHuggingPriority:UILayoutPriorityRequired forAxis:UILayoutConstraintAxisHorizontal];
    }
    
    return _sendField;
}

- (UITextField*) titleField {
    if (!_titleField) {
        _titleField = [[UITextField alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_titleField];
        [_titleField mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.editButton.mas_left);
            make.bottom.equalTo(weakSelf.noticeField.mas_top).with.offset(-10);
            make.width.mas_equalTo(0.3*weakSelf.bounds.size.width);
            make.height.mas_equalTo(30);
        }];
        
        _titleField.textColor = [UIColor blackColor];
        _titleField.alpha = 1.0;
        _titleField.backgroundColor = [UIColor whiteColor];
        _titleField.textAlignment = NSTextAlignmentLeft;
        _titleField.keyboardType = UIKeyboardTypeDefault;
        _titleField.returnKeyType = UIReturnKeyContinue;
        _titleField.keyboardAppearance = UIKeyboardAppearanceDefault;
        _titleField.delegate = self;
        _titleField.borderStyle = UITextBorderStyleRoundedRect;
        _titleField.hidden = YES;
    }
    
    return _titleField;
}

- (UITextField*) noticeField {
    if (!_noticeField) {
        _noticeField = [[UITextField alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_noticeField];
        [_noticeField mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.editButton.mas_left);
            make.bottom.equalTo(weakSelf.editButton.mas_top).with.offset(-10);
            make.width.equalTo(weakSelf.titleField.mas_width);
            make.height.mas_equalTo(0.2*weakSelf.bounds.size.height);
        }];
        
        _noticeField.textColor = [UIColor blackColor];
        _noticeField.alpha = 1.0;
        _noticeField.backgroundColor = [UIColor whiteColor];
        _noticeField.textAlignment = NSTextAlignmentLeft;
        _noticeField.keyboardType = UIKeyboardTypeDefault;
        _noticeField.returnKeyType = UIReturnKeyDone;
        _noticeField.keyboardAppearance = UIKeyboardAppearanceDefault;
        _noticeField.delegate = self;
        _noticeField.borderStyle = UITextBorderStyleRoundedRect;
        _noticeField.hidden = YES;
        _noticeField.textAlignment = NSTextAlignmentLeft;
    }
    
    return _noticeField;
}

- (UISlider*) beautyWhiteSlider {
    if (!_beautyWhiteSlider) {
//        _beautyWhiteSlider = [[UISlider alloc] initWithFrame:CGRectMake(0, 60, self.bounds.size.width / 2, 20)];
        _beautyWhiteSlider = [[UISlider alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyWhiteSlider];
        [_beautyWhiteSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.roomNotice.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyWhiteSlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyRuddySlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyBuffingSlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyCheekPinkSlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyShortenFaceSlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.top.equalTo(weakSelf.beautyThinFaceSlider.mas_bottom).with.offset(10);
            make.width.mas_equalTo(self.bounds.size.width / 2);
            make.height.mas_equalTo(20);
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

- (UIButton*) beautyButton {
    if (!_beautyButton) {
        _beautyOn = YES;
        _beautyButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_beautyButton];
        [_beautyButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton);
            make.top.equalTo(weakSelf.mutedButton.mas_bottom).with.offset(10);
            make.width.mas_equalTo(50);
            make.height.mas_equalTo(50);
        }];
        _beautySetsShowed = NO;
        [_beautyButton setImage:[UIImage imageNamed:@"beauty"] forState:UIControlStateNormal];
        [_beautyButton addTarget:self action:@selector(beautyButtonAction:) forControlEvents:UIControlEventTouchUpInside];

    }
    return _beautyButton;
}

- (UIButton*) membersButton {
    if (!_membersButton) {
        _membersButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_membersButton];
        [_membersButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton);
            make.top.equalTo(weakSelf.beautyButton.mas_bottom).with.offset(10);
            make.width.mas_equalTo(50);
            make.height.mas_equalTo(50);
        }];
        _membersViewShowed = NO;
        [_membersButton setImage:[UIImage imageNamed:@"members"] forState:UIControlStateNormal];
        [_membersButton addTarget:self action:@selector(membersButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _membersButton;
}

- (UIButton*) cameraButton {
    if (!_cameraButton) {
        _cameraButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_cameraButton];
        [_cameraButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton);
            make.top.equalTo(weakSelf.exitButton.mas_bottom).with.offset(10);
            make.width.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        [_cameraButton setImage:[UIImage imageNamed:@"camera"] forState:UIControlStateNormal];
        [_cameraButton addTarget:self action:@selector(toggleCamera:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _cameraButton;
}

- (UIButton*) pauseButton {
    if (!_pauseButton) {
        _isStopped = NO;
//        _pauseButton = [[UIButton alloc] initWithFrame:CGRectMake(self.bounds.size.width - 50, 140, 50, 30)];
        _pauseButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_pauseButton];
        [_pauseButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton.mas_centerX);
            make.top.equalTo(weakSelf.cameraButton.mas_bottom).with.offset(10);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        

        [_pauseButton setImage:[UIImage imageNamed:@"lock"] forState:UIControlStateNormal];
        [_pauseButton addTarget:self action:@selector(pauseButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _pauseButton;
}

- (UIButton*) mutedButton {
    if (!_mutedButton) {
        _isMuted = NO;
//        _mutedButton = [[UIButton alloc] initWithFrame:CGRectMake(self.bounds.size.width - 50, 180, 50, 30)];
        _mutedButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_mutedButton];
        [_mutedButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton.mas_centerX);
            make.top.equalTo(weakSelf.pauseButton.mas_bottom).with.offset(10);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_mutedButton setImage:[UIImage imageNamed:@"stop"] forState:UIControlStateNormal];
//        [_mutedButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
//        [_mutedButton setBackgroundColor:[UIColor yellowColor]];
        [_mutedButton addTarget:self action:@selector(mutedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _mutedButton;
}

- (UIButton*) shareButton {
    if (!_shareButton) {
        _shareButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_shareButton];
        [_shareButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.moreButton.mas_left).with.offset(-10);
            make.centerY.equalTo(weakSelf.moreButton.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_shareButton setImage:[UIImage imageNamed:@"share"] forState:UIControlStateNormal];
        [_shareButton addTarget:self action:@selector(shareButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _shareButton;
}

- (UIButton*) editButton {
    if (!_editButton) {
        _editButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_editButton];
        [_editButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.shareButton.mas_left).with.offset(-10);
            make.centerY.equalTo(weakSelf.moreButton.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_editButton setImage:[UIImage imageNamed:@"edit"] forState:UIControlStateNormal];
        [_editButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        _isEditingRoom = FALSE;
        [_editButton addTarget:self action:@selector(editButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _editButton;
}

- (UIButton*) moreButton {
    
    if (!_moreButton) {
        _moreButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_moreButton];
        [_moreButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.mas_right).with.offset(-10);
            make.bottom.equalTo(weakSelf.mas_bottom).with.offset(-33);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_moreButton setImage:[UIImage imageNamed:@"more"] forState:UIControlStateNormal];
//        [_moreButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
//        [_moreButton setBackgroundColor:[UIColor yellowColor]];
        _moreButtonShowed = FALSE;
        [_moreButton addTarget:self action:@selector(moreButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _moreButton;
}

- (UIButton*) exitButton{
    if (!_exitButton) {
        _exitButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_exitButton];
        [_exitButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.mas_right).with.offset(-5);
            make.top.equalTo(weakSelf.mas_top).with.offset(20);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_exitButton setImage:[UIImage imageNamed:@"exit"] forState:UIControlStateNormal];
        [_exitButton addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _exitButton;
}

- (UIButton*) addButton {
    if (!_addButton) {
        _addButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_addButton];
        [_addButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.roomTitle.mas_right).with.offset(9);
            make.centerY.equalTo(weakSelf.userProfileImageView.mas_centerY);
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(30);
        }];
        
        [_addButton setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addButton addTarget:self action:@selector(addButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addButton;
}

- (UIButton*) goodsButton {
    if (!_goodsButton) {
        _goodsButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_goodsButton];
        [_goodsButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mas_left).with.offset(9);
            make.centerY.equalTo(weakSelf.moreButton.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_goodsButton setImage:[UIImage imageNamed:@"goods"] forState:UIControlStateNormal];
        [_goodsButton addTarget:self action:@selector(goodsButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _goodsButton;
}

- (UISwitch*) beautySwitch{
    if(!_beautySwitch){
        _beautySwitch = [[UISwitch alloc]init];
        [self addSubview:_beautySwitch];
        __weak typeof(self) weakSelf = self;
        [_beautySwitch mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(weakSelf.beautyBigEyeSlider.mas_bottom).with.offset(5);
                    make.left.equalTo(weakSelf.beautyBuffingSlider.mas_left).with.offset(10);
        }];
        [_beautySwitch setTintColor:[UIColor colorWithRed:0 green:0.8 blue:0.7 alpha:0.8]];
        [_beautySwitch addTarget:self action:@selector(toggleBeauty:) forControlEvents:UIControlEventValueChanged];
        [_beautySwitch setHidden:YES];
        [_beautySwitch setOn:YES];
        if (@available(iOS 14.0, *)) {
            [_beautySwitch setPreferredStyle:UISwitchStyleAutomatic];
        }
    }
    return _beautySwitch;
}

- (UILabel*) roomTitle{
    if(!_roomTitle){
        self.roomTitle = [[UILabel alloc] init];
        [self addSubview:self.roomTitle];
        __weak typeof(self) weakSelf = self;
        [_roomTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_right).with.offset(10);
            make.top.equalTo(weakSelf.mas_top).with.offset(40);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(30);
        }];
        [_roomTitle setText:@"DefaultTitle"];
        [_roomTitle setTextColor:[UIColor whiteColor]];
        [_roomTitle setBackgroundColor:[UIColor colorWithWhite:0.4 alpha:0.4]];
        [_roomTitle.layer setCornerRadius:4];
        _roomTitle.layer.masksToBounds = YES;
    }
    return _roomTitle;
}

- (UILabel*) roomInfo{
    if(!_roomInfo){
        self.roomInfo = [[UILabel alloc] init];
        [self addSubview:self.roomInfo];
        __weak typeof(self) weakSelf = self;
        [_roomInfo mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_right).with.offset(10);
            make.top.equalTo(weakSelf.roomTitle.mas_bottom).with.offset(5);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(30);
        }];
        [_roomInfo setText:@"DefaultInfo"];
        [_roomInfo setTextColor:[UIColor whiteColor]];
        [_roomInfo setBackgroundColor:[UIColor colorWithWhite:0.4 alpha:0.4]];
        [_roomInfo.layer setCornerRadius:4];
        _roomInfo.layer.masksToBounds = YES;
        [_roomInfo setFont:[UIFont systemFontOfSize:12]];
    }
    return _roomInfo;
}

- (UITextView*) roomNotice{
    if(!_roomNotice){
        self.roomNotice = [[UITextView alloc] init];
        [self addSubview:self.roomNotice];
        __weak typeof(self) weakSelf = self;
        [_roomNotice mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_left);
            make.top.equalTo(weakSelf.roomInfo.mas_bottom).with.offset(10);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(60);
        }];
        [_roomNotice setText:@"DefaultNotice"];
        [_roomNotice setBackgroundColor:[UIColor colorWithWhite:0.4 alpha:0.4]];
        [_roomNotice setTextColor:[UIColor whiteColor]];
        [_roomNotice.layer setCornerRadius:4];
        [_roomNotice setEditable:NO];
    }

    return _roomNotice;
}

- (UIImageView*) userProfileImageView{
    if(!_userProfileImageView){
        _userProfileImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"default_pic2"]];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_userProfileImageView];
        [_userProfileImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mas_left).with.offset(10);
            make.top.equalTo(weakSelf.mas_top).with.offset(40);
            make.width.mas_equalTo(60);
            make.height.mas_equalTo(60);
        }];
    }
    return _userProfileImageView;
}

- (AIRBDCommentView*) commentView{
    if(!_commentView){
        _commentView = [[AIRBDCommentView alloc]init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_commentView];
        [_commentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.sendField.mas_top).with.offset(-10);
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.right.equalTo(weakSelf.sendField.mas_right);
            make.height.mas_equalTo(0.3*weakSelf.bounds.size.height);
        }];
        
    }
    return _commentView;
}

- (AIRBDItemsView*) membersView{
    if(!_membersView){
        _membersView = [[AIRBDItemsView alloc]init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_membersView];
        [_membersView mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.top.equalTo(weakSelf.membersButton.mas_top);
                    make.right.equalTo(weakSelf.membersButton.mas_left).with.offset(-5);
                    make.width.mas_equalTo(weakSelf.bounds.size.width/2-10);
                    make.height.mas_equalTo(200);
                }];
        
    }
    _membersView.hidden = YES;
    _membersView.ItemsViewdelegate = self;
    return _membersView;
}

- (UIStepper*)membersStepper{
    if(!_membersStepper){
        _membersStepper = [[UIStepper alloc]init];
        _membersStepper.minimumValue = 1;
        _membersStepper.maximumValue = 100;
        _membersStepper.stepValue = 1;
        __weak typeof(self) weakSelf = self;
        [self addSubview:_membersStepper];
        [_membersStepper setNeedsLayout];
        [_membersStepper mas_makeConstraints:^(MASConstraintMaker *make) {
                    make.bottom.equalTo(weakSelf.membersView.mas_top).with.offset(-5);
                    make.centerX.mas_equalTo(weakSelf.membersView.mas_centerX);
                    make.width.mas_equalTo(80);
                    make.height.mas_equalTo(40);
        }];
        _membersStepper.hidden = YES;
        [_membersStepper addTarget:self action:@selector(membersStepperAction:) forControlEvents:UIControlEventValueChanged];
    }
    return _membersStepper;
}

- (NSMutableDictionary *)infoDictionary{
    if(!_infoDictionary){
        _infoDictionary = [[NSMutableDictionary alloc]init];
    }
    return _infoDictionary;
}

#pragma mark -  LifeCycle

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
    return self;
}

- (instancetype) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self leaveRoom];
}

- (void) enterRoomWithID:(NSString*)roomID userID:(NSString*)userID {
    self.roomID = roomID;
    self.userID = userID;
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:roomID];
    self.room.delegate = self;
    [self.room enterRoom];
}

- (void) leaveRoom {
    if(self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
    }
}

- (void) createRoomWithConfig:(AIRBRoomEngineConfig*)config userID:(NSString*)userID completion:(void(^)(NSString* roomID))onGotRoomID {
//    self.userID = userID;
//    self.config = config;
//
//    NSString* templateId = @"default";
//    NSString* bizType = @"business";
//    NSString* title = @"weihe";
//    NSString* notice = @"weihe";
//
//    NSString* s = [NSString stringWithFormat:@"%@?domain=%@&bizType=%@&templateId=%@&title=%@&notice=%@&ownerId=%@", path, self.config.appID, bizType, templateId, title, notice, self.userID];
//
//    NSString* dateString = [Utility currentDateString];
//    NSString* nonce = [Utility randomNumString];
//
//    NSDictionary* headers = @{
//        @"a-app-id" : @"imp-room",
//        @"a-signature-method" : @"HMAC-SHA1",
//        @"a-signature-version" : @"1.0",
//        @"a-timestamp" : dateString,
//        @"a-signature-nonce" : nonce,
//    };
//
//    NSDictionary* params = @{
//        @"domain" : self.config.appID,
//        @"bizType" : bizType,
//        @"templateId" : templateId,
//        @"title" : title,
//        @"notice" : notice,
//        @"ownerId" : self.userID
//    };
//
//    NSLog(@"signedString:%@", signedString);
//
//    NSURL* url = [[NSURL alloc] initWithString:s];
//
//    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
//    request.HTTPMethod = @"POST";
//    [request setValue:@"imp-room" forHTTPHeaderField:@"a-app-id"];
//    [request setValue:@"HMAC-SHA1" forHTTPHeaderField:@"a-signature-method"];
//    [request setValue:@"1.0" forHTTPHeaderField:@"a-signature-version"];
//    [request setValue:signedString forHTTPHeaderField:@"a-signature"];
//    [request setValue:dateString forHTTPHeaderField:@"a-timestamp"];
//    [request setValue:nonce forHTTPHeaderField:@"a-signature-nonce"];
//
//    NSURLSession* session = [NSURLSession sharedSession];
//    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
//        if (data && !error) {
//            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
//                                                                options:NSJSONReadingMutableContainers
//                                                                  error:nil];
//            NSLog(@"createRoom data:%@", dic);
//            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
//                NSDictionary* resultDic = [dic valueForKey:@"result"];
//                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 0 && [resultDic valueForKey:@"roomId"]) {
//                    onGotRoomID([resultDic valueForKey:@"roomId"]);
//                }
//            }
//        } else if (error) {
//            ;
//        }
//    }];
//    [task resume];
}

#pragma mark - ItemsViewdelegate

-(void)useItem:(NSString *)itemID{
    if([itemID length]>0){
        dispatch_async(dispatch_get_main_queue(), ^{
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"管理该成员" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
            [alert addAction:[UIAlertAction actionWithTitle:@"禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat muteUserWithUserID:itemID muteTimeInSeconds:300 onSuccess:^{
                        
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"禁言用户失败:%@",errorMessage] duration:1.0];
                    });
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat unmuteUserWithUserID:itemID onSuccess:^{
                    
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"踢出房间" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room kickRoomUserWithUserID:itemID onSuccess:^{
                    [self updateUsersList];
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"删除用户失败:%@",errorMessage] duration:1.0];
                    });
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil]];
            alert.popoverPresentationController.sourceRect = [self.membersView cellForRowAtIndexPath:[self.membersView indexPathForSelectedRow]].frame;
            alert.popoverPresentationController.sourceView = self.membersView;
            [self.membersView setHidden:NO];
            [self.actionDelegate presentViewController:alert animated:YES completion:nil];
            
            
        });
    }
}

#pragma mark - AIRBRoomChannelProtocol

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRoomChannelErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
        [self.exitActionDelegate popViewControllerAnimated:YES];
    });
}

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info{
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.room.livePusher.delegate = self;
            self.roomEntered = YES;
            AIRBLivePusherOptions* options = [AIRBLivePusherOptions defaultOptions];
            options.faceBeautyOptions.beautyMode = AIRBLivePushBeautyModeProfessional;
            [self.room.livePusher startLocalPreviewWithOptions:options];
            [self setUpUI];
        }
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
            break;
            
        case AIRBRoomChannelEventRoomInfoGotten:{
            _infoDictionary = [NSMutableDictionary dictionaryWithDictionary:info];
        }
            break;
            
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
            NSString* messageType = @"";
            NSString* comment = nil;
 
            NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            switch (type) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    messageType = @"RoomMembersInOut";
                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
                    NSString* userID = [dataDic valueForKey:@"userId"];
                    if (userID.length > 0) {
                        if (enter) {
                            int count = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]]intValue];
                            comment = [NSString stringWithFormat:@"%@ 进入了房间",[dataDic valueForKey:@"nick"]];
                            [_infoDictionary setValue:[NSNumber numberWithInt:count] forKey:@"onlineCount"];
                            [self updateRoomInfo];
                            [_commentView insertNewComment:comment];
                        } else {
                            int count = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]]intValue];
                            comment = [NSString stringWithFormat:@"%@ 离开了房间",[dataDic valueForKey:@"nick"]];
                            [_infoDictionary setValue:[NSNumber numberWithInt:count] forKey:@"onlineCount"];
                            [self updateRoomInfo];
                            [_commentView insertNewComment:comment];
                        }
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    messageType = @"RoomTitleUpdated";
                    [self updateRoomTitle:[info valueForKey:@"data"]];
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated:
                    messageType = @"RoomNoticeUpdated";
                    [self updateRoomNotice:[info valueForKey:@"data"]];
                    break;
                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
                    messageType = @"LiveCreatedByOther";
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    messageType = @"LiveStartedByOther";
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:
                    messageType = @"LiveStoppedByOther";
                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived:
                    messageType = @"ChatLikeReceived";
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived:
                    messageType = @"ChatCommentReceived";
                    comment = [NSString stringWithFormat:@"%@:%@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeChatOneUserMutedOrUnmuted:
                    messageType = @"OneUserWasMuted";
                    if([[dataDic valueForKey:@"mute"] boolValue] == YES){
                        comment = [NSString stringWithFormat:@"%@被禁言%@秒",[dataDic valueForKey:@"muteUserNick"],[dataDic valueForKey:@"muteTime"]];
                    }else{
                        comment = [NSString stringWithFormat:@"%@被取消禁言",[dataDic valueForKey:@"muteUserNick"]];
                    }
                    
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
                    messageType = @"OneUserWasKickedOutRoom";
                    comment = [NSString stringWithFormat:@"%@被踢出房间",[dataDic valueForKey:@"kickUserName"]];
                    [_commentView insertNewComment:comment];
                    [self updateUsersList];
                    break;
                default:
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"type:%@,data:%@", messageType, [info valueForKey:@"data"] ? : nil] duration:2.0];
                    });
                    break;
            };
            
            break;
        }
        default:
            break;
    }
}

- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info{
    switch (event) {
        case AIRBLivePusherEventCreated: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.beautyOptions = self.room.livePusher.options.faceBeautyOptions;
            });
        }
            break;
        case AIRBLivePusherEventStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.livePusherStarted = YES;
                self.liveButton.enabled = YES;
            });
        }
            break;
            
        case AIRBLivePusherEventStopped: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.livePusherStarted = NO;
                self.liveButton.enabled = YES;
            });
            break;
        }
        case AIRBLivePusherEventNetworkPoored: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"遭遇弱网" duration:1.0];
            });
        }
            break;
        case AIRBLivePusherEventNetworkRecoveried: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"网络恢复" duration:1.0];
            });
        }
            break;
        default:
            break;
    }
}

- (void)liveButtonAction:(id)sender {
    if (!self.livePusherStarted) {
        self.livePusherStarted = YES;
        [self.room.livePusher startLiveStreaming];
        self.liveButton.hidden = YES;
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    if (textField == _sendField && textField.text.length > 0) {
        [self.room.chat sendMessage:textField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });;
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送失败" duration:1.0];
            });;
        }];
    }
    self.sendField.text = nil;
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
    return YES;
}

- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘高度
    CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat keyBoardHeight = keyBoardBounds.size.height;
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    __weak typeof(self) weakSelf = self;
    void (^animation)(void) = ^void(void) {
        if(self->_sendField.isEditing == YES){
            self->_sendField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - 30));
            self->_sendField.backgroundColor = [UIColor whiteColor];
            [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.left.equalTo(weakSelf);
                make.right.equalTo(weakSelf);
                make.bottom.equalTo(weakSelf.mas_bottom).with.offset(-33);
                make.height.mas_equalTo(30);
            }];
        }
        if(self->_titleField.isEditing == YES){
            self->_titleField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - self->_noticeField.bounds.size.height - 30));
        }
        if(self->_noticeField.isEditing == YES){
            self->_noticeField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - 30));
        }
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    __weak typeof(self) weakSelf = self;
    void (^animation)(void) = ^void(void) {
        self.sendField.transform = CGAffineTransformIdentity;
        self->_titleField.transform = CGAffineTransformIdentity;
        self->_noticeField.transform = CGAffineTransformIdentity;
        [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.goodsButton).with.offset(60);
            make.bottom.equalTo(weakSelf.mas_bottom).with.offset(-33);
            make.right.equalTo(weakSelf.editButton.mas_left).with.offset(-10);
            make.height.mas_equalTo(30);
        }];
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)toggleBeauty:(UIButton*)sender {
    if(_beautyOn == NO){
        _beautyOn = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"美颜已打开" duration:1.0];
        });
    }else{
        _beautyOn = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"美颜已关闭" duration:1.0];
        });
    }
    
    [self.room.livePusher toggleFaceBeauty];
}

- (void)toggleCamera:(UIButton*)sender {
    [self.room.livePusher toggleCamera];
}

- (void)sliderValueChanged:(UISlider *)slider{
    self.beautyOptions = self.room.livePusher.options.faceBeautyOptions;
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
    [self.room.livePusher updateFaceBeautyParameters:self.beautyOptions];
}

- (void)pauseButtonAction:(UIButton*)sender {
    if (_isStopped == NO) {
        _isStopped = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已暂停" duration:1.0];
        });
        [_pauseButton setImage:[UIImage imageNamed:@"locked"] forState:UIControlStateNormal];
        [self.room.livePusher pauseLiveStreaming];
    } else {
        _isStopped = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已继续" duration:1.0];
        });
        [_pauseButton setImage:[UIImage imageNamed:@"lock"] forState:UIControlStateNormal];
        [self.room.livePusher resumeLiveStreaming];
    }
}

- (void)mutedButtonAction:(UIButton*)sender {
    if (_isMuted == NO) {
        _isMuted = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已静音" duration:1.0];
        });
        [self.room.livePusher toggleMuted];
    } else {
        _isMuted = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已取消静音" duration:1.0];
        });
        [self.room.livePusher toggleMuted];
    }
}

- (void)shareButtonAction:(UIButton*)sender {
    UIPasteboard *pboard = [UIPasteboard generalPasteboard];
    pboard.string = self.roomID;
    [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@ 已拷贝", self.roomID] duration:1.0];
}

- (void)editButtonAction:(UIButton*)sender{
    if(self.isEditingRoom == NO){
        //编辑
        _titleField.placeholder = _roomTitle.text;
        _noticeField.placeholder = _roomNotice.text;
        [self.titleField setHidden:NO];
        [self.noticeField setHidden:NO];
        self.isEditingRoom = YES;
    }else{
        //完成编辑
        [self.titleField setHidden:YES];
        [self.noticeField setHidden:YES];
        if(self.titleField.text != nil){
            [_room updateRoomTitle:self.titleField.text onSuccess:^{

            } onFailure:^(NSString * _Nonnull errorMessage) {

            }];
        }
        if(self.noticeField.text != nil){
            [_room updateRoomNotice:self.noticeField.text onSuccess:^{

            } onFailure:^(NSString * _Nonnull errorMessage) {

            }];
        }
        //        [_editButton setTitle:@"编辑" forState:UIControlStateNormal];
        //        [_editButton setBackgroundColor:[UIColor yellowColor]];
        self.isEditingRoom = NO;
    }
    
}

- (void)exitButtonAction:(UIButton*)sender{
    //退出
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"退出直播……"] duration:1.0];
    });
    [self leaveRoom];
    if(_exitActionDelegate){
        [_exitActionDelegate popViewControllerAnimated:YES];
    }
}

- (void)beautyButtonAction:(UIButton*)sender{
    if(_beautySetsShowed == YES){
        _beautySetsShowed = NO;
        //隐藏美颜
        [self.beautyRuddySlider setHidden:YES];
        [self.beautyWhiteSlider setHidden:YES];
        [self.beautyBuffingSlider setHidden:YES];
        [self.beautyBigEyeSlider setHidden:YES];
        [self.beautyCheekPinkSlider setHidden:YES];
        [self.beautyThinFaceSlider setHidden:YES];
        [self.beautyShortenFaceSlider setHidden:YES];
        
        [self.beautyRuddySliderName setHidden:YES];
        [self.beautyWhiteSliderName setHidden:YES];
        [self.beautyBuffingSliderName setHidden:YES];
        [self.beautyBigEyeSliderName setHidden:YES];
        [self.beautyCheekPinkSliderName setHidden:YES];
        [self.beautyThinFaceSliderName setHidden:YES];
        [self.beautyShortenFaceSliderName setHidden:YES];
        [self.beautySwitch setHidden:YES];
    }else{
        _beautySetsShowed = YES;
        [self.beautyRuddySlider setHidden:NO];
        [self.beautyWhiteSlider setHidden:NO];
        [self.beautyBuffingSlider setHidden:NO];
        [self.beautyBigEyeSlider setHidden:NO];
        [self.beautyCheekPinkSlider setHidden:NO];
        [self.beautyThinFaceSlider setHidden:NO];
        [self.beautyShortenFaceSlider setHidden:NO];
        [self.beautyRuddySliderName setHidden:NO];
        [self.beautyWhiteSliderName setHidden:NO];
        [self.beautyBuffingSliderName setHidden:NO];
        [self.beautyBigEyeSliderName setHidden:NO];
        [self.beautyCheekPinkSliderName setHidden:NO];
        [self.beautyThinFaceSliderName setHidden:NO];
        [self.beautyShortenFaceSliderName setHidden:NO];
        [self.beautySwitch setHidden:NO];
    }
}

- (void)membersButtonAction:(UIButton*)sender{
    if(_membersViewShowed == YES){
        _membersViewShowed = NO;
        _membersView.hidden = YES;
        _membersStepper.hidden = YES;
    }else{
        _membersViewShowed = YES;
        [self updateUsersList];
        _membersView.hidden = NO;
        _membersStepper.hidden = NO;
    }
}

- (void)moreButtonAction:(UIButton*)sender{
    if(_moreButtonShowed == YES){
        _moreButtonShowed = NO;
        
    }else{
        _moreButtonShowed = YES;
        
    }
}

- (void)addButtonAction:(UIButton*)sender{
    //添加关注
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"成功添加关注/取消关注"] duration:1.0];
    });;
}

- (void)goodsButtonAction:(UIButton*)sender{
    //商品列表
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"展示商品列表……"] duration:1.0];
    });;
}

- (void)membersStepperAction:(UIStepper*)sender{
    [self updateUsersList];
}

-(void)setUpUI{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self addSubview:self.room.livePusher.pusherView];
        [self bringSubviewToFront:self.room.livePusher.pusherView];
        [self addSubview:self.liveButton];
        [self userProfileImageView];
        [self beautyButton];
        [self membersButton];
        [self cameraButton];
        [self exitButton];
        [self pauseButton];
        [self mutedButton];
        [self shareButton];
        [self editButton];
        
        [self roomTitle];
        [self roomInfo];
        [self roomNotice];
        [self updateRoomInfo];
        [self addButton];
        [self goodsButton];
        [self bringSubviewToFront:self.userProfileImageView];
        [self bringSubviewToFront:self.roomTitle];
        [self bringSubviewToFront:self.roomInfo];
        [self bringSubviewToFront:self.roomNotice];
        [self moreButton];
        [self sendField];
        [self titleField];
        [self noticeField];
        [self bringSubviewToFront:self.sendField];
        [self beautyRuddySlider];
        [self beautyWhiteSlider];
        [self beautyBuffingSlider];
        [self beautyThinFaceSlider];
        [self beautyCheekPinkSlider];
        [self beautyShortenFaceSlider];
        [self beautyBigEyeSlider];
        
        [self commentView];
        [self membersView];
        [self membersStepper];
        [self.beautyRuddySlider setHidden:YES];
        [self.beautyWhiteSlider setHidden:YES];
        [self.beautyBuffingSlider setHidden:YES];
        [self.beautyBigEyeSlider setHidden:YES];
        [self.beautyCheekPinkSlider setHidden:YES];
        [self.beautyThinFaceSlider setHidden:YES];
        [self.beautyShortenFaceSlider setHidden:YES];
        
        [self.beautyRuddySliderName setHidden:YES];
        [self.beautyWhiteSliderName setHidden:YES];
        [self.beautyBuffingSliderName setHidden:YES];
        [self.beautyBigEyeSliderName setHidden:YES];
        [self.beautyCheekPinkSliderName setHidden:YES];
        [self.beautyThinFaceSliderName setHidden:YES];
        [self.beautyShortenFaceSliderName setHidden:YES];
        [self.beautySwitch setHidden:YES];
    });
    [_commentView performSelector:@selector(insertNewComment:) withObject:@"您已进入房间" afterDelay:1];
}

-(void)updateUsersList{
    [self.room getRoomUserListWithPageNum:(int32_t)_membersStepper.value  pageSize:10 onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
        [self->_membersView updateItems:response.userList];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"获取用户信息失败:%@",errorMessage] duration:1.0];
        });
    }];
}

-(void)updateRoomInfo{
    if(_infoDictionary){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.roomTitle.text = [self->_infoDictionary valueForKey:@"title"];
            self.roomNotice.text = [self->_infoDictionary valueForKey:@"notice"];
            NSInteger count = [[NSString stringWithFormat:@"%@",[self->_infoDictionary valueForKey:@"onlineCount"]]integerValue];
            self.roomInfo.text = [NSString stringWithFormat:@"人数:%ld",(long)count];
        });
    }
}

-(void)updateRoomNotice:(NSString*)notice{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomNotice.text = notice;
        [self->_commentView performSelector:@selector(insertNewComment:) withObject:[NSString stringWithFormat:@"公告（更新）：%@",self->_roomNotice.text] afterDelay:1.2];
    });
}

-(void)updateRoomTitle:(NSString*)title{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomTitle.text = title;
    });
}

@end

