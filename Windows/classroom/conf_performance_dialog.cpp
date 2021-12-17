#include "conf_performance_dialog.h"
#include "ui_conf_performance_dialog.h"
#include "QGraphicsEffect"


ConfPerformanceDialog::ConfPerformanceDialog(QWidget* parent) :
  QDialog(nullptr, Qt::FramelessWindowHint),
  ui_(new Ui::ConfPerformanceDialog) {
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground);

  // focus
  setFocusPolicy(Qt::NoFocus);
  installEventFilter(this);
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
}


ConfPerformanceDialog::~ConfPerformanceDialog() {
  delete ui_;
}

void ConfPerformanceDialog::ShowAtTopLeft(int px, int py) {
  show();
  move(px, py);
  activateWindow();
}

void ConfPerformanceDialog::UpdateCpuInfo(const QString& str) {
  ui_->cpu->setText(str);
}

void ConfPerformanceDialog::UpdateBitrate(const QString& str) {
  ui_->network->setText(str);
}

void ConfPerformanceDialog::UpdateLantency(const QString& str) {
  ui_->latency->setText(str);
}

void ConfPerformanceDialog::UpdateMemory(const QString& str) {
  ui_->memory->setText(str);
}

bool ConfPerformanceDialog::eventFilter(QObject* target, QEvent* event) {
  // window not used, become deactivate
  if (QEvent::WindowDeactivate == event->type()) {
    QWidget* activeWnd = QApplication::activeWindow();
    if (activeWnd == this) {
      return false;
    }

    hide();
    return true;
  }

  return false;
}
