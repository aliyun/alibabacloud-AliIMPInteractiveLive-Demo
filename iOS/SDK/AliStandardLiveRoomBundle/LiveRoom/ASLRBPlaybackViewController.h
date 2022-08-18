//
//  ASLRBPlaybackViewController.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/28.
//

#import <UIKit/UIKit.h>
#import "../LiveRoomSetup/ASLRBLiveRoomViewController.h"
#import "ASLRBLiveRoomViewControllerProtocol.h"

@protocol ASLRBLiveRoomLoginDelegate;
NS_ASSUME_NONNULL_BEGIN

@interface ASLRBPlaybackViewController : ASLRBLiveRoomViewController<ASLRBLiveRoomViewControllerProtocol>
@end

NS_ASSUME_NONNULL_END
