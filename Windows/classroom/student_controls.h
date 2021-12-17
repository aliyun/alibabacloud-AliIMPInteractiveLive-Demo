#pragma once
#include <QWidget>
#include "qwidget_event_filter.h"
#include "bottom_widget_student_vm.h"
#include "ui/Button/multi_icon_button.h"

namespace Ui {
  class StudentControls;
} // namespace Ui

class StudentControls : public QWidget {
  Q_OBJECT
public:
  StudentControls(QWidget* parent, QWidget* listen);
  ~StudentControls();

  void UpdateClassRoomId(const std::string& room_id);
  void UpdateUserId(const std::string& user_id);
  void UpdateLinkmicStatus(bool linkmic);
  void UpdateRedLine(MultiIconButton* btn, bool show);
signals:
  void SignalMuteLocalAudio(bool mute);
  void SignalMuteLocalVideo(bool mute);

public slots:
  void OnNotifyChangeCameraMuteButtonStatus(bool mute);

 private slots:
  void OnAdjustControls();
  void OnRtcMuteToClient(bool mute);
  void on_LinkmicControl_clicked();
  void on_MicControl_clicked();
  void on_VideoControl_clicked();

private:
  void InitShadowBox();
  void ClearShadowBox();

 private:
  QWidget* parent_ = nullptr;
  std::unique_ptr<Ui::StudentControls> ui_;
  std::unique_ptr<QWidgetEventFilter> filter_;
  std::unique_ptr<BottomWidgetStudentVM> vm_;
  bool link_mic_status_ = false;
  bool video_status_ = true;
  bool mic_status_ = true;
};