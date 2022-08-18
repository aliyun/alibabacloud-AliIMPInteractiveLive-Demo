//
//  Utility.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/18.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBUtility : NSObject
+ (NSString*)currentDateString;
+ (NSString*)randomNumString;
+ (NSString *)getSignedRequestStringWithSecret:(NSString*)secret
                                        method:(NSString*)method
                                          path:(NSString*)path
                                    parameters:(NSDictionary*)params
                                       headers:(NSDictionary*)headers;
+ (NSString *)encodeToPercentEscapeString:(NSString *)input;
+ (BOOL) currentDeviceiPhone;
+ (BOOL) currentDeviceiPad;
+ (BOOL) isNum:(NSString *)checkedString;
+ (BOOL) isNumOrCharacter:(NSString *)checkedString;
@end

NS_ASSUME_NONNULL_END
