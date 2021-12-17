#pragma once
#include <QString>
#include <QObject>
#include <QDebug>
class LogUtil : public QObject
{
  Q_OBJECT
public:
  static void Debug(const QString& log_context);
 signals:
  void Log(const QString& log_context);

private slots:
  void Dolog(const QString& log_context);

private:
  static LogUtil& Instance();
  LogUtil();
};

