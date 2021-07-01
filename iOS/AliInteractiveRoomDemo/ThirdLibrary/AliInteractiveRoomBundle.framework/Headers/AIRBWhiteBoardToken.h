//
//  AIRBWhiteBoardToken.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBWhiteBoardToken : NSObject
@property (copy, nonatomic) NSString* accessToken;
@property (copy, nonatomic) NSString* collabHost;
@property (assign, nonatomic) int8_t permission;
@end

NS_ASSUME_NONNULL_END
