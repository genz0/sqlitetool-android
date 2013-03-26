/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * バイト列用ユーティリティ.
 */
public final class CollectionUtils {

    /**
     * コンストラクタ.
     *
     * ユーティリティクラスのため、プライベート
     */
    private CollectionUtils() {

    }

    /**
     * 初期値付Set生成.
     * 
     * 読み取り専用でかつ、初期値のあるSETを生成する
     * 
     * @param <T>
     *            型
     * @param args
     *            初期値
     * @return 引数に指定のある項目を入れたSet
     */
    public static <T> Set<T> newSet(T... args) {
        HashSet<T> result = new HashSet<T>();
        for (T t : args) {
            result.add(t);
        }
        return Collections.unmodifiableSet(result);
    }


}
