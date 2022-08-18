//
//  AIRBDAudienceViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import "ASLRBAudienceViewController.h"
#import <Masonry/Masonry.h>
#import "../CommonModels/ASLRBRoomInfoModel.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "ASLRBLogger.h"
#import "../LiveComment/ASLRBLiveCommentView.h"
#import "../LiveComment/ASLRBLiveSystemMessageModel.h"
#import "../LiveRoomBottomViews/ASLRBAudienceLiveRoomBottomViewHolder.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"
#import "../CommonViews/DetailsButton/ASLRBDetailsButton.h"
#import "../CommonViews/InsetLabel/ASLRBEdgeInsetLabel.h"
#import "../CommonTools/UIViewController+ASLRBCustomChildVC.h"
#import "../CommonTools/ASLRBResourceManager.h"
#import "../LiveRoomSetup/ASLRBLiveInitConfig.h"
#import "../LiveRoomSetup/ASLRBAppInitConfig.h"
#import "../LiveRoomSetup/ASLRBLiveRoomMiddleViewsConfig.h"
#import "../LiveRoomSetup/ASLRBLiveCommentViewConfig.h"
#import "ASLRBLiveRoomLoginDelegate.h"
#import "../LiveComment/ASLRBLiveCommentModel.h"
#import "../LiveFloatingWindow/ASLRBFloatingPlayWindow.h"
#import "../LiveFloatingWindow/ASLRBFloatingPlayWindowProtocol.h"
#import "../CommonTools/ASLRBCommonMacros.h"
#import "ASLSBSceneStudentBigClass.h"
//#import "../../../standard-classroom-ios/AliStandardClassroomBundle/ASCRBClassroom/ClassScene/ASLSBSceneStudentBigClass.h"
#import "../LiveRoomMiddleViews/ASLRBLivePlayLoadingIndicatorView.h"
#import "../LiveRoomSetup/ASLRBLiveRoomBackgroundImageConfig.h"


@interface ASLRBAudienceViewController ()<UITextFieldDelegate,AIRBRoomChannelDelegate,UIGestureRecognizerDelegate, AIRBLivePlayerDelegate, ASLSBSceneUIDelegate,ASLRBFloatingPlayWindowDelegate>

@property(nonatomic, assign) BOOL isUISettedUp;

@property (strong, nonatomic) UIImageView* playerTransitionImageView;

@property (assign,    atomic) BOOL roomEntered;
@property (assign, nonatomic) BOOL isSwitchingUser;
@property (copy, nonatomic) NSString* switchingUserID;
@property (copy, nonatomic) NSString* switchingUserNick;
@property (weak, nonatomic) id<ASLRBLiveRoomLoginDelegate> loginDelegate;
@property (assign, nonatomic) BOOL livePlayerStarted;
@property (assign, atomic) int videoWidth;
@property (assign, atomic) int videoHeight;
@property (nonatomic, strong) ASLRBAppInitConfig* appInitConfig;
@property (nonatomic, strong) ASLRBLiveInitConfig* liveInitConfig;
//@property (nonatomic, strong) ASLRBLiveRoomInitConfig* roomInitConfig;
//@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property(strong,nonatomic) ASLRBLiveInfoModel* liveModel;
@property (strong, nonatomic) ASLRBLivePlayLoadingIndicatorView* playLoadingIndicator;

@property(nonatomic, strong) ASLSBSceneStudentBigClass* liveScene;


@end

@implementation ASLRBAudienceViewController

@synthesize backgroundView = _backgroundView;
@synthesize upperLeftCustomizedViewHolder = _upperLeftCustomizedViewHolder;
@synthesize upperRightCustomizedViewHolder = _upperRightCustomizedViewHolder;
@synthesize middleCustomizedViewHolder = _middleCustomizedViewHolder;
@synthesize bottomCustomizedViewHolder = _bottomCustomizedViewHolder;
@synthesize liveCommentView = _liveCommentView;
@synthesize backgroundImage = _backgroundImage;
@synthesize playerViewContentMode = _playerViewContentMode;
@synthesize liveDetail = _liveDetail;
@synthesize bottomViewsHolder = _bottomViewsHolder;
@synthesize noticeButton = _noticeButton;
@synthesize liveRoomType = _liveRoomType;
@synthesize floatingPlayWindow = _floatingPlayWindow;

@synthesize linkMicLocalPreview = _linkMicLocalPreview;
@synthesize linkMicJoinedUsers = _linkMicJoinedUsers;
@synthesize enableViewRotation = _enableViewRotation;

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

-(id<ASLRBFloatingPlayWindowProtocol>)floatingPlayWindow {
    if (!_floatingPlayWindow) {
        _floatingPlayWindow = [[ASLRBFloatingPlayWindow alloc] init];
        ((ASLRBFloatingPlayWindow*)_floatingPlayWindow).delegate = self;
    }
    return _floatingPlayWindow;
}

- (UIImageView *)backgroundView{
    if(!_backgroundView){
        UIImageView* imageView = [[UIImageView alloc] initWithFrame:self.view.bounds];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
//        __weak typeof(self) weakSelf = self;
        [self.view addSubview:imageView];
        [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.edges.equalTo(self.view);
        }];
        
        _backgroundView = imageView;
    }
    return _backgroundView;
}

- (UIButton *)noticeButton{
    if (_liveInitConfig.middleViewsConfig.liveNoticeButtonHidden) {
        return nil;
    }
    if (!_noticeButton) {
        ASLRBDetailsButton* button = [[ASLRBDetailsButton alloc]initWithFrame:CGRectMake(0, 0, 64.8, 19.6) image:[UIImage imageNamed:@"直播-公告" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] title:@"公告"];
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(56);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(19);
            } else {
                make.top.equalTo(self.view).with.offset(56);
                make.left.equalTo(self.view).with.offset(19);
            }
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

- (UIView<ASLRBLiveCommentViewProtocol>*) liveCommentView {
    if(!_liveCommentView){
        void (^createCommentView)(void) = ^void(void) {
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

- (UIView<ASLRBLiveRoomBottomViewsHolderProtocol>*) bottomViewsHolder {
    if (!_bottomViewsHolder) {
        _bottomViewsHolder = [[ASLRBAudienceLiveRoomBottomViewHolder alloc] init];
        ((ASLRBAudienceLiveRoomBottomViewHolder*)_bottomViewsHolder).actionsDelegate = self;
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
        _middleCustomizedViewHolder = view;
    }
    return _middleCustomizedViewHolder;
}

- (UIView *)bottomCustomizedViewHolder{
    if(!_bottomCustomizedViewHolder){
        UIView* view = [[UIView alloc] initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 59)];
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
        [view.superview layoutSubviews];
        _bottomCustomizedViewHolder = view;
    }
    return _bottomCustomizedViewHolder;
}


- (void) setPlayerViewContentMode:(UIViewContentMode)playerViewContentMode {
    _playerViewContentMode = playerViewContentMode;
    if (playerViewContentMode == UIViewContentModeScaleAspectFit) {
        self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFit;
    } else if (playerViewContentMode == UIViewContentModeScaleAspectFill) {
        self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFill;
    } else if (playerViewContentMode == UIViewContentModeScaleToFill) {
        self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeFill;
    }
}

- (UIView*)playerView {
    return self.liveScene.room.livePlayer.playerView;
}

#pragma mark -Private Methods

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
    
    [self.view bringSubviewToFront:self.playLoadingIndicator];
    if(_middleCustomizedViewHolder){
        [self.view bringSubviewToFront:self.middleCustomizedViewHolder];
    }
    [self.view bringSubviewToFront:self.noticeButton];
    
    [self.view bringSubviewToFront:self.liveCommentView];
    
    if(!_bottomCustomizedViewHolder){
        [self.view bringSubviewToFront:self.bottomViewsHolder];
    }else{
        [self.view bringSubviewToFront:self.bottomCustomizedViewHolder];
    }
    
    if (self.backgroundImageBeforeLiving) {
        self.backgroundView.image = self.backgroundImageBeforeLiving;
    } else if (self.backgroundImage) {
        self.backgroundView.image = self.backgroundImage;
    }
    [self.view sendSubviewToBack:self.backgroundView];
    
    // 初始界面方向
    if ([UIApplication sharedApplication].statusBarOrientation == UIInterfaceOrientationPortrait || !self.enableViewRotation) {
        [self updateLayoutRotated:NO];
    } else{
        [self updateLayoutRotated:YES];
    }
    
    self.isUISettedUp = YES;
}

- (void) setupOnSuccess:(void(^)(NSString* liveID))onSuccess onFailure:(void(^)(NSString* errorMessage))onFailure {
    LOG("ASLRBAudienceViewController::setup.");
    
    if (self.liveInitConfig.liveID.length > 0) {
        if (![self.appInitConfig.userID isEqualToString:[self.liveDetail valueForKey:@"anchor_id"]]) {
            self.liveModel.roomID = [self.liveDetail valueForKey:@"room_id"];
            self.liveModel.title = [self.liveDetail valueForKey:@"title"];
            self.liveModel.notice = [self.liveDetail valueForKey:@"notice"];
            self.liveModel.anchorNick = [self.liveDetail valueForKey:@"anchor_nick"];
            self.liveModel.anchorID = [self.liveDetail valueForKey:@"anchor_id"];
            
            onSuccess(self.liveInitConfig.liveID);
        } else {
            onFailure([NSString stringWithFormat:@"You can't be an audience of your own live(%@).", self.liveInitConfig.liveID]);
        }
    } else {
        onFailure([NSString stringWithFormat:@"Invalid live-id(%@)", self.liveInitConfig.liveID]);
    }
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
            [self.liveScene.room.chat sendComment:message
                              extension:extension
                              onSuccess:^{
                [self.liveCommentView insertLiveComment:({
                    ASLRBLiveCommentModel* model = [[ASLRBLiveCommentModel alloc] init];
                    model.senderNick = self.appInitConfig.userNick;
                    model.senderID = self.appInitConfig.userID;
                    model.sentContent = message;
                    model;
                }) presentedCompulsorily:YES];
                onSuccess();
            }
                              onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
                onFailure(code, message);
            }];
        }
    }
}

- (void) sendLike{
    LOG("ASLRBAudienceViewController::sendLike.");
    [self.liveScene.room.chat sendLike];
}

- (void) sendCustomMessage:(NSString *)message toUsers:(NSArray<NSString *> *)userIDs onSuccess:(nonnull void (^)(void))onSuccess onFailure:(nonnull void (^)(NSString * _Nonnull))onFailure {
    LOG("ASLRBAudienceViewController::sendCustomMessage.");
    [self.liveScene.room.chat sendCustomMessage:message toUsers:userIDs onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}

- (void) sendCustomMessageToAll:(NSString *)message onSuccess:(nonnull void (^)(void))onSuccess onFailure:(nonnull void (^)(NSString * _Nonnull))onFailure{
    LOG("ASLRBAudienceViewController::sendCustomMessageToAll.");
    [self.liveScene.room.chat sendCustomMessageToALL:message onSuccess:^{
        onSuccess();
    } onFailure:^(NSString * _Nonnull errorMessage) {
        onFailure(errorMessage);
    }];
}


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
        _appInitConfig = appInitConfig;
        _liveInitConfig = liveInitConfig;
        _liveDetail = liveDetail;
        _liveModel = [[ASLRBLiveInfoModel alloc] init];
        _livePlayerStarted = NO;
        _loginDelegate = delegate;
        _loginDelegate.delegate = self;
        _playerViewContentMode = UIViewContentModeScaleAspectFill;
        _liveRoomType = ASLRBLiveRoomTypeLivePlaying;
        
        _liveScene = [[ASLSBSceneStudentBigClass alloc] initWithSceneLiveInfo:liveDetail role:1 userID:appInitConfig.userID nickName:appInitConfig.userNick userExtension:appInitConfig.userExtension conferenceId:liveInitConfig.enableLinkMic ? @"conferenceId" : @""];
        _liveScene.delegate = self;
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

- (void) switchAudience:(NSString *)userID nick:(NSString *)nick {
    if (!self.isSwitchingUser && ![self.appInitConfig.userID isEqualToString:userID]) {
        self.switchingUserID = userID;
        self.switchingUserNick = nick;
        self.isSwitchingUser = YES;

        if (self.livePlayerStarted) {
            [self.liveScene.room.livePlayer snapshotAsync];
        } else {
            [self.liveScene.room leaveRoom];
        }
    }
}

- (void) pausePlayer {
    [self.liveScene.room.livePlayer pause];
}

- (void) refreshPlayer {
    [self.liveScene.room.livePlayer refresh];
    
    [self.playLoadingIndicator show:YES];
}

- (void)mute:(BOOL)mute {
    [self.liveScene.room.livePlayer mute:mute];
}

- (void) getLiveRoomAdministers:(void(^)(NSArray* administers))onGotten {
    [self.liveScene.room getRoomDetail:^(AIRBRoomBasicInfo * _Nullable roomDetail) {
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

- (void) enterFloatingMode:(BOOL)enter {
    ((ASLRBFloatingPlayWindow*)self.floatingPlayWindow).playerView = self.liveScene.room.livePlayer.playerView;
    ((ASLRBFloatingPlayWindow*)self.floatingPlayWindow).parentViewControllerView = self.view;
    [((ASLRBFloatingPlayWindow*)self.floatingPlayWindow) enterFloatingMode:enter];
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
    
    // 屏幕旋转
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handleStatusBarOrientationDidChange:) name:UIApplicationDidChangeStatusBarOrientationNotification object:nil];
    
    ((ASLRBLiveCommentView*)self.liveCommentView).showLiveSystemMessage = YES;
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
    if(self->_backgroundView){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self->_backgroundView removeFromSuperview];
            self->_backgroundView = nil;
        });
    }
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
    
    if (_floatingPlayWindow && ((ASLRBFloatingPlayWindow*)_floatingPlayWindow).onFloatingMode) {
        [((ASLRBFloatingPlayWindow*)_floatingPlayWindow) enterFloatingMode:NO];
    }
    
    [self dismissChildViewController:self.presentedChildViewController animated:YES];
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer{
    return YES;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void) enterRoom {
    LOG("ASLRBAudienceViewController::enterRoom(%@).", self.liveModel.roomID);
    self.liveScene.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.liveModel.roomID bizType:@"standard_live" bizID:self.liveInitConfig.liveID];
    self.liveScene.room.delegate = self.liveScene;
    [self.liveScene.room enterRoomWithUserNick:self.appInitConfig.userNick extension:self.appInitConfig.userExtension];
}

- (void) leaveRoom {
    if(self.roomEntered){
        LOG("ASLRBAudienceViewController::leaveRoom(%@).", self.liveModel.roomID);
//        self.liveScene.room.delegate = nil;
        [self.liveScene leaveRoom];
//        self.liveScene.room = nil;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].idleTimerDisabled = NO;
    });
}

- (void)updatePlayerViewContentMode {
    if (self.videoWidth > 0 && self.videoHeight > 0) {
        UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
        if (self.videoWidth > self.videoHeight && (orientation == UIInterfaceOrientationPortrait || orientation == UIInterfaceOrientationPortraitUpsideDown)) {
            self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFit;
        } else if (self.videoWidth < self.videoHeight && (orientation == UIInterfaceOrientationLandscapeLeft || orientation == UIInterfaceOrientationLandscapeRight)) {
            self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFit;
        }
    }
}

- (void) queryAndShowHistoryComments {
    int32_t count = self.liveInitConfig.liveCommentViewsConfig.countOfHistoryCommentsWhenEntered;
    if (count > 0 && count <= 100) {
        [self.liveScene.room.chat queryCommentsWithSortedType:(AIRBRoomChatCommentsSortedTypeTimestampDescending) pageNum:1 pageSize:count onSuccess:^(AIRBRoomChannelCommentsResponse * _Nonnull response) {
            __weak typeof(self) weakSelf = self;
            [response.commentList enumerateObjectsWithOptions:NSEnumerationReverse usingBlock:^(AIRBRoomChannelComment * _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
                [weakSelf.liveCommentView insertLiveComment:obj.content commentSenderNick:obj.creatorNick commentSenderID:obj.creatorId presentedCompulsorily:NO];
            }];
        } onFailure:^(NSString * _Nonnull errorMsg) {
            LOG("ASLRBAudienceViewController::failed to queryAndShowHistoryComments(%@)", errorMsg);
        }];
    }
}

#pragma mark - liveScene uiAction
- (void)uiActionWhenSceneRoomEntered{
//    self.liveScene.room.livePlayer.delegate = self;
    self.liveScene.room.livePlayer.lowDelay = self.liveInitConfig.lowDelayLivePlaying;
    self.liveScene.room.livePlayer.contentMode = self.playerViewContentMode;
    self.roomEntered = YES;

//    [self.liveScene.room.livePlayer start];
    dispatch_async(dispatch_get_main_queue(), ^{
        ((ASLRBLiveCommentView*)self.liveCommentView).showComment = YES;
        [UIApplication sharedApplication].idleTimerDisabled = YES;
    });
    
    if (self.isSwitchingUser) {
        self.isSwitchingUser = NO;
        if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventSwitchAudienceSucceeded info:@{}];
        }
    }
}

- (void)uiActionWhenChatInfoGotten:(BOOL)succeeded errorMessag:(NSString*)errorMessage{
    if (succeeded){
        self.liveModel.likeCount = self.liveScene.sceneModel.likeCount;
        [self updateRoomInfo];
        if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
            
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventYourCommentsBannedOrNot info:@{
                @"ban" : [NSNumber numberWithBool:self.liveScene.sceneModel.isUserCommentBanned]
            }];
            
            //            if ([info objectForKey:@"ban_all"]) {
            [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventAllCommentsBannedOrNot info:@{
                @"ban" : [NSNumber numberWithBool:self.liveScene.sceneModel.isAllCommentBanned]
            }];
            //            }
        }
        
        // 处理输入框显示
        if (self.liveScene.sceneModel.isAllCommentBanned){
            dispatch_async(dispatch_get_main_queue(), ^{
                _bottomViewsHolder.commentInputField.text = nil;
                _bottomViewsHolder.commentInputField.userInteractionEnabled = NO;
                _bottomViewsHolder.commentInputField.placeholder = @"主播已开启全员禁言";
            });
        } else if (self.liveScene.sceneModel.isUserCommentBanned){
            dispatch_async(dispatch_get_main_queue(), ^{
                _bottomViewsHolder.commentInputField.text = nil;
                _bottomViewsHolder.commentInputField.userInteractionEnabled = NO;
                _bottomViewsHolder.commentInputField.placeholder = @"你被管理员禁言";
            });
        }
    } else{
        LOG("ASLRBAudienceViewController::failed to getCurrentChatInfo(%@)", errorMessage);
    }
}

- (void) uiActionWhenSceneStarted {
    [self.playLoadingIndicator show:YES];
}

- (void)uiActionWhenSceneNotStarted{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (self.backgroundImageBeforeLiving) {
            return;
        } else if (self.backgroundImage) {
            return;
        } else  if (!self.liveInitConfig.liveRoomBackgroundImageConfig.defaultBackgroundImageHidden){
            UIImageView* defaultView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"直播未开始" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
            defaultView.contentMode = UIViewContentModeScaleAspectFit;
            [self.backgroundView addSubview:defaultView];
            CGFloat actualWidth = MIN(self.backgroundView.bounds.size.width, self.backgroundView.bounds.size.height);
            [defaultView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.center.equalTo(self.view);
                make.width.mas_equalTo(actualWidth);
                make.height.mas_equalTo(actualWidth);
            }];
            
            UILabel* label = [[UILabel alloc] init];
            label.textAlignment = NSTextAlignmentCenter;
            label.text = @"主播尚未开播，请稍后再来～";
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
            label.textColor = [UIColor whiteColor];
            [defaultView addSubview:label];
            [label mas_makeConstraints:^(MASConstraintMaker *make) {
                make.centerX.equalTo(defaultView);
                make.top.equalTo(defaultView.mas_bottom).offset(-(actualWidth - actualWidth/714*393)/2); // 计算label的位置
                make.width.equalTo(defaultView);
                make.height.mas_equalTo(47);
            }];
        }
    });
}

- (void)uiActionWhenSceneHasEnded{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
        
        for (UIView* subview in self.backgroundView.subviews) {
            [subview removeFromSuperview];
        }
        if (self.backgroundImageAfterLiving) {
            self.backgroundView.image = self.backgroundImageAfterLiving;
        } else if (self.backgroundImage) {
            self.backgroundView.image = self.backgroundImage;
        } else if (!self.liveInitConfig.liveRoomBackgroundImageConfig.defaultBackgroundImageHidden){
            UIImageView* defaultView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"直播未开始" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
            defaultView.contentMode = UIViewContentModeScaleAspectFit;
            [self.backgroundView addSubview:defaultView];
            CGFloat actualWidth = MIN(self.backgroundView.bounds.size.width, self.backgroundView.bounds.size.height);
            [defaultView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.center.equalTo(self.view);
                make.width.mas_equalTo(actualWidth);
                make.height.mas_equalTo(actualWidth);
            }];
            
            UILabel* label = [[UILabel alloc] init];
            label.textAlignment = NSTextAlignmentCenter;
            label.text = @"直播已结束";
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
            label.textColor = [UIColor whiteColor];
            [defaultView addSubview:label];
            [label mas_makeConstraints:^(MASConstraintMaker *make) {
                make.centerX.equalTo(defaultView);
                make.top.equalTo(defaultView.mas_bottom).offset(-(actualWidth - actualWidth/714*393)/2); // 计算label的位置
                make.width.equalTo(defaultView);
                make.height.mas_equalTo(47);
            }];
        }
        [self.view sendSubviewToBack:self.backgroundView];
    });
}

- (void)uiActionWhenSceneEnded{
    self.livePlayerStarted = NO;
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveEnded info:@{}];
    }
    
    [self.playLoadingIndicator show:NO];
    
    dispatch_async(dispatch_get_main_queue(), ^{
//                   [self.liveScene.room.livePlayer stop];
//                   [self.liveScene.room.livePlayer.playerView removeFromSuperview];
        [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
        
        for (UIView* subview in self.backgroundView.subviews) {
            [subview removeFromSuperview];
        }
        if (self.backgroundImageAfterLiving) {
            self.backgroundView.image = self.backgroundImageAfterLiving;
        } else if (self.backgroundImage) {
            self.backgroundView.image = self.backgroundImage;
        } else if (!self.liveInitConfig.liveRoomBackgroundImageConfig.defaultBackgroundImageHidden){
            UIImageView* defaultView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"直播未开始" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil]];
            defaultView.contentMode = UIViewContentModeScaleAspectFit;
            [self.backgroundView addSubview:defaultView];
            CGFloat actualWidth = MIN(self.backgroundView.bounds.size.width, self.backgroundView.bounds.size.height);
            [defaultView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.center.equalTo(self.view);
                make.width.mas_equalTo(actualWidth);
                make.height.mas_equalTo(actualWidth);
            }];
            
            UILabel* label = [[UILabel alloc] init];
            label.textAlignment = NSTextAlignmentCenter;
            label.text = @"直播已结束";
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
            label.textColor = [UIColor whiteColor];
            [defaultView addSubview:label];
            [label mas_makeConstraints:^(MASConstraintMaker *make) {
                make.centerX.equalTo(defaultView);
                make.top.equalTo(defaultView.mas_bottom).offset(-(actualWidth - actualWidth/714*393)/2); // 计算label的位置
                make.width.equalTo(defaultView);
                make.height.mas_equalTo(47);
            }];
        }
        [self.view sendSubviewToBack:self.backgroundView];
    });
//                    [self.commentView insertNewComment:@"系统提示: 直播已结束"];
    [self.liveCommentView insertLiveSystemMessage:@"直播已结束"];
}


- (void) uiActionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)type data:(NSDictionary *)dataDic info:(NSDictionary *)info{
//       AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
       NSString* messageType = @"";
//       NSData *turnData = [[NSString stringWithFormat:@"%@",[info valueForKey:@"data"]]  dataUsingEncoding:NSUTF8StringEncoding];
//       NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
       NSString* comment = @"";
       switch (type) {
           case AIRBRoomChannelMessageTypeRoomMembersInOut:{
               messageType = @"RoomMembersInOut";
               
//               int32_t onlineCount = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]] intValue];
//               int32_t pv = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"pv"]] intValue];
//               int32_t uv = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"uv"]] intValue];
               
               self.liveModel.onlineCount = self.liveScene.sceneModel.onlineCount;
               self.liveModel.pv = self.liveScene.sceneModel.pv;
               self.liveModel.uv = self.liveScene.sceneModel.uv;
               [self updateRoomInfo];
               
               BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
               if (enter) {
                   NSString* nick = [dataDic valueForKey:@"nick"];
                   if (nick.length >= 16) {
                       nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
                   }
//                   [self.liveCommentView insertLiveSystemMessage:];
                   [self.liveCommentView insertLiveSystemMessageModel:({
                       ASLRBLiveSystemMessageModel* model = [[ASLRBLiveSystemMessageModel alloc] init];
                       model.rawMessage = [NSString stringWithFormat:@"%@进入了直播间", nick];
                       model.extension = @{
                           @"userID" : [dataDic valueForKey:@"userId"] ?: @"",
                           @"userNick" : [dataDic valueForKey:@"nick"] ?: @""
                       };
                       model;
                   })];
               }
           }
               break;
           case AIRBRoomChannelMessageTypeRoomTitleUpdated:
               messageType = @"RoomTitleUpdated";
//                    self.liveModel.title = [info valueForKey:@"data"];
//                    [self updateRoomInfo];
               break;
           case AIRBRoomChannelMessageTypeRoomNoticeUpdated: {
               messageType = @"RoomNoticeUpdated";
               self.liveModel.notice = [info valueForKey:@"data"];
               dispatch_async(dispatch_get_main_queue(), ^{
                   ((ASLRBDetailsButton*)(self.noticeButton)).text = self.liveModel.notice;
               });
           }
               break;
               
           case AIRBRoomChannelMessageTypeRoomExtensionUpdated: {
               if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveExtensionUpdated info:dataDic];
               }
               break;
           }
               
           case AIRBRoomChannelMessageTypeLiveCreatedByOther:
               messageType = @"LiveCreatedByOther";
               break;
           case AIRBRoomChannelMessageTypeLiveStartedByOther:
               messageType = @"LiveStartedByOther";
//               [self.liveScene.room.livePlayer start];
//                    [self updatePlayerView];
//                    [self.commentView insertNewComment:@"系统提示: 直播已开始"];
               [self.liveCommentView insertLiveSystemMessage:@"直播已开始"];
               if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveStarted info:dataDic];
               }
               break;
           case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
//               self.livePlayerStarted = NO;
//               messageType = @"LiveStoppedByOther";
//               if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
//                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveEnded info:@{}];
//               }
//               dispatch_async(dispatch_get_main_queue(), ^{
////                   [self.liveScene.room.livePlayer stop];
////                   [self.liveScene.room.livePlayer.playerView removeFromSuperview];
//                   [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
//
//                   if (self.backgroundImageAfterLiving) {
//                       self.backgroundView.image = self.backgroundImageAfterLiving;
//                   } else if (self.backgroundImage) {
//                       self.backgroundView.image = self.backgroundImage;
//                   }
//                   [self.view sendSubviewToBack:self.backgroundView];
//               });
////                    [self.commentView insertNewComment:@"系统提示: 直播已结束"];
//               [self.liveCommentView insertLiveSystemMessage:@"直播已结束"];
           }
               break;
           case AIRBRoomChannelMessageTypeLivePushStart:
               if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePushStarted info:dataDic];
               }
               break;
           case AIRBRoomChannelMessageTypeLivePushStop:
               if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePushStopped info:dataDic];
               }
               break;
           case AIRBRoomChannelMessageTypeChatLikeReceived:
               messageType = @"ChatLikeReceived";
               self.liveModel.likeCount = self.liveScene.sceneModel.likeCount;
               [self updateRoomInfo];
               break;
           case AIRBRoomChannelMessageTypeChatCommentReceived: {
               NSString* creatorID = [dataDic valueForKey:@"creatorOpenId"];
               if (creatorID.length > 0 && [creatorID isEqualToString:self.appInitConfig.userID]) {
                   
               } else {
                   messageType = @"ChatCommentReceived";
//                   [self.liveCommentView insertLiveComment:[dataDic valueForKey:@"content"] commentSenderNick:[dataDic valueForKey:@"creatorNick"] commentSenderID:creatorID presentedCompulsorily:NO];
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
                   comment = [NSString stringWithFormat:@" %@被管理员取消禁言",nick];
               }
//                    [_commentView insertNewComment:comment];
               [self.liveCommentView insertLiveSystemMessage:comment];
               
               // 处理输入框显示
               if (!self.liveScene.sceneModel.isAllCommentBanned){
                   if (self.liveScene.sceneModel.isUserCommentBanned){
                       dispatch_async(dispatch_get_main_queue(), ^{
                           _bottomViewsHolder.commentInputField.text = nil;
                           _bottomViewsHolder.commentInputField.userInteractionEnabled = NO;
                           _bottomViewsHolder.commentInputField.placeholder = @"你被管理员禁言";
                       });
                   } else{
                       dispatch_async(dispatch_get_main_queue(), ^{
                           _bottomViewsHolder.commentInputField.userInteractionEnabled = YES;
                           _bottomViewsHolder.commentInputField.placeholder = @"和主播说点什么…";
                       });
                   }
               }
           }
               
               break;
           case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
               messageType = @"OneUserWasKickedOutRoom";
               if ([[dataDic valueForKey:@"kickUser"] isEqualToString:self.appInitConfig.userID]){
                   if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                       [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventYouWereKickedOut info:@{}];
                   }
               } else {
                   NSString* nick = [dataDic valueForKey:@"kickUserName"];
                   if (nick.length >= 16) {
                       nick = [[nick substringToIndex:15] stringByAppendingString:@"**"];
                   }
                   comment = [NSString stringWithFormat:@"%@被管理员踢出直播间",nick];
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
                   [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventAllCommentsBannedOrNot info:@{@"ban" : @(mute)}];
               }
               
//                    [_commentView insertNewComment:mute ? @"系统: 管理员开启了全体禁言" : @"系统: 管理员取消了全体禁言"];
               [self.liveCommentView insertLiveSystemMessage:mute ? @"管理员开启了全体禁言" : @"管理员取消了全体禁言"];
               
               // 处理输入框显示
               if (mute) {
                   dispatch_async(dispatch_get_main_queue(), ^{
                       _bottomViewsHolder.commentInputField.text = nil;
                       _bottomViewsHolder.commentInputField.userInteractionEnabled = NO;
                       _bottomViewsHolder.commentInputField.placeholder = @"主播已开启全员禁言";
                   });
               } else if (!self.liveScene.sceneModel.isUserCommentBanned) {
                   dispatch_async(dispatch_get_main_queue(), ^{
                       _bottomViewsHolder.commentInputField.userInteractionEnabled = YES;
                       _bottomViewsHolder.commentInputField.placeholder = @"和主播说点什么…";
                   });
               } else{  // 需要请求服务端自己是否被单独禁言（需服务端优化）
                   [self.liveScene.room.chat getCurrentChatInfoOnSuccess:^(NSDictionary * _Nonnull info) {
                       self.liveScene.sceneModel.isUserCommentBanned = [[info valueForKey:@"ban"] boolValue];
                       if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                           [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventYourCommentsBannedOrNot info:@{
                               @"ban" : @(self.liveScene.sceneModel.isUserCommentBanned)
                           }];
                       }
                       if (!self.liveScene.sceneModel.isUserCommentBanned) {
                           dispatch_async(dispatch_get_main_queue(), ^{
                               _bottomViewsHolder.commentInputField.userInteractionEnabled = YES;
                               _bottomViewsHolder.commentInputField.placeholder = @"和主播说点什么…";
                           });
                       } else{
                           dispatch_async(dispatch_get_main_queue(), ^{
                               _bottomViewsHolder.commentInputField.text = nil;
                               _bottomViewsHolder.commentInputField.userInteractionEnabled = NO;
                               _bottomViewsHolder.commentInputField.placeholder = @"你被管理员禁言";
                           });
                       }
                   } onFailure:^(NSString * _Nonnull errMessage) {
                       LOG("ASLRBAudienceViewController::failed to getCurrentChatInfo(%@)", errMessage);
                       dispatch_async(dispatch_get_main_queue(), ^{
                           _bottomViewsHolder.commentInputField.userInteractionEnabled = YES;
                           _bottomViewsHolder.commentInputField.placeholder = @"和主播说点什么…";
                       });
                   }];
               }
               break;
           }
           default:

               break;
       };
}

#pragma mark - AIRBRoomChannelProtocol

- (void) uiActionOnAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    LOG("ASLRBAudienceViewController::uiActionOnAIRBRoomChannelErrorWithCode(%ld, %@).", (long)code, message);
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]) {
        if ([message hasPrefix:@"(1015"]) {
            [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorEnterRoomFailedWhenKicked withErrorMessage:[NSString stringWithFormat:@"onAIRBRoomChannelError:(0x%lx, %@)", (long)code, message]];
        } else {
            [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorOthers withErrorMessage:[NSString stringWithFormat:@"onAIRBRoomChannelError:(0x%lx, %@)", (long)code, message]];
        }
    }
}

- (void) uiActionOnAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    LOG("ASLRBAudienceViewController::onAIRBRoomChannelEvent(%ld, %@).", (long)event, info);
    switch (event) {
        case AIRBRoomChannelEventEntered:
            break;
        case AIRBRoomChannelEventRoomInfoGotten:{
//            self.liveModel.title = [info valueForKey:@"title"];
            self.liveModel.notice = self.liveScene.sceneModel.notice;
            self.liveModel.uv = self.liveScene.sceneModel.uv;
            self.liveModel.pv = self.liveScene.sceneModel.pv;
            self.liveModel.onlineCount = self.liveScene.sceneModel.onlineCount;
            
            if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
                NSDictionary* extension = [self.liveDetail objectForKey:@"extension"];
                if (extension && [extension isKindOfClass:[NSDictionary class]]) {
                    [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveExtensionUpdated info:extension];
                }
            }
            
            [self queryAndShowHistoryComments];
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
        case AIRBRoomChannelEventMessageReceived:
            break;
            
        default:
            break;
    }
}

- (void) uiActionOnAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomErrorInViewController:liveRoomError:withErrorMessage:)]){
        [self.delegate onASLRBLiveRoomErrorInViewController:self liveRoomError:ASLRBLiveRoomErrorLivePlayerError withErrorMessage:[NSString stringWithFormat:@"0x%lx, %@", (long)code, msg]];
    }
}

- (void) uiActionOnAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary *)info {
    LOG("ASLRBAudienceViewController::onAIRBLivePlayerEvent(%ld, %@).", (long)event, info);
    switch (event) {
        case AIRBLivePlayerEventLiveNotExist: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveNotExit info:nil];
            }
            break;
        }
        case AIRBLivePlayerEventLiveNotStarted: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveNotStarted info:nil];
            }
            break;
        }
        case AIRBLivePlayerEventLiveEnded: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLiveEnded info:nil];
            }
            break;
        }
            
        case AIRBLivePlayerEventPrepared: {
            [self.playLoadingIndicator show:NO];
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerPrepared info:nil];
            }
        }
            break;
        case AIRBLivePlayerEventStarted: {
            self.livePlayerStarted = YES;
            dispatch_async(dispatch_get_main_queue(), ^{
//                if (self.playerViewContentMode == UIViewContentModeScaleAspectFit) {
//                    self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFit;
//                } else if (self.playerViewContentMode == UIViewContentModeScaleAspectFill) {
//                    self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeAspectFill;
//                } else if (self.playerViewContentMode == UIViewContentModeScaleToFill) {
//                    self.liveScene.room.livePlayer.contentMode = AIRBVideoViewContentModeFill;
//                }
                
                self.videoWidth = [[info objectForKey:@"width"] intValue];
                self.videoHeight = [[info objectForKey:@"height"] intValue];
                [self updatePlayerViewContentMode];
                
                if (_playerTransitionImageView) {
                    [self.playerTransitionImageView removeFromSuperview];
                    self.playerTransitionImageView = nil;
                }
                
                if (_backgroundView) {
                    [self.backgroundView removeFromSuperview];
                    self.backgroundView = nil;
                }
                
                [self.view addSubview:self.liveScene.room.livePlayer.playerView];
                [self.view sendSubviewToBack:self.liveScene.room.livePlayer.playerView];
                [self.view setBackgroundColor:[UIColor clearColor]];
                
                [self.liveScene.room.livePlayer.playerView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.center.equalTo(self.view);
                    make.size.equalTo(self.view);
                }];
            });
            
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerStartPlaying info:nil];
            }
        }
            
            break;
            
        case AIRBLivePlayerEventStartLoading: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerStartLoading info:nil];
            }
            
            [self.playLoadingIndicator show:YES];
        }
            
            break;
        case AIRBLivePlayerEventEndLoading: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerEndLoading info:nil];
            }
            [self.playLoadingIndicator show:NO];
        }
            
            break;
        case AIRBLivePlayerEventNotification:{

        }
            break;
        case AIRBLivePlayerEventImageCaptured:{
            if (self.isSwitchingUser) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.playerTransitionImageView = [[UIImageView alloc] initWithImage:[info valueForKey:@"image"]];
                    self.playerTransitionImageView.frame = self.view.bounds;
                    [self.view addSubview:self.playerTransitionImageView];
                    [self.view sendSubviewToBack:self.playerTransitionImageView];
                    [self.liveScene.room.livePlayer.playerView removeFromSuperview];
                    [self.liveScene.room leaveRoom];
                });
            }
        }
            
        case AIRBLivePlayerEventVideoSizeChanged: {
            self.videoWidth = [[info objectForKey:@"width"] intValue];
            self.videoHeight = [[info objectForKey:@"height"] intValue];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self updatePlayerViewContentMode];
            });
            
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerVideoSizeChanged info:info];
            }
            
            break;
        }
            
        case AIRBLivePlayerEventDownloadBitrateUpdated: {
            if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
                [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventLivePlayerDownloadBitrateUpdated info:info];
            }
            break;
        }
        default:
            break;
    }
}

#pragma mark --ASLRBLiveRoomBottomViewActionsDelegate

- (void) onCommentSent:(NSString *)comment {
    [self sendComment:comment onSuccess:^{
        ;
    } onFailure:^(ASLRBLiveRoomError code, NSString * _Nonnull errorMessage) {
        ;
    }];
}

- (void) onShareButtonClicked {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventShareButtonDidClicked info:@{}];
    }
}

- (void) onLikeSent {
    [self.liveScene.room.chat sendLike];
}

#pragma mark -ASLRBFloatingPlayWindowDelegate

- (void)onASLRBFloatingPlayWindowExited {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventExitButtonDidClicked info:@{}];
    }
}

- (void)onASLRBFloatingPlayWindowTapped {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBAudienceEventFloatingPlayWindowTapped info:@{}];
    }
}

#pragma mark --NSNotification

- (void)systemActiveStateChange:(NSNotification *) note {
    if (!self.liveInitConfig.enableBackgroundLivePlaying) {
        if (note.name == UIApplicationDidBecomeActiveNotification) {
            if (self.livePlayerStarted) {
                [self refreshPlayer];
            }
        } else if (note.name == UIApplicationWillResignActiveNotification) {
            if (self.livePlayerStarted) {
                [self.liveScene.room.livePlayer pause];
            }
        }
    }
    if (note.name == UIApplicationWillResignActiveNotification && _floatingPlayWindow && ((ASLRBFloatingPlayWindow*)_floatingPlayWindow).onFloatingMode && _floatingPlayWindow.disappearAfterResignActive) {
        [((ASLRBFloatingPlayWindow*)_floatingPlayWindow) enterFloatingMode:NO];
    }
}

#pragma mark --UpdateUI

-(void)updateRoomInfo{
    if([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventLiveDataUpdated info:@{
            @"onlineCount" : @(self.liveModel.onlineCount),
            @"likeCount" : @(self.liveModel.likeCount),
            @"uv" : @(self.liveModel.uv),
            @"pv" : @(self.liveModel.pv)
        }];
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        if(!_upperLeftCustomizedViewHolder){
            [self.liveInfoViewHolder updateLikeCount:self.liveModel.likeCount];
            [self.liveInfoViewHolder updatePV:self.liveModel.pv];
        }
        if(self->_noticeButton){
            ((ASLRBDetailsButton*)(self.noticeButton)).text = self.liveModel.notice;
        }
    });
}

#pragma mark - 屏幕旋转
// 支持旋转
- (BOOL)shouldAutorotate{
    return self.enableViewRotation;
}

// 旋转的方向
- (UIInterfaceOrientationMask)supportedInterfaceOrientations{
    if (self.enableViewRotation){
        return UIInterfaceOrientationMaskAllButUpsideDown;
    } else{
        return UIInterfaceOrientationMaskPortrait;
    }
}

// 主动进行方向切换
- (void) switchViewOrientation{
    if (!self.enableViewRotation){
        return;
    }
    
    UIInterfaceOrientation interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    if (interfaceOrientation == UIInterfaceOrientationLandscapeLeft || interfaceOrientation == UIInterfaceOrientationLandscapeRight){
        [self setOrientiation:NO];
    } else{
        [self setOrientiation:YES];
    }
}

// 强制旋转
- (void)setOrientiation:(BOOL)rotated{
    if([[UIDevice currentDevice]respondsToSelector:@selector(setOrientation:)]) {
        SEL selector = NSSelectorFromString(@"setOrientation:");
        NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[UIDevice instanceMethodSignatureForSelector:selector]];
        [invocation setSelector:selector];
        [invocation setTarget:[UIDevice currentDevice]];
        int val = rotated ? UIInterfaceOrientationLandscapeRight : UIInterfaceOrientationPortrait; //设置方向
        [invocation setArgument:&val atIndex:2];
        [invocation invoke];
    }
}

// 界面方向改变的处理
- (void)handleStatusBarOrientationDidChange: (NSNotification *)notification{
    LOG("ASLRBAudienceViewController::handleStatusBarOrientationDidChange(%d).", self.enableViewRotation);
    if (!self.enableViewRotation){
        return;
    }
    
    UIInterfaceOrientation interfaceOrientation = [[UIApplication sharedApplication] statusBarOrientation];
    NSString* orientation;
    switch (interfaceOrientation) {
        case UIInterfaceOrientationUnknown:
            // NSLog(@"未知方向");
            break;
        case UIInterfaceOrientationPortrait:
            // NSLog(@"界面直立");
            [self updateLayoutRotated:NO];
            orientation = @"portrait";
            break;
        case UIInterfaceOrientationPortraitUpsideDown:
            // NSLog(@"界面直立，上下颠倒");
            break;
        case UIInterfaceOrientationLandscapeLeft:
            // NSLog(@"界面朝左");
            [self updateLayoutRotated:YES];
            orientation = @"landscapeLeft";
            break;
        case UIInterfaceOrientationLandscapeRight:
            // NSLog(@"界面朝右");
            [self updateLayoutRotated:YES];
            orientation = @"landscapeRight";
            break;
        default:
            break;
    }
    
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventViewOrientationChanged info:@{
            @"orientation" : orientation
        }];
    }
}

- (void)updateLayoutRotated:(BOOL)rotated{
    if (_bottomViewsHolder){
        [(ASLRBAudienceLiveRoomBottomViewHolder*)self.bottomViewsHolder updateLayoutRotated:rotated];
    }
    
    [(ASLRBLiveCommentView*)_liveCommentView updateLayoutRotated:rotated];
    
    [self updatePlayerViewContentMode];
    
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
 * 退出连麦
 */
- (void) linkMicLeave{
    [self.liveScene linkMicLeave];
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
 * 切换摄像头 (前后置切换)
 */
- (void) linkMicSwitchCamera{
    [self.liveScene linkMicSwitchCamera];
}

/**
 * 设置预览画面是否开启镜像（仅前置摄像头），默认开启
 * @param enable YES: 开启; NO: 关闭;
 */
- (void) linkMicSetPreviewMirror:(BOOL)enable{
    [self.liveScene linkMicSetPreviewMirror:enable];
}

/**
 * 设置视频流是否开启镜像，默认不开启
 * @param enable YES: 开启; NO: 关闭;
 */
- (void) linkMicSetCameraStreamMirror:(BOOL)enable{
    [self.liveScene linkMicSetCameraStreamMirror:enable];
}

- (void) linkMicSetPreviewContentMode:(ASLRBLinkMicViewContentMode)contentMode{
    [self.liveScene linkMicSetPreviewContentMode:(ASLSBLinkMicViewContentMode)contentMode];
}

- (void) linkMicSetRemoteCameraStreamContentMode:(ASLRBLinkMicViewContentMode)contentMode{
    [self.liveScene linkMicSetRemoteCameraStreamContentMode:(ASLSBLinkMicViewContentMode)contentMode];
}

/**
 * 开启本地麦克风，默认开启
 */
- (void) linkMicOpenMic{
    [self.liveScene linkMicOpenMic];
}

/**
 * 关闭本地麦克风
 */
- (void) linkMicCloseMic{
    [self.liveScene linkMicCloseMic];
}

/**
 * 处理主播的加入连麦邀请
 * 在{@link onASLSBLinkMicInvited:inviter:}事件中使用
 * @param agree true: 同意邀请; false: 拒绝邀请;
*/
- (void) linkMicHandleInvite:(BOOL)agree{
    [self.liveScene linkMicHandleInvite:agree];
}

/**
 * 申请连麦
 * 发出申请后, 接收方会收到{@link onASLSBLinkMicApplied:}事件
 */
- (void) linkMicApply{
    [self.liveScene linkMicApply];
}

/**
 * 取消申请连麦
 * 取消申请后, 接收方会收到{@link onASLSBLinkMicApplyCanceled:}事件
*/
- (void) linkMicCancelApply{
    [self.liveScene linkMicCancelApply];
}

- (void) linkMicHandleApplyResponse:(BOOL)join{
    [self.liveScene linkMicHandleApplyResponse:join];
}

- (void) onASLSBLinkMicEvent:(ASLSBLinkMicEvent)event info:(NSDictionary*)info{
    DELEGATE_ACTION_2ARG(onASLRBLinkMicEvent, (ASLRBLinkMicEvent)event, info, info);
    
    switch (event) {
        case ASLSBLinkMicEventLocalJoinSucceeded:
            self.livePlayerStarted = NO; // 内部停止了播放器
            [self.liveScene.room.livePlayer.playerView removeFromSuperview];
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
