#pragma once
#include <QNetworkAccessManager>
#include <QEventLoop>
#include <QNetworkReply>
#include <QObject>

class HttpHelper : public QObject {
  Q_OBJECT
public:
  HttpHelper();
  std::string GetResult(const std::string& url, std::map<std::string, std::string>& headers, const uint32_t& timeout_ms = 0, const std::function<void()>& timeout_callback = nullptr);
private:
  QNetworkAccessManager * network_manager_ = nullptr;
  QEventLoop *loop_ = nullptr;
};