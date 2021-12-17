#include "grid_display_widget.h"
#include "ui_grid_display_widget.h"
#include "grid_display_item_widget.h"
#include "ui_grid_display_item_widget.h"

#include <math.h>
#include "classroom_def.h"

GridDisplayWidget::GridDisplayWidget(QWidget* parent)
    : ui(new Ui::GridDisplayWidget), QWidget(parent) {
  ui->setupUi(this);
  setAttribute(Qt::WidgetAttribute::WA_StyledBackground);
}

GridDisplayWidget::~GridDisplayWidget() { delete ui; }

void GridDisplayWidget::AddWidget(GridDisplayItemWidget* widget) {
  item_list_.push_back(widget);
  UpdateLayout();
}

void GridDisplayWidget::UpdateLayout() {
  if (item_list_.size() == 0) return;

  // 计算方数 n * n
  int n = ceil(sqrt(item_list_.size()));

  // 计算item最大宽高
  int height = this->height();
  int width = this->width();

  int item_max_height = floor((height - n * 18) / n);
  int item_max_width = floor((width - n * 36) / n);
  item_max_height = item_max_width / 16 * 9;

  // 清理layout_list_;
  while (ui->gridLayout->count() > 0) {
    QLayoutItem* item = ui->gridLayout->takeAt(0);
    delete item;
  }
  layout_list_.clear();

  // 添加 n行layout
  for (int i = 0; i < n; i++) {
    QHBoxLayout* lay_out = new QHBoxLayout();
    layout_list_.push_back(lay_out);
    ui->gridLayout->addLayout(lay_out, i, 0, 1, n);
  }

  static int j = 0;
  for (int i = 0; i < item_list_.size(); i++) {
    // 更新item最大高度
    GridDisplayItemWidget* item = item_list_[i];
    item->Resize(item_max_width, item_max_height);
    layout_list_[i/n]->addWidget(item);
  }
}

void GridDisplayWidget::DelLastWidget() {
  if (item_list_.size() == 0) return;

  GridDisplayItemWidget* item = item_list_.back();
  item_list_.pop_back();
  delete item;
  UpdateLayout();
}

void GridDisplayWidget::ClearAllWidget() {
  if (item_list_.size() == 0) return;

  for (auto item : item_list_) {
    while (item->ui->VideoWidget->count() > 0) {
      QWidget* display = item->ui->VideoWidget->widget(0);
      RemoveDisplayFromContainer(display);
    }
    delete item;
  }
  item_list_.clear();

  UpdateLayout();
}
void GridDisplayWidget::SetConfig(std::shared_ptr<MainDisplayContainerConfig> config)
{
  config_ = config;
}
void GridDisplayWidget::UpdateConfig()
{
  config_->max_sub_display_count = 9;
}