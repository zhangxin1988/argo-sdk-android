package com.analysys.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.analysys.utils.AnsSpUtils;
import com.analysys.utils.Base64Utils;
import com.analysys.utils.Constants;
import com.analysys.utils.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 表操作
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
public class TableAllInfo {
  private Context mContext;

  public static class Holder {
    private static final TableAllInfo INSTANCE = new TableAllInfo();
  }

  public static TableAllInfo getInstance(Context context) {
    Holder.INSTANCE.initContext(context);
    return Holder.INSTANCE;
  }

  private void initContext(Context context) {
    if (Utils.isEmpty(mContext)) {
      if (!Utils.isEmpty(context)) {
        mContext = context;
      }
    }
  }

  /**
   * 存储数据
   */
  public synchronized void insert(String info, String type) {
    if (Utils.isEmpty(info)) {
      return;
    }
    String base64Info = Base64Utils.encode(info.getBytes());
    ContentValues values = new ContentValues();
    values.put(DBConfig.TableAllInfo.Column.INFO, base64Info);
    values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_SAVE);
    values.put(DBConfig.TableAllInfo.Column.TYPE, type);
    values.put(DBConfig.TableAllInfo.Column.INSERT_DATE, System.currentTimeMillis());
    try {
      if (Utils.isEmpty(mContext)) {
        return;
      }
      SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
      if (Utils.isEmpty(db)) {
        return;
      }
      DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
      db.insert(DBConfig.TableAllInfo.TABLE_NAME, null, values);
    } catch (Throwable e) {
    } finally {
      DBManage.getInstance(mContext).closeDB();
    }
  }

  /**
   * 查询上传
   */
  public synchronized JSONArray select() {
    JSONArray array = new JSONArray();
    Cursor cursor = null;
    try {
      if (Utils.isEmpty(mContext)) {
        return null;
      }
      SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
      if (Utils.isEmpty(db)) {
        return new JSONArray();
      }
      DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
      cursor = db.query(true, DBConfig.TableAllInfo.TABLE_NAME,
          new String[] {
              DBConfig.TableAllInfo.Column.INFO, DBConfig.TableAllInfo.Column.ID,
              DBConfig.TableAllInfo.Column.TYPE
          },
          null, null, null, null, DBConfig.TableAllInfo.Column.ID + " asc",
          "0," + Constants.MAX_SEND_COUNT);

      while (cursor.moveToNext()) {
        String info = cursor.getString(cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.INFO));
        int id = cursor.getInt(cursor.getColumnIndexOrThrow(DBConfig.TableAllInfo.Column.ID));
        ContentValues values = new ContentValues();
        values.put(DBConfig.TableAllInfo.Column.SIGN, DBConfig.Status.FLAG_UPLOADING);
        db.update(DBConfig.TableAllInfo.TABLE_NAME, values, DBConfig.TableAllInfo.Column.ID + "=?",
            new String[] { String.valueOf(id) });
        byte[] baseInfo = Base64Utils.decode(info);
        if (Utils.isEmpty(baseInfo)) {
          continue;
        }
        String base64Info = new String(baseInfo);
        JSONObject jsonInfo = new JSONObject(base64Info);
        array.put(jsonInfo);
      }
      return array;
    } catch (Throwable e) {
    } finally {
      if (cursor != null) {
        cursor.close();
      }
      DBManage.getInstance(mContext).closeDB();
    }
    return array;
  }

  /**
   * 查询条数
   */
  public synchronized long selectCount() {
    Cursor cursor = null;
    try {
      if (Utils.isEmpty(mContext)) {
        return 0;
      }
      SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
      final String sql = "select count(*) from " + DBConfig.TableAllInfo.TABLE_NAME;
      if (Utils.isEmpty(db)) {
        return 0;
      }
      DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
      cursor = db.rawQuery(sql, null);

      if (Utils.isEmpty(cursor)) {
        return 0;
      }
      cursor.moveToFirst();
      long count = cursor.getLong(0);
      return count;
    } catch (Throwable e) {
    } finally {
      if (Utils.isEmpty(cursor)) {
        cursor.close();
      }
      DBManage.getInstance(mContext).closeDB();
    }
    return 0;
  }

  /**
   * 删除数据
   */
  public synchronized void deleteData() {
    try {
      if (mContext == null) {
        return;
      }
      SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
      if (db == null) {
        return;
      }
      DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
      String whereClause = DBConfig.TableAllInfo.Column.SIGN + "=?";
      String[] whereArgs = { String.valueOf(DBConfig.Status.FLAG_UPLOADING) };
      db.delete(DBConfig.TableAllInfo.TABLE_NAME, whereClause, whereArgs);
    } catch (Throwable e) {
    } finally {
      DBManage.getInstance(mContext).closeDB();
    }
  }

  /**
   * 删除多条数据
   */
  public synchronized void delete() {
    try {
      if (mContext == null) {
        return;
      }
      SQLiteDatabase db = DBManage.getInstance(mContext).openDB();
      if (db == null) {
        return;
      }
      DBHelper.getInstance(mContext).createTable(DBConfig.TableAllInfo.CREATE_TABLE);
      final String sql = "delete from " + DBConfig.TableAllInfo.TABLE_NAME + " where id in(select "
          + DBConfig.TableAllInfo.Column.ID + " from " + DBConfig.TableAllInfo.TABLE_NAME + "limit "
          + Constants.DELETE_CACHE_COUNT + ")";
      db.execSQL(sql);
    } catch (Throwable e) {
    } finally {
      DBManage.getInstance(mContext).closeDB();
    }
  }
}
