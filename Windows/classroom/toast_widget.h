
#pragma once

#include <QDockWidget>
#include <QDialog>
#include <string>
#include <memory>
#include <QShowEvent>
#include <QHideEvent>

#include "ui_toast_widget.h"
#include "view/interface/i_toast_widget.h"

enum ToastWidgetType {
  ToastWidgetTypeSuccess,
  ToastWidgetTypeFailed,
  ToastWidgetTypeWaitting,
  ToastWidgetTypeInfo,
};

class ToastWidget : public QDialog, public IToastWidget{
	Q_OBJECT

private:
	std::unique_ptr<Ui::ToastWidget> ui;
  
  static const int MAX_WAIT_SECONDS = 5;

public:
  ToastWidget(QWidget *parent);
  void SetWaitSeconds(int32_t seconds);
  void SetType(ToastWidgetType type);
  void SetText(QString text);
	void ShowAt(int px, int py, bool active = true);
  void SetHide();
  virtual void showEvent(QShowEvent *) override;
  virtual void hideEvent(QHideEvent *event) override;
protected:
  virtual void InitAfterCreate() override;
  virtual void UnInit() override;
  virtual void ShowSuccessToast(const QString& text) override;
  virtual void ShowFailedToast(const QString& text) override;
  virtual void ShowWattingToast(const QString& text) override;
  virtual void ShowInfoToast(const QString& text) override;
  void SetIconStyle(ToastWidgetType type);
  void ShowCenter();
private slots:
  void OnSecondTimeOut();
  void OnShowSuccessToast(const QString& text);
  void OnShowFailedToast(const QString& text);
  void OnShowWattingToast(const QString& text);
  void OnShowInfoToast(const QString& text);
private:
  int show_count_ = 0;
  int32_t wait_seconds_ = 5;
};
