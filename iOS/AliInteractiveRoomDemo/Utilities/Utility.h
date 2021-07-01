//
//  Utility.h
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/18.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface Utility : NSObject
+ (NSString*)currentDateString;
+ (NSString*)randomNumString;
+ (NSString *)AIRBRequestSignWithSignSecret:(NSString*)secret
                                     method:(NSString*)method
                                       path:(NSString*)path
                                 parameters:(NSDictionary*)params
                                    headers:(NSDictionary*)headers;
+ (NSString *)encodeToPercentEscapeString:(NSString *)input;
@end

NS_ASSUME_NONNULL_END
