//
//  AIRBDClassroomStudent4iPhoneViewController.m
//  StandardClassroomForStudents
//
//  Created by 君渡 on 2021/12/13.
//

#import "AIRBDClassroomStudent4iPhoneViewController.h"
#import "AIRBDEnvironments.h"
#import <AliStandardClassroomBundle/AliStandardClassroomBundle.h>

@import AliInteractiveRoomBundle;

@interface AIRBDClassroomStudent4iPhoneViewController()<ASCRBStudentViewController4PhoneProtocolDelegate>

@property(nonatomic, copy) NSString* userID;
@property(nonatomic, copy) NSString* userNick;
@property(nonatomic, copy) NSString* classID;

@property (nonatomic, strong) ASCRBAppInitConfig* appConfig;
@property (nonatomic, strong) id<ASCRBStudentViewController4PhoneProtocol> studentVC4Phone;

@property (strong, nonatomic) UITextField* classIDTextField;
@property (strong, nonatomic) UITextField* userIDTextField;
@property (strong, nonatomic) UITextField* userNickTextField;

@end

@implementation AIRBDClassroomStudent4iPhoneViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    [self setupUI];
}

// 设置iPhone课堂学生端参数
- (void)setupStudentVC4Phone {
    //******** 初始化相关配置 ********//
    ASCRBAppInitConfig* appInitConfig = [[ASCRBAppInitConfig alloc]init];
    appInitConfig.appID = [AIRBDEnvironments shareInstance].interactiveClassRoomAppID;    // 在阿里云控制台开通互动直播后获取；
    appInitConfig.appKey = [AIRBDEnvironments shareInstance].interactiveClassRoomAppKey; // 在阿里云控制台开通互动直播后获取；
    appInitConfig.appServerUrl = [AIRBDEnvironments shareInstance].appServerUrl;   // AppServer部署好之后会获得；
    appInitConfig.appServerSignSecret = [AIRBDEnvironments shareInstance].signSecret;  // 在阿里云控制台开通互动直播后获取；
    appInitConfig.userID = self.userID;   // 自定义用户id，必须是英文字母或者阿拉伯数字或者二者组合
    appInitConfig.userNick = self.userNick;    // 自定义用户昵称，必传
    self.appConfig = appInitConfig;
    
    ASCRBClassInitConfig* classInitConfig = [[ASCRBClassInitConfig alloc] init];
    classInitConfig.role = ASCRBUserRoleStudent; // 学生
    classInitConfig.classID = self.classID;   // 需要进入的课程ID，老师侧传空则会创建课程
    
    //******** 创建对应的vc ********//
    // 获取vc后，先进行setup，成功后再push对应vc
    self.studentVC4Phone = [[ASCRBClassroomManager sharedInstance] createStudentVC4PhoneWithAppInitConfig:self.appConfig classInitConfig:classInitConfig];
    self.studentVC4Phone.delegate = self;
    [self.studentVC4Phone setup];
}

// 在对应的setup成功事件中push创建的vc
#pragma mark  - ASCRBStudentViewController4PhoneProtocolDelegate
- (void)onASCRBStudentViewController4Phone:(nonnull id<ASCRBStudentViewController4PhoneProtocol>)classroomVC classroomEvent:(ASCRBClassroomEvent)classroomEvent info:(nonnull NSDictionary *)info {
    switch (classroomEvent) {
        case ASCRBClassroomEventSetupSucceed:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.navigationController pushViewController:self.studentVC4Phone animated:YES];
            });
        }
            break;
        case ASCRBClassroomEventLeaveClassroom:
            self.studentVC4Phone = nil;
            break;
            
        default:
            break;
    }
}

- (void) onASCRBStudentViewController4Phone:(id<ASCRBStudentViewController4PhoneProtocol>)classroomVC classroomError:(ASCRBClassroomError)classroomError withErrorMessage:(NSString*)errorMessage {
    switch (classroomError) {
        case ASCRBClassroomErrorFailedToSetup:
            break;
            
        default:
            break;
    }
}

//************** 以下为简易登录UI ***************//
// 初始化登录UI
- (void) setupUI{
    UILabel* classTitle = [[UILabel alloc] init];
    classTitle.text = @"阿里云互动课堂";
    classTitle.frame = CGRectMake(0, 64, self.view.bounds.size.width, 100);
    classTitle.textAlignment = NSTextAlignmentCenter;
    classTitle.textColor = [UIColor blackColor];
    [classTitle setFont:[UIFont systemFontOfSize:35]];
    [self.view addSubview:classTitle];
    
    UITextField* classIDTextField = [[UITextField alloc] init];
    classIDTextField.placeholder = @"课程号(必填)";
    classIDTextField.frame = CGRectMake(50, 184, self.view.bounds.size.width - 100, 30);
    classIDTextField.textColor = [UIColor blackColor];
    classIDTextField.textAlignment = NSTextAlignmentLeft;
    classIDTextField.keyboardType = UIKeyboardTypeDefault;
    classIDTextField.returnKeyType = UIReturnKeySend;
    classIDTextField.keyboardAppearance = UIKeyboardAppearanceDefault;
    classIDTextField.delegate = self;
    classIDTextField.borderStyle = UITextBorderStyleRoundedRect;
    [self.view addSubview:classIDTextField];
    self.classIDTextField = classIDTextField;
    
    UITextField* userIDTextField = [[UITextField alloc] init];
    userIDTextField.placeholder = @"用户ID(必填,字母或数字)";
    userIDTextField.frame = CGRectMake(50, 234, self.view.bounds.size.width - 100, 30);
    userIDTextField.textColor = [UIColor blackColor];
    userIDTextField.textAlignment = NSTextAlignmentLeft;
    userIDTextField.keyboardType = UIKeyboardTypeDefault;
    userIDTextField.returnKeyType = UIReturnKeySend;
    userIDTextField.keyboardAppearance = UIKeyboardAppearanceDefault;
    userIDTextField.delegate = self;
    userIDTextField.borderStyle = UITextBorderStyleRoundedRect;
    [self.view addSubview:userIDTextField];
    self.userIDTextField = userIDTextField;
    
    UITextField* userNickTextField = [[UITextField alloc] init];
    userNickTextField.placeholder = @"用户昵称(必填)";
    userNickTextField.frame = CGRectMake(50, 284, self.view.bounds.size.width - 100, 30);
    userNickTextField.textColor = [UIColor blackColor];
    userNickTextField.textAlignment = NSTextAlignmentLeft;
    userNickTextField.keyboardType = UIKeyboardTypeDefault;
    userNickTextField.returnKeyType = UIReturnKeySend;
    userNickTextField.keyboardAppearance = UIKeyboardAppearanceDefault;
    userNickTextField.delegate = self;
    userNickTextField.borderStyle = UITextBorderStyleRoundedRect;
    [self.view addSubview:userNickTextField];
    self.userNickTextField = userNickTextField;
    
    UIButton* enterclassRoomButton = [[UIButton alloc] init];
    enterclassRoomButton.frame = CGRectMake(50, 334, self.view.bounds.size.width - 100, 60);
    [enterclassRoomButton addTarget:self action:@selector(enterClassroomButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    enterclassRoomButton.backgroundColor = [UIColor blueColor];
    [enterclassRoomButton setTitle:@"进入教室" forState:UIControlStateNormal];
    [self.view addSubview:enterclassRoomButton];
}

// 获取输入的信息并进入课堂
- (void) enterClassroomButtonAction:(UIButton*)sender{
    // 未判断输入是否合法，需自行添加判断逻辑
    self.classID = self.classIDTextField.text;
    self.userID = self.userIDTextField.text;
    self.userNick = self.userNickTextField.text;
    [self setupStudentVC4Phone];
}

#pragma -mark UITextFieldDelegate
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];
    return YES;
}

@end

