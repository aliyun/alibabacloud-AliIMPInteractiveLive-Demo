#pragma once
#include <QObject>
#include "view/interface/base_interface.h"
#include "meta_space.h"

enum ClassVMUpdateField {
  Field_ShowMainWnd = 1,
  Field_ErrorNotify = 1 << 1,
  Field_UpdateRole = 1 << 2,
  Field_GetClassDetailDone = 1 << 3,
};

enum ClassLoginErrType {
  ClassLoginErrNone,
  ClassLoginErrLogin,
  ClassLoginErrEnterRoom,
  ClassLoginErrNotTeacher,
  ClassLoginErrNetworkError,
  ClassLoginErrClassEnd,
  ClassLoginErrClassDetail,
};

struct ClassroomParam {
  ClassTypeEnum type = ClassTypeEnum_SmallClass;
  ClassRoleEnum role = ClassRoleEnum_Teacher;
  std::string class_room_id;
  std::string user_id;
  std::string app_id;
  ClassLoginErrType error = ClassLoginErrNone;
  int32_t class_status = 0;
};

class LoginWindowVM : public QObject {
  Q_OBJECT
 public:
  void UpdateClassType(ClassTypeEnum type);
  void UpdateClassRole(ClassRoleEnum role);
  void DoAutoLogin();
  void DoLogout();
  void UpdateClassroomId(const std::string& class_room_id);
  void UpdateUserId(const std::string& user_id);
  void UpdateAppId(const std::string& app_id);
  QString CreateRoom();
  ClassroomParam GetClassroomParam();
  void DoEnterRoom();
 signals:
  void SignalUpdateVM(int32_t field);

private:
  void GetLoginToken(alibaba::meta_space::TokenInfo& token_info);
 private:
  ClassroomParam param_;
};
