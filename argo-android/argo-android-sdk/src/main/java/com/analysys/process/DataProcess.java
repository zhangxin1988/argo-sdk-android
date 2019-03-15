package com.analysys.process;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.analysys.AnalysysConfig;
import com.analysys.AutomaticAcquisition;
import com.analysys.hybrid.HybridBridge;
import com.analysys.network.UploadManager;
import com.analysys.utils.AnsLog;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.AnsThreadPool;
import com.analysys.utils.CheckUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.LogPrompt;
import com.analysys.utils.Utils;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据处理分发
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */
public class DataProcess {

  private Application app = null;
  private Context mContext = null;
  boolean realAutoProfile = false;
  // 最大缓存条数
  public long cacheMaxCount = 0;

  private static class Holder {
    public static final DataProcess INSTANCE = new DataProcess();
  }

  public static DataProcess getInstance(Context context) {
    Holder.INSTANCE.initContext(context);
    return Holder.INSTANCE;
  }

  private void initContext(Context context) {
    if (Utils.isEmpty(mContext)) {
      if (!Utils.isEmpty(context)) {
        mContext = context.getApplicationContext();
      }
    }
  }

  /**
   * 初始化接口 config
   */
  public void init(AnalysysConfig config) {
    int type = 0;
    if (!Utils.isEmpty(config.getEncryptType())) {
      type = config.getEncryptType().getType();
    }
    init(config.getAppKey(), config.getChannel(),
        config.getBaseUrl(), config.isAutoProfile(), type);
  }

  /**
   * 处理初始化接口方法 不调用初始化接口: 获取不到key/channel,页面自动采集失效,电池信息采集失效
   */
  public void init(final String key, final String channel,
      final String baseUrl, final boolean autoProfile, final int config) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext)) {
            Mould.initMould(mContext);
            saveKey(key);
            saveChannel(channel);
            if (Utils.isMainProcess(mContext)) {
              setBaseUrl(baseUrl);
              registerLifecycleCallbacks();
              realAutoProfile = autoProfile;
              AnsSpUtils.setParam(mContext, Constants.SP_AUTO_PROFILE, autoProfile);
              AnsSpUtils.setParam(mContext, Constants.SP_REQUEST_VERSION, config);
            }
            LogPrompt.showInitLog(true);
          } else {
            LogPrompt.showInitLog(false);
          }
        } catch (Throwable throwable) {
        }
      }
    });
  }

  /**
   * https 上传地址 设置
   */
  private void setBaseUrl(String baseUrl) {
    if (!Utils.isEmpty(baseUrl)) {
      String completeUrl = Constants.HTTPS + baseUrl + Constants.HTTPS_PORT + "/up";
      changeUrlResetUser(completeUrl);
      AnsSpUtils.setParam(mContext, Constants.SP_USER_URL, completeUrl);
    }
  }

  /**
   * Activity 回调注册
   */
  private void registerLifecycleCallbacks() {
    if (Build.VERSION.SDK_INT >= 14) {
      if (Utils.isEmpty(app)) {
        app = (Application) mContext.getApplicationContext();
        app.registerActivityLifecycleCallbacks(new AutomaticAcquisition());
      }
    } else {
      appStart(false);
    }
  }

  /**
   * 存储key
   * 使用缓存的key，与传入的key对比
   */
  private void saveKey(String key) {
    String appKey = getNewKey(key);
    String spKey = Utils.getAppKey(mContext);
    if (!Utils.isEmpty(appKey)) {
      AnsSpUtils.setParam(mContext, Constants.SP_APP_KEY, appKey);
      if (!Utils.isEmpty(spKey) && !appKey.equals(spKey)) {
        resetUserInfo();
      }
      LogPrompt.showKeyLog(true, key);
    } else {
      LogPrompt.showKeyLog(false, key);
    }
  }

  /**
   * 读取XML和init中的key，校验后返回
   */
  private String getNewKey(String key) {
    String xmlKey = Utils.getManifestKeyChannel(mContext, Constants.SP_APP_KEY);
    if (!Utils.isEmpty(key)) {
      if (!Utils.isEmpty(xmlKey)) {
        return xmlKey.equals(key) ? key : null;
      } else {
        return key;
      }
    } else {
      return xmlKey;
    }
  }

  /**
   * 存储channel
   */
  private void saveChannel(String channel) {
    String appChannel = getNewChannel(channel);
    if (!Utils.isEmpty(appChannel)) {
      AnsSpUtils.setParam(mContext, Constants.SP_CHANNEL, appChannel);
      LogPrompt.showChannelLog(true, channel);
    } else {
      LogPrompt.showChannelLog(false, channel);
    }
  }

  /**
   * 获取 xml channel和 init channel，优先xml
   */
  private String getNewChannel(String channel) {
    String xmlChannel = Utils.getManifestKeyChannel(mContext, Constants.SP_CHANNEL);
    if (Utils.isEmpty(xmlChannel)) {
      if (!Utils.isEmpty(channel)) {
        xmlChannel = channel;
      }
    }
    return xmlChannel;
  }

  /**
   * debug 信息处理
   */
  public void setDebug(final int debug) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && 0 <= debug && debug >= 2) {
            debugResetUserInfo(debug);
            AnsSpUtils.setParam(mContext, Constants.SP_USER_DEBUG, debug);
            if (debug != 0) {
              AnsLog.isShowLog = true;
            }
            LogPrompt.showLog(Constants.API_SDM, true);
          } else {
            LogPrompt.showLog(Constants.API_SDM, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * debug切换判断是否需要重置发送
   */
  private void debugResetUserInfo(int debugNum) {
    int debug = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_USER_DEBUG, 0)));
    if (debug == 1) {
      if (debugNum == 0 || debugNum == 2) {
        resetUserInfo();
      }
    }
  }

  /**
   * 安装后首次启动/应用启动
   */
  public void appStart(final boolean isFromBackground) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          HashMap<String, Object> startUpMap = new HashMap<String, Object>();
          startUpMap.put(Constants.DEV_IS_FROM_BACKGROUND, isFromBackground);
          JSONObject eventData = PerfectData.getInstance(mContext)
              .getEventData(Constants.EN_APP_START, null, startUpMap);
          boolean isFirst = Utils.isFirstStart(mContext);
          if (isFirst) {
            sendProfileSetOnce(0);
          }
          trackEvent(Constants.API_AS, Constants.EN_PAGE_VIEW, eventData);
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 应用关闭
   */
  public void appEnd(final long duration) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          Map<String, Object> endInfo = new HashMap<String, Object>();
          endInfo.put(Constants.DEV_DURATION, duration);
          JSONObject endData = PerfectData.getInstance(mContext)
              .getEventData(Constants.EN_APP_END, null, endInfo);
          trackEvent(Constants.API_AE, Constants.EN_APP_END, endData);
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 页面信息处理
   */
  public void pageView(final Context context, final String pageName, final Map<String, Object> pageMap) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(pageName)) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(Constants.EVENT_PAGE_NAME, pageName);
            setTittleUrl(context, map);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.EN_PAGE_VIEW, pageMap, map);
            trackEvent(Constants.API_PV, Constants.EN_PAGE_VIEW, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PV, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * hybrid 使用 pageView 方法
   */
  public void hybridPageView(final String pageName, final Map<String, Object> pageMap) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(pageMap)) {
            Map<String, Object> pageInfo = null;
            if (!Utils.isEmpty(pageName)) {
              pageInfo = new HashMap<String, Object>();
              pageInfo.put(Constants.EVENT_PAGE_NAME, pageName);
            }
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.EN_PAGE_VIEW, pageMap, pageInfo);
            trackEvent(Constants.API_PV, Constants.EN_PAGE_VIEW, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PV, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 页面信息处理
   */
  public void autoCollectPageView(final Context context) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext)) {
            Map<String, Object> pageInfo = new HashMap<String, Object>();
            setTittleUrl(context, pageInfo);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.EN_PAGE_VIEW, null, pageInfo);
            trackEvent(Constants.API_PV, Constants.EN_PAGE_VIEW, eventData);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * pageView 添加URL 和 Tittle
   */
  private void setTittleUrl(Context context, Map<String, Object> pageInfo) {
    try {
      if (context != null && context instanceof Activity) {
        Activity activity = (Activity) context;
        if (pageInfo.get(Constants.PAGE_URL) == null) {
          pageInfo.put(Constants.PAGE_URL, activity.getClass().getCanonicalName());
        }
        if (pageInfo.get(Constants.PAGE_TITLE) == null) {
          pageInfo.put(Constants.PAGE_TITLE, activity.getTitle());
        }
      }
    } catch (Throwable e) {
    }
  }

  /**
   * 多个事件信息处理
   */
  public void track(final String eventName, final Map<String, Object> eventInfo) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(eventName)) {
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.EN_TRACK, eventInfo, null, eventName);
            trackEvent(Constants.API_TC, eventName, eventData);
          } else {
            LogPrompt.showLog(Constants.API_TC, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * distinct id 存储
   */
  public void identify(final String distinctId) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(distinctId)) {
            AnsSpUtils.setParam(mContext, Constants.SP_DISTINCT_ID, distinctId);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * alias id
   */
  public void alias(final String aliasId, final String originalId) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          String original = originalId;
          if (Utils.isEmpty(original)) {
            original = getOriginalId();
          }
          Map<String, Object> aliasMap = new HashMap<String, Object>();
          aliasMap.put(Constants.EN_ORIGINAL_ID, original);
          JSONObject eventData = PerfectData.getInstance(mContext)
              .getEventData(Constants.EN_ALIAS, aliasMap, null);
          if (!Utils.isEmpty(eventData)) {
            AnsSpUtils.setParam(mContext, Constants.SP_ALIAS_ID, aliasId);
            AnsSpUtils.setParam(mContext, Constants.SP_IS_LOGIN, 1);
            trackEvent(Constants.API_ALIAS, Constants.EN_ALIAS, eventData);
            sendProfileSetOnce(0);
          } else {
            LogPrompt.showLog(Constants.API_ALIAS, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 获取 originalId，优先取distinctId,无值，取UUID
   */
  private String getOriginalId() {
    String id = null;
    if (Utils.isEmpty(id)) {
      id = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_DISTINCT_ID, ""));
    }
    if (Utils.isEmpty(id)) {
      id = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_UUID, ""));
    }
    return id;
  }

  /**
   * 首次安装后是否发送profile_set_once
   */
  private void sendProfileSetOnce(int type) {
    try {
      boolean autoProfile = Boolean.valueOf(String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_AUTO_PROFILE, true)));
      if (autoProfile) {
        Map<String, Object> profileInfo = new HashMap<String, Object>();
        switch (type) {
          case 0:
            profileInfo.put(Constants.DEV_FIRST_VISIT_TIME, getFirstStartTime());
            profileInfo.put(Constants.DEV_FIRST_VISIT_LANGUAGE, Locale.getDefault().getLanguage());
            break;
          case 1:
            profileInfo.put(Constants.DEV_RESET_TIME, Utils.getTime());
            break;
          default:
            break;
        }
        JSONObject eventData = PerfectData.getInstance(mContext)
            .getEventData(Constants.PROFILE_SET_ONCE, null, profileInfo);
        trackEvent(Constants.API_PSO, Constants.EN_PAGE_VIEW, eventData);
      }
    } catch (Throwable e) {

    }
  }

  /**
   * 获取首次启动时间
   */
  private String getFirstStartTime() {
    String time = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_FIRST_START_TIME, ""));
    if (Utils.isEmpty(time)) {
      time = Utils.getTime();
      AnsSpUtils.setParam(mContext, Constants.SP_FIRST_START_TIME, time);
    }
    return time;
  }

  /**
   * profile set json
   */
  public void profileSet(final Map<String, Object> profile) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(profile)) {
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_SET, profile, null);
            trackEvent(Constants.API_PS, Constants.PROFILE_SET, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PS, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profile set 键值对
   *
   * @param propertyKey key
   * @param propertyValue value
   */
  public void profileSet(final String propertyKey, final Object propertyValue) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(propertyKey) && !Utils.isEmpty(propertyValue)) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            propertyMap.put(propertyKey, propertyValue);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_SET, propertyMap, null);
            trackEvent(Constants.API_PS, Constants.PROFILE_SET, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PS, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profileSetOnce json
   */
  public void profileSetOnce(final Map<String, Object> profile) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(profile)) {
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_SET_ONCE, profile, null);
            trackEvent(Constants.API_PSO, Constants.PROFILE_SET_ONCE, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PSO, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profile set 键值对
   */
  public void profileSetOnce(final String profileKey, final Object profileValue) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(profileKey) && !Utils.isEmpty(profileValue)) {
            Map<String, Object> propertyMap = new HashMap<String, Object>();
            propertyMap.put(profileKey, profileValue);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_SET_ONCE, propertyMap, null);
            trackEvent(Constants.API_PSO, Constants.PROFILE_SET_ONCE, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PSO, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profile increment
   */
  public void profileIncrement(final Map<String, ? extends Number> profile) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(profile)) {
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_INCREMENT, profile, null);
            trackEvent(Constants.API_PI, Constants.PROFILE_INCREMENT, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PI, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profile increment
   */
  public void profileIncrement(final String profileKey, final Number profileValue) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(profileKey) && !Utils.isEmpty(profileValue)) {
            Map<String, Object> profileMap = new HashMap<String, Object>();
            profileMap.put(profileKey, profileValue);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_INCREMENT, profileMap, null);
            trackEvent(Constants.API_PI, Constants.PROFILE_INCREMENT, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PI, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * profile append
   */
  public void profileAppend(final String propertyName, final Object propertyValue) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(propertyName) && !Utils.isEmpty(propertyValue)) {
            Map<String, Object> properMap = new HashMap<String, Object>();
            properMap.put(propertyName, propertyValue);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_APPEND, properMap, null);
            trackEvent(Constants.API_PA, Constants.PROFILE_APPEND, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PA, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 给一个列表类型的Profile增加一个元素
   */
  public void profileAppend(final Map<String, Object> profile) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(profile)) {
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_APPEND, profile, null);
            trackEvent(Constants.API_PA, Constants.PROFILE_APPEND, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PA, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 给一个列表类型的Profile增加一个或多个元素
   */
  public void profileAppend(final String profileKey, final List<Object> profileValue) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && Utils.isEmpty(profileKey) && Utils.isEmpty(profileKey)) {
            Map<String, Object> profileMap = new HashMap<String, Object>();
            profileMap.put(profileKey, profileValue);
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_APPEND, profileMap, null);
            trackEvent(Constants.API_PA, Constants.PROFILE_APPEND, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PA, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 删除单个用户属性
   */
  public void profileUnset(final String propertyKey) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(propertyKey)) {
            Map<String, Object> profileMap = new HashMap<String, Object>();
            profileMap.put(propertyKey, "");
            JSONObject eventData = PerfectData.getInstance(mContext)
                .getEventData(Constants.PROFILE_UNSET, null, profileMap);
            trackEvent(Constants.API_PU, Constants.PROFILE_UNSET, eventData);
          } else {
            LogPrompt.showLog(Constants.API_PU, false);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 清除所有用户属性
   */
  public void profileDelete() {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          JSONObject eventData = PerfectData.getInstance(mContext)
              .getEventData(Constants.PROFILE_DELETE, null, null);
          trackEvent(Constants.API_PD, Constants.PROFILE_DELETE, eventData);
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 注册单条通用属性
   */
  public void registerSuperProperty(final String key, final Object value) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(key) && !Utils.isEmpty(value)) {
            Map<String, Object> property = new HashMap<String, Object>();
            property.put(key, value);
            if (CheckUtils.checkParameter(property)) {
              saveSuperProperty(property);
              LogPrompt.showLog(Constants.API_RSP, true);
              return;
            }
          }
          LogPrompt.showLog(Constants.API_RSP, false);
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 注册多条公共属性
   */
  public void registerSuperProperties(final Map<String, Object> property) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && CheckUtils.checkParameter(property)) {
            saveSuperProperty(property);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 存储通用属性
   */
  private void saveSuperProperty(Map<String, Object> superProperty) {
    try {
      if (!Utils.isEmpty(superProperty)) {
        String getSuperProperty = String.valueOf(AnsSpUtils.getParam(
            mContext, Constants.SP_SUPER_PROPERTY, ""));
        if (!Utils.isEmpty(getSuperProperty)) {
          Utils.mergeJson(new JSONObject(getSuperProperty), new JSONObject(superProperty));
        }
        AnsSpUtils.setParam(mContext,
            Constants.SP_SUPER_PROPERTY, String.valueOf(new JSONObject(superProperty)));
      }
    } catch (Throwable e) {
    }
  }

  /**
   * 用户获取super Property
   */
  public Map<String, Object> getSuperProperty() {
    try {
      if (!Utils.isEmpty(mContext)) {
        String superProperty = String.valueOf(
            AnsSpUtils.getParam(mContext, Constants.SP_SUPER_PROPERTY, ""));
        if (!Utils.isEmpty(superProperty)) {
          return Utils.jsonToMap(new JSONObject(superProperty));
        }
      }
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 用户获取超级属性
   */
  public Object getSuperProperty(String propertyKey) {
    try {
      if (!Utils.isEmpty(mContext) && !Utils.isEmpty(propertyKey)) {
        String superProperty = String.valueOf(
            AnsSpUtils.getParam(mContext, Constants.SP_SUPER_PROPERTY, ""));
        if (!Utils.isEmpty(superProperty)) {
          return new JSONObject(superProperty).opt(propertyKey);
        }
      }
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 删除超级属性
   */
  public void unregisterSuperProperty(final String superPropertyName) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(superPropertyName)) {
            String property = String.valueOf(AnsSpUtils.getParam
                (mContext, Constants.SP_SUPER_PROPERTY, ""));
            if (!Utils.isEmpty(property)) {
              JSONObject json = new JSONObject(property);
              json.remove(superPropertyName);
              AnsSpUtils.setParam(mContext, Constants.SP_SUPER_PROPERTY, String.valueOf(json));
            }
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 删除所有超级属性
   */
  public void clearSuperProperty() {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          String property = String.valueOf(
              AnsSpUtils.getParam(mContext, Constants.SP_SUPER_PROPERTY, ""));
          if (!Utils.isEmpty(property)) {
            AnsSpUtils.setParam(mContext, Constants.SP_SUPER_PROPERTY, "");
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 用户自定义上传间隔时间
   */
  public void setIntervalTime(final long time) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && 1 < time) {
            AnsSpUtils.setParam(mContext, Constants.SP_USER_INTERVAL_TIME, time * 1000);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 用户自定义上传条数
   */
  public void setMaxEventSize(final long count) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && 1 < count) {
            AnsSpUtils.setParam(mContext, Constants.SP_USER_EVENT_COUNT, count);
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 设置最大缓存条数
   */
  public void setMaxCacheSize(final long count) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) || 100 < count) {
            cacheMaxCount = count;
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 获取最大缓存条数
   */
  public long getMaxCacheSize() {
    long count = 0;
    try {
      if (Utils.isEmpty(mContext)) {
        return count;
      }
      count = cacheMaxCount;
      if (count < 1) {
        count = Constants.MAX_CACHE_COUNT;
      }
    } catch (Throwable e) {
    }
    return count;
  }

  /**
   * 清除本地所有id、超级属性、重新生成id
   */
  public void reset() {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          resetUserInfo();
          AnsSpUtils.setParam(mContext, Constants.SP_UUID, "");
          LogPrompt.showLog(Constants.API_RESET, true);
          sendProfileSetOnce(1);
        } catch (Throwable e) {
          LogPrompt.showLog(Constants.API_RESET, false);
        }
      }
    });
  }

  /**
   * reset 接口 重置所有属性
   */
  private void resetUserInfo() {
    // 重置首次访问
    AnsSpUtils.setParam(mContext, Constants.SP_FIRST_START_TIME, "");
    // 重置首日访问
    AnsSpUtils.setParam(mContext, Constants.DEV_IS_FIRST_DAY, "");
    // 重置 通用属性
    AnsSpUtils.setParam(mContext, Constants.SP_SUPER_PROPERTY, "");
    // 重置 alias id
    AnsSpUtils.setParam(mContext, Constants.SP_ALIAS_ID, "");
    // 重置identify
    AnsSpUtils.setParam(mContext, Constants.SP_DISTINCT_ID, "");
    // 修改 isLogin
    AnsSpUtils.setParam(mContext, Constants.SP_IS_LOGIN, 0);
  }

  /**
   * 存储url
   */
  public void setUploadURL(final String url) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext) && !Utils.isEmpty(url)) {
            String getUrl = "";
            if (url.startsWith(Constants.HTTP)) {
              getUrl = Utils.checkUrl(url, Constants.HTTP);
            } else if (url.startsWith(Constants.HTTPS)) {
              getUrl = Utils.checkUrl(url, Constants.HTTPS);
            } else {
              return;
            }
            if (!Utils.isEmpty(getUrl)) {
              String completeUrl = getUrl + "/up";
              changeUrlResetUser(completeUrl);
              AnsSpUtils.setParam(mContext, Constants.SP_USER_URL, completeUrl);
            }
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 修改URL重置用户属性
   */
  private void changeUrlResetUser(String url) {
    try {
      String spServiceUrl = String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_SERVICE_URL, ""));
      if (Utils.isEmpty(spServiceUrl)) {
        String spUserUrl = String.valueOf(
            AnsSpUtils.getParam(mContext, Constants.SP_USER_URL, ""));
        if (Utils.isEmpty(spUserUrl)) {
          return;
        }
        String urlHost = new URL(url).getHost();
        String userUrlHost = new URL(spUserUrl).getHost();
        if (!Utils.isEmpty(urlHost)
            && !Utils.isEmpty(userUrlHost)
            && !urlHost.equals(userUrlHost)) {
          resetUserInfo();
        }
      }
    } catch (Throwable e) {
    }
  }

  /**
   * 是否自动采集
   */
  public void automaticCollection(final boolean isAuto) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          AnsSpUtils.setParam(mContext, Constants.SP_IS_COLLECTION, isAuto);
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 获取自动采集开关状态
   */
  public boolean getAutomaticCollection() {
    boolean isAuto = true;
    try {
      isAuto = Boolean.valueOf(String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_IS_COLLECTION, true)));
    } catch (Throwable e) {
    }
    return isAuto;
  }

  /**
   * 忽略多个页面自动采集
   */
  public void setIgnoredAutomaticCollection(final List<String> activitiesName) {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          if (!Utils.isEmpty(mContext)) {
            if (Utils.isEmpty(activitiesName)) {
              AnsSpUtils.setParam(mContext, Constants.SP_IGNORED_COLLECTION, "");
              return;
            }
            String activities = String.valueOf(
                AnsSpUtils.getParam(mContext, Constants.SP_IGNORED_COLLECTION, ""));
            if (Utils.isEmpty(activities)) {
              String names = Utils.toString(activitiesName);
              AnsSpUtils.setParam(mContext, Constants.SP_IGNORED_COLLECTION, names);
            } else {
              List<String> nameSet = Utils.toList(activities);
              for (String name : activitiesName) {
                nameSet.add(name);
              }
              String names = Utils.toString(nameSet);
              AnsSpUtils.setParam(mContext, Constants.SP_IGNORED_COLLECTION, names);
            }
          }
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 获取忽略自动采集的页面
   */
  public List<String> getIgnoredAutomaticCollection() {
    List<String> pageList = null;
    if (!Utils.isEmpty(mContext)) {
      String activities = String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_IGNORED_COLLECTION, ""));
      if (!Utils.isEmpty(activities)) {
        pageList = Utils.toList(activities);
      }
    }
    return pageList;
  }

  /**
   * 用户调用上传接口
   */
  public void flush() {
    AnsThreadPool.execute(new Runnable() {
      @Override
      public void run() {
        try {
          UploadManager.getInstance(mContext).flushSendManager();
        } catch (Throwable e) {
        }
      }
    });
  }

  /**
   * 接口数据处理
   */
  private void trackEvent(String apiName, String eventName, JSONObject eventData) {
    try {
      if (!Utils.isEmpty(eventName) && !Utils.isEmpty(eventData)) {

        LogPrompt.showLog(apiName, true);

        if (Constants.EN_ALIAS.equals(eventName)) {
          UploadManager.getInstance(mContext).aliasSendManager(eventName, eventData);
        } else {
          UploadManager.getInstance(mContext).sendManager(eventName, eventData);
        }
      } else {
        LogPrompt.showLog(apiName, false);
      }
    } catch (Throwable e) {
    }
  }

  /**
   * 拦截监听 URL
   */
  public void interceptUrl(Context context, String url, Object view) {
    try {
      if (!Utils.isEmpty(context) && !Utils.isEmpty(url)) {
        String decodedURL = java.net.URLDecoder.decode(url, "UTF-8");
        if (decodedURL.startsWith("analysysagent")) {
          HybridBridge.getInstance(context).execute(decodedURL, view);
        }
      }
    } catch (Throwable e) {
    }
  }

  /**
   * UA 增加 AnalysysAgent/Hybrid 字段
   */
  public void setHybridModel(Object webView) {
    if (!Utils.isEmpty(webView)) {
      try {
        Method getSettings = webView.getClass().getMethod(GET_SETTINGS);
        Object webSettings = getSettings.invoke(webView);
        Method getUserAgentString = webSettings.getClass().getMethod(GET_USER_AGENT);
        String userAgent = (String) getUserAgentString.invoke(webSettings);
        Method setUserAgentString = webSettings.getClass().getMethod(SET_USER_AGENT, String.class);
        if (!Utils.isEmpty(userAgent)) {
          setUserAgentString.invoke(webSettings, userAgent + Constants.AAH);
        } else {
          setUserAgentString.invoke(webSettings, Constants.AAH);
        }
      } catch (Throwable e) {
      }
    }
  }

  /**
   * UA 删除 AnalysysAgent/Hybrid 字段
   */
  public void resetHybridModel(Object webView) {
    if (!Utils.isEmpty(webView)) {
      try {
        Method getSettings = webView.getClass().getMethod(GET_SETTINGS);
        Object webSettings = getSettings.invoke(webView);
        Method getUserAgentString = webSettings.getClass().getMethod(GET_USER_AGENT);
        String userAgent = (String) getUserAgentString.invoke(webSettings);
        Method setUserAgentString = webSettings.getClass().getMethod(SET_USER_AGENT, String.class);
        if (!Utils.isEmpty(userAgent) && (userAgent.indexOf(Constants.AAH) > -1)) {
          userAgent = userAgent.replace(Constants.AAH, "");
          setUserAgentString.invoke(webSettings, userAgent);
        }
      } catch (Throwable e) {
      }
    }
  }

  private final String GET_SETTINGS = "getSettings";
  private final String GET_USER_AGENT = "getUserAgentString";
  private final String SET_USER_AGENT = "setUserAgentString";
}
