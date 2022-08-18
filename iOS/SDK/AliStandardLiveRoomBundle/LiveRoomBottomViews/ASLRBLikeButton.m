//
//  ASLRBLikeButton.m
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/11/16.
//

#import "ASLRBLikeButton.h"
#import "../CommonTools/ASLRBResourceManager.h"

@implementation ASLRBLikeButton

- (instancetype) init {
    self = [super init];
    if (self) {
        [self addTarget:self action:@selector(onClicked) forControlEvents:UIControlEventTouchUpInside];
    }
    return self;
}

- (void) onClicked {
    
    dispatch_async(dispatch_get_main_queue(), ^{
        self.onLikeSent();
    });
    
    //在主线程上调用该方法
    UIImageView *imageView = [[UIImageView alloc] init];
    CGRect frame = self.superview.superview.frame;
    // 初始frame，即设置了动画的起点
    imageView.frame = CGRectMake(self.frame.origin.x + self.superview.frame.origin.x, self.frame.origin.y + self.superview.frame.origin.y, 30,30);
    // 初始化imageView透明度为0
    imageView.alpha =0;
    imageView.backgroundColor = [UIColor clearColor];
    imageView.clipsToBounds = YES;
    // 用0.2秒的时间将imageView的透明度变成1.0，同时将其放大1.3倍，再缩放至1.1倍，这里参数根据需求设置
    [UIView animateWithDuration:0.2 animations:^{
        imageView.alpha =1.0;
//        imageView.frame = CGRectMake(frame.size.width -40, frame.size.height -90,30,30);
        CGAffineTransform transfrom = CGAffineTransformMakeScale(1.3,1.3);
        imageView.transform = CGAffineTransformScale(transfrom,1,1);
    }];
    [self.superview.superview addSubview:imageView];
    // 随机产生一个动画结束点的X值
    CGFloat finishX = frame.size.width - round(random() %200);
    // 动画结束点的Y值
    CGFloat finishY = 200;
    // imageView在运动过程中的缩放比例
    CGFloat scale = round(random() %2) +0.7;
    // 生成一个作为速度参数的随机数
    CGFloat speed =1/ round(random() %900) +0.6;
    // 动画执行时间
    NSTimeInterval duration =4* speed;
    // 如果得到的时间是无穷大，就重新附一个值（这里要特别注意，请看下面的特别提醒）
    if(duration == INFINITY) duration =2.412346;
    // 开始动画
    [UIView beginAnimations:nil context:(__bridge void *_Nullable)(imageView)];
    // 设置动画时间
    [UIView setAnimationDuration:duration];
    // 拼接图片名字
    imageView.image = [UIImage imageNamed:@"img-like_send" inBundle:[ASLRBResourceManager sharedInstance].resourceBundle compatibleWithTraitCollection:nil];
    // 设置imageView的结束frame
    imageView.frame =CGRectMake( finishX, finishY,30* scale,30* scale);
    // 设置渐渐消失的效果，这里的时间最好和动画时间一致
    [UIView animateWithDuration:duration animations:^{
        imageView.alpha =0;
    }];
    // 结束动画，调用onAnimationComplete:finished:context:函数
    [UIView setAnimationDidStopSelector:@selector(onAnimationComplete:finished:context:)];
    // 设置动画代理
    [UIView setAnimationDelegate:self];
    [UIView commitAnimations];
}

- (void)onAnimationComplete:(NSString*)animationID finished:(NSNumber*)finished context:(void*)context{
    UIImageView *imageView = (__bridge UIImageView*)(context);
    [imageView removeFromSuperview];
    imageView = nil;
}

@end
