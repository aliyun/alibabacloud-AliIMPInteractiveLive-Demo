#include "student_controls.h"
#include "common/icon_font_helper.h"
#include "common/logging.h"
#include "confirm_dialog.h"
#include "ui_student_controls.h"
#include "view/interface/i_toast_widget.h"
#include "view/view_component_manager.h"

extern QWidget* GetTopParentWidget(QWidget* widget);
StudentControls::StudentControls(QWidget* parent, QWidget* listen)
    : ui_(new Ui::StudentControls),
      parent_(parent),
      QWidget(parent, Qt::FramelessWindowHint | Qt::Tool),
      vm_(new BottomWidgetStudentVM) {
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);

  setMouseTracking(true);

  filter_ = std::make_unique<QWidgetEventFilter>();
  listen->installEventFilter(filter_.get());

  connect(filter_.get(), &QWidgetEventFilter::signalMove, this,
          &StudentControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this,
          &StudentControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this,
          &StudentControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this,
          &StudentControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this,
          &StudentControls::OnAdjustControls);

    connect(vm_.get(), &BottomWidgetStudentVM::SendRtcMuteToClient, this,
      &StudentControls::OnRtcMuteToClient); // receive mute/cancel mute message
  ui_->VideoControl->hide();
  ui_->MicControl->hide();
  IconFontHelper::Instance()->SetIcon(ui_->LinkmicControl, kLinkmic);

  InitShadowBox();
}

StudentControls::~StudentControls() {}

void StudentControls::InitShadowBox() {

  auto add_shadow_to_widget = [](QWidget* widget) {
    QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(widget);
    shadow->setOffset(0, 0);
    shadow->setColor(QColor("#CCCCCC"));
    shadow->setBlurRadius(24);
    widget->setGraphicsEffect(shadow);
  };

  add_shadow_to_widget(ui_->widget);
}

void StudentControls::ClearShadowBox() {
    ui_->widget->setGraphicsEffect(NULL);
}

void StudentControls::UpdateClassRoomId(const std::string& room_id) {
  vm_->UpdateClassroomId(room_id);
}

void StudentControls::UpdateUserId(const std::string& user_id) {
  vm_->UpdateUserId(user_id);
}

void StudentControls::UpdateRedLine(MultiIconButton* btn, bool show) {
  if (btn == ui_->VideoControl) {
    btn->SetDelta(QPoint(-2, 0));
  } else if (btn == ui_->MicControl) {
    btn->SetDelta(QPoint(0, 0));
  }
  if (show) {
    QVector<IconData> icon_list;
    icon_list.push_back({ QChar(0xe7ca), "#FF5219" });
    btn->SetIconFontList(icon_list);
  }
  else {
    QVector<IconData> icon_list;
    btn->SetIconFontList(icon_list);
  }
}

void StudentControls::UpdateLinkmicStatus(bool linkmic) {
  if (link_mic_status_ == linkmic) return;
  link_mic_status_ = linkmic;
  vm_->UpdateLinkMicStatus(link_mic_status_);

  // 先清除阴影效果
  ClearShadowBox();

  if (link_mic_status_ == true) {
    ui_->VideoControl->show();
    ui_->VideoControl->SetIcon(video_status_ ? kBottomCameraUnmute : kBottomCameraMute, 28);
    UpdateRedLine(ui_->VideoControl, !video_status_);
    ui_->MicControl->show();
    ui_->MicControl->SetIcon(mic_status_ ? kBottomMicUnmute : kBottomMicMute, 28);
    UpdateRedLine(ui_->MicControl, !mic_status_);
    IconFontHelper::Instance()->SetIcon(ui_->LinkmicControl, kCancelLinkmic);
  } else {
    ui_->VideoControl->hide();
    ui_->MicControl->hide();
    IconFontHelper::Instance()->SetIcon(ui_->LinkmicControl, kLinkmic);
  }
  ui_->widget->adjustSize();
  // 再重新绘制阴影效果
  InitShadowBox();
}

void StudentControls::OnAdjustControls() {
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (parent_->isVisible()) {
    QRect display_rect;
    display_rect = parent_->geometry();
    display_rect.moveTopLeft(parent_->mapToGlobal(display_rect.topLeft()));

    this->move(display_rect.x() + display_rect.width() - this->width(),
               display_rect.y() + display_rect.height() - this->height());

    if (this->isHidden()) {
      this->show();
      this->raise();
    }
  }
}

void StudentControls::OnRtcMuteToClient(bool mute) {
  if (mute) {
    ui_->MicControl->setDisabled(true);
    mic_status_ = false;
    vm_->MuteLocalAudio(!mic_status_);
    SignalMuteLocalAudio(!mic_status_);
    ui_->MicControl->SetIcon(kBottomMicMute , 28);
    UpdateRedLine(ui_->MicControl, !mic_status_);
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(QTranslate("InteractiveClass.AdminRtcMuteOn"));
  } else {
    ui_->MicControl->setDisabled(false);
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(QTranslate("InteractiveClass.AdminRtcMuteOff"));
  }

}

void StudentControls::on_LinkmicControl_clicked() {
  if (link_mic_status_ == false) {
    //二次确认
    ConfirmDialog dialog(GetTopParentWidget(this));
    DialogParam param;
    param.title = QTranslate("InteractiveClass.ReqLinkmicConfirm");
    param.content = QTranslate("InteractiveClass.ReqLinkmicNotify");
    param.type = DialogTypeOkCancel;
    param.icon_type = DialogIconTypeInfo;
    param.btn_vec = {QTranslate("OK"), QTranslate("Cancel")};
    dialog.SetDialogParam(param);
    if (dialog.exec() != QDialog::Accepted) {
      return;
    }
    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "on_LinkmicControl_clicked ReqLinkMic");
    vm_->ReqLinkMic(
        []() {
          QString notify = QTranslate("InteractiveClass.ReqLinkmic.Success");
          GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
        },
        []() {
          QString notify = QTranslate("InteractiveClass.ReqLinkmic.Fail");
          GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
        });
  } else {
    //二次确认
    ConfirmDialog dialog(GetTopParentWidget(this));
    DialogParam param;
    param.title = QTranslate("InteractiveClass.CancelLinkmicConfirm");
    param.content = QTranslate("InteractiveClass.CancelLinkmicNotify");
    param.type = DialogTypeOkCancel;
    param.icon_type = DialogIconTypeFailed;
    param.btn_vec = {QTranslate("OK"), QTranslate("Cancel")};
    dialog.SetDialogParam(param);
    if (dialog.exec() != QDialog::Accepted) {
      return;
    }

    LogWithTag(ClassroomTagLinkMic, LOG_INFO, "on_LinkmicControl_clicked ReqQuitLinkMic");
    vm_->ReqQuitLinkMic([]() {

    }, []() {

    });
  }
}

void StudentControls::on_MicControl_clicked() {
  mic_status_ = !mic_status_;
  vm_->MuteLocalAudio(!mic_status_);
  SignalMuteLocalAudio(!mic_status_);
  ui_->MicControl->SetIcon(mic_status_ ? kBottomMicUnmute : kBottomMicMute, 28);
  UpdateRedLine(ui_->MicControl, !mic_status_);
  QString notify = mic_status_ ? QTranslate("InteractiveClass.MicOn")
                            : QTranslate("InteractiveClass.MicOff");
  if (mic_status_) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  } else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }
}

void StudentControls::on_VideoControl_clicked() {
  video_status_ = !video_status_;
  SignalMuteLocalVideo(!video_status_);
  ui_->VideoControl->SetIcon(
      video_status_ ? kBottomCameraUnmute : kBottomCameraMute, 28);
  UpdateRedLine(ui_->VideoControl, !video_status_);
  QString notify = video_status_ ? QTranslate("InteractiveClass.CameraOn")
                            : QTranslate("InteractiveClass.CameraOff");
  if (video_status_) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  } else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }
}
void StudentControls::OnNotifyChangeCameraMuteButtonStatus(bool mute)
{
  bool old_mute = !video_status_;
  if (old_mute == mute) return;

  video_status_ = !mute;

  ui_->VideoControl->SetIcon(
    video_status_ ? kBottomCameraUnmute : kBottomCameraMute, 28);
  UpdateRedLine(ui_->VideoControl, !video_status_);
  QString notify = video_status_ ? QTranslate("InteractiveClass.CameraOn")
    : QTranslate("InteractiveClass.CameraOff");
  if (video_status_) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowSuccessToast(notify);
  }
  else {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(notify);
  }

}