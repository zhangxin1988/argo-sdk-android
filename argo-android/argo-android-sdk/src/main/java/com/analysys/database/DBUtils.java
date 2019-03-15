package com.analysys.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.analysys.utils.Utils;
import java.io.File;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 数据库表 校验
 * @Version: 1.0
 * @Create: 2018/3/15 07:54
 * @Author: WXC
 */
class DBUtils {

  /**
   * 数据库是否存在该表
   */
  public static boolean isTableExist(SQLiteDatabase db) {
    boolean result = false;
    Cursor cursor = null;
    try {
      final String sql = "select count(*) as c from sqlite_master where type ='table' and name ='"
          + DBConfig.TableAllInfo.TABLE_NAME.trim() + "' ";
      cursor = db.rawQuery(sql, null);
      if (cursor.moveToNext()) {
        int count = cursor.getInt(0);
        if (count > 0) {
          result = true;
        }
      }
    } catch (Throwable e) {
    } finally {
      if (!Utils.isEmpty(cursor)) {
        cursor.close();
      }
    }
    return result;
  }

  public static void deleteDBFile(String filePath) {
    if (!Utils.isEmpty(filePath)) {
      File result = new File(filePath);
      if (!Utils.isEmpty(result)) {
        if (result.exists()) {
          result.delete();
        }
      }
    }
  }
}
