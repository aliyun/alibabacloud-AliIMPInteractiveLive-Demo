#include "small_preview_controls.h"
#include "common/icon_font_helper.h"
#include "ui_small_preview_controls.h"


CPreviewControlsWidget::CPreviewControlsWidget(QWidget* parent, QWidget* listen)
  : QDialog(parent, Qt::FramelessWindowHint | Qt::Tool),
  parent_(parent),
  ui(new Ui::PreviewControls) {
  ui->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);
  UpdateBtnStatus(false);
  setMouseTracking(true);

  filter_ = std::make_unique<QWidgetEventFilter>();
  listen->installEventFilter(filter_.get());

  connect(filter_.get(), &QWidgetEventFilter::signalMove, this,
          &CPreviewControlsWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this,
          &CPreviewControlsWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this,
          &CPreviewControlsWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this,
          &CPreviewControlsWidget::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this,
          &CPreviewControlsWidget::OnAdjustControls);
}

CPreviewControlsWidget::~CPreviewControlsWidget() {
}

void CPreviewControlsWidget::UpdateBtnStatus(bool checked) {
  IconFontHelper::Instance()->SetIcon(ui->zoomBtn, kSwitchView, 20);
}

void CPreviewControlsWidget::on_zoomBtn_clicked() {
  bool checked = ui->zoomBtn->isChecked();
  UpdateBtnStatus(checked);
  emit NotifyZoom(checked);
}

void CPreviewControlsWidget::OnAdjustControls() {
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (parent_->isVisible()) {
    this->mapToGlobal(this->rect().topLeft());

    QRect cur_rect = this->rect();
    QRect par_rect = par_rect = parent_->geometry();

    // move
    QPoint par_top_left = parent_->mapToGlobal(par_rect.topLeft());
    QPoint cur_top_left = this->mapToGlobal(cur_rect.topLeft());
    if (cur_top_left != par_top_left) {
      this->move(par_top_left.x(), par_top_left.y());
    }

    // resize
    if (cur_rect.width() != par_rect.width() ||
        cur_rect.height() != par_rect.height()) {
      this->resize(par_rect.width(), par_rect.height());
    }

    // show
    if (this->isHidden()) {
      this->show();
      if (!this->isTopLevel()) {
        this->raise();
      }
    }
  } else {
    // hide
    if (!this->isHidden()) {
      this->hide();
    }
  }
}