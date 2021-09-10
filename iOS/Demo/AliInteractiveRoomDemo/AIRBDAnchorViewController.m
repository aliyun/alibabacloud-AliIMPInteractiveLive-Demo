//
//  AIRBDAnchorViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import "AIRBDAnchorViewController.h"
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
#import "AIRBDetailsButton.h"
#import "AIRBDEnvironments.h"

@interface AIRBDAnchorViewController ()<UITextFieldDelegate,AIRBRoomChannelDelegate,BeautySetsDelegate,UIGestureRecognizerDelegate, AIRBLivePusherDelegate,AIRBDMorePanelDelegate,AIRBDItemsViewDelegate>

@property (strong, nonatomic) UIImageView* backgroundView;

@property (strong, nonatomic) UIView* roomInfoHolder;
@property (strong, nonatomic) UILabel* roomtitleLabel;
@property (strong, nonatomic) UIImageView* userImg;
@property (strong, nonatomic) UILabel* roomOnlineCountLabel;
@property (strong, nonatomic) UILabel* roomLikeCountlabel;
@property (strong, nonatomic) UIButton* membersButton;
@property (strong, nonatomic) AIRBDetailsButton* noticeButton;

@property (strong, nonatomic) UIButton* exitButton;
@property (strong, nonatomic) UIButton* startLiveButton;
@property (strong, nonatomic) UIButton* agreeLiveProtocolPoint;
@property (strong, nonatomic) UILabel* liveProtocolLabel;

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
@property (strong, nonatomic) AIRBDBeautySetsView* beautySetsView;

@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@property (strong, nonatomic) id<AIRBRoomChannelProtocol> room;

@end

@implementation AIRBDAnchorViewController

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

- (UIImageView *)backgroundView{
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

- (UIButton *)membersButton{
    if (!_membersButton) {
        UIButton* button = [[UIButton alloc] init];
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
        button.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 10;
        button.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:12];
        [button setTitle:@" 观众" forState:UIControlStateNormal];
        [button setImage:[UIImage imageNamed:@"icon-audience"] forState:UIControlStateNormal];
        [button setContentMode:UIViewContentModeScaleAspectFit];
        [button addTarget:self action:@selector(membersButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _membersButton = button;
    }
    return _membersButton;
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
            make.left.equalTo(self.membersButton.mas_right).with.offset(10);
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

- (UIButton *)startLiveButton{
    if(!_startLiveButton){
        UIButton* button = [[UIButton alloc]init];
        [self.view addSubview:button];
        __weak typeof(self) weakSelf = self;
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            if (@available(iOS 11.0, *)) {
                make.bottom.equalTo(weakSelf.view.mas_safeAreaLayoutGuideBottom).with.offset(-219);
            } else {
                make.bottom.equalTo(weakSelf.view).with.offset(-219);
            }
            make.width.mas_equalTo(188);
            make.height.mas_equalTo(55);
        }];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 8;
        [button setBackgroundColor:[UIColor colorWithRed:252.0/255.0 green:119.0/255.0 blue:22.0/255.0 alpha:1.0/1.0]];
        [button addTarget:self action:@selector(startLiveButtonAction) forControlEvents:UIControlEventTouchUpInside];
        button.contentMode = UIViewContentModeScaleAspectFit;
        [button setBackgroundImage:[UIImage imageNamed:@"img-button_startlive"] forState:UIControlStateNormal];
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(42, 9, 108, 37);
        label.text = @"开始直播";
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:26];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [button addSubview:label];
        _startLiveButton = button;
//        [self agreeLiveProtocolPoint];
//        [self liveProtocolLabel];
    }
    return _startLiveButton;
}

- (UIButton *)agreeLiveProtocolPoint{
    if(!_agreeLiveProtocolPoint){
        UIButton* button = [[UIButton alloc]init];
        [self.view addSubview:button];
        __weak typeof(self) weakSelf = self;
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.startLiveButton.mas_left).with.offset(32);
            make.top.equalTo(weakSelf.startLiveButton.mas_bottom).with.offset(15);
            make.width.mas_equalTo(10);
            make.height.mas_equalTo(10);
        }];
        button.layer.borderColor = [[UIColor blackColor]CGColor];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 5;
        button.layer.borderWidth = 0.3;
        [button addTarget:self action:@selector(agreeLiveProtocolPointAction:) forControlEvents:UIControlEventTouchUpInside];
        _agreeLiveProtocolPoint = button;
    }
    return _agreeLiveProtocolPoint;
}

- (UILabel *)liveProtocolLabel{
    if(!_liveProtocolLabel){
        UILabel* label = [[UILabel alloc]init];
        [self.view addSubview:label];
        __weak typeof(self) weakSelf = self;
        [label mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.startLiveButton.mas_left).with.offset(47);
            make.top.equalTo(weakSelf.startLiveButton.mas_bottom).with.offset(12);
            make.width.mas_equalTo(110);
            make.height.mas_equalTo(14);
        }];
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:10];
        label.textColor = [UIColor colorWithRed:102.0/255.0 green:102.0/255.0 blue:90.0/255.0 alpha:1.0/1.0];
        NSMutableAttributedString* attributedString = [[NSMutableAttributedString alloc]initWithString:@"开播即代表同意直播协议"];
        [attributedString addAttribute:NSForegroundColorAttributeName value:[UIColor blackColor] range:[[attributedString string]rangeOfString:@"直播协议"]];
        [attributedString addAttribute:NSFontAttributeName value:[UIFont fontWithName:@"PingFangSC-Semibold" size:10] range:[[attributedString string]rangeOfString:@"直播协议"]];
        label.attributedText = attributedString;
        _liveProtocolLabel = label;
    }
    return _liveProtocolLabel;
}

- (UIView *)moreButtonsPanel{
    if(!_moreButtonsPanel){
        AIRBDMorePanelView* view = [[AIRBDMorePanelView alloc]initWithFrame:CGRectMake(0, 0, self.view.bounds.size.width, 200)];
        view.delegate = self;
        [self.view addSubview:view];
        [self.view bringSubviewToFront:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(self.view.mas_bottom);
            make.left.equalTo(self.view);
            make.width.mas_equalTo(self.view.bounds.size.width);
            make.height.mas_equalTo(200);
        }];
        _moreButtonsPanel = view;
    }
    return _moreButtonsPanel;
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
            make.width.mas_equalTo(157);
            make.height.mas_equalTo(40);
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

- (UIButton*) beautyButton {
    if (!_beautyButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.likeButton.mas_right).with.offset(10);
            make.centerY.equalTo(weakSelf.shareButton);
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        [button setImage:[UIImage imageNamed:@"icon-beauty"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(beautyButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _beautyButton = button;
    }
    return _beautyButton;
}

- (UIButton*) moreButton {
    if (!_moreButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            make.left.equalTo(weakSelf.beautyButton.mas_right).with.offset(10);
            make.centerY.equalTo(weakSelf.shareButton);
            make.width.mas_equalTo(40);
            make.height.mas_equalTo(40);
        }];
        [button setImage:[UIImage imageNamed:@"icon-more"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(moreButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _moreButton = button;
    }
    return _moreButton;
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

- (AIRBDBeautySetsView *)beautySetsView{
    if(!_beautySetsView){
        AIRBDBeautySetsView* beautySetsView = [[AIRBDBeautySetsView alloc]initWithFrame:CGRectMake(0, 0, 260, 210)];
        __weak typeof(self) weakSelf = self;
        beautySetsView.backgroundColor = [UIColor colorWithRed:1 green:1 blue:1 alpha:0.3];
        beautySetsView.layer.masksToBounds = YES;
        beautySetsView.layer.cornerRadius = 8;
        [self.view addSubview:beautySetsView];
        [beautySetsView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.bottom.equalTo(weakSelf.beautyButton.mas_top).with.offset(-260);
            make.left.equalTo(weakSelf.view).with.offset(10);
            make.height.mas_equalTo(220);
            make.width.mas_equalTo(260);
        }];
        beautySetsView.beautySetsDelegate = self;
        [beautySetsView loadSubviews];
        _beautySetsView = beautySetsView;
    }
    return _beautySetsView;
}

- (AIRBDItemsView*) membersView{
    if(!_membersView){
        _membersView = [[AIRBDItemsView alloc]init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:_membersView];
        [_membersView mas_makeConstraints:^(MASConstraintMaker *make) {
            make.top.equalTo(weakSelf.membersButton.mas_bottom).with.offset(5);
            make.left.equalTo(weakSelf.membersButton.mas_left);
            make.width.mas_equalTo(weakSelf.view.bounds.size.width/2-10);
            make.height.mas_equalTo(200);
        }];
        
    }
    _membersView.ItemsViewdelegate = self;
    return _membersView;
}

- (void)setUpUI{
    dispatch_async(dispatch_get_main_queue(), ^{
//        [self.view sendSubviewToBack:self.backgroundView];
        [self.view bringSubviewToFront:self.roomInfoHolder];
        [self.view bringSubviewToFront:self.exitButton];
        [self.view bringSubviewToFront:self.sendField];
        [self.view bringSubviewToFront:self.shareButton];
        [self.view bringSubviewToFront:self.likeButton];
        [self.view bringSubviewToFront:self.beautyButton];
        [self.view bringSubviewToFront:self.moreButton];
        [self.view bringSubviewToFront:self.membersButton];
        [self.view bringSubviewToFront:self.noticeButton];
        [self.view bringSubviewToFront:self.commentView];
        [self.view bringSubviewToFront:self.startLiveButton];
        [self.beautySetsView setHidden:YES];
        [self.membersView setHidden:YES];
        [self moreButtonsPanel];
        
        
    });
    
}

#pragma mark -- UI Action

- (void)membersButtonAction:(UIButton*)sender{
    if(_membersViewShowed == YES){
        _membersViewShowed = NO;
        _membersView.hidden = YES;
    }else{
        _membersViewShowed = YES;
        [self updateUsersList];
        _membersView.hidden = NO;
    }
}

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

- (void)startLiveButtonAction{
    dispatch_async(dispatch_get_main_queue(), ^{
//        for (UILabel* label in self.startLiveButton.subviews) {
//            dispatch_async(dispatch_get_main_queue(), ^{
//                [label removeFromSuperview];
//            });
//        }
//        UILabel *label = [[UILabel alloc] init];
//        label.frame = CGRectMake(42, 9, 108, 37);
//        label.text = @"加载中";
//        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:26];
//        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:0.7/1.0];
//        [self.startLiveButton addSubview:label];
        self.startLiveButton.alpha = 0.8;
        [self.room.livePusher startLiveStreaming];
    });
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

- (void)beautyButtonAction:(UIButton*)sender{
    self.beautySetsView.hidden = !self.beautySetsView.hidden;
}

- (void)moreButtonAction:(UIButton*)sender{
    if(!_morePanelShowed){
        [UIView animateWithDuration:0.2 animations:^{
            CGRect frame = self.moreButtonsPanel.frame;
            frame.origin.y = self.view.bounds.size.height - 200 ;
            self.moreButtonsPanel.frame = frame;
        }];
        [self.view bringSubviewToFront:self.moreButtonsPanel];
        _morePanelShowed = YES;
    }
}

- (void)pauseButtonAction:(UIButton*)sender {
    static BOOL liveStopped = NO;
    if (liveStopped == NO) {
        liveStopped = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已暂停" duration:1.0];
        });
        [self.room.livePusher pauseLiveStreaming];
    } else {
        liveStopped = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播已继续" duration:1.0];
        });
        [self.room.livePusher resumeLiveStreaming];
    }
}

- (void)editButtonAction:(UIButton*)sender {
    UIAlertController *roomTitleAlertController = [UIAlertController alertControllerWithTitle:nil
                                                                             message:@"请输入新的直播间标题"
                                                                      preferredStyle:UIAlertControllerStyleAlert];
    [roomTitleAlertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
    [roomTitleAlertController addAction:[UIAlertAction actionWithTitle:@"确定"
                                                        style:UIAlertActionStyleDefault
                                                      handler:^(UIAlertAction * _Nonnull action) {
        UITextField* textField = roomTitleAlertController.textFields.firstObject;
        if([textField.text length]>0){
            [self.room updateRoomTitle:textField.text onSuccess:^{
                
            } onFailure:^(NSString * _Nonnull errorMessage) {
                
            }];
        }
        UIAlertController *alertController = [UIAlertController alertControllerWithTitle:nil
                                                                                 message:@"请输入新的直播间公告"
                                                                          preferredStyle:UIAlertControllerStyleAlert];
        [alertController addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil]];
        [alertController addAction:[UIAlertAction actionWithTitle:@"确定"
                                                            style:UIAlertActionStyleDefault
                                                          handler:^(UIAlertAction * _Nonnull action) {
            UITextField* textField = alertController.textFields.firstObject;
            if([textField.text length]>0){
                [self.room updateRoomNotice:textField.text onSuccess:^{
                    
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    
                }];
            }
        }]];
        [alertController addTextFieldWithConfigurationHandler:^(UITextField*_Nonnull textField) {
            textField.placeholder = @"新的直播间公告";
        }];
        [self presentViewController:alertController animated:YES completion:nil];
    }]];
    [roomTitleAlertController addTextFieldWithConfigurationHandler:^(UITextField*_Nonnull textField) {
        textField.placeholder = @"新的直播间标题";
    }];
    [self presentViewController:roomTitleAlertController animated:YES completion:nil];
}

- (void)agreeLiveProtocolPointAction:(UIButton*)sender{
    static BOOL agreed = NO;
    if(agreed == NO){
        agreed = YES;
        self.agreeLiveProtocolPoint.backgroundColor = [UIColor orangeColor];
    }else{
        agreed = NO;
        self.agreeLiveProtocolPoint.backgroundColor = [UIColor clearColor];
    }
    
}

- (void)cameraButtonAction:(UIButton*)sender {
    [self.room.livePusher toggleCamera];
}

- (void)muteButtonAction:(UIButton*)sender {
    static BOOL isMuted = NO;
    if (isMuted == NO) {
        isMuted = YES;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已静音" duration:1.0];
        });
        [self.room.livePusher toggleMuted];
    } else {
        isMuted = NO;
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:@"直播间已取消静音" duration:1.0];
        });
        [self.room.livePusher toggleMuted];
    }
}

- (void)membersStepperAction:(UIStepper*)sender{
    [self updateUsersList];
}


#pragma mark --LifeCircle

- (instancetype)init {
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
    [self.view setBackgroundColor:[UIColor blackColor]];
    [self setUpUI];
}


- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    [self.view endEditing:YES];
//    self.commentView.hidden = !self.commentView.hidden;
    if(_morePanelShowed){
        [UIView animateWithDuration:0.2 animations:^{
            CGRect frame = self.moreButtonsPanel.frame;
            frame.origin.y = self.view.bounds.size.height;
            self.moreButtonsPanel.frame = frame;
        }];
    }
    _morePanelShowed = NO;
}

- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer*)gestureRecognizer{
    return YES;
}

- (void) dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
//    [self.room stopLiveStreaming];
    [self leaveRoom];
}

- (void) enterRoom{
    self.room = [[AIRBRoomEngine sharedInstance] getRoomChannelWithRoomID:self.roomModel.roomID];
    self.room.delegate = self;
    [self.room enterRoomWithUserNick:@"nick"];
    if(self.roomModel != nil){
        [self.room updateRoomTitle:self.roomModel.title onSuccess:^{

        } onFailure:^(NSString * _Nonnull errorMessage) {

        }];
    }
}

- (void) leaveRoom {
    if (self.roomEntered){
        [self.room leaveRoom];
        self.roomEntered = NO;
        self.room = nil;
    }
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].idleTimerDisabled = NO;
    });
}

- (void) createRoomWithCompletion:(void(^)(NSString* roomID))onGotRoomID {
    self.roomModel.title = [NSString stringWithFormat:@"%@的直播", self.roomModel.userID];
    self.roomModel.notice = [NSString stringWithFormat:@"%@的直播间公告", self.roomModel.userID];
    
    NSString* templateId = @"default";
    NSString* path = [NSString stringWithFormat:@"%@/api/login/createRoom", [AIRBDEnvironments shareInstance].appServerHost];
    NSString* s = [NSString stringWithFormat:@"%@?appId=%@&templateId=%@&title=%@&notice=%@&roomOwnerId=%@", path, self.roomModel.config.appID, templateId, self.roomModel.title, self.roomModel.notice, self.roomModel.userID];
    
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
        @"appId" : self.roomModel.config.appID,
        @"templateId" : templateId,
        @"title" : self.roomModel.title,
        @"notice" : self.roomModel.notice,
        @"roomOwnerId" : self.roomModel.userID
    };
    
    NSString* signedString = [Utility AIRBRequestSignWithSignSecret:[AIRBDEnvironments shareInstance].signSecret method:@"POST" path:path parameters:params headers:headers];
    NSLog(@"signedString:%@", signedString);
    
    
    s = [NSString stringWithFormat:@"%@?appId=%@&templateId=%@&title=%@&notice=%@&roomOwnerId=%@", path, self.roomModel.config.appID, templateId, [Utility encodeToPercentEscapeString:self.roomModel.title], [Utility encodeToPercentEscapeString:self.roomModel.notice], self.roomModel.userID];
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
            NSLog(@"createRoom data:%@", dic);
            if ([dic isKindOfClass:[NSDictionary class]] && dic.count > 0 && [dic valueForKey:@"result"]) {
                NSDictionary* resultDic = [dic valueForKey:@"result"];
                if ([resultDic isKindOfClass:[NSDictionary class]] && resultDic.count > 0 && [resultDic valueForKey:@"roomId"]) {
                    onGotRoomID([resultDic valueForKey:@"roomId"]);
                }
            }
        } else if (error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance]makeToast:[error description] duration:2.0];
            });
        }
    }];
    [task resume];
}

#pragma mark - ItemsViewdelegate

-(void)useItem:(NSString *)itemID{
    if([itemID length]>0){
        dispatch_async(dispatch_get_main_queue(), ^{
            UIAlertController *alert = [UIAlertController alertControllerWithTitle:@"管理该成员" message:nil preferredStyle:UIAlertControllerStyleActionSheet];
            [alert addAction:[UIAlertAction actionWithTitle:@"禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat banCommentWithUserID:itemID banTimeInSeconds:300 onSuccess:^{
                        
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"禁言用户失败:%@",errorMessage] duration:1.0];
                    });
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消禁言" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room.chat cancelBanCommentWithUserID:itemID onSuccess:^{
                    
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"取消用户禁言失败:%@",errorMessage] duration:1.0];
                    });
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"踢出直播间" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
                [self.room kickRoomUserWithUserID:itemID onSuccess:^{
                    [self updateUsersList];
                } onFailure:^(NSString * _Nonnull errorMessage) {
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"删除用户失败:%@",errorMessage] duration:1.0];
                    });
                }];
            }]];
            [alert addAction:[UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleDefault handler:nil]];
            alert.popoverPresentationController.sourceRect = [self.membersView cellForRowAtIndexPath:[self.membersView indexPathForSelectedRow]].frame;
            alert.popoverPresentationController.sourceView = self.membersView;
            [self presentViewController:alert animated:YES completion:nil];
            
        });
    }
}

#pragma mark - AIRBDBeautySetsDelegate

-(void)beautySetsValueChanged:(AIRBLivePusherFaceBeautyOptions* _Nonnull) beautyOptions{
    [self.room.livePusher updateFaceBeautyParameters:beautyOptions];
}

#pragma mark - AIRBRoomChannelProtocol

- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message{
    dispatch_async(dispatch_get_main_queue(), ^{
        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"onAIRBRoomChannelErrorWithCode:(0x%lx, %@)", (long)code, message] duration:3.0];
        [self.navigationController popViewControllerAnimated:YES];
    });
}

- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info{
    switch (event) {
        case AIRBRoomChannelEventEntered: {
            self.room.livePusher.delegate = self;
            self.roomEntered = YES;
            AIRBLivePusherOptions* options = [AIRBLivePusherOptions defaultOptions];
            options.faceBeautyOptions.beautyMode = AIRBLivePushBeautyModeProfessional;
            [self.room.livePusher startLocalPreviewWithOptions:options];
            [self.room.livePusher setContentMode:AIRBVideoViewContentModeAspectFill];
            dispatch_async(dispatch_get_main_queue(), ^{
                [UIApplication sharedApplication].idleTimerDisabled = YES;
                [self.view addSubview:self.room.livePusher.pusherView];
                [self.view sendSubviewToBack:self.room.livePusher.pusherView];
            });
        }
            break;
        case AIRBRoomChannelEventLeft:
            self.roomEntered = NO;
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
            
        case AIRBRoomChannelEventMessageReceived: {
            AIRBRoomChannelMessageType type = [[info valueForKey:@"type"] intValue];
            NSString* messageType = @"";
            NSString* comment = nil;
            NSData *turnData = [[info valueForKey:@"data"] dataUsingEncoding:NSUTF8StringEncoding];
            NSDictionary *dataDic = [NSJSONSerialization JSONObjectWithData:turnData options:NSJSONReadingMutableLeaves error:nil];
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
                    [self updateUsersList];
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
                    break;
                case AIRBRoomChannelMessageTypeLiveStoppedByOther:
                    messageType = @"LiveStoppedByOther";
                    break;
                case AIRBRoomChannelMessageTypeChatLikeReceived:
                    messageType = @"ChatLikeReceived";
                    self.roomModel.likeCount = [[dataDic valueForKey:@"likeCount"] intValue];
                    [self updateRoomInfo];
                    [_commentView insertNewComment:@"你收到了一个赞❤️"];
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
                    comment = [NSString stringWithFormat:@" %@被管理员踢出了直播间",[dataDic valueForKey:@"kickUser"]];
                    [_commentView insertNewComment:comment];
                    [self updateUsersList];
                    break;
                default:
                    dispatch_async(dispatch_get_main_queue(), ^{
                        [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"type:%@,data:%@", messageType, [info valueForKey:@"data"] ? : nil] duration:2.0];
                    });
                    break;
            };
//            [self.commentView insertNewComment:[NSString stringWithFormat:@"开发者信息:%@ ,%@",messageType, [info valueForKey:@"data"] ? : nil]];
            break;
        }
        default:
            break;
    }
}

- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info{
    switch (event) {
        case AIRBLivePusherEventPreviewStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.roomModel.beautyOptions = self.room.livePusher.options.faceBeautyOptions;
                self.beautySetsView.beautyOptions = self.roomModel.beautyOptions;
            });
            
        }
            break;
        case AIRBLivePusherEventStreamStarted: {
            dispatch_async(dispatch_get_main_queue(), ^{
                self.livePusherStarted = YES;
                self.startLiveButton.hidden = YES;
                
//                self.agreeLiveProtocolPoint.hidden = YES;
//                self.liveProtocolLabel.hidden = YES;
            });
            [self.commentView insertNewComment:@"系统提示: 直播已开始"];
        }
            break;
            
        case AIRBLivePusherEventStopped: {
//            [self.commentView insertNewComment:@"直播已停止"];
            [self.commentView insertNewComment:@"系统提示: 直播已停止"];
            break;
        }
        case AIRBLivePusherEventNetworkPoored: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"遭遇弱网" duration:1.0];
            });
        }
            break;
        case AIRBLivePusherEventNetworkRecoveried: {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"网络恢复" duration:1.0];
            });
        }
            break;
        default:
            break;
    }
}

#pragma mark --UITextFieldDelegate

- (BOOL)textFieldShouldReturn:(UITextField *)textField {
    [textField resignFirstResponder];    //主要是[receiver resignFirstResponder]在哪调用就能把receiver对应的键盘往下收
    if (textField == _sendField && textField.text.length > 0) {
        [self.room.chat sendComment:textField.text onSuccess:^{
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:@"发送成功" duration:1.0];
            });;
        } onFailure:^(NSString * _Nonnull errorMessage) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[AIRBDToast shareInstance] makeToast:[@"发送失败: " stringByAppendingString:errorMessage] duration:1.0];
            });;
        }];
        _sendField.text = nil;
    }
    
    return YES;
}

- (BOOL)textFieldShouldClear:(UITextField *)textField{
    return YES;
}

#pragma mark --UpdateUI

-(void)updateUsersList{
    [self.room getRoomUserListWithPageNum:1  pageSize:20 onSuccess:^(AIRBRoomChannelUserListResponse * _Nonnull response) {
        NSMutableArray* userList = [response.userList mutableCopy];
        AIRBRoomChannelUser* userToDelete;
        for(AIRBRoomChannelUser* user in userList){
            if([user.openID isEqualToString:self.roomModel.userID]){
                userToDelete = user;
                break;
            }
        }
        [userList removeObject:userToDelete];
        [self->_membersView updateItems:userList];
    } onFailure:^(NSString * _Nonnull errorMessage) {
        dispatch_async(dispatch_get_main_queue(), ^{
            [[AIRBDToast shareInstance] makeToast:[NSString stringWithFormat:@"获取用户信息失败:%@",errorMessage] duration:1.0];
        });
    }];
}

-(void)updateRoomInfo{
    dispatch_async(dispatch_get_main_queue(), ^{
        self.roomtitleLabel.text = self.roomModel.title;
        self.roomLikeCountlabel.text = [NSString stringWithFormat:@"%d点赞",self.roomModel.likeCount];
        self.roomOnlineCountLabel.text = [NSString stringWithFormat:@"%d观看",self.roomModel.onlineCount];
        self.noticeButton.text = [NSString stringWithFormat:@"直播间公告: %@",self.roomModel.notice];
    });
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
            make.width.mas_equalTo(157);
            make.height.mas_equalTo(40);
        }];
    };

    if (animationTime > 0) {
        [UIView animateWithDuration:animationTime animations:animation];
    } else {
        animation();
    }
}



@end
