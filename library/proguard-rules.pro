# 模块级 ProGuard 配置入口。当前库不使用反射加载实现类，暂不添加 keep 规则。
# 以下为 Android 插件生成的参考模板，后续新增反射或 WebView JS 接口时再按需启用。
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\SDK\Studio\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

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
