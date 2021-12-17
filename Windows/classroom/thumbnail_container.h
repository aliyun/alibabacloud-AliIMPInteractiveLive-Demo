#pragma once

#include <stdint.h>
#include <QWidget>
#include <memory>
#include <unordered_map>
#include "thumbnail_widget.h"

namespace Ui {
class ThumbnailContainer;
}

namespace thumbnail_loader {
class ThumbnailLoader;
}

class ThumbnailContainer : public QWidget {
  Q_OBJECT

 public:
  explicit ThumbnailContainer(QWidget* parent = nullptr);
  ~ThumbnailContainer();

  ThumbnailWidget* AddThumbnailWidget(std::string group,
                                      ThumbnailWidget::ThumbnailType type);
  void DelThumbnailWidget(ThumbnailWidget* widget);

  void AddResource(const std::string& group, const uint32_t group_page, const std::string& url);
signals:
  void NotifySelectPage(const std::string& group, const uint32_t group_page, const uint32_t wb_page);
  void NotifySizeChange();
 public slots:
  void OnWhiteBoardAdded();

 private slots:
  // for test
  void on_pushBtn_clicked();
  void on_addBtn_clicked();
  void on_delBtn_clicked();

  void OnWidgetListUnfold(ThumbnailWidget* widget);
  void OnResourceLoaded(const bool loaded, const std::string& group, const uint32_t group_page, QByteArray data);
  void OnListItemClicked(ThumbnailWidget* self, const uint32_t& index, const uint32_t& wb_page);

 private:
  void SetScrollBarStyle();

 public:
  Ui::ThumbnailContainer* ui;

 private:
  std::vector<std::string> group_list_;
  std::unordered_map<std::string, ThumbnailWidget*> group_thumbnail_map_;
  thumbnail_loader::ThumbnailLoader* thumbnail_loader_;
};
