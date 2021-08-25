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
 * 在房间内发送互动评论
 * @param comment 要发送的评论内容
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendComment:(NSString*)comment
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 发送点赞
 */
- (void) sendLike;

/**
 * 分页查询房间内互动评论
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
 * 禁止房间内某人发送互动评论
 * @param userID 被禁用户的id
 * @param seconds 禁言的时间, 单位为s, 默认为0, 表示永久禁言(除非取消禁言)
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) banCommentWithUserID:(nonnull NSString*)userID
             banTimeInSeconds:(int32_t)seconds
                    onSuccess:(void (^)(void))onSuccess
                    onFailure:(void (^)(NSString* errorMessage))onFailure;
                     
/**
 * 取消对房间内某人的禁止发送评论
 * @param userID 要取消禁言的用户id
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) cancelBanCommentWithUserID:(nonnull NSString*)userID
                          onSuccess:(void (^)(void))onSuccess
                          onFailure:(void (^)(NSString* errorMessage))onFailure;
                    

/**
 * 对房间内所有人禁止发送评论（不包括自己）或者取消，只有房间创建者才能调用生效
 * @param ban 禁止或取消禁止
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) banAllComment:(BOOL)ban
             onSuccess:(void (^)(void))onSuccess
             onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 在房间内给特定用户发送自定义消息
 * @param message 要发送的自定义消息内容
 * @param userList 消息接收对象，为空时不发送
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendCustomMessage:(NSString*)message toUsers:(NSArray*)userList onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 在房间内给所有用户发送自定义消息
 * @param message 要发送的自定义消息内容
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) sendCustomMessageToALL:(NSString*)message onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
