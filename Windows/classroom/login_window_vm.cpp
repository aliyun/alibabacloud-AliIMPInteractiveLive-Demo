#include "login_window_vm.h"
#include <QJsonArray>
#include <QJsonDocument>
#include <QJsonObject>
#include <condition_variable>
#include <mutex>
#include "api/base_api.h"
#include "common/logging.h"
#include "const/const.h"
#include "meta_space.h"
#include "ui_login_window.h"
#include "view/interface/i_main_window.h"
#include "view/view_component_manager.h"
#include "i_rtc.h"
#include "scheme_login.h"
#include "meta/class_status_type.h"

std::mutex mtx;             // 全局互斥锁
std::condition_variable cr; // 全局条件变量


void LoginWindowVM::UpdateClassType(ClassTypeEnum type) { param_.type = type; }

void LoginWindowVM::UpdateClassRole(ClassRoleEnum role) { param_.role = role; }

void LoginWindowVM::GetLoginToken(alibaba::meta_space::TokenInfo& token_info) {
  bool timeout = false;
  LogWithTag(ClassroomTagLogin, LOG_INFO, "getToken start");
  classroom::blog(LOG_INFO, "start getToken %s", "111");
  BaseApi::GetTokenApi(param_.user_id, token_info, 10000, [&timeout]() { timeout = true; });
  if (timeout == true) {
    param_.error = ClassLoginErrNetworkError;
    ClassVMUpdateField field = Field_ErrorNotify;
    emit SignalUpdateVM(field);
    LogWithTag(ClassroomTagLogin, LOG_ERROR, "getToken timeout");
    return;
  }
  if (token_info.access_token.empty() || token_info.refresh_token.empty()) {
    LogWithTag(ClassroomTagLogin, LOG_ERROR,
               "getToken error token_info.access_token:%s, "
               "token_info.refresh_token:%s",
               token_info.access_token.c_str(), token_info.refresh_token.c_str());
  }
  LogWithTag(ClassroomTagLogin, LOG_INFO, "getToken end");
}


void LoginWindowVM::DoAutoLogin() {
  auto meta_space = alibaba::meta_space::MetaSpace::GetInstance();
  if (meta_space) {
    alibaba::meta_space::TokenInfo token_info;

    //1. 获取toekn
    GetLoginToken(token_info);

    //2. 登录
    LogWithTag(ClassroomTagLogin, LOG_INFO, "Login start. uid:%s", param_.user_id.c_str());
    meta_space->Login(
      param_.user_id, token_info,
      [this]() {
      LogWithTag(ClassroomTagLogin, LOG_INFO, "Login success ");
      //先通过room id获取房间详情
      auto iroom_ptr =
        alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          param_.class_room_id);
      if (iroom_ptr) {
        std::shared_ptr<alibaba::meta_space::IRtc> rtc_plugin =
          std::dynamic_pointer_cast<alibaba::meta_space::IRtc>(iroom_ptr->GetPlugin(alibaba::meta_space::PluginRtc));
        if (rtc_plugin) {
          std::string class_id = SchemeLogin::Instance()->GetSchemeInfo().class_id;
          LogWithTag(ClassroomTagLogin, LOG_INFO, "SceneClassDetail class_id=%s", class_id.c_str());
          rtc_plugin->SceneClassDetail(class_id, [this](const alibaba::sceneclass::GetClassDetailRsp& rsp) {

            bool class_end = (rsp.status == (int32_t)alibaba::meta::ClassStatusType::CLASS_STATUS_END);
            if (class_end ) {
              int32_t field = Field_ErrorNotify;
              param_.error = ClassLoginErrClassEnd;
              emit SignalUpdateVM(field);
              return;
            }
            std::string creator_id = rsp.create_user_id;
            int32_t pos = rsp.create_user_id.find(std::string("@"));
            if (pos != std::string::npos) {
              creator_id = rsp.create_user_id.substr(0, pos);
            }
            if (creator_id == param_.user_id) {
              param_.role = ClassRoleEnum_Teacher;
            }
            else {  //不是房主，则以学生身份登陆
              param_.role = ClassRoleEnum_Student;
            }

            param_.class_room_id = rsp.room_id;
            param_.class_status = rsp.status;
            int32_t field = Field_UpdateRole | Field_GetClassDetailDone;
            emit SignalUpdateVM(field);
            
          }, [this](const alibaba::dps::DPSError& error_msg) {
            classroom::LogError(ClassroomTagLogin, "SceneClassDetail error", error_msg);
            param_.error = ClassLoginErrClassDetail;
            ClassVMUpdateField field = Field_ErrorNotify;
            emit SignalUpdateVM(field);
          });
        }
      }
      
    },
      [this](const alibaba::dps::DPSError& error_msg) {
      param_.error = ClassLoginErrLogin;
      ClassVMUpdateField field = Field_ErrorNotify;
      emit SignalUpdateVM(field);
      classroom::LogError(ClassroomTagLogin, "Login error", error_msg);
    });
  }
}

void LoginWindowVM::DoEnterRoom() {
  auto iroom_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          param_.class_room_id);
  std::string nick = SchemeLogin::Instance()->GetSchemeInfo().nick_name;
  
  LogWithTag(ClassroomTagLogin, LOG_INFO, "EnterRoom start. nick:%s", nick.c_str());
  iroom_ptr->EnterRoom((std::string)(nick),
                       [this, iroom_ptr]() {
                         LogWithTag(ClassroomTagLogin, LOG_INFO, "EnterRoom success");
                         // 不是房主，无法以老师身份进入房间
                         auto room_detail = iroom_ptr->GetRoomDetail();
                         if (param_.role == ClassRoleEnum_Teacher &&
                             room_detail.room_info.owner_id != param_.user_id) {
                           param_.error = ClassLoginErrNotTeacher;
                           ClassVMUpdateField field = Field_ErrorNotify;
                           emit SignalUpdateVM(field);
                           LogWithTag(ClassroomTagLogin, LOG_ERROR, "EnterRoom role type error");
                         } else {
                           ClassVMUpdateField field = Field_ShowMainWnd;
                           emit SignalUpdateVM(field);
                         }
                       },
                       [this](const alibaba::dps::DPSError& error_msg) {
                         param_.error = ClassLoginErrEnterRoom;
                         ClassVMUpdateField field = Field_ErrorNotify;
                         emit SignalUpdateVM(field);
                         classroom::LogError(ClassroomTagLogin, "EnterRoom error", error_msg);
                       });
}

void LoginWindowVM::UpdateClassroomId(const std::string& class_room_id) {
  param_.class_room_id = class_room_id;
}

void LoginWindowVM::UpdateUserId(const std::string& user_id) {
  param_.user_id = user_id;
}

void LoginWindowVM::UpdateAppId(const std::string& app_id) {
  param_.app_id = app_id;
}

ClassroomParam LoginWindowVM::GetClassroomParam() { return param_; }

QString LoginWindowVM::CreateRoom() {
  std::string reply =
      BaseApi::CreateRoomApi(param_.app_id, "classroom", "default", "newclassroom",
                             "nothing",
      param_.user_id, param_.class_room_id);

  QString res = QString::fromStdString(reply);
  QJsonParseError parse_json_err;
  QJsonDocument document =
      QJsonDocument::fromJson(res.toUtf8(), &parse_json_err);
  if (!(parse_json_err.error == QJsonParseError::NoError)) {
    classroom::blog(LOG_ERROR, "Create Room response json parse error");
    return "";
  }

  QJsonObject res_all = document.object();
  bool a = res_all["responseSuccess"].toBool();
  if (!a) {
    classroom::blog(LOG_INFO, "Create Room error, %s", res.toStdString().c_str());
    return "";
  }
  QJsonValue package_val = res_all.value(QStringLiteral("result"));
  QJsonObject package_data = package_val.toObject();
  return package_data["roomId"].toString();
}

void LoginWindowVM::DoLogout() {
  std::unique_lock<std::mutex> lck(mtx);
  auto meta_space = alibaba::meta_space::MetaSpace::GetInstance();
  meta_space->Logout(
      param_.user_id, [this]() { cr.notify_all(); },
      [this](const alibaba::dps::DPSError& error_msg) { cr.notify_all(); });
  cr.wait(lck);
}
