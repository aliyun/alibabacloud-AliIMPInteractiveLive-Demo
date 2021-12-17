#pragma once

#include <QWidget>
#include <QDialog>
#include "user_item_widget_vm.h"
#include <unordered_map>

class UserListWidgetVM;
class QListWidget;
class QListWidgetItem;
class UserItemWidget;
enum ClassRoleEnum;

namespace Ui {
  class UserListWidget;
}

class UserListWidget : public QWidget
{
  Q_OBJECT

public:
  explicit UserListWidget(QWidget* parent = nullptr);
  ~UserListWidget();

  void SetRoomId(const std::string& room_id);
  void LoadUserList();
  void UpdateClassRole(const ClassRoleEnum& role);
  void UpdateUserId(const std::string& user_id);
  std::shared_ptr<UserItemWidget> GetUserItemWidgetByUserId(const std::string& user_id);
 signals:
  void SignalListCountChange(int32_t count);
  void SignalListItemChange(const QString& user, bool link);
  void LinkMicUserNotify(const std::vector<std::string>& users);
private:
  void InitConnect();
  void AdjustListHeight();
  virtual void resizeEvent(QResizeEvent* event) override;

private slots:
  void OnVMUpdate(int32_t filed);
  //void OnClassTimer();
  void on_MuteAllButton_clicked();
  void OnListItemChanged();
  void OnLinkMicUserNotify(const std::vector<std::string>& users);
private:
  Ui::UserListWidget* ui;
  std::shared_ptr<UserListWidgetVM> vm_;

private:
  struct WidgetItemInfo {
    std::shared_ptr<UserItemWidget> user_item_widget;
    UserInfo user_info;

    WidgetItemInfo::~WidgetItemInfo(){
      user_item_widget.reset();
    }
  };
  int32_t link_mic_user_num_ = 0;
  int32_t no_link_mic_user_num_ = 0;
  std::unordered_map<std::string, std::shared_ptr<WidgetItemInfo>> user_item_widget_info_;
};
