#pragma once

#include <QDialog>
#include <QWidget>
#include <QHBoxLayout>
#include <vector>
#include "classroom_def.h"
namespace Ui {
class GridDisplayWidget;
}

class GridDisplayItemWidget;

class GridDisplayWidget : public QWidget {
  Q_OBJECT

 public:
  explicit GridDisplayWidget(QWidget* parent = nullptr);
  ~GridDisplayWidget();

  void AddWidget(GridDisplayItemWidget* widget);
  void DelLastWidget();

  void ClearAllWidget();
  void SetConfig(std::shared_ptr<MainDisplayContainerConfig> config);
  void UpdateConfig();
 private:
  void UpdateLayout();

public:
  Ui::GridDisplayWidget* ui;
 private:
  std::vector<GridDisplayItemWidget*> item_list_;
  std::vector<QHBoxLayout*> layout_list_;
  std::shared_ptr<MainDisplayContainerConfig> config_;
};
