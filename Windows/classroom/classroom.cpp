#include "classroom.h"

#include <sstream>

#include "api/base_api.h"
#include "meta/comment_received_event_model.h"
#include "meta/room_in_out_event_model.h"
#include "event/event_manager.h"

#include "meta_space.h"
#include "meta/rtc_event_listener.h"
#include "meta/rtc_stream_event.h"
#include "meta_space.h"
#include "room/get_room_user_list_req.h"
#include "room/get_room_user_list_rsp.h"
#include "ui_classroom_window.h"
#include "meta/comment_received_event_model.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;

class RtcEventListenerImpl : public RtcEventListener {
 public:
  RtcEventListenerImpl(ClassRoom* class_room) : class_room_(class_room) {}
  ~RtcEventListenerImpl() {}

  // From RtcEventListener
  virtual void OnRtcJoinRtcSuccess() {}

  virtual void OnRtcJoinRtcFail(const std::string& error) {}

  virtual void OnRtcStreamIn(const RtcStreamEvent& event) {
    // show
    QMetaObject::invokeMethod(
        class_room_, "OnShowStream",
        Q_ARG(const QString&, QString::fromStdString(event.user_id)));
  }

  virtual void OnRtcStreamUpdate(const RtcStreamEvent& event) {}

  virtual void OnRtcStreamOut(const std::string& uid) {}

  virtual void OnRtcRemoteJoinSuccess(const ConfUserEvent& event) {}

  virtual void OnRtcRemoteJoinFail(const ConfUserEvent& event) {}

  virtual void OnRtcConfUpdate(const ConfEvent& event) {}

  virtual void OnRtcRingStopped(const ConfStopRingEvent& event) {}

  virtual void OnRtcUserInvited(const ConfInviteEvent& event) {}

  virtual void OnRtcKickUser(const KickUserEvent& event) {}
  void OnRtcHandleApply(
      const alibaba::meta::RtcHandleApplyEvent&) {}

  virtual void OnRtcHandleApply(const RtcHandleApplyEvent & event) {}

  virtual void OnRtcAudioVolumeChange(const std::vector<RtcUserVolumeInfo> & event, int32_t total_volume) {}
private:
  ClassRoom* class_room_;
};

ClassRoom::ClassRoom(const std::string& room_id,
                     const std::string& user_id,
                     const Role& role)
    : ui(new Ui::ClassRoom),
      room_id_(room_id),
      user_id_(user_id),
      role_(role),
      iroom_ptr_(MetaSpace::GetInstance()->GetRoomInstance(room_id)) {
  ui->setupUi(this);
}

ClassRoom::~ClassRoom() { delete ui; }

void ClassRoom::Start() {
  //创建白板
  HWND white_hwnd = (HWND)ui->openGLWidget->winId();
  CreateBoard(white_hwnd);

  //设置listener
  SetEventListener();

  //获取用户列表
  GetAndShowRoomUserList();

  if (role_ == kTeacher) {
    //老师流程
    // 窗口预览
    HWND preview_hwnd = (HWND)ui->graphicsView->winId();
    iroom_ptr_->StartRtcPreview(preview_hwnd);
    // join Rtc
    iroom_ptr_->JoinRtc(user_id_);
  } else {
    //学生流程
  }
}

void ClassRoom::GetAndShowRoomUserList() {
  alibaba::room::GetRoomUserListReq req;
  req.page_num = 1;
  req.page_size = 100;
  auto on_success = [this](const ::alibaba::room::GetRoomUserListRsp& rsp) {
    for (auto user : rsp.user_list) {
      if (user.open_id == user_id_) continue;
      QMetaObject::invokeMethod(
          this, "OnAddUser",
          Q_ARG(const QString&, QString::fromStdString(user.open_id)));
    }
  };

  auto on_fail = [](const std::string& error_msg) {

  };
  iroom_ptr_->ListUser(req, on_success, on_fail);
}

// void ClassRoom::show_window(const std::string& room_id, const std::string&
// user_id)
//{
//
//
//
//
//
//
//  // JoinChannel
//  room_->JoinRtc(user_id);
//
//  alibaba::room::GetRoomUserListReq req;
//  req.page_num = 1;
//  req.page_size = 100;
//  auto on_success = [this](const ::alibaba::room::GetRoomUserListRsp & rsp) {
//    for (auto user : rsp.user_list) {
//      if (user.open_id == user_id_)
//        continue;
//      QMetaObject::invokeMethod(this, "OnAddUser", Q_ARG(const QString&,
//      QString::fromStdString(user.open_id)));
//    }
//  };
//
//  auto on_fail = [](const std::string & error_msg) {
//
//  };
//  room_->ListUser(req, on_success, on_fail);
//  this->show();
//}

void ClassRoom::on_pushButton_clicked() {
  iroom_ptr_->CloseWhiteBoard();
  iroom_ptr_->LeaveRoom(
      [this]() { QMetaObject::invokeMethod(this, "OnLeaveRoom"); },
      [](std::string error_msg) {});
  iroom_ptr_->LeaveRtc(true);
}

void ClassRoom::OnShowStream(const QString& uid) {
  HWND hwnd = (HWND)ui->graphicsView_2->winId();
  iroom_ptr_->ShowStream(uid.toStdString(), hwnd);
}

void ClassRoom::OnLeaveRoom() {
  ui->textBrowser->clear();
  user_list_.clear();
  ShowMainWindow();
  this->close();
}

void ClassRoom::OnAddUser(const const QString& uid) {
  if (user_list_.find(uid) != user_list_.end()) {
    //已在列表中，不处理
    return;
  }

  auto ctx = std::make_shared<UserContext>(uid, ui->UserList, iroom_ptr_);
  ctx->status_ = 0;
  user_list_[uid] = ctx;
  //显示列表
}

void ClassRoom::OnDelUser(const const QString& uid) {
  auto user_itor = user_list_.find(uid);
  if (user_itor == user_list_.end()) {
    //已在列表中，不处理
    return;
  }
  auto ctx = user_itor->second;
  user_list_.erase(uid);
}

void ClassRoom::on_pushButton_2_clicked() {
  std::string content = ui->lineEdit->text().toStdString();
  iroom_ptr_->SendComment(content);
  ui->lineEdit->clear();
}

void ClassRoom::on_ClassRoom_destroyed() {}

void ClassRoom::OnShowComment(const QString& content) {
  ui->textBrowser->append(content);
}

void ClassRoom::CreateBoard(HWND hwndWhiteboard) {
  //获取白板instance
  alibaba::room::RoomDetail room_detail = iroom_ptr_->GetRoomDetail();
  std::string wb_instance_id = "";
  std::string doc_data = "";
  for (auto plugin : room_detail.room_info.plugin_instance_info.instance_list) {
    if (plugin.plugin_id == "wb") {
      wb_instance_id = plugin.instance_id;
      break;
    }
  }
  if (wb_instance_id.empty()) {
    // 创建白板
  } else {
    // 获取白板doc_data
    doc_data = BaseApi::OpenWhiteBoardApi(wb_instance_id, user_id_);
  }

  bool can_multiple_edit = true;
  std::string config_data;
  if (can_multiple_edit) {
    // We can invite more than 1 user to get the doc and edit together
    // User need to get config/doc_data from ISV server
    config_data = "{}";
  } else {
    // We can edit it locally only and may use other media to share the board,
    // such as screen sharing or live
    config_data = "{\"module\":{\"document\":false}}";
    doc_data = "{}";
  }
  iroom_ptr_->OpenWhiteBoard(hwndWhiteboard, config_data, doc_data);
}

void ClassRoom::SetEventListener() {

}

void ClassRoom::ClearEventListener() {

}

void ClassRoom::on_pushButton_3_clicked() {
  ui->textBrowser->hide();
  ui->UserList->show();
}

void ClassRoom::on_pushButton_4_clicked() {
  ui->textBrowser->show();
  ui->UserList->hide();
}

const std::string ClassRoom::GetUserId() { return user_id_; }

UserUiItem::UserUiItem(const const QString& uid,
                       QListWidget* list,
                       std::shared_ptr<alibaba::meta_space::IRoom> room) {
  list_ = list;
  room_ = room;
  uid_ = uid;
  item_ = new QListWidgetItem(uid);
  user_widge_ = new QWidget(list);
  user_widge_->setObjectName(QString::fromUtf8("UserWidget"));
  user_widge_->setGeometry(QRect(810, 280, 261, 41));
  push_button_ = new QPushButton(user_widge_);
  push_button_->setObjectName(QString::fromUtf8("pushButton"));
  push_button_->setGeometry(QRect(170, 5, 81, 31));
  push_button_->setText(QString::fromLocal8Bit("邀请连麦"));
  connect(push_button_, SIGNAL(clicked()), this, SLOT(InvateLinkmic()));
  item_->setSizeHint(QSize(50, 40));
  list->addItem(item_);
  list->setItemWidget(item_, user_widge_);
}

UserUiItem::~UserUiItem() {
  //从列表中删除
  list_->removeItemWidget(item_);
  delete push_button_;
  delete user_widge_;
  delete item_;
}

void UserUiItem::InvateLinkmic() {
  room_->InviteJoinRtc(
      uid_.toStdString(),
      [this]() { push_button_->setText(QString::fromLocal8Bit("等待连麦")); },
      [](const std::string&) {

      });
}
