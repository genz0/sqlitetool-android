/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android.result;

import java.nio.channels.SocketChannel;

import android.database.Cursor;

/**
 * 結果編集用インタフェース.
 */
public interface ResultWriter {

    /**
     * 初期化.
     *
     * @param socketChannel
     *            出力先
     */
    void initialize(SocketChannel socketChannel);

    /**
     * ヘッターの編集.
     *
     * @param cursor
     *            カーソル
     */
    void putHeader(Cursor cursor);

    /**
     * ボディーの編集.
     *
     * @param cursor カーソル
     */
    void putBody(Cursor cursor);

    /**
     * メッセージ出力.
     * 
     * @param message
     *            メッセージ
     */
    void putMessage(String message);

    /**
     * メッセージ出力.
     * 
     * @param message
     *            メッセージ
     * @param t
     *            例外
     */
    void putMessage(String message, Throwable t);

}
