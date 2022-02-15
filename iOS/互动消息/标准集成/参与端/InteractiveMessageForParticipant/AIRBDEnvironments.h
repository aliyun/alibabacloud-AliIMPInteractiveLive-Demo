//
//  AIRBDEnvironments.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDEnvironments : NSObject
@property (copy, nonatomic) NSString* interactiveMessageRoomAppID; // 在阿里云低代码音视频工厂制台上开通应用后获得
@property (copy, nonatomic) NSString* interactiveMessageRoomAppKey; // 在阿里云低代码音视频工厂控制台上开通应用后获得
@property (copy, nonatomic) NSString* appServerURL; //参考
@property (copy, nonatomic) NSString* appServerSignSecret;

+ (instancetype)shareInstance;
@end

NS_ASSUME_NONNULL_END
