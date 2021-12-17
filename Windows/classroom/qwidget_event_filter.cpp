#include "qwidget_event_filter.h"
#include <qevent.h>
#include <Windows.h>

bool QWidgetEventFilter::eventFilter(QObject *obj, QEvent *event)
{
  int event_type = event->type();
  switch (event_type)
  {
  case QEvent::Enter:
  {
    emit signalMouseEnter((QWidget*)obj);
    break;
  }
  case QEvent::Leave:
  {
    emit signalMouseLeave((QWidget*)obj);
    break;
  }
  case QEvent::Resize:
  {
    QResizeEvent* resizeEvent = (QResizeEvent*)event;
    signalResize(resizeEvent->size(), resizeEvent->oldSize());
    break;
  } 
  case QEvent::Move:
  {
    QMoveEvent* moveEvent = (QMoveEvent*)event;
    signalMove(moveEvent->pos(), moveEvent->oldPos());
    break;
  }
  case QEvent::UpdateRequest:
  {
    signalUpdateRequest();
    break;
  }
  case QEvent::UpdateLater:
  {
    signalUpdateLater();
    break;
  }
  case QEvent::LayoutRequest:
  {
    signalLayoutRequest();
    break;
  }
  case QEvent::Paint:
  {
    QPaintEvent* paintEvent = (QPaintEvent*)event;
    signalQPaintEvent(paintEvent);
    break;
  }
  case QEvent::WindowActivate:
  {
    signalWindowActivate();
    break;
  }
  case QEvent::WindowDeactivate:
  {
    signalWindowDeactivate();
    break;
  }
  default:
  {
#ifdef _DEBUG

    /*char info[64] = { 0 };
    sprintf(info, "event_type : %d\r\n", event_type);
    ::OutputDebugStringA(info);*/
    
#endif
  }
    break;
  }

  return QObject::eventFilter(obj, event);
}