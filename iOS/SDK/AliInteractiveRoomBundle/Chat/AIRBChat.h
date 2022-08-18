//
//  AIRBChat.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBChatProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;

@interface AIRBChat : NSObject<AIRBChatProtocol>
@property (weak, nonatomic) AIRBRoomChannel* room;
@property (copy, nonatomic) NSString* chatID;

- (instancetype) initWithUserID:(NSString*)userID;
- (void) destroy;
@end

NS_ASSUME_NONNULL_END
