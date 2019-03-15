package com.analysys.utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by chris on 16/11/9.
 */
public class AnsThreadPool {
  private static final boolean DEBUG = true;

  public AnsThreadPool() {
  }

  private static class NetHodler {
    public static final ExecutorService NET_EXECUTORS = Executors.newSingleThreadExecutor();
  }

  private static class DBHodler {
    public static final ExecutorService DB_EXECUTORS = Executors.newFixedThreadPool(5);
  }

  public static ExecutorService getNetExecutor() {
    return NetHodler.NET_EXECUTORS;
  }

  public static ExecutorService getDBExecutor() {
    return DBHodler.DB_EXECUTORS;
  }

  private static void shoutDownNetExecutor() {
    if (!NetHodler.NET_EXECUTORS.isShutdown()) {
      NetHodler.NET_EXECUTORS.shutdown();
      try {
        NetHodler.NET_EXECUTORS.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        if (DEBUG) {
          AnsLog.e(e);
        }
      }
    }
  }

  private static void shoutDownDBExecutor() {
    if (!DBHodler.DB_EXECUTORS.isShutdown()) {
      DBHodler.DB_EXECUTORS.shutdown();
      try {
        DBHodler.DB_EXECUTORS.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        if (DEBUG) {
          AnsLog.e(e);
        }
      }
    }
  }

  public static void shoutdown() {
    shoutDownNetExecutor();
    shoutDownDBExecutor();
  }

  public static void pushDB(Runnable task) {
    if (task == null) {
      return;
    }
    try {
      if (!DBHodler.DB_EXECUTORS.isShutdown()) {
        getDBExecutor().execute(task);
      }
    } catch (Throwable e) {
      if (DEBUG) {
        AnsLog.e(e);
      }
    }
  }

  public static void pushNet(Runnable task) {
    if (task == null) {
      return;
    }
    try {
      if (!NetHodler.NET_EXECUTORS.isShutdown()) {
        getNetExecutor().execute(task);
      }
    } catch (Throwable e) {
      if (DEBUG) {
        AnsLog.e(e);
      }
    }
  }

  // 任务队列,为了最后的清理数据
  private static List<WeakReference<ScheduledFuture<?>>> queue = new ArrayList<WeakReference<ScheduledFuture<?>>>();
  private static ExecutorService executor = Executors.newSingleThreadExecutor();
  private static long MAX_WAIT_SECONDS = 5;

  public static void execute(Runnable command) {
    if (executor.isShutdown()) {
      executor = Executors.newSingleThreadExecutor();
    }

    executor.execute(command);
  }

  public static void waitForAsyncTask() {
    try {
      for (WeakReference<ScheduledFuture<?>> reference : queue) {
        ScheduledFuture<?> f = reference.get();
        if (f != null) {
          f.cancel(false);
        }
      }
      queue.clear();
      if (!executor.isShutdown()) {
        executor.shutdown();
      }
      executor.awaitTermination(MAX_WAIT_SECONDS, TimeUnit.SECONDS);
    } catch (Exception ignore) {
    }
  }
}
