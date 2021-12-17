#include "chat_widget.h"
#include <QDebug>
#include <QScrollBar>
#include "emoji_table.h"
#include "ui_chat_widget.h"
#include "view/view_component_manager.h"
#include "view/interface/i_toast_widget.h"
#include "common/icon_font_helper.h"


ChatWidget::ChatWidget(QWidget* parent)
    : QWidget(parent), ui(new Ui::ChatWidget) {
  ui->setupUi(this);
  ui->sendBtn->setEnabled(false);
  // setWindowFlags(Qt::FramelessWindowHint);  // remove window head
  vm_ptr_ = std::make_shared<ChatVM>();
  InitCheckBox();
  connect(vm_ptr_.get(), &ChatVM::SendMsgToClient, this,
          &ChatWidget::OnGetMessageFromVM); // receive chat message
  connect(vm_ptr_.get(), &ChatVM::SendMuteAllToClient, this,
          &ChatWidget::OnGetMuteAllFromVM); // receive mute/cancel mute message
  connect(vm_ptr_.get(), &ChatVM::SendMuteUserToClient, this,
          &ChatWidget::OnGetMuteUserFromVM); // receive mute/cancel mute message
  ui->chatWnd->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  ui->chatWnd->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
  ui->chatWnd->setVerticalScrollMode(QAbstractItemView::ScrollPerPixel);
  ui->inputTextEdit->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);


  emoji_table_ptr_ = new EmojiTable(this);
  connect(emoji_table_ptr_, &EmojiTable::InsertEmoji, this,
          &ChatWidget::OnReceiveEmoji);
  RectifyEmojiTable();
  emoji_table_ptr_->hide();

  ui->inputTextEdit->installEventFilter(this);
  ui->picBtn->setVisible(false);
  insert_msg_timer_ = new QTimer(this);
  connect(insert_msg_timer_, &QTimer::timeout, this, &ChatWidget::OnTimerInsertMsg);
  connect(ui->chatWnd->verticalScrollBar(), SIGNAL(valueChanged(int)),
    this, SLOT(OnScrollChange(int)));
}

ChatWidget::~ChatWidget() { delete ui; }

void ChatWidget::SetMaxLen(const int32_t lenn) { this->send_msg_interval_limit_ = lenn; }

void ChatWidget::InitCheckBox() {
  ui->checkBox->setCheckState(Qt::Unchecked);
  connect(ui->checkBox, &QCheckBox::stateChanged, this,
          &ChatWidget::onBoxStatusChanged);
}

void ChatWidget::resizeEvent(QResizeEvent* event) {
  for (int i = 0; i < ui->chatWnd->count(); i++) {
    ChatBubble* messageW =
        (ChatBubble*)ui->chatWnd->itemWidget(ui->chatWnd->item(i));
    QListWidgetItem* item = ui->chatWnd->item(i);

    DealMessage(messageW, item, messageW->GetContent(), messageW->GetTime(),
                messageW->GetUserId(), messageW->GetType(), messageW->GetCommentId());
  }
  RectifyEmojiTable();
  if (emoji_table_ptr_->isVisible()) {
    emoji_table_ptr_->hide();
    emoji_table_ptr_->show();
  }
}

bool ChatWidget::eventFilter(QObject* target, QEvent* event) {
  if (target == ui->inputTextEdit) {
    if (event->type() == QEvent::KeyPress) {
      QKeyEvent* k = static_cast<QKeyEvent*>(event);
      // ctrl+enter = '\n'
      if (k->key() == Qt::Key_Return &&
          (k->modifiers() & Qt::ControlModifier)) {
        ui->inputTextEdit->insertPlainText("\n");
        return true;
      }
      // enter = send
      if (k->key() == Qt::Key_Return) {
        on_sendBtn_clicked();
        return true;
      }
    }
  }
  return QWidget::eventFilter(target, event);
}

void ChatWidget::DealMessage(ChatBubble* bubble,
                             QListWidgetItem* item,
                             const QString& msg,
                             const QString& time,
                             const QString& username,
                             const uint16_t mytype,
                             const std::string& comment_id) {
  bubble->setFixedWidth(this->width());
  bubble->SetText(msg, time, username, mytype, comment_id);
  QSize size = bubble->AdjRect();
  item->setSizeHint(size);
  bubble->update();
  ui->chatWnd->setItemWidget(item, bubble);
}

void ChatWidget::UpdateChatInfo(const ChatParam& msg) {
  vm_ptr_->UpdateClassroomParam(msg);
  InitAfterCreate();
}

void ChatWidget::InitAfterCreate() {
  if (vm_ptr_->GetChatParam().role == ClassRoleEnum_Teacher)
    ui->checkBox->setText(QTranslate("InteractiveClass.AllMute"));
  else {
    ui->checkBox->setText(QTranslate("InteractiveClass.OnlyTeacher"));
    ui->checkBox->setVisible(false);  // todo 后面再补充
  }
}

void ChatWidget::on_sendBtn_clicked() {
  int64_t current_ms = QDateTime::currentDateTime().currentMSecsSinceEpoch();
  if (current_ms - last_send_msg_timestamp_ < send_msg_interval_limit_) {
    return;
  }
  // tell the server through rpc
  QString tmp = ui->inputTextEdit->toPlainText();
  tmp = tmp.trimmed();
  ui->inputTextEdit->setPlainText("");
  if (tmp.isEmpty()) {
    return;
  }
  vm_ptr_->UploadMsgToServer(tmp.toStdString());
  last_send_msg_timestamp_ = current_ms;
}

void ChatWidget::onBoxStatusChanged(int state) {
  if (vm_ptr_->GetChatParam().role == ClassRoleEnum_Teacher) {
    if (state == Qt::Checked) {
      vm_ptr_->UpLoadMuteAllToServer();
    } else if (state == Qt::Unchecked) {
      vm_ptr_->UploadCancelMuteAllToServer();
    }
  } else if (vm_ptr_->GetChatParam().role == ClassRoleEnum_Student) {
    // TODO
  }
}


void ChatWidget::on_emojiBtn_clicked() {
  RectifyEmojiTable();
  if (emoji_table_ptr_->isVisible())
    emoji_table_ptr_->hide();
  else
    emoji_table_ptr_->show();
}

void ChatWidget::RectifyEmojiTable() {
  QRect chatrect = ui->chatWnd->geometry();
  QRect emojirect = emoji_table_ptr_->geometry();
  emoji_table_ptr_->setGeometry(
      chatrect.x(), chatrect.height() + chatrect.y() - emojirect.height(),
      emojirect.width(), emojirect.height());
}


void ChatWidget::on_inputTextEdit_textChanged() {
  QString mychat = ui->inputTextEdit->toPlainText();
  if (mychat.length() == 0 || mychat.length() == mychat.count('\n')) {
    ui->sendBtn->setEnabled(false);
    return;
  }

  int length = mychat.count();

  if (length > send_msg_interval_limit_) {
    int position = ui->inputTextEdit->textCursor().position();
    QTextCursor textCursor = ui->inputTextEdit->textCursor();
    mychat.remove(position - (length - send_msg_interval_limit_), length - send_msg_interval_limit_);
    ui->inputTextEdit->setPlainText(mychat);
    textCursor.setPosition(position - (length - send_msg_interval_limit_));
    ui->inputTextEdit->setTextCursor(textCursor);
  }
  ui->sendBtn->setEnabled(true);
}


void ChatWidget::OnGetMessageFromVM(const std::string& creator_open_id,
                                    const std::string& content,
                                    const int64_t creat_at,
                                    const uint16_t msg_type,
                                    const std::string& comment_id,
                                    bool sync) {
  if (sync) {
    return SyncInsertMsg(creator_open_id, content, creat_at, msg_type, comment_id);
  }
  bool self = creator_open_id == vm_ptr_->GetChatParam().user_id;
  if (!self && msg_list_.size() > 200) {
    return;
  } else {
    ChatMsgItemModel model;
    model.creator_open_id = creator_open_id;
    model.content = content;
    model.creat_at = creat_at;
    model.msg_type = msg_type;
    model.comment_id = comment_id;

    msg_list_.push_back(std::move(model));

    if (!insert_msg_timer_->isActive()) {
      insert_msg_timer_->start(300);
    }
  }
  
}


void ChatWidget::SyncInsertMsg(const std::string& creator_open_id,
  const std::string& content,
  const int64_t creat_at,
  const uint16_t msg_type,
  const std::string& comment_id) {
  ChatBubble* mybubble = new ChatBubble();
  QListWidgetItem* myitem = new QListWidgetItem(ui->chatWnd);
  DealMessage(mybubble, myitem, QString::fromStdString(content),
    QString::number(creat_at),
    QString::fromStdString(creator_open_id), msg_type, comment_id);
  ui->chatWnd->setCurrentRow(ui->chatWnd->count() - 1);
  msg_filter_set_.insert(comment_id);
}


void ChatWidget::OnTimerInsertMsg() {
  // show chat message
  if (msg_list_.size() > 0) {

    const int max_count_limit = 200;
    int count = ui->chatWnd->count();
    while (count > max_count_limit) {
      QListWidgetItem * item = ui->chatWnd->takeItem(0);
      ChatBubble * dubble = dynamic_cast<ChatBubble*>(ui->chatWnd->itemWidget(item));
      if (dubble) {
        msg_filter_set_.erase(dubble->GetCommentId());
      }
      delete item;
      count = ui->chatWnd->count();
    }

    ChatMsgItemModel item = msg_list_.front();
    msg_list_.pop_front();

    if (msg_filter_set_.count(item.comment_id) > 0) {
      return;
    }

    ChatBubble* mybubble = new ChatBubble();
    QListWidgetItem* myitem = new QListWidgetItem(ui->chatWnd);
    DealMessage(mybubble, myitem, QString::fromStdString(item.content),
      QString::number(item.creat_at),
      QString::fromStdString(item.creator_open_id), item.msg_type, item.comment_id);
    
    msg_filter_set_.insert(item.comment_id);

    if (msg_list_bottom_) {
      ui->chatWnd->setCurrentRow(ui->chatWnd->count() - 1);
    }

  }
  if (msg_list_.size() == 0 && insert_msg_timer_->isActive()) {
    insert_msg_timer_->stop();
  }
}

void ChatWidget::OnGetMuteAllFromVM(const bool mute) {
  if (vm_ptr_->GetChatParam().role == ClassRoleEnum_Teacher) return;
  is_muted_ = mute;
  if (is_muted_)
    ui->inputTextEdit->setPlainText(QTranslate("InteractiveClass.AllMuting"));
  else
    ui->inputTextEdit->setPlainText("");
  ui->inputTextEdit->setDisabled(is_muted_);
  ui->emojiBtn->setDisabled(is_muted_);
  ui->sendBtn->setDisabled(is_muted_);

  if (mute) {
    GetViewComponent<IToastWidget>(kToastWindow)->ShowFailedToast(QTranslate("InteractiveClass.AdminMuteOn"));
  }
}

void ChatWidget::OnGetMuteUserFromVM(const bool mute) {}

void ChatWidget::OnReceiveEmoji(const QString& emoji) {
  ui->inputTextEdit->insertPlainText(emoji);
}

void ChatWidget::OnScrollChange(int pos) {
  if (pos != ui->chatWnd->verticalScrollBar()->maximum()) {
    msg_list_bottom_ = false;
  }
  else {
    msg_list_bottom_ = true;
  }
}