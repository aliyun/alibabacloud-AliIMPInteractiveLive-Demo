//
//  UIColor+ColorWithHexString.h
//  AliInteractiveRoomDemo
//
//  Created by 麦辣 on 2021/6/28.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIColor(ColorWithHexString)

+ (UIColor *) colorWithHexString: (NSString *)color alpha: (CGFloat)alpha;

@end

NS_ASSUME_NONNULL_END
