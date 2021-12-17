#pragma once
#include <string>
#include <Qstring>

QString str2qstr(const std::string& str);

std::string qstr2str(const QString& qstr);

void ForceExit(int32_t delay_ms);