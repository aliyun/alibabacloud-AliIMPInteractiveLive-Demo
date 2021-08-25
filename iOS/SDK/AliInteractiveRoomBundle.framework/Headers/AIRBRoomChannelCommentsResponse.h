//
//  AIRBRoomChannelCommentsResponse.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/27.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannelComment;

@interface AIRBRoomChannelCommentsResponse : NSObject
/**
 * 总数据量
 */
@property (nonatomic) int32_t total;

/**
 * 是否有更多
 */
@property (nonatomic) BOOL hasMore;

/**
 * 弹幕数据
 */
@property (nonatomic, nonnull) NSArray<AIRBRoomChannelComment *> * commentList;
@end

NS_ASSUME_NONNULL_END
