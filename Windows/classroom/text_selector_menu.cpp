#include "text_selector_menu.h"
#include "ui_text_selector_menu.h"
#include "QGraphicsEffect"
#include "common/icon_font_helper.h"

TextSelectorMenu::TextSelectorMenu(QWidget *parent) :
  QDialog(parent, Qt::FramelessWindowHint| Qt::Tool),
  ui(new Ui::TextSelectorMenu)
{
  setAttribute(Qt::WA_TranslucentBackground);
  ui->setupUi(this);
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  ui->slide->SetMinValue(20);
  ui->slide->SetDefaultValue(20);
  ui->slide->SetMaxValue(100);
  connect(ui->slide, &SizeSlideBar::NotifyValueChanged, this, &TextSelectorMenu::NotifyFontSizeChange);
  InitIconFont();
  installEventFilter(this);
  ui->frame->layout()->setAlignment(ui->line, Qt::AlignHCenter);
  hide();
}

TextSelectorMenu::~TextSelectorMenu() {
  delete ui;
}


bool TextSelectorMenu::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    hide();
  }
  return false;
}

void TextSelectorMenu::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->boldBtn, kBlodIcon);
  IconFontHelper::Instance()->SetIcon(ui->italicBtn, kItalicIcon);
  IconFontHelper::Instance()->SetIcon(ui->underLineBtn, kUnderlineIcon);
}

void TextSelectorMenu::on_boldBtn_clicked() {
  font_blod_ = ui->boldBtn->isChecked();
  NotifyFontBold(font_blod_);
}

void TextSelectorMenu::on_italicBtn_clicked() {
  font_italic_ = ui->italicBtn->isChecked();
  NotifyFontItalic(font_italic_);
}

void TextSelectorMenu::on_underLineBtn_clicked() {
  font_under_line_ = ui->underLineBtn->isChecked();
  NotifyFontUnderLine(font_under_line_);
}

int32_t TextSelectorMenu::GetTextType() {
  int32_t type = 0;
  if (font_blod_) {
    type |= TextSelectorBlod;
  }

  if (font_italic_) {
    type |= TextSelectorItalic;
  }

  if (font_under_line_) {
    type |= TextSelectorUnderLine;
  }
  return type;
}

void TextSelectorMenu::SetCurrentType() {

}


int32_t TextSelectorMenu::GetFontSize() {
  return ui->slide->GetValue();
}