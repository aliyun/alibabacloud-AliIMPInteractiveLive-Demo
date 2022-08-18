//
//  AIRBRTCUserModel.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2022/4/8.
//  Copyright © 2022 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRTCUserModel : NSObject

/**
 * 用户ID
 */
@property (nonatomic, copy) NSString * userId;

/**
 * 用户昵称
 */
@property (nonatomic, copy) NSString * nickname;

/**
 * 入会状态， 1: 初始状态 2: 呼叫状态 3:会议中 4: 入会失败 5: 被踢出 6:离会
 */
@property (nonatomic, assign) int32_t status;

/**
 * 摄像头状态， YES: 打开 NO: 关闭
 */
@property (nonatomic, assign) int32_t cameraOpened;

/**
 * 麦克风状态， YES: 打开 NO: 关闭
 */
@property (nonatomic, assign) int32_t micphoneOpened;

/**
 * 被单独静音状态， YES: 被静音 NO: 没有被静音
 */
@property (nonatomic) BOOL passiveMute;

@end

NS_ASSUME_NONNULL_END
