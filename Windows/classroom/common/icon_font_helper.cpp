#include "icon_font_helper.h"
#include <qmessagebox.h>

IconFontHelper* IconFontHelper::Instance() {
  static IconFontHelper instance;
  return &instance;
}

IconFontHelper::IconFontHelper(QObject*) {
  int font_id = QFontDatabase::addApplicationFont(QString::fromUtf8(":/res/iconfont.ttf"));
  QStringList string_list = QFontDatabase::applicationFontFamilies(font_id);
  if (string_list.size() > 0)
  {
    font_name_ = string_list.at(0);
  }
  icon_font_ = QFont(font_name_);
}

void IconFontHelper::SetIcon(QLabel* ctl, QChar c, int size) {
  if (font_name_.isEmpty()) {
    return;
  }
  if (size > 0) {
    icon_font_.setPixelSize(size);
  }
  ctl->setFont(icon_font_);
  ctl->setText(c);
}

void IconFontHelper::SetIcon(QPushButton* ctl, QChar c, int size) {
  if (font_name_.isEmpty()) {
    return;
  }
  if (size > 0) {
    icon_font_.setPixelSize(size);
  }
  ctl->setFont(icon_font_);
  ctl->setText(c);
}

void IconFontHelper::SetIcon(QCheckBox* ctl, QChar c, int size) {
  if (font_name_.isEmpty()) {
    return;
  }
  if (size > 0) {
    icon_font_.setPixelSize(size);
  }
  ctl->setFont(icon_font_);
  ctl->setText(c);
}