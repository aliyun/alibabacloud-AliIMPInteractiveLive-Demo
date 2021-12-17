#ifndef EMOJITABLE_H
#define EMOJITABLE_H
#include <QStringList>
#include <QTableWidget>


class EmojiTable : public QTableWidget {
  Q_OBJECT
public:
  explicit EmojiTable(QWidget* parent = nullptr);
  virtual ~EmojiTable();
  void SetRowCountAndColumnCount(const int32_t, const int32_t);

signals:
  void InsertEmoji(const QString& emojicode);
private:
  virtual bool eventFilter(QObject* target, QEvent* event) override;
private slots:
  void OncellClicked(const int32_t, const int32_t);


private:
  void AddEmoji();
  void InitStyle();
  void InitConnect();
  QStringList emoji_list_;
  QFont font_emoji_;
};

#endif // EMOJITABLE_H
