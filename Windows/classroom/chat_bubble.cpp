#include "chat_bubble.h"
#include <QDateTime>
#include <QDebug>
#include <QFontMetrics>
#include <QLabel>
#include <QMovie>
#include <QPaintEvent>
#include <QPainter>
#include <algorithm>
#include <cmath>
#include "common/icon_font_helper.h"

const int32_t kNamePixSize = 10;
const int32_t kTextPixSize = 12;
const int32_t kSideSpace = 10;      // margin between name and window leftside
const int32_t kSideUpSpace = 10;    // margin between name and window topside
const int32_t kMargin = 5;          // margin between name and text
const int32_t kNameLabelMargin = 2; // margin between name and name label
const int32_t kNameLabelContentMargin =
    2; // margin between name content and name label

ChatBubble::ChatBubble(QWidget* parent) : QWidget(parent) {
  font_name_ = QFont();
  font_name_.setFamily("Microsoft YaHei");
  font_name_.setPixelSize(kNamePixSize);
  font_text_ = QFont();
  font_text_.setFamily("Microsoft YaHei");  // MicrosoftYaHei
  font_text_.setPixelSize(kTextPixSize);
  // emoji collections
  // add 0x00 to avoid gibberish   0x1F601- 0x1F64F
  char32_t t[] = {0x1F601, 0x0};

  for (char32_t i = 0x1F601; i <= 0x1F64F; ++i) {
    t[0] = i;
    QString emojistr = QString::fromUcs4(t);
    my_vec_.push_back(emojistr);
  }
}


int32_t ChatBubble::CountEmoji(const QString& text) {
  int32_t n_emoji = 0;
  for (const auto& m : my_vec_) {
    n_emoji += text.count(m);
  }
  return n_emoji;
}

void ChatBubble::SetText(const QString& content,
                         const QString& time,
                         const QString& userid,
                         const uint16_t usertype, 
                         const std::string& comment_id) {
  // update info of the upcoming message
  content_ = content;
  time_ = time;
  userid_ = userid;
  type_ = usertype;
  comment_id_ = comment_id;
}

// calculate the size of the text rectangle
void ChatBubble::GetTextSize() {
  // default text rect width
  text_width_ = this->width() - 15;
  QFontMetricsF fm(font_text_);

  // deal with emoji
  // length of the message
  int32_t pix_text_width = int32_t(std::ceil(fm.width(content_)));
  int32_t pix_text_height = int32_t(std::ceil(fm.lineSpacing()));

  int32_t n_count = content_.count("\n");
  if (n_count == 0) {
    // if the text not filling one row
    if (pix_text_width < text_width_) {
      text_width_ = pix_text_width;
    }
    int32_t line_num = pix_text_width % text_width_ > 0
                           ? (pix_text_width / text_width_ + 1)
                           : pix_text_width / text_width_;
    // deal with emoji
    int32_t n_emojis = CountEmoji(content_);
    int32_t extra = 0;
    if (n_emojis > 0) {
      extra = line_num * 3 * fm.leading();
    }
    text_height_ = line_num * pix_text_height + extra;
  } else {
    QStringList my_list = content_.split("\n");
    int32_t maxlen = 0;
    for (int32_t i = 0; i < my_list.length(); ++i) {
      QString tmp = my_list[i];
      maxlen = std::max(maxlen, int32_t(std::ceil(fm.width(tmp))));
    }

    text_width_ = std::min(maxlen == 0 ? 1 : maxlen, text_width_);

    int32_t extra = 0;
    int32_t line_num = 0;
    for (int32_t i = 0; i < my_list.length(); ++i) {
      if (my_list[i] == "") {
        ++line_num;
        continue;
      }
      int32_t thisnum =
          (int32_t(std::ceil(fm.width(my_list[i])))) / text_width_;
      line_num =
          line_num + thisnum +
          ((int32_t(std::ceil(fm.width(my_list[i]))) % text_width_) > 0 ? 1
                                                                        : 0);
      if (CountEmoji(my_list[i])) extra = extra + fm.leading() * 3 * thisnum;
    }
    // deal with emoji
    text_height_ = line_num * pix_text_height + extra;
  }
}

void ChatBubble::GetNameSize() {
  userid2_ = userid_;
  QFontMetricsF fm(font_name_);
  name_height_ = int32_t(fm.lineSpacing());

  if (userid2_.length() == 0)
    userid2_ = "xxx";
  else if (userid2_.length() > 10)
    userid2_ = userid2_.mid(0, 10) + "...";

  name_width_ = int32_t(fm.width(userid2_+"0"));
}

void ChatBubble::GetNameLabelContentSize() {
  
  if (type_ & UserMe) {
    if (type_ & UserTeacher) {
      name_label_ = QTranslate("InteractiveClass.TeacherMe");
    }
    else {
      name_label_ = QTranslate("InteractiveClass.Self");
    }
  }
  else if (type_ & UserTeacher)
    name_label_ = QTranslate("InteractiveClass.Teacher");

  QFontMetricsF fm(font_name_);
  name_label_content_height_ = int32_t(fm.lineSpacing());
  name_label_content_width_ = int32_t(fm.width(name_label_));
  name_label_width_ = name_label_content_width_ + 2 * kNameLabelContentMargin;
  name_label_height_ = name_label_content_height_ + 2 * kNameLabelContentMargin;
}

// rectify sizes for painting
QSize ChatBubble::AdjRect() {
  GetTextSize();
  GetNameSize();
  GetNameLabelContentSize();

  name_left_rect_ = QRect(kSideSpace, kSideUpSpace, name_width_, name_height_);

  text_left_rect_ =
      QRect(name_left_rect_.x(), name_left_rect_.y() + name_height_ + kMargin,
            text_width_, text_height_);

  name_label_left_rect_ =
      QRect(name_left_rect_.x() + name_width_ + kNameLabelMargin,
            name_left_rect_.y() - kNameLabelContentMargin, name_label_width_,
            name_label_height_);


  name_label_content_left_rect_ =
      QRect(name_label_left_rect_.x() + kNameLabelContentMargin,
            name_label_left_rect_.y() + kNameLabelContentMargin,
            name_label_content_width_, name_label_content_height_);

  return QSize(text_left_rect_.width() + 2 * kSideSpace,
               text_left_rect_.height() + name_height_ + 3 + 2 * kSideUpSpace);
}

void ChatBubble::paintEvent(QPaintEvent* event) {
  Q_UNUSED(event);

  QPainter painter(this);
  // smoothing
  painter.setRenderHints(QPainter::Antialiasing |
                         QPainter::SmoothPixmapTransform);
  painter.setPen(Qt::NoPen);


  // draw content
  QPen penText;
  penText.setColor(QColor(51, 51, 51));
  painter.setPen(penText);
  QTextOption option(Qt::AlignLeft | Qt::AlignVCenter);
  option.setWrapMode(QTextOption::WrapAtWordBoundaryOrAnywhere);
  painter.setFont(font_text_);
  painter.drawText(text_left_rect_, content_, option);
  // draw name
  penText.setColor(Qt::gray);
  painter.setPen(penText);
  option.setWrapMode(QTextOption::WrapAtWordBoundaryOrAnywhere);
  painter.setFont(font_name_);
  painter.drawText(name_left_rect_, userid2_, option);

  if ((!(type_ & UserMe)) && (!(type_ & UserTeacher))) return;

  // draw name label
  QColor col_kuang(255, 235, 235);
  if (type_ & UserTeacher)
    col_kuang = QColor("#EAF9FF");

  painter.setPen(col_kuang);
  painter.setBrush(QBrush(col_kuang));
  painter.drawRoundedRect(name_label_left_rect_, 4, 4);

  // draw name label content

  if ((type_ & UserMe) && !(type_ & UserTeacher))
    penText.setColor(QColor("#FF7C7C"));
  else
    penText.setColor(QColor("#0085FF"));
  painter.setPen(penText);
  option.setWrapMode(QTextOption::WrapAtWordBoundaryOrAnywhere);
  painter.setFont(font_name_);
  painter.drawText(name_label_content_left_rect_, name_label_, option);
}
