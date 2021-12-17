#include "thumbnail_loader.h"

namespace thumbnail_loader
{

ThumbnailLoader::ThumbnailLoader()
{
    net_mgr_.reset(new QNetworkAccessManager, [](QNetworkAccessManager* obj){
        obj->deleteLater();
    });

    for (int i = 0; i < 10; ++i) {
        RequesterContextPtr ctx = std::make_shared<RequesterContext>();
        ctx->requester = std::make_shared<HttpRequester>(net_mgr_);
        ctx->requester->SetResponseCallback(
                    std::bind(&ThumbnailLoader::OnHttpRequesterResponse,
                              this,
                              std::placeholders::_1,
                              std::placeholders::_2,
                              std::placeholders::_3));

        requester_ctxs_.push_back(ctx);
        free_requester_ctx_.push_back(ctx);
    }
}
ThumbnailLoader::~ThumbnailLoader()
{
    for (size_t i = 0; i < requester_ctxs_.size(); ++i) {
        requester_ctxs_[i]->requester->SetResponseCallback(nullptr);
        requester_ctxs_[i]->requester->Cancel();
    }
    requester_ctxs_.clear();
    free_requester_ctx_.clear();
    net_mgr_.reset();
}

bool ThumbnailLoader::AddResource(const std::string& group, const uint32_t group_page, const std::string& url)
{
    bool need_tigger_download = false;
    bool ret_ok = false;
    {
        std::unique_lock<std::mutex> lock(mutex_);

        std::string resource_id = MakeResourceID(group, group_page);
        ResourceInfoPtr& resource_info = resource_map_[resource_id];
        if (!resource_info) {
            resource_info = std::make_shared<ResourceInfo>();
            resource_info->url = url;
            resource_info->resource_id = resource_id;
            resource_info->group = group;
            resource_info->group_page = group_page;
            queue_.push(resource_info);
            need_tigger_download = true;
            ret_ok = true;
        }
    }

    if (need_tigger_download) {
        TriggerDownload();
    }

    return ret_ok;
}

void ThumbnailLoader::LoadResource(const std::string& group, const uint32_t group_page)
{
    bool need_tigger_download = false;

    {
        std::unique_lock<std::mutex> lock(mutex_);

        std::string resource_id = MakeResourceID(group, group_page);
        auto found = resource_map_.find(resource_id);
        if (found != resource_map_.end()) {
            ResourceInfoPtr resource_info = found->second;
            bool ok = NotifyResourceLoaded(resource_info, true);

            // 资源还没加载成功
            if (!ok) {
                // 调整优先级下载
                uint64_t property_uint = ++download_property_;
                resource_info->property = property_uint;

                bool need_requeue = true;
                {
                    std::unique_lock<std::mutex> resource_lock(resource_info->mutex);
                    if (!resource_info->loading) {
                        queue_.push(resource_info);
                        need_requeue = false;
                    }
                }
                if (need_requeue) {
                    ReQueueResource();
                }
                need_tigger_download = true;
            }
        }
    }

    if (need_tigger_download) {
        TriggerDownload();
    }
}

std::string ThumbnailLoader::MakeResourceID(const std::string& group, const uint32_t group_page)
{
    std::string resource_id = group;
    char group_page_str[64] = {0};
    sprintf_s(group_page_str, "%u", group_page);
    return group + group_page_str;
}
void ThumbnailLoader::TriggerDownload()
{
    std::unique_lock<std::mutex> lock(mutex_);

    if (queue_.empty()) return;
    if (free_requester_ctx_.empty()) return;

    RequesterContextPtr request_ctx = free_requester_ctx_.back();
    free_requester_ctx_.pop_back();

    ResourceInfoPtr resource = queue_.top();
    queue_.pop();
    {
        std::unique_lock<std::mutex> resource_lock(resource->mutex);
        resource->loading = true;
    }

    request_ctx->working = true;
    request_ctx->resource = resource;
    request_ctx->requester->Request(resource->url);
}
void ThumbnailLoader::OnHttpRequesterResponse(HttpRequester* http_requester, bool ok, QByteArray data)
{
    for (size_t i = 0; i < requester_ctxs_.size(); ++i) {
        RequesterContextPtr requester_ctx = requester_ctxs_[i];
        if (requester_ctx) {
            if (requester_ctx->requester.get() != http_requester) {
                continue;
            }

            ResourceInfoPtr resource_info = requester_ctx->resource;
            requester_ctx->resource.reset();
            requester_ctx->working = false;

            {
                std::unique_lock<std::mutex> lock(mutex_);

                free_requester_ctx_.push_back(requester_ctx);

                {
                    std::unique_lock<std::mutex> resource_lock(resource_info->mutex);
                    resource_info->loaded = ok;
                    resource_info->data = data;
                    resource_info->loading = false;
                }
            }

            NotifyResourceLoaded(resource_info);
            TriggerDownload();
            break;
        }
    }
}
bool ThumbnailLoader::NotifyResourceLoaded(ResourceInfoPtr resource_info, bool only_if_loaded)
{
    std::unique_lock<std::mutex> lock(resource_info->mutex);
    if (only_if_loaded && !resource_info->loaded) return false;
    Q_EMIT SignalResourceLoaded(resource_info->loaded, resource_info->group, resource_info->group_page, resource_info->data);
    return true;
}
void ThumbnailLoader::ReQueueResource()
{
    std::unique_lock<std::mutex> lock(mutex_);
    ResourcePropertyQueue queue;
    while(!queue_.empty()) {
        queue.push(queue_.top());
        queue_.pop();
    }
    queue.swap(queue_);
}

}