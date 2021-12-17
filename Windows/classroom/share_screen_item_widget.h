#ifndef SHARE_SCREEN_ITEM_WIDGET_H
#define SHARE_SCREEN_ITEM_WIDGET_H

#include <QWidget>

struct ShareWindowsNode {
  bool is_screen;
  QString title;
  WId wid;
  bool open_system_audio;

  ShareWindowsNode() {
    open_system_audio = false;
  }
};

struct DisplayNode {
  int index;
  QString device_name;
  QString device_key;
};

namespace Ui {
  class ShareScreenItemWidget;
}

enum ShareScreenItemWidget_Status {
  ShareScreenItemWidget_Normal,
  ShareScreenItemWidget_Hover,
  ShareScreenItemWidget_Selected
};

class ShareScreenItemWidget : public QWidget {
  Q_OBJECT

public:
  explicit ShareScreenItemWidget(QWidget* parent = nullptr);
 ~ShareScreenItemWidget();
  void setInfo(const ShareWindowsNode& window_info);
 ShareWindowsNode getInfo();
  void setStatus(ShareScreenItemWidget_Status status);

 protected:
  virtual void enterEvent(QEvent* event) override;
  virtual void leaveEvent(QEvent* event) override;

private:
  QPixmap ScaleWindowThumbnail(const QPixmap& window_thumb,
                               QColor backgroud,
                               QSize size);
  QPixmap ScaleWindowThumb(const QPixmap& window_thumb,
                           QColor backgroud,
                           QSize size,
                           int padding,
                           QColor border);

  QString GetSubText(const QString& str, const QFont& font, int max_width);

  QPixmap GrabWindowEx(WId window);

  
  
private:
  Ui::ShareScreenItemWidget *ui;
 ShareWindowsNode window_info_;

 ShareScreenItemWidget_Status last_status_;
};

#endif // CONFIRM_DIALOG_H
