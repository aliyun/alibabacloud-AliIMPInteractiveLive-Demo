//
//  AIRBRoomEngine.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import "AIRBCommonDefines.h"
#import <Foundation/Foundation.h>
#import <AliInteractiveRoomBundle/AIRBRoomChannelProtocol.h>
#import <AliInteractiveRoomBundle/AIRBRoomSceneLiveProtocol.h>
#import <AliInteractiveRoomBundle/AIRBRoomSceneClassProtocol.h>
#import <AliInteractiveRoomBundle/AIRBProduct360Protocol.h> // #import "AIRBProduct360Protocol.h"

NS_ASSUME_NONNULL_BEGIN

extern NSString *const kAIRBReachabilityBecameWiFiNotification;
extern NSString *const kAIRBReachabilityBecameWWANNotification;
extern NSString *const kAIRBReachabilityBecameUnrechableNotification;

@class AIRBRoomEngineConfig;
@class AIRBRoomEngineAuthToken;
@class AIRBRoomEngine;
@class AIRBRoomEngineRoomListResponse;
@class AIRBRoomBasicInfo;

@protocol AIRBRoomEngineDelegate <NSObject>
@required
- (void) onAIRBRoomEngineEvent:(AIRBRoomEngineEvent)event info:(nullable NSDictionary*)info;
- (void) onAIRBRoomEngineErrorWithCode:(AIRBErrorCode)code errorMessage:(NSString*)msg;
- (void) onAIRBRoomEngineRequestToken:(void(^)(AIRBRoomEngineAuthToken* token))onTokenGotten;

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
 * 全局初始化，只需要调用一次
 */
- (void) globalInitOnceWithConfig:(nonnull AIRBRoomEngineConfig*)config;

/**
 * 是否已全局初始化成功
 */
- (BOOL) isInited;

/**
 * 登录
 * @param userID  需要登陆的用户ID, 必须是阿拉伯数字或者英文字母或二者的混合
 * @note 旧版本登录接口的token参数通过请求token事件（onAIRBRoomEngineRequestToken:）的回调获取
 */
- (void)loginWithUserID:(nonnull NSString*)userID;

/**
 * 登出当前已登录的用户
 */
- (void)logoutOnSuccess:(void (^)(void))onSuccess
              onFailure:(void (^)(NSString* errorMessage))onFailure;

/**
 * 用户是否已登录
 */
- (BOOL) isLogined:(NSString*)userID;

/**
 * 登陆成功后 ，获取RoomChannel实例
 * @param roomID 房间ID
 */
- (nullable id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(nonnull NSString*)roomID;

/**
 * 登陆成功后 ，获取RoomChannel实例
 * @param roomID 房间ID，必传
 * @param bizType 业务类型，非必传
 * @param bizID 业务ID，比如liveID或者classID，非必传
 */
- (nullable id<AIRBRoomChannelProtocol>) getRoomChannelWithRoomID:(nonnull NSString*)roomID
                                                          bizType:(NSString*)bizType
                                                            bizID:(NSString*)bizID;
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


- (id<AIRBRoomSceneLiveProtocol>) getRoomSceneLive;

- (id<AIRBRoomSceneClassProtocol>) getRoomSceneClass;

- (id<AIRBProduct360Protocol>) getProduct360Channel;

@end

NS_ASSUME_NONNULL_END
