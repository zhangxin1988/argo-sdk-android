package com.analysys.strategy;

import android.content.Context;
import com.analysys.database.TableAllInfo;
import com.analysys.network.UploadManager;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 本地策略判断
 * @Version: 1.0
 * @Create: 2018/3/16 17:53
 * @Author: WXC
 */
class DefaultPolicy extends SendStatus {
  /**
   * 判断网络是否通
   * 判断是否为WiFi
   * 断db内事件条数是否大于设置条数
   * 当前时间减去上次发送时间是否大于设置的间隔时间
   *
   * @throws Exception
   */
  @Override
  public boolean isSend(Context context) throws Throwable {
    if (context == null) {
      return false;
    }
    if ("WIFI".equals(Utils.networkType(context))) {
      return true;
    } else {
      long count = PolicyManager.getEventCount(context);
      long dbCount = TableAllInfo.getInstance(context).selectCount();
      if (dbCount > count) {
        return true;
      }
      long nowTime = System.currentTimeMillis();
      long sendTime = Long.valueOf(String.valueOf(
          AnsSpUtils.getParam(context, Constants.SP_SEND_TIME, 0L)));
      long interval = PolicyManager.getIntervalTime(context);
      if (nowTime - sendTime > interval) {
        return true;
      } else {
        UploadManager.getInstance(context).sendMessageDelayed(interval);
        return false;
      }
    }
  }
}