//
//  AIRBEnvironments.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/8.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBEnvironments : NSObject

+ (instancetype)shareInstance;

/**
 线上网关长连接服务器地址
*/
@property(nonatomic, copy) NSString* _Nonnull longLinkAddr;

/**
应用的语言设置, 默认为设备的locale信息
*/
@property(nonatomic, copy) NSString* _Nonnull appLocale;

/**
默认数据存储地址
*/
@property(nonatomic, copy) NSString* _Nonnull dataPath;

/**
应用日志信息存储位置
*/
@property(nonatomic, copy) NSString* _Nonnull logPath;

/**
时区名称
*/
@property(nonatomic, copy) NSString* _Nonnull timeZoneName;

/**
sdk版本
*/
@property(nonatomic, copy) NSString* _Nonnull sdkVersion;

/**
设备名称
*/
@property(nonatomic, copy) NSString* _Nonnull deviceName;

/**
设备类型
*/
@property(nonatomic, copy) NSString* _Nonnull deviceType;

/**
系统名称
*/
@property(nonatomic, copy) NSString* _Nonnull osName;

/**
系统版本
*/
@property(nonatomic, copy) NSString* _Nonnull osVersion;

/**
设备的语言设置
*/
@property(nonatomic, copy) NSString* _Nonnull deviceLocale;

/**
环境切换
*/
@property (nonatomic, assign) BOOL usePrereleaseEnvironment;

/**
云控配置
*/
@property (nonatomic, strong) NSDictionary<NSString *, NSString *> * cloudConfig;

/**
当前网络类型
*/
@property (nonatomic, copy) NSString* netowrkType;

@end

NS_ASSUME_NONNULL_END
