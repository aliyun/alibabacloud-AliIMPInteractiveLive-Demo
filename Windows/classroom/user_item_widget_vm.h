#pragma once
#include <QObject>
#include "meta_space.h"
#include "view/interface/base_interface.h"
#include "common/icon_font_helper.h"

class UserStatus {
 public:

  enum RtcStatus {
    kApplying = -1,
    kOnJoining = 2,
    kActive = 3,
    kJoinFailed = 4,
    kLeave = 6,
  };

  UserStatus() {
    RtcStatus status_ = kLeave;
    desc_ = "";
  }
  void operator=(const UserStatus& temp) { 
    status_ = temp.status_;
    desc_ = temp.desc_;
  }
  /**
   * 申请中 (该status仅为客户端标识, 故取-1)
   */
  static UserStatus Applying() { return UserStatus(kApplying, QTranslate("InteractiveClass.UserStatus.ApplyLink")); };
  /**
   * 呼叫状态
   */
  static UserStatus OnJoining() { return UserStatus(kOnJoining, QTranslate("InteractiveClass.UserStatus.Callingin")); };
  /**
   * 会议中
   */
  static UserStatus Active() { return UserStatus(kActive, QTranslate("InteractiveClass.UserStatus.AlreadyLink")); };
  /**
   * 入会失败
   */
  static UserStatus JoinFailed() { return UserStatus(kJoinFailed, QTranslate("InteractiveClass.UserStatus.Error")); };
  /**
   * 离会
   */
  static UserStatus Leave() { return UserStatus(kLeave, ""); };

  QString GetDesc() { return desc_; };

  RtcStatus GetStatus() { return status_; };

  static UserStatus GetUserStatus(int status) {
    switch (status) {
      case kApplying:
        return Applying();
      case kOnJoining:
        return OnJoining();
      case kActive:
        return Active();
      case kJoinFailed:
        return JoinFailed();
      case kLeave:
        return Leave();
      default:
        return Leave();
    }
  }

 private:
  RtcStatus status_;
  QString desc_;
  UserStatus(const RtcStatus& status, const QString& desc)
      : status_(status), desc_(desc) {}
};

class UserInfo {
 public:
  std::string user_id = "";
  std::string nick = "";
  UserStatus status = UserStatus::Leave();

  bool operator<(const UserInfo& temp) const {
    int sort_index_this = GetSortIndexByStatus(status);
    int sort_index_temp = GetSortIndexByStatus(temp.status);
    return sort_index_this > sort_index_temp;
  }

  //void operator=(const UserInfo& temp) {
  //  user_id = temp.user_id;
  //  nick = temp.nick;
  //  status = temp.status;
  //}

  static int GetSortIndexByStatus(UserStatus status) {
    switch (status.GetStatus()) {
      case UserStatus::kActive:
        return 5;
      case UserStatus::kOnJoining:
        return 4;
      case UserStatus::kApplying:
        return 3;
      case UserStatus::kJoinFailed:
        return 2;
      case UserStatus::kLeave:
        return 1;
      default:
        return 0;
        break;
    }
  }
};

enum UserItemField {
  UserItemField_InviteRtcSuccess = 1,
  UserItemField_InviteRtcFailed = 1 << 1,
  UserItemField_ApproveRtcSuccess = 1 << 2,
  UserItemField_ApproveRtcFailed = 1 << 3,
  UserItemField_KickUserSuccess = 1 << 4,
  UserItemField_KickUserFailed = 1 << 5,
  UserItemField_RefuseRtcSuccess = 1 << 6,
  UserItemField_RefuseRtcFailed = 1 << 7,
};

class UserItemWidgetVM : public QObject,
                         public std::enable_shared_from_this<UserItemWidgetVM> {
  Q_OBJECT
 public:
  UserItemWidgetVM();
  ~UserItemWidgetVM();

  void UpdateUserInfo(const UserInfo& user_info);
  void UpdateMyRole(const ClassRoleEnum& role);
  void UpdateClassroomId(const std::string& class_room_id);
  UserInfo GetUserInfo();
  ClassRoleEnum GetMyRole();

  void InviteRtc();
  void ApproveLinkmic();
  void RefuseLinkmic();
  void KickUserFromRtc();
  void IsConfStarted(const std::function<void(bool start)>& cb);
  void MuteUser(const bool& open);
 signals:
  void SignalUpdateVM(int32_t field);

 private slots:



 private:
  std::shared_ptr<alibaba::meta_space::IRoom> iroom_ptr_;
  ClassRoleEnum my_role_;
  UserInfo user_info_;
  std::string room_id_;
};
