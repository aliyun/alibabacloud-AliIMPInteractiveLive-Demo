//
//  AIRBDShopWindowViewController.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/10/9.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface SECLRAShopWindowViewController : UIViewController
@property (copy, nonatomic) void(^onTapped)(void);
@end

NS_ASSUME_NONNULL_END
