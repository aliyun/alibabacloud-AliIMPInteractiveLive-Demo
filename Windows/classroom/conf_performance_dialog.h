#pragma once
#include <QDialog>
namespace Ui {
  class ConfPerformanceDialog;
}

class ConfPerformanceDialog : public QDialog {
  Q_OBJECT

public:
  explicit ConfPerformanceDialog(QWidget* parent = 0);
  ~ConfPerformanceDialog();
  void ShowAtTopLeft(int px, int py);
  void UpdateCpuInfo(const QString& str);
  void UpdateBitrate(const QString& str);
  void UpdateLantency(const QString& str);
  void UpdateMemory(const QString& str);
private:
  virtual bool eventFilter(QObject* target, QEvent* event) override;
private:
  Ui::ConfPerformanceDialog* ui_ = nullptr;
};
