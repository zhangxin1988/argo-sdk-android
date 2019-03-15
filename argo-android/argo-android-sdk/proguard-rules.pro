-optimizationpasses 5
##打印混淆的详细信息
#-verbose
##声明不压缩输入文件。
#-dontshrink
 # 不进行预校验,Android不需要,可加快混淆速度。
-dontpreverify
#将次选项去除，可以混淆时进行优化
-dontoptimize
# 屏蔽警告
-ignorewarnings
# 混淆时不会产生形形色色的类名(混淆时不使用大小写混合类名)
-dontusemixedcaseclassnames
# 指定不去忽略非公共的库类(不跳过library中的非public的类)
-dontskipnonpubliclibraryclasses
# 指定不去忽略包可见的库类的成员
-dontskipnonpubliclibraryclassmembers
# 保护代码中的Annotation不被混淆
-keepattributes *Annotation*
# 避免混淆泛型, 这在JSON实体映射时非常重要
-keepattributes Signature
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.app.job.JobService
-keep public class * extends android.accessibilityservice.AccessibilityService
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keepclassmembers class * {
   public <init>(org.json.JSONObject);
}
#################################################################
########################### 易观混淆 ############################
#################################################################

# 打通所有的包
-repackageclass com.analysys
# 混淆到包名下
-dontwarn com.analysys.**
# 保证API不混淆
-keep class com.analysys.AnalysysAgent{
 *;
}
-keep class com.analysys.AnalysysConfig{
 *;
}
-keep class com.analysys.utils.UtilsAgent {
 *;
 }
-keep class com.analysys.EncryptEnum{
 *;
}
-keep class com.analysys.push.** {
 *;
 }
-keep class com.analysys.push.ThirdPushManager$* {
     <methods>;
 }

-keep class com.analysys.hybrid.HybridBridge {
     <methods>;
 }

-keep class com.analysys.push.PushProvider {
    <fields>;
 }
