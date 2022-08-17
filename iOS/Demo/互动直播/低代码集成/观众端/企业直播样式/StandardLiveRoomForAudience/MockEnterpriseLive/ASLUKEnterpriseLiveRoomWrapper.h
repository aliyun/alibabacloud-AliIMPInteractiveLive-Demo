//
//  ASLUKLiveRoomWrapper.h
//  AliStandardLiveUIKit
//
//  Created by fernando on 2022/5/17.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface ASLUKEnterpriseLiveRoomWrapper : NSObject
@property (weak, nonatomic) UIViewController* lastViewController;
- (void)setupLiveRoomWithAppID:(NSString*)appID
                        appKey:(NSString*)appKey
                  appServerUrl:(NSString*)serverUrl
               appServerSecret:(NSString*)secret
                        liveID:(NSString*)liveID
                        userID:(NSString*)userID
                      userNick:(NSString*)userNick
                     onSuccess:(void(^)(UIViewController* liveRoomViewController))onSuccess
                     onFailure:(void(^)(NSString* errorMessage))onFailure;
                          
- (void)destroyLiveRoom;
@end

NS_ASSUME_NONNULL_END
