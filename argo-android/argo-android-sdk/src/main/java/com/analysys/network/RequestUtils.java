package com.analysys.network;

import android.text.TextUtils;
import com.analysys.utils.AnsLog;
import com.analysys.utils.Utils;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 网络请求
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: Wang-X-C
 */
public class RequestUtils {

  /**
   * HTTP
   */
  public static String postRequest(String url, String value, String spv, String reqt, String reqv) {
    String response = null;
    InputStream is = null;
    ByteArrayOutputStream bos = null;
    PrintWriter pw = null;
    try {
      response = "";
      URL urlP;
      HttpURLConnection connection;
      is = null;
      bos = null;
      byte[] buffer = new byte[1024];
      urlP = new URL(url);
      connection = (HttpURLConnection) urlP.openConnection();
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setConnectTimeout(20 * 1000);
      connection.setReadTimeout(20 * 1000);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("spv", spv);
      if ("1".equals(reqv)) {
        connection.setRequestProperty("reqt", reqt);
        connection.setRequestProperty("reqv", reqv);
      }
      pw = new PrintWriter(connection.getOutputStream());
      pw.print(value);
      pw.flush();
      pw.close();
      //获取数据
      if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
        is = connection.getInputStream();
        bos = new ByteArrayOutputStream();
        int len;
        while (-1 != (len = is.read(buffer))) {
          bos.write(buffer, 0, len);
        }
        bos.flush();
        return bos.toString("utf-8");
      } else if (HttpURLConnection.HTTP_ENTITY_TOO_LARGE == connection.getResponseCode()) {
        response = "413";
      }
    } catch (Throwable e) {
      AnsLog.e(e);
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (Throwable e) {

        }
      }
      if (bos != null) {
        try {
          bos.close();
        } catch (Throwable e) {
        }
      }
    }
    return response;
  }

  /**
   * HTTPS
   */
  public static String postRequestHttps(String path, String requestData, String spv, String reqt, String reqv) {
    HttpsURLConnection connection = null;
    OutputStream outputStream = null;
    try {
      URL url = new URL(path);
      connection = (HttpsURLConnection) url.openConnection();
      connection.setSSLSocketFactory(createSSL());
      connection.setDoInput(true);
      connection.setDoOutput(true);
      connection.setUseCaches(false);
      connection.setRequestMethod("POST");
      connection.setConnectTimeout(20 * 1000);
      connection.setReadTimeout(20 * 1000);
      connection.setRequestProperty("spv", spv);
      if ("1".equals(reqv)) {
        connection.setRequestProperty("reqt", reqt);
        connection.setRequestProperty("reqv", reqv);
      }
      connection.connect();
      if (!TextUtils.isEmpty(requestData)) {
        outputStream = connection.getOutputStream();
        outputStream.write(requestData.getBytes("UTF-8"));
      }
      if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
        return Utils.readStream(connection.getInputStream());
      } else {
        return null;
      }
    } catch (Throwable e) {
      AnsLog.e(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
      if (outputStream != null) {
        try {
          outputStream.close();
        } catch (Throwable e) {
        }
      }
    }
    return null;
  }

  private static SSLSocketFactory createSSL() {
    try {
      if (tm != null) {
        tm = new TrustManager[] { myX509TrustManager };
      }
      if (sslContext == null) {
        sslContext = SSLContext.getInstance("TLS");
      }
      sslContext.init(null, tm, null);
      if (ssf == null) {
        ssf = sslContext.getSocketFactory();
      }
    } catch (Throwable throwable) {

    }
    return ssf;
  }

  private static TrustManager myX509TrustManager = new X509TrustManager() {
    @Override
    public X509Certificate[] getAcceptedIssuers() {
      return null;
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
    }
  };

  private static TrustManager[] tm = null;
  private static SSLContext sslContext = null;
  private static SSLSocketFactory ssf = null;
}