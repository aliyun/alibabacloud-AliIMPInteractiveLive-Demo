#include "main_window.h"

#include <QDebug>

#include "ui_main_window.h"
#include "classroom.h"
#include "api/base_api.h"
#include "meta_space.h"
#include "meta/room_in_out_event_model.h"
#include "meta/comment_received_event_model.h"

#include "view/view_component_manager.h"

using namespace alibaba::meta;
using namespace alibaba::meta_space;

MainWindow::MainWindow(QWidget *parent) :
    QWidget(parent),
    ui(new Ui::MainWindow)
{
    ui->setupUi(this);
    connect(ui->EnterRoomButton, SIGNAL(clicked()), this, SLOT(DoLogin()));
}

MainWindow::~MainWindow()
{
  delete ui;
}

void MainWindow::DoLogin() {
  //��ȡ��¼����room_id��user_id
  room_id_ = ui->ClassRoomIdEdit->text().toStdString();
  user_id_ = ui->UidEdit->text().toStdString();
  role_ = GetRole();

  auto meta_space = MetaSpace::GetInstance();
  if (meta_space) {
    //��ȡtoken
    TokenInfo token_info;
    BaseApi::GetTokenApi(user_id_, token_info);
    //��¼
    auto meta_space = MetaSpace::GetInstance();
    meta_space->Login(user_id_, token_info, [this]() {
      //��¼�ɹ������RPC�ӿڽ��뷿��
      qDebug() << "login success";
      QMetaObject::invokeMethod(this, "EnterRoom");
    }, [](std::string error_msg) {
      qDebug() << "login fail";
    });
  }
}

void MainWindow::DoReshow() {
  //this->show();
  GetViewComponent<QWidget>(kLoginWindow)->show();
}

void MainWindow::EnterRoom() {
  //��ȡ����ʵ��
  auto iroom_ptr = MetaSpace::GetInstance()->GetRoomInstance(room_id_);

  //���뷿��
  iroom_ptr->EnterRoom((std::string)(user_id_), [this]() {
    qDebug() << "EnterRoom success";
    QMetaObject::invokeMethod(this, "ShowClassRoomWindow");
  }, [](std::string error_msg) {
    qDebug() << "EnterRoom fail";
  });
}

void MainWindow::UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info) {
  room_id_ = meta_info.class_room_id;
  user_id_ = meta_info.user_id;
  role_ = (ClassRoom::Role)meta_info.role;
}

void MainWindow::ShowClassRoomWindow() {
  //���ҽ���
  class_room_ = std::make_shared<ClassRoom>(room_id_, user_id_, role_);
  class_room_->Start();
  connect(class_room_.get(), SIGNAL(ShowMainWindow()), this, SLOT(DoReshow()));

  //����������
  this->hide();

  //��ʾ���ҽ���
  class_room_->show();
}

void MainWindow::closeEvent(QCloseEvent *event) {

}

void MainWindow::InitAfterCreate() {
}

void MainWindow::UnInit() {
}

ClassRoom::Role MainWindow::GetRole() {
  if (ui->TeacherButton->isChecked()) {
    return ClassRoom::kTeacher;
  }
  else if (ui->StudentButton->isChecked()) {
    return ClassRoom::kStudent;
  }
  else {
    return ClassRoom::kTeacher;
  }
}



