#include "bottom_widget.h"
#include "bottom_widget_teacher_vm.h"
#include "common/icon_font_helper.h"
#include "common/logging.h"
#include "confirm_dialog.h"
#include "share_screen_hover_bar_dlg.h"
#include "share_screen_select_dlg.h"
#include "ui_bottom_widget.h"
#include "view/interface/i_toast_widget.h"
#include "view/view_component_manager.h"
#include "event/event_manager.h"

extern QWidget* GetTopParentWidget(QWidget* widget);
BottomWidget::BottomWidget(QWidget* parent)
    : QWidget(parent),
      ui(new Ui::BottomWidget),
      vm_(new BottomWidgetTeacherVM) {
  ui->setupUi(this);
  InitIconFont();
  InitDeviceListConnect(parent);
  connect(vm_.get(), &BottomWidgetTeacherVM::SignalUpdateVM, this,
          &BottomWidget::OnVMUpdate, Qt::QueuedConnection);
}

BottomWidget::~BottomWidget() { delete ui; }

void BottomWidget::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->micMore, kIconVNextOld);
  IconFontHelper::Instance()->SetIcon(ui->cameraMore, kIconVNextOld);
  ui->cameraBtn->SetIcon(kBottomCameraUnmute, 24);
  ui->cameraBtn->SetDelta(QPoint(-2, 0));
  ui->micBtn->SetIcon(kBottomMicUnmute, 24);

  IconFontHelper::Instance()->SetIcon(ui->classBtn, kBottomStartClass);
  IconFontHelper::Instance()->SetIcon(ui->shareScreenBtn, kBottomScreenShare);
  IconFontHelper::Instance()->SetIcon(ui->recordBtn, kBottomRecord);
  ui->classSuitWidget->setVisible(false);
  ui->classToolWidget->setVisible(false);
}

void BottomWidget::on_cameraMore_clicked() {
  if (ui->cameraMore->text() == QString(kIconVPre)) {
    IconFontHelper::Instance()->SetIcon(ui->cameraMore, kIconVNextOld);
    device_list_video_->HideDeviceList();
  } else {
    IconFontHelper::Instance()->SetIcon(ui->cameraMore, kIconVPre);
    QRect rc = ui->cameraMore->geometry();
    QPoint pt(rc.left(), rc.top());
    QPoint pt_mapped = ui->cameraMore->parentWidget()->mapToGlobal(pt);
    pt_mapped.setY(pt_mapped.y() - 12);
    device_list_video_->Init(DeviceTypeEnum_Video);
    device_list_video_->ShowDeviceList(pt_mapped);
  }
}

void BottomWidget::on_micMore_clicked() {
  QPoint pt = QCursor::pos();
  device_list_audio_->ShowDeviceList(pt);
  if (ui->micMore->text() == QString(kIconVPre)) {
    IconFontHelper::Instance()->SetIcon(ui->micMore, kIconVNextOld);
    device_list_audio_->HideDeviceList();
  } else {
    IconFontHelper::Instance()->SetIcon(ui->micMore, kIconVPre);
    QRect rc = ui->micMore->geometry();
    QPoint pt(rc.left(), rc.top());
    QPoint pt_mapped = ui->micMore->parentWidget()->mapToGlobal(pt);
    pt_mapped.setY(pt_mapped.y() - 12);
    device_list_audio_->Init(DeviceTypeEnum_Audio);
    device_list_audio_->ShowDeviceList(pt_mapped);
  }
}

void BottomWidget::on_micBtn_clicked() {
  bool checked = ui->micBtn->isChecked();
  LogWithTag(ClassroomTagLocalMedia, LOG_INFO, "on_micBtn_clicked checked:%d",
             checked);
  vm_->MuteLocalAudio(checked);
  SignalMuteLocalAudio(checked);
  UpdateButtonStatus(ui->micBtn);
  QString notify = !checked ? QTranslate("InteractiveClass.MicOn")
                            : QTranslate("InteractiveClass.MicOff");
  if (!checked) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  } else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }
  if (hover_bar_) {
    hover_bar_->setMicBtnChecked(checked);
  }
}

void BottomWidget::on_cameraBtn_clicked() {
  bool checked = ui->cameraBtn->isChecked();
  LogWithTag(ClassroomTagLocalMedia, LOG_INFO,
             "on_cameraBtn_clicked checked:%d", checked);
  SignalMuteLocalVideo(checked);

  UpdateButtonStatus(ui->cameraBtn);
  QString notify = !checked ? QTranslate("InteractiveClass.CameraOn")
                            : QTranslate("InteractiveClass.CameraOff");
  if (!checked) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  } else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }
}


void BottomWidget::on_classBtn_clicked() {
  bool checked = ui->classBtn->isChecked();
  UpdateButtonStatus(ui->classBtn);
  if (checked) {
    ui->classBtn->setEnabled(false);
    LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StartClass start");
    vm_->StartClass();
  } else {
    ConfirmDialog dialog(GetTopParentWidget(this));
    DialogParam param;
    param.title = QTranslate("InteractiveClass.EndClassConfirm");
    param.content = QTranslate("InteractiveClass.EndClassNotify");
    param.type = DialogTypeOkCancel;
    param.icon_type = DialogIconTypeFailed;
    param.btn_vec = {QTranslate("OK"), QTranslate("Cancel")};
    dialog.SetDialogParam(param);
    if (dialog.exec() != QDialog::Accepted) {
      ui->classBtn->setChecked(true);
      UpdateButtonStatus(ui->classBtn);
      return;
    }
    vm_->StopClass();
  }
}

void BottomWidget::OnHoverBarMicBtnCliecked() {
  bool checked = ui->micBtn->isChecked();
  ui->micBtn->setChecked(!checked);
  on_micBtn_clicked();
}

void BottomWidget::OnHoverBarRecordBtnCliecked() {
  bool checked = ui->recordBtn->isChecked();
  ui->recordBtn->setChecked(!checked);
  on_recordBtn_clicked();
}

void BottomWidget::OnSignalStopShare() {
  if (vm_->GetShareScreenOpen()) {
    bool is_checked = ui->shareScreenBtn->isChecked();
    ui->shareScreenBtn->setChecked(false);
    on_shareScreenBtn_clicked();
  }
}

void BottomWidget::OpenHoverBar() {
  if (hover_bar_ == nullptr) {
    hover_bar_ = new ShareScreenHoverBar(vm_->GetClassroomId(), this);
    connect(hover_bar_, &ShareScreenHoverBar::SignalMicBtnClicked, this,
            &BottomWidget::OnHoverBarMicBtnCliecked);
    connect(hover_bar_, &ShareScreenHoverBar::SignalStopShare, this,
            &BottomWidget::OnSignalStopShare);
    connect(hover_bar_, &ShareScreenHoverBar::SignalRecordBtnClicked, this,
            &BottomWidget::OnHoverBarRecordBtnCliecked);
  }
  hover_bar_->setMicBtnChecked(ui->micBtn->isChecked());
  hover_bar_->setDesktopAudioBtnChecked(selected_windows_info_.open_system_audio);
  hover_bar_->setRecordBtnChecked(ui->recordBtn->isChecked());
  hover_bar_->ShowTopCenter();
}

void BottomWidget::CloseHoverBar() {
  if (hover_bar_) {
    hover_bar_->HideTopCenter();
    hover_bar_->hide();
  }
}

void BottomWidget::on_shareScreenBtn_clicked() {
  ui->shareScreenBtn->setChecked(false);
  auto model = vm_->GetBottomWidgetModel();
  if (!model.class_started) {
    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(QTranslate("InteractiveClass.ShareCreen.NotStart"));
    return;
  }
  
  if (!vm_->GetShareScreenOpen()) {
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO,
               "on_shareScreenBtn_clicked start");
    
    ShareScreenSelectDlg dlg(vm_->GetClassroomId(), this);
    if (dlg.exec() != QDialog::Accepted) {
      return;
    }
    selected_windows_info_ = dlg.getSelectWindowInfo();
    if (selected_windows_info_.is_screen) {
      vm_->StartDesktopShareScreen(selected_windows_info_.wid);
    } else {
      vm_->StartWindowShareScreen(selected_windows_info_.wid);
    }

    if (selected_windows_info_.open_system_audio) {
      emit EventManager::Instance()->SignalTeacherShareSystemAudio(true);
    }
  } else {
    LogWithTag(ClassroomTagLocalMedia, LOG_INFO,
               "on_shareScreenBtn_clicked stop");
    vm_->StopShareScreen();
    CloseHoverBar();
  }
}

void BottomWidget::on_recordBtn_clicked() {
  
  auto model = vm_->GetBottomWidgetModel();
  if (model.class_started) {
    ui->recordBtn->setEnabled(false);
    if (model.recording_state == EnumRecordingState::EnumRecordingStateStopped 
      || model.recording_state == EnumRecordingState::EnumRecordingStatePaused) {
      vm_->StartRecording();
    }
    else if (model.recording_state == EnumRecordingState::EnumRecordingStateStarted) {
      vm_->StopRecording();
    }
    else {
      ui->recordBtn->setEnabled(true);
    }
  }
  else {
    OnVMUpdate(BottomWidgetTeacherRecodingState);
    // TODO hint
  }
}

void BottomWidget::on_stopBtn_clicked() {
  ConfirmDialog dialog(GetTopParentWidget(this));
  DialogParam param;
  param.title = QTranslate("InteractiveClass.LeaveClassConfirm");
  param.content = QTranslate("InteractiveClass.ReEnterRoom");
  param.type = DialogTypeOkCancel;
  param.icon_type = DialogIconTypeFailed;
  param.btn_vec = {QTranslate("OK"), QTranslate("Cancel")};
  dialog.SetDialogParam(param);
  if (dialog.exec() != QDialog::Accepted) {
    return;
  }
  LogWithTag(ClassroomTagLivePlay, LOG_INFO, "StopClass");
  vm_->StopClass();
}

void BottomWidget::UpdateClassroomInfo(const std::string& classroom_id,
                                       const std::string& uid) {
  vm_->UpdateClassroomId(classroom_id);
  vm_->UpdateUserId(uid);
  device_list_audio_->Init(DeviceTypeEnum_Audio);
  device_list_audio_->UpdateClassroomId(classroom_id);
  device_list_video_->Init(DeviceTypeEnum_Video);
  device_list_video_->UpdateClassroomId(classroom_id);
}


void BottomWidget::UpdateInClassStatus(bool need_start) {
  auto model = vm_->GetBottomWidgetModel();
  if (model.class_started) {
    return;
  }
  if (need_start) {
    ui->classBtn->setEnabled(false);
    vm_->StartClass();
    ui->classBtn->setChecked(true);
    UpdateButtonStatus(ui->classBtn);
  }
}

void BottomWidget::OnVMUpdate(int32_t field) {

  auto model = vm_->GetBottomWidgetModel();

  if (field & BottomWidgetFieldClassStarted) {
    ui->classBtn->setEnabled(true);
    emit NoitfyClassStart();
  }

  if (field & BottomWidgetTeacherStartDesktopShareScreen) {
    ui->shareScreenBtn->setDisabled(false);
    if (vm_->GetShareScreenOpen()) {
      ui->shareScreenBtn->setChecked(true);
      ui->shareScreenLabel->setText(QTranslate("InteractiveClass.EndShare"));
      OpenHoverBar();
    } else {
      ui->shareScreenBtn->setChecked(false);
      ConfirmDialog dialog(GetTopParentWidget(this));
      DialogParam param;
      param.title = QTranslate("InteractiveClass.ErrorOccor");
      param.content = QString::fromStdString("error:");
      param.content +=
          QString::fromStdString(std::to_string(vm_->GetLastErrorCode()));
      param.content += QString::fromStdWString(L",");
      param.content += QString::fromStdString(vm_->GetLastErrorMsg());
      param.type = DialogTypeConfirm;
      param.icon_type = DialogIconTypeFailed;
      param.btn_vec = {QTranslate("OK")};
      dialog.SetDialogParam(param);
      if (dialog.exec()) {
        return;
      }
    }
  }

  if (field & BottomWidgetTeacherStopShareScreen) {
    if (!vm_->GetShareScreenOpen()) {
      ui->shareScreenLabel->setText(QTranslate("InteractiveClass.ShareScreen"));
    } else {
      ConfirmDialog dialog(GetTopParentWidget(this));
      DialogParam param;
      param.title = QTranslate("InteractiveClass.ErrorOccor");
      param.content = QString::fromStdString("error:");
      param.content +=
          QString::fromStdString(std::to_string(vm_->GetLastErrorCode()));
      param.content += QString::fromStdWString(L",");
      param.content += QString::fromStdString(vm_->GetLastErrorMsg());
      param.type = DialogTypeConfirm;
      param.icon_type = DialogIconTypeFailed;
      param.btn_vec = {QTranslate("OK")};
      dialog.SetDialogParam(param);
      if (dialog.exec()) {
        return;
      }
    }
  }

  if (field & BottomWidgetTeacherRecodingState) {

    ui->recordBtn->setEnabled(true);

    if (model.recording_state == EnumRecordingState::EnumRecordingStateStarted) {
      ui->recordBtn->setChecked(true);
      Q_EMIT NotifyRecordingState(true);
    }
    else {
      ui->recordBtn->setChecked(false);
      Q_EMIT NotifyRecordingState(false);
    }
    if (hover_bar_ != nullptr) {
      hover_bar_->setRecordBtnChecked(ui->recordBtn->isChecked());
    }
    
    UpdateButtonStatus(ui->recordBtn);
  }
}

void BottomWidget::InitDeviceListConnect(QWidget* parent) {
  device_list_audio_ = new DeviceList(parent);
  connect(device_list_audio_, &DeviceList::SignalHide, this,
          &BottomWidget::OnNotifyDeviceListHide);
  device_list_video_ = new DeviceList(parent);
  connect(device_list_video_, &DeviceList::SignalHide, this,
          &BottomWidget::OnNotifyDeviceListHide);
}

void BottomWidget::OnNotifyDeviceListHide(DeviceTypeEnum type) {
  switch (type) {
    case DeviceTypeEnum_Audio:
      IconFontHelper::Instance()->SetIcon(ui->micMore, kIconVNextOld);
      break;
    case DeviceTypeEnum_Video:
      IconFontHelper::Instance()->SetIcon(ui->cameraMore, kIconVNextOld);
      break;
    default:
      break;
  }
}

void BottomWidget::UpdateButtonStatus(QWidget* btn) {
  if (btn == ui->cameraBtn) {
    bool checked = ui->cameraBtn->isChecked();
    ui->cameraBtn->SetIcon(
        checked ? kBottomCameraMute : kBottomCameraUnmute, 24);
    if (checked) {
      QVector<IconData> icon_list;
      icon_list.push_back({ QChar(0xe7ca), "#FF5219" });
      ui->cameraBtn->SetIconFontList(icon_list);
    }
    else {
      QVector<IconData> icon_list;
      ui->cameraBtn->SetIconFontList(icon_list);
    }
    ui->cameraLabel->setText(checked
                                 ? QTranslate("InteractiveClass.OpenCamera")
                                 : QTranslate("InteractiveClass.CloseCamera"));
  } else if (btn == ui->micBtn) {
    bool checked = ui->micBtn->isChecked();
    ui->micBtn->SetIcon(checked ? kBottomMicMute : kBottomMicUnmute, 24);
    if (checked) {
      QVector<IconData> icon_list;
      icon_list.push_back({ QChar(0xe7ca), "#FF5219" });
      ui->micBtn->SetIconFontList(icon_list);
    }
    else {
      QVector<IconData> icon_list;
      ui->micBtn->SetIconFontList(icon_list);
    }
    ui->micLabel->setText(checked ? QTranslate("InteractiveClass.CloseMute")
                                  : QTranslate("InteractiveClass.Mute"));
  } else if (btn == ui->shareScreenBtn) {
  } else if (btn == ui->recordBtn) {
    bool checked = ui->recordBtn->isChecked();
    ui->recordLabel->setText(checked ? QTranslate("InteractiveClass.StopRecord")
                                     : QTranslate("InteractiveClass.Record"));
  } else if (btn == ui->classBtn) {
    bool checked = ui->classBtn->isChecked();
    ui->classLabel->setText(checked
                                ? QTranslate("InteractiveClass.StopClass")
                                : QTranslate("InteractiveClass.StartClass"));
  }
}
void BottomWidget::OnNotifyChangeCameraMuteButtonStatus(bool mute) {
  bool checked = mute;
  bool old_checked = ui->cameraBtn->isChecked();
  if (old_checked == checked) return;

  ui->cameraBtn->setChecked(checked);
  UpdateButtonStatus(ui->cameraBtn);
  QString notify = !checked ? QTranslate("InteractiveClass.CameraOn")
                            : QTranslate("InteractiveClass.CameraOff");
  if (!checked) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  } else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }
}
void BottomWidget::OnAutoRecordChanged(bool on)
{
  vm_->EnableAutoRecord(on);
}