//
//  SECLRABottomViewHolder.m
//  AliLiveRoomUIForAudience
//
//  Created by fernando on 2022/5/9.
//

#import "SECLRABottomViewHolder.h"
#import <Masonry/Masonry.h>
#import "ASLUKResourceManager.h"
#import "UIColor+ColorWithHexString.h"

@interface SECLRABottomViewHolder()<UITextFieldDelegate>
@property (strong, nonatomic) UILabel* likeCountLabel;
@end

@implementation SECLRABottomViewHolder

/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

- (UIButton*)goodsButton {
    if (!_goodsButton) {
        _goodsButton = [[UIButton alloc] init];
        [_goodsButton addTarget:self action:@selector(onGoodsButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_goodsButton];
        [_goodsButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.mas_left).with.offset(17.3);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.size.mas_equalTo(CGSizeMake(33.3, 35));
        }];
        [_goodsButton setImage:[UIImage imageNamed:@"互动区-商品口袋" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _goodsButton;
}

- (UIButton*)likeButton {
    if (!_likeButton) {
        _likeButton = [[UIButton alloc] init];
        [_likeButton addTarget:self action:@selector(onLikeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_likeButton];
        [_likeButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.mas_right).with.offset(-14);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.size.mas_equalTo(CGSizeMake(36, 36));
        }];
        [_likeButton setImage:[UIImage imageNamed:@"互动区-点赞" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _likeButton;
}

- (UIButton*)moreButton {
    if (!_moreButton) {
        _moreButton = [[UIButton alloc] init];
        [_moreButton addTarget:self action:@selector(onMoreButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_moreButton];
        [_moreButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.floatingWindowButton.mas_left).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.size.mas_equalTo(CGSizeMake(36, 36));
        }];
        [_moreButton setImage:[UIImage imageNamed:@"互动区-主播互动" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _moreButton;
}

- (UIButton*)shareButton {
    if (!_shareButton) {
        _shareButton = [[UIButton alloc] init];
        [_shareButton addTarget:self action:@selector(onShareButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_shareButton];
        [_shareButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.likeButton.mas_left).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.size.mas_equalTo(CGSizeMake(36, 36));
        }];
        [_shareButton setImage:[UIImage imageNamed:@"互动区-分享" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _shareButton;
}

- (UIButton*)floatingWindowButton {
    if (!_floatingWindowButton) {
        _floatingWindowButton = [[UIButton alloc] init];
        [_floatingWindowButton addTarget:self action:@selector(onFloatingWindowButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self addSubview:_floatingWindowButton];
        [_floatingWindowButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.shareButton.mas_left).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.size.mas_equalTo(CGSizeMake(36, 36));
        }];
        [_floatingWindowButton setImage:[UIImage imageNamed:@"互动区-开启小窗口" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return  _floatingWindowButton;
}

- (UILabel*)likeCountLabel {
    if (!_likeCountLabel) {
        _likeCountLabel = [[UILabel alloc] init];
        _likeCountLabel.text = @"0";
        _likeCountLabel.backgroundColor = [UIColor whiteColor];
        _likeCountLabel.layer.cornerRadius = 8;
        _likeCountLabel.layer.masksToBounds = YES;
        _likeCountLabel.textColor = [UIColor colorWithHexString:@"#FF5722" alpha:1.0];
        _likeCountLabel.textAlignment = NSTextAlignmentCenter;
        _likeCountLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        [self.likeButton addSubview:_likeCountLabel];
        [_likeCountLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.bottom.equalTo(self.likeButton.mas_bottom).with.offset(-32);
            make.centerX.equalTo(self.likeButton.mas_centerX);
            make.size.mas_equalTo(CGSizeMake(36, 16));
        }];
    }
    return _likeCountLabel;
}

- (UITextField*)sendCommentField {
    if (!_sendCommentField) {
        _sendCommentField = [[UITextField alloc] init];
        [self addSubview:_sendCommentField];
        [_sendCommentField mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.goodsButton.mas_right).with.offset(7.33);
            make.right.equalTo(self.moreButton.mas_left).with.offset(-4);
            make.top.equalTo(self.mas_top).with.offset(12);
            make.height.mas_equalTo(36);
        }];
        _sendCommentField.layer.masksToBounds = YES;
        _sendCommentField.layer.cornerRadius = 20;
        _sendCommentField.textColor = [UIColor blackColor];
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说点什么…"
                                                                         attributes:@{
                                                                             NSForegroundColorAttributeName:[UIColor colorWithWhite:1 alpha:0.8],
                                                                             NSFontAttributeName:[UIFont systemFontOfSize:14]
                                                                         }];
        _sendCommentField.attributedPlaceholder = attrString;
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

- (void)setLikeCount:(int32_t)likeCount {
    _likeCount = likeCount;
    if ([[NSThread currentThread] isMainThread]) {
        self.likeCountLabel.text = [NSString stringWithFormat:@"%d", likeCount];
        [self likeAnimation];
    } else {
        dispatch_async(dispatch_get_main_queue(), ^{
            self.likeCountLabel.text = [NSString stringWithFormat:@"%d", likeCount];
            [self likeAnimation];
        });
    }
}

#pragma mark Lifecycle

- (instancetype)init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
        
        [self bringSubviewToFront:self.likeButton];
        [self.likeButton bringSubviewToFront:self.likeCountLabel];
        [self bringSubviewToFront:self.goodsButton];
        [self bringSubviewToFront:self.sendCommentField];
        [self bringSubviewToFront:self.shareButton];
        [self bringSubviewToFront:self.floatingWindowButton];
        [self bringSubviewToFront:self.moreButton];
    }
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark UIButton

- (void) onGoodsButtonClicked {
    [self.delegate onGoodsButtonClicked];
}

- (void) onLikeButtonClicked {
    [self.delegate onLikeButtonClicked];
    
    [self likeAnimation];
}

- (void) onShareButtonClicked {
    [self.delegate onShareButtonClicked];
}

- (void) onFloatingWindowButtonClicked {
    [self.delegate onFloatingWindowButtonClicked];
}

- (void) onMoreButtonClicked {
    [self.delegate onMoreButtonClicked];
}

#pragma mark UITextField
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.sendCommentField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收

    if (textField.text.length > 0) {
        [self.delegate onCommentSent:textField.text];
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
            make.bottom.equalTo(self.superview.superview).offset(-keyBoardHeight); //这里注意是从手机屏幕的最底部开始算（包括安全区）
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
        make.left.equalTo(self.goodsButton.mas_right).with.offset(7.33);
        make.right.equalTo(self.moreButton.mas_left).with.offset(-4);
        make.top.equalTo(self.mas_top).with.offset(12);
        make.height.mas_equalTo(36);
    }];
    [self layoutIfNeeded];
}

#pragma mark Like Animation
- (void) likeAnimation {
    
    //在主线程上调用该方法
    UIImageView *imageView = [[UIImageView alloc] init];
    CGRect frame = self.superview.superview.frame;
    // 初始frame，即设置了动画的起点
    imageView.frame = CGRectMake(self.likeButton.frame.origin.x + self.frame.origin.x + self.superview.frame.origin.x, self.likeButton.frame.origin.y + self.frame.origin.y + self.superview.frame.origin.y, 30,30);
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
    [self.superview.superview addSubview:imageView];
    // 随机产生一个动画结束点的X值
    CGFloat finishX = frame.size.width - round(random() % 200);
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
