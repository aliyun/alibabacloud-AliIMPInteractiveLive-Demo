//
//  NSTimer+WeakTarget.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2022/5/12.
//  Copyright © 2022 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface NSTimer (WeakTarget)

+ (NSTimer *)weakTargetScheduledTimerWithTimeInterval:(NSTimeInterval)ti target:(id)aTarget selector:(SEL)aSelector userInfo:(nullable id)userInfo repeats:(BOOL)yesOrNo;

@end

NS_ASSUME_NONNULL_END
