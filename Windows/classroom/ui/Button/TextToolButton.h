#pragma once

#include <QPushButton>
#include <QToolButton>
#include "ui/UiDataDefine.h"
#include "ui/Others/HintDotDecorator.h"
namespace Ui { class TextToolButton; };

class QLabel;

namespace dtguitoolkit {
  class MenuWidget;

  class TextToolButton : public QToolButton
  {
    Q_OBJECT

  public:
    TextToolButton(QWidget *parent = Q_NULLPTR);
    ~TextToolButton();

    void SetText(const QString& text);
    void SetIcon(const QChar& iconfont_code, int font_size);
    void SetIconFontList(const QVector<IconData>& icon_list);
    // more button
    void SetMoreBtnVisible(bool visible);
    // menu
    void SetMenu(dtguitoolkit::MenuWidget* menu);
    void SetMenu(QMenu* menu);

    /// customized feature
    // pulse
    void EnablePulseMode(bool enable);
    void SetPulseValue(float value);
    // dot hint
    void SetDotMode(HintDotDecorator::HintDotType type);
    void SetDotVisible(bool visible);

  protected:
    bool eventFilter(QObject *watched, QEvent *event) override;
    void resizeEvent(QResizeEvent* event) override;
    void paintEvent(QPaintEvent* event) override;

  private:
    void InitUi();
    void ResetCheckStatus();
    // more btn
    void InitMoreBtn();
    void ResetMoreBtnCheckStatus();

  signals:
    void SignalShowMore(bool checked);

  private:
    Ui::TextToolButton *ui;
    QPushButton* btn_more_ = nullptr;
    bool enable_pulse_ = false;
    HintDotDecorator* hint_dot_decorator_ = nullptr;
  };
}