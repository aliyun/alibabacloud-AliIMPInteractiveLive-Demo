//
//  AIRBDAudienceViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import "AIRBDAudienceViewController.h"
#import "AIRBDRoomInfoModel.h"
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import <Masonry/Masonry.h>
#import "Utilities/Utility.h"
#import "AIRBDToast.h"
#import "AIRBDCommentView.h"
#import "AIRBDItemsView.h"
#import "AIRBDBeautySetsView.h"
#import "AIRBDMorePanelView.h"
#import "AIRBDItemsView.h"
#import "UIColor+HexColor.h"
#import "AIRBDetailsButton.h"
@interface AIRBDAudienceViewController ()<UITextFieldDelegate,AIRBRoomChannelDelegate,UIGestureRecognizerDelegate, AIRBLivePlayerDelegate,AIRBDMorePanelDelegate,AIRBDItemsViewDelegate>

@property (strong, nonatomic) UIImageView* backgroundView;

@property (strong, nonatomic) UIView* roomInfoHolder;
@property (strong, nonatomic) UIImageView* userImg;
@property (strong, nonatomic) UILabel* roomtitleLabel;
@property (strong, nonatomic) UILabel* roomOnlineCountLabel;
@property (strong, nonatomic) UILabel* roomLikeCountlabel;
@property (strong, nonatomic) UIButton* membersButton;
@property (strong, nonatomic) AIRBDetailsButton* noticeButton;
@property (strong, nonatomic) UIButton* exitButton;

@property (strong, nonatomic) UITextField* sendField;
@property (strong, nonatomic) UIButton* shareButton;
@property (strong, nonatomic) UIButton* likeButton;
@property (strong, nonatomic) UIButton* beautyButton;
@property (strong, nonatomic) UIButton* moreButton;
@property (strong, nonatomic) AIRBDMorePanelView* moreButtonsPanel;

@property (assign, nonatomic) BOOL roomEntered;
@property (assign, nonatomic) BOOL livePusherStarted;
@property (assign, nonatomic) BOOL isEditingRoom;
@property (assign, nonatomic) BOOL morePanelShowed;
@property (assign, nonatomic) BOOL beautySetsShowed;
@property (assign, nonatomic) BOOL membersViewShowed;
@property (assign, nonatomic) BOOL isMuted;
@property (assign, nonatomic) BOOL isStopped;
@property (assign, nonatomic) BOOL beautyOn;

@property (strong, nonatomic) AIRBDCommentView* commentView;
@property (strong, nonatomic) AIRBDItemsView* membersView;

@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;

@end

@implementation AIRBDAudienceViewController

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

- (UIImageView *)backgroundView{
    if(!_backgroundView){
        UIImageView* imageView = [[UIImageView alloc]init];
        imageView.contentMode = UIViewContentModeScaleAspectFit;
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:imageView];
        [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            make.centerY.equalTo(weakSelf.view);
            make.width.mas_equalTo(273);
            make.height.mas_equalTo(144);
        }];
        [imageView setImage:[UIImage imageNamed:@"img-live_unstart"]];
        _backgroundView = imageView;
        
        UILabel* label = [[UILabel alloc]init];
        [self.backgroundView addSubview:label];
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            make.top.equalTo(weakSelf.backgroundView.mas_bottom);
            make.width.mas_equalTo(273);
            make.height.mas_equalTo(47);
        }];
        label.text = @"主播正在赶来的路上,请稍等～";
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:20];
        label.textColor = [UIColor whiteColor];
        
        
    }
    return _backgroundView;
}

- (UIView *)roomInfoHolder{
    if(!_roomInfoHolder){
        UIView* view = [[UIView alloc]init];
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(23);
            } else {
                make.top.equalTo(self.view).with.offset(23);
            }
            make.left.equalTo(self.view).with.offset(19);
            make.width.mas_equalTo(173);
            make.height.mas_equalTo(43);
        }];
        view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 21.5;
        [view addSubview:self.userImg];
        [view addSubview:self.roomtitleLabel];
        [view addSubview:self.roomOnlineCountLabel];
        [view addSubview:self.roomLikeCountlabel];
        _roomInfoHolder = view;
    }
    return _roomInfoHolder;
}

- (UIImageView *)userImg{
    if (!_userImg) {
        UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(2, 3, 36.5, 36.5)];
        [imageView setImage:[UIImage imageNamed:self.roomModel.userImg]];
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 18.25;
        _userImg = imageView;
    }
    return _userImg;
}

- (UILabel *)roomtitleLabel{
    if(!_roomtitleLabel){
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(48, 6, 117, 17)];
        label.text = self.roomModel.title;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:14];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _roomtitleLabel = label;
    }
    return _roomtitleLabel;
}

- (UILabel *)roomOnlineCountLabel{
    if(!_roomOnlineCountLabel){
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(47, 24, 43, 14)];
        label.text = [NSString stringWithFormat:@"%d观看",self.roomModel.onlineCount];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _roomOnlineCountLabel = label;
    }
    return _roomOnlineCountLabel;
}

- (UILabel *)roomLikeCountlabel{
    if(!_roomLikeCountlabel){
        UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(96, 24, 43, 14)];
        label.text = [NSString stringWithFormat:@"%d点赞",self.roomModel.likeCount];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _roomLikeCountlabel = label;
    }
    return _roomLikeCountlabel;
}

- (AIRBDetailsButton *)noticeButton{
    if (!_noticeButton) {
        AIRBDetailsButton* button = [[AIRBDetailsButton alloc]initWithFrame:CGRectMake(0, 0, 60, 21) image:[UIImage imageNamed:@"icon-notice"] title:@"公告"];
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(73);
            } else {
                make.top.equalTo(self.view).with.offset(73);
            }
            make.left.equalTo(self.view).with.offset(19);
            make.width.mas_equalTo(60);
            make.height.mas_equalTo(21);
        }];
        _noticeButton = button;
    }
    return _noticeButton;
}

- (UIButton*) exitButton{
    if (!_exitButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop).with.offset(20);
            } else {
                make.top.equalTo(weakSelf.view).with.offset(20);
                
            }
            make.right.equalTo(weakSelf.view.mas_right).with.offset(-15);
            make.height.mas_equalTo(41);
            make.width.mas_equalTo(41);
        }];
        [button setBackgroundImage:[UIImage imageNamed:@"icon-exit"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _exitButton = button;
    }
    return _exitButton;
}

- (UITextField*) sendField {
    if (!_sendField) {
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
            make.width.mas_equalTo(244);
            make.height.mas_equalTo(40);
        }];
        textField.layer.masksToBounds = YES;
        textField.layer.cornerRadius = 20;
        textField.textColor = [UIColor blackColor];
        textField.alpha = 0.8;
        NSAttributedString *attrString = [[NSAttributedString alloc] initWithString:@"和主播说点什么……"
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
        _sendField = textField;
    }
    
    return _sendField;
}

- (UIButton*) shareButton {
    if (!_shareButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.sendField.mas_right).with.offset(12);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-29);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-29);
            }
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        [button setImage:[UIImage imageNamed:@"icon-share"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(shareButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _shareButton = button;
    }
    return _shareButton;
}

- (UIButton*) likeButton {
    if (!_likeButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.shareButton.mas_right).with.offset(10);
            make.centerY.equalTo(weakSelf.shareButton);
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        [button setImage:[UIImage imageNamed:@"icon-like"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(likeButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _likeButton = button;
    }
    return _likeButton;
}

- (AIRBDCommentView*) commentView{
    if(!_commentView){
        _commentView = [[AIRBDCommentView alloc]initWithCommentStyle:BulletStyleNew];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:_commentView];
        [_commentView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.sendField.mas_top).with.offset(-10);
            make.left.equalTo(weakSelf.sendField.mas_left);
            make.height.mas_equalTo(230);
            make.width.mas_equalTo(260);
        }];
        [_commentView insertNewComment:@"系统提示: 欢迎大家来到直播间!直播间内严禁出现违法违规、低俗色情、吸烟酗酒等内容,若有违规行为请及时举报。"];
    }
    return _commentView;
}


- (void)setUpUI{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.view sendSubviewToBack:self.backgroundView];
        [self.view bringSubviewToFront:self.roomInfoHolder];
        [self.view bringSubviewToFront:self.exitButton];
        [self.view bringSubviewToFront:self.noticeButton];
        [self.view bringSubviewToFront:self.sendField];
        [self.view bringSubviewToFront:self.shareButton];
        [self.view bringSubviewToFront:self.likeButton];
        [self.view bringSubviewToFront:self.commentView];
    });
}


#pragma mark -- UI Action

-(void)exitButtonAction:(UIButton*)sender{
//    [self.room stopLiveStreaming];
    [self leaveRoom];
    [self.navigationController popViewControllerAnimated:YES];
    
}

- (void)shareButtonAction:(UIButton*)sender {
    UIPasteboard *pboard = [UIPasteboard generalPasteboard];
    pboard.string = self.roomModel.roomID;
    [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"%@ 已拷贝", self.roomModel.roomID] duration:1.0];
}

- (void)likeButtonAction:(UIButton*)sender {
    [self.room.chat sendLike];
//    [self.room.chat sendLikeWithCount:1 onSuccess:^{
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [[AIRBDToast shareInstance] makeToast:@"点赞成功" duration:1.0];
//        });
//    } onFailure:^(NSString * _Nonnull errorMessage) {
//        dispatch_async(dispatch_get_main_queue(), ^{
//            [[AIRBDToast shareInstance] makeToast:@"点赞失败" duration:1.0];
//        });
//    }];
}

#pragma mark --LifeCircle

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillShow:) name:UIKeyboardWillShowNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(keyBoardWillHide:) name:UIKeyboardWillHideNotification object:nil];
    }
    return self;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.navigationController.navigationBar.translucent = YES;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    [self.navigationController setNavigationBarHidden:YES];
    if(!_roomModel){
        self.roomModel = [[AIRBDRoomInfoModel alloc]init];
    }
    if ([[UIDevice currentDevice].systemVersion floatValue]>=7.0) {//侧滑退出手势
        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
            self.navigationController.interactivePopGestureRecognizer.delegate = self;
        }
    }
}

- (void)viewWillAppear:(BOOL)animated{
    [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
    [self setUpUI];
}

- (void)viewWillDisappear:(BOOL)animated{
    if(self->_backgroundView){
        dispatch_async(dispatch_get_main_queue(), ^{
            [self->_backgroundView removeFromSuperview];
            self->_backgroundView = nil;
        });
    }
    [self leaveRoom];
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer{
    return YES;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [self leaveRoom];
}

- (void) enterRoom{
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.roomModel.roomID];
    self.room.delegate = self;
    [self.room enterRoomWithUserNick:@"nick"];
}

- (void) leaveRoom{
    if(self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
        self.room = nil;
    }
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].idleTimerDisabled = NO;
    });
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
            dispatch_async(dispatch_get_main_queue(), ^{
                [UIApplication sharedApplication].idleTimerDisabled = YES;
            });
            
        }
            break;
        case AIRBRoomChannelEventRoomInfoGotten:{
            self.roomModel.title = [info valueForKey:@"title"];
            self.roomModel.notice = [info valueForKey:@"notice"];
            self.roomModel.onlineCount = [[info valueForKey:@"onlineCount"] intValue];
            [self.room.chat getCurrentChatInfoOnSuccess:^(int32_t totalComment, int32_t totalLike) {
                self.roomModel.likeCount = totalLike;
                [self updateRoomInfo];
            } onFailure:^(NSString * _Nonnull errMessage) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"failed to get chat detail:(%@)", errMessage] duration:3.0];
                });
            }];
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
            switch (type) {
                case AIRBRoomChannelMessageTypeRoomMembersInOut:{
                    messageType = @"RoomMembersInOut";
                    BOOL enter = [[dataDic valueForKey:@"enter"] boolValue];
                    NSString* userID = [dataDic valueForKey:@"userId"];
                    if (userID.length > 0) {
                        int count = [[NSString stringWithFormat:@"%@",[dataDic valueForKey:@"onlineCount"]]intValue];
                        NSString* nick = ([[dataDic valueForKey:@"userId"] isEqualToString:self.roomModel.ownerID])?@"主播":[dataDic valueForKey:@"nick"];
                        comment = [NSString stringWithFormat:@"%@ %@了直播间",nick,enter?@"进入":@"离开"];
                        self.roomModel.onlineCount = count;
                        [self updateRoomInfo];
                        [_commentView insertNewComment:comment];
                    }
                }
                    break;
                case AIRBRoomChannelMessageTypeRoomTitleUpdated:
                    messageType = @"RoomTitleUpdated";
                    self.roomModel.title = [info valueForKey:@"data"];
                    [self updateRoomInfo];
                    break;
                case AIRBRoomChannelMessageTypeRoomNoticeUpdated:
                    messageType = @"RoomNoticeUpdated";
                    self.roomModel.notice = [info valueForKey:@"data"];
                    [self updateRoomInfo];
                    break;
                case AIRBRoomChannelMessageTypeLiveCreatedByOther:
                    messageType = @"LiveCreatedByOther";
                    break;
                case AIRBRoomChannelMessageTypeLiveStartedByOther:
                    messageType = @"LiveStartedByOther";
                    [self.room.livePlayer start];
//                    [self updatePlayerView];
                    [self.commentView insertNewComment:@"系统提示: 直播已开始"];
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:{
                    messageType = @"LiveStoppedByOther";
                    dispatch_async(dispatch_get_main_queue(), ^{
//                        [[AIRBDToast shareInstance] makeToast:@"直播已结束" duration:3.0];
                        [self.room.livePlayer stop];
                        [self.room.livePlayer.playerView removeFromSuperview];
                        [self.view setBackgroundColor:[UIColor colorWithHexString:@"#333333" alpha:1.0]];
                        [self.view sendSubviewToBack:self.backgroundView];
                    });
                    [self.commentView insertNewComment:@"系统提示: 直播已结束"];
                }
                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived:
                    messageType = @"ChatLikeReceived";
                    self.roomModel.likeCount = [[dataDic valueForKey:@"likeCount"] intValue];
                    [self updateRoomInfo];
                    break;
                case AIRBRoomChannelMessageTypeChatCommentReceived:
                    messageType = @"ChatCommentReceived";
                    comment = [NSString stringWithFormat:@"%@: %@",[dataDic valueForKey:@"creatorNick"],[dataDic valueForKey:@"content"]];
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeChatOneUserCommentBannedOrNot:
                    messageType = @"OneUserWasMuted";
                    if([[dataDic valueForKey:@"mute"] boolValue] == YES){
                        comment = [NSString stringWithFormat:@" %@被管理员禁言%@秒",[dataDic valueForKey:@"muteUserNick"],[dataDic valueForKey:@"muteTime"]];
                    }else{
                        comment = [NSString stringWithFormat:@" %@被管理员取消禁言",[dataDic valueForKey:@"muteUserNick"]];
                    }
                    [_commentView insertNewComment:comment];
                    break;
                case AIRBRoomChannelMessageTypeRoomOneUserKickedOut:
                    messageType = @"OneUserWasKickedOutRoom";
                    if([[dataDic valueForKey:@"kickUser"] isEqualToString:self.roomModel.userID]){
                        [self exitButtonAction:nil];
                    }else{
                        comment = [NSString stringWithFormat:@" %@被管理员踢出直播间",[dataDic valueForKey:@"kickUserName"]];
                        [_commentView insertNewComment:comment];
                    }
//                    [self updateUsersList];
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

- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBLivePlayerErrorWithCode:(0x%lx, %@)", (long)code, msg] duration:3.0];
    });
}

- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary *)info {
    switch (event) {
        case AIRBLivePlayerEventStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.room.livePlayer setContentMode:AIRBVideoViewContentModeAspectFill];
                [self.view addSubview:self.room.livePlayer.playerView];
                [self.view sendSubviewToBack:self.room.livePlayer.playerView];
                [self.backgroundView removeFromSuperview];
                self.backgroundView = nil;
                [self.view setBackgroundColor:[UIColor clearColor]];
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

#pragma mark --UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [self.sendField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    
    if (textField.text.length > 0) {
        [self.room.chat sendComment:textField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[@"发送失败: " stringByAppendingString:errorMessage] duration:1.0];
            });;
        }];
    }
    self.sendField.text = nil;
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
     return YES;
}

#pragma mark --KVO

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
        if(self->_sendField.isEditing == YES){
            self->_sendField.transform = CGAffineTransformMakeTranslation(0, -(keyBoardHeight - 30));
            self->_sendField.layer.cornerRadius = 2;
            self->_sendField.backgroundColor = [UIColor whiteColor];
            [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
                make.left.equalTo(weakSelf.view);
                make.right.equalTo(weakSelf.view);
                make.bottom.equalTo(weakSelf.view.mas_bottom).with.offset(-33);
                make.height.mas_equalTo(30);
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
        self->_sendField.transform = CGAffineTransformIdentity;
        self->_sendField.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        self->_sendField.layer.cornerRadius = 20;
        [self->_sendField mas_remakeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-29);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-29);
            }
            make.left.equalTo(weakSelf.view.mas_left).with.offset(10);
            make.width.mas_equalTo(244);
            make.height.mas_equalTo(40);
        }];
    };

    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}

#pragma mark --UpdateUI

-(void)updateRoomInfo{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomtitleLabel.text = self.roomModel.title;
        self.roomLikeCountlabel.text = [NSString stringWithFormat:@"%d点赞",self.roomModel.likeCount];
        self.roomOnlineCountLabel.text = [NSString stringWithFormat:@"%d观看",self.roomModel.onlineCount];
        [UIView animateWithDuration:0.2 animations:^{
            self.noticeButton.text = [NSString stringWithFormat:@"直播间公告: %@",self.roomModel.notice];
        }];
    });
}

@end
