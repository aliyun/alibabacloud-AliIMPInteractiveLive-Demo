#pragma once


#include <QWidget>
#include "bottom_widget_teacher_vm.h"
#include "device_list.h"
#include "share_screen_hover_bar_dlg.h"
#include "share_screen_item_widget.h"

namespace Ui {
class BottomWidget;
}

class BottomWidgetTeacherVM;

class BottomWidget : public QWidget {
  Q_OBJECT

 public:
  explicit BottomWidget(QWidget* parent = nullptr);
  ~BottomWidget();
  void UpdateClassroomInfo(const std::string& classroom_id, const std::string& uid);
  void UpdateInClassStatus(bool need_start);
 signals:
  void NoitfyClassStart();
  void SignalMuteLocalAudio(bool mute);
  void SignalMuteLocalVideo(bool mute);
  void NotifyRecordingState(bool is_recording);

 private:
  void InitIconFont();
  void InitDeviceListConnect(QWidget* parent);
  void OpenHoverBar();
  void CloseHoverBar();


public slots:
  void OnNotifyChangeCameraMuteButtonStatus(bool mute);
  void OnHoverBarMicBtnCliecked();
  void OnHoverBarRecordBtnCliecked();
  void OnSignalStopShare();
  void OnAutoRecordChanged(bool on);

 private slots:
  void on_cameraMore_clicked();
  void on_micMore_clicked();
  void on_micBtn_clicked();
  void on_cameraBtn_clicked();
  void on_classBtn_clicked();
  void on_shareScreenBtn_clicked();
  void on_recordBtn_clicked();
  void on_stopBtn_clicked();
  void OnNotifyDeviceListHide(DeviceTypeEnum type);

  void OnVMUpdate(int32_t field);

 private:
  void UpdateButtonStatus(QWidget* btn);

 private:
  std::shared_ptr<BottomWidgetTeacherVM> vm_;
  Ui::BottomWidget* ui = nullptr;
  DeviceList* device_list_audio_ = nullptr;
  DeviceList* device_list_video_ = nullptr;
  ShareScreenHoverBar* hover_bar_ = nullptr;
  ShareWindowsNode selected_windows_info_;
};
