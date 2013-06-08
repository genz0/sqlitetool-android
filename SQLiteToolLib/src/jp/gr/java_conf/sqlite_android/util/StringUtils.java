/*
 * Copyright (C) 2013 genz0
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
     * 空文字判定.
     * 
     * 空文字を判定する。nullも空文字扱い.
     * 
     * @param text
     *            対象となるテキスト
     * @return true:空文字 false：空文字ではない
     */
    public static boolean isEmpty(String text) {
        return text == null || EMPTY.equals(text);
    }

}
