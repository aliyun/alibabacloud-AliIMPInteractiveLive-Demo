//
//  AIRBLivePlayerEngineWrapper.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@protocol AIRBLivePlayerEngineWrapperDelegate <NSObject>

@required
- (void) onLivePlayerEnginePrepareDone;
- (void) onLivePlayerEngineFirstRenderedStartWithInfo:(NSDictionary*)info;
- (void) onLivePlayerEngineLoadingStart;
- (void) onLivePlayerEngineLoadingEnd;
- (void) onLivePlayerEngineErrorCode:(unsigned long)code errorMessage:(NSString*)errorMessage;
- (void) onLivePlayerEngineImageSnapshot:(UIImage*)image;
- (void) onLivePlayerEngineVideoSizeChanged:(int)width height:(int)height;
- (void) onLivePlayerEngineDownloadBitrate:(int32_t)bitrate;

@optional
- (void) onLivePlayerEngineVideoRendered;

@end

// 以下接口保持方法名字、属性名字一致，类名字不要一样
// 另外，请保证在播放器接口类中能够触发AIRBLivePlayerEngineWrapperDelegate的 @required 部分

@interface AIRBLivePlayerEngineWrapper : NSObject
@property (weak, nonatomic) id<AIRBLivePlayerEngineWrapperDelegate> delegate;
@property (weak, nonatomic) UIView* playerView;
@property (assign, nonatomic) int8_t contentMode; // 0 表示 SCALETOFILL，1 表示SCALEASPECTFIT，2 表示SCALEASPECTFILL
@property (assign, nonatomic) BOOL lowDelay;

+ (instancetype) createPlayer;
- (void) startWithMediaURL:(NSString*)url;
- (void) pause;
- (void) resume;
- (void) stop;
- (void) snapshot;
- (void) toggleMuted;
@end

NS_ASSUME_NONNULL_END
