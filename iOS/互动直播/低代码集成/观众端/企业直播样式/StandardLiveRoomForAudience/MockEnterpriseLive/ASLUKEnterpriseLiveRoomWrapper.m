//
//  ASLUKLiveRoomWrapper.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/17.
//

#import "ASLUKEnterpriseLiveRoomWrapper.h"
#import <Masonry/Masonry.h>
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"
#import "MELHeaderViewHolder.h"
#import "MELVideoPlayViewsHolder.h"
#import "MELInteractionAreaViewController.h"
#import "MELLiveRoomLandscapeViewController.h"
#import "MELGiftsViewController.h"
#import "MELShareViewController.h"

const NSString* kMELCustomMessageActionSendGift = @"sendGift";
NSString * const kMELYourCommentBannedNotification = @"kMELYourCommentBannedNotification";
NSString * const kMELAllCommentBannedNotification = @"kMELAllCommentBannedNotification";

@interface ASLUKEnterpriseLiveRoomWrapper() <ASLRBLiveRoomViewControllerDelegate, ASLRBLiveCommentViewDelegate>
@property (nonatomic, strong) ASLRBLiveRoomViewController* liveRoomVC;
@property (nonatomic, strong) MELHeaderViewHolder* headerViewHolder;
@property (nonatomic, strong) MELVideoPlayViewsHolder* videoPlayBackground;
@property (nonatomic, strong) MELInteractionAreaViewController* liveInteractionVC;
@property (nonatomic, strong) MELLiveRoomLandscapeViewController* landscapeLiveRoomVC;
@property (nonatomic, strong) MELGiftsViewController* giftsVC;
@property (nonatomic, strong) MELShareViewController* shareVC;

@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* anchorNick;
@property (copy, nonatomic) NSString* anchorAvatarURL;
@property (copy, nonatomic) NSString* anchorIntroduction;
@property (copy, nonatomic) NSString* liveIntroduction;
@property (assign, nonatomic) int32_t currentPV;
@property (assign, nonatomic) int32_t currentLiveStatus;
@property (copy, nonatomic) NSString* livePrestartTimestamp;

@property (copy, nonatomic) void(^onSetupLiveRoomSuccess)(UIViewController* liveRoomViewController);
@property (copy, nonatomic) void(^onSetupLiveRoomFailure)(NSString* errorMessage);
@end

@implementation ASLUKEnterpriseLiveRoomWrapper

#pragma mark -Lifecycle

- (instancetype) init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleStatusBarOrientationDidChange:) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
    }
    
    return self;
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark -Public Methods

- (void)setupLiveRoomWithAppID:(NSString*)appID
                        appKey:(NSString*)appKey
                  appServerUrl:(NSString*)serverUrl
               appServerSecret:(NSString*)secret
                        liveID:(NSString*)liveID
                        userID:(NSString*)userID
                      userNick:(NSString*)userNick
                     onSuccess:(void(^)(UIViewController* liveRoomViewController))onSuccess
                     onFailure:(void(^)(NSString* errorMessage))onFailure {
    
    _liveID = liveID;
    _userID = userID;
    _userNick = userNick;
    
    _onSetupLiveRoomFailure = onFailure;
    _onSetupLiveRoomSuccess = onSuccess;
    
    __weak typeof(self) weakSelf = self;
    [[ASLRBLiveRoomManager sharedInstance] globalInitOnceWithConfig:({
        ASLRBAppInitConfig* config = [[ASLRBAppInitConfig alloc] init];
        config.appID = appID;
        config.appKey = appKey;
        config.appServerUrl = serverUrl;
        config.appServerSignSecret = secret;
        config.userID = weakSelf.userID;
        config.userNick = weakSelf.userNick;
        config;
    }) onSuccess:^{
        [weakSelf p_createLiveRoom];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        weakSelf.onSetupLiveRoomFailure(errorMessage);
    }];
}

- (void) destroyLiveRoom {
    [self.liveRoomVC enterFloatingMode:NO];
    [self.liveRoomVC exitLiveRoom];
}

#pragma mark -Private Methods
- (void) p_createLiveRoom {
    __weak typeof(self) weakSelf = self;
    [[ASLRBLiveRoomManager sharedInstance] createLiveRoomVCWithConfig:({
        ASLRBLiveInitConfig* config = [[ASLRBLiveInitConfig alloc] init];
        config.liveID = weakSelf.liveID;
        config.role = ASLRBUserRoleAudience;
        //隐藏默认的直播弹幕实现
        config.liveCommentViewsConfig.liveCommentViewHidden = YES;
        config.liveCommentViewsConfig.countOfHistoryCommentsWhenEntered = 100;
        config.middleViewsConfig.liveNoticeButtonHidden = YES;
        config.middleViewsConfig.livePlayLoadingIndicatorHidden = YES;
        config;
    }) onSuccess:^(ASLRBLiveRoomViewController * _Nonnull liveRoomVC) {
        [weakSelf p_setupLiveRoomWithVC:liveRoomVC];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        weakSelf.onSetupLiveRoomFailure(errorMessage);
    }];
}

- (void)p_setupLiveRoomWithVC:(ASLRBLiveRoomViewController*)liveRoomVC {
    __weak typeof(self) weakSelf = self;
    [liveRoomVC setupOnSuccess:^(NSString * _Nonnull liveID) {
        weakSelf.liveRoomVC = liveRoomVC;
        weakSelf.liveRoomVC.modalPresentationStyle = UIModalPresentationFullScreen;
        weakSelf.liveRoomVC.delegate = weakSelf;
        [weakSelf p_customizeLiveRoomVC];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        weakSelf.onSetupLiveRoomFailure(errorMessage);
    }];
}

- (void)p_customizeLiveRoomVC {
    
    self.liveRoomVC.floatingPlayWindow.disappearAfterResignActive = NO;
    
    [self.liveRoomVC getLiveDetail:^(NSDictionary * _Nonnull detail) {
        _currentPV = [[detail valueForKey:@"pv"] intValue];
        _liveTitle = [detail valueForKey:@"title"];
        _anchorNick = [detail valueForKey:@"anchor_nick"];
        _currentLiveStatus = [[detail valueForKey:@"status"] intValue];
        
        //获取直播扩展字段
        NSDictionary* customData = [detail valueForKey:@"extension"];
        
        //从直播自定义字段里获取当前主播的头像
        _anchorAvatarURL = [customData valueForKey:@"anchorAvatarURL"];
        _anchorIntroduction = [customData valueForKey:@"anchorIntroduction"];
        _liveIntroduction = [customData valueForKey:@"liveIntroduction"];
        _livePrestartTimestamp = [customData valueForKey:@"preStartTime"];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            // 自定义直播间顶部区域
            [self p_customizeHeaderViews];
            
            // 自定义直播间播放区域
            [self p_customizeVideoPlayArea];
            
            // 自定义直播间互动区域
            [self p_customizeInteractionArea];
            
            // 自定义直播间互动区域
            [self p_customizeLandscapeViews];
            
//            [self presentViewController:self.liveRoomVC animated:NO completion:nil];
            _onSetupLiveRoomSuccess(self.liveRoomVC);
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        _onSetupLiveRoomFailure(errorMessage);
    }];
}

// 自定义直播间顶部区域
-(void) p_customizeHeaderViews {
    
    // 先移除默认的左上角的主播头像和右上角的关闭直播间按钮
    [self.liveRoomVC.liveInfoViewHolder removeFromSuperview];
    [self.liveRoomVC.exitButton removeFromSuperview];
    
    _headerViewHolder = [[MELHeaderViewHolder alloc] init];
    _headerViewHolder.backgroundColor = [UIColor whiteColor];
    [self.liveRoomVC.view addSubview:_headerViewHolder];
    
    [_headerViewHolder mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.equalTo(self.liveRoomVC.view);
        make.right.equalTo(self.liveRoomVC.view);
        make.top.equalTo(self.liveRoomVC.view);
        make.height.mas_equalTo(88);
    }];
    
    __weak typeof(self) weakSelf = self;
    _headerViewHolder.onExit = ^{
        [weakSelf.liveRoomVC exitLiveRoom];
        [weakSelf.liveRoomVC.navigationController popViewControllerAnimated:YES];
        weakSelf.liveRoomVC = nil;
    };
    
    _headerViewHolder.liveTitleLabel.text = _liveTitle;
}

-(void) p_customizeVideoPlayArea {
    
    _videoPlayBackground = [[MELVideoPlayViewsHolder alloc] init];
    
    __weak typeof(self) weakSelf = self;
    _videoPlayBackground.onReload = ^{
        [weakSelf.liveRoomVC refreshPlayer];
        weakSelf.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadingStart;
    };
    _videoPlayBackground.onLandscape = ^{
        [weakSelf switchOrientiation:UIInterfaceOrientationLandscapeRight];
    };
    
    self.videoPlayBackground.portraitHeight = [UIScreen mainScreen].bounds.size.width / 1.78;
    [self.liveRoomVC.view addSubview:_videoPlayBackground];
    [_videoPlayBackground mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.equalTo(self.liveRoomVC.view);
        make.right.equalTo(self.liveRoomVC.view);
        make.height.mas_equalTo(self.videoPlayBackground.portraitHeight);
        make.top.equalTo(self.headerViewHolder.mas_bottom);
    }];
    
    _videoPlayBackground.anchorAvartarImageURL = [NSURL URLWithString:_anchorAvatarURL];
    _videoPlayBackground.anchorNick = _anchorNick;
    [_videoPlayBackground updateLivePV:_currentPV];
    
    if (_currentLiveStatus == 0){
        _videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLiveNotStarted;
    } else if (_currentLiveStatus == 1) {
        _videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadingStart;
    }
    _videoPlayBackground.livePrestartTimestamp = _livePrestartTimestamp;
}

-(void) p_customizeInteractionArea {
    
    //配置接收内部返回的
    self.liveRoomVC.liveCommentView.delegate = self;
    self.liveRoomVC.liveCommentView.hidden = YES;
    
    //移除默认底部工具栏
    [self.liveRoomVC.bottomViewsHolder removeFromSuperview];
    
    //添加自定义底部工具栏
    _liveInteractionVC = [[MELInteractionAreaViewController alloc] init];
    [self.liveRoomVC.view addSubview:_liveInteractionVC.view];
    if (_currentLiveStatus == 0){
        _liveInteractionVC.status = MELLiveRoomBottomViewStatusLiveNotStarted;
    } else if (_currentLiveStatus == 1) {
        _liveInteractionVC.status = MELLiveRoomBottomViewStatusLiveStarted;
    }
    
    [_liveInteractionVC.view mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.liveRoomVC.view);
        make.right.equalTo(self.liveRoomVC.view);
        make.bottom.equalTo(self.liveRoomVC.view);
        make.top.equalTo(self.videoPlayBackground.mas_bottom);
    }];
    
    __weak typeof(self) weakSelf = self;
    _liveInteractionVC.onShareButtonClicked = ^{
        [weakSelf p_showShareViewController];
    };
    _liveInteractionVC.onLikeButtonClicked = ^{
        [weakSelf.liveRoomVC sendLike];
    };
    _liveInteractionVC.onGiftButtonClicked = ^{
        [weakSelf p_showGiftsViewController];
    };
    _liveInteractionVC.onCommentSent = ^(NSString * _Nonnull comment) {
        [weakSelf.liveRoomVC sendComment:comment onSuccess:^{
            
        } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
            
        }];
    };
    _liveInteractionVC.onSubscribe = ^(void (^ _Nonnull didSubscribed)(BOOL)) {
        [weakSelf p_showSubscribeAlertVC:^(BOOL subscribed, NSString *phoneNumber) {
            didSubscribed(subscribed);
        }];
    };
    
    _liveInteractionVC.anchorNick =_anchorNick;
    _liveInteractionVC.anchorIntroduction = _anchorIntroduction;
    _liveInteractionVC.anchorAvartarImageURL = [NSURL URLWithString:_anchorAvatarURL];
    _liveInteractionVC.liveIntroduction = _liveIntroduction;
}

- (void)p_customizeLandscapeViews {
    
    self.liveRoomVC.enableViewRotation = YES;
    _landscapeLiveRoomVC = [[MELLiveRoomLandscapeViewController alloc] init];
    _landscapeLiveRoomVC.view.hidden = YES;
    
    [self.liveRoomVC.view addSubview:_landscapeLiveRoomVC.view];
    [_landscapeLiveRoomVC.view mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.edges.equalTo(self.liveRoomVC.view);
    }];
    
    __weak typeof(self) weakSelf = self;
    _landscapeLiveRoomVC.onBack = ^{
        [weakSelf switchOrientiation:UIInterfaceOrientationPortrait];
    };
    _landscapeLiveRoomVC.onLikeSend = ^{
        [weakSelf.liveRoomVC sendLike];
    };
    _landscapeLiveRoomVC.onGiftSend = ^{
        [weakSelf p_showGiftsViewController];
    };
    _landscapeLiveRoomVC.onCommentSent = ^(NSString * _Nonnull comment) {
        [weakSelf.liveRoomVC sendComment:comment onSuccess:^{
            
        } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
            
        }];
    };
    
    if (_currentLiveStatus == 0){
        _landscapeLiveRoomVC.status = MELLiveRoomLandscapeViewStatusStatusLiveNotStarted;
    } else if (_currentLiveStatus == 1) {
        _landscapeLiveRoomVC.status = MELLiveRoomLandscapeViewStatusStatusLiveStarted;
    }
    _landscapeLiveRoomVC.liveTitle = _liveTitle;
}

- (void) p_handleCustomMessage:(NSString *)info {
    if (info.length > 0) {
        NSData *turnData = [info dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
        NSString* action = [dataDic valueForKey:@"action"];
//        if ([action isEqualToString:kSECLRACustomMessageActionToBuy]) {
//            [self.customizedLiveSystemMessageLabel insertLiveSystemMessage:({
//                ASLRBLiveSystemMessageModel* model = [[ASLRBLiveSystemMessageModel alloc] init];
//                model.rawMessage = [NSString stringWithFormat:@"%@去购买了", [dataDic valueForKey:@"userNick"]];
//                model.extension = @{
//                    @"userId" : [dataDic valueForKey:@"userId"],
//                    @"userNick" : [dataDic valueForKey:@"userNick"]
//                };
//                model;
//            })];
//        } else if ([action isEqualToString:kSECLRACustomMessageActionFollow]) {
//            [self.customizedLiveCommentView insertNewComment:({
//                ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
//                model.sentContent = @"关注了主播";
//                model.senderID = [dataDic valueForKey:@"userId"];
//                model.senderNick = [dataDic valueForKey:@"userNick"];
//                model;
//            })];
//        } else
            if ([action isEqualToString:kMELCustomMessageActionSendGift]) {
            if ([[dataDic valueForKey:@"type"] isEqualToString:@"rocket"] && ![_userID isEqualToString:[dataDic valueForKey:@"userId"]]) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self p_launchGiftAnimationWithImageName:@"icon_rocket"];
                });
            }
        }
//            else if ([action isEqualToString:kSECLRACustomMessageActionUpdateGoods]) {
//            NSDictionary* goodsDetail = [dataDic valueForKey:@"goodsDetail"];
//            if (goodsDetail && [goodsDetail isKindOfClass:[NSDictionary class]]) {
//                self.currentGoodsImageUrl = [goodsDetail valueForKey:@"goods_image_url"];
//            }
//
//            __weak typeof(self) weakSelf = self;
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [weakSelf p_showGoodsCard:YES];
//
//                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)([[dataDic valueForKey:@"showSeconds"] intValue] * NSEC_PER_SEC)), dispatch_get_main_queue()
//                               , ^{
//                    [weakSelf p_showGoodsCard:NO];
//                });
//            });
//        }
    }
}

- (void)p_launchGiftAnimationWithImageName:(NSString*)imageName {
    
    UIImageView* imageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:imageName inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
    imageView.frame = CGRectMake([UIScreen mainScreen].bounds.size.width / 2 - 50, [UIScreen mainScreen].bounds.size.height, 100, 100);
    [self.liveRoomVC.view addSubview:imageView];
    [self.liveRoomVC.view bringSubviewToFront:imageView];
    
    __weak typeof(imageView) weakImageView = imageView;
    [UIView animateWithDuration:0.5 animations:^{
        
        //从底部飞到中间
        weakImageView.transform = CGAffineTransformMakeTranslation(0, -1 * ([UIScreen mainScreen].bounds.size.height / 2 + 50));
    } completion:^(BOOL finished) {
        if (finished) {
            [UIView animateWithDuration:0.5 animations:^{

                //放大三倍
                weakImageView.transform = CGAffineTransformScale(weakImageView.transform, 3.0, 3.0);
            } completion:^(BOOL finished) {

                [UIView animateWithDuration:0.5 animations:^{

                    //缩小到1倍
                    weakImageView.transform = CGAffineTransformScale(weakImageView.transform, 0.3333, 0.3333);

                } completion:^(BOOL finished) {

                    [UIView animateWithDuration:0.5 animations:^{

                        //从中间继续上飞到屏幕外
                        weakImageView.transform = CGAffineTransformTranslate(weakImageView.transform, 0, -1 * ([UIScreen mainScreen].bounds.size.height / 2 + 50));
                    } completion:^(BOOL finished) {
                        [weakImageView removeFromSuperview];
                    }];
                }];
            }];
        }
    }];
}

- (NSString*)p_dictionaryToJSonString:(NSDictionary*)dic {
    NSData* data = [NSJSONSerialization dataWithJSONObject:dic options:NSJSONWritingPrettyPrinted error:nil];
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
}

- (void) p_showShareViewController{
    if (!_shareVC) {
        _shareVC = [[MELShareViewController alloc] init];
    }

    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - 200, [UIScreen mainScreen].bounds.size.width, 200);
    [self.liveRoomVC presentChildViewController:_shareVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _shareVC;
}

- (void)p_showGiftsViewController {

    if (!_giftsVC) {
        _giftsVC = [[MELGiftsViewController alloc] init];
        _giftsVC.delegate = self;
    }

    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - 200, [UIScreen mainScreen].bounds.size.width, 200);
    
    [self.liveRoomVC presentChildViewController:_giftsVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _giftsVC;
}

- (void)p_showSubscribeAlertVC:(void(^)(BOOL subscribed, NSString* phoneNumber))didSubscribed {
    
    NSString* message = @"立即预约";
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
    
    NSMutableAttributedString *alertControllerMessageStr = [[NSMutableAttributedString alloc] initWithString:message];
    [alertControllerMessageStr addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:50/255.0 green:50/255.0 blue:51/255.0 alpha:1/1.0] range:NSRangeFromString(message)];
    [alertControllerMessageStr addAttribute:NSFontAttributeName value:[UIFont fontWithName:@"PingFangSC-Medium" size:16] range:NSRangeFromString(message)];
    [alertController setValue:alertControllerMessageStr forKey:@"attributedMessage"];
    
    [alertController addTextFieldWithConfigurationHandler:^(UITextField * _Nonnull textField) {
        textField.placeholder = @"请填写手机号";
    }];
    
    __weak typeof(alertController) weakAlertController = alertController;
    [alertController addAction:({
        UIAlertAction* action = [UIAlertAction actionWithTitle:@"提交" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            didSubscribed(YES, weakAlertController.textFields[0].text);
        }];
        [action setValue:[UIColor colorWithRed:0/255.0 green:188/255.0 blue:212/255.0 alpha:1/1.0] forKey:@"titleTextColor"];
        action;
    })];
    
    [alertController addAction:({
        UIAlertAction* action = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            didSubscribed(NO, nil);
        }];
        [action setValue:[UIColor colorWithRed:50/255.0 green:50/255.0 blue:51/255.0 alpha:1/1.0] forKey:@"titleTextColor"];
        action;
    })];
    [self.liveRoomVC presentViewController:alertController animated:NO completion:nil];
}

- (void)p_showLandscapeLiveRoom:(BOOL)show {
    if (show) {
        [_videoPlayBackground removeFromSuperview];
        [_landscapeLiveRoomVC.view addSubview:_videoPlayBackground];
        [_landscapeLiveRoomVC.view sendSubviewToBack:_videoPlayBackground];
        _landscapeLiveRoomVC.view.hidden = NO;
        
        [_videoPlayBackground mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self.landscapeLiveRoomVC.view);
        }];
        [_videoPlayBackground rotateToLandscape:YES];
        
    } else {
        _landscapeLiveRoomVC.view.hidden = YES;
        [_videoPlayBackground removeFromSuperview];
        [self.liveRoomVC.view addSubview:_videoPlayBackground];
        
        [_videoPlayBackground mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.liveRoomVC.view);
            make.right.equalTo(self.liveRoomVC.view);
            make.height.mas_equalTo(self.videoPlayBackground.portraitHeight);
            make.top.equalTo(self.headerViewHolder.mas_bottom);
        }];
        [_videoPlayBackground rotateToLandscape:NO];

        [_liveInteractionVC.view mas_remakeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(self.liveRoomVC.view);
            make.right.equalTo(self.liveRoomVC.view);
            make.bottom.equalTo(self.liveRoomVC.view);
            make.top.equalTo(self.videoPlayBackground.mas_bottom);
        }];
    }
    
    [self.liveRoomVC.view layoutIfNeeded];
}

// 强制旋转
- (void)switchOrientiation:(UIInterfaceOrientation)orientation {
    if ([[UIDevice currentDevice] respondsToSelector:@selector(setOrientation:)]) {
        SEL selector = NSSelectorFromString(@"setOrientation:");
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[UIDevice instanceMethodSignatureForSelector:selector]];
        [invocation setSelector:selector];
        [invocation setTarget:[UIDevice currentDevice]];
        [invocation setArgument:&orientation atIndex:2];
        [invocation invoke];
    }
}

#pragma mark ASLRBLiveRoomViewControllerDelegate

-(void) onASLRBLiveRoomEventInViewController:(ASLRBLiveRoomViewController *)liveRoomVC
                               liveRoomEvent:(ASLRBEvent)liveRoomEvent
                                        info:(NSDictionary *)info {
    switch (liveRoomEvent) {
            
        case ASLRBCommonEventLiveStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.liveInteractionVC.status = MELLiveRoomBottomViewStatusLiveStarted;
                self.landscapeLiveRoomVC.status = MELLiveRoomLandscapeViewStatusStatusLiveStarted;
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLiveStarted;
            });
        }
            break;
        case ASLRBCommonEventLiveDataUpdated: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.videoPlayBackground updateLivePV:[[info valueForKey:@"pv"] intValue]];
                [self.liveInteractionVC updateLikeCount:[[info valueForKey:@"likeCount"] intValue]];
                self.landscapeLiveRoomVC.likeCount = [[info valueForKey:@"likeCount"] intValue];
            });
        }
            break;
            
            
        case ASLRBAudienceEventLivePlayerStartPlaying: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusPlayStarted;
                
                self.liveInteractionVC.status = MELLiveRoomBottomViewStatusLiveStarted;
                [self.videoPlayBackground addSubview:self.liveRoomVC.playerView];
                [self.liveRoomVC.playerView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.top.equalTo(self.videoPlayBackground.mas_top);
                    make.left.equalTo(self.videoPlayBackground.mas_left);
                    make.bottom.equalTo(self.videoPlayBackground.mas_bottom);
                    make.right.equalTo(self.videoPlayBackground.mas_right);
                }];
                [self.videoPlayBackground sendSubviewToBack:self.liveRoomVC.playerView];
                [self.liveRoomVC setPlayerViewContentMode:UIViewContentModeScaleAspectFit];
            });
        }
            break;
            
        case ASLRBAudienceEventLivePlayerStartLoading:{
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadingStart;
            });
        }
            break;
            
        case ASLRBAudienceEventLivePlayerEndLoading: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadingEnd;
            });
        }
            break;
            
        case ASLRBAudienceEventLivePlayerPrepared: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadingEnd;
            });
        }
            break;
        case ASLRBAudienceEventLivePushStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusPushRecovered;
            });
        }
            break;
        case ASLRBAudienceEventLivePushStopped: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusPushStopped;
            });
        }
            break;
            
        case ASLRBCommonEventCustomMessageReceived: {
            [self p_handleCustomMessage:[info valueForKey:@"data"]];;
        }
            break;
            
        case ASLRBAudienceEventFloatingPlayWindowTapped: {
            if (self.lastViewController.navigationController.topViewController == self.lastViewController) {
                [self.lastViewController.navigationController pushViewController:self.liveRoomVC animated:NO];
            } else {
                [self.liveRoomVC dismissChildViewController:self.liveRoomVC.presentingChildViewController animated:NO];
            }
        }
            break;
        case ASLRBAudienceEventLiveEnded: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLiveEnded;
            });
        }
            break;
        case ASLRBCommonEventYourCommentsBannedOrNot: {
            [[NSNotificationCenter defaultCenter] postNotification:({
                NSNotification* notification = [[NSNotification alloc] initWithName:kMELYourCommentBannedNotification object:self userInfo:info];
                notification;
            })];
        }
            break;
            
        case ASLRBCommonEventAllCommentsBannedOrNot: {
            [[NSNotificationCenter defaultCenter] postNotification:({
                NSNotification* notification = [[NSNotification alloc] initWithName:kMELAllCommentBannedNotification object:self userInfo:info];
                notification;
            })];
        }
            break;
        default:
            break;
    }
}

- (void)onASLRBLiveRoomErrorInViewController:(ASLRBLiveRoomViewController *)liveRoomVC
                               liveRoomError:(ASLRBLiveRoomError)liveRoomError
                            withErrorMessage:(NSString *)errorMessage {
    switch (liveRoomError) {
        case ASLRBLiveRoomErrorLivePlayerError: {
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.videoPlayBackground.videoPlayStatus == MELVideoPlayStatusLoadingStart) {
                    self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusLoadFailed;
                }
                self.videoPlayBackground.videoPlayStatus = MELVideoPlayStatusPlayErrored;
            });
        }
            break;
            
        default:
            break;
    }
    
}

#pragma mark ASLRBLiveCommentViewDelegate

-(void) onASLRBLiveCommentJustAboutToPresent:(ASLRBLiveCommentModel *)comment {
    [self.liveInteractionVC insertLiveComment:comment];
    [self.landscapeLiveRoomVC insertLiveComment:comment];
}

-(void) onASLRBLiveSystmeMessageJustAboutToPresent:(ASLRBLiveSystemMessageModel *)systemMessage {
//    [self.customizedLiveSystemMessageLabel insertLiveSystemMessage:systemMessage];
}

#pragma mark MELGiftsDelegate
- (void)onRocketButtonClicked {
    [self.liveRoomVC dismissChildViewController:self.giftsVC animated:NO];

    [self.liveRoomVC sendCustomMessageToAll:[self p_dictionaryToJSonString:@{
        @"action" : kMELCustomMessageActionSendGift,
        @"type" : @"rocket",
        @"userId" : _userID,
        @"userNick" : _userNick
    }] onSuccess:^{
    } onFailure:^(NSString * _Nonnull errorMessage) {
    }];

    [self p_launchGiftAnimationWithImageName:@"icon_rocket"];
}

#pragma mark - NSNotification
//
//- (void)handleNSNotification:(NSNotification*)notification {
//    if ([notification.name isEqualToString:@"SELRAFollowButtonClicked"]) {
//        [self.liveRoomVC.view makeToast:@"关注成功" duration:1.0 position:CSToastPositionCenter];
//
//        [self.liveRoomVC sendCustomMessageToAll:[self p_dictionaryToJSonString:@{
//            @"action" : kSECLRACustomMessageActionFollow,
//            @"userId" : _userID,
//            @"userNick" : _userNick
//        }] onSuccess:^{
//
//        } onFailure:^(NSString * _Nonnull errorMessage) {
//
//        }];
//    }
//}

// 界面方向改变的处理
- (void)handleStatusBarOrientationDidChange:(NSNotification *)notification{
    
    UIInterfaceOrientation interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    
    switch (interfaceOrientation) {
        case UIInterfaceOrientationPortrait:
            [self p_showLandscapeLiveRoom:NO];
            break;
        case UIInterfaceOrientationLandscapeRight:
            [self p_showLandscapeLiveRoom:YES];
            break;
        default:
            break;
    }
}
@end
