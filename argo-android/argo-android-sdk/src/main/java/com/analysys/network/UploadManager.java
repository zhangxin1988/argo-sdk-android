package com.analysys.network;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.analysys.AnalysysAgent;
import com.analysys.database.TableAllInfo;
import com.analysys.strategy.PolicyManager;
import com.analysys.strategy.SendStatus;
import com.analysys.utils.AnsLog;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 上传管理
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */

public class UploadManager {

  private Context mContext;
  private sendDataHandler mHandler;

  private int constantlySend = 1;

  private String spv = "";
  private String reqv = "";

  private static class Holder {
    public static final UploadManager INSTANCE = new UploadManager();
  }

  public static UploadManager getInstance(Context context) {
    Holder.INSTANCE.initContext(context);
    return Holder.INSTANCE;
  }

  public UploadManager() {
    HandlerThread thread = new HandlerThread(Constants.THREAD_NAME, Thread.MIN_PRIORITY);
    thread.start();
    mHandler = new sendDataHandler(thread.getLooper());
  }

  private void initContext(Context context) {
    if (mContext == null) {
      if (context != null) {
        mContext = context;
      }
    }
  }

  /**
   * flush接口调用
   * SP_POLICY_NO 0=智能 1实时发送 2间隔发送
   */
  public void flushSendManager() throws Exception {

    if (Utils.isMainProcess(mContext)) {
      long servicePolicyNo = Long.valueOf(String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_POLICY_NO, -1L)));
      if (servicePolicyNo == -1 || servicePolicyNo == 1) {
        sendMessage();
      }
    } else {
      AnsLog.w("Send message failed, please send message in the main process.");
    }
  }

  /**
   * alias 消息实时发送
   *
   * @throws Exception
   */
  public void aliasSendManager(String type, JSONObject sendData) throws Exception {
    if (Utils.isEmpty(sendData)) {
      return;
    }
    TableAllInfo.getInstance(mContext).insert(sendData.toString(), type);
    if (Utils.isMainProcess(mContext)) {
      if (mHandler.hasMessages(constantlySend)) {
        mHandler.removeMessages(constantlySend);
      }
      sendMessage();
    } else {
      AnsLog.w("Send message failed, please send message in the main process.");
    }
  }

  /**
   * 判断 发送数据
   *
   * @throws Exception
   */
  public void sendManager(String type, JSONObject sendData) throws Throwable {

    if (Utils.isEmpty(sendData)) {
      return;
    }
    long maxCount = AnalysysAgent.getMaxCacheSize(mContext);
    long count = TableAllInfo.getInstance(mContext).selectCount();
    if (count >= maxCount) {
      TableAllInfo.getInstance(mContext).delete();
    }
    // 设置首次启动日期，用来做发送profileSetOne的标记
    Utils.setFirstDate(mContext);
    TableAllInfo.getInstance(mContext).insert(String.valueOf(sendData), type);
    if (Utils.isMainProcess(mContext)) {
      SendStatus sendStatus = PolicyManager.getPolicyType(mContext);
      boolean isSend = sendStatus.isSend(mContext);
      if (isSend) {
        if (mHandler.hasMessages(constantlySend)) {
          mHandler.removeMessages(constantlySend);
        }
        sendMessage();
      }
    } else {
      AnsLog.w("Send message failed, please send message in the main process");
    }
  }

  /**
   * 发送即时消息
   */
  public void sendMessage() {
    if (!mHandler.hasMessages(constantlySend)) {
      Message msg = Message.obtain();
      msg.what = constantlySend;
      mHandler.sendMessage(msg);
    }
  }

  /**
   * 发送delay消息
   */
  public void sendMessageDelayed(long time) {
    if (!mHandler.hasMessages(constantlySend)) {
      Message msg = Message.obtain();
      msg.what = constantlySend;
      mHandler.sendMessageDelayed(msg, time);
    }
  }

  /**
   * 处理数据压缩,上传和返回值解析
   */
  private class sendDataHandler extends Handler {
    private sendDataHandler(Looper looper) {
      super(looper);
    }

    @Override
    public void handleMessage(Message msg) {
      try {
        if (msg.what == constantlySend) {
          if (!Utils.isNetworkAvailable(mContext)) {
            AnsLog.w("No network！");
            return;
          }
          String url = getUrl();
          if (Utils.isEmpty(url)) {
            AnsLog.w("Please set up URL！");
            return;
          }
          JSONArray selectData = TableAllInfo.getInstance(mContext).select();
          if (!Utils.isEmpty(selectData)) {
            AnsLog.d("Send message to server:" + url + "\n data:  " + selectData);
            String encryptData = messageEncrypt(String.valueOf(selectData));
            String returnInfo = "";
            if (url.startsWith(Constants.HTTP)) {
              returnInfo = RequestUtils.postRequest(url, encryptData, spv, Constants.requestTime, reqv);
            } else if (url.startsWith(Constants.HTTPS)) {
              returnInfo = RequestUtils.postRequestHttps(url, encryptData, spv, Constants.requestTime, reqv);
            } else {
              return;
            }
            policyAnalysis(analysisStrategy(returnInfo));
          }
        }
      } catch (Throwable e) {
        AnsLog.e(e);
      }
    }
  }

  /**
   * 数据加密压缩编码或只压缩编码
   */
  private String messageEncrypt(String message) {
    try {
      if (Utils.isEmpty(spv)) {
        spv = Utils.getSpvInfo(mContext);
      }
      if (Utils.isEmpty(reqv)) {
        reqv = String.valueOf(AnsSpUtils.getParam(mContext,
            Constants.SP_REQUEST_VERSION, 0));
      }
      if ("1".equals(reqv)) {
        String encryptData = Utils.messageEncrypt(mContext, message);
        if (Utils.isEmpty(encryptData)) {
          reqv = null;
        } else {
          message = encryptData;
        }
      }
      return Utils.messageZip(message);
    } catch (Throwable throwable) {
    }
    return null;
  }

  /**
   * 返回值解密转json
   */
  private JSONObject analysisStrategy(String strategy) {
    try {
      if (Utils.isEmpty(strategy)) {
        return null;
      }
      strategy = Utils.messageUnzip(strategy);
      AnsLog.d("return code：" + strategy);
      JSONObject info = new JSONObject(strategy);
      return info;
    } catch (Throwable e) {
      String decompressInfo = Utils.messageUnzip(strategy);
      try {
        JSONObject decompressJson = new JSONObject(decompressInfo);
        return decompressJson;
      } catch (Throwable e1) {
        return null;
      }
    }
  }

  /**
   * 解析返回策略
   */
  public void policyAnalysis(JSONObject json) throws Exception {
    try {
      if (json != null) {
        int returnCode = json.optInt(Constants.SERVICE_CODE, -1);
        if (returnCode == 200) {
          AnsSpUtils.setParam(mContext, Constants.SP_FAILURE_COUNT, 0);
          deleteData();
          AnsSpUtils.setParam(mContext, Constants.SP_SEND_TIME, System.currentTimeMillis());
          AnsLog.d("Send message success.");
          return;
        } else {
          JSONObject policyJson = json.optJSONObject(Constants.SERVICE_POLICY);
          if (!Utils.isEmpty(policyJson)) {
            String hash = policyJson.optString(Constants.SERVICE_HASH);
            String serviceHash = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_SERVICE_HASH, ""));
            if (Utils.isEmpty(serviceHash) || !hash.equals(serviceHash)) {
              PolicyManager.analysisStrategy(mContext, policyJson);
            }
          }
          AnsLog.w(" Send message failed.");
          resetUpload();
        }
      } else {
        resetUpload();
      }
    } catch (Throwable e) {
      AnsLog.e(e);
      resetUpload();
    }
  }

  /**
   * 重传逻辑
   * 发送失败的次数是否大于设置的发送失败次数
   * 发送失败时间大于0 且 当前时间减上次上传失败的时间大于时间间隔立即发送,
   * 否则delay发送,delay时间范围内随机
   * 发送失败次数大于默认次数,清空重传次数,set失败时间点,delay发送时间为设置重传间隔时间
   *
   * @throws Exception
   */
  private synchronized void resetUpload() throws Exception {
    int failureCount = Integer.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_FAILURE_COUNT, 0)));
    long intervalTime = getResetUploadInterval();
    if (failureCount < getResetUploadCount()) {
      long failureTime = Long.valueOf(String.valueOf(
          AnsSpUtils.getParam(mContext, Constants.SP_FAILURE_TIME, -1L)));
      long nowTime = System.currentTimeMillis();
      if (failureTime > 0 && (nowTime - failureTime) > intervalTime) {
        sendMessage();
        AnsSpUtils.setParam(mContext, Constants.SP_FAILURE_TIME, -1L);
      } else {
        AnsSpUtils.setParam(mContext, Constants.SP_FAILURE_COUNT, failureCount + 1);
        long randomTime = Utils.getRandomNumb(10 * 1000, (int) Constants.FAILURE_INTERVAL_TIME);
        sendMessageDelayed(randomTime);
      }
    } else {
      AnsSpUtils.setParam(mContext, Constants.SP_FAILURE_COUNT, 0);
      AnsSpUtils.setParam(mContext, Constants.SP_FAILURE_TIME, System.currentTimeMillis());
      //            sendMessageDelayed(intervalTime);
    }
  }

  /**
   * 重传间隔时间
   *
   * @throws Exception
   */
  public long getResetUploadInterval() {
    long failDelayTime = Long.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_FAIL_TRY_DELAY, -1L)));
    if (failDelayTime != -1) {
      return failDelayTime;
    }
    return Constants.FAILURE_INTERVAL_TIME;
  }

  /**
   * 失败次数
   *
   * @throws Exception
   */
  public long getResetUploadCount() {
    long failCount = Long.valueOf(String.valueOf(
        AnsSpUtils.getParam(mContext, Constants.SP_FAIL_COUNT, -1L)));
    if (failCount != -1) {
      return failCount;
    } else {
      return Constants.FAILURE_COUNT;
    }
  }

  /**
   * 获取上传地址
   * 服务器下发 > 用户设置 > 默认设置
   *
   * @throws Exception
   */
  public String getUrl() {
    String url = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_SERVICE_URL, ""));
    if (!Utils.isEmpty(url)) {
      return url;
    }
    url = String.valueOf(AnsSpUtils.getParam(mContext, Constants.SP_USER_URL, ""));
    if (!Utils.isEmpty(url)) {
      return url;
    }
    return null;
  }

  /**
   * 上传成功后 清理数据库
   */
  public void deleteData() {
    TableAllInfo.getInstance(mContext).deleteData();
  }
}
