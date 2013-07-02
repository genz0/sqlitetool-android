/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;

import jp.gr.java_conf.sqlite_android.io.SocketChannelInputStream;

/**
 * SQL解析.
 * 
 * セミコロン「;」を終端に、文字列を解析する.<br/>
 * シングルコーテーション「'」ではテキスト(String)とみなし、そのまま通過させる.
 * 
 * @author matsuo
 * 
 */
public class SQLTokenizer {
    /** クオート. */
    private static final char QUOTE = '\'';
    /** セミコロン. */
    private static final char SEM = ';';
    /** CR. */
    private static final char CR = '\n';
    /** LF. */
    private static final char LF = '\r';
    /** LF. */
    private static final char SPACE = ' ';

    /** リーダー. */
    private Reader mInput;
    /** 終了判定フラグ. */
    private boolean mIsExit = true;

    /**
     * コンストラクタ.
     * 
     * SocketChannelを指定したコンストラクタ.
     * 
     * @param socketChannel
     *            SocketChannel
     */
    public SQLTokenizer(SocketChannel socketChannel) {
        try {
            mInput = new InputStreamReader(new SocketChannelInputStream(
                    socketChannel), StringUtils.ENCODE);
        } catch (UnsupportedEncodingException e) {
            ;// 絶対に来ない
        }

    }

    /**
     * SQL取得.
     * 
     * セミコロン「;」が来たら１つのSQLと判断し、文字列を返却する。
     * 
     * @return SQL 文字列。伝文が終了した場合null.
     * @throws IOException
     *             IO例外の発生
     */
    public String next() throws IOException {
        int input = -1;
        State state = QueryState.Init;
        StringBuffer sb = new StringBuffer();

        // ステートマシンでのループ処理
        while ((input = mInput.read()) != -1) {
            switch (input) {
            case QUOTE:
                state = state.quote(sb, (char) input);
                break;
            case SEM:
                state = state.sem(sb, (char) input);
                break;
            case CR:
            case LF:
                state = state.crlf(sb, (char) input);
                break;
            case SPACE:
                state = state.space(sb, (char) input);
                break;
            default:
                state = state.put(sb, (char) input);
                break;
            }

            if (state == QueryState.Finsh) {
                break;
            }
        }

        String result = null;
        boolean eof = (input == -1) && (sb.length() == 0);
        if (!eof) {
            result = new String(sb);
            // 通信開始から1文字も読めなかった場合、回線断と判断する。
            mIsExit = false;
        }

        return result;
    }

    /**
     * 通信終了判定.
     * 
     * @return true:通信の終了 false:回線接続中
     */
    public boolean isExit() {
        return mIsExit;
    }

    /**
     * SQL解析ステートマシンインタフェース.
     * 
     * @author matsuo
     * 
     */
    interface State {

        /**
         * quoteの処理.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態
         */
        State quote(StringBuffer sb, char input);

        /**
         * セミコロンの処理.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態
         */
        State sem(StringBuffer sb, char input);

        /**
         * crlfの処理.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態
         */
        State crlf(StringBuffer sb, char input);

        /**
         * spaceの処理.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態
         */
        State space(StringBuffer sb, char input);

        /**
         * その他の処理.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態
         */
        State put(StringBuffer sb, char input);

    }

    /**
     * SQL解析ステートマシンの実装.
     * 
     * @author matsuo
     * 
     */
    enum QueryState implements State {

        /** 初期状態. */
        Init {
            @Override
            public State sem(StringBuffer sb, char input) {
                // 無視
                return this;
            }

            @Override
            public State crlf(StringBuffer sb, char input) {
                // 無視
                return this;
            }

            @Override
            public State space(StringBuffer sb, char input) {
                // 無視
                return this;
            }
            @Override
            public State put(StringBuffer sb, char input) {
                super.put(sb, input);
                return Normal;
            }

        },

        /** 通常状態. */
        Normal,

        /** クオート状態. */
        Quoat {
            @Override
            public State quote(StringBuffer sb, char input) {
                super.quote(sb, input);
                // 閉じられたのでノーマルへ
                return Normal;
            }

            @Override
            public State sem(StringBuffer sb, char input) {
                super.quote(sb, input);
                // セミコロンが来ても状態を変えない
                return this;
            }
        },

        /** 終了状態. */
        Finsh;

        @Override
        public State put(StringBuffer sb, char input) {
            sb.append(input);
            return this;
        }

        @Override
        public State quote(StringBuffer sb, char input) {
            sb.append(input);
            return Quoat;
        }

        @Override
        public State crlf(StringBuffer sb, char input) {
            sb.append(input);
            return this;
        }

        @Override
        public State space(StringBuffer sb, char input) {
            sb.append(input);
            return this;
        }

        @Override
        public State sem(StringBuffer sb, char input) {
            sb.append(input);
            return Finsh;
        }
    }
}
