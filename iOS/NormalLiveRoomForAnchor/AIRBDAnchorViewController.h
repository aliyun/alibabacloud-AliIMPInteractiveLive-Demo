//
//  AIRBDAnchorViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@class AIRBDRoomInfoModel;
@interface AIRBDAnchorViewController : UIViewController

@property(strong,nonatomic) AIRBDRoomInfoModel* roomModel;
- (void) enterRoom;
- (void) createRoomWithCompletion:(void(^)(NSString* roomID))onGotRoomID;
- (void) leaveRoom;
@end

NS_ASSUME_NONNULL_END
