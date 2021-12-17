#include "device_list.h"
#include <QLabel>
#include "QBoxLayout"
#include "common/icon_font_helper.h"
#include "device_list_vm.h"



static const int kDeviceItemHeight = 36;
static const int kDeviceItemWidth = 280;
static const int kDeviceNameWidth = 240;
static const int kDeviceMenuData = 100;

#define kDeviceMenuType     "device_menu_type"
#define kDeviceMenuUserData "device_menu_user_data"

DeviceList::DeviceList(QWidget* parent)
  : device_list_(parent, Qt::FramelessWindowHint) {
  device_list_.setAttribute(Qt::WA_TranslucentBackground);
  QVBoxLayout* vbox = new QVBoxLayout(&device_list_);
  vbox->setSpacing(0);
  vbox->setContentsMargins(12, 12, 12, 12);
  device_list_.setLayout(vbox);
  list_ = new QListWidget(&device_list_);
  vbox->addWidget(list_);
  list_->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  list_->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  list_->setAutoScroll(false);
  list_->setEditTriggers(QAbstractItemView::NoEditTriggers);
  list_->setFocusPolicy(Qt::NoFocus);
  device_list_.setFixedWidth(kDeviceItemWidth +24);
  installEventFilter(this);
  QObject::connect(list_, SIGNAL(itemClicked(QListWidgetItem*)),
    this, SLOT(OnItemClicked(QListWidgetItem*)));
  device_list_.setStyleSheet("background-color:#FFFFFF; border-radius:3px;");
  vm_ = std::make_shared<DeviceListVM>();
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  list_->setGraphicsEffect(shadow);
}

DeviceList::~DeviceList() {

}

void DeviceList::Init(DeviceTypeEnum type) {
  type_ = type;
  InitDeviceList();
  device_list_.installEventFilter(this);
}

void DeviceList::ShowDeviceList(const QPoint& pos, bool is_up) {
  QPoint left_botton;
  if (is_up) {
    left_botton = QPoint(pos.x(), pos.y() - kDeviceItemHeight * list_item_.size());
  }
  else {
    left_botton = QPoint(pos.x(), pos.y());
  }
  device_list_.show();
  device_list_.move(left_botton);
}

void DeviceList::HideDeviceList() {
  device_list_.hide();
  emit SignalHide(type_);
}

QRect DeviceList::Geometry() {
  return device_list_.geometry();
}

void DeviceList::InitDeviceList() {
  list_->clear();
  list_item_.clear();
  check_icons_.clear();

  std::vector<DeviceItem> devices;
  if (type_ == DeviceTypeEnum_Audio) {
    devices = vm_->GetAudioDevice();
  }
  else {
    devices = vm_->GetVideoDevice();
  }
  size_t index = 0;
  for (auto iter = devices.begin(); iter != devices.end(); iter++) {
    const DeviceItem& it = *iter;
    AddDeviceListItem(QString::fromUtf8(it.name.c_str()), index++, it.selected);
  }


  device_list_.setFixedHeight(list_->count() * kDeviceItemHeight + 24);
}

bool DeviceList::IsVisible() {
  return device_list_.isVisible();
}

void DeviceList::UpdateClassroomId(const std::string& id) {
  vm_->UpdateClassroomId(id);
}

void DeviceList::AddDeviceListItem(const QString& text, const int index, bool checked) {
  QWidget* item_widget = new QWidget();
  item_widget->setFixedHeight(kDeviceItemHeight);
  item_widget->setStyleSheet(
    "QWidget {"
    "background-color: #FFFFFF 100%;"
    "}"
    "QWidget:hover {"
    "background-color: rgb(206, 229, 247);"
    "}");

  //text
  QLabel* text_label = new QLabel();
  text_label->setStyleSheet(
    "QLabel {"
    "font-family:\"\345\276\256\350\275\257\351\233\205\351\273\221\";"
    "font-size: 12px;"
    "color: #D2D2D2 100%;"
    "background-color: transparent;"
    "}");
  text_label->setFixedWidth(kDeviceNameWidth);
  QFontMetrics font_metrics(text_label->font());
  QString text_elided = font_metrics.elidedText(text, Qt::ElideRight, kDeviceNameWidth);
  text_label->setText(text_elided);
  //spacer
  QSpacerItem* hspacer = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);
  //icon
  QLabel* icon_label = new QLabel();
  icon_label->setFixedSize(QSize(16, 16));

  IconFontHelper::Instance()->SetIcon(icon_label, kIconCheck, 0);
  icon_label->setStyleSheet(
    "QLabel {"
    "font-family:\"lviconfont\";"
    "font-size: 14px;"
    "color: #666666;"
    "background-color: transparent;"
    "}");

  QHBoxLayout* layout = new QHBoxLayout();
  layout->addWidget(text_label);
  layout->addItem(hspacer);
  layout->addWidget(icon_label);
  layout->setSpacing(2);
  layout->setContentsMargins(8, 0, 8, 0);
  item_widget->setLayout(layout);

  QListWidgetItem * new_item = new QListWidgetItem();
  QMap<QString, QVariant> map;
  map.insert(kDeviceMenuUserData, QVariant(index));
  new_item->setData(kDeviceMenuData, QVariant(map));
  list_->addItem(new_item);
  new_item->setSizeHint(QSize(kDeviceItemWidth, kDeviceItemHeight));
  list_->setItemWidget(new_item, item_widget);

  icon_label->setVisible(checked);
  check_icons_.push_back(icon_label);
  list_item_.push_back(new_item);
}

void DeviceList::OnItemClicked(QListWidgetItem* item) {

  auto it = std::find(list_item_.begin(), list_item_.end(), item);
  if (it != list_item_.end()) {
    size_t index = it - list_item_.begin();
    size_t count = check_icons_.size();


    for (size_t i = 0; i < count; i++) {
      check_icons_[i]->setVisible(i == index);
    }

    std::vector<DeviceItem> devices;
    if (type_ == DeviceTypeEnum_Audio) {
      devices = vm_->GetAudioDevice();
      if (index < devices.size()) {
        vm_->SetCurrentAudioDevice(devices[index].name);
      }
    }
    else {
      devices = vm_->GetVideoDevice();
      if (index < devices.size()) {
        vm_->SetCurrentVideoDevice(devices[index].name);
      }
    }

  }
  HideDeviceList();
}

bool DeviceList::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    HideDeviceList();
  }
  return false;
}

