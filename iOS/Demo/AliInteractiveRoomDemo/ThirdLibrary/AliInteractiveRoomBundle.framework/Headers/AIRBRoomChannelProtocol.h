//
//  AIRBRoomChannelProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AIRBLivePlayerProtocol.h"
#import "AIRBLivePusherProtocol.h"
#import "AIRBRTCProtocol.h"
#import "AIRBChatProtocol.h"
#import "AIRBWhiteBoardProtocol.h"
#import "AIRBVodPlayerProtocol.h"
#import "AIRBDocumentProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannel;
@class AIRBRoomChannelUserListResponse;

@protocol AIRBRoomChannelDelegate <NSObject>
- (void) onAIRBRoomChannelEvent:(AIRBRoomChannelEvent) event info:(NSDictionary*)info;
@optional
- (void) onAIRBRoomChannelErrorWithCode:(AIRBErrorCode)code message:(NSString*)message;
@end

@protocol AIRBRoomChannelProtocol <NSObject>

/**
 * 所有事件和错误的回调通知
 */
@property (weak, nonatomic) id<AIRBRoomChannelDelegate> delegate;

/**
 * 用来进行房间互动消息收发的实例，具体接口见AIRBChatProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBChatProtocol> chat;

/**
 * 用来进行房间内进行直播推流的实例，具体接口见AIRBLivePusherProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBLivePusherProtocol> livePusher;

/**
 * 用来在房间内进行直播拉流的实例，具体接口见AIRBLivePlayerProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBLivePlayerProtocol> livePlayer;

/**
 * 用来在房间内进行点播的实例，具体接口见AIRBVodPlayerProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBVodPlayerProtocol> vodPlayer;

/**
 * 用来在房间实时音视频通信活动的实例，具体接口见AIRBRTCProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBRTCProtocol> rtc;

/**
 * 用来进行房间查看或操作的白板实例，具体接口见AIRBWhiteBoardProtocol，进入房间成功后才可使用
 */
@property (strong, nonatomic) id<AIRBWhiteBoardProtocol> whiteboard;

/**
 * 文档服务
 */
@property (strong, nonatomic) id<AIRBDocumentProtocol> document;

/**
 * 进入房间
 */
- (void) enterRoomWithUserNick:(NSString*)userNick;

/**
 * 离开房间
 */
- (void) leaveRoom;

/**
 * 更新房间标题
 * @param title 要更新的房间标题
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) updateRoomTitle:(nonnull NSString*)title
               onSuccess:(void (^)(void))onSuccess
               onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 更新房间公告
 * @param notice 要更新的房间公告
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) updateRoomNotice:(nonnull NSString*)notice
               onSuccess:(void (^)(void))onSuccess
               onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 获取房间内人员列表
 * @param pageNum  分页index，从1开始
 * @param pageSize  每页的数量
 * @param onSuccess 成功的回调，结果见AIRBRoomChannelUserListResponse
 * @param onFailure 失败的回调
 */
- (void) getRoomUserListWithPageNum:(int32_t)pageNum
                           pageSize:(int32_t)pageSize
                          onSuccess:(void (^)(AIRBRoomChannelUserListResponse * _Nonnull response))onSuccess
                          onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 从房间内踢出某人
 * @param kickUserID 被踢人的用户id
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) kickRoomUserWithUserID:(nonnull NSString*)kickUserID
                      onSuccess:(void (^)(void))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;

///**
// * 在房间内发送消息
// * @param message 消息内容
// * @param userIDs 发送消息的所有对象用户id，为空时表示给房间内所有人发消息
// * @param fromSystem 是否系统消息
// */
//- (void) sendRoomMessage:(NSString*)message type:(BOOL)fromSystem toUsers:(NSArray*)userIDs ;
@end

NS_ASSUME_NONNULL_END
