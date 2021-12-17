#include "user_list_widget.h"
#include "QDateTime"
#include "common/icon_font_helper.h"
#include "common/iconfont_define.h"
#include "ui_user_list_widget.h"
#include "user_list_widget_vm.h"
#include "user_item_widget.h"
#include "view/interface/i_toast_widget.h"
#include <QStackedWidget>
#include "event/event_manager.h"
#include "view/view_component_manager.h"

static const int USER_ITEM_WIDTH = 263;
static const int USER_ITEM_HEIGHT = 49;
static const int LIST_MAX_HEIGHT = 480;
UserListWidget::UserListWidget(QWidget* parent)
    : ui(new Ui::UserListWidget), QWidget(parent) {
  ui->setupUi(this);
  // InitPerformanceWidget();
  InitConnect();
  // InitIconFont();
  // InitClassCode();
}

UserListWidget::~UserListWidget() { delete ui; }

void UserListWidget::InitConnect() {
  vm_ = std::make_shared<UserListWidgetVM>();
  connect(vm_.get(), &UserListWidgetVM::SignalUpdateVM, this,
          &UserListWidget::OnVMUpdate, Qt::QueuedConnection);
  connect(vm_.get(), &UserListWidgetVM::SignalLinkMicUserUpdate, this,
    &UserListWidget::OnLinkMicUserNotify, Qt::QueuedConnection);
  connect(ui->listWidget, &QListWidget::itemSelectionChanged, this, &UserListWidget::OnListItemChanged);
}

void UserListWidget::OnLinkMicUserNotify(const std::vector<std::string>& users) {
  emit LinkMicUserNotify(users);
}

void UserListWidget::SetRoomId(const std::string& room_id) {
  vm_->UpdateClassroomId(room_id);
}

void UserListWidget::LoadUserList() {
  vm_->LoadUserList(false);
}

void UserListWidget::UpdateClassRole(const ClassRoleEnum& role) {
  vm_->UpdateClassRole(role);
  if (role == ClassRoleEnum_Student) {
    ui->MuteAllButton->hide();
  }
}

void UserListWidget::UpdateUserId(const std::string& user_id) {
  vm_->UpdateUserId(user_id);
}

void UserListWidget::OnListItemChanged() {
  QList<QListWidgetItem*> selected_items = ui->listWidget->selectedItems();
  if (selected_items.size() > 0) {
    QListWidgetItem* item = selected_items[0];
    QWidget* widget = ui->listWidget->itemWidget(item);
    QStackedWidget* stack_widget = dynamic_cast<QStackedWidget*>(widget);
    UserItemWidget* item_widget = dynamic_cast<UserItemWidget*>(stack_widget->widget(0));
    if (item_widget) {
      bool link = item_widget->IsLink();
      if (link) {
        emit SignalListItemChange(QString::fromStdString(item_widget->GetUserId()), link);
      }
    }
  }
}

void UserListWidget::OnVMUpdate(int32_t filed) {
  if (filed & Field_UserListUpdate) {


    bool is_student = vm_->GetUserListParam().role == ClassRoleEnum_Student;
    std::vector<UserInfo> user_list = vm_->GetUserList(false);

    std::vector<std::shared_ptr<WidgetItemInfo>> user_item_widget_info_temp;
    for (auto user : user_list) {
      if (user_item_widget_info_.find(user.user_id) !=
          user_item_widget_info_.end()) {
        user_item_widget_info_[user.user_id]->user_info = user;
        user_item_widget_info_temp.push_back(user_item_widget_info_[user.user_id]);
        RemoveDisplayFromContainer(user_item_widget_info_[user.user_id]->user_item_widget.get());
      } else {
        auto user_item_widget = std::make_shared<UserItemWidget>();
        auto item_info = std::make_shared<WidgetItemInfo>(); 
        item_info->user_item_widget = user_item_widget;
        item_info->user_info = user;
        user_item_widget_info_temp.push_back(item_info);
      }
    }
    user_item_widget_info_.clear();
    ui->listWidget->clear();
    ui->listWidget_2->clear();

    auto user_list_param = vm_->GetUserListParam();
    bool has_link_user = false;
    link_mic_user_num_ = 0;
    no_link_mic_user_num_ = 0;


    for (auto user_info : user_item_widget_info_temp) {
      auto user = user_info->user_info;
      // 显示用户列表
      if (user_list_param.role == ClassRoleEnum_Teacher &&
          user.user_id == user_list_param.user_id) {
        // 如果角色是老师不显示自己
        continue;
      }
        
      auto item = new QListWidgetItem();
      auto stack = new QStackedWidget();
      auto user_item_widget = user_info->user_item_widget.get();
      AddDisplayToContainer(stack, user_item_widget);
      user_item_widget->UpdateRoomId(user_list_param.class_room_id);
      user_item_widget->UpdateUserInfo(user, user_list_param.role);
      item->setSizeHint(QSize(USER_ITEM_WIDTH, USER_ITEM_HEIGHT)); 

      if (user.status.GetStatus() == 3) {
        link_mic_user_num_++;
        ui->listWidget->addItem(item);
        ui->listWidget->setItemWidget(item, stack);
      } else {
        no_link_mic_user_num_++;
        ui->listWidget_2->addItem(item);
        ui->listWidget_2->setItemWidget(item, stack);
      }
      if (!has_link_user&& user.status.GetStatus() == 3) {
        has_link_user = true;
      }
    }

    for (auto info : user_item_widget_info_temp) {
      user_item_widget_info_[info->user_info.user_id] = info;
    }

    AdjustListHeight();

    emit SignalListCountChange(user_list.size());
    if (!has_link_user && user_list_param.role == ClassRoleEnum_Teacher) {
      emit SignalListItemChange(QString::fromStdString(user_list_param.user_id), true);
    }
  }
}

void UserListWidget::resizeEvent(QResizeEvent* event) {
  QWidget::resizeEvent(event);
  AdjustListHeight();
}

void UserListWidget::AdjustListHeight(){
  int32_t container_height = height() - ui->bottomWidget->height();

  // 设置两张表的高度
  if ((link_mic_user_num_ + no_link_mic_user_num_) * USER_ITEM_HEIGHT <
    container_height) {
    // 如果总高度小于最大高度
    ui->listWidget->setMaximumHeight(link_mic_user_num_ * USER_ITEM_HEIGHT);
    ui->listWidget_2->setMaximumHeight(container_height - link_mic_user_num_ * USER_ITEM_HEIGHT);
  }
  else {
    // 如果总高度大于最大高度
    if (link_mic_user_num_ * USER_ITEM_HEIGHT < container_height / 2) {
      ui->listWidget->setMaximumHeight(link_mic_user_num_ * USER_ITEM_HEIGHT);
      ui->listWidget_2->setMaximumHeight(container_height - link_mic_user_num_ * USER_ITEM_HEIGHT);
    }
    else {
      ui->listWidget->setMaximumHeight(container_height / 2);
      ui->listWidget_2->setMaximumHeight(container_height / 2);
    }
  }
}

void UserListWidget::on_MuteAllButton_clicked() {
  auto check_status = ui->MuteAllButton->checkState();
  bool open = check_status == Qt::CheckState::Checked ? false : true;

  vm_->RtcMuteAll(open,[open, this]() {
    EventManager::Instance()->PostUITask([open, this]() {
      if (!open) {
        GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(QTranslate("InteractiveClass.AdminRtcMuteOn"));
      } else{
    
      }
    });
  }, [open, this](const alibaba::dps::DPSError& error){
    EventManager::Instance()->PostUITask([open, this, error]() {
      if (error.reason == "rtc_id is empty"){
        GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(QTranslate("InteractiveClass.MuteError.NotStart"));
      }
      if (!open) {
        ui->MuteAllButton->setChecked(open);
      } else{
        ui->MuteAllButton->setChecked(open);
      }
    });
  });
}

std::shared_ptr<UserItemWidget> UserListWidget::GetUserItemWidgetByUserId(
    const std::string& user_id) {
  auto item_info = user_item_widget_info_.find(user_id);
  if (item_info != user_item_widget_info_.end()) {
    return item_info->second->user_item_widget;
  } else {
    return nullptr;
  }
}