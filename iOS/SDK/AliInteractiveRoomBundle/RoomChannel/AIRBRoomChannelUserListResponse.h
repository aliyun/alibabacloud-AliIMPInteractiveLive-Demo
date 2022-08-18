//
//  AIRBRoomChannelUserListResponse.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/6/3.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannelUser;

@interface AIRBRoomChannelUserListResponse : NSObject
/**
 * 总人数
 */
@property (nonatomic) int32_t totalCount;

/**
 * 用户列表
 */
@property (nonatomic, nonnull) NSArray<AIRBRoomChannelUser *> * userList;

/**
 * 是否还有更多的数据
 */
@property (nonatomic) BOOL hasMore;
@end

NS_ASSUME_NONNULL_END
