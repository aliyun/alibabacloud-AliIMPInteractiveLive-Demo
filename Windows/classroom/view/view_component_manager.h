#pragma once

#include "view/view_defines.h"

#include <QWidget>
#include <mutex>


class ViewFactory {
public:
  static QWidget* GetViewInstance(const std::string& cls_name, QWidget* parent = nullptr);
};

class ViewComponentManager {
public:
  static ViewComponentManager* instance();
  QWidget* GetView(const std::string& cls_name, QWidget* parent_if_create = nullptr);
  bool HasView(const std::string& cls_name);
  void BindViewConnect(const std::string& sender_obj_name, const std::string& recv_obj_name);

private:
  ViewComponentManager();
  ~ViewComponentManager();

private:
  static std::once_flag once_flag_;
  std::map<std::string, QWidget*> map_view_;
};

template <class T>
inline T* GetViewComponent(const std::string& cls_name, QWidget* parent_if_create) {
  QWidget* view = ViewComponentManager::instance()->GetView(cls_name, parent_if_create);
  return dynamic_cast<T*>(view);
}

template <class T>
inline T* GetViewComponent(const std::string& cls_name) {
  QWidget* view = ViewComponentManager::instance()->GetView(cls_name, nullptr);
  return dynamic_cast<T*>(view);
}

template <>
inline QWidget* GetViewComponent<QWidget>(const std::string& cls_name) {
  QWidget* view = ViewComponentManager::instance()->GetView(cls_name, nullptr);
  return view;
}

template <>
inline QWidget* GetViewComponent<QWidget>(const std::string& cls_name, QWidget* parent_if_create) {
  QWidget* view = ViewComponentManager::instance()->GetView(cls_name, parent_if_create);
  return view;
}
