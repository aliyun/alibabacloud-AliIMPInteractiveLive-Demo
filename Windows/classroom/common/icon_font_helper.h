#pragma once

#include <QtGui>
#include <QtWidgets>
#include <qfont.h>
#include <QApplication>
#include "iconfont_define.h"

class IconFontHelper {
private:
  explicit IconFontHelper(QObject*parent = 0);
  QFont icon_font_;
  QString font_name_;
public:
  static IconFontHelper* Instance();
  void SetIcon(QLabel* ctl, QChar c, int size = 16);
  void SetIcon(QPushButton* ctl, QChar c, int size = 16);
  void SetIcon(QCheckBox* ctl, QChar c, int size = 16);
};

#define QTranslate(key) QApplication::translate("", key, nullptr)