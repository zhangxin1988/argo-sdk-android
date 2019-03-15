package com.analysys.utils;

import android.text.TextUtils;
import com.analysys.process.Mould;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/22 16:43
 * @Author: Wang-X-C
 */
public class CheckUtils {

  /**
   * 过滤掉value为空的数据
   */
  public static void pushToMap(Map<String, Object> map, String key, String value) {
    try {
      if (!Utils.isEmpty(key) && !Utils.isEmpty(value)) {
        map.put(key, value);
      }
    } catch (Throwable e) {
      AnsLog.e(e);
    }
  }

  /**
   * 过滤掉value为-1的数据
   */
  public static void pushToJSON(JSONObject json, String key, int value) {
    try {
      if (value != -1) {
        if (!json.has(key)) {
          json.put(key, value);
        }
      }
    } catch (Throwable e) {
      AnsLog.e(e);
    }
  }

  public static boolean checkEventName(Object eventInfo) {
    String eventName = String.valueOf(eventInfo);
    if (!checkKeyName(eventName)) {
      AnsLog.w(LogPrompt.getNamingErr(eventName));
      return false;
    }
    if (Constants.MAX_EVENT_NAME_LENGTH < eventName.length()) {
      AnsLog.w(LogPrompt.getWhatLengthErr(eventName));
      return false;
    }
    return true;
  }

  /**
   * 校验array大小
   */
  public static boolean checkArraySize(int size) {
    if (Constants.MAX_ARRAY_SIZE < size) {
      AnsLog.w(LogPrompt.ARRAY_SIZE_ERROR);
      return false;
    }
    return true;
  }

  /**
   * 校验map大小
   */
  public static boolean checkMapSize(int size) {
    if (Mould.userKeysLimit != 0 && Mould.userKeysLimit < size) {
      AnsLog.w(LogPrompt.MAP_SIZE_ERROR);
      return false;
    }
    return true;
  }

  /**
   * 接口传参校验
   */
  public static boolean checkParameter(Map<String, Object> parameters) {
    if (parameters != null) {
      if (!checkMapSize(parameters.size())) {
        return false;
      }
      Set<String> keys = parameters.keySet();
      for (String key : keys) {
        if (!reflexCheck(key, Mould.contextKey)) {
          return false;
        }
        Object value = parameters.get(key);
        if (!Utils.isEmpty(value)) {
          if (!reflexCheck(value, Mould.contextValue)) {
            return false;
          }
        } else {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 通过反射funcList内方法校验key
   */
  private static boolean reflexCheck(Object data, JSONArray methodArray) {
    for (int i = 0; i < methodArray.length(); i++) {
      Object object =
          Utils.reflexUtils(Constants.CHECK_PATH, methodArray.optString(i), new Class[] { Object.class }, data);
      if (object != null) {
        return (Boolean) object;
      }
    }
    return true;
  }

  /**
   * 校验xContext内key字符串长度
   */
  public static boolean checkKey(Object objKey) {
    if (objKey != null) {
      String key = String.valueOf(objKey);
      if (!TextUtils.isEmpty(key) && Constants.MAX_KEY_LENGTH < key.length()) {
        AnsLog.w(LogPrompt.getKeyLengthErr(key));
        return false;
      }
      if (!checkKeyName(key)) {
        AnsLog.w(LogPrompt.getNamingErr(key));
        return false;
      }
      if (!isReservedKeywords(key)) {
        AnsLog.w(LogPrompt.getReservedKeywordsErr(key));
        return false;
      }
    }
    return true;
  }

  /**
   * 判断是否可以覆盖
   */
  private static boolean isReservedKeywords(String key) {
    if (Mould.reservedKeywords != null) {
      JSONArray reservedKeywords = Mould.reservedKeywords;
      for (int i = 0; i < reservedKeywords.length(); i++) {
        if (key.equals(reservedKeywords.optString(i))) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * 校验key命名是否规范
   */
  private static boolean checkKeyName(String key) {
    Pattern p = Pattern.compile(Constants.KEY_RULE);
    Matcher m = p.matcher(key);
    return m.matches();
  }

  /**
   * 校验value数据类型
   */
  public static boolean checkValue(Object value) {
    if (value instanceof Number || value instanceof Boolean) {
    } else if (value instanceof String) {
      return checkValueLength(String.valueOf(value));
    } else if (value.getClass().isArray()) {
      if (value instanceof String[] ? false : true) {
        AnsLog.w(LogPrompt.TYPE_ERROR);
        return false;
      }
      if (!checkArraySize(((String[]) value).length)) {
        return false;
      }
      for (String v : (String[]) value) {
        if (!checkValueLength(String.valueOf(v))) {
          return false;
        }
      }
    } else if (value instanceof List<?>) {
      for (String va : (List<String>) value) {
        if (!checkValueLength(String.valueOf(va))) {
          return false;
        }
      }
    } else {
      AnsLog.w(LogPrompt.TYPE_ERROR);
      return false;
    }
    return true;
  }

  /**
   * 校验xContext内value字符串长度
   */
  public static boolean checkValueLength(String info) {
    if (!TextUtils.isEmpty(info) && Constants.MAX_VALUE_LENGTH < info.length()) {
      AnsLog.w(LogPrompt.getValueLengthErr(info));
      return false;
    }
    return true;
  }

  /**
   * 通过遍历funcList内方法
   * 通过反射校验数据是否合法
   */
  public static boolean checkFiled(JSONObject ruleMould, Object data) {
    //AnsLog.e("校验111：：：" + ruleMould);
    if (ruleMould != null) {
      JSONArray funcList = ruleMould.optJSONArray(Constants.FUNC_LIST);
      //AnsLog.e(data + ":<< 校验 >>:" + funcList);
      if (funcList != null) {
        for (int i = 0; i < funcList.length(); i++) {
          String method = funcList.optString(i);
          //AnsLog.e("校验2：：：" + method);
          Object err = Utils.reflexUtils(Constants.CHECK_PATH, method, new Class[] { Object.class }, data);
          //AnsLog.e("返回值11：" + err);
          if (err == null || !(Boolean) err) {
            return false;
          }
        }
      }
    }
    return true;
  }
}
