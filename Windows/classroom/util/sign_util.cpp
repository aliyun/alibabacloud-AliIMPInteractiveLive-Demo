#include "sign_util.h"
#include <algorithm>
#include <QCryptographicHash>
#include <QUuid>
#include <QDateTime>
#include <QTextCodec>

std::string& replace_str(std::string& str, const std::string& to_replaced, const std::string& newchars)
{
  for (std::string::size_type pos(0); pos != std::string::npos; pos += newchars.length())
  {
    pos = str.find(to_replaced, pos);
    if (pos != std::string::npos)
      str.replace(pos, to_replaced.length(), newchars);
    else
      break;
  }
  return   str;
}

const std::string SignUtil::kAppId = "a-app-id";
const std::string SignUtil::kSignature = "a-signature"; // 签名结果串
const std::string SignUtil::kSignatureMethod = "a-signature-method";
const std::string SignUtil::kTimestamp = "a-timestamp";
const std::string SignUtil::kSignatureVersion = "a-signature-version";
const std::string SignUtil::kSignatureNonce = "a-signature-nonce";
const std::string SignUtil::kSignatureMethodValue = "HMAC-SHA1";
const std::string SignUtil::kSignatureVersionValue = "1.0";
const std::string SignUtil::kAlgorithmName = "HmacSHA1";
const std::string SignUtil::kEncoding = "UTF-8";
const std::string SignUtil::kUrlEncoding = "UTF-8";

const std::vector<std::string> SignUtil::processed_headers = { 
  kAppId , 
  kSignature ,
  kSignatureMethod ,
  kTimestamp ,
  kSignatureVersion ,
  kSignatureNonce
};

std::string SignUtil::GetSign(
  std::string sign_secret,
  std::string method,
  std::string path,
  std::map<std::string, std::string> params,
  std::map<std::string, std::string> headers) {

  //1 从headers中提取签名公共参数signedHeaders
  std::map<std::string, std::string> signed_headers;
  for (auto header : processed_headers) {
    auto value = headers.find(header);
    if (value == headers.end()) {
      continue;
    }
    signed_headers[header] = value->second;
  }

  //2 构造规范化的params/signedHeaders字符串（参数按key排序后，组合成&key=value形式）
  std::string query_string = CanonicalizedQuery(params);
  std::string header_string = CanonicalizedQuery(signed_headers);

  //3 与method, path一起构造签名字符串stringToSign
  std::string string_to_sign = BuildSignString(method, path, query_string, header_string);

  //4 计算签名
  std::string expected_signature = Sign(string_to_sign, sign_secret + "&");
  return expected_signature;
}

std::string SignUtil::GetUTCTimeStampFormatString() {
  QDateTime current_time = QDateTime::currentDateTimeUtc();//显示时间，格式为：年-月-日 时：分：秒 周几
  QString time_string = current_time.toString("yyyy-MM-ddTHH:mm:ssZ");
  return time_string.toStdString();
}

std::string SignUtil::GenerateNonce() {
  QString str_id = QUuid::createUuid().toString();
  str_id.remove("{").remove("}").remove("-");
  return str_id.toStdString();
}

std::string SignUtil::CanonicalizedQuery(std::map<std::string, std::string> query_params_map) {
  /*query_params_map*/
  // std::map是排序,直接拼装

  std::string canonicalized_query_string = "";
  for (auto itor : query_params_map) {
    canonicalized_query_string.append("&") 
      .append(PercentEncode(itor.first))
      .append("=")
      .append(PercentEncode(itor.second));
  }

  return canonicalized_query_string.substr(1);
}

std::string SignUtil::PercentEncode(std::string value) {
  QTextCodec *utf8 = QTextCodec::codecForName("utf-8");
  QByteArray encoded = utf8->fromUnicode(QString::fromStdString(value)).toPercentEncoding();
  value = encoded.toStdString();
  value = replace_str(value, "+", "%20");
  value = replace_str(value, "*", "%2A");
  value = replace_str(value, "%7E", "~");
  return value;
}

std::string SignUtil::BuildSignString(std::string method, std::string url_path, std::string query_string, std::string header_string) {
  std::string build_string = "";
  build_string.append(method);
  build_string.append("+");
  build_string.append(PercentEncode(url_path));
  build_string.append("+");
  build_string.append(PercentEncode(query_string));
  build_string.append("+");
  build_string.append(PercentEncode(header_string));
  return build_string;
}

std::string SignUtil::Sign(std::string string_to_sign, std::string sign_secret) {
  QByteArray key = QByteArray::fromStdString(sign_secret);
  QByteArray base_string = QByteArray::fromStdString(string_to_sign);
  int block_size = 64; 
  if (key.length() > block_size) { 
    key = QCryptographicHash::hash(key, QCryptographicHash::Sha1);
  }
  QByteArray inner_padding(block_size, char(0x36)); 
  QByteArray outer_padding(block_size, char(0x5c)); 

 
  for (int i = 0; i < key.length(); i++) {
    inner_padding[i] = inner_padding[i] ^ key.at(i);
    outer_padding[i] = outer_padding[i] ^ key.at(i);
  }

  QByteArray total = outer_padding;
  QByteArray part = inner_padding;
  part.append(base_string);
  total.append(QCryptographicHash::hash(part, QCryptographicHash::Sha1));
  QByteArray hashed = QCryptographicHash::hash(total, QCryptographicHash::Sha1);
  return PercentEncode(hashed.toBase64().replace("\n", "").toStdString());
}