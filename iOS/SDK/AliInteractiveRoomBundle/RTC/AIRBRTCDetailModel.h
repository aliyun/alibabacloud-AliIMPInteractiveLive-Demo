//
//  AIRBRTCDetailModel.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2022/4/8.
//  Copyright © 2022 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRTCUserModel;

@interface AIRBRTCDetailModel : NSObject

/**
 * 房间ID
 */
@property (nonatomic, copy) NSString * roomId;

/**
 * 会议ID
 */
@property (nonatomic, copy) NSString * confId;

/**
 * 用户列表
 */
@property (nonatomic, strong) NSArray<AIRBRTCUserModel *> * userList;

/**
 * 会议状态 0: 未开始 1: 进行中 2: 已结束
 */
@property (nonatomic, assign) int32_t status;

/**
 * 扩展信息
 */
@property (nonatomic, copy) NSString * extension;

/**
 * 全员禁音 YES: 全员禁音 NO: 非全员禁音
 */
@property (nonatomic, assign) BOOL muteAll;

@end

NS_ASSUME_NONNULL_END
