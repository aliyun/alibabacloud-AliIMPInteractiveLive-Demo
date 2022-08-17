//
//  InteractiveMessageViewController.m
//  InteractiveMessageForCreator
//
//  Created by fernando on 2022/2/15.
//

#import "InteractiveMessageViewController.h"

#import <Masonry/Masonry.h>

#import "AIRBDEnvironments.h"
#import "AIRBDToast.h"

@import AliInteractiveRoomBundle;
@import UIKit;

@interface InteractiveMessageViewController () <UITextFieldDelegate, AIRBRoomEngineDelegate, AIRBRoomChannelDelegate>
@property (strong, nonatomic) UITextField* inputTextField;
@property (strong, nonatomic) NSString* userID;
@property (strong, nonatomic) NSString* roomID;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@end

@implementation InteractiveMessageViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    
    self.userID = @"123456";
    self.roomID = @"e5267475-ffe5-447d-90cb-bec6fb8b944d";
    [self p_initRoomEngine];
}

#pragma mark -Properties
- (UITextField*) inputTextField {
    if (!_inputTextField) {
        UITextField* textField = [[UITextField alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:textField];
        [textField mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-29);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-29);
            }
            make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
            make.right.equalTo(weakSelf.view.mas_right).with.offset(-10);
            make.height.mas_equalTo(50);
        }];
        textField.layer.masksToBounds = YES;
        textField.layer.cornerRadius = 20;
        textField.textColor = [UIColor blackColor];
        textField.alpha = 0.8;
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"说点什么……"
                                                                         attributes:@{
                                                                             NSForegroundColorAttributeName:[UIColor whiteColor],
                                                                             NSFontAttributeName:[UIFont systemFontOfSize:14]
                                                                         }];
        textField.attributedPlaceholder = attrString;
        textField.backgroundColor = [UIColor colorWithWhite:0.1 alpha:0.7];
        textField.textAlignment = NSTextAlignmentLeft;
        textField.keyboardType = UIKeyboardTypeDefault;
        textField.returnKeyType = UIReturnKeySend;
        textField.keyboardAppearance = UIKeyboardAppearanceDefault;
        textField.delegate = self;
        textField.borderStyle = UITextBorderStyleRoundedRect;
        [textField setContentHuggingPriority:UILayoutPriorityRequired forAxis:UILayoutConstraintAxisHorizontal];
        _inputTextField = textField;
    }
    
    return _inputTextField;
}

#pragma mark -Private Methods
- (void) p_initRoomEngine {
    
    [[AIRBRoomEngine sharedInstance] setDelegate:self];
    [[AIRBRoomEngine sharedInstance] globalInitOnceWithConfig:({
        AIRBRoomEngineConfig* config = [[AIRBRoomEngineConfig alloc] init];
        config.appID = [AIRBDEnvironments shareInstance].interactiveMessageRoomAppID;
        config.appKey = [AIRBDEnvironments shareInstance].interactiveMessageRoomAppKey;
        config.deviceID = [[[UIDevice currentDevice] identifierForVendor] UUIDString];
        config;
    })];
}

- (void) p_loginRoomEngine {
    
    [[AIRBRoomEngine sharedInstance] loginWithUserID:self.userID];
}

- (void) p_enterRoom {
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.roomID];
    self.room.delegate = self;;
    
    [self.room enterRoomWithUserNick:@"你的昵称"];
}

- (void)p_leaveRoom {
    [self.room leaveRoom];
}

- (void) p_loadUI {
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.view bringSubviewToFront:self.inputTextField];
    });
}

#pragma mark -AIRBRoomEngineDelegate

- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBRoomEngineEventEngineStarted:
            [self p_loginRoomEngine];
            break;
            
        case AIRBRoomEngineEventEngineLogined: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"登录成功！" duration:1.0];
                
                [self p_enterRoom];
            });
        }
            
        default:
            break;
    }
}

- (void) onAIRBRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString *)msg {
    
}

- (void)onAIRBRoomEngineRequestToken:(void (^)(AIRBRoomEngineAuthToken * _Nonnull))onTokenGotten {
    NSString* path = [NSString stringWithFormat:@"%@/api/login/getToken", [AIRBDEnvironments shareInstance].appServerURL];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&appKey=%@&userId=%@&deviceId=%@", path, [AIRBDEnvironments shareInstance].interactiveMessageRoomAppID, [AIRBDEnvironments shareInstance].interactiveMessageRoomAppKey, self.userID, [[[UIDevice currentDevice] identifierForVendor] UUIDString]];
    
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
        @"appId" : [AIRBDEnvironments shareInstance].interactiveMessageRoomAppID,
        @"appKey" : [AIRBDEnvironments shareInstance].interactiveMessageRoomAppKey,
        @"userId" : self.userID,
        @"deviceId" : [[[UIDevice currentDevice] identifierForVendor] UUIDString]
    };
    
    NSString* signedString = [AIRBUtility getSignedRequestStringWithSecret:[AIRBDEnvironments shareInstance].appServerSignSecret method:@"POST" path:path parameters:params headers:headers];
    
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

#pragma mark -AIRBRoomChannelDelegate

- (void)onAIRBRoomChannelEvent:(AIRBRoomChannelEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            [self p_loadUI];
        }
            break;
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
            NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            switch (type) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated:
                    break;
                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:
                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived: {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:@"你收到一个点赞" duration:2.0];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived:{
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@: %@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]] duration:2.0];
                    });
                }
                    break;
                case AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot:
                    break;
                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
                    break;
                default:
                    break;
            };
        }
            
        default:
            break;
    }
}

- (void)onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString *)message {
    ;
}

#pragma mark -UITextFieldDelegate
- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    if (textField == _inputTextField && textField.text.length > 0) {
        [self.room.chat sendComment:textField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });;
        } onFailure:^(AIRBErrorCode code, NSString * _Nonnull message) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[@"发送失败: " stringByAppendingString:message] duration:1.0];
            });;
        }];
        _inputTextField.text = nil;
    }
    
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
    return YES;
}


#pragma mark -NSNotification
- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘高度
    CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat keyBoardHeight = keyBoardBounds.size.height;
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];

    // 定义好动作
    __weak typeof(self) weakSelf = self;
    void (^animation)(void) = ^void(void) {
        if(self->_inputTextField.isEditing == YES){
            self->_inputTextField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - 30));
            self->_inputTextField.layer.cornerRadius = 2;
            self->_inputTextField.backgroundColor = [UIColor lightGrayColor];
            [self->_inputTextField mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.left.equalTo(weakSelf.view);
                make.right.equalTo(weakSelf.view);
                make.bottom.equalTo(weakSelf.view.mas_bottom).with.offset(-33);
                make.height.mas_equalTo(50);
            }];
        }
    };

    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)keyBoardWillHide:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];

    // 定义好动作
    __weak typeof(self) weakSelf = self;
    void (^animation)(void) = ^void(void) {
        self->_inputTextField.transform = CGAffineTransformIdentity;
        self->_inputTextField.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        self->_inputTextField.layer.cornerRadius = 20;
        [self->_inputTextField mas_remakeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-29);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-29);
            }
            make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
            make.right.equalTo(weakSelf.view.mas_right).with.offset(-10);
            make.height.mas_equalTo(50);
        }];
    };

    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}
@end
