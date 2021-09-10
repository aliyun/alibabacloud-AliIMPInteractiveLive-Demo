//
//  AIRBRoomChannelComment.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/27.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomChannelComment : NSObject
/**
 * 话题Id
 */
@property (nonatomic, nonnull) NSString * topicId;

/**
 * 弹幕Id
 */
@property (nonatomic, nonnull) NSString * commentId;

/**
 * 弹幕类型
 */
@property (nonatomic) int32_t type;

/**
 * 发送者Id
 */
@property (nonatomic, nonnull) NSString * creatorId;

/**
 * 发送者昵称
 */
@property (nonatomic, nonnull) NSString * creatorNick;

/**
 * 弹幕内容
 */
@property (nonatomic, nonnull) NSString * content;

/**
 * 创建时间
 */
@property (nonatomic) int64_t createAt;

/**
 * 拓展字段
 */
@property (nonatomic, nonnull) NSDictionary<NSString *, NSString *> * extension;
@end

NS_ASSUME_NONNULL_END
