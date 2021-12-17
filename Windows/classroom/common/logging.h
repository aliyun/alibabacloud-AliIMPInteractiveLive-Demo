#pragma once
#include <vadefs.h>
#include <fstream>
#include "dps_error.h"

enum {

	LOG_ERROR = 100,

	LOG_WARNING = 200,

	LOG_INFO = 300,

	LOG_DEBUG = 400
};


namespace classroom {
typedef void (*LogHandler)(int lvl, const char *msg, va_list args);


void BaseLogVa(int log_level, const char *format, va_list args);
void blog(int log_level, const char *format, ...);
void LogError(const std::string& tag, const std::string& info, const alibaba::dps::DPSError& err);

#define ClassroomTagLogin "TAG_Login"               //登录相关
#define ClassroomTagLivePlay "TAG_Live"             //直播相关
#define ClassroomTagLinkMic "TAG_LinkMic"           //连麦相关
#define ClassroomTagWhiteboard "TAG_Whiteboard"     //白板相关
#define ClassroomTagLocalMedia "TAG_LocalMedia"     //本地媒体相关
#define ClassroomTagUserList "TAG_UserList"         //成员列表相关
#define ClassroomTagPPT "TAG_PPT"                   //文档相关
#define ClassroomTagRTC "TAG_RTC"                   // TC相关

#define LogWithTag(tag, log_level, fmt, ...) \
  classroom::blog(log_level, "[%s][%s(%d)]: "##fmt, tag, __FUNCTION__, __LINE__, ##__VA_ARGS__)
}