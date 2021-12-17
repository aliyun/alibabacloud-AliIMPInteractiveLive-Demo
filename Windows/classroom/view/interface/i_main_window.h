#pragma once

#include "base_interface.h"
#include <string>
#include <QPoint>

enum ClassroomStatusType
{
  ClassroomStatusTypeNotStart = 0,
  ClassroomStatusTypeStarted = 1,
  ClassroomStatusTypeEnded = 2,
};

struct ClassroomMetaInfo {
  ClassTypeEnum type = ClassTypeEnum_SmallClass;
  ClassRoleEnum role = ClassRoleEnum_Teacher;
  std::string class_room_id;
  std::string user_id;
  int32_t status = ClassroomStatusTypeNotStart;
};
class IMainWindow :public IBase {
public:
  virtual ~IMainWindow() {}
  virtual void ShowClassRoomWindow() = 0;
  virtual void UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info) = 0;
};
