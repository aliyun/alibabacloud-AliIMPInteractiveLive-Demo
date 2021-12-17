#ifndef SHARE_SCREEN_SELECT_DIALOG_H
#define SHARE_SCREEN_SELECT_DIALOG_H

#include <QDialog>
#include "ali_rtc_screen_share_type.h"
#include "i_room.h"
#include "i_rtc.h"
#include "meta_space.h"
#include "rtc_screen_source.h"
#include "share_screen_item_widget.h"
#include <QListWidget>

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;

namespace Ui {
class ShareScreenSelectDlg;
}

class ShareScreenSelectDlg : public QDialog {
  Q_OBJECT

public:
  explicit ShareScreenSelectDlg(const std::string& class_room_id, QWidget* parent = nullptr);
 ~ShareScreenSelectDlg();

  ShareWindowsNode getSelectWindowInfo();
 private:
  void InitWindowPos();
  void Init();

  void AddWindowItem(const ShareWindowsNode& window_info);
  
private slots:
  void on_okBtn_clicked();
  void on_cancelBtn_clicked();
  void on_list_item_doubleclicked(QListWidgetItem* listWidgetItem);
  void on_list_item_select_changed();

 private:
  Ui::ShareScreenSelectDlg* ui_;
  std::string class_room_id_;
  ShareWindowsNode select_window_info_;
  ShareScreenItemWidget* last_hover_item = nullptr;
  ShareScreenItemWidget* last_select_item = nullptr;
};

#endif // CONFIRM_DIALOG_H
