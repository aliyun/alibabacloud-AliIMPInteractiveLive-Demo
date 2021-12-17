#include "share_screen_item_widget.h"
#include "ui_share_screen_item_widget.h"
#include "common/icon_font_helper.h"
#include <QApplication>
#include <QDesktopWidget>
#include <QPainter>
#include <QPixmap>
#include <QFontDatabase>
#include <qscreen.h>
#include <qwindow.h>
#include <qtwinextras/qwinfunctions.h> 


static const QColor kShareItemBorderColor = QColor(255, 255, 255, 0.16 * 255);
static const QColor kShareItemSelectBorderColor = QColor(0, 128, 255);
static const QColor kShareItemBackgroundColor = QColor(247, 247, 247, 255);


ShareScreenItemWidget::ShareScreenItemWidget(QWidget *parent) : 
  QWidget(parent), 
  ui(new Ui::ShareScreenItemWidget) {
  ui->setupUi(this);

  ui->frame_2->setProperty("status", "normal");
  ui->frame_2->style()->polish(ui->frame_2);
  setMouseTracking(true);
}

ShareScreenItemWidget::~ShareScreenItemWidget() {
  delete ui;
}

QScreen* GetQScreenByWindowInfo(const ShareWindowsNode& window_info) {
  BOOL enum_result = TRUE;
  DisplayNode display_node;
  DISPLAY_DEVICEW device;
  device.cb = sizeof(device);
  enum_result = EnumDisplayDevicesW(NULL, window_info.wid, &device, 0);
  if (!enum_result) {
    return NULL;
  }

  QList<QScreen*> screens = QGuiApplication::screens();

  for (int i = 0; i < screens.size(); i ++) {
    if (screens.at(i)->name() == QString::fromStdWString(device.DeviceName)) {
      return screens.at(i);
    }
  }
  return NULL;
}

void ShareScreenItemWidget::setInfo(const ShareWindowsNode& window_info) { 
  window_info_ = window_info;
  QPixmap window_thumb;
  
  if (window_info.is_screen) {
    QScreen* screen = GetQScreenByWindowInfo(window_info);
    if (screen) {
      window_thumb = screen->grabWindow(0);
    }
  } else {
    window_thumb = GrabWindowEx(window_info.wid);
  }

  const QSize& icon_size = ui->preview->size();
  int padding = 0;

  QPixmap scaled_window_thumb =
      ScaleWindowThumb(window_thumb, kShareItemBackgroundColor, icon_size,
                       padding, kShareItemBorderColor);

  QString item_text = GetSubText(window_info.title, ui->title->font(),
                                 icon_size.width() - 8);
  ui->title->setText(item_text);
  ui->preview->setPixmap(scaled_window_thumb);
}

ShareWindowsNode ShareScreenItemWidget::getInfo() {
  return window_info_;
}

QString ShareScreenItemWidget::GetSubText(const QString& str,
                                         const QFont& font,
                                         int max_width) {
  QString strTemp = str;
#if defined(WIN32)
  max_width += 8;
#endif
  QFontMetrics font_width(font);
  int width = font_width.width(strTemp);
  if (width >= max_width) {
    strTemp = font_width.elidedText(strTemp, Qt::ElideRight, max_width);
    if (strTemp.length() == 1) {
      // ...
      return "";
    }
  }

  return strTemp;
}

bool is_windows8_1_or_greater() {
  static bool g_inited = false;
  static bool win81_greater = false;

  if (g_inited) {
    return win81_greater;
  }
  typedef void(__stdcall * NTPROC)(DWORD*, DWORD*, DWORD*);
  HINSTANCE hinst = LoadLibrary(L"ntdll.dll");
  NTPROC proc = (NTPROC)GetProcAddress(hinst, "RtlGetNtVersionNumbers");
  if (proc) {
    DWORD dwMajor, dwMinor, dwBuildNumber;
    proc(&dwMajor, &dwMinor, &dwBuildNumber);
    if (dwMajor == 6 && dwMinor >= 3) {
      win81_greater = true;
    }
    if (dwMajor > 6) {
      win81_greater = true;
    }
  }
  g_inited = true;
  return win81_greater;
}

QPixmap ShareScreenItemWidget::GrabWindowEx(WId window) {
  QSize window_size;
  HWND hwnd = reinterpret_cast<HWND>(window);
  if (hwnd) {
    RECT r;
    GetClientRect(hwnd, &r);
    window_size = QSize(r.right - r.left, r.bottom - r.top);
  } else {
    // Grab current screen. The client rectangle of GetDesktopWindow() is the
    // primary screen, but it is possible to grab other screens from it.
    hwnd = GetDesktopWindow();
    const QRect screenGeometry = geometry();
    window_size = screenGeometry.size();
  }
  int width = window_size.width();
  int height = window_size.height();

  // Create and setup bitmap
  HDC display_dc = GetDC(nullptr);
  HDC bitmap_dc = CreateCompatibleDC(display_dc);
  HBITMAP bitmap = CreateCompatibleBitmap(display_dc, width, height);
  HGDIOBJ null_bitmap = SelectObject(bitmap_dc, bitmap);

  // copy data
  HDC window_dc = GetDC(hwnd);
  DWORD ex_styles = (DWORD)GetWindowLongPtr(hwnd, GWL_EXSTYLE);
  if (ex_styles & WS_EX_LAYERED) {
    PrintWindow(hwnd, bitmap_dc, PW_RENDERFULLCONTENT);
  } else {
    BOOL result = FALSE;
    if (!QtWin::isCompositionEnabled()) {
      if (is_windows8_1_or_greater()) {
        result = PrintWindow(hwnd, bitmap_dc, PW_RENDERFULLCONTENT);
      } else {
        result = PrintWindow(hwnd, bitmap_dc, 0);
      }
    }
    if (!result) {
      if (is_windows8_1_or_greater()) {
        result = PrintWindow(hwnd, bitmap_dc, PW_RENDERFULLCONTENT);
      }
      if (!result) {
        BitBlt(bitmap_dc, 0, 0, width, height, window_dc, 0, 0, SRCCOPY);
      }
    }
  }

  // clean up all but bitmap
  ReleaseDC(hwnd, window_dc);
  SelectObject(bitmap_dc, null_bitmap);
  DeleteDC(bitmap_dc);

  const QPixmap pixmap = QtWin::fromHBITMAP(bitmap);

  DeleteObject(bitmap);
  ReleaseDC(nullptr, display_dc);

  return pixmap;
}

QPixmap ShareScreenItemWidget::ScaleWindowThumbnail(const QPixmap& window_thumb,
                                                   QColor backgroud,
                                                   QSize size) {
  if (size.width() <= 0 || size.height() <= 0) {
    return QPixmap();
  }
  QSize source_size = window_thumb.size();
  QSize scaled_size(size.width(), size.height());
  if (source_size.width() <= 0 || source_size.height() <= 0 ||
      scaled_size.width() <= 0 || scaled_size.height() <= 0) {
    return QPixmap();
  }
  double aspect = (double)scaled_size.width() / scaled_size.height();
  double source_aspect = (double)source_size.width() / source_size.height();
  if (source_aspect < 0.1) {
    source_aspect = 0.1;
  }
  if (source_size.width() <= scaled_size.width() &&
      source_size.height() <= scaled_size.height()) {
    scaled_size = source_size;
  } else if (source_aspect > aspect) {
    scaled_size.setHeight(scaled_size.width() / source_aspect);
  } else {
    scaled_size.setWidth(scaled_size.height() * source_aspect);
  }

  QImage result_image(size, QImage::Format_ARGB32_Premultiplied);
  QPainter painter;
  painter.begin(&result_image);
  painter.setRenderHint(QPainter::Antialiasing, true);
  painter.setCompositionMode(QPainter::CompositionMode_Source);
  painter.fillRect(result_image.rect(), backgroud);
  QSize scaled_size_dpi(scaled_size.width() ,
                        scaled_size.height());
  painter.drawPixmap((size.width() - scaled_size.width()) / 2,
                     (size.height() - scaled_size.height()) / 2,
                     window_thumb.scaled(scaled_size_dpi, Qt::IgnoreAspectRatio,
                                         Qt::SmoothTransformation));
  //  painter.setPen(QPen(border, 4));
  painter.drawRect(result_image.rect());
  painter.end();
  return QPixmap::fromImage(result_image);
}

QPixmap ShareScreenItemWidget::ScaleWindowThumb(const QPixmap& window_thumb,
                                               QColor backgroud,
                                               QSize size,
                                               int padding,
                                               QColor border) {
  if (size.width() <= 0 || size.height() <= 0) {
    return QPixmap();
  }
  QSize source_size = window_thumb.size();
  QSize scaled_size(size.width() - 2 * padding, size.height() - 2 * padding);
  if (source_size.width() <= 0 || source_size.height() <= 0 ||
      scaled_size.width() <= 0 || scaled_size.height() <= 0) {
    return QPixmap();
  }
  double aspect = (double)scaled_size.width() / scaled_size.height();
  double source_aspect = (double)source_size.width() / source_size.height();
  if (source_aspect < 0.1) {
    source_aspect = 0.1;
  }
  if (source_size.width() <= scaled_size.width() &&
      source_size.height() <= scaled_size.height()) {
    scaled_size = source_size;
  } else if (source_aspect > aspect) {
    scaled_size.setHeight(scaled_size.width() / source_aspect);
  } else {
    scaled_size.setWidth(scaled_size.height() * source_aspect);
  }

  QImage result_image(size, QImage::Format_ARGB32_Premultiplied);
  QPainter painter;
  painter.begin(&result_image);
  painter.setRenderHint(QPainter::Antialiasing, true);
  painter.setCompositionMode(QPainter::CompositionMode_Source);
  painter.fillRect(result_image.rect(), backgroud);
  QSize scaled_size_dpi(scaled_size.width(),
                        scaled_size.height());
  painter.drawPixmap((size.width() - scaled_size.width()) / 2,
                     (size.height() - scaled_size.height()) / 2,
                     window_thumb.scaled(scaled_size_dpi, Qt::IgnoreAspectRatio,
                                         Qt::SmoothTransformation));
  /*painter.setPen(QPen(border, 4));
  painter.drawRect(result_image.rect());*/
  painter.end();
  return QPixmap::fromImage(result_image);
}

void ShareScreenItemWidget::setStatus(ShareScreenItemWidget_Status status) {
  last_status_ = status;
  switch (status) {
    case ShareScreenItemWidget_Normal:
      ui->frame_2->setProperty("status", "normal");
      ui->frame_2->style()->polish(ui->frame_2);
      break;
    case ShareScreenItemWidget_Hover:
      ui->frame_2->setProperty("status", "hover");
      ui->frame_2->style()->polish(ui->frame_2);
      break;
    case ShareScreenItemWidget_Selected:
      ui->frame_2->setProperty("status", "selected");
      ui->frame_2->style()->polish(ui->frame_2);
      break;
    default:
      break;
  }
}

void ShareScreenItemWidget::enterEvent(QEvent* event) {
  if (last_status_ != ShareScreenItemWidget_Selected) {
    setStatus(ShareScreenItemWidget_Hover);
  }
}
void ShareScreenItemWidget::leaveEvent(QEvent* event) {
  if (last_status_ != ShareScreenItemWidget_Selected) {
    setStatus(ShareScreenItemWidget_Normal);
  }
}