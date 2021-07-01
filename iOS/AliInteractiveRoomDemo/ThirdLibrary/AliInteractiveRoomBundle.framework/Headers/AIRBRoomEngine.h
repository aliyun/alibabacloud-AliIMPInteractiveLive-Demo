//
//  AIRBRoomEngine.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBCommonDefines.h"
#import <Foundation/Foundation.h>
#import "AIRBRoomChannelProtocol.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomEngineConfig;
@class AIRBRoomEngineAuthToken;
@class AIRBRoomEngine;
@class AIRBRoomEngineRoomListResponse;

@protocol AIRBRoomEngineDelegate <NSObject>
@required
- (void) onRoomEngineEvent:(AIRBRoomEngineEvent)event info:(nullable NSDictionary*)info object:(AIRBRoomEngine*)object;
- (void) onRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString*)msg object:(AIRBRoomEngine*)object;
- (void) requestLoginTokenWithCompletion:(void(^)(AIRBRoomEngineAuthToken* token))onTokenGotten;

@optional
- (void) onLog:(NSString*)message object:(AIRBRoomEngine*)object;
@end

@interface AIRBRoomEngine : NSObject

@property (weak, nonatomic) id<AIRBRoomEngineDelegate> delegate;

/**
 * 获取RoomEngine全局单例对象
 */
+ (AIRBRoomEngine*)sharedInstance;
/**
 *全局初始化，只需要调用一次
 */
- (void) globalSetupOnceWithConfig:(nonnull AIRBRoomEngineConfig*)config;
/**
 * 登陆
 * @param userID  需要登陆的用户ID
 */
- (void)loginWithUserID:(nonnull NSString*)userID;
/**
 * 登出
 * @param userID  需要登出的用户ID
 */
- (void)logoutWithUserID:(nonnull NSString*)userID;

/**
 * 登陆成功后 ，获取RoomChannel实例
 * @param roomID 房间ID
 */
- (nullable id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(nonnull NSString*)roomID;

/**
 * 获取房间列表
 * @param bizType 房间业务类型，default，business，classroom
 * @param pageNum 页码，从1开始
 * @param pageSize 当前页面房间数量
 */
- (void) getRoomListWithBizType:(nonnull NSString *)bizType
                        PageNum:(int32_t)pageNum
                       pageSize:(int32_t)pageSize
                      onSuccess:(void (^)(AIRBRoomEngineRoomListResponse * _Nonnull response))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;
@end

NS_ASSUME_NONNULL_END
