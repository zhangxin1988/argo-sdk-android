package com.analysys.process;

import android.content.Context;
import android.text.TextUtils;
import com.analysys.utils.AnsLog;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/22 17:46
 * @Author: Wang-X-C
 */
public class PerfectData {

  Context mContext;

  private static class Holder {
    public static PerfectData INSTANCE = new PerfectData();
  }

  public static PerfectData getInstance(Context context) {
    if (Holder.INSTANCE.mContext == null) {
      if (context != null) {
        Holder.INSTANCE.mContext = context;
      }
    }
    return Holder.INSTANCE;
  }

  /**
   * 获取完整上传Json数据
   * 参数1.eventName
   * 参数2.用户传参
   * 参数3.默认采集
   * 参数4.eventName track事件使用
   */
  public JSONObject getEventData(Object... objects) throws Throwable {
    JSONObject allJob = null;
    if (objects != null && mContext != null) {
      String eventName = String.valueOf(objects[0]);
      JSONObject eventMould = getEventMould(eventName);
      //  如果为track校验事件名称
      if (Constants.EN_TRACK.equals(eventName)) {
        String eventInfo = String.valueOf(objects[3]);
        if (!checkTrack(eventName, eventInfo)) {
          return null;
        }
        eventName = eventInfo;
      }
      Map<String, Object> data = toMap(objects[1]);
      if (!CheckUtils.checkParameter(data)) {
        return null;
      }
      data = mergeMap(data, objects[2]);
      setSuperProperty(eventName, data);
      allJob = fillData(eventName, eventMould, data);
    }
    return allJob;
  }

  public Map<String, Object> mergeMap(Map<String, Object> userParameter, Object autoCollect) {
    Map<String, Object> allMap = new HashMap<String, Object>();
    if (userParameter != null) {
      allMap.putAll(userParameter);
    }
    if (autoCollect != null) {
      allMap.putAll((Map<String, Object>) autoCollect);
    }
    return allMap;
  }

  /**
   * 校验track xWhat 值
   */
  private boolean checkTrack(String eventName, String eventInfo) {
    JSONObject trackMould = Mould.ruleMould.optJSONObject(eventName);
    return CheckUtils.checkFiled(trackMould, eventInfo);
  }

  /**
   * 获取事件字段模板
   */
  private JSONObject getEventMould(String eventName) {
    //  如果为profile系列则读取基础profile模板
    if (eventName.startsWith(Constants.PROFILE)) {
      return Mould.fieldsMould.optJSONObject(Constants.PROFILE);
    }
    return Mould.fieldsMould.optJSONObject(eventName);
  }

  /**
   * 转map
   */
  public Map<String, Object> toMap(Object data) {
    if (data != null) {
      return (Map<String, Object>) data;
    }
    return null;
  }

  /**
   * 添加通用属性
   *
   * @throws Throwable
   */
  private void setSuperProperty(String eventName, Map<String, Object> contextMap) throws Throwable {
    if (!Constants.EN_ALIAS.equals(eventName)
        && !eventName.startsWith(Constants.PROFILE)) {
      String property = String.valueOf(AnsSpUtils.getParam(mContext,
          Constants.SP_SUPER_PROPERTY, ""));
      if (!TextUtils.isEmpty(property)) {
        JSONObject superProperty = new JSONObject(property);
        Iterator<String> keys = superProperty.keys();
        while (keys.hasNext()) {
          String key = keys.next();
          contextMap.put(key, superProperty.opt(key));
        }
      }
    }
  }

  /**
   * 通过遍历字段模板填充数据
   *
   * @throws Throwable
   */
  public JSONObject fillData(String eventName, JSONObject eventMould, Map<String, Object> contextMap) throws Throwable {
    JSONObject allJob = new JSONObject();
    //  1.获取 遍历 外层outer key列表
    JSONArray outerKeys = eventMould.optJSONArray(Constants.OUTER);
    for (int i = 0; i < outerKeys.length(); i++) {
      String outKey = outerKeys.optString(i);
      JSONObject fieldRuleMould = Mould.ruleMould.optJSONObject(outKey);
      JSONArray insideKeys = eventMould.optJSONArray(outKey);
      //  2.判断是否存在内层嵌套key，如果存在遍历内层key列表
      if (insideKeys != null) {
        int length = insideKeys.length();
        checkSize(contextMap, length);
        for (int j = 0; j < length; j++) {
          String contextKey = insideKeys.getString(j);
          JSONObject contextRuleMould = fieldRuleMould.optJSONObject(contextKey);
          //  3.获取value并校验
          Object insideValue = getValue(contextRuleMould, null);
          if (!CheckUtils.checkFiled(fieldRuleMould, insideValue)) {
            return null;
          }
          if (!contextMap.containsKey(contextKey)) {
            contextMap.put(contextKey, insideValue);
          }
        }
        allJob.put(outKey, new JSONObject(contextMap));
      } else {
        //  3.获取value并校验
        Object outerValue = getValue(fieldRuleMould, eventName);
        if (!CheckUtils.checkFiled(fieldRuleMould, outerValue)) {
          return null;
        }
        if (!contextMap.containsKey(outKey)) {
          allJob.put(outKey, outerValue);
        }
      }
    }
    return allJob;
  }

  private void checkSize(Map<String, Object> contextMap, int size) {
    int diff = (contextMap.size() + size) - Mould.allKeysLimit;
    if (0 < diff) {
      List<String> keys = new ArrayList<>(contextMap.keySet());
      for (int i = 0; i < diff; i++) {
        contextMap.remove(keys.get(i));
      }
    }
  }

  /**
   * 通过ruleMould模板规则获取value值
   * 0：方法获取
   * 1. 默认值
   * 2. 值传入
   */
  public Object getValue(JSONObject ruleMould, String what) {
    Object object = null;
    if (ruleMould != null) {
      int type = ruleMould.optInt(Constants.VALUE_TYPE);
      String value = ruleMould.optString(Constants.VALUE);
      if (type == 0) {
        object = reflexGetData(value);
      } else if (type == 1) {
        object = value;
      } else if (type == 2) {
        object = what;
      }
    }
    return object;
  }

  /**
   * 通过反射获取需要填充的数据
   */
  public Object reflexGetData(String method) {
    Object object = Utils.reflexUtils(Constants.METHOD_PATH,
        method, new Class[] { Context.class }, mContext);
    return object;
  }
}
