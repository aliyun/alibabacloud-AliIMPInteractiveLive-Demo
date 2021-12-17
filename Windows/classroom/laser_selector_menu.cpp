#include "laser_selector_menu.h"
#include "ui_laser_selector_menu.h"
#include "QGraphicsEffect"
#include "QButtonGroup"
#include "common/icon_font_helper.h"

LaserSelectorMenu::LaserSelectorMenu(QWidget *parent) :
  QDialog(parent, Qt::FramelessWindowHint| Qt::Tool),
  ui(new Ui::LaserSelectorMenu)
{
  ui->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground);
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  QButtonGroup* group = new QButtonGroup(this);
  group->addButton(ui->laserBtn);
  group->addButton(ui->laserNewBtn);
  InitIconFont();
  installEventFilter(this);
  hide();
}

LaserSelectorMenu::~LaserSelectorMenu()
{
  delete ui;
}


bool LaserSelectorMenu::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    hide();
  }
  return false;
}


void LaserSelectorMenu::on_laserBtn_clicked() {
  type_ = LaserSelectorNormal;
  emit NotifyUseLaser();
}

void LaserSelectorMenu::on_laserNewBtn_clicked() {
  type_ = LaserSelectoNew;
  emit NotifyUseLaserNew();
}


LaserSelectorType LaserSelectorMenu::GetLaserType() {
  return type_;
}

void LaserSelectorMenu::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->laserBtn, kLaser);
  IconFontHelper::Instance()->SetIcon(ui->laserNewBtn, kLaserNewIcon);
}

void LaserSelectorMenu::SetCurrentType() {
  switch (type_)
  {
  case LaserSelectorNormal:
    emit NotifyUseLaser();
    ui->laserBtn->setChecked(true);
    break;
  case LaserSelectoNew:
    emit NotifyUseLaserNew();
    ui->laserNewBtn->setChecked(true);
    break;
  default:
    break;
  }
}