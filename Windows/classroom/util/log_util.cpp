#include "log_util.h"
#include "itime_manager.h"
#include "common/logging.h"


void LogUtil::Debug(const QString& log_context) {
  emit LogUtil::Instance().Log(log_context);
}
LogUtil& LogUtil::Instance() {
  static LogUtil instance;
  return instance;
}

void LogUtil::Dolog(const QString& log_context) { 
  classroom::blog(LOG_INFO, log_context.toStdString().c_str());
}

LogUtil::LogUtil() {
  QObject::connect(this, &LogUtil::Log, this, &LogUtil::Dolog, Qt::AutoConnection);
}