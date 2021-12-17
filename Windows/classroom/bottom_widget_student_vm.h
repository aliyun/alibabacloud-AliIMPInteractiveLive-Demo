#pragma once
#include <QObject>
#include <mutex>
#include <functional>
#include "bottom_widget_vm.h"
#include "meta/mute_message_event.h"

class BottomWidgetStudentVM : public BottomWidgetVM {
  Q_OBJECT
 public:
  BottomWidgetStudentVM();
  ~BottomWidgetStudentVM();

signals:
  void SendRtcMuteToClient(bool mute);

 public:
  void ReqLinkMic(std::function<void()> on_success, std::function<void()> on_failure);
  void ReqQuitLinkMic(std::function<void()> on_success, std::function<void()> on_failure);
  void QuitClass();
  void UpdateLinkMicStatus(bool is_link_mic);
public slots:
  void OnRtcMuteMessage(const alibaba::meta::MuteMessageEvent& event);
 private:
  std::atomic<bool> is_link_mic_ = false;
  std::atomic<bool> is_mute_rtc_ = false;
};

