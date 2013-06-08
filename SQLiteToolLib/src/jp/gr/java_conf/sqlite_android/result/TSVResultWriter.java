/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android.result;

import java.io.PrintWriter;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.sqlite_android.io.SocketChannelOutputStream;
import jp.gr.java_conf.sqlite_android.util.IOUtils;
import android.database.Cursor;

/**
 * DBの結果編集(TSV形式).
 */
public class TSVResultWriter implements ResultWriter {

    /** 出力用Writer. */
    private PrintWriter mWriter;

    /*
     * (非 Javadoc)
     * 
     * @see
     * jp.gr.java_conf.sqlite_android.editor.ResultEditor#initialize(java.nio
     * .channels.SocketChannel)
     */
    @Override
    public void create(SocketChannel socketChannel) {
        mWriter = new PrintWriter(new SocketChannelOutputStream(socketChannel),
                true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see jp.gr.java_conf.sqlite_android.result.ResultWriter#destroy()
     */
    @Override
    public void destroy() {
        IOUtils.close(mWriter);
    }

    /*
     * (非 Javadoc)
     * 
     * @see
     * jp.gr.java_conf.genzo.sqlitelib.ResultEditor#putHeader(android.database
     * .Cursor)
     */
    @Override
    public void putHeader(Cursor cursor) {

        StringBuffer sb = new StringBuffer();
        int colcnt = cursor.getColumnCount();
        for (int row = 0; row < colcnt; row++) {
            sb.append(cursor.getColumnName(row)).append("\t");
        }

        sb.deleteCharAt(sb.length() - 1);
        mWriter.println(sb);
    }

    /*
     * (非 Javadoc)
     * 
     * @see
     * jp.gr.java_conf.genzo.sqlitelib.ResultEditor#putBody(android.database
     * .Cursor)
     */
    @Override
    public void putBody(Cursor cursor) {
        StringBuffer sb = new StringBuffer();
        int colcnt = cursor.getColumnCount();
        for (int row = 0; row < colcnt; row++) {
            sb.append(cursor.getString(row)).append("\t");
        }

        sb.deleteCharAt(sb.length() - 1);
        mWriter.println(sb);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * jp.gr.java_conf.sqlite_android.result.ResultWriter#putFooter(android.
     * database.Cursor)
     */
    @Override
    public void putFooter(Cursor cursor) {
        mWriter.println("\n");
    }

    /*
     * (非 Javadoc)
     * 
     * @see
     * jp.gr.java_conf.sqlite_android.editor.ResultEditor#putMessage(java.lang
     * .String)
     */
    @Override
    public void putMessage(String message) {
        mWriter.println(message);

    }

    /*
     * (非 Javadoc)
     * 
     * @see
     * jp.gr.java_conf.sqlite_android.editor.ResultEditor#putMessage(java.lang
     * .String, java.lang.Throwable)
     */
    @Override
    public void putMessage(String message, Throwable t) {
        String output = message + ":" + t.getMessage();
        mWriter.println(output);
    }

}
