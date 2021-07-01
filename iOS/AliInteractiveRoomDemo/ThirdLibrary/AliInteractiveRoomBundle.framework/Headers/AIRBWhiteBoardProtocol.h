//
//  AIRBWhiteBoardProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBWhiteBoardConfig;
@class AIRBWhiteBoardToken;

@protocol AIRBWhiteBoardDelegate <NSObject>
- (void) onAIRBWhiteBoardErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBWhiteBoardEvent:(AIRBWhiteBoardEvent)event info:(NSDictionary*)info;
- (void) requestWhiteBoardAccessTokenWithDocKey:(NSString*)docKey completion:(void (^)(AIRBWhiteBoardToken* token))onGotToken;
@end

@protocol AIRBWhiteBoardProtocol <NSObject>
@property (strong, nonatomic) UIView* whiteboardView;
@property (weak, nonatomic) id<AIRBWhiteBoardDelegate> delegate;

- (void) openWithConfig:(AIRBWhiteBoardConfig*)config;
- (void) destroy;
@end

NS_ASSUME_NONNULL_END
