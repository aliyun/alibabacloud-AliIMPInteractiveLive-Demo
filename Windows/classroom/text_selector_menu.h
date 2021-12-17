#ifndef TEXT_SELECTOR_MENU_H
#define TEXT_SELECTOR_MENU_H

#include <QDialog>

namespace Ui {
  class TextSelectorMenu;
}

enum TextSelectorType {
  TextSelectorBlod = 1,
  TextSelectorItalic = 1 << 1,
  TextSelectorUnderLine = 1 << 2,
};

class TextSelectorMenu : public QDialog
{
  Q_OBJECT

public:
  explicit TextSelectorMenu(QWidget *parent = nullptr);
  ~TextSelectorMenu();
  virtual bool eventFilter(QObject* target, QEvent* event) override;
  int32_t GetTextType();
  void SetCurrentType();
  int32_t GetFontSize();
signals:
  void NotifyFontSizeChange(int32_t);
  void NotifyFontBold(bool);
  void NotifyFontItalic(bool);
  void NotifyFontUnderLine(bool);
private slots:
  void on_boldBtn_clicked();
  void on_italicBtn_clicked();
  void on_underLineBtn_clicked();
private:
  void InitIconFont();
private:
  Ui::TextSelectorMenu *ui;
  bool font_blod_ = false;
  bool font_italic_ = false;
  bool font_under_line_ = false;
};

#endif // TEXT_SELECTOR_MENU_H
