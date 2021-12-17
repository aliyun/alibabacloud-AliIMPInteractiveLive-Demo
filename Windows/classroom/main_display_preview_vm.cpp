#include "main_display_preview_vm.h"
#include "meta_space.h"
#include "i_white_board.h"
#include <algorithm>
#include <QFileDialog>
#include "common/icon_font_helper.h"
#include <alibabacloud/oss/OssClient.h>
#include "i_doc.h"
#include "doc/report_upload_status_req.h"
#include "common/logging.h"
#include "doc/create_doc_conversion_task_req.h"
#include "doc/create_doc_conversion_task_rsp.h"
#include "doc/get_doc_req.h"
#include "doc/get_doc_rsp.h"
#include "doc/create_doc_rsp.h"
#include "doc/create_doc_req.h"
#include "yield_helper.h"
#include <chrono>
#include "QString"
#include <set>
#include "event/event_manager.h"
#include <QJsonObject>
#include <QJsonArray>
#include <QFile>
#include <QJsonDocument>

using namespace alibaba::meta;
using namespace alibaba::meta_space;
using namespace alibaba::dps;



struct NaitveCallbackRequest {
  std::string session;
};

struct  NaitveCallbackResponse {
  std::string src;
  std::string type;
};

struct NativeCallbackItem {
  NaitveCallbackRequest request;
  NaitveCallbackResponse response;
};



std::string ToJson(const NativeCallbackItem& item) {

  QJsonObject obj_callback;
  QJsonObject obj_request;
  QJsonObject obj_response;
  obj_request.insert("session", item.request.session.c_str());
  obj_response.insert("src", item.response.src.c_str());
  obj_response.insert("type", item.response.type.c_str());
  obj_callback.insert("request", obj_request);
  obj_callback.insert("response", obj_response);
  return QString(QJsonDocument(obj_callback).toJson(QJsonDocument::Compact)).toStdString();
}


UrlReplaceItem FromJson(const std::string& json) {
  QJsonDocument json_document = QJsonDocument::fromJson(json.c_str());
  if (json_document.isNull()) {
    return UrlReplaceItem();
  }
  UrlReplaceItem item;
  QJsonObject json_object = json_document.object();
  QJsonValue src_value = json_object["src"];
  item.url = src_value.toString().toStdString();
  QJsonValue type_value = json_object["type"];
  item.type = type_value.toString().toStdString();
  QJsonValue session_value = json_object["session"];
  item.session = session_value.toString().toStdString();
  return item;
}


class OssCredentialsProvider : public AlibabaCloud::OSS::CredentialsProvider {
 public:
  OssCredentialsProvider(const std::string& app_key_id,
                         const std::string& app_key_sec,
                         const std::string& app_key_token)
      : access_key_id_(app_key_id),
        access_key_secret_(app_key_sec),
        secret_token_(app_key_token) {}
  virtual AlibabaCloud::OSS::Credentials getCredentials() override {
    return AlibabaCloud::OSS::Credentials(access_key_id_, access_key_secret_,
                                          secret_token_);
  }

 private:
  std::string access_key_id_;
  std::string access_key_secret_;
  std::string secret_token_;
};

void MainDisplayPreviewVM::OnBnClickedButtonClear() {
  std::string api_name = "clearBoard";
  std::string api_param = "";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonPenNormal() {
  std::string api_name = "setToolType";
  std::string api_param = "pen";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonUndo() {
  std::string api_name = "undo";
  std::string api_param = "";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonRedo() {
  std::string api_name = "redo";
  std::string api_param = "";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonPenLaserPen() {
  std::string api_name = "setToolType";
  std::string api_param = "laser";
  ExecuteCommand(api_name, api_param);
#ifdef DEBUG
  auto iroom_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->ShowDevTools();
  }
#endif // DEBUG

}


void MainDisplayPreviewVM::OnBnClickedButtonPenLaserPenNew() {
  std::string api_name = "setToolType";
  std::string api_param = "laserPen";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonPenCircle() {
  std::string api_name = "setToolType";
  std::string api_param = "circle";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonPenRect() {
  std::string api_name = "setToolType";
  std::string api_param = "rect";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonRoundedRect() {
  std::string api_name = "setToolType";
  std::string api_param = "roundRect";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonTriangle() {
  std::string api_name = "setToolType";
  std::string api_param = "triangle";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonRightTriangle() {
  std::string api_name = "setToolType";
  std::string api_param = "rightTriangle";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonPenText() {
  std::string api_name = "setToolType";
  std::string api_param = "text";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonStraightLine() {
  std::string api_name = "setToolType";
  std::string api_param = "straight";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::OnBnClickedButtonPenSelect() {
  std::string api_name = "setToolType";
  std::string api_param = "pointer";
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::OnBnClickedButtonZoomOut() {
  model_.scale += 0.1;
  std::string api_name = "setScale";
  std::string api_param = std::to_string(model_.scale);
  ExecuteCommand(api_name, api_param);
  GetScale();
}


void MainDisplayPreviewVM::UpdateScale(double scale) {
  model_.scale = scale;
  std::string api_name = "setScale";
  std::string api_param = std::to_string(model_.scale);
  ExecuteCommand(api_name, api_param);
  GetScale();
}

void MainDisplayPreviewVM::OnBnClickedButtonZoomIn() {
  model_.scale -= 0.1;
  if (model_.scale <= 1e-6) {
    model_.scale = 0.1;
  }
  std::string api_name = "setScale";
  std::string api_param = std::to_string(model_.scale);
  ExecuteCommand(api_name, api_param);
  GetScale();
}


void MainDisplayPreviewVM::OnBnClickedButtonNext() {

  if (model_.doc_current_page + 1 <= model_.doc_total_page) {
    DocGotoPage(model_.doc_current_page + 1);
  }
}

void MainDisplayPreviewVM::OnBnClickedButtonPre() {
  if (model_.doc_current_page > 1) {
    DocGotoPage(model_.doc_current_page - 1);
  }
}

void MainDisplayPreviewVM::OnBnClickedButtonFirst() {
  DocGotoPage(1);
}

void MainDisplayPreviewVM::OnBnClickedButtonLast() {
  DocGotoPage(model_.doc_total_page);
}
void MainDisplayPreviewVM::DocGotoPage(
    const uint32_t doc_page, std::function<void()> finish_cb) {

  if (!model_.current_doc_id.empty()) {

    bool ok = false;
    uint32_t wb_page_index = 0;

    for (const DocInfoContext& doc_info : model_.all_doc_info) {
      for (const DocPageInfoContext& page_info : doc_info.doc_page_info) {
        if (doc_info.doc_id == model_.current_doc_id) {
          if (page_info.group_page == doc_page) {
            ok = true;
            wb_page_index = page_info.wb_page - 1;
            break;
          }
        }
      }
    }

    if (ok) {
      WhiteboardGoToPage(wb_page_index);
    }
  }
}
void MainDisplayPreviewVM::WhiteboardGotoLastPage(std::function<void()> finish_cb)
{
  auto final_cb = [this, finish_cb](int32_t current_page, int32_t total_page) {
    model_.wb_total_page = total_page;
    model_.wb_current_page_index = current_page;
    if (finish_cb) finish_cb();
  };

  GetPageInfo([this, final_cb](int32_t current_page, int32_t total_page) {
    if (current_page != total_page - 1) {
      std::string api_name = "gotoScene";
      ExecuteCommand(api_name, std::to_string(total_page - 1));
      GetPageInfo([this, final_cb](int32_t current_page, int32_t total_page) {
        final_cb(current_page, total_page);
      });
    } else {
      final_cb(current_page, total_page);
    }
  });
}


void MainDisplayPreviewVM::OnBnClickedButtonAddPage() {

  if (model_.current_doc_id == "wb") {
    boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::CoroutineWhiteboardDocAddPage, this, std::placeholders::_1));
  }
  else {
    // TODO, 提示
  }
}

void MainDisplayPreviewVM::OnBnClickedButtonErase() {
  std::string api_name = "setToolType";
  std::string api_param = "eraser";
  ExecuteCommand(api_name, api_param);
}

bool MainDisplayPreviewVM::CoroutineUpdateDocStatus(
    boost::asio::yield_context yield,
    const std::string& doc_id,
    DocUploadStatus status) {

  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IDoc> doc_plugin =
        std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    alibaba::doc::ReportUploadStatusReq req;
    req.doc_id = doc_id;
    req.upload_status = (int32_t)status;
    doc_plugin->ReportUploadStatus(
        req,
        [status, &ctx](const alibaba::doc::ReportUploadStatusRsp& rsp) {
          ctx.resume(boost::system::error_code(), true);
          classroom::blog(LOG_INFO, "report upload status, %d",
                          (int32_t)status);
        },
        [status, &ctx](const alibaba::dps::DPSError& err) {
          ctx.resume(boost::system::error_code(), false);
          classroom::blog(LOG_ERROR, "report upload status failed, status=%",
                          (int32_t)status);
        });
  } else {
    return false;
  }

  return ctx.result();
}

bool MainDisplayPreviewVM::CoroutineCreateDoc(
    boost::asio::yield_context yield,
    const QString& file_path,
    bool private_oss,
    alibaba::doc::CreateDocRsp* out_rsp,
    alibaba::dps::DPSError* dps_error) {
  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model_.room_id);
  if (iroom_ptr) {
    QFileInfo file_info(file_path);
    std::string file_name = file_info.baseName().toStdString();
    std::string file_ext = file_info.suffix().toStdString();

    std::shared_ptr<IDoc> doc_plugin =
        std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    alibaba::doc::CreateDocReq req;
    req.doc_name = file_name;
    req.doc_type = file_ext;
    req.room_id = model_.room_id;
    req.permission = private_oss ? "private":"";
    doc_plugin->CreateDoc(
        req,
        [&ctx, out_rsp](const alibaba::doc::CreateDocRsp& rsp) {
          classroom::blog(LOG_INFO, "create doc success, %s",
                          rsp.doc_id.c_str());
          *out_rsp = rsp;
          ctx.resume(boost::system::error_code(), true);
        },
        [&ctx, dps_error](const alibaba::dps::DPSError& err) {
          classroom::LogError(ClassroomTagPPT, "create doc failed,", err);
          *dps_error = err;
          ctx.resume(boost::system::error_code(), true);
        });
  } else {
    return false;
  }

  return ctx.result();
}


void MainDisplayPreviewVM::OnBnClickedButtonUpload(QWidget* parent) {
  if (!model_.upload_ppt_done) {
    int32_t field = MainDisplayPreviewDocUploading;
    emit SignalUpdateVM(field);
    return;
  }
  QString path = QFileDialog::getOpenFileName(parent,
    QTranslate("InteractiveClass.WhiteBoard.SelectFile"), "",
    "Document Files (*.ppt *.pptx *.doc *.docx *.pdf)", nullptr);
  if (path.isEmpty()) {
    return;
  }
  model_.file_path = path;
  model_.is_upload_doc = true;
  model_.insert_progress = 0;
  model_.upload_progress = 0;
  int32_t field = MainDisplayPreviewDocProcessingStart;
  emit SignalUpdateVM(field);

  model_.upload_ppt_done = false;
  boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::CoroutineRunUploadCombined, this, std::placeholders::_1, path, RunUploadType::RunUploadTypeUploadDOC));
}

void MainDisplayPreviewVM::OnBnClickedButtonUpload2(
    QWidget* parent) {

  if (!model_.upload_ppt_done) {
    int32_t field = MainDisplayPreviewDocUploading;
    emit SignalUpdateVM(field);
    return;
  }
  QString path = QFileDialog::getOpenFileName(parent,
    QTranslate("InteractiveClass.WhiteBoard.SelectFile"), "",
    "Document Files (*.ppt *.pptx *.doc *.docx *.pdf)", nullptr);
  if (path.isEmpty()) {
    return;
  }
  model_.file_path = path;
  model_.is_upload_doc = true;
  model_.insert_progress = 0;
  model_.upload_progress = 0;
  int32_t field = MainDisplayPreviewDocProcessingStart;
  emit SignalUpdateVM(field);
  
  model_.upload_ppt_done = false;
  boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::CoroutineRunUploadProcess, this, std::placeholders::_1, path, RunUploadType::RunUploadTypeUploadDOC));
}


bool MainDisplayPreviewVM::CoroutineCreateDocCombined(
  boost::asio::yield_context yield,
  const QString& file_path,
  bool private_oss,
  std::string* rsp,
  alibaba::dps::DPSError* dps_error) {
  yield_helper::context<bool> ctx(yield);
  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model_.room_id);
  if (iroom_ptr) {
    QFileInfo file_info(file_path);
    std::string file_name = file_info.baseName().toStdString();
    std::string file_ext = file_info.suffix().toStdString();

    std::shared_ptr<IDoc> doc_plugin =
      std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    auto progress_callback = [this](size_t increment, int64_t transferred, int64_t total) {
      if (total > 0) {
        model_.upload_progress = transferred * 100 / total;
        std::function<void()> callback = [this] {
          emit SignalUpdateVM(MainDisplayPreviewDocProcessingUploadProgress);
        };
        MainDisplayPreviewVM::PostToUIThread(callback);
      }
    };
    doc_plugin->UploadFile(file_path.toLocal8Bit().data(), file_name, file_ext, model_.room_id, private_oss,
      progress_callback,
      [&ctx, rsp](const std::string& doc_id) {
      classroom::blog(LOG_INFO, "create doc success, %s",
        doc_id.c_str());
      *rsp = doc_id;
      ctx.resume(boost::system::error_code(), true);
    },
      [&ctx, dps_error](const alibaba::dps::DPSError& err) {
      classroom::LogError(ClassroomTagPPT, "create doc failed,", err);
      *dps_error = err;
      ctx.resume(boost::system::error_code(), true);
    });
  }
  else {
    return false;
  }

  return ctx.result();
}

bool MainDisplayPreviewVM::CoroutineTransCodeDocCombined(boost::asio::yield_context yield,
  const std::string& doc_id,
  const std::string& target_name,
  std::string* target_doc_id,
  std::vector<std::string> * url_list,
  alibaba::dps::DPSError* dps_error) {

  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IDoc> doc_plugin =
      std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    alibaba::doc::CreateDocConversionTaskReq req;
    req.source_doc_id = doc_id;
    req.target_name = target_name;
    req.target_type = "jpg";
    doc_plugin->ConvertDocToPic(doc_id, target_name, "jpg",
      [&ctx, url_list, target_doc_id](
        const std::string& rsp, const std::vector<std::string>& urls) {
      classroom::blog(LOG_INFO, "tanscode doc success, %s",
        rsp.c_str());
      *target_doc_id = rsp;
      *url_list = urls;
      ctx.resume(boost::system::error_code(), true);
    },
      [&ctx, dps_error](const alibaba::dps::DPSError& err) {
      classroom::LogError(ClassroomTagPPT, "tanscode doc failed", err);
      *dps_error = err;
      ctx.resume(boost::system::error_code(), false);
    });
  }
  else {
    return false;
  }

  return ctx.result();
}

void MainDisplayPreviewVM::CoroutineRunUploadCombined(
  boost::asio::yield_context yield,
  const QString& file_path,
  const RunUploadType run_upload_type) {
  bool fail = true;
  std::string doc_id;
  std::vector<std::string> doc_url_list;
  alibaba::doc::GetDocRsp get_doc_rsp;
  uint32_t image_width = 100;
  uint32_t image_height = 100;

  do {

    alibaba::dps::DPSError dps_error;
    std::string create_doc_id;
    bool private_oss = false;
    if (run_upload_type == RunUploadType::RunUploadTypeUploadImage) {
      QImage image_file;
      image_file.load(file_path);
      if (image_file.isNull()) {
        break;
      }
      image_width = image_file.width();
      image_height = image_file.height();
    }


    QFileInfo file_info(file_path);
    std::string file_name = file_info.baseName().toStdString();
    std::string file_ext = file_info.suffix().toStdString();
    bool need_transcode = IsFileNeedTransCode(file_ext);

    
    // 创建Doc
    bool ok = CoroutineCreateDocCombined(yield, file_path, private_oss, &create_doc_id, &dps_error);
    if (!ok) break;
   
    emit SignalUpdateVM(MainDisplayPreviewDocUploadedSuccess);

    emit SignalUpdateVM(MainDisplayPreviewDocProcessingTranscode);
    std::string check_doc_id = create_doc_id;

    // 需要转码
    std::string transcode_doc_id;
    std::vector<std::string> convert_urls;
    if (need_transcode) {
      ok = CoroutineTransCodeDocCombined(yield, create_doc_id, file_name, &transcode_doc_id, &convert_urls, &dps_error);
      if (!ok) break;
      check_doc_id = transcode_doc_id;
    }

    doc_url_list = convert_urls;
    doc_id = check_doc_id;

    fail = false;

  } while (0);


  if (!fail) {

    if (run_upload_type == RunUploadType::RunUploadTypeUploadImage) {
      if (doc_url_list.size() == 1) {
        uint32_t target_width = image_width;
        uint32_t target_height = image_height;
        if (image_width > 600 || image_height > 600) {
          target_height = target_height / 2;
          target_width = target_width / 2;
        }
        model_.map_update_urls[GetUrlMainPath(doc_url_list[0])] = doc_url_list[0];
        bool ok = CoroutineReportWhiteboardPageOperate(yield, doc_id, 1, model_.doc_current_page, kEnumWhiteboardPageOperate_Add, "image");
        ok = CoroutineAddImage(yield, 5000, doc_url_list[0], target_width, target_height);
      }
    }
    else if (run_upload_type == RunUploadType::RunUploadTypeUploadDOC) {
      // 转跳到尾页, 以便从尾部插入新文档
      bool ok = false;
      uint32_t total_page = 0;
      while (!coroutine_abort_run_upload_process_) {
        ok = CoroutineGotoLastPage(yield, 3000, &total_page);
        if (ok) break; else CoroutineSleep(yield, 1000);
      }
      if (coroutine_abort_run_upload_process_) return;
      assert(total_page >= 1);

      // 更新白板页映射关系
      const uint32_t doc_page_size = doc_url_list.size();
      ok = CoroutineReportWhiteboardPageOperate(yield, doc_id, doc_page_size, total_page, kEnumWhiteboardPageOperate_Add, "page");

      uint32_t last_total_page = total_page;
      uint32_t current_total_page = total_page;
      model_.insert_progress = 0;

      // 执行白板页插入
      for (size_t i = 0; i < doc_url_list.size(); ++i) {

        const std::string& url = doc_url_list[i];
        model_.map_update_urls[GetUrlMainPath(url)] = url;
        // 从尾部可靠加页, 加页-查询
        do {
          ok = CoroutineWhiteboardAppPage(yield, 5000);
          while (!coroutine_abort_run_upload_process_) {
            ok = CoroutineGotoLastPage(yield, 5000, &current_total_page);
            if (ok) break; else CoroutineSleep(yield, 3000);
          }
        } while (!coroutine_abort_run_upload_process_ && current_total_page == last_total_page);
        if (coroutine_abort_run_upload_process_) return;
        last_total_page = current_total_page;

        // 设备背景图
        while (!coroutine_abort_run_upload_process_) {
          ok = CoroutineWhiteboardSetCurrentPageBackground(yield, url, 3000);
          if (ok) break; else CoroutineSleep(yield, 3000);
        }
        model_.insert_progress = (i + 1) * 100 / doc_url_list.size();
        emit SignalUpdateVM(MainDisplayPreviewDocProcessingInsert);
        if (coroutine_abort_run_upload_process_) return;

      }
      // 根据最新映射关系展示
      CoroutineUpdateDocInfo(yield);
      WhiteboardGoToPage(last_total_page - doc_url_list.size());
    }

    Q_EMIT SignalUploadDocSuccess(get_doc_rsp);
    Q_EMIT SignalUpdateVM(MainDisplayPreviewDocProcessingComplete);
    model_.upload_ppt_done = true;
  }
  else {
    Q_EMIT SignalUpdateVM(MainDisplayPreviewDocUploadedFailed);
    model_.upload_ppt_done = true;
  }
}

void MainDisplayPreviewVM::CoroutineRunUploadProcess(
  boost::asio::yield_context yield, 
  const QString& file_path,
  const RunUploadType run_upload_type) {

  bool fail = true;
  std::string doc_id;
  std::vector<std::string> doc_url_list;
  alibaba::doc::GetDocRsp get_doc_rsp;
  uint32_t image_width = 100;
  uint32_t image_height = 100;

  do {
    
    alibaba::dps::DPSError dps_error;
    alibaba::doc::CreateDocRsp create_doc_rsp;
    bool private_oss = true;
    if (run_upload_type == RunUploadType::RunUploadTypeUploadImage) {
      QImage image_file;
      image_file.load(file_path);
      if (image_file.isNull()) {
        break;
      }
      image_width = image_file.width();
      image_height = image_file.height();
    }


    QFileInfo file_info(file_path);
    std::string file_name = file_info.baseName().toStdString();
    std::string file_ext = file_info.suffix().toStdString();
    bool need_transcode = IsFileNeedTransCode(file_ext);

    // 创建Doc
    bool ok = CoroutineCreateDoc(yield, file_path, private_oss, &create_doc_rsp, &dps_error);
    if (!ok) break;
    ok = CoroutineUpdateDocStatus(yield, create_doc_rsp.doc_id, DocUploadStatusUploading);

    // 上传文件
    ok = CoroutineDoUpload(yield, create_doc_rsp.doc_id, file_path,
                       create_doc_rsp.credentials.access_key_id,
                       create_doc_rsp.credentials.access_key_secret,
                       create_doc_rsp.credentials.security_token,
                       create_doc_rsp.oss_object, create_doc_rsp.oss_bucket);

    bool upload_ok = ok;
    ok = CoroutineUpdateDocStatus(yield, create_doc_rsp.doc_id, upload_ok ? DocUploadStatusUploadSuccess : DocUploadStatusUploadFailed);

    if (!upload_ok) break;    

    std::string check_doc_id = create_doc_rsp.doc_id;

    // 需要转码
    std::string transcode_doc_id;
    if (need_transcode) {
      ok = CoroutineTransCodeDoc(yield, create_doc_rsp.doc_id, file_name, &transcode_doc_id, &dps_error);
      if (!ok) break;
      check_doc_id = transcode_doc_id;
    }

    // 轮询DOC状态
    bool get_doc_ok = false;
    for (;;) {
      ok = CoroutineGetDoc(yield, check_doc_id, &get_doc_rsp, &dps_error);
      if (!ok || get_doc_rsp.status == 3 || get_doc_rsp.status == 4) break; // fail      
      if (get_doc_rsp.status == 0) { // success
        emit SignalUpdateVM(MainDisplayPreviewDocUploadedSuccess);
        get_doc_ok = true;
        break;
      }
      emit SignalUpdateVM(MainDisplayPreviewDocProcessingTranscode);
      CoroutineSleep(yield, 2000); // continue
    }

    if (!get_doc_ok) break;

    doc_url_list = get_doc_rsp.url_list;
    doc_id = check_doc_id;

    fail = false;

  } while (0);


  if (!fail) {

    if (run_upload_type == RunUploadType::RunUploadTypeUploadImage) {
      if (doc_url_list.size() == 1) {
        uint32_t target_width = image_width;
        uint32_t target_height = image_height;
        if (image_width > 600 || image_height > 600) {
          target_height = target_height / 2;
          target_width = target_width / 2;
        }
        model_.map_update_urls[GetUrlMainPath(doc_url_list[0])] = doc_url_list[0];
        bool ok = CoroutineReportWhiteboardPageOperate(yield, doc_id, 1, model_.doc_current_page, kEnumWhiteboardPageOperate_Add, "image");
        ok = CoroutineAddImage(yield, 5000, doc_url_list[0], target_width, target_height);
      }
    }
    else if (run_upload_type == RunUploadType::RunUploadTypeUploadDOC) {
      // 转跳到尾页, 以便从尾部插入新文档
      bool ok = false;
      uint32_t total_page = 0;
      while (!coroutine_abort_run_upload_process_) {
        ok = CoroutineGotoLastPage(yield, 3000, &total_page);
        if (ok) break; else CoroutineSleep(yield, 1000);
      }
      if (coroutine_abort_run_upload_process_) return;
      assert(total_page >= 1);

      // 更新白板页映射关系
      const uint32_t doc_page_size = doc_url_list.size();
      ok = CoroutineReportWhiteboardPageOperate(yield, doc_id, doc_page_size, total_page, kEnumWhiteboardPageOperate_Add, "page");

      uint32_t last_total_page = total_page;
      uint32_t current_total_page = total_page;
      model_.insert_progress = 0;
      
      // 执行白板页插入
      for (size_t i = 0; i < doc_url_list.size(); ++i) {

        const std::string& url = doc_url_list[i];
        model_.map_update_urls[GetUrlMainPath(url)] = url;
        // 从尾部可靠加页, 加页-查询
        do {
          ok = CoroutineWhiteboardAppPage(yield, 5000);
          while (!coroutine_abort_run_upload_process_) {
            ok = CoroutineGotoLastPage(yield, 5000, &current_total_page);
            if (ok) break; else CoroutineSleep(yield, 3000);
          }
        } while (!coroutine_abort_run_upload_process_ && current_total_page == last_total_page);
        if (coroutine_abort_run_upload_process_) return;
        last_total_page = current_total_page;

        // 设备背景图
        while (!coroutine_abort_run_upload_process_) {
          ok = CoroutineWhiteboardSetCurrentPageBackground(yield, url, 3000);
          if (ok) break; else CoroutineSleep(yield, 3000);
        }
        model_.insert_progress = (i + 1) * 100 / doc_url_list.size();
        emit SignalUpdateVM(MainDisplayPreviewDocProcessingInsert);
        if (coroutine_abort_run_upload_process_) return;

      }
      // 根据最新映射关系展示
      CoroutineUpdateDocInfo(yield);
      WhiteboardGoToPage(last_total_page - doc_url_list.size());
    }

    Q_EMIT SignalUploadDocSuccess(get_doc_rsp);
    Q_EMIT SignalUpdateVM(MainDisplayPreviewDocProcessingComplete);
    model_.upload_ppt_done = true;
  }
  else {
    Q_EMIT SignalUpdateVM(MainDisplayPreviewDocUploadedFailed);
    model_.upload_ppt_done = true;
  }
}
void MainDisplayPreviewVM::AbortCoroutineRunUploadProcess()
{
  coroutine_abort_run_upload_process_ = false;
}

bool MainDisplayPreviewVM::CoroutineTransCodeDoc(
    boost::asio::yield_context yield,
    const std::string& doc_id,
    const std::string& target_name,
    std::string* target_doc_id,
    alibaba::dps::DPSError* dps_error) {

  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IDoc> doc_plugin =
        std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    alibaba::doc::CreateDocConversionTaskReq req;
    req.source_doc_id = doc_id;
    req.target_name = target_name;
    req.target_type = "jpg";
    doc_plugin->CreateDocConversionTask(
        req,
        [&ctx,
         target_doc_id](
            const alibaba::doc::CreateDocConversionTaskRsp& rsp) {
          classroom::blog(LOG_INFO, "tanscode doc success, %s",
                          rsp.target_doc_id.c_str());
          *target_doc_id = rsp.target_doc_id;
          ctx.resume(boost::system::error_code(), true);
        },
        [&ctx, dps_error](const alibaba::dps::DPSError& err) {
          classroom::LogError(ClassroomTagPPT, "tanscode doc failed", err);
          *dps_error = err;
          ctx.resume(boost::system::error_code(), false);
        });
  } else {
    return false;
  }

  return ctx.result();
}

void MainDisplayPreviewVM::CoroutineSleep(boost::asio::yield_context yield,
                                        const uint32_t ms) {
  boost::system::error_code ec;
  boost::asio::steady_timer timer(ioc_);
  timer.expires_from_now(std::chrono::milliseconds(ms));
  timer.async_wait(yield[ec]);
}

bool MainDisplayPreviewVM::CoroutineReportWhiteboardPageOperate(
  boost::asio::yield_context yield,
  const std::string& page_group,
  const int32_t page_group_size,
  const int32_t wb_page_number,
  const EnumWhiteboardPageOperate op_type,
  const std::string& type)
{
  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> wb_plugin =
      std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    alibaba::wb::ReportWhiteboardPageOperateReq req;


    std::string whiteboard_id;
    {
      auto instance_list = iroom_ptr->GetRoomDetail().room_info.plugin_instance_info.instance_list;
      for (auto plugin : instance_list) {
        if (plugin.plugin_id == "wb") {
          whiteboard_id = plugin.instance_id;
          break;
        }
      }
    }

    if (whiteboard_id.empty()) return false;

    switch (op_type)
    {
    case EnumWhiteboardPageOperate::kEnumWhiteboardPageOperate_Add:
      req.operate = "add";
      break;
    default:
      return false;
      break;
    }

    req.whiteboard_id = whiteboard_id;
    req.page_group = page_group;
    req.page_size = page_group_size;
    req.page_number = wb_page_number;
    req.type = type;
    
    wb_plugin->ReportWhiteboardPageOperate(req,
      [&ctx](const ::alibaba::wb::ReportWhiteboardPageOperateRsp& rsp) {
        ctx.resume(boost::system::error_code(), true);

      }, 
      [&ctx](const ::alibaba::dps::DPSError& error_msg) {
        ctx.resume(boost::system::error_code(), false);
      }
    );

  }
  else {
    return false;
  }

  return ctx.result();
}
bool MainDisplayPreviewVM::IsFileNeedTransCode(const std::string& file_ext) {
  QString tmp = QString::fromStdString(file_ext);
  tmp = tmp.toLower();

  static char* ext_list[] = {"doc", "docx", "ppt", "pptx", "pdf"};

  bool need_transcode = false;

  for (int i = 0; i < _countof(ext_list); ++i) {
    if (tmp == ext_list[i]) {
      need_transcode = true;
      break;
    }
  }

  return need_transcode;
}
bool MainDisplayPreviewVM::CoroutineGetDoc(
    boost::asio::yield_context yield,
                                       const std::string& doc_id,
                                       alibaba::doc::GetDocRsp* out_rsp,
                                       alibaba::dps::DPSError* dps_error) {
  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
      alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
          model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IDoc> doc_plugin =
        std::dynamic_pointer_cast<IDoc>(iroom_ptr->GetPlugin(PluginDoc));
    alibaba::doc::GetDocReq req;
    req.doc_id = doc_id;
    doc_plugin->GetDoc(
        req,
        [&ctx, out_rsp](const alibaba::doc::GetDocRsp& rsp) {
          classroom::blog(LOG_INFO, "check transcode status, %d",
                          (int32_t)rsp.status);
          *out_rsp = rsp;
          ctx.resume(boost::system::error_code(), true);
        },
        [&ctx](const alibaba::dps::DPSError& err) {
          classroom::LogError(ClassroomTagPPT, "check doc status failed", err);
          ctx.resume(boost::system::error_code(), false);
        });
  }

  return ctx.result();
}
bool MainDisplayPreviewVM::CoroutineDoUpload(
    boost::asio::yield_context yield,
                                         const std::string& doc_id,
                                         const QString& file_path,
                                         const std::string& ak,
                                         const std::string& sk,
                                         const std::string& access_token,
                                         const std::string& obj_name,
                                         const std::string& oss_bucket) {
  yield_helper::context<bool> ctx(yield);

  using namespace AlibabaCloud::OSS;
  std::string access_key_id = ak;
  std::string access_key_secret = sk;
  std::string end_point = "http://oss-cn-shanghai.aliyuncs.com";
  model_.upload_progress = 0;
  /* 填写Bucket名称，例如examplebucket */
  std::string bucket_name = oss_bucket;
  /* 填写文件完整路径，例如exampledir/exampleobject.txt。文件完整路径中不能包含Bucket名称
   */
  std::string object_name = obj_name;

  std::unique_ptr<std::thread> task(new std::thread([=, &ctx]() {
    ClientConfiguration conf;
    OssClient client(
        end_point,
        std::make_shared<OssCredentialsProvider>(ak, sk, access_token), conf);
    /* 填写本地文件完整路径，例如D:\\localpath\\examplefile.txt，其中localpath为本地文件examplefile.txt所在本地路径
     */

    std::shared_ptr<std::iostream> file_stream = std::make_shared<std::fstream>(
        file_path.toLocal8Bit(), std::ios::in | std::ios::binary);

    PutObjectRequest request(bucket_name, object_name, file_stream);

    /*（可选）请参见如下示例设置存储类型及访问权限ACL */
    // request.MetaData().addHeader("x-oss-object-acl", "private");
    // request.MetaData().addHeader("x-oss-storage-class", "Standard");
    AlibabaCloud::OSS::TransferProgress tp;
    tp.Handler = [doc_id, this](size_t increment, int64_t transferred, int64_t total,
                          void* user_data) {
      if (total > 0) {
        classroom::blog(LOG_INFO, "upload doc, doc_id=%s, progress=%d",
                        doc_id.c_str(), transferred * 100 / total);
        model_.upload_progress = transferred * 100 / total; 
        std::function<void()> callback = [this] {
          emit SignalUpdateVM(MainDisplayPreviewDocProcessingUploadProgress);
        };
        MainDisplayPreviewVM::PostToUIThread(callback);
      }
    };
    request.setTransferProgress(tp);
    auto outcome = client.PutObject(request);
    bool success = outcome.isSuccess();
    ctx.resume(boost::system::error_code(), success);
  }));
  task->detach();

  return ctx.result();
}

void MainDisplayPreviewVM::OnBnClickedMultiSelect() {
  std::string api_name = "setToolType";
  std::string api_param = "multiSelect";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::UpdateLineWidth(int32_t value) {
  std::string api_name = "setCurrentBrush";
  std::string api_param = QString("{\"strokeWidth\":%1}").arg(value).toStdString();
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::UpdateFontSize(int32_t value) {
  std::string api_name = "setCurrentBrush";
  std::string api_param = QString("{\"fontSize\":%1}").arg(value).toStdString();
  ExecuteCommand(api_name, api_param);
}


void MainDisplayPreviewVM::UpdateSelectedBold() {
  std::string api_name = "updateSelectedTextStyle";
  std::string api_param = "{\"fontWeight\":\"bold\"}";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::UpdateSelectedItalic() {
  std::string api_name = "updateSelectedTextStyle";
  std::string api_param = "{\"fontStyle\":\"italic\"}";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::UpdateSelectedUnderLine() {
  std::string api_name = "updateSelectedTextStyle";
  std::string api_param = "{\"textDecoration\":\"underline\"}";
  ExecuteCommand(api_name, api_param);
}

void MainDisplayPreviewVM::AddImage(const std::string& url, int32_t width, int32_t height, std::function<void(const std::string&)> finish_cb) {
  std::string api_name = "addImage";
  std::string api_param = QString("{\"width\":%1, \"height\": %2, \"href\":\"%3\"}").arg(width).arg(height).arg(url.c_str()).toStdString();
  ExecuteCommand(api_name, api_param, finish_cb);
}

void MainDisplayPreviewVM::OnBnClickedButtonAddImage(QWidget* parent) {
  if (!model_.upload_ppt_done) {
    int32_t field = MainDisplayPreviewDocUploading;
    emit SignalUpdateVM(field);
    return;
  }
  QString path = QFileDialog::getOpenFileName(parent,
    QTranslate("InteractiveClass.WhiteBoard.SelectFile"), "",
    "Image Files (*.jpg *.bmp *.png)", nullptr);
  if (path.isEmpty()) {
    return;
  }

  model_.upload_ppt_done = false;
  model_.is_upload_doc = false;
  boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::CoroutineRunUploadProcess, this, std::placeholders::_1, path, RunUploadType::RunUploadTypeUploadImage));
}

void MainDisplayPreviewVM::GetScale() {
  std::string api_name = "getScale";
  ExecuteCommand(api_name, "", [this](const std::string& ret) {
    QString true_ret = QString::fromStdString(ret);
    true_ret = true_ret.replace(QString("\""), QString(""));
    double scale = std::atof(true_ret.toStdString().c_str());
    model_.scale = scale;
    int32_t field = MainDisplayPreviewFieldScale;
    Q_EMIT SignalUpdateVM(field);   
  });
}

void MainDisplayPreviewVM::GetPageIndex(const std::function<void(int32_t page)>& callback) {
  std::string api_name = "getCurrentSceneIndex";
  ExecuteCommand(api_name, "", [this, callback](const std::string& ret) {
    QString true_ret = QString::fromStdString(ret);
    true_ret = true_ret.replace(QString("\""), QString(""));
    int32_t count = std::atoi(true_ret.toStdString().c_str());
    if (count >= 0) {
      if (callback) {
        callback(count);
      }
    }
  });
}

void MainDisplayPreviewVM::GetPageTotalIndex(const std::function<void(int32_t page)>& callback) {
  std::string api_name = "getScenesCount";
  ExecuteCommand(api_name, "", [this, callback](const std::string& ret) {
    QString true_ret = QString::fromStdString(ret);
    true_ret = true_ret.replace(QString("\""), QString(""));
    int32_t count = std::atoi(true_ret.toStdString().c_str());
    if (count >= 1) {
      if (callback) {
        callback(count);
      }
    }
  });
}


void MainDisplayPreviewVM::UpdatePageInfo(std::function<void(bool)> finish_cb) {

  GetPageInfo([this, finish_cb](int32_t current_page, int32_t total_page) {

    model_.wb_total_page = total_page;
    model_.wb_current_page_index = current_page;

    std::string doc_id;
    uint32_t doc_current_page = 0;
    uint32_t doc_total_page = 0;
    bool doc_vaild = QueryDocInfoByWhiteboardPage(
        model_.wb_current_page_index, doc_id, doc_current_page, doc_total_page);

    if (doc_vaild) {
      model_.current_doc_id = doc_id;
      model_.doc_current_page = doc_current_page;
      model_.doc_total_page = doc_total_page;
    }

    int32_t field = MainDisplayPreviewFieldCurrentIndex |
      MainDisplayPreviewFieldTotalIndex;
    emit SignalUpdateVM(field);

    if (finish_cb) finish_cb(doc_vaild);

  });
}

bool MainDisplayPreviewVM::QueryDocInfoByWhiteboardPage(
  const uint32_t wb_page_index, 
  std::string& doc_id, 
  uint32_t& doc_page, 
  uint32_t& doc_total_page)
{
  bool ok = false;
  for (const DocInfoContext& doc_info : model_.all_doc_info) {
    for (const DocPageInfoContext& page_info : doc_info.doc_page_info) {
      if (page_info.wb_page == wb_page_index + 1) {
        doc_id = doc_info.doc_id;
        doc_page = page_info.group_page;
        doc_total_page = doc_info.doc_page_info.size();

        if (doc_page > doc_total_page) {
          // 映射关系错误
          assert(0);
          ok = false;
        }
        else {
          ok = true;
        }
        break;
      }
    }  
  }

  return ok;
}
void MainDisplayPreviewVM::WhiteboardGoToPage(const uint32_t wb_page_index) {

  GetPageInfo([this, wb_page_index](int32_t current_page, int32_t total_page) {

    model_.wb_current_page_index = current_page;
    model_.wb_total_page = total_page;

    if (wb_page_index < 0 || wb_page_index >= model_.wb_total_page) {
      assert(0);
      return;
    }

    std::string doc_id;
    uint32_t doc_current_page = 0;
    uint32_t doc_total_page = 0;
    bool doc_vaild = QueryDocInfoByWhiteboardPage(wb_page_index, doc_id, doc_current_page, doc_total_page);

    if (!doc_vaild) return;

    model_.current_doc_id = doc_id;
    model_.doc_current_page = doc_current_page;
    model_.doc_total_page = doc_total_page;

    std::string api_name = "gotoScene";
    ExecuteCommand(api_name, std::to_string(wb_page_index));

    int32_t field =
      MainDisplayPreviewFieldCurrentIndex | MainDisplayPreviewFieldTotalIndex;
    Q_EMIT SignalUpdateVM(field);

  });
}

void MainDisplayPreviewVM::GetPageInfo(const std::function<void(int32_t current, int32_t total)>& callback) {

  auto get_current_page = [callback](int32_t page, int32_t total) {
    if (callback) {
      callback(page, total);
    }
  };
  auto get_total_page = [get_current_page, this](int32_t totlal) {
    auto function = std::bind(get_current_page, std::placeholders::_1, totlal);
    GetPageIndex(function);
  };
  GetPageTotalIndex(get_total_page);
}

MainDisplayPreivewModel MainDisplayPreviewVM::GetModel() {
  return model_;
}

void MainDisplayPreviewVM::ExecuteCommand(const std::string &api_name, const std::string &api_parameter) {
  auto iroom_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->InvokeWhiteBoardMethod(api_name, api_parameter, nullptr);
  }
}

void MainDisplayPreviewVM::ExecuteCommand(const std::string &api_name,
  const std::string &api_parameter,
  const std::function<void(const std::string&)>& callback) {
  auto iroom_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->InvokeWhiteBoardMethod(api_name, api_parameter, [this, callback](const std::string& res) {
      PostToUIThread([callback, res]() {
        if (callback) callback(res);
      });
    });
  }
}

void MainDisplayPreviewVM::PostToUIThread(std::function<void()> cb)
{
  EventManager::Instance()->PostUITask(cb);
}

void MainDisplayPreviewVM::EventCallback(const std::string& event, const std::string& data) {

  if (event == "E_BoardCreated") {
    // 白板就绪通知
    UpdateDocRelation();
  } else if (event == "ALIYUNBOARD_GET_PREVIEW_URL") {
    UrlReplaceItem item = FromJson(data);
    if (!item.url.empty()) {
      boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::RequestUpdateDocUrls, this, std::placeholders::_1, item));
    }
    
  } 
}

void MainDisplayPreviewVM::UpdateDocRelation() {
  boost::asio::spawn(ioc_, std::bind(&MainDisplayPreviewVM::CoroutineSyncWhiteboardPageMappingRelation, this, std::placeholders::_1));
}

void MainDisplayPreviewVM::UpdateUserId(const std::string& user_id) {
  model_.user_id = user_id;
}

void MainDisplayPreviewVM::UpdateRoomId(const std::string& room_id) {

  model_.room_id = room_id;

  auto iroom_ptr = alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> white_board_plugin = std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    white_board_plugin->AddListener(this,
      [this](const std::string& evt, const std::string& data) {
        PostToUIThread([this, evt, data]() {
          EventCallback(evt, data);
          });
      });
  }

  PrepareOssSdk();
}
void MainDisplayPreviewVM::UpdateUserRole(const ClassRoleEnum user_role)
{
  model_.user_role = user_role;
}

void MainDisplayPreviewVM::PrepareOssSdk() {
  static std::once_flag flag;
  std::call_once(flag, []() {
    AlibabaCloud::OSS::InitializeSdk();
  });
}

MainDisplayPreviewVM::MainDisplayPreviewVM() {

  connect(&ui_thread_run_ioc_timer_, SIGNAL(timeout()), this, SLOT(RunIoContext()));
  ui_thread_run_ioc_timer_.setInterval(10);
  ui_thread_run_ioc_timer_.start();

  qRegisterMetaType<alibaba::doc::GetDocRsp>("alibaba::doc::GetDocRsp");
}
MainDisplayPreviewVM::~MainDisplayPreviewVM() {

  AbortCoroutineSyncWhiteboardPageMappingRelation();
  AbortCoroutineUpdateDocInfo();

  disconnect(&ui_thread_run_ioc_timer_, SIGNAL(timeout()), this, SLOT(RunIoContext()));
  ui_thread_run_ioc_timer_.stop();
  ioc_.run();

  AlibabaCloud::OSS::ShutdownSdk();
}

void MainDisplayPreviewVM::RunIoContext()
{
  EventManager::Instance()->PostUITask([this]() {
    ioc_.poll();
  });
}
void MainDisplayPreviewVM::AddPptImage(const std::vector<std::string>& urls) {
  GetPageIndex([urls](int32_t current_page) {
    int32_t current_index = current_page;
    
  });
}

void MainDisplayPreviewVM::AddPage(std::function<void()> finish_cb)
{
  ExecuteCommand("addScene", "", [finish_cb](const std::string&) {
    if (finish_cb) finish_cb();
  });
}

void MainDisplayPreviewVM::AbortCoroutineSyncWhiteboardPageMappingRelation()
{
  coroutine_abort_sync_whiteboard_page_mapping_relation_ = true;
}
void MainDisplayPreviewVM::CoroutineSyncWhiteboardPageMappingRelation(boost::asio::yield_context yield)
{
  ::alibaba::wb::GetWhiteboardPageInfoRsp wb_page_info_rsp;
  alibaba::dps::DPSError dps_error;

  // 获取映射关系表
  while (!coroutine_abort_sync_whiteboard_page_mapping_relation_) {
    bool ok = CoroutineGetWhiteboardPageInfo(yield, &wb_page_info_rsp, &dps_error);
    if (ok) break; else CoroutineSleep(yield, 5000);
  }
  if (coroutine_abort_sync_whiteboard_page_mapping_relation_) return;

  // 获取白板总页码
  uint64_t total_page_count = 0;
  while (!coroutine_abort_sync_whiteboard_page_mapping_relation_) {
    bool ok = CoroutineWhiteboardTotalPageCount(yield, 5000, &total_page_count);
    if (ok) break; else CoroutineSleep(yield, 5000);
  }
  if (coroutine_abort_sync_whiteboard_page_mapping_relation_) return;

  // 同步处理
  if (wb_page_info_rsp.page_list.empty()) {
    // 后台服务异常, 默认至少由白板wb，不应该空
    // 尝试补偿, 插入0位置之后
    assert(0);
    for (int i = 0; i < 3 && !coroutine_abort_sync_whiteboard_page_mapping_relation_; ++i) {
      bool ok = CoroutineReportWhiteboardPageOperate(yield, "wb", total_page_count, 0, kEnumWhiteboardPageOperate_Add, "page");
      if (ok) break; else CoroutineSleep(yield, 5000);
    }
  }
  else if (wb_page_info_rsp.page_list.size() != total_page_count) {
    // TODO, 页码不匹配补偿
  }

  // 根据最新映射关系展示
  CoroutineUpdateDocInfo(yield);

  // 白板就绪通知
  Q_EMIT SignalUpdateVM(MainDisplayPreviewWhiteBoardLoaded);
  
}

void MainDisplayPreviewVM::CoroutineUpdateDocInfo(boost::asio::yield_context yield)
{
  ::alibaba::wb::GetWhiteboardPageInfoRsp wb_page_info_rsp;
  ::alibaba::dps::DPSError dps_error;

  // 获取最新映射关系
  while (!coroutine_abort_update_doc_info_) {
    bool ok = CoroutineGetWhiteboardPageInfo(yield, &wb_page_info_rsp, &dps_error);
    if (ok) break; else CoroutineSleep(yield, 5000);
  }
  if (coroutine_abort_update_doc_info_) return;

  std::map<std::string, DocInfoContext> doc_map;
  for (auto& page_info : wb_page_info_rsp.page_list) {

    const std::string& doc_id = page_info.group;
    DocInfoContext& doc_info = doc_map[doc_id];
    doc_info.doc_id = doc_id;

    DocPageInfoContext doc_page;
    doc_page.group_page = page_info.group_page;
    doc_page.wb_page = page_info.whiteboard_page;
    doc_info.doc_page_info.push_back(doc_page);
  }

  std::set<std::string> fail_doc_id_list;

  for (auto& it : doc_map) {
    DocInfoContext& doc_info = it.second;

    if (doc_info.doc_id == "wb") {
      // 白板没有预览缩略图, 跳过
      continue;
    }

    ::alibaba::doc::GetDocRsp get_doc_dsp;

    while (!coroutine_abort_update_doc_info_) {
      bool ok = CoroutineGetDoc(yield, doc_info.doc_id, &get_doc_dsp, &dps_error);
      if (ok) break; else CoroutineSleep(yield, 5000);
    }
    if (coroutine_abort_update_doc_info_) break;

    doc_info.doc_name = get_doc_dsp.doc_name;
    doc_info.doc_type = get_doc_dsp.doc_type;

    if (get_doc_dsp.status != 0) {
      // 未上传成功, 但却已经产生记录
      assert(0);
      fail_doc_id_list.insert(doc_info.doc_id);
      continue;
    }

    if (doc_info.doc_page_info.size() != get_doc_dsp.url_list.size()) {
      // 文档Doc信息, 跟记录信息不一致
      assert(0);
      fail_doc_id_list.insert(doc_info.doc_id);
      continue;
    }

    for (size_t i = 0; i < doc_info.doc_page_info.size(); ++i) {
      DocPageInfoContext& doc_page = doc_info.doc_page_info[i];
      const std::string& url = get_doc_dsp.url_list[i];
      doc_page.page_url = url;
    }

  }
  if (coroutine_abort_update_doc_info_) return;

  for (const std::string& fail_doc_id : fail_doc_id_list) {
    doc_map.erase(fail_doc_id);
  }

  std::vector<DocInfoContext> all_doc_info;
  for (auto it : doc_map) {
    const std::string& doc_id = it.first;
    auto found = fail_doc_id_list.find(doc_id);
    if (found != fail_doc_id_list.end()) continue;
    all_doc_info.push_back(it.second);
  }

  for (auto iter = wb_page_info_rsp.intra_page_resource_list.begin(); 
    iter != wb_page_info_rsp.intra_page_resource_list.end(); iter++) {
    model_.intra_page_resource_list.push_back(iter->url);
  }
  
  // 白板至于最先位置
  for (auto it = all_doc_info.begin(); it != all_doc_info.end();) {
    if (it->doc_id == "wb") {
      auto info = *it;
      all_doc_info.erase(it);
      all_doc_info.insert(all_doc_info.begin(), info);
      break;
    }
    else {
      ++it;
    }
  }

  // 保存最新文档页码映射关系
  model_.all_doc_info = all_doc_info;
  model_.update_all_doc_info_done = true;
  TransDocUrlsInMap();
  UpdateDocUrls(yield);
  CoroutineUpdatePageInfo(yield, 5000);

  Q_EMIT SignalUpdateDocInfo(all_doc_info);
}
void MainDisplayPreviewVM::AbortCoroutineUpdateDocInfo()
{
  coroutine_abort_update_doc_info_ = true;
}

bool MainDisplayPreviewVM::CoroutineGetWhiteboardPageInfo(
  boost::asio::yield_context yield,
  ::alibaba::wb::GetWhiteboardPageInfoRsp* out_rsp,
  alibaba::dps::DPSError* dps_error
)
{
  yield_helper::context<bool> ctx(yield);

  auto iroom_ptr =
    alibaba::meta_space::MetaSpace::GetInstance()->GetRoomInstance(
      model_.room_id);
  if (iroom_ptr) {
    std::shared_ptr<IWhiteBoard> wb_plugin =
      std::dynamic_pointer_cast<IWhiteBoard>(iroom_ptr->GetPlugin(PluginWhiteBoard));
    alibaba::wb::GetWhiteboardPageInfoReq req;


    std::string whiteboard_id;
    {
      auto instance_list = iroom_ptr->GetRoomDetail().room_info.plugin_instance_info.instance_list;
      for (auto plugin : instance_list) {
        if (plugin.plugin_id == "wb") {
          whiteboard_id = plugin.instance_id;
          break;
        }
      }
    }

    if (whiteboard_id.empty()) return false;

    req.whiteboard_id = whiteboard_id;
    req.is_generate_url = true;
    wb_plugin->GetWhiteboardPageInfo(req,
      [&ctx, out_rsp](const ::alibaba::wb::GetWhiteboardPageInfoRsp& rsp) {
        *out_rsp = rsp;
        ctx.resume(boost::system::error_code(), true);
      },
      [&ctx, dps_error](const ::alibaba::dps::DPSError& error_msg) {
        *dps_error = error_msg;
        ctx.resume(boost::system::error_code(), false);
      }
    );
  }
  else {
    return false;
  }

  return ctx.result();
}
bool MainDisplayPreviewVM::CoroutineWhiteboardTotalPageCount(
  boost::asio::yield_context yield,
  const uint32_t timeout_ms,
  uint64_t* total_page)
{

  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  UpdatePageInfo([finish_flag](bool) { *finish_flag = true; });

  while (1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;
    if (*finish_flag) {
      if (total_page) {
        *total_page = model_.wb_total_page;
      }
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 200);
  }

  return ret_ok;
}

bool MainDisplayPreviewVM::CoroutineGotoLastPage(
  boost::asio::yield_context yield,
  const uint32_t timeout_ms,
  uint32_t* total_page)
{
  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  WhiteboardGotoLastPage([finish_flag]() { *finish_flag = true; });

  while (1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;

    if (*finish_flag) {
      if (total_page) {
        *total_page = model_.wb_total_page;
      }
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 100);
  }

  return ret_ok;
}
bool MainDisplayPreviewVM::CoroutineWhiteboardSetCurrentPageBackground(
  boost::asio::yield_context yield,
  const std::string& url,
  const uint32_t timeout_ms
)
{
  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  ExecuteCommand("addBackgroundImage", url, [finish_flag](const std::string&) { *finish_flag = true; });

  while(1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;
    if (*finish_flag) {
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 50);
  }

  return ret_ok;  
}
bool MainDisplayPreviewVM::CoroutineWhiteboardAppPage(
  boost::asio::yield_context yield,
  const uint32_t timeout_ms
)
{
  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  AddPage([finish_flag]() { *finish_flag = true; });

  while (1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;
    if (*finish_flag) {
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 50);
  }

  return ret_ok;
}

bool MainDisplayPreviewVM::CoroutineAddImage(
  boost::asio::yield_context yield,
  const uint32_t timeout_ms,
  const std::string& url,
  const uint32_t width,
  const uint32_t height)
{
  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  AddImage(url, width, height, [finish_flag](const std::string& res) { *finish_flag = true; });

  while (1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;
    if (*finish_flag) {
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 50);
  }

  return ret_ok;
}

void MainDisplayPreviewVM::CoroutineWhiteboardDocAddPage(
  boost::asio::yield_context yield)
{
  bool fail = true;

  do {
    if (model_.current_doc_id != "wb") break;

    // 转跳到尾页, 以便从尾部插入新文档
    bool ok = false;
    uint32_t total_page = 0;
    while (1) {
      ok = CoroutineGotoLastPage(yield, 3000, &total_page);
      if (ok) break; else CoroutineSleep(yield, 1000);
    }
    assert(total_page >= 1);

    // 更新白板页映射关系
    ok = CoroutineReportWhiteboardPageOperate(yield, "wb", 1, total_page, kEnumWhiteboardPageOperate_Add, "page");

    // 插页
    uint32_t last_total_page = total_page;
    do {
      ok = CoroutineWhiteboardAppPage(yield, 5000);
      while (1) {
        ok = CoroutineGotoLastPage(yield, 5000, &total_page);
        if (ok) break; else CoroutineSleep(yield, 3000);
      }
    } while (last_total_page == total_page);


    // 根据最新映射关系展示
    CoroutineUpdateDocInfo(yield);

    fail = false;

  } while (0);

}

bool MainDisplayPreviewVM::CoroutineUpdatePageInfo(
  boost::asio::yield_context yield,
  const uint32_t timeout_ms,
  bool* doc_vaild
)
{
  auto t_expired = std::chrono::steady_clock::now() + std::chrono::milliseconds(timeout_ms);
  bool ret_ok = false;

  std::shared_ptr<std::atomic_bool> finish_flag = std::make_shared<std::atomic_bool>(false);
  std::shared_ptr<std::atomic_bool> doc_vaild_flag = std::make_shared<std::atomic_bool>(false);

  UpdatePageInfo([finish_flag, doc_vaild_flag](bool doc_vaild) { *finish_flag = true; *doc_vaild_flag = doc_vaild; });

  while (1) {
    auto now = std::chrono::steady_clock::now();
    if (now >= t_expired) break;
    if (*finish_flag) {
      if (doc_vaild) {
        *doc_vaild = *doc_vaild_flag;
      }
      ret_ok = true;
      break;
    }
    CoroutineSleep(yield, 50);
  }
  return ret_ok;
}


void MainDisplayPreviewVM::UpdateDocUrls(boost::asio::yield_context yield) {
  if (model_.update_urls_request.size() <= 0 || !model_.update_all_doc_info_done) {
    return;
  }
  std::vector<UrlReplaceItem> vec_request = std::move(model_.update_urls_request);
  for (auto iter = vec_request.begin(); iter != vec_request.end(); iter++) {
    auto callback = [](const std::string& param) {
      
    };
    NativeCallbackItem item;
    item.request.session = iter->session;
    
    std::string replace_url = model_.map_update_urls[GetUrlMainPath(iter->url)];
    item.response.type = iter->type;
    item.response.src = replace_url.empty() ? iter->url: replace_url;
   
    ExecuteCommand("nativeCallback", ToJson(item), callback);
  }
}

std::string MainDisplayPreviewVM::GetUrlMainPath(const std::string& url) {
  if (url.empty()) {
    return "";
  }

  size_t pos = url.find("?");
  if (pos == std::string::npos) {
    return url;
  }
  else {
    return url.substr(0, pos);
  }
}

void MainDisplayPreviewVM::TransDocUrlsInMap() {
  for (auto doc_context_iter = model_.all_doc_info.begin(); 
    doc_context_iter != model_.all_doc_info.end(); doc_context_iter++) {
    for (auto iter_page_context = doc_context_iter->doc_page_info.begin();
      iter_page_context != doc_context_iter->doc_page_info.end(); iter_page_context++) {
      model_.map_update_urls[GetUrlMainPath(iter_page_context->page_url)] = iter_page_context->page_url;
    }
  }

  for (auto inner_url = model_.intra_page_resource_list.begin(); 
    inner_url != model_.intra_page_resource_list.end(); inner_url++) {
    model_.map_update_urls[GetUrlMainPath(*inner_url)] = *inner_url;
  }
}


void MainDisplayPreviewVM::RequestUpdateDocUrls(boost::asio::yield_context yield, const UrlReplaceItem& request) {
  model_.update_urls_request.push_back(request);
  UpdateDocUrls(yield);
}