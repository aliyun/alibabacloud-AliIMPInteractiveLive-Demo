//
//  ASLSBLinkMicHandler.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/4/11.
//

#import <Foundation/Foundation.h>
#import <AliInteractiveRoomBundle/AliInteractiveRoomBundle.h>
#import "ASLSBCommonDefines.h"
#import "ASLSBLinkMicDelegate.h"
#import "ASLSBLinkMicProtocol.h"
#import "ASLSBSceneModel.h"

NS_ASSUME_NONNULL_BEGIN

@protocol ASLSBLinkMicHandlerDelegate <NSObject, ASLSBLinkMicDelegate>

@property(nonatomic, weak) id<ASLSBLinkMicDelegate> delegate;
@property(nonatomic, strong) id<AIRBRoomChannelProtocol> room;
@property(nonatomic, strong) ASLSBSceneModel* sceneModel;
@property(nonatomic, strong) ASLSBSceneUserModel* userModel;

@end

@interface ASLSBLinkMicHandler : NSObject<ASLSBLinkMicProtocol, AIRBRTCDelegate>

@property(nonatomic, weak) id<ASLSBLinkMicHandlerDelegate> delegate;
@property(nonatomic, assign) CGSize videoStreamTypeHighDimensions;
@property(nonatomic, strong) AIRBRTCConfig* RTCConfig;

- (void)linkMicJoin:(BOOL)updateRTCInfo;

- (void)actionWhenSceneRoomEntered;
- (void)actionWhenRoomMessageReceived:(AIRBRoomChannelMessageType)messageType data:(NSDictionary *)data info:(NSDictionary *)info;
		
@end

NS_ASSUME_NONNULL_END
