#include "thumbnail_loader_http_requester.h"

namespace thumbnail_loader
{

HttpRequester::HttpRequester(std::shared_ptr<QNetworkAccessManager> net_mgr)
    : net_mgr_(net_mgr)
{
    connect(net_mgr_.get(), &QNetworkAccessManager::finished, this, &HttpRequester::OnRequestFinish);
}
HttpRequester::~HttpRequester()
{
    disconnect(net_mgr_.get(), &QNetworkAccessManager::finished, this, &HttpRequester::OnRequestFinish);
}

void HttpRequester::SetResponseCallback(ResponseCallback cb)
{
    std::unique_lock<std::mutex> lock(cb_mutex_);
    cb_ = cb;
}

bool HttpRequester::Request(const std::string& url, const int32_t timeout_ms, const uint32_t max_retry_count)
{
    std::unique_lock<std::mutex> lock(mutex_);
    if (doing_request_) return false;

    doing_request_ = true;

    auto thiz = shared_from_this();
    QTimer::singleShot(0, [this, thiz, timeout_ms, max_retry_count, url](){
        cancel_ = false;
        timeout_ms_ = timeout_ms;
        max_retry_count_ = max_retry_count;
        try_count_ = 0;
        url_ = url;
        AsyncRequest();
        }
    );

    return true;
}
void HttpRequester::Cancel()
{
    auto thiz = shared_from_this();
    QTimer::singleShot(0, [this, thiz] {
        cancel_ = true;
        if (reply_) {
            reply_->abort();
        }
    });
}
void HttpRequester::WaitFinish()
{
    std::unique_lock<std::mutex> lock(mutex_);
    while(doing_request_) {
        cv_.wait(lock);
    }
}


void HttpRequester::AsyncRequest()
{
    self_ = shared_from_this();

    ++try_count_;
    QNetworkRequest request(QUrl(url_.c_str()));
    reply_ = net_mgr_->get(request);

    timer_.setInterval(timeout_ms_);
    timer_.setSingleShot(true);
    connect(&timer_, &QTimer::timeout, reply_, &QNetworkReply::abort);
    timer_.start();
}
void HttpRequester::OnRequestFinish(QNetworkReply* reply)
{
    if (reply != reply_) return;

    bool ok = false;
    QByteArray data;
    timer_.stop();
    disconnect(&timer_, &QTimer::timeout, reply_, &QNetworkReply::abort);

    if (reply->isFinished() && reply->error() == QNetworkReply::NoError) {
        ok = true;
        data = reply->readAll();
    }
    reply_->deleteLater();
    reply_ = nullptr;
    bool need_retry = !ok && try_count_ < max_retry_count_ && !cancel_;

    cancel_ = false;

    if (need_retry) {
        AsyncRequest();
    }
    else {

        {
            std::unique_lock<std::mutex> lock(mutex_);
            doing_request_ = false;
            cv_.notify_all(); 
        }
        {
            std::unique_lock<std::mutex> lock_cb(cb_mutex_);
            if (cb_) {
                cb_(this, ok, data);
            }
        }
    }

    self_.reset();
}

}
