#pragma once
#include <QObject>
#include <QSize>
#include <QPoint>
#include <qevent.h>
class QWidgetEventFilter : public QObject
{
  Q_OBJECT

public:
signals:
  void signalResize(const QSize &size, const QSize &oldSize);
  void signalMove(const QPoint &pos, const QPoint &oldPos);
  void signalUpdateRequest();
  void signalUpdateLater();
  void signalLayoutRequest();
  void signalQPaintEvent(QPaintEvent* evt);
  void signalWindowActivate();
  void signalWindowDeactivate();
  void signalMouseEnter(QWidget* widget);
  void signalMouseLeave(QWidget* widget);
protected:
  bool eventFilter(QObject *obj, QEvent *event) override;
};

