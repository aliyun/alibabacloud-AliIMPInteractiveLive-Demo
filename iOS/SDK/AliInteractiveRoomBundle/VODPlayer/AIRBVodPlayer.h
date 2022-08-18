//
//  AIRBVodPlayer.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/7/6.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBVodPlayerProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;

@interface AIRBVodPlayer : NSObject<AIRBVodPlayerProtocol>
@property (weak, nonatomic) AIRBRoomChannel* room;

- (instancetype) initWithUserID:(NSString*)userID;
@end

NS_ASSUME_NONNULL_END
