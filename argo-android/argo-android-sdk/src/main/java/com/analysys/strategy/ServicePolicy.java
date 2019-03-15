package com.analysys.strategy;

import android.content.Context;
import com.analysys.database.TableAllInfo;
import com.analysys.network.UploadManager;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 服务器策略判断
 * @Version: 1.0
 * @Create: 2018/3/16 17:53
 * @Author: WXC
 */
class ServicePolicy extends SendStatus {

  private Context mContext = null;

  /**
   * 服务器策略处理
   * 网络不通返回false
   * 判断debug模式是否打开
   * 策略为 0:智能发送,判断db内事件条数是否大于设置条数,当前时间减去上次发送时间是否大于设置的间隔时间
   * 策略为 1:实时发送,
   * 策略为 2:间隔发送,
   *
   * @throws Exception
   */
  @Override
  public boolean isSend(Context context) throws Throwable {
    if (context == null) {
      return false;
    }
    mContext = context;
    int serviceDebug = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_SERVICE_DEBUG, -1)));
    if (serviceDebug == 1 || serviceDebug == 2) {
      return true;
    }
    long policyNo = Long.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_POLICY_NO, -1L)));
    if (policyNo == 0) {
      if ("WIFI".equals(Utils.networkType(context))) {
        return true;
      } else {
        long eventCount = PolicyManager.getEventCount(mContext);
        long dbCount = TableAllInfo.getInstance(context).selectCount();
        //数据库size大于设置
        if (dbCount > eventCount) {
          return true;
        }
        long nowTime = System.currentTimeMillis();
        long sendTime = Long.valueOf(String.valueOf(
            AnsSpUtils.getParam(mContext, Constants.SP_SEND_TIME, 0L)));
        long intervalTime = PolicyManager.getIntervalTime(mContext);
        if (nowTime - sendTime > intervalTime) {
          return true;
        } else {
          UploadManager.getInstance(context).sendMessageDelayed(intervalTime);
          return false;
        }
      }
    } else if (policyNo == 1) {
      return true;
    } else if (policyNo == 2) {
      long intervalTime = PolicyManager.getIntervalTime(mContext);
      long nowTime = System.currentTimeMillis();
      long sendTime = Long.valueOf(String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_SEND_TIME, 0L)));
      if (nowTime - sendTime > intervalTime) {
        return true;
      } else {
        UploadManager.getInstance(context).sendMessageDelayed(intervalTime);
        return false;
      }
    }
    return false;
  }
}
