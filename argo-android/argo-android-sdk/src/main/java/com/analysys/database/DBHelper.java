package com.analysys.database;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseCorruptException;
import android.database.sqlite.SQLiteOpenHelper;
import com.analysys.utils.Utils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: 建库建表
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBHelper extends SQLiteOpenHelper {
  private static final String DBNAME = "analysys.data";
  private static final int VERSION = 1;
  private static Context mContext;

  private static class Holder {
    private static final DBHelper INSTANCE = new DBHelper(mContext);
  }

  public static DBHelper getInstance(Context context) {
    if (Utils.isEmpty(mContext)) {
      mContext = context;
    }
    return Holder.INSTANCE;
  }

  public DBHelper(Context context) {
    super(context, DBNAME, null, VERSION);
    createTables();
  }

  @Override
  public void onCreate(SQLiteDatabase db) {
    db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

  }

  @SuppressLint("SdCardPath")
  private void createTables() {
    SQLiteDatabase db = null;
    try {
      db = getWritableDatabase();
      if (Utils.isEmpty(db)) {
        return;
      }
      if (!DBUtils.isTableExist(db)) {
        db.execSQL(DBConfig.TableAllInfo.CREATE_TABLE);
      }
    } catch (SQLiteDatabaseCorruptException e) {
      DBUtils.deleteDBFile("/data/data/" + mContext.getPackageName() + "/databases/" + DBNAME);
      createTables();
    } catch (Throwable e) {
      //AnsLog.e(e);
    }
  }

  /**
   * 建表
   */
  public void createTable(String createSQL) {
    try {
      SQLiteDatabase db = getWritableDatabase();
      if (!DBUtils.isTableExist(db)) {
        db.execSQL(createSQL);
      }
    } catch (Throwable e) {
      //AnsLog.e(e);
    }
  }
}
