package com.analysys;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2018/9/12 18:05
 * @Author: Wang-X-C
 */
public class AnalysysConfig {
  private String channel;
  private String baseUrl;
  private boolean autoProfile;
  private EncryptEnum encryptType;
  private String appKey;

  public String getAppKey() {
    return appKey;
  }

  public void setAppKey(String appKey) {
    this.appKey = appKey;
  }

  public String getChannel() {
    return channel;
  }

  public void setChannel(String channel) {
    this.channel = channel;
  }

  public String getBaseUrl() {
    return baseUrl;
  }

  public void setBaseUrl(String baseUrl) {
    this.baseUrl = baseUrl;
  }

  public boolean isAutoProfile() {
    return autoProfile;
  }

  public void setAutoProfile(boolean autoProfile) {
    this.autoProfile = autoProfile;
  }

  public EncryptEnum getEncryptType() {
    return encryptType;
  }

  public void setEncryptType(EncryptEnum encryptType) {
    this.encryptType = encryptType;
  }
}
