#pragma once
#include <QStringList>
#include <unordered_map>


class PrmMap {
 public:
  explicit PrmMap(int& argc, char* argv[]);
  const std::string GetSwitch(const std::string&); // return "" if not exits
  bool HasSwitch(const std::string&);

 private:
  std::pair<std::string, std::string> FindKeyValue(const QString& switcher);
  std::unordered_map<std::string, std::string> mymap_;
};
