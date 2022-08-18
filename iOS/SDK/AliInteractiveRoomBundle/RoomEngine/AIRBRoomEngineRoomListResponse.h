//
//  AIRBRoomEngineRoomListResponse.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AliInteractiveRoomBundle/AIRBRoomBasicInfo.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomBasicInfo;

@interface AIRBRoomEngineRoomListResponse : NSObject
/**
 * 租户下的房间列表基础信息
 */
@property (nonatomic, nonnull) NSArray<AIRBRoomBasicInfo *> * roomBasicInfoList;

/**
 * 租户下的房间列表总数
 */
@property (nonatomic) int32_t total;

/**
 * 是否还有更多的数据
 */
@property (nonatomic) BOOL hasMore;
@end

NS_ASSUME_NONNULL_END
