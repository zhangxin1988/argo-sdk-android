package com.analysys.demo.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.analysys.AnalysysAgent;
import com.analysys.demoplugin.R;

public class WebViewDemo extends AppCompatActivity {
  private WebView mWebView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_web_view_demo);

    webview();
  }

  private void webview() {
    mWebView = (WebView) findViewById(R.id.wv_main);
    mWebView.loadUrl("file:///android_asset/index.html");
    mWebView.setWebViewClient(new MyWebviewClient());
    mWebView.getSettings().setJavaScriptEnabled(true);
    AnalysysAgent.setHybridModel(this, mWebView);
  }

  class MyWebviewClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
      Log.d("analysys.hybrid", "onPageFinished url:" + url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      AnalysysAgent.interceptUrl(WebViewDemo.this, url, view);
      return false;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mWebView.removeAllViews();
    mWebView.destroy();
    AnalysysAgent.resetHybridModel(this, mWebView);
  }
}
