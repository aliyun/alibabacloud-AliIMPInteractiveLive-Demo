//
//  AliRtcEngine+Plugin.h
//  PluginAudio
//
//  Created by mt on 2021/2/5.
//  Copyright © 2021 mt. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AliRtcEngine.h"

/**
 * @brief 错误码
*/
enum AliRtcPluginErrorCode{
  AliRtcPluginErrorCodeSuccess = 0,
  AliRtcPluginErrorCodeFailed = -1,
  AliRtcPluginErrorNoFindPlugin = -2,
  AliRtcPluginErrorParameterError = -3,
  AliRtcPluginErrorLoadError = -4,
  AliRtcPluginErrorCreateError = -5,
  AliRtcPluginErrorInitError = -6,
  AliRtcPluginErrorInitTokenError = -7,
};

/**
 * @brief 数据处理返回码
*/
enum AliRtcPluginProcessCode{
  AliRtcPluginProcessCodeFailed = -1,
  AliRtcPluginProcessCodeSuccess = 0,
  AliRtcPluginProcessCodeMemoryChange = 1,
};

/**
 * @brief 插件类型
*/
enum AliRtcPluginDataType{
  AliRtcPluginDataTypeShared = 0,  /* 共享型 */
  AliRtcPluginDataTypeVideo,       /* 视频 */
  AliRtcPluginDataTypeAudio,       /* 音频 */
  AliRtcPluginDataTypeEncryption,  /* 加密 */
  AliRtcPluginDataTypeMax,
};

/**
 * @brief 插件类型
*/
enum AliRtcPluginOperationType{
  AliRtcPluginShared = 0,          /* 共享型 */
  AliRtcPluginPreOperation,        /* 前处理 */
  AliRtcPluginPostOperation,       /* 后处理 */
  AliRtcPluginEncodeOperation,     /* 编码器 */
  AliRtcPluginDecodeOperation,     /* 解码器 */
  AliRtcPluginRecord,              /* 采集 */
  AliRtcPluginRender,              /* 渲染 */
};


@interface AliRtcEngine(Plugin)

/**
 * @brief SDK添加音视频处理算法插件
 * @param moduleName 插件包名
 * @param plugnName 插件类名
 * @param pluginType 插件类型
 * @return return >0 为pluginID，return<0 Failure
*/
- (int32_t)enablePlugin:(NSString *_Nullable)moduleName plugnName:(NSString *_Nullable)plugnName pluginType:(int)pluginType  opType:(uint32_t)opType option:(const void*_Nullable)option;

/**
 * @brief 删除插件
 * @return return=0 Success
*/
- (int32_t)removePlugin:(uint32_t)pluginId;

/**
 * @brief 设置插件参数
 * @param pluginId 插件ID,由 enablePlugin接口 返回
 * @param opType 参数
 * @param option 参数值
 * @return return=0 Success
*/
- (int32_t)setPluginOption:(uint32_t)pluginId opType:(uint32_t)opType option:(const void* _Nullable )option;

/**
 * @brief 获取插件设置
 * @param pluginId 插件ID,由 enablePlugin接口 返回
 * @param opType 参数
 * @param option 查找结果
 * @return return=0 Success
*/
- (int32_t)getPluginOption:(uint32_t)pluginId opType:(uint32_t)opType option:(void* _Nonnull )option;
@end
