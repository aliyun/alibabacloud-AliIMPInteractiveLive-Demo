#pragma once
#include <QObject>
#include <mutex>
#include "bottom_widget_vm.h"
#include "i_room.h"

enum DeviceListField {


};

struct DeviceItem {
  std::string name;
  bool selected = false;
};



class DeviceListVM : public QObject{
  Q_OBJECT
public:
  void UpdateClassroomId(const std::string& classroom_id);
  std::vector<DeviceItem> GetAudioDevice();
  std::string GetCurrentAudioDevice();
  bool SetCurrentAudioDevice(const std::string& device);
  std::vector<DeviceItem> GetVideoDevice();
  std::string GetCurrentVideoDevice();
  bool SetCurrentVideoDevice(const std::string& device);

signals:
  void SignalUpdateVM(int32_t field);
private:
  std::shared_ptr<alibaba::meta_space::IRoom> room_ptr_;
};
