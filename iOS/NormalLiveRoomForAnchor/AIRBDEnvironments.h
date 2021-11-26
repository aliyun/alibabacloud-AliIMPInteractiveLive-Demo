//
//  AIRBDEnvironments.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDEnvironments : NSObject
@property (copy, nonatomic) NSString* interactiveClassRoomAppID; // 在阿里云互动直播控制台上开通应用后获得
@property (copy, nonatomic) NSString* interactiveClassRoomAppKey; // 在阿里云互动直播控制台上开通应用后获得
@property (copy, nonatomic) NSString* appServerUrl; // 业务方自己的appserver域名
@property (copy, nonatomic) NSString* signSecret; // 业务方自定义的加签secret

+ (instancetype)shareInstance;
@end

NS_ASSUME_NONNULL_END
