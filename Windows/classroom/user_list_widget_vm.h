#pragma once
#include <QObject>
#include "meta_space.h"
#include "view/interface/base_interface.h"
#include "user_item_widget_vm.h"
#include "conf_user_model.h"
#include "rtc_handle_apply_event.h"
#include "conf_user_event.h"
#include "conf_invite_event.h"
#include "conf_apply_join_channel_event.h"

enum UserListVMUpdateField {
  Field_UserListUpdate = 1,
  Field_UserLevea = 1 << 1,
  Field_UserStatusUpdate = 1 << 2,
};

struct UserListParam {
  ClassTypeEnum type = ClassTypeEnum_SmallClass;
  ClassRoleEnum role = ClassRoleEnum_Teacher;
  std::string class_room_id;
  std::string user_id;
};

class UserListWidgetVM : public QObject {
  Q_OBJECT
 public:
  UserListWidgetVM();
  ~UserListWidgetVM();
  void UpdateClassType(ClassTypeEnum type);
  void UpdateClassRole(ClassRoleEnum role);
  void UpdateClassroomId(const std::string& class_room_id);
  void UpdateUserId(const std::string& user_id);
  UserListParam GetUserListParam();

  /**
   * @return 返回已排序的用户列表
   */
  std::vector<UserInfo> GetUserList(const bool include_teacher = true);

  /**
   * 查询用户列表的分页数据
   *
   * @param queryRtcUserList 是否查询rtc用户列表
   */
  void LoadUserList(bool queryRtcUserList);
  
  void UpdateConfUserData(const ::alibaba::rtc::ConfUserModel& event, UserStatus status);

  void InvateRtc(const std::string& user_id);

  void RtcMuteAll(const bool& open, const std::function<void()>& on_success, const std::function<void(alibaba::dps::DPSError)>& on_fail);
 signals:
  void SignalUpdateVM(int32_t field);
  void SignalLinkMicUserUpdate(const std::vector<std::string>& user_list);
 private slots:
  void OnRoomInOut(const alibaba::meta::RoomInOutEventModel& event);
  void OnRoomUserKicked(const alibaba::meta::KickUserEventModel& event);
  void OnRtcUserInvited(const alibaba::meta::ConfInviteEvent& event);
  void OnRtcJoinRtcSuccess();
  void OnRtcRemoteJoinSuccess(const alibaba::meta::ConfUserEvent& event);
  void OnRtcRemoteJoinFail(const alibaba::meta::ConfUserEvent& event);
  void OnRtcApplyJoinChannel(const alibaba::meta::ConfApplyJoinChannelEvent& event);
  void OnRtcHandleApply(const alibaba::meta::RtcHandleApplyEvent& event);
  void OnRtcLeaveUser(const alibaba::meta::ConfUserEvent& event);
  void OnRtcKickUser(const alibaba::meta::ConfUserEvent& event);
 private:
  void InnerLoadUserList(bool queryRtcUserList);
  void NotifyUserListChanged();
  void AddUser(const UserInfo& user_model);
  bool HasUser(const std::string& user_id);
  void RemoveUser(const std::string& user_id);
  bool IsTeacher(const std::string& user_id);
  bool IsMyUserId(const std::string& user_id);
  void MergeRtcUserAndRoomUser(
      std::vector<::alibaba::rtc::ConfUserModel> rtc_user_list,
      std::vector<::alibaba::rtc::ConfUserModel> apply_user_list,
      std::vector<::alibaba::room::RoomUserModel> room_user_list);

  void LoadAllUserOfRtc(
      std::function<void(std::vector<::alibaba::rtc::ConfUserModel>)> callback);

  void LoadAllApplyUserOfRtc(
      std::function<void(std::vector<::alibaba::rtc::ConfUserModel>)> callback);

  void LoadAllUserOfRoom(
      std::function<void(std::vector<::alibaba::room::RoomUserModel>)>
          callback);

  // 房间用户列表的最大人数
  static const int kMaxUserCount4Room = 500;
  // Rtc用户列表的最大人数
  static const int kMaxUserCount4Rtc = 200;
  // Rtc申请连麦用户列表的最大人数
  static const int kMaxApplyUserCount4Rtc = 200;

  std::shared_ptr<alibaba::meta_space::IRoom> iroom_ptr_;

  UserListParam param_;
  // userId -> User 映射关系
  std::map<std::string, UserInfo> userid_2_user_;
  UserInfo my_user_info_;
  UserInfo teacher_user_info_;
  std::string teacher_id_;
};
