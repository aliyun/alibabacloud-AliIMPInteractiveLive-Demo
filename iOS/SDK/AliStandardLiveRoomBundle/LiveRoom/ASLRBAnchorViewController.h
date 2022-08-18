//
//  AIRBDAnchorViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import <UIKit/UIKit.h>
#import "../LiveRoomSetup/ASLRBLiveRoomViewController.h"
#import "ASLRBLiveRoomViewControllerProtocol.h"
#import "../LiveRoomBottomViews/ASLRBLiveRoomBottomViewActionsDelegate.h"

NS_ASSUME_NONNULL_BEGIN

@interface ASLRBAnchorViewController : ASLRBLiveRoomViewController <ASLRBLiveRoomViewControllerProtocol, ASLRBLiveRoomBottomViewActionsDelegate>
@end

NS_ASSUME_NONNULL_END
