//
//  ASLSBLinkMicDelegate.h
//  AliStandardClassroomBundle
//
//  Created by 刘再勇 on 2022/1/6.
//

#import <Foundation/Foundation.h>
#import "../LinkMic/ASLSBLinkMicUserModel.h"

NS_ASSUME_NONNULL_BEGIN

@class AIRBRTCUserVolumeInfo;

typedef NS_ENUM(NSInteger, ASLSBLinkMicEvent)
{
    ASLSBLinkMicEventLocalPreviewStarted = 0,
    ASLSBLinkMicEventLocalJoinSucceeded = 1,
    ASLSBLinkMicEventLocalLeaveSucceeded = 2,
//    ASLSBLinkMicEventDestroySucceeded,
//    ASLSBLinkMicEventBypassLiveStarted,
//    ASLSBLinkMicEventBypassLiveStopped,
//    ASLSBLinkMicEventBypassLiveDestroyed,
//    ASLSBLinkMicEventNotification,
//    ASLSBLinkMicEventNetworkConnectionLost,      // 网络连接断开
//    ASLSBLinkMicEventNetworkReconnectStart,      // 网络开始重连
//    ASLSBLinkMicEventNetworkReconnectSuccess,    // 网络重连成功
//    ASLSBLinkMicEventNetworkConnectionFailed,    // 网络连接失败（不再进行重连）
};

typedef NS_ENUM(NSInteger, ASLSBLinkMicError)
{
    ASLSBLinkMicErrorInternal = 0x101,
    ASLSBLinkMicErrorLinkMicNotEnabled = 0x102,
    ASLSBLinkMicErrorNotAllowedToOpenMic = 0x103,
};

@protocol ASLSBLinkMicDelegate <NSObject>

- (void) onASLSBLinkMicEvent:(ASLSBLinkMicEvent)event info:(NSDictionary*)info;

- (void) onASLSBLinkMicError:(ASLSBLinkMicError)error message:(NSString*)msg;

- (void) onASLSBLinkMicUserJoined:(BOOL)isNewJoined userList:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicUserLeft:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicCameraStreamAvailable:(NSString*)userID view:(UIView*)view;

- (void) onASLSBLinkMicRemoteCameraStateChanged:(NSString*)userID open:(BOOL)open;

- (void) onASLSBLinkMicRemoteMicStateChanged:(NSArray<NSString*>*)userIDList open:(BOOL)open;

- (void) onASLSBLinkMicScreenShareStreamAvailable:(NSString*)userID view:(UIView*)view;

- (void) onASLSBLinkMicRemoteScreenShareStateChanged:(NSString*)userID open:(BOOL)open;

- (void) onASLSBLinkMicInvited:(ASLSBLinkMicUserModel*)inviter userInvitedList:(NSArray<ASLSBLinkMicUserModel*>*)userInvitedList;

- (void) onASLSBLinkMicInviteCanceledForMe;

- (void) onASLSBLinkMicInviteRejected:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicApplied:(BOOL)isNewApplied userList:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicApplyCanceled:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicApplyResponse:(BOOL)approve user:(NSString*)userID;

- (void) onASLSBLinkMicKicked:(NSArray<ASLSBLinkMicUserModel*>*)userList;

- (void) onASLSBLinkMicSelfMicAllowed:(BOOL)allowed; // 废弃

- (void) onASLSBLinkMicSelfMicChangedByOthers:(BOOL)open;

- (void) onASLSBLinkMicAllMicAllowed:(BOOL)allowed;

- (void) onASLSBLinkMicActiveSpeaker:(NSString*)userID;

- (void) onASLSBLinkMicAudioVolumeCallback:(NSArray <AIRBRTCUserVolumeInfo *>*)volumeInfoArray totalVolume:(int)totalVolume;

@end

NS_ASSUME_NONNULL_END
