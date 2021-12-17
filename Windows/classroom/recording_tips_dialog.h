#pragma once
#include <QDialog>
#include "qwidget_event_filter.h"
#include <memory>
namespace Ui {
  class RecodingTips;
}

class RecordingTipsDialog : public QDialog {
  Q_OBJECT

public:
  explicit RecordingTipsDialog(QWidget* parent);
  ~RecordingTipsDialog();
  int32_t GetTopMargin();
public slots:
  void OnNotifyRecodingState(bool is_recording);

private slots:
  void OnAdjustControls();

private:
  QWidget* parent_ = nullptr;
  std::unique_ptr<Ui::RecodingTips> ui_;
  std::unique_ptr<QWidgetEventFilter> filter_;
};