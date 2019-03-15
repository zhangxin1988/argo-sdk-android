package com.analysys.process;

import android.content.Context;
import com.analysys.utils.Constants;
import java.io.InputStream;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/2/21 15:49
 * @Author: Wang-X-C
 */
public class Mould {

  static final String MM_BASE = "base";
  static final String MM_OUTER = "outer";
  static final String MM_X_CONTEXT = "xcontext";
  static final String MM_RESERVED_KEYWORDS = "ReservedKeywords";

  static final String FUNC_LIST = "checkFuncList";
  static final String CONTEXT_KEY = "contextKey";
  static final String CONTEXT_VALUE = "contextValue";
  static final String ADDITIONAL = "additional";

  static final String USER_KEYS_LIMIT = "userKeysLimit";
  static final String ALL_KEYS_LIMIT = "allKeysLimit";

  static final String ANS_FIELDS_MOULD = "AnsFieldsMould.json";
  static final String CUSTOMER_FIELDS_MOULD = "CustomerFieldsMould.json";
  static final String ANS_RULE_MOULD = "AnsRuleMould.json";
  static final String CUSTOMER_RULE_MOULD = "CustomerRuleMould.json";

  public static JSONObject fieldsMould = null;
  public static JSONObject ruleMould = null;
  public static JSONArray contextKey = null;
  public static JSONArray contextValue = null;
  public static JSONArray reservedKeywords = null;
  public static JSONObject additional = null;
  public static int userKeysLimit = 0;
  public static int allKeysLimit = 0;

  /**
   * 初始化模板
   *
   * @throws Throwable
   */
  public static void initMould(Context context) throws Throwable {
    if (ruleMould == null) {
      fieldsMould = mergeFieldsMould(context);
      ruleMould = mergeRuleMould(context);
      if (ruleMould != null) {
        JSONObject xContextRueMould = ruleMould.optJSONObject(Constants.X_CONTEXT);
        contextKey = xContextRueMould.optJSONObject(CONTEXT_KEY).getJSONArray(FUNC_LIST);
        contextValue = xContextRueMould.optJSONObject(CONTEXT_VALUE).getJSONArray(FUNC_LIST);
        reservedKeywords = ruleMould.optJSONArray(MM_RESERVED_KEYWORDS);
        additional = xContextRueMould.optJSONObject(ADDITIONAL);
        if (additional != null) {
          userKeysLimit = additional.optInt(USER_KEYS_LIMIT);
          allKeysLimit = additional.optInt(ALL_KEYS_LIMIT);
        }
      }
    }
  }

  /**
   * 合并字段模板
   *
   * @throws Throwable
   */
  private static JSONObject mergeFieldsMould(Context context) throws Throwable {
    JSONObject defaultMould = new JSONObject(getMould(context, ANS_FIELDS_MOULD));
    JSONObject customerMould = new JSONObject(getMould(context, CUSTOMER_FIELDS_MOULD));
    mergeCustomerMould(defaultMould, customerMould);
    mergeSharedMould(defaultMould);
    return defaultMould;
  }

  /**
   * 合并基础模板到事件模板
   *
   * @throws Throwable
   */
  private static void mergeSharedMould(JSONObject defaultMould) throws Throwable {
    Iterator<String> keys = defaultMould.keys();
    JSONObject baseMould = defaultMould.optJSONObject(MM_BASE);
    while (keys.hasNext()) {
      String key = keys.next();
      if (!MM_BASE.equals(key)) {
        mergeFields(defaultMould.optJSONObject(key), baseMould);
      }
    }
    defaultMould.remove(MM_BASE);
  }

  /**
   * 合并基础事件模板和定制事件模板
   *
   * @throws Throwable
   */
  private static void mergeCustomerMould(JSONObject defaultEvent, JSONObject customerEvent) throws Throwable {
    Iterator<String> keys = customerEvent.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject defaultMould = defaultEvent.optJSONObject(key);
      if (defaultMould != null) {
        mergeFields(defaultMould, customerEvent.optJSONObject(key));
      } else {
        defaultEvent.put(key, customerEvent.optJSONObject(key));
      }
    }
  }

  /**
   * 合并基础公共模板和定制公共模板
   *
   * @throws Throwable
   */
  private static void mergeFields(JSONObject defaultMould, JSONObject customerMould) throws Throwable {

    JSONArray defaultOuter = defaultMould.optJSONArray(MM_OUTER);
    JSONArray customerOuter = customerMould.optJSONArray(MM_OUTER);
    if (defaultOuter == null) {
      defaultMould.put(MM_OUTER, customerOuter);
    } else {
      mergeJsonArray(defaultOuter, customerOuter);
    }
    JSONArray defaultXContext = defaultMould.optJSONArray(MM_X_CONTEXT);
    JSONArray customerXContext = customerMould.optJSONArray(MM_X_CONTEXT);
    if (defaultXContext == null) {
      defaultMould.put(MM_X_CONTEXT, customerXContext);
    } else {
      mergeJsonArray(defaultXContext, customerXContext);
    }
  }

  /**
   * 合并规则模板
   *
   * @throws Throwable
   */
  private static JSONObject mergeRuleMould(Context context) throws Throwable {
    JSONObject defaultRule = new JSONObject(getMould(context, ANS_RULE_MOULD));
    JSONObject customerRule = new JSONObject(getMould(context, CUSTOMER_RULE_MOULD));
    //   合并不允许覆盖字段
    mergeJsonArray(defaultRule.optJSONArray(MM_RESERVED_KEYWORDS),
        customerRule.optJSONArray(MM_RESERVED_KEYWORDS));
    customerRule.remove(MM_RESERVED_KEYWORDS);

    mergeEventRule(defaultRule.optJSONObject(MM_X_CONTEXT), customerRule.optJSONObject(MM_X_CONTEXT));
    customerRule.remove(MM_X_CONTEXT);

    mergeEventRule(defaultRule, customerRule);

    return defaultRule;
  }

  /**
   * 合并默认、定制两个模板
   *
   * @throws Throwable
   */
  private static void mergeEventRule(JSONObject defaultRule, JSONObject customerRule) throws Throwable {
    if (customerRule != null) {
      Iterator<String> keys = customerRule.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        defaultRule.put(key, customerRule.optJSONObject(key));
      }
    }
  }

  /**
   * 读取模板
   *
   * @throws Throwable
   */
  private static String getMould(Context context, String fileName) throws Throwable {
    InputStream inputStream = context.getResources().getAssets().open(fileName);
    int size = inputStream.available();
    byte[] bytes = new byte[size];
    inputStream.read(bytes);
    inputStream.close();
    return new String(bytes);
  }

  /**
   * 合并两个jsonArray
   */
  private static void mergeJsonArray(JSONArray base, JSONArray other) {
    if (base != null && other != null) {
      for (int i = 0; i < other.length(); i++) {
        base.put(other.optString(i));
      }
    }
  }
}
