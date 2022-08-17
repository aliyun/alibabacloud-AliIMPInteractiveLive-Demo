//
//  MELVideoPlayInteractionViewController.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import "MELVideoPlayInteractionViewController.h"
#import "MELInteractiveMessageViewsHolder.h"
#import <Masonry/Masonry.h>
#import "UIColor+ColorWithHexString.h"
#import "ASLUKResourceManager.h"
#import "MELLivePlayBottomViews.h"

@interface MELVideoPlayInteractionViewController ()

@end

@implementation MELVideoPlayInteractionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self.view bringSubviewToFront:self.interactiveMessageView];
    [self.view bringSubviewToFront:self.livePlayBottomViews];
    self.view.backgroundColor = [UIColor colorWithHexString:@"#F5F5F5" alpha:1.0];
}

//- (instancetype)initWithStatus:(MELLiveRoomBottomViewStatus)status {
//    self = [super init];
//    if (self) {
//        _status = status;
//        [self.view bringSubviewToFront:self.livePlayBottomViews];
//        [self.view bringSubviewToFront:self.interactiveMessageView];
//    }
//    return self;
//}

- (MELLivePlayBottomViews*)livePlayBottomViews {
    if (!_livePlayBottomViews) {
        _livePlayBottomViews = [[MELLivePlayBottomViews alloc] initWithStatus:_status];
        [self.view addSubview:_livePlayBottomViews];
        [_livePlayBottomViews mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.bottom.equalTo(self.view);
            make.height.mas_equalTo(92);
        }];
        
        __weak typeof(self) weakSelf = self;
        _livePlayBottomViews.onShare = ^{
            weakSelf.onShareButtonClicked();
        };
        _livePlayBottomViews.onLikeSent = ^{
            weakSelf.onLikeButtonClicked();
        };
        _livePlayBottomViews.onGiftSent = ^{
            weakSelf.onGiftButtonClicked();
        };
        _livePlayBottomViews.onCommentSent = ^(NSString * _Nonnull comment) {
            weakSelf.onCommentSent(comment);
        };
    }
    return _livePlayBottomViews;
}

- (MELInteractiveMessageViewsHolder*)interactiveMessageView {
    if (!_interactiveMessageView) {
        _interactiveMessageView = [[MELInteractiveMessageViewsHolder alloc] init];
        _interactiveMessageView.backgroundColor = [UIColor colorWithHexString:@"#F5F5F5" alpha:1.0];
        _interactiveMessageView.hidden = NO;
        [self.view addSubview:_interactiveMessageView];
        [_interactiveMessageView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.bottom.equalTo(self.livePlayBottomViews.mas_top);
            make.left.equalTo(self.view).with.offset(16);
            make.right.equalTo(self.view).with.offset(-16);
            make.top.equalTo(self.view);
        }];
        [_interactiveMessageView startPresentingLiveComment];
    }
    return _interactiveMessageView;
}

- (void)setStatus:(MELLiveRoomBottomViewStatus)status {
    _status = status;
    self.livePlayBottomViews.status = status;
}

- (void)insertLiveComment:(ASLRBLiveCommentModel *)model {
    [self.interactiveMessageView insertLiveComment:model];
}

- (void)updateLikeCount:(int32_t)count {
    self.livePlayBottomViews.likeCount = count;
}

@end
