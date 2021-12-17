#include "bottom_widget_vm.h"
#include "meta_space.h"
#include "common/http_helper.h"
#include "common/logging.h"
#include "view/view_component_manager.h"
#include "view/interface/i_main_window.h"
#include "QDateTime"
#include "meta/ali_rtc_video_track.h"
#include "meta/ali_rtc_audio_mute_mode.h"
#include "i_rtc.h"
#include "QTimer"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

void BottomWidgetVM::UpdateClassroomId(const std::string& class_room_id) {
  {
    std::lock_guard<std::mutex> lock(param_mutex_);
    param_.class_room_id = class_room_id;

  }

  room_ptr_ = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(class_room_id);
}

std::string BottomWidgetVM::GetClassroomId() {
  return param_.class_room_id;
}

void BottomWidgetVM::UpdateUserId(const std::string& uid) {
  {
    std::lock_guard<std::mutex> lock(param_mutex_);
    param_.uid = uid;
  }
}

void BottomWidgetVM::UpdateClassType(int32_t type) {
  std::lock_guard<std::mutex> lock(param_mutex_);
  param_.type = type;
}
void BottomWidgetVM::UpdateRecordingState(EnumRecordingState state)
{
  std::lock_guard<std::mutex> lock(param_mutex_);
  param_.recording_state = state;
}
void BottomWidgetVM::UpdateClassStarted(bool started)
{
  std::lock_guard<std::mutex> lock(param_mutex_);
  param_.class_started = started;
}
void BottomWidgetVM::UpdateAudoStartRecordingAfterClassStart(bool auto_start)
{
  std::lock_guard<std::mutex> lock(param_mutex_);
  param_.auto_start_recording_after_class_start = auto_start;
}

BottomWidgetModel BottomWidgetVM::GetBottomWidgetModel() {
  std::lock_guard<std::mutex> lock(param_mutex_);
  return param_;
}

void BottomWidgetVM::MuteLocalAudio(bool mute) {
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "MuteLocalMic mute:%d", mute);
    rtc_plugin->MuteLocalMic(mute, alibaba::meta::AliRtcAudioMuteMode::MUTE_LOCAL_AUDIO_MODE_MUTE_ONLY_MIC);
  }
}
