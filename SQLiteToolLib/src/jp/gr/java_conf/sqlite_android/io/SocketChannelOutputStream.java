/*
 * Copyright (C) 2013 genzo
 */
package jp.gr.java_conf.sqlite_android.io;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * チャネルアウトプットストリーム.
 *
 * SocketChannelをOutputStreamで出力。
 */
public class SocketChannelOutputStream extends OutputStream {

    /** ソースとなるチャネル. */
    private final SocketChannel mSourceChannel;

    /**
     * コンストラクタ.
     *
     * SocketChannelを指定下コンストラクタ.
     *
     * @param channel
     *            SocketChannel
     */
    public SocketChannelOutputStream(SocketChannel channel) {
        mSourceChannel = channel;
    }

    /*
     * (非 Javadoc)
     *
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int oneByte) throws IOException {
        byte[] wrappedByte = {
                (byte) oneByte,
        };
        write(wrappedByte);
    }

    /*
     * (非 Javadoc)
     *
     * @see java.io.OutputStream#write(byte[], int, int)
     */
    @Override
    public void write(byte[] source, int offset, int length) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(source, offset, length);
        int total = 0;
        while (total < length) {
            total += mSourceChannel.write(buffer);
        }
    }

    /*
     * (非 Javadoc)
     * 
     * @see java.io.OutputStream#close()
     */
    @Override
    public void close() throws IOException {
        super.close();
        // 何もしない
    }
}
