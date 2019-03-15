package com.analysys.utils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 系统工具类
 * @Version: 1.0
 * @Create: 2018/3/7
 * @Author: Wang-X-C
 */

public class Utils {

  /**
   * 获取随机数
   */
  public static long getRandomNumb(int startNum, int endNum) {
    if (endNum > startNum) {
      Random random = new Random();
      return (long) random.nextInt(endNum - startNum) + startNum;
    }
    return 0;
  }

  /**
   * 应用上线渠道
   */
  public static String getManifestKeyChannel(Context context, String type) {
    String value = null;
    try {
      ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(
          context.getPackageName(), PackageManager.GET_META_DATA);
      if (type.equals(Constants.SP_APP_KEY)) {
        value = appInfo.metaData.getString(Constants.DEV_ANALYSYS_APPKEY);
      } else if (type.equals(Constants.SP_CHANNEL)) {
        value = appInfo.metaData.getString(Constants.DEV_ANALYSYS_CHANNEL);
      }
    } catch (Throwable e) {
    }
    return value;
  }

  /**
   * 获取当前时间,格式 yyyy-MM-dd hh:mm:ss.SSS
   */
  public static String getTime() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
        "yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    Date date = new Date(System.currentTimeMillis());
    String time = simpleDateFormat.format(date);
    return time;
  }

  public static List<String> toList(String names) {
    List<String> list = new ArrayList<String>();
    if (names.indexOf("$$") != -1) {
      String[] array = names.split("\\$\\$");
      for (int i = 0; i < array.length; i++) {
        list.add(array[i]);
      }
    } else {
      list.add(names);
    }
    return list;
  }

  public static String toString(List<String> list) {
    String names = "";
    if (list != null && !list.isEmpty()) {
      for (String name : list) {
        if (Utils.isEmpty(names)) {
          names = name;
        } else {
          names += "$$" + name;
        }
      }
    }
    return names;
  }

  /**
   * Json 转 Map
   */
  public static Map<String, Object> jsonToMap(JSONObject jsonObject) {
    Map<String, Object> map = new HashMap<String, Object>();
    Iterator<String> keys = jsonObject.keys();
    while (keys.hasNext()) {
      Object key = keys.next();
      if (key == null) {
        continue;
      }
      String sKey = key.toString();
      map.put(sKey, jsonObject.optString(sKey));
    }
    return map;
  }

  /**
   * gzip压缩 base64编码
   */
  public static String messageZip(String message) {
    byte[] gzipMessage = ZipUtils.compressForGzip(message);
    String baseMessage = Base64Utils.encode(gzipMessage);
    if (Utils.isEmpty(baseMessage)) {
      return "";
    }
    return baseMessage;
  }

  /**
   * base64解码 gzip解压缩
   */
  public static String messageUnzip(String message) {
    if (Utils.isEmpty(message)) {
      return null;
    }
    byte[] base64Message = Base64Utils.decode(message);
    String gzipMessage = ZipUtils.decompressForGzip(base64Message);
    if (Utils.isEmpty(gzipMessage)) {
      return message;
    }
    return gzipMessage;
  }

  private static String appId = null;

  /**
   * aes加密 gzip压缩 base64编码
   */
  public static String messageEncrypt(Context context, String message) {
    try {
      if (Utils.isEmpty(Constants.requestTime)) {
        Constants.requestTime = String.valueOf(System.currentTimeMillis());
      }
      if (Utils.isEmpty(appId)) {
        appId = getAppKey(context);
      }
      String pwd = getEncryptKey(Constants.PLATFORM, appId, Constants.DEV_SDK_VERSION_NAME, Constants.requestTime);
      if (pwd != null) {
        return AESUtils.encrypt(message.getBytes(), pwd.getBytes());
      }
    } catch (Throwable throwable) {
    }
    return null;
  }

  /**
   * 获取加密key
   */
  private static String getEncryptKey(String platform, String appId, String sdkVersionName, String time) {
    String key = "";
    try {
      String originalKey = platform + appId + sdkVersionName + time;
      MessageDigest md5 = MessageDigest.getInstance("MD5");
      String md5Code = AESUtils.toHex(md5.digest(originalKey.getBytes("utf-8")));
      String encodeKey = Base64Utils.encode(md5Code.getBytes());
      String[] versionCode = sdkVersionName.split("\\.");
      int length = versionCode.length;
      if (length > 2) {
        int valuePosition = Integer.parseInt(versionCode[length - 1]);
        int startPosition = Integer.parseInt(versionCode[length - 2]);
        key = getEncryptCode(encodeKey, startPosition, valuePosition);
      }
    } catch (Throwable e) {
    }
    return key;
  }

  /**
   * 加密秘钥 生成
   *
   * @param startPosition 取数的起始位置 偶数从前往后，奇数从后往前
   * @param valuePosition 取数位置，取奇数位还是偶数位 奇数取奇数位值，偶数取值偶数位值
   */
  private static String getEncryptCode(String rowKey, int startPosition, int valuePosition) {
    if (rowKey == null) {
      return null;
    }
    if (startPosition % 2 != 0) {
      rowKey = new StringBuilder(rowKey).reverse().toString();
    }
    StringBuffer sbKey = new StringBuffer();
    if (valuePosition % 2 != 0) {
      for (int i = 0; i < rowKey.length(); i++) {
        sbKey.append(rowKey.charAt(i));
        i++;
      }
    } else {
      for (int i = 0; i < rowKey.length(); i++) {
        i++;
        sbKey.append(rowKey.charAt(i));
      }
    }
    if (sbKey.length() < 16) {
      for (int i = rowKey.length() - 1; 0 < i; i--) {
        sbKey.append(rowKey.charAt(i));
        if (sbKey.length() == 16) {
          return sbKey.toString();
        }
      }
    } else {
      return sbKey.substring(0, 16);
    }
    return sbKey.toString();
  }

  /**
   * 获取上传头
   */
  public static String getSpvInfo(Context context) {
    try {
      String appId = getAppKey(context);
      String sdkVersion = Constants.DEV_SDK_VERSION_NAME;
      String policyVersion = String.valueOf(AnsSpUtils.getParam(context, Constants.SP_SERVICE_HASH, ""));

      PackageManager manager = context.getPackageManager();
      PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
      String appVersion = info.versionName;
      String spv = Constants.PLATFORM + "|" + appId + "|" + sdkVersion + "|" + policyVersion + "|" + appVersion;
      return Base64Utils.encode(spv.getBytes());
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 是否首次启动
   */
  public static boolean isFirstStart(Context context) {
    if (context != null) {
      String first = String.valueOf(AnsSpUtils.getParam(context,
          Constants.SP_FIRST_START_TIME, ""));
      return Utils.isEmpty(first);
    }
    return false;
  }

  /**
   * 数据首次校验完成后调用
   */
  static String firstStart = null;

  public static void setFirstDate(Context context) {
    if (Utils.isEmpty(firstStart)) {
      firstStart = String.valueOf(
          AnsSpUtils.getParam(context, Constants.SP_FIRST_START_TIME, ""));
      if (Utils.isEmpty(firstStart)) {
        String time = Utils.getTime();
        AnsSpUtils.setParam(context, Constants.SP_FIRST_START_TIME, time);
        firstStart = time;
      }
    }
  }

  /**
   * 获取当前日期,格式 yyyy/MM/dd
   */
  public static String getDay() {
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
        "yyyy-MM-dd", Locale.getDefault());
    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+08:00"));
    Date date = new Date(System.currentTimeMillis());
    String time = simpleDateFormat.format(date);
    return time;
  }

  public static String checkUrl(String url, String protocol) {
    String subUrl = url.substring(protocol.length(), url.length());
    int index = subUrl.lastIndexOf("/");
    if (index == -1) {
      return url;
    } else if (index == subUrl.length() - 1) {
      return url.substring(0, url.length() - 1);
    } else {
      return null;
    }
  }

  /**
   * 判断数据是否为空值
   */
  public static boolean isEmpty(Object object) {
    if (object == null) {
      return true;
    } else if (object instanceof String) {
      return TextUtils.isEmpty((String) object) ? true : false;
    } else if (object instanceof JSONObject) {
      return ((JSONObject) object).length() < 1 ? true : false;
    } else if (object instanceof JSONArray) {
      return ((JSONArray) object).length() < 1 ? true : false;
    } else if (object instanceof Map) {
      return ((Map) object).isEmpty() ? true : false;
    } else if (object instanceof List) {
      return ((List) object).isEmpty() ? true : false;
    } else if (object instanceof Set) {
      return ((Set) object).isEmpty() ? true : false;
    } else {
      return false;
    }
  }

  /**
   * 判断是否为主进程
   */
  public static boolean isMainProcess(Context context) {
    if (context == null) {
      return false;
    }
    ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
    List<ActivityManager.RunningAppProcessInfo> runningApps = activityManager.getRunningAppProcesses();
    if (runningApps == null) {
      return false;
    }
    String process = "";
    for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
      if (proInfo.pid == android.os.Process.myPid()) {
        if (proInfo.processName != null) {
          process = proInfo.processName;
        }
      }
    }
    return context.getPackageName().equals(process);
  }

  /**
   * 获取网络类型
   */
  public static String networkType(Context context) {
    String netType = "";
    // 检测权限
    if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
      return "Unknown";
    }
    ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context
        .CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    if (networkInfo == null) {
      return netType;
    }
    int nType = networkInfo.getType();
    if (nType == ConnectivityManager.TYPE_WIFI) {
      netType = "WIFI";
    } else if (nType == ConnectivityManager.TYPE_MOBILE) {
      int nSubType = networkInfo.getSubtype();
      return String.valueOf(nSubType);
    }
    return netType;
  }

  /**
   * 检测权限
   *
   * @param context Context
   * @param permission 权限名称
   * @return true:已允许该权限; false:没有允许该权限
   */
  public static boolean checkPermission(Context context, String permission) {
    boolean result = false;
    if (Build.VERSION.SDK_INT >= 23) {
      try {
        Class<?> clazz = Class.forName("android.content.Context");
        Method method = clazz.getMethod("checkSelfPermission", String.class);
        int rest = (Integer) method.invoke(context, permission);
        result = rest == PackageManager.PERMISSION_GRANTED;
      } catch (Throwable e) {
        result = false;
      }
    } else {
      PackageManager pm = context.getPackageManager();
      if (pm.checkPermission(permission,
          context.getPackageName()) == PackageManager.PERMISSION_GRANTED) {
        result = true;
      }
    }
    return result;
  }

  /**
   * 获取 AppKey
   */
  public static String getAppKey(Context context) {
    try {
      return String.valueOf(AnsSpUtils.getParam(
          context, Constants.SP_APP_KEY, ""));
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 检测当的网络状态
   *
   * @param context Context
   * @return true 表示网络可用
   */
  public static boolean isNetworkAvailable(Context context) {
    if (!checkPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
      return false;
    }
    ConnectivityManager connectivity = (ConnectivityManager)
        context.getSystemService(Context.CONNECTIVITY_SERVICE);
    if (connectivity != null) {
      NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
      if (networkInfo != null && networkInfo.isConnected()) {
        // 当前网络是连接的
        if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
          // 当前所连接的网络可用
          return true;
        }
      }
    }
    return false;
  }

  /**
   * 两个json合并,source 参数合并到 dest参数
   *
   * @throws Exception
   */
  public static void mergeJson(final JSONObject source, JSONObject dest) throws Exception {
    if (Utils.isEmpty(source)) {
      return;
    } else if (dest == null) {
      return;
    }
    Iterator<String> superPropertyIterator = source.keys();
    while (superPropertyIterator.hasNext()) {
      String key = superPropertyIterator.next();
      Object value = source.get(key);
      dest.put(key, value);
    }
  }

  /**
   * 读取流
   */
  public static String readStream(InputStream is) {
    StringBuffer sb = null;
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(is));
      String line = null;
      sb = new StringBuffer();
      while ((line = reader.readLine()) != null) {
        sb.append(line);
      }
      reader.close();
    } catch (Throwable throwable) {
    } finally {
      try {
        is.close();
      } catch (Throwable e) {
      }
    }
    return sb.toString();
  }

  /**
   * 带参数 反射
   */
  public static Object reflexUtils(String classPath, String methodName, Class[] classes, Object... objects) {
    try {
      Class<?> cl = Class.forName(classPath);
      Method method = cl.getDeclaredMethod(methodName, classes);
      //类中的成员变量为private,必须进行此操作
      method.setAccessible(true);
      Object object = cl.newInstance();
      return method.invoke(object, objects);
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 无参 反射
   */
  public static Object reflexUtils(String classPath, String methodName) {
    try {
      Class<?> cl = Class.forName(classPath);
      Method method = cl.getDeclaredMethod(methodName);
      //类中的成员变量为private,必须进行此操作
      method.setAccessible(true);
      Object object = cl.newInstance();
      return method.invoke(object);
    } catch (Throwable e) {
      AnsLog.e(e);
    }
    return null;
  }
}