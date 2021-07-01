//
//  AIRBDToast.m
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/17.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBDToast.h"

static int changeCount;

#define SCREEN_WIDTH [UIScreen mainScreen].bounds.size.width
#define SCREEN_HEIGHT [UIScreen mainScreen].bounds.size.height

@interface AIRBDToast() {
    DialogsLabel *dialogsLabel;
    NSTimer *countTimer;
}

@end

@implementation AIRBDToast
+ (instancetype)shareInstance {
    static AIRBDToast *singleton = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        dispatch_async(dispatch_get_main_queue(), ^{
            singleton = [[AIRBDToast alloc] init];
        });
    });
    return singleton;
}
// 初始化方法
- (instancetype)init {
    self = [super init];
    if (self) {
        dialogsLabel = [[DialogsLabel alloc] init];
        countTimer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(changeTime) userInfo:nil repeats:YES];
        countTimer.fireDate = [NSDate distantFuture];//关闭定时器
    }
    return self;
}

- (void)makeToast:(NSString *)message duration:(CGFloat)duration{
    if ([message length] == 0) {
        return;
    }
    
    [dialogsLabel setMessageText:message];
    dispatch_async(dispatch_get_main_queue(), ^{
        [[[UIApplication sharedApplication] keyWindow] addSubview:self->dialogsLabel];
    });
    dialogsLabel.alpha = 0.8;
    countTimer.fireDate = [NSDate distantPast];//开启定时器
    changeCount = duration;
}
//定时器回调方法
- (void)changeTime {
    if(changeCount-- <= 0){
        countTimer.fireDate = [NSDate distantFuture]; //关闭定时器
        [UIView animateWithDuration:0.2f animations:^{
            dialogsLabel.alpha = 0;
        } completion:^(BOOL finished) {
            [self->dialogsLabel removeFromSuperview];
        }];
    }
}
@end

@implementation DialogsLabel
- (instancetype)init {
    self = [super init];
    if (self) {
        self.layer.cornerRadius = 8;
        self.layer.masksToBounds = YES;
        self.backgroundColor = [UIColor blackColor];
        self.numberOfLines = 0;
        self.textAlignment = NSTextAlignmentCenter;
        self.textColor = [UIColor whiteColor];
        self.font = [UIFont systemFontOfSize:15];
    }
    return self;
}

- (void)setMessageText:(NSString *)text {
    [self setText:text];
 CGRect rect = [self.text boundingRectWithSize:CGSizeMake(SCREEN_WIDTH - 20, MAXFLOAT) options:NSStringDrawingUsesLineFragmentOrigin|NSStringDrawingUsesFontLeading attributes:@{NSFontAttributeName:self.font} context:nil];
    CGFloat width = rect.size.width + 20;
    CGFloat height = rect.size.height + 20;
    CGFloat x = (SCREEN_WIDTH - width) / 2;
    CGFloat y = (SCREEN_HEIGHT - height) / 2;
    self.frame = CGRectMake(x, y, width, height);
}
@end
