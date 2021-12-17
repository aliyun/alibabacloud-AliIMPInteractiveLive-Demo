#include "title_widget_vm.h"
#include "meta_space.h"
#include "common/http_helper.h"
#include "view/view_component_manager.h"
#include "view/interface/i_main_window.h"
#include "QDateTime"
#include "QTimer"

#ifdef WIN32
#include <windows.h>
#include <psapi.h>
#pragma comment(lib, "psapi.lib")
#endif
#include "event/event_manager.h"



TitleWidgetVM::TitleWidgetVM(){

  timer_ = new QTimer(this);
  auto event_manager = EventManager::Instance();
  connect(event_manager.get(), &EventManager::SignalRtcStatus, this, &TitleWidgetVM::OnRtcStatus);
  timer_->start(5000);

}

void TitleWidgetVM::UpdateClassroomId(const std::string& class_room_id, const std::string& title) {
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.class_room_code = class_room_id;
    param_.title = title;
  }
  int32_t field = TitleWidgetFiled_ClassId |
    TitleWidgetFiled_Memory | TitleWidgetFiled_Network | TitleWidgetFiled_Cpu;
  emit SignalUpdateVM(field);
}

#ifdef WIN32
int GetMemoryUsageInternal() {
  HANDLE hProcess = GetCurrentProcess();
  PROCESS_MEMORY_COUNTERS pmc;
  if (GetProcessMemoryInfo(hProcess, &pmc, sizeof(pmc)))
  {
    return pmc.WorkingSetSize / (1024 * 1024);
  }
  return 0;
}

void GetMemoryUsage(int32_t& used, int32_t& total) {
  MEMORYSTATUSEX sys_mem_status;
  sys_mem_status.dwLength = sizeof(MEMORYSTATUSEX);
  if (!GlobalMemoryStatusEx(&sys_mem_status)) {
    used = 0;
    total = 0;
    return;
  }

  total = sys_mem_status.ullTotalPhys / 1024 / 1024;
  used = (sys_mem_status.ullTotalPhys - sys_mem_status.ullAvailPhys) / 1024 / 1024;
}
#endif

void TitleWidgetVM::OnRtcStatus(const alibaba::meta::AliRtcStats & event) {
  process_cpu_usage_ = event.app_cpu;
  system_cpu_usage_ = event.system_cpu;
  param_.cpu = process_cpu_usage_ + 0.5;
  param_.memory = GetMemoryUsageInternal();
  GetMemoryUsage(param_.memory_used, param_.memory_total);
  int32_t field = TitleWidgetFiled_Cpu | TitleWidgetFiled_Memory;
  emit SignalUpdateVM(field);
}

void TitleWidgetVM::UpdateStartTime(bool started, int64_t time) {
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.start_time = time;
    param_.class_started = started;
  }
  int32_t field = TitleWidgetFiled_StartTime;
  emit SignalUpdateVM(field);
}


void TitleWidgetVM::UpdateNetwork(double latency, int64_t bitrate) {
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.network_latency = latency;
    param_.bitrate = bitrate;
  }
  int32_t field = TitleWidgetFiled_Network;
  emit SignalUpdateVM(field);
}

TitleWidgetModel TitleWidgetVM::GetTitleWidgetModel() {
  std::lock_guard<std::mutex> locker(param_mutex_);
  return param_;
}

void TitleWidgetVM::OnNotifyPerformanceData(int32_t cpu, int32_t memory, int32_t network) {
  {
    std::lock_guard<std::mutex> locker(param_mutex_);
    param_.cpu = cpu;
    param_.memory = memory;
    param_.network_latency = network;
  }
  int32_t field = TitleWidgetFiled_Cpu | TitleWidgetFiled_Memory | TitleWidgetFiled_Network;
  emit SignalUpdateVM(field);
}