//
//  AIRBRoomChannel.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "AIRBCommonDefines.h"
#import "AIRBRoomChannelProtocol.h"

@class RoomManager;
NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomChannel : NSObject<AIRBRoomChannelProtocol>

@property (copy, nonatomic) NSString* roomOwnerID;
@property (strong, nonatomic) RoomManager* roomManager;

- (instancetype) initWithRoomID:(nonnull NSString*)roomID userID:(NSString*)userID;

@end

NS_ASSUME_NONNULL_END
