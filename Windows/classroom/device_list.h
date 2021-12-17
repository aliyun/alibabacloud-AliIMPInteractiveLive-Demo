#pragma once

#include <QDialog>
#include <QLabel>
#include <QListWidget>
#include <QMenu>
#include <string>
#include <vector>

enum DeviceTypeEnum {
  DeviceTypeEnum_Audio,
  DeviceTypeEnum_Video,
};

class DeviceListVM;
class DeviceList : public QObject {
  Q_OBJECT
public:
  DeviceList(QWidget* parent);
  ~DeviceList();

  void Init(DeviceTypeEnum type);
  void ShowDeviceList(const QPoint& pos, bool is_up = true);
  void HideDeviceList();
  QRect Geometry();
  bool IsVisible();
  void UpdateClassroomId(const std::string& id);
signals:
  void SignalHide(DeviceTypeEnum type);
protected:
  virtual bool eventFilter(QObject* target, QEvent* event) override;

private slots:
  void OnItemClicked(QListWidgetItem* item);
private:
  void InitDeviceList();
  void AddDeviceListItem(const QString& text, const int index, bool checked);

  DeviceTypeEnum type_ = DeviceTypeEnum_Audio;
  bool inited_ = false;
  QDialog device_list_;
  QListWidget* list_ = nullptr;
  std::vector<QListWidgetItem*> list_item_;
  std::vector<QLabel*> check_icons_;
  std::shared_ptr<DeviceListVM> vm_;
};

