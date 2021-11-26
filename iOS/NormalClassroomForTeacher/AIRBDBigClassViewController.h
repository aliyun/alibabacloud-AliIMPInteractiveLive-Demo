//
//  AIRBDBigClassViewController.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/5/21.
//

#import <UIKit/UIKit.h>

@class AIRBRoomEngineConfig;

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDBigClassViewController : UIViewController
@property (copy, nonatomic) NSString* userID;
@property (strong, nonatomic) AIRBRoomEngineConfig* config;
@end

NS_ASSUME_NONNULL_END
