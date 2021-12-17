#include "chat_vm.h"
#include <windows.h>
#include <QDebug>
#include "api/base_api.h"
#include "chat_bubble.h"
#include "chat_listener.h"
#include "i_chat.h"
#include "i_room.h"
#include "scheme_login.h"
#include "view/interface/i_main_window.h"
#include "view/view_component_manager.h"
#include "common/logging.h"
#include "event/event_manager.h"
#include "dps_error.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

ChatVM::ChatVM(QObject* parent) : QObject(parent) {
  auto listener_ptr = EventManager::Instance();
  connect(listener_ptr.get(),
          &EventManager::SignalMsgArrived, this, &ChatVM::ReceiveMsgFromServer,
          Qt::BlockingQueuedConnection);
  connect(listener_ptr.get(),
          &EventManager::SignalMuteUserArrived, this,
          &ChatVM::ReceiveMuteUserFromServer, Qt::BlockingQueuedConnection);
  connect(listener_ptr.get(),
          &EventManager::SignalMuteAllArrived, this,
          &ChatVM::ReceiveMuteAllFromServer, Qt::BlockingQueuedConnection);
}

ChatVM::~ChatVM() { qDebug() << "did not enter room" << endl; }

void ChatVM::EnterIroom() {
  iroom_ptr_ =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      prm_.class_room_id);
  room_dtl_ = iroom_ptr_->GetRoomDetail();

}

const ChatParam& ChatVM::GetChatParam() { return this->prm_; }

void ChatVM::UploadMsgToServer(const std::string& msg) {
  std::shared_ptr<IChat> chat_plugin =
      std::dynamic_pointer_cast<IChat>(iroom_ptr_->GetPlugin(PluginChat));
  chat_plugin->SendComment(msg, [this, msg](const std::string& id) {
    EventManager::Instance()->PostUITask([this, msg, id]() {
      ChatMsg msg_item;
      msg_item.comment_id = id;
      msg_item.content = msg;
      msg_item.creator_nick = SchemeLogin::Instance()->GetSchemeInfo().nick_name;
      msg_item.creator_open_id = prm_.user_id;
      msg_item.type = 0;
      ReceiveMsgFromServer(msg_item);
    });
  }, [](const alibaba::dps::DPSError& err) {
    classroom::blog(LOG_ERROR, "send comment error");
  });
}

void ChatVM::UpLoadMuteAllToServer() {
  std::shared_ptr<IChat> chat_plugin =
      std::dynamic_pointer_cast<IChat>(iroom_ptr_->GetPlugin(PluginChat));
  chat_plugin->MuteAll();
}

void ChatVM::UploadCancelMuteAllToServer() {
  std::shared_ptr<IChat> chat_plugin =
      std::dynamic_pointer_cast<IChat>(iroom_ptr_->GetPlugin(PluginChat));
  chat_plugin->CancelMuteAll();
}

void ChatVM::UpdateClassroomParam(const ChatParam& new_prm) {
  // try to enter room after updating client info
  prm_.role = new_prm.role;
  prm_.type = new_prm.type;
  prm_.user_id = new_prm.user_id;
  prm_.class_room_id = new_prm.class_room_id;
  EnterIroom();
  GetHistoryMsg([this](const std::list<ChatMsg>& msgs) {
    for (auto iter = msgs.begin(); iter != msgs.end(); iter++) {
      uint16_t msg_type = 0;
      ChatMsg message = *iter;
      if (message.creator_open_id == prm_.user_id) {
        msg_type |= ChatBubble::UserMe;
        if (message.creator_open_id == room_dtl_.room_info.owner_id)
          msg_type |= ChatBubble::UserTeacher;
        else
          msg_type |= ChatBubble::UserStudent;
      }
      else {
        msg_type |= ChatBubble::UserOther;
        if (message.creator_open_id == room_dtl_.room_info.owner_id)
          msg_type |= ChatBubble::UserTeacher;
        else
          msg_type |= ChatBubble::UserStudent;
      }

      emit SendMsgToClient(message.creator_open_id, message.content,
        message.create_at, msg_type, message.comment_id, true);
    }
  });
}

void ChatVM::ReceiveMsgFromServer(const ChatMsg& message) {
  uint16_t msg_type = 0;

  if (message.creator_open_id == prm_.user_id) {
    msg_type |= ChatBubble::UserMe;
    if (message.creator_open_id == room_dtl_.room_info.owner_id)
      msg_type |= ChatBubble::UserTeacher;
    else
      msg_type |= ChatBubble::UserStudent;
  } else {
    msg_type |= ChatBubble::UserOther;
    if (message.creator_open_id == room_dtl_.room_info.owner_id)
      msg_type |= ChatBubble::UserTeacher;
    else
      msg_type |= ChatBubble::UserStudent;
  }

  emit SendMsgToClient(message.creator_open_id, message.content,
                       message.create_at, msg_type, message.comment_id, false);
}

void ChatVM::ReceiveMuteUserFromServer(const MuteUserMsg& message) {
  emit SendMuteUserToClient(message.mute);
}

void ChatVM::ReceiveMuteAllFromServer(const MuteAllMsg& message) {
  // true :mute   |  false :cancel mute
  emit SendMuteAllToClient(message.mute);
}


void ChatVM::GetHistoryMsg(const std::function<void(const std::list<ChatMsg>& msgs)>& callback) {
  std::shared_ptr<IChat> chat_plugin = std::dynamic_pointer_cast<IChat>(iroom_ptr_->GetPlugin(PluginChat));
  if (!chat_plugin) {
    return;
  }

  alibaba::chat::ListCommentReq req;
  req.page_num = 1;
  req.page_size = 50;
  req.sort_type = 0;
  chat_plugin->ListComment(req, [callback](const alibaba::chat::ListCommentRsp & rsp) {
    EventManager::Instance()->PostUITask([callback, rsp]() {
    
      std::list<ChatMsg> ret_msgs;
      auto msgs = rsp.comment_model_list;

      for (auto iter = msgs.begin(); iter != msgs.end(); iter++) {
        ChatMsg tmp;
        auto msg = *iter;
        tmp.type = msg.type;
        tmp.content = msg.content;
        tmp.topic_id = msg.topic_id;
        tmp.create_at = msg.create_at;
        tmp.comment_id = msg.comment_id;
        tmp.creator_nick = msg.creator_nick;
        tmp.creator_open_id = msg.creator_id;
        ret_msgs.push_front(tmp);
      }

      SAFE_CALLBACK(callback)(ret_msgs);
    });
    
  }, [callback](const alibaba::dps::DPSError& err) {
    EventManager::Instance()->PostUITask([callback]() {
      SAFE_CALLBACK(callback)({});
    });
    
  });
}