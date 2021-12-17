#ifndef CLASS_MAIN_WINDOW_H
#define CLASS_MAIN_WINDOW_H

#include <QButtonGroup>
#include <QLabel>
#include <QMainWindow>
#include <QStackedWidget>
#include <QWidget>
#include <functional>
#include <memory>
#include <queue>

#include "classroom_def.h"
#include "display_controls.h"
#include "meta/ali_rtc_video_track.h"
#include "qwidget_event_filter.h"
#include "view/interface/i_main_window.h"

namespace Ui {
class ClassMainWindow;
}

class ThumbnailContainer;

// ��ʦ��UI״̬
// linkmic�Ƿ�����
// shared�Ƿ�����Ļ
// �����͸�����ѡdisplay
enum TeacherUIState {
  k_teacheruistate_linkmic0_shared0 = 0, // wb, rtcpreview
  k_teacheruistate_linkmic0_shared1 = 3, // rtcscreen, rtcpreview
  k_teacheruistate_linkmic1_shared0 = 4, // wb, rtcpreview
  k_teacheruistate_linkmic1_shared1 = 6, // rtcscreen, rtcpreview
};

// ѧ����UI״̬
// linkmic�Ƿ�����
// shared�Ƿ�����Ļ
// �����͸�����ѡdisplay
enum StudentUIState {
  k_studentuistate_linkmic0_shared0 = 8,  // wb, live
  k_studentuistate_linkmic0_shared1 = 10, // live, rtcpreview
  k_studentuistate_linkmic1_shared0 = 12, // wb, rtcpreview
  k_studentuistate_linkmic1_shared1 = 14, // rtcscreen_rtcpreview
};


class ClassMainWindowVM;
class StudentControls;
class ChatWidget;
class ConfirmDialog;
class ThumbnailWidget;
class RecordingTipsDialog;
class SwitchZoomDialog;


class ClassMainWindow : public QWidget, public IMainWindow {
  Q_OBJECT
 public:
  explicit ClassMainWindow(QWidget* parent = nullptr);
 private:
  virtual void InitAfterCreate() override;
  virtual void UnInit() override;
  virtual void ShowClassRoomWindow() override;
  virtual void UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info) override;
  void InitBottomWidget();
  void InitMedia();
  void InitWhiteBoard();
  void InitStudentControls();
  void InitUserListWidget();
  void InitScreenShareMask();
  void InitScreenSreamWidget();
  void InitThumbnailContainer();
  void AdjustThumbnailContainer();
  virtual void moveEvent(QMoveEvent* event) override;
  void AddChatWidget(const ClassroomMetaInfo& meta_info);
  void InitTableWidget();
  virtual void resizeEvent(QResizeEvent* event) override;
  virtual bool nativeEvent(const QByteArray& eventType,
                           void* message,
                           long* result) override;
  DisplayControls* GetDisplayControlsByUID(const std::string& uid);
  void UpdateMainDisplayContainerSubDisplay();
  void AdjustPreviewControls();
  void ZoomSwitchDisplay();
  void UpdateDisplayControlsStatusName(DisplayControls* widget,
                                       const std::string uid,
                                       const std::string user_name,
                                       const bool is_teacher);

 signals:
  void NotifyChangeCameraMuteButtonStatus(const bool mute);

 private slots:
  void OnVMUpdate(int32_t filed);
  void OnZoom(bool zoom);
  void OnGroupButtonClicked(int btn_id);
  void OnNewMsg();
  void OnUserListCountChange(int32_t);
  void OnListItemChange(const QString& user_id, bool link);
  void OnNotifyWhiteBoardLoaded();
  void OnWhiteBoardDisplayUpdateSize();
  QWidget* GetDisplayByDisplayContentType(DisplayContentType type);
  DisplayContentType GetDisplayContentTypeByDisplay(QWidget* display);
  void OnCloseBtnClicked();
  void OnAutoRecordChanged(bool);
  void OnConfLayoutChanged(int32_t layout);
  bool HitTestDrag(QPoint mouse_wnd_pos, long* result);
  void OnMainDisplayContainerNotifyUpdateTopWindowLayout();
  void OnMainDisplayContainerPageUp();
  void OnMainDisplayContainerPageDown();
  void OnPlayerVideoFirstFrameRender();
  void OnRtcFirstLocalVideoFrameDrawn(int32_t width,
                                      int32_t height,
                                      int32_t elapsed);
  void SetLocalPreviewVideoMute(const bool mute);


 private:
  // ����UI��Ӧ����
  void OnMainUIHandleLinkmic(bool linkmic);
  // ����UI��Ӧ������Ļ
  void OnMainUIHandleSharedScreen(bool start_shared_screen);
  // ��ʦ��UI״̬ת������
  void OnChangeTeacherUIState(TeacherUIState next_state);
  // ѧ����UI״̬ת������
  void OnChangeStudentUIState(StudentUIState next_state);

  void CleanupLinkmicResource();
  void ModifyLinkMicResource();

  // �û�����Display, ��ͬ��������Ļ����״̬
  bool ReplaceDisplay(QWidget* to, QWidget* from);
  // ��������Ļ������ʾ������(�������, ͨ������������ʵ��)
  bool SetMainDisplayPreferContent(DisplayContentType type);

 private:
  std::shared_ptr<Ui::ClassMainWindow> ui;
  std::shared_ptr<ClassMainWindowVM> vm_;
  ChatWidget* chat_wid_ = nullptr;
  QWidget* bottom_widget_ = nullptr;
  QWidget* preview_controls_ = nullptr;
  QWidget* main_preview_controls_ = nullptr;
  std::shared_ptr<StudentControls> student_controls_ = nullptr;
  QButtonGroup* btn_group_ = nullptr;
  QLabel* new_msg_notify_ = nullptr;
  QWidget* player_display_ = nullptr;
  QWidgetEventFilter event_filter_whiteboard_display_;
  RecordingTipsDialog* recording_tips_dialog_ = nullptr;
  SwitchZoomDialog* switch_zoom_dialog_ = nullptr;

  QWidget* rtc_preview_display_ = nullptr;
  QWidget* whiteboard_display_ = nullptr;
  QWidget* live_display_ = nullptr;
  QWidget* rtc_screen_display_ = nullptr;
  QLabel* screen_label_ = nullptr;
  std::unique_ptr<DisplayControls> live_display_display_controls_;
  std::unique_ptr<DisplayControls> rtc_preview_display_display_controls_;
  std::unique_ptr<DisplayControls> whiteboard_display_display_controls_;
  TeacherUIState teacher_ui_state_ = k_teacheruistate_linkmic0_shared0;
  StudentUIState student_ui_state_ = k_studentuistate_linkmic0_shared0;

  // ָ��ѧ���������
  ConfirmDialog* student_invited_confirm_dialog_ = nullptr;
  // ѧ���������ȴ�ȷ��ʱ��, Ϊ����UI״̬��, ����������
  std::queue<std::function<void()>> queued_command_;

  struct LinkMicUserInfo {
    ~LinkMicUserInfo() {
      display_controls.reset();
      display.reset();
    }

    std::string uid;
    bool is_teacher = false;
    bool enable_video_stream = false;

    std::unique_ptr<QWidget> display;
    std::unique_ptr<DisplayControls> display_controls;
  };
  typedef std::shared_ptr<LinkMicUserInfo> LinkMicUserInfoPtr;
  std::map<std::string, LinkMicUserInfoPtr> linkmic_user_info_; // uid -> info
  std::vector<std::string> linkmic_user_uid_fifo_; // ��¼�����û�����˳��
  int32_t speaker_display_first_user_index_ = 0;
  ThumbnailContainer* thumbnail_container_ = nullptr;
};

#endif // CLASS_MAIN_WINDOW_H