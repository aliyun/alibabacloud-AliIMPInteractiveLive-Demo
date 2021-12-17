#ifndef CHAT_VM_H
#define CHAT_VM_H

#include <QObject>
#include "login_window_vm.h"
#include "meta_space.h"
#include "event/event_param.h"

struct ChatParam {
  ClassTypeEnum type = ClassTypeEnum_SmallClass;
  ClassRoleEnum role = ClassRoleEnum_Teacher;
  std::string class_room_id;
  std::string user_id;
};

class ChatVM : public QObject {
  Q_OBJECT
public:
  explicit ChatVM(QObject* parent = nullptr);
  ~ChatVM();
  void GetHistoryMsg(const std::function<void(const std::list<ChatMsg>& msgs)>& callback);
signals:
  void SendMsgToClient(const std::string&,
    const std::string&,
    const int64_t,
    const uint16_t,
    const std::string& comment_id,
    bool sync);
  void SendMuteAllToClient(const bool);
  void SendMuteUserToClient(const bool);

public slots:
  void UploadMsgToServer(const std::string&);
  void UpLoadMuteAllToServer();
  void UploadCancelMuteAllToServer();
  const ChatParam& GetChatParam();
  void UpdateClassroomParam(const ChatParam&);
  void ReceiveMsgFromServer(const ChatMsg&);
  void ReceiveMuteAllFromServer(const MuteAllMsg&);
  void ReceiveMuteUserFromServer(const MuteUserMsg&);
  void EnterIroom();

private:
  ChatParam prm_;
  std::shared_ptr<alibaba::meta_space::IRoom> iroom_ptr_;
  alibaba::room::RoomDetail room_dtl_; // room details
};

#endif // CHAT_VM_H
