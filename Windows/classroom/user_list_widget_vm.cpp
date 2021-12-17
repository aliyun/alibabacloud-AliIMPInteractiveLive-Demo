#include "user_list_widget_vm.h"
#include "api/base_api.h"
#include "event/event_manager.h"
#include "meta/conf_invite_event.h"
#include "meta/conf_user_event.h"
#include "meta/kick_user_event_model.h"
#include "meta/room_in_out_event_model.h"
#include "meta/conf_user_event.h"
#include "meta/conf_apply_join_channel_event.h"
#include "meta_space.h"
#include "rtc/list_apply_link_mic_user_req.h"
#include "rtc/list_apply_link_mic_user_rsp.h"
#include "rtc/list_conf_user_req.h"
#include "rtc/list_conf_user_rsp.h"
#include "ui_login_window.h"
#include "view/interface/i_main_window.h"
#include "view/view_component_manager.h"
#include "view/interface/i_toast_widget.h"
#include "meta/rtc_handle_apply_event.h"
#include "i_rtc.h"
#include "common/icon_font_helper.h"
#include "common/logging.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

UserListWidgetVM::UserListWidgetVM() {
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRoomInOut, this,
                   &UserListWidgetVM::OnRoomInOut, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcRemoteJoinSuccess, this,
                   &UserListWidgetVM::OnRtcRemoteJoinSuccess, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcRemoteJoinFail, this,
                   &UserListWidgetVM::OnRtcRemoteJoinFail, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(), 
                   &EventManager::SignalRtcApplyJoinChannel, this, 
                   &UserListWidgetVM::OnRtcApplyJoinChannel, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcHandleApply, this,
                   &UserListWidgetVM::OnRtcHandleApply, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcLeaveUser, this,
                   &UserListWidgetVM::OnRtcLeaveUser, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcKickUser, this,
                   &UserListWidgetVM::OnRtcKickUser, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcUserInvited, this,
                   &UserListWidgetVM::OnRtcUserInvited, Qt::DirectConnection);
  QObject::connect(EventManager::Instance().get(),
                   &EventManager::SignalRtcJoinRtcSuccess, this,
                   &UserListWidgetVM::OnRtcJoinRtcSuccess, Qt::DirectConnection);
  
}

UserListWidgetVM::~UserListWidgetVM() {
  QObject::disconnect(EventManager::Instance().get(),
                      &EventManager::SignalRoomInOut, this,
                      &UserListWidgetVM::OnRoomInOut);
}

void UserListWidgetVM::UpdateClassType(ClassTypeEnum type) {
  param_.type = type;
}

void UserListWidgetVM::UpdateClassRole(ClassRoleEnum role) {
  param_.role = role;
}

void UserListWidgetVM::UpdateClassroomId(const std::string& class_room_id) {
  param_.class_room_id = class_room_id;
  if (!param_.class_room_id.empty()) {
    iroom_ptr_ = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
        param_.class_room_id);
    if (iroom_ptr_)
      teacher_id_ = iroom_ptr_->GetRoomDetail().room_info.owner_id;
  }
}

void UserListWidgetVM::UpdateUserId(const std::string& user_id) {
  param_.user_id = user_id;
}

UserListParam UserListWidgetVM::GetUserListParam() { return param_; }

std::vector<UserInfo> UserListWidgetVM::GetUserList(const bool include_teacher) {
  std::vector<UserInfo> user_list;

  if (include_teacher) {
    if (!teacher_user_info_.user_id.empty()) {
      user_list.push_back(teacher_user_info_);
    }
  }
  
  if (!my_user_info_.user_id.empty()) {
    user_list.push_back(my_user_info_);
  }

  std::vector<UserInfo> user_list_temp;
  for (auto itor : userid_2_user_) {
    user_list_temp.push_back(itor.second);
  }
  std::sort(user_list_temp.begin(), user_list_temp.end());
  for (auto user : user_list_temp) {
    user_list.push_back(user);
  }

  return user_list;
}

void UserListWidgetVM::InnerLoadUserList(bool queryRtcUserList) {
  if (queryRtcUserList) {
    // 1. 请求Rtc成员列表 + 房间成员列表
    // 1.1 请求会议成员列表
    LoadAllUserOfRtc(
        [this](std::vector<::alibaba::rtc::ConfUserModel> rtcUserList) {
          // 1.2 请求会议申请连麦成员列表
          LoadAllApplyUserOfRtc([this, rtcUserList](
                                    std::vector<::alibaba::rtc::ConfUserModel>
                                        applyUserList) {
            // 1.3 请求房间成员列表
            LoadAllUserOfRoom(
                [this, rtcUserList, applyUserList](
                    std::vector<::alibaba::room::RoomUserModel> roomUserList) {
                  MergeRtcUserAndRoomUser(rtcUserList, applyUserList,
                                          roomUserList);
                  NotifyUserListChanged();
                });
          });
        });
  } else {
    LoadAllUserOfRoom(
        [this](std::vector<::alibaba::room::RoomUserModel> roomUserList) {
          MergeRtcUserAndRoomUser(std::vector<::alibaba::rtc::ConfUserModel>(),
                                  std::vector<::alibaba::rtc::ConfUserModel>(),
                                  roomUserList);
          NotifyUserListChanged();
        });
  }
}

void UserListWidgetVM::LoadUserList(bool queryRtcUserList) {
  InnerLoadUserList(queryRtcUserList);
  NotifyUserListChanged();
} 

void UserListWidgetVM::UpdateConfUserData(const ::alibaba::rtc::ConfUserModel& user, UserStatus status){
  if (user.user_id == my_user_info_.user_id) {
    my_user_info_.status = status;
  } else if (user.user_id == teacher_user_info_.user_id) {
    teacher_user_info_.status = status;
  } else {
    if (userid_2_user_.find(user.user_id) != userid_2_user_.end()) {
      userid_2_user_[user.user_id].status = status;
    }
  }
}

void UserListWidgetVM::NotifyUserListChanged() {
  int32_t field = Field_UserListUpdate;
  emit SignalUpdateVM(field);
}

void UserListWidgetVM::AddUser(const UserInfo& user_model) {
  if (IsTeacher(user_model.user_id)) {
    teacher_user_info_ = user_model;
    teacher_user_info_.nick = QString("%1(%2)").arg(QString::fromStdString(teacher_user_info_.nick)).arg(QTranslate("InteractiveClass.TeacherTitle")).toStdString();

  } else if (IsMyUserId(user_model.user_id)) {
    my_user_info_ = user_model;
    my_user_info_.nick = QString("%1(%2)").arg(QString::fromStdString(my_user_info_.nick)).arg(QTranslate("InteractiveClass.Self")).toStdString();
  }
  else {
    userid_2_user_[user_model.user_id] = user_model;
  }
}

bool UserListWidgetVM::HasUser(const std::string& user_id) {
  if (user_id.empty()) return true;
  if (userid_2_user_.find(user_id) != userid_2_user_.end()) {
    return true;
  } else if (user_id == teacher_user_info_.user_id) {
    return true;
  } else if (user_id == my_user_info_.user_id) {
    return true;
  } else {
    return false;
  }
}

void UserListWidgetVM::RemoveUser(const std::string& user_id) {
  if (IsTeacher(user_id)) {
    teacher_user_info_.user_id = "";
  }
  userid_2_user_.erase(user_id);
}

bool UserListWidgetVM::IsTeacher(const std::string& user_id) { 
    return user_id == teacher_id_; 
}

bool UserListWidgetVM::IsMyUserId(const std::string& user_id) {
  return user_id == param_.user_id;
}

// 查询所有会议成员 (考虑到分页接口的merge逻辑较复杂,
// 这里取pageSize=300一次性拉取全量会议成员)
void UserListWidgetVM::LoadAllUserOfRtc(
    std::function<void(std::vector<::alibaba::rtc::ConfUserModel>)> callback) {
  alibaba::rtc::ListConfUserReq req;
  req.page_index = 1;
  req.page_size = kMaxUserCount4Rtc;
  auto on_success = [this,
                     callback](const ::alibaba::rtc::ListConfUserRsp& rsp) {
    callback(rsp.user_list);
  };

  auto on_fail = [callback](const DPSError& error_msg) {
    std::vector<alibaba::rtc::ConfUserModel> empty_user_list;
    callback(empty_user_list);
    classroom::LogError(ClassroomTagUserList, "list conf user error, ", error_msg);
  };
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    rtc_plugin->ListConfUser(req, on_success, on_fail);
  }
}

// 查询所有申请连麦成员
void UserListWidgetVM::LoadAllApplyUserOfRtc(
    std::function<void(std::vector<::alibaba::rtc::ConfUserModel>)> callback) {
  alibaba::rtc::ListApplyLinkMicUserReq req;
  req.page_index = 1;
  req.page_size = kMaxApplyUserCount4Rtc;
  auto on_success =
      [this, callback](const ::alibaba::rtc::ListApplyLinkMicUserRsp& rsp) {
        callback(rsp.user_list);
      };

  auto on_fail = [callback](const DPSError& error_msg) {
    std::vector<alibaba::rtc::ConfUserModel> empty_user_list;
    callback(empty_user_list);
    classroom::LogError(ClassroomTagUserList,"list apply linkmic user error, ", error_msg);
  };
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    rtc_plugin->ListApplyLinkMicUser(req, on_success, on_fail);
  }
}

// 查询房间在线列表
void UserListWidgetVM::LoadAllUserOfRoom(
    std::function<void(std::vector<::alibaba::room::RoomUserModel>)> callback) {
  alibaba::room::GetRoomUserListReq req;
  req.page_num = 1;
  req.page_size = 100;
  auto on_success = [this,
                     callback](const ::alibaba::room::GetRoomUserListRsp& rsp) {
    callback(rsp.user_list);
  };

  auto on_fail = [callback](const DPSError& error_msg) {
    std::vector<alibaba::room::RoomUserModel> empty_user_list;
    callback(empty_user_list);
    classroom::LogError(ClassroomTagUserList, "list user error, ", error_msg);
  };
  if (iroom_ptr_) iroom_ptr_->ListUser(req, on_success, on_fail);
}

void UserListWidgetVM::InvateRtc(const std::string& user_id) {}

void UserListWidgetVM::RtcMuteAll(const bool& open, const std::function<void()>& on_success, const std::function<void(alibaba::dps::DPSError)>& on_fail) {
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    rtc_plugin->MuteAll(open, on_success, on_fail);
  }
}

void UserListWidgetVM::OnRoomInOut(
    const alibaba::meta::RoomInOutEventModel& event) {
  if (event.enter) {
    // 进入房间
    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowSuccessToast(QString::fromStdString(event.nick) + QTranslate("InteractiveClass.WhoEnterRoom"));
    UserInfo user;
    user.nick = event.nick;
    user.user_id = event.user_id;
    user.status = UserStatus::Leave();
    AddUser(user);
  } else {
    // 退出房间
    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowSuccessToast(QString::fromStdString(event.nick) + QTranslate("InteractiveClass.WhoLeaveRoom"));
    RemoveUser(event.user_id);
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRoomUserKicked(
    const alibaba::meta::KickUserEventModel& event) {}

void UserListWidgetVM::OnRtcUserInvited(
    const alibaba::meta::ConfInviteEvent& event) {
  for (auto user : event.callee_list){
    UpdateConfUserData(user, UserStatus::OnJoining());
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcJoinRtcSuccess() {
  InnerLoadUserList(true);
}

void UserListWidgetVM::OnRtcRemoteJoinSuccess(
    const alibaba::meta::ConfUserEvent& event) {
  for (auto user : event.user_list) {
    if (IsMyUserId(user.user_id)) {
      // 自己成功进入rtc，重新拉取rtc会议列表
      InnerLoadUserList(true);
    } else {
      UpdateConfUserData(user, UserStatus::Active());
    }
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcRemoteJoinFail(const alibaba::meta::ConfUserEvent& event) {
  for (auto user : event.user_list){
    UpdateConfUserData(user, UserStatus::JoinFailed());
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcApplyJoinChannel(
    const alibaba::meta::ConfApplyJoinChannelEvent& event) {
  if (event.is_apply) {
    QString msg = QString::fromStdString(event.apply_user.user_id) + QTranslate("InteractiveClass.ApplyLinkMic");
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(msg);
    UpdateConfUserData(event.apply_user, UserStatus::Applying());
  } else {
    QString msg = QString::fromStdString(event.apply_user.user_id) + QTranslate("InteractiveClass.CancelAppliyLinkMic");
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(msg);
    UpdateConfUserData(event.apply_user, UserStatus::Leave());
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcHandleApply(
    const alibaba::meta::RtcHandleApplyEvent& event) {
  if (event.uid == my_user_info_.user_id) {
    if (event.approve) {
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowSuccessToast(QTranslate("InteractiveClass.LinkMic.Accept"));
    } else {
      GetViewComponent<IToastWidget>(kToastWindow)
          ->ShowFailedToast(QTranslate("InteractiveClass.LinkMic.Reject"));
    }
  }

  ::alibaba::rtc::ConfUserModel user;
  user.user_id = event.uid;
  if (event.approve){
    UpdateConfUserData(user, UserStatus::OnJoining());
  } else {
    UpdateConfUserData(user, UserStatus::Leave());
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcLeaveUser(
    const alibaba::meta::ConfUserEvent& event) {
  for (auto user : event.user_list) {
    if (IsMyUserId(user.user_id)) {
      // 自己离开会议，重新加载
      InnerLoadUserList(false);
    } else {
      UpdateConfUserData(user, UserStatus::Leave());
    }
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::OnRtcKickUser(
  const alibaba::meta::ConfUserEvent& event) {
  for (auto user : event.user_list) {
    QString msg = QString::fromStdString(user.user_id) + QTranslate("InteractiveClass.Kicked");
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(msg);
  }

  for (auto user : event.user_list) {
    if (IsMyUserId(user.user_id)) {
      // 被踢的是自己
      InnerLoadUserList(false);
    } else {
      UpdateConfUserData(user, UserStatus::Leave());
    }
  }
  NotifyUserListChanged();
}

void UserListWidgetVM::MergeRtcUserAndRoomUser(
  std::vector<::alibaba::rtc::ConfUserModel> rtc_user_list,
  std::vector<::alibaba::rtc::ConfUserModel> apply_user_list,
  std::vector<::alibaba::room::RoomUserModel> room_user_list) {
  userid_2_user_.clear();
  my_user_info_.user_id = "";
  teacher_user_info_.user_id = "";
  // 1. 房间成员列表
  if (!room_user_list.empty()) {
    for (auto room_user_model : room_user_list) {
      std::string user_id = room_user_model.open_id;
      if (HasUser(user_id)) {
        // 已添加过，不再重复添加，去重
        continue;
      }

      UserInfo user_item;
      user_item.user_id = user_id;
      user_item.nick = room_user_model.nick;
      user_item.status = UserStatus::Leave();
      AddUser(user_item);
    }
  }

  // 2. RTC成员列表，修正成员状态
  bool has_rtc_user_list = !rtc_user_list.empty();
  std::vector<std::string> active_users;
  if (has_rtc_user_list) {
    for (auto conf_user_model : rtc_user_list) {
      std::string user_id = conf_user_model.user_id;
      // 修正成员状态
      UpdateConfUserData(conf_user_model, UserStatus::GetUserStatus(conf_user_model.status));
      if (conf_user_model.status == 3) {
        active_users.push_back(conf_user_model.user_id);
      }
    }
  }

  if (active_users.size()) {
    emit SignalLinkMicUserUpdate(active_users);
  }

  //3. 会议申请连麦成员列表
  bool has_apply_user_list = !apply_user_list.empty();
  if (has_apply_user_list) {
    for (auto conf_user_model : apply_user_list) {
      std::string user_id = conf_user_model.user_id;
      // 修正成员状态
      UpdateConfUserData(conf_user_model, UserStatus::Applying());
    }
  }

}