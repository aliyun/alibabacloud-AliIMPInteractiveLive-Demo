//
//  AIRBLivePusherEngineWrapper.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/21.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBLivePusherOptions;

@protocol AIRBLivePusherEngineWrapperDelegate <NSObject>

- (void) onLivePusherErrorWithCode:(int32_t)code message:(NSString*)msg;
- (void) onLivePusherPreviewStarted;
- (void) onLivePuhserStreamingStartedWithMediaInfo:(NSDictionary*)info;
- (void) onLivePuhserStreamingStopped;
- (void) onLivePuhserStreamingRestarted;
- (void) onLivePusherStreamingConnectFailed;
- (void) onLivePusherStreamingNetworkPoor;
- (void) onLivePusherStreamingNetworkConnectionLost;
- (void) onLivePusherStreamingNetworkRecoveried;
- (void) onLivePusherStreamingReconnectStart;
- (void) onLivePusherStreamingReconnectSuccess:(BOOL)success;
- (void) onLivePusherStreamingStatistics:(NSDictionary*)statistics;
@end

@interface AIRBLivePusherEngineWrapper : NSObject

@property (weak, nonatomic) id<AIRBLivePusherEngineWrapperDelegate> delegate;
@property (copy, nonatomic) NSString* mediaURL;
@property (strong, nonatomic) UIView* preview;
@property (assign, nonatomic) int8_t contentMode;
@property (weak, nonatomic) id queenEngine;
@property (weak, nonatomic) UIViewController* faceBeautyConfigViewController;

+ (instancetype) createPusherEngine;
- (void) setupLivePusherEngineWithOptions:(AIRBLivePusherOptions*)options
                                  preview:(UIView*)preview
                              cloudConfig:(NSDictionary*)cloudConfig;

- (void) setupLivePusherEngineWithOptions:(AIRBLivePusherOptions*)options
                               appGroupID:(NSString*)appGroupID
                              cloudConfig:(NSDictionary*)cloudConfig;
- (void) startLiveStreaming;
- (void) restartLiveStreaming;
- (void) pauseLiveStreaming;
- (void) resumeLiveStreaming;
- (void) stopLiveStreaming;

- (void) toggleMuted;
- (void) toggleCamera;
- (void) setPreviewMirror:(BOOL)mirror;
- (void) setStreamingVideoMirror:(BOOL)mirror;

@end

NS_ASSUME_NONNULL_END
