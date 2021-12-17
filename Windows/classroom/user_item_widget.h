#pragma once

#include <QWidget>
#include <QDialog>

class UserItemWidgetVM;
class UserInfo;
enum ClassRoleEnum;

namespace Ui {
class UserItemWidget;
}

class UserItemWidget : public QWidget {
  Q_OBJECT

public:
  explicit UserItemWidget(QWidget* parent = nullptr);
 ~UserItemWidget();

  void UpdateRoomId(const std::string& room_id);
  void UpdateUserInfo(UserInfo& user_info, ClassRoleEnum& my_role); 
  void SetUserAvatar(const std::string& avatar_url);
  void SetStyle();
  std::string GetUserId();
  bool IsLink();
  void SetCameraStatusVisable(bool Visable);
  void SetMicStatusVisable(bool Visable);
  void SetCameraStatus(bool is_open);
  void SetMicStatus(bool is_open);
 private:
  void ResetButton() { SetStyle(); }
  void InitConnect();
  void InitIconFont();
  //void InitPerformanceWidget();
  //void InitIconFont();
  //void InitClassCode();
  //void InitTimer();
  //virtual void mousePressEvent(QMouseEvent *event) override;
  //virtual void mouseMoveEvent(QMouseEvent *event) override;
  //virtual void mouseReleaseEvent(QMouseEvent *event) override;
  //void UpdateParentStartPos(QPoint pos);
  //QPoint PressPoint() const { return press_point_; }
private slots:
  void OnVMUpdate(int32_t filed);
  void OnLinkmicClicked();
  void OnApproveLinkmicClicked();
  void OnRefuseLinkmicClicked();
  void OnKickUserFromRtcClicked();
  void OnMoreSettingBtnClicked();
  void OnMicBtnClicked();
 private:
  void SetInviteButton();
  void SetRevokeButton();
  void SetHangUpButton();
  void SetApplyingButton();
 private:
  Ui::UserItemWidget* ui;
  std::shared_ptr<UserItemWidgetVM> vm_;
  //QString cpu_format_info_;
  //QString memory_format_info_;
  //QString network_format_info_;
  //QTimer* class_timer_ = nullptr;
  //int64_t start_time_ = 0;
  bool is_reject_btn_ = false;
  bool mic_open_status_ = false;
};

