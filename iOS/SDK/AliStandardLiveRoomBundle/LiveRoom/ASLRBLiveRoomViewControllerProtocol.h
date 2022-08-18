//
//  ASLRBLiveRoomViewControllerProtocol.h
//  AliStandardLiveRoomBundle
//
//  Created by fernando on 2021/10/28.
//

#import <Foundation/Foundation.h>

@class ASLRBAppInitConfig, ASLRBLiveInitConfig;
@protocol ASLRBLiveRoomLoginDelegate;

NS_ASSUME_NONNULL_BEGIN

@protocol ASLRBLiveRoomViewControllerProtocol <NSObject>
@property (copy, nonatomic) NSDictionary* liveDetail;
@property (strong, nonatomic) UIImageView* backgroundView;

- (instancetype) initWithAppInitConfig:(ASLRBAppInitConfig*)appInitConfig
                        liveInitConfig:(ASLRBLiveInitConfig*)liveInitConfig
                            liveDetail:(NSDictionary*)liveDetail
                              delegate:(id<ASLRBLiveRoomLoginDelegate>)delegate;
                     
@end

NS_ASSUME_NONNULL_END
