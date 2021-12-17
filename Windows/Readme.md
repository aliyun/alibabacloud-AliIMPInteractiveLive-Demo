====

## Windows 环境

### 下载依赖

由于github对大文件上传有限制，依赖库需要手动下载，并放到当前deps目录下,大致目录结构应如下：

```
+--classroom
+--camke
+--deps
|    +--gaea
|    +--imageformats
|    +--json
|    +--meta
|    +--OpenSSL
|    +--oss_sdk
|    +--player
|    +--whiteboard
|    +--AliRTCSdk
|    +--boost
|    +--curl
|    +--dps
+--build_win.bat
+--CMakeLists.txt
+--Readme.md
```

下载地址：https://paas-sdk.oss-cn-shanghai.aliyuncs.com/paas/imp/windows/deps.zip

### 安装VS2017

从微软官方下载vs2017安装器，其中c++桌面开发为必选项

### 安装 Qt5.12.2_vs2017

http://download.qt.io/official_releases/qt/5.12/5.12.2/qt-opensource-windows-x86-5.12.2.exe

### 安装cmake

https://github.com/Kitware/CMake/releases/download/v3.22.1/cmake-3.22.1-windows-x86_64.msi
并将cmake的bin路径添加到环境变量PATH

### 使用批处理编译

build_win.bat

### windows下需可能要注意的点

1.需要先修改build_win的环境变量，其中vs2017, 如果使用的是专业版，那么路径应该为下方路径
set vs2017="C:\Program Files (x86)\Microsoft Visual Studio\2017\Professional\Common7\IDE\devenv.exe"
如果使用的是社区版，那么需要修改devenv.exe的路径

2.Qt的安装目录，如果安装到D盘，如以下所示
set QTDIR=D:\Qt\Qt5.12.2\5.12.2\msvc2017
如果安装到其他位置，需要自行修改，注意路径不能有中文

3.CMake.exe 需要设置到PATH环境变量中

4.SDK及三方依赖都放到了deps文件夹
