//
//  AIRBDAnchorViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import "ASLRBAnchorViewController.h"
#import <Masonry/Masonry.h>
#import "../CommonViews/UserList/ASLRBUserListViewController.h"
#import "ASLRBLogger.h"
#import "../CommonModels/ASLRBRoomInfoModel.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
 
#import "../LiveRoomMoreInteractionPanel/ASLRBMorePanelView.h"
#import "../LiveRoomBottomViews/ASLRBAnchorLiveRoomBottomViewHolder.h"
#import "../LiveComment/ASLRBLiveCommentView.h"
#import "../CommonViews/DetailsButton/ASLRBDetailsButton.h"
#import "../CommonViews/InsetLabel/ASLRBEdgeInsetLabel.h"

#import "../LiveRoomSetup/ASLRBLiveInitConfig.h"
#import "../LiveRoomSetup/ASLRBAppInitConfig.h"
#import "../CommonTools/UIViewController+ASLRBCustomChildVC.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"

#import "../CommonTools/UIViewController+ASLRBCustomChildVC.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
#import "ASLRBLiveRoomLoginDelegate.h"
#import "../CommonTools/ASLRBResourceManager.h"
#import "../LiveRoomSetup/ASLRBLiveBusinessInfo.h"
#import "../LiveRoomSetup/ASLRBLiveRoomMiddleViewsConfig.h"
#import "ASLRBLiveRoomUserModel.h"
#import "../LivePrestartViews/ASLRBLivePrestartViewsHolderProtocol.h"
#import "../LivePrestartViews/ASLRBLivePrestartViewsHolder.h"
#import "../CommonTools/ASLRBCommonMacros.h"
#import "../LiveRoomMiddleViews/ASLRBLivePushStatusView.h"
#import "../LiveRoomMiddleViews/ASLRBLiveRoomMemberButton.h"
#import "../LiveRoomMiddleViews/ASLRBLiveRestartPushAlertController.h"
#import "../LiveRoomMiddleViews/ASLRBLivePushLoadingIndicatorView.h"
#import "../LiveRoomSetup/ASLRBLiveCommentViewConfig.h"
#import "../LiveComment/ASLRBLiveCommentModel.h"

#import "ASLSBSceneTeacherBigClass.h"
//#import "../../../standard-classroom-ios/AliStandardClassroomBundle/ASCRBClassroom/ClassScene/ASLSBSceneTeacherBigClass.h"

@interface ASLRBAnchorViewController ()<UITextFieldDelegate,AIRBRoomChannelDelegate,UIGestureRecognizerDelegate, AIRBLivePusherDelegate,AIRBDUserListViewDelegate, ASLRBLiveRoomBottomViewActionsDelegate, ASLSBSceneUIDelegate>

@property(nonatomic, assign) BOOL isUISettedUp;

//@property (strong, nonatomic) UIView* membersButton;
//@property (strong, nonatomic) ASLRBDetailsButton* noticeButton;

@property (assign, atomic)    BOOL roomEntered;
@property (assign, nonatomic) BOOL livePusherStarted;
@property (assign, nonatomic) BOOL morePanelShowed;

@property(strong,nonatomic) ASLRBLiveInfoModel* liveModel;


@property (strong, nonatomic) ASLRBUserListViewController* userListViewController;
@property (strong, nonatomic) UIViewController* beautyViewController;

@property (weak, nonatomic) id<ASLRBLiveRoomLoginDelegate> loginDelegate;
@property (nonatomic, strong) ASLRBAppInitConfig* appInitConfig;
@property (nonatomic, strong) ASLRBLiveInitConfig* liveInitConfig;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property (strong, nonatomic) ASLRBLivePushStatusView* pushStatusView;
@property (strong, nonatomic) ASLRBLivePushLoadingIndicatorView* pushLoadingIndicator;

@property(nonatomic, strong) ASLSBSceneTeacherBigClass* liveScene;

@end

@implementation ASLRBAnchorViewController

@synthesize upperLeftCustomizedViewHolder = _upperLeftCustomizedViewHolder;
@synthesize upperRightCustomizedViewHolder = _upperRightCustomizedViewHolder;
@synthesize middleCustomizedViewHolder = _middleCustomizedViewHolder;
@synthesize bottomCustomizedViewHolder = _bottomCustomizedViewHolder;
@synthesize livePrestartCustomizedViewHolder = _livePrestartCustomizedViewHolder;
@synthesize liveCommentView = _liveCommentView;
@synthesize liveDetail = _liveDetail;
@synthesize bottomViewsHolder = _bottomViewsHolder;
@synthesize moreInteractionPanel = _moreInteractionPanel;
@synthesize noticeButton = _noticeButton;
@synthesize membersButton = _membersButton;
@synthesize liveRoomType = _liveRoomType;
@synthesize livePrestartViewsHolder = _livePrestartViewsHolder;

@synthesize linkMicLocalPreview = _linkMicLocalPreview;
@synthesize linkMicJoinedUsers = _linkMicJoinedUsers;
@synthesize enableViewRotation = _enableViewRotation;

@synthesize previewContentMode = _previewContentMode;
@synthesize externalLivePushPreview = _externalLivePushPreview;

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

- (UIView<ASLRBLivePrestartViewsHolderProtocol>*) livePrestartViewsHolder {
    if (!_livePrestartViewsHolder) {
        _livePrestartViewsHolder = [[ASLRBLivePrestartViewsHolder alloc] init];
        ((ASLRBLivePrestartViewsHolder*)_livePrestartViewsHolder).delegate = self;
        [self.view addSubview:_livePrestartViewsHolder];
        [_livePrestartViewsHolder mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self.view);
        }];
        
    }
    return _livePrestartViewsHolder;
}

- (UIView *)membersButton{
    if (_liveInitConfig.middleViewsConfig.liveMembersButtonHidden) {
        return nil;
    }
    if (!_membersButton) {
        
        _membersButton = [[ASLRBLiveRoomMemberButton alloc] init];
        _membersButton.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
        _membersButton.layer.cornerRadius = 12.3;
        [self.view addSubview:_membersButton];
        [_membersButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(56);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(18);
            } else {
                make.top.equalTo(self.view).with.offset(56);
                make.left.equalTo(self.view.mas_left).with.offset(18);
            }
            make.width.mas_equalTo(64.8);
            make.height.mas_equalTo(19.6);
        }];
        
        __weak typeof(self) weakSelf = self;
        ((ASLRBLiveRoomMemberButton*)_membersButton).onMemberButtonClicked = ^{
            if(!weakSelf.userListViewController){
                ASLRBUserListViewController* viewController = [[ASLRBUserListViewController alloc] init];
                viewController.userListViewDelegate = weakSelf;
                weakSelf.userListViewController = viewController;
            }
            CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - 340, weakSelf.view.frame.size.width, 340);
            [weakSelf presentChildViewController:weakSelf.userListViewController animated:YES presentedFrame:frame direction:ASLRBViewControllerPresentFromBottom];
            weakSelf.presentedChildViewController = weakSelf.userListViewController;

            [weakSelf updateUsersList];
        };
    }
    return _membersButton;
}

- (UIButton *)noticeButton{
    if (_liveInitConfig.middleViewsConfig.liveNoticeButtonHidden) {
        return nil;
    }
    if (!_noticeButton) {
        ASLRBDetailsButton* button = [[ASLRBDetailsButton alloc] initWithFrame:CGRectMake(0, 0, 64.8, 19.6) image:[UIImage imageNamed:@"直播-公告" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] title:@"公告"];
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(56);
            } else {
                make.top.equalTo(self.view).with.offset(56);
            }
            make.left.equalTo(self.membersButton.mas_right).with.offset(10);
            make.width.mas_equalTo(64.8);
            make.height.mas_equalTo(19.6);
        }];
        _noticeButton = button;
    }
    return _noticeButton;
}

- (ASLRBLivePushStatusView*)pushStatusView {
    if (_liveInitConfig.middleViewsConfig.livePushStatusLabelHidden || _liveInitConfig.enableLinkMic) { // 连麦直播中隐藏
        return nil;
    }
    
    if (!_pushStatusView) {
        _pushStatusView = [[ASLRBLivePushStatusView alloc] init];
        [self.view addSubview:_pushStatusView];
        [_pushStatusView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.width.mas_equalTo(75);
            make.height.mas_equalTo(30);
            if (@available(iOS 11.0, *)) {
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.right.equalTo(self.view.mas_right);
            }
            make.top.equalTo(self.view.mas_top).with.offset(85);
        }];
    }
    return _pushStatusView;
}

- (ASLRBLivePushLoadingIndicatorView*)pushLoadingIndicator {
    if (_liveInitConfig.middleViewsConfig.livePushLoadingIndicatorHidden) {
        return nil;
    }

    if (!_pushLoadingIndicator) {
        void(^onCreate)(void) = ^{
            _pushLoadingIndicator = [[ASLRBLivePushLoadingIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
            _pushLoadingIndicator.hidden = YES;
            _pushLoadingIndicator.tintColor = [UIColor whiteColor];
            _pushLoadingIndicator.color = [UIColor whiteColor];
            [self.view addSubview:_pushLoadingIndicator];
            [_pushLoadingIndicator mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
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
    return  _pushLoadingIndicator;
}

- (UIView<ASLRBLiveRoomMoreInteractionPanelProtocol> *) moreInteractionPanel{
    if(!_moreInteractionPanel){
        CGRect frame;
        if (@available(iOS 11.0, *)) {
            CGRect safeAreaFrame = self.view.safeAreaLayoutGuide.layoutFrame;
            frame = CGRectMake(safeAreaFrame.origin.x, 0, safeAreaFrame.size.width, 200);
        } else {
            frame = CGRectMake(0, 0, self.view.bounds.size.width, 200);
        }
        
        ASLRBMorePanelView* view = [[ASLRBMorePanelView alloc] initWithFrame:frame];
        view.delegate = self;
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.left.equalTo(self.view);
                make.right.equalTo(self.view.mas_right);
            }
            make.top.equalTo(self.view.mas_bottom);
            make.height.mas_equalTo(200);
        }];
        _moreInteractionPanel = view;
    }
    return _moreInteractionPanel;
}

- (UIView<ASLRBLiveRoomBottomViewsHolderProtocol>*) bottomViewsHolder {
    if (!_bottomViewsHolder) {
        _bottomViewsHolder = [[ASLRBAnchorLiveRoomBottomViewHolder alloc] init];
        ((ASLRBAnchorLiveRoomBottomViewHolder*)_bottomViewsHolder).actionsDelegate = self;
        [self.view addSubview:_bottomViewsHolder];
        
        [_bottomViewsHolder mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.bottom.equalTo(self.view);
                make.left.equalTo(self.view);
                make.right.equalTo(self.view);
            }
            make.height.mas_equalTo(59);
        }];
    }
    return _bottomViewsHolder;
}
- (UIView<ASLRBLiveCommentViewProtocol>*) liveCommentView {
    if(!_liveCommentView){
        void (^createCommentView)(void) = ^{
            _liveCommentView = [[ASLRBLiveCommentView alloc] initWithConfig:_liveInitConfig.liveCommentViewsConfig];
            [self.view addSubview:_liveCommentView];
            [_liveCommentView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                if (@available(iOS 11.0, *)) {
                    make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom).with.offset(-59);
                    make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(10);
                } else {
                    make.bottom.equalTo(self.view).with.offset(-59);
                    make.left.equalTo(self.view.mas_left).with.offset(10);
                }
                make.right.equalTo(self.view.mas_right).with.offset(-1 * kLiveCommentPortraitRightGap);
                make.height.mas_equalTo(kLiveCommentPortraitHeight);
            }];
            [_liveCommentView.superview layoutIfNeeded];
        };
        
        if ([[NSThread currentThread] isMainThread]) {
            createCommentView();
        } else {
            dispatch_sync(dispatch_get_main_queue(), ^{
                createCommentView();
            });
        }
    }
    return _liveCommentView;
}

- (UIView *)upperLeftCustomizedViewHolder{
    if(!_upperLeftCustomizedViewHolder){
        UIView* view = [[UIView alloc] init];
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(8);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft);
            } else {
                make.top.equalTo(self.view).with.offset(8);
                make.left.equalTo(self.view);
            }
            make.width.mas_equalTo(183);
            make.height.mas_equalTo(43);
        }];
        [view.superview layoutIfNeeded];
        _upperLeftCustomizedViewHolder = view;
        view.hidden = YES;
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
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(183);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.top.equalTo(self.view).with.offset(8);
                make.left.equalTo(self.view).with.offset(183);
                make.right.equalTo(self.view.mas_right);
            }
            make.height.mas_equalTo(43);
        }];
        [view.superview layoutIfNeeded];
        _upperRightCustomizedViewHolder = view;
        view.hidden = YES;
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
                make.left.equalTo(weakSelf.view.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(weakSelf.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.top.equalTo(self.view).with.offset(51);
                make.left.equalTo(weakSelf.view.mas_left);
                make.right.equalTo(weakSelf.view.mas_right);
            }
            make.bottom.equalTo(weakSelf.liveCommentView.mas_top);
        }];
        [view.superview layoutSubviews];
        view.hidden = YES;
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
                make.bottom.equalTo(self.view.mas_safeAreaLayoutGuideBottom);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.bottom.equalTo(self.view);
                make.left.equalTo(self.view);
                make.right.equalTo(self.view);
            }
            make.height.mas_equalTo(59);
        }];
        view.hidden = YES;
        _bottomCustomizedViewHolder = view;
    }
    return _bottomCustomizedViewHolder;
}

- (UIView *)livePrestartCustomizedViewHolder{
    if(!_livePrestartCustomizedViewHolder){
        UIView* view = [[UIView alloc] initWithFrame:self.view.bounds];
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        _livePrestartCustomizedViewHolder = view;
    }
    return _livePrestartCustomizedViewHolder;
}

- (void) setPreviewContentMode:(UIViewContentMode)previewContentMode {
    _previewContentMode = previewContentMode;
    self.room.livePusher.contentMode = [self translateContentMode:previewContentMode];
}

- (AIRBVideoViewContentMode)translateContentMode:(UIViewContentMode)contentMode {
    if (contentMode == UIViewContentModeScaleAspectFit) {
        return  AIRBVideoViewContentModeAspectFit;
    } else if (contentMode == UIViewContentModeScaleAspectFill) {
        return AIRBVideoViewContentModeAspectFill;
    } else if (contentMode == UIViewContentModeScaleToFill) {
        return AIRBVideoViewContentModeFill;
    }
    return AIRBVideoViewContentModeAspectFit;
}

- (void) setUpUI{
    
    if(!_upperLeftCustomizedViewHolder){
        [self.view bringSubviewToFront:self.liveInfoViewHolder];
        self.liveInfoViewHolder.alpha = 0.0;
    }else{
        [self.view bringSubviewToFront:self.upperLeftCustomizedViewHolder];
    }
    
    if(!_upperRightCustomizedViewHolder){
        [self.view bringSubviewToFront:self.exitButton];
        self.exitButton.alpha = 0.0;
    }else{
        [self.view bringSubviewToFront:self.upperRightCustomizedViewHolder];
    }
    
    [self.view bringSubviewToFront:self.pushStatusView];
    self.pushStatusView.alpha = 0.0;
    
    if(_middleCustomizedViewHolder){
        [self.view bringSubviewToFront:self.middleCustomizedViewHolder];
    }
    [self.view bringSubviewToFront:self.membersButton];
    [self.view bringSubviewToFront:self.noticeButton];
    self.membersButton.alpha = 0.0;
    self.noticeButton.alpha = 0.0;
    
    [self.view bringSubviewToFront:self.liveCommentView];
    
    if(!_bottomCustomizedViewHolder){
        [self.view bringSubviewToFront:self.bottomViewsHolder];
        self.bottomViewsHolder.alpha = 0.0;
//        [self moreInteractionPanel];
    }else{
        [self.view bringSubviewToFront:self.bottomCustomizedViewHolder];
    }
    
    if(!_livePrestartCustomizedViewHolder){
        [self.view bringSubviewToFront:self.livePrestartViewsHolder];
    }else{
        [self.view bringSubviewToFront:self.livePrestartCustomizedViewHolder];
    }
    
    [self updateLayoutRotated:self.liveInitConfig.pushOrientation == 1];

    self.isUISettedUp = YES;
}

- (void) showUI{
    
    [UIView animateWithDuration:0.3 animations:^{
//        self.livePrestartCustomizedViewHolder.alpha = 0;
        if(!_upperLeftCustomizedViewHolder){
            self.liveInfoViewHolder.alpha = 1;
            self.liveInfoViewHolder.anchorNickLabel.text = self.liveModel.anchorNick;
            if (!self.liveInfoViewHolder.anchorAvatarView.image) {
                self.liveInfoViewHolder.anchorAvatarView.image = [UIImage imageNamed:@"img-user-default" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
            }
        } else {
            self.upperLeftCustomizedViewHolder.hidden = NO;
        }
        if(!_upperRightCustomizedViewHolder){
            self.exitButton.alpha = 1;
        }else{
            self.upperRightCustomizedViewHolder.hidden = NO;
        }
        
        if(!_bottomCustomizedViewHolder){
            self.bottomViewsHolder.alpha = 1;

        }else{
            self.bottomCustomizedViewHolder.hidden = NO;
        }
        
        self.pushStatusView.alpha = 1.0;
        
        if(!_middleCustomizedViewHolder){
            self.membersButton.alpha = 1;
            self.noticeButton.alpha = 1;
        }else{
            self.middleCustomizedViewHolder.hidden = NO;
        }

        self.liveCommentView.alpha = 1;
        
        if (_livePrestartViewsHolder) {
            [self.livePrestartViewsHolder removeFromSuperview];
        }
        
        if (_livePrestartCustomizedViewHolder) {
            [self.livePrestartCustomizedViewHolder removeFromSuperview];
            _livePrestartCustomizedViewHolder = nil;
        }
    }];
}

- (void) setupOnSuccess:(void(^)(NSString* liveID))onSuccess onFailure:(void(^)(NSString* errorMessage))onFailure {
    
    if (self.liveInitConfig.liveID.length > 0) {
        if ([self.appInitConfig.userID isEqualToString:[self.liveDetail valueForKey:@"anchor_id"]]) {
            int32_t status = [[self.liveDetail valueForKey:@"status"] intValue];
            if (status < 2) {
                self.liveModel.roomID = [self.liveDetail valueForKey:@"room_id"];
                self.liveModel.title = [self.liveDetail valueForKey:@"title"];
                self.liveModel.notice = [self.liveDetail valueForKey:@"notice"];
                self.liveModel.anchorNick = [self.liveDetail valueForKey:@"anchor_nick"];
                self.liveModel.anchorID = [self.liveDetail valueForKey:@"anchor_id"];
                
                self.liveScene = [[ASLSBSceneTeacherBigClass alloc] initWithSceneLiveInfo:self.liveDetail role:0 userID:self.appInitConfig.userID nickName:self.appInitConfig.userNick userExtension:self.appInitConfig.userExtension conferenceId:self.liveInitConfig.enableLinkMic ? @"conferenceId" : @""];
                self.liveScene.delegate = self;
                
                onSuccess(self.liveInitConfig.liveID);
                return;
            } else {
                onFailure([NSString stringWithFormat:@"You can't continue this live(%@) when it's ended already.", self.liveInitConfig.liveID]);
            }
        } else {
            onFailure([NSString stringWithFormat:@"You aren't the anchor of this live(%@).", self.liveInitConfig.liveID]);
        }
        self.liveInitConfig.liveID = nil;
    } else {
        LOG("ASLRBAnchorViewController::createLive");
        [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] createLiveWithTitle:self.liveInitConfig.liveBusinessInfo.liveTitle ?: @"互动直播"
                                                                         notice:self.liveInitConfig.liveBusinessInfo.liveNotice
                                                                       coverUrl:self.liveInitConfig.liveBusinessInfo.liveCoverURL ?: @""
                                                                     anchorNick:self.appInitConfig.userNick
                                                                  enableLinkMic:self.liveInitConfig.enableLinkMic
                                                                      extension:self.liveInitConfig.liveBusinessInfo.liveCustomData
                                                                      onSuccess:^(NSDictionary * _Nonnull response){
            self.liveModel.roomID = response ? response[@"room_id"] : @"";
            self.liveModel.anchorNick = self.appInitConfig.userNick;
            self.liveInitConfig.liveID = response ? response[@"live_id"] : @"";
            self.liveModel.notice = response ? response[@"notice"] : @"";
            
            self.liveScene = [[ASLSBSceneTeacherBigClass alloc] initWithSceneLiveInfo:response role:0 userID:self.appInitConfig.userID nickName:self.appInitConfig.userNick userExtension:self.appInitConfig.userExtension conferenceId:self.liveInitConfig.enableLinkMic ? @"conferenceId" : @""];
            self.liveScene.delegate = self;
            
            onSuccess(self.liveInitConfig.liveID);
        } onFailure:^(NSString * _Nonnull error) {
            onFailure(error);
        }];
    }
}

- (void) startLiveAndUpdateConfig:(ASLRBLiveInitConfig *)config{
    LOG("ASLRBAnchorViewController::startLiveAndUpdateConfig");
    
    [self updateLiveConfig:config onSuccess:^{
    } onFailure:^(NSString * _Nonnull errorMessage) {
    }];

    [self.liveScene.pusherEngine startLive];
    
//    [self.pushLoadingIndicator show:YES];
}

- (void) exitLiveRoom {
    LOG("ASLRBAnchorViewController::exitLiveRoom");
    [self leaveRoom:YES];
    
    [self.loginDelegate dispatchLogoutTask];
}

- (void) exitLiveRoom:(BOOL)stopLive {
    LOG("ASLRBAnchorViewController::exitLiveRoom:%d", stopLive);
    [self leaveRoom:stopLive];
    
    [self.loginDelegate dispatchLogoutTask];
}

- (void) updateLiveConfig:(ASLRBLiveInitConfig*)config
                onSuccess:(void (^)(void))onSuccess
                onFailure:(void (^)(NSString* errorMessage))onFailure {
    LOG("ASLRBAnchorViewController::updateLiveConfig");
    if (config) {
        [self.liveScene.pusherEngine updateLiveInfoWithTitle:config.liveBusinessInfo.liveTitle
                                                liveCoverURL:config.liveBusinessInfo.liveCoverURL
                                               customDataStr:config.liveCustomData
                                               customDataDic:config.liveBusinessInfo.liveCustomData
                                                   onSuccess:^{
            onSuccess();
        }
                                                   onFailure:^(NSString * _Nonnull errorMessage) {
            onFailure(errorMessage);
        }];
        
        [self updateLiveBusinessInfo:config.liveBusinessInfo onSuccess:^{
        } onFailure:^(NSString * _Nonnull errorMessage) {
        }];
        
    } else {
        onFailure(@"Invalid input config");
    }
}

- (void) updateLiveBusinessInfo:(ASLRBLiveBusinessInfo *)info onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    LOG("ASLRBAnchorViewController::updateLiveBusinessInfo");
    [self.liveScene.pusherEngine updateLiveBusinessInfoWithLiveID:self.liveInitConfig.liveID title:info.liveTitle notice:info.liveNotice liveCoverURL:info.liveCoverURL customDataDic:info.liveCustomData onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) switchCamera{
    LOG("ASLRBAnchorViewController::switchCamera");
    [self.liveScene.pusherEngine switchCamera];
}

- (void) setFlash:(BOOL)open {
    [self.room.livePusher setFlash:open];
}

- (void)showBeautyPanel{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self onBeautyButtonClicked];
    });
}

- (void) toggleMutedMicrophone {
    [self.liveScene.pusherEngine toggleMutedMicrophone];
}

- (void) mirrorLiveVideo:(BOOL)mirror {
    [self.liveScene.pusherEngine mirrorLiveVideo:mirror];
}

- (void) pauseLiveStreaming:(BOOL)pause {
    [self.liveScene.pusherEngine pauseLiveStreaming:pause];
}

- (void) restartLiveStreaming {
    [self.liveScene.pusherEngine restartLiveStreaming];
    
    [self.pushLoadingIndicator show:YES];
}

- (void) sendComment:(NSString *)message onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(ASLRBLiveRoomError, NSString * _Nonnull))onFailure {
    [self sendComment:message extension:nil onSuccess:^{
        onSuccess();
    } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
        onFailure(code, errorMessage);
    }];
}

- (void) sendComment:(NSString *)message
           extension:(NSDictionary<NSString *,NSString *> *)extension
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(ASLRBLiveRoomError code, NSString * errorMessage))onFailure {
    if (message.length > 0) {
        if (message.length > self.liveInitConfig.maxCommentLength) {
            onFailure(ASLRBLiveRoomCommentLengthExceedsLimit, @"");
        } else {
            [self.room.chat sendComment:message
                              extension:extension
                              onSuccess:^{
                
                [self.liveCommentView insertLiveComment:message commentSenderNick:self.appInitConfig.userNick commentSenderID:self.appInitConfig.userID presentedCompulsorily:YES];
                onSuccess();
            }
                              onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
                onFailure(code, message);
            }];
        }
    }
}

- (void) banCommentsOfUser:(NSString *)userID bannedSeconds:(int32_t)banSeconds ban:(BOOL)ban {
    if (ban) {
        [self.room.chat banCommentWithUserID:userID banTimeInSeconds:banSeconds onSuccess:^{
            LOG("ASLRBAnchorViewController::banCommentsOfUser(%@) success.", userID);
        } onFailure:^(NSString * _Nonnull errorMessage) {
            LOG("ASLRBAnchorViewController::banCommentsOfUser(%@) failed.", userID);
        }];
    } else {
        [self.room.chat cancelBanCommentWithUserID:userID onSuccess:^{
            LOG("ASLRBAnchorViewController::cancelBanComment(%@) success.", userID);
        } onFailure:^(NSString * _Nonnull errorMessage) {
            LOG("ASLRBAnchorViewController::cancelBanComment(%@) failed.", userID);
        }];
    }
}

- (void)banAllComments:(BOOL)ban{
    [self.room.chat banAllComment:ban onSuccess:^{
        LOG("ASLRBAnchorViewController::banAllComments success.");
    } onFailure:^(NSString * _Nonnull errorMessage) {
        LOG("ASLRBAnchorViewController::banAllComments failed(%@).", errorMessage);
    }];
}

- (void) kickUser:(NSString *)userID kickedSeconds:(int32_t)kickedSeconds onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    [self.room kickRoomUserWithUserID:userID kickedSeconds:kickedSeconds onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) sendLike{
    LOG("ASLRBAnchorViewController::sendLike.");
    [self.room.chat sendLike];
}

- (void) sendCustomMessage:(NSString *)message toUsers:(NSArray<NSString *> *)userIDs onSuccess:(nonnull void (^)(void))onSuccess onFailure:(nonnull void (^)(NSString * _Nonnull))onFailure {
    [self.room.chat sendCustomMessage:message toUsers:userIDs onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) sendCustomMessageToAll:(NSString *)message onSuccess:(nonnull void (^)(void))onSuccess onFailure:(nonnull void (^)(NSString * _Nonnull))onFailure{
    [self.room.chat sendCustomMessageToALL:message onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) getLiveUserListWithPageNum:(int32_t)pageNum pageSize:(int32_t)pageSize onSuccess:(void (^)(NSDictionary * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    [self.room getRoomUserListWithPageNum:pageNum pageSize:pageSize onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
        NSMutableArray* userList = [[NSMutableArray alloc] init];
        for (AIRBRoomChannelUser* user in response.userList) {
            [userList addObject:({
                ASLRBLiveRoomUserModel* model = [[ASLRBLiveRoomUserModel alloc] init];
                model.openID = user.openID;
                model.nick = user.nick;
                model.role = user.role;
                model.extension = [user.extension copy];
                model;
            })];
        }
        onSuccess(@{
            @"hasMore" : @(response.hasMore),
            @"totalCount" : @(response.totalCount),
            @"userList" : userList
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) getLiveRoomAdministers:(void(^)(NSArray* administers))onGotten {
    [self.room getRoomDetail:^(AIRBRoomBasicInfo * _Nullable roomDetail) {
        if (roomDetail.administers) {
            onGotten([roomDetail.administers copy]);
        }
    }];
}

- (void) getLiveDetail:(void (^)(NSDictionary * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    [[[AIRBRoomEngine sharedInstance] getRoomSceneLive] getLiveDetailWithLiveID:self.liveInitConfig.liveID onSuccess:^(NSDictionary * _Nonnull response) {
        onSuccess([response copy]);
    } onFailure:^(NSString * _Nonnull error) {
        onFailure(error);
    }];
}

#pragma mark -ASLRBLivePrestartViewsHolderDelegate

- (void) onPrestartStartLiveButtonClicked:(NSString*)liveTitle {
    [self startLiveAndUpdateConfig:({
        ASLRBLiveInitConfig* config = [[ASLRBLiveInitConfig alloc] init];
        ASLRBLiveBusinessInfo* info = [[ASLRBLiveBusinessInfo alloc] init];
        info.liveTitle = liveTitle;
        config.liveBusinessInfo = info;
        config;
    })];
}
- (void) onPrestartSwitchCameraButtonClicked {
    [self.liveScene.pusherEngine switchCamera];
}
- (void) onPrestartBeautyButtonClicked {
    [self onBeautyButtonClicked];
}
- (void) onPrestartExitButtonClicked {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventExitButtonDidClicked info:@{
            @"from" : @"prestart"
        }];
    }
}

#pragma mark --ASLRBMorePanelDelegate

- (void)pauseButtonAction:(UIButton*)sender {
    static BOOL liveStopped = NO;
    if (!liveStopped) {
        liveStopped = YES;
        [self.liveScene.pusherEngine pauseLiveStreaming:YES];
    } else {
        liveStopped = NO;
        [self.liveScene.pusherEngine pauseLiveStreaming:NO];
    }
}

- (void)editButtonAction:(UIButton*)sender {
    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                             message:@"请输入新的直播公告"
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
    [alertController addAction:[UIAlertAction actionWithTitle:@"确定"
                                                        style:UIAlertActionStyleDefault
                                                      handler:^(UIAlertAction * _Nonnull action) {
        UITextField* textField = alertController.textFields.firstObject;
        if([textField.text length] > 0){
            [self.room updateRoomNotice:textField.text onSuccess:^{
                dispatch_async(dispatch_get_main_queue(), ^{
                    ((ASLRBDetailsButton*)self.noticeButton).text = textField.text;
                });
            } onFailure:^(NSString * _Nonnull errorMessage) {

            }];
        }
    }]];
    [alertController addTextFieldWithConfigurationHandler:^(UITextField*_Nonnull textField) {
        textField.placeholder = @"向观众介绍你的直播吧";
    }];
    [self presentViewController:alertController animated:YES completion:nil];
}

-(void)mirrorButtonAction:(UIButton*)sender{
    if(sender.tag == 0){
        [self.liveScene.pusherEngine mirrorLiveVideo:YES];
    }else{
        [self.liveScene.pusherEngine mirrorLiveVideo:NO];
    }
}

- (void) cameraButtonAction:(UIButton*)sender {
    [self.liveScene.pusherEngine switchCamera];
}

- (void)muteButtonAction:(UIButton*)sender {
    [self.liveScene.pusherEngine toggleMutedMicrophone];
}

- (void)banAllCommentsButtonAction:(UIButton *)sender{
    BOOL ban = 1 - sender.tag;
    [self.room.chat banAllComment:ban onSuccess:^{
        
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

//- (void)prestartExitButtonAction:(UIButton*)sender {
//    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
//        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventExitButtonDidClicked info:@{}];
//    }
//}

#pragma mark --LifeCycle

- (id<AIRBRoomChannelProtocol>)room{
    return self.liveScene.room;
}

- (instancetype)init {
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNetworkRechabilityChange:) name:kAIRBReachabilityBecameWiFiNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNetworkRechabilityChange:) name:kAIRBReachabilityBecameWWANNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleNetworkRechabilityChange:) name:kAIRBReachabilityBecameUnrechableNotification object:nil];
    }
    return self;
}

- (instancetype) initWithAppInitConfig:(ASLRBAppInitConfig *)appInitConfig
                        liveInitConfig:(ASLRBLiveInitConfig *)liveInitConfig
                            liveDetail:(NSDictionary *)liveDetail
                              delegate:(id<ASLRBLiveRoomLoginDelegate>)delegate {
    self = [self init];
    if (self) {
        _appInitConfig = appInitConfig;
        _liveInitConfig = liveInitConfig;
        _liveDetail = liveDetail;
        _loginDelegate = delegate;
        _loginDelegate.delegate = self;
        _liveModel = [[ASLRBLiveInfoModel alloc] init];
        _liveRoomType = ASLRBLiveRoomTypeLivePushing;
        _previewContentMode = UIViewContentModeScaleAspectFill;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.navigationController.navigationBar.translucent = YES;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    [self.navigationController setNavigationBarHidden:YES];
//    if ([[UIDevice currentDevice].systemVersion floatValue] >= 7.0) {//侧滑退出手势
//        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
//            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
//            self.navigationController.interactivePopGestureRecognizer.delegate = self;
//        }
//    }
    LOG("ASLRBAnchorViewController::enterRoom when viewDidLoad.");
    [self enterRoom];
}

- (void)viewWillAppear:(BOOL)animated{
    LOG("ASLRBAnchorViewController::viewWillAppear.");
    [super viewWillAppear:animated];
    if(!self.isUISettedUp){
        [self setUpUI];
        [self.view setBackgroundColor:[UIColor blackColor]];
    }
}

- (void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
    LOG("ASLRBAnchorViewController::viewDidDisappear.");
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
    if (self.presentedChildViewController == self.liveScene.pusherEngine.faceBeautyConfigViewController) {
        if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventFaceBeautyViewControllerWillDisappear info:nil];
        }
//        [self hiddenAllSubviewsExceptBeautyViews:NO];
    }
    [self dismissChildViewController:self.presentedChildViewController animated:YES];
    
//    self.commentView.hidden = !self.commentView.hidden;
    if(_morePanelShowed){
        [UIView animateWithDuration:0.2 animations:^{
            CGRect frame = self.moreInteractionPanel.frame;
            frame.origin.y = self.view.bounds.size.height;
            self.moreInteractionPanel.frame = frame;
        }];
        _morePanelShowed = NO;
    }
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer{
    return YES;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    LOG("ASLRBAnchorViewController::dealloc.");
}

- (void) enterRoom {
    LOG("ASLRBAnchorViewController::enterRoom(%@).", self.liveModel.roomID);
    [self.liveScene enterRoom];
//    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.liveModel.roomID bizType:@"standard_live" bizID:self.liveInitConfig.liveID];
//    self.room.delegate = self;
//    [self.room enterRoomWithUserNick:self.appInitConfig.userNick extension:self.appInitConfig.userExtension];
    if(self.liveModel){
        [self.room updateRoomTitle:self.liveModel.title onSuccess:^{
            
        } onFailure:^(NSString * _Nonnull errorMessage) {

        }];
    }
}

- (void) leaveRoom:(BOOL)stopLive {
    if (self.roomEntered) {
        LOG("ASLRBAnchorViewController::leaveRoom(%@).", self.liveModel.roomID);
        [self.room leaveRoom];
        self.roomEntered = NO;
        self.room = nil;
    }
    
    if (self.liveInitConfig.liveID.length > 0 && stopLive) {
        [self.liveScene.pusherEngine stopLiveOnSuccess:^{
            LOG("ASLRBAnchorViewController::stopLive succeeded.");
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLiveEnded info:@{}];
            }
        } onFailure:^(NSString * _Nonnull error) {
            LOG("ASLRBAnchorViewController::stopLive failed(%@).", error);
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]) {
                [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorLivePusherError withErrorMessage:[NSString stringWithFormat:@"failed to stop live(%@)", error]];
            }
        }];
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].idleTimerDisabled = NO;
    });
}

- (void) queryAndShowHistoryComments {
    int32_t count = self.liveInitConfig.liveCommentViewsConfig.countOfHistoryCommentsWhenEntered;
    if (count > 0 && count <= 100) {
        [self.room.chat queryCommentsWithSortedType:(AIRBRoomChatCommentsSortedTypeTimestampDescending) pageNum:1 pageSize:count onSuccess:^(AIRBRoomChannelCommentsResponse * _Nonnull response) {
            __weak typeof(self) weakSelf = self;
            [response.commentList enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(AIRBRoomChannelComment * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                [weakSelf.liveCommentView insertLiveComment:obj.content commentSenderNick:obj.creatorNick commentSenderID:obj.creatorId presentedCompulsorily:NO];
            }];
        } onFailure:^(NSString * _Nonnull errorMsg) {
            LOG("ASLRBAudienceViewController::failed to queryAndShowHistoryComments(%@)", errorMsg);
        }];
    }
}

#pragma mark - ItemsViewdelegate

-(void)useItem:(NSString *)itemID{
    if ([itemID length] > 0) {
        dispatch_async(dispatch_get_main_queue(), ^{
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"管理该成员" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
            [alert addAction:[UIAlertAction actionWithTitle:@"禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat banCommentWithUserID:itemID banTimeInSeconds:300 onSuccess:^{

                } onFailure:^(NSString * _Nonnull errorMessage) {

                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat cancelBanCommentWithUserID:itemID onSuccess:^{

                } onFailure:^(NSString * _Nonnull errorMessage) {

                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"踢出直播间" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room kickRoomUserWithUserID:itemID onSuccess:^{
                    [self updateUsersList];
                } onFailure:^(NSString * _Nonnull errorMessage) {

                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil]];
            alert.popoverPresentationController.sourceRect = self.userListViewController.view.frame;
            alert.popoverPresentationController.sourceView = self.userListViewController.view;
            [self presentViewController:alert animated:YES completion:nil];
        });
    }
}

#pragma mark - AIRBRoomChannelProtocol

- (void) uiActionOnAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]) {
        [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorOthers withErrorMessage:[NSString stringWithFormat:@"onAIRBRoomChannelError:(0x%lx, %@)", (long)code, message]];
    }
}

- (void) uiActionOnAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    LOG("ASLRBAnchorViewController::onAIRBRoomChannelEvent(%ld, %@).", (long)event, info);
    switch (event) {
        case AIRBRoomChannelEventEntered: {
//            self.room.livePusher.delegate = self;
            self.roomEntered = YES;
            if (self.liveInitConfig.appGroupID.length > 0) {
                [self.liveScene.pusherEngine startScreenCaptureWithOrientation:self.liveInitConfig.pushOrientation appGroupID:self.liveInitConfig.appGroupID];
            } else {
                [self.liveScene.pusherEngine startPreview:self.externalLivePushPreview pushOrientation:self.liveInitConfig.pushOrientation];
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [UIApplication sharedApplication].idleTimerDisabled = YES;
            });
        }
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
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
            
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                NSDictionary* extension = [self.liveDetail objectForKey:@"extension"];
                if (extension && [extension isKindOfClass:[NSDictionary class]]) {
                    [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveExtensionUpdated info:extension];
                }
            }
            
            [self queryAndShowHistoryComments];
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
//                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
//                    NSString* userID = [dataDic valueForKey:@"userId"];
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
                        [self.liveCommentView insertLiveSystemMessage:[NSString stringWithFormat:@"%@进入了直播间", nick]];
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated: {
                    self.liveModel.notice = [info valueForKey:@"data"];
                    dispatch_async(dispatch_get_main_queue(), ^{
                        ((ASLRBDetailsButton*)self.noticeButton).text = self.liveModel.notice;
                    });
                }
                    break;
                    
                case AIRBRoomChannelMessageTypeRoomExtensionUpdated: {
                    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                        NSData* dicData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
                        NSDictionary* extension = [NSJSONSerialization JSONObjectWithData:dicData options:NSJSONReadingMutableContainers error:nil];
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveExtensionUpdated info:extension];
                    }
                    break;
                }
                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    messageType = @"LiveStartedByOther";
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther: {
                    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLiveEnded info:@{}];
                    }
                }
                    break;
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
//                        [self.liveCommentView insertLiveComment:[dataDic valueForKey:@"content"] commentSenderNick:[dataDic valueForKey:@"creatorNick"] commentSenderID:creatorID presentedCompulsorily:NO];
                        [self.liveCommentView insertLiveComment:({
                            ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
                            model.sentContent = [dataDic valueForKey:@"content"];
                            model.senderNick = [dataDic valueForKey:@"creatorNick"];
                            model.senderID = creatorID;
                            model.extension = [dataDic valueForKey:@"extension"];
                            model;
                        }) presentedCompulsorily:NO];
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot: {
                    messageType = @"OneUserWasMuted";
                    BOOL mute = [[dataDic valueForKey:@"mute"] boolValue];
                    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventYourCommentsBannedOrNot info:@{
                            @"ban" : @(mute)
                            
                        }];
                    }
                    
                    NSString* nick = [dataDic valueForKey:@"muteUserNick"];
                    if (nick.length >= 16) {
                        nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
                    }
                    if(mute){
                        comment = [NSString stringWithFormat:@" %@被管理员禁言%@秒",nick,[dataDic valueForKey:@"muteTime"]];
                    }else{
                        comment = [NSString stringWithFormat:@"%@被管理员取消禁言",nick];
                    }
//                    [_commentView insertNewComment:comment];
                    [self.liveCommentView insertLiveSystemMessage:comment];
                }
                    
                    break;
                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut: {
                    messageType = @"OneUserWasKickedOutRoom";
                    NSString* nick = [dataDic valueForKey:@"kickUser"];
                    if (nick.length >= 16) {
                        nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
                    }
                    comment = [NSString stringWithFormat:@"%@被管理员踢出了直播间", nick];
//                    [_commentView insertNewComment:comment];
                    [self.liveCommentView insertLiveSystemMessage:comment];
                }
                    break;
                case AIRBRoomChannelMessageTypeChatCustomMessageReceived: {
                    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventCustomMessageReceived info:@{
                            @"data" : [info valueForKey:@"data"]
                        }];
                    }
                    break;
                }
                case AIRBRoomChannelMessageTypeChatAllUsersCommentBannedOrNot: {
                    BOOL mute = [dataDic[@"mute"] boolValue];
                    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventAllCommentsBannedOrNot info:@{
                            @"ban" : @(mute)
                        }];
                    }
                    
//                    [_commentView insertNewComment:mute ? @"系统: 管理员开启了全体禁言" : @"系统: 管理员取消了全体禁言"];
                    [self.liveCommentView insertLiveSystemMessage:mute ? @"管理员开启了全体禁言" : @"管理员取消了全体禁言"];
//                    dispatch_async(dispatch_get_main_queue(), ^{
//                        _sendField.placeholder = mute ? @"您已开启全员禁言…" : @"说点什么…";
//                    });
                    break;
                }
                default:

                    break;
            };
//            [self.commentView insertNewComment:[NSString stringWithFormat:@"开发者信息:%@ ,%@",messageType, [info valueForKey:@"data"] ? : nil]];
            break;
        }
        default:
            break;
    }
}

- (void) onASLSBPusherEngineEvent:(ASLSBPusherEngineEvent)event info:(NSDictionary*)info{
    switch (event) {
        case ASLSBPusherEngineEventPreviewStarted:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.view addSubview:self.liveScene.pusherEngine.localPreview];
                [self.view sendSubviewToBack:self.liveScene.pusherEngine.localPreview];
            });
        }
            break;
        case ASLSBPusherEngineEventStreamResumed:
        case ASLSBPusherEngineEventStreamStarted:{
            dispatch_async(dispatch_get_main_queue(), ^{
                self.livePusherStarted = YES;
                [self showUI];
                ((ASLRBLiveCommentView*)self.liveCommentView).showComment = YES;
                ((ASLRBLiveCommentView*)self.liveCommentView).showLiveSystemMessage = YES;
                [self.liveCommentView insertLiveSystemMessage:event == AIRBLivePusherEventStreamStarted ? @"直播已开始" : @"直播已恢复"];
            });
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStarted info:info];
            }
            
            _pushStatusView.pushStatus = ASLRBLivePushStatusFluent;
            [self.pushLoadingIndicator show:NO];
        }
            break;
        case ASLSBPusherEngineEventStopped:
            
            break;
            
        default:
            break;
    }
}

- (void) uiActionOnAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info{
    LOG("ASLRBAnchorViewController::onAIRBLivePuhserEvent(%ld, %@).", (long)event, info);
    switch (event) {
        case AIRBLivePusherEventPreviewStarted:
            break;
        case AIRBLivePusherEventStreamResumed:
        case AIRBLivePusherEventStreamStarted:
            break;
            
        case AIRBLivePusherEventStreamRestarted: {
            [self.liveCommentView insertLiveSystemMessage:@"直播已恢复"];
            _pushStatusView.pushStatus = ASLRBLivePushStatusFluent;
            [self.pushLoadingIndicator show:NO];
        }
            break;
        case AIRBLivePusherEventStopped: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLiveEnded info:@{}];
            }
            [self.liveCommentView insertLiveSystemMessage:@"直播已停止"];
        }
            break;
        case AIRBLivePusherEventNetworkConnectFailed: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingConnectFailed info:@{}];
            }
            
            _pushStatusView.pushStatus = ASLRBLivePushStatusBrokenOff;
            [self.pushLoadingIndicator show:NO];
            __weak typeof(self) weakSelf = self;
            if (!_liveInitConfig.middleViewsConfig.livePushRestartAlertHidden) {
                [ASLRBLiveRestartPushAlertController showAlertWithMessage:@"直播中断，请检查网络状态后重试" restartAction:^{
                    [weakSelf restartLiveStreaming];
                } parentVC:self];
            }
        }
            break;
        case AIRBLivePusherEventNetworkPoored: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingPoorNetworkStart info:@{}];
            }
            _pushStatusView.pushStatus = ASLRBLivePushStatusStuttering;
        }
            break;
        case AIRBLivePusherEventNetworkRecoveried: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingPoorNetworkEnd info:@{}];
            }
            _pushStatusView.pushStatus = ASLRBLivePushStatusFluent;
        }
            break;
            
        case AIRBLivePusherEventNetworkReconnectStart: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingReconnectStart info:@{}];
            }
            
            _pushStatusView.pushStatus = ASLRBLivePushStatusBrokenOff;
            [self.pushLoadingIndicator show:YES];
        }
            break;
        case AIRBLivePusherEventNetworkReconnectSuccess: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingReconnectSuccess info:@{}];
            }
            _pushStatusView.pushStatus = ASLRBLivePushStatusFluent;
            [self.pushLoadingIndicator show:NO];
            break;
        }
        case AIRBLivePusherEventNetworkReconnectFailed: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingReconnectFailed info:@{}];
            }
            _pushStatusView.pushStatus = ASLRBLivePushStatusBrokenOff;
            [self.pushLoadingIndicator show:NO];
            __weak typeof(self) weakSelf = self;
            if (!_liveInitConfig.middleViewsConfig.livePushRestartAlertHidden) {
                [ASLRBLiveRestartPushAlertController showAlertWithMessage:@"直播中断，请检查网络状态后重试" restartAction:^{
                    [weakSelf restartLiveStreaming];
                } parentVC:self];
            }
        }
            break;
        case AIRBLivePusherEventNetworkConnectionLost: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherStreamingNetworkConnectionLost info:@{}];
            }
            _pushStatusView.pushStatus = ASLRBLivePushStatusBrokenOff;
            [self.pushLoadingIndicator show:NO];
        }
            break;
            
        case AIRBLivePusherEventStreamingUploadBitrateUpdated: {
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAnchorEventLivePusherUploadBitrateUpdated info:info];
            }
        }
            break;
        default:
            break;
    }
}

- (void)uiActionOnAIRBLivePusherError:(AIRBErrorCode)errorCode message:(NSString *)errorMessage{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]){
        [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorLivePusherError withErrorMessage:[NSString stringWithFormat:@"0x%lx, %@", (long)errorCode, errorMessage]];
    }
}

#pragma mark --ASLRBLiveRoomBottomViewActionsDelegate

- (void) onLikeSent {
    [self.room.chat sendLike];
    
    
}

- (void) onCommentSent:(NSString *)comment {
    [self sendComment:comment onSuccess:^{
        
    } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) onShareButtonClicked {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventShareButtonDidClicked info:@{}];
    }
}

- (void) onBeautyButtonClicked {
    if (self.liveInitConfig.pushOrientation != 0) { // 暂时只支持竖屏
        return;
    }

    if (self.liveScene.pusherEngine.faceBeautyConfigViewController) {
        
        CGRect frame;
        if (@available(iOS 11.0, *)) {
            CGRect safeAreaFrame = self.view.safeAreaLayoutGuide.layoutFrame;
            frame = CGRectMake(safeAreaFrame.origin.x, [UIScreen mainScreen].bounds.size.height - 200, safeAreaFrame.size.width, 200);
        } else {
            frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - 200, self.view.frame.size.width, 200);
        }
        
        if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventFaceBeautyViewControllerWillAppear info:nil];
        }
        
        [self presentChildViewController:self.liveScene.pusherEngine.faceBeautyConfigViewController animated:YES presentedFrame:frame direction:(ASLRBViewControllerPresentFromBottom)];
        self.presentedChildViewController = self.liveScene.pusherEngine.faceBeautyConfigViewController;
    }
}

- (void) onMoreInteractionButtonClicked {
    if (!_moreInteractionPanel) {
        [self moreInteractionPanel];
        [self.view layoutIfNeeded];
    }
    [UIView animateWithDuration:0.2 animations:^{
        CGRect frame = self.moreInteractionPanel.frame;
        frame.origin.y = self.view.bounds.size.height - 200 ;
        self.moreInteractionPanel.frame = frame;
    }];
    [self.view bringSubviewToFront:self.moreInteractionPanel];
    _morePanelShowed = YES;
}

#pragma mark --UpdateUI

-(void)updateUsersList{
    [self.room getRoomUserListWithPageNum:1  pageSize:20 onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
        NSMutableArray* userList = [response.userList mutableCopy];
        AIRBRoomChannelUser* userToDelete;
        for(AIRBRoomChannelUser* user in userList){
            if([user.openID isEqualToString:self.appInitConfig.userID]){
                userToDelete = user;
                break;
            }
        }
        [userList removeObject:userToDelete];
        [self.userListViewController updateUsersWithArray:userList];
    } onFailure:^(NSString * _Nonnull errorMessage) {

    }];
}

-(void)updateRoomInfo{
    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
        [self.room.chat getCurrentChatInfoOnSuccess:^(NSDictionary * _Nonnull info) {
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveDataUpdated info:@{
                @"onlineCount" : @(self.liveModel.onlineCount),
                @"likeCount" : @(self.liveModel.likeCount),
                @"commentCount" : @([[info objectForKey:@"total_comment"] intValue]),
                @"uv" : @(self.liveModel.uv),
                @"pv" : @(self.liveModel.pv)
            }];
        } onFailure:^(NSString * _Nonnull errMessage) {
            
        }];
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        if(!_upperLeftCustomizedViewHolder){
            [self.liveInfoViewHolder updateLikeCount:self.liveModel.likeCount];
            [self.liveInfoViewHolder updatePV:self.liveModel.pv];
        }
        if(_noticeButton){
            ((ASLRBDetailsButton*)self.noticeButton).text = self.liveModel.notice;
        }
    });
}

#pragma mark - 屏幕旋转
// 支持旋转
- (BOOL)shouldAutorotate{
    return NO;
}

// 旋转的方向
- (UIInterfaceOrientationMask)supportedInterfaceOrientations{
    if (self.liveInitConfig.pushOrientation == 1){
        return UIInterfaceOrientationMaskLandscapeRight;
    } else {
        return UIInterfaceOrientationMaskPortrait;
    }
}

- (void)updateLayoutRotated:(BOOL)rotated{
    if (_bottomViewsHolder){
        [(ASLRBAnchorLiveRoomBottomViewHolder*)self.bottomViewsHolder updateLayoutRotated:rotated];
    }
    
    if (_livePrestartViewsHolder){
        [(ASLRBLivePrestartViewsHolder*)_livePrestartViewsHolder updateLayoutRotated:rotated];
    }
    
    [(ASLRBLiveCommentView*)_liveCommentView updateLayoutRotated:rotated];
    
    if (!rotated){  // 竖屏
        if (_middleCustomizedViewHolder){
            [self.middleCustomizedViewHolder mas_remakeConstraints:^(MASConstraintMaker *make) {
                if (@available(iOS 11.0, *)) {
                    make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(51);
                    make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft);
                    make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
                } else {
                    make.top.equalTo(self.view).with.offset(51);
                    make.left.equalTo(self.view.mas_left);
                    make.right.equalTo(self.view.mas_right);
                }
                make.bottom.equalTo(self.liveCommentView.mas_top);
            }];
            [self.middleCustomizedViewHolder.superview layoutSubviews];
        }
        return;
    }
    
    // 横屏
    if (_middleCustomizedViewHolder){
        [self.middleCustomizedViewHolder mas_remakeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(51);
                make.left.equalTo(self.liveCommentView.mas_safeAreaLayoutGuideRight);
                make.right.equalTo(self.view.mas_safeAreaLayoutGuideRight);
            } else {
                make.top.equalTo(self.view).with.offset(51);
                make.left.equalTo(self.liveCommentView.mas_right);
                make.right.equalTo(self.view.mas_right);
            }
            make.bottom.equalTo((_bottomViewsHolder ? _bottomViewsHolder : _bottomCustomizedViewHolder).mas_top);
        }];
        [self.middleCustomizedViewHolder.superview layoutSubviews];
    }
}

//- (void) hiddenAllSubviewsExceptBeautyViews:(BOOL)hidden {
//    NSArray* subviews = [self.view subviews];
//    for (UIView* subview in subviews) {
//        if (subview != self.room.livePusher.faceBeautyConfigViewController.view && subview != self.room.livePusher.pusherView) {
//            subview.hidden = hidden;
//        }
//    }
//}

#pragma mark -Network Rechability Notification

- (void)handleNetworkRechabilityChange:(NSNotification*)notification {
    if (notification.name == kAIRBReachabilityBecameWiFiNotification || notification.name == kAIRBReachabilityBecameWWANNotification) {
        if (_pushStatusView.pushStatus == ASLRBLivePushStatusBrokenOff) {
            __weak typeof(self) weakSelf = self;
            if (!_liveInitConfig.middleViewsConfig.livePushRestartAlertHidden) {
                [ASLRBLiveRestartPushAlertController showAlertWithMessage:@"检测到您的网络已恢复，是否恢复推流" restartAction:^{
                    [weakSelf restartLiveStreaming];
                } parentVC:self];
            }
        }
    } else if (notification.name == kAIRBReachabilityBecameUnrechableNotification) {
        _pushStatusView.pushStatus = ASLRBLivePushStatusBrokenOff;
    }
}

#pragma mark -LinkMic

- (BOOL)isCameraOpened{
    return self.liveScene.cameraOpened;
}
- (BOOL)isMicOpened{
    return self.liveScene.micOpened;
}

- (BOOL)isSelfMicAllowed{
    return self.liveScene.selfMicAllowed;
}

- (BOOL)isAllMicAllowed{
    return self.liveScene.allMicAllowed;
}


- (UIView*)linkMicLocalPreview {
    return self.liveScene.linkMicLocalPreview;
}

- (NSDictionary<NSString*, ASLRBLinkMicUserModel*>*)linkMicJoinedUsers{
    return (NSDictionary<NSString*, ASLRBLinkMicUserModel*>*)self.liveScene.linkMicJoinedUsers;
}

/**
 * 打开摄像头
 */
- (void) linkMicOpenCamera{
    [self.liveScene linkMicOpenCamera];
}

/**
 * 关闭摄像头
 */
- (void) linkMicCloseCamera{
    [self.liveScene linkMicCloseCamera];
}

/**
 * 邀请观众加入连麦
 * 发出邀请后, 接收方会收到{@link onASLRBLinkMicInvited:userInvitedList:}事件
 * @param userIDs 被邀请的用户ID
 */
- (void) linkMicInvite:(NSArray<NSString*>*)userIDs{
    [self.liveScene linkMicInvite:userIDs];
}

/**
 * 取消邀请
 * 发出邀请后, 接收方会收到{@link onASLSBLinkMicInviteCanceledForMe}事件
 * @param userIDs 被取消邀请的用户ID
 */
- (void) linkMicCancelInvite:(NSArray<NSString*>*)userIDs{
    [self.liveScene linkMicCancelInvite:userIDs];
}

/**
 * 处理观众的加入连麦申请
 * 在{@link onASLRBLinkMicApplied:userList:}事件中使用
 * @param userID 待处理的用户ID
 * @param agree YES: 同意申请; NO: 拒绝申请;
 */
- (void) linkMicHandleApply:(NSString*)userID agree:(BOOL)agree{
    [self.liveScene linkMicHandleApply:userID agree:agree];
}

/**
 * 踢出连麦
 * @param userIDs 被踢出的用户ID
 */
- (void) linkMicKick:(NSArray<NSString*>*)userIDs{
    [self.liveScene linkMicKick:userIDs];
}

/**
 * 设置预设的旁路直播布局方式（麦下观众观看的画面布局）
 * @param type 预设的布局样式
 * @param userIDs 要展示的用户ID列表:从左上到右下依次排序, 为@""则该位置为空
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) linkMicSetEnumBypassLiveLayout:(ASLRBEnumBypassLiveLayoutType)type
                                userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                              onSuccess:(void(^)(void))onSuccess
                              onFailure:(void(^)(NSString* error))onFailure{
    [self.liveScene linkMicSetEnumBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type userIDs:userIDs onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull error) {
        onFailure(error);
    }];
}

/**
 * 设置自定义的旁路直播布局方式（麦下观众观看的画面布局）
 * @param userModels 要展示的视频流，具体见{@link ASLRBCustomBypassLiveLayoutUserModel}
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) linkMicSetCustomBypassLiveLayout:(NSArray<ASLRBCustomBypassLiveLayoutUserModel*>*) userModels
                                onSuccess:(void(^)(void))onSuccess
                                onFailure:(void(^)(NSString* error))onFailure{
    [self.liveScene linkMicSetCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel *> *)userModels onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull error) {
        onFailure(error);
    }];
}

/**
 * 设置本地预览画面的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetPreviewContentMode:(ASLRBLinkMicViewContentMode)contentMode{
    [self.liveScene linkMicSetPreviewContentMode:(ASLSBLinkMicViewContentMode)contentMode];
}

/**
 * 设置本地观看远端视频流的填充方式，默认为ASLRBLinkMicViewContentModeAuto
 * @param contentMode 填充方式
 */
- (void) linkMicSetRemoteCameraStreamContentMode:(ASLRBLinkMicViewContentMode)contentMode{
    [self.liveScene linkMicSetRemoteCameraStreamContentMode:(ASLSBLinkMicViewContentMode)contentMode];
}

- (void) onASLSBLinkMicEvent:(ASLSBLinkMicEvent)event info:(NSDictionary*)info{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicEvent, (ASLRBLinkMicEvent)event, info, info);
    
    switch (event) {
        case ASLSBLinkMicEventLocalJoinSucceeded:
            break;
            
        default:
            break;
    }
}

- (void) onASLSBLinkMicError:(ASLSBLinkMicError)error message:(NSString*)msg{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicError, (ASLRBLinkMicError)error, message, msg);
}

- (void) onASLSBLinkMicUserJoined:(BOOL)isNewJoined userList:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicUserJoined, userList);
    DELEGATE_ACTION_2ARG(onASLRBLinkMicUserJoined, isNewJoined, userList, userList);
}

- (void) onASLSBLinkMicUserLeft:(NSArray<ASLSBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicUserLeft, (NSArray<ASLRBLinkMicUserModel*>*)userList);
}

- (void) onASLSBLinkMicCameraStreamAvailable:(NSString*)userID view:(UIView*)view{
    [self.liveScene.room.rtc subscribeRemoteVideoStream:YES type:AIRBRTCVideoStreamTypeHigh fromUser:userID];
    BOOL isAnchor = [userID isEqualToString:self.liveScene.sceneModel.roomOwnerId];
    DELEGATE_ACTION_3ARG(onASLRBLinkMicCameraStreamAvailable, userID, isAnchor, isAnchor, view, view);
}

- (void) onASLSBLinkMicRemoteCameraStateChanged:(NSString*)userID open:(BOOL)open{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicRemoteCameraStateChanged, userID, open, open);
}

- (void) onASLSBLinkMicRemoteMicStateChanged:(NSArray<NSString*>*)userList open:(BOOL)open{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicRemoteMicStateChanged, (NSArray<NSString*>*)userList, open, open);
}

- (void) onASLSBLinkMicInvited:(ASLSBLinkMicUserModel*)inviter userInvitedList:(NSArray<ASLSBLinkMicUserModel*>*)userInvitedList{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicInvited, (ASLRBLinkMicUserModel*)inviter, userInvitedList, (NSArray<ASLRBLinkMicUserModel*>*)userInvitedList);
}

- (void) onASLSBLinkMicInviteCanceledForMe{
    DELEGATE_ACTION(onASLRBLinkMicInviteCanceledForMe);
}

- (void) onASLSBLinkMicInviteRejected:(NSArray<ASLSBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicInviteRejected, (NSArray<ASLRBLinkMicUserModel*>*)userList);
}

- (void) onASLSBLinkMicApplied:(BOOL)isNewApplied userList:(NSArray<ASLSBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicApplied, isNewApplied, userList, (NSArray<ASLRBLinkMicUserModel*>*)userList);
}

- (void) onASLSBLinkMicApplyCanceled:(NSArray<ASLSBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicApplyCanceled, (NSArray<ASLRBLinkMicUserModel*>*)userList);
}

- (void) onASLSBLinkMicApplyResponse:(BOOL)approve user:(NSString*)userID{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicApplyResponse, approve, user, userID);
}

- (void) onASLSBLinkMicKicked:(NSArray<ASLSBLinkMicUserModel*>*)userList{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicKicked, (NSArray<ASLRBLinkMicUserModel*>*)userList);
}

- (void) onASLSBLinkMicSelfMicAllowed:(BOOL)allowed{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicSelfMicAllowed, allowed);
}

- (void) onASLSBLinkMicSelfMicChangedByOthers:(BOOL)open{
    if (open) {
        DELEGATE_ACTION(onASLRBLinkMicAnchorInviteToOpenMic);
    } else {
        DELEGATE_ACTION(onASLRBLinkMicSelfMicClosedByAnchor);
    }
}

- (void) onASLSBLinkMicAllMicAllowed:(BOOL)allowed{
    DELEGATE_ACTION_1ARG(onASLRBLinkMicAllMicAllowed, allowed);
}

@end
