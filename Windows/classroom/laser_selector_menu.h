#ifndef LASER_SELECTOR_MENU_H
#define LASER_SELECTOR_MENU_H

#include <QDialog>

namespace Ui {
  class LaserSelectorMenu;
}

enum LaserSelectorType
{
  LaserSelectorNormal,
  LaserSelectoNew
};

class LaserSelectorMenu : public QDialog
{
  Q_OBJECT

public:
  explicit LaserSelectorMenu(QWidget *parent = nullptr);
  ~LaserSelectorMenu();
  virtual bool eventFilter(QObject* target, QEvent* event) override;
  LaserSelectorType GetLaserType();
  void InitIconFont();
  void SetCurrentType();
signals:
  void NotifyUseLaser();
  void NotifyUseLaserNew();
private slots:
  void on_laserBtn_clicked();
  void on_laserNewBtn_clicked();
private:
  Ui::LaserSelectorMenu *ui;
  LaserSelectorType type_ = LaserSelectorNormal;
};

#endif // LASER_SELECTOR_MENU_H
