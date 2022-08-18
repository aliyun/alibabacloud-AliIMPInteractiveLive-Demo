//
//  ASLSBAppInitConfig.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/2/16.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLSBAppInitConfig : NSObject

/**
 *在阿里云低代码音视频工厂控制台上开通应用后获得，必传
 */
@property (nonatomic, copy) NSString* appID;

/**
 *在阿里云低代码音视频工厂控制台上开通应用后获得，必传
 */
@property (nonatomic, copy) NSString* appKey;

/**
 *已经部署了低代码音视频工厂服务的用户服务器地址，必传，例如https://xxx.com
 */
@property (nonatomic, copy) NSString* appServerUrl;

/**
 *在阿里云低代码音视频工厂控制台上开通应用后获得，必传
 */
@property (nonatomic, copy) NSString* appServerSignSecret;

/**
 *用户id，仅支持英文字母或者阿拉伯数字或者二者的组合，必传
 */
@property (nonatomic, copy) NSString* userID;

/**
 *用户昵称，支持任意字符，必传
 */
@property (nonatomic, copy) NSString* userNick;

/**
 *用户相关的自定义扩展信息
 */
@property (nonatomic, copy) NSDictionary<NSString*,NSString*>* userExtension;

@end

NS_ASSUME_NONNULL_END
