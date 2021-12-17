#include "conf_share_dialog.h"
#include "ui_conf_share_dialog.h"
#include <QClipboard>
#include "QGraphicsEffect"


ConfShareDialog::ConfShareDialog(QWidget* parent) :
  QDialog(nullptr, Qt::FramelessWindowHint),
  ui_(new Ui::ConfShareDialog) {
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
  ui_->titleWidget->layout()->setAlignment(ui_->copyBtn, Qt::AlignTop);
}


ConfShareDialog::~ConfShareDialog() {
  delete ui_;
}

void ConfShareDialog::ShowAtTopLeft(int px, int py) {
  show();
  move(px, py);
  activateWindow();
}

bool ConfShareDialog::eventFilter(QObject* target, QEvent* event) {
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


void ConfShareDialog::UpdateTitle(const std::string& title) {
  if (title.empty()) {
    return;
  }
  ui_->title->document()->setPlainText(QString::fromStdString(title));
  ui_->title->adjustSize();
  resize(width(), 1);
}


void ConfShareDialog::UpdateClassId(const std::string& class_id){
  ui_->code->setText(QString::fromStdString(class_id));
}

void ConfShareDialog::on_copyBtn_clicked() {
  QString title = ui_->title->document()->toPlainText();
  QString code = ui_->code->text();
  QString copy_data = QString("%1\n%2\n").arg(title).arg(code);
  QClipboard *board = QApplication::clipboard();
  board->clear();
  board->setText(copy_data);
}

