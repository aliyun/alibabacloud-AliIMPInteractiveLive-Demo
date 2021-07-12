//
//  AIRBRoomEngineConfig.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/7.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomEngineConfig : NSObject

@property (copy, nonatomic, nonnull) NSString *appID;
@property (copy, nonatomic, nonnull) NSString *appKey;
@property (copy, nonatomic, nonnull) NSString *deviceID;
@property (assign, nonatomic) int8_t environmentType; // 0：预发环境，1：线上环境；默认线上环境

@end

NS_ASSUME_NONNULL_END
