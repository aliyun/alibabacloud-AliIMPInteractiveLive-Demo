#ifndef CHATLISTENER_H
#define CHATLISTENER_H
#include <QObject>
#include "chat_vm.h"
#include "meta/chat_event_listener.h"
#include "meta/mute_all_comment_event_model.h"
#include "meta/mute_user_comment_event_model.h"
#include "event/event_manager.h"

class ChatListener : public QObject, public alibaba::meta::ChatEventListener {
  Q_OBJECT
 public:
  explicit ChatListener(QObject* parent = nullptr);
  virtual void OnCommentReceived(
      const alibaba::meta::CommentReceivedEventModel& event) override;
  virtual void OnMuteAllComment(
      const alibaba::meta::MuteAllCommentEventModel& event) override;
  virtual void OnMuteUserComment(
      const alibaba::meta::MuteUserCommentEventModel& event) override;
  virtual void OnLikeCountChange(const alibaba::meta::LikeEventModel & event) override;
 signals:
  void SignalMsgArrived(const ChatMsg&);
  void SignalMuteUserArrived(const MuteUserMsg&);
  void SignalMuteAllArrived(const MuteAllMsg&);
};

#endif // CHATLISTENER_H
