#pragma once

#include "base_interface.h"
#include <string>
#include <QPoint>

class IToastWidget :public IBase {
public:
  virtual ~IToastWidget() {}
  virtual void ShowSuccessToast(const QString& text) = 0;
  virtual void ShowFailedToast(const QString& text) = 0;
  virtual void ShowWattingToast(const QString& text) = 0;
  virtual void ShowInfoToast(const QString& text) = 0;
};
