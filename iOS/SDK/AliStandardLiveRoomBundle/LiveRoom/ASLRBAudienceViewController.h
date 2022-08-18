//
//  AIRBDAudienceViewController.h
//  AliInteractiveLiveDemo
//
//  Created by 麦辣 on 2021/6/2.
//

#import <UIKit/UIKit.h>
#import "../LiveRoomSetup/ASLRBLiveRoomViewController.h"
#import "ASLRBLiveRoomViewControllerProtocol.h"
#import "../LiveRoomBottomViews/ASLRBLiveRoomBottomViewActionsDelegate.h"

@class ASLRBLiveInitConfig;
@protocol ASLRBLiveRoomLoginDelegate;
NS_ASSUME_NONNULL_BEGIN


@interface ASLRBAudienceViewController : ASLRBLiveRoomViewController<ASLRBLiveRoomViewControllerProtocol,ASLRBLiveRoomBottomViewActionsDelegate>
@end

NS_ASSUME_NONNULL_END
