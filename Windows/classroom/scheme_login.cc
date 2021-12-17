#include "scheme_login.h"
#include <memory>

#include "view/view_component_manager.h"
#include <QUrlQuery> 



std::once_flag SchemeLogin::once_flag_;
SchemeLogin* SchemeLogin::Instance() {
  static SchemeLogin* instance = nullptr;
  std::call_once(once_flag_, [&]() {
    instance = new SchemeLogin;
  });
  return instance;
}


void SchemeLogin::Init(const SchemeInfo& scheme_info) {
  scheme_info_ = scheme_info;
}



SchemeInfo SchemeLogin::GetSchemeInfo() {
  return scheme_info_;
}
