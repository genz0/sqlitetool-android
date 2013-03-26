/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import jp.gr.java_conf.sqlite_android.result.ResultWriter;
import jp.gr.java_conf.sqlite_android.result.TSVResultWriter;
import jp.gr.java_conf.sqlite_android.util.CollectionUtils;
import jp.gr.java_conf.sqlite_android.util.IOUtils;
import jp.gr.java_conf.sqlite_android.util.LogUtils;
import jp.gr.java_conf.sqlite_android.util.StringUtils;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * クラインとと通信するクラス.
 *
 * いったん接続すると、切断されるまで回線を維持し続ける。
 */
public class SQLiteServer implements Runnable {

    /** Socketの情報. */
    private ServerSocketChannel mServerChannel = null;
    /** セレクタ. */
    private Selector mSelector = null;
    /** SQLiteOpenHelper. */
    private SQLiteOpenHelper mHelper = null;

    /**
     * DBの指定.
     *
     * 引数で指定されたファイル名で、SQLiteOpenHelperを生成する。
     *
     * @param context
     *            コンテキスト
     * @param dbName
     *            ファイル名
     */
    public void setDBName(Context context, String dbName) {
        mHelper = new SQLiteOpenHelper(context, dbName, null, 1) {
            @Override
            public void onCreate(SQLiteDatabase db) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion,
                    int newVersion) {
                // バージョンは気にしない
            }

            @Override
            public void onDowngrade(SQLiteDatabase db, int oldVersion,
                    int newVersion) {
                // バージョンは気にしない
            }
        };
    }

    /**
     * 初期化.
     *
     * サーバソケットをオープンし、接続待ちを行う。
     *
     * @param port
     *            初期化するポート番号
     * @return true:処理成功 false:処理失敗
     * @throws IOException
     *             初期化失敗
     */
    public boolean initialize(int port) throws IOException {

        boolean result = true;

        // 初期化ずみ
        if (mServerChannel != null) {
            return result;
        }

        // ソケットチャネルを生成・設定
        mServerChannel = ServerSocketChannel.open();
        mServerChannel.socket().setReuseAddress(true);
        mServerChannel.socket().bind(new InetSocketAddress(port));
        // ノンブロッキングモードに設定
        mServerChannel.configureBlocking(false);
        // セレクタの生成
        mSelector = Selector.open();
        // ソケットチャネルをセレクタに登録
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
            LogUtils.e("Selector#select", e);
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
        sc.configureBlocking(false); // 非同期に変更

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

            editor.initialize(socketChannel);
            db = mHelper.getWritableDatabase();
            db.beginTransaction();

            String sql = null;

            while ((sql = tokenizer.next()) != null) {

                if (StringUtils.EMPTY.equals(sql)) {
                    // 最後に改行だけみたいなパターンの時
                    continue;
                }

                LogUtils.getLogger().info("### execute dql [" + sql + "]");
                editor.putMessage("\n" + sql);

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

                cursor.close();
            }

            // 全部成功したらcommit
            db.setTransactionSuccessful();


        } catch (SQLException e) {
            editor.putMessage("[sql error!!]", e);
        } catch (IOException e) {
            editor.putMessage("[IOError !!]", e);
        } catch (Exception e) {
            editor.putMessage("[Error !!]", e);
        } finally {
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

    class SQLTokenizer {

        private Reader mInput = null;
        private boolean mIsExit = true;

        SQLTokenizer(SocketChannel socketChannel) {
            try {
                mInput = new InputStreamReader(new SocketChannelInputStream(
                        socketChannel), StringUtils.ENCODE);
            } catch (UnsupportedEncodingException e) {
                ;// 絶対に来ない
            }
        }

        String next() throws IOException {
            int input = -1;
            State state = NormalState.state();
            StringBuffer sb = new StringBuffer();
            while ((input = mInput.read()) != -1) {

                state = state.put(sb, (char) input);
                if (state == null) {
                    break;
                }
            }

            String result = null;
            boolean eof = (input == -1) && (sb.length() == 0);
            if (!eof) {
                result = new String(sb);
                mIsExit = false;
            }

            return result;
        }

        public boolean isExit() {
            return mIsExit;
        }

    }

    interface State {
        char QUOTE = '\'';
        char EOL = ';';

        State put(StringBuffer sb, char input);

    }

    static class NormalState implements State {
        static final NormalState STATE = new NormalState();
        static final Set<Character> PRE_IGNORE = CollectionUtils.newSet('\n',
                '\r', ' ', ';');

        @Override
        public State put(StringBuffer sb, char input) {

            if (sb.length() > 0 || !PRE_IGNORE.contains(input)) {
                // 先頭にあるホワイトスペース及び改行を無視する
                sb.append(input);
            }

            if (input == QUOTE) {
                return QuoteState.state();
            }
            if (input == EOL) {
                // 一行分の編集終了
                return null;
            }
            return this;
        }

        static State state() {
            return STATE;
        }
    }

    static class QuoteState implements State {
        private static final QuoteState STATE = new QuoteState();

        private char mPreInput = Character.MIN_VALUE;

        @Override
        public State put(StringBuffer sb, char input) {
            sb.append(input);

            if (mPreInput == QUOTE) {
                // 2こ続きならリセットしてQUOTEモードのまま
                mPreInput = Character.MIN_VALUE;
                return this;
            }
            mPreInput = input;

            if (input == QUOTE) {
                // QUOTEモード終了
                return NormalState.state();
            }

            // EOLが来てもQuoteのまま
            return this;
        }

        static State state() {
            STATE.mPreInput = Character.MIN_VALUE;
            return STATE;
        }

    }

}
