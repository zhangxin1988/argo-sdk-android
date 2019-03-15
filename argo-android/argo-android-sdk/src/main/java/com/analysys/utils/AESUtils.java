package com.analysys.utils;

import android.text.TextUtils;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class AESUtils {

  /**
   * 加密
   */
  public static String encrypt(byte[] content, byte[] rawPassword) {
    try {
      SecretKeySpec secretKeySpec = new SecretKeySpec(rawPassword, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
      byte[] result = cipher.doFinal(content);
      return toHex(result);
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * 解密
   *
   * @param content
   * @param rawPassword
   * @return
   */
  public static byte[] decrypt(String content, byte[] rawPassword) {
    try {
      byte[] contents = toBytes(content);
      SecretKeySpec secretKeySpec = new SecretKeySpec(rawPassword, "AES");
      Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
      byte[] result = cipher.doFinal(contents);
      return result;
    } catch (Throwable e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * 通过最初的rawKey获得子key
   */
  private static byte[] getRawKey(byte[] rawPassword) {
    try {
      KeyGenerator keygen = KeyGenerator.getInstance("AES");
      SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
      sr.setSeed(rawPassword);
      keygen.init(128, sr);
      SecretKey secretKey = keygen.generateKey();
      byte[] result = secretKey.getEncoded();
      return result;
    } catch (Throwable e) {
    }
    return null;
  }

  /**
   * to bytes
   */
  public static byte[] toBytes(String cipherString) {
    if (TextUtils.isEmpty(cipherString)) {
      return null;
    }
    int len = cipherString.length() / 2;
    byte[] result = new byte[len];
    for (int i = 0; i < len; i++) {
      result[i] = Integer.valueOf(cipherString.substring(2 * i, 2 * i + 2), 16).byteValue();
    }
    return result;
  }

  /**
   * to String
   */
  public static String toHex(byte[] bytes) {
    final char[] hexDigits = "0123456789ABCDEF".toCharArray();
    StringBuilder ret = new StringBuilder(bytes.length * 2);
    for (int i = 0; i < bytes.length; i++) {
      ret.append(hexDigits[(bytes[i] >> 4) & 0x0f]);
      ret.append(hexDigits[bytes[i] & 0x0f]);
    }
    return ret.toString();
  }
}
