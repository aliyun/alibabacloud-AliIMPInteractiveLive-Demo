#include "confirm_dialog.h"
#include "ui_confirm_dialog.h"
#include "common/icon_font_helper.h"

ConfirmDialog::ConfirmDialog(QWidget *parent) :
  QDialog(parent,  Qt::FramelessWindowHint),
  ui(new Ui::ConfirmDialog) {
  ui->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);
  ui->iconWidget->layout()->setAlignment(ui->icon, Qt::AlignTop);
}

ConfirmDialog::~ConfirmDialog() {
  delete ui;
}


void ConfirmDialog::SetDialogParam(const DialogParam& param) {
  ui->title->setText(param.title);
  ui->content->setText(param.content);
  SetIconStyle(param.icon_type);
  if (param.type == DialogTypeConfirm) {
    ui->cancelBtn->setVisible(false);
  }
  if (param.btn_vec.size() > 0) {
    ui->okBtn->setText(param.btn_vec[0]);
  }

  if (param.btn_vec.size() > 1) {
    ui->cancelBtn->setText(param.btn_vec[1]);
  }

  if (param.content.length() == 0) {
    ui->contentWidget->setVisible(false);
  }
  resize(1, 1);
}

void ConfirmDialog::on_okBtn_clicked() {
  accept();
}

void ConfirmDialog::on_cancelBtn_clicked() {
  reject();
}


const QString kConstIconTemp = "QLabel#icon{"
"font-family:\"lviconfont\";"
"font-size:20px;"
"color:%1;"
"}";

void ConfirmDialog::SetIconStyle(DialogIconType type) {
  switch (type)
  {
  case DialogIconTypeSuccess:
    ui->icon->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->icon, kNotifySuccess);
    break;
  case DialogIconTypeFailed:
    ui->icon->setStyleSheet(kConstIconTemp.arg("#FF7C7C"));
    IconFontHelper::Instance()->SetIcon(ui->icon, kNotifyWarning);
    break;
  case DialogIconTypeWaitting:
    ui->icon->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->icon, kNotifyWarning);
    break;
  case DialogIconTypeInfo:
    ui->icon->setStyleSheet(kConstIconTemp.arg("#4FACFF"));
    IconFontHelper::Instance()->SetIcon(ui->icon, kNotifyInfo);
    break;
  default:
    break;
  }
}