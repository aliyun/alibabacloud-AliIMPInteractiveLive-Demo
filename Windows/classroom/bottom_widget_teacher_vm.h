#pragma once
#include <QObject>
#include <mutex>
#include "bottom_widget_vm.h"
#include "i_rtc.h"
#include "i_white_board.h"

class BottomWidgetTeacherVM : public BottomWidgetVM {
  Q_OBJECT
 public:
  void StartClass();
  void StopClass();
  void EnableAutoRecord(bool on);
  bool StartRecording();
  bool StopRecording();

  std::vector<alibaba::meta::RtcScreenSource> GetShareScreenSource(
      alibaba::meta::AliRtcScreenShareType source_type);

  void StartDesktopShareScreen(int desktop_id);
  void StartWindowShareScreen(int window_id);
  void StopShareScreen();
  bool GetShareScreenOpen();

  int GetLastErrorCode();
  std::string GetLastErrorMsg();

private:

  enum RecordOpType
  {
    RecordOpTypeStart,
    RecordOpTypeStop,
    RecordOpTypePause,
    RecordOpTypeResume,
  };

  std::string GetWhiteboardID();
  void WhiteboardRecord(RecordOpType op_type);
  void RtcRecord(RecordOpType op_type);
  void NotifyRecodingState();

 private:
  bool share_screen_open_ = false;
  int last_error_code_ = 0;
  std::string last_error_msg_;
};
