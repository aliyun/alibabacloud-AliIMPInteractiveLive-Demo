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
- (void) sendMessage:(NSString*)message
           onSuccess:(void (^)(void))onSuccess
           onFailure:(void (^)(NSString* errorMessage))onFailure;

- (void) sendLikeWithCount:(int32_t)count
                 onSuccess:(void (^)(void))onSuccess
                 onFailure:(void (^)(NSString*  errorMessage))onFailure;

- (void) queryCommentsWithSortedType:(AIRBRoomChatCommentsSortedType)sortedType
                             pageNum:(int)pageNum
                            pageSize:(int)pageSize
                           onSuccess:(void (^)(AIRBRoomChannelCommentsResponse* response))onSuccess
                           onFailure:(void (^)(NSString* errorMsg))onFailure;

/**
 *禁言的时间, 单位为s, 默认为0, 表示永久禁言(除非取消禁言)
 */
- (void) muteUserWithUserID:(nonnull NSString*)userID
          muteTimeInSeconds:(int32_t)seconds
                  onSuccess:(void (^)(void))onSuccess
                  onFailure:(void (^)(NSString* errorMessage))onFailure;

- (void) unmuteUserWithUserID:(nonnull NSString*)userID
                    onSuccess:(void (^)(void))onSuccess
                    onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 *YES为全体禁言，NO为取消全体禁言
 */
- (void) muteAll:(BOOL)mute
       onSuccess:(void (^)(void))onSuccess
       onFailure:(void (^)(NSString* errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
