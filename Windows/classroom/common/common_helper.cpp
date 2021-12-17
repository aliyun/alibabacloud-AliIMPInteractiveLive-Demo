#include "common_helper.h"
#include <QTimer>
#include <QApplication>

QString str2qstr(const std::string& str)
{
  return QString::fromLocal8Bit(str.data());
}

std::string qstr2str(const QString& qstr)
{
  QByteArray cdata = qstr.toLocal8Bit();
  return std::string(cdata);
}

void ForceExit(int32_t delay_ms) {
  QTimer::singleShot(delay_ms, []() {
    QApplication::quit();
  });
}