#pragma once

#include <QWidget>
#include <QDialog>

namespace Ui {
class GridDisplayItemWidget;
}

class GridDisplayItemWidget : public QWidget {
  Q_OBJECT

public:
  explicit GridDisplayItemWidget(QWidget* parent = nullptr);
 ~GridDisplayItemWidget();

  void Resize(int w, int h);
public:
  Ui::GridDisplayItemWidget* ui;
};

