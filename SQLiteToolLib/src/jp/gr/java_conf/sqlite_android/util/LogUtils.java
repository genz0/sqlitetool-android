/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import jp.gr.java_conf.sqlite_android.BuildConfig;
import jp.gr.java_conf.sqlite_android.SQLiteServer;
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
        LOGGER = Logger.getLogger(SQLiteServer.class.getPackage().getName());

        if (BuildConfig.DEBUG) {
            try {
                // 出力ファイルを追加モードで指定する
                FileHandler handler = new FileHandler(LOGFILE, LOGFILE_LIMIT,
                        LOGFILE_COUNT, true);
                // 出力フォーマットを指定する
                handler.setFormatter(new LogFormatter());
                LOGGER.addHandler(handler);
            } catch (IOException e) {
                Log.e(TAG, "LogfileOutput Error !!", e);
            }
        }

        LOGGER.setLevel(BuildConfig.DEBUG ? Level.ALL : Level.WARNING);

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

    /**
     * ログのフォーマッタ.
     * 
     * @author genzo
     * 
     */
    static class LogFormatter extends Formatter {

        @Override
        public String format(LogRecord r) {
            StringBuilder sb = new StringBuilder(256);

            long millis = r.getMillis();
            String time = String.format("%tD %<tT.%<tL", millis);
            sb.append(time);
            sb.append(' ');
            sb.append(Thread.currentThread().getName());
            sb.append(' ');

            String className = r.getSourceClassName();
            String methodName = r.getSourceMethodName();
            sb.append(className != null ? className : "???");
            sb.append("#");
            sb.append(methodName != null ? methodName : "N/A");
            // sb.append('(').append(r.getSequenceNumber()).append(')');
            sb.append(' ');

            sb.append(formatMessage(r));
            sb.append('\n');

            Throwable throwable = r.getThrown();
            if (throwable != null) {
                throwable.printStackTrace(new PrintWriter(
                        new StringBuilderWriter(sb)));
            }
            return new String(sb);
        }
    }

    /**
     * StringBuilderのWriterラッパー.
     * 
     * @author genz0
     * 
     */
    static class StringBuilderWriter extends Writer {

        /** 対象のStringBuilder. */
        private final StringBuilder mBuffer;

        /**
         * コンストラクタ.
         * 
         * @param sb
         *            対象のStringBuilder
         */
        StringBuilderWriter(StringBuilder sb) {
            mBuffer = sb;
        }

        @Override
        public void close() throws IOException {
            // nop
        }

        @Override
        public void flush() throws IOException {
            // nop
        }

        @Override
        public void write(char[] buf, int offset, int count) throws IOException {
            mBuffer.append(buf, offset, count);
        }
    }
}
