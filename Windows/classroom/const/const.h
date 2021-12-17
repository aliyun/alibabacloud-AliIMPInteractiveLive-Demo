#pragma once
#include <string>

class Const
{
public:
  /**
   * kAppId (来自于阿里云控制台)
   */
  const static std::string kAppId;

  /**
   * APP_KEY (来自于阿里云控制台)
   */
  const static std::string kAppKey;

  /**
   * 验签公钥 (用户服务端按需选择)
   */
  const static std::string kSignSecret;

  /**
   * 用户服务端链接地址 (接入时替换为自己的服务地址)
   */
  const static std::string kAppServerUrl;

private:

};


