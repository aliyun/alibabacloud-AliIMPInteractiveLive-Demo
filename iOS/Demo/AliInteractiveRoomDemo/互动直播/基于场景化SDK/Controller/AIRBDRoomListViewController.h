//
//  AIRBDRoomListViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/23.
//

#import <UIKit/UIKit.h>
@class AIRBRoomEngineConfig;
NS_ASSUME_NONNULL_BEGIN

@interface AIRBDRoomListViewController : UIViewController

@property (copy, nonatomic) NSString* userID;
@property (strong, nonatomic) AIRBRoomEngineConfig* config;

@end

NS_ASSUME_NONNULL_END
