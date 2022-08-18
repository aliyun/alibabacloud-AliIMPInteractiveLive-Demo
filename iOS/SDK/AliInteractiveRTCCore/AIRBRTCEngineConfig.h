//
//  AIRBRTCEngineConfig.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface AIRBRTCEngineConfig : NSObject
@property (copy, nonatomic) NSString* userID;
@property (copy, nonatomic) NSString* userNick;
@property (copy, nonatomic) NSString* conferenceID;
@property (nonatomic, nonnull) NSString * token;

@property (nonatomic, nonnull) NSString * gslb;

@property (nonatomic, nonnull) NSString * appId;

@property (nonatomic) int64_t timestamp;

@property (nonatomic, nonnull) NSString * nonce;

@property (nonatomic, assign) BOOL audioOnlyModeEnabled;
@property (nonatomic, assign) CGSize videoDimensions;
@property (nonatomic, assign) BOOL dualStreamPublished;

+ (instancetype) createConfig;
@end

NS_ASSUME_NONNULL_END
