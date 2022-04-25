## 配置应用

/assets 目录下创建
> app_settings_anchor.json
app_settings_audience.json
demo_param.json

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
  "liveId": ""
}
```

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


