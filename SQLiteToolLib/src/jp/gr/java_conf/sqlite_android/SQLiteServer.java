/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

import jp.gr.java_conf.sqlite_android.result.ResultWriter;
import jp.gr.java_conf.sqlite_android.result.TSVResultWriter;
import jp.gr.java_conf.sqlite_android.util.IOUtils;
import jp.gr.java_conf.sqlite_android.util.LogUtils;
import jp.gr.java_conf.sqlite_android.util.SQLTokenizer;
import jp.gr.java_conf.sqlite_android.util.StringUtils;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * クライアントと通信するクラス.
 * 
 * 回線の接続を維持する常時接続型のサーバ機能。
 */
public class SQLiteServer implements Runnable {

    /** Socketの情報. */
    private ServerSocketChannel mServerChannel = null;
    /** セレクタ. */
    private Selector mSelector = null;
    /** SQLiteOpenHelper. */
    private SQLiteOpenHelper mHelper = null;

    /**
     * 初期化.
     * 
     * サーバソケットをオープンし、接続待ちを行う。
     * 
     * @param context
     *            コンテキスト
     * @param dbName
     *            ファイル名
     * @param port
     *            初期化するポート番号
     * 
     * @return true:処理成功 false:処理失敗
     * @throws IOException
     *             初期化失敗
     */
    public boolean initialize(Context context, String dbName, int port)
            throws IOException {

        boolean result = true;

        // 初期化ずみ
        if (mServerChannel != null) {
            return result;
        }

        mHelper = new SQLiteHelper(context, dbName);

        // ソケットチャネルを生成・設定
        mServerChannel = ServerSocketChannel.open();
        mServerChannel.socket().setReuseAddress(true);
        mServerChannel.socket().bind(new InetSocketAddress(port));
        mServerChannel.configureBlocking(false);
        mSelector = Selector.open();
        mServerChannel.register(mSelector, SelectionKey.OP_ACCEPT);

        return result;
    }

    /**
     * サーバ起動.
     */
    public void startServer() {
        LogUtils.getLogger().info("StartServer!!");
        new Thread(this, "SQLiteServer").start();
    }

    /**
     * サーバ停止.
     */
    public void stopServer() {
        IOUtils.close(mSelector);
        IOUtils.close(mServerChannel);

        mSelector = null;
        mServerChannel = null;
    }

    /*
     * (非 Javadoc)
     * 
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {

        try {
            while (mSelector.select() > 0) {

                Set<SelectionKey> keys = mSelector.selectedKeys();
                Iterator<SelectionKey> it = keys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove(); // 選択されたキーを削除

                    // Acceptの場合
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key
                                .channel();
                        onAccept(ssc);
                        continue;
                    }

                    // 入力
                    if (key.isReadable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        onRecieve(channel);
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            LogUtils.getLogger().log(Level.SEVERE, "Selector#select", e);
        }
    }

    /**
     * Accept.
     * 
     * ノンブロッキング指定とRead指定
     * 
     * @param ssc
     *            ServerSocketChannel
     * @throws IOException
     *             入出力例外
     */
    private void onAccept(ServerSocketChannel ssc) throws IOException {
        SocketChannel sc = ssc.accept();
        sc.configureBlocking(false);

        // Selector に読み込みを登録
        sc.register(mSelector, SelectionKey.OP_READ);
        LogUtils.getLogger().info("### client connect. ->" + sc);
    }

    /**
     * データ受信.
     * 
     * 接続のあったチャネルより情報を読み込む
     * 
     * @param socketChannel
     *            SocketChannel
     */
    private void onRecieve(SocketChannel socketChannel) {

        SQLiteDatabase db = null;
        Cursor cursor = null;
        SQLTokenizer tokenizer = new SQLTokenizer(socketChannel);
        ResultWriter editor = new TSVResultWriter();
        try {

            editor.create(socketChannel);
            db = mHelper.getWritableDatabase();
            db.beginTransaction();

            String sql = null;

            while ((sql = tokenizer.next()) != null) {

                if (StringUtils.isEmpty(sql)) {
                    // 改行だけの場合
                    continue;
                }

                LogUtils.getLogger().info("### execute sql [" + sql + "]");
                editor.putMessage(sql);

                cursor = db.rawQuery(sql, null);

                boolean hasNext = cursor.moveToFirst();
                if (!hasNext) {
                    // 結果が無いタイプ
                    editor.putMessage("(no result!!)");
                    cursor.close();
                    continue;
                }

                // ヘッダー出力
                editor.putHeader(cursor);

                // ボディー出力
                while (hasNext) {
                    editor.putBody(cursor);
                    hasNext = cursor.moveToNext();
                }

                // フッター出力
                editor.putFooter(cursor);

                cursor.close();
            }

            // 全部成功したらcommit
            db.setTransactionSuccessful();

        } catch (Exception e) {
            LogUtils.getLogger().log(Level.SEVERE, "[Error !!]", e);
            editor.putMessage("[Error !!]", e);
        } finally {
            editor.destroy();

            if (db != null) {
                db.endTransaction();
            }
            IOUtils.close(cursor);
            IOUtils.close(db);
            // コネクト解除
            if (tokenizer.isExit()) {
                LogUtils.getLogger().info(
                        "### client disconnect. ->" + socketChannel);
                IOUtils.close(socketChannel);
            }
        }
    }

    /**
     * バージョン操作しないSQLiteOpenHelper.
     * 
     * 以下を何もしないオーバライドとすることで、単純にDB操作だけを行う。
     * <ul>
     * <li>onCreate</li>
     * <li>onUpgrade</li>
     * <li>onDowngrade</li>
     * </ul>
     * 
     */
    private static class SQLiteHelper extends SQLiteOpenHelper {

        /**
         * コンストラクタ.
         * 
         * @param context
         *            コンテキスト
         * @param dbName
         *            データベースファイル名
         */
        SQLiteHelper(Context context, String dbName) {
            super(context, dbName, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // nop
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // バージョンは気にしない
        }

        @Override
        public void onDowngrade(SQLiteDatabase db, int oldVersion,
                int newVersion) {
            // バージョンは気にしない
        }
    }
}