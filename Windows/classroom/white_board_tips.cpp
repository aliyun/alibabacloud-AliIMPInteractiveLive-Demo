#include "white_board_tips.h"
#include "ui_wb_tips.h"


WhiteBoardTips::WhiteBoardTips(QWidget *parent):
  ui(new Ui::WhiteBoardTips)
  , QDialog(parent, Qt::WindowStaysOnTopHint | Qt::FramelessWindowHint | Qt::Tool){
  ui->setupUi(this);

  setAttribute(Qt::WA_TranslucentBackground);
  setAttribute(Qt::WA_ShowWithoutActivating);
  setFocusPolicy(Qt::NoFocus);
  hide();
}

void WhiteBoardTips::SetContent(const QString& tips) {
  ui->content->setText(tips);
}

void WhiteBoardTips::MoveTo(const QPoint& pt) {
  int32_t widget_height = height();
  QPoint target_pt = pt;
  target_pt.setY(pt.y() - widget_height / 2) ;
  target_pt.setX(pt.x() + 6);
  move(target_pt);
}


bool WhiteBoardTips::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    hide();
  }
  return false;
}

void WhiteBoardTips::OnShowTips(const QPoint& pt, const QString& context) {
  SetContent(context);
  MoveTo(pt);
  show();
}

void WhiteBoardTips::OnHideTips() {
  hide();
}