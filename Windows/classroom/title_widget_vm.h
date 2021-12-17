#pragma once
#include <QObject>
#include <mutex>
#include "meta/ali_rtc_stats.h"


enum TitleWidgetFiled {
  TitleWidgetFiled_Cpu = 1,
  TitleWidgetFiled_Memory = 1 << 1,
  TitleWidgetFiled_Network = 1 << 2,
  TitleWidgetFiled_StartTime = 1 << 3,
  TitleWidgetFiled_ClassId = 1 << 4,
};


struct TitleWidgetModel {
  int32_t cpu = 0;
  int32_t memory = 0;
  int32_t memory_total = 0;
  int32_t memory_used = 0;
  double network_latency = 0;
  std::string class_room_code;
  int64_t start_time = 0;
  int64_t bitrate = 0;
  std::string title;
  bool class_started = false;
};

class QTimer;

class TitleWidgetVM : public QObject {
    Q_OBJECT
public:
  TitleWidgetVM();
  void UpdateClassroomId(const std::string& class_room_id, const std::string& title);
  void UpdateStartTime(bool started, int64_t time);
  void UpdateNetwork(double latency, int64_t bitrate);
  TitleWidgetModel GetTitleWidgetModel();
signals:
  void SignalUpdateVM(int32_t field);
  
private slots:
  void OnNotifyPerformanceData(int32_t cpu, int32_t memory, int32_t network);
  void OnRtcStatus(const alibaba::meta::AliRtcStats & event);
private:
  TitleWidgetModel param_;
  std::mutex param_mutex_;

  double process_cpu_usage_ = 0.0;
  double system_cpu_usage_ = 0.0;
  uint64_t tick_count_ = 0;
  QTimer* timer_ = nullptr;
};

