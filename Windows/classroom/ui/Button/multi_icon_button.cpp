#include "multi_icon_button.h"
#include "ui_multi_icon_button.h"
#include <QLabel>
#include "common/icon_font_helper.h"

  MultiIconButton::MultiIconButton(QWidget *parent)
    : QToolButton(parent)
  {
    ui = new Ui::MultiIconButton();
    ui->setupUi(this);
  }

  MultiIconButton::~MultiIconButton()
  {
    delete ui;
  }


  void MultiIconButton::SetIcon(const QChar& iconfont_code, int font_size) {
    IconFontHelper::Instance()->SetIcon(ui->labelIcon, iconfont_code, font_size);
  }

  void MultiIconButton::SetIconFontList(const QVector<IconData>& icon_list) {
    ui->labelIcon->SetMoreIconList(icon_list);
  }

  void MultiIconButton::SetDelta(const QPoint& pt) {
    ui->labelIcon->SetDelta(pt);
  }

  QLabel* MultiIconButton::GetIconLabel() const {
    return ui->labelIcon;
  }
