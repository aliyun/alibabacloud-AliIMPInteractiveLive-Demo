#pragma once

#include <QWidget>
#include <QListWidgetItem>
#include <vector>
#include <stdint.h>
#include <memory>

namespace Ui {
class ThumbnailWidgetItem;
}

class ThumbnailWidgetItem : public QWidget {
  Q_OBJECT

 public:
  explicit ThumbnailWidgetItem(QListWidgetItem* item = nullptr, QWidget* parent = nullptr, const bool& is_wb = true);
  ~ThumbnailWidgetItem();

  QListWidgetItem* GetWidgetItem();
  void SetIndex(const int& index);
  int GetIndex();
  void SetWbPage(const uint32_t& wb_page);
  uint32_t GetWbPage();
  void SetSelected(const bool& selected);
  void SetImageData(const QByteArray& data);
 signals:

 private slots:

 private:
  void SetNumberStyle(const bool& selected);
public:
  Ui::ThumbnailWidgetItem* ui;

private:
  QListWidgetItem* item_;
  bool is_wb_;
  int index_ = 0;
  uint32_t wb_page_ = 0;
};
