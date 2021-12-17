#ifndef WIDGET_H
#define WIDGET_H

#include <QWidget>
#include "classroom.h"
#include "view/interface/i_main_window.h"

namespace Ui {
class MainWindow;
}

class MainWindow : public QWidget, public IMainWindow
{
    Q_OBJECT

public:
    explicit MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void DoLogin();

    void DoReshow();

    void EnterRoom();

    void ShowClassRoomWindow();
    void UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info);
signals:
    void ShowClassRoom(const std::string& room_id, const std::string& user_id);

protected:
    void closeEvent(QCloseEvent *event);
    virtual void InitAfterCreate() override;
    virtual void UnInit() override;
private:
  ClassRoom::Role GetRole();

    Ui::MainWindow *ui;
    std::shared_ptr<ClassRoom> class_room_;
    std::string room_id_;
    std::string user_id_;
    ClassRoom::Role role_;
    void Login();

};

#endif // WIDGET_H
