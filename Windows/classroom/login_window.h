#ifndef LOGINWINDOW_H
#define LOGINWINDOW_H

#include <QDialog>
#include <QLabel>
#include <QMovie>
#include <QWidget>
#include "view/interface/i_login_window.h"

class LoginWindowVM;

namespace Ui {
class LoginWindow;
}

class LoginWindow : public QDialog, public ILoginWindow {
  Q_OBJECT

 public:
  explicit LoginWindow(QWidget* parent = nullptr);
  ~LoginWindow();

 private:
  void InitBackground();
  void InitLineEdit();
  void InitLoadingIcon();
  void InitQuitBtn();
  void UpdateLoginBtnStatus();
 protected:
  virtual void InitAfterCreate() override;
  virtual void UnInit() override;
  virtual void mousePressEvent(QMouseEvent *event) override;
  virtual void mouseMoveEvent(QMouseEvent *event) override;
  virtual void mouseReleaseEvent(QMouseEvent *event) override;
 private slots:
  void OnVMUpdate(int32_t filed);
  void OnComboBoxIndexChanged(int);
  void OnComboBoxRoleIndexChanged(int);
  void on_loginBtn_clicked();
  void on_classRoomIdEdit_textChanged(const QString&);
  void on_userIdEdit_textChanged(const QString&);
  void on_classUserNickEdit_textChanged(const QString&);
  void onQuit();

 private:
  Ui::LoginWindow* ui;
  std::shared_ptr<LoginWindowVM> vm_;
  QMovie* loading_movie_ = nullptr;
  QLabel* loading_icon_ = nullptr;
  QPushButton* quitBtn_ = nullptr;

  QPoint press_point_;
  QPoint start_pos_;
  QPoint parent_start_pos_;
  bool drag_window_ = true;
  bool mouse_btn_pressed = false;
};

#endif // LOGINWINDOW_H
