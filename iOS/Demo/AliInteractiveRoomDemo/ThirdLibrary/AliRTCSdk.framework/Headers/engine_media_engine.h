#ifndef engine_media_engine_h
#define engine_media_engine_h

#include "engine_define.h"

namespace AliRTCSdk
{
    /**
     * @brief 音频track的类型
    */
    typedef enum {
      AliEngineAudioTrackNo = 0,
      AliEngineAudioTrackMic = 1,
      AliEngineAudioTrackEnd = 0xffffffff
    } AliEngineAudioTrack;

    /**
     * @brief 视频track的类型
     */
    typedef enum {
        AliEngineVideoTrackNo     = 0,
        AliEngineVideoTrackCamera = 1,
        AliEngineVideoTrackScreen = 2,
        AliEngineVideoTrackBoth   = 3,
    } AliEngineVideoTrack;


    /**
     * @brief 相机流类型
     */
    typedef enum {
      AliEngineVideoStreamTypeNone = 0,
      AliEngineVideoStreamTypeHigh = 1,
      AliEngineVideoStreamTypeLow = 2,
    } AliEngineVideoStreamType;

    /**
     * @brief 视频数据源
     */
    typedef enum {
      AliEngineVideoSourceCamera = 0,
      AliEngineVideoSourceScreenShare = 1,
      AliEngineVideosourceTypeMax,
    } AliEngineVideoSource;

    /**
     * @brief 录制视频数据源
    */
    typedef enum {
      AliEngineVideoRecordSourceCamera = 0,
      AliEngineVideoRecordSourceScreenShare = 1,
      AliEngineVideoRecordSourceBoth = 2,
      AliEngineVideoRecordSourceTypeMax,
    } AliEngineVideoRecordSource;

    /**
     * @brief 视频数据类型
     */
    typedef enum {
        AliEngineVideoFormatUnknow  = -1,
        AliEngineVideoFormatBGRA    = 0,
        AliEngineVideoFormatI420    = 1,
        AliEngineVideoFormatNV21    = 2,
        AliEngineVideoFormatNV12    = 3,
        AliEngineVideoFormatRGBA    = 4,
        AliEngineVideoFormatI422    = 5,
        AliEngineVideoFormatARGB    = 6,
        AliEngineVideoFormatABGR    = 7,
        AliEngineVideoFormatRGB24   = 8,
        AliEngineVideoFormatBGR24   = 9,
        AliEngineVideoFormatRGB565  = 10,
        AliEngineVideoFormatTextureOES  = 11,
        AliEngineVideoFormatTexture2D  = 12,
    } AliEngineVideoFormat;

    /**
     * @brief 视频数据格式
     */
    typedef enum {
        AliEngineBufferTypeRawData          = 0,
        AliEngineBufferTypeTexture,
    } AliEngineBufferType;


    /**
     * @brief 视频纹理格式
     */
    typedef enum {
      AliEngineTextureTypeUnknown = -1,
      AliEngineTextureTypePre = 0,
      AliEngineTextureTypePost = 1,
    } AliEngineTextureType;

    /**
     * @brief 视频窗口填充方式
     */
    typedef enum {
        AliEngineRenderModeAuto     = 0,
        AliEngineRenderModeStretch  = 1,
        AliEngineRenderModeFill     = 2,
        AliEngineRenderModeCrop     = 3,
        AliEngineRenderModeScroll   = 4,
        AliEngineRenderModeNoChange = 99,
    } AliEngineRenderMode;

    /**
     * @brief 视频旋转角度
     */
    typedef enum {
      AliEngineRotationModeNoChange = -1,
      AliEngineRotationMode_0 = 0,
      AliEngineRotationMode_90 = 90,
      AliEngineRotationMode_180 = 180,
      AliEngineRotationMode_270 = 270,
    } AliEngineRotationMode;

    /**
     * @brief 音频声道类型
    */
    typedef enum {
      AliEngineMonoAudio = 1,
      AliEngineStereoAudio = 2,
    } AliEngineAudioNumChannelType;

    /**
     * @brief 音频采样率类型
     */
    typedef enum {
      AliEngineAudioSampleRate_8000 = 0,
      AliEngineAudioSampleRate_11025 = 1,
      AliEngineAudioSampleRate_16000 = 2,
      AliEngineAudioSampleRate_22050 = 3,
      AliEngineAudioSampleRate_32000 = 4,
      AliEngineAudioSampleRate_44100 = 5,
      AliEngineAudioSampleRate_48000 = 6,
    } AliEngineAudioSampleRate;

    /**
     * @brief 音频数据源
     */
    typedef enum {
      AliEngineAudiosourcePub = 0,
      AliEngineAudiosourceSub = 1,
      AliEngineAudiosourceRawData = 2,
    } AliEngineAudioSource;

    /**
     * @brief 视频数据输出位置
     */
    typedef enum {
      AliEnginePositionPostCapture = 1 << 0,
      AliEnginePositionPreRender = 1 << 1,
      AliEnginePositionPreEncoder = 1 << 2,
    } AliEngineVideoObserPosition;

    /**
     * @brief 音频裸数据
     */
    typedef struct  {
        void* dataPtr        = 0;
        int numOfSamples    = 0;
        int bytesPerSample  = 0;
        int numOfChannels   = 0;
        int sampleRate = 0;
        int samplesPerSec   = 0;
    } AliEngineAudioRawData;

    /**
     * @brief 视频裸数据
     */
    typedef struct  {
        AliEngineVideoFormat format = AliEngineVideoFormatUnknow;
        AliEngineBufferType type    = AliEngineBufferTypeRawData;
        long dataLength  = 0;
        void* pixelBuffer = 0;
        void* dataPtr    = 0;
        void* dataYPtr   = 0;
        void* dataUPtr   = 0;
        void* dataVPtr   = 0;
        int strideY      = 0;
        int strideU      = 0;
        int strideV      = 0;
        int height       = 0;
        int width        = 0;
        int rotation     = 0;
        int stride       = 0;
        long long timeStamp = 0;
        int textureId    = 0;
        float transformMatrix[16] = {0};    
    } AliEngineVideoRawData;

    /**
     * @brief 音频数据输出
     */
    class ALI_RTC_API IAudioFrameObserver {
    public:
      virtual ~IAudioFrameObserver() {}
      /**
       * @brief 本地采集音频数据回调
       */
      virtual bool OnRawDataAudioFrame(AliEngineAudioRawData audioRawData) = 0;

      /**
       * @brief 本地推流音频数据回调
       */
      virtual bool OnPublishAudioFrame(AliEngineAudioRawData audioRawData) = 0;

      /**
       * @brief 本地订阅音频数据回调
       */
      virtual bool OnSubscribeAudioFrame(AliEngineAudioRawData audioRawData) = 0;
        
      /**
       * @brief 混音前的指定用户的音频数据回调
      */
      virtual bool OnPlaybackAudioFrameBeforeMixing(const char *uid, AliEngineAudioRawData audioRawData) = 0;
    };

    /**
     * @brief 视频数据对象
     */
    class ALI_RTC_API IVideoFrameObserver {
    public:
      virtual ~IVideoFrameObserver() {}
      /**
       * @brief 订阅的本地采集视频数据回调
       * @param videoSource video source
       * @param videoRawData video sample
       * @return true: 需要写回SDK（只对I420和native(ios/mac)有效），false: 不需要写回SDK
       */
      virtual bool OnCaptureVideoSample(AliEngineVideoSource videoSource, AliEngineVideoRawData &videoRawData) = 0;

      /**
       * @brief 订阅的本地编码前视频数据回调
       * @param videoSource video source
       * @param videoRawData video sample
       * @return true: 需要写回SDK（只对I420和native(ios/mac)有效），false: 不需要写回SDK
       */
      virtual bool OnPreEncodeVideoSample(AliEngineVideoSource videoSource, AliEngineVideoRawData &videoRawData) = 0;

      /**
       * @brief 订阅的远端视频数据回调
       * @param uid user id
       * @param videoSource video source
       * @param videoRawData video sample
       * @return true: 需要写回SDK（只对I420和native(ios/mac)有效），false: 不需要写回SDK
       */
      virtual bool OnRemoteVideoSample(const char *uid,
        AliEngineVideoSource videoSource,
        AliEngineVideoRawData &videoRawData) = 0;

      /**
       * @brief 视频数据输出格式
       * @return 期望视频输出格式
       */ 
      virtual AliEngineVideoFormat GetVideoFormatPreference() { return AliEngineVideoFormatI420; }

      /**
       * @brief 视频数据输出内容
       * @return 期望视频输出内容，参考AliEngineVideoObserPosition
       */
      virtual uint32_t GetObservedFramePosition() { return static_cast<uint32_t>(AliEnginePositionPostCapture | AliEnginePositionPreRender); };
    };


    /**
     * @brief 视频纹理数据对象
     */
    class ALI_RTC_API IVideoTextureObserver {
    public:
      virtual ~IVideoTextureObserver() {}
      /**
       * @brief 视频GL纹理创建回调
       * @param context context
       */
      virtual void OnTextureCreate(void* context) {}

      /**
       * @brief 视频GL纹理更新回调
       * @param textureId texture id
       * @param width width
       * @param height height
       * @param videoRawData video sample
       */
      virtual int OnTextureUpdate(int textureId, int width, int height, AliEngineVideoRawData &videoRawData) { return 0; }

      /**
       * @brief 视频GL纹理销毁回调
       */
      virtual void OnTextureDestory() {}
    };

    /**
     * @brief 音视频媒体数据相关功能
     */
    class ALI_RTC_API IAliEngineMediaEngine
    {
    public:
      /**
       * 释放IAliEngineMediaEngine资源
       */
      virtual void Release() = 0;

      /**
       * @brief 订阅视频数据输出
       * @param observer 视频数据接收对象
       * @note 输出数据将通过IVideoFrameObserver回调返回
       */
      virtual void RegisterVideoSampleObserver(IVideoFrameObserver* observer) = 0;

      /**
       * @brief 取消订阅视频数据输出
       */
      virtual void UnRegisterVideoSampleObserver(IVideoFrameObserver* observer) = 0;

      /**
       * @brief 订阅本地视频纹理数据
       * @param observer 视频数据接收对象
       * @note 输出数据将通过IVideoTextureObserver回调返回
       */
      virtual void RegisterLocalVideoTextureObserver(IVideoTextureObserver* observer) = 0;

      /**
       * @brief 取消本地视频纹理数据输出
       */
      virtual void UnRegisterLocalVideoTextureObserver(IVideoTextureObserver* observer) = 0;

      /**
       * @brief 订阅音频数据输出
       * @param observer 音频数据接收对象
       * @return 0为成功，非0失败
       * @note 调用前需要先通过SubscribeAudioData/SetSubscribeAudioNumChannel/SetSubscribeAudioSampleRate接口设置需要输出的音频数据参数
       */
      virtual int RegisterAudioFrameObserver(IAudioFrameObserver* observer) = 0;
        
      /**
       * @brief 取消音频数据输出
       * @param observer 音频数据接收对象
       */
      virtual void UnRegisterAudioFrameObserver(IAudioFrameObserver* observer) = 0;
      
      /**
       * @brief 订阅音频事件（默认订阅）, 如音频啸叫
       * @return 0为成功，非0失败
       */
      virtual int RegisterAudioEventObserver() = 0;
        
      /**
       * @brief 取消订阅音频事件
       */
      virtual void UnRegisterAudioEventObserver() = 0;

      /**
       * @brief 设置输出音频声道数，默认单声道（混音前数据不支持该参数设置）
       * @param audioNumChannel 声道数
       */
      virtual void SetSubscribeAudioNumChannel(AliEngineAudioNumChannelType audioNumChannel) = 0;

      /**
       * @brief 设置输出音频采样率，默认44.1k（混音前数据不支持该参数设置）
       * @param audioSampleRate 采样率
       */
      virtual void SetSubscribeAudioSampleRate(AliEngineAudioSampleRate audioSampleRate) = 0;

      /**
       * @brief 订阅音频数据
       * @param audioSource 音频数据源
       * @note 订阅音频数据输出前，需先通过SetSubscribeAudioNumChannel与SetSubscribeAudioSampleRate设置输出音频数据参数
       */
      virtual void SubscribeAudioData(AliEngineAudioSource audioSource) = 0;

      /**
       * @brief 取消订阅音频数据
       * @param audioSource 音频数据源
       */
      virtual void UnsubscribeAudioData(AliEngineAudioSource audioSource) = 0;

      /**
       * @brief 启用外部视频输入源
       * @param enable YES 开启，NO 关闭
       * @param useTexture 是否使用texture 模式
       * @param type 流类型
       * @return 0为成功，非0失败
       */
      virtual int SetExternalVideoSource(bool enable,
        bool useTexture,
        AliEngineVideoTrack type,
        AliEngineRenderMode renderMode) = 0;

      /**
       * @brief 输入视频数据
       * @param frame 帧数据
       * @param type 流类型
       * @return 0为成功，非0失败
       */
      virtual int PushExternalVideoFrame(const AliEngineVideoRawData &frame,
        AliEngineVideoTrack type) = 0;

      /**
       * @brief 设置是否启用外部音频输入源
       * @param enable YES 开启，NO 关闭
       * @param sampleRate 采样率 16000 48000...
       * @param channelsPerFrame 声道数 1 or 2
       * @return return>=0 Success, return<0 Failure
       */
      virtual int SetExternalAudioSource(bool enable,
        unsigned int sampleRate,
        unsigned int channelsPerFrame) = 0;

      /**
       * @brief 输入音频数据
       * @param data 音频数据 不建议超过40ms数据
       * @param sampleLen 采样
       * @param timestamp 时间戳
       * @return return>=0 Success, return<0 Failure
       */
      virtual int PushExternalAudioFrameRawData(const void * data,
        unsigned int sampleLen,
        double timestamp) = 0;

      /**
       * @brief 设置混音音量
       * @param vol 音量 0-100
       * @return 0为成功，非0失败
       */
      virtual int SetExternalAudioPublishVolume(int vol) = 0;

      /**
       * @brief 获取混音音量
       * @return vol 音量
       */
      virtual int GetExternalAudioPublishVolume() = 0;

      /**
       * @brief 设置是否与麦克风采集音频混合
       * @param mixed YES 混音，NO 完全替换麦克风采集数据
       */
      virtual int SetMixedWithMic(bool mixed) = 0;

      /**
       * @brief 设置是否启用外部输入音频播放
       * @param enable YES 开启，NO 关闭
       * @param sampleRate 采样率 16k 48k...
       * @param channelsPerFrame 采样率 16k 48k...
       * @return >=0表示成功， <0表示失败
       * @note 可通过接口setExternalAudioPlayoutVolume设置输入音频播放音量
       */
      virtual int SetExteranlAudioRender(bool enable, unsigned int sampleRate, unsigned int channelsPerFrame) = 0;

      /**
       * @brief 输入外部音频播放数据
       * @param audioSamples 音频数据
       * @param sampleLength 音频数据长度
       * @param sampleRate 音频采样率
       * @param channelsPerFrame 音频声道数
       * @param timestamp 时间戳
       * @return <0表示失败
       */
      virtual int PushExternalAudioRenderRawData(const void* audioSamples, unsigned int sampleLength, unsigned int sampleRate, unsigned int channelsPerFrame, long long timestamp) = 0;

      /**
       * @brief 设置音频外部播放源音量
       * @return <0表示失败
       */
      virtual int SetExternalAudioRenderVolume(int volScal) = 0;

      /**
       * @brief 获取音频外部播放源音量
       * @return <0表示失败
       */
      virtual int GetExternalAudioRenderVolume() = 0;

    protected:
      virtual ~IAliEngineMediaEngine() {}
    };
    
}

#endif /* engine_media_engine_h */
