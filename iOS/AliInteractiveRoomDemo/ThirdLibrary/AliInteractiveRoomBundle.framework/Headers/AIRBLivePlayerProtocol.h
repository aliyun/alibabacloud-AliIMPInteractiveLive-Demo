//
//  AIRBLivePlayerProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBLivePlayerDelegate <NSObject>
- (void) onAIRBLivePlayerErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBLivePlayerEvent:(AIRBLivePlayerEvent)event info:(NSDictionary*)info;
- (void) onAIRBLivePlayerImageSnapshot:(UIImage*)image;
@end

@protocol AIRBLivePlayerProtocol <NSObject>
@property (strong, nonatomic) UIView* playerView;
@property (weak, nonatomic) id<AIRBLivePlayerDelegate> delegate;
@property (assign, nonatomic) float playerVolume;
@property (assign, nonatomic) AIRBLiveViewContentMode contentMode;

- (void) start;
- (void) pause;
- (void) resume;
- (void) stop;
- (void) toggleMuted;
@end

NS_ASSUME_NONNULL_END
