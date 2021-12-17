#include "thumbnail_widget.h"
#include "ui_thumbnail_widget.h"
#include "common/icon_font_helper.h"
#include "doc/get_doc_rsp.h"
#include <QWidget>

ThumbnailWidget::ThumbnailWidget(QListWidgetItem* item, QWidget* parent, ThumbnailType type)
    : ui(new Ui::ThumbnailWidget), QWidget(parent), item_(item), thumbnail_type_(type){
  ui->setupUi(this);
  ui->addBtn->hide();
  ui->delBtn->hide();
  FoldList();
  connect(ui->listWidget, &QListWidget::itemClicked, this, &ThumbnailWidget::OnListItemClicked);
  ui->widget->installEventFilter(this);
}

ThumbnailWidget::~ThumbnailWidget() {
  delete ui;
}

void ThumbnailWidget::on_pushBtn_clicked() {
  if (b_fold_) {
    UnfoldList();
  } else {
    FoldList();
  }
}

void ThumbnailWidget::on_addBtn_clicked() {
  AddThumbnailWidgetItem();
}

void ThumbnailWidget::on_delBtn_clicked() {
  if (item_list_.empty()) return;
  auto itor = item_list_.back();
  if (itor != nullptr) {
    item_list_.pop_back();
    DelThumbnailWidgetItem(itor);
  }
}

// 折叠列表
void ThumbnailWidget::FoldList() {
  if (!b_fold_) {
    IconFontHelper::Instance()->SetIcon(ui->pushBtn, kThumbnailWidgetUnfold);
    b_fold_ = true;
    UpdateSize();
  }
}

// 展开列表
void ThumbnailWidget::UnfoldList() {
  if (b_fold_) {
    IconFontHelper::Instance()->SetIcon(ui->pushBtn, kThumbnailWidgetFold);
    b_fold_ = false;
    SignalListUnfold(this);
    UpdateSize();
  }
}

void ThumbnailWidget::SetTitle(const QString& title) {
  ui->label->setText(title);
}

void ThumbnailWidget::SetItemImage(const uint32_t& page, const QByteArray& data) {
  if (page > item_list_.size() || page == 0) return;
  
  item_list_[page - 1]->SetImageData(data);
}

QListWidgetItem* ThumbnailWidget::GetWidgetItem() {
  return item_;
}

ThumbnailWidgetItem* ThumbnailWidget::AddThumbnailWidgetItem(const std::string& duplicate_key) {

  // 去重
  if (!duplicate_key.empty()) {
    for (const DuplicateInfo& info : dup_list_) {
      if (info.dup_key == duplicate_key) {
        return nullptr;
      }
    }
  }

  bool is_wb = thumbnail_type_ == ThumbnailTypeWhiteboard ? true : false;
  QListWidgetItem* item = new QListWidgetItem();
  ThumbnailWidgetItem* widget = new ThumbnailWidgetItem(item, this, is_wb);

  dup_list_.push_back(DuplicateInfo{widget, duplicate_key});


  item_list_.push_back(widget);
  ui->listWidget->addItem(item);
  ui->listWidget->setItemWidget(item, widget);
  if (ui->listWidget->currentItem() == nullptr) {
    ui->listWidget->setCurrentItem(item);
  }
  widget->SetIndex(item_list_.size());
  widget->SetWbPage(item_list_.size()-1);
  UpdateSize();
  return widget;
}

void ThumbnailWidget::DelThumbnailWidgetItem(ThumbnailWidgetItem* widget) {
  auto item = widget->GetWidgetItem();
  ui->listWidget->removeItemWidget(item);

  for (auto it = dup_list_.begin(); it != dup_list_.end(); ) {
    if (it->widget == widget) {
      it = dup_list_.erase(it);
      break;
    }
    else {
      ++it;
    }
  }

  delete item;
  UpdateSize();
}

void ThumbnailWidget::SetCurIndex(int32_t index) {
  if (index >= item_list_.size()) return;
  auto item = item_list_[index];
  ui->listWidget->setCurrentItem(item->GetWidgetItem());
  ui->listWidget->scrollToItem(item->GetWidgetItem(), QAbstractItemView::PositionAtCenter);
}

bool ThumbnailWidget::eventFilter(QObject *obj, QEvent *event) {
  if (obj == ui->widget) {
    if (event->type() == QEvent::MouseButtonRelease) {
      on_pushBtn_clicked();
      return true;
    }
  }
  return false;
}

void ThumbnailWidget::UpdateSize() {
  if (b_fold_) {
    ui->listWidget->hide();
    QSize size = ui->widget->size();
    if (item_) {
      item_->setSizeHint(size);
    }
  } else {
    QSize size = ui->widget->size();
    int item_num = item_list_.size();
    if (item_) {
      if (item_num == 0) {
        item_->setSizeHint(QSize(size.width(), size.height()));
        ui->listWidget->hide();
      } else {
        ui->listWidget->show();
        item_->setSizeHint(QSize(size.width(), size.height() + item_num * (122 + 12) + 4));
      }
      
    }
  }
}

void ThumbnailWidget::OnListItemClicked() {
  QList<QListWidgetItem*> selected_items = ui->listWidget->selectedItems();
  if (selected_items.size() > 0) {
    QListWidgetItem* item = selected_items[0];
    QWidget* widget = ui->listWidget->itemWidget(item);
    ThumbnailWidgetItem* item_widget = dynamic_cast<ThumbnailWidgetItem*>(widget);
    if (item_widget) {
      item_widget->SetSelected(true);
      emit SignalListItemClicked(this, item_widget->GetIndex(), item_widget->GetWbPage());
    }
    for (auto item : item_list_) {
      if (item != item_widget) {
        item->SetSelected(false);
      }
    }
  }
}

void ThumbnailWidget::OnWhiteBoardChanged(const uint32_t& cur_index, const uint32_t& total_index) {
  int add_number = total_index - item_list_.size();
  for (int i = 0; i < add_number; i++) {
    AddThumbnailWidgetItem();
  }
  SetCurIndex(cur_index);
}

void ThumbnailWidget::OnUploadDocSuccess(const alibaba::doc::GetDocRsp& doc_info) {
  return;
}
