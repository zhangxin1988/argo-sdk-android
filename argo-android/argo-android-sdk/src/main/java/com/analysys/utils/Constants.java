package com.analysys.utils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 常量类
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */

public class Constants {

  public static final String DEV_SDK_VERSION_NAME = "4.1.2";

  public static final String EN_PAGE_VIEW = "$pageview";
  public static final String EN_APP_START = "$startup";
  public static final String EN_APP_END = "$end";
  public static final String EN_ALIAS = "$alias";
  public static final String EN_ORIGINAL_ID = "$original_id";
  public static final String EN_TRACK = "$track";

  public static final String PROFILE = "$profile";
  public static final String PROFILE_SET = "$profile_set";
  public static final String PROFILE_UNSET = "$profile_unset";
  public static final String PROFILE_DELETE = "$profile_delete";
  public static final String PROFILE_APPEND = "$profile_append";
  public static final String PROFILE_SET_ONCE = "$profile_set_once";
  public static final String PROFILE_INCREMENT = "$profile_increment";

  public static final String PAGE_URL = "$url";
  public static final String PAGE_TITLE = "$title";

  public static final String EVENT_PAGE_NAME = "$pagename";

  public static final String X_WHO = "xwho";
  public static final String X_WHEN = "xwhen";
  public static final String X_APP_ID = "appid";
  public static final String X_CONTEXT = "xcontext";
  public static final String X_WHAT = "xwhat";

  public static final String SP_FIRST_START_TIME = "firstStartTime";
  public static final String SP_AUTO_PROFILE = "autoProfile";

  public static final String SP_CHANNEL = "appChannel";
  public static final String SP_APP_KEY = "appKey";
  public static final String SP_SEND_TIME = "uploadTime";
  public static final String SP_ALIAS_ID = "aliasId";
  public static final String SP_DISTINCT_ID = "distinctId";
  public static final String SP_UUID = "uuid";
  public static final String SP_SUPER_PROPERTY = "superProperty";
  public static final String SP_USER_URL = "userUrl";

  public static final String SP_USER_DEBUG = "userDebug";
  public static final String SP_USER_INTERVAL_TIME = "userIntervalTime";
  public static final String SP_USER_EVENT_COUNT = "userEventCount";
  public static final String SP_POLICY_NO = "policyNo";
  public static final String SP_SERVICE_EVENT_COUNT = "serviceEventCount";
  public static final String SP_SERVICE_INTERVAL_TIME = "serviceTimerInterval";
  public static final String SP_FAIL_COUNT = "failCount";
  public static final String SP_FAIL_TRY_DELAY = "failTryDelay";
  public static final String SP_SERVICE_DEBUG = "serviceDebug";
  public static final String SP_SERVICE_URL = "serviceUrl";
  public static final String SP_SERVICE_HASH = "serviceHash";
  public static final String SP_FAILURE_TIME = "failureTime";
  public static final String SP_FAILURE_COUNT = "failureCount";
  public static final String SP_IS_COLLECTION = "isCollection";
  public static final String SP_IGNORED_COLLECTION = "ignoredCollection";
  public static final String SP_IS_LOGIN = "isLogin";
  public static final String SP_REQUEST_VERSION = "requestVersion";
  public static final String SP_SESSION_ID = "getSessionId";
  public static final String SP_EVENT_TIME = "lastEventTime";
  public static final String SP_START_DAY = "startDay";
  public static final String SP_PAGE_END_TIME = "pageEndTime";

  public static final String SERVICE_CODE = "code";
  public static final String SERVICE_POLICY = "policy";
  public static final String SERVICE_POLICY_NO = "policyNo";
  public static final String SERVICE_EVENT_COUNT = "eventCount";
  public static final String SERVICE_TIMER_INTERVAL = "timerInterval";
  public static final String SERVICE_FAIL_COUNT = "failCount";
  public static final String SERVICE_FAIL_TRY_DELAY = "failTryDelay";
  public static final String SERVICE_DEBUG_MODE = "debugMode";
  public static final String SERVICE_SERVER_URL = "serverUrl";
  public static final String SERVICE_HASH = "hash";

  public static final String DEV_SYSTEM = "Android";
  public static final String DEV_IS_FIRST_DAY = "$is_first_day";
  public static final String DEV_IS_FROM_BACKGROUND = "$is_from_background";
  public static final String DEV_DURATION = "$duration";
  public static final String DEV_FIRST_VISIT_TIME = "$first_visit_time";
  public static final String DEV_RESET_TIME = "$reset_time";
  public static final String DEV_FIRST_VISIT_LANGUAGE = "$first_visit_language";
  public static final String DEV_ANALYSYS_CHANNEL = "ANALYSYS_CHANNEL";
  public static final String DEV_ANALYSYS_APPKEY = "ANALYSYS_APPKEY";

  public static final String AAH = " AnalysysAgent/Hybrid";

  /** 默认满足条数上传 */
  public static final long EVENT_COUNT = 10;
  /** 默认间隔时间 */
  public static final long INTERVAL_TIME = 15 * 1000;
  /** 失败重传次数 */
  public static final long FAILURE_COUNT = 3;
  /** 重传时间间隔 */
  public static final long FAILURE_INTERVAL_TIME = 30 * 1000;

  /** track 事件名称长度 */
  public static final long MAX_EVENT_NAME_LENGTH = 100;
  /** xContext内key值限制 */
  public static final long MAX_KEY_LENGTH = 125;
  /** xContext内value值限制 */
  public static final long MAX_VALUE_LENGTH = 255;
  /** id值限制 */
  public static final long MAX_ID_LENGTH = 255;
  /** xContext内条数限制 */
  public static final long MAX_X_CONTEXT_LENGTH = 300;
  /** 发送最大条数 */
  public static final long MAX_SEND_COUNT = 100;
  /** 数组集合限制 */
  public static final long MAX_ARRAY_SIZE = 100;
  /** 默认最大缓存条数 */
  public static final long MAX_CACHE_COUNT = 10000;
  /** 到达缓存上限后，继续存储，每次删除的条数 */
  public static final long DELETE_CACHE_COUNT = 10;
  /** 退出后台间隔时间 */
  public static final long BG_INTERVAL_TIME = 30 * 1000;

  public static final String SAVE_TYPE = "$Event";

  public static final String THREAD_NAME = "send_data_thread";

  public static final String PLATFORM = "Android";

  public static final String KEY_RULE = "^(^[$a-zA-Z][$a-zA-Z0-9_]{0,})$";

  /** 4.0.6版本 log */
  public static final String API_INIT = "init";
  public static final String API_PS = "profileSet";
  public static final String API_PSO = "profileSetOnce";
  public static final String API_PI = "profileIncrement";
  public static final String API_PA = "profileAppend";
  public static final String API_PU = "profileUnset";
  public static final String API_PD = "profileDelete";
  public static final String API_RSP = "registerSuperProperty";
  public static final String API_USP = "unRegisterSuperProperty";
  public static final String API_CSP = "clearSuperProperties";
  public static final String API_GSP = "getSuperProperty";
  public static final String API_RESET = "reset";

  /** 4.1.1 版本 log */
  public static final String API_SDM = "setDebugMode";
  public static final String API_SUU = "setUploadURL";
  public static final String API_TC = "track";
  public static final String API_PV = "pageView";
  public static final String API_ALIAS = "alias";
  public static final String API_IF = "identify";
  public static final String API_AS = "appStart";
  public static final String API_AE = "appEnd";

  public static final String API_SIT = "setIntervalTime";
  public static final String API_SMCS = "setMaxCacheSize";
  public static final String API_SMES = "setMaxEventSize";

  public static final String API_SAC = "setAutomaticCollection";
  public static final String API_IU = "interceptUrl";
  public static final String API_SHM = "setHybridModel";
  public static final String API_RHM = "resetHybridModel";

  public static final String HTTP = "http://";
  public static final String HTTPS = "https://";
  /**
   * 4.0.4以上使用该套端口
   */
  public static final String HTTP_PORT = ":8089";
  public static final String HTTPS_PORT = ":4089";

  public static final String UTM_SOURCE = "$utm_source";
  public static final String UTM_MEDIUM = "$utm_medium";
  public static final String UTM_CAMPAIGN = "$utm_campaign";
  public static final String UTM_CAMPAIGN_ID = "$campaign_id";
  public static final String UTM_CONTENT = "$utm_content";
  public static final String UTM_TERM = "$utm_term";

  /** 判断是否发送过startUp */
  public static boolean isSendAppStart = false;

  public static String requestTime = null;

  public static final String CHECK_PATH = "com.analysys.utils.CheckUtils";

  public static final String OUTER = "outer";
  public static final String VALUE = "value";
  public static final String VALUE_TYPE = "valueType";
  public static final String FUNC_LIST = "checkFuncList";
  public static final String METHOD_PATH = "com.analysys.utils.MethodUtils";
}

