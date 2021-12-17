#pragma once
#include "windows.h"
#include "QTranslator"
#include "QApplication"
#include <string>
#include <sstream>
#include "common/logging.h"
#include "QDir"
#include <map>
#define DEFAULT_LANG "zh-CN"


inline BOOL AdjustProcessCurrentDirectoryClassRoom()
{
#ifdef WIN32
  wchar_t file_name[MAX_PATH];
  GetModuleFileNameW(NULL, file_name, MAX_PATH);
  std::wstring str_file_path = file_name;
  size_t pos = str_file_path.rfind('\\');
  str_file_path = str_file_path.substr(0, pos);
  return SetCurrentDirectoryW(str_file_path.c_str());
#else
  return true;
#endif
}

inline bool check_path(const char* data, const char* path,
  std::string& output) {
  std::ostringstream str;
  str << path << data;
  output = str.str();

  printf("Attempted path: %s\n", output.c_str());
  QDir dir;
  dir.setPath(path);
  
  return dir.exists();
}

inline bool GetDataFilePathClassRoom(const char* data, std::string& output) {
  if (check_path(data, "./data/", output))
    return true;
  return false;
}


class LookupTransLate {
public:
  LookupTransLate(const std::string& path) {
    QFile file(path.c_str());
    if (!file.open(QIODevice::ReadOnly | QIODevice::Text)) {
      return;
    }
    while (!file.atEnd()) {
      QByteArray line = file.readLine();
      QString str(line);
      str = str.trimmed();
      if (str.isEmpty()) {
        continue;
      }
      QString key = str.section('=', 0, 0);
      QString value = str.section('=', 1, 1);
      if (value.length() > 2) {
        value = value.mid(1, value.length() - 2);
      }
      translate_map_[key] = value;
    }
  }
  QString LookUp(const QString& key) {
    return translate_map_[key];
  }
private:
  std::map<QString, QString> translate_map_;
};

class ClassroomApplication : public QApplication {
  Q_OBJECT
public:
  ClassroomApplication(int &argc, char **argv, int flag = QCoreApplication::ApplicationFlags) :QApplication(argc, argv, flag) {}

  inline const char *GetLocale() const {
    return locale.c_str();
  }

  inline std::shared_ptr<LookupTransLate> GetTextLookup() const {
    return text_lookup_;
  }

  inline QString GetString(const char *lookupVal) const {
    return text_lookup_->LookUp(lookupVal);
  }


  bool InitLocale() {

    AdjustProcessCurrentDirectoryClassRoom();

    std::string zh_cn_path;
    if (!GetDataFilePathClassRoom("locale/" DEFAULT_LANG ".ini", zh_cn_path)) {

      return false;
    }

    text_lookup_.reset(new LookupTransLate(zh_cn_path.c_str()));

    return true;
  }
private:
  std::string                    locale;
  std::shared_ptr<LookupTransLate>               text_lookup_;
};

class ClassroomTranslator : public QTranslator {
  Q_OBJECT

public:
  ClassroomTranslator(const ClassroomApplication* application) :application_(application) {};
  virtual bool isEmpty() const override {
    return false;
  }

  virtual QString translate(const char *context, const char *source_text,
    const char *disambiguation, int n) const {

    auto lookup_ptr = application_->GetTextLookup();
    return lookup_ptr->LookUp(source_text);
  }
private:
  const ClassroomApplication* application_ = nullptr;
};