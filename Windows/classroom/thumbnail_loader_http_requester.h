#pragma once

#include <QtNetwork/QNetworkAccessManager>
#include <QEventLoop>
#include <QNetworkReply>
#include <QObject>
#include <QTimer>
#include <thread>
#include <queue>
#include <memory>
#include <mutex>
#include <functional>
#include <condition_variable>

namespace thumbnail_loader
{

class HttpRequester : public QObject, public std::enable_shared_from_this<HttpRequester>
{
    Q_OBJECT
public:
    HttpRequester(std::shared_ptr<QNetworkAccessManager> net_mgr);
    ~HttpRequester();

    typedef std::function<void(HttpRequester* http_requester, bool ok, QByteArray data)> ResponseCallback;

    void SetResponseCallback(ResponseCallback cb);

    bool Request(const std::string& url, const int32_t timeout_ms = 5000, const uint32_t max_retry_count = 3);
    void Cancel();
    void WaitFinish();

private:

    void AsyncRequest();
    void OnRequestFinish(QNetworkReply* reply);

private:
    std::shared_ptr<QNetworkAccessManager> net_mgr_;
    std::mutex mutex_;
    std::condition_variable cv_;
    bool doing_request_ = false;
    bool cancel_ = false;

    std::mutex cb_mutex_;
    ResponseCallback cb_;

    // keep alive during requesting
    typedef std::shared_ptr<HttpRequester> HttpRequesterPtr;
    HttpRequesterPtr self_;

    std::string url_;
    int32_t timeout_ms_ = 5000;
    uint32_t max_retry_count_ = 3;
    uint32_t try_count_ = 0;
    QNetworkReply *reply_ = nullptr;
    QTimer timer_;
};

}