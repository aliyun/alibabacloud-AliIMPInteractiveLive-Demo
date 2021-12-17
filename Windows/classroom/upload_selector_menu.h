#ifndef UPLOAD_SELECTOR_MENU_H
#define UPLOAD_SELECTOR_MENU_H

#include <QDialog>

namespace Ui {
  class UploadSelectorMenu;
}

class UploadSelectorMenu : public QDialog
{
  Q_OBJECT

public:
  explicit UploadSelectorMenu(QWidget *parent = nullptr);
  ~UploadSelectorMenu();
  virtual bool eventFilter(QObject* target, QEvent* event) override;
signals:
  void NotifyUploadPptClicked();
private:
  Ui::UploadSelectorMenu *ui;
};

class QUploadItemEventFilter : public QObject {
  Q_OBJECT

public:
  QUploadItemEventFilter(QObject* parent);
signals:
  void NotifyClicked(QWidget* widget);

protected:
  bool eventFilter(QObject *obj, QEvent *event) override;
};

#endif // UPLOAD_SELECTOR_MENU_H
