#include "class_main_window_vm.h"
#include "api/base_api.h"
#include "common/http_helper.h"
#include "event/event_manager.h"
#include "meta/conf_invite_event.h"
#include "meta/rtc_event_listener.h"
#include "meta_space.h"
#include "view/interface/i_main_window.h"
#include "view/view_component_manager.h"
#include "i_rtc.h"
#include "i_white_board.h"
#include "i_player.h"
#include "common/logging.h"
#include <iosfwd>
#include "QApplication"
#include <QTimer>
#include "common/common_helper.h"
#include <sstream>
#include "scheme_login.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

void ClassMainWindowVM::UpdateClassroomId(const std::string& class_room_id) {
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.class_room_id = class_room_id;
  }

  //设置全局的EventManager
  EventManager::Instance()->SetRoomId(class_room_id);
  InitRtcListener();

  rtc_display_manager_.SetPlayFunction(
    [this](const std::string& user_id, void* hwnd) {


  });

  UpdateConfInfo();
}

ClassMainWindowModel ClassMainWindowVM::GetClassMainWindowModel() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_;
}

std::string ClassMainWindowVM::GetClassroomId() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_.class_room_id;
}

std::string ClassMainWindowVM::GetUserId() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_.user_id;
}

::alibaba::rtc::ConfUserModel ClassMainWindowVM::GetCaller() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_.caller;
}

std::string ClassMainWindowVM::GetTeacherUserId() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    auto room_detail = room_ptr->GetRoomDetail();
    return room_detail.room_info.owner_id;
  }
  return "";
}

void ClassMainWindowVM::UpdateUserId(const std::string& user_id) {
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.user_id = user_id;
}

void ClassMainWindowVM::UpdateRole(ClassRoleEnum role) {
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.role = role;
}

void ClassMainWindowVM::UpdateType(ClassTypeEnum type) {
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.type = type;
}

void ClassMainWindowVM::UpdateCaller(::alibaba::rtc::ConfUserModel caller) {
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.caller = caller;
}

void ClassMainWindowVM::UpdateRtcStatus(RtcStatusEnum rtc_status) {
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.rtc_status = rtc_status;
}
void ClassMainWindowVM::UpdateMainDisplayMode(MainDisplayMode mode)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.main_display_mode = mode;
}

bool ClassMainWindowVM::StudentOnRtc() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  for (auto itor : param_.rtc_user_list) {
    if (!itor.second.is_teacher && itor.second.uid == param_.user_id) {
      return true;
    }
  }
  return false;
}

void ClassMainWindowVM::UpdateScreenShared(bool is_sharing)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.is_screen_sharing = is_sharing;
}
RtcStatusEnum ClassMainWindowVM::GetRtcStatus() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_.rtc_status;
}

void ClassMainWindowVM::TryStartLivePlay(void* hwnd) {
  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model.class_room_id);
  if (room_ptr) {
    // 判断是否有直播
    auto room_detail = room_ptr->GetRoomDetail();
    bool has_live_info = false;
    for (auto instance :
      room_detail.room_info.plugin_instance_info.instance_list) {
      if (instance.plugin_id == "live") has_live_info = true;
    }
    if (has_live_info) {
      // 老师正在上课
      std::shared_ptr<IPlayer> player_plugin = std::dynamic_pointer_cast<IPlayer>(room_ptr->GetPlugin(PluginPlayer));
      if (player_plugin) {
        LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StartPlay start");
        player_plugin->StartPlay(
            hwnd,
            []() {
              LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StartPlay success");
            },
            [](const DPSError& error_msg) {
              classroom::LogError(ClassroomTagLivePlay, "StartPlay error", error_msg);
            });
      }
    }
    else {
      // 老师暂未上课
      // todo
      LogWithTag(ClassroomTagLivePlay, LOG_INFO, "no live info");
    }
  }
}

void ClassMainWindowVM::JoinChannel() {
  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
  rtc_plugin->JoinRtc(model.user_id, [this]() {

  }, [](const DPSError& error) {
    classroom::LogError("", "join rtc error, ", error);
  });
}

void ClassMainWindowVM::StartRtcPreview(void* hwnd) {
  if (param_.b_preview && hwnd == param_.hwnd) return;
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "StartRtcPreview");
    rtc_plugin->StartRtcPreview(hwnd);
    param_.hwnd = hwnd;
    param_.b_preview = true;
  }
}


void ClassMainWindowVM::StopRtcPreview() {
  if (param_.b_preview == false) return;
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "StopPreview");
    rtc_plugin->StopPreview();
    param_.b_preview = false;
  }
}

void ClassMainWindowVM::StartLivePlay(void* hwnd) {
  if (param_.exitting) {
    return;
  }
  ClassMainWindowModel model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    // 学生并且没有在连麦中
    auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
        model.class_room_id);
    if (room_ptr) {
      std::shared_ptr<IPlayer> player_plugin = std::dynamic_pointer_cast<IPlayer>(room_ptr->GetPlugin(PluginPlayer));
      player_plugin->StartPlay(hwnd, []() {}, [](const DPSError& err) {
        classroom::LogError("", "start live play error, ", err);
      });
    }
  }
}

void ClassMainWindowVM::StopLivePlay() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    // 学生并且没有在连麦中
    auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
        model.class_room_id);
    if (room_ptr) {
      std::shared_ptr<IPlayer> player_plugin = std::dynamic_pointer_cast<IPlayer>(room_ptr->GetPlugin(PluginPlayer));
      player_plugin->StopPlay();
    }
  }
}

void ClassMainWindowVM::CreateWhiteBoard(void* wnd) {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    alibaba::room::RoomDetail room_detail = room_ptr->GetRoomDetail();
    std::string wb_instance_id = "";
    
    for (auto plugin :
      room_detail.room_info.plugin_instance_info.instance_list) {
      if (plugin.plugin_id == "wb") {
        wb_instance_id = plugin.instance_id;
        break;
      }
    }
    LogWithTag(ClassroomTagWhiteboard, LOG_INFO, "Get wb_instance_id : %s",wb_instance_id.c_str());
    auto create_wb_function = [this, wnd](const std::string& instance_id, const std::string& uid, bool read_only) {
      LogWithTag(ClassroomTagWhiteboard, LOG_INFO, "create_wb_function : %s",instance_id.c_str());
      ClassMainWindowModel model = GetClassMainWindowModel();
      auto room_ptr =
          alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
              model.class_room_id);
      if (room_ptr) {
        std::shared_ptr<IWhiteBoard> wb_plugin =
            std::dynamic_pointer_cast<IWhiteBoard>(
                room_ptr->GetPlugin(PluginWhiteBoard));
        wb_plugin->OpenWhiteBoardService([this, read_only,wnd, instance_id](const ::alibaba::wb::OpenWhiteboardRsp& rsp) {
          std::string prefix;
          
          std::stringstream ss;
              int permission = rsp.permission;
              if (read_only) {
                permission = 1;
              }
              ss << "{\"accessToken\":\"" << rsp.accessToken
                 << "\",\"collabHost\":\"" << prefix + rsp.collabHost
                 << "\",\"permission\":" << permission
                 << ",\"userInfo\":{\"avatarUrl\":\"\",\"nick\":\"" << rsp.userId
                 << "\",\"nickPinyin\":\"\",\"userId\":\"" << rsp.userId
                 << "\"},\"docKey\":\"" << instance_id
                 << "\",\"schema\":{\"size\":{\"width\": 40, \"height\":22.5 , "
                    "\"dpi\":144 }}, \"defaultToolType\":\""
                 << (read_only ? "pointer" : "pen") << "\""  << ",\"pointerMultiSelect\": false}";

          std::string data = ss.str();

          EventManager::Instance()->PostUITask(
              [this, wnd, data, read_only]() {
                ClassMainWindowModel model = GetClassMainWindowModel();
                auto room_ptr = alibaba::meta_space::MetaSpace::GetInstance()
                                    ->GetRoomInstance(model.class_room_id);
                if (!room_ptr) {
                  return;
                }

                std::string doc_data = data;
                bool can_multiple_edit = true;
                std::string config_data;
                if (can_multiple_edit) {
                  // We can invite more than 1 user to get the doc and edit
                  // together User need to get config/doc_data from ISV server
                  std::stringstream ss;
                  ss << "{\"schema\":{\"size\":{\"width\": 40, \"height\":22.5 "
                        ", \"dpi\":144 } , \"slides\": [{\"id\": "
                        "\"1\",\"background\": \"#FFFFFF\"}]}, "
                        "\"defaultToolType\":\""
                     << (read_only ? "pointer" : "pen")
                     << "\", \"fitMode\":1, \"forceSync\":"
                     << (read_only ? "false" : "true")
                     << ", \"replay\": false,\"showLoading\": false, "
                        "\"syncMode\": 1, \"maxSceneCount\": 100000, "
                        "\"fitMode\": 1,\"module\": {\"document\": true}, \"pointerMultiSelect\": false}";
                  config_data = ss.str();
                } else {
                  // We can edit it locally only and may use other media to
                  // share the board, such as screen sharing or live
                  config_data = "{\"module\":{\"document\":false}}";
                  doc_data = "{}";
                }
                std::shared_ptr<IWhiteBoard> white_board_plugin =
                    std::dynamic_pointer_cast<IWhiteBoard>(
                        room_ptr->GetPlugin(PluginWhiteBoard));
                white_board_plugin->OpenWhiteBoard(wnd, config_data, doc_data);
              });
        },
        [this](const ::alibaba::dps::DPSError& error_msg) {
              LogWithTag(ClassroomTagWhiteboard, LOG_INFO,
                         "CreateWhiteBoard open faild, code=%d, msg=%s",
                         error_msg.code, error_msg.reason.c_str());
        });
      }
    };

    bool read_only = param_.role == ClassRoleEnum_Student;
    if (wb_instance_id.empty()) {
      std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(room_ptr->GetPlugin(PluginWhiteBoard));
      if (white_board_plugin) {
        LogWithTag(ClassroomTagWhiteboard, LOG_INFO, "CreateWhiteBoard start");
        white_board_plugin->CreateWhiteBoard(
            [create_wb_function, model,
             read_only](const std::string& instance_id) {
              create_wb_function(instance_id, model.user_id, read_only);
            },
            [](const alibaba::dps::DPSError& error) {
              classroom::LogError(ClassroomTagWhiteboard, "CreateWhiteBoard error", error);
            });
      } else {
        LogWithTag(ClassroomTagWhiteboard, LOG_ERROR, "CreateWhiteBoard no white_board_plugin");
      }
    } 
    else {
      create_wb_function(wb_instance_id, model.user_id, read_only);
    }

  }
}

void ClassMainWindowVM::CloseWhiteBoard() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(room_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->CloseWhiteBoard();
  }
}


void ClassMainWindowVM::UpdateWhiteBoard(int32_t width, int32_t height) {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(room_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->SetBoardViewPos(0, 0, width, height);
  }
}

void ClassMainWindowVM::InitRtcListener() {
  QObject::connect(
    EventManager::Instance().get(), &EventManager::SignalRtcApplyJoinChannel,
    this, &ClassMainWindowVM::OnRtcApplyJoinChannel, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcHandleApply, this,
    &ClassMainWindowVM::OnRtcHandleApply, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcStreamIn, this,
    &ClassMainWindowVM::OnRtcStreamIn, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcKickUser, this,
    &ClassMainWindowVM::OnRtcKickUser, Qt::DirectConnection);


  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalClassStart, this,
    &ClassMainWindowVM::OnClassStart, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalClassEnd, this,
    &ClassMainWindowVM::OnClassEnd, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcUserInvited, this,
    &ClassMainWindowVM::OnRtcUserInvited, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcJoinRtcSuccess, this,
    &ClassMainWindowVM::OnRtcJoinRtcSuccess, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcLeaveUser, this,
    &ClassMainWindowVM::OnRtcLeaveUser, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalNetwordLatency, this,
    &ClassMainWindowVM::OnRtcLatency, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcRemoteJoinSuccess, this,
    &ClassMainWindowVM::OnRtcRemoteJoinSuccess, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(), 
    &EventManager::SignalStudentShareScreen, this, 
    &ClassMainWindowVM::OnSignalStudentShareScreen, Qt::DirectConnection);

  QObject::connect(
      EventManager::Instance().get(), &EventManager::SignalTeacherShareScreen,
      this, &ClassMainWindowVM::OnSignalTeacherShareScreen, Qt::DirectConnection);

  QObject::connect(
      EventManager::Instance().get(), &EventManager::SignalTeacherShareSystemAudio,
      this, &ClassMainWindowVM::OnSignalTeacherShareSystemAudio, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalAudioMute, this,
    &ClassMainWindowVM::OnRtcAudioMute, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalVideoMute, this,
    &ClassMainWindowVM::OnRtcVideoMute, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(),
    &EventManager::SignalRtcActiveSpeaker, this,
    &ClassMainWindowVM::OnRtcActiveSpeaker, Qt::DirectConnection);

  QObject::connect(EventManager::Instance().get(), 
    &EventManager::SignalRtcFirstRemoteVideoFrameDrawn, this, 
    &ClassMainWindowVM::OnRtcFirstRemoteVideoFrameDrawn);
  
}

void ClassMainWindowVM::OnRtcStreamIn(
  const alibaba::meta::RtcStreamEvent& event) {

  if (event.is_teacher &&
    (event.ali_rtc_video_track == AliRtcVideoTrack::ALI_RTC_VIDEO_TRACK_SCREEN
    || event.ali_rtc_video_track == AliRtcVideoTrack::ALI_RTC_VIDEO_TRACK_BOTH)) {
    // 如果老师已经共享了屏幕
    if (param_.role == ClassRoleEnum_Student) {
      NotifyViewScreenShareStart();
    }
  }

  AddRTCUser(event.user_id, event.user_name, event.is_teacher, event.mute_local_mic, event.mute_local_camera);  
}
void ClassMainWindowVM::OnRtcStreamOut(const std::string& uid) {
  std::vector<std::string> uids;
  uids.push_back(uid);
  RemoveRTCUsers(uids);  
}

void ClassMainWindowVM::ShowStream(const std::string& user_id, void* hwnd) {
  
  StopStream(user_id);

  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->ShowStream(user_id, hwnd);
  }
}

void ClassMainWindowVM::StopStream(const std::string& user_id) {

  UpdateRtcUserVideoMute(user_id, true);

  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->StopStream(user_id);
  }
}

void ClassMainWindowVM::StartShowScreenStream(void* hwnd) {
  ClassMainWindowModel model = GetClassMainWindowModel();

  StopShowScreenStream();

  auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->StartShowScreenStream(GetTeacherUserId(), hwnd);
  }
}

void ClassMainWindowVM::StopShowScreenStream() {
  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->StopShowScreenStream(GetTeacherUserId());
  }
}

void ClassMainWindowVM::ApproveLinkMic() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->ApproveLinkMic(model.user_id, true, []() {},
      [](const DPSError& err) {
       classroom::LogError("", "approve linkmic error true, ", err);
    });
  }
}

void ClassMainWindowVM::UpdateConfInfo() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    std::string class_id = SchemeLogin::Instance()->GetSchemeInfo().class_id;
   
    rtc_plugin->SceneClassDetail(class_id, [this](const alibaba::sceneclass::GetClassDetailRsp& rsp) {
      classroom::blog(LOG_INFO, "SceneClassDetail success, conf_id=%s, start_time=%lld", rsp.conf_id.c_str(), rsp.start_time);

      EventManager::Instance()->PostUITask([this, rsp]() {
        param_.start_time = rsp.start_time;
        param_.title = rsp.title;
        param_.class_started = rsp.status == ClassroomStatusTypeStarted;
        int32_t field = ClassMainWindowField_ConfStartTime;
        emit SignalUpdateVM(field);
      });

    }, [](const DPSError& err) {
      classroom::blog(LOG_INFO, "SceneClassDetail failed. errno=%d, reason=%s", err.code, err.reason.c_str());
    });
  }
}

void ClassMainWindowVM::RejectLinkMic() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->ApproveLinkMic(model.user_id, false, []() {},
      [](const DPSError& err) {
      classroom::LogError("approve linkmic", "approve linkmic error false, ", err);
    });
  }
}
void ClassMainWindowVM::OnRtcApplyJoinChannel(const alibaba::meta::ConfApplyJoinChannelEvent& event) {

}

void ClassMainWindowVM::OnRtcHandleApply(
  const alibaba::meta::RtcHandleApplyEvent& event) {
  auto model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student && model.user_id == event.uid) {
    if (event.approve) {
    }
    else {
      UpdateRtcStatus(RtcStatusEnum_Out);
      UpdateModelAndNotifyViewShowMediaControl(false);
    }
  }
}

void ClassMainWindowVM::OnRtcUserInvited(
  const alibaba::meta::ConfInviteEvent& event) {
  ClassMainWindowModel model = GetClassMainWindowModel();
  UpdateCaller(event.caller);
  for (auto user : event.callee_list) {
    if (user.user_id == model.user_id) {
      LogWithTag(ClassroomTagLinkMic, LOG_INFO, "Receive RtcInvite");
      UpdateRtcStatus(RtcStatusEnum_Invited);
      int32_t field = ClassMainWindowField_RtcInvited;
      emit SignalUpdateVM(field);
    }
  }
}
void ClassMainWindowVM::UpdateModelAndNotifyViewShowMediaControl(const bool show)
{
  {
    std::unique_lock<std::mutex> lock(param_mutex_);
    param_.show_media_control = show;
  }
  int32_t field = ClassMainWindowField_ShowMediaControl;
  emit SignalUpdateVM(field);
}

void ClassMainWindowVM::OnRtcJoinRtcSuccess() {
  auto model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    UpdateRtcStatus(RtcStatusEnum_Joining);
    UpdateModelAndNotifyViewShowMediaControl(true);
  }
}

void ClassMainWindowVM::OnRtcLeaveUser(
  const alibaba::meta::ConfUserEvent& event) {
  OnRtcKickUser(event);
  ClassMainWindowModel model = GetClassMainWindowModel();
}

void ClassMainWindowVM::OnRtcKickUser(
  const alibaba::meta::ConfUserEvent& event) {
  auto model = GetClassMainWindowModel();
  bool need_leave_rtc = false;

  for (auto user : event.user_list) {
    if (model.role == ClassRoleEnum_Student && model.user_id == user.user_id) {
      LogWithTag(ClassroomTagLinkMic, LOG_INFO, "Receive RtcKicUser");
      need_leave_rtc = true;
      break;
    }
  }

  if (need_leave_rtc) {
    // 离开RTC, 清理所有资源
    LeaveRTC();  
  }
  else {
    // 仅仅踢掉用户
    std::vector<std::string> users;
    for (auto user : event.user_list) {
      LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnRtcKickUser uid:%s",user.user_id.c_str());
      users.push_back(user.user_id);
    }
    RemoveRTCUsers(users);
  }
}


void ClassMainWindowVM::OnClassStart(const std::string& class_id) {
  std::string class_param = SchemeLogin::Instance()->GetSchemeInfo().class_id;
  if (class_param != class_id) {
    return;
  }
  ClassMainWindowModel model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    int32_t field = ClassMainWindowField_LiveStarted;
    emit SignalUpdateVM(field);
  }
  
  UpdateConfInfo();
  
}

void ClassMainWindowVM::OnClassEnd(const std::string& class_id){
  std::string class_param = SchemeLogin::Instance()->GetSchemeInfo().class_id;
  if (class_param != class_id) {
    return;
  }
  ClassMainWindowModel model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    int32_t field = ClassMainWindowField_LiveStopped;
    emit SignalUpdateVM(field);
  }
}

void ClassMainWindowVM::OnRtcLatency(double latency, int64_t bitrate) {
  EventManager::Instance()->PostUITask([this, latency, bitrate]() {
    param_.latency = latency;
    param_.bitrate = bitrate;
    int32_t field = ClassMainWindowField_NetworkLatency;
    emit SignalUpdateVM(field);
  });
  
}

void ClassMainWindowVM::OnRtcVideoMute(const std::string & uid, bool is_mute) {
  UpdateRtcUserVideoMute(uid, is_mute);
  NotifyViewUpdateRtcUserState();
}

void ClassMainWindowVM::OnRtcAudioMute(const std::string & uid, bool is_mute) {
  UpdateRtcUserAudioMute(uid, is_mute);
  NotifyViewUpdateRtcUserState();
}

void ClassMainWindowVM::OnRtcActiveSpeaker(const std::string& uid)
{
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    for (auto it : param_.rtc_user_list) {
      bool main_speaker = !uid.empty() && it.second.uid == uid;
      it.second.active_speaker = main_speaker;
    }
  }
  NotifyViewUpdateRtcUserState();
}

void ClassMainWindowVM::OnRtcRemoteJoinSuccess(const alibaba::meta::ConfUserEvent& event) {
  auto model = GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Student) {
    UpdateModelAndNotifyViewShowMediaControl(true);
  }
}

void ClassMainWindowVM::UpdateZoomMode(bool zoom) {
  param_.zoom_mode = !param_.zoom_mode;
  int32_t field = ClassMainWindowField_ZoomMode;
  emit SignalUpdateVM(field);
}

void ClassMainWindowVM::LeaveRTC()
{
  auto model = GetClassMainWindowModel();

  auto room_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model.class_room_id);

  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    if (rtc_plugin) {
      LogWithTag(ClassroomTagLinkMic, LOG_INFO, "LeaveRtc start");
      rtc_plugin->LeaveRtc(
          false,
          [this]() {
            LogWithTag(ClassroomTagLinkMic, LOG_INFO, "LeaveRtc success");
            UpdateModelAndNotifyViewShowMediaControl(false);
          },
          [](const DPSError& err) {
            classroom::LogError(ClassroomTagLinkMic, "LeaveRtc error", err);
          });
    }
  }
  auto old_rtc_status = model.rtc_status;
  UpdateRtcStatus(RtcStatusEnum_Out);
  if (old_rtc_status == RtcStatusEnum_Joining) {
    // 仅在连麦状态下清理RTC User
    RemoveAllRTCUser();
  }
}
void ClassMainWindowVM::AddRTCUser(
  const std::string& uid,
  const std::string& user_name,
  const bool is_teacher,
  const bool is_audio_mute,
  const bool is_video_mute)
{
  bool rtc_user_list_change = false;

  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    auto found = param_.rtc_user_list.find(uid);
    if (found == param_.rtc_user_list.end()) {
      rtc_user_list_change = true;
      RtcUserInfo& info = param_.rtc_user_list[uid];
      info.uid = uid;
      info.is_teacher = is_teacher;
      info.user_name = user_name;
      info.audio_mute = is_audio_mute;
      info.video_mute = is_video_mute;
    }
  }

  if (rtc_user_list_change) {
    OnRtcUserListChange();
  }

}
void ClassMainWindowVM::RemoveAllRTCUser()
{
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.rtc_user_list.clear();
  }
  
  // 强制执行, 恢复正确状态
  OnRtcUserListChange();
}
void ClassMainWindowVM::RemoveRTCUsers(const std::vector<std::string>& uids) {
  bool rtc_user_list_change = false;
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    for (auto uid : uids) {
      bool rtc_user_list_change_temp = param_.rtc_user_list.erase(uid) > 0;
      if (rtc_user_list_change == false) rtc_user_list_change = rtc_user_list_change_temp;
    }
  }

  if (rtc_user_list_change) {
    OnRtcUserListChange();
  }
}
void ClassMainWindowVM::UpdateSmallDisplayContentType(DisplayContentType type)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.small_display_content_type = type;
}
void ClassMainWindowVM::UpdateMainDisplayContentType(DisplayContentType type)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  param_.main_display_content_type = type;
}
void ClassMainWindowVM::NotifyViewUpdateRtcUserList()
{
  int32_t field = ClassMainWindowField_UpdateRtcUserList;
  emit SignalUpdateVM(field);
}
void ClassMainWindowVM::NotifyViewUpdateRtcUserState()
{
  int32_t field = ClassMainWindowFiled_UpdateRtcUserState;
  emit SignalUpdateVM(field);
}
void ClassMainWindowVM::NotifyViewScreenShareStart() {
  int32_t field = ClassMainWindowField_ScreenShareStart;
  emit SignalUpdateVM(field);
}
void ClassMainWindowVM::NotifyViewScreenShareStop() {
  int32_t field = ClassMainWindowField_ScreenShareStop;
  emit SignalUpdateVM(field);
}
void ClassMainWindowVM::OnRtcUserListChange()
{
  NotifyViewUpdateRtcUserList();
}

void ClassMainWindowVM::LeaveRoom() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model.class_room_id);

  std::shared_ptr<IRtc> rtc_plugin;
  std::shared_ptr<IWhiteBoard> white_board_plugin;
  if (room_ptr) {
    rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(
        room_ptr->GetPlugin(PluginWhiteBoard));
    if (white_board_plugin) {
      white_board_plugin->CloseWhiteBoard();
    }
    if (rtc_plugin) {
      rtc_plugin->LeaveRtc(false, []() {}, [](const DPSError& err) {
        classroom::LogError("", "leave rtc error false, ", err);
      });
    }
    ForceExit(2000);
    room_ptr->LeaveRoom([this]() { 
      QApplication::quit(); 
    }, [](const DPSError& err) {
      classroom::LogError("", "leave room error, ", err);
    });
  }

}

void ClassMainWindowVM::StopClass() {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    std::shared_ptr<IWhiteBoard> white_plugin = std::dynamic_pointer_cast<IWhiteBoard>(room_ptr->GetPlugin(PluginWhiteBoard));
    rtc_plugin->StopRoadPublish([this, rtc_plugin, white_plugin, room_ptr]() {
      EventManager::Instance()->PostUITask([this, rtc_plugin, white_plugin, room_ptr]() {
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
          classroom::LogError("", "leave rtc failed, ", error);
        });
        room_ptr->LeaveRoom([]() {
          QApplication::quit();
        }, [](const DPSError& error_msg) {
          classroom::LogError("", "leave room failed, ", error_msg);
        });
      });

    }, [](const DPSError& error_msg) {
      classroom::LogError("", "stop class failed, ", error_msg);
    });
    ForceExit(2000);
  }
}

void ClassMainWindowVM::MuteLocalVideo(bool mute) 
{
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model.class_room_id);
  if (room_ptr) {
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "MuteLocalVideo mute:%d", mute);
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->MuteLocalCamera(mute, alibaba::meta::AliRtcVideoTrack::ALI_RTC_VIDEO_TRACK_CAMERA);
    QTimer::singleShot(100, [rtc_plugin, mute]() {
      rtc_plugin->EnableLocalCamera(!mute);
    });
  }
}

void ClassMainWindowVM::OnSignalStudentShareScreen(const std::string& conf_id,
                                                 bool open) {
  if (param_.role == ClassRoleEnum_Teacher) {
    return;
  }
  if (open) {
    NotifyViewScreenShareStart();
  } else {
    NotifyViewScreenShareStop();
  }
} 

void ClassMainWindowVM::OnSignalTeacherShareScreen(bool open) {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model.class_room_id);
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
  if (open) {
    NotifyViewScreenShareStart();
    SetScreenShareLayout();
  }
  else {
    rtc_plugin->EnableSystemAudioRecording(false);
    NotifyViewScreenShareStop();
  }
}

void ClassMainWindowVM::OnSignalTeacherShareSystemAudio(bool open) {
  ClassMainWindowModel model = GetClassMainWindowModel();
  auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model.class_room_id);
  std::shared_ptr<IRtc> rtc_plugin =
      std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
  if (open) {
    rtc_plugin->EnableSystemAudioRecording(true);
  } else {
    rtc_plugin->EnableSystemAudioRecording(false);
  }
}

void ClassMainWindowVM::SetScreenShareLayout() {
  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    std::vector<std::string> user_ids;
    user_ids.push_back(model.user_id);
    rtc_plugin->SetLayout(user_ids, alibaba::meta::RtcLayoutModel::SCREEN_SHARE, [](){
      
    }, [](const alibaba::dps::DPSError& error){

    });
  }
}

void ClassMainWindowVM::OnRtcFirstRemoteVideoFrameDrawn(const std::string & uid, int32_t video_track, int32_t width, int32_t height, int32_t elapsed)
{
  UpdateRtcUserVideoMute(uid, false);
  NotifyViewUpdateRtcUserState();
}

void ClassMainWindowVM::UpdateRtcUserVideoMute(const std::string& user_id, const bool mute)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  auto found = param_.rtc_user_list.find(user_id);
  if (found != param_.rtc_user_list.end()) {
    found->second.video_mute = mute;
  }
}
void ClassMainWindowVM::UpdateRtcUserAudioMute(const std::string& user_id, const bool mute)
{
  std::lock_guard<std::mutex> locker(param_mutex_);
  auto found = param_.rtc_user_list.find(user_id);
  if (found != param_.rtc_user_list.end()) {
    found->second.audio_mute = mute;
  }
}

void ClassMainWindowVM::SetLayout(const std::vector<std::string>& user_ids, const alibaba::meta::RtcLayoutModel& layout_model) {
  ClassMainWindowModel model = GetClassMainWindowModel();

  auto room_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model.class_room_id);
  if (room_ptr) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(room_ptr->GetPlugin(PluginRtc));
    rtc_plugin->SetLayout(
        user_ids, layout_model,
        []() {},
        [](const alibaba::dps::DPSError& error) {});
  }
}

void ClassMainWindowVM::SetExitting() {
  param_.exitting = true;
}