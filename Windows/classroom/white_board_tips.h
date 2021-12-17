#pragma once
#include "QDialog"


namespace Ui {
  class WhiteBoardTips;
}

class WhiteBoardTips : public QDialog {
  Q_OBJECT
public:
  WhiteBoardTips(QWidget *parent);
  void SetContent(const QString& tips);
  void MoveTo(const QPoint& pt);
public slots:
  void OnShowTips(const QPoint& pt, const QString& context);
  void OnHideTips();
private:
  virtual bool eventFilter(QObject* target, QEvent* event) override;

private:
  int show_count_ = 0;
  int32_t wait_seconds_ = 5;
  std::unique_ptr<Ui::WhiteBoardTips> ui;
  static const int MAX_WAIT_SECONDS = 5;

};
