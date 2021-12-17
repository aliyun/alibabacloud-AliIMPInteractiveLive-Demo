#ifndef CLASSROOM_H
#define CLASSROOM_H

#include <QWidget>
#include <QListWidgetItem>
#include <QPushButton>

#include "meta_space.h"

namespace alibaba {
  namespace meta_space {
    class IRoom;
  }
}

namespace Ui {
class ClassRoom;
}

class UserUiItem : public QObject {
  Q_OBJECT
public:
  UserUiItem(const const QString& uid, QListWidget* list, std::shared_ptr<alibaba::meta_space::IRoom> room);

  ~UserUiItem();
public:
  QWidget *user_widge_;
  QPushButton *push_button_;
  QListWidgetItem* item_;
  QListWidget* list_;
  std::shared_ptr<alibaba::meta_space::IRoom> room_;
  QString uid_;
private slots:
  void InvateLinkmic();
};

class UserContext
{
public:
  UserContext(const QString& uid, QListWidget* list, std::shared_ptr<alibaba::meta_space::IRoom> room) {
    item_ = new UserUiItem(uid, list, room);
  }
  ~UserContext() {
    delete item_;
  }

  QString uid_;
  int status_;
  UserUiItem* item_;
};

using namespace alibaba::meta;
using namespace alibaba::meta_space;

class ClassRoom : public QWidget
{
  Q_OBJECT
public:
  enum Role
  {
    kTeacher = 0,
    kStudent = 1,
  };

  explicit ClassRoom(const std::string& room_id, const std::string& user_id, const Role& role);
  ~ClassRoom();

  const std::string GetUserId();
  void Start();
private slots:
  void on_ClassRoom_destroyed();

  void on_pushButton_clicked();

  void on_pushButton_2_clicked();

  void on_pushButton_3_clicked();

  void on_pushButton_4_clicked();

  void OnShowComment(const QString & content);

  void OnShowStream(const QString& uid);

  void OnLeaveRoom();

  void OnAddUser(const const QString& uid);

  void OnDelUser(const const QString& uid); 

signals:
  void ShowMainWindow();
private:
  void CreateBoard(HWND hwndWhiteboard);

  void SetEventListener();

  void ClearEventListener();

  void GetAndShowRoomUserList();

  Ui::ClassRoom *ui;
  //room info
  std::string room_id_;
  std::string user_id_;
  Role role_;
  std::shared_ptr<alibaba::meta_space::IRoom> iroom_ptr_;

  double scale_ = 1.0;
  bool browser_destroyed_ = true;
  bool closing_ = false;
  std::map<QString, std::shared_ptr<UserContext>> user_list_;
};

#endif // CLASSROOM_H
