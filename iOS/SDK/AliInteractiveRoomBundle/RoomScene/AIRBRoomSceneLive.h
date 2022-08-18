//
//  AIRBRoomSceneLive.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/9/15.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBRoomSceneLiveProtocol.h"

NS_ASSUME_NONNULL_BEGIN


@interface AIRBRoomSceneLive : NSObject<AIRBRoomSceneLiveProtocol>
@property (copy, nonatomic) NSString* userID;

- (instancetype) initWithUserID:(NSString*)userID;
@end

NS_ASSUME_NONNULL_END
