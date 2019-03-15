package com.analysys.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import com.analysys.process.Session;
import java.io.BufferedReader;
import java.io.FileReader;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/24 11:06
 * @Author: Wang-X-C
 */
public class MethodUtils {

  /**
   * 获取 Channel
   */
  public static String getChannel(Context context) {
    String channel = "";
    try {
      channel = String.valueOf(AnsSpUtils.getParam(context, Constants.SP_CHANNEL, ""));
      if (Utils.isEmpty(channel)) {
        channel = Utils.getManifestKeyChannel(context, Constants.SP_CHANNEL);
        if (!Utils.isEmpty(channel)) {
          AnsSpUtils.setParam(context, Constants.SP_CHANNEL, channel);
        }
      }
    } catch (Throwable throwable) {
    }
    return channel;
  }

  /**
   * 获取AppKey
   */
  public Object getAppId(Context context) {
    return Utils.getAppKey(context);
  }

  /**
   * 获取distinct id 如果用户没有调用，获取androidId
   */
  public static String getUserId(Context context) {
    String id = String.valueOf(AnsSpUtils.getParam(context, Constants.SP_ALIAS_ID, ""));
    if (!Utils.isEmpty(id)) {
      return id;
    }
    id = String.valueOf(AnsSpUtils.getParam(context, Constants.SP_DISTINCT_ID, ""));
    if (!Utils.isEmpty(id)) {
      return id;
    }
    id = String.valueOf(AnsSpUtils.getParam(context, Constants.SP_UUID, ""));
    if (!Utils.isEmpty(id)) {
      return id;
    }
    id = String.valueOf(java.util.UUID.randomUUID());
    AnsSpUtils.setParam(context, Constants.SP_UUID, id);
    return id;
  }

  /**
   * 获取当前时间
   */
  public Object getCurrentTime(Context context) {
    return System.currentTimeMillis();
  }

  /**
   * 获取时区
   */
  public String getTimeZone(Context context) {
    return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT);
  }

  /**
   * 获取生产厂商
   */
  public String getManufacturer(Context context) {
    return Build.MANUFACTURER;
  }

  /**
   * 获取应用版本名称
   */
  public static String getVersionName(Context context) {
    try {
      final PackageManager packageManager = context.getPackageManager();
      final PackageInfo packageInfo = packageManager.
          getPackageInfo(context.getPackageName(), 0);
      return packageInfo.versionName;
    } catch (Throwable e) {
      return "";
    }
  }

  public String getDeviceModel(Context context) {
    return Build.MODEL;
  }

  public String getOSVersion(Context context) {
    return Constants.DEV_SYSTEM + " " + Build.VERSION.RELEASE;
  }

  /**
   * 网路状态
   */
  public String getNetwork(Context context) {
    return Utils.networkType(context);
  }

  /**
   * 获取当前的运营商
   */
  public static String getOperatorName(Context context) {
    if (Utils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
      TelephonyManager mTelephonyMgr = (TelephonyManager)
          context.getSystemService(Context.TELEPHONY_SERVICE);
      String imsi = mTelephonyMgr.getSubscriberId();
      if (!Utils.isEmpty(imsi)) {
        if (imsi.startsWith("46000") || imsi.startsWith("46002")) {
          return "中国移动";
        } else if (imsi.startsWith("46001")) {
          return "中国联通";
        } else if (imsi.startsWith("46003")) {
          return "中国电信";
        }
      }
    }
    return null;
  }

  /**
   * 获取屏幕宽度
   */
  public static Object getScreenWidth(Context context) {
    int width = -1;
    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      DisplayMetrics metrics = new DisplayMetrics();
      activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      width = metrics.widthPixels;
    } else {
      WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
      DisplayMetrics dm = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(dm);
      width = dm.widthPixels;
    }
    if (width == -1) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      width = dm.widthPixels;
    }
    return width;
  }

  /**
   * 获取 屏幕高度
   * 如果context是Activity获取的是物理的屏幕尺寸 如果不是获取的是Activity的尺寸
   */
  public Object getScreenHeight(Context context) {
    int height = -1;
    if (context instanceof Activity) {
      Activity activity = (Activity) context;
      DisplayMetrics metrics = new DisplayMetrics();
      activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
      height = metrics.heightPixels;
    } else {
      WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
      DisplayMetrics dm = new DisplayMetrics();
      wm.getDefaultDisplay().getMetrics(dm);
      height = dm.heightPixels;
    }
    if (height == -1) {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      height = dm.heightPixels;
    }
    return height;
  }

  /**
   * 获取品牌
   */
  public Object getBrand(Context context) {
    return Build.BRAND;
  }

  /**
   * 获取语言
   */
  public Object getDeviceLanguage(Context context) {
    return Locale.getDefault().getLanguage();
  }

  /**
   * 是否首日访问
   */
  public Object isFirstDay(Context context) {
    String nowTime = Utils.getDay();
    String firstDay = String.valueOf(AnsSpUtils.getParam(
        context, Constants.DEV_IS_FIRST_DAY, ""));
    if (Utils.isEmpty(firstDay)) {
      AnsSpUtils.setParam(context, Constants.DEV_IS_FIRST_DAY, nowTime);
      return true;
    } else {
      return firstDay.equals(nowTime);
    }
  }

  /**
   * 获取 session id
   */
  public String getSessionId(Context context) {
    return Session.getInstance(context).getSessionId();
  }

  /**
   * 是否首次启动
   */
  public Object isFirstTime(Context context) {
    return Utils.isFirstStart(context);
  }

  /**
   * 获取debug状态 服务器设置 > 用户设置 > 默认设置
   */
  public static Object getDebugMode(Context context) {
    int debug = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(context, Constants.SP_SERVICE_DEBUG, -1)));
    if (debug != -1) {
      return debug;
    }
    debug = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(context, Constants.SP_USER_DEBUG, -1)));
    if (debug != -1) {
      return debug;
    }
    return 0;
  }

  /**
   * 获取 SDK 版本号
   */
  public String getLibVersion(Context context) {
    return Constants.DEV_SDK_VERSION_NAME;
  }

  /**
   * 是否登录
   */
  public boolean getLogin(Context context) {
    int isLogin = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(context, Constants.SP_IS_LOGIN, 0)));
    return isLogin == 1;
  }

  /**
   * 获取 IMEI
   */
  public static String getIMEI(Context context) {
    try {
      if (Utils.checkPermission(context, Manifest.permission.READ_PHONE_STATE)) {
        TelephonyManager telephonyMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyMgr.getDeviceId();
      }
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 获取mac地址
   */
  public Object getMac(Context context) {
    try {
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        return getMacBySystemInterface(context);
      } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
        return getMacByFileAndJavaAPI(context);
      } else {
        return getMacByJavaAPI();
      }
    } catch (Throwable e) {
    }
    return null;
  }

  private static String getMacBySystemInterface(Context context) throws Throwable {
    if (context != null) {
      WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
      if (Utils.checkPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
      }
    }
    return null;
  }

  private static String getMacByFileAndJavaAPI(Context context) throws Throwable {
    String mac = getMacShell();
    return !Utils.isEmpty(mac) ? mac : getMacByJavaAPI();
  }

  private static String getMacByJavaAPI() throws Throwable {
    Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
    while (interfaces.hasMoreElements()) {
      NetworkInterface netInterface = interfaces.nextElement();
      if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
        byte[] addr = netInterface.getHardwareAddress();
        if (addr == null || addr.length == 0) {
          continue;
        }
        StringBuilder buf = new StringBuilder();
        for (byte b : addr) {
          buf.append(String.format("%02X:", b));
        }
        if (buf.length() > 0) {
          buf.deleteCharAt(buf.length() - 1);
        }
        return buf.toString().toLowerCase(Locale.getDefault());
      }
    }
    return "";
  }

  private static String getMacShell() throws Throwable {

    String[] urls = new String[] {
        "/sys/class/net/wlan0/address",
        "/sys/class/net/eth0/address",
        "/sys/devices/virtual/net/wlan0/address"
    };
    String mc;
    for (int i = 0; i < urls.length; i++) {
      mc = reaMac(urls[i]);
      if (mc != null) {
        return mc;
      }
    }
    return null;
  }

  private static String reaMac(String url) throws Throwable {
    String macInfo = null;
    FileReader fstream = new FileReader(url);
    BufferedReader in = null;
    if (fstream != null) {
      try {
        in = new BufferedReader(fstream, 1024);
        macInfo = in.readLine();
      } finally {
        if (fstream != null) {
          try {
            fstream.close();
          } catch (Throwable e) {

          }
        }
        if (in != null) {
          try {
            in.close();
          } catch (Throwable e) {

          }
        }
      }
    }
    return macInfo;
  }
}
