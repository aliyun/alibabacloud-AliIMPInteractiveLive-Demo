#include "display_controls.h"
#include "ui_display_controls.h"
#include "common/icon_font_helper.h"


DisplayControls::DisplayControls(QWidget* parent, QWidget* listen)
  : QDialog(parent, Qt::FramelessWindowHint | Qt::Tool),
  parent_(parent),
  ui_(new Ui::DisplayControls) {
  ui_->setupUi(this);
  setAttribute(Qt::WA_TranslucentBackground, true);
  setMouseTracking(true);

  filter_ = std::make_unique<QWidgetEventFilter>();
  listen->installEventFilter(filter_.get());

  connect(filter_.get(), &QWidgetEventFilter::signalMove, this, &DisplayControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalResize, this, &DisplayControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalQPaintEvent, this, &DisplayControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowActivate, this, &DisplayControls::OnAdjustControls);
  connect(filter_.get(), &QWidgetEventFilter::signalWindowDeactivate, this, &DisplayControls::OnAdjustControls);
  UpdateZoomBtnStatus(false);
  UpdateUserNameStatus(false);
  SetVideoVisable(false);
  InitIconFont();
}

DisplayControls::~DisplayControls() {
  disconnect(this);
}

void DisplayControls::OnAdjustControls() {
  if (windowState() & Qt::WindowMinimized) {
    this->hide();
    return;
  }

  if (parent_->isVisible()) {

    QRect cur_rect = this->rect();
    QRect par_rect = parent_->geometry();

    // move
    QPoint par_top_left = parent_->mapToGlobal(par_rect.topLeft());
    QPoint cur_top_left = this->mapToGlobal(cur_rect.topLeft());
    if (cur_top_left != par_top_left) {
      this->move(par_top_left.x(), par_top_left.y());
    }

    // resize
    if (cur_rect.width() != par_rect.width() || cur_rect.height() != par_rect.height()) {
      this->resize(par_rect.width(), par_rect.height());
    }

    // show
    if (this->isHidden()) {
      this->show();
      if (!this->isTopLevel()) {
        this->raise();
      }
    }
  }
  else {
    // hide
    if (!this->isHidden()) {
      this->hide();
    }
  }
}

void DisplayControls::SetUserName(const QString& user_name) {
  QString str_user_name = user_name;
  QFontMetrics font_width(ui_->nameLabel->font());
  int width = font_width.width(str_user_name);
  if (width >= 120) {
    str_user_name = font_width.elidedText(str_user_name, Qt::ElideRight, 120);
  }

  ui_->nameLabel->setText(str_user_name);
  ui_->teacherNameLabel->setText(str_user_name);
  UpdateUserNameStatus(true);
}

void DisplayControls::SetAudioMute(const bool mute) {
  if (mute) {
    IconFontHelper::Instance()->SetIcon(ui_->micStatus, kMicClose);
  }
  else {
    IconFontHelper::Instance()->SetIcon(ui_->micStatus, kMicOpen);
  }
  UpdateRedLine(ui_->micStatus, mute);
}

void DisplayControls::SetVideoMute(const bool mute) {
  if (mute) {
    ui_->noVideoLabel->setVisible(true);
    IconFontHelper::Instance()->SetIcon(ui_->noVideoLabel, kNoVideoStatus);
    IconFontHelper::Instance()->SetIcon(ui_->cameraStatus, kCameraClose);
    UpdateRedLine(ui_->cameraStatus, true);
    // 把背景颜色设置为灰色
    this->setStyleSheet(QString::fromUtf8("QWidget {\n""  background-color: #f0f0f0;\n""}"));
    ui_->widget_2->setStyleSheet(QString::fromUtf8("QWidget#widget_2{\n""  background-color: #f0f0f0;\n""}"));
    ui_->label->setVisible(false);
  }
  else {
    ui_->noVideoLabel->setVisible(false);
    IconFontHelper::Instance()->SetIcon(ui_->cameraStatus, kCameraOpen);
    UpdateRedLine(ui_->cameraStatus, false);
    // 把背景颜色设置为透明
    this->setStyleSheet(QString::fromUtf8("QWidget {\n""  background-color: transparent;\n""}"));
    ui_->widget_2->setStyleSheet(QString::fromUtf8(
      "QWidget#widget_2{\n"
      " background-color: qlineargradient(x1:0, y1:0, x2:0, y2:1, stop:0  rgba(255, 255, 255,0),stop:1 rgba(0, 0, 0, 255));\n"
    "}"));
    ui_->label->setVisible(teacher_name_label_enable_);
  }
}

void DisplayControls::SetActiveSpeaker(const bool active) {
  if (active) {
    IconFontHelper::Instance()->SetIcon(ui_->micStatus, kSpeaking);
  }
  else {
    IconFontHelper::Instance()->SetIcon(ui_->micStatus, kMicOpen);
  }
}


void DisplayControls::UpdateZoomBtnStatus(bool show){
  ui_->label->setVisible(teacher_name_label_enable_ && show);
}

void DisplayControls::SetBottomVisable(bool visable) {
  if (visable) {
    ui_->widget_2->setStyleSheet(QString::fromUtf8(
      "QWidget#widget_2{\n"
      " background-color: qlineargradient(x1:0, y1:0, x2:0, y2:1, stop:0  rgba(255, 255, 255,0),stop:1 rgba(0, 0, 0, 255));\n"
    "}"));  
  } else {
    ui_->widget_2->setStyleSheet(QString::fromUtf8("QWidget#widget_2 {\n""  background-color: transparent;\n""}"));
  }
}

void DisplayControls::UpdateUserNameStatus(bool show)
{
  if (name_lable_enable_)
    ui_->nameLabel->setVisible(show);

  if (teacher_name_label_enable_)
    ui_->teacherNameLabel->setVisible(show);
}

void DisplayControls::InitIconFont() {

}

void DisplayControls::UpdateRedLine(MultiIconLabel* label, bool show) {
  if (label == ui_->cameraStatus) {
    label->SetDelta(QPoint(-1, 0));
  }
  else if (label == ui_->micStatus) {
    label->SetDelta(QPoint(0, 0));
  }
  if (show) {
    QVector<IconData> icon_list;
    icon_list.push_back({ QChar(0xe7ca), "#FF5219" });
    label->SetMoreIconList(icon_list);
  }
  else {
    QVector<IconData> icon_list;
    label->SetMoreIconList(icon_list);
  }
}


void DisplayControls::on_zoomBtn_clicked() {
  zoom_ = !zoom_;
  emit NotifyZoom(zoom_);
}

void DisplayControls::SetTeacherNameLabelEnable(bool enable) {
  teacher_name_label_enable_ = enable;
  if (enable == false) {
    ui_->widget_3->setVisible(false);
    ui_->label->setVisible(teacher_name_label_enable_);
  }
}

void DisplayControls::SetNameLabelEnable(bool enable) {
  name_lable_enable_ = enable;
}

void DisplayControls::SetVideoVisable(bool visable) {
  ui_->cameraStatus->setVisible(visable);
  ui_->label->setVisible(teacher_name_label_enable_ && visable);
}

void DisplayControls::SetAudioVisable(bool visable) {
  ui_->micStatus->setVisible(visable);
  ui_->label->setVisible(teacher_name_label_enable_ && visable);
}