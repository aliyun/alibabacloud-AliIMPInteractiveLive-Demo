#include "switch_zoom_dialog.h"
#include "ui_switch_zoom.h"
#include "common/icon_font_helper.h"

SwitchZoomDialog::SwitchZoomDialog(QWidget* parent, QWidget* ref)
    : QDialog(parent, Qt::FramelessWindowHint/* | Qt::WindowStaysOnTopHint*/),
  parent_(parent),
  ref_(ref),
  ui_(new Ui::SwitchZoom),
  filter_(new QWidgetEventFilter)
{

  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);
  ref_->installEventFilter(filter_.get());
  connect(filter_.get(), &QWidgetEventFilter::signalMove, this, &SwitchZoomDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this, &SwitchZoomDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this, &SwitchZoomDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this, &SwitchZoomDialog::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this, &SwitchZoomDialog::OnAdjustControls);
  IconFontHelper::Instance()->SetIcon(ui_->pushButton, kSwitchView);
}
SwitchZoomDialog::~SwitchZoomDialog() {
  disconnect(this);
}

void SwitchZoomDialog::OnNotifyRecodingState(bool is_recording) {
  this->setVisible(is_recording);
}

void SwitchZoomDialog::on_pushButton_clicked() {
  emit NotifyZoom(true);
}

void SwitchZoomDialog::OnAdjustControls() {
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (ref_->isVisible()) {
    QRect cur_rect = this->rect();
    QRect par_rect = parent_->geometry();
    QPoint par_top_left = parent_->mapToGlobal(par_rect.topLeft());

    QRect dst_rect(par_top_left.x(), par_top_left.y(),
                   cur_rect.width(), cur_rect.height());

    // move
    if (cur_rect != dst_rect) {
      this->move(dst_rect.x(), dst_rect.y());
    }

    //if (!this->isActiveWindow()) {
    //  this->activateWindow();
    //}
    // show
    if (this->isHidden()) {
      this->show();
    }
  } else {
    // hide
    if (!this->isHidden()) {
      this->hide();
    }
  }
}