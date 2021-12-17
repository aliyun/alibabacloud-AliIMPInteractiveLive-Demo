#pragma once
#include <map>
#include <string>
#include <vector>
#include <set>
#include <functional>

class VideoDisplayInfo {
 public:
  VideoDisplayInfo() {}
  ~VideoDisplayInfo() {}

 public:
  std::string user_id_ = "";
  void* hwnd_ = nullptr;
  bool is_displaying_ = false;
  uint64_t index_ = 0;
};

class VideoDisplayMgr {
 public:
  enum DisplayMode {
    MainMode = 0,
    SpeakerMode = 1,
    GridMode = 2,
  };



  VideoDisplayMgr(std::function<void(std::string user_id, void* hwnd)> start_play_func, std::function<void(std::string user_id)> stop_play_func);
  ~VideoDisplayMgr();

  void SwitchMode(DisplayMode mode);

  int PageNum();

  void SetPageIndex(int page_index);

  std::vector<VideoDisplayInfo> GetDisplayInfoList();

  void SetMainDisplay(std::string user_id);

  VideoDisplayInfo GetMainDisplayInfo();

  void AddVideoDisplayInfo(std::string user_id);

  void DelVideoDisplayInfo(std::string user_id);

  VideoDisplayMgr::DisplayMode GetDisplayMode();

  void AddHwnd(void* hwnd);

  int GetLinmMicNum();

  std::set<void*> GetToDestroyHwnd();

  void ClearToDestroyHwnd();
  private:
  std::string GetPlayUserId();

  bool IsHwndExist(void* hwnd);

  void TryStartPlay();

  void TryStopPlay(std::string user_id);

  void UpdateHwndStatus();
 private:
  DisplayMode display_mode_ = GridMode;
  std::map<std::string, VideoDisplayInfo> display_info_map_;
  std::set<void*> used_hwnd_list_;
  std::set<void*> unuse_hwnd_list_;
  std::set<void*> to_destroy_hwnd_list_;


  std::function<void(std::string user_id, void* hwnd)> start_play_func_;
  std::function<void(std::string user_id)> stop_play_func_;
};
