#ifndef SIZE_SLIDE_BAR_H
#define SIZE_SLIDE_BAR_H

#include <QWidget>

namespace Ui {
  class SizeSlideBar;
}

class SizeSlideBar : public QWidget
{
  Q_OBJECT

public:
  explicit SizeSlideBar(QWidget *parent = nullptr);
  ~SizeSlideBar();
  void SetMaxValue(int32_t value);
  void SetDefaultValue(int32_t value);
  void SetMinValue(int32_t value);
  int32_t GetValue();
signals:
  void NotifyValueChanged(int32_t value);
private:
  Ui::SizeSlideBar *ui;
};

#endif // SIZE_SLIDE_BAR_H
