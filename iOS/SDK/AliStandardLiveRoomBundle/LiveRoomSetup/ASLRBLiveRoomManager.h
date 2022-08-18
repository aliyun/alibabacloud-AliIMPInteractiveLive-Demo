//
//  ASLRBLiveRoomManager.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/7/20.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <AliStandardLiveRoomBundle/ASLRBCommonDefines.h>

NS_ASSUME_NONNULL_BEGIN


@class ASLRBAppInitConfig, ASLRBLiveInitConfig, ASLRBLiveRoomViewController;

@interface ASLRBLiveRoomManager : NSObject
/**
 * 获取ASLRBLiveRoomManager全局单例对象
 */
+ (ASLRBLiveRoomManager*)sharedInstance;

/**
 * 接收内部日志用的block；注意避免block强引用外部对象；
 * 注意：必须要在globalInitOnceWithConfig之前设置；
 */
@property (strong, nonatomic) void(^onLogMessage)(NSString* log);

/**
 * 初始化；收到onSuccess后再进行下一步；
 * @param config 初始化需要的配置信息，具体见ASLRBAppInitConfig
 * @param onSuccess  初始化成功后回调；注意避免block内强引用外部对象造成循环引用
 * @param onFailure  初始化失败时回调，会有具体的错误信息；注意避免block内强引用外部对象造成循环引用
 */
- (void) globalInitOnceWithConfig:(ASLRBAppInitConfig*)config
                        onSuccess:(void(^)(void))onSuccess
                        onFailure:(void(^)(NSString* errorMessage))onFailure;



/**
 * 初始化；收到onSuccess后再进行下一步；
 * @param config 直播初始化需要的配置信息，具体见ASLRBLiveInitConfig
 * @param onCompletion  初始化成功后回调；注意避免block内强引用外部对象造成循环引用
 */
- (void) createLiveRoomVCWithConfig:(ASLRBLiveInitConfig *)config
                       onCompletion:(void(^)(ASLRBLiveRoomViewController* liveRoomVC))onCompletion  DEPRECATED_MSG_ATTRIBUTE("建议使用新接口 -(void) createLiveRoomVCWithConfig:onSuccess:onFailure:");

/**
 * 初始化；收到onSuccess后再进行下一步；
 * @param config 直播初始化需要的配置信息，具体见ASLRBLiveInitConfig
 * @param onSuccess 成功回调，返回vc
 * @param onFailure  失败回调，返回错误信息
 */
- (void) createLiveRoomVCWithConfig:(ASLRBLiveInitConfig *)config
                          onSuccess:(void(^)(ASLRBLiveRoomViewController* liveRoomVC))onSuccess
                          onFailure:(void(^)(NSString* errorMessage))onFailure;
@end

NS_ASSUME_NONNULL_END
