package com.analysys.utils;

import java.util.Arrays;
import org.json.JSONArray;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/11/14 14:43
 * @Author: Wang-X-C
 */
public class LogPrompt {

  public static final String KEY_SUCCESS = "Set key success. current key: ";
  public static final String KEY_FAILED = "Set key failed. current key: ";

  public static final String CHANNEL_SUCCESS = "Set channel success. current channel: ";
  public static final String CHANNEL_FAILED = "Set channel failed. current channel: ";

  public static final String NOT_EMPTY = " can not be empty!";

  public static final String SUCCESS = ": set success!";
  public static final String FAILED = ": set failed!";

  public static final String ID_EMPTY = "id can not be empty!";
  public static final String VALUE_EMPTY = "value can not be empty!";

  public static final String KEY_ERR = "Please make sure that the appkey in manifest matched with init API!";

  static final String ERR_HEAD = "The length of the property value string [";

  static final String WHAT_LENGTH_ERR = "] needs to be 1-100!";
  static final String KEY_LENGTH_ERR = "] needs to be 1-125!";
  static final String VALUE_LENGTH_ERR = "] needs to be 1-255!";

  public static final String ARRAY_SIZE_ERROR = " The length of the property value array needs to be 1-100!";
  public static final String MAP_SIZE_ERROR = " The length of the property key-value pair needs to be 1-100!";

  public static final String TYPE_ERROR =
      "Property value invalid, support type: String/Number/boolean/String collection/String array!";

  final static String NAMING_ERROR = "] does not conform to naming rules!";

  final static String BRACKETS = "[ ";

  final static String RESERVED_KEYWORDS_ERR = " ] is a reserved field!";

  public static final String INIT_SUCCESS = "Init Android Analysys Java sdk success, version: ";
  public static final String INIT_FAILED = "Please init Analysys Android SDK .";
  
  public static void showLog(String apiName, boolean success) {

    if (success) {
      AnsLog.i(apiName + SUCCESS);
    } else {
      AnsLog.w(apiName + FAILED);
    }
  }

  public static void showInitLog(boolean success) {
    if (success) {
      AnsLog.i(INIT_SUCCESS + Constants.DEV_SDK_VERSION_NAME);
    } else {
      AnsLog.w(INIT_FAILED);
    }
  }

  public static void showChannelLog(boolean success, String channel) {
    if (success) {
      AnsLog.i(LogPrompt.CHANNEL_SUCCESS + channel);
    } else {
      AnsLog.w(LogPrompt.CHANNEL_FAILED + channel);
    }
  }

  public static void showKeyLog(boolean success, String key) {
    if (success) {
      AnsLog.i(KEY_SUCCESS + key);
    } else {
      AnsLog.w(KEY_FAILED + key);
    }
  }

  /**
   * 事件名称长度异常
   */
  public static String getWhatLengthErr(String value) {
    return ERR_HEAD + getSubString(value) + WHAT_LENGTH_ERR;
  }

  /**
   * key长度异常
   */
  public static String getKeyLengthErr(String value) {
    return ERR_HEAD + getSubString(value) + KEY_LENGTH_ERR;
  }

  /**
   * value长度异常
   */
  public static String getValueLengthErr(String value) {
    return ERR_HEAD + getSubString(value) + VALUE_LENGTH_ERR;
  }

  /**
   * 命名异常
   */
  public static String getNamingErr(String value) {
    return BRACKETS + getSubString(value) + NAMING_ERROR;
  }

  /**
   * 保留字段异常
   */
  public static String getReservedKeywordsErr(String value) {
    return BRACKETS + getSubString(value) + RESERVED_KEYWORDS_ERR;
  }

  /**
   * 超长字段截取
   */
  public static String getSubString(String value) {
    if (!Utils.isEmpty(value)) {
      if (value.length() > 10) {
        value = value.substring(0, 10) + "....";
      }
    }
    return value;
  }
}
