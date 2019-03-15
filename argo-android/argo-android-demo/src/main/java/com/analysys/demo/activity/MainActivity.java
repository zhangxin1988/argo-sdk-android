package com.analysys.demo.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.analysys.AnalysysAgent;
import com.analysys.demo.utils.SpUtils;
import com.analysys.demo.utils.ToastUtils;
import com.analysys.demoplugin.R;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

public class MainActivity extends AppCompatActivity {

  String tag = "analysys";
  Context mContext = MainActivity.this;
  View scroller;
  long[] mHits = new long[3];
  ToggleButton automaticCollection;
  public String autoKey = "autoKey";
  TextView textViewKey, textViewDefaultUrl;
  EditText
      etUploadUrl, etSocketUrl, etConfigUrl;
  LinearLayout defaultLayout, uploadLayout,
      configLayout, socketLayout;

  //如果勾选了不再询问
  private static final int NOT_NOTICE = 2;
  //权限请求码
  private final int mRequestCode = 100;
  String[] permissions = new String[] {
      Manifest.permission.ACCESS_NETWORK_STATE,
      Manifest.permission.READ_PHONE_STATE,
      Manifest.permission.ACCESS_WIFI_STATE
  };
  // 存储未授予的权限
  List<String> mPermissionList = new ArrayList<>();

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    init();
    initPermission();
  }

  public void onClick(View v) {
    switch (v.getId()) {

      /* URL设置 */
      case R.id.button_save:
        boolean saveUrl = (boolean) SpUtils.getParam(mContext, "isVisualShow", false);
        if (textViewKey.getText() != null) {
          String key = textViewKey.getText().toString();
          if (TextUtils.isEmpty(key)) {
            return;
          }
          //SpUtils.setParam(mContext, "appKey", key);
        }
        if (etUploadUrl.getText() != null) {
          String url = etUploadUrl.getText().toString();
          AnalysysAgent.setUploadURL(mContext, url);
        }

        if (saveUrl) {
          if (etConfigUrl.getText() != null) {
            String url = etConfigUrl.getText().toString();
            //AnalysysAgent.setVisitorConfigURL(mContext, url);
          }
          if (etSocketUrl.getText() != null) {
            String url = etSocketUrl.getText().toString();
            //AnalysysAgent.setVisitorDebugURL(mContext, url);
          }
          String key = textViewKey.getText().toString();
          AnalysysAgent.init(mContext, key, "wandoujia", "");
        } else {
          if (textViewDefaultUrl.getText() != null && textViewKey.getText() != null) {
            try {
              String url = textViewDefaultUrl.getText().toString();
              String key = textViewKey.getText().toString();
              URL url1 = new URL(url);
              AnalysysAgent.init(mContext, key, "wandoujia", url1.getHost());
            } catch (Throwable e) {
            }
          }
        }
        break;

      /* 设置用户属性 */

      case R.id.singleProfileSet:
        //要统计分析使用邮箱登录的用户
        AnalysysAgent.profileSet(mContext, "Email", "yonghu@163.com");
        break;
      case R.id.moreProfileSet:
        //要统计用户的登录方式邮箱登录,微信登录
        Map<String, Object> profileSet = new HashMap<>();
        profileSet.put("Email", "yonghu@163.com");
        profileSet.put("WeChatID", "weixinhao");
        AnalysysAgent.profileSet(mContext, profileSet);
        break;
      case R.id.singleProfileSetOnce:
        //要统计用户的激活时间
        AnalysysAgent.profileSetOnce(mContext, "activationTime", "1521594686781");
        break;
      case R.id.moreProfileSetOnce:
        //要统计激活时间和首次登陆时间
        Map<String, Object> setOnceProfile = new HashMap<>();
        setOnceProfile.put("activationTime", "1521594686781");
        setOnceProfile.put("loginTime", "1521594726781");
        AnalysysAgent.profileSetOnce(mContext, setOnceProfile);
        break;
      case R.id.singleProfileIncrement:
        //用户年龄增加了一岁
        AnalysysAgent.profileIncrement(mContext, "age", 1);
        break;
      case R.id.moreProfileIncrement:
        //用户年龄增加了一岁积分增加了200
        Map<String, Number> incrementProfile = new HashMap<>();
        incrementProfile.put("age", 1);
        incrementProfile.put("integral", 200);
        AnalysysAgent.profileIncrement(mContext, incrementProfile);
        break;
      case R.id.singleProfileAppend:
        //添加用户爱好
        AnalysysAgent.profileAppend(mContext, "hobby", "Music");
        break;
      case R.id.moreProfileAppend:
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("hobby", "PlayBasketball");
        map.put("sports", "Run");
        AnalysysAgent.profileAppend(mContext, map);

        break;
      case R.id.listProfileAppend:
        List<String> list = new ArrayList<String>();
        list.add("PlayBasketball");
        list.add("music");
        AnalysysAgent.profileAppend(mContext, "hobby", list);
        break;
      case R.id.profileUnset:
        // 要删除已经设置的用户年龄这一属性,
        AnalysysAgent.profileUnset(mContext, "age");
        break;
      case R.id.profileDelete:
        //清除用户的所有属性
        AnalysysAgent.profileDelete(mContext);
        break;

      /*  ID设置  */

      case R.id.alias:
        //一班同学升学时，调换班级到三班，
        AnalysysAgent.alias(mContext, "sanban", "yiban");
        break;
      case R.id.identify:
        //淘宝店铺使用该功能时，只关注访客用户或店铺会员，不关注设备信息
        AnalysysAgent.identify(mContext, "identifyId");
        ToastUtils.show("不产生事件");
        break;
      case R.id.reset:
        //清除本地现有的用户属性,包括通用属性
        AnalysysAgent.reset(mContext);
        ToastUtils.show("不产生事件");
        break;

      /* 自动采集设置 */

      case R.id.automaticCollection:
        SpUtils.setParam(mContext, autoKey, automaticCollection.isChecked());
        AnalysysAgent.setAutomaticCollection(mContext, automaticCollection.isChecked());
        break;
      case R.id.moreIgnoredAutomaticCollection:
        List<String> arrayList = new ArrayList<String>();
        //arrayList.add("TopVisualMainActivity");
        arrayList.add("com.analysys.demo.activity.MainActivity");
        AnalysysAgent.setIgnoredAutomaticCollectionActivities(mContext, arrayList);
        ToastUtils.show("不产生事件");
        break;
      case R.id.getIgnoredAutomaticCollection:
        List<String> activityNames = AnalysysAgent.getIgnoredAutomaticCollection(mContext);
        if (activityNames != null) {
          for (String name : activityNames) {
            Log.i(tag, "忽略自动采集的页面名称：" + name);
          }
        }
        ToastUtils.show("不产生事件");
        break;
      case R.id.jumpSecondActivity:
        //Intent intent = new Intent(MainActivity.this, TestActivity.class);
        //startActivity(intent);
        break;

      /* 通用属性 */

      case R.id.singleRegisterSuperProperty:
        //购买一年腾讯会员，今年内只需设置一次即可
        AnalysysAgent.registerSuperProperty(mContext, "member", "VIP");
        ToastUtils.show("不产生事件");
        break;
      case R.id.moreRegisterSuperProperty:
        //购买了一年腾讯会员和设置了用户的年龄
        Map<String, Object> property = new HashMap<>();
        property.put("age", "20");
        property.put("member", "VIP");
        AnalysysAgent.registerSuperProperties(mContext, property);
        ToastUtils.show("不产生事件");
        break;
      case R.id.singleUnregisterSuperProperty:
        //删除设置的用户年龄属性
        AnalysysAgent.unRegisterSuperProperty(mContext, "age");
        ToastUtils.show("不产生事件");
        break;
      case R.id.clearSuperProperty:
        //清除所有已经设置的用户数属性
        AnalysysAgent.clearSuperProperties(mContext);
        ToastUtils.show("不产生事件");
        break;
      case R.id.getSingleSuperProperty:
        //查看已经设置的“member”的通用属性
        Object singleSuperProperty = AnalysysAgent.getSuperProperty(mContext, "member");
        if (singleSuperProperty != null) {
          Log.i(tag, "属性值：" + singleSuperProperty.toString());
        }
        ToastUtils.show("不产生事件");
        break;
      case R.id.getMapSuperProperty:
        //查看所有已经设置的通用属性
        Map<String, Object> getProperty = AnalysysAgent.getSuperProperties(mContext);
        String properties = "";
        if (getProperty != null) {
          for (Map.Entry<String, Object> entry : getProperty.entrySet()) {
            Log.i(tag, "属性:Key = " + entry.getKey() + ", Value = " + entry.getValue());
          }
          ToastUtils.show("不产生事件");
        }
        break;

      /* 上传设置 */

      case R.id.setURL:
        //设置URL数据上传地址
        AnalysysAgent.setUploadURL(mContext, "");
        ToastUtils.show("不产生事件");
        break;
      case R.id.setIntervalTime:
        //上传的时间间隔定为20秒上传一次
        AnalysysAgent.setIntervalTime(mContext, 20);
        ToastUtils.show("不产生事件");
        break;
      case R.id.setEventCount:
        //设置上传条数
        AnalysysAgent.setMaxEventSize(mContext, 20);
        ToastUtils.show("不产生事件");
        break;
      case R.id.flush:
        //需要立刻将所有数据上传
        AnalysysAgent.flush(mContext);
        break;
      case R.id.setMaxCacheSize:
        //设置本地数据缓存上限值为2000条
        AnalysysAgent.setMaxCacheSize(mContext, 2000);
        ToastUtils.show("不产生事件");
        break;

      /* 事件接口 */

      case R.id.singleTrack:
        //统计用户确认订单的事件
        AnalysysAgent.track(mContext, "confirmOrder");
        break;
      case R.id.multipleTrack:
        //用户购买某一商品需支付2000元，
        Map<String, Object> track = new HashMap<>();
        track.put("money", 2000);
        AnalysysAgent.track(mContext.getApplicationContext(), "payment", track);
        break;

      /* 页面接口 */

      case R.id.singlePageView:
        //服务正在开展某个活动,需要统计活动页面时
        AnalysysAgent.pageView(mContext, "活动页");
        break;
      case R.id.multiplePageView:
        //购买一部iPhone手机,手机价格为8000元
        Map<String, Object> page = new HashMap<>();
        page.put("commodityName", "iPhone");
        page.put("commodityPrice", 8000);
        AnalysysAgent.pageView(mContext, "商品页", page);
        break;


      /* 测试事件类型 */

      case R.id.jumpWebview:
        Intent intent3 = new Intent(MainActivity.this, WebViewDemo.class);
        startActivity(intent3);
        break;

      default:
        break;
    }
  }

  /**
   * 初始化
   */
  private void init() {

    textViewKey = (TextView) findViewById(R.id.textView_key);
    textViewDefaultUrl = (TextView) findViewById(R.id.textViewDefault_url);
    etUploadUrl = (EditText) findViewById(R.id.et_upload_url);
    etSocketUrl = (EditText) findViewById(R.id.et_socket_url);
    etConfigUrl = (EditText) findViewById(R.id.et_config_url);

    defaultLayout = (LinearLayout) findViewById(R.id.default_layout);
    uploadLayout = (LinearLayout) findViewById(R.id.upload_layout);
    configLayout = (LinearLayout) findViewById(R.id.config_layout);
    socketLayout = (LinearLayout) findViewById(R.id.socket_layout);

    automaticCollection = (ToggleButton) findViewById(R.id.automaticCollection);

    boolean isAuto = Boolean.valueOf(SpUtils.getParam(mContext, autoKey, true).toString());
    automaticCollection.setChecked(isAuto);

    scroller = findViewById(R.id.trigger_container);

    editSetUrl();
  }

  /**
   * editText set
   */
  private void editSetUrl() {

    String key = SpUtils.getParam(mContext, "appKey", "").toString();
    String uploadUrl = SpUtils.getParam(mContext, "userUrl", "").toString();
    String socketUrl = SpUtils.getParam(mContext, "debugVisualUrl", "").toString();
    String configUrl = SpUtils.getParam(mContext, "getStrategyUrl", "").toString();
    textViewKey.setText(key);
    textViewDefaultUrl.setText(subUrl(uploadUrl));
    etUploadUrl.setText(subUrl(uploadUrl));
    etSocketUrl.setText(subUrl(socketUrl));
    etConfigUrl.setText(subUrl(configUrl));
  }

  private String subUrl(String url) {
    try {
      if (TextUtils.isEmpty(url)) {
        return "";
      }
      if (url.startsWith("http://") || url.startsWith("https://")) {
        URL u = new URL(url);
        return u.getProtocol() + "://" + u.getHost() + ":" + u.getPort();
      }
      if (url.startsWith("ws://")) {
        url = url.replace("ws", "http");
        URL u = new URL(url);
        return "ws://" + u.getHost() + ":" + u.getPort();
      }
      if (url.startsWith("wss://")) {
        url = url.replace("wss", "http");
        URL u = new URL(url);
        return "wss://" + u.getHost() + ":" + u.getPort();
      }
    } catch (Throwable e) {

    }
    return "";
  }

  private void isVisibilityUrl() {
    defaultLayout.setVisibility(View.GONE);
    uploadLayout.setVisibility(View.VISIBLE);
    configLayout.setVisibility(View.VISIBLE);
    socketLayout.setVisibility(View.VISIBLE);
  }

  private void initPermission() {
    if (Build.VERSION.SDK_INT >= 23) {
      mPermissionList.clear();
      for (int i = 0; i < permissions.length; i++) {
        if (ContextCompat.checkSelfPermission(this, permissions[i]) != PERMISSION_GRANTED) {
          mPermissionList.add(permissions[i]);
        }
      }
      if (mPermissionList.size() > 0) {
        ActivityCompat.requestPermissions(this, permissions, mRequestCode);
      }
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 1) {
      for (int i = 0; i < permissions.length; i++) {
        if (grantResults[i] == PERMISSION_GRANTED) {
          Toast.makeText(this, "" + "权限" + permissions[i] + "申请成功", Toast.LENGTH_SHORT).show();
        }
      }
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (mRequestCode == NOT_NOTICE) {
      initPermission();
    }
  }
}
