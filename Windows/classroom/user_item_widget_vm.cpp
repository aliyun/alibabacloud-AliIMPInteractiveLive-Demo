#include "user_item_widget_vm.h"
#include "api/base_api.h"
#include "i_rtc.h"
#include "common/logging.h"
#include "event/event_manager.h"


using namespace alibaba::meta_space;
using namespace alibaba::dps;

UserItemWidgetVM::UserItemWidgetVM() {}

UserItemWidgetVM::~UserItemWidgetVM() {}

void UserItemWidgetVM::UpdateUserInfo(const UserInfo& user_info) {
  user_info_ = user_info;
}

void UserItemWidgetVM::UpdateMyRole(const ClassRoleEnum& role) {
  my_role_ = role;
}

void UserItemWidgetVM::UpdateClassroomId(const std::string& class_room_id) {
  room_id_ = class_room_id;
  if (!room_id_.empty()) {
    iroom_ptr_ = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
        room_id_);
  }
}

UserInfo UserItemWidgetVM::GetUserInfo() { return user_info_; }

ClassRoleEnum UserItemWidgetVM::GetMyRole() { return my_role_; }

void UserItemWidgetVM::InviteRtc() {
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    std::weak_ptr<UserItemWidgetVM> weak_ptr = shared_from_this();
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "InviteJoinRtc start, uid:%s", user_info_.user_id.c_str());
    auto uid = user_info_.user_id;
    rtc_plugin->InviteJoinRtc(user_info_.user_id,
                              [weak_ptr, uid]() {
                                LogWithTag(ClassroomTagLinkMic, LOG_INFO, "InviteJoinRtc success, uid:%s", uid.c_str());
                                auto ptr = weak_ptr.lock();
                                if (ptr) {
                                  int32_t filed =
                                  UserItemField_InviteRtcSuccess;
                                  emit ptr->SignalUpdateVM(filed);
                                }
                              },
                              [weak_ptr](const DPSError& error_msg) {
                                auto ptr = weak_ptr.lock();
                                if (ptr) {
                                  int32_t filed = UserItemField_InviteRtcFailed;
                                  emit ptr->SignalUpdateVM(filed);
                                }
                                classroom::LogError(ClassroomTagLinkMic, "InviteJoinRtc error", error_msg);
                              });
  }
}

void UserItemWidgetVM::MuteUser(const bool& open) {
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin =
        std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "MuteUser start, uid:%s",
               user_info_.user_id.c_str());
    auto uid = user_info_.user_id;
    rtc_plugin->MuteUser(
        user_info_.user_id, open,
        [uid]() {
          LogWithTag(ClassroomTagLinkMic, LOG_INFO, "MuteUser success, uid:%s", uid.c_str());
        },
        [](const DPSError& error_msg) {
          classroom::LogError(ClassroomTagLinkMic, "MuteUser error", error_msg);
        });
  }
}

void UserItemWidgetVM::ApproveLinkmic() {
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    std::weak_ptr<UserItemWidgetVM> weak_ptr = shared_from_this();
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApproveLinkMic true start, uid:%s", user_info_.user_id.c_str());
    auto uid = user_info_.user_id;
    rtc_plugin->ApproveLinkMic(user_info_.user_id, true,
                               [weak_ptr, uid]() {
                                 LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApproveLinkMic true success, uid:%s", uid.c_str());
                                 auto ptr = weak_ptr.lock();
                                 if (ptr) {
                                   int32_t filed = UserItemField_ApproveRtcSuccess;
                                   emit ptr->SignalUpdateVM(filed);
                                 }
                               },
                               [weak_ptr](const DPSError& error_msg) {
                                 auto ptr = weak_ptr.lock();
                                 if (ptr) {
                                   //int32_t filed = UserItemField_ApproveRtcFailed;
                                   //emit ptr->SignalUpdateVM(filed);
                                 }
                                 classroom::LogError(ClassroomTagLinkMic, "ApproveLinkMic true error, ", error_msg);
                               });
  }
}

void UserItemWidgetVM::RefuseLinkmic() {
  if (iroom_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    std::weak_ptr<UserItemWidgetVM> weak_ptr = shared_from_this();
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApproveLinkMic false start, uid:%s", user_info_.user_id.c_str());
    auto uid = user_info_.user_id;
    rtc_plugin->ApproveLinkMic(user_info_.user_id, false,
                               [weak_ptr, uid]() {
                                 LogWithTag(ClassroomTagLinkMic, LOG_INFO, "ApproveLinkMic false success, uid:%s", uid.c_str());
                                 auto ptr = weak_ptr.lock();
                                 if (ptr) {
                                   int32_t filed =
                                       UserItemField_RefuseRtcSuccess;
                                   emit ptr->SignalUpdateVM(filed);
                                 }
                               },
                               [weak_ptr](const DPSError& error_msg) {
                                 auto ptr = weak_ptr.lock();
                                 if (ptr) {
                                   int32_t filed =
                                      UserItemField_RefuseRtcFailed;
                                   emit ptr->SignalUpdateVM(filed);
                                 }
                                 classroom::LogError(ClassroomTagLinkMic, "ApproveLinkMic false error, ", error_msg);
                               });
  }
}

void UserItemWidgetVM::KickUserFromRtc() {
  if (iroom_ptr_) {
    std::vector<std::string> users;
    users.push_back(user_info_.user_id);
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    std::weak_ptr<UserItemWidgetVM> weak_ptr = shared_from_this();
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "KickUserFromRtc start, uid:%s", user_info_.user_id.c_str());
    auto uid = user_info_.user_id;
    rtc_plugin->KickUserFromRtc(users,
                                [weak_ptr, uid]() {
                                  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "KickUserFromRtc success, uid:%s", uid.c_str());
                                  auto ptr = weak_ptr.lock();
                                  if (ptr) {
                                    int32_t filed =
                                        UserItemField_KickUserSuccess;
                                    emit ptr->SignalUpdateVM(filed);
                                  }
                               },
                                [weak_ptr](const DPSError& error_msg) {
                                  classroom::LogError("kick user", "kick user from rtc error, ", error_msg);
                                  auto ptr = weak_ptr.lock();
                                  if (ptr) {
                                    int32_t filed =
                                        UserItemField_KickUserFailed;
                                    emit ptr->SignalUpdateVM(filed);
                                  }
                               });
  }
}


void UserItemWidgetVM::IsConfStarted(const std::function<void(bool start)>& cb) {

  auto ui_cb = [cb](bool start) {
    EventManager::Instance()->PostUITask([cb, start]() {
      SAFE_CALLBACK(cb)(start);
    });
  };
  if (iroom_ptr_) {
   
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(iroom_ptr_->GetPlugin(PluginRtc));
    std::weak_ptr<UserItemWidgetVM> weak_ptr = shared_from_this();
    rtc_plugin->GetConfDetail([ui_cb](const alibaba::rtc::GetConfDetailRsp& rsp) {
      ui_cb(rsp.conf_info_model.start_time > 0);
    }, [ui_cb](const ::alibaba::dps::DPSError & error_msg) {
      ui_cb(false);
    });
  }
}