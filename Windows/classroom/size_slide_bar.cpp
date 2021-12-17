#include "size_slide_bar.h"
#include "ui_size_slide_bar.h"

SizeSlideBar::SizeSlideBar(QWidget *parent) :
  QWidget(parent),
  ui(new Ui::SizeSlideBar) {
  ui->setupUi(this);
  connect(ui->slider, &QSlider::valueChanged, [this](int value) {
    ui->data->setText(QString("%1").arg(value));
    emit NotifyValueChanged(value);
  });
}

SizeSlideBar::~SizeSlideBar() {
  delete ui;
}

void SizeSlideBar::SetMaxValue(int32_t value) {
  ui->slider->setMaximum(value);
}

void SizeSlideBar::SetDefaultValue(int32_t value) {
  ui->slider->setValue(value);
  ui->data->setText(QString("%1").arg(value));
}

void SizeSlideBar::SetMinValue(int32_t value) {
  ui->slider->setMinimum(value);
}

int32_t SizeSlideBar::GetValue() {
  return ui->slider->value();
}

