#include "main_display_container.h"
#include "ui_main_display_container.h"

#include "grid_display_widget.h"
#include "grid_display_item_widget.h"
#include "ui_grid_display_item_widget.h"
#include "ui_speaker_display_widget.h"
#include "ui_grid_display_widget.h"
#include "speaker_display_widget.h"

MainDisplayContainer::MainDisplayContainer(QWidget* parent)
    : ui(new Ui::MainDisplayContainer), QWidget(parent) {

  config_ = std::make_shared<MainDisplayContainerConfig>();
  ui->setupUi(this);
  ui->SpeakerDisplay->SetConfig(config_);
  ui->GridDisplay->SetConfig(config_);
  setAttribute(Qt::WidgetAttribute::WA_StyledBackground);
  connect(ui->SpeakerDisplay, &SpeakerDisplayWidget::signalNotifyUpdateTopWindowLayout, this, &MainDisplayContainer::OnSpeakerDisplayWidgetNotifyUpdateTopWindowLayout);
  connect(ui->SpeakerDisplay->ui->leftBtn, &QPushButton::clicked, this, &MainDisplayContainer::OnSpeakerDisplayWidgetNotifyPageUp);
  connect(ui->SpeakerDisplay->ui->rightBtn, &QPushButton::clicked, this, &MainDisplayContainer::OnSpeakerDisplayWidgetNotifyPageDown);
  connect(ui->GridDisplay->ui->leftBtn, &QPushButton::clicked, this, &MainDisplayContainer::OnGridDisplayWidgetNotifyPageUp);
  connect(ui->GridDisplay->ui->rightBtn, &QPushButton::clicked, this, &MainDisplayContainer::OnGridDisplayWidgetNotifyPageDown);
}

MainDisplayContainer::~MainDisplayContainer() {
  delete ui;
}

void MainDisplayContainer::AddSubDisplay(QWidget* widget) {
  GridDisplayItemWidget* item = new GridDisplayItemWidget();
  AddDisplayToContainer(item->ui->VideoWidget, widget);

  switch (ui->stackedWidget->currentIndex()) {
    case MainDisplayMode::MainDisplayMode_SpeakerMode: {
      widget->setMaximumSize(QSize(178, 99));
      widget->setMinimumSize(QSize(178, 99));
      ui->SpeakerDisplay->AddTopWidget(item);
    } break;
    case MainDisplayMode::MainDisplayMode_GridMode: {
      widget->setMaximumSize(QSize(100000, 100000));
      widget->setMinimumSize(QSize(0, 0));
      ui->GridDisplay->AddWidget(item);
    } break;
    default:
      break;
  }
}

void MainDisplayContainer::ClearAllSubDisplay() {

  ui->SpeakerDisplay->ClearAllWidget();
  ui->GridDisplay->ClearAllWidget();
}

void MainDisplayContainer::SetMainDisplay(QWidget* widget) {

  ClearMainDisplay();

  switch (ui->stackedWidget->currentIndex()) {
    case MainDisplayMode::MainDisplayMode_BigDisplayMode: {
      AddDisplayToContainer(ui->BigDisplayContain, widget);
    } break;
    case MainDisplayMode::MainDisplayMode_SpeakerMode: {
      AddDisplayToContainer(ui->SpeakerDisplay->ui->bigDisplayContainer, widget);
    } break;
    default:
      AddDisplayToContainer(&main_display_container_, widget);
      break;
  }
}
QWidget* MainDisplayContainer::ClearMainDisplay()
{
  QWidget* main_display = nullptr;

  switch (ui->stackedWidget->currentIndex()) {
  case MainDisplayMode::MainDisplayMode_BigDisplayMode: {
    if (ui->BigDisplayContain->count()) {
      main_display = ui->BigDisplayContain->widget(0);
    }
  } break;
  case MainDisplayMode::MainDisplayMode_SpeakerMode: {
    if (ui->SpeakerDisplay->ui->bigDisplayContainer->count()) {
      main_display = ui->SpeakerDisplay->ui->bigDisplayContainer->widget(0);
    }
  } break;
  default:
    if (main_display_container_.count()) {
      main_display = main_display_container_.widget(0);
    }
    break;
  }

  if (main_display) {
    RemoveDisplayFromContainer(main_display);
  }

  return main_display;
}
void MainDisplayContainer::SetMode(MainDisplayMode mode_type) {

  QWidget* main_display = ClearMainDisplay();
  ui->stackedWidget->setCurrentIndex(mode_type);
  if (main_display) {
    SetMainDisplay(main_display);
  }
}

void MainDisplayContainer::UpdateLayout() {

  
}

void MainDisplayContainer::SetConfig(const MainDisplayContainerConfig& config)
{
  *config_ = config;
  ui->SpeakerDisplay->SetConfig(config_);
  ui->GridDisplay->SetConfig(config_);
}
MainDisplayContainerConfig MainDisplayContainer::GetConfig()
{
  if (ui->stackedWidget->currentIndex() == MainDisplayMode::MainDisplayMode_SpeakerMode) {
    ui->SpeakerDisplay->UpdateConfig();
  }
  else if (ui->stackedWidget->currentIndex() == MainDisplayMode::MainDisplayMode_GridMode) {
    ui->GridDisplay->UpdateConfig();
  }
  return *config_;
}
void MainDisplayContainer::OnSpeakerDisplayWidgetNotifyUpdateTopWindowLayout()
{
  if (ui->stackedWidget->currentIndex() == MainDisplayMode::MainDisplayMode_SpeakerMode) {
    emit signalNotifyUpdateTopWindowLayout();
  }
}
void MainDisplayContainer::OnSpeakerDisplayWidgetNotifyPageUp()
{
  if (ui->stackedWidget->currentIndex() == MainDisplayMode::MainDisplayMode_SpeakerMode) {
    emit signalNotifyPageUp();
  }
}
void MainDisplayContainer::OnSpeakerDisplayWidgetNotifyPageDown()
{
  if (ui->stackedWidget->currentIndex() == MainDisplayMode::MainDisplayMode_SpeakerMode) {
    emit signalNotifyPageDown();
  }
}

void MainDisplayContainer::OnGridDisplayWidgetNotifyPageUp() {
  if (ui->stackedWidget->currentIndex() ==
      MainDisplayMode::MainDisplayMode_GridMode) {
    emit signalNotifyPageUp();
  }
}

void MainDisplayContainer::OnGridDisplayWidgetNotifyPageDown() {
  if (ui->stackedWidget->currentIndex() ==
      MainDisplayMode::MainDisplayMode_GridMode) {
    emit signalNotifyPageDown();
  }
}

int32_t MainDisplayContainer::GetBigDisplayHeight() {
  return ui->SpeakerDisplay->GetBigDisplayHeight();
}