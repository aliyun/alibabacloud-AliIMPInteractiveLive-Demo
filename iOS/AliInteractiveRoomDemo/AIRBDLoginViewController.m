//
//  AIRBDLoginViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/23.
//

#import "AIRBDLoginViewController.h"
#import "AIRBDSetRoomViewController.h"
#import "AIRBDBigClassViewController.h"
#import "AIRBDRoomListViewController.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDToast.h"
#import "Utility.h"
#import "AIRBDEnvironments.h"
#import <Masonry/Masonry.h>

@interface AIRBDLoginViewController ()<UITextFieldDelegate,AIRBRoomEngineDelegate>
@property (strong, nonatomic) UIImageView* backgroundView;
@property (strong, nonatomic) UIView* userIDInputHolder;
@property (strong, nonatomic) UITextField* userIDInputField;
@property (strong, nonatomic) UIButton* loginButton;
@property (strong, nonatomic) UIButton* environmentButton;
@property (strong, nonatomic) UIButton* sceneButton;
@property (copy, nonatomic) NSString* userID;
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@end

@implementation AIRBDLoginViewController

#pragma mark -UI

- (UIImageView *) backgroundView{
    if(!_backgroundView){
        UIImageView* imageView = [[UIImageView alloc]init];
        imageView.contentMode = UIViewContentModeScaleAspectFill;
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:imageView];
        [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop);
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom);
            } else {
                make.top.equalTo(weakSelf.view);
                make.bottom.equalTo(weakSelf.view);
            }
            make.left.equalTo(weakSelf.view);
            make.right.equalTo(weakSelf.view);
        }];
        [imageView setImage:[UIImage imageNamed:@"img-setroom-background"]];
        _backgroundView = imageView;
    }
    return _backgroundView;
}

- (UIView *) userIDInputHolder{
    if(!_userIDInputHolder){
        UIView* view = [[UIView alloc]init];
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(232);
            } else {
                make.top.equalTo(self.view).with.offset(232);
            }
            make.centerX.equalTo(self.view);
            make.width.mas_equalTo(327);
            make.height.mas_equalTo(58);
        }];
        view.backgroundColor = [UIColor colorWithWhite:0.2 alpha:0.7];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 8;
        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(23, 15, 50, 28)];
        label.text = @"昵称";
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:18];
        label.textColor = [UIColor colorWithRed:242/255.0 green:242/255.0 blue:242/255.0 alpha:1.0/1.0];
        [view addSubview:label];
        [view addSubview:self.userIDInputField];
        NSString * userIDLast = [[NSUserDefaults standardUserDefaults] stringForKey:@"AIRBDUserID"];
        self.userIDInputField.text = userIDLast;
        _userIDInputHolder = view;
    }
    return _userIDInputHolder;
}

- (UITextField *) userIDInputField{
    if(!_userIDInputField){
        UITextField* textField = [[UITextField alloc] initWithFrame:CGRectMake(101, 18, 152, 22)];
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"数字或英文字母"
                                                                         attributes:@{
                                                                             NSForegroundColorAttributeName:[UIColor lightGrayColor],
                                                                             NSFontAttributeName:[UIFont systemFontOfSize:16]
                                                                         }];
        textField.attributedPlaceholder = attrString;
        textField.textColor = [UIColor whiteColor];
        textField.font = [UIFont fontWithName:@"PingFangSC-Regular" size:16];
        textField.backgroundColor = [UIColor clearColor];
        textField.textAlignment = NSTextAlignmentLeft;
        textField.keyboardType = UIKeyboardTypeNumbersAndPunctuation;
        textField.returnKeyType = UIReturnKeyDone;
        textField.keyboardAppearance = UIKeyboardAppearanceDefault;
        textField.delegate = self;
        textField.borderStyle = UITextBorderStyleNone;
        textField.autocorrectionType = UITextAutocorrectionTypeNo;   //不自动纠错
        _userIDInputField = textField;
    }
    return _userIDInputField;
}

- (UIButton *) loginButton{
    if(!_loginButton){
        UIButton* button = [[UIButton alloc]init];
        [self.view addSubview:button];
        __weak typeof(self) weakSelf = self;
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-166);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-166);
            }
            make.width.mas_equalTo(326);
            make.height.mas_equalTo(55);
        }];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 8;
//        [button setBackgroundColor:[UIColor colorWithRed:252.0/255.0 green:119.0/255.0 blue:22.0/255.0 alpha:1.0/1.0]];
        button.contentMode = UIViewContentModeScaleAspectFit;
        [button setBackgroundImage:[UIImage imageNamed:@"img-button_login"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(loginButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(103, 13, 109.4, 28)];
        label.text = @"登录";
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:22];
        label.textColor = [UIColor colorWithRed:255/255.0 green:255/255.0 blue:255/255.0 alpha:1.0/1.0];
        [button addSubview:label];
        _loginButton = button;
    }
    return _loginButton;
}

- (UIButton *)environmentButton{
    if(!_environmentButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(10, 85, 100, 40)];
        [button setBackgroundColor:[UIColor colorWithWhite:0.5 alpha:0.8]];
        [button setTag:1];
        [button setTitle:@"线上" forState:UIControlStateNormal];
        [button addTarget:self action:@selector(environmentButtonAction) forControlEvents:UIControlEventTouchUpInside];
        _environmentButton = button;
    }
    return _environmentButton;
}

- (UIButton *)sceneButton{
    if(!_sceneButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(10, 40, 100, 40)];
        [button setBackgroundColor:[UIColor colorWithWhite:0.5 alpha:0.8]];
        int AIRBDSceneButtonTag = [[[NSUserDefaults standardUserDefaults] stringForKey:@"AIRBDSceneButtonTag"] intValue];
        if (AIRBDSceneButtonTag == 1){
            [button setTag:1];
            [button setTitle:@"互动直播" forState:UIControlStateNormal];
        } else {
            [button setTag:0];
            [button setTitle:@"互动课堂" forState:UIControlStateNormal];
        }
        
        [button addTarget:self action:@selector(sceneButtonAction) forControlEvents:UIControlEventTouchUpInside];
        _sceneButton = button;
    }
    return _sceneButton;
}

-(void)environmentButtonAction{
    self.environmentButton.tag = 1 - self.environmentButton.tag;
    if(self.environmentButton.tag == 1){
        [self.environmentButton setTitle:@"线上" forState:UIControlStateNormal];
    }else{
        [self.environmentButton setTitle:@"预发" forState:UIControlStateNormal];
    }
}

-(void)sceneButtonAction{
    self.sceneButton.tag = 1 - self.sceneButton.tag;
    if(self.sceneButton.tag == 1){
        [self.sceneButton setTitle:@"互动直播" forState:UIControlStateNormal];
    }else{
        [self.sceneButton setTitle:@"互动课堂" forState:UIControlStateNormal];
    }
    [[NSUserDefaults standardUserDefaults] setObject:[NSNumber numberWithLong:self.sceneButton.tag] forKey:@"AIRBDSceneButtonTag"];
}

-(void)setUpUI{
    [self.view sendSubviewToBack:self.backgroundView];
    [self.view bringSubviewToFront:self.userIDInputHolder];
    [self.view bringSubviewToFront:self.loginButton];
    
#ifdef DEBUG
    [self.view addSubview:self.environmentButton];
#endif
    
    [self.view addSubview:self.sceneButton];
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setUpUI];
}

#pragma mark - UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    if (textField.text.length > 0) {
        self.userID = textField.text;
    }
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
     return YES;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
}

#pragma mark - AIRBRoomEngineDelegate

- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBRoomEngineEventEngineStarted:
            [self requestTokenAndLogin];
            break;
        case AIRBRoomEngineEventEngineLogined: {
            static dispatch_once_t onceToken;
            dispatch_once(&onceToken, ^{
                dispatch_async(dispatch_get_main_queue(), ^{
//                    [self gotoSetRoomViewController];
                    if(self.sceneButton.tag == 1){
                        [self gotoRoomListViewController];//电商的房间列表
                    }else{
                        [self gotoBigClassViewController];
                    }
                });
            });
        }
            break;
        case AIRBRoomEngineEventEngineLogouted:
            break;
            
        default:
            break;
    }
}

- (void) onRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString *)msg object:(AIRBRoomEngine *)object {
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"登录出错(%ld, %@)", (long)code, msg] duration:3.0];
        self.loginButton.enabled = YES;
        for (id obj in [self.loginButton subviews]) {
            if([obj isKindOfClass:[UILabel class]]){
                [obj setText:@"重新登录"];
                break;
            }
        }
        self.userIDInputField.enabled = YES;
    });
    
}

- (void) requestTokenAndLogin {
    NSString* path = [NSString stringWithFormat:@"http://%@/api/login/getToken", [AIRBDEnvironments shareInstance].appServerHost];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&appKey=%@&userId=%@&deviceId=%@", path, self.config.appID, self.config.appKey, _userID, self.config.deviceID];
    
    NSString* dateString = [Utility currentDateString];
    NSString* nonce = [Utility randomNumString];
    
    NSDictionary* headers = @{
        @"a-app-id" : @"imp-room",
        @"a-signature-method" : @"HMAC-SHA1",
        @"a-signature-version" : @"1.0",
        @"a-timestamp" : dateString,
        @"a-signature-nonce" : nonce,
    };
    
    NSDictionary* params = @{
        @"appId" : self.config.appID,
        @"appKey" : self.config.appKey,
        @"userId" : self.userID,
        @"deviceId" : self.config.deviceID
    };
    
    NSString* signedString = [Utility AIRBRequestSignWithSignSecret:[AIRBDEnvironments shareInstance].signSecret method:@"POST" path:path parameters:params headers:headers];
    
    NSURL* url = [[NSURL alloc] initWithString:s];
    
    NSMutableURLRequest* request = [NSMutableURLRequest requestWithURL:url];
    request.HTTPMethod = @"POST";
    [request setValue:@"imp-room" forHTTPHeaderField:@"a-app-id"];
    [request setValue:@"HMAC-SHA1" forHTTPHeaderField:@"a-signature-method"];
    [request setValue:@"1.0" forHTTPHeaderField:@"a-signature-version"];
    [request setValue:signedString forHTTPHeaderField:@"a-signature"];
    [request setValue:dateString forHTTPHeaderField:@"a-timestamp"];
    [request setValue:nonce forHTTPHeaderField:@"a-signature-nonce"];
    
    NSURLSession* session = [NSURLSession sharedSession];
    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        
        if (data && !error) {
            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
                                                                options:NSJSONReadingMutableContainers
                                                                  error:nil];
            
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 1 && [resultDic valueForKey:@"accessToken"] && [resultDic valueForKey:@"refreshToken"]) {
                    AIRBRoomEngineAuthToken* token = [[AIRBRoomEngineAuthToken alloc] init];
                    token.accessToken = [NSString stringWithString:[resultDic valueForKey:@"accessToken"]];
                    token.refreshToken = [NSString stringWithString:[resultDic valueForKey:@"refreshToken"]];
                    [[AIRBRoomEngine sharedInstance] loginWithUserID:self.userID token:token];
                    return;
                }
            }
        }
    }];
    [task resume];
}

- (void) gotoSetRoomViewController{
    AIRBDSetRoomViewController *setRoomViewController = [[AIRBDSetRoomViewController alloc]initWithUserID:self.userID config:self.config];
    setRoomViewController.edgesForExtendedLayout = UIRectEdgeNone;
    setRoomViewController.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:setRoomViewController animated:YES];
}

- (void) gotoRoomListViewController{
    AIRBDRoomListViewController *roomListViewController = [[AIRBDRoomListViewController alloc]init];
    roomListViewController.userID = self.userID;
    roomListViewController.config = self.config;
    roomListViewController.edgesForExtendedLayout = UIRectEdgeNone;
    roomListViewController.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:roomListViewController animated:YES];
}

- (void) gotoBigClassViewController{
    AIRBDBigClassViewController *bigClassViewController = [[AIRBDBigClassViewController alloc]init];
    bigClassViewController.userID = self.userID;
    bigClassViewController.config = self.config;
    bigClassViewController.edgesForExtendedLayout = UIRectEdgeNone;
    bigClassViewController.hidesBottomBarWhenPushed = YES;
    [self.navigationController pushViewController:bigClassViewController animated:YES];
    
}

- (void) loginButtonAction:(UIButton*)sender {
    
    [self.userIDInputField resignFirstResponder];
    if (self.userIDInputField.text.length > 0) {
        self.userID = self.userIDInputField.text;
    }else{
        self.userID = [Utility randomNumString];
    }
    if (self.userID.length > 0) {
        for (id obj in [self.loginButton subviews]) {
            if([obj isKindOfClass:[UILabel class]]){
                [obj setText:@"登录中"];
                break;
            }
        }
        self.loginButton.alpha = 0.8;
        self.loginButton.enabled = NO;
        self.userIDInputField.enabled = NO;
        
        self.config = [[AIRBRoomEngineConfig alloc] init];
        if (self.sceneButton.tag == 0) {
            self.config.appID = [AIRBDEnvironments shareInstance].interactiveClassRoomAppID;
            self.config.appKey = [AIRBDEnvironments shareInstance].interactiveClassRoomAppKey;
        } else {
            self.config.appID = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppID;
            self.config.appKey = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppKey;
        }
        
        if (self.environmentButton.tag == 0) {
            self.config.environmentType = 0;
        } else {
            self.config.environmentType = 1;
        }
        
        self.config.deviceID = [[UIDevice currentDevice] identifierForVendor].UUIDString;
        self.userID = self.userID;
        [AIRBRoomEngine sharedInstance].delegate = self;
        [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:self.config];
    }
    
    [[NSUserDefaults standardUserDefaults] setObject:self.userIDInputField.text forKey:@"AIRBDUserID"];
}

@end
