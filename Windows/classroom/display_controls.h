#pragma once
#include <QWidget>
#include <QDockWidget>
#include <QDialog>
#include <string>
#include <memory>
#include <QTimer>
#include <QPointer>
#include <QString>
#include <memory>
#include "qwidget_event_filter.h"
#include "ui/Label/MultiIconLabel.h"

namespace Ui {
  class DisplayControls;
} // namespace Ui

class DisplayControls : public QDialog
{
  Q_OBJECT
public:
  DisplayControls(QWidget* parent, QWidget* listen);
  ~DisplayControls();
  void UpdateZoomBtnStatus(bool show);
  void UpdateUserNameStatus(bool show);
  void InitIconFont();
  void SetTeacherNameLabelEnable(bool enable);
  void SetNameLabelEnable(bool enable);
  void SetVideoVisable(bool visable);
  void SetAudioVisable(bool visable);
  void SetBottomVisable(bool visable);
  void UpdateRedLine(MultiIconLabel* btn, bool show);
 signals:
  void NotifyZoom(bool);
public slots:
  void SetUserName(const QString& user_name);
  void SetAudioMute(const bool mute);
  void SetVideoMute(const bool mute);
  void SetActiveSpeaker(const bool active);
  void OnAdjustControls();
  void on_zoomBtn_clicked();
private:
  QWidget* parent_ = nullptr;
  std::unique_ptr<Ui::DisplayControls> ui_;
  std::unique_ptr<QWidgetEventFilter> filter_;
  bool teacher_name_label_enable_ = true;
  bool name_lable_enable_ = true;
  bool zoom_ = false;
};