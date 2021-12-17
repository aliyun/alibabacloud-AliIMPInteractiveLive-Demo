#include "chat_listener.h"
#include "meta/comment_received_event_model.h"


ChatListener::ChatListener(QObject* parent) : QObject(parent) {}

void ChatListener::OnCommentReceived(
    const alibaba::meta::CommentReceivedEventModel& event) {
  ChatMsg tmp;
  tmp.type = event.type;
  tmp.content = event.content;
  tmp.topic_id = event.topic_id;
  tmp.create_at = event.create_at;
  tmp.comment_id = event.comment_id;
  tmp.creator_nick = event.creator_nick;
  tmp.creator_open_id = event.creator_open_id;
  emit SignalMsgArrived(tmp);
}

void ChatListener::OnMuteUserComment(
    const alibaba::meta::MuteUserCommentEventModel& event) {
  MuteUserMsg tmp;
  tmp.mute = event.mute;
  tmp.mute_time = event.mute_time;
  tmp.mute_user_nick = event.mute_user_nick;
  tmp.mute_user_open_id = event.mute_user_open_id;
  tmp.topic_id = event.topic_id;
  emit SignalMuteUserArrived(tmp);
}

void ChatListener::OnMuteAllComment(
    const alibaba::meta::MuteAllCommentEventModel& event) {
  MuteAllMsg tmp;
  tmp.mute = event.mute;
  tmp.topic_id = event.topic_id;
  emit SignalMuteAllArrived(tmp);
}

void ChatListener::OnLikeCountChange(const alibaba::meta::LikeEventModel & event) {
}