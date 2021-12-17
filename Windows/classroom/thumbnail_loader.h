#pragma once

#include "thumbnail_loader_http_requester.h"

namespace thumbnail_loader
{

class ThumbnailLoader : public QObject
{
    Q_OBJECT
public:
    ThumbnailLoader();
    ~ThumbnailLoader();

    bool AddResource(const std::string& group, const uint32_t group_page, const std::string& url);
    void LoadResource(const std::string& group, const uint32_t group_page);

signals:
    void SignalResourceLoaded(const bool loaded, const std::string& group, const uint32_t group_pag, QByteArray data);

private:
    struct ResourceInfo
    {
        std::string resource_id;
        std::string group;
        uint32_t group_page = 0;
        std::string url;

        std::mutex mutex;
        uint64_t property = 0;          // 优先级, 越大越优先
        bool loaded = false;            // 是否加载成功
        bool loading = false;           // 是否正在加载, 队列中等待, 或者请求中
        QByteArray data;
    };
    typedef std::shared_ptr<ResourceInfo> ResourceInfoPtr;

    struct ResourcePropertyCompare
    {
        bool operator()(ResourceInfoPtr &a, ResourceInfoPtr &b) const
        {
            return a->property < b->property;
        }
    };

    struct RequesterContext
    {
        std::shared_ptr<HttpRequester> requester;
        bool working;
        ResourceInfoPtr resource;
    };
    typedef std::shared_ptr<RequesterContext> RequesterContextPtr;

    std::string MakeResourceID(const std::string& group, const uint32_t group_page);
    void TriggerDownload();
    void OnHttpRequesterResponse(HttpRequester* http_requester, bool ok, QByteArray data);
    bool NotifyResourceLoaded(ResourceInfoPtr resource_info, bool only_if_loaded = false);
    void ReQueueResource();

private:
    std::vector<RequesterContextPtr> requester_ctxs_;

    std::mutex mutex_;
    std::vector<RequesterContextPtr> free_requester_ctx_;
    uint64_t download_property_ = 0;
    std::shared_ptr<QNetworkAccessManager> net_mgr_;
    typedef std::priority_queue<ResourceInfoPtr, std::vector<ResourceInfoPtr>, ResourcePropertyCompare> ResourcePropertyQueue;
    ResourcePropertyQueue queue_;
    std::map<std::string, ResourceInfoPtr> resource_map_;
};

}