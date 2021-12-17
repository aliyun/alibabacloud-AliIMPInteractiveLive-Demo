#pragma once

#include <QDockWidget>
#include <QDialog>
#include <string>
#include <memory>
#include <QTimer>
#include <QPointer>
#include "qwidget_event_filter.h"

namespace Ui {
  class PreviewControls;
}

class CPreviewControlsWidget :public QDialog
{
  Q_OBJECT

public:
  CPreviewControlsWidget(QWidget* parent, QWidget* listen);
  ~CPreviewControlsWidget();
signals:
  void NotifyZoom(bool bigger);
private:
  void UpdateBtnStatus(bool checked);
private slots:
  void on_zoomBtn_clicked();
  void OnAdjustControls();

private:
  std::unique_ptr<Ui::PreviewControls> ui;
  std::unique_ptr<QWidgetEventFilter> filter_;
  QWidget* parent_ = nullptr;
};
