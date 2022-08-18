//
//  AIRBLoggerManager.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/5/11.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AIRBCommonDefines.h"
NS_ASSUME_NONNULL_BEGIN

@protocol AIRBLoggerManagerDelegate <NSObject>

- (void) onLogMessage:(NSString*)message;

@end

@interface AIRBLoggerManager : NSObject
@property (weak, nonatomic) id<AIRBLoggerManagerDelegate> delegate;
@property (assign, nonatomic) AIRBLoggerLevel loggerLevel;

+ (instancetype) sharedInstance;
- (void) log:(NSString*)message withLevel:(AIRBLoggerLevel)level;
@end

NS_ASSUME_NONNULL_END
