#include "classroom_def.h"

QStackedWidget* RemoveDisplayFromContainer(QWidget* display) {

  static QStackedWidget* tmp_parent;
  if (!tmp_parent) {
    tmp_parent = new QStackedWidget();
  }
  QStackedWidget* parent = dynamic_cast<QStackedWidget*>(display->parentWidget());
  if (parent) {
    parent->removeWidget(display);
  }
  display->setParent(tmp_parent);

  return parent;
}
void AddDisplayToContainer(QStackedWidget* container, QWidget* display)
{
  RemoveDisplayFromContainer(display);
  container->addWidget(display);
  container->setCurrentWidget(display);
}