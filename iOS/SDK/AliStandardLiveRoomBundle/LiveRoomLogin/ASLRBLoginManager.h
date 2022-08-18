//
//  ASLRBLoginManager.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/26.
//

#import <Foundation/Foundation.h>
#import "../LiveRoom/ASLRBLiveRoomLoginDelegate.h"

@class ASLRBAppInitConfig, ASLRBLiveInitConfig, ASLRBLiveRoomViewController;

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLoginManagerDelegate <NSObject>

- (void) onASLRBLoginManagerEventKickedOut;

@end

@interface ASLRBLoginManager : NSObject <ASLRBLiveRoomLoginDelegate>

@property(nonatomic, weak)id<ASLRBLoginManagerDelegate> delegate;

- (void) loginWithConfig:(ASLRBAppInitConfig*)config
               onSuccess:(void (^)(ASLRBAppInitConfig* loginedAppInitConfig))onSuccess
               onFailure:(void (^)(NSString * errorMessage))onFailure;
@end

NS_ASSUME_NONNULL_END
