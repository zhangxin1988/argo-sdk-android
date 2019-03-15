package com.analysys.strategy;

import android.content.Context;
import com.analysys.database.TableAllInfo;
import com.analysys.network.UploadManager;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Constants;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 用户策略判断
 * @Version: 1.0
 * @Create: 2018/3/16 17:54
 * @Author: WXC
 */
class UserPolicy extends SendStatus {

  private Context mContext = null;

  /**
   * 用户策略处理
   * 判断用户设置debug模式为1或2时,实时上传.
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
    mContext = context;
    int debug = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_USER_DEBUG, -1)));
    if (debug == 1 || debug == 2) {
      return true;
    }
    long eventCount = PolicyManager.getEventCount(mContext);
    long dbCount = TableAllInfo.getInstance(context).selectCount();
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
}

