//
//  AIRBRoomChannelUser.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/6/3.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomChannelUser : NSObject
/**
 * 用户id
 */
@property (nonatomic, nonnull) NSString * openID;

/**
 * 用户昵称
 */
@property (nonatomic, nonnull) NSString * nick;

/**
 * 用户角色
 */
@property (nonatomic, nonnull) NSString * role;

/**
 * 用户扩展信息
 */
@property (nonatomic, nonnull) NSDictionary<NSString *, NSString *> * extension;
@end

NS_ASSUME_NONNULL_END
