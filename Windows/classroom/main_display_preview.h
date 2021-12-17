#ifndef MAIN_DISPLAY_PREVIEW_H
#define MAIN_DISPLAY_PREVIEW_H

#include <QPushButton>
#include <QWidget>
#include "classroom_def.h"
namespace Ui {
class MainDisplayPreview;
}

namespace alibaba {
namespace doc {
struct GetDocRsp;
} // namespace doc
} // namespace alibaba

class MainDisplayPreviewVM;
class WhiteBoardTips;
class QWidgetEventFilter;
class PenSelectorMenu;
class LaserSelectorMenu;
class TextSelectorMenu;
class UploadSelectorMenu;
class ThumbnailContainer;
class ThumbnailWidget;
struct DocInfoContext;
class PPTUploadPorgressDialog;

class MainDisplayPreview : public QWidget {
  Q_OBJECT

 public:
  explicit MainDisplayPreview(QWidget* parent = nullptr, ThumbnailContainer* thumbnail_container = nullptr);
  ~MainDisplayPreview();
  void UpdateUserId(const std::string& user_id);
  void UpdateRoomId(const std::string& room_id);
  void UpdateUserRole(const ClassRoleEnum user_role);
  void ShowToolbar(bool show);
  void ResetButton();
  void SetWhiteBoardMaskVisible(bool visible);
  void UpdatePointerMode();
  void UpdateWhiteBoardSize(const QSize& sz);
  void UpdateScale();
 private:
  void InitIconFont();
  void InitShadowBox();
  void InitTips();
  void InitMenu();
  void InitThumbnail();
  QString GetTipsContent(QPushButton* btn);
  void PopUpMenu(QWidget* widget, const QPoint& pt);
  void TextStyleNotify();
 signals:
  void NotifyWhiteBoardLoaded();
  void NotifyShowWbTips(const QPoint& pt, const QString& content);
  void NotifyHideWbTips();

 public slots:
  void OnSelectPage(const std::string& group, const uint32_t group_page, const uint32_t wb_page);
 private slots:
  void OnVMUpdate(int32_t field);
  void OnUpdateDocInfo(const std::vector<DocInfoContext>& all_doc_info);
  void on_penBtn_clicked();
  void on_clearBtn_clicked();
  void on_laserBtn_clicked();
  void on_reBtn_clicked();
  void on_selectBtn_clicked();
  void on_textBtn_clicked();
  void on_unBtn_clicked();

  void on_zoomInBtn_clicked();
  void on_zoomOutBtn_clicked();
  void on_firstBtn_clicked();
  void on_lastBtn_clicked();
  void on_newPage_clicked();
  void on_nextBtn_clicked();
  void on_preBtn_clicked();
  void on_eraseBtn_clicked();
  void on_uploadPptBtn_clicked();
  void on_multiSelectBtn_clicked();
  void on_picBtn_clicked();
  void OnMouseEnterWbBtn(QWidget* btn);
  void OnMouseLeaveWbBtn(QWidget* btn);
  void OnMouseEnterWbMenu(QWidget* btn);
  void OnMouseLeaveWbMenu(QWidget* btn);
  void OnNotifyLineWidthChange(int32_t value);
  void OnNotifyShapeRect();
  void OnNotifyShapeRoundedRect();
  void OnNotifyShapeCircle();
  void OnNotifyShapeTriangle();
  void OnNotifyShapeRightTriangle();
  void OnNotifyShapeNormalPen();
  void OnNotifyShapeStraightPen();
  void OnNotifyUseLaser();
  void OnNotifyUseLaserNew();
  void OnNotifyFontSizeChange(int32_t);
  void OnNotifyFontBold(bool);
  void OnNotifyFontItalic(bool);
  void OnNotifyFontUnderLine(bool);
  void OnNotifyUploadPpt();

 private:
  std::shared_ptr<MainDisplayPreviewVM> vm_;
  Ui::MainDisplayPreview* ui;
  WhiteBoardTips* wb_tips_ = nullptr;
  std::unique_ptr<QWidgetEventFilter> filter_;
  std::unique_ptr<QWidgetEventFilter> menu_filter_;
  PenSelectorMenu* pen_select_menu_ = nullptr;
  LaserSelectorMenu* laser_select_menu_ = nullptr;
  TextSelectorMenu* text_select_menu_ = nullptr;
  UploadSelectorMenu* upload_select_menu_ = nullptr;
  ThumbnailContainer* thumbnail_container_ = nullptr;
  std::map<std::string, ThumbnailWidget*> doc_map_;
  PPTUploadPorgressDialog* ppt_progress_dialog_ = nullptr; 
};

#endif // MAIN_DISPLAY_PREVIEW_H
