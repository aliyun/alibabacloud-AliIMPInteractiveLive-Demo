//
//  AIRBRTC.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBRTCProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;

@interface AIRBRTC : NSObject<AIRBRTCProtocol>
@property (weak, nonatomic) AIRBRoomChannel* room;

- (instancetype) initWithUserID:(NSString*)userID userNick:(NSString*)userNick;
- (void) updateConferenceID:(NSString*)conferenceID;
- (void) updateLiveID:(NSString*)liveID;
@end

NS_ASSUME_NONNULL_END
