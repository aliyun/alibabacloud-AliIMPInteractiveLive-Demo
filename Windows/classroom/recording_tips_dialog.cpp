#include "recording_tips_dialog.h"
#include "ui_recording_tips.h"
#include "class_main_window.h"

RecordingTipsDialog::RecordingTipsDialog(QWidget* parent) :
  QDialog(parent, Qt::FramelessWindowHint),
  parent_(parent),
  ui_(new Ui::RecodingTips),
  filter_(new QWidgetEventFilter)
{
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);

  parent->installEventFilter(filter_.get());
  connect(filter_.get(), &QWidgetEventFilter::signalMove, this, &RecordingTipsDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this, &RecordingTipsDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this, &RecordingTipsDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this, &RecordingTipsDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this, &RecordingTipsDialog::OnAdjustControls);
}

RecordingTipsDialog::~RecordingTipsDialog()
{
  disconnect(this);
}

void RecordingTipsDialog::OnNotifyRecodingState(bool is_recording)
{
  this->setVisible(is_recording);
}

int32_t RecordingTipsDialog::GetTopMargin() {
  int32_t margin_top = 200;
  return margin_top;
}

void RecordingTipsDialog::OnAdjustControls()
{
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (this->isVisible()) {

    QRect cur_rect = this->rect();
    QRect par_rect = parent_->geometry();
    QRect dst_rect(
      par_rect.x() + 28, 
      par_rect.y() + GetTopMargin(), 
      cur_rect.width(), 
      cur_rect.height());

    // move
    if (cur_rect != dst_rect) {
      this->move(dst_rect.x(), dst_rect.y());
    }

    // show
    if (this->isHidden()) {
      this->show();
    }
  }
  else {
    // hide
    if (!this->isHidden()) {
      this->hide();
    }
  }
}