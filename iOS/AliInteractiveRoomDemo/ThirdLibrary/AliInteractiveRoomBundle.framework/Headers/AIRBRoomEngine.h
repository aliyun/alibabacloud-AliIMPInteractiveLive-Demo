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
@class AIRBRoomBasicInfo;

@protocol AIRBRoomEngineDelegate <NSObject>
@required
- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(nullable NSDictionary*)info;
- (void) onAIRBRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString*)msg;

@optional
- (void) onLog:(NSString*)message;
@end

@interface AIRBRoomEngine : NSObject

@property (weak, nonatomic) id<AIRBRoomEngineDelegate> delegate;
@property (assign, nonatomic) AIRBLoggerLevel logLevel; //默认AIRBLoggerLevelError

/**
 * 获取RoomEngine全局单例对象
 */
+ (AIRBRoomEngine*)sharedInstance;

/**
 *全局初始化，只需要调用一次
 */
- (void) globalInitOnceWithConfig:(nonnull AIRBRoomEngineConfig*)config;

/**
 * 登陆
 * @param userID  需要登陆的用户ID, 必须是阿拉伯数字或者英文字母或二者的混合
 * @param token  登陆鉴权需要的token，具体见AIRBRoomEngineAuthToken
 */
- (void)loginWithUserID:(nonnull NSString*)userID token:(nonnull AIRBRoomEngineAuthToken*)token;

/**
 * 登出
 */
- (void)logout;

/**
 * 登陆成功后 ，获取RoomChannel实例
 * @param roomID 房间ID
 */
- (nullable id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(nonnull NSString*)roomID;

/**
 * 获取房间列表
 * @param pageNum 页码，从1开始
 * @param pageSize 当前页面房间数量
 */
- (void) getRoomListWithPageNum:(int32_t)pageNum
                       pageSize:(int32_t)pageSize
                      onSuccess:(void (^)(AIRBRoomEngineRoomListResponse * _Nonnull response))onSuccess
                      onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 获取房间详细信息
 * @param roomID 房间ID
 */
- (void) getRoomDetailWithID:(NSString*) roomID
                   onSuccess:(void (^)(AIRBRoomBasicInfo * _Nonnull info))onSuccess
                   onFailure:(void (^)(NSString* errorMessage))onFailure;
@end

NS_ASSUME_NONNULL_END
