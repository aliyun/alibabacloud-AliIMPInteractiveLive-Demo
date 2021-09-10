//
//  AIRBDSetRoomViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/17.
//

#import <UIKit/UIKit.h>
@class AIRBRoomEngineConfig;
NS_ASSUME_NONNULL_BEGIN

@interface AIRBDSetRoomViewController : UIViewController
- (instancetype) initWithUserID:(NSString*)userID config:(AIRBRoomEngineConfig*)config;
@end

NS_ASSUME_NONNULL_END
