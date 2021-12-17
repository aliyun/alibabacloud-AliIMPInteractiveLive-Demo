#include "bottom_widget_student_vm.h"
#include "QApplication"
#include "common/common_helper.h"
#include "common/logging.h"
#include "i_rtc.h"
#include "i_white_board.h"
#include "meta_space.h"
#include "view/interface/i_toast_widget.h"
#include "view/view_component_manager.h"
#include "event/event_manager.h"

using namespace alibaba::meta_space;
using namespace alibaba::dps;

BottomWidgetStudentVM::BottomWidgetStudentVM() {
  auto listener_ptr = EventManager::Instance();
  connect(listener_ptr.get(), &EventManager::SignalRtcMuteMessage, this,
          &BottomWidgetStudentVM::OnRtcMuteMessage, Qt::DirectConnection);
}

BottomWidgetStudentVM::~BottomWidgetStudentVM() {}
void BottomWidgetStudentVM::ReqLinkMic(std::function<void()> on_success,
                                       std::function<void()> on_failure) {
  if (room_ptr_) {
    auto rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    if (rtc_plugin) {
      rtc_plugin->ApplyJoinRtc(true, [on_success]() { 
                                 on_success(); 
                                 LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApplyJoinRtc true success");
                               },
                               [on_failure](const DPSError& err) {
                                 on_failure();
                                 classroom::LogError(ClassroomTagLinkMic,"ApplyJoinRtc true error", err);
                               });
    }
  }
}

void BottomWidgetStudentVM::ReqQuitLinkMic(std::function<void()> on_success,
                                           std::function<void()> on_failure) {
  std::shared_ptr<IRtc> rtc_plugin;
  if (room_ptr_) {
    rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  }
  if (!is_link_mic_) {
    if (rtc_plugin) {
      rtc_plugin->ApplyJoinRtc(false,
                               [on_success]() {
                                 on_success();
                                 LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApplyJoinRtc false success");
                               },
                               [on_failure](const DPSError& err) {
                                 on_failure();
                                 classroom::LogError(ClassroomTagLinkMic,"ApplyJoinRtc false error", err);
                               });
    }
  } else {
    if (rtc_plugin) {
      rtc_plugin->LeaveRtc(false, [on_success]() { on_success(); },
                           [on_failure](const DPSError& err) {
                             on_failure();
                             classroom::LogError(ClassroomTagLinkMic, "LeaveRtc error", err);
                           });
    }
  }
}

void BottomWidgetStudentVM::QuitClass() {
  std::shared_ptr<IRtc> rtc_plugin;
  std::shared_ptr<IWhiteBoard> white_board_plugin;
  if (room_ptr_) {
    rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(
        room_ptr_->GetPlugin(PluginWhiteBoard));
    if (white_board_plugin) {
      white_board_plugin->CloseWhiteBoard();
    }
    if (rtc_plugin) {
      rtc_plugin->LeaveRtc(false, []() {},
                           [](const DPSError& err) {
                             classroom::LogError("", "student leave rtc,", err);
                           });
    }
    ForceExit(2000);
    room_ptr_->LeaveRoom([this]() { QApplication::quit(); },
                         [](const DPSError& err) {
                           classroom::LogError("", "student leave room,", err);
                         });
  }
}

void BottomWidgetStudentVM::UpdateLinkMicStatus(bool is_link_mic) {
  is_link_mic_ = is_link_mic;
}

void BottomWidgetStudentVM::OnRtcMuteMessage(const alibaba::meta::MuteMessageEvent& event) {
  auto model = GetBottomWidgetModel();

  for (auto user_id : event.user_list) {
    if (user_id == model.uid) {
      if (event.open) 
        is_mute_rtc_ = false;
      else
        is_mute_rtc_ = true;
      emit SendRtcMuteToClient(is_mute_rtc_);
    }
  }
}
