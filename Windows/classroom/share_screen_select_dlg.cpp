#include "share_screen_select_dlg.h"
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
#include "ui_share_screen_select_dlg.h"
#include "common/icon_font_helper.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;



ShareScreenSelectDlg::ShareScreenSelectDlg(const std::string& class_room_id,
                                           QWidget* parent)
    : QDialog(parent, Qt::FramelessWindowHint),
      ui_(new Ui::ShareScreenSelectDlg) {
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground);
  QGraphicsDropShadowEffect* shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  class_room_id_ = class_room_id;
  ui_->window_list->setMovement(QListView::Static);
  ui_->window_list->setUniformItemSizes(true);
  ui_->window_list->setSelectionMode(QAbstractItemView::SingleSelection);
  ui_->window_list->setSpacing(5);
  QObject::connect(ui_->window_list,
                   SIGNAL(itemDoubleClicked(QListWidgetItem*)), this,
                   SLOT(on_list_item_doubleclicked(QListWidgetItem*)));
  QObject::connect(ui_->window_list, SIGNAL(itemSelectionChanged()), this,
                   SLOT(on_list_item_select_changed()));
  InitWindowPos();
  Init();
}


ShareScreenSelectDlg::~ShareScreenSelectDlg() { delete ui_; }

ShareWindowsNode ShareScreenSelectDlg::getSelectWindowInfo() {
  return select_window_info_;
}

void ShareScreenSelectDlg::Init() {
  std::shared_ptr<alibaba::meta_space::IRoom> room_ptr_ =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          class_room_id_);
  ;
  std::shared_ptr<IRtc> rtc_plugin =
      std::dynamic_pointer_cast<IRtc>(room_ptr_->GetPlugin(PluginRtc));
  std::vector<alibaba::meta::RtcScreenSource> desktop_source_list =
      rtc_plugin->GetScreenShareSourceInfo(
          alibaba::meta::AliRtcScreenShareType::ALI_RTC_SCREEN_SHARE_DESKTOP);

  int list_size = desktop_source_list.size();
  for (int i = 0; i < list_size; ++i) {
    ShareWindowsNode node;
    node.is_screen = true;
    QString title = QTranslate("InteractiveClass.MainScreen");
    if (i != 0) {
      title.append(std::to_string(i + 1).c_str());
    }
    node.title = title;
    int tmp_id = std::atoi(desktop_source_list[i].source_id.c_str());
    node.wid = tmp_id;
    AddWindowItem(node);
  }

  int placeholder_count = 0;
  if (list_size % 4 != 0) {
    placeholder_count = 4 - (list_size % 4);
  }
  for (int i = 0; i < placeholder_count; i++) {
    QListWidgetItem* item = new QListWidgetItem("");
    item->setFlags(item->flags() & ~Qt::ItemIsEnabled & ~Qt::ItemIsSelectable);
    item->setBackgroundColor(QColor(255,255,255));
    ui_->window_list->addItem(item);
  }

  std::vector<alibaba::meta::RtcScreenSource> windows_source_list =
      rtc_plugin->GetScreenShareSourceInfo(
          alibaba::meta::AliRtcScreenShareType::ALI_RTC_SCREEN_SHARE_WINDOW);

  list_size = windows_source_list.size();
  for (int i = 0; i < list_size; ++i) {
    ShareWindowsNode node;
    node.is_screen = false;
    node.title = QString::fromStdString(windows_source_list[i].source_name);
    int tmp_id = std::atoi(windows_source_list[i].source_id.c_str());
    node.wid = tmp_id;
    AddWindowItem(node);
  }

  auto first_item = ui_->window_list->item(0);
  if (first_item) {
    ui_->window_list->setItemSelected(first_item, true);
  }
}

void ShareScreenSelectDlg::AddWindowItem(const ShareWindowsNode& window_info) {
  
  QListWidgetItem* item = new QListWidgetItem;
  ShareScreenItemWidget *item_widget = new ShareScreenItemWidget(this);
  item_widget->setInfo(window_info);
  QSize item_size = item_widget->size();
  item->setSizeHint(item_size);
  ui_->window_list->addItem(item);
  ui_->window_list->setItemWidget(item, item_widget);
}

void ShareScreenSelectDlg::InitWindowPos() {
  QWidget* parent = parentWidget();
  if (parent) {
    QSize parent_size = parentWidget()->size();
    if (parent_size.height() > height() && parent_size.width() > width()) {
      return;
    }
  }

  {
    QDesktopWidget* desktop = QApplication::desktop();
    QRect screen = desktop->availableGeometry();
    QSize sz = size();
    if (sz.width() > screen.width()) {
      sz.setWidth(screen.width());
    }
    if (sz.height() > screen.height()) {
      sz.setHeight(screen.height());
    }
    resize(sz);
    move((screen.width() - sz.width()) / 2,
         (screen.height() - sz.height()) / 2);
  }
}

void ShareScreenSelectDlg::on_okBtn_clicked() { 
  select_window_info_.open_system_audio = ui_->openDesktopAudio->isChecked();
  accept(); 
}

void ShareScreenSelectDlg::on_cancelBtn_clicked() { reject(); }

void ShareScreenSelectDlg::on_list_item_doubleclicked(QListWidgetItem* listWidgetItem) {
  ShareScreenItemWidget *widget =
      reinterpret_cast<ShareScreenItemWidget*> (ui_->window_list->itemWidget(listWidgetItem));
  select_window_info_ = widget->getInfo();
  select_window_info_.open_system_audio = ui_->openDesktopAudio->isChecked();
  accept();
}

void ShareScreenSelectDlg::on_list_item_select_changed() {
  QList<QListWidgetItem*> items = ui_->window_list->selectedItems();
  if (items.size() <= 0) {
    return;
  }

  QListWidgetItem* listWidgetItem = items.at(0);
  ShareScreenItemWidget* widget = reinterpret_cast<ShareScreenItemWidget*>(
      ui_->window_list->itemWidget(listWidgetItem));
  select_window_info_ = widget->getInfo();
  if (last_select_item != nullptr) {
    last_select_item->setStatus(ShareScreenItemWidget_Normal);
  }
  widget->setStatus(ShareScreenItemWidget_Selected);
  last_select_item = widget;
}
