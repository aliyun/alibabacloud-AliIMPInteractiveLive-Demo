//
//  AIRBDAudienceView.m
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/14.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBDAudienceView.h"
#import "AIRBDCommentView.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "AIRBDToast.h"
#import "Utilities/Utility.h"
#import <Masonry/Masonry.h>
@interface AIRBDAudienceView() <AIRBRoomChannelDelegate, UITextFieldDelegate, AIRBLivePlayerDelegate>
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;
@property (assign, nonatomic) BOOL roomEntered;
@property (assign, nonatomic) BOOL noticeShowed;
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* roomID;
@property (strong, nonatomic) NSMutableArray* infoDictionary;
@property (strong, nonatomic) UIView* pushPreview;
@property (strong, nonatomic) UIView* playView;
@property (strong, nonatomic) UITextField* sendField;
@property (strong, nonatomic) UIButton* exitButton;
@property (strong, nonatomic) UIButton* pauseButton;
@property (strong, nonatomic) UIButton* likeButton;
@property (strong, nonatomic) UIButton* mutedButton;
@property (strong, nonatomic) UISlider* volumeSlider;
@property (strong, nonatomic) UILabel* volumeName;
@property (strong, nonatomic) UILabel* roomTitle;
@property (strong, nonatomic) UITextView* roomNotice;
@property (strong, nonatomic) UIButton* scaleModeButton;
@property (strong, nonatomic) UIButton* shareButton;
@property (strong, nonatomic) UIButton * setButton;



@property (assign, nonatomic) int audienceNum;
@property (strong, nonatomic) UIButton* moreButton;
@property (strong, nonatomic) UIButton* addButton;
@property (strong, nonatomic) UIButton* goodsButton;
@property (assign, nonatomic) BOOL moreButtonShowed;
@property (assign, nonatomic) BOOL isMuted;
@property (assign, nonatomic) BOOL isStopped;
@property (assign, nonatomic) BOOL isSetting;
@property (strong, nonatomic) UILabel* roomInfo;
@property (strong, nonatomic) UIImageView* userProfileImageView;
@property (strong, nonatomic) AIRBDCommentView* commentView;

@end

@implementation AIRBDAudienceView

- (UITextField*) sendField {
    if (!_sendField) {
        if (!_sendField) {
            _sendField = [[UITextField alloc] init];
            __weak typeof(self) weakSelf = self;
            [self addSubview:_sendField];
            [_sendField mas_makeConstraints:^(MASConstraintMaker *make) {
                make.left.equalTo(weakSelf.goodsButton).with.offset(60);
                make.bottom.equalTo(weakSelf.mas_bottom).with.offset(-33);
                make.right.equalTo(weakSelf.likeButton.mas_left).with.offset(-10);
                make.height.mas_equalTo(30);
            }];
            NSLog(@"boundsinfo:width:%f height:%f",self.bounds.size.width,self.bounds.size.height);
            _sendField.textColor = [UIColor blackColor];
            _sendField.alpha = 0.8;
            _sendField.placeholder = @"说点什么…";
            _sendField.backgroundColor = [UIColor grayColor];
            _sendField.textAlignment = NSTextAlignmentLeft;
            _sendField.keyboardType = UIKeyboardTypeDefault;
            _sendField.returnKeyType = UIReturnKeySend;
            _sendField.keyboardAppearance = UIKeyboardAppearanceDefault;
            _sendField.delegate = self;
            _sendField.borderStyle = UITextBorderStyleRoundedRect;
        }
    }
    return _sendField;
}

- (UIButton*) exitButton{
    if (!_exitButton) {
        _exitButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_exitButton];
        [_exitButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.mas_right).with.offset(-5);
            make.top.equalTo(weakSelf.mas_top).with.offset(20);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_exitButton setImage:[UIImage imageNamed:@"exit"] forState:UIControlStateNormal];
        [_exitButton addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _exitButton;
}

- (UIButton*) likeButton {
    if (!_likeButton) {
        _likeButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_likeButton];
        [_likeButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.shareButton.mas_left).with.offset(-10);
            make.centerY.equalTo(weakSelf.sendField.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_likeButton setImage:[UIImage imageNamed:@"like"] forState:UIControlStateNormal];
        [_likeButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_likeButton addTarget:self action:@selector(likeButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _likeButton;
}

- (UIButton*) pauseButton {
    if (!_pauseButton) {
        _isStopped = NO;
        _pauseButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_pauseButton];
        [_pauseButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton.mas_centerX);
            make.top.equalTo(weakSelf.exitButton.mas_bottom).with.offset(10);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        [_pauseButton setImage:[UIImage imageNamed:@"lock"] forState:UIControlStateNormal];
        [_pauseButton addTarget:self action:@selector(pauseButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _pauseButton;
}

- (UIButton*) mutedButton {
    if (!_mutedButton) {
        _isMuted = NO;
        _mutedButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_mutedButton];
        [_mutedButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton.mas_centerX);
            make.top.equalTo(weakSelf.pauseButton.mas_bottom).with.offset(10);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        [_mutedButton setImage:[UIImage imageNamed:@"stop"] forState:UIControlStateNormal];
        [_mutedButton addTarget:self action:@selector(mutedButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _mutedButton;
}

- (UIButton*) setButton {
    if (!_setButton) {
        _setButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_setButton];
        [_setButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.exitButton);
            make.top.equalTo(weakSelf.mutedButton.mas_bottom).with.offset(10);
            make.width.mas_equalTo(50);
            make.height.mas_equalTo(50);
        }];
        _isSetting = NO;
        [_setButton setImage:[UIImage imageNamed:@"set"] forState:UIControlStateNormal];
        [_setButton addTarget:self action:@selector(setButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _setButton;
}

- (UIButton*) scaleModeButton {
    if (!_scaleModeButton) {
        _scaleModeButton = [[UIButton alloc] init];
        [self addSubview:_scaleModeButton];
        __weak typeof(self) weakSelf = self;
        [_scaleModeButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.roomTitle.mas_left);
            make.top.equalTo(weakSelf.volumeSlider.mas_bottom).with.offset(30);
            make.width.mas_equalTo(100);
            make.height.mas_equalTo(40);
        }];
        [_scaleModeButton setTitle:@"ScaleFit" forState:UIControlStateNormal];
        [_scaleModeButton setTitleColor:[UIColor blackColor] forState:UIControlStateNormal];
        [_scaleModeButton setBackgroundColor:[UIColor colorWithRed:0 green:0.3 blue:0.5 alpha:0.8]];
        [_scaleModeButton addTarget:self action:@selector(scaleModeButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _scaleModeButton;
}

- (UIButton*) shareButton {
    if (!_shareButton) {
        _shareButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_shareButton];
        [_shareButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.moreButton.mas_left).with.offset(-10);
            make.centerY.equalTo(weakSelf.sendField.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_shareButton setImage:[UIImage imageNamed:@"share"] forState:UIControlStateNormal];
        [_shareButton addTarget:self action:@selector(shareButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _shareButton;
}

- (UIButton*) addButton {
    if (!_addButton) {
        _addButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_addButton];
        [_addButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.roomTitle.mas_right).with.offset(9);
            make.centerY.equalTo(weakSelf.userProfileImageView.mas_centerY);
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(30);
        }];
        
        [_addButton setImage:[UIImage imageNamed:@"add"] forState:UIControlStateNormal];
        [_addButton addTarget:self action:@selector(addButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _addButton;
}

- (UIButton*) goodsButton {
    if (!_goodsButton) {
        _goodsButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_goodsButton];
        [_goodsButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mas_left).with.offset(9);
            make.centerY.equalTo(weakSelf.sendField.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_goodsButton setImage:[UIImage imageNamed:@"goods"] forState:UIControlStateNormal];
        [_goodsButton addTarget:self action:@selector(goodsButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _goodsButton;
}

- (UIButton*) moreButton {
    
    if (!_moreButton) {
        _moreButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_moreButton];
        [_moreButton mas_makeConstraints:^(MASConstraintMaker *make) {
            make.right.equalTo(weakSelf.mas_right).with.offset(-10);
            make.centerY.equalTo(weakSelf.sendField.mas_centerY);
            make.height.mas_equalTo(50);
            make.width.mas_equalTo(50);
        }];
        
        [_moreButton setImage:[UIImage imageNamed:@"more"] forState:UIControlStateNormal];
        _moreButtonShowed = FALSE;
        [_moreButton addTarget:self action:@selector(moreButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _moreButton;
}

- (UILabel*) roomTitle{
    if(!_roomTitle){
        self.roomTitle = [[UILabel alloc] init];
        [self addSubview:self.roomTitle];
        __weak typeof(self) weakSelf = self;
        [_roomTitle mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_right).with.offset(10);
            make.top.equalTo(weakSelf.mas_top).with.offset(40);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(30);
        }];
        [_roomTitle setText:@"DefaultTitle"];
        [_roomNotice setBackgroundColor:[UIColor grayColor]];
        [_roomTitle setTextColor:[UIColor whiteColor]];
        [_roomTitle setShadowColor:[UIColor blackColor]];
        [_roomTitle setBackgroundColor:[UIColor grayColor]];
        [_roomTitle.layer setCornerRadius:4];
        _roomTitle.layer.masksToBounds = YES;
        [_roomTitle setAlpha:0.7];
    }
    return _roomTitle;
}

- (UILabel*) roomInfo{
    if(!_roomInfo){
        self.roomInfo = [[UILabel alloc] init];
        [self addSubview:self.roomInfo];
        __weak typeof(self) weakSelf = self;
        [_roomInfo mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_right).with.offset(10);
            make.top.equalTo(weakSelf.roomTitle.mas_bottom).with.offset(5);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(25);
        }];
        [_roomInfo setText:@"房间ID:01 人数:0"];
        [_roomNotice setBackgroundColor:[UIColor grayColor]];
        [_roomInfo setTextColor:[UIColor whiteColor]];
        [_roomInfo setShadowColor:[UIColor blackColor]];
        [_roomInfo setBackgroundColor:[UIColor grayColor]];
        [_roomInfo.layer setCornerRadius:4];
        _roomInfo.layer.masksToBounds = YES;
        [_roomInfo setAlpha:0.7];
    }
    return _roomInfo;
}

- (UITextView*) roomNotice{
    if(!_roomNotice){
        self.roomNotice = [[UITextView alloc] init];
        [self addSubview:self.roomNotice];
        __weak typeof(self) weakSelf = self;
        [_roomNotice mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.userProfileImageView.mas_left);
            make.top.equalTo(weakSelf.userProfileImageView.mas_bottom).with.offset(10);
            make.width.mas_equalTo(150);
            make.height.mas_equalTo(90);
        }];
        [_roomNotice setText:@"DefaultNotice"];
        [_roomNotice setBackgroundColor:[UIColor grayColor]];
        [_roomNotice setTextColor:[UIColor whiteColor]];
        [_roomNotice setAlpha:0.8];
        [_roomNotice.layer setCornerRadius:4];
        [_roomNotice setEditable:NO];
    }

    return _roomNotice;
}

- (UIImageView*) userProfileImageView{
    if(!_userProfileImageView){
        _userProfileImageView = [[UIImageView alloc]initWithImage:[UIImage imageNamed:@"default_pic2"]];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_userProfileImageView];
        [_userProfileImageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.mas_left).with.offset(10);
            make.top.equalTo(weakSelf.mas_top).with.offset(40);
            make.width.mas_equalTo(60);
            make.height.mas_equalTo(60);
        }];
    }
    return _userProfileImageView;
}

- (AIRBDCommentView*) commentView{
    if(!_commentView){
        _commentView = [[AIRBDCommentView alloc]init];
        __weak typeof(self) weakSelf = self;
        [self addSubview:_commentView];
        [_commentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.sendField.mas_top).with.offset(-10);
            make.left.equalTo(weakSelf.mas_left).with.offset(5);
            make.right.equalTo(weakSelf.sendField.mas_right);
            make.height.mas_equalTo(0.3*weakSelf.bounds.size.height);
        }];
        
    }
    return _commentView;
}

- (UISlider*)volumeSlider {
    if (!_volumeSlider) {
        _volumeSlider = [[UISlider alloc]init];
        [self addSubview:_volumeSlider];
        [self bringSubviewToFront:_volumeSlider];
        __weak typeof(self) weakSelf = self;
        [_volumeSlider mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf).with.offset(5);
            make.top.equalTo(weakSelf.roomNotice.mas_bottom).with.offset(10);
            make.right.equalTo(weakSelf.mas_left).with.offset(weakSelf.bounds.size.width / 2);
            make.height.mas_equalTo(20);

        }];
        [self addSubview:_volumeSlider];
        _volumeSlider.maximumValue = 1.0;
        _volumeSlider.minimumValue = 0;
        _volumeSlider.value = 1.0;
        [_volumeSlider addTarget:self action:@selector(sliderValueChanged:) forControlEvents:UIControlEventTouchUpInside];
        
        self.volumeName = [[UILabel alloc] init];
        [self addSubview:self.volumeName];
        [_volumeName mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.volumeSlider.mas_right).with.offset(3);
            make.centerY.equalTo(weakSelf.volumeSlider.mas_centerY);
            make.height.mas_equalTo(40);
            make.right.equalTo(weakSelf.mas_right).with.offset(-60);
        }];
        

        
        [self.volumeName setText:@"音量"];
        [self.volumeName setTextColor:[UIColor whiteColor]];
    }
    return _volumeSlider;
}

#pragma mark - LifeCycle

- (instancetype) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
    return self;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self leaveRoom];
}

- (void) enterRoomWithID:(NSString*)roomID userID:(nonnull NSString *)userID{
    self.userID = userID;
    self.roomID = roomID;
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:roomID];
    self.room.delegate = self;
    [self setUpUI];
    [self.room enterRoom];
}

- (void) leaveRoom{
//    [self.room stopLivePlayer];
    if(self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
    }
}

#pragma mark - AIRBRoomChannelProtocol

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    NSLog(@"hqwei::onAIRBRoomChannelErrorWithCode:(%0lXlx, %@)", (long)code, message);
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onRoomChannelErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
    });
}

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info {
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.room.livePlayer.delegate = self;
            self.roomEntered = YES;
            [self.room.livePlayer start];
        }
            break;
        case AIRBRoomChannelEventRoomInfoGotten:{
            _infoDictionary = [info mutableCopy];
            [self updateRoomInfo];
        }
            break;
            
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
            break;
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
            NSString* messageType = @"";
            NSData *turnData = [[NSString stringWithFormat:@"%@",[info valueForKey:@"data"]]  dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
            NSString* comment = @"";
            int count = 0;
            switch (type) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    messageType = @"RoomMembersInOut";
                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
                    NSString* userID = [dataDic valueForKey:@"userId"];
                    if (userID.length > 0) {
                        if (enter) {
                            count = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]]intValue];
                            comment = [NSString stringWithFormat:@"%@ 进入了房间",[dataDic valueForKey:@"nick"]];
                            [_infoDictionary setValue:[NSNumber numberWithInt:count] forKey:@"onlineCount"];
                            [self updateRoomInfo];
                            [_commentView insertNewComment:comment];
                        } else {
                            count = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]]intValue];
                            comment = [NSString stringWithFormat:@"%@ 离开了房间",[dataDic valueForKey:@"nick"]];
                            [_infoDictionary setValue:[NSNumber numberWithInt:count] forKey:@"onlineCount"];
                            [self updateRoomInfo];
                            [_commentView insertNewComment:comment];
                        }
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    messageType = @"RoomTitleUpdated";
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:@"房间主题已更新" duration:3.0];
                    });
                    [self updateRoomTitle:[info valueForKey:@"data"]];
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated:
                    messageType = @"RoomNoticeUpdated";
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:@"房间公告已更新" duration:3.0];
                    });
                    [self updateRoomNotice:[info valueForKey:@"data"]];
                    break;
                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
                    messageType = @"LiveCreatedByOther";
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    messageType = @"LiveStartedByOther";
                    [self.room.livePlayer start];
//                    [self updatePlayerView];
                    comment = @"直播开始";
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
                    messageType = @"LiveStoppedByOther";
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:@"直播已结束" duration:3.0];
                        [self.room.livePlayer stop];
                    });
                    comment = @"直播已结束";
                    [_commentView insertNewComment:comment];
                }
                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived:
                    messageType = @"ChatLikeReceived";
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived:
                    messageType = @"ChatCommentReceived";
                    comment = [NSString stringWithFormat:@"%@:%@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeChatOneUserMutedOrUnmuted:
                    messageType = @"OneUserWasMuted";
                    if([[dataDic valueForKey:@"mute"] boolValue] == YES){
                        comment = [NSString stringWithFormat:@"%@被禁言%@秒",[dataDic valueForKey:@"muteUserNick"],[dataDic valueForKey:@"muteTime"]];
                    }else{
                        comment = [NSString stringWithFormat:@"%@被取消禁言",[dataDic valueForKey:@"muteUserNick"]];
                    }
                    
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
                    messageType = @"OneUserWasKickedOutRoom";
                    comment = [NSString stringWithFormat:@"%@被踢出房间",[dataDic valueForKey:@"kickUserName"]];
                    [_commentView insertNewComment:comment];
                    break;
                default:
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"type:%@,data:%@", messageType, [info valueForKey:@"data"] ? : nil] duration:2.0];
                    });
                    break;
            };
            break;
        }
            
        default:
            break;
    }
}

- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBLivePlayerEventStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self addSubview:self.room.livePlayer.playerView];
                [self sendSubviewToBack:self.room.livePlayer.playerView];
            });
        }

            break;
            
        case AIRBLivePlayerEventStartLoading:
            break;
        case AIRBLivePlayerEventNotification:{
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [[AIRBDToast shareInstance] makeToast:[info valueForKey:@"data"] duration:3.0];
//            });
        }
            break;
        default:
            break;
    }
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.sendField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    
    if (textField.text.length > 0) {
        [self.room.chat sendMessage:textField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送失败" duration:1.0];
            });;
        }];
    }
    self.sendField.text = nil;
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
     return YES;
}

- (void)keyBoardWillShow:(NSNotification *) note {
    // 获取用户信息
    NSDictionary *userInfo = [NSDictionary dictionaryWithDictionary:note.userInfo];
    // 获取键盘高度
    CGRect keyBoardBounds  = [[userInfo objectForKey:UIKeyboardFrameEndUserInfoKey] CGRectValue];
    CGFloat keyBoardHeight = keyBoardBounds.size.height;
    // 获取键盘动画时间
    CGFloat animationTime  = [[userInfo objectForKey:UIKeyboardAnimationDurationUserInfoKey] floatValue];
    
    // 定义好动作
    void (^animation)(void) = ^void(void) {
        if(self->_sendField.isEditing == YES){
            self->_sendField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - 30));
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
    void (^animation)(void) = ^void(void) {
        self.sendField.transform = CGAffineTransformIdentity;
    };
    
    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

- (void)likeButtonAction:(id)sender {
    [self.room.chat sendLikeWithCount:1 onSuccess:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"点赞成功" duration:1.0];
        });
    } onFailure:^(NSString * _Nonnull errorMessage) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"点赞失败" duration:1.0];
        });
    }];
}

- (void)pauseButtonAction:(UIButton*)sender {
    if (_isStopped == NO) {
        _isStopped = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已暂停" duration:1.0];
        });
        [self.room.livePlayer pause];
        [_pauseButton setImage:[UIImage imageNamed:@"locked"] forState:UIControlStateNormal];
//        [self.room pauseLiveStreaming];
    } else {
        _isStopped = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已继续" duration:1.0];
        });
        [self.room.livePlayer resume];
        [_pauseButton setImage:[UIImage imageNamed:@"lock"] forState:UIControlStateNormal];
//        [self.room resumeLiveStreaming];
    }
}

- (void)mutedButtonAction:(UIButton*)sender {
    if (_isMuted == NO) {
        _isMuted = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已静音" duration:1.0];
        });
        [self.room.livePlayer toggleMuted];
    } else {
        _isMuted = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已取消静音" duration:1.0];
        });
        [self.room.livePlayer toggleMuted];
    }
}

-(void)sliderValueChanged:(UISlider *)slider{
    self.room.livePlayer.playerVolume = slider.value;
}

- (void)scaleModeButtonAction:(UIButton*)sender {
    if ([[sender titleForState:UIControlStateNormal] isEqualToString:@"ScaleFill"]) {
        [sender setTitle:@"ScaleFit" forState:UIControlStateNormal];
        [self.room.livePlayer setContentMode:(AIRBLiveViewContentModeAspectFill)];
    } else if ([[sender titleForState:UIControlStateNormal] isEqualToString:@"ScaleFit"]) {
        [sender setTitle:@"Fill" forState:UIControlStateNormal];
        [self.room.livePlayer setContentMode:(AIRBLiveViewContentModeAspectFit)];
    } else {
        [sender setTitle:@"ScaleFill" forState:UIControlStateNormal];
        [self.room.livePlayer setContentMode:(AIRBLiveViewContentModeFill)];
    }
}

- (void)shareButtonAction:(UIButton*)sender {
    [[AIRBDToast shareInstance] makeToast:self.roomID duration:1.0];
}

- (void)exitButtonAction:(UIButton*)sender{
    //退出
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"退出房间……"] duration:1.0];
    });
    [self.room leaveRoom];
    if(_exitActionDelegate){
        [_exitActionDelegate popViewControllerAnimated:YES];
    }
}

- (void)setButtonAction:(UIButton*)sender{
    if(_isSetting == YES){
        _isSetting = NO;
        [_volumeSlider setHidden:YES];
        [_scaleModeButton setHidden:YES];
        [_volumeName setHidden:YES];
    }else{
        _isSetting = YES;
        [_volumeSlider setHidden:NO];
        [_scaleModeButton setHidden:NO];
        [_volumeName setHidden:NO];
    }
}


- (void)moreButtonAction:(UIButton*)sender{
    if(_moreButtonShowed == YES){
        _moreButtonShowed = NO;
    }else{
        _moreButtonShowed = YES;

    }
}

- (void)addButtonAction:(UIButton*)sender{
    //添加关注
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"成功添加关注/取消关注"] duration:1.0];
    });;
}

- (void)goodsButtonAction:(UIButton*)sender{
    //商品列表
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"展示商品列表……"] duration:1.0];
    });;
}

- (void)setUpUI{
    [self addSubview:self.sendField];
    [self exitButton];
    [self likeButton];
    [self userProfileImageView];
    
    [self pauseButton];
    [self mutedButton];
    [self goodsButton];
    [self moreButton];
    
    [self shareButton];
    [self bringSubviewToFront:_shareButton];
    [self addButton];
    [self likeButton];
    [self exitButton];
    [self commentView];
    [self roomInfo];
    [self roomNotice];
    if(_infoDictionary){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.roomTitle.text = [self->_infoDictionary valueForKey:@"title"];
            self.roomNotice.text = [self->_infoDictionary valueForKey:@"notice"];
            self.roomInfo.text = [NSString stringWithFormat:@"人数:%@ uv:%@",[self->_infoDictionary valueForKey:@"onlineCount"],[self->_infoDictionary valueForKey:@"uv"]];
        });
    }
    [self scaleModeButton];
    [self volumeSlider];
    [self setButton];
    
    
    [_volumeSlider setHidden:YES];
    [_volumeName setHidden:YES];
    [_scaleModeButton setHidden:YES];
    
}

-(void)updateRoomInfo{
    if(_infoDictionary){
        dispatch_async(dispatch_get_main_queue(), ^{
            self.roomTitle.text = [self->_infoDictionary valueForKey:@"title"];
            self.roomNotice.text = [self->_infoDictionary valueForKey:@"notice"];
            NSInteger count = [[NSString stringWithFormat:@"%@",[self->_infoDictionary valueForKey:@"onlineCount"]]integerValue];
            self.roomInfo.text = [NSString stringWithFormat:@"人数:%ld",(long)count];
            
        });
    }
}

-(void)updateRoomNotice:(NSString*)notice{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomNotice.text = notice;
        [self->_commentView performSelector:@selector(insertNewComment:) withObject:[NSString stringWithFormat:@"公告（更新）：%@",self->_roomNotice.text] afterDelay:1.2];
    });
}

-(void)updateRoomTitle:(NSString*)title{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomTitle.text = title;
    });
}
@end
