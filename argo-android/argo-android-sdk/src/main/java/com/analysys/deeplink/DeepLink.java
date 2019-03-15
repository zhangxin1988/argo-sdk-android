package com.analysys.deeplink;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;
import java.util.HashMap;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/12/5 16:09
 * @Author: Wang-X-C
 */
public class DeepLink {
  private static String uriAddress = null;
  private static final String UTM_SOURCE = "utm_source";
  private static final String UTM_MEDIUM = "utm_medium";
  private static final String UTM_CAMPAIGN = "utm_campaign";
  private static final String UTM_CAMPAIGN_ID = "campaign_id";
  private static final String UTM_CONTENT = "utm_content";
  private static final String UTM_TERM = "utm_term";
  private static final String HM_SR = "hmsr";
  private static final String HM_PL = "hmpl";
  private static final String HM_CU = "hmcu";
  private static final String HM_KW = "hmkw";
  private static final String HM_CI = "hmci";

  public static Map<String, Object> getUtmValue(Activity activity) throws Throwable {
    Map<String, Object> utmMap = new HashMap<String, Object>();
    Intent intent = activity.getIntent();
    if (!Utils.isEmpty(intent)) {
      Uri uri = intent.getData();
      if (!Utils.isEmpty(uri)) {
        // 判断两次是否为同一次deepLink
        if (isUriEquals(uri)) {
          return utmMap;
        }
        if (!Utils.isEmpty(uri.getQueryParameter(UTM_SOURCE))
            && !Utils.isEmpty(uri.getQueryParameter(UTM_MEDIUM))
            && !Utils.isEmpty(uri.getQueryParameter(UTM_CAMPAIGN))) {
          getUtmInfo(utmMap, uri);
        } else if (!Utils.isEmpty(uri.getQueryParameter(HM_SR))
            && !Utils.isEmpty(uri.getQueryParameter(HM_PL))
            && !Utils.isEmpty(uri.getQueryParameter(HM_CU))) {
          getHmInfo(utmMap, uri);
        }
      }
    }
    return utmMap;
  }

  private static void getUtmInfo(Map<String, Object> map, Uri uri) {
    CheckUtils.pushToMap(map, Constants.UTM_SOURCE, uri.getQueryParameter(UTM_SOURCE));
    CheckUtils.pushToMap(map, Constants.UTM_MEDIUM, uri.getQueryParameter(UTM_MEDIUM));
    CheckUtils.pushToMap(map, Constants.UTM_CAMPAIGN, uri.getQueryParameter(UTM_CAMPAIGN));
    CheckUtils.pushToMap(map, Constants.UTM_CAMPAIGN_ID, uri.getQueryParameter(UTM_CAMPAIGN_ID));
    CheckUtils.pushToMap(map, Constants.UTM_CONTENT, uri.getQueryParameter(UTM_CONTENT));
    CheckUtils.pushToMap(map, Constants.UTM_TERM, uri.getQueryParameter(UTM_TERM));
  }

  private static void getHmInfo(Map<String, Object> map, Uri uri) {
    CheckUtils.pushToMap(map, Constants.UTM_SOURCE, uri.getQueryParameter(HM_SR));
    CheckUtils.pushToMap(map, Constants.UTM_MEDIUM, uri.getQueryParameter(HM_PL));
    CheckUtils.pushToMap(map, Constants.UTM_CAMPAIGN, uri.getQueryParameter(HM_CU));
    CheckUtils.pushToMap(map, Constants.UTM_CAMPAIGN, uri.getQueryParameter(HM_KW));
    CheckUtils.pushToMap(map, Constants.UTM_CONTENT, uri.getQueryParameter(HM_CI));
  }

  private static boolean isUriEquals(Uri uri) {

    if (!Utils.isEmpty(uriAddress)) {
      if (uriAddress.equals(uri.toString())) {
        return true;
      }
    }
    uriAddress = uri.toString();
    return false;
  }
}
