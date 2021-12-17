#ifndef CONFIRM_DIALOG_H
#define CONFIRM_DIALOG_H

#include <QDialog>

namespace Ui {
  class ConfirmDialog;
}

enum DialogType {
  DialogTypeConfirm,
  DialogTypeOkCancel,
};

enum DialogIconType {
  DialogIconTypeSuccess,
  DialogIconTypeFailed,
  DialogIconTypeWaitting,
  DialogIconTypeInfo,
};

struct DialogParam {
  QString title;
  QString content;
  DialogType type = DialogTypeConfirm;
  DialogIconType icon_type = DialogIconTypeSuccess;
  std::vector<QString> btn_vec;
};

class ConfirmDialog : public QDialog {
  Q_OBJECT

public:
  explicit ConfirmDialog(QWidget *parent = nullptr);
  ~ConfirmDialog();
  void SetDialogParam(const DialogParam& param);
private:
  void SetIconStyle(DialogIconType type);
private slots:
  void on_okBtn_clicked();
  void on_cancelBtn_clicked();
private:
  Ui::ConfirmDialog *ui;
};

#endif // CONFIRM_DIALOG_H
