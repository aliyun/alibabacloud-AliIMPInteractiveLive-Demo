#include "prm_map.h"

PrmMap::PrmMap(int& argc, char* argv[]) {
  for (int i = 0; i < argc; ++i) {
    QString tmp(argv[i]);
    if (!tmp.contains("--")) continue;
    auto res = FindKeyValue(tmp);
    if (res.first == "") continue;
    mymap_[res.first] = res.second;
  }
}

const std::string PrmMap::GetSwitch(const std::string& s) {
  if (mymap_.find(s) == mymap_.end())
    return "";
  else
    return mymap_[s];
}

bool PrmMap::HasSwitch(const std::string& s) {
  if (mymap_.find(s) == mymap_.end())
    return false;
  else
    return true;
}

std::pair<std::string, std::string> PrmMap::FindKeyValue(
    const QString& switcher) {
  QStringList switchlist = switcher.split("--");
  if (switchlist.size() != 2) return {"", ""};
  auto switchstr = switchlist[1];
  int idx = switchstr.indexOf('=');
  if (-1 == idx || idx == switchstr.size() - 1)
    return {switchstr.toStdString(), ""};
  return {switchstr.mid(0, idx).toStdString(),
          switchstr.mid(idx + 1).toStdString()};
}