#pragma once
#include <QObject>
#include <memory>
#include <unordered_map>


class RtcUserDataMgr : public QObject,
  public std::enable_shared_from_this<RtcUserDataMgr> {
  Q_OBJECT
signals:
  void SignalVideoMute(const std::string& uid, bool mute);
  void SignalAudioMute(const std::string& uid, bool mute);
public:
  static std::shared_ptr<RtcUserDataMgr> GetInstance();
  RtcUserDataMgr();
  //update in ui thread
  void UpdateVideoMute(const std::string& uid, bool mute);
  void UpdateAudioMute(const std::string& uid, bool mute);
  bool GetVideoMute(const std::string& uid);
  bool GetAudioMute(const std::string& uid);
private slots:
  void OnUpdateVideoMute(const std::string& uid, bool mute);
  void OnUpdateAudioMute(const std::string& uid, bool mute);
private:
  std::unordered_map<std::string, bool> video_mute_status_;
  std::unordered_map<std::string, bool> audio_mute_status_;
};
