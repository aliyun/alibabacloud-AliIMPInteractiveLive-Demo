//
//  AIRBDAnchorView.h
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/14.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AIRBRoomEngineConfig;

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDAnchorView : UIView
- (void) createRoomWithConfig:(AIRBRoomEngineConfig*)config userID:(NSString*)userID completion:(void(^)(NSString* roomID))onGotRoomID;
- (void) enterRoomWithID:(NSString*)roomID userID:(NSString*)userID;
- (void) leaveRoom;
@property(strong,nonatomic)UINavigationController* exitActionDelegate;
@property(strong,nonatomic)UIViewController* actionDelegate;
@end

NS_ASSUME_NONNULL_END
