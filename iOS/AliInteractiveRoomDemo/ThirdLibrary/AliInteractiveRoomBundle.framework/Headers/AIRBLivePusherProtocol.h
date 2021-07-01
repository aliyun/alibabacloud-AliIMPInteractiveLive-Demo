//
//  AIRBLivePusherProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import "AIRBCommonDefines.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBLivePusherOptions;
@class AIRBLivePusherFaceBeautyOptions;


@protocol AIRBLivePusherDelegate <NSObject>
- (void) onAIRBLivePuhserEvent:(AIRBLivePusherEvent)event info:(NSDictionary*)info;
- (void) onAIRBLivePusherError:(AIRBErrorCode)errorCode message:(NSString*)errorMessage;
@end

@protocol AIRBLivePusherProtocol <NSObject>
@property (weak, nonatomic) id<AIRBLivePusherDelegate> delegate;
@property (strong, nonatomic) AIRBLivePusherOptions* options;
@property (strong, nonatomic) UIView* pusherView;
@property (assign, nonatomic) AIRBLiveViewContentMode contentMode;

- (void) startLocalPreviewWithOptions:(AIRBLivePusherOptions*)options;
- (void) startLiveStreaming;
- (void) pauseLiveStreaming;
- (void) resumeLiveStreaming;
- (void) stopLiveStreaming;

- (void) toggleCamera;
- (void) toggleMuted;
- (void) setPreviewMirror:(BOOL)mirror;
- (void) setStreamingVideoMirror:(BOOL)mirror;
- (void) toggleFaceBeauty;
- (void) updateFaceBeautyParameters:(AIRBLivePusherFaceBeautyOptions*) beautyOptions;

@end

NS_ASSUME_NONNULL_END
