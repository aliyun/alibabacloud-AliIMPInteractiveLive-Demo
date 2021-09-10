//
//  AIRBDSetRoomViewController.m
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/17.
//

#import "AIRBDSetRoomViewController.h"
#import "AIRBDAnchorViewController.h"
#import "AIRBDRoomInfoModel.h"
#import <Masonry/Masonry.h>
@interface AIRBDSetRoomViewController ()<UIGestureRecognizerDelegate>

@property(nonatomic,strong) UIImageView* backgroundImage;
@property(nonatomic,strong) UIButton* exitButton;
@property(nonatomic,strong) UIView* roomSettingHolder;
@property(nonatomic,strong) UIView* roomTypeHolder;
@property(nonatomic,strong) UIImageView* userImg;
@property(nonatomic,strong) UILabel* titleLabel;
@property(nonatomic,strong) UILabel* userLabel;
@property(nonatomic,strong) UIButton* changeUserImgButton;
@property(nonatomic,strong) UIButton* editTitleButton;
@property(nonatomic,strong) UIButton* createRoomButton;
@property(strong,nonatomic) AIRBDRoomInfoModel* roomModel;

@end

@implementation AIRBDSetRoomViewController

- (instancetype)initWithUserID:(NSString *)userID config:(AIRBRoomEngineConfig *)config{
    self = [super init];
    if(self){
        if(!_roomModel){
            _roomModel = [[AIRBDRoomInfoModel alloc]init];
        }
        _roomModel.userID = userID;
        _roomModel.title = [NSString stringWithFormat:@"%@'s room",userID];
        _roomModel.hostName = userID;
        _roomModel.config = config;
    }
    return self;
}

#pragma mark -- UI控件懒加载，自上往下，自父视图到子视图，自左到右

- (UIImageView *)backgroundImage{
    if(!_backgroundImage){
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
        _backgroundImage = imageView;
    }
    return _backgroundImage;
}

- (UIButton*) exitButton{
    if (!_exitButton) {
        _exitButton = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:_exitButton];
        [_exitButton mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop).with.offset(20);
                make.left.equalTo(weakSelf.view.mas_safeAreaLayoutGuideLeft).with.offset(15);
            } else {
                make.top.equalTo(weakSelf.view).with.offset(20);
                make.left.equalTo(weakSelf.view).with.offset(15);
            }
            make.height.mas_equalTo(41);
            make.width.mas_equalTo(41);
        }];
        [_exitButton setBackgroundImage:[UIImage imageNamed:@"icon-back"] forState:UIControlStateNormal];
        [_exitButton addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _exitButton;
}

- (UIView *)roomSettingHolder{
    if (!_roomSettingHolder) {
        UIView* view = [[UIView alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop).with.offset(163);
            } else {
                make.top.equalTo(weakSelf.view.mas_top).with.offset(163);
            }
            make.height.mas_equalTo(87);
            make.width.mas_equalTo(316);
        }];
        view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 8;
        [view addSubview:self.userImg];
        [view addSubview:self.titleLabel];
        [view addSubview:self.userLabel];
        [view addSubview:self.editTitleButton];
        _roomSettingHolder = view;
    }
    return _roomSettingHolder;
}

- (UIImageView *)userImg{
    if (!_userImg) {
        UIImageView* imageView = [[UIImageView alloc] initWithFrame:CGRectMake(6, 7, 71, 71)];
        [imageView setImage:[UIImage imageNamed:self.roomModel.userImg]];
        imageView.layer.masksToBounds = YES;
        imageView.layer.cornerRadius = 6;
        _userImg = imageView;
    }
    return _userImg;
}

- (UILabel *)titleLabel{
    if(!_titleLabel){
        UILabel *label = [[UILabel alloc]initWithFrame:CGRectMake(87, 18, 150, 25)];
        label.textAlignment = NSTextAlignmentLeft;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:18];
        label.text = self.roomModel.title;
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _titleLabel = label;
    }
    return _titleLabel;
}

- (UILabel *)userLabel{
    if(!_userLabel){
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(87, 43, 56, 20);
        label.text = self.roomModel.hostName;
        label.font = [UIFont fontWithName:@"PingFangSC-Regular" size:14];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        _userLabel = label;
    }
    return _userLabel;
}

- (UIButton *)editTitleButton{
    if(!_editTitleButton){
        UIButton* button = [[UIButton alloc]initWithFrame:CGRectMake(242, 26, 12.5, 12.5)];
        [button setBackgroundColor:[UIColor colorWithWhite:0.2 alpha:0.3]];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 3;
        [button setImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(editTitleButtonAction:) forControlEvents:UIControlEventTouchUpInside];
    }
    return _editTitleButton;
}

- (UIView *)roomTypeHolder{
    if(!_roomTypeHolder){
        UIView* view = [[UIView alloc]init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            make.centerX.equalTo(weakSelf.view);
            make.top.equalTo(weakSelf.roomSettingHolder.mas_bottom).with.offset(8);
            make.height.mas_equalTo(38);
            make.width.mas_equalTo(316);
        }];
        view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 8;
        UILabel* label = [[UILabel alloc]initWithFrame:CGRectMake(11, 8, 183, 22)];
        label.text = @"房间类型：互动直播间";
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:16];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [view addSubview:label];
        _roomTypeHolder = view;
    }
    return _roomTypeHolder;
}

- (UIButton *)createRoomButton{
    if(!_createRoomButton){
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
            make.width.mas_equalTo(188);
            make.height.mas_equalTo(55);
        }];
        button.layer.masksToBounds = YES;
        button.layer.cornerRadius = 8;
        [button setBackgroundImage:[UIImage imageNamed:@"img-button_startlive"] forState:UIControlStateNormal];
        [button addTarget:self action:@selector(gotoAnchorViewController) forControlEvents:UIControlEventTouchUpInside];
        
        UILabel *label = [[UILabel alloc] init];
        label.frame = CGRectMake(40, 17, 109, 20);
        label.text = @"发起直播";
        label.textAlignment = NSTextAlignmentCenter;
        label.font = [UIFont fontWithName:@"PingFangSC-Semibold" size:22];
        label.textColor = [UIColor colorWithRed:255.0/255.0 green:255.0/255.0 blue:255.0/255.0 alpha:1.0/1.0];
        [button addSubview:label];
        _createRoomButton = button;
    }
    return _createRoomButton;
}

-(void)exitButtonAction:(UIButton*)sender{
    [self.navigationController popViewControllerAnimated:YES];
}

-(void)editTitleButtonAction:(UIButton*)sender{
    
}

- (void)viewDidLoad {
    [super viewDidLoad];
    [self.view setBackgroundColor:[UIColor whiteColor]];
    self.edgesForExtendedLayout = UIRectEdgeNone;
    self.navigationController.navigationBar.translucent = NO;
    self.automaticallyAdjustsScrollViewInsets = YES;
    self.extendedLayoutIncludesOpaqueBars = YES;
    if(!_roomModel){
        self.roomModel = [[AIRBDRoomInfoModel alloc]init];
    }
    if ([[UIDevice currentDevice].systemVersion floatValue]>=7.0) {//侧滑退出手势
        if ([self.navigationController respondsToSelector:@selector(interactivePopGestureRecognizer)]) {
            self.navigationController.interactivePopGestureRecognizer.enabled = YES;
            self.navigationController.interactivePopGestureRecognizer.delegate = self;
        }
    }
    [self setUpUI];
}

-(void)setUpUI{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self.view sendSubviewToBack:self.backgroundImage];
        [self.view bringSubviewToFront:self.exitButton];
        [self.view bringSubviewToFront:self.roomSettingHolder];
        [self.view bringSubviewToFront:self.roomTypeHolder];
        [self.view bringSubviewToFront:self.createRoomButton];
    });
}

-(void)gotoAnchorViewController{
    AIRBDAnchorViewController* anchorViewController = [[AIRBDAnchorViewController alloc]init];
    anchorViewController.roomModel = self.roomModel;
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
}

#pragma mark  - UIGestureRecognizerDelegate
- (BOOL)gestureRecognizerShouldBegin:(UIGestureRecognizer *)gestureRecognizer{
    if ([self.navigationController.viewControllers count] == 1) {
        return NO;
    }else{
        return YES;
    }
}

@end
