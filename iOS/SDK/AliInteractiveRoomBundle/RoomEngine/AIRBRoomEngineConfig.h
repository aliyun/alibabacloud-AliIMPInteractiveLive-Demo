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
@property (copy, nonatomic, nonnull) NSString *deviceID; // 建议不同设备的使用不同的deviceID

@end

NS_ASSUME_NONNULL_END
