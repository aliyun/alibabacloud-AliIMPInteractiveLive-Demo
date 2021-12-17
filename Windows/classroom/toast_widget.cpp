#include "toast_widget.h"
#include "QTimer"
#include "view/view_component_manager.h"
#include "common/icon_font_helper.h"


namespace {
  const QString kToastBkColor = "#171A1D";
  const QString kToastStyle = QString("color:#FFFFFF; background-color: %1; border-radius: 6px; border:1px solid rgba(0,0,0,0.2);").arg(kToastBkColor);
}

ToastWidget::ToastWidget(QWidget *parent)
  : QDialog(parent, Qt::WindowStaysOnTopHint | Qt::FramelessWindowHint | Qt::Tool),
	  ui(new Ui::ToastWidget)
{
	ui->setupUi(this);
  
  setAttribute(Qt::WA_TranslucentBackground);
  setAttribute(Qt::WA_ShowWithoutActivating);
  setFocusPolicy(Qt::NoFocus);
  //SetBlackInfoMode();
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
}

void ToastWidget::SetType(ToastWidgetType type) {
}

void ToastWidget::SetText(QString text)
{
  ui->toastLabel->setText(text);

  // adjust length
  QFontMetrics fontWidth(ui->toastLabel->font());
  int strWidth = fontWidth.boundingRect(text).width();
  setFixedSize(strWidth + 84, height());
}

void ToastWidget::ShowAt(int px, int py, bool active)
{
	
  show();
  raise();
  move(px, py);
  ++show_count_;
  QTimer::singleShot(wait_seconds_ * 1000, this, &ToastWidget::OnSecondTimeOut);
}

void ToastWidget::showEvent(QShowEvent *e) {
  QDialog::showEvent(e);
}

void ToastWidget::hideEvent(QHideEvent *event) {
  QDialog::hideEvent(event);
}

void ToastWidget::OnSecondTimeOut()
{
  --show_count_;
  if (show_count_ <= 0) {
    hide();
  }
}


void ToastWidget::SetHide() {
  hide();
}

void ToastWidget::SetWaitSeconds(int32_t seconds) {
  wait_seconds_ = seconds;
}


void ToastWidget::InitAfterCreate() {
}

void ToastWidget::UnInit() {
}

void ToastWidget::OnShowSuccessToast(const QString& text) {
  hide();
  SetIconStyle(ToastWidgetTypeSuccess);
  SetText(text);
  ShowCenter();
}

void ToastWidget::OnShowFailedToast(const QString& text) {
  hide();
  SetIconStyle(ToastWidgetTypeFailed);
  SetText(text);
  ShowCenter();
}

void ToastWidget::OnShowWattingToast(const QString& text) {
  hide();
  SetIconStyle(ToastWidgetTypeWaitting);
  SetText(text);
  ShowCenter();
}

void ToastWidget::OnShowInfoToast(const QString& text) {
  hide();
  SetIconStyle(ToastWidgetTypeInfo);
  SetText(text);
  ShowCenter();
}

void ToastWidget::ShowSuccessToast(const QString& text) {
  QMetaObject::invokeMethod(this, "OnShowSuccessToast", Q_ARG(QString, text));
}

void ToastWidget::ShowFailedToast(const QString& text) {
  QMetaObject::invokeMethod(
      this, "OnShowFailedToast", Q_ARG(QString, text));
}

void ToastWidget::ShowWattingToast(const QString& text) {
  QMetaObject::invokeMethod(
      this, "OnShowWattingToast",Q_ARG(QString, text));
}

void ToastWidget::ShowInfoToast(const QString& text) {
  QMetaObject::invokeMethod(
    this, "OnShowInfoToast", Q_ARG(QString, text));
}

void ToastWidget::ShowCenter() {
  QRect rect;
  QPoint pos;
  auto login_window = GetViewComponent<QWidget>(kLoginWindow);
  if (login_window->isVisible()) {
    rect = login_window->rect();
    pos = login_window->pos();
  } else {
    auto main_window = GetViewComponent<QWidget>(kMainWindow);
    if (main_window) {
      rect = main_window->rect();
      pos = main_window->pos();
    }
  }

  int32_t left = (rect.width() - width()) / 2 + pos.rx();
  int32_t top = pos.ry() + 100;
  QTimer::singleShot(200, [this, left, top]() {
    ShowAt(left, top);
  });
  
}

const QString kConstIconTemp = "QLabel#iconLabel{"
"font-family:\"lviconfont\";"
"font-size:20px;"
"color:%1;"
"}";

void ToastWidget::SetIconStyle(ToastWidgetType type) {
  switch (type)
  {
  case ToastWidgetTypeSuccess:
    ui->iconLabel->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->iconLabel, kNotifySuccess);
    break;
  case ToastWidgetTypeFailed:
    ui->iconLabel->setStyleSheet(kConstIconTemp.arg("#FF7C7C"));
    IconFontHelper::Instance()->SetIcon(ui->iconLabel, kNotifyWarning);
    break;
  case ToastWidgetTypeWaitting:
    ui->iconLabel->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->iconLabel, kNotifyWarning);
    break;
  case ToastWidgetTypeInfo:
    ui->iconLabel->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->iconLabel, kNotifyInfo);
    break;
  default:
    break;
  }
}