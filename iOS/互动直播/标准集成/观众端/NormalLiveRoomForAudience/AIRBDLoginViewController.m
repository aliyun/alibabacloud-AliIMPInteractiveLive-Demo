//
//  AIRBDLoginViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/23.
//

#import "AIRBDLoginViewController.h"
#import "AIRBDToast.h"
#import "AIRBDEnvironments.h"
#import <Masonry/Masonry.h>
#import "AIRBDAudienceViewController.h"
#import "AIRBDRoomInfoModel.h"

@import AliInteractiveRoomBundle;

@interface AIRBDLoginViewController ()<AIRBRoomEngineDelegate>
@property (copy, nonatomic) NSString* userID;
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@end

@implementation AIRBDLoginViewController


- (void)viewDidLoad {
    [super viewDidLoad];
    [self login];
}

#pragma mark - AIRBRoomEngineDelegate

- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBRoomEngineEventEngineStarted: // 初始化成功，进行登录
            [[AIRBRoomEngine sharedInstance] loginWithUserID:self.userID];
            break;
        case AIRBRoomEngineEventEngineLogined: { // 登录成功
            dispatch_async(dispatch_get_main_queue(), ^{
                AIRBDAudienceViewController* audienceCiewController = [[AIRBDAudienceViewController alloc]init];
                audienceCiewController.roomModel = [[AIRBDRoomInfoModel alloc] init];
                audienceCiewController.roomModel.roomID = @"";
                audienceCiewController.roomModel.userID = self.userID;
                audienceCiewController.roomModel.userNickName = @"";
                [self.navigationController pushViewController:audienceCiewController animated:YES];
                [self.navigationController setNavigationBarHidden:YES];
                [audienceCiewController enterRoom];
            });
        }
            break;
            
        default:
            break;
    }
}

// 获取登录token
- (void) onAIRBRoomEngineRequestToken:(void (^)(AIRBRoomEngineAuthToken * _Nonnull))onTokenGotten {
    NSString* path = [NSString stringWithFormat:@"%@/api/login/getToken", [AIRBDEnvironments shareInstance].appServerUrl]; //获取token的服务地址
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&appKey=%@&userId=%@&deviceId=%@", path, self.config.appID, self.config.appKey, _userID, self.config.deviceID];
    
    NSString* dateString = [AIRBUtility currentDateString];
    NSString* nonce = [AIRBUtility randomNumString];
    
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
    
    NSString* signedString = [AIRBUtility getSignedRequestStringWithSecret:[AIRBDEnvironments shareInstance].signSecret method:@"POST" path:path parameters:params headers:headers];
    
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
                    onTokenGotten(token);
                    return;
                }
            }
        }
    }];
    [task resume];
}

- (void) onRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString *)msg object:(AIRBRoomEngine *)object {
    
}

- (void) login {
    
    self.userID = @"xxxx"; //必传，自定义用户id，必须是英文字母或者阿拉伯数字或者二者组合
    self.config = [[AIRBRoomEngineConfig alloc] init];
    self.config.appID = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppID; //在控制台开通标准接入互动直播后获取的应用ID；
    self.config.appKey = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppKey; //在控制台开通标准接入互动直播后获取的iOS端的App Key；
    
    self.config.deviceID = [[UIDevice currentDevice] identifierForVendor].UUIDString;
    self.userID = self.userID;
    [AIRBRoomEngine sharedInstance].delegate = self;
    [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:self.config];
}

@end

