#include "pen_selector_menu.h"
#include "ui_pen_selector_menu.h"
#include "QGraphicsEffect"
#include "common/icon_font_helper.h"
#include "size_slide_bar.h"

PenSelectorMenu::PenSelectorMenu(QWidget *parent) :
  QDialog(parent, Qt::FramelessWindowHint | Qt::Tool),
  ui(new Ui::PenSelectorMenu)
{
  setAttribute(Qt::WA_TranslucentBackground);
  ui->setupUi(this);
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  InitIconFont();
  QButtonGroup *btn_group = new QButtonGroup(this);
  btn_group->addButton(ui->circleBtn);
  btn_group->addButton(ui->rectBtn);
  btn_group->addButton(ui->roundRectBtn);
  btn_group->addButton(ui->triangleBtn);
  btn_group->addButton(ui->straightTriangleBtn);
  btn_group->addButton(ui->normalPenBtn);
  btn_group->addButton(ui->straightPenBtn);
  ui->slide->SetDefaultValue(4);
  ui->slide->SetMaxValue(30);
  connect(ui->slide, &SizeSlideBar::NotifyValueChanged, this, &PenSelectorMenu::NotifyLineWidthChange);
  SetCurrentType();
  installEventFilter(this);
  hide();
}

PenSelectorMenu::~PenSelectorMenu()
{
  delete ui;
}

void PenSelectorMenu::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->circleBtn, kCircleNew);
  IconFontHelper::Instance()->SetIcon(ui->rectBtn, kRectNew);
  IconFontHelper::Instance()->SetIcon(ui->roundRectBtn, kRectRounded);
  IconFontHelper::Instance()->SetIcon(ui->triangleBtn, kTriangle);
  IconFontHelper::Instance()->SetIcon(ui->straightTriangleBtn, kTriangleStraight);
  IconFontHelper::Instance()->SetIcon(ui->normalPenBtn, kPen);
  IconFontHelper::Instance()->SetIcon(ui->straightPenBtn, kStraightLine);
  ui->frame->layout()->setAlignment(ui->line, Qt::AlignHCenter);
  ui->frame->layout()->setAlignment(ui->line_2, Qt::AlignHCenter);
}

PenSelectorMenuShapeType PenSelectorMenu::GetShapeType() {
  return shape_type_;
}

int32_t PenSelectorMenu::GetPenSize() {
  return ui->slide->GetValue();
}

void PenSelectorMenu::SetCurrentType() {
  switch (shape_type_)
  {
  case PenSelectorMenuShapeNone:
    shape_type_ = PenSelectorMenuShapePen;
    ui->normalPenBtn->setChecked(true);
    emit NotifyShapeNormalPen();
    break;
  case PenSelectorMenuShapePen:
    ui->normalPenBtn->setChecked(true);
    emit NotifyShapeNormalPen();
    break;
  case PenSelectorMenuShapeStraightPen:
    ui->straightPenBtn->setChecked(true);
    emit NotifyShapeStraightPen();
    break;
  case PenSelectorMenuShapeRect:
    ui->rectBtn->setChecked(true);
    emit NotifyShapeRect();
    break;
  case PenSelectorMenuShapeRoundedRect:
    ui->roundRectBtn->setChecked(true);
    emit NotifyShapeRoundedRect();
    break;
  case PenSelectorMenuShapeCircle:
    ui->circleBtn->setChecked(true);
    emit NotifyShapeCircle();
    break;
  case PenSelectorMenuShapeTriangle:
    ui->triangleBtn->setChecked(true);
    emit NotifyShapeTriangle();
    break;
  case PenSelectorMenuShapeRightTriangle:
    ui->straightTriangleBtn->setChecked(true);
    emit NotifyShapeRightTriangle();
    break;
  default:
    break;
  }
}

void PenSelectorMenu::on_circleBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeCircle;
  SetCurrentType();
}

void PenSelectorMenu::on_rectBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeRect;
  SetCurrentType();
}

void PenSelectorMenu::on_roundRectBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeRoundedRect;
  SetCurrentType();
}

void PenSelectorMenu::on_triangleBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeTriangle;
  SetCurrentType();
}

void PenSelectorMenu::on_straightTriangleBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeRightTriangle;
  SetCurrentType();
}

void PenSelectorMenu::on_normalPenBtn_clicked() {
  shape_type_ = PenSelectorMenuShapePen;
  SetCurrentType();
}

void PenSelectorMenu::on_straightPenBtn_clicked() {
  shape_type_ = PenSelectorMenuShapeStraightPen;
  SetCurrentType();
}

bool PenSelectorMenu::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    hide();
  }
  return false;
}

