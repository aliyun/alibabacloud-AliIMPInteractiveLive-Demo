//
//  ASLUKLiveRoomWrapper.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/17.
//

#import "ASLUKECommerceLiveRoomWrapper.h"
#import "SECLRALiveCommentView.h"
#import "SECLRALiveSystemMessageLabel.h"
#import "ASLUKEdgeInsetLabel.h"
#import "SECLRALiveRoomInfoHolderView.h"
#import "ASLUKResourceManager.h"
#import <Masonry/Masonry.h>
#import "SECLRALiveCommentModel.h"
#import "SECLRABottomViewHolder.h"
#import "UIView+Toast.h"
#import "UIColor+ColorWithHexString.h"
#import "SECLRAShareViewController.h"
#import "SECLRAShopWindowViewController.h"
#import "SECLRAGoodsDetailViewController.h"
#import "SECLRAMoreInteractionViewController.h"
#import "SECLRAGiftsViewController.h"
#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>

const NSString* kSECLRACustomMessageActionFollow = @"follow";
const NSString* kSECLRACustomMessageActionToBuy = @"toBuy";
const NSString* kSECLRACustomMessageActionSendGift = @"sendGift";
const NSString* kSECLRACustomMessageActionUpdateGoods = @"updateGoods";

@interface ASLUKECommerceLiveRoomWrapper() <ASLRBLiveRoomViewControllerDelegate, ASLRBLiveCommentViewDelegate, SECLRACommentViewDelegate>
@property (nonatomic, strong) ASLRBLiveRoomViewController* liveRoomVC;
@property (nonatomic, strong) SECLRALiveCommentView* customizedLiveCommentView;
@property (nonatomic, strong) SECLRALiveSystemMessageLabel* customizedLiveSystemMessageLabel;
@property (strong, nonatomic) ASLUKEdgeInsetLabel* unpresentedCommentNotificationLabel;
@property (strong, nonatomic) SECLRALiveRoomInfoHolderView* liveInfoView;
@property (strong, nonatomic) UILabel* rankingListLabel;
@property (strong, nonatomic) SECLRABottomViewHolder* bottomViewHolder;
@property (strong, nonatomic) SECLRAShareViewController* shareVC;
@property (strong, nonatomic) SECLRAShopWindowViewController* shopVC;
@property (strong, nonatomic) SECLRAGoodsDetailViewController* goodsDetailVC;
@property (strong, nonatomic) SECLRAMoreInteractionViewController* moreInteractionVC;
@property (strong, nonatomic) SECLRAGiftsViewController* giftsVC;
@property (strong, nonatomic) UIImageView* goodsCardView;
@property (copy, nonatomic) NSString* currentGoodsImageUrl;
@property (copy, nonatomic) NSString* liveID;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (copy, nonatomic) NSString* liveTitle;
@property (copy, nonatomic) NSString* anchorNick;
@property (copy, nonatomic) NSString* anchorAvatarURL;
@property (assign, nonatomic) int32_t currentPV;

@property (copy, nonatomic) void(^onSetupLiveRoomSuccess)(UIViewController* liveRoomViewController);
@property (copy, nonatomic) void(^onSetupLiveRoomFailure)(NSString* errorMessage);
@end

@implementation ASLUKECommerceLiveRoomWrapper

#pragma mark -Lifecycle

- (instancetype) init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNSNotification:) name:@"SELRAFollowButtonClicked" object:nil];
    }
    return self;
}

- (void) dealloc {
    [self.customizedLiveSystemMessageLabel stopPresenting];
    [self.customizedLiveCommentView stopPresenting];
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
        config.userID = _userID;
        config.userNick = _userNick;
        config;
    }) onSuccess:^{
        [weakSelf p_createLiveRoom];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        _onSetupLiveRoomFailure(errorMessage);
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
        config.liveID = _liveID;
        config.role = ASLRBUserRoleAudience;
        //隐藏默认的直播弹幕实现
        config.liveCommentViewsConfig.liveCommentViewHidden = YES;
        config.liveCommentViewsConfig.countOfHistoryCommentsWhenEntered = 100;
        config.middleViewsConfig.liveNoticeButtonHidden = NO;
        config;
    }) onSuccess:^(ASLRBLiveRoomViewController * _Nonnull liveRoomVC) {
        [weakSelf p_setupLiveRoomWithVC:liveRoomVC];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        _onSetupLiveRoomFailure(errorMessage);
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
        _onSetupLiveRoomFailure(errorMessage);
    }];
}

- (void)p_customizeLiveRoomVC {
    
    self.liveRoomVC.backgroundImageAfterLiving =  [UIImage imageNamed:@"背景" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
    self.liveRoomVC.backgroundImageBeforeLiving = [UIImage imageNamed:@"背景" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
    
    self.liveRoomVC.floatingPlayWindow.disappearAfterResignActive = NO;
    
    [self.liveRoomVC getLiveDetail:^(NSDictionary * _Nonnull detail) {
        _currentPV = [[detail valueForKey:@"pv"] intValue];
        _liveTitle = [detail valueForKey:@"title"];
        _anchorNick = [detail valueForKey:@"anchor_nick"];
        
        //获取直播扩展字段
        NSDictionary* customData = [detail valueForKey:@"extension"];

        //从直播自定义字段里获取当前主播正在展示的商品信息
        NSString* currentGoodsDetail = [customData valueForKey:@"currentGoodsDetail"];
        if (currentGoodsDetail) {
            NSData *data = [currentGoodsDetail dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary* dataDic = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:nil];
            if (dataDic && [dataDic isKindOfClass:[NSDictionary class]]) {
                _currentGoodsImageUrl = [dataDic valueForKey:@"goods_image_url"];
            }
        }
        
        //从直播自定义字段里获取当前主播的头像
        _anchorAvatarURL = [customData valueForKey:@"anchorAvatarURL"];
        
        dispatch_async(dispatch_get_main_queue(), ^{
            // 自定义直播间左上角头像区域
            [self p_customizeUpperLeftViews];
            
            // 自定义直播间右上角区域
            [self p_customizeUpperRightViews];
            
            // 自定义直播间中间区域
            [self p_customizeMiddleViews];
            
            // 自定义直播间弹幕区域
            [self p_customizeLiveCommentViews];
            
            // 自定义直播间底部区域
            [self p_customizeBottomViews];
            
//            [self presentViewController:self.liveRoomVC animated:NO completion:nil];
            _onSetupLiveRoomSuccess(self.liveRoomVC);
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        _onSetupLiveRoomFailure(errorMessage);
    }];
}

// 自定义直播间左上角区域
-(void) p_customizeUpperLeftViews {
        
    _liveInfoView = [[SECLRALiveRoomInfoHolderView alloc] init];
    [self.liveRoomVC.upperLeftCustomizedViewHolder addSubview:_liveInfoView];
    [_liveInfoView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.mas_equalTo(self.liveRoomVC.upperLeftCustomizedViewHolder).with.offset(14);
        make.top.mas_equalTo(self.liveRoomVC.upperLeftCustomizedViewHolder);
        make.bottom.mas_equalTo(self.liveRoomVC.upperLeftCustomizedViewHolder);
        make.right.mas_equalTo(self.liveRoomVC.upperLeftCustomizedViewHolder.mas_right).with.offset(29);
    }];
    _liveInfoView.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
    _liveInfoView.layer.masksToBounds = YES;
    _liveInfoView.layer.cornerRadius = 21;
    _liveInfoView.anchorNickLabel.text = self.liveTitle;
    [_liveInfoView updatePV:_currentPV];
    if (_anchorAvatarURL) {
        [_liveInfoView.anchorAvatarView setImage:[UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:_anchorAvatarURL]]]];
    } else {
        [_liveInfoView.anchorAvatarView setImage:[UIImage imageNamed:@"默认头像" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
    }
}

-(void) p_customizeUpperRightViews {
    
}

-(void) p_customizeMiddleViews {
    _rankingListLabel = [[UILabel alloc] init];
    _rankingListLabel.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
    _rankingListLabel.layer.cornerRadius = 12.3;
    _rankingListLabel.layer.shouldRasterize = YES;
    _rankingListLabel.layer.masksToBounds = YES;
    [self.liveRoomVC.view addSubview:_rankingListLabel];
    [_rankingListLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.equalTo(self.liveRoomVC.noticeButton.mas_right).with.offset(6);
        make.centerY.equalTo(self.liveRoomVC.noticeButton);
        make.size.mas_equalTo(CGSizeMake(131, 20));
    }];
    
    UIButton* fireButton = [[UIButton alloc] init];
    [fireButton setImage:[UIImage imageNamed:@"商家排名" inBundle:[ASLUKResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
    [_rankingListLabel addSubview:fireButton];
    [fireButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.size.mas_equalTo(CGSizeMake(16, 16));
        make.left.equalTo(_rankingListLabel.mas_left).with.offset(6);
        make.centerY.equalTo(_rankingListLabel);
    }];
    
    UILabel* textLabel = [[UILabel alloc] init];
    textLabel.text = @"这里是榜单名称99+名";
    textLabel.textColor = [UIColor whiteColor];
    textLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:10];
    [_rankingListLabel addSubview:textLabel];
    [textLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.size.mas_equalTo(CGSizeMake(99, 14));
        make.right.equalTo(_rankingListLabel.mas_right).with.offset(-6);
        make.centerY.equalTo(_rankingListLabel);
    }];
}

-(void) p_customizeLiveCommentViews {

    //设置delegate，接收弹幕事件
    self.liveRoomVC.liveCommentView.delegate = self;
    
    // 系统消息label
    _customizedLiveSystemMessageLabel = [[SECLRALiveSystemMessageLabel alloc] init];
    _customizedLiveSystemMessageLabel.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
    _customizedLiveSystemMessageLabel.layer.cornerRadius = 15;
    _customizedLiveSystemMessageLabel.layer.masksToBounds = YES;
    _customizedLiveSystemMessageLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
    _customizedLiveSystemMessageLabel.textAlignment = NSTextAlignmentCenter;
    _customizedLiveSystemMessageLabel.textColor = [UIColor whiteColor];
    _customizedLiveSystemMessageLabel.alpha = 0.0;
    [self.liveRoomVC.liveCommentView addSubview:_customizedLiveSystemMessageLabel];
    [_customizedLiveSystemMessageLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.size.mas_equalTo(CGSizeMake(115, 26));
        make.left.equalTo(self.liveRoomVC.liveCommentView.mas_left).with.offset(-115);
        make.top.equalTo(self.liveRoomVC.liveCommentView.mas_top);
    }];
    
    // 基于tableView的弹幕区域
    _customizedLiveCommentView = [[SECLRALiveCommentView alloc] init];
    _customizedLiveCommentView.commentDelegate = self;
    [self.liveRoomVC.liveCommentView addSubview:_customizedLiveCommentView];
    [_customizedLiveCommentView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.left.equalTo(self.liveRoomVC.liveCommentView.mas_left);
        make.right.equalTo(self.liveRoomVC.liveCommentView.mas_right);
        make.bottom.equalTo(self.liveRoomVC.liveCommentView.mas_bottom);
        make.top.equalTo(self.liveRoomVC.liveCommentView.mas_bottom).with.offset(-1);
    }];

    [self.liveRoomVC.liveCommentView layoutIfNeeded];
    
    [_customizedLiveCommentView startPresenting];
    [_customizedLiveCommentView insertNewComment:({
        ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
        model.senderNick = @"公告";
        model.sentContent = @"欢迎大家来到直播间！直播间内严禁出现违法违规、低俗色情、抽烟酗酒等内容，若发现违规行为请及时举报。";
        model;
    })];
    
    // 未读弹幕个数label
    _unpresentedCommentNotificationLabel = [[ASLUKEdgeInsetLabel alloc] init];
    _unpresentedCommentNotificationLabel.textInsets = UIEdgeInsetsMake(0.0, 6.0, 0.0, 6.0);
    _unpresentedCommentNotificationLabel.clipsToBounds = YES;
    _unpresentedCommentNotificationLabel.hidden = YES;
    _unpresentedCommentNotificationLabel.backgroundColor = [UIColor whiteColor];
    _unpresentedCommentNotificationLabel.layer.cornerRadius = 8.0;
    _unpresentedCommentNotificationLabel.layer.masksToBounds = YES;
    _unpresentedCommentNotificationLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
    _unpresentedCommentNotificationLabel.textColor = [UIColor colorWithRed:255/255.0 green:68/255.0 blue:44/255.0 alpha:1/1.0];
    _unpresentedCommentNotificationLabel.userInteractionEnabled = YES;
    [self.liveRoomVC.liveCommentView addSubview:_unpresentedCommentNotificationLabel];
    [_unpresentedCommentNotificationLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.bottom.equalTo(self.liveRoomVC.liveCommentView.mas_bottom);
        make.left.equalTo(self.liveRoomVC.liveCommentView.mas_left);
        make.height.mas_equalTo(22);
        make.width.mas_equalTo(64);
    }];
    
    UIButton* actionButton = [[UIButton alloc] init];
    actionButton.backgroundColor = [UIColor clearColor];
    [_unpresentedCommentNotificationLabel addSubview:actionButton];
    [actionButton addTarget:self action:@selector(onUnpresentedCommentLabelClicked:) forControlEvents:UIControlEventTouchUpInside];
    [actionButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.center.equalTo(_unpresentedCommentNotificationLabel);
        make.size.equalTo(_unpresentedCommentNotificationLabel);
    }];
}

-(void) p_customizeBottomViews {
    _bottomViewHolder = [[SECLRABottomViewHolder alloc] init];
    _bottomViewHolder.delegate = self;
    [self.liveRoomVC.bottomCustomizedViewHolder addSubview:_bottomViewHolder];
    [_bottomViewHolder mas_makeConstraints:^(MASConstraintMaker *make) {
        make.edges.equalTo(self.liveRoomVC.bottomCustomizedViewHolder);
    }];
}

- (void) p_handleCustomMessage:(NSString *)info {
    if (info.length > 0) {
        NSData *turnData = [info dataUsingEncoding:NSUTF8StringEncoding];
        NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
        NSString* action = [dataDic valueForKey:@"action"];
        if ([action isEqualToString:kSECLRACustomMessageActionToBuy]) {
            [self.customizedLiveSystemMessageLabel insertLiveSystemMessage:({
                ASLRBLiveSystemMessageModel* model = [[ASLRBLiveSystemMessageModel alloc] init];
                model.rawMessage = [NSString stringWithFormat:@"%@去购买了", [dataDic valueForKey:@"userNick"]];
                model.extension = @{
                    @"userId" : [dataDic valueForKey:@"userId"],
                    @"userNick" : [dataDic valueForKey:@"userNick"]
                };
                model;
            })];
        } else if ([action isEqualToString:kSECLRACustomMessageActionFollow]) {
            [self.customizedLiveCommentView insertNewComment:({
                ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
                model.sentContent = @"关注了主播";
                model.senderID = [dataDic valueForKey:@"userId"];
                model.senderNick = [dataDic valueForKey:@"userNick"];
                model;
            })];
        } else if ([action isEqualToString:kSECLRACustomMessageActionSendGift]) {
            if ([[dataDic valueForKey:@"type"] isEqualToString:@"rocket"] && ![_userID isEqualToString:[dataDic valueForKey:@"userId"]]) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self p_launchGiftAnimationWithImageName:@"icon_rocket"];
                });
            }
        } else if ([action isEqualToString:kSECLRACustomMessageActionUpdateGoods]) {
            NSDictionary* goodsDetail = [dataDic valueForKey:@"goodsDetail"];
            if (goodsDetail && [goodsDetail isKindOfClass:[NSDictionary class]]) {
                self.currentGoodsImageUrl = [goodsDetail valueForKey:@"goods_image_url"];
            }
            
            __weak typeof(self) weakSelf = self;
            dispatch_async(dispatch_get_main_queue(), ^{
                [weakSelf p_showGoodsCard:YES];

                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)([[dataDic valueForKey:@"showSeconds"] intValue] * NSEC_PER_SEC)), dispatch_get_main_queue()
                               , ^{
                    [weakSelf p_showGoodsCard:NO];
                });
            });
        }
    }
}

- (NSString*)p_dictionaryToJSonString:(NSDictionary*)dic {
    NSData* data = [NSJSONSerialization dataWithJSONObject:dic options:NSJSONWritingPrettyPrinted error:nil];
    return [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
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

- (void) p_showGoodsCard:(BOOL)show {
    
    [UIView animateWithDuration:0.5 animations:^{
        
        if (show) {
            if (!_goodsCardView) {
                _goodsCardView = [[UIImageView alloc] init];
                [_goodsCardView setImage:[UIImage imageWithData:[NSData dataWithContentsOfURL:[NSURL URLWithString:self.currentGoodsImageUrl]]]];
                _goodsCardView.userInteractionEnabled = YES;
                [_goodsCardView addGestureRecognizer:[[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(onGoodsCardTapped)]];
            }
            
            [self.liveRoomVC.view addSubview:_goodsCardView];
            
            [_goodsCardView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.bottom.equalTo(self.liveRoomVC.bottomCustomizedViewHolder.mas_top);
                make.left.equalTo(self.liveRoomVC.view).with.offset(14);
                make.height.mas_equalTo(80);
                make.width.mas_equalTo(260);
            }];
            
            [self.liveRoomVC.liveCommentView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.left.equalTo(self.liveRoomVC.view).with.offset(self.liveRoomVC.liveCommentView.frame.origin.x);
                make.bottom.equalTo(self.goodsCardView.mas_top);
                make.height.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.height);
                make.width.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.width);
            }];
        } else {
            if (self.goodsCardView) {
                [self.goodsCardView removeFromSuperview];
            }
            
            if (self.liveRoomVC) {
                [self.liveRoomVC.liveCommentView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.left.equalTo(self.liveRoomVC.view).with.offset(self.liveRoomVC.liveCommentView.frame.origin.x);
                    make.bottom.equalTo(self.liveRoomVC.bottomCustomizedViewHolder.mas_top);
                    make.height.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.height);
                    make.width.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.width);
                }];
            }
        }
    }];
}

-(void) p_showGoodsDetailVC {
    if (!_goodsDetailVC) {
        _goodsDetailVC = [[SECLRAGoodsDetailViewController alloc] init];
    }

    [self.liveRoomVC presentChildViewController:_goodsDetailVC animated:YES presentedFrame:[UIScreen mainScreen].bounds direction:ASLRBViewControllerPresentFromLeft];
    self.liveRoomVC.presentedChildViewController = _goodsDetailVC;
}

#pragma mark GoodsCardViewTapGestureAction

- (void) onGoodsCardTapped {
    [self p_showGoodsDetailVC];
    [self.liveRoomVC enterFloatingMode:YES];
    
    [self.liveRoomVC sendCustomMessageToAll:[self p_dictionaryToJSonString:@{
        @"action" : kSECLRACustomMessageActionToBuy,
        @"userId" : self.userID,
        @"userNick" : self.userNick
    }] onSuccess:^{

    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

#pragma mark ASLRBLiveRoomViewControllerDelegate

-(void) onASLRBLiveRoomEventInViewController:(ASLRBLiveRoomViewController *)liveRoomVC
                               liveRoomEvent:(ASLRBEvent)liveRoomEvent
                                        info:(NSDictionary *)info {
    switch (liveRoomEvent) {
        case ASLRBCommonEventExitButtonDidClicked: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.liveRoomVC exitLiveRoom];
                [self.liveRoomVC.navigationController popViewControllerAnimated:YES];
                self.liveRoomVC = nil;
            });
        }
            break;
        case ASLRBCommonEventLiveDataUpdated: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.liveInfoView updatePV:[[info valueForKey:@"pv"] intValue]];
                self.bottomViewHolder.likeCount = [[info valueForKey:@"likeCount"] intValue];
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
        default:
            break;
    }
}

- (void)onASLRBLiveRoomErrorInViewController:(ASLRBLiveRoomViewController *)liveRoomVC
                               liveRoomError:(ASLRBLiveRoomError)liveRoomError
                            withErrorMessage:(NSString *)errorMessage {
    
}

#pragma mark ASLRBLiveCommentViewDelegate

-(void) onASLRBLiveCommentJustAboutToPresent:(ASLRBLiveCommentModel *)comment {
    [self.customizedLiveCommentView insertNewComment:comment];
}

-(void) onASLRBLiveSystmeMessageJustAboutToPresent:(ASLRBLiveSystemMessageModel *)systemMessage {
    [self.customizedLiveSystemMessageLabel insertLiveSystemMessage:systemMessage];
}

#pragma mark -unpresentedCommentLabelClicked action

- (void) onUnpresentedCommentLabelClicked:(UIButton*)sender {
    [self.customizedLiveCommentView scrollToNewestComment];
}

#pragma mark -SECLRACommentViewDelegate

-(void) actionWhenUnpresentedCommentCountChange:(int32_t)count {
    if (count > 0) {
        self.unpresentedCommentNotificationLabel.text = [NSString stringWithFormat:@"%d条新消息", count];
        CGSize sizeNew = [self.unpresentedCommentNotificationLabel.text sizeWithAttributes:@{NSFontAttributeName:self.unpresentedCommentNotificationLabel.font}];
        [self.unpresentedCommentNotificationLabel mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(sizeNew.width + 18);
        }];
        self.unpresentedCommentNotificationLabel.hidden = NO;
    } else {
        self.unpresentedCommentNotificationLabel.hidden = YES;
    }
}

-(void) actionWhenOneCommentPresentedWithActualHeight:(int32_t)height {
    if (height > self.liveRoomVC.liveCommentView.bounds.size.height - 28) {
        return;
    }
        
    [self.customizedLiveCommentView mas_updateConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.top.equalTo(self.liveRoomVC.liveCommentView.mas_bottom).with.offset(-1 * MIN(height, MAX(self.liveRoomVC.liveCommentView.bounds.size.height - 28, 0)));
    }];
    [self.liveRoomVC.liveCommentView layoutIfNeeded];
}

- (void) onLiveCommentCellInteraction:(NSInteger)interaction extension:(nonnull NSDictionary *)extension {
    if (interaction == SECLRALiveCommentCellInteractionTypeFollow) {
        [self.liveInfoView follow];
    } else if (interaction == SECLRALiveCommentCellInteractionTypeComment) {
        [self.liveRoomVC sendComment:[extension valueForKey:@"comment"] onSuccess:^{
            
        } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
            
        }];
    }
}

#pragma mark -SECLRABottomViewHolderDelegate

- (void)onLikeButtonClicked {
    [self.liveRoomVC sendLike];
}

- (void)onShareButtonClicked {
    if (!_shareVC) {
        _shareVC = [[SECLRAShareViewController alloc] init];
    }
    
    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - ([UIScreen mainScreen].bounds.size.width / 2.67), [UIScreen mainScreen].bounds.size.width, 200);
    [self.liveRoomVC presentChildViewController:_shareVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _shareVC;
}

- (void)onFloatingWindowButtonClicked {

    NSString* message = @"开启直播小窗口边看边买吧~";
    
    UIAlertController* alertController = [UIAlertController alertControllerWithTitle:nil message:message preferredStyle:UIAlertControllerStyleAlert];
    
    NSMutableAttributedString *alertControllerMessageStr = [[NSMutableAttributedString alloc] initWithString:message];
    [alertControllerMessageStr addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.8] range:NSRangeFromString(message)];
    [alertControllerMessageStr addAttribute:NSFontAttributeName value:[UIFont systemFontOfSize:16] range:NSRangeFromString(message)];
    [alertController setValue:alertControllerMessageStr forKey:@"attributedMessage"];
    
    [alertController addAction:({
        UIAlertAction* action = [UIAlertAction actionWithTitle:@"开启" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [self.liveRoomVC enterFloatingMode:YES];
            
            [self.liveRoomVC.navigationController popViewControllerAnimated:YES];
        }];
        [action setValue:[UIColor colorWithHexString:@"#FB622B" alpha:1.0] forKey:@"titleTextColor"];
        action;
    })];
    
    [alertController addAction:({
        UIAlertAction* action = [UIAlertAction actionWithTitle:@"关闭" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }];
        [action setValue:[UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.6] forKey:@"titleTextColor"];
        action;
    })];
    [self.liveRoomVC presentViewController:alertController animated:NO completion:nil];
}

- (void)onGoodsButtonClicked {
    if (!_shopVC) {
        _shopVC = [[SECLRAShopWindowViewController alloc] init];
        
        __weak typeof(self) weakSelf = self;
        _shopVC.onTapped = ^{
            
            [weakSelf.liveRoomVC dismissChildViewController:weakSelf.shopVC animated:NO];
            
            [weakSelf.liveRoomVC sendCustomMessageToAll:[weakSelf p_dictionaryToJSonString:@{
                @"action" : kSECLRACustomMessageActionToBuy,
                @"userId" : weakSelf.userID,
                @"userNick" : weakSelf.userNick
            }] onSuccess:^{

            } onFailure:^(NSString * _Nonnull errorMessage) {
                
            }];

            [weakSelf p_showGoodsDetailVC];
            
            [weakSelf.liveRoomVC enterFloatingMode:YES];
        };
    }
    
    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - [UIScreen mainScreen].bounds.size.width * 1.2, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.width * 1.2);
    [self.liveRoomVC presentChildViewController:_shopVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _shopVC;
}

- (void)onMoreButtonClicked {
    
    if (!_moreInteractionVC) {
        _moreInteractionVC = [[SECLRAMoreInteractionViewController alloc] init];
        _moreInteractionVC.delegate = self;
    }
    
    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - ([UIScreen mainScreen].bounds.size.width / 2.67), [UIScreen mainScreen].bounds.size.width, 200);
    [self.liveRoomVC presentChildViewController:_moreInteractionVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _moreInteractionVC;
}

- (void)onCommentSent:(NSString*)comment {
    [self.liveRoomVC sendComment:comment onSuccess:^{
        
    } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
        
    }];
}

#pragma mark SECLRAMoreInteractionDelegate
- (void)onGiftButtonClicked {
    [self.liveRoomVC dismissChildViewController:self.moreInteractionVC animated:NO];
    
    if (!_giftsVC) {
        _giftsVC = [[SECLRAGiftsViewController alloc] init];
        _giftsVC.delegate = self;
    }
    
    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - ([UIScreen mainScreen].bounds.size.width / 2.67), [UIScreen mainScreen].bounds.size.width, 200);
    [self.liveRoomVC presentChildViewController:_giftsVC animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
    self.liveRoomVC.presentedChildViewController = _giftsVC;
}

#pragma mark SECLRAGiftsDelegate
- (void)onRocketButtonClicked {
    [self.liveRoomVC dismissChildViewController:self.giftsVC animated:NO];

    [self.liveRoomVC sendCustomMessageToAll:[self p_dictionaryToJSonString:@{
        @"action" : kSECLRACustomMessageActionSendGift,
        @"type" : @"rocket",
        @"userId" : _userID,
        @"userNick" : _userNick
    }] onSuccess:^{
    } onFailure:^(NSString * _Nonnull errorMessage) {
    }];
    
    [self p_launchGiftAnimationWithImageName:@"icon_rocket"];
}

#pragma mark - NSNotification

- (void)handleNSNotification:(NSNotification*)notification {
    if ([notification.name isEqualToString:@"SELRAFollowButtonClicked"]) {
        [self.liveRoomVC.view makeToast:@"关注成功" duration:1.0 position:CSToastPositionCenter];
        
        [self.liveRoomVC sendCustomMessageToAll:[self p_dictionaryToJSonString:@{
            @"action" : kSECLRACustomMessageActionFollow,
            @"userId" : _userID,
            @"userNick" : _userNick
        }] onSuccess:^{
            
        } onFailure:^(NSString * _Nonnull errorMessage) {
            
        }];
    }
}
@end
