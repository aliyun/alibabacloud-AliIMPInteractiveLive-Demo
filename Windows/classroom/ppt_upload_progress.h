#ifndef PPT_UPLOAD_PROGRESS_H
#define PPT_UPLOAD_PROGRESS_H

#include <QDialog>
enum PPTUploadStatus
{
  PPTUploadStatusStart = 0,
  PPTUploadUploading,
  PPTUploadProcessing,
  PPTUploadSuccess,
  PPTUploadComplete,
  PPTUploadFailed,
};
namespace Ui {
  class PptUploadProgress;
}

class PPTUploadPorgressDialog : public QDialog {
  Q_OBJECT

public:
  explicit PPTUploadPorgressDialog(QWidget *parent = nullptr);
  ~PPTUploadPorgressDialog();
  void UpdateFilePath(const QString& file_path);
  void UpdateState(PPTUploadStatus status, int32_t percent = -1); 
private:
  void Init();
  void SetCloseVisible(bool show);
  void UpdateProgress(int32_t percent);
private slots:
  void on_closeBtn_clicked();
private:
  Ui::PptUploadProgress *ui_;
  PPTUploadStatus status_ = PPTUploadStatusStart;
  int32_t progress_ = 0;
  QTimer* timer_ = nullptr;
};

#endif // PPT_UPLOAD_PROGRESS_H
