//
//  ASLRBPlaybackViewController.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/28.
//

#import "ASLRBPlaybackViewController.h"
#import <Masonry/Masonry.h>
#import "../CommonModels/ASLRBRoomInfoModel.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>

#import "ASLRBLogger.h"
#import "../LiveRoomSetup/ASLRBLiveInitConfig.h"
#import "../LiveRoomSetup/ASLRBAppInitConfig.h"
//#import "../LiveComment/ASLRBLiveCommentView.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
#import "../CommonViews/DetailsButton/ASLRBDetailsButton.h"
#import "../CommonViews/InsetLabel/ASLRBEdgeInsetLabel.h"
#import "../CommonTools/UIViewController+ASLRBCustomChildVC.h"
//#import "../Interface/ASLRBLiveCommentViewProtocol.h"
#import "../CommonTools/ASLRBResourceManager.h"
#import "ASLRBLiveRoomLoginDelegate.h"
#import "../LiveRoomBottomViews/ASLRBPlaybackLiveRoomBottomViewHolder.h"
#import "../LiveRoomSetup/ASLRBLiveRoomMiddleViewsConfig.h"
#import "../LiveRoomMiddleViews/ASLRBLivePlayLoadingIndicatorView.h"

@interface ASLRBPlaybackViewController ()<UITextFieldDelegate,AIRBRoomChannelDelegate,UIGestureRecognizerDelegate, AIRBVodPlayerDelegate>

@property(nonatomic, assign) BOOL isUISettedUp;
//@property (strong, nonatomic) UIView* liveInfoHolder;
//@property (strong, nonatomic) UIImageView* userImg;
//@property (strong, nonatomic) UILabel* anchorNickLabel;
//@property (strong, nonatomic) UILabel* liveOnlineCountLabel;
//@property (strong, nonatomic) UILabel* liveLikeCountlabel;
//@property (strong, nonatomic) UIButton* membersButton;
//@property (strong, nonatomic) ASLRBDetailsButton* noticeButton;
//@property (strong, nonatomic) UIButton* exitButton;

//@property (strong, nonatomic) UITextField* sendField;
//@property (strong, nonatomic) UIButton* shareButton;
//@property (strong, nonatomic) UIButton* likeButton;

@property (strong, nonatomic) UIImageView* playerTransitionImageView;

@property (assign,    atomic) BOOL roomEntered;
@property (assign, nonatomic) BOOL isSwitchingUser;
@property (copy, nonatomic) NSString* switchingUserID;
@property (copy, nonatomic) NSString* switchingUserNick;
@property (weak, nonatomic) id<ASLRBLiveRoomLoginDelegate> loginDelegate;
@property (assign, nonatomic) BOOL livePlayerStarted;
//@property (assign, nonatomic) BOOL morePanelShowed;
//@property (assign, nonatomic) BOOL membersViewShowed;
//@property (assign, nonatomic) BOOL isMuted;
//@property (assign, nonatomic) BOOL isStopped;
//@property (assign, nonatomic) BOOL beautyOn;

@property (nonatomic, strong) ASLRBAppInitConfig* appInitConfig;
@property (nonatomic, strong) ASLRBLiveInitConfig* liveInitConfig;
//@property (nonatomic, strong) ASLRBLiveRoomInitConfig* roomInitConfig;
//@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property(strong,nonatomic) ASLRBLiveInfoModel* liveModel;
@property(strong,nonatomic) ASLRBLivePlayLoadingIndicatorView* playLoadingIndicator;

@end

@implementation ASLRBPlaybackViewController

@synthesize upperLeftCustomizedViewHolder = _upperLeftCustomizedViewHolder;
@synthesize upperRightCustomizedViewHolder = _upperRightCustomizedViewHolder;
@synthesize middleCustomizedViewHolder = _middleCustomizedViewHolder;
@synthesize bottomCustomizedViewHolder = _bottomCustomizedViewHolder;
//@synthesize liveCommentView = _liveCommentView;
//@synthesize backgroundImage = _backgroundImage;
@synthesize playerViewContentMode = _playerViewContentMode;
@synthesize liveDetail = _liveDetail;
@synthesize noticeButton = _noticeButton;
@synthesize bottomViewsHolder = _bottomViewsHolder;
@synthesize liveRoomType = _liveRoomType;

@synthesize enableViewRotation = _enableViewRotation;

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

//- (UIView *)liveInfoHolder{
//    if(!_liveInfoHolder) {
//        UIView* view = [[UIView alloc]init];
//        [self.view addSubview:view];
//        [view mas_makeConstraints:^(MASConstraintMaker *make) {
//            if (@available(iOS 11.0, *)) {
//                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(8);
//            } else {
//                make.top.equalTo(self.view).with.offset(8);
//            }
//            make.left.equalTo(self.view).with.offset(10);
//            make.width.mas_equalTo(173);
//            make.height.mas_equalTo(43);
//        }];
//        view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
//        view.layer.masksToBounds = YES;
//        view.layer.cornerRadius = 21.5;
//        [view addSubview:self.userImg];
//        [view addSubview:self.anchorNickLabel];
//        [view addSubview:self.liveOnlineCountLabel];
//        [view addSubview:self.liveLikeCountlabel];
//        _liveInfoHolder = view;
//    }
//    return _liveInfoHolder;
//}
//
//- (UIImageView *)userImg{
//    if (!_userImg) {
//        UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(2, 3, 36.5, 36.5)];
//        if(self.liveModel.avatar){
//            [imageView setImage:self.liveModel.avatar];
//        }else{
//            [imageView setImage:[UIImage imageNamed:self.liveModel.userImg]];
//        }
//        imageView.layer.masksToBounds = YES;
//        imageView.layer.cornerRadius = 18.25;
//        _userImg = imageView;
//    }
//    return _userImg;
//}
//
//
//- (UILabel *)anchorNickLabel{
//    if(!_anchorNickLabel){
//        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(48, 6, 117, 17)];
//        label.text = self.liveModel.anchorNick;
//        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:14];
//        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
//        _anchorNickLabel = label;
//    }
//    return _anchorNickLabel;
//}
//
//- (UILabel *)liveOnlineCountLabel{
//    if(!_liveOnlineCountLabel){
//        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(47, 24, 43, 14)];
//        label.text = [NSString stringWithFormat:@"%d观看",self.liveModel.onlineCount];
//        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
//        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
//        _liveOnlineCountLabel = label;
//    }
//    return _liveOnlineCountLabel;
//}
//
//- (UILabel *)liveLikeCountlabel{
//    if(!_liveLikeCountlabel){
//        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(96, 24, 43, 14)];
//        label.text = [NSString stringWithFormat:@"%d点赞",self.liveModel.likeCount];
//        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
//        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
//        _liveLikeCountlabel = label;
//    }
//    return _liveLikeCountlabel;
//}

- (UIButton *)noticeButton{
    if (!_noticeButton) {
        ASLRBDetailsButton* button = [[ASLRBDetailsButton alloc]initWithFrame:CGRectMake(0, 0, 64.8, 19.6) image:[UIImage imageNamed:@"直播-公告" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]  title:@"公告"];
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(56);
            } else {
                make.top.equalTo(self.view).with.offset(56);
            }
            make.left.equalTo(self.view).with.offset(19);
            make.width.mas_equalTo(64.8);
            make.height.mas_equalTo(19.6);
        }];
        _noticeButton = button;
    }
    return _noticeButton;
}

- (ASLRBLivePlayLoadingIndicatorView*)playLoadingIndicator {
    if (_liveInitConfig.middleViewsConfig.livePlayLoadingIndicatorHidden) {
        return nil;
    }
    if (!_playLoadingIndicator) {
        void(^onCreate)(void) = ^{
            _playLoadingIndicator = [[ASLRBLivePlayLoadingIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
            _playLoadingIndicator.hidden = YES;
            _playLoadingIndicator.tintColor = [UIColor whiteColor];
            _playLoadingIndicator.color = [UIColor whiteColor];
            [self.view addSubview:_playLoadingIndicator];
            [_playLoadingIndicator mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.size.mas_equalTo(CGSizeMake(38.4, 38.4));
                make.centerX.equalTo(self.view.mas_centerX);
                make.centerY.equalTo(self.view.mas_centerY);
            }];
        };
        if ([NSThread isMainThread]) {
            onCreate();
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                onCreate();
            });
        }
    }
    return  _playLoadingIndicator;
}

- (UIView<ASLRBLiveRoomBottomViewsHolderProtocol>*) bottomViewsHolder {
    if (!_bottomViewsHolder) {
        _bottomViewsHolder = [[ASLRBPlaybackLiveRoomBottomViewHolder alloc] init];
        ((ASLRBPlaybackLiveRoomBottomViewHolder*)_bottomViewsHolder).actionsDelegate = self;
        [self.view addSubview:_bottomViewsHolder];
        
        [_bottomViewsHolder mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).with.offset(-45);
            } else {
                make.bottom.equalTo(self.view).with.offset(-45);
            }
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.height.mas_equalTo(59);
        }];
    }
    return _bottomViewsHolder;
}

//- (UIButton*) exitButton{
//    if (!_exitButton) {
//        UIButton* button = [[UIButton alloc] init];
//        __weak typeof(self) weakSelf = self;
//        [self.view addSubview:button];
//        [button mas_makeConstraints:^(MASConstraintMaker *make) {
//            if (@available(iOS 11.0, *)) {
//                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop).with.offset(10);
//            } else {
//                make.top.equalTo(weakSelf.view).with.offset(10);
//
//            }
//            make.right.equalTo(weakSelf.view.mas_right).with.offset(-10);
//            make.height.mas_equalTo(30);
//            make.width.mas_equalTo(30);
//        }];
//        [button setBackgroundImage:[UIImage imageNamed:@"icon-exit" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
//        [button setAdjustsImageWhenHighlighted:NO];
//        [button addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
//        _exitButton = button;
//    }
//    return _exitButton;
//}

//- (UITextField*) sendField {
//    if (!_sendField) {
//        UITextField* textField = [[UITextField alloc] init];
//        __weak typeof(self) weakSelf = self;
//        [self.view addSubview:textField];
//        [textField mas_makeConstraints:^(MASConstraintMaker *make) {
//            if (@available(iOS 11.0, *)) {
//                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-9);
//            } else {
//                make.bottom.equalTo(weakSelf.view).with.offset(-9);
//            }
//            make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
//            make.width.mas_equalTo(257);
//            make.height.mas_equalTo(40);
//        }];
//        textField.layer.masksToBounds = YES;
//        textField.layer.cornerRadius = 20;
//        textField.textColor = [UIColor blackColor];
//        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说点什么……"
//                                                                         attributes:@{
//                                                                             NSForegroundColorAttributeName:[UIColor colorWithWhite:1 alpha:0.8],
//                                                                             NSFontAttributeName:[UIFont systemFontOfSize:14]
//                                                                         }];
//        textField.attributedPlaceholder = attrString;
//        textField.backgroundColor = [UIColor colorWithWhite:0.1 alpha:0.7];
//        textField.textAlignment = NSTextAlignmentLeft;
//        textField.keyboardType = UIKeyboardTypeDefault;
//        textField.returnKeyType = UIReturnKeySend;
//        textField.keyboardAppearance = UIKeyboardAppearanceDefault;
//        textField.delegate = self;
//        textField.borderStyle = UITextBorderStyleRoundedRect;
//        [textField setContentHuggingPriority:UILayoutPriorityRequired forAxis:UILayoutConstraintAxisHorizontal];
//        _sendField = textField;
//    }
    
//    return _sendField;
//}

//- (UIButton*) shareButton {
//    if (!_shareButton) {
//        UIButton* button = [[UIButton alloc] init];
//        __weak typeof(self) weakSelf = self;
//        [self.view addSubview:button];
//
////        [button mas_makeConstraints:^(MASConstraintMaker *make) {
////            make.left.equalTo(weakSelf.sendField.mas_right).with.offset(12);
////            if (@available(iOS 11.0, *)) {
////                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-9);
////            } else {
////                make.bottom.equalTo(weakSelf.view).with.offset(-9);
////            }
////            make.width.mas_equalTo(40);
////            make.height.mas_equalTo(40);
////        }];
//        [button mas_makeConstraints:^(MASConstraintMaker *make) {
//            make.right.equalTo(weakSelf.likeButton.mas_left).with.offset(-10);
//            make.centerY.equalTo(weakSelf.likeButton);
//            make.width.mas_equalTo(40);
//            make.height.mas_equalTo(40);
//        }];
//
//        [button setImage:[UIImage imageNamed:@"直播-互动区-分享" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
//        [button setAdjustsImageWhenHighlighted:NO];
//        [button addTarget:self action:@selector(shareButtonAction:) forControlEvents:UIControlEventTouchUpInside];
//        _shareButton = button;
//    }
//    return _shareButton;
//}

//- (UIButton*) likeButton {
//    if (!_likeButton) {
//        UIButton* button = [[UIButton alloc] init];
//        __weak typeof(self) weakSelf = self;
//        [self.view addSubview:button];
//
////        [button mas_makeConstraints:^(MASConstraintMaker *make) {
////            make.left.equalTo(weakSelf.shareButton.mas_right).with.offset(10);
////            make.centerY.equalTo(weakSelf.shareButton);
////            make.width.mas_equalTo(40);
////            make.height.mas_equalTo(40);
////        }];
//        [button mas_makeConstraints:^(MASConstraintMaker *make) {
//            if (@available(iOS 11.0, *)) {
//                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-9);
//            } else {
//                make.bottom.equalTo(weakSelf.view).with.offset(-9);
//            }
//            make.right.equalTo(weakSelf.view.mas_right).with.offset(-10);
//            make.width.mas_equalTo(40);
//            make.height.mas_equalTo(40);
//        }];
//
//        [button setImage:[UIImage imageNamed:@"直播-互动区-点赞" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
//        [button setAdjustsImageWhenHighlighted:NO];
//        [button addTarget:self action:@selector(likeButtonAction:) forControlEvents:UIControlEventTouchUpInside];
//        _likeButton = button;
//    }
//    return _likeButton;
//}

//- (UIView<ASLRBLiveCommentViewProtocol>*) liveCommentView {
//    if(!_liveCommentView){
//        void (^createCommentView)(void) = ^void(void) {
//            _liveCommentView = [[ASLRBLiveCommentView alloc] init];
//            [self.view addSubview:_liveCommentView];
//            [_liveCommentView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//                if (@available(iOS 11.0, *)) {
//                    make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).with.offset(-59);
//                } else {
//                    make.bottom.equalTo(self.view).with.offset(-59);
//                }
//                make.left.equalTo(self.view.mas_left).with.offset(10);
//                make.height.mas_equalTo(230);
//                make.width.mas_equalTo(260);
//            }];
//        };
//
//        if ([[NSThread currentThread] isMainThread]) {
//            createCommentView();
//        } else {
//            dispatch_sync(dispatch_get_main_queue(), ^{
//                createCommentView();
//            });
//        }
//    }
//    return _liveCommentView;
//}

//- (ASLRBLiveSystemMessageLabel*)internalSystemMessageLabel {
//    if (!_internalSystemMessageLabel) {
//        _internalSystemMessageLabel = [[ASLRBLiveSystemMessageLabel alloc] init];
//        _internalSystemMessageLabel.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
//        _internalSystemMessageLabel.layer.cornerRadius = 15;
//        _internalSystemMessageLabel.layer.masksToBounds = YES;
//        _internalSystemMessageLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:12];
//        _internalSystemMessageLabel.textAlignment = NSTextAlignmentCenter;
//        _internalSystemMessageLabel.textColor = [UIColor colorWithHexString:@"#FFFFFF" alpha:1.0];
//        _internalSystemMessageLabel.alpha = 0.0;
//        [self.liveCommentView addSubview:_internalSystemMessageLabel];
//        [_internalSystemMessageLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//            make.size.mas_equalTo(CGSizeMake(150, 26));
//            make.left.equalTo(self.liveCommentView.mas_left);
//            make.top.equalTo(self.liveCommentView.mas_top);
//        }];
//    }
//    return _internalSystemMessageLabel;
//}

//- (ASLRBCommentView*)internalCommentView {
//    if (!_internalCommentView) {
//        _internalCommentView = [[ASLRBCommentView alloc] initWithCommentStyle:AICBCommentStyleLive];
//        _internalCommentView.commentDelegate = self;
//        _internalCommentView.alpha = 0.0;
//        [self.liveCommentView addSubview:_internalCommentView];
//        [_internalCommentView mas_makeConstraints:^(MASConstraintMaker *make) {
//            make.top.equalTo(self.internalSystemMessageLabel.mas_bottom);
//            make.bottom.equalTo(self.liveCommentView.mas_bottom);
//            make.left.equalTo(self.liveCommentView.mas_left);
//            make.right.equalTo(self.liveCommentView.mas_right);
//        }];
//        [_internalCommentView insertNewComment:@"系统提示: 欢迎大家来到直播间!直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容,若有违规行为请及时举报。"];
//    }
//    return _internalCommentView;
//}

- (UIView *)upperLeftCustomizedViewHolder{
    if(!_upperLeftCustomizedViewHolder){
        UIView* view = [[UIView alloc] init];
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(8);
            } else {
                make.top.equalTo(self.view).with.offset(8);
            }
            make.left.equalTo(self.view);
            make.width.mas_equalTo(183);
            make.height.mas_equalTo(43);
        }];
        [view.superview layoutIfNeeded];
        _upperLeftCustomizedViewHolder = view;
    }
    return _upperLeftCustomizedViewHolder;
}

- (UIView *)upperRightCustomizedViewHolder{
    if(!_upperRightCustomizedViewHolder){
        UIView* view = [[UIView alloc] init];
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(8);
            } else {
                make.top.equalTo(self.view).with.offset(8);
            }
            make.left.equalTo(self.view).with.offset(183);
            make.right.equalTo(self.view.mas_right);
            make.height.mas_equalTo(43);
        }];
        [view.superview layoutIfNeeded];
        _upperRightCustomizedViewHolder = view;
    }
    return _upperRightCustomizedViewHolder;
}

- (UIView *)middleCustomizedViewHolder{
    if(!_middleCustomizedViewHolder){
        __weak typeof(self) weakSelf = self;
        UIView* view = [[UIView alloc] init];
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(51);
            } else {
                make.top.equalTo(self.view).with.offset(51);
            }
            make.left.equalTo(weakSelf.view.mas_left);
            make.right.equalTo(weakSelf.view.mas_right);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).with.offset(-104);
            } else {
                make.bottom.equalTo(self.view).with.offset(-104);
            }
        }];
        [view.superview layoutSubviews];
        _middleCustomizedViewHolder = view;
    }
    return _middleCustomizedViewHolder;
}


- (UIView *)bottomCustomizedViewHolder{
    if(!_bottomCustomizedViewHolder){
        UIView* view = [[UIView alloc]initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 59)];
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).with.offset(-45);
            } else {
                make.bottom.equalTo(self.view).with.offset(-45);
            }
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.height.mas_equalTo(59);
        }];
        [view.superview layoutSubviews];
        _bottomCustomizedViewHolder = view;
    }
    return _bottomCustomizedViewHolder;
}

- (void) setPlayerViewContentMode:(UIViewContentMode)playerViewContentMode {
    _playerViewContentMode = playerViewContentMode;
    if (playerViewContentMode == UIViewContentModeScaleAspectFit) {
        self.room.vodPlayer.contentMode = AIRBVideoViewContentModeAspectFit;
    } else if (playerViewContentMode == UIViewContentModeScaleAspectFill) {
        self.room.vodPlayer.contentMode = AIRBVideoViewContentModeAspectFill;
    } else if (playerViewContentMode == UIViewContentModeScaleToFill) {
        self.room.vodPlayer.contentMode = AIRBVideoViewContentModeFill;
    }
}

- (void)setUpUI{
    if(!_upperLeftCustomizedViewHolder){
        [self.view bringSubviewToFront:self.liveInfoViewHolder];
        self.liveInfoViewHolder.anchorNickLabel.text = self.liveModel.anchorNick;
        if (!self.liveInfoViewHolder.anchorAvatarView.image) {
            self.liveInfoViewHolder.anchorAvatarView.image = [UIImage imageNamed:@"img-user-default" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
        }
    }else{
        [self.view bringSubviewToFront:self.upperLeftCustomizedViewHolder];
    }
    
    if(!_upperRightCustomizedViewHolder){
        [self.view bringSubviewToFront:self.exitButton];
    }else{
        [self.view bringSubviewToFront:self.upperRightCustomizedViewHolder];
    }
    
    if(!_bottomCustomizedViewHolder){
        [self.view bringSubviewToFront:self.bottomViewsHolder];
    }else{
        [self.view bringSubviewToFront:self.bottomCustomizedViewHolder];
    }
    
    if(!_middleCustomizedViewHolder){
        [self.view bringSubviewToFront:self.noticeButton];
    }else{
        [self.view bringSubviewToFront:self.middleCustomizedViewHolder];
    }
    
//    [self.view sendSubviewToBack:self.backgroundView];
//    [self.view bringSubviewToFront:self.liveCommentView];
    
    self.isUISettedUp = YES;
}

- (void) setupOnSuccess:(void(^)(NSString* liveID))onSuccess onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOG("ASLRBPlaybackViewController::setup.");
    
    self.liveModel.roomID = [self.liveDetail valueForKey:@"room_id"];
    self.liveModel.title = [self.liveDetail valueForKey:@"title"];
    self.liveModel.notice = [self.liveDetail valueForKey:@"notice"];
    self.liveModel.anchorNick = [self.liveDetail valueForKey:@"anchor_nick"];
    self.liveModel.anchorID = [self.liveDetail valueForKey:@"anchor_id"];
    
    onSuccess(self.liveInitConfig.liveID);
}

- (void) sendComment:(NSString *)message onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(ASLRBLiveRoomError, NSString * _Nonnull))onFailure {
//    if (message.length > 0) {
//        if (message.length > self.liveInitConfig.maxCommentLength) {
//            onFailure(ASLRBLiveRoomCommentLengthExceedsLimit, @"");
//        } else {
//            [self.room.chat sendComment:message onSuccess:^{
//                [((ASLRBLiveCommentView*)self.liveCommentView) insertNewCommentCompulsorily:[NSString stringWithFormat:@"%@: %@", self.appInitConfig.userNick, message]];
//                onSuccess();
//            } onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
//                onFailure(code, message);
//            }];
//        }
//    }
}

- (void) sendComment:(NSString *)message
           extension:(NSDictionary<NSString *,NSString *> *)extension
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(ASLRBLiveRoomError code, NSString * errorMessage))onFailure {
//    if (message.length > 0) {
//        if (message.length > self.liveInitConfig.maxCommentLength) {
//            onFailure(ASLRBLiveRoomCommentLengthExceedsLimit, @"");
//        } else {
//            [self.room.chat sendComment:message
//                              extension:extension
//                              onSuccess:^{
//                [((ASLRBLiveCommentView*)self.liveCommentView) insertNewCommentCompulsorily:[NSString stringWithFormat:@"%@: %@", self.appInitConfig.userNick, message]];
//                onSuccess();
//            }
//                              onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
//                onFailure(code, message);
//            }];
//        }
//    }
}

- (void) sendLike{
    LOG("ASLRBPlaybackViewController::sendLike.");
    [self.room.chat sendLike];
}

#pragma mark -- UI Action

#pragma mark --LifeCircle

- (instancetype)init
{
    self = [super init];
    if (self) {
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationWillResignActiveNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(systemActiveStateChange:) name:UIApplicationDidBecomeActiveNotification object:nil];
    }
    return self;
}

- (instancetype) initWithAppInitConfig:(ASLRBAppInitConfig *)appInitConfig liveInitConfig:(ASLRBLiveInitConfig *)liveInitConfig liveDetail:(NSDictionary *)liveDetail delegate:(id<ASLRBLiveRoomLoginDelegate>)delegate {
    self = [self init];
    if (self) {
        _liveDetail = liveDetail;
        _appInitConfig = appInitConfig;
        _liveInitConfig = liveInitConfig;
        _liveModel = [[ASLRBLiveInfoModel alloc] init];
        _livePlayerStarted = NO;
        _loginDelegate = delegate;
        _playerViewContentMode = UIViewContentModeScaleAspectFit;
        _liveRoomType = ASLRBLiveRoomTypeLivePlayback;
    }
    return self;
}

- (void)exitLiveRoom{
    [self leaveRoom];
    
    [self.loginDelegate dispatchLogoutTask];
}

- (void)exitLiveRoom:(BOOL)stopLive {
    [self exitLiveRoom];
}

- (void) getLiveRoomAdministers:(void(^)(NSArray* administers))onGotten {
    [self.room getRoomDetail:^(AIRBRoomBasicInfo * _Nullable roomDetail) {
        onGotten([roomDetail.administers copy]);
    }];
}

- (void) getLiveDetail:(void (^)(NSDictionary * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] getLiveDetailWithLiveID:self.liveInitConfig.liveID onSuccess:^(NSDictionary * _Nonnull response) {
        onSuccess([response copy]);
    } onFailure:^(NSString * _Nonnull error) {
        onFailure(error);
    }];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.navigationController.navigationBar.translucent = YES;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    [self.navigationController setNavigationBarHidden:YES];
//    if ([[UIDevice currentDevice].systemVersion floatValue] >=7.0) {//侧滑退出手势
//        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
//            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
//            self.navigationController.interactivePopGestureRecognizer.delegate = self;
//        }
//    }
//    ((ASLRBLiveCommentView*)self.liveCommentView).showLiveSystemMessage = YES;
    [self enterRoom];
}

- (void)viewWillAppear:(BOOL)animated{
    [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
    [super viewWillAppear:animated];
    if(!self.isUISettedUp){
        [self setUpUI];
        [self.view setBackgroundColor:[UIColor blackColor]];
    }
}

- (void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
    [self dismissChildViewController:self.presentedChildViewController animated:YES];
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer{
    return YES;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) enterRoom {
    LOG("ASLRBPlaybackViewController::enterRoom(%@).", self.liveModel.roomID);
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.liveModel.roomID bizType:@"standard_live" bizID:self.liveInitConfig.liveID];
    self.room.delegate = self;
    [self.room enterRoomWithUserNick:self.appInitConfig.userNick extension:self.appInitConfig.userExtension];
}

- (void) leaveRoom {
    if(self.roomEntered){
        LOG("ASLRBPlaybackViewController::leaveRoom(%@).", self.liveModel.roomID);
        self.room.delegate = nil;
        [self.room leaveRoom];
        self.room = nil;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].idleTimerDisabled = NO;
    });
}

#pragma mark - AIRBRoomChannelProtocol

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]) {
        [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorOthers withErrorMessage:[NSString stringWithFormat:@"onAIRBRoomChannelError:(0x%lx, %@)", (long)code, message]];
    }
}

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    LOG("ASLRBPlaybackViewController::onAIRBLivePuhserEvent(%ld, %@).", (long)event, info);
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.room.vodPlayer.delegate = self;
            self.room.vodPlayer.autoPlay = YES;
            self.roomEntered = YES;
            
            [self.room.vodPlayer prepareWithMediaURL:[self.liveDetail valueForKey:@"playback_url"] contentID:self.liveInitConfig.liveID];
            
            [self.playLoadingIndicator show:YES];
            
            dispatch_async(dispatch_get_main_queue(), ^{
//                ((ASLRBLiveCommentView*)self.liveCommentView).showComment = YES;
                [UIApplication sharedApplication].idleTimerDisabled = YES;
            });
            
            if (self.isSwitchingUser) {
                self.isSwitchingUser = NO;
                if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                    [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventSwitchAudienceSucceeded info:@{}];
                }
            }
        }
            break;
        case AIRBRoomChannelEventRoomInfoGotten:{
//            self.liveModel.title = [info valueForKey:@"title"];
            self.liveModel.notice = [info valueForKey:@"notice"];
            self.liveModel.uv = [[info valueForKey:@"uv"] intValue];
            self.liveModel.pv = [[info valueForKey:@"pv"] intValue];
            self.liveModel.onlineCount = [[info valueForKey:@"onlineCount"] intValue];
            [self.room.chat getCurrentChatInfoOnSuccess:^(NSDictionary * _Nonnull info) {
                self.liveModel.likeCount = [[info valueForKey:@"total_like"] intValue];
                [self updateRoomInfo];
            } onFailure:^(NSString * _Nonnull errMessage) {
                
            }];
        }
            break;
        case AIRBRoomChannelEventLeft: {
            self.roomEntered = NO;
            if (self.isSwitchingUser) {
                if ([self.loginDelegate respondsToSelector:@selector(switchLoginedUser:onSuccess:onFailure:)]) {
                    [self.loginDelegate switchLoginedUser:self.switchingUserID onSuccess:^(ASLRBAppInitConfig * _Nonnull appInitConfig) {
                        self.appInitConfig.userNick = self.switchingUserNick;
                        self.appInitConfig.userID = self.switchingUserID;
                        self.switchingUserNick = nil;
                        self.switchingUserID = nil;
                        [self enterRoom];
                    } onFailure:^(NSString * _Nonnull errorMessage) {
                        
                    }];
                }
            }
        }
            break;
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
            NSString* messageType = @"";
            NSData *turnData = [[NSString stringWithFormat:@"%@",[info valueForKey:@"data"]]  dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            NSString* comment = @"";
            switch (type) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    messageType = @"RoomMembersInOut";
                    
                    int32_t onlineCount = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]] intValue];
                    int32_t pv = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"pv"]] intValue];
                    int32_t uv = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"uv"]] intValue];
                    
                    self.liveModel.onlineCount = onlineCount;
                    self.liveModel.pv = pv;
                    self.liveModel.uv = uv;
                    [self updateRoomInfo];
                    
                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
                    if (enter) {
                        NSString* nick = [dataDic valueForKey:@"nick"];
                        if (nick.length >= 16) {
                            nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
                        }
//                        [self.liveCommentView insertLiveSystemMessage:[NSString stringWithFormat:@"%@进入了直播间", nick] duration:1.0];
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    messageType = @"RoomTitleUpdated";
//                    self.liveModel.title = [info valueForKey:@"data"];
//                    [self updateRoomInfo];
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated: {
//                    messageType = @"RoomNoticeUpdated";
//                    self.liveModel.notice = [info valueForKey:@"data"];
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        self.noticeButton.text = self.liveModel.notice;
//                    });
                }
//                    [self updateRoomInfo];
                    break;
//                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
//                    messageType = @"LiveCreatedByOther";
//                    break;
//                case AIRBRoomChannelMessageTypeLiveStartedByOther:
//                    messageType = @"LiveStartedByOther";
//                    [self.room.livePlayer start];
////                    [self updatePlayerView];
////                    [self.commentView insertNewComment:@"系统提示: 直播已开始"];
//                    [self.liveCommentView insertLiveSystemMessage:@"系统提示：直播已开始" duration:1.0];
//                    break;
//                case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
//                    self.livePlayerStarted = NO;
//                    messageType = @"LiveStoppedByOther";
//                    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
//                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveEnded info:@{}];
//                    }
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        [self.room.livePlayer stop];
//                        [self.room.livePlayer.playerView removeFromSuperview];
//                        [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
//                        [self.view sendSubviewToBack:self.backgroundView];
//                    });
////                    [self.commentView insertNewComment:@"系统提示: 直播已结束"];
//                    [self.liveCommentView insertLiveSystemMessage:@"系统提示：直播已结束" duration:1.0];
//                }
//                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived:
                    messageType = @"ChatLikeReceived";
                    self.liveModel.likeCount = [[dataDic valueForKey:@"likeCount"] intValue];
                    [self updateRoomInfo];
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived: {
                    NSString* creatorID = [dataDic valueForKey:@"creatorOpenId"];
                    if (creatorID.length > 0 && [creatorID isEqualToString:self.appInitConfig.userID]) {
                        
                    } else {
                        messageType = @"ChatCommentReceived";
                        comment = [NSString stringWithFormat:@"%@: %@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
//                        [((ASLRBLiveCommentView*)self.liveCommentView) insertNewComment:comment];
                    }
                }
                    
                    break;
//                case AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot: {
//                    messageType = @"OneUserWasMuted";
//                    NSString* nick = [dataDic valueForKey:@"muteUserNick"];
//                    if (nick.length >= 16) {
//                        nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
//                    }
//                    if([[dataDic valueForKey:@"mute"] boolValue] == YES){
//                        comment = [NSString stringWithFormat:@" %@被管理员禁言%@秒",nick,[dataDic valueForKey:@"muteTime"]];
//                    }else{
//                        comment = [NSString stringWithFormat:@" %@被管理员取消禁言",nick];
//                    }
////                    [_commentView insertNewComment:comment];
//                    [self.liveCommentView insertLiveSystemMessage:comment duration:1.0];
//                }
//
//                    break;
//                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
//                    messageType = @"OneUserWasKickedOutRoom";
//                    if([[dataDic valueForKey:@"kickUser"] isEqualToString:self.appInitConfig.userID]){
//                        dispatch_async(dispatch_get_main_queue(), ^{
//                            [self exitButtonAction:nil];
//                        });
//                    }else{
//                        NSString* nick = [dataDic valueForKey:@"kickUserName"];
//                        if (nick.length >= 16) {
//                            nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
//                        }
//                        comment = [NSString stringWithFormat:@"%@被管理员踢出直播间",nick];
//                        [self.liveCommentView insertLiveSystemMessage:comment duration:1.0];
//                    }
//                    break;
                case AIRBRoomChannelMessageTypeChatCustomMessageReceived: {
                    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventCustomMessageReceived info:@{
                            @"data" : [info valueForKey:@"data"]
                        }];
                    }
                    break;
                }
//                case AIRBRoomChannelMessageTypeChatAllUsersCommentBannedOrNot: {
//                    BOOL mute = [dataDic[@"mute"] boolValue];
//                    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
//                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventAllCommentsBannedOrNot info:@{@"ban" : @(mute)}];
//                    }
//
////                    [_commentView insertNewComment:mute ? @"系统: 管理员开启了全体禁言" : @"系统: 管理员取消了全体禁言"];
//                    [self.liveCommentView insertLiveSystemMessage:mute ? @"系统: 管理员开启了全体禁言" : @"系统: 管理员取消了全体禁言" duration:1.0];
//                    if (mute) {
//                        dispatch_async(dispatch_get_main_queue(), ^{
//                            _sendField.text = nil;
//                            _sendField.userInteractionEnabled = NO;
//                            _sendField.placeholder = @"主播已开启全员禁言…";
//                        });
//                    } else {
//                        dispatch_async(dispatch_get_main_queue(), ^{
//                            _sendField.userInteractionEnabled = YES;
//                            _sendField.placeholder = @"和主播说点什么…";
//                        });
//                    }
//                    break;
//                }
                default:

                    break;
            };
            break;
        }
            
        default:
            break;
    }
}

- (void) onAIRBVodPlayerErrorWithCode:(AIRBErrorCode)code message:(NSString *)msg {
    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]){
        [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorLivePlayerError withErrorMessage:[NSString stringWithFormat:@"0x%lx, %@", (long)code, msg]];
    }
}
    
- (void) onAIRBVodPlayerEvent:(AIRBVodPlayerEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBVodPlayerEventPrepareDone: {
            self.livePlayerStarted = YES;
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.playerViewContentMode == UIViewContentModeScaleAspectFit) {
                    self.room.vodPlayer.contentMode = AIRBVideoViewContentModeAspectFit;
                } else if (self.playerViewContentMode == UIViewContentModeScaleAspectFill) {
                    self.room.vodPlayer.contentMode = AIRBVideoViewContentModeAspectFill;
                } else if (self.playerViewContentMode == UIViewContentModeScaleToFill) {
                    self.room.vodPlayer.contentMode = AIRBVideoViewContentModeFill;
                }
                
                if (_playerTransitionImageView) {
                    [self.playerTransitionImageView removeFromSuperview];
                    self.playerTransitionImageView = nil;
                }
                
//                if (_backgroundView) {
//                    [self.backgroundView removeFromSuperview];
//                    self.backgroundView = nil;
//                }
                
                [self.view addSubview:self.room.vodPlayer.playerView];
                [self.view addSubview:self.room.vodPlayer.playerControlView];
                [self.room.vodPlayer.playerControlView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.left.equalTo(self.view.mas_left);
                    make.top.equalTo(self.view.mas_top);
                    make.right.equalTo(self.view.mas_right);
                    if (@available(iOS 11.0, *)) {
                        make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
                    } else {
                        make.bottom.equalTo(self.view.mas_bottom);
                    }
                }];
                [self.view sendSubviewToBack:self.room.vodPlayer.playerControlView];
                [self.view sendSubviewToBack:self.room.vodPlayer.playerView];
                [self.view setBackgroundColor:[UIColor clearColor]];
                
                [self.playLoadingIndicator show:NO];
            });
            
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerStartPlaying info:nil];
            }
        }
            
            break;
            
        case AIRBVodPlayerEventStartLoading: {
            [self.playLoadingIndicator show:YES];
        }
            break;
            
        case AIRBVodPlayerEventEndLoading: {
            [self.playLoadingIndicator show:NO];
        }
            
        default:
            break;
    }
}

//#pragma mark --UITextFieldDelegate
//
//- (BOOL)textFieldShouldReturn:(UITextField *)textField {
//    [self.sendField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
//
//    if (textField.text.length > 0) {
//        [self sendComment:textField.text onSuccess:^{
//        } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
//        }];
//    }
//    self.sendField.text = nil;
//    return YES;
//}
//
//- (BOOL)textFieldShouldClear:(UITextField *)textField{
//     return YES;
//}

//#pragma mark --KVO
//
//- (void)keyBoardWillShow:(NSNotification *) note {
//    // 获取用户信息
//    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
//    // 获取键盘高度
//    CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
//    CGFloat keyBoardHeight = keyBoardBounds.size.height;
//    __weak typeof(self) weakSelf = self;
////    [self.commentView mas_remakeConstraints:^(MASConstraintMaker *make) {
////        if (@available(iOS 11.0, *)) {
////            make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-10 - keyBoardHeight);
////        } else {
////            make.bottom.equalTo(weakSelf.view).with.offset(-10 - keyBoardHeight);
////        }
////        make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
////        make.height.mas_equalTo(230);
////        make.width.mas_equalTo(260);
////    }];
//    if(!_sendField){
//        return;
//    }
//    // 获取键盘动画时间
//    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
//
//    // 定义好动作
////    CGFloat offset = [UIScreen mainScreen].bounds.size.height - self.sendField.frame.origin.y - self.sendField.frame.size.height;
//    void (^animation)(void) = ^void(void) {
//        if(self->_sendField.isEditing == YES){
//            self->_sendField.transform = CGAffineTransformMakeTranslation(0, -keyBoardHeight);
//            self->_sendField.layer.cornerRadius = 2;
//            self->_sendField.backgroundColor = [UIColor colorWithWhite:1 alpha:0.3];
//            [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
//                make.left.equalTo(weakSelf.view);
//                make.right.equalTo(weakSelf.view);
//                if (@available(iOS 11.0, *)) {
//                    make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-3);
//                } else {
//                    make.bottom.equalTo(weakSelf.view).with.offset(-3);
//                }
//                make.height.mas_equalTo(40);
//            }];
//        }
//    };
//
//    if (animationTime > 0) {
//        [UIView animateWithDuration:animationTime animations:animation];
//    } else {
//        animation();
//    }
//}
//
//- (void)keyBoardWillHide:(NSNotification *) note {
//    // 获取用户信息
//    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
//    // 获取键盘动画时间
//    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
//    __weak typeof(self) weakSelf = self;
////    [self.commentView mas_remakeConstraints:^(MASConstraintMaker *make) {
////        if (@available(iOS 11.0, *)) {
////            make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-59);
////        } else {
////            make.bottom.equalTo(weakSelf.view).with.offset(-59);
////        }
////        make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
////        make.height.mas_equalTo(230);
////        make.width.mas_equalTo(260);
////    }];
//    if(!_sendField){
//        return;
//    }
//    // 定义好动作
//    void (^animation)(void) = ^void(void) {
//        self->_sendField.transform = CGAffineTransformIdentity;
//        self->_sendField.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
//        self->_sendField.layer.cornerRadius = 20;
//        [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
//            if (@available(iOS 11.0, *)) {
//                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-9);
//            } else {
//                make.bottom.equalTo(weakSelf.view).with.offset(-9);
//            }
//            make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
//            make.width.equalTo(weakSelf.view).multipliedBy(1/3.0);
//            make.height.mas_equalTo(40);
//        }];
//    };
//
//    if (animationTime > 0) {
//        [UIView animateWithDuration:animationTime animations:animation];
//    } else {
//        animation();
//    }
//}

- (void)systemActiveStateChange:(NSNotification *) note {
    if (note.name == UIApplicationDidBecomeActiveNotification) {
//        if (self.livePlayerStarted) {
//            [self.room.livePlayer refresh];
//        }
    } else if (note.name == UIApplicationWillResignActiveNotification) {
//        if (self.livePlayerStarted) {
//            [self.room.livePlayer pause];
//        }
    }
}

#pragma mark --UpdateUI

-(void)updateRoomInfo{
//    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
//        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveDataUpdated info:@{
//            @"onlineCount" : @(self.liveModel.onlineCount),
//            @"likeCount" : @(self.liveModel.likeCount),
//            @"uv" : @(self.liveModel.uv),
//            @"pv" : @(self.liveModel.pv)
//        }];
//    }
    dispatch_async(dispatch_get_main_queue(), ^{
        if(!_upperLeftCustomizedViewHolder){
            [self.liveInfoViewHolder updateLikeCount:self.liveModel.likeCount];
            [self.liveInfoViewHolder updatePV:self.liveModel.pv];
        }
//        if(self->_noticeButton){
//            self.noticeButton.text = self.liveModel.notice;
//        }
    });
}

//#pragma mark - 点赞动画
//
//- (void)likeAnimation {
//    //在主线程上调用该方法
//    UIImageView *imageView = [[UIImageView alloc] init];
//    CGRect frame = self.view.frame;
//    // 初始frame，即设置了动画的起点
//    imageView.frame = CGRectMake(self.likeButton.frame.origin.x, self.likeButton.frame.origin.y,30,30);
//    // 初始化imageView透明度为0
//    imageView.alpha =0;
//    imageView.backgroundColor = [UIColor clearColor];
//    imageView.clipsToBounds = YES;
//    // 用0.2秒的时间将imageView的透明度变成1.0，同时将其放大1.3倍，再缩放至1.1倍，这里参数根据需求设置
//    [UIView animateWithDuration:0.2 animations:^{
//        imageView.alpha =1.0;
////        imageView.frame = CGRectMake(frame.size.width -40, frame.size.height -90,30,30);
//        CGAffineTransform transfrom = CGAffineTransformMakeScale(1.3,1.3);
//        imageView.transform = CGAffineTransformScale(transfrom,1,1);
//    }];
//    [self.view addSubview:imageView];
//    // 随机产生一个动画结束点的X值
//    CGFloat finishX = frame.size.width - round(random() %200);
//    // 动画结束点的Y值
//    CGFloat finishY = 200;
//    // imageView在运动过程中的缩放比例
//    CGFloat scale = round(random() %2) +0.7;
//    // 生成一个作为速度参数的随机数
//    CGFloat speed =1/ round(random() %900) +0.6;
//    // 动画执行时间
//    NSTimeInterval duration =4* speed;
//    // 如果得到的时间是无穷大，就重新附一个值（这里要特别注意，请看下面的特别提醒）
//    if(duration == INFINITY) duration =2.412346;
//    // 开始动画
//    [UIView beginAnimations:nil context:(__bridge void *_Nullable)(imageView)];
//    // 设置动画时间
//    [UIView setAnimationDuration:duration];
//    // 拼接图片名字
//    imageView.image = [UIImage imageNamed:@"img-like_send.png" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
//    // 设置imageView的结束frame
//    imageView.frame =CGRectMake( finishX, finishY,30* scale,30* scale);
//    // 设置渐渐消失的效果，这里的时间最好和动画时间一致
//    [UIView animateWithDuration:duration animations:^{
//        imageView.alpha =0;
//    }];
//    // 结束动画，调用onAnimationComplete:finished:context:函数
//    [UIView setAnimationDidStopSelector:@selector(onAnimationComplete:finished:context:)];
//    // 设置动画代理
//    [UIView setAnimationDelegate:self];
//    [UIView commitAnimations];
//}
///// 动画完后销毁iamgeView
//- (void)onAnimationComplete:(NSString*)animationID finished:(NSNumber*)finished context:(void*)context{
//    UIImageView *imageView = (__bridge UIImageView*)(context);
//    [imageView removeFromSuperview];
//    imageView = nil;
//}

#pragma mark --ASLRBLiveRoomBottomViewActionsDelegate

- (void) onShareButtonClicked {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventShareButtonDidClicked info:@{}];
    }
}

- (void) onLikeSent {
    [self sendLike];
}

@end
