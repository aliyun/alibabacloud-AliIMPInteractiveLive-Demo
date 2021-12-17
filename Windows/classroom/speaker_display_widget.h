#pragma once

#include <QDialog>
#include <QWidget>
#include <QHBoxLayout>
#include <vector>
#include "classroom_def.h"
#include <memory>

namespace Ui {
class SpeakerDisplayWidget;
}

class GridDisplayItemWidget;

class SpeakerDisplayWidget : public QWidget {
  Q_OBJECT

 public:
  enum ModeType {
    SpeakerMode = 0,
    GridMode = 1,
  };

  explicit SpeakerDisplayWidget(QWidget* parent = nullptr);
  ~SpeakerDisplayWidget();

  void AddTopWidget(GridDisplayItemWidget* widget);
  void DelLastTopWidget();
  void ClearAllWidget();
  void SetConfig(std::shared_ptr<MainDisplayContainerConfig> config);

  void UpdateConfig();
  int32_t GetBigDisplayHeight();
 private:
  void UpdateLayout();
  bool GetPerfectTopWindowSize(QRect* rect, uint32_t* max_num);
  void CheckNeedAdjustTopWindowLayout();

  virtual void moveEvent(QMoveEvent *event) override;
  virtual void resizeEvent(QResizeEvent *event) override;

Q_SIGNALS:
  void signalNotifyUpdateTopWindowLayout();

public:
 Ui::SpeakerDisplayWidget* ui;
 private:

  std::vector<GridDisplayItemWidget*> item_list_;
  GridDisplayItemWidget* big_widget_;
  std::shared_ptr<MainDisplayContainerConfig> config_;

};
