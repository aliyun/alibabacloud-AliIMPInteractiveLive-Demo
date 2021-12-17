#pragma once

#include <QWidget>
#include <QDialog>
#include "view/interface/i_main_window.h"

class TitleWidgetVM;
class ConfLayoutDialog;
class ConfPerformanceDialog;
class ConfShareDialog;
class SettingList;

namespace Ui {
  class TitleWidget;
}

enum ConfLayoutEnum {
  ConfLayoutMain,
  ConfLayoutSpeaker,
  ConfLayoutGrid,
};


class TitleWidget : public QWidget
{
  Q_OBJECT

public:
  explicit TitleWidget(QWidget *parent = nullptr);
  ~TitleWidget();
  void UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info);
  void UpdateStartTime(bool started, int64_t time);
  void UpdateNetwork(double latency, int64_t bitrate);
  void UpdateTitle(const std::string& title);
signals:
  void NotifyClose();
private:
  void InitConnect();
  void InitIconFont();
  void InitClassCode();
  void InitTimer();
  void InitHoverWidget();
  void SetLiveStarted(bool start);
  virtual void mousePressEvent(QMouseEvent *event) override;
  virtual void mouseMoveEvent(QMouseEvent *event) override;
  virtual void mouseReleaseEvent(QMouseEvent *event) override;
  void UpdateParentStartPos(QPoint pos);
  QPoint PressPoint() const { return press_point_; }
  virtual bool eventFilter(QObject* target, QEvent* event) override;
private slots:
  void OnVMUpdate(int32_t filed);
  void OnClassTimer();
  void on_shareInfoBtn_clicked();
  void on_performance_clicked();
  void on_closeBtn_clicked();
  void OnLiveStarted();

 private:
  Ui::TitleWidget *ui;
  std::shared_ptr<TitleWidgetVM> vm_;
  QString cpu_format_info_;
  QString memory_format_info_;
  QString network_format_info_;
  QTimer* class_timer_ = nullptr;
  int64_t start_time_ = 0;

private:
  QPoint press_point_;
  QPoint start_pos_;
  QPoint parent_start_pos_;
  bool drag_window_ = true;
  bool mouse_btn_pressed = false;
  ConfPerformanceDialog* performance_dialog_ = nullptr;
  ConfShareDialog* conf_share_dialog_ = nullptr;
};

