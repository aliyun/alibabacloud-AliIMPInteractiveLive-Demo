//
//  AIRBRoomSceneLiveProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/9/15.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomSceneLiveBusinessInfo;

@protocol AIRBRoomSceneLiveProtocol <NSObject>
- (void) createLiveWithTitle:(NSString*)title
                      notice:(NSString*)notice
                    coverUrl:(NSString*)url
                  anchorNick:(NSString*)nick
               enableLinkMic:(BOOL)enable
                   extension:(NSDictionary*)extension
                   onSuccess:(void(^)(NSDictionary* response))onSuccess
                   onFailure:(void(^)(NSString* error))onFailure DEPRECATED_MSG_ATTRIBUTE("即将废弃，建议使用服务端相关接口");

- (void) stopLiveWithLiveID:(NSString*)liveID
                  onSuccess:(void(^)(void))onSuccess
                  onFailure:(void(^)(NSString* error))onFailure DEPRECATED_MSG_ATTRIBUTE("即将废弃，建议使用服务端相关接口");

- (void) getLiveDetailWithLiveID:(NSString*)liveID
                       onSuccess:(void(^)(NSDictionary* response))onSuccess
                       onFailure:(void(^)(NSString* error))onFailure DEPRECATED_MSG_ATTRIBUTE("即将废弃，建议使用服务端相关接口");

- (void) getLiveListWithStatus:(int32_t)status
                       pageNum:(int32_t)pageNum
                      pageSize:(int32_t)pageSize
                     onSuccess:(void(^)(NSDictionary* response))onSuccess
                     onFailure:(void(^)(NSString* error))onFailure DEPRECATED_MSG_ATTRIBUTE("即将废弃，建议使用服务端相关接口");

- (void) updateLiveBusinessInfo:(AIRBRoomSceneLiveBusinessInfo*)info
                      onSuccess:(void(^)(void))onSuccess
                      onFailure:(void(^)(NSString* error))onFailure DEPRECATED_MSG_ATTRIBUTE("即将废弃，建议使用服务端相关接口");
@end

NS_ASSUME_NONNULL_END
