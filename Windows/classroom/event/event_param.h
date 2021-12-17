#pragma once
#include <string>

struct ChatMsg {
  std::string topic_id;
  std::string comment_id;
  // sender id
  std::string creator_open_id;
  // sender nick name
  std::string creator_nick;
  int64_t create_at = 0;
  int32_t type = 0;
  std::string content;
};

struct MuteUserMsg {
  /**
   * mute/cancel mute
   */
  bool mute = true;
  /**
   * mute time
   */
  int64_t mute_time = 0;
  /**
   * nick of the user being muted
   */
  std::string mute_user_nick;
  /**
   * userid of the user being muted
   */
  std::string mute_user_open_id;
  /**
   * topic id
   */
  std::string topic_id;
};

struct MuteAllMsg {
  /**
   * mute/cancel mute
   */
  bool mute = true;
  /**
   * topic id
   */
  std::string topic_id;
};

