//
//  AIRBDEnvironments.h
//  AliInteractiveRoomDemo
//
//  Created by fernando on 2021/6/30.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBDEnvironments : NSObject
@property (copy, nonatomic) NSString* appID;
@property (copy, nonatomic) NSString* appKey;
@property (copy, nonatomic) NSString* releaseLongLinkURL;
@property (copy, nonatomic) NSString* prereleaseLongLinkURL;
@property (copy, nonatomic) NSString* appServerHost;
@property (copy, nonatomic) NSString* signSecret;

+ (instancetype)shareInstance;
@end

NS_ASSUME_NONNULL_END
