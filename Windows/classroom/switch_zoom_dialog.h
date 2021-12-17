#pragma once
#include <QDialog>
#include "qwidget_event_filter.h"
#include <memory>
namespace Ui {
class SwitchZoom;

}
class SwitchZoomDialog : public QDialog {
  Q_OBJECT

public:
  explicit SwitchZoomDialog(QWidget* parent, QWidget* ref);
 ~SwitchZoomDialog();

signals:
  void NotifyZoom(bool);

public slots:
  void OnNotifyRecodingState(bool is_recording);
 void on_pushButton_clicked();

private slots:
  void OnAdjustControls();


private:
  QWidget* parent_ = nullptr;
  QWidget* ref_ = nullptr;
  std::unique_ptr<Ui::SwitchZoom> ui_;
  std::unique_ptr<QWidgetEventFilter> filter_;
};