//
//  AIRBLivePlayer.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/12.
//  Copyright © 2021 AliYun. All rights reserved.
//

//#import <Foundation/Foundation.h>
//#import <UIKit/UIKit.h>
#import "AIRBLivePlayerProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;

@interface AIRBLivePlayer : NSObject<AIRBLivePlayerProtocol>
@property (weak, nonatomic) AIRBRoomChannel* room;

- (instancetype) initWithUserID:(NSString*)userID;
- (void) updateLiveID:(NSString*)liveID;
- (void) notifyLivePushStart:(BOOL)started;

@end

NS_ASSUME_NONNULL_END
