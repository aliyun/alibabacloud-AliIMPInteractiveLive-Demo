//
//  MELLiveRoomLandscapeViewController.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/10.
//

#import "MELLiveRoomLandscapeViewController.h"
#import "MELLandscapeLiveCommentTableView.h"
#import "MELLikeButton.h"
#import "MELLiveInfoViewHolder.h"
#import "ASLUKResourceManager.h"
#import "UIColor+ColorWithHexString.h"
#import <Masonry/Masonry.h>

#define isIphoneX ({ \
BOOL isPhoneX = NO; \
if (@available(iOS 11.0, *)) { \
    if (!UIEdgeInsetsEqualToEdgeInsets([UIApplication sharedApplication].delegate.window.safeAreaInsets, UIEdgeInsetsZero)) { \
    isPhoneX = YES; \
    } \
} \
isPhoneX; \
}) \

extern NSString * const kMELYourCommentBannedNotification;
extern NSString * const kMELAllCommentBannedNotification;

@interface MELLiveRoomLandscapeViewController ()
@property (strong, nonatomic) MELLandscapeLiveCommentTableView* liveCommentView;
@property (strong, nonatomic) UIButton* backButton;
@property (strong, nonatomic) UILabel* liveTitleLabel;
@property (strong, nonatomic) UIButton* giftButton;
@property (strong, nonatomic) UITextField* commentSender;
@property (assign, nonatomic) BOOL allCommentBanned;
@property (assign, nonatomic) BOOL yourCommentBanned;
@end

@implementation MELLiveRoomLandscapeViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self.view bringSubviewToFront:self.liveCommentView];
    [self.view bringSubviewToFront:self.giftButton];
    [self.view bringSubviewToFront:self.likeButton];
    [self.view bringSubviewToFront:self.liveTitleLabel];
    [self.view bringSubviewToFront:self.commentSender];
}

// 旋转的方向
- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskAllButUpsideDown;
}

- (MELLandscapeLiveCommentTableView*)liveCommentView {
    if (!_liveCommentView) {
        _liveCommentView = [[MELLandscapeLiveCommentTableView alloc] init];
        [self.view addSubview:_liveCommentView];
        
        [_liveCommentView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(81);
            } else {
                make.left.equalTo(self.view).with.offset(81);
            }
            make.bottom.equalTo(self.commentSender.mas_top).with.offset(-6);
            make.size.mas_equalTo(CGSizeMake(248, 174));
        }];
    }
    [_liveCommentView startPresenting];
    return _liveCommentView;
}

- (UIButton*)giftButton {
    if (!_giftButton) {
        _giftButton = [[UIButton alloc] init];
        [_giftButton addTarget:self action:@selector(onGiftButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:_giftButton];
        [_giftButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.view.mas_right).with.offset(-74);
            make.bottom.equalTo(self.view).with.offset(-21);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
//        [_giftButton setImage:[UIImage imageNamed:@"企业直播-礼物-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _giftButton;
}

- (MELLikeButton*)likeButton {
    if (!_likeButton) {
        _likeButton = [[MELLikeButton alloc] init];
        _likeButton.likeCountLabel.hidden = NO;
        [_likeButton addTarget:self action:@selector(onLikeButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [self.view addSubview:_likeButton];
        [_likeButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.right.equalTo(self.giftButton.mas_left).with.offset(-6);
            make.bottom.equalTo(self.view).with.offset(-21);
            make.size.mas_equalTo(CGSizeMake(38, 38));
        }];
//        [_likeButton setImage:[UIImage imageNamed:@"企业直播-点赞-可选" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    }
    return _likeButton;
}

- (UITextField*)commentSender {
    if (!_commentSender) {
        _commentSender = [[UITextField alloc] init];
        [self.view addSubview:_commentSender];
        [_commentSender mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.view.mas_left).with.offset(81);
            make.bottom.equalTo(self.view.mas_bottom).with.offset(-21);
            make.size.mas_equalTo(CGSizeMake(272, 38));
        }];
        _commentSender.layer.masksToBounds = YES;
        _commentSender.layer.cornerRadius = 20;
        _commentSender.textColor = [UIColor blackColor];
//        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说些什么吧…"
//                                                                         attributes:@{
//                                                                             NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#333333" alpha:1.0],
//                                                                             NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
//                                                                         }];
//        _commentSender.attributedPlaceholder = attrString;
        _commentSender.backgroundColor = [UIColor colorWithHexString:@"#F6F6F6" alpha:1.0];
        _commentSender.textAlignment = NSTextAlignmentLeft;
        _commentSender.keyboardType = UIKeyboardTypeDefault;
        _commentSender.returnKeyType = UIReturnKeySend;
        _commentSender.keyboardAppearance = UIKeyboardAppearanceDefault;
        _commentSender.delegate = self;
        _commentSender.borderStyle = UITextBorderStyleRoundedRect;
        [_commentSender setContentHuggingPriority:UILayoutPriorityRequired forAxis:UILayoutConstraintAxisHorizontal];
    }
    return _commentSender;
}

- (UIButton*)backButton {
    if (!_backButton) {
        _backButton = [[UIButton alloc] init];
        [_backButton addTarget:self action:@selector(onBackButtonClicked) forControlEvents:UIControlEventTouchUpInside];
        [_backButton setImage:[UIImage imageNamed:@"横屏-返回" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [self.view addSubview:_backButton];
        [_backButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(24, 24));
            make.left.equalTo(self.view.mas_left).with.offset(60);
            make.top.equalTo(self.view).with.offset(28);
        }];
    }
    return _backButton;
}

- (UILabel*)liveTitleLabel {
    if (!_liveTitleLabel) {
        _liveTitleLabel = [[UILabel alloc] init];
        _liveTitleLabel.textAlignment = NSTextAlignmentLeft;
        _liveTitleLabel.textColor = [UIColor whiteColor];
        [self.view addSubview:_liveTitleLabel];
        _liveTitleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:15];
        [_liveTitleLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.backButton.mas_right).with.offset(8);
            make.size.mas_equalTo(CGSizeMake(300, 24));
            make.top.equalTo(self.view.mas_top).with.offset(28);
        }];
    }
    return _liveTitleLabel;
}

- (void)setLikeCount:(int32_t)likeCount {
    self.likeButton.likeCountLabel.text = [NSString stringWithFormat:@"%d", likeCount];
}

- (void)setStatus:(MELLiveRoomLandscapeViewStatus)status {
    _status = status;
    switch (status) {
        case MELLiveRoomLandscapeViewStatusStatusLiveNotStarted:
            [self p_setDisableStyle:YES];
            break;
        case MELLiveRoomLandscapeViewStatusStatusLiveStarted: {
            [self p_setDisableStyle:NO];
            [self p_handleLiveCommentBannedStatusChangeNotification];
            break;
        }
        default:
            break;
    }
}
- (void)setLiveTitle:(NSString *)liveTitle {
    self.liveTitleLabel.text = liveTitle;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveCustomNotification:) name:kMELYourCommentBannedNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(onReceiveCustomNotification:) name:kMELAllCommentBannedNotification object:nil];
    }
    return self;
}

- (void)dealloc {
    [_liveCommentView stopPresenting];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)insertLiveComment:(ASLRBLiveCommentModel *)model {
    [_liveCommentView insertNewComment:model presentedCompulsorily:YES];
}

-(void)p_setDisableStyle:(BOOL)disable {
    
    NSString* giftButtonImageName = @"企业直播-礼物-可选";
    NSString* likeButtonImageName = @"企业直播-点赞-可选";
    UIColor* sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#333333" alpha:0.8];
    self.likeButton.likeCountLabel.hidden = NO;
    
    if (disable) {
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
    self.commentSender.attributedPlaceholder = attrString;
    self.commentSender.userInteractionEnabled = !disable;
    
    [self.likeButton setImage:[UIImage imageNamed:likeButtonImageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    self.likeButton.userInteractionEnabled = !disable;
    
    [self.giftButton setImage:[UIImage imageNamed:giftButtonImageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    self.giftButton.userInteractionEnabled = !disable;
}

- (void)p_handleLiveCommentBannedStatusChangeNotification {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (_status == MELLiveRoomLandscapeViewStatusStatusLiveNotStarted) {
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
            self.commentSender.attributedPlaceholder = attrString;
            self.commentSender.userInteractionEnabled = NO;
            self.commentSender.text = nil;
        } else {
            UIColor* sendFieldPlaceHolderColor = [UIColor colorWithHexString:@"#333333" alpha:0.8];
            NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说些什么吧…"
                                                                             attributes:@{
                                                                                 NSForegroundColorAttributeName:sendFieldPlaceHolderColor ,
                                                                                 NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:14]
                                                                             }];
            self.commentSender.attributedPlaceholder = attrString;
            self.commentSender.userInteractionEnabled = YES;
        }
    });
}

#pragma mark --NSNotification

- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    CGFloat keyBoardHeight = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue].size.height;
    
    if(self.commentSender.isEditing){
        self.commentSender.layer.cornerRadius = 2;
        self.commentSender.backgroundColor = [UIColor whiteColor];
        self.commentSender.textColor = [UIColor blackColor];
        
        UIWindow * window = [[[UIApplication sharedApplication] delegate] window];
        [self.commentSender mas_remakeConstraints:^(MASConstraintMaker *make) {
            
            int offset = 0;
            if (isIphoneX) {
                offset = 50;
            }
            
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(offset);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight).with.offset(-1 * offset);
            } else {
                make.left.equalTo(self.view).with.offset(offset);
                make.right.equalTo(self.view).with.offset(-1 * offset);
            }
            
            make.bottom.equalTo(window).offset(-keyBoardHeight); //这里注意是从手机屏幕的最底部开始算（包括安全区）
            make.height.mas_equalTo(40);
        }];
        [self.view layoutIfNeeded];
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    self.commentSender.transform = CGAffineTransformIdentity;
    self.commentSender.backgroundColor = [UIColor colorWithHexString:@"#F6F6F6" alpha:1.0];
    self.commentSender.layer.cornerRadius = 20;
    self.commentSender.textColor = [UIColor blackColor];
    [self.commentSender mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.view.mas_left).with.offset(81);
        make.bottom.equalTo(self.view.mas_bottom).with.offset(-21);
        make.size.mas_equalTo(CGSizeMake(272, 38));
    }];
    [self.view layoutIfNeeded];
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

#pragma mark UITextField
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.commentSender resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    if (textField.text.length > 0) {
        self.onCommentSent(textField.text);
    }
    self.commentSender.text = nil;
    return YES;
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

#pragma mark UIButton Selectors
- (void)onGiftButtonClicked {
    self.onGiftSend();
}

- (void)onLikeButtonClicked {
    self.onLikeSend();
    
    [self likeAnimation];
}

- (void)onBackButtonClicked {
    self.onBack();
}

@end
