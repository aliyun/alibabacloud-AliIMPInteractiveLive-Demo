#include "emoji_table.h"
#include <QDebug>
#include <QHeaderView>
#include <QLabel>
#include <QScrollBar>
#include <QApplication>

const int32_t kEmojiNum = 80;
const int32_t kEmojiColumn = 4;
const int32_t kTableWidth = 250;
const int32_t kTableHeight = 200;
EmojiTable::EmojiTable(QWidget* parent) : QTableWidget(parent) {
  // set table size
  SetRowCountAndColumnCount(kEmojiNum, kEmojiColumn);
  setMouseTracking(true);
  // set emoji size
  font_emoji_ = QFont();
  font_emoji_.setPixelSize(20);
  this->setFont(font_emoji_);

  // collect emojis
  // add 0x0 to avoid gibberish 0x1F601- 0x1F64F
  char32_t t[] = {0x1F601, 0x0};
  for (char32_t i = 0x1F601; i <= 0x1F64F; ++i) {
    t[0] = i;
    QString emojistr = QString::fromUcs4(t);
    emoji_list_.push_back(emojistr);
  }

  AddEmoji();
  InitStyle();
  InitConnect();
  setEditTriggers(QAbstractItemView::NoEditTriggers);
  installEventFilter(this);
}

EmojiTable::~EmojiTable() { clearContents(); }

void EmojiTable::SetRowCountAndColumnCount(const int32_t totalCount,
                                           const int32_t columnCount) {
  int n_row = totalCount / columnCount;
  if (totalCount % columnCount > 0) ++n_row;
  setRowCount(n_row);
  setColumnCount(columnCount);
}

void EmojiTable::AddEmoji() {
  if (rowCount() == 0 || columnCount() == 0) {
    return;
  }
  int column_count = columnCount();
  int emoji_index = 0;
  int row_index = 0;
  int column_index = 0;
  // fill emojis into the table
  auto it = emoji_list_.begin();
  while (it != emoji_list_.end()) {
    row_index = emoji_index / column_count;
    column_index = emoji_index % column_count;
    setItem(row_index, column_index, new QTableWidgetItem(*it));
    ++emoji_index;
    ++it;
  }
}

void EmojiTable::InitStyle() {
  setFixedSize(kTableWidth, kTableHeight);
  verticalHeader()->setVisible(false);
  horizontalHeader()->setVisible(false);
  horizontalHeader()->setSectionResizeMode(QHeaderView::Stretch);
  verticalHeader()->setSectionResizeMode(QHeaderView::Stretch);
  setShowGrid(false);
  setObjectName("BaseEmojiWgt");
  setStyleSheet(
      "QLabel:hover{border:1px solid #caced1;}"
      "QTableWidget#BaseEmojiWgt{border:1px solid #D8DCE0;}"
      "QTableWidget::item:selected{background-color:white;outline:0px;}");

  horizontalScrollBar()->setStyleSheet("QScrollBar{height:10px;}");
  setHorizontalScrollMode(QAbstractItemView::ScrollPerPixel);
  verticalScrollBar()->setStyleSheet("QScrollBar{width:10px;}");
  setVerticalScrollMode(QAbstractItemView::ScrollPerPixel);
}

void EmojiTable::InitConnect() {
  connect(this, &EmojiTable::cellClicked, this, &EmojiTable::OncellClicked);
}

void EmojiTable::OncellClicked(const int32_t row, const int32_t column) {
  if (row < 0 || column < 0 || row >= rowCount() || column >= columnCount()) {
    return;
  }
  QString emoji = item(row, column)->text();
  if (!emoji.isEmpty()) {
    hide();
    emit InsertEmoji(emoji);
  }
}

bool EmojiTable::eventFilter(QObject* target, QEvent* event) {
  // window not used, become deactivate
  if (QEvent::WindowDeactivate == event->type()) {
    QWidget* activeWnd = QApplication::activeWindow();
    if (activeWnd == this) {
      return false;
    }

    hide();
    return true;
  }

  return false;
}

