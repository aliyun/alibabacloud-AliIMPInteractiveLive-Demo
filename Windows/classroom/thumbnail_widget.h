#pragma once

#include <QWidget>
#include <QListWidgetItem>
#include <vector>
#include <stdint.h>
#include <memory>
#include <set>
#include "thumbnail_widget_item.h"

namespace Ui {
class ThumbnailWidget;
}
namespace alibaba {
namespace doc {
struct GetDocRsp;
} // namespace doc
} // namespace alibaba
class ThumbnailWidget : public QWidget {
  Q_OBJECT

 public:
  enum ThumbnailType {
    ThumbnailTypeWhiteboard = 0,
    ThumbnailTypePPT = 1,
  };
  explicit ThumbnailWidget(QListWidgetItem* item = nullptr, QWidget* parent = nullptr, ThumbnailType = ThumbnailTypeWhiteboard);
  ~ThumbnailWidget();

  void FoldList();
  void UnfoldList();
  void SetTitle(const QString& title);
  void SetItemImage(const uint32_t& page, const QByteArray& data);
  QListWidgetItem* GetWidgetItem();

  ThumbnailWidgetItem* AddThumbnailWidgetItem(const std::string& duplicate_key = "");
  void DelThumbnailWidgetItem(ThumbnailWidgetItem* widget);
  void SetCurIndex(int32_t index);
signals:
  void SignalListUnfold(ThumbnailWidget* self);
  void SignalListItemClicked(ThumbnailWidget* self, const uint32_t& index, const uint32_t& wb_page);

public slots:
  void OnWhiteBoardChanged(const uint32_t& cur_index, const uint32_t& total_index);
  void OnUploadDocSuccess(const alibaba::doc::GetDocRsp& doc_info);
private slots:
  void OnListItemClicked();
  void on_pushBtn_clicked();
   // for test
  void on_addBtn_clicked();
  void on_delBtn_clicked();
private:
  void UpdateSize();

  virtual bool eventFilter(QObject *obj, QEvent *event) override;

public:
  Ui::ThumbnailWidget* ui;

private:
  QListWidgetItem* item_;
  std::vector<ThumbnailWidgetItem*> item_list_;
  bool b_fold_ = false;
  ThumbnailType thumbnail_type_;

  struct DuplicateInfo
  {
    ThumbnailWidgetItem* widget = nullptr;
    std::string dup_key;
  };
  std::vector<DuplicateInfo> dup_list_;
};
