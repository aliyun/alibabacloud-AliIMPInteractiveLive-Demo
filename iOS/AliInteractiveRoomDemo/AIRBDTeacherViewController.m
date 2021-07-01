//
//  AIRBDTeacherViewController.m
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/9.
//

#import "AIRBDTeacherViewController.h"
#import "AIRBDTeacherView.h"
@interface AIRBDTeacherViewController ()
@property(strong,nonatomic)AIRBDTeacherView* teacherView;
@end

@implementation AIRBDTeacherViewController

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
    [[NSNotificationCenter defaultCenter]removeObserver:self];
}

- (void)viewWillAppear:(BOOL)animated{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(rotateScreen) name:UIApplicationDidBecomeActiveNotification object:nil];
    });
    [self rotateScreen];
}

- (void) viewWillDisappear:(BOOL)animated {
    [self.teacherView stop];
}

- (void)rotateScreen{
    SEL selector = NSSelectorFromString(@"setOrientation:");
    NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[UIDevice instanceMethodSignatureForSelector:selector]];
    [invocation setSelector:selector];
    [invocation setTarget:[UIDevice currentDevice]];
    int val = UIInterfaceOrientationLandscapeRight;//旋转的方向
    [invocation setArgument:&val atIndex:2];
    [invocation invoke];
    
    UIInterfaceOrientation orientation = [[UIApplication sharedApplication] statusBarOrientation];
    if (UIInterfaceOrientationIsPortrait(orientation)) {
        self.teacherView.frame = CGRectMake(0, 0, self.view.bounds.size.height, self.view.bounds.size.width);
    } else {
        self.teacherView.frame = CGRectMake(0, 0, self.view.bounds.size.width, self.view.bounds.size.height);
    }
}

- (void)setView:(UIView*)view{
    [super setView:view];
    _teacherView = (AIRBDTeacherView*)view;
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event{
    if (@available(iOS 11.0, *)) {
        [self setNeedsUpdateOfHomeIndicatorAutoHidden];
    }
    [self.view endEditing:YES];
}

- (BOOL)prefersHomeIndicatorAutoHidden{
    return YES;
}
@end
