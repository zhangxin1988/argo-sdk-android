package com.analysys.hybrid;

import android.content.Context;
import android.text.TextUtils;
import android.webkit.WebView;
import com.analysys.AnalysysAgent;
import com.analysys.process.DataProcess;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;

public class HybridBridge {

  private Context mContext = null;
  private static final String SCHEME = "analysysagent";

  private static class Holder {
    private static HybridBridge instance = new HybridBridge();
  }

  private HybridBridge() {
  }

  public static HybridBridge getInstance(Context context) {
    Holder.instance.initContext(context);
    return Holder.instance;
  }

  private void initContext(Context context) {
    if (mContext == null) {
      if (context != null) {
        mContext = context.getApplicationContext();
      }
    }
  }

  /**
   * process msg from JS
   *
   * @throws Exception
   */
  public void execute(final String url, final Object webView) throws Exception {
    try {
      if (url.startsWith(SCHEME)) {
        String info = url.substring((SCHEME.length() + 1), url.length());
        JSONObject obj = new JSONObject(info);
        if (obj != null && obj.length() > 0) {
          String functionName = obj.optString("functionName");
          JSONArray args = obj.optJSONArray("functionParams");
          String callback = obj.optString("callbackFunName");
          //call the method
          Class<HybridBridge> classType = HybridBridge.class;
          if (TextUtils.isEmpty(callback)) {
            Method method = classType.getDeclaredMethod(functionName, JSONArray.class);
            method.invoke(Holder.instance, args);
          } else {
            Method method = classType.getDeclaredMethod(functionName, JSONArray.class, String.class, WebView.class);
            method.invoke(Holder.instance, args, callback, webView);
          }
        }
      }
    } catch (Throwable e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unused")
  private void registerSuperProperty(JSONArray array) {

    if (array != null && array.length() > 0) {
      String key = array.optString(0);
      Object value = array.opt(1);
      AnalysysAgent.registerSuperProperty(mContext, key, value);
    }
  }

  @SuppressWarnings("unused")
  private void registerSuperProperties(JSONArray array) {
    if (array != null && array.length() > 0) {

      JSONObject obj = array.optJSONObject(0);
      if (obj != null && obj.length() > 0) {
        Map<String, Object> map = convertToMap(obj);
        AnalysysAgent.registerSuperProperties(mContext, map);
      }
    }
  }

  @SuppressWarnings("unused")
  private void unRegisterSuperProperty(JSONArray array) {
    if (array != null && array.length() > 0) {
      String key = array.optString(0);
      if (!TextUtils.isEmpty(key)) {
        AnalysysAgent.unRegisterSuperProperty(mContext, key);
      }
    }
  }

  @SuppressWarnings("unused")
  private void profileAppend(JSONArray array) {

    if (array != null && array.length() > 0) {

      if (array.length() == 2) {
        String key = array.optString(0);
        Object value = array.opt(1);
        AnalysysAgent.profileAppend(mContext, key, value);
      } else {
        JSONObject obj = array.optJSONObject(0);
        if (obj != null && obj.length() > 0) {
          Map<String, Object> map = convertToMap(obj);
          AnalysysAgent.profileAppend(mContext, map);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void profileSet(JSONArray array) {
    if (array != null && array.length() > 0) {
      if (array.length() == 2) {
        String key = array.optString(0);
        Object value = array.opt(1);
        AnalysysAgent.profileSet(mContext, key, value);
      } else {
        JSONObject obj = array.optJSONObject(0);
        if (obj != null && obj.length() > 0) {
          Map<String, Object> map = convertToMap(obj);
          AnalysysAgent.profileSet(mContext, map);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void profileIncrement(JSONArray array) {
    if (array != null && array.length() > 0) {
      if (array.length() == 2) {
        String key = array.optString(0);
        Number value = (Number) array.opt(1);
        AnalysysAgent.profileIncrement(mContext, key, value);
      } else {
        JSONObject obj = array.optJSONObject(0);
        if (obj != null && obj.length() > 0) {
          Map<String, Number> map = convertToNumberMap(obj);
          AnalysysAgent.profileIncrement(mContext, map);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void profileSetOnce(JSONArray array) {
    if (array != null && array.length() > 0) {
      if (array.length() == 2) {
        String key = array.optString(0);
        Object value = array.opt(1);
        AnalysysAgent.profileSetOnce(mContext, key, value);
      } else {
        JSONObject obj = array.optJSONObject(0);
        if (obj != null && obj.length() > 0) {
          Map<String, Object> map = convertToMap(obj);
          AnalysysAgent.profileSetOnce(mContext, map);
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void clearSuperProperties(JSONArray array) {
    AnalysysAgent.clearSuperProperties(mContext);
  }

  @SuppressWarnings("unused")
  private void reset(JSONArray array) {
    AnalysysAgent.reset(mContext);
  }

  @SuppressWarnings("unused")
  private void profileDelete(JSONArray array) {
    AnalysysAgent.profileDelete(mContext);
  }

  @SuppressWarnings("unused")
  private void profileUnset(JSONArray array) {
    if (array != null && array.length() > 0) {
      String key = array.optString(0);
      if (!TextUtils.isEmpty(key)) {
        AnalysysAgent.profileUnset(mContext, key);
      }
    }
  }

  @SuppressWarnings("unused")
  private void getSuperProperty(JSONArray array, String callBack, WebView webView) {
    if (array != null && array.length() > 0) {
      String key = array.optString(0);
      if (!TextUtils.isEmpty(key)) {
        Object value = AnalysysAgent.getSuperProperty(mContext, key);
        if (value != null) {
          webView.loadUrl("javascript:" + callBack + "('" + value.toString() + "')");
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void getSuperProperties(JSONArray array, String callBack, WebView webView) {
    Map<String, Object> res = AnalysysAgent.getSuperProperties(mContext);
    if (res != null && res.size() > 0) {
      webView.loadUrl("javascript:" + callBack + "('" + res.toString() + "')");
    }
  }

  @SuppressWarnings("unused")
  private void pageView(JSONArray array) {
    if (array.length() > 1) {
      String pageName = array.optString(0);
      JSONObject obj = array.optJSONObject(1);
      Map<String, Object> map = convertToMap(obj);
      DataProcess.getInstance(mContext).hybridPageView(pageName, map);
    } else {
      String pageName = array.optString(0);
      if (!TextUtils.isEmpty(pageName)) {
        AnalysysAgent.pageView(mContext, pageName);
      }
    }
  }

  @SuppressWarnings("unused")
  private void track(JSONArray array) {
    if (array.length() > 1) {
      String eventName = array.optString(0);
      JSONObject eventInfo = array.optJSONObject(1);
      Map<String, Object> map = convertToMap(eventInfo);
      AnalysysAgent.track(mContext, eventName, map);
    } else {
      String eventName = array.optString(0);
      if (!TextUtils.isEmpty(eventName)) {
        AnalysysAgent.track(mContext, eventName);
      }
    }
  }

  @SuppressWarnings("unused")
  private void identify(JSONArray array) {
    String distinctId = array.optString(0);
    if (!TextUtils.isEmpty(distinctId)) {
      AnalysysAgent.identify(mContext, distinctId);
    }
  }

  @SuppressWarnings("unused")
  private void alias(JSONArray array) {
    String aliasId = array.optString(0);
    String originalId = array.optString(1);
    AnalysysAgent.alias(mContext, aliasId, originalId);
  }

  /**
   * convert JSONObject to Map
   */
  private Map<String, Object> convertToMap(JSONObject obj) {
    Map<String, Object> res = new HashMap<String, Object>();
    if (obj != null && obj.length() > 0) {
      Iterator<String> it = obj.keys();
      while (it.hasNext()) {
        final String key = it.next();
        final Object o = obj.opt(key);
        res.put(key, o);
      }
    }
    return res;
  }

  private Map<String, Number> convertToNumberMap(JSONObject obj) {
    Map<String, Number> res = new HashMap<String, Number>();
    if (obj != null && obj.length() > 0) {
      Iterator<String> it = obj.keys();
      while (it.hasNext()) {
        final String key = it.next();
        final Number o = (Number) obj.opt(key);
        res.put(key, o);
      }
    }
    return res;
  }
}
