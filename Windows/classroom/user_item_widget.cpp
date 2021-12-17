#include "user_item_widget.h"
#include "ui_user_item_widget.h"
#include "user_item_widget_vm.h"
#include "title_widget_vm.h"
#include "common/iconfont_define.h"
#include "common/icon_font_helper.h"
#include "view/view_component_manager.h"
#include "view/interface/i_toast_widget.h"
#include "QDateTime"
#include "common/logging.h"


UserItemWidget::UserItemWidget(QWidget* parent)
    : ui(new Ui::UserItemWidget), QWidget(parent) {
  ui->setupUi(this);
  ui->labelNickname->setAttribute(Qt::WA_TranslucentBackground);
  ui->labelStatus->setAttribute(Qt::WA_TranslucentBackground);
  ui->widget->setAttribute(Qt::WA_TranslucentBackground);
  ui->widget_2->setAttribute(Qt::WA_TranslucentBackground);
  ui->widget_3->setAttribute(Qt::WA_TranslucentBackground);
  ui->moreSettingBtn->setAttribute(Qt::WA_TranslucentBackground);
  ui->firstBtn->hide();
  ui->secondBtn->hide();
  ui->cameraStatus->hide();
  ui->micStatus->hide();
  connect(ui->moreSettingBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnMoreSettingBtnClicked);
  connect(ui->micStatus, &QPushButton::clicked, this,
          &UserItemWidget::OnMicBtnClicked);
  InitConnect();
  InitIconFont();
  ui->moreSettingBtn->setVisible(false);
}

UserItemWidget::~UserItemWidget() {
  delete ui;
}

void UserItemWidget::UpdateRoomId(const std::string& room_id) {
  vm_->UpdateClassroomId(room_id);
}

void UserItemWidget::UpdateUserInfo(UserInfo& user_info,
                                    ClassRoleEnum& my_role) {
  vm_->UpdateUserInfo(user_info);
  vm_->UpdateMyRole(my_role);
  SetStyle();
}
void UserItemWidget::SetUserAvatar(const std::string& avatar_url) {
  //todo
}

std::string UserItemWidget::GetUserId() {
  UserInfo user_info = vm_->GetUserInfo();
  return user_info.user_id;
}

bool UserItemWidget::IsLink() {
  UserInfo user_info = vm_->GetUserInfo();
  return user_info.status.GetStatus() == 3;
}

void UserItemWidget::SetCameraStatusVisable(bool visable) {
  ui->cameraStatus->setVisible(visable);
}

void UserItemWidget::SetMicStatusVisable(bool visable) {
  ui->micStatus->setVisible(visable);
}

void UserItemWidget::SetCameraStatus(bool is_open) { 
  if (is_open){
    IconFontHelper::Instance()->SetIcon(ui->cameraStatus, kBottomCameraUnmute);
  } else {
    IconFontHelper::Instance()->SetIcon(ui->cameraStatus, kBottomCameraMute);
  }
}

void UserItemWidget::SetMicStatus(bool is_open) {
  mic_open_status_ = is_open;
  if (is_open) {
    IconFontHelper::Instance()->SetIcon(ui->micStatus, kBottomMicUnmute);
  } else {
    IconFontHelper::Instance()->SetIcon(ui->micStatus, kBottomMicMute);
  }
}
void UserItemWidget::SetStyle() {
  auto user_info = vm_->GetUserInfo();
  auto my_role = vm_->GetMyRole();

  ui->labelNickname->setText(QString::fromStdString(user_info.nick.c_str()));
  ui->labelStatus->setText(user_info.status.GetDesc());

  if (my_role == ClassRoleEnum_Teacher) {
    switch (user_info.status.GetStatus()) {
      case UserStatus::kJoinFailed:
      case UserStatus::kLeave: {
        // 离会状态
        SetInviteButton();
      } break;
      case UserStatus::kOnJoining: {
        //待应答，显示撤销按钮
        SetRevokeButton();
      } break;
      case UserStatus::kApplying: {
        //申请中，显示连接/拒绝
        SetApplyingButton();
      } break;
      case UserStatus::kActive: {
        //已连麦，显示挂断
        SetHangUpButton();
      } break;
      default:
        break;
    }

    if (user_info.status.GetStatus() == UserStatus::kActive) {
      SetCameraStatusVisable(true);
      SetMicStatusVisable(true);
    } else {
      SetCameraStatusVisable(false);
      SetMicStatusVisable(false);
    }
  }
}

void UserItemWidget::InitConnect() {
  vm_ = std::make_shared<UserItemWidgetVM>();
  connect(vm_.get(), &UserItemWidgetVM::SignalUpdateVM, this,
          &UserItemWidget::OnVMUpdate, Qt::QueuedConnection);
}

void UserItemWidget::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->cameraStatus, kBottomCameraUnmute);
  IconFontHelper::Instance()->SetIcon(ui->micStatus, kBottomMicUnmute);
  IconFontHelper::Instance()->SetIcon(ui->moreSettingBtn, kMoreVertical);
}

void UserItemWidget::OnVMUpdate(int32_t filed) {
  if (filed & UserItemField_InviteRtcSuccess) {
    //待应答，显示撤销按钮
    SetRevokeButton();
    ui->labelStatus->setText(QTranslate("InteractiveClass.UserStatus.Calling"));
  }

  if (filed & UserItemField_ApproveRtcSuccess){

  }

  if (filed & UserItemField_KickUserSuccess) {

  }

  if (filed & UserItemField_RefuseRtcSuccess) {
    QTimer::singleShot(100, [this]() {
      GetViewComponent<IToastWidget>(kToastWindow)->
        ShowSuccessToast(QTranslate(is_reject_btn_ ? "InteractiveClass.UserStatus.RefuseApproveSuccess" : "InteractiveClass.UserStatus.RefuseSuccess"));
    
    });
    
  }

  if (filed & UserItemField_InviteRtcFailed) {
    vm_->IsConfStarted([this](bool start) {
      if (!start) {
        GetViewComponent<IToastWidget>(kToastWindow)->
          ShowFailedToast(QTranslate("InteractiveClass.InviteError.NotStart"));
      }
    });
    ResetButton();
  }

  if ((filed & UserItemField_KickUserFailed) || (filed & UserItemField_ApproveRtcFailed)) {
    ResetButton();
  }
}

void UserItemWidget::OnLinkmicClicked() {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnLinkmicClicked, uid:%s", vm_->GetUserInfo().user_id.c_str());
  vm_->InviteRtc();
  disconnect(ui->firstBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnLinkmicClicked);
}

void UserItemWidget::OnApproveLinkmicClicked() {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnApproveLinkmicClicked, uid:%s", vm_->GetUserInfo().user_id.c_str());
  vm_->ApproveLinkmic();
  vm_->InviteRtc();
  disconnect(ui->firstBtn, &QPushButton::clicked, this,
             &UserItemWidget::OnApproveLinkmicClicked);
}

void UserItemWidget::OnRefuseLinkmicClicked() {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnApproveLinkmicClicked, uid:%s", vm_->GetUserInfo().user_id.c_str());
  vm_->RefuseLinkmic();
  disconnect(ui->secondBtn, &QPushButton::clicked, this,
             &UserItemWidget::OnRefuseLinkmicClicked);
}

void UserItemWidget::OnKickUserFromRtcClicked() {
  LogWithTag(ClassroomTagLinkMic, LOG_INFO, "OnKickUserFromRtcClicked, uid:%s", vm_->GetUserInfo().user_id.c_str());
  vm_->KickUserFromRtc();
  disconnect(ui->secondBtn, &QPushButton::clicked, this,
             &UserItemWidget::OnKickUserFromRtcClicked);
}

void UserItemWidget::OnMoreSettingBtnClicked() {
  static int i = 0;
  if (i % 4 == 0) {
    SetCameraStatusVisable(true);
    SetMicStatusVisable(true);
  } else if (i % 4 == 1) {
    SetCameraStatus(false);
    SetMicStatus(false);
  } else if (i % 4 == 2) {
    SetCameraStatus(true);
    SetMicStatus(true);
  } else {
    SetCameraStatusVisable(false);
    SetMicStatusVisable(false);
  }

  i++;
}

void UserItemWidget::OnMicBtnClicked() {
  vm_->MuteUser(!mic_open_status_);
}

void UserItemWidget::SetInviteButton() {
  // 邀请连麦按钮
  ui->firstBtn->setText(QTranslate("InteractiveClass.UserStatus.Apply"));
  ui->firstBtn->show();
  ui->secondBtn->hide();
  ui->firstBtn->disconnect();
  connect(ui->firstBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnLinkmicClicked);
}

void UserItemWidget::SetHangUpButton() {
  // 挂断按钮
  ui->secondBtn->setText(QTranslate("InteractiveClass.UserStatus.Hangup"));
  ui->secondBtn->disconnect();
  connect(ui->secondBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnKickUserFromRtcClicked);
  ui->secondBtn->show();
  ui->firstBtn->hide();
}

void UserItemWidget::SetApplyingButton() {
  ui->firstBtn->setText(QTranslate("InteractiveClass.UserStatus.Connect"));
  ui->firstBtn->show();
  ui->secondBtn->setText(QTranslate("InteractiveClass.UserStatus.Reject"));
  ui->secondBtn->show();
  ui->firstBtn->disconnect();
  ui->secondBtn->disconnect();
  connect(ui->firstBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnApproveLinkmicClicked);
  connect(ui->secondBtn, &QPushButton::clicked, this,
          &UserItemWidget::OnRefuseLinkmicClicked);
  is_reject_btn_ = true;
}

void UserItemWidget::SetRevokeButton() {
  ui->secondBtn->setText(QTranslate("InteractiveClass.UserStatus.Redraw"));
  ui->secondBtn->show();
  ui->firstBtn->hide();
  ui->secondBtn->disconnect();
  connect(ui->secondBtn, &QPushButton::clicked, this,
             &UserItemWidget::OnKickUserFromRtcClicked);
  connect(ui->secondBtn, &QPushButton::clicked, this,
    &UserItemWidget::OnRefuseLinkmicClicked);
  is_reject_btn_ = false;
}