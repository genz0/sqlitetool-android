/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android.util;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import jp.gr.java_conf.sqlite_android.BuildConfig;

import android.os.Environment;
import android.util.Log;

/**
 * ログユーティリティ.
 */
public class LogUtils {

    /** Logger. */
    private static final Logger LOGGER;
    /** Log TAG. */
    private static final String TAG = "sqlitelib";

    /** Log FileName. */
    private static final String LOGFILE = Environment
            .getExternalStorageDirectory() + "/" + TAG + "_%g.txt";
    /** Log FileName Limit Size. */
    private static final int LOGFILE_LIMIT = 1024;
    /** Log FileName Rotation Count. */
    private static final int LOGFILE_COUNT = 2;

    static {
        LOGGER = Logger.getLogger(TAG);

        if (BuildConfig.DEBUG) {
            try {
                // 出力ファイルを追加モードで指定する
                FileHandler fh = new FileHandler(LOGFILE, LOGFILE_LIMIT,
                        LOGFILE_COUNT, true);
                // 出力フォーマットを指定する
                fh.setFormatter(new SimpleFormatter());
                LOGGER.addHandler(fh);
            } catch (IOException e) {
                Log.e(TAG, "LogfileOutput Error !!", e);
            }
        }

        LOGGER.setLevel(BuildConfig.DEBUG ? Level.ALL : Level.OFF);

    }

    /**
     * コンストラクタ.
     *
     * ユーティリティクラスのため、プライベート
     */
    private LogUtils() {

    }

    /**
     * Logger取得.
     *
     * @return Logger
     */
    public static Logger getLogger() {
        return LOGGER;
    }

    /**
     * エラーログ出力.
     *
     * @param message
     *            メッセージ
     * @param thrown
     *            例外
     */
    public static void e(String message, Throwable thrown) {
        LOGGER.log(Level.SEVERE, message, thrown);
    }

    // /**
    // * Logcat用ハンドラ.
    // *
    // */
    // private static class LogcatHandler extends Handler {
    //
    // /*
    // * (非 Javadoc)
    // *
    // * @see java.util.logging.Handler#close()
    // */
    // @Override
    // public void close() {
    // }
    //
    // /*
    // * (非 Javadoc)
    // *
    // * @see java.util.logging.Handler#flush()
    // */
    // @Override
    // public void flush() {
    // }
    //
    // /*
    // * (非 Javadoc)
    // *
    // * @see java.util.logging.Handler#publish(java.util.logging.LogRecord)
    // */
    // @Override
    // public void publish(LogRecord record) {
    // if (!isLoggable(record)) {
    // return;
    // }
    // if (getFormatter() == null) {
    // Log.d(TAG, record.getMessage());
    // } else {
    // String msg = getFormatter().format(record);
    // Log.d(TAG, msg);
    // }
    // }
    //
    // }

}
