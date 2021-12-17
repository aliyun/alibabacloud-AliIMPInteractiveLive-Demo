#include "class_main_window.h"

#include <Dwmapi.h>
#include <windowsx.h>

#include "QButtonGroup"
#include "QTimer"
#include "bottom_widget.h"
#include "chat_widget.h"
#include "class_main_window_vm.h"
#include "common/icon_font_helper.h"
#include "conf_performance_dialog.h"
#include "conf_share_dialog.h"
#include "confirm_dialog.h"
#include "display_controls.h"
#include "event/event_manager.h"
#include "main_display_container.h"
#include "main_display_preview.h"
#include "recording_tips_dialog.h"
#include "switch_zoom_dialog.h"
#include "small_preview_controls.h"
#include "student_controls.h"
#include "thumbnail_container.h"
#include "thumbnail_widget.h"
#include "ui_class_main_window.h"
#include "ui_main_display_container.h"
#include "view/interface/i_toast_widget.h"
#include "user_item_widget.h"
#include "scheme_login.h"
#include "view/view_component_manager.h"

extern QWidget* GetTopParentWidget(QWidget* widget);

const int32_t kConstStudentBtnIndex = 0;
const int32_t kConstChatBtnIndex = 1;

void SetWidgetBorderless(const QWidget* widget) {
#ifdef Q_OS_WIN
  HWND hwnd = reinterpret_cast<HWND>(widget->winId());

  const LONG style = (WS_POPUP | WS_CAPTION | WS_SYSMENU | WS_MINIMIZEBOX |
                      WS_MAXIMIZEBOX | WS_THICKFRAME | WS_CLIPCHILDREN);
  SetWindowLongPtr(hwnd, GWL_STYLE, style);

  const MARGINS shadow = {1, 1, 1, 1};
  DwmExtendFrameIntoClientArea(hwnd, &shadow);

  SetWindowPos(hwnd, 0, 0, 0, 0, 0, SWP_FRAMECHANGED | SWP_NOMOVE | SWP_NOSIZE);
#endif
}

ClassMainWindow::ClassMainWindow(QWidget* parent)
    : QWidget(parent,
              Qt::FramelessWindowHint | Qt::Window | Qt::FramelessWindowHint |
                  Qt::WindowSystemMenuHint | Qt::WindowMinMaxButtonsHint),
      ui(new Ui::ClassMainWindow) {
  
  ui->setupUi(this);
  SetWidgetBorderless(this);
  whiteboard_display_ = ui->mainDisplayContainer->ui->whiteboardDisplay;
  live_display_ = ui->mainDisplayContainer->ui->liveDisplay;
  rtc_preview_display_ = ui->mainDisplayContainer->ui->rtcPreviewDisplay;
  rtc_screen_display_ = ui->mainDisplayContainer->ui->rtcSreenDisplay;
  live_display_->setUpdatesEnabled(false);
  rtc_preview_display_->setUpdatesEnabled(false);

  switch_zoom_dialog_ = new SwitchZoomDialog(ui->smallDisplayContainer, this);
  switch_zoom_dialog_->setVisible(true);
  connect(switch_zoom_dialog_,
          &SwitchZoomDialog::NotifyZoom, this, &ClassMainWindow::OnZoom);

  rtc_preview_display_display_controls_ =
      std::make_unique<DisplayControls>(rtc_preview_display_, this);
  rtc_preview_display_display_controls_->UpdateZoomBtnStatus(true);
  rtc_preview_display_display_controls_->SetNameLabelEnable(false);

  live_display_display_controls_ =
      std::make_unique<DisplayControls>(live_display_, this);
  live_display_display_controls_->SetVideoMute(true);
  live_display_display_controls_->SetAudioMute(false);
  live_display_display_controls_->SetActiveSpeaker(false);
  live_display_display_controls_->UpdateZoomBtnStatus(true);
  live_display_display_controls_->SetNameLabelEnable(false);
  live_display_display_controls_->SetVideoVisable(false);
  live_display_display_controls_->SetAudioVisable(false);

  rtc_preview_display_display_controls_->SetVideoMute(true);
  rtc_preview_display_display_controls_->SetAudioMute(false);
  rtc_preview_display_display_controls_->SetActiveSpeaker(false);

  whiteboard_display_display_controls_ = std::make_unique<DisplayControls>(whiteboard_display_, this);
  whiteboard_display_display_controls_->SetAudioVisable(false);
  whiteboard_display_display_controls_->SetVideoVisable(false);
  whiteboard_display_display_controls_->SetVideoMute(false);
  whiteboard_display_display_controls_->UpdateZoomBtnStatus(false);
  whiteboard_display_display_controls_->SetBottomVisable(false);
  whiteboard_display_display_controls_->SetNameLabelEnable(false);
  whiteboard_display_display_controls_->SetUserName(QTranslate("InteractiveClass.CoursewareDisplay"));
  RemoveDisplayFromContainer(whiteboard_display_);
  RemoveDisplayFromContainer(live_display_);
  RemoveDisplayFromContainer(rtc_preview_display_);
  RemoveDisplayFromContainer(rtc_screen_display_);

  InitTableWidget();
  InitThumbnailContainer();
  whiteboard_display_->installEventFilter(&event_filter_whiteboard_display_);
  setWindowTitle(QTranslate("InteractiveClass.Title"));

  QDesktopWidget* desktop = QApplication::desktop();
  if (desktop) {
    QRect screen = desktop->screenGeometry();
    this->move((screen.width() - this->width()) / 2,
               (screen.height() - this->height()) / 2);
  }
}

void ClassMainWindow::InitAfterCreate() {
  vm_ = std::make_shared<ClassMainWindowVM>();
  connect(vm_.get(), &ClassMainWindowVM::SignalUpdateVM, this,
          &ClassMainWindow::OnVMUpdate, Qt::QueuedConnection);

  connect(ui->userListWidget, &UserListWidget::SignalListItemChange, this,
          &ClassMainWindow::OnListItemChange);
  connect(&event_filter_whiteboard_display_,
          &QWidgetEventFilter::signalQPaintEvent, this,
          &ClassMainWindow::OnWhiteBoardDisplayUpdateSize);
  connect(ui->titleWidget, &TitleWidget::NotifyClose, this,
          &ClassMainWindow::OnCloseBtnClicked);

  connect(ui->mainDisplayContainer,
          &MainDisplayContainer::signalNotifyUpdateTopWindowLayout, this,
          &ClassMainWindow::OnMainDisplayContainerNotifyUpdateTopWindowLayout);
  connect(ui->mainDisplayContainer, &MainDisplayContainer::signalNotifyPageUp,
          this, &ClassMainWindow::OnMainDisplayContainerPageUp);
  connect(ui->mainDisplayContainer, &MainDisplayContainer::signalNotifyPageDown,
          this, &ClassMainWindow::OnMainDisplayContainerPageDown);
  connect(EventManager::Instance().get(),
          &EventManager::SignalPlayerVideoFirstFrameRender, this,
          &ClassMainWindow::OnPlayerVideoFirstFrameRender);
  connect(EventManager::Instance().get(),
          &EventManager::SignalRtcFirstLocalVideoFrameDrawn, this,
          &ClassMainWindow::OnRtcFirstLocalVideoFrameDrawn);
}

void ClassMainWindow::UnInit() {}

DisplayControls* ClassMainWindow::GetDisplayControlsByUID(
    const std::string& uid) {
  auto found = linkmic_user_info_.find(uid);
  if (found != linkmic_user_info_.end()) {
    return found->second->display_controls.get();
  }
  return nullptr;
}

void ClassMainWindow::UpdateMainDisplayContainerSubDisplay() {
  // 处理前提
  // 1. MainDisplayMode_SpeakerMode 或者 MainDisplayMode_GridMode
  // 2. speaker_display_first_user_index_ 选定好了首个显示用户（范围正确）
  // 3. linkmic_user_uid_fifo_ 已经调整完毕


  if (speaker_display_first_user_index_ < 0 ||
      speaker_display_first_user_index_ >= linkmic_user_uid_fifo_.size()) {
    // 选定首个显示用户不正确，超出范围
    assert(0);
    return;
  }

  auto model = vm_->GetClassMainWindowModel();

  if (model.main_display_mode ==
      MainDisplayMode::MainDisplayMode_BigDisplayMode) {
    // 连麦下不该出现这种case
    assert(0);
  } else if (model.main_display_mode ==
                 MainDisplayMode::MainDisplayMode_SpeakerMode ||
             model.main_display_mode ==
                 MainDisplayMode::MainDisplayMode_GridMode) {
    ui->mainDisplayContainer->ClearAllSubDisplay();

    // 计算哪些用户该被显示出来
    MainDisplayContainerConfig config = ui->mainDisplayContainer->GetConfig();

    std::vector<std::string> show_uid_list;

    for (size_t i = 0; i < config.max_sub_display_count; ++i) {
      int index = speaker_display_first_user_index_ + i;
      if (index >= linkmic_user_uid_fifo_.size()) {
        break;
      }
      const std::string& uid = linkmic_user_uid_fifo_[index];
      show_uid_list.push_back(uid);
    }

    // 处理需要被显示的用户和不需要被显示的用户
    for (auto it : linkmic_user_info_) {
      LinkMicUserInfoPtr info = it.second;
      auto found =
          std::find(show_uid_list.begin(), show_uid_list.end(), info->uid);
      bool need_show = found != show_uid_list.end();

      if (need_show) {
        if (!info->enable_video_stream) {
          vm_->ShowStream(info->uid, (void*)info->display->winId());
          info->enable_video_stream = true;
        }
        ui->mainDisplayContainer->AddSubDisplay(info->display.get());
      } else {
        if (info->enable_video_stream) {
          vm_->StopStream(info->uid);
          info->enable_video_stream = false;
        }
        if (info->display) {
          RemoveDisplayFromContainer(info->display.get());
        }
      }
    }
    if (model.role == ClassRoleEnum_Teacher) {
      RtcLayoutModel rtc_layout_model;
      std::vector<std::string> layout_user_ids;
      if (model.main_display_mode ==
          MainDisplayMode::MainDisplayMode_SpeakerMode) {
        rtc_layout_model = RtcLayoutModel::ONE_SUPPORT_FOUR;
        for (int i = 0; i < 4; i++) {
          if (i >= show_uid_list.size())
            layout_user_ids.push_back("");
          else
            layout_user_ids.push_back(show_uid_list[i]);
        }
        layout_user_ids.push_back(model.user_id);
      } else {
        rtc_layout_model = RtcLayoutModel::NINE_GRID;
        layout_user_ids.push_back(model.user_id);
        for (auto itor : show_uid_list) layout_user_ids.push_back(itor);
      }
      vm_->SetLayout(layout_user_ids, rtc_layout_model);   
    }
  } else {
    // 未定义行为
    assert(0);
  }
}

QWidget* ClassMainWindow::GetDisplayByDisplayContentType(
    DisplayContentType type) {
  switch (type) {
    case DisplayContentType_Live:
      return live_display_;
    case DisplayContentType_RtcPreview:
      return rtc_preview_display_;
    case DisplayContentType_WhiteBoard:
      return whiteboard_display_;
    case DisplayContentType_SreenStream:
      return rtc_screen_display_;
    default:
      break;
  }
  return nullptr;
}
DisplayContentType ClassMainWindow::GetDisplayContentTypeByDisplay(
    QWidget* display) {
  if (display == rtc_preview_display_) {
    return DisplayContentType_RtcPreview;
  } else if (display == live_display_) {
    return DisplayContentType_Live;
  } else if (display == whiteboard_display_) {
    return DisplayContentType_WhiteBoard;
  } else if (display == rtc_screen_display_) {
    return DisplayContentType_SreenStream;
  } else {
    assert(0);
    return DisplayContentType_Unknown;
  }
}

void ClassMainWindow::CleanupLinkmicResource() {
  // 对之前所有连麦用户，停止拉流，删除显示显示
  ui->mainDisplayContainer->ClearAllSubDisplay();
  for (auto it : linkmic_user_info_) {
    LinkMicUserInfoPtr info = it.second;
    vm_->StopStream(info->uid);
    if (info->display) {
      RemoveDisplayFromContainer(info->display.get());
    }
  }
  linkmic_user_info_.clear();
  linkmic_user_uid_fifo_.clear();
  speaker_display_first_user_index_ = 0;
}
void ClassMainWindow::ModifyLinkMicResource() {
  auto model = vm_->GetClassMainWindowModel();

  std::vector<LinkMicUserInfoPtr> add_list;
  std::vector<LinkMicUserInfoPtr> remove_list;

  for (auto it = linkmic_user_info_.begin(); it != linkmic_user_info_.end();) {
    auto found = model.rtc_user_list.find(it->first);
    if (found == model.rtc_user_list.end()) {
      remove_list.push_back(it->second);
      it = linkmic_user_info_.erase(it);
    } else {
      ++it;
    }
  }

  for (auto it : model.rtc_user_list) {
    auto found = linkmic_user_info_.find(it.first);
    if (found == linkmic_user_info_.end()) {
      LinkMicUserInfoPtr info = std::make_shared<LinkMicUserInfo>();
      info->uid = it.second.uid;
      info->is_teacher = it.second.is_teacher;
      info->enable_video_stream = false;
      info->display = std::make_unique<QWidget>();
      info->display->setUpdatesEnabled(false);
      info->display->setMaximumSize(QSize(178, 99));
      info->display->setMinimumSize(QSize(178, 99));
      info->display_controls =
          std::make_unique<DisplayControls>(info->display.get(), this);
      info->display_controls->SetTeacherNameLabelEnable(false);
      UpdateDisplayControlsStatusName(info->display_controls.get(),
                                      it.second.uid, it.second.user_name,
                                      it.second.is_teacher);
      info->display_controls->SetAudioMute(it.second.audio_mute);
      info->display_controls->SetVideoMute(it.second.video_mute);
      info->display_controls->SetBottomVisable(false);

      auto user_item = ui->userListWidget->GetUserItemWidgetByUserId(info->uid);
      {
        // 显示成员列表音视频状态
        if (user_item) {
          user_item->SetCameraStatus(!it.second.video_mute);
          user_item->SetMicStatus(!it.second.audio_mute);
        }
      }

      // info->display_controls->SetActiveSpeaker(it.second.active_speaker);

      add_list.push_back(info);
    }
  }

  // TODO 判断哪些不用显示
  for (auto info : remove_list) {
    // 清理下线用户资源
    if (info->enable_video_stream) {
      vm_->StopStream(info->uid);
    }
    if (info->display) {
      RemoveDisplayFromContainer(info->display.get());
      info->display_controls.reset();
      info->display.reset();
    }

    // 重新计算顶部首个显示用户index
    for (size_t i = 0; i < linkmic_user_uid_fifo_.size(); ++i) {
      const std::string& uid = linkmic_user_uid_fifo_[i];
      if (uid == info->uid) {
        linkmic_user_uid_fifo_.erase(linkmic_user_uid_fifo_.begin() + i);
        if (i <= speaker_display_first_user_index_) {
          --speaker_display_first_user_index_;
        }
        if (speaker_display_first_user_index_ < 0)
          speaker_display_first_user_index_ = 0;
        break;
      }
    }
  }
  for (auto info : add_list) {
    linkmic_user_uid_fifo_.push_back(info->uid);
    linkmic_user_info_[info->uid] = info;
  }

  // 子屏处理
  UpdateMainDisplayContainerSubDisplay();
}
void ClassMainWindow::OnVMUpdate(int32_t filed) {
  auto model = vm_->GetClassMainWindowModel();

  if (filed & ClassMainWindowField_ScreenShareStart ||
      filed & ClassMainWindowField_ScreenShareStop) {
    bool start = filed & ClassMainWindowField_ScreenShareStart;
    if (student_invited_confirm_dialog_) {
      queued_command_.push(
          [this, start]() { OnMainUIHandleSharedScreen(start); });
    } else {
      OnMainUIHandleSharedScreen(start);
    }
  }

  // rtc连麦用户状态变化, 比如闭麦，开关摄像头等
  if (filed & ClassMainWindowFiled_UpdateRtcUserState) {
    for (auto it : model.rtc_user_list) {
      auto info = it.second;
      DisplayControls* display_controls = GetDisplayControlsByUID(info.uid);
      if (display_controls) {
        UpdateDisplayControlsStatusName(display_controls, info.uid,
                                        info.user_name, info.is_teacher);
        display_controls->SetAudioMute(info.audio_mute);
        display_controls->SetVideoMute(info.video_mute);
        auto user_item = ui->userListWidget->GetUserItemWidgetByUserId(info.uid);
        {
          // 显示成员列表音视频状态
          if (user_item) {
            user_item->SetCameraStatus(!info.video_mute);
            user_item->SetMicStatus(!info.audio_mute);         
          }
        }
        // display_controls->SetActiveSpeaker(info.active_speaker);
      } else {
        // 用户不存在，可能添加删除UI列表状态有问题
        assert(0);
      }
    }
  }

  // rtc连麦用户列表变化
  if (filed & ClassMainWindowField_UpdateRtcUserList) {
    // 根据最新的列表, 显示
    if (model.rtc_user_list.empty()) {
      OnMainUIHandleLinkmic(false);
      if (model.role == ClassRoleEnum_Teacher) {
        std::vector<std::string> layout_user_ids;
        layout_user_ids.push_back(model.user_id);
        vm_->SetLayout(layout_user_ids, RtcLayoutModel::ONE_GRID);       
      }
    } else {
      OnMainUIHandleLinkmic(true);
      ModifyLinkMicResource();
    }
  }

  if (filed & ClassMainWindowField_ShowMediaControl) {
    if (!model.show_media_control && model.role == ClassRoleEnum_Student) {
      student_controls_->UpdateLinkmicStatus(false);
    }
  }

  if (filed & ClassMainWindowField_ZoomMode) {
    if (model.zoom_mode == true) {
      whiteboard_display_display_controls_->UpdateZoomBtnStatus(true);
      whiteboard_display_display_controls_->SetBottomVisable(true);
      rtc_preview_display_display_controls_->SetVideoVisable(false);
      rtc_preview_display_display_controls_->SetAudioVisable(false);
      rtc_preview_display_display_controls_->SetBottomVisable(false);
      rtc_preview_display_display_controls_->UpdateZoomBtnStatus(false);
      live_display_display_controls_->SetBottomVisable(false);
      live_display_display_controls_->UpdateZoomBtnStatus(false);
    } else {
      whiteboard_display_display_controls_->UpdateZoomBtnStatus(false);
      whiteboard_display_display_controls_->SetBottomVisable(false);
      rtc_preview_display_display_controls_->SetVideoVisable(true);
      rtc_preview_display_display_controls_->SetAudioVisable(true);
      rtc_preview_display_display_controls_->SetBottomVisable(true);
      rtc_preview_display_display_controls_->UpdateZoomBtnStatus(true);
      live_display_display_controls_->SetBottomVisable(true);
      live_display_display_controls_->UpdateZoomBtnStatus(true);
    }
    ZoomSwitchDisplay();
  }

  if (filed & ClassMainWindowField_LiveStarted) {
    bool zoom = model.zoom_mode;
    vm_->StopLivePlay();
    live_display_display_controls_->SetVideoMute(true);
    vm_->StartLivePlay((void*)live_display_->winId());
  }

  if (filed & ClassMainWindowField_LiveStopped) {
    if (model.role != ClassRoleEnum::ClassRoleEnum_Student) {
      return;
    }

    vm_->StopLivePlay();
    vm_->StopRtcPreview();
    vm_->LeaveRTC();
    ConfirmDialog dialog(GetTopParentWidget(this));
    DialogParam param;
    param.title = QTranslate("InteractiveClass.TipsTitle");
    param.content = QTranslate("InteractiveClass.ClassHasEnd");
    param.type = DialogTypeConfirm;
    param.icon_type = DialogIconTypeInfo;
    param.btn_vec = {QTranslate("Rooms.Conf.Confirm")};
    dialog.SetDialogParam(param);
    bool accept = dialog.exec() == QDialog::Accepted;

    vm_->LeaveRoom();
  }

  if (filed & ClassMainWindowField_ConfStartTime) {
    
    ui->titleWidget->UpdateStartTime(model.class_started, model.start_time);
    ui->titleWidget->UpdateTitle(model.title);
    BottomWidget* teacher_widget = static_cast<BottomWidget*>(bottom_widget_);
    if (teacher_widget) {
      teacher_widget->UpdateInClassStatus(model.class_started);
    }
  }

  if (filed & ClassMainWindowField_NetworkLatency) {
    ui->titleWidget->UpdateNetwork(model.latency, model.bitrate);
  }

  if (filed & ClassMainWindowField_RtcInvited) {
    if (model.role == ClassRoleEnum::ClassRoleEnum_Student &&
        (student_ui_state_ == k_studentuistate_linkmic0_shared0 ||
         student_ui_state_ == k_studentuistate_linkmic0_shared1)) {
      if (student_ui_state_ == k_studentuistate_linkmic0_shared0) {
        ReplaceDisplay(live_display_, rtc_preview_display_);
      }

      // 打开预览
      rtc_preview_display_display_controls_->SetVideoMute(true);
      vm_->StopRtcPreview();
      vm_->StartRtcPreview((void*)rtc_preview_display_->winId());


      // 弹出对话框
      ConfirmDialog dialog(GetTopParentWidget(this));
      DialogParam param;
      param.title = QTranslate("InteractiveClass.ReceiveLinkMic")
                        .arg(QString::fromStdString(vm_->GetCaller().user_id));
      param.content = QTranslate("InteractiveClass.CommunicateWithTeacher");
      param.type = DialogTypeOkCancel;
      param.icon_type = DialogIconTypeFailed;
      param.btn_vec = {QTranslate("InteractiveClass.Accept"),
                       QTranslate("InteractiveClass.Reject")};
      dialog.SetDialogParam(param);

      student_invited_confirm_dialog_ = &dialog;
      bool accept = dialog.exec() == QDialog::Accepted;
      student_invited_confirm_dialog_ = nullptr;

      if (student_ui_state_ == k_studentuistate_linkmic0_shared0) {
        ReplaceDisplay(rtc_preview_display_, live_display_);
      }

      // 执行缓冲起的命令
      while (!queued_command_.empty()) {
        queued_command_.front()();
        queued_command_.pop();
      }

      if (accept && vm_->GetRtcStatus() == RtcStatusEnum_Invited) {
        vm_->StopLivePlay();
        vm_->ApproveLinkMic();
        vm_->JoinChannel();
        student_controls_->UpdateLinkmicStatus(true);
      } else {
        vm_->RejectLinkMic();
        vm_->StopRtcPreview();
        // 拒绝后，无法接收到自己的拒绝通知，需要主动刷新成员列表
        ui->userListWidget->LoadUserList();
      }
    }
  }
}

void ClassMainWindow::ShowClassRoomWindow() { show(); }

void ClassMainWindow::UpdateClassMetaInfo(const ClassroomMetaInfo& meta_info) {
  vm_->UpdateClassroomId(meta_info.class_room_id);
  vm_->UpdateRole(meta_info.role);
  vm_->UpdateUserId(meta_info.user_id);
  vm_->UpdateType(meta_info.type);
  ui->titleWidget->UpdateClassMetaInfo(meta_info);
  if (meta_info.role == ClassRoleEnum_Student) {
    rtc_screen_display_->setUpdatesEnabled(false);
    live_display_display_controls_->SetUserName(
        QString::fromStdString(vm_->GetTeacherUserId()));
  } else {
    rtc_preview_display_display_controls_->SetUserName(QTranslate("InteractiveClass.TeacherSelfTitle"));
  }

  vm_->UpdateMainDisplayContentType(
      DisplayContentType::DisplayContentType_WhiteBoard);
  ui->mainDisplayContainer->SetMode(
      MainDisplayMode::MainDisplayMode_BigDisplayMode);

  ui->mainDisplayContainer->SetMainDisplay(whiteboard_display_);

  InitUserListWidget();
  InitBottomWidget();
  InitMedia();
  AddChatWidget(meta_info);


  QTimer::singleShot(100, [this]() {
    InitWhiteBoard();
    InitScreenSreamWidget();
  });

}
void ClassMainWindow::InitUserListWidget() {
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();
  //获取房间成员列表
  ui->userListWidget->SetRoomId(model.class_room_id);
  ui->userListWidget->UpdateClassRole((ClassRoleEnum)model.role);
  ui->userListWidget->UpdateUserId(model.user_id);
  ui->userListWidget->LoadUserList();
}

void ClassMainWindow::InitBottomWidget() {
  if (bottom_widget_) {
    return;
  }
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Teacher) {
    BottomWidget* teahcer_widget = new BottomWidget(ui->bottomContainer);
    teahcer_widget->UpdateClassroomInfo(model.class_room_id, model.user_id);
    bottom_widget_ = teahcer_widget;
    ui->bottomLayout->addWidget(bottom_widget_);
    bottom_widget_->setVisible(true);
    recording_tips_dialog_ = new RecordingTipsDialog(this);
    recording_tips_dialog_->setVisible(false);


    connect(teahcer_widget, &BottomWidget::SignalMuteLocalAudio,
            rtc_preview_display_display_controls_.get(),
            &DisplayControls::SetAudioMute);
    connect(teahcer_widget, &BottomWidget::SignalMuteLocalVideo, this,
            &ClassMainWindow::SetLocalPreviewVideoMute);
    connect(this, &ClassMainWindow::NotifyChangeCameraMuteButtonStatus,
            teahcer_widget,
            &BottomWidget::OnNotifyChangeCameraMuteButtonStatus);
    connect(teahcer_widget, &BottomWidget::NotifyRecordingState,
            recording_tips_dialog_,
            &RecordingTipsDialog::OnNotifyRecodingState);

    teahcer_widget->OnAutoRecordChanged(true);

  } else {
    ui->controlBarContainer->setVisible(false);
    InitStudentControls();
  }
}

void ClassMainWindow::InitMedia() {
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();
  if (model.role == ClassRoleEnum_Teacher) {
    // 老师端
    // 开启预览
    AddDisplayToContainer(ui->smallDisplayContainer, rtc_preview_display_);
    vm_->UpdateSmallDisplayContentType(
        DisplayContentType::DisplayContentType_RtcPreview);
    rtc_preview_display_display_controls_->SetVideoMute(true);
    vm_->StartRtcPreview((void*)rtc_preview_display_->winId());
  } else {
    // 学生
    // 播放直播
    vm_->UpdateSmallDisplayContentType(
        DisplayContentType::DisplayContentType_Live);
    AddDisplayToContainer(ui->smallDisplayContainer, live_display_);
    live_display_display_controls_->SetVideoMute(true);
    vm_->TryStartLivePlay((void*)live_display_->winId());
  }
}

void ClassMainWindow::InitStudentControls() {
  StudentControls* widget = new StudentControls(ui->leftContainer, this);
  student_controls_.reset(widget);
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();
  student_controls_->UpdateClassRoomId(model.class_room_id);
  student_controls_->UpdateUserId(model.user_id);

  connect(widget, &StudentControls::SignalMuteLocalAudio,
          rtc_preview_display_display_controls_.get(),
          &DisplayControls::SetAudioMute);
  connect(widget, &StudentControls::SignalMuteLocalVideo, this,
          &ClassMainWindow::SetLocalPreviewVideoMute);
  connect(this, &ClassMainWindow::NotifyChangeCameraMuteButtonStatus, widget,
          &StudentControls::OnNotifyChangeCameraMuteButtonStatus);
}


void ClassMainWindow::InitThumbnailContainer() {
  thumbnail_container_ = new ThumbnailContainer(ui->scenceContainer);
  connect(thumbnail_container_, &ThumbnailContainer::NotifySizeChange,
          [this]() {
            QTimer::singleShot(0, [this]() { AdjustThumbnailContainer(); });
          });
  AdjustThumbnailContainer();
}

void ClassMainWindow::AdjustThumbnailContainer() {
  if (thumbnail_container_) {
    QSize sz_wb = ui->leftContainer->size();
    QPoint pos_wb = ui->leftContainer->pos();
    int32_t preview_width = thumbnail_container_->width();
    int32_t pos_x = pos_wb.x() + sz_wb.width() - preview_width;
    int32_t main_display_height = whiteboard_display_->height();
    thumbnail_container_->move(pos_x, sz_wb.height() - main_display_height);
    thumbnail_container_->resize(preview_width, main_display_height);
  }
}

void ClassMainWindow::InitScreenSreamWidget() {
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();

  if (model.role == ClassRoleEnum_Teacher) {
    InitScreenShareMask();
  }
}

void ClassMainWindow::InitWhiteBoard() {
  WId white_board_hwnd = whiteboard_display_->winId();
  QSize size = whiteboard_display_->size();
  vm_->CreateWhiteBoard((void*)white_board_hwnd);
  ClassMainWindowModel model = vm_->GetClassMainWindowModel();

  ThumbnailContainer* thumbnail =
      model.role == ClassRoleEnum_Student ? nullptr : thumbnail_container_;
  MainDisplayPreview* widget =
      new MainDisplayPreview(whiteboard_display_, thumbnail);
  main_preview_controls_ = widget;
  widget->SetWhiteBoardMaskVisible(false);

  widget->UpdateUserId(model.user_id);
  widget->UpdateRoomId(model.class_room_id);
  widget->UpdateUserRole(model.role);

  bool student = model.role == ClassRoleEnum_Student;
  if (student && widget && thumbnail_container_) {
    thumbnail_container_->setVisible(false);
  }
  widget->SetWhiteBoardMaskVisible(false);
  connect(widget, &MainDisplayPreview::NotifyWhiteBoardLoaded, this,
          &ClassMainWindow::OnNotifyWhiteBoardLoaded);

  AdjustPreviewControls();
}

// void ClassMainWindow::InitSmallWindowControls() {
//   CPreviewControlsWidget* widget =
//       new CPreviewControlsWidget(ui->smallDisplayContainer, this);
//   preview_controls_ = widget;
//   connect(widget, &CPreviewControlsWidget::NotifyZoom, this,
//           &ClassMainWindow::OnZoom);
// }


void ClassMainWindow::InitScreenShareMask() {
  if (screen_label_) {
    return;
  }

  auto adjust_size = [](QWidget* widget, const QSize& size) {
    int32_t fontsize = size.height() / 5 - 2;
    int32_t height = size.height() / 5 + 10;
    QString style_sheet = QString(
                              "font-family:\"lviconfont\"; font-size:%1px; "
                              "color:rgb(102, 102, 102);")
                              .arg(fontsize);
    widget->resize(height, height);
    widget->move((size.width() - height) / 2, (size.height() - height) / 2);

    widget->setStyleSheet(style_sheet);
  };

  screen_label_ = new QLabel(rtc_screen_display_);
  QSize sz = rtc_screen_display_->size();
  screen_label_->setVisible(true);

  IconFontHelper::Instance()->SetIcon(screen_label_, kShareScreenIcon);
  adjust_size(screen_label_, sz);
  auto event_filter = new QWidgetEventFilter;
  rtc_screen_display_->installEventFilter(event_filter);
  connect(event_filter, &QWidgetEventFilter::signalResize,
          [this, adjust_size](const QSize& size, const QSize& old_size) {
            adjust_size(screen_label_, size);
          });
}

void ClassMainWindow::OnZoom(bool zoom) { vm_->UpdateZoomMode(zoom); }

void ClassMainWindow::moveEvent(QMoveEvent* event) {
  QWidget::moveEvent(event);
  AdjustPreviewControls();
}

void ClassMainWindow::resizeEvent(QResizeEvent* event) {
  QWidget::resizeEvent(event);
  QSize sz = ui->leftContainer->size();
  if (sz.width() > 0 && sz.height() > 0 && vm_) {
    vm_->UpdateWhiteBoard(sz.width(), sz.height());
  }
  AdjustPreviewControls();
  AdjustThumbnailContainer();
}

bool ClassMainWindow::HitTestDrag(QPoint mouse_wnd_pox, long* result) {
#if defined(WIN32)
  if (mouse_wnd_pox.x() < -300 || mouse_wnd_pox.y() < -300) {
    *result = HTCLIENT;
    return true;
  }
  // calc diagonal line resize first
  if (mouse_wnd_pox.y() > 12 / 2 &&
      mouse_wnd_pox.y() < ui->titleWidget->height() && mouse_wnd_pox.x() > 0 &&
      mouse_wnd_pox.x() < ui->titleWidget->width() - 120) {
    *result = HTCAPTION;
    return true;
  } else if (mouse_wnd_pox.y() <= 12 && mouse_wnd_pox.x() <= 12) {
    *result = HTTOPLEFT;
    return true;
  } else if (mouse_wnd_pox.y() <= 12 &&
             mouse_wnd_pox.x() >= this->width() - 12) {
    *result = HTTOPRIGHT;
    return true;
  } else if (mouse_wnd_pox.y() >= this->height() - 12 &&
             mouse_wnd_pox.x() >= this->width() - 12) {
    *result = HTBOTTOMRIGHT;
    return true;
  } else if (mouse_wnd_pox.y() >= this->height() - 12 &&
             mouse_wnd_pox.x() <= 12) {
    *result = HTBOTTOMLEFT;
    return true;
  }

  if (mouse_wnd_pox.x() <= 12) {
    *result = HTLEFT;


    return true;
  } else if (mouse_wnd_pox.x() >= this->width() - 12) {
    *result = HTRIGHT;

    return true;
  } else if (mouse_wnd_pox.y() >= this->height() - 12) {
    *result = HTBOTTOM;
    return true;
  } else if (mouse_wnd_pox.y() <= 12 / 2 &&
             !(windowState() & Qt::WindowMaximized)) {
    *result = HTTOP;
    return true;
  } else {
    QWidget* childAtWidget = childAt(mouse_wnd_pox);
    if (ui->titleWidget == childAtWidget) {
      *result = HTCAPTION;
      return true;
    }
  }
#endif

  return false;
}

bool ClassMainWindow::nativeEvent(const QByteArray& eventType,
                                  void* message,
                                  long* result) {
#if defined(WIN32)
  MSG* msg = (MSG*)message;
  switch (msg->message) {
    case WM_NCCALCSIZE: {
      // this kills the window frame and title bar we added with
      // WS_THICKFRAME and WS_CAPTION
      *result = 0;
      return true;
    }

    case WM_SYSCOMMAND: {
      return false;
    }

    case WM_NCLBUTTONDBLCLK:
      if (isMaximized()) {
        showNormal();
      } else {
        showMaximized();
      }
      return true;

    case WM_NCHITTEST: {
      std::string version = QT_VERSION_STR;
      int32_t scale_factor = 1;
      // pos in current resolution
      int x_param = GET_X_LPARAM(msg->lParam);
      int y_param = GET_Y_LPARAM(msg->lParam);
      // translate to pos in window
      int xPos = x_param / scale_factor - this->x();
      int yPos = y_param / scale_factor - this->y();
      if (version == "5.14.0") {
        const int mouse_pos_fix = 7;
        xPos += mouse_pos_fix;
      }
      if (HitTestDrag(QPoint(xPos, yPos), result)) {
        return true;
      }
    }

    break;
  }
#endif

  return false;
}

void ClassMainWindow::AddChatWidget(const ClassroomMetaInfo& meta_info) {
  if (chat_wid_) {
    return;
  }
  chat_wid_ = new ChatWidget(ui->chatWidgetContainer);
  ui->chatWidgetContainer->layout()->addWidget(chat_wid_);

  ChatParam tmp;
  tmp.role = meta_info.role;
  tmp.type = meta_info.type;
  tmp.class_room_id = meta_info.class_room_id;
  tmp.user_id = meta_info.user_id;
  chat_wid_->UpdateChatInfo(tmp);
  chat_wid_->show();
}

void ClassMainWindow::InitTableWidget() {
  ui->userListWidget->setVisible(false);
  ui->chatWidgetContainer->setVisible(true);
  btn_group_ = new QButtonGroup(ui->tabWidget);
  btn_group_->addButton(ui->studentBtn, kConstStudentBtnIndex);
  btn_group_->addButton(ui->chatBtn, kConstChatBtnIndex);
  connect(btn_group_, SIGNAL(buttonClicked(int)), this,
          SLOT(OnGroupButtonClicked(int)));
  new_msg_notify_ = new QLabel(ui->tabWidget);
  new_msg_notify_->setFixedSize(8, 8);
  new_msg_notify_->move(260, 18);
  new_msg_notify_->setStyleSheet("border-radius:4px; background:red;");
  new_msg_notify_->setVisible(false);
  connect(ui->userListWidget, &UserListWidget::SignalListCountChange, this,
          &ClassMainWindow::OnUserListCountChange);
}

void ClassMainWindow::OnGroupButtonClicked(int btn_id) {
  switch (btn_id) {
    case kConstChatBtnIndex:
      ui->userListWidget->setVisible(false);
      ui->chatWidgetContainer->setVisible(true);
      if (new_msg_notify_) {
        new_msg_notify_->setVisible(false);
      }
      break;
    case kConstStudentBtnIndex:
      ui->userListWidget->setVisible(true);
      ui->chatWidgetContainer->setVisible(false);
      break;
    default:
      break;
  }
}

void ClassMainWindow::OnNewMsg() {
  if (ui->chatWidgetContainer->isVisible() && new_msg_notify_) {
    new_msg_notify_->setVisible(true);
  }
}

void ClassMainWindow::OnUserListCountChange(int32_t count) {
  QString text = QTranslate("InteractiveClass.StudentCount").arg(count);
  ui->studentBtn->setText(text);
}


void ClassMainWindow::OnListItemChange(const QString& user_id, bool link) {
  auto model = vm_->GetClassMainWindowModel();
}

void ClassMainWindow::OnNotifyWhiteBoardLoaded() {
  if (main_preview_controls_) {
    MainDisplayPreview* control =
        dynamic_cast<MainDisplayPreview*>(main_preview_controls_);
    control->UpdateWhiteBoardSize(ui->leftContainer->size());
    auto model = vm_->GetClassMainWindowModel();
    if (model.role == ClassRoleEnum_Student) {
      control->UpdatePointerMode();
    } else {
      control->SetWhiteBoardMaskVisible(true);
    }
  }
}
void ClassMainWindow::OnWhiteBoardDisplayUpdateSize() {
  QSize size = whiteboard_display_->size();

  vm_->UpdateWhiteBoard(size.width(), size.height() );
}

void ClassMainWindow::OnCloseBtnClicked() {
  auto model = vm_->GetClassMainWindowModel();

  if (model.role == ClassRoleEnum_Teacher && model.start_time > 0) {
    ConfirmDialog dialog(GetTopParentWidget(this));
    DialogParam param;
    param.title = QTranslate("InteractiveClass.EndClassConfirm");
    param.content = QTranslate("InteractiveClass.EndClassNotify");
    param.type = DialogTypeOkCancel;
    param.icon_type = DialogIconTypeFailed;
    param.btn_vec = {QTranslate("OK"), QTranslate("Cancel")};
    dialog.SetDialogParam(param);
    if (dialog.exec() != QDialog::Accepted) {
      return;
    }
    vm_->StopClass();
  } else {
    vm_->SetExitting();
    vm_->StopLivePlay();
    vm_->StopRtcPreview();
    vm_->LeaveRoom();
  }
}

void ClassMainWindow::OnAutoRecordChanged(bool) {}


void ClassMainWindow::OnConfLayoutChanged(int32_t layout) {
  // 切换视图模式
  auto model = vm_->GetClassMainWindowModel();

  if (linkmic_user_info_.empty()) {

    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowSuccessToast(QTranslate("InteractiveClass.NotLinkmic"));
    return;
  }

  if (model.main_display_mode ==
      MainDisplayMode::MainDisplayMode_BigDisplayMode) {
    // 连麦下不该出现这种case
    assert(0);
    return;
  }

  switch (layout) {
    case ConfLayoutSpeaker: {
      ui->mainDisplayContainer->ClearAllSubDisplay();
      ui->mainDisplayContainer->SetMode(MainDisplayMode_SpeakerMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_SpeakerMode);
      speaker_display_first_user_index_ = 0;
      UpdateMainDisplayContainerSubDisplay();

    } break;
    case ConfLayoutGrid: {
      ui->mainDisplayContainer->ClearAllSubDisplay();
      ui->mainDisplayContainer->SetMode(MainDisplayMode_GridMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_GridMode);
      speaker_display_first_user_index_ = 0;
      UpdateMainDisplayContainerSubDisplay();

    } break;
    default:

      break;
  }

  AdjustPreviewControls();
}

void ClassMainWindow::OnMainDisplayContainerNotifyUpdateTopWindowLayout() {
  UpdateMainDisplayContainerSubDisplay();
}
void ClassMainWindow::OnMainDisplayContainerPageUp() {
  MainDisplayContainerConfig config = ui->mainDisplayContainer->GetConfig();
  uint32_t oldspeaker_display_first_user_index =
      speaker_display_first_user_index_;
  speaker_display_first_user_index_ -= config.max_sub_display_count;
  if (speaker_display_first_user_index_ < 0)
    speaker_display_first_user_index_ = 0;

  if (oldspeaker_display_first_user_index !=
      speaker_display_first_user_index_) {
    UpdateMainDisplayContainerSubDisplay();
  }
}
void ClassMainWindow::OnMainDisplayContainerPageDown() {
  MainDisplayContainerConfig config = ui->mainDisplayContainer->GetConfig();
  int next_index =
      speaker_display_first_user_index_ + config.max_sub_display_count;

  if (next_index < linkmic_user_uid_fifo_.size()) {
    speaker_display_first_user_index_ = next_index;
    UpdateMainDisplayContainerSubDisplay();
  } else {
    // 无需翻页
  }
}

void ClassMainWindow::AdjustPreviewControls() {
  if (!vm_) {
    return;
  }
  auto model = vm_->GetClassMainWindowModel();

  if (!main_preview_controls_) {
    return;
  }

  if ((windowState() & Qt::WindowMinimized) || model.is_screen_sharing ||
      (model.role == ClassRoleEnum_Teacher &&
       (model.main_display_mode == MainDisplayMode_GridMode ||
        model.main_display_content_type != DisplayContentType_WhiteBoard))) {
    if (thumbnail_container_) {
      thumbnail_container_->hide();
    }

    if (main_preview_controls_) {
      main_preview_controls_->hide();
    }
    return;
  }

  if (isVisible()) {
    if (model.role == ClassRoleEnum_Teacher) {
      if (thumbnail_container_) {
        thumbnail_container_->show();
      }
    }

    QRect preview_rect;
    preview_rect = ui->leftContainer->geometry();
    preview_rect.moveTopLeft(
        ui->leftContainer->parentWidget()->mapToGlobal(preview_rect.topLeft()));

    if (main_preview_controls_) {
      main_preview_controls_->resize(preview_rect.width(),
                                     preview_rect.height());
      main_preview_controls_->move(preview_rect.x(), preview_rect.y());

      main_preview_controls_->show();
      main_preview_controls_->raise();
    }
  }
}
void ClassMainWindow::OnPlayerVideoFirstFrameRender() {
  live_display_display_controls_->SetVideoMute(false);
}
void ClassMainWindow::OnRtcFirstLocalVideoFrameDrawn(int32_t width,
                                                     int32_t height,
                                                     int32_t elapsed) {
  rtc_preview_display_display_controls_->SetVideoMute(false);
}
void ClassMainWindow::SetLocalPreviewVideoMute(const bool mute) {
  rtc_preview_display_display_controls_->SetVideoMute(mute);
  if (mute) {
    vm_->StopRtcPreview();
    vm_->MuteLocalVideo(true);
  } else {
    vm_->StopRtcPreview();
    vm_->StartRtcPreview((void*)rtc_preview_display_->winId());
    vm_->MuteLocalVideo(false);
  }

  // 通知相关按钮状态更新
  emit NotifyChangeCameraMuteButtonStatus(mute);
}
void ClassMainWindow::UpdateDisplayControlsStatusName(
    DisplayControls* widget,
    const std::string uid,
    const std::string user_name,
    const bool is_teacher) {
  if (!widget) return;

  QString name;

  if (!user_name.empty()) {
    name = QString::fromUtf8(user_name.c_str());
  } else {
    name = QString::fromUtf8(uid.c_str());
  }

  if (is_teacher) {
    name += QString::fromStdWString(L"(") +
            QTranslate("InteractiveClass.TeacherTitle") +
            QString::fromStdWString(L")");
  }

  widget->SetUserName(name);
}
void ClassMainWindow::ZoomSwitchDisplay() {
  auto model = vm_->GetClassMainWindowModel();

  DisplayContentType main_content_type = model.main_display_content_type;
  DisplayContentType small_content_type = model.small_display_content_type;

  QWidget* main_display = GetDisplayByDisplayContentType(main_content_type);
  QWidget* small_display = GetDisplayByDisplayContentType(small_content_type);

  QStackedWidget* main_parent =
      dynamic_cast<QStackedWidget*>(main_display->parentWidget());
  QStackedWidget* small_parent =
      dynamic_cast<QStackedWidget*>(small_display->parentWidget());

  AddDisplayToContainer(main_parent, small_display);
  AddDisplayToContainer(small_parent, main_display);

  vm_->UpdateMainDisplayContentType(small_content_type);
  vm_->UpdateSmallDisplayContentType(main_content_type);

  AdjustPreviewControls();

  QTimer::singleShot(1000, [this]() {
    MainDisplayPreview* control =
      dynamic_cast<MainDisplayPreview*>(main_preview_controls_);

    if (control) {
      control->UpdateScale();
    }
  });
  
}
void ClassMainWindow::OnMainUIHandleLinkmic(bool linkmic) {
  auto model = vm_->GetClassMainWindowModel();

  if (model.role == ClassRoleEnum::ClassRoleEnum_Teacher) {
    if (linkmic) {
      if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared0) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic1_shared0);
      } else if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared1) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic1_shared1);
      }
    } else {
      if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared0) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic0_shared0);
      } else if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared1) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic0_shared1);
      }
    }
  } else {
    if (linkmic) {
      if (student_ui_state_ == k_studentuistate_linkmic0_shared0) {
        OnChangeStudentUIState(k_studentuistate_linkmic1_shared0);
      } else if (student_ui_state_ == k_studentuistate_linkmic0_shared1) {
        OnChangeStudentUIState(k_studentuistate_linkmic1_shared1);
      }
    } else {
      if (student_ui_state_ == k_studentuistate_linkmic1_shared0) {
        OnChangeStudentUIState(k_studentuistate_linkmic0_shared0);
      } else if (student_ui_state_ == k_studentuistate_linkmic1_shared1) {
        OnChangeStudentUIState(k_studentuistate_linkmic0_shared1);
      }
    }
  }
  AdjustThumbnailContainer();
}
void ClassMainWindow::OnMainUIHandleSharedScreen(bool start_shared_screen) {
  auto model = vm_->GetClassMainWindowModel();

  if (model.role == ClassRoleEnum::ClassRoleEnum_Teacher) {
    if (start_shared_screen) {
      if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared0) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic0_shared1);
      } else if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared0) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic1_shared1);
      } else {
        // 重复触发, 不处理
      }
    } else {
      if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared1) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic0_shared0);
      } else if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared1) {
        OnChangeTeacherUIState(k_teacheruistate_linkmic1_shared0);
      } else {
        // 重复触发, 不处理
      }
    }

  } else {
    if (start_shared_screen) {
      if (student_ui_state_ == k_studentuistate_linkmic0_shared0) {
        OnChangeStudentUIState(k_studentuistate_linkmic0_shared1);
      } else if (student_ui_state_ == k_studentuistate_linkmic1_shared0) {
        OnChangeStudentUIState(k_studentuistate_linkmic1_shared1);
      } else {
        // 重复触发, 不处理
      }
    } else {
      if (student_ui_state_ == k_studentuistate_linkmic0_shared1) {
        OnChangeStudentUIState(k_studentuistate_linkmic0_shared0);
      } else if (student_ui_state_ == k_studentuistate_linkmic1_shared1) {
        OnChangeStudentUIState(k_studentuistate_linkmic1_shared0);
      } else {
        // 重复触发, 不处理
      }
    }
  }
}

bool ClassMainWindow::ReplaceDisplay(QWidget* to, QWidget* from) {
  DisplayContentType to_type = GetDisplayContentTypeByDisplay(to);
  DisplayContentType from_type = GetDisplayContentTypeByDisplay(from);
  auto model = vm_->GetClassMainWindowModel();

  if (to_type == DisplayContentType::DisplayContentType_Unknown ||
      from_type == DisplayContentType::DisplayContentType_Unknown) {
    assert(0);
    return false;
  }

  if (model.main_display_content_type == to_type) {
    QStackedWidget* parent = RemoveDisplayFromContainer(to);
    assert(parent != NULL);
    AddDisplayToContainer(parent, from);
    vm_->UpdateMainDisplayContentType(from_type);
  } else if (model.small_display_content_type == to_type) {
    QStackedWidget* parent = RemoveDisplayFromContainer(to);
    assert(parent != NULL);
    AddDisplayToContainer(parent, from);
    vm_->UpdateSmallDisplayContentType(from_type);
  } else {
    assert(0);
    return false;
  }

  return true;
}
bool ClassMainWindow::SetMainDisplayPreferContent(DisplayContentType type) {
  auto model = vm_->GetClassMainWindowModel();
  if (model.main_display_content_type == type) {
    // nothing to do
  } else if (model.small_display_content_type == type) {
    ZoomSwitchDisplay();
  } else {
    assert(0);
    return false;
  }

  return true;
}
void ClassMainWindow::OnChangeTeacherUIState(TeacherUIState next_state) {
  // 无需变化
  if (teacher_ui_state_ == next_state) return;

  auto model = vm_->GetClassMainWindowModel();

  assert(model.role == ClassRoleEnum::ClassRoleEnum_Teacher);

  if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared0) {
    if (next_state == k_teacheruistate_linkmic0_shared1) {
      ReplaceDisplay(whiteboard_display_, rtc_screen_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_SreenStream);
      SetLocalPreviewVideoMute(true);
      vm_->UpdateScreenShared(true);
      AdjustPreviewControls();
      teacher_ui_state_ = next_state;
    } else if (next_state == k_teacheruistate_linkmic1_shared0) {
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_SpeakerMode);

      vm_->UpdateMainDisplayMode(MainDisplayMode_SpeakerMode);
      teacher_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (teacher_ui_state_ == k_teacheruistate_linkmic0_shared1) {
    if (next_state == k_teacheruistate_linkmic0_shared0) {
      ReplaceDisplay(rtc_screen_display_, whiteboard_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      SetLocalPreviewVideoMute(false);
      vm_->UpdateScreenShared(false);
      AdjustPreviewControls();
      teacher_ui_state_ = next_state;
    } else if (next_state == k_teacheruistate_linkmic1_shared1) {
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_SpeakerMode);

      vm_->UpdateMainDisplayMode(MainDisplayMode_SpeakerMode);
      teacher_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared0) {
    if (next_state == k_teacheruistate_linkmic1_shared1) {
      ReplaceDisplay(whiteboard_display_, rtc_screen_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_SreenStream);
      SetLocalPreviewVideoMute(true);
      vm_->UpdateScreenShared(true);
      AdjustPreviewControls();
      teacher_ui_state_ = next_state;
    } else if (next_state == k_teacheruistate_linkmic0_shared0) {
      CleanupLinkmicResource();
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_BigDisplayMode);

      vm_->UpdateMainDisplayMode(MainDisplayMode_BigDisplayMode);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      teacher_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (teacher_ui_state_ == k_teacheruistate_linkmic1_shared1) {
    if (next_state == k_teacheruistate_linkmic0_shared1) {
      CleanupLinkmicResource();
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_BigDisplayMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_BigDisplayMode);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_SreenStream);
      teacher_ui_state_ = next_state;
    } else if (next_state == k_teacheruistate_linkmic1_shared0) {
      ReplaceDisplay(rtc_screen_display_, whiteboard_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      SetLocalPreviewVideoMute(false);
      vm_->UpdateScreenShared(false);
      AdjustPreviewControls();
      teacher_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else {
    // 未定义的状态
    assert(0);
  }
}
void ClassMainWindow::OnChangeStudentUIState(StudentUIState next_state) {
  auto model = vm_->GetClassMainWindowModel();

  assert(model.role == ClassRoleEnum::ClassRoleEnum_Student);

  if (student_ui_state_ == k_studentuistate_linkmic0_shared0) {
    if (next_state == k_studentuistate_linkmic0_shared1) {
      ReplaceDisplay(whiteboard_display_, rtc_preview_display_);
      rtc_preview_display_display_controls_->SetVideoMute(true);
      SetMainDisplayPreferContent(DisplayContentType::DisplayContentType_Live);
      student_ui_state_ = next_state;
    } else if (next_state == k_studentuistate_linkmic1_shared0) {
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_SpeakerMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_SpeakerMode);
      ReplaceDisplay(live_display_, rtc_preview_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      student_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (student_ui_state_ == k_studentuistate_linkmic0_shared1) {
    if (next_state == k_studentuistate_linkmic0_shared0) {
      ReplaceDisplay(rtc_preview_display_, whiteboard_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      student_ui_state_ = next_state;
    } else if (next_state == k_studentuistate_linkmic1_shared1) {
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_SpeakerMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_SpeakerMode);
      ReplaceDisplay(live_display_, rtc_screen_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_SreenStream);
      vm_->StartShowScreenStream((void*)rtc_screen_display_->winId());
      student_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (student_ui_state_ == k_studentuistate_linkmic1_shared0) {
    if (next_state == k_studentuistate_linkmic0_shared0) {
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_BigDisplayMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_BigDisplayMode);
      CleanupLinkmicResource();
      ReplaceDisplay(rtc_preview_display_, live_display_);
      live_display_display_controls_->SetVideoMute(true);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      vm_->StopLivePlay();
      vm_->StartLivePlay((void*)live_display_->winId());
      student_ui_state_ = next_state;
    } else if (next_state == k_studentuistate_linkmic1_shared1) {
      ReplaceDisplay(whiteboard_display_, rtc_screen_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_SreenStream);
      vm_->StartShowScreenStream((void*)rtc_screen_display_->winId());
      student_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else if (student_ui_state_ == k_studentuistate_linkmic1_shared1) {
    if (next_state == k_studentuistate_linkmic1_shared0) {
      vm_->StopShowScreenStream();
      ReplaceDisplay(rtc_screen_display_, whiteboard_display_);
      SetMainDisplayPreferContent(
          DisplayContentType::DisplayContentType_WhiteBoard);
      student_ui_state_ = next_state;
    } else if (next_state == k_studentuistate_linkmic0_shared1) {
      vm_->StopShowScreenStream();
      ui->mainDisplayContainer->SetMode(
          MainDisplayMode::MainDisplayMode_BigDisplayMode);
      vm_->UpdateMainDisplayMode(MainDisplayMode_BigDisplayMode);
      CleanupLinkmicResource();
      ReplaceDisplay(rtc_screen_display_, live_display_);
      SetMainDisplayPreferContent(DisplayContentType::DisplayContentType_Live);
      live_display_display_controls_->SetVideoMute(true);
      vm_->StopLivePlay();
      vm_->StartLivePlay((void*)live_display_->winId());
      student_ui_state_ = next_state;
    } else {
      assert(0);
    }
  } else {
    // 未定义的状态
    assert(0);
  }
}