//
//  AIRBDLiveViewController.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/20.
//

#import <UIKit/UIKit.h>

@class AIRBRoomEngineConfig;

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDLiveViewController : UIViewController
- (instancetype) initWithUserID:(NSString*)userID config:(AIRBRoomEngineConfig*)config;
@end

NS_ASSUME_NONNULL_END
