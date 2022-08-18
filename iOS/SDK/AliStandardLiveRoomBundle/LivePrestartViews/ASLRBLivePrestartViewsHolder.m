//
//  ASLRBLivePrestartViewsHolder.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/1/13.
//

#import "ASLRBLivePrestartViewsHolder.h"

#import <Masonry/Masonry.h>

#import "ASLRBLivePrestartViewsHolderProtocol.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
#import "../CommonTools/ASLRBResourceManager.h"

@interface ASLRBLivePrestartViewsHolder() <ASLRBLivePrestartViewsHolderProtocol,UITextViewDelegate>
@property (strong, nonatomic) UILabel* liveTitlePlaceHolder;
@property (strong, nonatomic) UILabel* liveTitleCharactorsCountLabel;
@end

@implementation ASLRBLivePrestartViewsHolder

@synthesize startLiveButton = _startLiveButton;
@synthesize liveTitleTextView = _liveTitleTextView;
@synthesize liveTitleEditButton = _liveTitleEditButton;
@synthesize liveTitleMaxLength = _liveTitleMaxLength;
//@synthesize liveTitleBackgroundView = _liveTitleBackgroundView;

//@synthesize switchCameraLabel = _switchCameraLabel;
@synthesize switchCameraButton = _switchCameraButton;
//@synthesize beautyLabel = _beautyLabel;
@synthesize beautyButton = _beautyButton;

@synthesize exitButton = _exitButton;

- (UIButton*)exitButton {
    if (!_exitButton) {
        UIButton* backButton = [[UIButton alloc] init];
        [backButton setImage:[UIImage imageNamed:@"icon-exit" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [backButton addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:backButton];
        [backButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.mas_safeAreaLayoutGuideTop).with.offset(10);
                make.right.equalTo(self.mas_safeAreaLayoutGuideRight).with.offset(-10);
            } else {
                make.top.equalTo(self.mas_top).with.offset(10);
                make.right.equalTo(self.mas_right).with.offset(-10);
            }
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(30);
        }];
        _exitButton = backButton;
    }
    return _exitButton;
}

- (UIButton*) startLiveButton {
    if (!_startLiveButton) {
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(startLiveButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 25;
        button.backgroundColor = [UIColor colorWithHexString:@"#FF8E19" alpha:1];
        [button setTitle:@"开始直播" forState:UIControlStateNormal];
        button.titleLabel.textColor = [UIColor whiteColor];
        button.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
        [self addSubview:button];
        
        [button mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(self);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.mas_safeAreaLayoutGuideBottom).with.offset(-150);
            } else {
                make.bottom.equalTo(self.mas_bottom).with.offset(-150);
            }
            make.width.mas_equalTo(246);
            make.height.mas_equalTo(50);
        }];
        
        _startLiveButton = button;
    }
    return _startLiveButton;
}

- (UITextView*)liveTitleTextView {
    if (!_liveTitleTextView) {
        UITextView *textView = [[UITextView alloc] init];
        textView.editable = YES;
        textView.delegate = self;
        textView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.15];
        textView.layer.masksToBounds = YES;
        textView.layer.cornerRadius = 8;
        textView.textAlignment = NSTextAlignmentLeft;
        textView.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:18];
        textView.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        textView.showsVerticalScrollIndicator = NO;
        textView.showsHorizontalScrollIndicator = NO;
        [self addSubview:textView];
        
        [textView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(self);
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.mas_safeAreaLayoutGuideTop).with.offset(80);
            } else {
                make.top.equalTo(self.mas_top).with.offset(80);
            }
            make.height.mas_equalTo(85);
            make.width.mas_equalTo(266);
        }];
        _liveTitleTextView = textView;
    }
    return _liveTitleTextView;
}

- (UIButton*)liveTitleEditButton {
    if (!_liveTitleEditButton) {
        UIButton* button = [[UIButton alloc] init];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 3;
        [button setImage:[UIImage imageNamed:@"icon-edit" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(liveTitleEditButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        [self.liveTitleTextView addSubview:button];
        
        [button mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.liveTitleTextView.mas_top).with.offset(5);
            make.left.equalTo(self.liveTitleTextView.mas_left).with.offset(240);
            make.size.mas_equalTo(CGSizeMake(24, 24));
        }];
        _liveTitleEditButton = button;
    }
    return _liveTitleEditButton;
}

- (UILabel*)liveTitlePlaceHolder {
    if (!_liveTitlePlaceHolder) {
        UILabel* label = [[UILabel alloc] init];
        label.text = @"请输入直播标题";
        label.textColor = [UIColor lightGrayColor];
        [self.liveTitleTextView addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.liveTitleTextView.mas_left).with.offset(5);
            make.top.equalTo(self.liveTitleTextView.mas_top).with.offset(5);
            make.size.mas_equalTo(CGSizeMake(200, 24));
        }];
        
        _liveTitlePlaceHolder = label;
    }
    return _liveTitlePlaceHolder;
}

- (void)setLiveTitleMaxLength:(NSUInteger)liveTitleMaxLength {
    _liveTitleMaxLength = liveTitleMaxLength;
    self.liveTitleCharactorsCountLabel.text = [NSString stringWithFormat:@"%lu/%lu", self.liveTitleTextView.text.length, _liveTitleMaxLength];
}

- (UILabel*)liveTitleCharactorsCountLabel {
    if (!_liveTitleCharactorsCountLabel) {
        _liveTitleCharactorsCountLabel = [[UILabel alloc] init];
        _liveTitleCharactorsCountLabel.text = [NSString stringWithFormat:@"0/%lu", self.liveTitleMaxLength];
        _liveTitleCharactorsCountLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:10];
        _liveTitleCharactorsCountLabel.textColor = [UIColor lightGrayColor];
        [self.liveTitleTextView addSubview:_liveTitleCharactorsCountLabel];
        [_liveTitleCharactorsCountLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.liveTitleTextView.mas_top).with.offset(55);
            make.left.equalTo(self.liveTitleTextView.mas_left).with.offset(236);
            make.size.mas_equalTo(CGSizeMake(30, 30));
        }];
    }
    return _liveTitleCharactorsCountLabel;
}

- (UIButton*)switchCameraButton {
    if (!_switchCameraButton) {
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(switchCameraButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.imageView.contentMode = UIViewContentModeScaleAspectFill;
        button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentFill;
        button.contentVerticalAlignment = UIControlContentVerticalAlignmentFill;
        [button setImage:[UIImage imageNamed:@"icon-camera_switch" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(self).with.offset(-80);
            make.bottom.equalTo(self.startLiveButton.mas_top).with.offset(-47);
            make.width.mas_equalTo(36);
            make.height.mas_equalTo(32);
        }];
        
        [button addSubview:({
            UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(-10, 36, 60, 18)];
            label.textAlignment = NSTextAlignmentCenter;
            label.adjustsFontSizeToFitWidth = YES;
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
            label.textColor = [UIColor whiteColor];
            label.text = @"翻转";
            label;
        })];
        
        _switchCameraButton = button;
    }
    return _switchCameraButton;
    
}

- (UIButton*)beautyButton {
    if (!_beautyButton) {
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(beautyButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.imageView.contentMode = UIViewContentModeScaleAspectFill;
        [button setImage:[UIImage imageNamed:@"icon-beauty_white" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(self).with.offset(80);
            make.bottom.equalTo(self.startLiveButton.mas_top).with.offset(-47);
            make.width.mas_equalTo(36);
            make.height.mas_equalTo(36);
        }];
        
        [button addSubview:({
            UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(-10, 40, 60, 18)];
            label.textAlignment = NSTextAlignmentCenter;
            label.adjustsFontSizeToFitWidth = YES;
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
            label.textColor = [UIColor whiteColor];
            label.text = @"美颜";
            label;
        })];
        _beautyButton = button;
    }
    return _beautyButton;
}

#pragma mark -Lifecycle

-(instancetype) init {
    self = [super init];
    if (self) {
        _liveTitleMaxLength = 35;
        [self bringSubviewToFront:self.liveTitleTextView];
        [self.liveTitleTextView bringSubviewToFront:self.liveTitleEditButton];
        [self.liveTitleTextView bringSubviewToFront:self.liveTitlePlaceHolder];
        [self.liveTitleTextView bringSubviewToFront:self.liveTitleCharactorsCountLabel];
        
        [self bringSubviewToFront:self.startLiveButton];
        [self bringSubviewToFront:self.switchCameraButton];
        [self bringSubviewToFront:self.beautyButton];
        [self bringSubviewToFront:self.exitButton];
        
        UITapGestureRecognizer* tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onTapped:)];
        [self addGestureRecognizer:tap];
    }
    return self;
}

// 横屏
- (void) updateLayoutRotated:(BOOL)rotated{
    if (rotated) {
        [self.startLiveButton mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.centerX.equalTo(self);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.mas_safeAreaLayoutGuideBottom).with.offset(-10);
            } else {
                make.bottom.equalTo(self.mas_bottom).with.offset(-10);
            }
            make.width.mas_equalTo(246);
            make.height.mas_equalTo(50);
        }];
    }
}

#pragma mark -UIButton Selectors
- (void)startLiveButtonAction:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(onPrestartStartLiveButtonClicked:)]) {
        [self.delegate onPrestartStartLiveButtonClicked:self.liveTitleTextView.text];
    }
    [sender setTitle:@"加载中  " forState:UIControlStateNormal];
    UIActivityIndicatorView* spinner = [[UIActivityIndicatorView alloc] initWithFrame:CGRectMake(140, 0, 50, 50)];
    spinner.color = [UIColor whiteColor];
    spinner.tintColor = [UIColor whiteColor];
    [sender addSubview:spinner];
    [spinner startAnimating];
    sender.userInteractionEnabled = NO;
    sender.alpha = 0.8;
}

- (void)switchCameraButtonAction:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(onPrestartSwitchCameraButtonClicked)]) {
        [self.delegate onPrestartSwitchCameraButtonClicked];
    }
}

- (void)beautyButtonAction:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(onPrestartBeautyButtonClicked)]) {
        [self.delegate onPrestartBeautyButtonClicked];
    }
}

- (void)liveTitleEditButtonAction:(UIButton*)sender {
    [self.liveTitleTextView becomeFirstResponder];
}

- (void)exitButtonAction:(UIButton*)sender {
    if ([self.delegate respondsToSelector:@selector(onPrestartExitButtonClicked)]) {
        [self.delegate onPrestartExitButtonClicked];
    }
}

#pragma mark -UITapGestureRecognizer Selector
- (void) onTapped:(UITapGestureRecognizer*)sender {
    [self.liveTitleTextView resignFirstResponder];
}

#pragma mark -UITextViewDelegate

- (BOOL)textViewShouldBeginEditing:(UITextView *)textView {
    return YES;
}
- (BOOL)textViewShouldEndEditing:(UITextView *)textView {
    return YES;
}

- (void)textViewDidBeginEditing:(UITextView *)textView {
    self.liveTitlePlaceHolder.alpha = 0.0;
    self.liveTitleEditButton.alpha = 0.0;
}

- (void)textViewDidEndEditing:(UITextView *)textView {
    if (self.liveTitleTextView.text.length > 0) {
        self.liveTitlePlaceHolder.alpha = 0.0;
    } else {
        self.liveTitlePlaceHolder.alpha = 1.0;
        self.liveTitleEditButton.alpha = 1.0;
    }
}

- (BOOL)textView:(UITextView *)textView shouldChangeTextInRange:(NSRange)range replacementText:(NSString *)text {
    if (range.location >= self.liveTitleMaxLength) {
        return NO;
    }
    if ([text isEqualToString:@"\n"]) {
        [self.liveTitleTextView resignFirstResponder];
        return NO;
    }
    return YES;
}

- (void)textViewDidChangeSelection:(UITextView *)textView {
    self.liveTitleCharactorsCountLabel.text = [NSString stringWithFormat:@"%lu/%lu", textView.text.length, self.liveTitleMaxLength];
}
@end
