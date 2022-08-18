//
//  AIRBRTCProtocol.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/6/22.
//  Copyright © 2021 AliYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

#import <AliInteractiveRoomBundle/AIRBCommonDefines.h>

NS_ASSUME_NONNULL_BEGIN

@class AIRBRoomChannelUserListResponse;
@class AIRBRTCConfig;
@class AIRBRTCBypassLiveLayoutPeerVideoModel;
@class AIRBRTCUserVolumeInfo, AIRBRTCUserModel, AIRBRTCDetailModel;

@protocol AIRBRTCDelegate <NSObject>
- (void) onAIRBRTCErrorWithCode:(AIRBErrorCode)code message:(NSString*)msg;
- (void) onAIRBRTCEvent:(AIRBRTCEvent)event info:(NSDictionary*)info;

/**
 * 相机流/屏幕共享流画面可用/不可用时触发的消息
 * @param available YES: 可用 NO: 不可用
 * @param userID 画面的userID
 * @param view 对应的画面，不可用时为空
 * @param type 类型（相机流/屏幕共享流）
 */
- (void) onAIRBRTCRemotePeerViewAvailable:(BOOL)available userID:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type;

/**
 * 相机流/屏幕共享流画面可用时触发的消息
 * @param userID 画面可用的userID
 * @param view 对应的画面
 * @param type 类型（相机流/屏幕共享流）
 */
- (void) onAIRBRTCRemotePeerViewAvailable:(NSString*)userID view:(UIView*)view type:(AIRBRTCVideoViewType)type DEPRECATED_MSG_ATTRIBUTE("建议使用新接口 - (void) onAIRBRTCRemotePeerViewAvailable:userID:view:type:");

/**
 * 第一帧视频帧显示时触发的消息
 * @param userID 画面可用的userID
 * @param type 类型（相机流/屏幕共享流）
 */
- (void) onAIRBRTCRemotePeerVideoFirstFrameDrawn:(NSString*)userID type:(AIRBRTCVideoViewType)type;

/**
 * 当前正在说话的人
 * @param userID 说话人userID, 为"0"表示本地说话人。其返回的是当前时间段内声音最大的用户ID，而不是瞬时声音最大的用户ID
*/
- (void) onAIRBRTCActiveSpeaker:(NSString*)userID;

/**
 * 用户的音频音量、语音状态和userID回调
 * @param volumeInfoArray 表示回调用户音量信息数组，包含用户userID、语音状态以及音量，userID为"0"表示本地说话人。
 * @param totalVolume 混音后的总音量，范围[0,255]。在本地用户的回调中，totalVolume为本地用户混音后的音量；在远端用户的回调中，totalVolume为所有说话者混音后的总音量
 */
- (void)onAIRBRTCAudioVolumeCallback:(NSArray <AIRBRTCUserVolumeInfo *> *_Nullable)volumeInfoArray totalVolume:(int)totalVolume;

/**
 * 网络质量变化时发出的消息
 * @param userID 网络质量发生变化的userID
 * @param upQuality  上行网络质量
 * @param downQuality  下行网络质量
 * @note 当网络质量发生变化时触发，userID为@""时代表自己的网络质量变化
 */
- (void)onAIRBRTCNetworkQualityChanged:(NSString *_Nonnull)userID
                      upNetworkQuality:(AIRBRTCNetworkQuality)upQuality
                    downNetworkQuality:(AIRBRTCNetworkQuality)downQuality;
@end

@protocol AIRBRTCProtocol <NSObject>

/**
 * 摄像头本地预览画面view
 */
@property (strong, nonatomic) UIView* rtcLocalView;

/**
 * 是否开启本地相机预览镜像，默认为是
 * @note 仅对前置摄像头画面生效
 */
@property (nonatomic, assign) BOOL previewMirrorEnabled;

/**
 * 是否开启相机流镜像，默认为否
 * @note 仅对前置摄像头画面生效
 */
@property (nonatomic, assign) BOOL videoStreamMirrorEnabled;


/**
 * 本地相机预览的填充模式，默认为AIRBRTCViewContentModeAuto
 */
@property (nonatomic, assign) AIRBRTCViewContentMode previewContentMode;

/**
 * 远端视频流在本地的填充模式，默认为AIRBRTCViewContentModeAuto
 */
@property (nonatomic, assign) AIRBRTCViewContentMode remoteVideoStreamContentMode;


/**
 * 用来接收RTC相关的事件和错误
 */
@property (weak, nonatomic) id<AIRBRTCDelegate> delegate;

/**
 * 美颜操作句柄，是 一个来自Queen.framework的实例对象，内部已经构造好，外部直接使用即可，具体见Queen.framework
 */
@property (weak, nonatomic, readonly) id queenEngine;

/**
 * 内部已经实现好了的一个美颜操控面板，可以直接push来展示；面板建议高度200，建议宽度与屏幕宽度相同；需要自定义实现功能请使用queenEngine
 */
@property (weak, nonatomic, readonly) UIViewController* faceBeautyConfigViewController;

/**
 * 获取房间内当前的RTC信息，具体信息见{@link AIRBRTCDetailModel}
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) getCurrentRTCDetailOnSuccess:(void(^)(AIRBRTCDetailModel* info))onSuccess onFailure:(void(^)(NSString* errMessage))onFailure;

/**
 * RTC配置，具体见AIRBRTCConfig
 * @note 在joinChannel之前配置
 */
- (void) setConfig:(AIRBRTCConfig*)config;

/**
 * 加入RTC
 * @note 旧版本中命名为joinChannelWithConfig
 */
- (void) joinChannel;

/**
 * 离开RTC
 * @param destroy YES表示结束RTC，NO表示只离开不结束
 * @note 只有房主可以结束
 * @note 注意：调用离开房间（AIRBRoomChannelProtocol中的leaveRoom）的时机，应该在离会成功（AIRBRTCEventLeaveSucceeded）或者结束RTC成功（AIRBRTCEventDestroySucceeded）之后
 */
- (void) leaveChannel:(BOOL)destroy;

/**
 * 离开RTC并结束RTC和旁路直播（如果有的话）
 * @note 旧版本接口，等同于stopBypassLiveStreaming:YES +  leaveChannel:YES
 */
//- (void) leaveChannel;

/**
 * 发送邀请其他人加入RTC的消息
 * @param userIDs 被邀请的userID数组
 */
- (void) addPeers:(NSArray<NSString*>*)userIDs;

/**
 * 发送将其他人踢出RTC的消息
 * @param userIDs 被踢出的userID数组
 */
- (void) removePeers:(NSArray<NSString*>*)userIDs;

/**
 * 发送同意/拒绝其他人加入RTC的申请的消息
 * @param approve YES表示同意，NO表示拒绝
 * @param userID 被同意/拒绝申请的userID
 */
- (void) approveJoiningApplication:(BOOL)approve fromPeer:(NSString*)userID; //approveRTCJoiningApplication

/**
 * 发送同意/拒绝连麦邀请的消息
 * @param accept YES表示同意，NO表示拒绝
 */
- (void) acceptCall:(BOOL)accept;

/**
 * 发送申请/撤销申请加入RTC的消息
 * @param applyOrWithdraw YES表示申请，NO表示撤销申请
 */
- (void) applyForJoining:(BOOL)applyOrWithdraw;

/**
 * 分页请求RTC成员列表
 * @param type 成员类型：已加入RTC / 申请加入RTC
 * @param pageNum 页码，从1开始
 * @param pageSize 每页的数据条数，最大为200
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) queryCurrentPeerListWithType:(AIRBRTCPeerType)type
                                 pageNum:(int32_t)pageNum
                                pageSize:(int32_t)pageSize
                               onSuccess:(void(^)(AIRBRoomChannelUserListResponse* rsp))onSuccess
                               onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 静音/开启本地麦克风
 * @param mute YES为关闭，NO为开启
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) muteLocalMicphone:(BOOL)mute
                 onSuccess:(void(^)(void))onSuccess
                 onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 静音/解除静音某个成员
 * @param mute YES为静音，NO为取消静音，默认为开启
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 * @note 只有房主可以调用
 */
- (void) muteRemoteMicphone:(BOOL)mute remotePeer:(NSString*)userID
                      onSuccess:(void(^)(void))onSuccess
                      onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 静音/解除静音全体成员
 * @param mute YES为静音，NO为取消静音
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 * @note 只有房主可以调用
 */
- (void) muteAllRemoteMicphone:(BOOL)mute
                     onSuccess:(void(^)(void))onSuccess
                     onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 关闭/开启本地摄像头和本地预览
 * @param mute YES为关闭，NO为开启，默认为开启
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 * @note 开启摄像头会自动开启本地预览
 */
- (void) muteLocalCamera:(BOOL)mute
               onSuccess:(void(^)(void))onSuccess
               onFailure:(void(^)(NSString* errorMessage))onFailure;

/**
 * 设置基础美颜
 * @param enable 基础美颜开关
 * @param whiteningLevel 美白等级[0-1.0]
 * @param smoothnessLevel 磨皮等级[0-1.0]
 * @note 高级美颜请使用faceBeautyConfigViewController或queenEngine
*/
- (void) setBasicFaceBeauty:(BOOL)enable
             whiteningLevel:(float)whiteningLevel
            smoothnessLevel:(float)smoothnessLevel;

/**
 * 订阅/取消订阅音频流
 * @param sub YES为订阅，NO为取消订阅
 * @param userID 要订阅的用户userID
 * @note 默认订阅全部音频
 */
- (void) subscribeRemoteAudioStream:(BOOL)sub fromUser:(NSString*)userID;

/**
 * 订阅/取消订阅视频流
 * @param sub YES为订阅，NO为取消订阅
 * @param type 视频流类型，具体见AIRBRTCVideoStreamType
 * @param userID 要订阅的用户userID
 */
- (void) subscribeRemoteVideoStream:(BOOL)sub type:(AIRBRTCVideoStreamType)type fromUser:(NSString*)userID;

/**
 * 订阅/取消订阅屏幕分享流
 * @param sub YES为订阅，NO为取消订阅
 * @param userID 要订阅的用户userID
 */
- (void) subscribeRemoteScreenShareStream:(BOOL)sub fromUser:(NSString*)userID;

/**
 * 切换前后摄像头
 */
- (void) toggleLocalCamera;

/**
 * 开启屏幕分享
 * @param appGroup 插件和宿主app约定使用相同的appGroup
 * @return 0: 成功；其他: 失败
 */
- (int)startScreenShare:(NSString*)appGroup;

/**
 * 停止屏幕分享
 * @return 0: 成功；其他: 失败
 */
- (int)stopScreenShare;

/**
 * 开启旁路直播推流
 * @param resolutionType 旁路直播分辨率类型，具体见AIRBRTCBypassLiveResolutionType
 * @note 旧版本中命名为startPublishingBypassLive
 */
- (void) startBypassLiveStreaming:(AIRBRTCBypassLiveResolutionType)resolutionType;

/**
 * 停止旁路直播推流
 * @param destroy YES表示结束旁路直播，NO表示只停止推流不结束直播
 */
- (void) stopBypassLiveStreaming:(BOOL)destroy;

/**
 * 设置预设的旁路推流的布局
 * @param type 预设的布局样式
 * @param userIDs 要展示的用户ID列表:从左上到右下依次排序, 为@""则该位置为空
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) setBypassLiveLayout:(AIRBRTCBypassLiveLayoutType)type
                     userIDs:(NSArray<NSString*>* _Nonnull) userIDs
                   onSuccess:(void(^)(void))onSuccess
                   onFailure:(void(^)(NSString* error))onFailure;

/**
 * 设置自定义的旁路推流的布局（在开启旁路推流成功之后调用）
 * @param peerModels 要展示的视频流，具体见AIRBRTCBypassLiveLayoutPeerVideoModel
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) setCustomBypassLiveLayout:(NSArray<AIRBRTCBypassLiveLayoutPeerVideoModel*>*) peerModels
                         onSuccess:(void(^)(void))onSuccess
                         onFailure:(void(^)(NSString* error))onFailure;

/**
 * 开始录制
 * @param resolutionType 录制分辨率类型，具体见AIRBRTCBypassLiveResolutionType
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) startRecording:(AIRBRTCBypassLiveResolutionType)resolutionType
              onSuccess:(void(^)(void))onSuccess
              onFailure:(void(^)(NSString* error))onFailure;

/**
 * 暂停录制
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) pauseRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;

/**
 * 恢复录制
 * @param resolutionType 录制分辨率类型，具体见AIRBRTCBypassLiveResolutionType
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) resumeRecording:(AIRBRTCBypassLiveResolutionType)resolutionType
               onSuccess:(void(^)(void))onSuccess
               onFailure:(void(^)(NSString* error))onFailure;

/**
 * 停止录制
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) stopRecordingOnSuccess:(void(^)(void))onSuccess onFailure:(void(^)(NSString* error))onFailure;

/**
 * 获取录制的回放视频URL
 * @param conferenceID 回放视频的会议/连麦插件ID
 * @param onSuccess 成功的回调
 * @param onFailure 失败的回调
 */
- (void) getRecordedVideoUrlWithConferenceID:(NSString*)conferenceID
                                   onSuccess:(void(^)(NSString* url))onSuccess
                                   onFailure:(void(^)(NSString* error))onFailure;
                              

@end

NS_ASSUME_NONNULL_END
