//
//  AIRBRoomBasicInfo.h
//  AliInteractiveRoomBundle
//
//  Created by 刘再勇 on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBPluginInstanceItem : NSObject
/**
 * 插件ID
 */
@property (nonatomic, copy) NSString * pluginId;

/**
 * 实例ID
 */
@property (nonatomic, copy) NSString * instanceId;

/**
 * 创建时间ms
 */
@property (nonatomic, assign) int64_t createTime;

/**
 * 插件透传信息
 */
@property (nonatomic, copy) NSDictionary<NSString *, NSString *> * extension;

@end

@interface AIRBPluginInstanceInfo : NSObject
/**
 * 插件实例列表
 */
@property (nonatomic, copy) NSArray<AIRBPluginInstanceItem *> * instanceList;

@end

@interface AIRBRoomBasicInfo : NSObject

/**
 * 房间id
 */
@property (nonatomic, copy) NSString * roomID;

/**
 * 房间标题
 */
@property (nonatomic, copy) NSString * title;

/**
 * 房间公告
 */
@property (nonatomic, copy) NSString * notice;

/**
 * 房主的用户Id
 */
@property (nonatomic, copy) NSString * ownerID;

/**
 * uv
 */
@property (nonatomic, assign) int32_t uv;

/**
 * 在线人数
 */
@property (nonatomic, assign) int32_t onlineCount;

/**
 * 插件信息
 */
@property (nonatomic, strong) AIRBPluginInstanceInfo * pluginInstanceInfo;

/**
 * pv
 */
@property (nonatomic, assign) int32_t pv;

/**
 * 房间扩展信息
 */
@property (nonatomic, copy) NSDictionary<NSString *, NSString *> * extension;

/**
 * 房间的所有管理员
 */
@property (nonatomic, copy) NSArray<NSString *> * administers;

@end

NS_ASSUME_NONNULL_END
