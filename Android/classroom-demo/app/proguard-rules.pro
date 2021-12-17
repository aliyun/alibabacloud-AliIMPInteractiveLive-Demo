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

-dontnote *.**
-dontwarn *.**

# live sdk
-keep class com.aliyun.** { *; }
-keep class com.alivc.** { *;}
-keep class com.cicada.** { *; }

# lwp
-keep class com.alibaba.dingpaas.** { *; }
-keep class com.dingtalk.mars.** { *; }
-keep class com.dingtalk.bifrost.** { *; }
-keep class com.dingtalk.mobile.** { *; }
-keep class org.android.spdy.** { *; }

# fastjson
-dontwarn com.alibaba.fastjson.**
-keepattributes Signature
-keepattributes *Annotation*

# document
-keep class com.aliyun.roompaas.document.exposable.DocumentService
-keep class com.aliyun.roompaas.document.DocumentServiceImpl

#oss
-keep class com.alibaba.sdk.android.oss.** { *; }
-dontwarn okio.**
-dontwarn org.apache.commons.codec.binary.**