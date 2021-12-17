#pragma once

#include <QLabel>
#include "ui/UiDataDefine.h"


class MultiIconLabel : public QLabel
{
  Q_OBJECT

public:
  MultiIconLabel(QWidget *parent = nullptr);
  ~MultiIconLabel();

  void SetMoreIconList(const QVector<IconData>& icon_list);
  void SetDelta(const QPoint& pt);
protected:
  virtual void paintEvent(QPaintEvent * event) override;

protected:
  QVector<IconData> icon_list_;
  QPoint delta_;
};
