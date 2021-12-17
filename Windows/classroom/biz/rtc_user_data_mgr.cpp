#include "rtc_user_data_mgr.h"
#include <mutex>
#include "event/event_manager.h"


std::shared_ptr<RtcUserDataMgr> RtcUserDataMgr::GetInstance() {
  static std::once_flag once_flag;
  static std::shared_ptr<RtcUserDataMgr> instance;
  std::call_once(once_flag, [&]() {
    instance = std::make_shared<RtcUserDataMgr>();
  });
  return instance;
}

RtcUserDataMgr::RtcUserDataMgr() {
  auto event_manager = EventManager::Instance();
  connect(event_manager.get(), &EventManager::SignalVideoMute, this, &RtcUserDataMgr::OnUpdateVideoMute);
  connect(event_manager.get(), &EventManager::SignalAudioMute, this, &RtcUserDataMgr::OnUpdateAudioMute);
}

void RtcUserDataMgr::UpdateVideoMute(const std::string& uid, bool mute) {
  bool need_update = false;
  auto i_find = video_mute_status_.find(uid);
  if (i_find != video_mute_status_.end()) {
    need_update = i_find->second != mute;
    i_find->second = mute;
  } else {
    video_mute_status_[uid] = mute;
    need_update = true;
  }
  if (need_update) {
    emit SignalVideoMute(uid, mute);
  }
}

void RtcUserDataMgr::UpdateAudioMute(const std::string& uid, bool mute) {

  bool need_update = false;
  auto i_find = audio_mute_status_.find(uid);
  if (i_find != audio_mute_status_.end()) {
    need_update = i_find->second != mute;
    i_find->second = mute;
  }
  else {
    audio_mute_status_[uid] = mute;
    need_update = true;
  }
  if (need_update) {
    emit SignalAudioMute(uid, mute);
  }
}

bool RtcUserDataMgr::GetVideoMute(const std::string& uid) {
  auto i_find = video_mute_status_.find(uid);
  if (i_find != video_mute_status_.end()) {
    return i_find->second;
  }
  return false;
}

bool RtcUserDataMgr::GetAudioMute(const std::string& uid) {
  auto i_find = audio_mute_status_.find(uid);
  if (i_find != audio_mute_status_.end()) {
    return i_find->second;
  }
  return false;
}

void RtcUserDataMgr::OnUpdateVideoMute(const std::string& uid, bool mute) {
  UpdateVideoMute(uid, mute);
}

void RtcUserDataMgr::OnUpdateAudioMute(const std::string& uid, bool mute) {
  UpdateAudioMute(uid, mute);
}