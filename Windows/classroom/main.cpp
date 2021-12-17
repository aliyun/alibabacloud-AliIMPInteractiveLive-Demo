#include <shlobj_core.h>
#include <windows.h>
#include <QApplication>
#include <fstream>

#include "chat_widget.h"
#include "classroom.h"
#include "common/logging.h"
#include "const/const.h"
#include "login_window.h"
#include "main_window.h"
#include "meta_space.h"
#include "prm_map.h"
#include "scheme_login.h"
#include "util/log_util.h"
#include "view/view_component_manager.h"
#include "windows.h"
#include "QTranslator"
#include "classroom_application.h"
#include "QMessageBox"
#include "QFileInfo"
#include "QDir"



using namespace alibaba::meta_space;

extern std::string GetDeviceId();
namespace classroom {
  extern std::string GetConfigPath();
}


#ifdef __cplusplus
extern "C" {
#endif
  int main(int argc, char* argv[]) {
    // initialize commandline map
    PrmMap pm(argc, argv);
   
    ClassroomApplication a(argc, argv);
    a.InitLocale();
    ClassroomTranslator translater(&a);
    a.installTranslator(&translater);
    auto meta_space = MetaSpace::GetInstance();
    if (meta_space) {
      MetaSpaceContext ctx;

      ctx.app_id = Const::kAppId;
      ctx.app_key = Const::kAppKey;
      
      ctx.device_id = GetDeviceId();
      QString path = QDir::currentPath()+ "/dps";
      std::string path_str = path.toStdString();
      QDir dir(path);
      
      if (!dir.exists()) {
        bool mk_success = dir.mkdir(path);
      }
      ctx.data_path = path.toStdString();
      ctx.env = (int32_t)alibaba::meta::EnvType::ENV_ONLINE;
     
      meta_space->Init(ctx, []() { LogUtil::Debug("meta_space init success"); },
        [&](const alibaba::dps::DPSError& err) {
        classroom::blog(LOG_ERROR, "meta_space init failed, %s", err.reason.c_str());
      });
      meta_space->SetLogHandler(alibaba::meta::LogLevel::LOG_LEVEL_DEBUG,
        [](alibaba::meta::LogLevel log_level,
          const std::string& log_content) {
         classroom::blog(LOG_INFO, log_content.c_str());
        
      });
    }
    QGuiApplication::setAttribute(Qt::AA_UseHighDpiPixmaps);

    GetViewComponent<QWidget>(kToastWindow);
    auto login_window = GetViewComponent<QWidget>(kLoginWindow);
    login_window->show();
    a.exec();

    meta_space->SetLogHandler(alibaba::meta::LogLevel::LOG_LEVEL_DEBUG, nullptr);
    meta_space->Uninit();
    return 0;
  }
#ifdef __cplusplus
}
#endif