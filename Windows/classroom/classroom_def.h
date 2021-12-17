#pragma once

#include <QStackedWidget>
#include <QWidget>

enum ClassTypeEnum {
  ClassTypeEnum_SmallClass = 0,
  ClassTypeEnum_BigClass = 1,
};

enum ClassRoleEnum {
  ClassRoleEnum_Teacher = 0,
  ClassRoleEnum_Student = 1,
};

enum MainDisplayMode {
  MainDisplayMode_BigDisplayMode = 0,
  MainDisplayMode_SpeakerMode = 1,
  MainDisplayMode_GridMode = 2,
};

enum RtcStatusEnum {
  RtcStatusEnum_Out = 0,
  RtcStatusEnum_Invited = 1,
  RtcStatusEnum_Approved = 2,
  RtcStatusEnum_Joining = 3,
};

enum DisplayContentType
{
  DisplayContentType_Live,
  DisplayContentType_RtcPreview,
  DisplayContentType_WhiteBoard,
  DisplayContentType_SreenStream,

  DisplayContentType_Unknown,
};

// return parent(if QStackedWidget)
QStackedWidget* RemoveDisplayFromContainer(QWidget* display);
void AddDisplayToContainer(QStackedWidget* container, QWidget* display);

struct MainDisplayContainerConfig
{
  uint32_t max_sub_display_count = 5;
};