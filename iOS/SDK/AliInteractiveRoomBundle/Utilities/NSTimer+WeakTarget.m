//
//  NSTimer+WeakTarget.m
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2022/5/12.
//  Copyright © 2022 AliYun. All rights reserved.
//

#import "NSTimer+WeakTarget.h"

@interface AIRBTemporaryTimerTarget : NSObject

@property(nonatomic, weak) id actionTarget;
@property(nonatomic, assign) SEL actionSelector;

- (instancetype)initWithActionTarget:(id)target actionSelector:(SEL)selector;

@end

@implementation AIRBTemporaryTimerTarget

- (instancetype)initWithActionTarget:(id)target actionSelector:(SEL)selector{
    self = [super init];
    if (self) {
        _actionTarget = target;
        _actionSelector = selector;
    }
    return self;
}

- (void)timerAction:(NSTimer*)timer{
    IMP imp = [self.actionTarget methodForSelector:self.actionSelector];
    void (*func)(id, SEL) = (void*)imp;
    func(self.actionTarget, self.actionSelector);
}

@end

@implementation NSTimer (WeakTarget)

+ (NSTimer *)weakTargetScheduledTimerWithTimeInterval:(NSTimeInterval)ti target:(id)aTarget selector:(SEL)aSelector userInfo:(nullable id)userInfo repeats:(BOOL)yesOrNo{
    __block NSTimer* timer;
    void(^onCreate)(void) = ^(void){
        AIRBTemporaryTimerTarget* timerTarget = [[AIRBTemporaryTimerTarget alloc] initWithActionTarget:aTarget actionSelector:aSelector];
        timer = [NSTimer scheduledTimerWithTimeInterval:ti target:timerTarget selector:@selector(timerAction:) userInfo:userInfo repeats:yesOrNo];
        timer.fireDate = [NSDate distantFuture];
    };
    
    // 在主线程
    if ([NSThread isMainThread]) {
        onCreate();
    } else {
        dispatch_sync(dispatch_get_main_queue(), ^{
            onCreate();
        });
    }
    
    return timer;
}

@end
