#include "login_window.h"
#include "QComboBox"
#include "QListView"
#include "QMovie"
#include "QFile"
#include "login_window_vm.h"
#include "scheme_login.h"
#include "ui_login_window.h"
#include "view/interface/i_main_window.h"
#include "view/interface/i_toast_widget.h"
#include "view/view_component_manager.h"
#include "common/icon_font_helper.h"
#include "confirm_dialog.h"
#include "common/logging.h"
#include "const/const.h"

LoginWindow::LoginWindow(QWidget* parent)
  : QDialog(parent, Qt::FramelessWindowHint), ui(new Ui::LoginWindow) {
  ui->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground);
  InitBackground();
  InitLineEdit();
  InitLoadingIcon();
  InitQuitBtn();
  setWindowTitle(QTranslate("InteractiveClass.Title"));
  QGraphicsDropShadowEffect *shadow = new QGraphicsDropShadowEffect(this);
  shadow->setOffset(0, 0);
  shadow->setColor(QColor("#CCCCCC"));
  shadow->setBlurRadius(12);
  setGraphicsEffect(shadow);
  ui->loginBtn->setFocus();
}

LoginWindow::~LoginWindow() { delete ui; }

void LoginWindow::InitLineEdit() {
  QRegExpValidator* revalidotor =
    new QRegExpValidator(QRegExp("[a-zA-Z0-9-]{100}"), this);
  ui->classRoomIdEdit->setValidator(revalidotor);
  ui->userIdEdit->setValidator(revalidotor);
  ui->classRoomIdEdit->setTextMargins(6, 0, 0, 0);
  ui->userIdEdit->setTextMargins(6, 0, 0, 0);
  ui->classUserNickEdit->setTextMargins(6, 0, 0, 0);
}

void LoginWindow::InitBackground() {
  QFile file(":/skin/qss/LoginSkin.qss");
  if (file.open(QIODevice::ReadOnly)) {
    auto style = file.readAll();
    ui->inputWidget->setStyleSheet(style);
    file.close();
  }
}

void LoginWindow::InitLoadingIcon() {
  loading_movie_ = new QMovie(QString::fromUtf8(":/res/images/loading.gif"),
    QByteArray(), ui->inputArea);
  loading_icon_ = new QLabel(ui->inputArea);
  loading_icon_->resize(40, 40);
  loading_icon_->setMovie(loading_movie_);
  loading_icon_->move(50, 320);
  loading_icon_->setVisible(false);
  loading_movie_->setScaledSize(loading_icon_->size());
}

void LoginWindow::InitQuitBtn() {
  quitBtn_ = new QPushButton(this);
  quitBtn_->setFixedSize(30, 30);
  quitBtn_->move(this->width() - 42, 12);
  quitBtn_->setStyleSheet(QString("border: none;"));
  IconFontHelper::Instance()->SetIcon(quitBtn_, QChar(0xe977), 16);
  connect(quitBtn_, &QPushButton::clicked, this, &LoginWindow::onQuit);
  quitBtn_->show();
  quitBtn_->setFocusPolicy(Qt::NoFocus);
}

void LoginWindow::OnComboBoxIndexChanged(int index) {
  vm_->UpdateClassType((ClassTypeEnum)index);
}

void LoginWindow::OnComboBoxRoleIndexChanged(int index) {
  vm_->UpdateClassRole((ClassRoleEnum)index);
}

void LoginWindow::onQuit() { QApplication::quit(); }

void LoginWindow::OnVMUpdate(int32_t filed) {
  ClassroomParam param = vm_->GetClassroomParam();
  if (filed & Field_ShowMainWnd) {
    auto main_wind = GetViewComponent<IMainWindow>(kMainWindow);
    ClassroomMetaInfo meta_info;
    meta_info.class_room_id = param.class_room_id;
    meta_info.type = param.type;
    meta_info.user_id = param.user_id;
    meta_info.role = param.role;
    meta_info.status = param.class_status;
    main_wind->UpdateClassMetaInfo(meta_info);
    main_wind->ShowClassRoomWindow();
    hide();
  }


  if (filed & Field_ErrorNotify) {
    switch (param.error) {
    case ClassLoginErrLogin:
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(QTranslate("InteractiveClass.Error.User"));
      break;
    case ClassLoginErrEnterRoom:
      vm_->DoLogout();
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(QTranslate("InteractiveClass.Error.Room"));
      break;
    case ClassLoginErrNotTeacher:
      vm_->DoLogout();
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(QTranslate("InteractiveClass.Error.NotTeacher"));
      break;
    case ClassLoginErrNetworkError:
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(QTranslate("InteractiveClass.Error.User"));
      break;
    case ClassLoginErrClassEnd:
    {

      ConfirmDialog dialog;
      DialogParam param;
      param.title = QTranslate("InteractiveClass.ErrorOccor");
      param.content = QTranslate("InteractiveLive.ClassAlreadyEnd");
      param.type = DialogTypeConfirm;
      param.icon_type = DialogIconTypeFailed;
      param.btn_vec = { QTranslate("OK") };
      dialog.SetDialogParam(param);
      dialog.exec();

      QApplication::quit();
    }
    break;
    case ClassLoginErrClassDetail:
    {

      ConfirmDialog dialog;
      DialogParam param;
      param.title = QTranslate("InteractiveClass.ErrorOccor");
      param.content = QTranslate("InteractiveLive.ClassDetailError");
      param.type = DialogTypeConfirm;
      param.icon_type = DialogIconTypeFailed;
      param.btn_vec = { QTranslate("OK") };
      dialog.SetDialogParam(param);
      dialog.exec();

      QApplication::quit();
    }
    break;
    default:
      break;
    }

    ui->loginBtn->setEnabled(true);
    loading_icon_->setVisible(false);
    loading_movie_->stop();
    ui->loginBtn->setText(QTranslate("InteractiveClass.DoLogin"));
  }

  if (filed & Field_GetClassDetailDone) {
    std::string room_id = vm_->GetClassroomParam().class_room_id;
    ui->classRoomIdEdit->setText(QString::fromStdString(room_id));
    QString user_id = ui->userIdEdit->text().trimmed();
    vm_->UpdateUserId(user_id.toStdString());
    vm_->DoEnterRoom();
  }
}


void LoginWindow::on_loginBtn_clicked() {
  QString class_room_id = ui->classRoomIdEdit->text().trimmed();
  QString user_id = ui->userIdEdit->text().trimmed();
  QString nick = ui->classUserNickEdit->text().trimmed();
  if (class_room_id.isEmpty() || user_id.isEmpty() || nick.isEmpty()) {
    return;
  }
  ui->loginBtn->setEnabled(false);
  auto login_info = SchemeLogin::Instance()->GetSchemeInfo();
  login_info.class_id = class_room_id.toStdString();
  login_info.nick_name = nick.toStdString();
  login_info.app_id = Const::kAppId;
  login_info.uid = user_id.toStdString();
  SchemeLogin::Instance()->Init(login_info);
  std::string uid = login_info.uid;
  vm_->UpdateAppId(login_info.app_id);
  vm_->UpdateUserId(uid);
  vm_->DoAutoLogin();
  ui->loginBtn->setText(QTranslate("InteractiveClass.Entering"));
  ui->loginBtn->setEnabled(false);
  loading_icon_->setVisible(true);
  loading_movie_->start();
}

void LoginWindow::InitAfterCreate() {
  vm_ = std::make_shared<LoginWindowVM>();
  connect(vm_.get(), &LoginWindowVM::SignalUpdateVM, this,
    &LoginWindow::OnVMUpdate, Qt::QueuedConnection);
}

void LoginWindow::UnInit() {}


void LoginWindow::on_classRoomIdEdit_textChanged(const QString&) {
  UpdateLoginBtnStatus();
}

void LoginWindow::on_classUserNickEdit_textChanged(const QString&) {
  UpdateLoginBtnStatus();
}

void LoginWindow::on_userIdEdit_textChanged(const QString&) {
  UpdateLoginBtnStatus();
}

void LoginWindow::UpdateLoginBtnStatus() {
  if (ui->classRoomIdEdit->text().isEmpty() ||
    ui->userIdEdit->text().isEmpty() ||
    ui->classUserNickEdit->text().isEmpty()) {
    ui->loginBtn->setEnabled(false);
    ui->loginBtn->setStyleSheet("QPushButton#loginBtn{background: rgb(163,192,255);}");
  }
  else {
    ui->loginBtn->setEnabled(true);
    ui->loginBtn->setStyleSheet("QPushButton#loginBtn{background: #4FACFF;}");
  }
}

void LoginWindow::mousePressEvent(QMouseEvent *event)
{
  if (drag_window_) {
    press_point_ = event->globalPos();
    start_pos_ = pos();

    QWidget* top_parent = this;
    if (top_parent) {
      parent_start_pos_ = top_parent->pos();
    }

    mouse_btn_pressed = true;
  }

  QDialog::mousePressEvent(event);
}

void LoginWindow::mouseMoveEvent(QMouseEvent *event)
{
  if (drag_window_ && mouse_btn_pressed) {
    auto move_pos = event->globalPos();
    auto offset_pos = move_pos - press_point_;
    if (offset_pos == QPoint(0, 0)) {
      return QDialog::mouseMoveEvent(event);
    }

    move(start_pos_ + offset_pos);

  }

  QDialog::mouseMoveEvent(event);
}

void LoginWindow::mouseReleaseEvent(QMouseEvent *event)
{
  if (drag_window_) {
    mouse_btn_pressed = false;
  }

  QDialog::mouseReleaseEvent(event);
}