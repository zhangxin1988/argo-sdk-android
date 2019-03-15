package com.analysys.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import com.analysys.utils.Utils;

/**
 * @Copyright © 2018 EGuan Inc. All rights reserved.
 * @Description: openDb, CloseDb
 * @Version: 1.0
 * @Create: 2018/2/3 17:31
 * @Author: WXC
 */
class DBManage {

  private DBHelper dbHelper;
  private Context mContext;
  private SQLiteDatabase db;

  private DBManage() {
  }

  private static class Holder {
    private static final DBManage INSTANCE = new DBManage();
  }

  public static synchronized DBManage getInstance(Context context) {
    Holder.INSTANCE.initContext(context);
    Holder.INSTANCE.initDatabaseHelper(context);
    return Holder.INSTANCE;
  }

  private void initContext(Context context) {
    if (Utils.isEmpty(mContext)) {
      if (!Utils.isEmpty(context)) {
        mContext = context;
      }
    }
  }

  private void initDatabaseHelper(Context context) {
    if (Utils.isEmpty(dbHelper)) {
      dbHelper = new DBHelper(context);
    }
  }

  public synchronized SQLiteDatabase openDB() {
    db = dbHelper.getWritableDatabase();
    return db;
  }

  public synchronized void closeDB() {
    try {
      if (!Utils.isEmpty(db)) {
        db.close();
      }
    } finally {
      db = null;
    }
  }
}
