#include "ppt_upload_progress.h"
#include "ui_ppt_upload_progress.h"
#include <QtWin>
#include "QFileInfo"
#include "QFileIconProvider"
#include "common/icon_font_helper.h"

PPTUploadPorgressDialog::PPTUploadPorgressDialog(QWidget *parent) :
  QDialog(parent, Qt::FramelessWindowHint | Qt::Tool),
  ui_(new Ui::PptUploadProgress) {
  setAttribute(Qt::WA_TranslucentBackground);
  ui_->setupUi(this);
  Init();
  UpdateProgress(0);
}

PPTUploadPorgressDialog::~PPTUploadPorgressDialog() {
  delete ui_;
}


void PPTUploadPorgressDialog::UpdateFilePath(const QString& file_path) {
  const int32_t max_width = 285;
  QFileInfo file_info(file_path);

  QString str_temp = file_info.baseName();
  QFontMetrics font_width(ui_->fileNameLabel->font());
  int width = font_width.width(str_temp);
  if (width >= max_width) {
    str_temp = font_width.elidedText(str_temp, Qt::ElideRight, max_width);
  }

  ui_->fileNameLabel->setText(str_temp);

  QFileIconProvider provider;
  QIcon icon = provider.icon(file_info);
  QPixmap icon_pix = icon.pixmap(36, 36).scaled(QSize(36, 36), Qt::KeepAspectRatio, Qt::SmoothTransformation);
  ui_->fileIcon->setPixmap(icon_pix);
}

void PPTUploadPorgressDialog::UpdateState(PPTUploadStatus status, int32_t percent) {
  switch (status)
  {
  case PPTUploadStatusStart:
    ui_->titleLabel->setText(QTranslate("InteractiveClass.UploadingTips"));
    status_ = status;
    break;
  case PPTUploadUploading:
    status_ = status;
    break;
  case PPTUploadProcessing:
    
    ui_->titleLabel->setText(QTranslate("InteractiveClass.ProcessingTips"));
    
    status_ = status;
    break;
  case PPTUploadSuccess:
    if (status_ == PPTUploadProcessing) {
      ui_->titleLabel->setText(QTranslate("InteractiveClass.InserttingTips"));
    }
    
    status_ = status;
    break;
  case PPTUploadComplete:
    status_ = status;
    UpdateProgress(percent);
    accept();
    break;
  case PPTUploadFailed:
    status_ = status;
    ui_->titleLabel->setText(QTranslate("InteractiveClass.UploadingFailed"));
    SetCloseVisible(true);
    break;
  default:
    break;
  }
  UpdateProgress(percent);
}

void PPTUploadPorgressDialog::Init() {
  SetCloseVisible(false);
}

void PPTUploadPorgressDialog::SetCloseVisible(bool show) {
  ui_->closeBtn->setVisible(show);
}


void PPTUploadPorgressDialog::UpdateProgress(int32_t percent) {
  switch (status_)
  {

  case PPTUploadUploading:
    progress_ = percent / 2;
    break;
  case PPTUploadProcessing:
    if (progress_ < 90) {
      progress_ += 2;
    }
    break;
  case PPTUploadSuccess:
    if (progress_ < 90) {
      progress_ = 90;
    }
    if (percent > 0) {
      progress_ = 90 + percent / 10;
    }

    break;
  case PPTUploadComplete:
    progress_ = 100;
    break;
  case PPTUploadFailed:

    break;
  default:
    break;
  }


  ui_->progressBar->setValue(progress_);
  ui_->progressLabel->setText(QString("%1%").arg(progress_));
}

void PPTUploadPorgressDialog::on_closeBtn_clicked() {
  accept();
}