/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android.util;


/**
 * 文字列列用ユーティリティ.
 */
public final class StringUtils {

    /** デフォルトエンコード. */
    public static final String ENCODE = "UTF-8";

    /** 空の文字列. */
    public static final String EMPTY = "";

    /**
     * コンストラクタ.
     *
     * ユーティリティクラスのため、プライベート
     */
    private StringUtils() {

    }


    /**
     * 文字列(改行付).
     * 
     * @param message
     *            テキスト
     * @return 改行付文字列
     */
    public static String addLF(String message) {
        return message + "\n";
    }
}
