#include "title_widget.h"
#include "ui_title_widget.h"
#include "title_widget_vm.h"
#include "common/iconfont_define.h"
#include "common/icon_font_helper.h"
#include "QDateTime"
#include "conf_performance_dialog.h"
#include "conf_share_dialog.h"
#include "view/view_component_manager.h"
#include "view/interface/i_toast_widget.h"
#include "event/event_manager.h"
#include <stdlib.h>

TitleWidget::TitleWidget(QWidget *parent) :
  ui(new Ui::TitleWidget), QWidget(parent) {
  ui->setupUi(this);
  InitConnect();
  InitIconFont();
  InitClassCode();
  InitHoverWidget();
  ui->networkLabel->installEventFilter(this);
  SetLiveStarted(false);
  ui->env->setVisible(false);
  ui->env_label->setVisible(false);
}

TitleWidget::~TitleWidget() {
  delete ui;
}

void TitleWidget::InitConnect() {
  vm_ = std::make_shared<TitleWidgetVM>();
  connect(vm_.get(), &TitleWidgetVM::SignalUpdateVM, this, &TitleWidget::OnVMUpdate);
}

void TitleWidget::OnLiveStarted() { 
  SetLiveStarted(true);
  if (class_timer_) {
    class_timer_->stop();
    delete class_timer_;
    class_timer_ = nullptr;
  }
  class_timer_ = new QTimer(this);
  connect(class_timer_, &QTimer::timeout, this, &TitleWidget::OnClassTimer);
  class_timer_->start(1000);
}


void TitleWidget::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->shareInfoBtn, kIconMore, 18);
  IconFontHelper::Instance()->SetIcon(ui->statusLabel, kDing, 18);

  IconFontHelper::Instance()->SetIcon(ui->networkLabel, kPerformance, 18);
  IconFontHelper::Instance()->SetIcon(ui->closeBtn, kCloseWindow, 18);
  
}

void TitleWidget::OnVMUpdate(int32_t filed) {
  const TitleWidgetModel& model = vm_->GetTitleWidgetModel();
  if (filed & TitleWidgetFiled_Cpu) {
    if (performance_dialog_) {
      performance_dialog_->UpdateCpuInfo(QString("%1%").arg(model.cpu));
    }
  }

  if (filed & TitleWidgetFiled_Memory) {
    if (performance_dialog_) {
      QString text;
      if (model.memory_used > 1024) {
        text = QString::asprintf("%.1fGB", (double)model.memory_used / 1024.0);
      } else {
        text = QString("%1MB").arg(model.memory_used);
      }
      text += "/";

      if (model.memory_total > 1024) {
        text += QString::asprintf("%.1fGB", (double)model.memory_total / 1024.0);
      }
      else {
        text += QString("%1MB").arg(model.memory_total);
      }
      performance_dialog_->UpdateMemory(text);
    }

  }

  if (filed & TitleWidgetFiled_Network) {
    if (performance_dialog_) {
      performance_dialog_->UpdateBitrate(QString("%1kbps").arg(model.bitrate));
      performance_dialog_->UpdateLantency(QString("%1ms").arg((int32_t)model.network_latency));
    }
  }
  
  if (filed & TitleWidgetFiled_StartTime) {
    InitTimer();
    if (conf_share_dialog_) {
      conf_share_dialog_->UpdateTitle(model.title);
    }
  }

  if (filed & TitleWidgetFiled_ClassId) {
    if (conf_share_dialog_) {
      conf_share_dialog_->UpdateClassId(model.class_room_code);
    }
  }
}

void TitleWidget::InitClassCode() {
  const TitleWidgetModel& model = vm_->GetTitleWidgetModel();
  std::string code = model.class_room_code;
  vm_->UpdateClassroomId(code, "");
  //ui->code->setText(QString::fromStdString(code));
}

void TitleWidget::SetLiveStarted(bool start) {
  ui->unStartedLabel->setVisible(!start);
  ui->time->setVisible(start);
  ui->statusLabel->setStyleSheet(start ? "color:rgb(255, 85, 0);" : "color:rgb(79, 172, 255);");
}

void TitleWidget::InitTimer() {
  auto model = vm_->GetTitleWidgetModel();
  start_time_ = model.start_time;
  if (!model.class_started) {
    SetLiveStarted(false);
  } else {
    SetLiveStarted(true);
    if (class_timer_) {
      class_timer_->stop();
      delete class_timer_;
      class_timer_ = nullptr;
    }
    class_timer_ = new QTimer(this);
    connect(class_timer_, &QTimer::timeout, this, &TitleWidget::OnClassTimer);
    class_timer_->start(1000);
  }
  
}

void TitleWidget::OnClassTimer() {
  QDateTime current_date_time = QDateTime::currentDateTime();
  int64_t seconds = current_date_time.toSecsSinceEpoch() - start_time_/1000;

  QString str_time;
  if (seconds > 0) {
    int32_t hours = seconds / 3600;
    int32_t minutes = (seconds - 3600 * hours) / 60;
    seconds = seconds % 60;
    if (hours > 0) {
      if (hours < 10) {
        str_time += ("0" + QString::fromStdString(std::to_string(hours)) + ":");
      } else {
        str_time += QString::fromStdString(std::to_string(hours)) + ":";
      }
      
    } else {
      str_time += "00:";
    }

    if (minutes > 0) {
      if (minutes < 10) {
        str_time += ("0" + QString::fromStdString(std::to_string(minutes)) + ":");
      } else {
        str_time += QString::fromStdString(std::to_string(minutes)) + ":";
      }
    }
    else {
      str_time += "00:";
    }
    if (seconds > 0) {
      if (seconds < 10){
        str_time += ("0" + QString::fromStdString(std::to_string(seconds)));
      } else {
        str_time += QString::fromStdString(std::to_string(seconds));
      }
      
    }
    else {
      str_time += "00";
    }

  }
 
  ui->time->setText(str_time);
  emit EventManager::Instance()->SignalLiveTime(str_time);
}



QWidget* GetTopParentWidget(QWidget* widget) {
  QWidget* top_parent_widget = widget;
  while (top_parent_widget->parentWidget())
  {
    top_parent_widget = top_parent_widget->parentWidget();
  }

  return top_parent_widget;
}

void TitleWidget::mousePressEvent(QMouseEvent *event) {
  if (drag_window_) {
    press_point_ = event->globalPos();
    start_pos_ = pos();

    QWidget* top_parent = GetTopParentWidget(this);
    if (top_parent) {
      parent_start_pos_ = top_parent->pos();
    }

    mouse_btn_pressed = true;
  }

  mouse_btn_pressed = true;
  QWidget::mousePressEvent(event);
}

void TitleWidget::mouseMoveEvent(QMouseEvent *event) {
  if( drag_window_ && mouse_btn_pressed) {
    auto move_pos = event->globalPos();
    auto offset_pos = move_pos - press_point_;
    if (offset_pos == QPoint(0, 0)) {
      return QWidget::mouseMoveEvent(event);
    }

    QWidget* top_parent = GetTopParentWidget(this);
    if (top_parent) {
      auto new_pos = parent_start_pos_ + offset_pos;
      QWidget* top_parent = GetTopParentWidget(this);
      if (top_parent) {
        top_parent->move(new_pos);
      }
    }
    else {
      move(start_pos_ + offset_pos);
    }
  }

  QWidget::mouseMoveEvent(event);
}

void TitleWidget::mouseReleaseEvent(QMouseEvent *event) { 
  if (drag_window_) {
    mouse_btn_pressed = false;
  }

  QWidget::mouseReleaseEvent(event);
}

void TitleWidget::UpdateParentStartPos(QPoint pos) {
  if (drag_window_) {
    parent_start_pos_ = pos;
  }
}

bool TitleWidget::eventFilter(QObject* target, QEvent* event) {
  if (event->type() == QEvent::MouseButtonRelease) {
    if (target == ui->networkLabel) {
      on_performance_clicked();
      return true;
    } 

  }
  
  return QWidget::eventFilter(target, event);
}

void TitleWidget::UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info) {
  vm_->UpdateClassroomId(meta_info.class_room_id, "");
}

void TitleWidget::UpdateStartTime(bool started, int64_t time) {
  vm_->UpdateStartTime(started, time);
}

void TitleWidget::UpdateNetwork(double latency, int64_t bitrate) {
  vm_->UpdateNetwork(latency, bitrate);
}

void TitleWidget::UpdateTitle(const std::string& title) {
  if (conf_share_dialog_) {
    conf_share_dialog_->UpdateTitle(title);
  }
  QString str_temp = QString::fromStdString(title);
  QFontMetrics font_width(ui->time->font());
  int width = font_width.width(str_temp);
  if (width >= 240) {
    str_temp = font_width.elidedText(str_temp, Qt::ElideRight, 240);
  }
  ui->title->setText(str_temp);
}

void TitleWidget::InitHoverWidget() {

  if (!performance_dialog_) {
    performance_dialog_ = new ConfPerformanceDialog();
  }

  if (!conf_share_dialog_) {
    conf_share_dialog_ = new ConfShareDialog();
  }
    
}

void TitleWidget::on_shareInfoBtn_clicked() {
  auto model = vm_->GetTitleWidgetModel();
  if (model.start_time <= 0) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(QTranslate("InteractiveClass.NotStart"));
    return;
  }
  QPoint pt = QCursor::pos();
  if (conf_share_dialog_) {
    conf_share_dialog_->ShowAtTopLeft(pt.x(), pt.y());
  }
}


void TitleWidget::on_performance_clicked() {
  QPoint pt = QCursor::pos();
  if (performance_dialog_) {
    performance_dialog_->ShowAtTopLeft(pt.x(), pt.y());
  }
}


void TitleWidget::on_closeBtn_clicked() {
  emit NotifyClose();
}

