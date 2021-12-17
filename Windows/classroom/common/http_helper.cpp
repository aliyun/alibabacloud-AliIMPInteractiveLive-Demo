#include "http_helper.h"

#include <QTimer>

#include "util/sign_util.h"

HttpHelper::HttpHelper() {
  network_manager_ = new QNetworkAccessManager;
  loop_ = new QEventLoop;
  connect(network_manager_, SIGNAL(finished(QNetworkReply*)), loop_, SLOT(quit()));
}

std::string HttpHelper::GetResult(
    const std::string& url,
    std::map<std::string, std::string>& headers,
    const uint32_t& timeout_ms,
    const std::function<void()>& timeout_callback) {
  QNetworkRequest request(QUrl(url.c_str()));

  for (auto header : headers) {
    request.setRawHeader(QByteArray::fromStdString(header.first), QByteArray::fromStdString(header.second));
  }

  // timer
  QTimer timer;
  bool is_timer_run = false;
  if (timeout_ms != 0) {
    timer.setInterval(timeout_ms); // 设置超时时间ms
    timer.setSingleShot(true);      // 单次触发 
    connect(&timer, &QTimer::timeout, loop_, &QEventLoop::quit);
    timer.start();
    is_timer_run = true;
  }

  QNetworkReply *reply = network_manager_->get(request);
  loop_->exec();

  std::string result = "";

  if (is_timer_run && !timer.isActive()) {
    disconnect(network_manager_, SIGNAL(finished(QNetworkReply*)), loop_,
               SLOT(quit()));
    reply->abort();
    reply->deleteLater();
    if (timeout_callback) {
      timeout_callback();
    }
  } else {
    timer.stop();
    QByteArray bytes = reply->readAll();
    auto error = reply->error();
    std::string debug_info = reply->errorString().toStdString();
    result = bytes.toStdString();
  }
  return result;
}