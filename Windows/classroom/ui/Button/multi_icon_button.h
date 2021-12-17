#pragma once

#include <QPushButton>
#include <QToolButton>
#include "ui/UiDataDefine.h"

namespace Ui { class MultiIconButton; };

class QLabel;



class MultiIconButton : public QToolButton {
  Q_OBJECT

public:
  MultiIconButton(QWidget *parent = Q_NULLPTR);
  ~MultiIconButton();

  void SetIcon(const QChar& iconfont_code, int font_size);
  void SetIconFontList(const QVector<IconData>& icon_list);
  void SetDelta(const QPoint& pt);
private:
  QLabel* GetIconLabel() const;

private:
  Ui::MultiIconButton *ui;
};
