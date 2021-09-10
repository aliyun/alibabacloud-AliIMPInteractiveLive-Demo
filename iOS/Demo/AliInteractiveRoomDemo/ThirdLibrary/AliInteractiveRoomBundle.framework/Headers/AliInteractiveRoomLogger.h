//
//  AliInteractiveRoomLogger.h
//  AliInteractiveRoomBundle
//
//  Created by fernando on 2021/8/27.
//  Copyright © 2021 AliYun. All rights reserved.
//

#ifndef AliInteractiveRoomLogger_h
#define AliInteractiveRoomLogger_h


void AIRBLogVerbose(const char* _Nullable format, ...);
void AIRBLogDebug(const char* _Nullable format, ...);
void AIRBLogInfo(const char* _Nullable format, ...);
void AIRBLogWarning(const char* _Nullable format, ...);
void AIRBLogError(const char* _Nullable format, ...);

#define LOGV(...) AIRBLogVerbose(__VA_ARGS__)
#define LOGD(...) AIRBLogDebug(__VA_ARGS__)
#define LOGI(...) AIRBLogInfo(__VA_ARGS__)
#define LOGW(...) AIRBLogWarning(__VA_ARGS__)
#define LOGE(...) AIRBLogError(__VA_ARGS__)


#endif /* AliInteractiveRoomLogger_h */
