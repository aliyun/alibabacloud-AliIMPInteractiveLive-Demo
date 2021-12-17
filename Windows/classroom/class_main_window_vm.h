#pragma once
#include <QObject>
#include <mutex>
#include <set>
#include "meta/conf_invite_event.h"
#include "meta/conf_user_event.h"
#include "meta/live_common_event_model.h"
#include "meta/rtc_handle_apply_event.h"
#include "meta/rtc_stream_event.h"
#include "meta/conf_user_event.h"
#include "meta/conf_apply_join_channel_event.h"
#include "meta/rtc_layout_model.h"
#include "view/interface/base_interface.h"
#include "classroom_def.h"

enum ClassMainWindowField {
  ClassMainWindowField_ShowMediaControl = 1,
  ClassMainWindowField_ZoomMode = 1 << 1,
  ClassMainWindowField_LiveStarted = 1 << 2,
  ClassMainWindowField_LiveStopped = 1 << 3,
  ClassMainWindowField_RtcInvited = 1 << 6,
  ClassMainWindowField_ConfStartTime = 1 << 7,
  ClassMainWindowField_NetworkLatency = 1 << 8,
  ClassMainWindowField_WhiteBoardLoaded = 1 << 9,
  ClassMainWindowField_UpdateRtcUserList = 1 << 11,
  ClassMainWindowField_ScreenShareStart = 1 << 12,
  ClassMainWindowField_ScreenShareStop = 1 << 13,
  ClassMainWindowFiled_UpdateRtcUserState = 1 << 14,
};

struct RtcUserInfo
{
  std::string uid;
  std::string user_name;
  bool is_teacher = false;
  bool audio_mute = true;
  bool video_mute = true;
  bool active_speaker = false;
};


struct ClassMainWindowModel {
  std::string class_room_id;
  std::string user_id;
  ClassRoleEnum role = ClassRoleEnum::ClassRoleEnum_Teacher;
  ClassTypeEnum type = ClassTypeEnum::ClassTypeEnum_SmallClass;
  MainDisplayMode main_display_mode = MainDisplayMode::MainDisplayMode_BigDisplayMode;
  DisplayContentType small_display_content_type = DisplayContentType::DisplayContentType_RtcPreview;
  DisplayContentType main_display_content_type = DisplayContentType::DisplayContentType_WhiteBoard;

  std::map<std::string, RtcUserInfo> rtc_user_list; // uid -> info

  bool show_media_control = false;
  void* hwnd = nullptr;
  bool zoom_mode = false;
  ::alibaba::rtc::ConfUserModel caller;
  RtcStatusEnum rtc_status = RtcStatusEnum_Out;

  bool is_screen_sharing = false;

  int64_t start_time = 0;
  bool class_started = false;
  double latency = 0.0;
  bool b_preview = false;
  int64_t bitrate = 0;
  std::string title;
  bool exitting = false;
};


class RtcDisplayManager {
public:
  typedef std::function<void(const std::string& user_id, void* hwnd)>
    DisplayFunc;
  typedef std::function<void(const std::string& user_id)> StopPlayFunc;

  RtcDisplayManager() : is_displaying_(false), cur_display_userid_("") {}
  void SetPlayFunction(const DisplayFunc& display_func) {
    display_func_ = display_func;
  }

  void SetStopPlayFunction(const StopPlayFunc& stop_play_func) {
    stop_play_func_ = stop_play_func;
  }

  void AddRtcUserInfo(const std::string& user_id) {
    if (IsEmpty()) {
      cur_display_userid_ = user_id;
      if (display_func_ && display_hwnd_) {
        display_func_(cur_display_userid_, display_hwnd_);
        userid_2_isdisplay_[user_id] = true;
        is_displaying_ = true;
      }
      else {
        userid_2_isdisplay_[user_id] = false;
      }
    }
    else {
      userid_2_isdisplay_[user_id] = false;
    }
  }

  void DelRtcUserInfo(const std::string& user_id) {
    auto user = userid_2_isdisplay_.find(user_id);
    if (user != userid_2_isdisplay_.end()) {
      if (user->second == true) {
        if (stop_play_func_) stop_play_func_(user->first);
        is_displaying_ = false;
      }
      userid_2_isdisplay_.erase(user);
      for (auto user2 : userid_2_isdisplay_) {
        if (display_func_ && display_hwnd_) {
          display_func_(user2.first, display_hwnd_);
          is_displaying_ = true;
          return;
        }
      }
    }
  }

  void UpdateHwnd(void* hwnd) {
    if (is_displaying_) {
      display_hwnd_ = hwnd;
      if (display_func_ && stop_play_func_) {
        stop_play_func_(cur_display_userid_);
        display_func_(cur_display_userid_, display_hwnd_);
      }
    }
  }

  void ShowStream(const std::string& user_id) {
    auto user = userid_2_isdisplay_.find(user_id);
    if (user != userid_2_isdisplay_.end()) {
      if (user->second == true) {
        return;
      }
      else {
        if (display_func_ && display_hwnd_) {
          if (IsDisplaying()) {
            stop_play_func_(cur_display_userid_);
            userid_2_isdisplay_[cur_display_userid_] = false;
            display_func_(user->first, display_hwnd_);
            cur_display_userid_ = user->first;
          }
          else {
            display_func_(user->first, display_hwnd_);
            cur_display_userid_ = user->first;
            is_displaying_ = true;
          }
        }
      }
    }
  }

  void StopStream(const std::string& user_id) {
    auto user = userid_2_isdisplay_.find(user_id);
    if (user != userid_2_isdisplay_.end()) {
      if (user->second == true) {
        if (stop_play_func_) {
          stop_play_func_(user_id);
          is_displaying_ = false;
          userid_2_isdisplay_[user_id] = false;
          cur_display_userid_ = "";
        }
      }
    }
  }

private:
  bool IsEmpty() { return userid_2_isdisplay_.empty(); }
  bool IsDisplaying() { return is_displaying_; }

private:
  void* display_hwnd_;
  std::map<std::string, bool> userid_2_isdisplay_;
  std::string cur_display_userid_;
  bool is_displaying_;
  DisplayFunc display_func_;
  StopPlayFunc stop_play_func_;
};

class ClassMainWindowVM : public QObject {
  Q_OBJECT
public:
  void UpdateClassroomId(const std::string& class_room_id);
  void UpdateUserId(const std::string& user_id);
  void UpdateRole(ClassRoleEnum role);
  void UpdateType(ClassTypeEnum type);
  void UpdateCaller(::alibaba::rtc::ConfUserModel caller);
  void UpdateRtcStatus(RtcStatusEnum rtc_status);
  void UpdateMainDisplayMode(MainDisplayMode mode);
  void UpdateScreenShared(bool is_sharing);
  void UpdateRtcUserVideoMute(const std::string& user_id, const bool mute);
  void UpdateRtcUserAudioMute(const std::string& user_id, const bool mute);
  ClassMainWindowModel GetClassMainWindowModel();
  std::string GetClassroomId();
  std::string GetUserId();
  std::string GetTeacherUserId();
  ::alibaba::rtc::ConfUserModel GetCaller();
  RtcStatusEnum GetRtcStatus();

  bool StudentOnRtc();

  void UpdateSmallDisplayContentType(DisplayContentType type);
  void UpdateMainDisplayContentType(DisplayContentType type);

  void CreateWhiteBoard(void*);
  void CloseWhiteBoard();
  void UpdateWhiteBoard(int32_t width, int32_t height);
  void InitRtcListener();
  void UpdateZoomMode(bool zoom);
  void StartRtcPreview(void* hwnd);
  void StopRtcPreview();
  void TryStartLivePlay(void* hwnd);
  void StartLivePlay(void* hwnd);
  void StopLivePlay();
  void ShowStream(const std::string& user_id, void* hwnd);
  void StopStream(const std::string& user_id);
  void StartShowScreenStream(void* hwnd);
  void StopShowScreenStream();
  void ApproveLinkMic();
  void RejectLinkMic();
  void JoinChannel();
  void LeaveRTC();
  void UpdateConfInfo();
  void LeaveRoom();
  void StopClass();
  void MuteLocalVideo(bool mute);
  void SetLayout(const std::vector<std::string>& user_ids, const alibaba::meta::RtcLayoutModel& model);
  void SetExitting();
private slots:
  void OnRtcStreamIn(const alibaba::meta::RtcStreamEvent& event);
  void OnRtcStreamOut(const std::string& uid);
  void OnRtcApplyJoinChannel(const alibaba::meta::ConfApplyJoinChannelEvent& event);
  void OnRtcHandleApply(const alibaba::meta::RtcHandleApplyEvent& event);
  void OnRtcUserInvited(const alibaba::meta::ConfInviteEvent& event);
  void OnRtcJoinRtcSuccess();
  void OnRtcLeaveUser(const alibaba::meta::ConfUserEvent& event);
  void OnRtcRemoteJoinSuccess(const alibaba::meta::ConfUserEvent& event);
  void OnRtcKickUser(const alibaba::meta::ConfUserEvent& event);
  void OnRtcLatency(double latency, int64_t bitrate);
  void OnSignalStudentShareScreen(const std::string& conf_id, bool open);
  void OnSignalTeacherShareScreen(bool open);
  void OnSignalTeacherShareSystemAudio(bool open);
  void OnRtcVideoMute(const std::string & uid, bool is_mute);
  void OnRtcAudioMute(const std::string & uid, bool is_mute);
  void OnRtcActiveSpeaker(const std::string& uid);
  void OnRtcFirstRemoteVideoFrameDrawn(const std::string & uid, int32_t video_track, int32_t width, int32_t height, int32_t elapsed);
  void OnClassStart(const std::string& class_id);
  void OnClassEnd(const std::string& class_id);
signals:
  void SignalUpdateVM(int32_t field);

private:
  void UpdateModelAndNotifyViewShowMediaControl(const bool show);
  void NotifyViewUpdateRtcUserList();
  void NotifyViewUpdateRtcUserState();
  void NotifyViewScreenShareStart();
  void NotifyViewScreenShareStop();
  void AddRTCUser(
    const std::string& uid,
    const std::string& user_name,
    const bool is_teacher,
    const bool is_audio_mute,
    const bool is_video_mute
  );
  void RemoveRTCUsers(const std::vector<std::string> & uids);
  void RemoveAllRTCUser();
  void OnRtcUserListChange();
  void SetScreenShareLayout();


private:
  std::mutex param_mutex_;
  ClassMainWindowModel param_;
  RtcDisplayManager rtc_display_manager_;
};
