#include "main_display_preview.h"
#include "QButtonGroup"
#include "common/icon_font_helper.h"
#include "doc/get_doc_rsp.h"
#include "laser_selector_menu.h"
#include "main_display_preview_vm.h"
#include "pen_selector_menu.h"
#include "qwidget_event_filter.h"
#include "text_selector_menu.h"
#include "thumbnail_container.h"
#include "thumbnail_widget.h"
#include "thumbnail_widget_item.h"
#include "ui_main_display_preview.h"
#include "upload_selector_menu.h"
#include "view/interface/i_toast_widget.h"
#include "view/view_component_manager.h"
#include "white_board_tips.h"
#include "ppt_upload_progress.h"

MainDisplayPreview::MainDisplayPreview(QWidget* parent, ThumbnailContainer* thumbnail_container)
    : QWidget(parent, Qt::FramelessWindowHint | Qt::Tool),
      ui(new Ui::MainDisplayPreview),
      thumbnail_container_(thumbnail_container) {
  ui->setupUi(this);
  vm_.reset(new MainDisplayPreviewVM);
  setAttribute(Qt::WA_TranslucentBackground, true);
  QButtonGroup* btn_group = new QButtonGroup(ui->toolBarBottom);
  btn_group->addButton(ui->penBtn);
  btn_group->addButton(ui->laserBtn);
  btn_group->addButton(ui->multiSelectBtn);
  btn_group->addButton(ui->selectBtn);
  btn_group->addButton(ui->textBtn);
  btn_group->addButton(ui->eraseBtn);
  ResetButton();
  InitIconFont();
  InitShadowBox();
  InitTips();
  InitMenu();
  InitThumbnail();
  connect(vm_.get(), &MainDisplayPreviewVM::SignalUpdateVM, this,
          &MainDisplayPreview::OnVMUpdate, Qt::QueuedConnection);
  qRegisterMetaType<alibaba::doc::GetDocRsp>("alibaba::doc::GetDocRsp");

  connect(vm_.get(), &MainDisplayPreviewVM::SignalUpdateDocInfo, this,
    &MainDisplayPreview::OnUpdateDocInfo);
  connect(thumbnail_container_, &ThumbnailContainer::NotifySelectPage, this, &MainDisplayPreview::OnSelectPage);
  QTimer::singleShot(5000, [this]() { vm_->UpdatePageInfo(); });
}

MainDisplayPreview::~MainDisplayPreview() { delete ui; }

void MainDisplayPreview::ShowToolbar(bool show) {
  ui->toolBarBottom->setVisible(show);
}

void MainDisplayPreview::ResetButton() { ui->penBtn->setChecked(true); }

void MainDisplayPreview::SetWhiteBoardMaskVisible(bool visible) {
  ui->whiteBoardPage->setVisible(visible);
}

void MainDisplayPreview::UpdatePointerMode() { on_selectBtn_clicked(); }



void MainDisplayPreview::UpdateWhiteBoardSize(const QSize& sz) {
  if (sz.width() > 0) {

    QPoint pt = geometry().topLeft();
    double scale = ((double)sz.width()) / 2268.0;
    vm_->UpdateScale(scale);
  }
}

void MainDisplayPreview::UpdateScale() {
  if (vm_) {
    vm_->GetScale();
  }
}

QString MainDisplayPreview::GetTipsContent(QPushButton* btn) {
  if (btn == ui->clearBtn) {
    return QTranslate("InteractiveClass.WhiteBoard.Clear");
  } else if (btn == ui->eraseBtn) {
    return QTranslate("InteractiveClass.WhiteBoard.Erase");
  } else if (btn == ui->selectBtn) {
    return QTranslate("InteractiveClass.WhiteBoard.Pointer");
  } else if (btn == ui->multiSelectBtn) {
    return QTranslate("InteractiveClass.WhiteBoard.SelectRect");
  } else if (btn == ui->picBtn) {
    return QTranslate("InteractiveClass.WhiteBoard.Pic");
  } else {
    return "";
  }
}

void MainDisplayPreview::InitTips() {
  filter_ = std::make_unique<QWidgetEventFilter>();
  menu_filter_ = std::make_unique<QWidgetEventFilter>();
  wb_tips_ = new WhiteBoardTips(this);
  connect(this, &MainDisplayPreview::NotifyShowWbTips, wb_tips_,
          &WhiteBoardTips::OnShowTips);
  connect(this, &MainDisplayPreview::NotifyHideWbTips, wb_tips_,
          &WhiteBoardTips::OnHideTips);
  connect(filter_.get(), &QWidgetEventFilter::signalMouseEnter, this,
          &MainDisplayPreview::OnMouseEnterWbBtn);
  connect(filter_.get(), &QWidgetEventFilter::signalMouseLeave, this,
          &MainDisplayPreview::OnMouseLeaveWbBtn);
  connect(menu_filter_.get(), &QWidgetEventFilter::signalMouseEnter, this,
          &MainDisplayPreview::OnMouseEnterWbMenu);
  connect(menu_filter_.get(), &QWidgetEventFilter::signalMouseLeave, this,
          &MainDisplayPreview::OnMouseLeaveWbMenu);
  ui->selectBtn->installEventFilter(filter_.get());
  ui->eraseBtn->installEventFilter(filter_.get());
  ui->clearBtn->installEventFilter(filter_.get());
  ui->multiSelectBtn->installEventFilter(filter_.get());
  ui->picBtn->installEventFilter(filter_.get());
  ui->penBtn->installEventFilter(menu_filter_.get());
  ui->textBtn->installEventFilter(menu_filter_.get());
  ui->laserBtn->installEventFilter(menu_filter_.get());
  ui->uploadPptBtn->installEventFilter(menu_filter_.get());
}

void MainDisplayPreview::InitMenu() {
  pen_select_menu_ = new PenSelectorMenu(nullptr);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyLineWidthChange, this,
          &MainDisplayPreview::OnNotifyLineWidthChange);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeCircle, this,
          &MainDisplayPreview::OnNotifyShapeCircle);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeNormalPen, this,
          &MainDisplayPreview::OnNotifyShapeNormalPen);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeStraightPen, this,
          &MainDisplayPreview::OnNotifyShapeStraightPen);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeRect, this,
          &MainDisplayPreview::OnNotifyShapeRect);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeRoundedRect, this,
          &MainDisplayPreview::OnNotifyShapeRoundedRect);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeTriangle, this,
          &MainDisplayPreview::OnNotifyShapeTriangle);
  connect(pen_select_menu_, &PenSelectorMenu::NotifyShapeRightTriangle, this,
          &MainDisplayPreview::OnNotifyShapeRightTriangle);
  laser_select_menu_ = new LaserSelectorMenu(nullptr);
  connect(laser_select_menu_, &LaserSelectorMenu::NotifyUseLaser, this,
          &MainDisplayPreview::OnNotifyUseLaser);
  connect(laser_select_menu_, &LaserSelectorMenu::NotifyUseLaserNew, this,
          &MainDisplayPreview::OnNotifyUseLaserNew);
  text_select_menu_ = new TextSelectorMenu(nullptr);
  connect(text_select_menu_, &TextSelectorMenu::NotifyFontSizeChange, this,
          &MainDisplayPreview::OnNotifyFontSizeChange);
  connect(text_select_menu_, &TextSelectorMenu::NotifyFontBold, this,
          &MainDisplayPreview::OnNotifyFontBold);
  connect(text_select_menu_, &TextSelectorMenu::NotifyFontItalic, this,
          &MainDisplayPreview::OnNotifyFontItalic);
  connect(text_select_menu_, &TextSelectorMenu::NotifyFontUnderLine, this,
          &MainDisplayPreview::OnNotifyFontUnderLine);
  upload_select_menu_ = new UploadSelectorMenu(nullptr);
  connect(upload_select_menu_, &UploadSelectorMenu::NotifyUploadPptClicked,
          this, &MainDisplayPreview::OnNotifyUploadPpt);
}

void MainDisplayPreview::InitThumbnail() {
  //todo init thunmbnail
}

void MainDisplayPreview::InitShadowBox() {
  auto add_shadow_to_widget = [](QWidget* widget) {
    QGraphicsDropShadowEffect* shadow = new QGraphicsDropShadowEffect(widget);
    shadow->setOffset(0, 0);
    shadow->setColor(QColor("#CCCCCC"));
    shadow->setBlurRadius(8);
    widget->setGraphicsEffect(shadow);
  };

  add_shadow_to_widget(ui->toolBarBottom);
  add_shadow_to_widget(ui->toolBarLeft);
  add_shadow_to_widget(ui->toolBarScale);
  add_shadow_to_widget(ui->toolbarReUndo);
}

void MainDisplayPreview::InitIconFont() {
  IconFontHelper::Instance()->SetIcon(ui->selectBtn, kPointer);
  IconFontHelper::Instance()->SetIcon(ui->picBtn, kPicInsertIcon);
  IconFontHelper::Instance()->SetIcon(ui->multiSelectBtn, kMultiSelectIcon);
  IconFontHelper::Instance()->SetIcon(ui->penBtn, kPen);
  IconFontHelper::Instance()->SetIcon(ui->textBtn, kText);
  IconFontHelper::Instance()->SetIcon(ui->laserBtn, kLaser);
  IconFontHelper::Instance()->SetIcon(ui->eraseBtn, kErase);
  IconFontHelper::Instance()->SetIcon(ui->clearBtn, kClean);
  IconFontHelper::Instance()->SetIcon(ui->unBtn, kUndo);
  IconFontHelper::Instance()->SetIcon(ui->reBtn, kRedo);
  IconFontHelper::Instance()->SetIcon(ui->zoomInBtn, kSmaller);
  IconFontHelper::Instance()->SetIcon(ui->zoomOutBtn, kBigger);
  IconFontHelper::Instance()->SetIcon(ui->firstBtn, kFirstPage);
  IconFontHelper::Instance()->SetIcon(ui->lastBtn, kLastPage);
  IconFontHelper::Instance()->SetIcon(ui->preBtn, kPrePage);
  IconFontHelper::Instance()->SetIcon(ui->nextBtn, kNextPage);
  IconFontHelper::Instance()->SetIcon(ui->newPage, kNewPage);
  IconFontHelper::Instance()->SetIcon(ui->uploadPptBtn, kUploadPptIcon);
  ui->toolBarLeft->layout()->setAlignment(ui->line, Qt::AlignHCenter);
  ui->toolBarLeft->layout()->setAlignment(ui->line_2, Qt::AlignHCenter);
}

void MainDisplayPreview::UpdateUserId(const std::string& user_id) {
  vm_->UpdateUserId(user_id);
}

void MainDisplayPreview::UpdateRoomId(const std::string& room_id) {
  vm_->UpdateRoomId(room_id);
}
void MainDisplayPreview::UpdateUserRole(const ClassRoleEnum user_role)
{
  vm_->UpdateUserRole(user_role);
}

void MainDisplayPreview::OnVMUpdate(int32_t field) {
  auto model = vm_->GetModel();
  if (field & MainDisplayPreviewFieldCurrentIndex) {
    QString index =
        QString("%1/%2").arg(model.doc_current_page).arg(model.doc_total_page);
    ui->pageLabel->setText(index);
  }

  if (field & MainDisplayPreviewFieldScale) {
    int32_t scale_int = (int32_t)(model.scale * 100.0);
    QString scale = QString("%1%").arg(scale_int);
    ui->scaleLabel->setText(scale);
  }

  if (field & MainDisplayPreviewFieldTotalIndex) {
    QString index =
        QString("%1/%2").arg(model.doc_current_page).arg(model.doc_total_page);
    ui->pageLabel->setText(index);
  }

  if (field & MainDisplayPreviewWhiteBoardLoaded) {
    Q_EMIT NotifyWhiteBoardLoaded();
  }

  if (field & MainDisplayPreviewDocUploadedFailed) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadFailed);
    }
    else {
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowFailedToast(
          QTranslate("InteractiveClass.WhiteBoard.UploadFileError"));
    }
  }

  if (field & MainDisplayPreviewDocUploading) {
    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowInfoToast(QTranslate("InteractiveClass.WhiteBoard.Uploading"));
  }

  if (field & MainDisplayPreviewDocUploadedSuccess) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadSuccess);
    } else {
      GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowSuccessToast(
          QTranslate("InteractiveClass.WhiteBoard.UploadSuccess"));
    }

  }

  if (field & MainDisplayPreviewDocProcessingStart) {
    if (!ppt_progress_dialog_ && model.is_upload_doc) {
      ui->stackedWidget->setStyleSheet("QStackedWidget#stackedWidget{\nbackground:rgba(0,0,0, 0.9);\n}");
      ppt_progress_dialog_ = new PPTUploadPorgressDialog(this);
      ppt_progress_dialog_->UpdateFilePath(model.file_path);
      ppt_progress_dialog_->exec();
      ui->stackedWidget->setStyleSheet("");
      delete ppt_progress_dialog_;
      ppt_progress_dialog_ = nullptr;
    }
  }

  if (field & MainDisplayPreviewDocProcessingUploadProgress) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadUploading, model.upload_progress);
    }
  }

  if (field & MainDisplayPreviewDocProcessingTranscode) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadProcessing);
    }
  }

  if (field & MainDisplayPreviewDocProcessingComplete) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadComplete);
    }
  }

  if (field & MainDisplayPreviewDocProcessingInsert) {
    if (ppt_progress_dialog_) {
      ppt_progress_dialog_->UpdateState(PPTUploadSuccess, model.insert_progress);
    }
  }
}

void MainDisplayPreview::OnUpdateDocInfo(const std::vector<DocInfoContext>& all_doc_info)
{
  if (thumbnail_container_) {

    for (const DocInfoContext& doc_info : all_doc_info) {

      bool is_whiteboard = doc_info.doc_id == "wb";
      ThumbnailWidget* doc_widget = thumbnail_container_->AddThumbnailWidget(
        doc_info.doc_id, 
        is_whiteboard ? ThumbnailWidget::ThumbnailTypeWhiteboard : ThumbnailWidget::ThumbnailTypePPT);

      if (doc_widget) {
        QString title = is_whiteboard ? QTranslate("InteractiveClass.WhiteBoard") : QString::fromStdString(doc_info.doc_name);
        doc_widget->SetTitle(title);
        for (const DocPageInfoContext& doc_page : doc_info.doc_page_info) {
          const std::string duplicate_key = std::to_string(doc_page.group_page);
          ThumbnailWidgetItem* doc_item = doc_widget->AddThumbnailWidgetItem(duplicate_key);
          if (doc_item) {
            doc_item->SetIndex(doc_page.group_page);
            doc_item->SetWbPage(doc_page.wb_page);
          }     
          if (!is_whiteboard) {
            thumbnail_container_->AddResource(doc_info.doc_id, doc_page.group_page, doc_page.page_url);
          }
        }
      }
    }
  }
}

void MainDisplayPreview::OnSelectPage(const std::string& group, const uint32_t group_page, const uint32_t wb_page) {
  if (wb_page) {
    uint32_t wb_page_index = wb_page - 1;
    vm_->WhiteboardGoToPage(wb_page_index);
  }
}

void MainDisplayPreview::on_penBtn_clicked() {
  if (pen_select_menu_) {
    pen_select_menu_->SetCurrentType();
  }
  PopUpMenu(pen_select_menu_, ui->penBtn->mapToGlobal(QPoint(20, 20)));
  ui->penBtn->setChecked(true);
}

void MainDisplayPreview::on_zoomInBtn_clicked() {
  vm_->OnBnClickedButtonZoomIn();
}

void MainDisplayPreview::on_zoomOutBtn_clicked() {
  vm_->OnBnClickedButtonZoomOut();
}

void MainDisplayPreview::on_firstBtn_clicked() {
  vm_->OnBnClickedButtonFirst();
}

void MainDisplayPreview::on_lastBtn_clicked() { vm_->OnBnClickedButtonLast(); }

void MainDisplayPreview::on_newPage_clicked() {
  vm_->OnBnClickedButtonAddPage();
}


void MainDisplayPreview::on_nextBtn_clicked() { vm_->OnBnClickedButtonNext(); }

void MainDisplayPreview::on_preBtn_clicked() { vm_->OnBnClickedButtonPre(); }

void MainDisplayPreview::on_eraseBtn_clicked() {
  vm_->OnBnClickedButtonErase();
}

void MainDisplayPreview::on_clearBtn_clicked() {
  vm_->OnBnClickedButtonClear();
}

void MainDisplayPreview::on_uploadPptBtn_clicked() {
  PopUpMenu(upload_select_menu_, ui->uploadPptBtn->mapToGlobal(QPoint(20, 20)));
  // for test
}

void MainDisplayPreview::on_multiSelectBtn_clicked() {
  vm_->OnBnClickedMultiSelect();
}

void MainDisplayPreview::on_picBtn_clicked() {
  vm_->OnBnClickedButtonAddImage(this);
}


void MainDisplayPreview::OnMouseEnterWbBtn(QWidget* btn) {
  QPushButton* push_btn = dynamic_cast<QPushButton*>(btn);
  if (push_btn) {
    QString tips = GetTipsContent(push_btn);
    if (!tips.isEmpty()) {
      QPoint pt = push_btn->mapToGlobal(QPoint(11, 11));
      Q_EMIT NotifyShowWbTips(pt, tips);
    }
  }
}

void MainDisplayPreview::OnMouseLeaveWbBtn(QWidget* btn) {
  Q_EMIT NotifyHideWbTips();
}

void MainDisplayPreview::OnMouseEnterWbMenu(QWidget* btn) {
  QPushButton* push_btn = dynamic_cast<QPushButton*>(btn);
  if (push_btn) {
    if (push_btn == ui->penBtn) {
      PopUpMenu(pen_select_menu_, ui->penBtn->mapToGlobal(QPoint(11, 11)));
    } else if (push_btn == ui->laserBtn) {
      PopUpMenu(laser_select_menu_, ui->laserBtn->mapToGlobal(QPoint(11, 11)));

    } else if (push_btn == ui->textBtn) {
      PopUpMenu(text_select_menu_, ui->textBtn->mapToGlobal(QPoint(11, 11)));
    } else if (push_btn == ui->uploadPptBtn) {
      PopUpMenu(upload_select_menu_,
                ui->uploadPptBtn->mapToGlobal(QPoint(11, 11)));
    }
  }
}

void MainDisplayPreview::OnMouseLeaveWbMenu(QWidget* btn) {}


void MainDisplayPreview::OnNotifyLineWidthChange(int32_t value) {
  vm_->UpdateLineWidth(value);
}

void MainDisplayPreview::OnNotifyShapeRect() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonPenRect();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeRoundedRect() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonRoundedRect();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeCircle() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonPenCircle();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeTriangle() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonTriangle();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeRightTriangle() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonRightTriangle();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeNormalPen() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonPenNormal();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}

void MainDisplayPreview::OnNotifyShapeStraightPen() {
  ui->penBtn->setChecked(true);
  vm_->OnBnClickedButtonStraightLine();
  if (pen_select_menu_) {
    int32_t size = pen_select_menu_->GetPenSize();
    vm_->UpdateLineWidth(size);
  }
}


void MainDisplayPreview::OnNotifyUseLaser() {
  ui->laserBtn->setChecked(true);
  vm_->OnBnClickedButtonPenLaserPen();
}

void MainDisplayPreview::OnNotifyUseLaserNew() {
  ui->laserBtn->setChecked(true);
  vm_->OnBnClickedButtonPenLaserPenNew();
}

void MainDisplayPreview::OnNotifyFontSizeChange(int32_t size) {
  vm_->UpdateFontSize(size);
}

void MainDisplayPreview::OnNotifyFontBold(bool) {
  vm_->UpdateSelectedBold();
  TextStyleNotify();
}

void MainDisplayPreview::OnNotifyFontItalic(bool) {
  vm_->UpdateSelectedItalic();
  TextStyleNotify();
}

void MainDisplayPreview::OnNotifyFontUnderLine(bool) {
  vm_->UpdateSelectedUnderLine();
  TextStyleNotify();
}

void MainDisplayPreview::TextStyleNotify() {
  if (!ui->selectBtn->isChecked() && !ui->multiSelectBtn->isChecked()) {
    GetViewComponent<IToastWidget>(kToastWindow)
        ->ShowInfoToast(
            QTranslate("InteractiveClass.WhiteBoard.TextStypleNotify"));
  }
}

void MainDisplayPreview::OnNotifyUploadPpt() {
  vm_->OnBnClickedButtonUpload(this);
  //vm_->OnBnClickedButtonUpload2(this);
}

void MainDisplayPreview::on_laserBtn_clicked() {
  if (laser_select_menu_) {
    laser_select_menu_->SetCurrentType();
  }
  PopUpMenu(laser_select_menu_, ui->laserBtn->mapToGlobal(QPoint(20, 20)));
}

void MainDisplayPreview::on_reBtn_clicked() { vm_->OnBnClickedButtonRedo(); }

void MainDisplayPreview::on_selectBtn_clicked() {
  if (ui->selectBtn->isChecked()) {
    vm_->OnBnClickedButtonPenSelect();
  }
}

void MainDisplayPreview::on_textBtn_clicked() {
  PopUpMenu(text_select_menu_, ui->textBtn->mapToGlobal(QPoint(20, 20)));
  vm_->OnBnClickedButtonPenText();
  if (text_select_menu_) {
    int32_t size = text_select_menu_->GetFontSize();
    vm_->UpdateFontSize(size);
  }
}

void MainDisplayPreview::on_unBtn_clicked() { vm_->OnBnClickedButtonUndo(); }

void MainDisplayPreview::PopUpMenu(QWidget* widget, const QPoint& pt) {
  if (widget) {
    QPoint target_pt = pt + QPoint(20, -widget->height() / 2);
    widget->move(target_pt);
    widget->show();
    widget->raise();
    widget->activateWindow();
  }
}
