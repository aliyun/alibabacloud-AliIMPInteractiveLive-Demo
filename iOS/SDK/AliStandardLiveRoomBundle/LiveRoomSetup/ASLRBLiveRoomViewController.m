//
//  ASLRBLiveRoomViewController.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/7/20.
//

#import "ASLRBLiveRoomViewController.h"
#import <Masonry/Masonry.h>
#import "../CommonTools/ASLRBResourceManager.h"
#import "../LiveRoomInfoViews/ASLRBLiveRoomInfoHolderView.h"

@interface ASLRBLiveRoomViewController ()

@end

@implementation ASLRBLiveRoomViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
}

- (void)viewWillAppear:(BOOL)animated {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventViewWillAppear info:nil];
    }
}

- (void) viewDidAppear:(BOOL)animated {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventViewDidAppear info:nil];
    }
}

- (void) viewWillDisappear:(BOOL)animated {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventViewWillDisappear info:nil];
    }
}

- (void) viewDidDisappear:(BOOL)animated {
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventViewDidDisappear info:nil];
    }
}

- (void) setupOnSuccess:(void(^)(NSString* roomID))onSuccess onFailure:(void(^)(NSString* errorMessage))onFailure {
    
}

- (void)startLiveAndUpdateConfig:(ASLRBLiveInitConfig *)config{
    
}

- (UIButton*)exitButton{
    if (!_exitButton) {
        UIButton* button = [[UIButton alloc] init];
        __weak typeof(self) weakSelf = self;
        [self.view addSubview:button];
        [button mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(weakSelf.view.mas_safeAreaLayoutGuideTop).with.offset(10);
                make.right.equalTo(weakSelf.view.mas_safeAreaLayoutGuideRight).with.offset(-10);
            } else {
                make.top.equalTo(weakSelf.view).with.offset(10);
                make.right.equalTo(weakSelf.view.mas_right).with.offset(-10);
            }
            make.height.mas_equalTo(30);
            make.width.mas_equalTo(30);
        }];
        [button setBackgroundImage:[UIImage imageNamed:@"icon-exit" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [button setAdjustsImageWhenHighlighted:NO];
        [button addTarget:self action:@selector(exitButtonAction:) forControlEvents:UIControlEventTouchUpInside];
        _exitButton = button;
    }
    return _exitButton;
}

- (UIView<ASLRBLiveRoomInfoViewsHolderProtocol>*) liveInfoViewHolder {
    if(!_liveInfoViewHolder) {
        ASLRBLiveRoomInfoHolderView* view = [[ASLRBLiveRoomInfoHolderView alloc]init];
        [self.view addSubview:view];
        [view mas_makeConstraints:^(MASConstraintMaker *make) {
            if (@available(iOS 11.0, *)) {
                make.top.equalTo(self.view.mas_safeAreaLayoutGuideTop).with.offset(8);
                make.left.equalTo(self.view.mas_safeAreaLayoutGuideLeft).with.offset(10);
            } else {
                make.top.equalTo(self.view).with.offset(8);
                make.left.equalTo(self.view).with.offset(10);
            }
            make.width.mas_equalTo(173);
            make.height.mas_equalTo(43);
        }];
        view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.3];
        view.layer.masksToBounds = YES;
        view.layer.cornerRadius = 21.5;
        _liveInfoViewHolder = view;
    }
    return _liveInfoViewHolder;
}

- (void) sendCustomMessage:(NSString *)message toUsers:(NSArray<NSString *> *)userIDs onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    ;
}

- (void)banAllComments:(BOOL)ban{
    
}

- (void) sendCustomMessageToAll:(NSString *)message onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * _Nonnull))onFailure {
    ;
}

- (void) switchAudience:(NSString *)userID nick:(NSString *)nick {
    ;
}

- (void) updateLiveConfig:(ASLRBLiveInitConfig *)config
                onSuccess:(void (^)(void))onSuccess
                onFailure:(void (^)(NSString* errorMessage))onFailure {
}

- (void) refreshPlayer {
    ;
}

- (void) enterFloatingMode:(BOOL)enter {
    
}

-(void)exitButtonAction:(UIButton*)sender{
    if (_exitButton && [self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]) {
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventExitButtonDidClicked info:@{
            @"from" : @"liveroom"
        }];
    }
}

#pragma mark - ASLRBLoginManagerDelegate

- (void) onASLRBLoginManagerEventKickedOut{
    if ([self.delegate respondsToSelector:@selector(onASLRBLiveRoomEventInViewController:liveRoomEvent:info:)]){
        [self.delegate onASLRBLiveRoomEventInViewController:self liveRoomEvent:ASLRBCommonEventForcedToDisconnect info:@{}];
    }
}

@end
