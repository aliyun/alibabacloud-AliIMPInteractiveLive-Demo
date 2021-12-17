#include "MultiIconLabel.h"
#include <QPainter>

MultiIconLabel::MultiIconLabel(QWidget *parent)
  : QLabel(parent) {
}

MultiIconLabel::~MultiIconLabel() {
}

void MultiIconLabel::SetMoreIconList(const QVector<IconData>& icon_list) {
  icon_list_ = icon_list;
}

void MultiIconLabel::SetDelta(const QPoint& pt) {
  delta_ = pt;
}

void MultiIconLabel::paintEvent(QPaintEvent * event) {
  // draw default icon
  QLabel::paintEvent(event);

  QPainter painter(this);
  painter.setRenderHint(QPainter::Antialiasing, true);
  auto left_top = rect().topLeft();
  left_top += delta_;
  auto rt = rect();
  rt.moveTopLeft(left_top);

  // draw mulit-icon
  const auto& cur_font = font();
  painter.setFont(cur_font);
  for (auto icon_data : icon_list_) {
    painter.setPen(QColor(icon_data.icon_color));
    painter.drawText(rt, Qt::AlignCenter, icon_data.icon_code);
  }
}
