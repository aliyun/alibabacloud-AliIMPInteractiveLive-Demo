#include "base_api.h"

#include <sstream>
#include <QTextCodec>
#include <QJsonDocument>
#include <QJsonParseError>
#include <QJsonObject>
#include "const/const.h"
#include "util/sign_util.h"
#include "util/log_util.h"
#include "common/http_helper.h"
#include "meta_space.h"
#include <windows.h>

#include "scheme_login.h"
#include "common/logging.h"


extern std::string GetDeviceId() {

  static std::string id;
  if (!id.empty()) {
    return id;
  }
  char name[MAX_PATH];
  DWORD serno;
  DWORD length;
  DWORD file_flag;
  char file_name[MAX_PATH];
  BOOL ret = GetVolumeInformationA("c:\\", name, MAX_PATH, &serno, &length, &file_flag, file_name, MAX_PATH);
  if (ret) {
    id = std::to_string(serno);
  };
  return id;
}

BaseApi::BaseApi()
{
}

BaseApi::~BaseApi()
{
}

std::string BaseApi::Request(
    const std::string& api,
    std::map<std::string, std::string> reqs,
    const uint32_t& timeout_ms,
    const std::function<void()>& timeout_callback) {
  std::string api_url = Const::kAppServerUrl + api;

  std::string timestamp = SignUtil::GetUTCTimeStampFormatString();
  std::string nonce = SignUtil::GenerateNonce();

  std::map<std::string, std::string> headers;
  headers["a-app-id"] = "imp-room";
  headers["a-signature-method"] = "HMAC-SHA1";
  headers["a-signature-version"] = "1.0";
  headers["a-timestamp"] = timestamp;
  headers["a-signature-nonce"] = nonce;

  std::map<std::string, std::string> params;

  params["appId"] = Const::kAppId;
  params["appKey"] = Const::kAppKey;
  

  params["deviceId"] = GetDeviceId();
  for (auto req : reqs) {
    params[req.first] = req.second;
  }

  std::string sign = SignUtil::GetSign(Const::kSignSecret, "POST", api_url, params, headers);
  headers["a-signature"] = sign;

  std::string url = api_url + "?" + GetPostDataString(params);
   

  std::string result = HttpHelper().GetResult(url, headers, timeout_ms, timeout_callback);
  return result;
}

std::string BaseApi::GetPostDataString(const std::map<std::string, std::string>& params) {
  std::string result = "";
  for (auto param_itor : params) {
    result.append("&");

    QTextCodec *utf8 = QTextCodec::codecForName("utf-8");
    std::string key = param_itor.first;
    QByteArray key_encoded = utf8->fromUnicode(QString::fromStdString(key)).toPercentEncoding();
    result.append(key_encoded.toStdString());

    result.append("=");

    std::string value = param_itor.second;
    QByteArray value_encoded = utf8->fromUnicode(QString::fromStdString(value)).toPercentEncoding();
    result.append(value_encoded.toStdString());
  }
  if (!result.empty())
    result = result.substr(1);

  return result;
}

std::string BaseApi::CreateRoomApi(
  const std::string& domain,
  const std::string& biz_type,
  const std::string& template_id,
  const std::string& title,
  const std::string& notice,
  const std::string& owner_id, 
  const std::string& room_id) {
  std::string api = "/api/login/createRoom";
  std::map<std::string, std::string> reqs;
  reqs["domain"] = domain;
  reqs["bizType"] = biz_type;
  reqs["templateId"] = template_id;
  reqs["title"] = title;
  reqs["notice"] = notice;
  reqs["RoomOwnerId"] = owner_id;
  reqs["roomId"] = room_id;
  std::string ret = Request(api, reqs);

  return ret;
}

void BaseApi::GetTokenApi(
    const std::string& user_id,
    alibaba::meta_space::TokenInfo& token_info,
    const uint32_t& timeout_ms,
    const std::function<void()>& timeout_callback) {
  std::string api = "/api/login/getToken";
  std::map<std::string, std::string> reqs;
  reqs["userId"] = user_id;

  std::string ret = Request(api, reqs, timeout_ms, timeout_callback);

  //json解析
  QJsonParseError parse_json_err;
  QJsonDocument document = QJsonDocument::fromJson(QString::fromStdString(ret).toUtf8(), &parse_json_err);
  if (!(parse_json_err.error == QJsonParseError::NoError))
  {
    classroom::blog(LOG_ERROR, "json parse error");
    return;
  }

  QJsonObject json_object = document.object();

  token_info.access_token = json_object["result"].toObject()["accessToken"].toString().toStdString();
  token_info.refresh_token = json_object["result"].toObject()["refreshToken"].toString().toStdString();
}