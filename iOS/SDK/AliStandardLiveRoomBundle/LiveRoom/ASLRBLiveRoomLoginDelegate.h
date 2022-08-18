//
//  ASLRBLiveRoomVideoControllerProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/26.
//

#import <Foundation/Foundation.h>

@class ASLRBAppInitConfig;
@protocol ASLRBLoginManagerDelegate;

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomLoginDelegate <NSObject>

@property(nonatomic, weak)id<ASLRBLoginManagerDelegate> delegate;

- (void) switchLoginedUser:(NSString*)newUserID
                 onSuccess:(void(^)(ASLRBAppInitConfig* appInitConfig))onSuccess
                 onFailure:(void(^)(NSString* errorMessage))onFailure;

- (void) dispatchLogoutTask;
@end

NS_ASSUME_NONNULL_END
