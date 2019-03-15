package com.analysys;

import android.content.Context;
import com.analysys.process.DataProcess;
import java.util.List;
import java.util.Map;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: API
 * @Version: 1.0
 * @Create: 2018/2/2
 * @Author: Wang-X-C
 */
public class AnalysysAgent {
  /**
   * 初始化接口
   *
   * @param key 网站获取的Key
   * @param channel 应用下发的渠道
   */
  public static void init(Context context, String key, String channel, String baseUrl) {
    DataProcess.getInstance(context).init(key, channel, baseUrl, true, 0);
  }

  /**
   * 初始化接口
   *
   * @param key 网站获取的Key
   * @param channel 应用下发的渠道
   */
  public static void init(Context context, String key, String channel, String baseUrl, boolean autoProfile) {
    DataProcess.getInstance(context).init(key, channel, baseUrl, autoProfile, 0);
  }

  /**
   * 初始化接口
   *
   * @param config 初始化配置信息
   */
  public static void init(Context context, AnalysysConfig config) {
    DataProcess.getInstance(context).init(config);
  }

  /**
   * debug 模式
   *
   * @param debugMode 0、关闭debug模式
   * 1、打开Debug模式,但该模式下发送的数据仅用于调试，不计入平台数据统计
   * 2、打开Debug模式,该模式下发送的数据可计入平台数据统计
   */
  public static void setDebugMode(Context context, int debugMode) {
    DataProcess.getInstance(context).setDebug(debugMode);
  }

  /**
   * 设置上传数据地址
   *
   * @param url 数据上传地址,
   * 数据上传地址必须以"http://"或"https://"开头,必须携带端口号
   * 长度小于255字符
   */
  public static void setUploadURL(Context context, String url) {
    DataProcess.getInstance(context).setUploadURL(url);
  }

  /**
   * 设置上传间隔时间
   * debug 设置为 0 时,数据上传时间间隔,单位:秒
   *
   * @param flushInterval 间隔时间,time值大于1
   */
  public static void setIntervalTime(Context context, long flushInterval) {
    DataProcess.getInstance(context).setIntervalTime(flushInterval);
  }

  /**
   * 本地缓存上限值
   *
   * @param size 上传条数,count值大于1
   */
  public static void setMaxCacheSize(Context context, long size) {
    DataProcess.getInstance(context).setMaxCacheSize(size);
  }

  /**
   * 读取最大缓存条数
   */
  public static long getMaxCacheSize(Context context) {
    return DataProcess.getInstance(context).getMaxCacheSize();
  }

  /**
   * 设置事件最大上传条数
   * 当 debug 设置 0 时,数据累积条数大于设置条数后触发上传
   *
   * @param size 上传条数,size值大于1
   */
  public static void setMaxEventSize(Context context, long size) {
    DataProcess.getInstance(context).setMaxEventSize(size);
  }

  /**
   * 手动上传
   * 调用该接口立即上传数据
   */
  public static void flush(Context context) {
    DataProcess.getInstance(context).flush();
  }

  /**
   * 身份关联.
   * 新distinctID关联到原有originalID
   * originalID 原始id. 该变量将会映射到aliasID,
   *
   * @param aliasId 长度大于0且小于255字符
   * @param originalId 可以是现在使用也可以是历史使用的id,不局限于本地正使用的distinctId,
   * 若为空则使用本地的distinctId,长度大于0且小于255字符
   */
  public static void alias(Context context, String aliasId, String originalId) {
    DataProcess.getInstance(context).alias(aliasId, originalId);
  }

  /**
   * 用户ID设置
   *
   * @param distinctId 唯一身份标识,长度大于0且小于255字符
   */
  public static void identify(Context context, String distinctId) {
    DataProcess.getInstance(context).identify(distinctId);
  }

  /**
   * 清除本地设置
   */
  public static void reset(Context context) {
    DataProcess.getInstance(context).reset();
  }

  /**
   * 添加事件
   *
   * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ / $,不支持中文和乱码,最大长度99字符
   */
  public static void track(Context context, String eventName) {
    DataProcess.getInstance(context).track(eventName, null);
  }

  /**
   * 添加多属性事件
   *
   * @param eventName 事件名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于99字符
   * @param eventInfo 事件属性,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
   */
  public static void track(Context context, String eventName, Map<String, Object> eventInfo) {
    DataProcess.getInstance(context).track(eventName, eventInfo);
  }

  /**
   * 添加页面
   *
   * @param pageName 页面标识，为字符串,最大长度255字符
   */
  public static void pageView(Context context, String pageName) {
    DataProcess.getInstance(context).pageView(context, pageName, null);
  }

  /**
   * 添加页面信息
   *
   * @param pageName 页面标识,字符串,最大长度255字符
   * @param pageInfo 页面信息,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
   */
  public static void pageView(Context context, String pageName, Map<String, Object> pageInfo) {
    DataProcess.getInstance(context).pageView(context, pageName, pageInfo);
  }

  /**
   * 注册单个通用属性
   *
   * @param superPropertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,
   * 不支持中文和乱码,长度必须小于125字符
   * @param superPropertyValue 属性值,支持部分类型：String/Number/boolean/集合/数组;
   * 若为字符串,则最大长度255字符;
   * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
   */
  public static void registerSuperProperty(Context context, String superPropertyName, Object superPropertyValue) {
    DataProcess.getInstance(context).registerSuperProperty(superPropertyName, superPropertyValue);
  }

  /**
   * 注册多个通用属性
   *
   * @param superProperty 事件公共属性,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   * value支持部分类型：String/Number/boolean/集合/数组,若为字符串,最大长度255字符
   */
  public static void registerSuperProperties(Context context, Map<String, Object> superProperty) {
    DataProcess.getInstance(context).registerSuperProperties(superProperty);
  }

  /**
   * 删除单个通用属性
   *
   * @param superPropertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,
   * 不支持中文和乱码,长度必须小于125字符
   */
  public static void unRegisterSuperProperty(Context context, String superPropertyName) {
    DataProcess.getInstance(context).unregisterSuperProperty(superPropertyName);
  }

  /**
   * 清除所有通用属性
   */
  public static void clearSuperProperties(Context context) {
    DataProcess.getInstance(context).clearSuperProperty();
  }

  /**
   * 获取单个通用属性
   *
   * @param key 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   */
  public static Object getSuperProperty(Context context, String key) {
    return DataProcess.getInstance(context).getSuperProperty(key);
  }

  /**
   * 获取全部通用属性
   */
  public static Map<String, Object> getSuperProperties(Context context) {
    return DataProcess.getInstance(context).getSuperProperty();
  }

  /**
   * 设置用户单个属性,如果之前存在,则覆盖,否则,创建
   *
   * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   * @param propertyValue 属性的值,支持部分类型：String/Number/boolean/集合/数组;
   * 若为字符串,则最大长度255字符;
   * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
   */
  public static void profileSet(Context context, String propertyName, Object propertyValue) {
    DataProcess.getInstance(context).profileSet(propertyName, propertyValue);
  }

  /**
   * 设置用户的多个属性,如果之前存在,则覆盖,否则,创建
   *
   * @param property 属性列表,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   * value若为字符串,最大长度255字符
   */
  public static void profileSet(Context context, Map<String, Object> property) {
    DataProcess.getInstance(context).profileSet(property);
  }

  /**
   * 设置用户单个固有属性
   *
   * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   * @param propertyValue 属性的值,支持部分类型：String/Number/boolean/集合/数组;
   * 若为字符串,则最大长度255字符;
   * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
   */
  public static void profileSetOnce(Context context, String propertyName, Object propertyValue) {
    DataProcess.getInstance(context).profileSetOnce(propertyName, propertyValue);
  }

  /**
   * 设置用户多个固有属性
   * 与用户行为相关的 属性设置 如首次启动
   *
   * @param property 属性列表,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   * value若为字符串,最大长度255字符
   */
  public static void profileSetOnce(Context context, Map<String, Object> property) {
    DataProcess.getInstance(context).profileSetOnce(property);
  }

  /**
   * 设置用户属性的单个相对变化值(相对增加,减少)
   *
   * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   * @param propertyValue 属性的值,value支持类型:Number
   */
  public static void profileIncrement(Context context, String propertyName, Number
      propertyValue) {
    DataProcess.getInstance(context).profileIncrement(propertyName, propertyValue);
  }

  /**
   * 设置用户属性的多个相对变化值(相对增加,减少)
   * 如年龄等
   *
   * @param property 属性列表,最多包含100条,
   * 且key以字母或 $ 开头,包括大小写字母/数字/ _ / $,最大长度125字符,不支持乱码和中文,
   */
  public static void profileIncrement(Context context, Map<String, Number> property) {
    DataProcess.getInstance(context).profileIncrement(property);
  }

  /**
   * 列表类型属性增加单个元素
   */
  public static void profileAppend(Context context, String propertyName, Object propertyValue) {
    DataProcess.getInstance(context).profileAppend(propertyName, propertyValue);
  }

  /**
   * 列表类型属性增加多个元素
   *
   * @param propertyValue 新增的元素,属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   * 支持部分类型：String/Number/boolean/集合/数组;
   * 若为字符串,则最大长度255字符;
   * 若为数组或集合,则最多包含100条,且key约束条件与属性名称一致,value最大长度255字符
   */
  public static void profileAppend(Context context, Map<String, Object> propertyValue) {
    DataProcess.getInstance(context).profileAppend(propertyValue);
  }

  /**
   * 列表类型属性增加多个元素
   *
   * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   * @param propertyValue 新增的元素集合,支持类型:String/Number/boolean,
   * 集合内最多包含100条,若为字符串,最大长度255字符
   */
  public static void profileAppend(Context context, String propertyName, List<Object> propertyValue) {
    DataProcess.getInstance(context).profileAppend(propertyName, propertyValue);
  }

  /**
   * 删除单个用户属性
   *
   * @param propertyName 属性名称,以字母或$开头,可以包含大小写字母/数字/ _ /$,不支持中文和乱码,长度必须小于125字符
   */
  public static void profileUnset(Context context, String propertyName) {
    DataProcess.getInstance(context).profileUnset(propertyName);
  }

  /**
   * 清除所有用户的属性
   */
  public static void profileDelete(Context context) {
    DataProcess.getInstance(context).profileDelete();
  }

  /**
   * 自动采集页面信息
   *
   * @param isAuto 默认为true
   */
  public static void setAutomaticCollection(Context context, boolean isAuto) {
    DataProcess.getInstance(context).automaticCollection(isAuto);
  }

  /**
   * 获取自动采集页面信息开关状态
   */
  public static boolean getAutomaticCollection(Context context) {
    return DataProcess.getInstance(context).getAutomaticCollection();
  }

  /**
   * 忽略部分页面自动采集
   */
  public static void setIgnoredAutomaticCollectionActivities(Context context, List<String> activitiesName) {
    DataProcess.getInstance(context).setIgnoredAutomaticCollection(activitiesName);
  }

  /**
   * 获取忽略自动采集的页面
   */
  public static List<String> getIgnoredAutomaticCollection(Context context) {
    return DataProcess.getInstance(context).getIgnoredAutomaticCollection();
  }

  /**
   * 拦截监听 URL
   */
  public static void interceptUrl(Context context, String url, Object webView) {
    DataProcess.getInstance(context).interceptUrl(context, url, webView);
  }

  /**
   * 设置UA
   */
  public static void setHybridModel(Context context, Object webView) {
    DataProcess.getInstance(context).setHybridModel(webView);
  }

  /**
   * 还原 User-Agent 中的字符串
   */
  public static void resetHybridModel(Context context, Object webView) {
    DataProcess.getInstance(context).resetHybridModel(webView);
  }
}

