#pragma once
#include "classroom_def.h"


class IBase {
public:
  virtual ~IBase() {}
  virtual void InitAfterCreate() = 0;
  virtual void UnInit() = 0;
};

