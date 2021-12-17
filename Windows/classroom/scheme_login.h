#pragma once

#include <string>
#include <functional>
#include <QObject>


struct SchemeInfo {
  std::string app_id;
  std::string device_id;
  std::string room_id;
  std::string class_id;
  std::string nick_name;
  std::string uid;
};

class SchemeLogin : public QObject {
  Q_OBJECT
public:
  static SchemeLogin* Instance();

  void Init(const SchemeInfo& scheme_info);

  SchemeInfo GetSchemeInfo();
private:
  SchemeLogin() {};
  ~SchemeLogin() {};

private:
  static std::once_flag once_flag_;
  SchemeInfo scheme_info_;
};

