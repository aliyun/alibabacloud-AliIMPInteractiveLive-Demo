#include "share_screen_hover_bar_dlg.h"
#include <QApplication>
#include <QDesktopWidget>
#include <QPainter>
#include <QPixmap>
#include <QFontDatabase>
#include <qscreen.h>
#include <qwindow.h>
#include <qtwinextras/qwinfunctions.h> 
#include "ali_rtc_screen_share_type.h"
#include "i_room.h"
#include "i_rtc.h"
#include "meta_space.h"
#include "rtc_screen_source.h"
#include "ui_share_screen_hover_bar.h"
#include "common/icon_font_helper.h"
#include "event/event_manager.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;



ShareScreenHoverBar::ShareScreenHoverBar(const std::string& class_room_id,
                                           QWidget* parent)
    : QDialog(parent, Qt::FramelessWindowHint | Qt::Tool),
      ui_(new Ui::ShareScreenHoverBar) {
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground);

  InitDeviceListConnect(parent);
  InitIconFont();
  //  SetAlwaysOnTop(this, true);
  connect(EventManager::Instance().get(), &EventManager::SignalLiveTime, this,
          &ShareScreenHoverBar::OnSignalLiveTime);
}


ShareScreenHoverBar::~ShareScreenHoverBar() { delete ui_; }

void ShareScreenHoverBar::ShowTopCenter() {
  show();
#ifdef WIN32
  QDesktopWidget* desktop = QApplication::desktop();
  QRect screen = desktop->screenGeometry();
  int screenWidth = screen.width();
  QRect widgetRect = geometry();
  move((screenWidth - widgetRect.width()) / 2, 0);
#endif
}

void ShareScreenHoverBar::HideTopCenter() {
  ;
}

void ShareScreenHoverBar::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui_->micMore, kIconVNextOld);
  IconFontHelper::Instance()->SetIcon(ui_->micBtn, kHoverBarMicUnmute);
  IconFontHelper::Instance()->SetIcon(ui_->desktopAudioBtn, kHoverBarDeskAudioUnmute);
  IconFontHelper::Instance()->SetIcon(ui_->recordBtn, kBottomRecord);
}

void ShareScreenHoverBar::InitDeviceListConnect(QWidget* parent) {
  device_list_audio_ = new DeviceList(parent);
  connect(device_list_audio_, &DeviceList::SignalHide, this,
          &ShareScreenHoverBar::OnNotifyDeviceListHide);

  device_list_audio_->Init(DeviceTypeEnum_Audio);
  device_list_audio_->UpdateClassroomId(class_room_id_);
}

void ShareScreenHoverBar::OnNotifyDeviceListHide(DeviceTypeEnum type) {
  IconFontHelper::Instance()->SetIcon(ui_->micMore, kIconVNextOld);
}

void ShareScreenHoverBar::OnSignalLiveTime(QString time) {
  ui_->timeLable->setText(time);
}

void ShareScreenHoverBar::setMicBtnChecked(bool mute) {
  if (mute) {
    IconFontHelper::Instance()->SetIcon(ui_->micBtn, kHoverBarMicMute);
  } else {
    IconFontHelper::Instance()->SetIcon(ui_->micBtn, kHoverBarMicUnmute);  
  }

  ui_->micLabel->setText(mute ? QTranslate("InteractiveClass.CloseMute")
                                : QTranslate("InteractiveClass.Mute"));
}

void ShareScreenHoverBar::setRecordBtnChecked(bool record) {
  ui_->recordBtn->setChecked(record);

  ui_->recordLable->setText(record ? QTranslate("InteractiveClass.StopRecord")
                              : QTranslate("InteractiveClass.Record"));
}

void ShareScreenHoverBar::on_micMore_clicked() {
  QPoint pt = QCursor::pos();
  device_list_audio_->ShowDeviceList(pt, false);
  if (ui_->micMore->text() == QString(kIconVPre)) {
    IconFontHelper::Instance()->SetIcon(ui_->micMore, kIconVNextOld);
    device_list_audio_->HideDeviceList();
  } else {
    IconFontHelper::Instance()->SetIcon(ui_->micMore, kIconVPre);
    QRect rc = ui_->micMore->geometry();
    QPoint pt(rc.left(), rc.top());
    QPoint pt_mapped = ui_->micMore->parentWidget()->mapToGlobal(pt);
    pt_mapped.setY(pt_mapped.y() - 12);
    device_list_audio_->Init(DeviceTypeEnum_Audio);
    device_list_audio_->ShowDeviceList(pt_mapped, false);
  }
}

void ShareScreenHoverBar::on_micBtn_clicked() {
  emit SignalMicBtnClicked();
}

void ShareScreenHoverBar::setDesktopAudioBtnChecked(bool open) {
  if (open) {
    ui_->desktopAudioBtn->setChecked(true);
    IconFontHelper::Instance()->SetIcon(ui_->desktopAudioBtn, kHoverBarDeskAudioUnmute);
  } else {
    ui_->desktopAudioBtn->setChecked(false);
    IconFontHelper::Instance()->SetIcon(ui_->desktopAudioBtn, kHoverBarDeskAudioMute);
  }
}

void ShareScreenHoverBar::on_desktopAudioBtn_clicked() {
  bool checked = ui_->desktopAudioBtn->isChecked();
  emit EventManager::Instance()->SignalTeacherShareSystemAudio(checked);
  if (checked) {
    IconFontHelper::Instance()->SetIcon(ui_->desktopAudioBtn, kHoverBarDeskAudioUnmute);
  }
  else {
    IconFontHelper::Instance()->SetIcon(ui_->desktopAudioBtn, kHoverBarDeskAudioMute);
  }
}

void ShareScreenHoverBar::on_stopShare_clicked() {
  emit SignalStopShare();
}

void ShareScreenHoverBar::on_recordBtn_clicked() {
  emit SignalRecordBtnClicked();
}

void ShareScreenHoverBar::mousePressEvent(QMouseEvent* event) {
  press_point_ = event->globalPos();
  start_pos_ = pos();

  QWidget* top_parent = this;
  if (top_parent) {
    parent_start_pos_ = top_parent->pos();
  }

  mouse_btn_pressed = true;

  QDialog::mousePressEvent(event);
}

void ShareScreenHoverBar::mouseMoveEvent(QMouseEvent* event) {
  if (mouse_btn_pressed) {
    auto move_pos = event->globalPos();
    auto offset_pos = move_pos - press_point_;
    if (offset_pos == QPoint(0, 0)) {
      return QDialog::mouseMoveEvent(event);
    }

    move(start_pos_ + offset_pos);
  }

  QDialog::mouseMoveEvent(event);
}


void ShareScreenHoverBar::mouseReleaseEvent(QMouseEvent* event) {
  mouse_btn_pressed = false;

  QDialog::mouseReleaseEvent(event);
}

