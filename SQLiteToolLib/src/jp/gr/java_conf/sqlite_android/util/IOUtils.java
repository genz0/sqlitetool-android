/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android.util;

import java.io.Closeable;
import java.io.IOException;
import java.nio.channels.Selector;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * IOユーティリティ.
 */
public final class IOUtils {

    /**
     * コンストラクタ.
     *
     * ユーティリティクラスのため、プライベート
     */
    private IOUtils() {

    }

    /**
     * クローズ.
     *
     * Closeableのクローズ処理。例外を適切に処理する。
     *
     * @param target
     *            クローズ対象
     * @return true:クローズ成功 false:失敗
     */
    public static boolean close(Closeable target) {
        boolean result = true;
        if (target == null) {
            return result;
        }
        try {
            target.close();
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * クローズ.
     *
     * Selectorのクローズ処理。例外を適切に処理する。
     *
     * @param target
     *            クローズ対象
     * @return true:クローズ成功 false:失敗
     */
    public static boolean close(Selector target) {
        boolean result = true;
        if (target == null) {
            return result;
        }
        try {
            target.close();
        } catch (IOException e) {
            result = false;
        }
        return result;
    }

    /**
     * クローズ.
     *
     * Cursorのクローズ処理。例外を適切に処理する。
     *
     * @param target
     *            クローズ対象
     * @return true:クローズ成功 false:失敗
     */
    public static boolean close(Cursor target) {
        boolean result = true;
        if (target == null) {
            return result;
        }
        target.close();
        return result;
    }

    /**
     * クローズ.
     *
     * SQLiteDatabaseのクローズ処理。例外を適切に処理する。
     *
     * @param target
     *            クローズ対象
     * @return true:クローズ成功 false:失敗
     */
    public static boolean close(SQLiteDatabase target) {
        boolean result = true;
        if (target == null) {
            return result;
        }
        target.close();
        return result;
    }

}
