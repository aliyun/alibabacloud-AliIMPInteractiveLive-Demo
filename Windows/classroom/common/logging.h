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

#define ClassroomTagLogin "TAG_Login"               //��¼���
#define ClassroomTagLivePlay "TAG_Live"             //ֱ�����
#define ClassroomTagLinkMic "TAG_LinkMic"           //�������
#define ClassroomTagWhiteboard "TAG_Whiteboard"     //�װ����
#define ClassroomTagLocalMedia "TAG_LocalMedia"     //����ý�����
#define ClassroomTagUserList "TAG_UserList"         //��Ա�б����
#define ClassroomTagPPT "TAG_PPT"                   //�ĵ����
#define ClassroomTagRTC "TAG_RTC"                   // TC���

#define LogWithTag(tag, log_level, fmt, ...) \
  classroom::blog(log_level, "[%s][%s(%d)]: "##fmt, tag, __FUNCTION__, __LINE__, ##__VA_ARGS__)
}