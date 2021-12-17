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

// 老师端UI状态
// linkmic是否连麦
// shared是否共享屏幕
// 主屏和副屏可选display
enum TeacherUIState {
  k_teacheruistate_linkmic0_shared0 = 0, // wb, rtcpreview
  k_teacheruistate_linkmic0_shared1 = 3, // rtcscreen, rtcpreview
  k_teacheruistate_linkmic1_shared0 = 4, // wb, rtcpreview
  k_teacheruistate_linkmic1_shared1 = 6, // rtcscreen, rtcpreview
};

// 学生端UI状态
// linkmic是否连麦
// shared是否共享屏幕
// 主屏和副屏可选display
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
  // 主体UI响应连麦
  void OnMainUIHandleLinkmic(bool linkmic);
  // 主题UI响应共享屏幕
  void OnMainUIHandleSharedScreen(bool start_shared_screen);
  // 老师端UI状态转换处理
  void OnChangeTeacherUIState(TeacherUIState next_state);
  // 学生端UI状态转换处理
  void OnChangeStudentUIState(StudentUIState next_state);

  void CleanupLinkmicResource();
  void ModifyLinkMicResource();

  // 置换主或副Display, 并同步更新屏幕内容状态
  bool ReplaceDisplay(QWidget* to, QWidget* from);
  // 设置主屏幕倾向显示的内容(如果可以, 通过交互主副屏实现)
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

  // 指向学生端邀请框
  ConfirmDialog* student_invited_confirm_dialog_ = nullptr;
  // 学生端邀请框等待确认时候, 为保护UI状态机, 缓冲起命令
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
  std::vector<std::string> linkmic_user_uid_fifo_; // 记录连麦用户出现顺序
  int32_t speaker_display_first_user_index_ = 0;
  ThumbnailContainer* thumbnail_container_ = nullptr;
};

#endif // CLASS_MAIN_WINDOW_H