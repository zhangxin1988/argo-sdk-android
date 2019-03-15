package com.analysys.demo;

import android.app.Application;
import com.analysys.AnalysysAgent;
import com.analysys.AnalysysConfig;
import com.analysys.EncryptEnum;

/**
 * @Copyright © 2019 EGuan Inc. All rights reserved.
 * @Description: TODO
 * @Version: 1.0
 * @Create: 2019/1/5 14:49
 * @Author: Wang-X-C
 */
public class AnsApplication extends Application {

  private static Application mApplication = null;

  @Override public void onCreate() {
    super.onCreate();

    mApplication = this;
    //  设置 debug 模式，值：0、1、2
    AnalysysAgent.setDebugMode(this, 2);
    AnalysysConfig config = new AnalysysConfig();
    // 设置key
    config.setAppKey("956f010e0e6d7634");
    // 设置渠道
    config.setChannel("wandoujia");
    // 设置追踪新用户的首次属性
    config.setAutoProfile(true);
    // 设置使用AES加密
    config.setEncryptType(EncryptEnum.AES);
    // 调用初始接口
    AnalysysAgent.init(this, config);
    // 设置地址接口
     AnalysysAgent.setUploadURL(this, "https://arksdk.analysys.cn:4089");
     //AnalysysAgent.setUploadURL(this, "http://192.168.8.76:8089");
    // AnalysysAgent.setVisitorDebugURL(this, "ws://192.168.8.76:9091");
    // AnalysysAgent.setVisitorConfigURL(this, "http://192.168.8.76:8089");

  }
  public static Application getApp() {
    return mApplication;
  }
}
