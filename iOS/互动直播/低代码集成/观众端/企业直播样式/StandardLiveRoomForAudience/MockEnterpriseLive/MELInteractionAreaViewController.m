//
//  MELInteractionAreaViewController.m
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/6/8.
//

#import "MELInteractionAreaViewController.h"
#import <Masonry/Masonry.h>
#import "UIColor+ColorWithHexString.h"
#import "MELVideoPlayInteractionViewController.h"
#import "MELLiveIntroductionViewController.h"

const NSString* kMELTabItemTitleInteractiveMessage = @"互动消息";
const NSString* kMELTabItemTitleLiveInfomation = @"直播信息";
const int8_t kMELChildInteractionViewControllersCount = 4;

#define kScreenWidth [UIScreen mainScreen].bounds.size.width


@interface MELInteractionAreaViewController ()<UIScrollViewDelegate>
@property (strong, nonatomic) UIScrollView* mainScrollView;
@property (strong, nonatomic) UIView *contentContainerView;/**<内容容器视图*/
@property (strong, nonatomic) UIView *btnContainerView;/**<按钮容器视图*/
@property (strong, nonatomic) UILabel *slideLabel;/**<滚动条*/
@property (strong, nonatomic) NSMutableArray<UIButton*> *btnsArray;/**<按钮数组*/
@property (strong, nonatomic) MELVideoPlayInteractionViewController* interactionVC;
@property (strong, nonatomic) MELLiveIntroductionViewController* liveIntroductionVC;
@property (strong, nonatomic) UIViewController* mockVC1;
@property (strong, nonatomic) UIViewController* mockVC2;
@end

@implementation MELInteractionAreaViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [self p_setupMainScrollView];
}

#pragma makr Properties

-(UIScrollView*)mainScrollView {
    if (!_mainScrollView) {
        _mainScrollView = [[UIScrollView alloc] init];
        [self.view addSubview:_mainScrollView];
        _mainScrollView.delegate = self;
        _mainScrollView.backgroundColor = [UIColor whiteColor];
        _mainScrollView.pagingEnabled = YES;
        _mainScrollView.showsHorizontalScrollIndicator = NO;
        _mainScrollView.showsVerticalScrollIndicator = NO;
        
        [_mainScrollView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.right.bottom.equalTo(self.view);
            make.top.equalTo(self.btnContainerView.mas_bottom);
        }];
    }
    return _mainScrollView;
}

- (UIView*)contentContainerView {
    if (!_contentContainerView) {
        _contentContainerView = [[UIView alloc] init];
        [self.mainScrollView addSubview:_contentContainerView];
        [_contentContainerView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.edges.equalTo(self.mainScrollView);
            make.height.equalTo(self.mainScrollView.mas_height); // 横向滑动，所以要明确设置高度
        }];
        
//        NSArray<UIView*> *views = @[self.mockVC1.view,self.mockVC2.view];
//        for (NSInteger i = 0; i < 2; i++) {
//            [_contentContainerView addSubview:views[i]];
//            [views[i] mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
//                make.top.bottom.equalTo(_contentContainerView);
//                if (i == 0) {
//                    make.left.equalTo(self.liveIntroduction.mas_right);
//                } else {
//                    make.left.equalTo(views[i - 1].mas_right);
//                }
//                make.width.mas_equalTo(kScreenWidth);
//
//                if (i == kMELChildInteractionViewControllersCount - 1) {
//                    make.right.equalTo(_contentContainerView.mas_right);
//                }
//            }];
//        }
    }
    return _contentContainerView;
}

- (UIView*)btnContainerView {
    if (!_btnContainerView) {
        _btnContainerView = [[UIView alloc] init];;
        _btnContainerView.backgroundColor = [UIColor whiteColor];
        [self.view addSubview:_btnContainerView];
        [_btnContainerView mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.left.right.equalTo(self.view);
            make.top.equalTo(self.view);
            make.height.mas_equalTo(44);
        }];
        
        for (int i = 0; i < kMELChildInteractionViewControllersCount; i++) {
            UIButton * btn = [UIButton buttonWithType:UIButtonTypeCustom];
            [btn setTitleColor:[UIColor colorWithRed:50/255.0 green:50/255.0 blue:51/255.0 alpha:1/1.0] forState:UIControlStateSelected];
            [btn setTitleColor:[UIColor colorWithRed:150/255.0 green:151/255.0 blue:153/255.0 alpha:1/1.0] forState:UIControlStateNormal];
            btn.titleLabel.font = [UIFont fontWithName:@"PingFangSC-Medium" size:16];
            [btn addTarget:self action:@selector(sliderAction:) forControlEvents:UIControlEventTouchUpInside];
            btn.tag = i;
            [self.btnsArray addObject:btn];
            if (i == 0) {
                btn.selected = YES;
                [btn setTitle:@"互动消息" forState:UIControlStateNormal];
            } else if (i == 1) {
                [btn setTitle:@"直播信息" forState:UIControlStateNormal];
            } else if (i == 2) {
                [btn setTitle:@"自定义项" forState:UIControlStateNormal];
            } else if (i == 3) {
                [btn setTitle:@"自定义项" forState:UIControlStateNormal];
            }
            [_btnContainerView addSubview:btn];
            [btn mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.top.equalTo(self.btnContainerView);
                make.size.mas_equalTo(CGSizeMake(kScreenWidth / kMELChildInteractionViewControllersCount, 40));
                make.left.equalTo(self.btnContainerView.mas_left).with.offset(i * (kScreenWidth / kMELChildInteractionViewControllersCount));
            }];
        }
    }
    return _btnContainerView;
}

- (UILabel*)slideLabel {
    if (!_slideLabel) {
        _slideLabel = [[UILabel alloc] init];
        _slideLabel.layer.cornerRadius = 1.5;
        _slideLabel.layer.maskedCorners = YES;
        [self.btnContainerView addSubview:_slideLabel];
        _slideLabel.backgroundColor = [UIColor colorWithHexString:@"#00BCD4" alpha:1.0];
        [_slideLabel mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(40, 3));
            make.bottom.equalTo(self.btnContainerView);
            make.centerX.equalTo(self.btnsArray[0].mas_centerX);
        }];
    }
    return _slideLabel;
}

-(MELVideoPlayInteractionViewController*)interactionVC {
    if (!_interactionVC) {
        _interactionVC = [[MELVideoPlayInteractionViewController alloc] init];
        [self.contentContainerView addSubview:_interactionVC.view];
        _interactionVC.status = _status;
        [_interactionVC.view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.bottom.equalTo(self.contentContainerView);
            make.left.equalTo(self.contentContainerView);
            make.width.mas_equalTo(kScreenWidth);
        }];
        
        __weak typeof(self) weakSelf = self;
        _interactionVC.onCommentSent = ^(NSString * _Nonnull comment) {
            weakSelf.onCommentSent(comment);
        };
        _interactionVC.onLikeButtonClicked = ^{
            weakSelf.onLikeButtonClicked();
        };
        _interactionVC.onShareButtonClicked = ^{
            weakSelf.onShareButtonClicked();
        };
        _interactionVC.onGiftButtonClicked = ^{
            weakSelf.onGiftButtonClicked();
        };
    }
    return _interactionVC;
}

- (MELLiveIntroductionViewController*)liveIntroductionVC {
    if (!_liveIntroductionVC) {
        _liveIntroductionVC = [[MELLiveIntroductionViewController alloc] init];
        [self.contentContainerView addSubview:_liveIntroductionVC.view];
        _liveIntroductionVC.status = _status;
        [_liveIntroductionVC.view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.bottom.equalTo(self.contentContainerView);
            make.left.equalTo(self.interactionVC.view.mas_right);
            make.width.mas_equalTo(kScreenWidth);
        }];
        
        __weak typeof(self) weakSelf = self;
        _liveIntroductionVC.onSubscribe = ^(void (^ _Nonnull didSubscribed)(BOOL)) {
            weakSelf.onSubscribe(didSubscribed);
        };
        _liveIntroductionVC.onShareButtonClicked = ^{
            weakSelf.onShareButtonClicked();
        };
    }
    return _liveIntroductionVC;
}

- (UIViewController*)mockVC1 {
    if (!_mockVC1) {
        _mockVC1 = [[UIViewController alloc] init];
        [self.contentContainerView addSubview:_mockVC1.view];
        [_mockVC1.view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.bottom.equalTo(self.contentContainerView);
            make.left.equalTo(self.liveIntroductionVC.view.mas_right);
            make.width.mas_equalTo(kScreenWidth);
        }];
    }
    return _mockVC1;
}

- (UIViewController*)mockVC2 {
    if (!_mockVC2) {
        _mockVC2 = [[UIViewController alloc] init];
        [self.contentContainerView addSubview:_mockVC2.view];
        [_mockVC2.view mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.bottom.equalTo(self.contentContainerView);
            make.left.equalTo(self.liveIntroductionVC.view.mas_right);
            make.right.equalTo(self.contentContainerView.mas_right);
            make.width.mas_equalTo(kScreenWidth);
        }];
    }
    return _mockVC2;
}

-(NSMutableArray<UIButton*> *)btnsArray {
   if (!_btnsArray) {
       _btnsArray = [NSMutableArray array];
   }
   return _btnsArray;
}

- (void)setAnchorNick:(NSString *)anchorNick {
    self.liveIntroductionVC.anchorNick = anchorNick;
}

- (void)setAnchorIntroduction:(NSString *)anchorIntroduction {
    self.liveIntroductionVC.anchorIntroduction = anchorIntroduction;
}

- (void)setAnchorAvartarImageURL:(NSURL *)anchorAvartarImageURL {
    self.liveIntroductionVC.anchorAvartarImageURL = anchorAvartarImageURL;
}

- (void)setLiveIntroduction:(NSString *)liveIntroduction {
    self.liveIntroductionVC.liveIntroduction = liveIntroduction;
}

- (void)setStatus:(MELLiveRoomBottomViewStatus)status {
    self.liveIntroductionVC.status = status;
    self.interactionVC.status = status;
}

#pragma mark UIButton Selectors
-(void)sliderAction:(UIButton *)sender{
    [self p_sliderAnimationWithTag:sender.tag];
    [UIView animateWithDuration:0.3 animations:^{
        _mainScrollView.contentOffset = CGPointMake(kScreenWidth * (sender.tag), 0);
    } completion:^(BOOL finished) {
        
    }];
}

#pragma mark Public Methods
- (void)insertLiveComment:(ASLRBLiveCommentModel *)model {
    [self.interactionVC insertLiveComment:model];
}

- (void)updateLikeCount:(int32_t)count {
    [self.interactionVC updateLikeCount:count];
}

#pragma mark Private Methods
- (void)p_setupMainScrollView {
    [self.view bringSubviewToFront:self.btnContainerView];
    [self.btnContainerView bringSubviewToFront:self.slideLabel];
    [self.contentContainerView bringSubviewToFront:self.mockVC1.view];
    [self.contentContainerView bringSubviewToFront:self.mockVC2.view];
    [self.contentContainerView bringSubviewToFront:self.interactionVC.view];
    [self.contentContainerView bringSubviewToFront:self.liveIntroductionVC.view];
    [self.mainScrollView bringSubviewToFront:self.contentContainerView];
}

-(void)p_sliderAnimationWithTag:(NSInteger)tag{
    [self.btnsArray enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
        btn.selected = NO;
    }];
    //获取被选中的按钮
    UIButton *selectedBtn = self.btnsArray[tag];
    selectedBtn.selected = YES;
    
    //动画
    [UIView animateWithDuration:0.3 animations:^{
        [self.slideLabel mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.size.mas_equalTo(CGSizeMake(40, 3));
            make.bottom.equalTo(self.btnContainerView);
            make.centerX.equalTo(selectedBtn.mas_centerX);
        }];
    } completion:^(BOOL finished) {
//        [self.btnsArray enumerateObjectsUsingBlock:^(UIButton *btn, NSUInteger idx, BOOL * _Nonnull stop) {
//            btn.titleLabel.font = [UIFont boldSystemFontOfSize:16];
//        }];
//        selectedBtn.titleLabel.font = [UIFont boldSystemFontOfSize:19];
    }];
}

#pragma mark UIScrollViewDelegate
-(void)scrollViewDidScroll:(UIScrollView *)scrollView{
    double index_ = scrollView.contentOffset.x / kScreenWidth;
    [self p_sliderAnimationWithTag:(int)(index_ + 0.5)];
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
