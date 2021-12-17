#include "bottom_widget_teacher_vm.h"
#include "meta_space.h"
#include "QApplication"
#include "common/logging.h"
#include "event/event_manager.h"
#include "common/common_helper.h"
#include "scheme_login.h"


using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;


void BottomWidgetTeacherVM::StartClass() {
  if (/*param_.type == ClassTypeEnum_BigClass && */room_ptr_) {
    auto model = GetBottomWidgetModel();
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    if (!rtc_plugin) {
      LogWithTag(ClassroomTagLivePlay, LOG_ERROR, "StartClass error. rtc_plugin is null");
      return;
    }
    std::string class_id = SchemeLogin::Instance()->GetSchemeInfo().class_id;
    if (!class_id.empty()) {
      LogWithTag(ClassroomTagLivePlay, LOG_INFO, "SceneStartClass start. class_id:%s", class_id.c_str());
      rtc_plugin->SceneStartClass(class_id, [](const alibaba::sceneclass::StartClassRsp& rsp) {
        LogWithTag(ClassroomTagLivePlay, LOG_INFO, "SceneStartClass success.");
      }, [](const alibaba::dps::DPSError& error) {
        classroom::LogError(ClassroomTagLivePlay, "SceneStartClass error, ", error);
      });
    }
    

    LogWithTag(ClassroomTagLivePlay, LOG_INFO, "JoinRtc start. uid:%s", model.uid.c_str());
    rtc_plugin->JoinRtc(model.uid, [this]() {
      LogWithTag(ClassroomTagLivePlay, LOG_INFO, "JoinRtc success.");
      std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
      LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StartRoadPublish start");
      rtc_plugin->StartRoadPublish([this]() {
        LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StartRoadPublish success");
        auto model = GetBottomWidgetModel();
        UpdateClassStarted(true);
        // 自动录制
        if (model.auto_start_recording_after_class_start) {
          StartRecording();
        }  
        EventManager::Instance()->PostUITask([this]() {
          emit SignalUpdateVM(BottomWidgetFieldClassStarted);
        });
      }, [](const DPSError& err) {
        classroom::LogError(ClassroomTagLivePlay, "StartRoadPublish error, ", err);
      });
    }, [](const DPSError& error) {
      classroom::LogError(ClassroomTagLivePlay,"JoinRtc error, ", error);
    });
  }
}

void BottomWidgetTeacherVM::StopClass() {
  if (room_ptr_) {

    // 关闭录制
    UpdateClassStarted(false);
    StopRecording();

    // 老师下麦时, 同时要销毁旁路推流的直播实例
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    std::shared_ptr<IWhiteBoard> white_plugin = std::dynamic_pointer_cast<IWhiteBoard>(room_ptr_->GetPlugin(PluginWhiteBoard));
    rtc_plugin->StopRoadPublish([this, rtc_plugin, white_plugin](){
      EventManager::Instance()->PostUITask([this, rtc_plugin, white_plugin]() {
        white_plugin->CloseWhiteBoard();
        std::string class_id = SchemeLogin::Instance()->GetSchemeInfo().class_id;
        if (!class_id.empty()) {
          LogWithTag(ClassroomTagLivePlay, LOG_INFO, "SceneStopClass start. class_id:%s", class_id.c_str());
          rtc_plugin->SceneStopClass(class_id, [](const alibaba::sceneclass::StopClassRsp& rsp) {
            LogWithTag(ClassroomTagLivePlay, LOG_INFO, "SceneStopClass success.");
          }, [](const alibaba::dps::DPSError& error) {
            classroom::LogError(ClassroomTagLivePlay, "SceneStopClass error, ", error);
          });
        }
        
        rtc_plugin->LeaveRtc(true, []() {}, [](const DPSError& error) {
          classroom::LogError(ClassroomTagLivePlay,"LeaveRtc error, ", error);
        });
        ForceExit(2000);
        room_ptr_->LeaveRoom([]() {
          QApplication::quit();
        }, [](const DPSError& error_msg) {
         classroom::LogError(ClassroomTagLivePlay, "LeaveRoom error, ", error_msg);
        });
      });
    }, [](const DPSError& error_msg){
      classroom::LogError(ClassroomTagLivePlay, "StopRoadPublish error, ", error_msg);
    });

  }
}

bool BottomWidgetTeacherVM::StartRecording() 
{
  bool ret_ok = false;

  auto model = GetBottomWidgetModel();
  if (model.recording_state == EnumRecordingState::EnumRecordingStateStopped 
    || model.recording_state == EnumRecordingState::EnumRecordingStatePaused) {

    if (model.class_started) {

      if (model.recording_state == EnumRecordingStateStopped) {
        RecordOpType op_type = RecordOpType::RecordOpTypeStart;
        RtcRecord(op_type);
        WhiteboardRecord(op_type);
        UpdateRecordingState(EnumRecordingState::EnumRecordingStateStarted);
        ret_ok = true;
      }
      else if (model.recording_state == EnumRecordingStatePaused){
        RecordOpType op_type = RecordOpType::RecordOpTypeResume;
        RtcRecord(op_type);
        WhiteboardRecord(op_type);
        UpdateRecordingState(EnumRecordingState::EnumRecordingStateStarted);
        ret_ok = true;
      }
    }
  }

  NotifyRecodingState();

  return ret_ok;
}

void BottomWidgetTeacherVM::EnableAutoRecord(bool on)
{
  UpdateAudoStartRecordingAfterClassStart(on);
}
bool BottomWidgetTeacherVM::StopRecording()
{
  bool ret_ok = false;

  auto model = GetBottomWidgetModel();
  if (model.recording_state == EnumRecordingState::EnumRecordingStateStarted) {
  
    if (model.class_started) {
      RecordOpType op_type = RecordOpType::RecordOpTypePause;
      RtcRecord(op_type);
      WhiteboardRecord(op_type);
      UpdateRecordingState(EnumRecordingState::EnumRecordingStatePaused);
      ret_ok = true;
    }
    else {
      RecordOpType op_type = RecordOpType::RecordOpTypeStop;
      RtcRecord(op_type);
      WhiteboardRecord(op_type);
      UpdateRecordingState(EnumRecordingState::EnumRecordingStateStopped);
      ret_ok = true;
    }
  }

  NotifyRecodingState();

  return ret_ok;
}

std::vector<alibaba::meta::RtcScreenSource>
BottomWidgetTeacherVM::GetShareScreenSource(alibaba::meta::AliRtcScreenShareType source_type) { 
  std::vector<alibaba::meta::RtcScreenSource> source_list;
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    source_list = rtc_plugin->GetScreenShareSourceInfo(source_type);
  }
  return source_list;
}

void BottomWidgetTeacherVM::StartDesktopShareScreen(int desktop_id) {
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    alibaba::meta::AliRtcScreenShareConfig config;
    rtc_plugin->StartDesktopShareScreen(desktop_id, config, 
      [this] { 
        LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "ShareScreen success");
          share_screen_open_ = true;
          EventManager::Instance()->PostUITask([this]() {
                emit SignalUpdateVM(BottomWidgetTeacherStartDesktopShareScreen);
                emit EventManager::Instance()->SignalTeacherShareScreen(true);
              });
    },
      [this](const DPSError& error_msg) {
          share_screen_open_ = false;
          last_error_code_ = error_msg.code;
          last_error_msg_ = error_msg.reason;
          emit SignalUpdateVM(BottomWidgetTeacherStartDesktopShareScreen);
          classroom::LogError(ClassroomTagLocalMedia, "ShareScreen error", error_msg);
    });
  }
}

void BottomWidgetTeacherVM::StartWindowShareScreen(int window_id) {
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    alibaba::meta::AliRtcScreenShareConfig config;

    rtc_plugin->StartWindowShareScreen(
        window_id, config,
        [this] {
          LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "ShareScreen success");
          share_screen_open_ = true;
          EventManager::Instance()->PostUITask([this]() {
                emit SignalUpdateVM(BottomWidgetTeacherStartDesktopShareScreen);
                emit EventManager::Instance()->SignalTeacherShareScreen(true);
              });
        },
        [this](const DPSError& error_msg) {
          share_screen_open_ = false;
          last_error_code_ = error_msg.code;
          last_error_msg_ = error_msg.reason;
          emit SignalUpdateVM(BottomWidgetTeacherStartDesktopShareScreen);
          classroom::LogError(ClassroomTagLocalMedia, "ShareScreen error",
                              error_msg);
        });
  }
}

void BottomWidgetTeacherVM::StopShareScreen() {
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    rtc_plugin->StopShareScreen(
        [this] {
          LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "StopShareScreen success");
          share_screen_open_ = false;
          EventManager::Instance()->PostUITask([this](){
             emit SignalUpdateVM(BottomWidgetTeacherStopShareScreen);
             emit EventManager::Instance()->SignalTeacherShareScreen(false);
          });
        },
        [this](const DPSError& error_msg) {
          last_error_code_ = error_msg.code;
          last_error_msg_ = error_msg.reason;
          emit SignalUpdateVM(BottomWidgetTeacherStopShareScreen);
          classroom::LogError(ClassroomTagLocalMedia, "StopShareScreen error", error_msg);
        });
  }
}

bool BottomWidgetTeacherVM::GetShareScreenOpen() { 
  return share_screen_open_; 
}

int BottomWidgetTeacherVM::GetLastErrorCode() { 
  return last_error_code_;
}
std::string BottomWidgetTeacherVM::GetLastErrorMsg() {
  return last_error_msg_;
}
std::string BottomWidgetTeacherVM::GetWhiteboardID()
{
  std::string whiteboard_id;
  bool ok = false;
  if (room_ptr_) {
    auto instance_list = room_ptr_->GetRoomDetail().room_info.plugin_instance_info.instance_list;
    for (auto info : instance_list) {
      if (info.plugin_id == "wb") {
        whiteboard_id = info.instance_id;
        ok = true;
        break;
      }
    }
  }

  // ok
  return whiteboard_id;
}

void BottomWidgetTeacherVM::WhiteboardRecord(RecordOpType op_type)
{
  if (!room_ptr_) return;

  const std::string whiteboard_id = GetWhiteboardID();

  if (whiteboard_id.empty()) {
    LogWithTag(ClassroomTagWhiteboard, LOG_ERROR, "Whiteboard id is empty !!!");
    return;
  }

  std::shared_ptr<IWhiteBoard> wb_plugin =
    std::dynamic_pointer_cast<IWhiteBoard>(
      room_ptr_->GetPlugin(PluginWhiteBoard));

  switch (op_type) {
    case RecordOpType::RecordOpTypeStart:
      wb_plugin->StartRecord(
          whiteboard_id,
          []() {
            LogWithTag(ClassroomTagWhiteboard, LOG_INFO,
                       "Whiteboard StartRecord success");
          },
          [](const ::alibaba::dps::DPSError& error_msg) {
            classroom::LogError(ClassroomTagWhiteboard,
                                "Whiteboard StartRecord error", error_msg);
          });
      break;
    case RecordOpType::RecordOpTypeStop:
      wb_plugin->StopRecord(
          whiteboard_id,
          []() {
            LogWithTag(ClassroomTagWhiteboard, LOG_INFO,
                       "Whiteboard StopRecord success");
          },
          [](const ::alibaba::dps::DPSError& error_msg) {
            classroom::LogError(ClassroomTagWhiteboard,
                                "Whiteboard StopRecord error", error_msg);
          });
      break;
    case RecordOpType::RecordOpTypePause:
      wb_plugin->PauseRecord(
          whiteboard_id,
          []() {
            LogWithTag(ClassroomTagWhiteboard, LOG_INFO,
                       "Whiteboard PauseRecord success");
          },
          [](const ::alibaba::dps::DPSError& error_msg) {
            classroom::LogError(ClassroomTagWhiteboard,
                                "Whiteboard PauseRecord error", error_msg);
          });
      break;
    case RecordOpType::RecordOpTypeResume:
      wb_plugin->ResumeRecord(
          whiteboard_id,
          []() {
            LogWithTag(ClassroomTagWhiteboard, LOG_INFO,
                       "Whiteboard ResumeRecord success");
          },
          [](const ::alibaba::dps::DPSError& error_msg) {
            classroom::LogError(ClassroomTagWhiteboard,
                                "Whiteboard ResumeRecord error", error_msg);
          });
      break;
    default:
      LogWithTag(ClassroomTagWhiteboard, LOG_ERROR, "WhiteboardRecord Unknown Operator !!!");
      break;
  }

}
void BottomWidgetTeacherVM::RtcRecord(RecordOpType op_type)
{
  if (!room_ptr_) return;

  std::shared_ptr<IRtc> rtc_plugin =
    std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));

  bool start_record = true;

  switch (op_type) {
  case RecordOpType::RecordOpTypeStart:
  case RecordOpType::RecordOpTypeResume:
    start_record = true;
    break;
  case RecordOpType::RecordOpTypePause:
  case RecordOpType::RecordOpTypeStop:
    start_record = false;
    break;
  default:
    LogWithTag(ClassroomTagWhiteboard, LOG_ERROR, "RtcRecord Unknown Operator !!!");
    return;
  }

  if (start_record) {
    rtc_plugin->StartRecord([]() {},
                            [](const ::alibaba::dps::DPSError& error_msg) {
                              classroom::LogError(ClassroomTagRTC,
                                                  "Rtc StartRecord error",
                                                  error_msg);
                            });
  } else {
    rtc_plugin->StopRecord([]() {},
                            [](const ::alibaba::dps::DPSError& error_msg) {
                              classroom::LogError(ClassroomTagRTC,
                                                  "Rtc StopRecord error",
                                                  error_msg);
                            });
  }
}
void BottomWidgetTeacherVM::NotifyRecodingState()
{
  Q_EMIT SignalUpdateVM(BottomWidgetTeacherRecodingState);
}