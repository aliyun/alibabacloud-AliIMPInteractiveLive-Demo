#include "grid_conf_views_controls.h"
#include <QGraphicsDropShadowEffect>
#include <QPushButton>

#include "conf_view_controls_row.h"

#include "common/icon_font_helper.h"

const size_t kGridViewMaxRows = 3;
const size_t kGridBigViewIndex = kGridViewMaxRows * kGridViewMaxRows;
static const int HNextPageButtonWidth = 20;
static const int HNextPageButtonHeight = 64;
static const int FullScreenButtonWidth = 26;
static const char* PrePageButtonStyle = "QPushButton {"
                                        "font-family:\"lviconfont\";"
                                        "font-size: 16px;"
                                        "background-color: #8F1E2022;"
                                        "border: 0px solid #000000;"
                                        "border-top-right-radius: 3px;"
                                        "border-bottom-right-radius: 3px;"
                                        "color:rgba(255, 255, 255, 0.56);"
                                        "}"
                                        "QPushButton:hover {"
                                        "color:#2877C7;"
                                        "}";
static const char* NextPageButtonStyle = "QPushButton {"
    "font-family:\"lviconfont\";"
    "font-size: 16px;"
    "background-color: #8F1E2022;"
    "border: 0px solid #000000;"
    "border-top-left-radius: 3px;"
    "border-bottom-left-radius: 3px;"
    "color:rgba(255, 255, 255, 0.56);"
    "}"
    "QPushButton:hover {"
    "color:#2877C7;"
    "}";
static const char* FuncButtonStyle =
  "QPushButton {"
  "font-family:\"lviconfont\";"
  "font-size: 20px;"
  "background-color: #8F1E2022;"
  "border: 0px solid #000000;"
  "color:rgba(255, 255, 255, 0.56);"
  "}"
  "QPushButton:hover {"
  "color:#2877C7;"
  "}";

CGridConfViewsControls::CGridConfViewsControls(QWidget* parent)
#if defined(OS_MACOSX)
  : QDialog(parent, Qt::FramelessWindowHint)
#else
  : QDialog(parent, Qt::FramelessWindowHint | Qt::Tool)
#endif
  , ui(new Ui::GridConfViewsControls) {

  ui->setupUi(this);
  setAttribute(Qt::WA_AlwaysShowToolTips);
  setAttribute(Qt::WA_TranslucentBackground);
  pre_page_button_ = new QPushButton("previous", this);
  pre_page_button_->setStyleSheet(PrePageButtonStyle);
  pre_page_button_->setCursor(QCursor(Qt::PointingHandCursor));
  pre_page_button_->setVisible(false);
  next_page_button_ = new QPushButton("next", this);
  next_page_button_->setStyleSheet(NextPageButtonStyle);
  next_page_button_->setCursor(QCursor(Qt::PointingHandCursor));
  next_page_button_->setVisible(false);
  //full screen button
 
  IconFontHelper::Instance()->SetIcon(pre_page_button_, kIconHPre);
  IconFontHelper::Instance()->SetIcon(next_page_button_, kIconHNext);
  connect(pre_page_button_, SIGNAL(clicked()), this, SLOT(on_btnPrePage_clicked()));
  connect(next_page_button_, SIGNAL(clicked()), this, SLOT(on_btnNextPage_clicked()));
//   connect(full_screen_button_, SIGNAL(clicked()), this, SLOT(on_btnFullScreen_clicked()));
//   connect(CConferenceManager::instance(), SIGNAL(FullScreenStateChanged()),
//           this, SLOT(OnFullScreenStateChanged()));
//   connect(CConferenceManager::instance(), SIGNAL(HostRecordStatusChanged(bool)),
//           this, SLOT(OnHostRecordStatusChanged(bool)));
//   connect(CConferenceManager::instance(), SIGNAL(DisplayMemberCountChanged()),
//     this, SLOT(OnDisplayMemberCountChanged()));
//   connect(CConferenceManager::instance(), SIGNAL(PageSizeChanged()),
//     this, SLOT(OnPageSizeChanged()));
  InitViewControls();
}

CGridConfViewsControls::~CGridConfViewsControls() {

}

void CGridConfViewsControls::InitViewControls() {
  view_controls_rows_.resize(kGridViewMaxRows);
  QSpacerItem* vspacer0 = new QSpacerItem(20, 40, QSizePolicy::Minimum, QSizePolicy::Expanding);
  ui->widget_conf_view->layout()->addItem(vspacer0);
  for (size_t i = 0; i < kGridViewMaxRows; i++) {
    CConfViewControlsRow* conf_view_controls_row =
      new CConfViewControlsRow(ui->widget_conf_view);
    conf_view_controls_row->InitViewControls(kGridViewMaxRows, ConfViewItemTypeBig);
    view_controls_rows_[i] = conf_view_controls_row;
    ui->widget_conf_view->layout()->addWidget(conf_view_controls_row);
  }
  QSpacerItem* vspacer1 = new QSpacerItem(20, 40, QSizePolicy::Minimum, QSizePolicy::Expanding);
  ui->widget_conf_view->layout()->addItem(vspacer1);
}

std::vector<int> CGridConfViewsControls::ChangeToView(int view_count) {
  if (view_count <1 || view_count > kGridViewMaxRows * kGridViewMaxRows) {
    return {};
  }
  switch (view_count) {
  case 1: {
    return ChangeToViews(1, 1, 1);
    break;
  }
  case 2: {
    return ChangeToViews(2, 1, 2);
    break;
  }
  case 3: {
    return ChangeToViews(2, 2, 3);
    break;
  }
  case 4: {
    return ChangeToViews(2, 2, 4);
    break;
  }
  case 5: {
    return ChangeToViews(3, 2, 5);
    break;
  }
  case 6: {
    return ChangeToViews(3, 2, 6);
    break;
  }
  case 7: {
    return ChangeToViews(3, 3, 7);
    break;
  }
  case 8: {
    return ChangeToViews(3, 3, 8);
    break;
  }
  case 9: {
    return ChangeToViews(3, 3, 9);
    break;
  }
  case 10: {
    return ChangeToViews(4, 3, 10);
    break;
  }
  case 11: {
    return ChangeToViews(4, 3, 11);
    break;
  }
  case 12: {
    return ChangeToViews(4, 3, 12);
    break;
  }
  case 13: {
    return ChangeToViews(4, 4, 13);
    break;
  }
  case 14: {
    return ChangeToViews(4, 4, 14);
    break;
  }
  case 15: {
    return ChangeToViews(4, 4, 15);
    break;
  }
  case 16: {
    return ChangeToViews(4, 4, 16);
    break;
  }
  case 17: {
    return ChangeToViews(5, 4, 17);
    break;
  }
  case 18: {
    return ChangeToViews(5, 4, 18);
    break;
  }
  case 19: {
    return ChangeToViews(5, 4, 19);
    break;
  }
  case 20: {
    return ChangeToViews(5, 4, 20);
    break;
  }
  case 21: {
    return ChangeToViews(5, 5, 21);
    break;
  }
  case 22: {
    return ChangeToViews(5, 5, 22);
    break;
  }
  case 23: {
    return ChangeToViews(5, 5, 23);
    break;
  }
  case 24: {
    return ChangeToViews(5, 5, 24);
    break;
  }
  case 25: {
    return ChangeToViews(5, 5, 25);
    break;
  }
  default:
    break;
  }
  return {};
}

std::vector<int> CGridConfViewsControls::ChangeToViews(
  int columns, int rows, int count) {

  std::vector<int> ret_indexex;
  int current_count = count;
  for (int i = 0; i < kGridViewMaxRows; i++) {
    if (i < rows) {
      view_controls_rows_[i]->setVisible(true);
      int visible_count = columns;
      if (current_count < columns) {
        visible_count = current_count;
      }
      std::vector<int> view_indexes =
        view_controls_rows_[i]->SetVisibleViewCount(visible_count);
      current_count -= visible_count;
      for (size_t j = 0; j < view_indexes.size(); j++) {
        ret_indexex.push_back(i * kGridViewMaxRows + view_indexes[j]);
      }
    } else {
      view_controls_rows_[i]->setVisible(false);
      view_controls_rows_[i]->SetVisibleViewCount(0);
    }
  }
  CalcViewsSize();
  return ret_indexex;
}


QSize GetMaxSizeKeepRatio(
  int container_width,
  int container_height,
  int raw_width,
  int raw_height) {

  QSize ret;
  double float_width = container_width;
  double float_height = container_height;
  double ratio = float_width / float_height;

  double float_raw_width = raw_width;
  double float_raw_height = raw_height;
  double raw_ratio = float_raw_width / float_raw_height;

  if (ratio - raw_ratio > 0.0000001) {
    ret.setHeight(container_height);
    double target_width = raw_ratio * container_height;
    ret.setWidth(target_width);
  }
  else {
    ret.setWidth(container_width);
    double target_height = container_width / raw_ratio;
    ret.setHeight(target_height);
  }
  return ret;
}

QSize GetMax169RatioSize(int width, int height) {
  return GetMaxSizeKeepRatio(width, height, 16, 9);
}


void CGridConfViewsControls::CalcViewsSize() {

  int height = frameSize().height();
  int visible_row = 0;
  for (size_t i = 0; i < kGridViewMaxRows; i++) {
    if (view_controls_rows_[i]->isVisible()) {
      visible_row += 1;
    }
  }
  if (visible_row == 0) {
    return;
  }
  int v_spacing = (visible_row - 1) * ui->widget_conf_view->layout()->spacing();
  int max_item_height = (height - v_spacing) / visible_row;
  for (size_t i = 0; i < kGridViewMaxRows; i++) {
    if (view_controls_rows_[i]->isVisible()) {
      std::vector<QWidget*> visible_widgets = view_controls_rows_[i]->GetVisibleView();
      if (visible_widgets.size() > 0) {
        int h_spacing = (visible_widgets.size() - 1) * view_controls_rows_[i]->layout()->spacing();
        int max_item_width = (view_controls_rows_[i]->width() - h_spacing) / visible_widgets.size();
        QSize current_size = GetMax169RatioSize(max_item_width, max_item_height);
        for (auto it : visible_widgets) {
          it->setFixedSize(current_size);
        }
      }
    }
  }
}

void CGridConfViewsControls::ResizeWindow(const QSize& size) {
  setFixedSize(size);
  pre_page_button_->setGeometry(0, (size.height() - HNextPageButtonHeight) / 2,
                                HNextPageButtonWidth, HNextPageButtonHeight);
  next_page_button_->setGeometry(size.width() - HNextPageButtonWidth,
                                 (size.height() - HNextPageButtonHeight) / 2,
                                 HNextPageButtonWidth,
                                 HNextPageButtonHeight);

}

void CGridConfViewsControls::UpdateViewItemInfo(int index, const ConfViewControlModel& info) {
  if (!IsValidIndex(index)) {
    blog(LOG_WARNING, "UpdateViewItemInfo , index=%d", index);
    return;
  }
  int row_index = index / kGridViewMaxRows;
  int column_index = index % kGridViewMaxRows;
  return view_controls_rows_[row_index]->UpdateViewItemInfo(column_index, info);
}

void CGridConfViewsControls::ResetAllViewsControls() {
  ConfViewControlModel info;
  info.field = 
    ConfViewControlFieldNick |
    ConfViewControlFieldCamera |
    ConfViewControlFieldMic;
  for (int i = 0; i < kGridViewMaxRows * kGridViewMaxRows; i++) {
    UpdateViewItemInfo(i, info);
  }
}

void CGridConfViewsControls::ShowPreviousNextButton(bool show) {
//   if (show) {
//     if (pre_page_button_) {
//       pre_page_button_->setVisible(CConferenceManager::instance()->HasPreviousPage());
//     }
//     if (next_page_button_) {
//       next_page_button_->setVisible(CConferenceManager::instance()->HasNextPage());
//     }
//   }
//   else {
//     if (pre_page_button_) {
//       pre_page_button_->setVisible(false);
//     }
//     if (next_page_button_) {
//       next_page_button_->setVisible(false);
//     }
//   }
}

void CGridConfViewsControls::enterEvent(QEvent *event) {
  ShowPreviousNextButton(true);
  QWidget::enterEvent(event);
}

void CGridConfViewsControls::leaveEvent(QEvent *event) {
  QWidget::leaveEvent(event);
}

void CGridConfViewsControls::on_btnPrePage_clicked() {
  
//   IPCClientManager::instance()->GetNotifyMgr()->ReqBILog(bilog);
//   CConferenceManager::instance()->SwitchToPreviousPage();
  ShowPreviousNextButton(true);
}

void CGridConfViewsControls::on_btnNextPage_clicked() {

  ShowPreviousNextButton(true);
}



bool CGridConfViewsControls::IsValidIndex(int index) {
  if (index >= 0 && index < kGridViewMaxRows * kGridViewMaxRows) {
    return true;
  }
  return false;
}


void CGridConfViewsControls::keyPressEvent(QKeyEvent* event) {
  switch (event->key()) {
  case Qt::Key_Escape: {
    
    break;
  }
  default:
    QDialog::keyPressEvent(event);
    break;
  }
}

void CGridConfViewsControls::mousePressEvent(QMouseEvent *event) {
  QDialog::mousePressEvent(event);

}


void CGridConfViewsControls::OnPageSizeChanged() {
  ShowPreviousNextButton(true);
}
