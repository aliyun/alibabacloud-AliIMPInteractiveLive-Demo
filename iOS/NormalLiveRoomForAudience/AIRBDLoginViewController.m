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
#import "AIRBDEnvironments.h"
#import <Masonry/Masonry.h>
#import "AIRBDAnchorViewController.h"
#import "AIRBDRoomInfoModel.h"


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
        case AIRBRoomEngineEventEngineStarted:
            [[AIRBRoomEngine sharedInstance] loginWithUserID:self.userID];
            break;
        case AIRBRoomEngineEventEngineLogined: {
            dispatch_async(dispatch_get_main_queue(), ^{
                AIRBDAnchorViewController* anchorViewController = [[AIRBDAnchorViewController alloc]init];
                anchorViewController.roomModel = [[AIRBDRoomInfoModel alloc] init];
                anchorViewController.roomModel.userID = self.userID;
                anchorViewController.roomModel.config = self.config;
                anchorViewController.roomModel.notice = @"";
                anchorViewController.roomModel.title = @"";
                [self.navigationController pushViewController:anchorViewController animated:YES];
                [self.navigationController setNavigationBarHidden:YES];
                [anchorViewController createRoomWithCompletion:^(NSString * _Nonnull roomID) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        anchorViewController.roomModel.roomID = roomID;
                        [anchorViewController enterRoom];
                        UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"房间ID" message:roomID preferredStyle:UIAlertControllerStyleAlert];
                        [alert addAction:[UIAlertAction actionWithTitle:@"拷贝" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                            UIPasteboard *pboard = [UIPasteboard generalPasteboard];
                            pboard.string = roomID;
                        }]];
                        [self presentViewController:alert animated:true completion:nil];
                    });
                }];
            });
        }
            break;
            
        default:
            break;
    }
}

- (void) onAIRBRoomEngineRequestToken:(void (^)(AIRBRoomEngineAuthToken * _Nonnull))onTokenGotten {
    NSString* path = [NSString stringWithFormat:@"%@/api/login/getToken", [AIRBDEnvironments shareInstance].appServerUrl];
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
    
    self.userID = @"xxxx";
    self.config = [[AIRBRoomEngineConfig alloc] init];
    self.config.appID = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppID;
    self.config.appKey = [AIRBDEnvironments shareInstance].interactiveLiveRoomAppKey;
    
    self.config.deviceID = [[UIDevice currentDevice] identifierForVendor].UUIDString;
    self.userID = self.userID;
    [AIRBRoomEngine sharedInstance].delegate = self;
    [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:self.config];
}

@end

