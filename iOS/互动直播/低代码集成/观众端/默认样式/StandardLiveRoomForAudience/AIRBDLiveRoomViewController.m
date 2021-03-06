//
//  AIRBDLiveRoomViewController.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/8/17.
//

#import "AIRBDLiveRoomViewController.h"
#import <Masonry/Masonry.h>
#import "AIRBDToast.h"
#import "UIColor+HexColor.h"
#import "UIViewController+Extension.h"
//#import "AIRBDAbilityDemonstrationViewController.h"
//#import "AIRBDShopWindowViewController.h"
#import "AIRBDShareViewController.h"
#import "AIRBDEnvironments.h"
#import "AIRBDLinkMicCollectionViewHolder.h"

@import AliStandardLiveRoomBundle;

@interface AIRBDLiveRoomViewController () <ASLRBLiveRoomViewControllerDelegate>
@property(nonatomic, copy) NSString* userID;
@property(nonatomic, copy) NSString* liveID;
@property(nonatomic, copy) NSString* liveTitle;
@property(nonatomic, assign) AIRBDLiveRoomUserRole role;

@property(nonatomic, strong) ASLRBLiveRoomViewController* liveRoomVC;

@property(nonatomic, strong) UITextField* titleTextField;
//@property (nonatomic, strong) UILabel* networkStatusLabel;
//@property (nonatomic, strong) UIButton* networkStatusButton;
//@property (nonatomic, strong) AIRBDAbilityDemonstrationViewController* abilityDemonstrationVC;
//@property (nonatomic, strong) AIRBDShopWindowViewController* shopWindowVC;
//@property (nonatomic, strong) UIView* goodsCardView;
@property (nonatomic, strong) AIRBDShareViewController* shareVC;

// **************  观众连麦相关 *************** //
@property (nonatomic, strong) UIButton* linMickButton;
@property (nonatomic, strong) UIButton* cameraButton;
@property (nonatomic, strong) UIButton* micButton;
@property (nonatomic, strong) UIView* linMickView;
@property (nonatomic, strong) UIAlertController* alertController;
@property (nonatomic, strong) UIView* linkMicViewHolder; // 承载连麦画面的view
@property(nonatomic, strong) AIRBDLinkMicCollectionViewHolder* linkMicCollectionViewHolder; // 可滑动的连麦中的画面
@property(nonatomic, strong) NSMutableArray<NSString*>* linkMicUserArray; // 可滑动的连麦中的画面的顺序
// ***************************************** //

@end

@implementation AIRBDLiveRoomViewController

- (instancetype) initWithUserID:(NSString *)userID liveId:(NSString *)liveID role:(AIRBDLiveRoomUserRole)role title:(NSString *)title {
    self = [super init];
    if(self){
        _userID = userID;
        _liveID = liveID;
        _liveTitle = title;
        _role = role;
        [self setUp];
    }
    return self;
}

- (void)setUp{
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    __weak typeof(self) weakSelf = self;

    [[ASLRBLiveRoomManager sharedInstance] globalInitOnceWithConfig:({
        ASLRBAppInitConfig *config = [[ASLRBAppInitConfig alloc]init];
        config.appID = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppID;
        config.appKey = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppKey;
        config.appServerUrl = [AIRBDEnvironments shareInstance].appServerUrl;
        config.appServerSignSecret = [AIRBDEnvironments shareInstance].signSecret;
        config.userID = weakSelf.userID;
        config.userNick = [NSString stringWithFormat:@"%@的昵称", self.userID];
        config;
    }) onSuccess:^{
        [[ASLRBLiveRoomManager sharedInstance] createLiveRoomVCWithConfig:({
            ASLRBLiveInitConfig* config = [[ASLRBLiveInitConfig alloc] init];
            config.liveID = weakSelf.liveID;
            config.role = weakSelf.role;
            config;
        }) onCompletion:^(ASLRBLiveRoomViewController * _Nonnull liveRoomVC) {
            
            [liveRoomVC setupOnSuccess:^(NSString * _Nonnull liveID) {
                weakSelf.liveID = liveID;
                dispatch_async(dispatch_get_main_queue(), ^{
                    liveRoomVC.modalPresentationStyle = UIModalPresentationFullScreen;
                    liveRoomVC.delegate = weakSelf;
                    weakSelf.liveRoomVC = liveRoomVC;
                    
                    weakSelf.liveRoomVC.backgroundImage = [UIImage imageNamed:@"img-background"];

                        [weakSelf customizeAudienceLiveRoom];
    //                }
            
                    [weakSelf.navigationController pushViewController:liveRoomVC animated:YES];
                });
                
            } onFailure:^(NSString * _Nonnull errorMessage) {
                NSLog(@"低代码互动直播setup失败：%@", errorMessage);
                
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:errorMessage duration:3.0];
                    
                    [weakSelf dismissViewControllerAnimated:NO completion:nil];
                    [weakSelf.navigationController popViewControllerAnimated:YES];
                });
            }];
        }];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        
    }];
}

- (void) customizeAudienceLiveRoom {
//    [self customizeLinkMicLiveRoom];    // 需要体验连麦请取消该行代码注释
}

#pragma mark - ASLRBLiveRoomViewControllerDelegate

- (void)onASLRBLiveRoomErrorInViewController:(nonnull ASLRBLiveRoomViewController *)liveRoomVC liveRoomError:(ASLRBLiveRoomError)liveRoomError withErrorMessage:(nonnull NSString *)errorMessage {
    NSLog(@"");
}

- (void)onASLRBLiveRoomEventInViewController:(nonnull ASLRBLiveRoomViewController *)liveRoomVC liveRoomEvent:(ASLRBEvent)liveRoomEvent info:(nonnull NSDictionary *)info {
    switch (liveRoomEvent) {
        case ASLRBCommonEventExitButtonDidClicked:{
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.role == AIRBDLiveRoomUserRoleAnchor) {

                } else {
                    [self.liveRoomVC exitLiveRoom];
                    [self dismissViewControllerAnimated:NO completion:nil];
                    [self.navigationController popViewControllerAnimated:YES];
                }
            });
            break;
        }
        case ASLRBCommonEventShareButtonDidClicked: {
//                UIPasteboard *pboard = [UIPasteboard generalPasteboard];
//                pboard.string = self.liveID;
//                UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:@"直播ID" message:self.liveID preferredStyle:UIAlertControllerStyleAlert];
//                [alertVC addAction:[UIAlertAction actionWithTitle:@"已拷贝" style:UIAlertActionStyleDefault handler:nil]];
//                [self.liveRoomVC presentViewController:alertVC animated:YES completion:nil];
            
            if (!self.shareVC) {
                self.shareVC = [[AIRBDShareViewController alloc] init];
            }
            
            CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - ([UIScreen mainScreen].bounds.size.width / 2.67), [UIScreen mainScreen].bounds.size.width, ([UIScreen mainScreen].bounds.size.width / 2.67));
            [self.liveRoomVC presentChildViewController:self.shareVC animated:YES presentedFrame:frame direction:AIRBDViewControllerPresentFromBottom];
            self.liveRoomVC.presentedChildViewController = self.shareVC;
        }
            break;
            
        default:
            break;
    }
}

#pragma mark - CustomUIAction{
- (void) backButtonAction:(UIButton*)sender {
    [self.liveRoomVC exitLiveRoom];
    
    [self dismissViewControllerAnimated:NO completion:nil];
    [self.navigationController popViewControllerAnimated:YES];
}

- (void)startLiveButtonAction:(UIButton*)sender{
    [self.liveRoomVC startLiveAndUpdateConfig:({
        ASLRBLiveInitConfig* config = [[ASLRBLiveInitConfig alloc]init];
        config.liveBusinessInfo.liveTitle = self.titleTextField.text;
        config;
    })];
    [sender setTitle:@"加载中  " forState:UIControlStateNormal];
    UIActivityIndicatorView* spinner = [[UIActivityIndicatorView alloc]initWithFrame:CGRectMake(140, 0, 50, 50)];
    spinner.color = [UIColor whiteColor];
    spinner.tintColor = [UIColor whiteColor];
    [sender addSubview:spinner];
    [spinner startAnimating];
    sender.userInteractionEnabled = NO;
    sender.alpha = 0.8;
}

- (void)switchCameraButtonAction:(UIButton*)sender{
    [self.liveRoomVC switchCamera];
}

- (void)beautyButtonAction:(UIButton*)sender{
    [self.liveRoomVC showBeautyPanel];
}

- (void)editTitleButtonAction:(UIButton*)sender{
    [self.titleTextField becomeFirstResponder];
}

/* *********** 观众连麦相关 ************* */
/* 需要体验连麦请在主播端创建直播时打开连麦开关 config.enableLinkMic = YES;
 * 并在此观众端取消以下代码行的注释：
 *      [self customizeLinkMicLiveRoom];
 * 以及Podfile文件中的代码行的注释：
 *      pod 'AliInteractiveRTCCore', common_version
 *      pod 'AliRTCSdk', '2.5.7'
 */
#pragma mark - linkMic
- (NSMutableArray<NSString*>*) linkMicUserArray{
    if (!_linkMicUserArray){
        _linkMicUserArray = [[NSMutableArray<NSString*> alloc] init];
    }
    return _linkMicUserArray;
}

- (AIRBDLinkMicCollectionViewHolder*)linkMicCollectionViewHolder{
    if (!_linkMicCollectionViewHolder){
        AIRBDLinkMicCollectionViewHolder* collectionViewHolder = [[AIRBDLinkMicCollectionViewHolder alloc] initWithFrame:CGRectMake(4, 150, self.view.bounds.size.width / 3.0, self.view.bounds.size.width / 2.0) userID:self.userID userNick:[NSString stringWithFormat:@"%@的昵称", self.userID]];
        collectionViewHolder.delegate = self;
        _linkMicCollectionViewHolder = collectionViewHolder;
        
        [self.linkMicViewHolder addSubview:_linkMicCollectionViewHolder];
        [self.linkMicViewHolder bringSubviewToFront:_linkMicCollectionViewHolder];
    }
    return _linkMicCollectionViewHolder;
}

- (UIView*)linkMicViewHolder{
    if (!_linkMicViewHolder) {
        UIView* view = [[UIView alloc] init];
        view.backgroundColor = [UIColor blackColor];
        _linkMicViewHolder = view;
        
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.liveRoomVC.view addSubview:self->_linkMicViewHolder];
            [self.liveRoomVC.view sendSubviewToBack:self->_linkMicViewHolder];
            [self->_linkMicViewHolder mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.edges.equalTo(self.liveRoomVC.view);
            }];
        });
    }
    return _linkMicViewHolder;
}

- (void) customizeLinkMicLiveRoom {
    self.liveRoomVC.enableViewRotation = YES;   // 开启横竖屏切换
    
    self.linMickButton = [[UIButton alloc] init];
    self.linMickButton.frame = CGRectMake(0, 0, 40, 40);
    self.linMickButton.tag = 0;
    [self.linMickButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
    self.linMickButton.titleLabel.lineBreakMode = 0;
    self.linMickButton.layer.cornerRadius = 5.0;
    self.linMickButton.backgroundColor = [UIColor blackColor];
    [self.linMickButton addTarget:self action:@selector(linkMicButtonAction) forControlEvents:UIControlEventTouchUpInside];
    [self.liveRoomVC.bottomViewsHolder addSubview:self.linMickButton];
    [self.linMickButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.right.equalTo(self.liveRoomVC.bottomViewsHolder.shareButton.mas_left).offset(-10);
        make.width.equalTo(self.liveRoomVC.bottomViewsHolder.shareButton);
        make.bottom.equalTo(self.liveRoomVC.bottomViewsHolder.shareButton);
        make.top.equalTo(self.liveRoomVC.bottomViewsHolder.shareButton);
    }];
    
    self.cameraButton = [[UIButton alloc] init];
    self.cameraButton.frame = CGRectMake(0, 0, 40, 40);
    self.cameraButton.tag = 0;
    [self.cameraButton setTitle:@"关摄\n像头" forState:UIControlStateNormal];
    [self.cameraButton setTitle:@"开摄\n像头" forState:UIControlStateSelected];
    self.cameraButton.titleLabel.lineBreakMode = 0;
    self.cameraButton.layer.cornerRadius = 5.0;
    self.cameraButton.backgroundColor = [UIColor blackColor];
    [self.cameraButton addTarget:self action:@selector(cameraButtonAction) forControlEvents:UIControlEventTouchUpInside];
    self.cameraButton.hidden = YES;
    [self.liveRoomVC.bottomViewsHolder addSubview:self.cameraButton];
    [self.cameraButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.right.equalTo(self.linMickButton.mas_left).offset(-10);
        make.bottom.equalTo(self.linMickButton);
        make.width.equalTo(self.linMickButton);
        make.height.equalTo(self.linMickButton);
    }];
    
    self.micButton = [[UIButton alloc] init];
    self.micButton.frame = CGRectMake(0, 0, 40, 40);
    self.micButton.tag = 0;
    [self.micButton setTitle:@"关麦\n克风" forState:UIControlStateNormal];
    [self.micButton setTitle:@"开麦\n克风" forState:UIControlStateSelected];
    self.micButton.titleLabel.lineBreakMode = 0;
    self.micButton.layer.cornerRadius = 5.0;
    self.micButton.backgroundColor = [UIColor blackColor];
    [self.micButton addTarget:self action:@selector(micButtonAction) forControlEvents:UIControlEventTouchUpInside];
    self.micButton.hidden = YES;
    [self.liveRoomVC.bottomViewsHolder addSubview:self.micButton];
    [self.micButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.right.equalTo(self.cameraButton.mas_left).offset(-10);
        make.bottom.equalTo(self.cameraButton);
        make.width.equalTo(self.cameraButton);
        make.height.equalTo(self.cameraButton);
    }];
}

- (void)linkMicButtonAction{
    if (self.linMickButton.tag == 0){    // 申请连麦
        [self.liveRoomVC linkMicApply];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.linMickButton setTitle:@"取消\n申请" forState:UIControlStateNormal];
            self.linMickButton.tag = 1;
        });
    } else if (self.linMickButton.tag == 1){    // 取消申请
        [self.liveRoomVC linkMicCancelApply];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self.linMickButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
            self.linMickButton.tag = 0;
        });
    } else if (self.linMickButton.tag == 2){    // 退出连麦
        [self.liveRoomVC linkMicLeave];
    }
}

- (void)cameraButtonAction{
    if (self.cameraButton.selected){    // 开启摄像头
        [self.liveRoomVC linkMicOpenCamera];
        self.cameraButton.selected = NO;
    } else{ // 关闭摄像头
        [self.liveRoomVC linkMicCloseCamera];
        self.cameraButton.selected = YES;
        [self.linkMicCollectionViewHolder reloadCollectionViewData];
    }
}

- (void)micButtonAction{
    if (self.micButton.selected){    // 开启麦克风
        [self.liveRoomVC linkMicOpenMic];
        self.micButton.selected = NO;
    } else{ // 关闭麦克风
        [self.liveRoomVC linkMicCloseMic];
        self.micButton.selected = YES;
    }
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicEvent:(ASLRBLinkMicEvent)event info:(NSDictionary*)info{
    switch (event) {
        case ASLRBLinkMicEventLocalPreviewStarted:{
            if (![self.linkMicUserArray containsObject:self.userID]){
                [self.linkMicUserArray insertObject:self.userID atIndex:0];
            }
            [self.linkMicCollectionViewHolder reloadCollectionViewData];
        }
            break;
        case ASLRBLinkMicEventLocalJoinSucceeded:{
            if (![self.linkMicUserArray containsObject:self.userID]){
                [self.linkMicUserArray insertObject:self.userID atIndex:0];
            }
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.linMickButton setTitle:@"退出\n连麦" forState:UIControlStateNormal];
                self.linMickButton.tag = 2;
                self.cameraButton.hidden = NO;
                self.micButton.hidden = NO;
                
                [[AIRBDToast shareInstance] makeToast:@"加入连麦成功" duration:2.0];
            });
        }
            break;
        case ASLRBLinkMicEventLocalLeaveSucceeded:{
            [self.linkMicUserArray removeAllObjects];
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.linMickButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
                self.linMickButton.tag = 0;
                self.cameraButton.hidden = YES;
                self.micButton.hidden = YES;
                
                [[AIRBDToast shareInstance] makeToast:@"退出连麦成功" duration:2.0];
                [self.linkMicViewHolder removeFromSuperview];
                self->_linkMicCollectionViewHolder = nil;
                self->_linkMicViewHolder = nil;
            });
        }
            break;
            
        default:
            break;
    }
}

- (void) onASLRBLinkMicError:(ASLRBLinkMicError)error message:(NSString*)msg{
    switch (error) {
        case ASLRBLinkMicErrorLinkMicNotEnabled:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"连麦不可用(not enabled)" duration:2.0];
            });
        }
            break;
        case ASLRBLinkMicErrorNotAllowedToOpenMic:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"全体静音中，无法打开麦克风" duration:2.0];
                self.micButton.selected = YES;
            });
        }
            break;
            
        default:
            break;
    }
}

- (void) onASLRBLinkMicUserJoined:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        if (!user.isAnchor){
            if (![self.linkMicUserArray containsObject:user.userID]){
                [self.linkMicUserArray addObject:user.userID];
            }
        }
        NSLog(@"%@加入连麦", user.nickname);
    }
    
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicUserLeft:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        [self.linkMicUserArray removeObject:user.userID];
        NSLog(@"%@退出连麦", user.nickname);
    }
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicCameraStreamAvailable:(NSString*)userID isAnchor:(BOOL)isAnchor view:(UIView*)view{
    
    dispatch_async(dispatch_get_main_queue(), ^{
        if (isAnchor){
            [self.liveRoomVC.view addSubview:self.linkMicViewHolder];
            [self.liveRoomVC.view sendSubviewToBack:self.linkMicViewHolder];

            [self.linkMicViewHolder addSubview:view];
            [self.linkMicViewHolder sendSubviewToBack:view];
            [view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.edges.equalTo(self.linkMicViewHolder);
            }];
        }
    });
    
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicRemoteCameraStateChanged:(NSString*)userID open:(BOOL)open{
    NSLog(@"%@开关摄像头(%d)", userID, open);
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicRemoteMicStateChanged:(NSArray<NSString*>*)userIDList open:(BOOL)open{
    for (NSString* userID in userIDList){
        if ([userID isEqualToString:self.userID]){
            continue;
        }
        NSLog(@"%@开关麦克风(%d)", userID, open);
    }
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicInvited:(ASLRBLinkMicUserModel*)inviter userInvitedList:(NSArray<ASLRBLinkMicUserModel*>*)userInvitedList{
    for (ASLRBLinkMicUserModel* user in userInvitedList){
        if ([user.userID isEqualToString:self.userID]){
            __weak typeof(self) weakSelf = self;
            dispatch_async(dispatch_get_main_queue(), ^{
                UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"您收到了主播的连麦邀请\n是否接受？" message:@"连麦成功后，即可与主播进行沟通" preferredStyle:UIAlertControllerStyleAlert];
                [alertController addAction:[UIAlertAction actionWithTitle:@"接受" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                    [weakSelf.liveRoomVC linkMicHandleInvite:YES];
                }]];
                [alertController addAction:[UIAlertAction actionWithTitle:@"拒绝" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                    [weakSelf.liveRoomVC linkMicHandleInvite:NO];
                }]];
                
                weakSelf.alertController = alertController;
                [weakSelf.liveRoomVC presentViewController:self.alertController animated:YES completion:nil];
            });
        }
    }
}

- (void) onASLRBLinkMicInviteCanceledForMe{
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"主播撤销了连麦邀请"] duration:2.0];
        [weakSelf.alertController dismissViewControllerAnimated:YES completion:nil];
    });
}

- (void) onASLRBLinkMicInviteRejected:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@拒绝了连麦邀请", user.nickname] duration:2.0];
        });
    }
}

- (void) onASLRBLinkMicApplied:(BOOL)isNewApplied userList:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    dispatch_async(dispatch_get_main_queue(), ^{
        for (ASLRBLinkMicUserModel* user in userList){
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@申请连麦(%d)", user.nickname, isNewApplied] duration:2.0];
        }
    });
}

- (void) onASLRBLinkMicApplyCanceled:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    dispatch_async(dispatch_get_main_queue(), ^{
        for (ASLRBLinkMicUserModel* user in userList){
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@取消了连麦申请", user.nickname] duration:2.0];
        }
    });
}

- (void) onASLRBLinkMicApplyResponse:(BOOL)approve user:(NSString*)userID{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@的连麦申请被处理了(%d)", userID, approve] duration:2.0];
        
        if ([userID isEqualToString:self.userID]){
            [self.linMickButton setTitle:@"申请\n连麦" forState:UIControlStateNormal];
            self.linMickButton.tag = 0;
            
            if (approve){
                __weak typeof(self) weakSelf = self;
                dispatch_async(dispatch_get_main_queue(), ^{
                    UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"主播同意了你的连麦申请\n是否连麦？" message:@"连麦成功后，即可与主播进行沟通" preferredStyle:UIAlertControllerStyleAlert];
                    [alertController addAction:[UIAlertAction actionWithTitle:@"是" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                        [weakSelf.liveRoomVC linkMicHandleApplyResponse:YES];
                    }]];
                    [alertController addAction:[UIAlertAction actionWithTitle:@"否" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                        [weakSelf.liveRoomVC linkMicHandleApplyResponse:NO];
                    }]];
                    
                    weakSelf.alertController = alertController;
                    [weakSelf.liveRoomVC presentViewController:weakSelf.alertController animated:YES completion:nil];
                });
            }
        }
    });
}

- (void) onASLRBLinkMicKicked:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@被踢出连麦", user.nickname] duration:2.0];
        });
    }
}

- (void) onASLRBLinkMicSelfMicClosedByAnchor{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"已被主播静音"] duration:2.0];
        self.micButton.selected = YES;
        [self.linkMicCollectionViewHolder reloadCollectionViewData];
    });
}

- (void) onASLRBLinkMicAnchorInviteToOpenMic{
    __weak typeof(self) weakSelf = self;
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:@"主播请求取消你的静音" message:@"" preferredStyle:UIAlertControllerStyleAlert];
        [alertController addAction:[UIAlertAction actionWithTitle:@"取消静音" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [weakSelf.liveRoomVC linkMicOpenMic];
            weakSelf.micButton.selected = NO;
            [self.linkMicCollectionViewHolder reloadCollectionViewData];
        }]];
        [alertController addAction:[UIAlertAction actionWithTitle:@"保持静音" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
            
        }]];
        
        weakSelf.alertController = alertController;
        [weakSelf.liveRoomVC presentViewController:weakSelf.alertController animated:YES completion:nil];
    });
}

- (void) onASLRBLinkMicAllMicAllowed:(BOOL)allowed{
    dispatch_async(dispatch_get_main_queue(), ^{
        if (allowed) {
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"主播关闭了全员静音"] duration:2.0];
        } else {
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"主播开启了全员静音"] duration:2.0];
            self.micButton.selected = YES;
            [self.linkMicCollectionViewHolder reloadCollectionViewData];
        }
    });
}

@end
