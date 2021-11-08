//
//  HMACSHA1.h
//  AliInteractiveRoomBundleDemo
//
//  Created by fernando on 2021/5/18.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface HMACSHA1 : NSObject
+ (NSString *)hmacsha1:(NSString *)text key:(NSString *)secret;
@end

NS_ASSUME_NONNULL_END
