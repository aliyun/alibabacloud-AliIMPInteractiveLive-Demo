#pragma once
#include <QObject>
#include "meta_space.h"
#include "rtc_event_listener.h"
#include "live_event_listener.h"
#include "room_event_listener.h"
#include "chat_event_listener.h"
#include "player_media_event_listener.h"
#include "conf_user_event.h"
#include "event/event_param.h"
#include "meta/class_status_event_model.h"
#include <mutex>
#include <functional>
#include <list>

class EventManager : public QObject,
  public alibaba::meta::RoomEventListener,
  public alibaba::meta::ChatEventListener,
  public alibaba::meta::RtcEventListener,
  public alibaba::meta::LiveEventListener,
  public alibaba::meta::PlayerMediaEventListener,
  public std::enable_shared_from_this<EventManager> {

  Q_OBJECT
public:
  static std::shared_ptr<EventManager> Instance();
  void SetRoomId(const std::string& room_id);
  void ClearRoomId(const std::string& room_id);
  void PostUITask(const std::function<void()>& task);
private slots:
  void OnProcessUITask();
signals:
  void SignalRoomInOut(const alibaba::meta::RoomInOutEventModel& event);
  void SignalRoomUserKicked(const alibaba::meta::KickUserEventModel& event);
  void SignalRtcJoinRtcSuccess();
  void SignalRtcUserInvited(const alibaba::meta::ConfInviteEvent& event);
  void SignalRtcHandleApply(const alibaba::meta::RtcHandleApplyEvent& event);
  void SignalRtcRemoteJoinSuccess(const alibaba::meta::ConfUserEvent& event);
  void SignalRtcRemoteJoinFail(const alibaba::meta::ConfUserEvent& event);
  void SignalRtcApplyJoinChannel(
    const alibaba::meta::ConfApplyJoinChannelEvent& event);
  void SignalRtcLeaveUser(const alibaba::meta::ConfUserEvent& event);
  void SignalRtcStreamIn(const alibaba::meta::RtcStreamEvent& event);
  void SignalRtcKickUser(const alibaba::meta::ConfUserEvent& event);
  void SignalLiveCreated(const alibaba::meta::LiveCommonEventModel& event);
  void SignalLiveStarted(const alibaba::meta::LiveCommonEventModel& event);
  void SignalLiveStopped(const alibaba::meta::LiveCommonEventModel& event);
  void SignalNetwordLatency(double latency, int64_t kbps);
  void SignalNetworkQuality(const std::string& uid, int32_t up_quality, int32_t down_quality);
  void SignalAudioMute(const std::string& uid, bool mute);
  void SignalVideoMute(const std::string& uid, bool mute);
  void SignalRtcActiveSpeaker(const std::string& uid);
  void SignalPlayerVideoFirstFrameRender();
  void SignalRtcFirstRemoteVideoFrameDrawn(const std::string & uid, int32_t video_track, int32_t width, int32_t height, int32_t elapsed);
  void SignalRtcFirstLocalVideoFrameDrawn(int32_t width, int32_t height, int32_t elapsed);

  //学生端收到屏幕分享信息
  void SignalStudentShareScreen(const std::string& conf_id, bool open);
  //老师端发端屏幕分享信息
  void SignalTeacherShareScreen(bool open);
  void SignalTeacherShareSystemAudio(bool open);
  void SignalMsgArrived(const ChatMsg&);
  void SignalMuteUserArrived(const MuteUserMsg&);
  void SignalMuteAllArrived(const MuteAllMsg&);
  void SignalRtcMuteMessage(const alibaba::meta::MuteMessageEvent& event);
  void SignalLiveTime(QString time);
  void SignalClassStart(const std::string& class_id);
  void SignalClassEnd(const std::string& class_id);
  void SignalProcessUITask();
  void SignalRtcStatus(const alibaba::meta::AliRtcStats & event);
private:
  EventManager();
  ~EventManager();
  static void Destory(EventManager* ptr) { delete ptr; };
  // From RoomEventListener
  virtual void OnRoomInOut(
    const alibaba::meta::RoomInOutEventModel& event) override;

  virtual void OnRoomNoticeChanged(const std::string& event) override;

  virtual void OnRoomTitleChanged(const std::string& event) override;

  virtual void OnRoomUserKicked(
    const alibaba::meta::KickUserEventModel& event) override;

  // From ChatEventListenerImpl
  virtual void OnCommentReceived(
    const alibaba::meta::CommentReceivedEventModel& event) override;

  virtual void OnMuteAllComment(
    const alibaba::meta::MuteAllCommentEventModel& event) override;

  virtual void OnMuteUserComment(
    const alibaba::meta::MuteUserCommentEventModel& event) override;

  virtual void OnLikeCountChange(const alibaba::meta::LikeEventModel & event) override;

  // From RtcEventListener
  virtual void OnRtcJoinRtcSuccess() override;

  virtual void OnRtcJoinRtcFail(const ::alibaba::dps::DPSError & error) override;

  virtual void OnRtcStreamIn(
    const alibaba::meta::RtcStreamEvent& event) override;

  virtual void OnRtcStreamUpdate(
    const alibaba::meta::RtcStreamEvent& event) override;

  virtual void OnRtcStreamOut(const std::string& uid) override;

  virtual void OnRtcRemoteJoinSuccess(
    const alibaba::meta::ConfUserEvent& event) override;

  virtual void OnRtcRemoteJoinFail(
    const alibaba::meta::ConfUserEvent& event) override;

  virtual void OnRtcConfUpdate(const alibaba::meta::ConfEvent& event) override;

  virtual void OnRtcRingStopped(
    const alibaba::meta::ConfStopRingEvent& event) override;

  virtual void OnRtcUserInvited(
    const alibaba::meta::ConfInviteEvent& event) override;

  virtual void OnRtcKickUser(
    const alibaba::meta::ConfUserEvent& event) override;

  virtual void OnRtcLeaveUser(
    const alibaba::meta::ConfUserEvent& event) override;

  virtual void OnRtcStart(const alibaba::meta::ConfEvent& event) override;

  virtual void OnRtcEnd(const alibaba::meta::ConfEvent& event) override;

  virtual void OnRtcCommand(
    const alibaba::meta::ConfCommandEvent& event) override;

  virtual void OnRtcApplyJoinChannel(
    const alibaba::meta::ConfApplyJoinChannelEvent& event) override;

  virtual void OnRtcHandleApply(
    const alibaba::meta::RtcHandleApplyEvent& event) override;

  virtual void OnRtcAudioVolumeChange(
    const std::vector<alibaba::meta::RtcUserVolumeInfo>& event,
    int32_t total_volume) override;

  virtual void OnRtcStats(const alibaba::meta::AliRtcStats & event) override;

  virtual void OnRtcNetworkQualityChanged(const std::string & uid,
    alibaba::meta::AliRtcNetworkQuality up_quality, alibaba::meta::AliRtcNetworkQuality down_quality) override;

  virtual void OnUserAudioMuted(const std::string & uid, bool is_mute) override;

  virtual void OnUserVideoMuted(const std::string & uid, bool is_mute) override;

  virtual void OnActiveSpeaker(const std::string & uid) override;

  /**
   * 直播创建
   */
  virtual void OnLiveCreated(
    const alibaba::meta::LiveCommonEventModel& event) override;

  /**
   * 直播开始
   */
  virtual void OnLiveStarted(
    const alibaba::meta::LiveCommonEventModel& event) override;

  /**
   * 直播结束
   */
  virtual void OnLiveStopped(
    const alibaba::meta::LiveCommonEventModel& event) override;

  virtual void OnShareScreen(const alibaba::meta::ShareScreenMessageModel& event) override;

  virtual void OnPlayerVideoFirstFrameRender() override;

  /**
   * 远端用户的第一帧视频帧显示时触发这个消息
   */
  virtual void OnRtcFirstRemoteVideoFrameDrawn(const std::string & uid, alibaba::meta::AliRtcVideoTrack video_track, int32_t width, int32_t height, int32_t elapsed) override;

  /**
   * 预览开始显示第一帧视频帧时触发这个消息
   */
  virtual void OnRtcFirstLocalVideoFrameDrawn(int32_t width, int32_t height, int32_t elapsed) override;

  /**
   * RTC禁音消息
   */
  virtual void OnRtcMuteMessage(const alibaba::meta::MuteMessageEvent& event) override;

  virtual void OnClassStatusChanged(const alibaba::meta::ClassStatusEventModel & event) override;

  virtual void OnRtcWarning(int32_t warning, const std::string & message) override;

  virtual void OnRtcError(int32_t error, const std::string & message) override;

  virtual void OnPlayerEvent(int32_t event) override;

  virtual void OnPlayerError(int32_t error_code, const std::string & error_message) override;

  virtual void OnCustomMessageReceived(const std::string & event) override;
private:
  std::list<std::function<void()>> task_list_;
  std::mutex task_list_mutex_;
};
