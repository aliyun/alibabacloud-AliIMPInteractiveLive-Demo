//
//  AIRBDToast.h
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/17.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface DialogsLabel : UILabel
- (void)setMessageText:(NSString *)text;
@end

@interface AIRBDToast : NSObject
//创建声明单例方法
+ (instancetype)shareInstance;
- (void)makeToast:(NSString *)message duration:(CGFloat)duration;
@end
NS_ASSUME_NONNULL_END
