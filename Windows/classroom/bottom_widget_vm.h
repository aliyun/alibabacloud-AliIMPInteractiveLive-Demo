#pragma once
#include <QObject>
#include <mutex>
#include "i_room.h"
#include "view/interface/base_interface.h"


// ¼��״̬
enum EnumRecordingState
{
  EnumRecordingStateStopped = 0,  // ��ֹͣ¼��      
  EnumRecordingStateStarted = 1,  // �ѿ�ʼ¼��
  EnumRecordingStatePaused  = 2,  // ����ͣ¼��
};
struct BottomWidgetModel {
  std::string class_room_id;
  std::string uid;
  int32_t type = ClassTypeEnum_SmallClass;
  bool class_started = false;                                       // �Ƿ����Ͽ�״̬
  bool auto_start_recording_after_class_start = true;               // �Ƿ��Ͽκ��Զ�¼��
  EnumRecordingState recording_state = EnumRecordingStateStopped;   // ¼��״̬
};

enum BottomWidgetField {
  BottomWidgetFieldClassStarted = 1,
  BottomWidgetFieldClassStoped = 1 << 1,
  BottomWidgetTeacherStartRecording = 1 << 2,
  BottomWidgetTeacherGetShareScreenSource = 1 << 3,
  BottomWidgetTeacherStartDesktopShareScreen = 1 << 4,
  BottomWidgetTeacherStopShareScreen = 1 << 5,
  BottomWidgetTeacherRecodingState = 1 << 6,
};

class BottomWidgetVM : public QObject {
    Q_OBJECT
public:
  void UpdateClassroomId(const std::string& class_room_id);
  void UpdateUserId(const std::string& uid);
  void UpdateClassType(int32_t type);
  void UpdateRecordingState(EnumRecordingState state);
  void UpdateClassStarted(bool started);
  void UpdateAudoStartRecordingAfterClassStart(bool auto_start);
  std::string GetClassroomId();
  BottomWidgetModel GetBottomWidgetModel();
  void MuteLocalAudio(bool mute);
signals:
  void SignalUpdateVM(int32_t field);
protected:
  BottomWidgetModel param_;
  std::mutex param_mutex_;

  std::shared_ptr<alibaba::meta_space::IRoom> room_ptr_;
};

