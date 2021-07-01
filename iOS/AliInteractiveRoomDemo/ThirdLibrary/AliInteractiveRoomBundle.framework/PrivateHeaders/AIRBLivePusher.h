//
//  AIRBLivePusher.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

//#import "AIRBLivePusherOptions.h"
#import "AIRBLivePusherProtocol.h"

@class AIRBRoomChannel;

NS_ASSUME_NONNULL_BEGIN

//@protocol AIRBLivePusherDelegate <NSObject>

//- (void) onLivePusherErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg object:(AIRBLivePusher*)object;
//- (void) onLivePusherPreviewStartedWithObject:(AIRBLivePusher*)object;
//- (void) onLivePuhserStreamingStartedWithObject:(AIRBLivePusher*)object;
//- (void) onLivePusherStreamingNetworkPoorWithObject:(AIRBLivePusher*)object;
//- (void) onLivePusherStreamingNetworkRecoveriedWithObject:(AIRBLivePusher*)object;
//@end

@interface AIRBLivePusher : NSObject<AIRBLivePusherProtocol>
@property (weak, nonatomic) AIRBRoomChannel* room;

- (instancetype) initWithUserID:(NSString*)userID;
- (void) updateLiveID:(NSString*)liveID;
@end

NS_ASSUME_NONNULL_END
