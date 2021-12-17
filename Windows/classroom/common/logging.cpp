#include "logging.h"
#include <stdio.h>
#include <stdlib.h>
#include <stdarg.h>
#include <mutex>
#include <fstream>
#include <ratio>
#include <windows.h>
#include <shlobj_core.h>
#include <iostream>

#ifdef _DEBUG
static int log_output_level = LOG_DEBUG;
#else
static int log_output_level = LOG_INFO;
#endif

namespace classroom {


static void DefLogHandler(int log_level, const char *format, va_list args) {
  char out[4096];
  vsnprintf(out, sizeof(out), format, args);

  std::cout << out << std::endl;
}

#ifdef _MSC_VER
#define NORETURN __declspec(noreturn)
#else
#define NORETURN __attribute__((noreturn))
#endif


static LogHandler log_handler = DefLogHandler;



void BaseLogVa(int log_level, const char *format, va_list args) {
  log_handler(log_level, format, args);
}

void blog(int log_level, const char *format, ...) {
  va_list args;

  va_start(args, format);
  BaseLogVa(log_level, format, args);
  va_end(args);
}


void LogError(const std::string& tag, const std::string& info, const alibaba::dps::DPSError& err) {
  LogWithTag(tag.c_str(), LOG_ERROR, "%s, errno=%d, reason=%s", info.c_str(), err.code, err.reason.c_str());
}

}
