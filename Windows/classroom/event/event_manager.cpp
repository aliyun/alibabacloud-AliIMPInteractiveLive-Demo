#include "event_manager.h"
#include <mutex>

#include "i_chat.h"
#include "i_live.h"
#include "i_rtc.h"
#include "meta/ali_rtc_stats.h"
#include "meta/comment_received_event_model.h"
#include "meta/conf_apply_join_channel_event.h"
#include "meta/conf_invite_event.h"
#include "meta/conf_user_event.h"
#include "meta/kick_user_event_model.h"
#include "meta/live_common_event_model.h"
#include "meta/room_in_out_event_model.h"
#include "meta/rtc_event_listener.h"
#include "meta/rtc_handle_apply_event.h"
#include "meta/rtc_stream_event.h"
#include "meta/rtc_user_volume_info.h"
#include "meta/share_screen_message_model.h"
#include "mute_all_comment_event_model.h"
#include "mute_user_comment_event_model.h"
#include "meta/mute_message_event.h"
#include "room/get_room_user_list_req.h"
#include "room/get_room_user_list_rsp.h"
#include "common/logging.h"
#include "meta/class_status_type.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;

std::shared_ptr<EventManager> EventManager::Instance() {
  static std::once_flag once_flag;
  static std::shared_ptr<EventManager> instance;
  std::call_once(once_flag, [&]() {
    instance =
      std::shared_ptr<EventManager>(new EventManager, EventManager::Destory);
  });
  return instance;
}

EventManager::EventManager() {

  qRegisterMetaType<alibaba::meta::RoomInOutEventModel>(
    "alibaba::meta::RoomInOutEventModel");
  qRegisterMetaType<alibaba::meta::KickUserEventModel>(
    "alibaba::meta::KickUserEventModel");
  qRegisterMetaType<alibaba::meta::ConfInviteEvent>(
    "alibaba::meta::ConfInviteEvent");
  qRegisterMetaType<alibaba::meta::RtcHandleApplyEvent>(
    "alibaba::meta::RtcHandleApplyEvent");
  qRegisterMetaType<alibaba::meta::ConfUserEvent>(
    "alibaba::meta::ConfUserEvent");
  qRegisterMetaType<alibaba::meta::ConfApplyJoinChannelEvent>(
    "alibaba::meta::ConfApplyJoinChannelEvent");
  qRegisterMetaType<alibaba::meta::RtcStreamEvent>(
    "alibaba::meta::RtcStreamEvent");
  qRegisterMetaType<alibaba::meta::LiveCommonEventModel>(
    "alibaba::meta::LiveCommonEventModel");
  qRegisterMetaType<alibaba::meta::AliRtcStats>("alibaba::meta::AliRtcStats");
  qRegisterMetaType<alibaba::meta::MuteMessageEvent>("alibaba::meta::MuteMessageEvent");
  qRegisterMetaType<MuteAllMsg>("MuteAllMsg");
  qRegisterMetaType<MuteUserMsg>("MuteUserMsg");
  qRegisterMetaType<ChatMsg>("ChatMsg");
  connect(this, &EventManager::SignalProcessUITask,
    this, &EventManager::OnProcessUITask);
}

EventManager::~EventManager() {}


void EventManager::SetRoomId(const std::string& room_id) {
  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(room_id);

  std::shared_ptr<IRtc> rtc_plugin =
    std::dynamic_pointer_cast<IRtc>(iroom_ptr->GetPlugin(PluginRtc));
  std::shared_ptr<ILive> live_plugin =
    std::dynamic_pointer_cast<ILive>(iroom_ptr->GetPlugin(PluginLive));
  std::shared_ptr<IChat> chat_plugin =
    std::dynamic_pointer_cast<IChat>(iroom_ptr->GetPlugin(PluginChat));

  iroom_ptr->SetRoomEventListener(shared_from_this());
  if (chat_plugin) {
    chat_plugin->SetChatEventListener(shared_from_this());
  }

  if (rtc_plugin) {
    rtc_plugin->SetRtcEventListener(shared_from_this());
  }

  if (live_plugin) {
    live_plugin->SetLiveEventListener(shared_from_this());
  }
}

void EventManager::ClearRoomId(const std::string& room_id) {
  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(room_id);
  std::shared_ptr<IRtc> rtc_plugin =
    std::dynamic_pointer_cast<IRtc>(iroom_ptr->GetPlugin(PluginRtc));

  std::shared_ptr<IChat> chat_plugin =
    std::dynamic_pointer_cast<IChat>(iroom_ptr->GetPlugin(PluginChat));

  iroom_ptr->SetRoomEventListener(nullptr);
  if (chat_plugin) {
    chat_plugin->SetChatEventListener(nullptr);
  }

  if (rtc_plugin) {
    rtc_plugin->SetRtcEventListener(nullptr);
  }
}

void EventManager::PostUITask(const std::function<void()>& task) {
  {
    std::lock_guard<std::mutex> locker(task_list_mutex_);
    task_list_.push_back(task);
  }
  emit SignalProcessUITask();
}

void EventManager::OnProcessUITask() {
  std::list<std::function<void()>> task_list;
  {
    std::lock_guard<std::mutex> locker(task_list_mutex_);
    task_list = std::move(task_list_);
  }
  for (auto iter = task_list.begin(); iter != task_list.end(); iter++) {
    std::function<void()> func = *iter;
    if (func) {
      func();
    }
  }
}

void EventManager::OnRoomInOut(const RoomInOutEventModel& event) {
  PostUITask([=]() {
    emit SignalRoomInOut(event);
  });

}

void EventManager::OnRoomNoticeChanged(const std::string& event) {}

void EventManager::OnRoomTitleChanged(const std::string& event) {}

void EventManager::OnRoomUserKicked(const KickUserEventModel& event) {
  PostUITask([=]() {
    emit SignalRoomUserKicked(event);
  });

}


void EventManager::OnCommentReceived(const CommentReceivedEventModel& event) {
  ChatMsg tmp;
  tmp.type = event.type;
  tmp.content = event.content;
  tmp.topic_id = event.topic_id;
  tmp.create_at = event.create_at;
  tmp.comment_id = event.comment_id;
  tmp.creator_nick = event.creator_nick;
  tmp.creator_open_id = event.creator_open_id;
  PostUITask([this, tmp]() {
    emit SignalMsgArrived(tmp);
  });

}

void EventManager::OnMuteAllComment(const MuteAllCommentEventModel& event) {
  MuteAllMsg tmp;
  tmp.mute = event.mute;
  tmp.topic_id = event.topic_id;
  PostUITask([this, tmp]() {
    emit SignalMuteAllArrived(tmp);
  });

}

void EventManager::OnMuteUserComment(const MuteUserCommentEventModel& event) {
  MuteUserMsg tmp;
  tmp.mute = event.mute;
  tmp.mute_time = event.mute_time;
  tmp.mute_user_nick = event.mute_user_nick;
  tmp.mute_user_open_id = event.mute_user_open_id;
  tmp.topic_id = event.topic_id;

  PostUITask([this, tmp]() {
    emit SignalMuteUserArrived(tmp);
  });
}

void EventManager::OnRtcJoinRtcSuccess() { emit SignalRtcJoinRtcSuccess(); }

void EventManager::OnRtcJoinRtcFail(const ::alibaba::dps::DPSError& error) {}

void EventManager::OnRtcStreamIn(const alibaba::meta::RtcStreamEvent& event) {
  PostUITask([=]() {
    emit SignalRtcStreamIn(event);
  });

}

void EventManager::OnLikeCountChange(
  const alibaba::meta::LikeEventModel& event) {}

void EventManager::OnRtcStreamUpdate(
  const alibaba::meta::RtcStreamEvent& event) {}

void EventManager::OnRtcStreamOut(const std::string& uid) {}

void EventManager::OnRtcRemoteJoinSuccess(
  const alibaba::meta::ConfUserEvent& event) {
  PostUITask([=]() {
    emit SignalRtcRemoteJoinSuccess(event);
  });

}

void EventManager::OnRtcRemoteJoinFail(
  const alibaba::meta::ConfUserEvent& event) {
  PostUITask([=]() {
    emit SignalRtcRemoteJoinFail(event);
  });

}

void EventManager::OnRtcConfUpdate(const alibaba::meta::ConfEvent& event) {}

void EventManager::OnRtcRingStopped(
  const alibaba::meta::ConfStopRingEvent& event) {}

void EventManager::OnRtcUserInvited(
  const alibaba::meta::ConfInviteEvent& event) {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnRtcUserInvited");
  PostUITask([=]() {
    emit SignalRtcUserInvited(event);
  });

}

void EventManager::OnRtcKickUser(const alibaba::meta::ConfUserEvent& event) {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnRtcKickUser");
  PostUITask([=]() {
    emit SignalRtcKickUser(event);
  });

}

void EventManager::OnRtcLeaveUser(const alibaba::meta::ConfUserEvent& event) {
  PostUITask([=]() {
    emit SignalRtcLeaveUser(event);
  });

}

void EventManager::OnRtcStart(const alibaba::meta::ConfEvent& event) {}

void EventManager::OnRtcEnd(const alibaba::meta::ConfEvent& event) {}

void EventManager::OnRtcCommand(const alibaba::meta::ConfCommandEvent& event) {}

void EventManager::OnRtcApplyJoinChannel(
  const alibaba::meta::ConfApplyJoinChannelEvent& event) {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "uid:%s, is_apply:%d", event.apply_user.user_id.c_str(), event.is_apply);
  PostUITask([=]() {
    emit SignalRtcApplyJoinChannel(event);
  });

}

void EventManager::OnRtcHandleApply(
  const alibaba::meta::RtcHandleApplyEvent& event) {
  PostUITask([=]() {
    emit SignalRtcHandleApply(event);
  });

}

void EventManager::OnRtcAudioVolumeChange(
  const std::vector<alibaba::meta::RtcUserVolumeInfo>& event,
  int32_t total_volume) {}

void EventManager::OnLiveCreated(
  const alibaba::meta::LiveCommonEventModel& event) {
  PostUITask([=]() {
    emit SignalLiveCreated(event);
  });

}

void EventManager::OnLiveStarted(
  const alibaba::meta::LiveCommonEventModel& event) {
  PostUITask([=]() {
    emit SignalLiveStarted(event);
  });

}

void EventManager::OnLiveStopped(
  const alibaba::meta::LiveCommonEventModel& event) {
  PostUITask([=]() {
    emit SignalLiveStopped(event);
  });

}

void EventManager::OnRtcStats(const alibaba::meta::AliRtcStats& event) {
  PostUITask([=]() {
    emit SignalNetwordLatency(event.lastmile_delay, event.sent_k_bitrate);
    emit SignalRtcStatus(event);
  });

}

void EventManager::OnRtcNetworkQualityChanged(
  const std::string& uid,
  alibaba::meta::AliRtcNetworkQuality up_quality,
  alibaba::meta::AliRtcNetworkQuality down_quality) {
  PostUITask([=]() {
    emit SignalNetworkQuality(uid, (int32_t)up_quality,
      (int32_t)down_quality);
  });

}

void EventManager::OnUserAudioMuted(const std::string& uid, bool is_mute) {
  PostUITask([=]() {
    emit SignalAudioMute(uid, is_mute);
  });

}

void EventManager::OnUserVideoMuted(const std::string& uid, bool is_mute) {
  PostUITask([=]() {
    emit SignalVideoMute(uid, is_mute);
  });

}

void EventManager::OnShareScreen(
  const alibaba::meta::ShareScreenMessageModel& event) {
  PostUITask([=]() {
    emit SignalStudentShareScreen(event.conf_id, event.open);
  });
  

}

void EventManager::OnActiveSpeaker(const std::string& uid) {
  PostUITask([=]() {
    emit SignalRtcActiveSpeaker(uid);
  });
}

void EventManager::OnPlayerVideoFirstFrameRender() {
  PostUITask([=]() {
    emit SignalPlayerVideoFirstFrameRender();
  });
  
}
void EventManager::OnRtcFirstRemoteVideoFrameDrawn(const std::string& uid,
  AliRtcVideoTrack video_track,
  int32_t width,
  int32_t height,
  int32_t elapsed) {
  PostUITask([=]() {
    emit SignalRtcFirstRemoteVideoFrameDrawn(uid, (int32_t)video_track, width,
      height, elapsed);
  });
 
}
void EventManager::OnRtcFirstLocalVideoFrameDrawn(int32_t width,
  int32_t height,
  int32_t elapsed) {
  PostUITask([=]() {
    emit SignalRtcFirstLocalVideoFrameDrawn(width, height, elapsed);
  });

}

void EventManager::OnRtcMuteMessage(const alibaba::meta::MuteMessageEvent& event) {
  PostUITask([=]() {
    emit SignalRtcMuteMessage(event);
  });
}

void EventManager::OnClassStatusChanged(const alibaba::meta::ClassStatusEventModel & event) {
  PostUITask([=]() {
    if (event.status == (int32_t)alibaba::meta::ClassStatusType::CLASS_STATUS_START) {
      emit SignalClassStart(event.class_id);
    }
    else if (event.status == (int32_t)alibaba::meta::ClassStatusType::CLASS_STATUS_END) {
      emit SignalClassEnd(event.class_id);
    }
  });
  classroom::blog(LOG_INFO, "OnClassStatusChanged, class=%s, status=%d", event.class_id.c_str(), event.status);
  
}

void EventManager::OnRtcWarning(int32_t warning, const std::string & message) {
  LogWithTag(ClassroomTagLinkMic, LOG_WARNING, "code:%d, message:%s", warning, message.c_str());
}

void EventManager::OnRtcError(int32_t error, const std::string & message) {
  LogWithTag(ClassroomTagLinkMic, LOG_ERROR, "code:%d, message:%s", error, message.c_str());
}

void EventManager::OnPlayerEvent(int32_t event) {
  classroom::blog(LOG_INFO, "OnPlayerEvent, event=%d", event);
}

void EventManager::OnPlayerError(int32_t error_code, const std::string & error_message) {
  classroom::blog(LOG_ERROR, "OnPlayerError, code=%d, message=%s", error_code, error_message.c_str());
}

void EventManager::OnCustomMessageReceived(const std::string & event) {

}