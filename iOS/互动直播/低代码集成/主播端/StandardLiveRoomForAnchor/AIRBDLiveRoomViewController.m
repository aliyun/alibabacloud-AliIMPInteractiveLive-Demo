//
//  AIRBDLiveRoomViewController.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/8/17.
//

#import "AIRBDLiveRoomViewController.h"
//#import <AliStandardLiveRoomBundle/AliStandardLiveRoomBundle.h>
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
@property (nonatomic, strong) UILabel* networkStatusLabel;
@property (nonatomic, strong) UIButton* networkStatusButton;
//@property (nonatomic, strong) AIRBDAbilityDemonstrationViewController* abilityDemonstrationVC;
//@property (nonatomic, strong) AIRBDShopWindowViewController* shopWindowVC;
@property (nonatomic, strong) UIView* goodsCardView;
@property (nonatomic, strong) AIRBDShareViewController* shareVC;

// **************  主播连麦相关 *************** //
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
            config.middleViewsConfig.liveMembersButtonHidden = NO;
            config.middleViewsConfig.liveNoticeButtonHidden = NO;
//            config.enableLinkMic = YES; // 连麦开关
            config;
        }) onCompletion:^(ASLRBLiveRoomViewController * _Nonnull liveRoomVC) {
            [liveRoomVC setupOnSuccess:^(NSString * _Nonnull liveID) {
                NSLog(@"liveID:%@", liveID);
                weakSelf.liveID = liveID;
                dispatch_async(dispatch_get_main_queue(), ^{
                    liveRoomVC.modalPresentationStyle = UIModalPresentationFullScreen;
                    liveRoomVC.delegate = weakSelf;
                    weakSelf.liveRoomVC = liveRoomVC;
                    
                    weakSelf.liveRoomVC.backgroundImage = [UIImage imageNamed:@"img-background"];
                    
    //                [weakSelf customizeAnchorLiveRoom];
            
                    [weakSelf presentViewController:liveRoomVC animated:NO completion:nil];
                });
                
            } onFailure:^(NSString * _Nonnull errorMessage) {
                NSLog(@"低代码互动直播主播端setup失败：%@", errorMessage);
                
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

// 自定义起播页
- (void)customizeAnchorLiveRoomPrestartView {
    UIView* roomSettingHolder;
//    UIImageView* userAvatar;
    UIButton *startLiveButton,*switchCameraButton,*beautyButtonAction,*editTitleButton;
    UIView* preView = self.liveRoomVC.livePrestartCustomizedViewHolder;
//    [preView addSubview:({
//        UIView* view = [[UIView alloc]initWithFrame:preView.bounds];
//        [view setBackgroundColor:[UIColor colorWithWhite:0 alpha:0.2]];
//        view;
//    })];
    
    if (!preView) {
        return;
    }
    
    UIButton* backButton = [[UIButton alloc] init];
    [backButton setImage:[UIImage imageNamed:@"icon-exit"] forState:UIControlStateNormal];
    [backButton addTarget:self action:@selector(backButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    [preView addSubview:backButton];
    [backButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        if (@available(iOS 11.0, *)) {
            make.top.equalTo(preView.mas_safeAreaLayoutGuideTop).with.offset(10);
        } else {
            make.top.equalTo(preView.mas_top).with.offset(10);
        }
        make.right.equalTo(preView.mas_right).with.offset(-10);
        make.height.mas_equalTo(30);
        make.width.mas_equalTo(30);
    }];
    
    [preView addSubview:({
        UIView* view = [[UIView alloc] init];
        view.backgroundColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:0.8];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 8;
//        [view addSubview:({
//            UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(7, 7, 71, 71)];
////            [imageView setImage:self.roomConfig.anchorAvatar];
//            imageView.layer.masksToBounds = YES;
//            imageView.layer.cornerRadius = 8;
//            userAvatar = imageView;
//            imageView;
//        })];
        [view addSubview:({
            UITextField *textField = [[UITextField alloc] initWithFrame:CGRectMake(5, 5, 240, 40)];
            textField.textAlignment = NSTextAlignmentLeft;
            textField.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
            textField.text = self.liveTitle ? : nil;
            textField.placeholder = @"加个直播标题让更多人看见";
            textField.textColor = [UIColor colorWithRed:0.0/255.0 green:0.0/255.0 blue:0.0/255.0 alpha:0.8/1.0];
            self.titleTextField = textField;
            textField;
        })];
        [view addSubview:({
            UIButton* button = [[UIButton alloc] initWithFrame:CGRectMake(250, 13, 18, 18)];
            button.layer.masksToBounds = YES;
            button.layer.cornerRadius = 3;
            [button setImage:[UIImage imageNamed:@"icon-edit"] forState:UIControlStateNormal];
            [button addTarget:self action:@selector(editTitleButtonAction:) forControlEvents:UIControlEventTouchUpInside];
            editTitleButton = button;
            button;
        })];
        roomSettingHolder = view;
        view;
    })];
    [preView addSubview:({
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(startLiveButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 10;
        button.backgroundColor = [UIColor colorWithHexString:@"#FB622B" alpha:1.0];
        [button setTitle:@"开始直播" forState:UIControlStateNormal];
        button.titleLabel.textColor = [UIColor colorWithHexString:@"#FFFFFF" alpha:1.0];
        button.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16];
        startLiveButton = button;
        button;
    })];
    [preView addSubview:({
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(switchCameraButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.imageView.contentMode = UIViewContentModeScaleAspectFill;
        button.contentHorizontalAlignment = UIControlContentHorizontalAlignmentFill;
        button.contentVerticalAlignment = UIControlContentVerticalAlignmentFill;
        [button setImage:[UIImage imageNamed:@"icon-camera_switch"] forState:UIControlStateNormal];
        switchCameraButton = button;
        [button addSubview:({
            UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(-10, 36, 60, 18)];
            label.textAlignment = NSTextAlignmentCenter;
            label.adjustsFontSizeToFitWidth = YES;
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
            label.textColor = [UIColor whiteColor];
            label.text = @"翻转";
            label;
        })];
        button;
    })];
    [preView addSubview:({
        UIButton* button = [[UIButton alloc] init];
        [button addTarget:self action:@selector(beautyButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        button.imageView.contentMode = UIViewContentModeScaleAspectFill;
        [button setImage:[UIImage imageNamed:@"icon-beauty_white"] forState:UIControlStateNormal];
        beautyButtonAction = button;
        [button addSubview:({
            UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(-10, 40, 60, 18)];
            label.textAlignment = NSTextAlignmentCenter;
            label.adjustsFontSizeToFitWidth = YES;
            label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
            label.textColor = [UIColor whiteColor];
            label.text = @"美颜";
            label;
        })];
        button;
    })];
    
    //约束
    [roomSettingHolder mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerX.equalTo(preView);
        if (@available(iOS 11.0, *)) {
            make.top.equalTo(preView.mas_safeAreaLayoutGuideTop).with.offset(80);
        } else {
            make.top.equalTo(preView.mas_top).with.offset(80);
        }
        make.height.mas_equalTo(85);
        make.width.mas_equalTo(275);
    }];
    [startLiveButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.centerX.equalTo(preView);
        if (@available(iOS 11.0, *)) {
            make.bottom.equalTo(preView.mas_safeAreaLayoutGuideBottom).with.offset(-95);
        } else {
            make.bottom.equalTo(preView.mas_bottom).with.offset(-95);
        }
        make.width.mas_equalTo(275);
        make.height.mas_equalTo(48);
    }];
    [switchCameraButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.left.equalTo(startLiveButton.mas_left).with.offset(68);
        make.bottom.equalTo(startLiveButton.mas_top).with.offset(-35);
        make.width.mas_equalTo(36);
        make.height.mas_equalTo(32);
    }];
    [beautyButtonAction mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.right.equalTo(startLiveButton.mas_right).with.offset(-68);
        make.bottom.equalTo(startLiveButton.mas_top).with.offset(-35);
        make.width.mas_equalTo(36);
        make.height.mas_equalTo(36);
    }];
}

- (void) addAnchorCustomizedViews {
    // 网络状态变化
//    UIView* networkStatusView = [[UIView alloc] init];
//    [self.liveRoomVC.view addSubview:networkStatusView];
//    [networkStatusView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//        make.width.mas_equalTo(75);
//        make.height.mas_equalTo(30);
//        make.right.equalTo(self.liveRoomVC.view.mas_right);
//        make.top.equalTo(self.liveRoomVC.view.mas_top).with.offset(85);
//    }];
//
//    self.networkStatusButton = [[UIButton alloc] init];
//    self.networkStatusButton.layer.cornerRadius = 3;
//    self.networkStatusButton.clipsToBounds = YES;
//    self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#51C359" alpha:1.0];
//    [networkStatusView addSubview:self.networkStatusButton];
//    [self.networkStatusButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//        make.width.mas_equalTo(6);
//        make.height.mas_equalTo(6);
//        make.left.equalTo(networkStatusView.mas_left);
//        make.centerY.equalTo(networkStatusView.mas_centerY);
//    }];
//
//    self.networkStatusLabel = [[UILabel alloc] init];
//    self.networkStatusLabel.text = @"网络良好";
//    self.networkStatusLabel.font = [UIFont fontWithName:@"PingFangSC-Regular" size:12];
//    [networkStatusView addSubview:self.networkStatusLabel];
//    [self.networkStatusLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//        make.width.mas_equalTo(52);
//        make.height.mas_equalTo(18);
//        make.right.equalTo(networkStatusView.mas_right).with.offset(-12);
//        make.centerY.equalTo(networkStatusView.mas_centerY);
//    }];
    
    //更多能力展示
    UIView* abilitiesDemonstrationEntranceView = [[UIView alloc] init];
    abilitiesDemonstrationEntranceView.backgroundColor = [UIColor colorWithRed:0.0 green:0.0 blue:0.0 alpha:0.2];
    abilitiesDemonstrationEntranceView.layer.cornerRadius = 10;
    [self.liveRoomVC.view addSubview:abilitiesDemonstrationEntranceView];
    [abilitiesDemonstrationEntranceView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(105);
        make.height.mas_equalTo(22);
        make.right.equalTo(self.liveRoomVC.view.mas_right);
        make.top.equalTo(self.liveRoomVC.view.mas_top).with.offset(125);
    }];
    
    UIImageView* abilitiesDemonstrationEntranceImageView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"img-moreabilities"]];
    [abilitiesDemonstrationEntranceView addSubview:abilitiesDemonstrationEntranceImageView];
    [abilitiesDemonstrationEntranceImageView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(14.4);
        make.height.mas_equalTo(14.4);
        make.left.equalTo(abilitiesDemonstrationEntranceView.mas_left).with.offset(11);
        make.centerY.equalTo(abilitiesDemonstrationEntranceView.mas_centerY);
    }];
    
    UIButton* abilitiesDemonstrationEntranceTextButton = [[UIButton alloc] init];
    [abilitiesDemonstrationEntranceTextButton addTarget:self action:@selector(abilitiesDemonstrationEntranceButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    [abilitiesDemonstrationEntranceView addSubview:abilitiesDemonstrationEntranceTextButton];
    [abilitiesDemonstrationEntranceTextButton setAttributedTitle:[[NSAttributedString alloc] initWithString:@"更多能力"
                                                                   attributes:
                                    @{
                                        NSForegroundColorAttributeName:[UIColor colorWithHexString:@"#FFFFFF" alpha:1.0],
                                        NSFontAttributeName:[UIFont fontWithName:@"PingFangSC-Regular" size:12]
                                    }] forState:UIControlStateNormal];
    [abilitiesDemonstrationEntranceTextButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(48);
        make.height.mas_equalTo(14);
        make.left.equalTo(abilitiesDemonstrationEntranceView.mas_left).with.offset(31);
        make.centerY.equalTo(abilitiesDemonstrationEntranceView.mas_centerY);
    }];
    
    UIButton* abilitiesDemonstrationEntranceImgButton = [[UIButton alloc] init];
    [abilitiesDemonstrationEntranceImgButton addTarget:self action:@selector(abilitiesDemonstrationEntranceButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    [abilitiesDemonstrationEntranceImgButton setImage:[UIImage imageNamed:@"icon-back"] forState:UIControlStateNormal];
    [abilitiesDemonstrationEntranceView addSubview:abilitiesDemonstrationEntranceImgButton];
    abilitiesDemonstrationEntranceImgButton.transform = CGAffineTransformMakeRotation(M_PI);
    [abilitiesDemonstrationEntranceImgButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
        make.width.mas_equalTo(10);
        make.height.mas_equalTo(10);
        make.left.equalTo(abilitiesDemonstrationEntranceView.mas_left).with.offset(87);
        make.centerY.equalTo(abilitiesDemonstrationEntranceView.mas_centerY);
    }];
}

- (void) customizeAnchorLiveRoom {
    [self customizeAnchorLiveRoomPrestartView];
}

- (void) customizeAudienceLiveRoom {
    
}

#pragma mark - ASLRBLiveRoomViewControllerDelegate

- (void)onASLRBLiveRoomErrorInViewController:(nonnull ASLRBLiveRoomViewController *)liveRoomVC liveRoomError:(ASLRBLiveRoomError)liveRoomError withErrorMessage:(nonnull NSString *)errorMessage {
    
}

- (void)onASLRBLiveRoomEventInViewController:(nonnull ASLRBLiveRoomViewController *)liveRoomVC liveRoomEvent:(ASLRBEvent)liveRoomEvent info:(nonnull NSDictionary *)info {
    switch (liveRoomEvent) {
        case ASLRBCommonEventExitButtonDidClicked:{
            dispatch_async(dispatch_get_main_queue(), ^{
                if (self.role == AIRBDLiveRoomUserRoleAnchor) {
                    UIAlertController *alertVC = [UIAlertController alertControllerWithTitle:@"提示" message:@"还有观众正在路上，确定要结束直播吗？" preferredStyle:UIAlertControllerStyleAlert];
                    UIAlertAction* conform = [UIAlertAction actionWithTitle:@"确定" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                        [self.liveRoomVC exitLiveRoom];
                        [self dismissViewControllerAnimated:NO completion:nil];
                        [self.navigationController popViewControllerAnimated:YES];
                    }];
                    UIAlertAction* cancel = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil];
                    
                    [alertVC addAction:cancel];
                    [alertVC addAction:conform];
                    [self.liveRoomVC presentViewController:alertVC animated:YES completion:nil];
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
        case ASLRBAnchorEventLivePusherStarted: {
            if (self.role == ASLRBUserRoleAnchor) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self addAnchorCustomizedViews];
                });
            }
        }
            break;
            
        case ASLRBAnchorEventLivePusherStreamingReconnectStart: {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.networkStatusLabel.text = @"网络中断";
//                self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#FE3143" alpha:1.0];
//
////                [[AIRBDToast shareInstance] makeToast:@"网络中断，正在重连" duration:1.0];
//            });
        }
            break;
        case ASLRBAnchorEventLivePusherStreamingReconnectSuccess: {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.networkStatusLabel.text = @"网络良好";
//                self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#51C359" alpha:1.0];
//
////                [[AIRBDToast shareInstance] makeToast:@"网络重连成功" duration:1.0];
//            });
            
        }
            break;
        case ASLRBAnchorEventLivePusherStreamingPoorNetworkStart: {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.networkStatusLabel.text = @"网络不佳";
//                self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#FFA623" alpha:1.0];
//            });
            
        }
            break;
        case ASLRBAnchorEventLivePusherStreamingPoorNetworkEnd: {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.networkStatusLabel.text = @"网络良好";
//                self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#51C359" alpha:1.0];
//            });
            
        }
            break;
        case ASLRBAnchorEventLivePusherStreamingNetworkConnectionLost: {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                self.networkStatusLabel.text = @"网络中断";
//                self.networkStatusButton.backgroundColor = [UIColor colorWithHexString:@"#FE3143" alpha:1.0];
//
//                [[AIRBDToast shareInstance] makeToast:@"网络中断，请检查网络" duration:1.0];
//            });
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

- (void) abilitiesDemonstrationEntranceButtonAction:(UIButton*)sender {
//    CGRect frame = CGRectMake([UIScreen mainScreen].bounds.size.width - 250, 0, 250, [UIScreen mainScreen].bounds.size.height);
//    if (!self.abilityDemonstrationVC) {
//        self.abilityDemonstrationVC = [[AIRBDAbilityDemonstrationViewController alloc] init];
//        self.abilityDemonstrationVC.delegate = self;
//    }
//    [self.liveRoomVC presentChildViewController:self.abilityDemonstrationVC animated:YES presentedFrame:frame direction:AIRBDViewControllerPresentFromRight];
//    self.liveRoomVC.presentedChildViewController = self.abilityDemonstrationVC;
}

#pragma mark - AIRBDAbilityDemonstrationVCProtocol

//- (void) onShopWindowItemClicked {
//    [self.liveRoomVC dismissChildViewController:self.abilityDemonstrationVC animated:YES];
//
//    if (!self.shopWindowVC) {
//        self.shopWindowVC = [[AIRBDShopWindowViewController alloc] init];
//    }
//
//    CGRect frame = CGRectMake(0, [UIScreen mainScreen].bounds.size.height - [UIScreen mainScreen].bounds.size.width * 1.2, [UIScreen mainScreen].bounds.size.width, [UIScreen mainScreen].bounds.size.width * 1.2);
//    [self.liveRoomVC presentChildViewController:self.shopWindowVC animated:YES presentedFrame:frame direction:AIRBDViewControllerPresentFromBottom];
//    self.liveRoomVC.presentedChildViewController = self.shopWindowVC;
//}
//
//- (void) onGoodsCardItemClicked {
//    [self.liveRoomVC dismissChildViewController:self.abilityDemonstrationVC animated:YES];
//
//    [UIView animateWithDuration:0.2 animations:^{
//        if (!self.goodsCardView) {
//            self.goodsCardView = [[UIImageView alloc] initWithImage:[UIImage imageNamed:@"直播-商品卡片"]];
//            [self.liveRoomVC.view addSubview:self.goodsCardView];
//            [self.goodsCardView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//                make.bottom.equalTo(self.liveRoomVC.bottomCustomizedViewHolder.mas_top);
//                make.left.equalTo(self.liveRoomVC.view).with.offset(14);
//                make.height.mas_equalTo(80);
//                make.width.mas_equalTo(260);
//            }];
//        }
//
//        [self.liveRoomVC.liveCommentView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
//            make.left.equalTo(self.liveRoomVC.view).with.offset(self.liveRoomVC.liveCommentView.frame.origin.x);
//            make.bottom.equalTo(self.goodsCardView.mas_top);
//            make.height.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.height);
//            make.width.mas_equalTo(self.liveRoomVC.liveCommentView.bounds.size.width);
//        }];
//    }];
//}

#pragma mark - linkMic
// **************  主播连麦相关 *************** //
/* 需要体验连麦请取消以下代码行的注释：
 *      config.enableLinkMic = YES;
 * 以及Podfile文件中的代码行的注释：
 *      pod 'AliInteractiveRTCCore', common_version
 *      pod 'AliRTCSdk', '2.5.7'
 */
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

- (void) onASLRBLinkMicEvent:(ASLRBLinkMicEvent)event info:(NSDictionary*)info{
    switch (event) {
        case ASLRBLinkMicEventLocalPreviewStarted:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.linkMicViewHolder addSubview:self.liveRoomVC.linkMicLocalPreview];
                [self.linkMicViewHolder sendSubviewToBack:self.liveRoomVC.linkMicLocalPreview];
                [self.liveRoomVC.linkMicLocalPreview mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                    make.edges.equalTo(self.linkMicViewHolder);
                }];
            });
        }
            break;
        case ASLRBLinkMicEventLocalJoinSucceeded:{
            
        }
            break;
        case ASLRBLinkMicEventLocalLeaveSucceeded:{
            [self.linkMicUserArray removeAllObjects];
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
    
    // 主播根据人连麦数等设置旁路直播布局，即麦下的观众看到的画面
//        [self.liveRoomVC linkMicSetEnumBypassLiveLayout:ASLRBEnumBypassLiveLayoutTypeFivePeer userIDs:userIDs onSuccess:^{
//
//        } onFailure:^(NSString * _Nonnull error) {
//
//        }];
        
//        [self.liveRoomVC linkMicSetCustomBypassLiveLayout:(nonnull NSArray<ASLRBCustomBypassLiveLayoutUserModel *> *) onSuccess:^{
//
//        } onFailure:^(NSString * _Nonnull error) {
//
//        }];
}

- (void) onASLRBLinkMicUserLeft:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        [self.linkMicUserArray removeObject:user.userID];
        NSLog(@"%@退出连麦", user.nickname);
    }
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicCameraStreamAvailable:(NSString*)userID isAnchor:(BOOL)isAnchor view:(UIView*)view{
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicRemoteCameraStateChanged:(NSString*)userID open:(BOOL)open{
    NSLog(@"%@开关摄像头(%d)", userID, open);
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicRemoteMicStateChanged:(NSArray<NSString*>*)userIDList open:(BOOL)open{
    for (NSString* userID in userIDList){
        NSLog(@"%@开关麦克风(%d)", userID, open);
    }
    [self.linkMicCollectionViewHolder reloadCollectionViewData];
}

- (void) onASLRBLinkMicInvited:(ASLRBLinkMicUserModel*)inviter userInvitedList:(NSArray<ASLRBLinkMicUserModel*>*)userInvitedList{
    
}

- (void) onASLRBLinkMicInviteCanceledForMe{
    
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
            
            if (self.role == AIRBDLiveRoomUserRoleAnchor){
                __weak typeof(self) weakSelf = self;
                UIAlertController *alertController = [UIAlertController alertControllerWithTitle:[NSString stringWithFormat:@"%@申请连麦，是否同意？", user.nickname] message:@"" preferredStyle:UIAlertControllerStyleAlert];
                [alertController addAction:[UIAlertAction actionWithTitle:@"同意" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                    [weakSelf.liveRoomVC linkMicHandleApply:user.userID agree:YES];
                }]];
                [alertController addAction:[UIAlertAction actionWithTitle:@"拒绝" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
                    [weakSelf.liveRoomVC linkMicHandleApply:user.userID agree:NO];
                }]];
                
                [weakSelf.liveRoomVC presentViewController:alertController animated:YES completion:nil];
            }
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
    
}

- (void) onASLRBLinkMicKicked:(NSArray<ASLRBLinkMicUserModel*>*)userList{
    for (ASLRBLinkMicUserModel* user in userList){
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@被踢出连麦", user.nickname] duration:2.0];
        });
    }
}

- (void) onASLRBLinkMicSelfMicClosedByAnchor{
    
}

- (void) onASLRBLinkMicAnchorInviteToOpenMic{
    
}

- (void) onASLRBLinkMicAllMicAllowed:(BOOL)allowed{
    
}
@end
