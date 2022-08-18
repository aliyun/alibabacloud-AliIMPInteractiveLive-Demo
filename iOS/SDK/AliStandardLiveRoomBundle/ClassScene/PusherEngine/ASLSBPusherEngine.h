//
//  ASLSBPusherEngine.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/20.
//

#import <Foundation/Foundation.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "ASLSBSceneModel.h"
#import "ASLSBLinkMicHandlerTeacher.h"

NS_ASSUME_NONNULL_BEGIN

@protocol ASLSBPusherEngineDelegate <NSObject>

@property(nonatomic, strong) id<AIRBRoomChannelProtocol> room;
@property(nonatomic, strong) ASLSBSceneModel* sceneModel;
//@property(nonatomic, strong) ASLSBSceneUserModel* userModel;
@property(nonatomic, strong) ASLSBLinkMicHandlerTeacher* linkMicHandler;

- (void) onASLSBPusherEngineEvent:(ASLSBPusherEngineEvent)event info:(NSDictionary*)info;

@end

@interface ASLSBPusherEngine : NSObject<AIRBRTCDelegate, AIRBLivePusherDelegate>

@property(nonatomic, weak) id<ASLSBPusherEngineDelegate> delegate;
@property(nonatomic, assign) AIRBRTCBypassLiveResolutionType byPassLiveResolutionType;  // 旁路推流的分辨率, 默认为1280x720


@property (strong, nonatomic) UIView* localPreview;// 推流时的相机画面预览view
@property (weak, nonatomic, readonly) UIViewController* faceBeautyConfigViewController; // 美颜面板

- (void) startLive;

- (void) pauseLive;

- (void) stopLiveOnSuccess:(void(^)(void))onSuccess
                 onFailure:(void(^)(NSString* error))onFailure;

- (void) startPreview:(UIView*)preview pushOrientation:(int)orientation;  // 开启预览,推流横竖屏方向 0:固定竖屏 1:固定横屏
- (void) startScreenCaptureWithOrientation:(int)orientation
                                appGroupID:(NSString*)appGroupID;  // 开始屏幕共享,推流横竖屏方向 0:固定竖屏 1:固定横屏

- (void) switchCamera;  // 前后摄像头

- (void) toggleMutedMicrophone; // 开关麦克风

- (void) mirrorLiveVideo:(BOOL)mirror;  // 开关镜像

- (void) pauseLiveStreaming:(BOOL)pause;    // 暂停直播推流

- (void) restartLiveStreaming;  // 重新启动推流

// 原子层的直播信息
- (void) updateLiveInfoWithTitle:(NSString*)title liveCoverURL:(NSString*)coverURL customDataStr:(NSString*)customDataStr customDataDic: (NSDictionary<NSString*,NSString*>*)customDataDic onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString* errorMessage))onFailure;

// 场景层的直播信息
- (void) updateLiveBusinessInfoWithLiveID:(NSString*)liveID title:(NSString*)title notice:(NSString*)notice liveCoverURL:(NSString*)coverURL customDataDic: (NSDictionary<NSString*,NSString*>*)customDataDic onSuccess:(void (^)(void))onSuccess onFailure:(void (^)(NSString * errorMessage))onFailure;

@end

NS_ASSUME_NONNULL_END
