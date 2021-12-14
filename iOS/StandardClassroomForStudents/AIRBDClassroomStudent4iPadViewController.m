//
//  AIRBDClassroomStudent4iPadViewController.m
//  StandardClassroomForStudents
//
//  Created by 君渡 on 2021/12/13.
//

#import "AIRBDClassroomStudent4iPadViewController.h"
#import "AIRBDEnvironments.h"
#import <AliStandardClassroomBundle/AliStandardClassroomBundle.h>

@import AliInteractiveRoomBundle;

@interface AIRBDClassroomStudent4iPadViewController()<ASCRBStudentViewController4PadProtocolDelegate>

@property(nonatomic, copy) NSString* userID;
@property(nonatomic, copy) NSString* userNick;
@property(nonatomic, copy) NSString* classID;

@property (nonatomic, strong) ASCRBAppInitConfig* appConfig;
@property (nonatomic, strong) id<ASCRBStudentViewController4PadProtocol> studentVC4Pad;

@end

@implementation AIRBDClassroomStudent4iPadViewController

-(instancetype) initWithUserID:(NSString*)userID userNick:(NSString*)userNick classID:(NSString* _Nullable) classID{
    self = [super init];
    if(self){
        _userID = userID;
        _classID = classID;
        _userNick = userNick;
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
        
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
    self.studentVC4Pad = [[ASCRBClassroomManager sharedInstance] createStudentVC4PadWithAppInitConfig:self.appConfig classInitConfig:classInitConfig];
    self.studentVC4Pad.delegate = self;
    [self.studentVC4Pad setup];
}

// 在对应的setup成功事件中push创建的vc
#pragma mark  - ASCRBStudentViewController4PadProtocolDelegate
- (void)onASCRBStudentViewController4Pad:(nonnull id<ASCRBStudentViewController4PadProtocol>)classroomVC classroomEvent:(ASCRBClassroomEvent)classroomEvent info:(nonnull NSDictionary *)info {
    switch (classroomEvent) {
        case ASCRBClassroomEventSetupSucceed:{
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.navigationController pushViewController:self.studentVC4Pad animated:YES];
            });
        }
            break;
        case ASCRBClassroomEventLeaveClassroom:
            self.studentVC4Pad = nil;
            break;
            
        default:
            break;
    }
}

- (void) onASCRBStudentViewController4Pad:(id<ASCRBStudentViewController4PadProtocol>)classroomVC classroomError:(ASCRBClassroomError)classroomError withErrorMessage:(NSString*)errorMessage {
    switch (classroomError) {
        case ASCRBClassroomErrorFailedToSetup:
            break;
            
        default:
            break;
    }
}

@end

