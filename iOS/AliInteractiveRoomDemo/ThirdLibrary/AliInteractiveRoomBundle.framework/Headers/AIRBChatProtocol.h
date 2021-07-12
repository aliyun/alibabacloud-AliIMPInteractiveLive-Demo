//
//  AIRBChatProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN


@class AIRBRoomChannelCommentsResponse;

@protocol AIRBChatProtocol <NSObject>

/**
 * 获取房间内当前的互动信息，包括点赞总数和评论的总数
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) getCurrentChatInfoOnSuccess:(void(^)(int32_t totalComment, int32_t totalLike))onSuccess onFailure:(void(^)(NSString* errMessage))onFailure;

/**
 * 发送消息
 * @param message 要发送的消息内容
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendMessage:(NSString*)message
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 发送点赞
 */
- (void) sendLike;

/**
 * 分页查询房间内消息
 * @param sortedType 查询评论按照时间的排序方式
 * @param pageNum 查询的页码，从1开始
 * @param pageSize 每页的大小
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) queryCommentsWithSortedType:(AIRBRoomChatCommentsSortedType)sortedType
                             pageNum:(int)pageNum
                            pageSize:(int)pageSize
                           onSuccess:(void (^)(AIRBRoomChannelCommentsResponse* response))onSuccess
                           onFailure:(void (^)(NSString* errorMsg))onFailure;

/**
 * 对房间内某人禁言
 * @param userID 被禁用户的id
 * @param seconds 禁言的时间, 单位为s, 默认为0, 表示永久禁言(除非取消禁言)
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) muteUserWithUserID:(nonnull NSString*)userID
          muteTimeInSeconds:(int32_t)seconds
                  onSuccess:(void (^)(void))onSuccess
                  onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 取消对房间内某人的禁言
 * @param userID 要取消禁言的用户id
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) unmuteUserWithUserID:(nonnull NSString*)userID
                    onSuccess:(void (^)(void))onSuccess
                    onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 对房间内所有人禁言（不包括自己）或者取消全体禁言，只有房间创建者才能调用生效
 * @param mute 禁言或取消禁言
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) muteAll:(BOOL)mute
       onSuccess:(void (^)(void))onSuccess
       onFailure:(void (^)(NSString* errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
