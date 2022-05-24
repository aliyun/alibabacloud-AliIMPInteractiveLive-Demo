# alicloud_impinteraction_sdk

本插件是`低代码音视频工厂标准集成（无UI）`（以下简称`标准集成`）的Flutter实现插件。

## 环境要求

### Android环境要求：

- 系统版本：支持Android 5.0及以上版本。
- API版本：支持21及以上版本。
- CPU架构：支持实体设备架构armeabi-v7a、arm64-v8a。
- 其他：不支持视频采集旋转，不支持USB外接摄像头。

### iOS环境要求：

- 设备类型：支持iPhone和iPad所有型号。
- CPU架构：支持iOS真机armv7、arm64架构，不支持模拟器i386、X86_64架构。
- 系统版本：支持iOS 10.0及以上版本。
- 其他：不支持bitcode，不支持屏幕旋转。 前提条件 客户端集成前，请确保已经通过控制台创建应用并获取客户端集成需要的信息（应用ID，APP
  Key，低代码集成服务地址，低代码集成服务密钥）。创建指引请参见创建应用。

## 开始接入

欲使用本插件，需了解`直播低代码`[接入流程](https://help.aliyun.com/document_detail/409684.html)

## 前提条件
客户端集成前，请确保已经通过控制台创建应用并获取客户端集成需要的信息（应用ID，APP Key，低代码集成服务地址，低代码集成服务密钥）。创建指引请参见[创建应用](https://help.aliyun.com/document_detail/409736.htm)

## 工程配置

### Android工程配置

#### minSdkVersion配置

工程配置中添加minSdkVersion配置项 (> 21)

```groovy
android {
  defaultConfig {
    minSdkVersion 21
  }
}
```

#### 覆盖android:label

在应用的AndroidManifest.xml文件的`<application>`标签中添加`'tools:replace="android:label"`
> add 'tools:replace="android:label"' to <application> element at AndroidManifest.xml

### iOS工程配置

#### 支持系统版本
iOS 10.0
- iOS工程 podFile中：
  > platform :ios, '10.0'
- xCode工程配置中：
  General -> DeploymentInfo -> iOS10.0

#### 关闭bitcode
不支持`bitcode`——需在工程配置中关闭`bitcode`

#### 配置设备权限
需要在Xcode工程本身的plist文件中主动配置以下三项：

| Key  |  Value |
|---|---|
|  Privacy - Camera Usage Description	 |  Use camera |
|  Privacy - Microphone Usage Description |  Use microphone |
|  Application uses Wi-Fi |  YES |


## 配置应用与API使用

完整的代码实现逻辑请参见[开源工程](https://github.com/aliyun/alibabacloud-AliIMPInteractiveLive-Demo/tree/master/Flutter/sdk)

### 初始化

需要传入应用配置信息
> （应用ID，APP Key，低代码集成服务地址，低代码集成服务密钥）。创建指引请参见[创建应用](https://help.aliyun.com/document_detail/409736.htm)
> 需注意AppKey区分平台，此插件中需同时传入 `appKey4Android` （Android平台AppKey）、`appKey4iOS`（iOS平台AppKey）

/assets 目录下创建
> app_settings_anchor.json
> app_settings_audience.json
> demo_param.json

分别配置：
app_settings_anchor.json
```json
{
  "userId": "",
  "role" : "anchor",
  "appId": "",
  "appKey4Android": "",
  "appKey4iOS": "",
  "tokenServerHost": "",
  "serverHost": "",
  "serverSecret": ""
}
```
app_settings_audience.json
```json
{
  "userId": "",
  "role" : "audience",
  "appId": "",
  "appKey4Android": "",
  "appKey4iOS": "",
  "tokenServerHost": "",
  "serverHost": "",
  "serverSecret": ""
}
```

demo_param.json
```json
{
  "liveId": "",
  "roomId": ""
}
```

```dart
  Future<void> initPlugin(anchor) async {
    M res = {'result': 'unknown'};
    try {
      String config =
          anchor ? 'app_settings_anchor.json' : 'app_settings_audience.json';
      final String settingJson = await rootBundle.loadString('assets/$config');
      appSettings = await json.decode(settingJson);
      String userId = (appSettings['userId'] as String).isEmpty
          ? Utils.randomName()
          : appSettings['userId'];

      final String paramJson =
          await rootBundle.loadString('assets/demo_param.json');
      demoParam = await json.decode(paramJson);

      String deviceId = await Utils.getId() ?? '';

      var param = {
        'userId': userId,
        'appId': appSettings['appId'],
        'appKey4Android': appSettings['appKey4Android'],
        'appKey4iOS': appSettings['appKey4iOS'],
        'serverHost': appSettings['serverHost'],
        'serverSecret': appSettings['serverSecret'],
        'deviceId': deviceId,
      };

      var appKey = defaultTargetPlatform == TargetPlatform.android
          ? appSettings['appKey4Android']
          : appSettings['appKey4iOS'];
      regEvent(appSettings['tokenServerHost'], userId, appSettings['appId'],
          appKey, deviceId);
      res = await IMPSdkRoomEngine.init(param);
      res = await IMPSdkRoomEngine.login();
      res = await IMPSdkRoomChannel.setRoomId({
        'roomId': 'ceba71c7-145e-436f-8a2b-cf23daf307be',
      });
      res = await IMPSdkRoomChannel.enterRoom({
        'nick': 'nickOf' + userId,
      });

      if (appSettings['role'] == 'anchor') {
        var param = {
          'resolution': 'VALUE_540P',
        };
        res = await IMPSdkLivePusher.startPreview(param);
        res = await IMPSdkLivePusher.startLive();
      } else {
        res = await IMPSdkLivePlayer.start();
      }
    } on PlatformException catch (e) {
      res!['result'] =
          'api e: code=${e.code}, msg=${e.message}, details=${e.details}';
      debugPrint('startPreview result:${res['result']}');
    }

    if (!mounted) return;

    debugPrint('init result:${res!['result']}');
    setState(() {
      _initResult = res!['result'];
    });
  }
```

#### 配置主播推流分辨率：
```dart
      var param = {
        'resolution': 'VALUE_540P',
      };
      res = await IMPSdkLivePusher.startPreview(param);
```
'resolution' 可取值：

| Key               | Value |
|-----------------|-----|
| 'VALUE_480P'    | 480P  |
| 'VALUE_540P'   | 540P  |
| 'VALUE_720P'   | 720P  |

### 开启直播或者进入直播间
```dart
  void setUpAsAnchor() {
  listComment();
  login();
}

void setUpAsAudience() {
  String inputId = sceneIdController.text;
  String? sceneId = inputId.isEmpty ? demoParam['liveId'] : inputId;
  if (sceneId?.isEmpty ?? true) {
    debugPrint('empty input');
  } else {
    listComment();
    login();
  }
}
```

