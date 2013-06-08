/*
 * Copyright (C) 2013 genz0
 */
package jp.gr.java_conf.sqlite_android.util;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.nio.channels.SocketChannel;
import java.util.Set;

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
    /** リーダー. */
    private Reader mInput = null;
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
        while ((input = mInput.read()) != -1) {

            state = state.put(sb, (char) input);
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
         * 文字列編集.
         * 
         * 引数 input に指定された文字をどのようにするか.
         * 
         * @param sb
         *            編集する文字列バッファ.
         * @param input
         *            入力文字
         * @return 次の状態へのステータス
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
            public State put(StringBuffer sb, char input) {
                // 先頭にあるホワイトスペース及び改行を無視する
                if (sb.length() == 0 && PRE_IGNORE.contains(input)) {
                    return this;
                }
                sb.append(input);
                // それ以外が来たときはノーマルステータスへ
                return Normal;
            }

        },

        /** 通常状態. */
        Normal {
            @Override
            public State put(StringBuffer sb, char input) {
                sb.append(input);
                switch (input) {
                case QUOTE:
                    return Quoat;
                case EOL:
                    // 一行分の編集終了
                    return Finsh;
                default:
                    // 以外はステータスを変えない
                    return this;
                }
            }
        },

        /** クオート状態. */
        Quoat {
            @Override
            public State put(StringBuffer sb, char input) {
                sb.append(input);
                switch (input) {
                case QUOTE:
                    // 閉じられたのでノーマルへ
                    return Normal;
                default:
                    // 以外はステータスを変えない
                    return this;
                }
            }
        },

        /** 終了状態. */
        Finsh {
            @Override
            public State put(StringBuffer sb, char input) {
                return Finsh;
            }
        };

        /** クオート. */
        private static final char QUOTE = '\'';

        /** EOL. */
        private static final char EOL = ';';

        /**
         * 無視する文字.
         * 
         * 通常モードでは、以下は無視する.
         * <ul>
         * <li>改行(0x0d or 0x0a)</li>
         * <li>スペース(0x20)</li>
         * <li>セミコロン(0x3b)</li>
         * </ul>
         * 
         */
        private static final Set<Character> PRE_IGNORE = CollectionUtils
                .newSet('\n', '\r', ' ', ';');
    }
}
