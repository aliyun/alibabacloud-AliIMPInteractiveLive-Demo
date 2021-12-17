#include "thumbnail_container.h"
#include "ui_thumbnail_container.h"
#include "thumbnail_widget.h"
#include "common/icon_font_helper.h"
#include "thumbnail_loader.h"
#include <QWidget>

ThumbnailContainer::ThumbnailContainer(QWidget* parent)
    : ui(new Ui::ThumbnailContainer), QWidget(parent), thumbnail_loader_(new thumbnail_loader::ThumbnailLoader){
  ui->setupUi(this);
  ui->addBtn->hide();
  ui->delBtn->hide();
  SetScrollBarStyle();
  connect(thumbnail_loader_, &thumbnail_loader::ThumbnailLoader::SignalResourceLoaded, this, &ThumbnailContainer::OnResourceLoaded);
}

ThumbnailContainer::~ThumbnailContainer() {
  delete ui;
  delete thumbnail_loader_;
}

void ThumbnailContainer::on_pushBtn_clicked() {
  const int32_t kExpendingWidth = 227;
  const int32_t kUnExpendingWidth = 12;
  if (ui->listWidget->isVisible()) {
    ui->listWidget->setVisible(false);
    ui->pushBtn->setStyleSheet(QString::fromUtf8("QPushButton#pushBtn{\n""border-image:url(:/res/images/unfold.png);\n""}"));
    resize(kUnExpendingWidth, height());
  } else {
    ui->listWidget->setVisible(true);
    ui->pushBtn->setStyleSheet(QString::fromUtf8("QPushButton#pushBtn{\n""border-image:url(:/res/images/fold.png);\n""}"));
    resize(kExpendingWidth, height());
  }
  emit NotifySizeChange();
}

ThumbnailWidget* ThumbnailContainer::AddThumbnailWidget(std::string group, ThumbnailWidget::ThumbnailType type) {
  auto itor = group_thumbnail_map_.find(group);
  if (itor != group_thumbnail_map_.end()) {
    return itor->second;
  }
  QListWidgetItem* item = new QListWidgetItem();
  ThumbnailWidget* widget = new ThumbnailWidget(item, this, type);
  group_thumbnail_map_[group] = widget;
  group_list_.push_back(group);
  connect(widget, &ThumbnailWidget::SignalListUnfold, this, &ThumbnailContainer::OnWidgetListUnfold);
  connect(widget, &ThumbnailWidget::SignalListItemClicked, this, &ThumbnailContainer::OnListItemClicked);
  
  ui->listWidget->addItem(item);
  ui->listWidget->setItemWidget(item, widget);
  return widget;
}

void ThumbnailContainer::DelThumbnailWidget(ThumbnailWidget* widget) {
  auto item = widget->GetWidgetItem();
  ui->listWidget->removeItemWidget(item);
  delete item;
}

void ThumbnailContainer::AddResource(const std::string& group, const uint32_t group_page, const std::string& url) {
  thumbnail_loader_->AddResource(group, group_page, url);
}

void ThumbnailContainer::on_addBtn_clicked() { 
  static int j = 111;
  ThumbnailWidget* widget = AddThumbnailWidget(std::to_string(j++) ,ThumbnailWidget::ThumbnailTypePPT);
  int i = group_thumbnail_map_.size();
  if (i == 1)
    widget->SetTitle(QString::fromStdWString(L"°×°å"));
  else 
    widget->SetTitle(QString::fromStdWString(L"¿Î¼þ") + QString::fromStdWString(std::to_wstring(i - 1)));
  i++;
}

void ThumbnailContainer::on_delBtn_clicked() {
  if (group_list_.empty()) return;
  auto itor = group_list_.back();
  auto widget = group_thumbnail_map_.find(itor);
  if (widget != group_thumbnail_map_.end()) {
    group_list_.pop_back();
    DelThumbnailWidget(widget->second);
    group_thumbnail_map_.erase(widget);
  }
}

void ThumbnailContainer::OnWidgetListUnfold(ThumbnailWidget* widget) {
  for (auto itor : group_thumbnail_map_) {
    if (itor.second != widget) {
      itor.second->FoldList();
    }
  }
}

void ThumbnailContainer::OnResourceLoaded(const bool loaded, const std::string& group, const uint32_t group_page, QByteArray data){
  if (loaded) {
    auto itor = group_thumbnail_map_.find(group);
    if (itor != group_thumbnail_map_.end()) {
      itor->second->SetItemImage(group_page, data);
    }
  }
}

void ThumbnailContainer::OnListItemClicked(ThumbnailWidget* self, const uint32_t& index, const uint32_t& wb_page) {
  std::string group = "";
  for (auto itor : group_thumbnail_map_) {
    if (itor.second == self) {
      group = itor.first;
      break;
    }
  }
  Q_EMIT NotifySelectPage(group, index, wb_page);
}

void ThumbnailContainer::OnWhiteBoardAdded() {

}

void ThumbnailContainer::SetScrollBarStyle() {
  ui->listWidget->verticalScrollBar()->setStyleSheet(QString::fromUtf8("QScrollBar:vertical{\n"
  "width:8px;\n"
  "background:rgba(0,0,0,0%);\n"
  "border-radius:4px;\n"
  "}\n"
  "QScrollBar::handle:vertical{\n"
  "width:8px;\n"
  "background:rgb(232,232,232);\n"
  "border-radius:4px;\n"
  "min-height:20;\n"
  "}\n"
  "QScrollBar::add-line:vertical{\n"
  "border:none;\n"
  "background:none;\n"
  "}\n"
  "QScrollBar::sub-line:vertical{\n"
  "border:none;\n"
  "background:none;\n"
  "}"));
}