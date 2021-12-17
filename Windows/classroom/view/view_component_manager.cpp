#include "view_component_manager.h"
#include "Interface/base_interface.h"

#include <mutex>
#include <QObject>
#include "login_window.h"
#include "main_window.h"
#include "class_main_window.h"
#include "toast_widget.h"

QWidget* ViewFactory::GetViewInstance(const std::string& cls_name, QWidget* parent) {
  QWidget* view_prt = nullptr;
  if (cls_name == kLoginWindow) {
    auto main_window = new LoginWindow(parent);
    view_prt = main_window;
  } else if (cls_name == kMainWindow) {
    auto main_window = new ClassMainWindow(parent);
    view_prt = main_window;
  } else if (cls_name == kToastWindow) {
    auto toast = new ToastWidget(nullptr);
    view_prt = toast;
  }

  return view_prt;
}


std::once_flag ViewComponentManager::once_flag_;

ViewComponentManager* ViewComponentManager::instance() {
  static ViewComponentManager* instance = nullptr;
  std::call_once(once_flag_, [&]() {
    instance = new ViewComponentManager;
  });
  return instance;
}

QWidget* ViewComponentManager::GetView(const std::string& cls_name, QWidget* parent_if_create) {
  QWidget* view_prt = nullptr;
  auto i_find = map_view_.find(cls_name);
  if (i_find != map_view_.end()){
    view_prt = i_find->second;
  }
  else {
    view_prt = ViewFactory::GetViewInstance(cls_name, parent_if_create);
    map_view_[cls_name] = view_prt;

    auto ibase = dynamic_cast<IBase*>(view_prt);
    if (ibase) {
      ibase->InitAfterCreate();
    }
  }
  return view_prt;
}

bool ViewComponentManager::HasView(const std::string& cls_name) {
  auto i_find = map_view_.find(cls_name);
  return i_find != map_view_.end();
}

void ViewComponentManager::BindViewConnect(const std::string& sender_obj_name, const std::string& recv_obj_name) {
  QWidget* sender_widget = nullptr;
  QWidget* recv_widget = nullptr;

  auto i_find = map_view_.find(sender_obj_name);
  if (i_find != map_view_.end()) {
    sender_widget = i_find->second;
  }
  i_find = map_view_.find(recv_obj_name);
  if (i_find != map_view_.end()) {
    recv_widget = i_find->second;
  }
}

ViewComponentManager::ViewComponentManager() {
}

ViewComponentManager::~ViewComponentManager() {
}
