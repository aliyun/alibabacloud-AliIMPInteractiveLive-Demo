#ifndef engine_defined_h
#define engine_defined_h

#ifdef __ANDROID__
#undef ALIRTCEXPORT
#define ALIRTCEXPORT __attribute__((visibility("default")))
#else
#define ALIRTCEXPORT 
#endif

#if defined(_WIN32)
#if defined(ALIRTCSDK_EXPORTS)
#define ALI_RTC_API __declspec(dllexport)
#else
#define ALI_RTC_API __declspec(dllimport)
#endif
#elif defined(__ANDROID__)
#define ALI_RTC_API __attribute__((visibility("default")))
#elif defined(__APPLE__)
#include <TargetConditionals.h>
#define ALI_RTC_API
#else
#define ALI_RTC_API
#endif

#endif
