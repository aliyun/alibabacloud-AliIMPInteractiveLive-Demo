#include "TextToolButton.h"
#include "ui_TextToolButton.h"
#include "ui/Menu/MenuWidget.h"
#include "iconfont/icon_font_helper_common.h"

namespace dtguitoolkit {
  const int kMoreBtnIconFontSize = 10;
  const QSize kMoreBtnSize = QSize(16, 28);
  const QPoint kPulseOffset = QPoint(10, 6);

  TextToolButton::TextToolButton(QWidget *parent)
    : QToolButton(parent)
  {
    ui = new Ui::TextToolButton();
    ui->setupUi(this);

    InitUi();
  }

  TextToolButton::~TextToolButton()
  {
    delete ui;
  }

  void TextToolButton::SetText(const QString& text) {
    ui->labelText->setText(text);
  }

  void TextToolButton::SetIcon(const QChar& iconfont_code, int font_size) {
    if (enable_pulse_) {
      IconFontHelperCommon::instance()->SetIcon<QLabel>(ui->labelPulseIcon, iconfont_code, font_size);
    } else {
      IconFontHelperCommon::instance()->SetIcon<QLabel>(ui->labelIcon, iconfont_code, font_size);
    }
  }

  void TextToolButton::SetMoreBtnVisible(bool visible) {
    btn_more_->setVisible(visible);
  }

  void TextToolButton::EnablePulseMode(bool enable) {
    enable_pulse_ = enable;
    ui->labelIcon->setVisible(!enable);
    ui->labelPulseIcon->setVisible(enable);
    ui->labelPulseIcon->SetPulsePos(kPulseOffset);
  }

  void TextToolButton::SetPulseValue(float value) {
    ui->labelPulseIcon->SetPulseValue(value);
  }

  void TextToolButton::SetDotMode(HintDotDecorator::HintDotType type) {
    if (!hint_dot_decorator_) {
      hint_dot_decorator_ = new HintDotDecorator(this);
    }

    hint_dot_decorator_->InitDecorator(type);
  }

  void TextToolButton::SetDotVisible(bool visible) {
    if (hint_dot_decorator_) {
      hint_dot_decorator_->SetVisible(visible);
    }
  }

  void TextToolButton::SetIconFontList(const QVector<IconData>& icon_list) {
    ui->labelIcon->SetMoreIconList(icon_list);
    ui->labelPulseIcon->SetMoreIconList(icon_list);
  }

  void TextToolButton::SetMenu(dtguitoolkit::MenuWidget* menu) {
    if (!menu) {
      return;
    }
    connect(menu, &dtguitoolkit::MenuWidget::SignalDeactiveHide, this, &TextToolButton::ResetMoreBtnCheckStatus);
    connect(menu, &dtguitoolkit::MenuWidget::SignalDeactiveHide, this, &TextToolButton::ResetCheckStatus);
  }

  void TextToolButton::SetMenu(QMenu* menu) {
    if (!menu) {
      return;
    }
    connect(menu, &QMenu::aboutToHide, btn_more_, &QPushButton::toggle);
    connect(menu, &QMenu::aboutToHide, this, &TextToolButton::ResetCheckStatus);
  }

  bool TextToolButton::eventFilter(QObject *watched, QEvent *event) {
    if (watched == btn_more_) {
      switch (event->type()) {
        case QEvent::Enter:
        case QEvent::Leave:
          update();
          break;
        default:
          break;
      }
    }
    return QToolButton::eventFilter(watched, event);
  }

  void TextToolButton::InitUi() {
    InitMoreBtn();
    SetMoreBtnVisible(false);
    EnablePulseMode(false);
  }

  void TextToolButton::InitMoreBtn() {
    btn_more_ = new QPushButton(this);
    btn_more_->setFixedSize(kMoreBtnSize);
    btn_more_->setObjectName("btnMore");
    btn_more_->installEventFilter(this);
    btn_more_->setCheckable(true);
    IconFontHelperCommon::instance()->SetIcon<QPushButton>(btn_more_, KIconDownArrowTriAngle, kMoreBtnIconFontSize);
    connect(btn_more_, &QPushButton::toggled, this, [=](bool checked) {
        QChar code = checked ? KIconUpArrowTriAngle : KIconDownArrowTriAngle;
        IconFontHelperCommon::instance()->SetIcon<QPushButton>(btn_more_, code, kMoreBtnIconFontSize);
    });
    connect(btn_more_, &QPushButton::clicked, this, &TextToolButton::SignalShowMore);
  }

  void TextToolButton::ResetMoreBtnCheckStatus() {
    if (!btn_more_->underMouse()) {
      btn_more_->setChecked(false);
    }
  }

  void TextToolButton::ResetCheckStatus() {
    if (!btn_more_->underMouse()) {
      setChecked(false);
    }
  }

  void TextToolButton::resizeEvent(QResizeEvent* event) {
    if (btn_more_) {
      static int kPadding = 2;
      if (enable_pulse_) {
        btn_more_->move(QPoint(ui->labelPulseIcon->geometry().right() + kPadding, geometry().top()));
      } else {
        btn_more_->move(QPoint(ui->labelIcon->geometry().right() + kPadding, geometry().top()));
      }
    }
    if (hint_dot_decorator_) {
      static int kPadding = -2;
      if (enable_pulse_) {
        hint_dot_decorator_->AdjustHintDotPos(
                QPoint(ui->labelPulseIcon->geometry().right() + kPadding, geometry().top()));
      } else {
        hint_dot_decorator_->AdjustHintDotPos(QPoint(ui->labelIcon->geometry().right() + kPadding, geometry().top()));
      }
    }
  }

  void TextToolButton::paintEvent(QPaintEvent* event) {
    QStyleOption opt;
    opt.init(this);
    if (btn_more_ && btn_more_->underMouse()) {
      opt.state &= ~QStyle::State_MouseOver;
    }
    QPainter p(this);
    style()->drawPrimitive(QStyle::PE_Widget, &opt, &p, this);
  }
}
