#include "device_list_vm.h"
#include "meta_space.h"
#include "engine_device_manager.h"
#include "i_rtc.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

std::vector<DeviceItem> DeviceListVM::GetAudioDevice() {
  std::vector<DeviceItem>  device_item_vec;

  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    std::string default_device = rtc_plugin->GetCurrentAudioCaptureMame();
    auto device_list = rtc_plugin->GetAudioCaptureList();
    for (auto iter = device_list.begin(); iter != device_list.end(); iter++) {
      DeviceItem item{ iter->device_name, iter->device_name == default_device };
      device_item_vec.push_back(item);
    }
  }
  return device_item_vec;
}

std::vector<DeviceItem> DeviceListVM::GetVideoDevice() {
  
  std::vector<DeviceItem>  device_item_vec;
  if (room_ptr_) {
    std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
    std::string default_device = rtc_plugin->GetCurrentVideoCaptureMame();
    auto device_list = rtc_plugin->GetVideoCaptureList();
    for (auto iter = device_list.begin(); iter != device_list.end(); iter++) {
      DeviceItem item{ iter->device_name, iter->device_name == default_device };
      device_item_vec.push_back(item);
    }
  }
  return device_item_vec;
}


std::string DeviceListVM::GetCurrentAudioDevice() {
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  return room_ptr_ ? rtc_plugin->GetCurrentAudioCaptureMame() : "";
}

bool DeviceListVM::SetCurrentAudioDevice(const std::string& device) {
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  return room_ptr_ ? (rtc_plugin->SetCurrentAudioCaptureMame(device) == 0) : false;
}

std::string DeviceListVM::GetCurrentVideoDevice() {
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  return room_ptr_ ? rtc_plugin->GetCurrentVideoCaptureMame() : "";
}

bool DeviceListVM::SetCurrentVideoDevice(const std::string& device) {
  std::shared_ptr<IRtc> rtc_plugin = std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  return room_ptr_ ? (rtc_plugin->SetCurrentVideoCaptureMame(device) == 0) : false;
}

void DeviceListVM::UpdateClassroomId(const std::string& classroom_id) {
  room_ptr_ = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(classroom_id);
}