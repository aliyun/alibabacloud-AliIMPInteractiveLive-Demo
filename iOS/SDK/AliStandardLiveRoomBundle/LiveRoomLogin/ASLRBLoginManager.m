//
//  ASLRBLoginManager.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/26.
//

#import "ASLRBLoginManager.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>

#import "../LiveRoomSetup/ASLRBAppInitConfig.h"
#import "../LiveRoomSetup/ASLRBLiveInitConfig.h"
#import "../LiveRoom/ASLRBLiveRoomLoginDelegate.h"
#import "ASLRBLogger.h"

@interface ASLRBLoginManager() <AIRBRoomEngineDelegate>
@property (nonatomic, strong) AIRBRoomEngineConfig* roomEngineConfig;
@property (nonatomic, strong) ASLRBAppInitConfig* inputAppInitConfig;
@property (nonatomic, strong) ASLRBAppInitConfig* loginedAppInitConfig;
@property (nonatomic, strong) NSTimer* logoutTimer;
@property (nonatomic, assign) BOOL isSwitchingUser;
@property (nonatomic, copy) NSString* switchingUserID;
@property (copy, nonatomic) void(^onGlobalInitSuccess)(ASLRBAppInitConfig* appInitConfig);
@property (copy, nonatomic) void(^onGlobalInitFailure)(NSString* errorMessage);
@property (nonatomic, strong) dispatch_queue_t taskQueue;
@property (atomic, assign) int8_t logoutTimerCount;
@end

@implementation ASLRBLoginManager

- (instancetype) init {
    self = [super init];
    if (self) {
        _loginedAppInitConfig = [[ASLRBAppInitConfig alloc] init];
        
        if (!_logoutTimer) {
            _logoutTimer = [NSTimer scheduledTimerWithTimeInterval:60.0 target:self selector:@selector(logout) userInfo:nil repeats:YES];
            _logoutTimer.fireDate = [NSDate distantFuture];
        }
        
        _taskQueue = dispatch_queue_create("com.aliyun.vc.imp.standardlive", DISPATCH_QUEUE_SERIAL);
        _logoutTimerCount = 0;
    }
    return self;
}

- (void) loginWithConfig:(ASLRBAppInitConfig*)config
               onSuccess:(void (^)(ASLRBAppInitConfig * _Nonnull))onSuccess
               onFailure:(void (^)(NSString * _Nonnull))onFailure {
    dispatch_async(self.taskQueue, ^{
        
        self.inputAppInitConfig = config;
        
        [self stopLogoutTimer];
        
        LOG("ASLRBLoginManager::login with user(%@)", self.inputAppInitConfig.userID);
        
        //已有登陆，则退出，重新登录新user
        if (self.loginedAppInitConfig.userID.length > 0 && ![self.loginedAppInitConfig.userID isEqualToString:self.inputAppInitConfig.userID]) { //不相等时默认把前一个登出
            LOG("ASLRBLoginManager::logout pre user(%@).", self.loginedAppInitConfig.userID);
            [[AIRBRoomEngine sharedInstance] logoutOnSuccess:^{
                LOG("ASLRBLoginManager::login new user(%@).", self.inputAppInitConfig.userID);
                self.onGlobalInitSuccess = onSuccess;
                self.onGlobalInitFailure = onFailure;
                [[AIRBRoomEngine sharedInstance] loginWithUserID:self.inputAppInitConfig.userID];
            } onFailure:^(NSString * _Nonnull errorMessage) {
                onFailure(errorMessage);
            }];
            return;
        }
        
        //当前新user已重新则直接返回
        if ([[AIRBRoomEngine sharedInstance] isLogined:self.inputAppInitConfig.userID]){
            LOG("ASLRBLoginManager::current user(%@) logined already.", self.inputAppInitConfig.userID);
            [self updateLoginedAppInitConfig];
            onSuccess(self.loginedAppInitConfig);
            return;
        }
        
        self.onGlobalInitSuccess = onSuccess;
        self.onGlobalInitFailure = onFailure;
        
        self.roomEngineConfig = [[AIRBRoomEngineConfig alloc] init];
        self.roomEngineConfig.appID = self.inputAppInitConfig.appID;
        self.roomEngineConfig.appKey = self.inputAppInitConfig.appKey;
        self.roomEngineConfig.deviceID = [[UIDevice currentDevice] identifierForVendor].UUIDString;
        [AIRBRoomEngine sharedInstance].delegate = self;
        [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:self.roomEngineConfig];
        [[AIRBRoomEngine sharedInstance] setLogLevel:AIRBLoggerLevelDebug];
    });
}

- (void) logout {
    if (_logoutTimerCount-- <= 0) {
        [self stopLogoutTimer];
        dispatch_async(self.taskQueue, ^{
            LOG("ASLRBLoginManager::begin logout.");
            [[AIRBRoomEngine sharedInstance] logoutOnSuccess:^{
                [self resetLoginedAppInitConfig];
            } onFailure:^(NSString * _Nonnull errorMessage) {
                LOG("ASLRBLoginManager::logout failed(%@).", errorMessage);
            }];
        });
    }
}

- (void) startLogoutTimer {
    LOG("ASLRBLoginManager::start timer.");
    _logoutTimerCount = 3;
    self.logoutTimer.fireDate = [NSDate distantPast];
}

- (void) stopLogoutTimer {
    LOG("ASLRBLoginManager::stop timer.");
    self.logoutTimer.fireDate = [NSDate distantFuture];
}

- (void) updateLoginedAppInitConfig {
    self.loginedAppInitConfig.appID = self.inputAppInitConfig.appID;
    self.loginedAppInitConfig.appKey = self.inputAppInitConfig.appKey;
    self.loginedAppInitConfig.appServerSignSecret = self.inputAppInitConfig.appServerSignSecret;
    self.loginedAppInitConfig.appServerUrl = self.inputAppInitConfig.appServerUrl;
    self.loginedAppInitConfig.userID = self.inputAppInitConfig.userID;
    self.loginedAppInitConfig.userNick = self.inputAppInitConfig.userNick;
    self.loginedAppInitConfig.userExtension = self.inputAppInitConfig.userExtension;
}

- (void) resetLoginedAppInitConfig {
    self.loginedAppInitConfig.appID = @"";
    self.loginedAppInitConfig.appKey = @"";
    self.loginedAppInitConfig.appServerSignSecret = @"";
    self.loginedAppInitConfig.appServerUrl = @"";
    self.loginedAppInitConfig.userID = @"";
    self.loginedAppInitConfig.userNick = @"";
    self.loginedAppInitConfig.userExtension = @{};
}

#pragma mark - AIRBRoomEngineDelegate

- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(NSDictionary *)info {
    LOG("ASLRBLoginManager::onAIRBRoomEngineEvent(%ld, %@)", (long)event, info);
    switch (event) {
        case AIRBRoomEngineEventEngineStarted:
            LOG("ASLRBLoginManager::AIRBRoomEngineEventEngineStarted");
            [[AIRBRoomEngine sharedInstance] loginWithUserID:self.inputAppInitConfig.userID];
            break;
        case AIRBRoomEngineEventEngineLogined: {
            LOG("ASLRBLoginManager::AIRBRoomEngineEventEngineLogined");
            if (self.isSwitchingUser) {
                self.switchingUserID = nil;
                self.isSwitchingUser = NO;
            }
            
            [self updateLoginedAppInitConfig];
            
            if (self.onGlobalInitSuccess) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.onGlobalInitSuccess(self.loginedAppInitConfig);
                });
            }
        }
            break;
        case AIRBRoomEngineEventEngineKickedOut: {
            LOG("ASLRBLoginManager::AIRBRoomEngineEventEngineKickedOut");
            if ([self.delegate respondsToSelector:@selector(onASLRBLoginManagerEventKickedOut)]){
                [self.delegate onASLRBLoginManagerEventKickedOut];
            }
        }
            break;
            
        default:
            break;
    }
}

- (void) onAIRBRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString *)msg {
    LOG("ASLRBLoginManager::onAIRBRoomEngineErrorWithCode(0x%x, %@)", code, msg);
    if (self.onGlobalInitFailure) {
        self.onGlobalInitFailure([NSString stringWithFormat:@"(%ld, %@)", (long)code, msg]);
    }
}

- (void) onAIRBRoomEngineRequestToken:(void (^)(AIRBRoomEngineAuthToken * _Nonnull))onTokenGotten {
    LOG("ASLRBLoginManager::onAIRBRoomEngineRequestToken");
    NSString* path = [NSString stringWithFormat:@"%@/api/login/getToken", self.inputAppInitConfig.appServerUrl];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&appKey=%@&userId=%@&deviceId=%@", path, self.roomEngineConfig.appID, self.roomEngineConfig.appKey, self.inputAppInitConfig.userID, self.roomEngineConfig.deviceID];
    
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
        @"appId" : self.roomEngineConfig.appID,
        @"appKey" : self.roomEngineConfig.appKey,
        @"userId" : self.inputAppInitConfig.userID,
        @"deviceId" : self.roomEngineConfig.deviceID
    };
        
    NSString* signedString = [AIRBUtility getSignedRequestStringWithSecret:self.inputAppInitConfig.appServerSignSecret method:@"POST" path:path parameters:params headers:headers];
    
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
    __weak typeof(self) weakSelf = self;
    NSURLSessionTask* task = [session dataTaskWithRequest:request completionHandler:^(NSData * _Nullable data, NSURLResponse * _Nullable response, NSError * _Nullable error) {
        AIRBRoomEngineAuthToken* token = nil;
        if (data && !error) {
            NSDictionary* dic = [NSJSONSerialization JSONObjectWithData:data
                                                                options:NSJSONReadingMutableContainers
                                                                  error:nil];
            
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 1 && [resultDic valueForKey:@"accessToken"] && [resultDic valueForKey:@"refreshToken"]) {
                    token = [[AIRBRoomEngineAuthToken alloc] init];
                    token.accessToken = [NSString stringWithString:[resultDic valueForKey:@"accessToken"]];
                    token.refreshToken = [NSString stringWithString:[resultDic valueForKey:@"refreshToken"]];
                } else {
                    if (weakSelf.onGlobalInitFailure) {
                        weakSelf.onGlobalInitFailure(dic[@"message"]);
                    }
                }
            }
            LOG("ASLRBLoginManager::onAIRBRoomEngineRequestToken response(%@)", dic);
        }
        onTokenGotten(token);
    }];
    [task resume];
}

- (void) onLog:(NSString *)message {
    LOG("%@", message);
}

#pragma mark - ASLRBLiveRoomLoginDelegate

- (void) switchLoginedUser:(NSString *)newUserID onSuccess:(void (^)(ASLRBAppInitConfig * _Nonnull))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    dispatch_async(self.taskQueue, ^{
        self.isSwitchingUser = YES;
        self.switchingUserID = newUserID;
        self.onGlobalInitSuccess = onSuccess;
        self.onGlobalInitFailure = onFailure;
        [[AIRBRoomEngine sharedInstance] logoutOnSuccess:^{
            self.inputAppInitConfig.userID = newUserID;
            [[AIRBRoomEngine sharedInstance] loginWithUserID:self.inputAppInitConfig.userID];
        } onFailure:^(NSString * _Nonnull errorMessage) {
            onFailure(errorMessage);
        }];
    });
}

- (void) dispatchLogoutTask {
    LOG("ASLRBLoginManager::dispatchLogoutTask.");
    [self startLogoutTimer];
}

@end
