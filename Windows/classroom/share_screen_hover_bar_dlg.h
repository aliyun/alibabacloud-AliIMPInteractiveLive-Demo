#ifndef SHARE_SCREEN_HOVER_BAR_DIALOG_H
#define SHARE_SCREEN_HOVER_BAR_DIALOG_H

#include <QDialog>
#include <QListWidget>
#include "ali_rtc_screen_share_type.h"
#include "device_list.h"
#include "i_room.h"
#include "i_rtc.h"
#include "meta_space.h"
#include "rtc_screen_source.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

namespace Ui {
class ShareScreenHoverBar;
}

class ShareScreenHoverBar : public QDialog {
  Q_OBJECT

 public:
  explicit ShareScreenHoverBar(const std::string& class_room_id,
                               QWidget* parent = nullptr);
  ~ShareScreenHoverBar();

  void ShowTopCenter();
  void HideTopCenter();

  void setMicBtnChecked(bool mute);
  void setRecordBtnChecked(bool record);
  void setDesktopAudioBtnChecked(bool open);

 private:
  void InitIconFont();
  void InitDeviceListConnect(QWidget* parent);

 private slots:
  void on_micMore_clicked();
  void on_micBtn_clicked();
  void on_desktopAudioBtn_clicked();
  void on_stopShare_clicked();
  void on_recordBtn_clicked();
  void OnNotifyDeviceListHide(DeviceTypeEnum type);
  void OnSignalLiveTime(QString time);
 signals:
  void SignalMicBtnClicked();
  void SignalStopShare();
  void SignalRecordBtnClicked();


      protected : void
                  mousePressEvent(QMouseEvent* event) override;
  void mouseReleaseEvent(QMouseEvent* event) override;
  void mouseMoveEvent(QMouseEvent* event) override;

 private:
  Ui::ShareScreenHoverBar* ui_;
  std::string class_room_id_;

  DeviceList* device_list_audio_ = nullptr;

  bool mouse_btn_pressed = false;
  QPoint press_point_;
  QPoint start_pos_;
  QPoint parent_start_pos_;
};

#endif // CONFIRM_DIALOG_H
