//
//  MELLiveIntroductionViewController.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import "MELLiveIntroductionViewController.h"
#import <Masonry/Masonry.h>
#import "UIColor+ColorWithHexString.h"
#import "MELLiveIntroductionBottomViews.h"
#import "MELLiveIntroductionContentView.h"

@interface MELLiveIntroductionViewController ()

@property (strong, nonatomic) MELLiveIntroductionBottomViews* bottomViews;
@property (strong, nonatomic) MELLiveIntroductionContentView* contentViews;

@end

@implementation MELLiveIntroductionViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
//    [self.view bringSubviewToFront:self.bottomViews];
    [self.view bringSubviewToFront:self.contentViews];
    self.view.backgroundColor = [UIColor colorWithHexString:@"#F5F5F5" alpha:1.0];
}

- (MELLiveIntroductionBottomViews*)bottomViews {
    if (!_bottomViews) {
        _bottomViews = [[MELLiveIntroductionBottomViews alloc] init];
        _bottomViews.backgroundColor = [UIColor whiteColor];
        _bottomViews.status = _status;
        [self.view addSubview:_bottomViews];
        [_bottomViews mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.bottom.equalTo(self.view);
            make.height.mas_equalTo(92);
        }];
        
        __weak typeof(self) weakSelf = self;
        _bottomViews.onShare = ^{
            weakSelf.onShareButtonClicked();
        };
        _bottomViews.onSubscribe = ^(void (^ _Nonnull didSubscribed)(BOOL)) {
            weakSelf.onSubscribe(didSubscribed);
        };
    }
    return _bottomViews;
}

- (MELLiveIntroductionContentView*)contentViews {
    if (!_contentViews) {
        _contentViews = [[MELLiveIntroductionContentView alloc] init];
        _contentViews.backgroundColor = [UIColor colorWithHexString:@"#F5F5F5" alpha:1.0];
        [self.view addSubview:_contentViews];
//        [_contentViews mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//            make.left.equalTo(self.view);
//            make.right.equalTo(self.view);
//            make.bottom.equalTo(self.view.mas_bottom);
//            make.top.equalTo(self.view);
//        }];
    }
    return _contentViews;
}

- (void)setStatus:(MELLiveRoomBottomViewStatus)status {
    _status = status;
    if (status == MELLiveRoomBottomViewStatusLiveStarted) {
        [self.bottomViews removeFromSuperview];
        [self.contentViews mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.bottom.equalTo(self.view.mas_bottom);
            make.top.equalTo(self.view);
        }];
    } else {
        [self.contentViews mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.equalTo(self.view);
            make.right.equalTo(self.view);
            make.bottom.equalTo(self.bottomViews.mas_top);
            make.top.equalTo(self.view);
        }];
    }
}

- (void)setAnchorNick:(NSString *)anchorNick {
    if (anchorNick) {
        self.contentViews.anchorNickLabel.text = anchorNick;
    }
}

- (void)setAnchorIntroduction:(NSString *)anchorIntroduction {
    if (anchorIntroduction) {
        self.contentViews.anchorIntroductionLabel.text = anchorIntroduction;
    }
}

- (void)setAnchorAvartarImageURL:(NSURL *)anchorAvartarImageURL {
    if (anchorAvartarImageURL) {
        [self.contentViews.anchorAvartarView setImage:[UIImage imageWithData:[NSData dataWithContentsOfURL:anchorAvartarImageURL]]];
    }
}

-(void)setLiveIntroduction:(NSString *)liveIntroduction {
    if (liveIntroduction) {
        self.contentViews.liveContentIntroduction.text = liveIntroduction;
    }
}
/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

@end
