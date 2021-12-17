#include "speaker_display_widget.h"
#include <math.h>
#include "grid_display_item_widget.h"
#include "ui_grid_display_item_widget.h"
#include "ui_speaker_display_widget.h"
#include "classroom_def.h"

SpeakerDisplayWidget::SpeakerDisplayWidget(QWidget* parent)
    : ui(new Ui::SpeakerDisplayWidget), QWidget(parent) {
  ui->setupUi(this);
  setAttribute(Qt::WidgetAttribute::WA_StyledBackground);

}

SpeakerDisplayWidget::~SpeakerDisplayWidget() { delete ui; }

void SpeakerDisplayWidget::AddTopWidget(GridDisplayItemWidget* widget) {
  item_list_.push_back(widget);
  UpdateLayout();
}

void SpeakerDisplayWidget::UpdateLayout() {
  if (item_list_.size() == 0) return;

  QRect perfect_window_rect;
  bool ok = GetPerfectTopWindowSize(&perfect_window_rect, NULL);
  if (!ok) {
    return;
  }

  for (int i = 0; i < ui->horizontalLayout_2->count(); i++) {
    QLayoutItem* layout_item = ui->horizontalLayout_2->itemAt(i);
    if (layout_item->spacerItem()) {
      ui->horizontalLayout_2->removeItem(layout_item);
      i--;
    }
  }

  /*ui->horizontalLayout_2->addStretch();*/
  for (auto item : item_list_) {
    QRect rect = ui->horizontalLayout_2->geometry();
    item->Resize(perfect_window_rect.width(), perfect_window_rect.height());
    int count = ui->horizontalLayout_2->count();
    ui->horizontalLayout_2->addWidget(item, 0, Qt::AlignCenter);
  }
  ui->horizontalLayout_2->addStretch();
}

void SpeakerDisplayWidget::DelLastTopWidget() {
  if (item_list_.size() == 0) return;

  GridDisplayItemWidget* item = item_list_.back();
  item_list_.pop_back();
  delete item;
  UpdateLayout();
}

void SpeakerDisplayWidget::ClearAllWidget() {
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
void SpeakerDisplayWidget::SetConfig(std::shared_ptr<MainDisplayContainerConfig> config)
{
  config_ = config;
}
void SpeakerDisplayWidget::UpdateConfig()
{
  uint32_t max_num = 0;
  bool ok = GetPerfectTopWindowSize(NULL, &max_num);
  if (ok) {
    config_->max_sub_display_count = max_num;
  }
}
bool SpeakerDisplayWidget::GetPerfectTopWindowSize(QRect* rect, uint32_t* max_num)
{
  QRect dst_rect = ui->widget_2->geometry();
  const uint32_t windows_height = dst_rect.height();
  const uint32_t window_width = windows_height / 9.0 * 16.0;
  if (window_width == 0) return false;

  if (rect) {
    rect->setHeight(windows_height);
    rect->setWidth(window_width);
  }

  uint32_t tmp_max_num = dst_rect.width() / window_width;
  if (max_num) {
    *max_num = tmp_max_num;
  }
  
  return true;
}
void SpeakerDisplayWidget::CheckNeedAdjustTopWindowLayout()
{
  uint32_t max_window_num = 0;
  bool ok = GetPerfectTopWindowSize(NULL, &max_window_num);
  if (!ok) return;

  int old_max_sub_display_count = config_->max_sub_display_count;
  config_->max_sub_display_count = max_window_num;

  if (old_max_sub_display_count > item_list_.size()) {
    // 原本未满, 有足够空间
  }
  else {
    if (old_max_sub_display_count != max_window_num) {
      emit signalNotifyUpdateTopWindowLayout();
    }
  }
}
void SpeakerDisplayWidget::moveEvent(QMoveEvent *event)
{
  CheckNeedAdjustTopWindowLayout();  
}
void SpeakerDisplayWidget::resizeEvent(QResizeEvent *event)
{
  CheckNeedAdjustTopWindowLayout();
}

int32_t SpeakerDisplayWidget::GetBigDisplayHeight() {
  return ui->bigDisplayContainer->height();
}