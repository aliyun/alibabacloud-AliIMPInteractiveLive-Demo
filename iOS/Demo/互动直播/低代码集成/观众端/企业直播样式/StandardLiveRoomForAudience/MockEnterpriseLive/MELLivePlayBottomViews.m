//
//  MELLivePlayBottomViews.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/6.
//

#import "MELLivePlayBottomViews.h"
#import <Masonry/Masonry.h>
#import "MELLikeButton.h"
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"

extern NSString * const kMELYourCommentBannedNotification;
extern NSString * const kMELAllCommentBannedNotification;

@implementation MELLivePlayBottomViews

- (UITextField*)sendCommentField {
    if (!_sendCommentField) {
        _sendCommentField = [[UITextField alloc] init];
        [self addSubview:_sendCommentField];
        [_sendCommentField mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(16);
            make.top.equalTo(self.mas_top).with.offset(9);
            make.size.mas_equalTo(CGSizeMake(205, 36));
        }];
        _sendCommentField.layer.masksToBounds = YES;
        _sendCommentField.layer.cornerRadius = 20;
        _sendCommentField.textColor = [UIColor blackColor];
//        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说些什么吧…"
//                                                                         attributes:@{
//                                                                             NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#F6F6F6" alpha:1.0] ,
//                                                                             NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
//                                                                         }];
//        _sendCommentField.attributedPlaceholder = attrString;
        _sendCommentField.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.2];
        _sendCommentField.textAlignment = NSTextAlignmentLeft;
        _sendCommentField.keyboardType = UIKeyboardTypeDefault;
        _sendCommentField.returnKeyType = UIReturnKeySend;
        _sendCommentField.keyboardAppearance = UIKeyboardAppearanceDefault;
        _sendCommentField.delegate = self;
        _sendCommentField.borderStyle = UITextBorderStyleRoundedRect;
        [_sendCommentField setContentHuggingPriority:UILayoutPriorityRequired forAxis:UILayoutConstraintAxisHorizontal];
    }
    return _sendCommentField;
}

- (UIButton*)giftButton {
    if (!_giftButton) {
        _giftButton = [[UIButton alloc] init];
        [_giftButton addTarget:self action:@selector(onGiftButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_giftButton];
        [_giftButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.mas_right).with.offset(-16);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
        [_giftButton setImage:[UIImage imageNamed:@"企业直播-礼物-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _giftButton;
}

- (MELLikeButton*)likeButton {
    if (!_likeButton) {
        _likeButton = [[MELLikeButton alloc] init];
        [_likeButton addTarget:self action:@selector(onLikeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_likeButton];
        [_likeButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.giftButton.mas_left).with.offset(-6);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
        [_likeButton setImage:[UIImage imageNamed:@"企业直播-点赞-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _likeButton;
}

- (UIButton*)shareButton {
    if (!_shareButton) {
        _shareButton = [[UIButton alloc] init];
        [_shareButton addTarget:self action:@selector(onShareButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_shareButton];
        [_shareButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.likeButton.mas_left).with.offset(-6);
            make.top.equalTo(self.mas_top).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
        [_shareButton setImage:[UIImage imageNamed:@"企业直播-分享-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _shareButton;
}

- (void)setStatus:(MELLiveRoomBottomViewStatus)status {
    _status = status;
    switch (status) {
        case MELLiveRoomBottomViewStatusLiveNotStarted:{
            [self p_setDisableStyle:YES];
        }
            break;
            
        case MELLiveRoomBottomViewStatusLiveStarted: {
            [self p_setDisableStyle:NO];
            [self p_handleLiveCommentBannedStatusChangeNotification];
        }
            break;
            
        default:
            break;
    }
}

- (void)setLikeCount:(int32_t)likeCount {
    self.likeButton.likeCountLabel.text = [NSString stringWithFormat:@"%d", likeCount];
}

#pragma mark Lifecycle

-(instancetype)initWithStatus:(MELLiveRoomBottomViewStatus)status {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveCustomNotification:) name:kMELYourCommentBannedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveCustomNotification:) name:kMELAllCommentBannedNotification object:nil];
        
        self.backgroundColor = [UIColor whiteColor];
        
        self.status = status;
    }
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark Private Methods

-(void)p_setDisableStyle:(BOOL)disable {
    
    NSString* shareButtonImageName = @"企业直播-分享-可选";
    NSString* giftButtonImageName = @"企业直播-礼物-可选";
    NSString* likeButtonImageName = @"企业直播-点赞-可选";
    UIColor* sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#333333" alpha:0.8];
    self.likeButton.likeCountLabel.hidden = NO;
    
    if (disable) {
        shareButtonImageName = @"企业直播-分享-不可选";
        giftButtonImageName = @"企业直播-礼物-不可选";
        likeButtonImageName = @"企业直播-点赞-不可选";
        sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#999999" alpha:0.8];
        self.likeButton.likeCountLabel.hidden = YES;
    }
    
    NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说些什么吧…"
                                                                     attributes:@{
                                                                         NSForegroundColorAttributeName:sendFieldPlaceHolderColor ,
                                                                         NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
                                                                     }];
    self.sendCommentField.attributedPlaceholder = attrString;
    self.sendCommentField.userInteractionEnabled = !disable;
    
    [self.shareButton setImage:[UIImage imageNamed:shareButtonImageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    self.shareButton.userInteractionEnabled = !disable;
    
    [self.likeButton setImage:[UIImage imageNamed:likeButtonImageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    self.likeButton.userInteractionEnabled = !disable;
    
    [self.giftButton setImage:[UIImage imageNamed:giftButtonImageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    self.giftButton.userInteractionEnabled = !disable;
}

- (void)p_handleLiveCommentBannedStatusChangeNotification {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (_status == MELLiveRoomBottomViewStatusLiveNotStarted) {
            return;
        }
        if (_allCommentBanned || _yourCommentBanned) {
            NSString* text = @"主播已开启全员禁言";
            if (_yourCommentBanned && !_allCommentBanned) {
                text = @"您已被主播禁言";
            }
            UIColor* sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#999999" alpha:0.8];
            NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:text
                                                                             attributes:@{
                                                                                 NSForegroundColorAttributeName:sendFieldPlaceHolderColor ,
                                                                                 NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
                                                                             }];
            self.sendCommentField.attributedPlaceholder = attrString;
            self.sendCommentField.userInteractionEnabled = NO;
            self.sendCommentField.text = nil;
        } else {
            UIColor* sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#333333" alpha:0.8];
            NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说些什么吧…"
                                                                             attributes:@{
                                                                                 NSForegroundColorAttributeName:sendFieldPlaceHolderColor ,
                                                                                 NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
                                                                             }];
            self.sendCommentField.attributedPlaceholder = attrString;
            self.sendCommentField.userInteractionEnabled = YES;
        }
    });
}

#pragma mark UIButton Selectors
- (void)onGiftButtonClicked {
    self.onGiftSent();
}

- (void)onLikeButtonClicked {
    self.onLikeSent();
    
    [self likeAnimation];
}

- (void)onShareButtonClicked {
    self.onShare();
}

#pragma mark UITextField
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.sendCommentField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    if (textField.text.length > 0) {
        self.onCommentSent(textField.text);
    }
    self.sendCommentField.text = nil;
    return YES;
}
#pragma mark --NSNotification

- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    CGFloat keyBoardHeight = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size.height;
    
    if(self.sendCommentField.isEditing){
        self.sendCommentField.layer.cornerRadius = 2;
        self.sendCommentField.backgroundColor = [UIColor whiteColor];
        self.sendCommentField.textColor = [UIColor blackColor];
        [self.sendCommentField mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self);
            make.right.equalTo(self);
            make.bottom.equalTo(self.superview.superview.superview.superview.superview).offset(-keyBoardHeight); //这里注意是从手机屏幕的最底部开始算（包括安全区）
            make.height.mas_equalTo(40);
        }];
        [self layoutIfNeeded];
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    self.sendCommentField.transform = CGAffineTransformIdentity;
    self.sendCommentField.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.2];
    self.sendCommentField.layer.cornerRadius = 20;
    self.sendCommentField.textColor = [UIColor whiteColor];
    [self.sendCommentField mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.mas_left).with.offset(16);
        make.top.equalTo(self.mas_top).with.offset(9);
        make.size.mas_equalTo(CGSizeMake(205, 36));
    }];
    [self layoutIfNeeded];
}

-(void)onReceiveCustomNotification:(NSNotification*)notification {
    if ([notification.name isEqualToString:kMELAllCommentBannedNotification]) {
        _allCommentBanned = [[notification.userInfo valueForKey:@"ban"] boolValue];
        [self p_handleLiveCommentBannedStatusChangeNotification];
    } else if ([notification.name isEqualToString:kMELYourCommentBannedNotification]) {
        _yourCommentBanned = [[notification.userInfo valueForKey:@"ban"] boolValue];
        [self p_handleLiveCommentBannedStatusChangeNotification];
    }
}

#pragma mark Like Animation
- (void) likeAnimation {
    
    UIWindow * window = [[[UIApplication sharedApplication] delegate] window];
    CGRect rect = [self.likeButton convertRect:self.likeButton.bounds toView:window];
    
    //在主线程上调用该方法
    UIImageView *imageView = [[UIImageView alloc] init];
//    CGRect frame = self.superview.superview.superview.frame;
    // 初始frame，即设置了动画的起点
    imageView.frame = rect;
    // 初始化imageView透明度为0
    imageView.alpha = 0;
    imageView.backgroundColor = [UIColor clearColor];
    imageView.clipsToBounds = YES;
    // 用0.2秒的时间将imageView的透明度变成1.0，同时将其放大1.3倍，再缩放至1.1倍，这里参数根据需求设置
    [UIView animateWithDuration:0.4 animations:^{
        imageView.alpha = 1.0;
        CGAffineTransform transfrom = CGAffineTransformMakeScale(1.3,1.3);
        imageView.transform = CGAffineTransformScale(transfrom,1,1);
    }];
    [window addSubview:imageView];
    // 随机产生一个动画结束点的X值
    CGFloat finishX = rect.origin.x + (100 - round(random() % 200));
    // 动画结束点的Y值
    CGFloat finishY = (arc4random() % 150)+ 50;
    // imageView在运动过程中的缩放比例
    CGFloat scale = round(random() % 2) + 0.7;
    // 生成一个作为速度参数的随机数
    CGFloat speed = 1 / round(random() % 900) +0.6;
    // 动画执行时间
    NSTimeInterval duration = 4 * speed;
    // 如果得到的时间是无穷大，就重新附一个值（这里要特别注意，请看下面的特别提醒）
    if(duration == INFINITY) duration = 2.412346;
    // 开始动画
    [UIView beginAnimations:nil context:(__bridge void *_Nullable)(imageView)];
    // 设置动画时间
    [UIView setAnimationDuration:duration];
    // 拼接图片名字
    
    NSString* imageName = [NSString stringWithFormat:@"ilr_icon_like_clicked_%d", random() % 5];
    imageView.image = [UIImage imageNamed:imageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
    // 设置imageView的结束frame
    imageView.frame = CGRectMake(finishX, finishY,30* scale,30* scale);
    // 设置渐渐消失的效果，这里的时间最好和动画时间一致
    [UIView animateWithDuration:duration animations:^{
        imageView.alpha = 0;
    }];
    // 结束动画，调用onAnimationComplete:finished:context:函数
    [UIView setAnimationDidStopSelector:@selector(onAnimationComplete:finished:context:)];
    // 设置动画代理
    [UIView setAnimationDelegate:self];
    [UIView commitAnimations];
}

- (void)onAnimationComplete:(NSString*)animationID finished:(NSNumber*)finished context:(void*)context{
    UIImageView *imageView = (__bridge UIImageView*)(context);
    [imageView removeFromSuperview];
    imageView = nil;
}

@end
