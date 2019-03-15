package com.analysys;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import com.analysys.process.DataProcess;
import com.analysys.deeplink.DeepLink;
import com.analysys.process.Session;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 自动采集页面信息
 * @Version: 1.0
 * @Create: 2018/3/4
 * @Author: Wang-X-C
 */
public class AutomaticAcquisition implements Application.ActivityLifecycleCallbacks {
  /** 应用启动时间 */
  private long appStartTime = 0;
  private int startedCount = 0;
  private boolean fromBackground = false;

  @Override
  public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
  }

  @Override
  public void onActivityStarted(Activity activity) {
    try {
      activityStart(activity);
    } catch (Throwable e) {
    }
  }

  private void activityStart(Activity activity) throws Throwable {
    Map<String, Object> utmMap = DeepLink.getUtmValue(activity);
    // 由页面时差和内存参数判断是否重置session
    Session.getInstance(activity).resetSession(Utils.isEmpty(utmMap) ? true : false);
    // 标记页面名称，用于track事件中$pagename
    //Constants.pageName = activity.getClass().getName();
    if (startedCount == 0) {
      firstStart(activity);
    }
    startedCount = startedCount + 1;
    pageInfo(activity, utmMap);
  }

  /**
   * 发送应用启动消息
   */
  private void firstStart(Activity activity) {
    appStartTime = System.currentTimeMillis();
    DataProcess.getInstance(activity).appStart(fromBackground);
    // 用于判断是否是后台启动
    if (!fromBackground) {
      fromBackground = true;
    }
  }

  /**
   * 应用自动采集页面信息
   */
  private void pageInfo(Activity activity, Map<String, Object> utmMap) {
    boolean isCollection =
        Boolean.valueOf(String.valueOf(AnsSpUtils.getParam(activity, Constants.SP_IS_COLLECTION, true)));
    if (isCollection) {
      if (!isAutomaticCollection(activity)) {
        DataProcess.getInstance(activity).autoCollectPageView(activity);
      }
    }
  }

  @Override
  public void onActivityResumed(Activity activity) {

  }

  /**
   * 页面是否忽略自动采集，false不自动采集，true自动采集
   */
  private boolean isAutomaticCollection(Activity activity) {
    String activities = String.valueOf(AnsSpUtils.getParam(activity, Constants.SP_IGNORED_COLLECTION, ""));
    if (Utils.isEmpty(activities)) {
      return false;
    }
    List<String> pageNames = Utils.toList(activities);
    for (String pageName : pageNames) {
      String nowPageName = activity.getClass().getSimpleName();
      if (nowPageName.equals(pageName)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void onActivityPaused(Activity activity) {
    Session.getInstance(activity).setPageEnd();
  }

  @Override
  public void onActivityStopped(Activity activity) {

    startedCount = startedCount - 1;
    if (startedCount < 0) {
      startedCount = 0;
    }
    if (startedCount == 0) {
      long nowTime = System.currentTimeMillis();
      if (appStartTime > 0) {
        DataProcess.getInstance(activity).appEnd(nowTime - appStartTime);
      }
    }
  }

  @Override
  public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
  }

  @Override
  public void onActivityDestroyed(Activity activity) {
  }
}
