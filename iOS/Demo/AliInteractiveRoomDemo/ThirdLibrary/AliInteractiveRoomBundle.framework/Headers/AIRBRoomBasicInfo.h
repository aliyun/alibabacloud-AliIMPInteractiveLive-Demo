//
//  AIRBRoomBasicInfo.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomBasicInfo : NSObject

/**
 * 房间id
 */
@property (nonatomic, nonnull) NSString * roomID;

/**
 * 房间标题名字
 */
@property (nonatomic, nonnull) NSString * title;

/**
 * 房主的用户id
 */
@property (nonatomic, nonnull) NSString * ownerID;

@end

NS_ASSUME_NONNULL_END
