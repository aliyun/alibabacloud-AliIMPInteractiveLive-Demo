#pragma once
#include <QDialog>

namespace Ui {
  class ConfShareDialog;
}

class ConfShareDialog : public QDialog {
  Q_OBJECT

public:
  explicit ConfShareDialog(QWidget* parent = 0);
  ~ConfShareDialog();
  void ShowAtTopLeft(int px, int py);
  void UpdateTitle(const std::string& title);
  void UpdateClassId(const std::string& class_id);
private slots:
  void on_copyBtn_clicked();
private:
  virtual bool eventFilter(QObject* target, QEvent* event) override;
private:
  Ui::ConfShareDialog* ui_ = nullptr;
};
