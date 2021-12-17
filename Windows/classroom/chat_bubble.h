#ifndef CHATBUBBLE_H
#define CHATBUBBLE_H

#include <QFont>
#include <QLabel>
#include <QWidget>
#include <vector>

class ChatBubble : public QWidget {
  Q_OBJECT
 public:
  explicit ChatBubble(QWidget* parent = nullptr);
  enum UserType {
    UserSystem = 0x01,  // system
    UserTeacher = 0x02, // teacher
    UserStudent = 0x04, // student
    UserMe = 0x08,      // me
    UserOther = 0x10    // others
  };


 signals:

 public slots:
  void SetText(const QString&, const QString&, const QString&, const uint16_t, const std::string& comment_id);
  QSize AdjRect();

  const QString& GetContent() { return content_; }
  const QString& GetTime() { return time_; }
  const QString& GetUserId() { return userid_; }
  const uint16_t& GetType() { return type_; }
  const std::string GetCommentId() { return comment_id_; }

 protected:
  void paintEvent(QPaintEvent* event);

 private:
  void GetTextSize();
  void GetNameSize();
  void GetNameLabelContentSize();
  int32_t CountEmoji(const QString&);
  QFont font_text_; // text font
  QFont font_name_; // name font

  int32_t text_width_ = 0;                // text rect width
  int32_t text_height_ = 0;               // text rect height
  int32_t name_width_ = 0;                // name rect width
  int32_t name_height_ = 0;               // name rect height
  int32_t name_label_width_ = 0;          // name label rect width
  int32_t name_label_height_ = 0;         // name label rect height
  int32_t name_label_content_width_ = 0;  // name label content rect width
  int32_t name_label_content_height_ = 0; // name label content rect height


  QString content_;    // message content
  QString time_;       // message time
  QString userid_;     // message userid
  QString userid2_;    // rectified userid
  QString name_label_; // name label content
  uint16_t type_;      // message type

  QRect name_left_rect_;
  QRect text_left_rect_;
  QRect name_label_left_rect_;
  QRect name_label_content_left_rect_;
  QStringList my_vec_;
  std::string comment_id_;
};

#endif // CHATBUBBLE_H
