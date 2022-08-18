//
//  ASLRBLiveRestartPushAlertController.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2022/3/24.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBLiveRestartPushAlertController : UIAlertController

+ (void) showAlertWithMessage:(NSString*)message
                restartAction:(void(^)(void))restartAction
                     parentVC:(UIViewController*)parentVC;
                 
@end

NS_ASSUME_NONNULL_END
