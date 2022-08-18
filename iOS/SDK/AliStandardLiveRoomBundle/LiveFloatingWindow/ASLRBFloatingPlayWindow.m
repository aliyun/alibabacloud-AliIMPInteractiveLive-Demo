//
//  ASLRBFloatingPlayWindow.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/9.
//

#import "ASLRBFloatingPlayWindow.h"

#import <Masonry/Masonry.h>

#import "ASLRBFloatingPlayWindowProtocol.h"
#import "../CommonTools/ASLRBResourceManager.h"
#import "../CommonTools/UIColor+ColorWithHexString.h"

#define screenW  [UIScreen mainScreen].bounds.size.width
#define screenH  [UIScreen mainScreen].bounds.size.height

@interface ASLRBFloatingPlayWindow() <ASLRBFloatingPlayWindowProtocol>
@property (assign, nonatomic) CGPoint touchStartPoint;
@property (assign, nonatomic) CGFloat touchStartX;
@property (assign, nonatomic) CGFloat touchStartY;
@property (assign, nonatomic) BOOL frameSetAlready;
@end

@implementation ASLRBFloatingPlayWindow

@synthesize enableTappedToBack = _enableTappedToBack;
@synthesize borderWidth = _borderWidth;
@synthesize initialFrame = _initialFrame;
@synthesize disappearAfterResignActive = _disappearAfterResignActive;

- (instancetype) initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        
        _enableTappedToBack = YES;
        _borderWidth = 2;
        _disappearAfterResignActive = YES;
        
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        self.backgroundColor = [UIColor colorWithHexString:@"0xFB622B" alpha:1.0];
        
        UIButton* exitButton = [[UIButton alloc] init];
        [self addSubview:exitButton];
        [exitButton setImage:[UIImage imageNamed:@"icon-exit" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil] forState:UIControlStateNormal];
        [exitButton addTarget:self action:@selector(exitButtonAction) forControlEvents:UIControlEventTouchUpInside];
        [exitButton mas_makeConstraints:^(MASConstraintMaker * _Nonnull make) {
            make.top.equalTo(self.mas_top).with.offset(5);
            make.right.equalTo(self.mas_right).with.offset(-5);
            make.size.mas_equalTo(CGSizeMake(25, 25));
        }];
    }
    return self;
}

- (void) enterFloatingMode:(BOOL)enter {
    
    if (self.playerView) {
        [self.playerView removeFromSuperview];
        if (enter) {
            _onFloatingMode = YES;
            UIView* rootView = [[UIApplication sharedApplication] keyWindow];
            [rootView addSubview:self];
            
            if (!_frameSetAlready) {
                if (CGRectIsEmpty(self.initialFrame)) {
                    CGFloat targetFloatWindowWidth = [UIScreen mainScreen].bounds.size.width / 3;
                    CGFloat targetFloatWindowHeight = (targetFloatWindowWidth / 9) * 16;
                    self.frame = CGRectMake(targetFloatWindowWidth * 2, [UIScreen mainScreen].bounds.size.height / 2 - targetFloatWindowHeight / 2, targetFloatWindowWidth, targetFloatWindowHeight);
                } else {
                    self.frame = self.initialFrame;
                }
                _frameSetAlready = YES;
            }
            
            [self addSubview:self.playerView];
            self.playerView.layer.cornerRadius = self.layer.cornerRadius;
            self.playerView.layer.masksToBounds = YES;
            self.playerView.userInteractionEnabled = NO;
            [self sendSubviewToBack:self.playerView];
            
            [self.playerView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.top.equalTo(self.mas_top).with.offset(_borderWidth);
                make.left.equalTo(self.mas_left).with.offset(_borderWidth);
                make.bottom.equalTo(self.mas_bottom).with.offset(-1 * _borderWidth);
                make.right.equalTo(self.mas_right).with.offset(-1 * _borderWidth);
            }];
        } else {
            _onFloatingMode = NO;
            [self removeFromSuperview];
            self.playerView.layer.cornerRadius = 0;
            [self.parentViewControllerView addSubview:self.playerView];
            [self.parentViewControllerView sendSubviewToBack:self.playerView];
            [self.playerView mas_remakeConstraints:^(MASConstraintMaker * _Nonnull make) {
                make.center.equalTo(self.parentViewControllerView);
                make.size.equalTo(self.parentViewControllerView);
            }];
        }
    }
}

#pragma mark UIButton

- (void)exitButtonAction {
    
    [self enterFloatingMode:NO];
    
    if ([self.delegate respondsToSelector:@selector(onASLRBFloatingPlayWindowExited)]) {
        [self.delegate onASLRBFloatingPlayWindowExited];
    }
}

#pragma mark --Overwrite UIResponder

- (void) touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    [super touchesBegan:touches withEvent:event];
    
    //按钮刚按下的时候，获取此时的起始坐标
    UITouch *touch = [touches anyObject];
    _touchStartPoint = [touch locationInView:self];
    
    _touchStartX = self.frame.origin.x;
    _touchStartY = self.frame.origin.y;
}

- (void) touchesMoved:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
 
    UITouch *touch = [touches anyObject];
    CGPoint currentPosition = [touch locationInView:self];
    
    //偏移量(当前坐标 - 起始坐标 = 偏移量)
    CGFloat offsetX = currentPosition.x - _touchStartPoint.x;
    CGFloat offsetY = currentPosition.y - _touchStartPoint.y;
    
    //移动后的按钮中心坐标
    CGFloat centerX = self.center.x + offsetX;
    CGFloat centerY = self.center.y + offsetY;
    self.center = CGPointMake(centerX, centerY);
    
    //父试图的宽高
    CGFloat superViewWidth = screenW;
    CGFloat superViewHeight = screenH;
    CGFloat windowX = self.frame.origin.x;
    CGFloat windowY = self.frame.origin.y;
    CGFloat windowW = self.frame.size.width;
    CGFloat windowH = self.frame.size.height;
    
    //x轴左右极限坐标
    if (windowX > superViewWidth){
        //按钮右侧越界
        CGFloat centerX = superViewWidth - windowW/2;
        self.center = CGPointMake(centerX, centerY);
    }else if (windowX < 0){
        //按钮左侧越界
        CGFloat centerX = windowW * 0.5;
        self.center = CGPointMake(centerX, centerY);
    }
    
    //默认都是有导航条的，有导航条的，父试图高度就要被导航条占据，固高度不够
//    CGFloat defaultNaviHeight = 64;
    CGFloat judgeSuperViewHeight = superViewHeight - windowH;
    
    //y轴上下极限坐标
    if (windowY <= 0){
        //按钮顶部越界
        centerY = windowH * 0.7;
        self.center = CGPointMake(centerX, centerY);
    }
    else if (windowY > judgeSuperViewHeight){
        //按钮底部越界
        CGFloat y = superViewHeight - windowH * 0.5;
        self.center = CGPointMake(windowX, y);
    }
}

- (void) touchesEnded:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    CGFloat windowY = self.frame.origin.y;
    CGFloat windowX = self.frame.origin.x;
    CGFloat minDistance = 2;
    
    //结束move的时候，计算移动的距离是>最低要求，如果没有，就调用按钮点击事件
    BOOL isOverX = fabs(windowX - _touchStartX) > minDistance;
    BOOL isOverY = fabs(windowY - _touchStartY) > minDistance;
    
    if (isOverX || isOverY) {
        //超过移动范围就不响应点击 - 只做移动操作
        [self touchesCancelled:touches withEvent:event];
    }else{
        [super touchesEnded:touches withEvent:event];
        
        if (_enableTappedToBack) {
            [self enterFloatingMode:NO];
        }
        
        if ([self.delegate respondsToSelector:@selector(onASLRBFloatingPlayWindowTapped)]) {
            [self.delegate onASLRBFloatingPlayWindowTapped];
        }
        return;
    }
    
    //设置移动方法
    [self setMovingDirectionWithX:windowX y:windowY];
}

- (void)setMovingDirectionWithX:(CGFloat)windowX y:(CGFloat)windowY{
    if (self.center.x >= screenW / 2) {

        [UIView animateWithDuration:0.5 animations:^{
            //按钮靠右自动吸边
            CGFloat windowX = screenW - self.bounds.size.width;
            self.frame = CGRectMake(windowX, windowY, self.bounds.size.width,  self.bounds.size.height);
        }];
    }else{

        [UIView animateWithDuration:0.5 animations:^{
            //按钮靠左吸边
            CGFloat windowX = 0;
            self.frame = CGRectMake(windowX, windowY, self.bounds.size.width,  self.bounds.size.height);
        }];
    }
}

@end
