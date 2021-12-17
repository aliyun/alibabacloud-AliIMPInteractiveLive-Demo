#pragma once
#include <string>
#include <vector>
#include <map>
class SignUtil
{

public:
  /*
  * 签名公共参数HeaderKey
  */
  const static std::string kAppId;
  const static std::string kSignature; // 签名结果串
  const static std::string kSignatureMethod;
  const static std::string kTimestamp;
  const static std::string kSignatureVersion;
  const static std::string kSignatureNonce;

  // 签名算法固定值
  const static std::string kSignatureMethodValue;

  // 签名算法版本固定值
  const static std::string kSignatureVersionValue;

  // 签名算法参数
  const static std::string kAlgorithmName;
  const static std::string kEncoding;
  const static std::string kUrlEncoding;

  /**
   * 获取签名
   *
   * @param signSecret 签名密钥
   * @param method     {@link }，不区分大小写
   * @param path       示例：/api/service，必须以"/"开头
   * @param params     请求参数
   * @param headers    请求头部（含签名），仅读取以下头部，header 名全小写
   *                     业务参数：
   *                      「a-token」
   *                     公共参数：
   *                      「a-app-id」
   *                      「a-signature」
   *                      「a-signature-method」
   *                      「a-timestamp」
   *                      「a-signature-version」
   *                      「a-signature-nonce」
   * @return sign      签名
   */
  static std::string GetSign(
    std::string signSecret, 
    std::string method, 
    std::string path, 
    std::map<std::string, std::string> params,
    std::map<std::string, std::string> headers);

  static std::string GetUTCTimeStampFormatString();

  static std::string GenerateNonce();
private:
  /**
 * 将参数排序后，进行规范化设置，组合成请求字符串
 * @param queryParamsMap   所有请求参数
 * @return 规范化的请求字符串
 */
  static std::string CanonicalizedQuery(std::map<std::string, std::string> query_params_map);

  /**
   * URL编码
   * 使用UTF-8字符集按照 RFC3986 规则编码请求参数和参数取值
   */
  static std::string PercentEncode(std::string value);

  /***
   * 构造签名字符串
   * @param method       HTTP请求的方法
   * @param urlPath      HTTP请求的路径
   * @param queryString  规范化的请求字符串
   * @param headerString 规范化的头部字符串
   * @return 签名字符串
   */
  static std::string BuildSignString(std::string method, std::string urlPath, std::string queryString, std::string headerString);

  /***
   * 计算签名
   * @param stringToSign 签名字符串
   * @param signSecret   签名密钥
   * @return 计算得到的签名
   */
  static std::string Sign(std::string string_to_sign, std::string sign_secret);

  const static std::vector<std::string> processed_headers;
};

