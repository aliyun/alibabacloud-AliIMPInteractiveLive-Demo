#include "video_display_mgr.h"

VideoDisplayMgr::VideoDisplayMgr(
    std::function<void(std::string user_id, void* hwnd)> start_play_func,
    std::function<void(std::string user_id)> stop_play_func)
 :start_play_func_(start_play_func), stop_play_func_(stop_play_func){

}

VideoDisplayMgr::~VideoDisplayMgr() {}

void VideoDisplayMgr::SwitchMode(DisplayMode mode) {
  display_mode_ = mode;
}

int VideoDisplayMgr::PageNum() { return 0; }

void VideoDisplayMgr::SetPageIndex(int page_index) {

}

std::vector<VideoDisplayInfo> VideoDisplayMgr::GetDisplayInfoList() {
  std::vector<VideoDisplayInfo> list;
  return list;
}

void VideoDisplayMgr::SetMainDisplay(std::string user_id) {

}

VideoDisplayInfo VideoDisplayMgr::GetMainDisplayInfo() {
  return VideoDisplayInfo();
}

void VideoDisplayMgr::AddVideoDisplayInfo(std::string user_id) {
  if (display_info_map_.find(user_id) != display_info_map_.end()) return;
  VideoDisplayInfo info;
  info.user_id_ = user_id;
  display_info_map_[user_id] = info;
  TryStartPlay();
}

void VideoDisplayMgr::DelVideoDisplayInfo(std::string user_id) {
  if (display_info_map_.find(user_id) == display_info_map_.end()) return;
  TryStopPlay(user_id);
  auto display_info = display_info_map_.find(user_id);
  if (display_info != display_info_map_.end()){
    display_info_map_.erase(display_info);
  }
  UpdateHwndStatus();
}

VideoDisplayMgr::DisplayMode VideoDisplayMgr::GetDisplayMode() {
  return display_mode_;
}

void VideoDisplayMgr::AddHwnd(void* hwnd) {
  if (IsHwndExist(hwnd)) return;
  unuse_hwnd_list_.insert(hwnd);
  TryStartPlay();
}

std::string VideoDisplayMgr::GetPlayUserId() {
  for (auto display_info = display_info_map_.begin();
       display_info != display_info_map_.end(); display_info++) {
    if (display_info->second.is_displaying_ == false) {
      return display_info->first;
    }
  }
  return "";
}

bool VideoDisplayMgr::IsHwndExist(void* hwnd) {
  auto h1 = used_hwnd_list_.find(hwnd);
  auto h2 = unuse_hwnd_list_.find(hwnd);
  return (h1 != used_hwnd_list_.end() || h2 != unuse_hwnd_list_.end());
}

void VideoDisplayMgr::TryStartPlay() {
  auto hwnd = unuse_hwnd_list_.begin();
  std::string user_id = GetPlayUserId();
  if (!user_id.empty() && hwnd!= unuse_hwnd_list_.end()) {
    start_play_func_(user_id, *hwnd);

    auto info = display_info_map_.find(user_id);
    if (info != display_info_map_.end()) {
      info->second.hwnd_ = *hwnd;
      info->second.is_displaying_ = true;
    }

    used_hwnd_list_.insert(*hwnd);
    unuse_hwnd_list_.erase(hwnd);
  }
}

void VideoDisplayMgr::TryStopPlay(std::string user_id) {
  stop_play_func_(user_id);
  auto display_info = display_info_map_.find(user_id);
  if (display_info != display_info_map_.end() && display_info->second.is_displaying_){
    auto hwnd = display_info->second.hwnd_;
    display_info->second.hwnd_ = nullptr;
    display_info->second.is_displaying_ = false;
    unuse_hwnd_list_.insert(hwnd);
    used_hwnd_list_.erase(hwnd);
  }
}

void VideoDisplayMgr::UpdateHwndStatus() {
  int total_user_num = display_info_map_.size();
  int total_hwnd_num = unuse_hwnd_list_.size() + used_hwnd_list_.size();
  if (total_hwnd_num > total_user_num) {
    for (int i = 0; i < total_hwnd_num - total_user_num; i++) {
      auto hwnd = unuse_hwnd_list_.begin();
      if (hwnd == unuse_hwnd_list_.end()) return;
      to_destroy_hwnd_list_.insert(*hwnd);
      unuse_hwnd_list_.erase(hwnd);
    }
  }
}

int VideoDisplayMgr::GetLinmMicNum() {
  return display_info_map_.size();
}

std::set<void*> VideoDisplayMgr::GetToDestroyHwnd() {
  return to_destroy_hwnd_list_;
}

void VideoDisplayMgr::ClearToDestroyHwnd() {
  to_destroy_hwnd_list_.clear();
}