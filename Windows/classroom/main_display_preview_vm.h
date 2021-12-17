#pragma 

#include <boost/asio.hpp>
#include <boost/asio/spawn.hpp>

#include <QTimer>
#include <QObject>
#include <QString>
#include <functional>
#include <mutex>
#include <unordered_map>
#include <thread>
#include <atomic>
#include <unordered_set>
#include "classroom_def.h"


namespace alibaba {
namespace doc {
struct CreateDocRsp;
struct GetDocRsp;
}
namespace dps {
struct DPSError;
}
namespace wb {
struct GetWhiteboardPageInfoRsp;
}
} // namespace alibaba


enum MainDisplayPreviewField {
  MainDisplayPreviewFieldScale = 1,
  MainDisplayPreviewFieldCurrentIndex = 1 << 1,
  MainDisplayPreviewFieldTotalIndex = 1 << 2,
  MainDisplayPreviewWhiteBoardLoaded = 1 << 3,
  MainDisplayPreviewDocUploading = 1 << 4,
  MainDisplayPreviewDocUploadedSuccess = 1 << 5,
  MainDisplayPreviewDocUploadedFailed = 1 << 6,
  MainDisplayPreviewDocProcessingStart = 1 << 7,
  MainDisplayPreviewDocProcessingUploadProgress = 1 << 8,
  MainDisplayPreviewDocProcessingTranscode = 1 << 9,
  MainDisplayPreviewDocProcessingInsert = 1 << 10,
  MainDisplayPreviewDocProcessingComplete = 1 << 11,
};

enum DocUploadStatus {
  DocUploadStatusUploading = 0,
  DocUploadStatusUploadSuccess = 1,
  DocUploadStatusUploadFailed = 2,
};

enum RunUploadType {
  RunUploadTypeUploadImage = 0,
  RunUploadTypeUploadDOC   = 1,
};

struct DocPageInfoContext
{
  std::string page_url;
  uint32_t group_page;
  uint32_t wb_page;
};
struct DocInfoContext
{
  std::string doc_id;
  std::string doc_name;
  std::string doc_type;
  std::vector<DocPageInfoContext> doc_page_info;
};

struct UrlReplaceItem {
  std::string url;
  std::string session;
  std::string type;
};

struct MainDisplayPreivewModel {

  double scale = 1.0;

  std::string user_id;
  std::string room_id;
  ClassRoleEnum user_role;

  // 实际白板sdk页码
  uint32_t wb_total_page = 1;
  uint32_t wb_current_page_index = 0;

  // 当前文档的逻辑页码
  std::string current_doc_id;         
  uint32_t doc_total_page = 0;
  uint32_t doc_current_page = 0;

  bool upload_ppt_done = true;

  std::vector<DocInfoContext> all_doc_info; // 文档页码映射关系
  std::vector<std::string> intra_page_resource_list;
  bool update_all_doc_info_done = false;
  std::unordered_map<std::string, std::string> map_update_urls;
  std::vector<UrlReplaceItem> update_urls_request;
  int32_t upload_progress = 0;
  int32_t insert_progress = 0;
  QString file_path;
  bool is_upload_doc = false;
};

enum EnumWhiteboardPageOperate
{
  kEnumWhiteboardPageOperate_Add = 1,
};


class MainDisplayPreviewVM : public QObject {
  Q_OBJECT
 signals:
  void SignalUpdateVM(int32_t field);
  void SignalUploadDocSuccess(const alibaba::doc::GetDocRsp& doc_info);
  void SignalUpdateDocInfo(const std::vector<DocInfoContext>& all_doc_info);

public:
  MainDisplayPreviewVM();
  ~MainDisplayPreviewVM();
  void UpdateUserId(const std::string& user_id);
  void UpdateRoomId(const std::string& room_id);
  void UpdateUserRole(const ClassRoleEnum user_role);
  void UpdateLineWidth(int32_t);
  void UpdateFontSize(int32_t);
  void UpdateSelectedBold();
  void UpdateSelectedItalic();
  void UpdateSelectedUnderLine();
  void UpdateDocRelation();
  void OnBnClickedButtonClear();
  void OnBnClickedButtonPenNormal();
  void OnBnClickedButtonUndo();
  void OnBnClickedButtonRedo();
  void OnBnClickedButtonPenLaserPen();
  void OnBnClickedButtonPenLaserPenNew();
  void OnBnClickedButtonPenCircle();
  void OnBnClickedButtonPenRect();
  void OnBnClickedButtonRoundedRect();
  void OnBnClickedButtonTriangle();
  void OnBnClickedButtonRightTriangle();
  void OnBnClickedButtonPenText();
  void OnBnClickedButtonStraightLine();
  void OnBnClickedButtonPenSelect();
  void OnBnClickedButtonZoomOut();
  void OnBnClickedButtonZoomIn();
  void OnBnClickedButtonNext();
  void OnBnClickedButtonPre();
  void OnBnClickedButtonFirst();
  void OnBnClickedButtonLast();
  void OnBnClickedButtonAddPage();
  void OnBnClickedButtonErase();
  void UpdateScale(double scale);
  MainDisplayPreivewModel GetModel();
  void GetScale();
  void GetPageIndex(const std::function<void(int32_t page)>& callback = nullptr);
  void GetPageTotalIndex(const std::function<void(int32_t page)>& callback = nullptr);
  void GetPageInfo(const std::function<void(int32_t current, int32_t total)>& callback = nullptr);
  void WhiteboardGotoLastPage(std::function<void()> finish_cb = nullptr);
  void UpdatePageInfo(std::function<void(bool doc_vaild)> finish_cb = nullptr);
  void WhiteboardGoToPage(const uint32_t wb_page_index);
  void OnBnClickedButtonAddImage(QWidget* parent);
  void OnBnClickedButtonUpload2(QWidget* parent);
  void OnBnClickedButtonUpload(QWidget* parent);
  void OnBnClickedMultiSelect();
  void DocGotoPage(const uint32_t doc_page, std::function<void()> finish_cb = nullptr);

  
private slots:
  void RunIoContext();

private:
  void CoroutineRunUploadProcess(
    boost::asio::yield_context yield,
    const QString& file_path,
    const RunUploadType run_upload_type);
  void AbortCoroutineRunUploadProcess();

  void CoroutineRunUploadCombined(
    boost::asio::yield_context yield,
    const QString& file_path,
    const RunUploadType run_upload_type);

  bool CoroutineCreateDoc(
    boost::asio::yield_context yield,
    const QString& file_path,
    bool private_oss,
    alibaba::doc::CreateDocRsp* rsp,
    alibaba::dps::DPSError* dps_error);

  bool CoroutineCreateDocCombined(
    boost::asio::yield_context yield,
    const QString& file_path,
    bool private_oss,
    std::string* rsp,
    alibaba::dps::DPSError* dps_error);

  bool CoroutineTransCodeDocCombined(boost::asio::yield_context yield,
    const std::string& doc_id,
    const std::string& target_name,
    std::string* target_doc_id,
    std::vector<std::string> * url_list,
    alibaba::dps::DPSError* dps_error);

  bool CoroutineUpdateDocStatus(boost::asio::yield_context yield,
                            const std::string& doc_id,
                            DocUploadStatus status);

  bool CoroutineDoUpload(boost::asio::yield_context yield,
                     const std::string& doc_id,
                     const QString& file_path,
                     const std::string& ak,
                     const std::string& sk,
                     const std::string& access_token,
                     const std::string& obj_name,
                     const std::string& oss_bucket);

  bool CoroutineTransCodeDoc(boost::asio::yield_context yield,
                         const std::string& doc_id,
                         const std::string& target_name,
                         std::string* target_doc_id,
                         alibaba::dps::DPSError* dps_error);

  bool CoroutineGetDoc(boost::asio::yield_context yield,
                   const std::string& doc_id,
                   alibaba::doc::GetDocRsp* out_rsp,
                   alibaba::dps::DPSError* dps_error);

  void CoroutineSleep(boost::asio::yield_context yield,
                               const uint32_t ms);

  bool CoroutineGotoLastPage(boost::asio::yield_context yield,
                             const uint32_t timeout_ms,
                             uint32_t* total_page);

  bool CoroutineReportWhiteboardPageOperate(
    boost::asio::yield_context yield,
    const std::string& page_group,
    const int32_t page_group_size,
    const int32_t wb_page_number,
    const EnumWhiteboardPageOperate op_type,
    const std::string& type);

  void RequestUpdateDocUrls(boost::asio::yield_context yield, const UrlReplaceItem& request);
  void UpdateDocUrls(boost::asio::yield_context yield);
  void TransDocUrlsInMap();
  std::string GetUrlMainPath(const std::string& url);
  bool IsFileNeedTransCode(const std::string& file_ext);

  void AddImage(const std::string& url, int32_t width, int32_t height, std::function<void(const std::string&)> finish_cb = nullptr);

  void CoroutineSyncWhiteboardPageMappingRelation(boost::asio::yield_context yield);
  void AbortCoroutineSyncWhiteboardPageMappingRelation();

  void CoroutineUpdateDocInfo(boost::asio::yield_context yield);
  void AbortCoroutineUpdateDocInfo();

  bool CoroutineGetWhiteboardPageInfo(
    boost::asio::yield_context yield, 
    ::alibaba::wb::GetWhiteboardPageInfoRsp* rsp, 
    alibaba::dps::DPSError* dps_error);

  bool CoroutineWhiteboardTotalPageCount(
    boost::asio::yield_context yield,
    const uint32_t timeout_ms,
    uint64_t* total_page);

  bool CoroutineWhiteboardSetCurrentPageBackground(
    boost::asio::yield_context yield,
    const std::string& url,
    const uint32_t timeout_ms
  );

  bool CoroutineWhiteboardAppPage(
    boost::asio::yield_context yield,
    const uint32_t timeout_ms
  );

  bool CoroutineAddImage(
    boost::asio::yield_context yield,
    const uint32_t timeout_ms,
    const std::string& url, 
    const uint32_t width, 
    const uint32_t height);

  void CoroutineWhiteboardDocAddPage(
    boost::asio::yield_context yield);

  bool CoroutineUpdatePageInfo(
    boost::asio::yield_context yield,
    const uint32_t timeout_ms,
    bool* doc_vaild = nullptr
  );

private:
  void PrepareOssSdk();
  
  bool QueryDocInfoByWhiteboardPage(const uint32_t wb_page_index, std::string& doc_id, uint32_t& doc_page, uint32_t& doc_total_page);
  void AddPptImage(const std::vector<std::string>& urls);
  void AddPage(std::function<void()> finish_cb);
  void EventCallback(const std::string& event, const std::string& data);
  void ExecuteCommand(const std::string &api_name, const std::string &api_parameter);
  void ExecuteCommand(const std::string &api_name, 
    const std::string &api_parameter, 
    const std::function<void(const std::string&)>& callback);
  static void PostToUIThread(std::function<void()> cb);

private:

  MainDisplayPreivewModel model_;

  std::recursive_mutex mutex_doc_;
  std::unordered_map<std::string, int32_t> map_check_doc_status_;

  QTimer ui_thread_run_ioc_timer_;
  boost::asio::io_context ioc_;
  std::atomic_bool coroutine_abort_run_upload_process_ = false;
  std::atomic_bool coroutine_abort_sync_whiteboard_page_mapping_relation_ = false;
  std::atomic_bool coroutine_abort_update_doc_info_ = false;
};

