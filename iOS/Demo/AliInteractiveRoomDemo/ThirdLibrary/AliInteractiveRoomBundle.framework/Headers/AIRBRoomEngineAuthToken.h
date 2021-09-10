//
//  AIRBRoomEngineAuthToken.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/8.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRoomEngineAuthToken : NSObject
@property (nonatomic, copy, nonnull) NSString *accessToken;
@property (nonatomic, copy, nonnull) NSString *refreshToken;
@end

NS_ASSUME_NONNULL_END
