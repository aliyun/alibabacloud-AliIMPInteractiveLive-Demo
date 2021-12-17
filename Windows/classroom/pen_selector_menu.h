#ifndef PEN_SELECTOR_MENU_H
#define PEN_SELECTOR_MENU_H

#include <QDialog>

namespace Ui {
  class PenSelectorMenu;
}

enum PenSelectorMenuShapeType
{
  PenSelectorMenuShapeNone,
  PenSelectorMenuShapePen,
  PenSelectorMenuShapeStraightPen,
  PenSelectorMenuShapeRect,
  PenSelectorMenuShapeRoundedRect,
  PenSelectorMenuShapeCircle,
  PenSelectorMenuShapeTriangle,
  PenSelectorMenuShapeRightTriangle,
};

class PenSelectorMenu : public QDialog
{
  Q_OBJECT

public:
  explicit PenSelectorMenu(QWidget *parent = nullptr);
  ~PenSelectorMenu();
  PenSelectorMenuShapeType GetShapeType();
  void SetCurrentType();
  int32_t GetPenSize();
signals:
  void NotifyLineWidthChange(int32_t value);
  void NotifyShapeRect();
  void NotifyShapeRoundedRect();
  void NotifyShapeCircle();
  void NotifyShapeTriangle();
  void NotifyShapeRightTriangle();
  void NotifyShapeNormalPen();
  void NotifyShapeStraightPen();
private slots:
  void on_circleBtn_clicked();
  void on_rectBtn_clicked();
  void on_roundRectBtn_clicked();
  void on_triangleBtn_clicked();
  void on_straightTriangleBtn_clicked();
  void on_normalPenBtn_clicked();
  void on_straightPenBtn_clicked();
private:
  void InitIconFont();
  virtual bool eventFilter(QObject* target, QEvent* event) override;
private:
  Ui::PenSelectorMenu *ui;
  PenSelectorMenuShapeType shape_type_ = PenSelectorMenuShapeNone;
};

#endif // PEN_SELECTOR_MENU_H
