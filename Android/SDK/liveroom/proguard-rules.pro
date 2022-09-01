# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-dontwarn com.aliyun.standard.liveroom.lib.**
#-keep class com.aliyun.standard.liveroom.lib.**  { *; }
-keep class com.aliyun.standard.liveroom.lib.LiveActivity  { *; }
-keep class com.aliyun.roompaas.roombase.response.**  { *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype  { *; }
-keepclassmembers class com.aliyun.standard.liveroom.LivePrototype.** { *; }
#-keepclassmembers class com.aliyun.standard.liveroom.lib.LivePrototype.**  { *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype$InitParam  { public protected *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype$OpenLiveParam  { public protected *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype$Role  { public protected *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype$CreateRoomListener  { public protected *; }

-keep class com.aliyun.standard.liveroom.lib.LiveHook  { public protected *; }
-keep class com.aliyun.standard.liveroom.lib.LivePrototype  { public protected *; }
-keep class com.aliyun.roompaas.uibase.util.AppUtil  { public protected *; }

-keep class com.aliyun.roompaas.live.exposable.LiveService
-keep class com.aliyun.roompaas.live.LiveServiceImpl
-keep class com.aliyun.roompaas.live.LiveInnerServiceImpl

-keep class com.aliyun.roompaas.chat.exposable.ChatService
-keep class com.aliyun.roompaas.chat.ChatServiceImpl

-keep class com.aliyun.roompaas.base.inner.InnerService

-keep class com.aliyun.roompaas.biz.exposable.model.TokenInfo

# fastjson proguard rules
# https://github.com/alibaba/fastjson
-dontwarn com.alibaba.fastjson.**
-keepattributes Signature
-keepattributes *Annotation*