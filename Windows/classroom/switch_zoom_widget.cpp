#include "switch_zoom_widget.h"
#include "ui_switch_zoom_widget.h"
#include "common/icon_font_helper.h"

SwitchZoomWidget::SwitchZoomWidget(QWidget* parent, QWidget* ref) :
  QWidget(parent, Qt::WindowStaysOnTopHint | Qt::FramelessWindowHint),
  parent_(parent),
  ref_(ref),
  ui_(new Ui::SwitchZoomWidget),
  filter_(new QWidgetEventFilter)
{
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);

  parent->installEventFilter(filter_.get());
  connect(ui_->zoomBtn, SIGNAL(clicked()), this, SLOT(OnClickButton()));
  connect(filter_.get(), &QWidgetEventFilter::signalMove, this, &SwitchZoomWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this, &SwitchZoomWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this, &SwitchZoomWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this, &SwitchZoomWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this, &SwitchZoomWidget::OnAdjustControls);
  IconFontHelper::Instance()->SetIcon(ui_->zoomBtn, kSwitchView);
}
SwitchZoomWidget::~SwitchZoomWidget() {
  disconnect(this);
}

void SwitchZoomWidget::OnNotifyRecodingState(bool is_recording) {
  this->setVisible(is_recording);
}

void SwitchZoomWidget::OnClickButton() {
  emit NotifyZoom(true);
}

void SwitchZoomWidget::OnAdjustControls() {
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (ref_->isVisible()) {
    QRect cur_rect = this->rect();
    QRect ref_rect = ref_->geometry();
    QRect par_rect = parent_->geometry();
    // move
    QPoint ref_top_left = ref_->mapToGlobal(ref_rect.topLeft());
    QPoint cur_top_left = this->mapToGlobal(cur_rect.topLeft());
    QPoint par_top_left = parent_->mapToGlobal(par_rect.topLeft());
    if (cur_top_left.x() - 11 != ref_top_left.x() || cur_top_left.y() - 134 != ref_top_left.y()) {
      int x = ref_top_left.x()/* - par_rect.topLeft().x()*/;
      int y = ref_top_left.y() /*- par_rect.topLeft().y()*/;
      this->move(x/* + 11*/, y/* + 134*/);
    }

    if (!this->isTopLevel()) {
      this->raise();
      ui_->zoomBtn->raise();
    }

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