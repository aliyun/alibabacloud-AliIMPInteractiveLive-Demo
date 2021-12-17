#include "grid_display_item_widget.h"
#include "ui_grid_display_item_widget.h"

GridDisplayItemWidget::GridDisplayItemWidget(QWidget* parent)
    : ui(new Ui::GridDisplayItemWidget), QWidget(parent) {
  ui->setupUi(this);
}

GridDisplayItemWidget::~GridDisplayItemWidget() {
  delete ui;
}

void GridDisplayItemWidget::Resize(int w, int h) {
  this->resize(w, h);
  this->setMaximumSize(w, h);
  ui->VideoWidget->resize(w, h);
  ui->VideoWidget->setMaximumSize(w, h);
  QWidget* widget = ui->VideoWidget->currentWidget();
  if (widget) {
    widget->resize(w, h);
    widget->setMaximumSize(w, h);
  }
}