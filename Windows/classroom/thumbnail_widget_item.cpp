#include "thumbnail_widget_item.h"
#include "ui_thumbnail_widget_item.h"
#include "common/icon_font_helper.h"
#include "common/http_helper.h"
#include <QWidget>

ThumbnailWidgetItem::ThumbnailWidgetItem(QListWidgetItem* item, QWidget* parent, const bool& is_wb)
    : ui(new Ui::ThumbnailWidgetItem), QWidget(parent), item_(item), is_wb_(is_wb) {
  ui->setupUi(this);
  
  if (is_wb_) {
    IconFontHelper::Instance()->SetIcon(ui->wbLabel, kWbLabel);
    ui->imageLabel->setVisible(false);
  } else {
    ui->wbLabel->setVisible(false);
  }

  QSize size = this->size();
  item_->setSizeHint(size);
}

ThumbnailWidgetItem::~ThumbnailWidgetItem() {
  delete ui;
}

QListWidgetItem* ThumbnailWidgetItem::GetWidgetItem() {
  return item_;
}

void ThumbnailWidgetItem::SetIndex(const int& index) {
  index_ = index;
  ui->numberLabel->setText(QString::fromStdString(std::to_string(index)));
}

int ThumbnailWidgetItem::GetIndex() { return index_; }

void ThumbnailWidgetItem::SetSelected(const bool& selected) {
  SetNumberStyle(selected);
}

void ThumbnailWidgetItem::SetWbPage(const uint32_t& wb_page) {
  wb_page_ = wb_page;
}
uint32_t ThumbnailWidgetItem::GetWbPage() {
  return wb_page_;
}
void ThumbnailWidgetItem::SetImageData(const QByteArray& data) {
  if (is_wb_) return;
  QPixmap pixmap;
  pixmap.loadFromData(data);
  ui->imageLabel->setPixmap(pixmap.scaled(ui->imageLabel->size()));
}

void ThumbnailWidgetItem::SetNumberStyle(const bool& selected) {
  if (selected) {
    ui->numberLabel->setStyleSheet(
        QString::fromUtf8("QLabel#numberLabel{\n"
                          "  opacity: 0.8;\n"
                          "  background: rgba(79, 172, 255, 0.8);\n"
                          "  color: #ffffff;\n"
                          "  font-family: \"PingFangSC-Medium\";\n"
                          "  font-size: 12px;\n"
                          "  font-weight: 400;\n"
                          "  line-height: 20px;\n"
                          "  border-radius: 2px;\n"
                          "}")); 
  } else {
    ui->numberLabel->setStyleSheet(
        QString::fromUtf8("QLabel#numberLabel{\n"
                          "  opacity: 0.8;\n"
                          "  background: rgba(0,0,0,0.8);;\n"
                          "  color: #ffffff;\n"
                          "  font-family: \"PingFangSC-Medium\";\n"
                          "  font-size: 12px;\n"
                          "  font-weight: 400;\n"
                          "  line-height: 20px;\n"
                          "  border-radius: 2px;\n"
                          "}")); 
  }
}