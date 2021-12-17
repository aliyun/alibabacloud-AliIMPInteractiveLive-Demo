#include "upload_selector_menu.h"
#include "ui_upload_selector_menu.h"
#include "QGraphicsEffect"



bool QUploadItemEventFilter::eventFilter(QObject *obj, QEvent *event) {
  switch (event->type())
  {
  case QEvent::MouseButtonRelease:
    emit NotifyClicked((QWidget*)obj);
    break;
  default:
    break;
  }
  return QObject::eventFilter(obj, event);
}

QUploadItemEventFilter::QUploadItemEventFilter(QObject* parent)
:QObject(parent) {

}

UploadSelectorMenu::UploadSelectorMenu(QWidget *parent) :
  QDialog(parent, Qt::FramelessWindowHint| Qt::Tool),
  ui(new Ui::UploadSelectorMenu)
{
  setAttribute(Qt::WA_TranslucentBackground);
  ui->setupUi(this);
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  QUploadItemEventFilter* filter = new QUploadItemEventFilter(this);
  ui->uploadPptItem->installEventFilter(filter);
  connect(filter, &QUploadItemEventFilter::NotifyClicked, [this](QWidget*) {
    emit NotifyUploadPptClicked();
  });
  installEventFilter(this);
  hide();
}

UploadSelectorMenu::~UploadSelectorMenu() {
  delete ui;
}


bool UploadSelectorMenu::eventFilter(QObject* target, QEvent* event) {
  if (QEvent::WindowDeactivate == event->type()) {
    hide();
  }
  return false;
}

