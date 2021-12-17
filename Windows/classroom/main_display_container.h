#pragma once

#include <QDialog>
#include <QWidget>
#include <vector>
#include <stdint.h>
#include "classroom_def.h"
#include <memory>

namespace Ui {
class MainDisplayContainer;
}

class GridDisplayWidget;


class MainDisplayContainer : public QWidget {
  Q_OBJECT

 public:

  explicit MainDisplayContainer(QWidget* parent = nullptr);
  ~MainDisplayContainer();

  void SetConfig(const MainDisplayContainerConfig& config);
  MainDisplayContainerConfig GetConfig();

  void SetMode(MainDisplayMode mode_type);
  void SetMainDisplay(QWidget* widget);
  QWidget* ClearMainDisplay();
  void AddSubDisplay(QWidget* widget);
  void ClearAllSubDisplay();
  int32_t GetBigDisplayHeight();
signals:
  void signalNotifyUpdateTopWindowLayout();
  void signalNotifyPageUp();
  void signalNotifyPageDown();

 private slots:
  void OnSpeakerDisplayWidgetNotifyUpdateTopWindowLayout();
  void OnSpeakerDisplayWidgetNotifyPageUp();
  void OnSpeakerDisplayWidgetNotifyPageDown();
  void OnGridDisplayWidgetNotifyPageUp();
  void OnGridDisplayWidgetNotifyPageDown();

 private:
  void UpdateLayout();

public:
  Ui::MainDisplayContainer* ui;
private:
  std::shared_ptr<MainDisplayContainerConfig> config_;
  QStackedWidget main_display_container_;
};
