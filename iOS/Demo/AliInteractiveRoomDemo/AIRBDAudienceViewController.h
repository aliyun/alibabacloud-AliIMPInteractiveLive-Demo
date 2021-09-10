//
//  AIRBDAudienceViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import <UIKit/UIKit.h>
@class AIRBDRoomInfoModel;
NS_ASSUME_NONNULL_BEGIN

@interface AIRBDAudienceViewController : UIViewController
@property(strong,nonatomic) AIRBDRoomInfoModel* roomModel;
- (void) enterRoom;
- (void) leaveRoom;
@end

NS_ASSUME_NONNULL_END
