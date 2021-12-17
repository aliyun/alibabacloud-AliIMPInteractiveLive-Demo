#pragma once
#include <string>
#include <map>
#include <functional>

class BaseReq;
namespace alibaba {
  namespace meta_space { 
    struct TokenInfo;
  }
}

class BaseApi
{
public:
  BaseApi();
  ~BaseApi();

  static void GetTokenApi(
      const std::string& user_id,
      alibaba::meta_space::TokenInfo& token_info,
      const uint32_t& timeout_ms = 0,
      const std::function<void()>& timeout_callback = nullptr);
  static std::string CreateRoomApi(
    const std::string& domain,
    const std::string& biz_type,
    const std::string& template_id,
    const std::string& title,
    const std::string& notice,
    const std::string& owner_id,
    const std::string& room_id);

private:
  static std::string Request(const std::string& api, std::map<std::string, std::string> reqs, const uint32_t& timeout_ms = 0, const std::function<void()>& timeout_callback = nullptr);
  static std::string GetPostDataString(const std::map<std::string, std::string>& params);

};

