#ifndef CHATWIDGET_H
#define CHATWIDGET_H

#include <QListWidget>
#include <QPushButton>
#include <QString>
#include <QWidget>
#include "chat_bubble.h"
#include "chat_vm.h"
#include "emoji_table.h"
#include <unordered_set>


namespace Ui {
class ChatWidget;
}

class ChatWidget : public QWidget {
  struct ChatMsgItemModel
  {
    std::string creator_open_id;
    std::string content;
    int64_t creat_at = 0;
    uint16_t msg_type = 0;
    std::string comment_id;
  };
  Q_OBJECT

 public:
  explicit ChatWidget(QWidget* parent = nullptr);
  ~ChatWidget();
  void UpdateChatInfo(const ChatParam&);
  void SetMaxLen(const int32_t);

 protected:
  virtual void ChatWidget::resizeEvent(QResizeEvent* event);
  bool eventFilter(QObject* target, QEvent* event);
  void SyncInsertMsg(const std::string&,
    const std::string&,
    const int64_t,
    const uint16_t,
    const std::string&);
 private slots:
  void InitCheckBox();
  void on_sendBtn_clicked();
  void onBoxStatusChanged(int);
  void on_emojiBtn_clicked();
  void on_inputTextEdit_textChanged();

  void OnGetMessageFromVM(const std::string&,
                          const std::string&,
                          const int64_t,
                          const uint16_t,
                          const std::string&,
                          bool sync);
  void OnGetMuteAllFromVM(const bool);
  void OnGetMuteUserFromVM(const bool);
  void InitAfterCreate();
  void DealMessage(ChatBubble* bubble,
                   QListWidgetItem* item,
                   const QString& msg,
                   const QString& time,
                   const QString& username,
                   const uint16_t mytype,
                   const std::string& comment_id);

  void OnReceiveEmoji(const QString&);
  void RectifyEmojiTable();
  void OnTimerInsertMsg();
  void OnScrollChange(int pos);
 private:
  bool is_muted_ = false;
  Ui::ChatWidget* ui;
  std::shared_ptr<ChatVM> vm_ptr_;
  EmojiTable* emoji_table_ptr_ = nullptr;
  QTimer* insert_msg_timer_ = nullptr;
  std::list<ChatMsgItemModel> msg_list_;
  std::unordered_set<std::string> msg_filter_set_;
  int32_t send_msg_length_limit_ = 50;
  int32_t send_msg_interval_limit_ = 1000;
  int64_t last_send_msg_timestamp_ = 0;
  bool msg_list_bottom_ = true;
  // QPushButton* mute_button_ptr_ = nullptr;
};

#endif // CHATWIDGET_H
